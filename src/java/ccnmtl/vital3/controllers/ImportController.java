package ccnmtl.vital3.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.commands.ImportCommand;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.Assignment;
import ccnmtl.vital3.AssignmentMaterialAssoc;
import ccnmtl.vital3.CustomField;
import ccnmtl.vital3.CustomFieldValue;
import ccnmtl.vital3.Material;
import ccnmtl.vital3.Question;
import ccnmtl.vital3.QuestionMaterialAssoc;
import ccnmtl.vital3.Unit;
import ccnmtl.vital3.UnitMaterialAssoc;
import ccnmtl.vital3.VitalParticipant;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.Vital3Utils;

public class ImportController extends Vital3CommandController {
    
    // people must be at least TA to access this page.
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.TA_ACCESS;
    }
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
        ImportCommand command = (ImportCommand) commandObj;
        HashMap model = new HashMap();
        
        UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
        model.put("currentUser", currentUser);
        
        Set units = command.getUnits();
        VitalWorksite targetWorksite = command.getWorksite();
        VitalWorksite sourceWorksite = command.getSourceWorksite();
        model.put("worksite", targetWorksite);
        
        String mode = command.getMode();
        model.put("mode",mode);
        
        if (mode.equals("chooseSource")) {
            
            List worksites = null;
            if (userContextInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITES)) {
                // show all worksites for admins:
                worksites = ucm.findAllWorksites(false, true);
            } else {
                // show only worksites to which user has TA_ACCESS:
                worksites = new ArrayList();
                ucm.decorateUser(currentUser, true);
                Set participants = currentUser.getParticipants();
                Iterator participantsIt = participants.iterator();
                while (participantsIt.hasNext()) {
                    VitalParticipant participant = (VitalParticipant) participantsIt.next();
                    if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0) {
                        VitalWorksite worksite = participant.getWorksite();
                        worksites.add(worksite);
                    }
                }
                ucm.decorateWorksites(worksites, false, true);
            }
            
            // remove the target worksite from the list of potential source worksites:
            worksites.remove(targetWorksite);
            
            model.put("worksites", worksites);
            model.put("worksiteId", targetWorksite.getId());
            
            return new ModelAndView("import", model);
            
            //////////////////////////
        } else if (mode.equals("displayItems")) {
            
            // make sure the user has proper access to source worksite:
            ucm.decorateUser(currentUser, true);
            VitalParticipant participant = currentUser.getParticipantForWorksiteId(sourceWorksite.getId());
            if (participant == null || participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) < 0)
                return errorMAV(errors, "error.import.source.auth");
            
            // for all data structures here, I will use a two-element array.
            // Index 0 represents things corresponding to the source worksite. Index 1 for the target worksite.
            VitalWorksite[] worksites = new VitalWorksite[2];
            worksites[0] = sourceWorksite;
            worksites[1] = targetWorksite;
            
            List[] unitMapLists = new ArrayList[2];
            unitMapLists[0] = new ArrayList();
            unitMapLists[1] = new ArrayList();
            
            // Vars named $sourceUnits and $targetUnits are each a list of hashMaps, ordered by hashMap.unit.startDate (natural order of units)
            // Each hashMap has three keys: "assignments", "materials", and "unit".
            // The "unit" key maps to an instance of the unit.
            // The "assignments" key maps to a list of that unit's assignments, ordered by assignment.ordinalValue (sort using natural ordering).
            // The "materials" key maps to a list of that unit's materials ordered by material.title (sort using an omnicomparator).
            // This list will include all materials associated with the unit, assignments in the unit, or questions in those assignments.
            // If needed, there will be a unit id -1 which will include all materials that have NO associations to any units, assignments, or questions.
            
            OmniComparator materialTitleComparator = new OmniComparator(Material.class, "getTitle");
            
            // materials found to be associated with other things in the worksite will be removed from loose materials sets:
            List[] looseMaterialLists = new ArrayList[2];
            looseMaterialLists[0] = new ArrayList(sourceWorksite.getMaterials());
            looseMaterialLists[1] = new ArrayList(targetWorksite.getMaterials());
            
            // start with source, later switch to target (1)
            int sourceOrTarget = 0;
            while (sourceOrTarget < 2) {
                
                VitalWorksite worksite = worksites[sourceOrTarget];
                List looseMaterials = looseMaterialLists[sourceOrTarget];
                List unitMapList = unitMapLists[sourceOrTarget];
                
                Iterator unitIter = worksite.getUnits().iterator();
                while (unitIter.hasNext()) {
                    
                    Unit unit = (Unit) unitIter.next();
                    ArrayList assignments = new ArrayList(unit.getAssignments());
                    ArrayList materials = new ArrayList();
                    
                    // collect all the associated materials (union the sets):
                    Set questions = Vital3Utils.initCollections(Assignment.class, assignments, "questions", Question.class);
                    materials.addAll(unit.getMaterials());
                    Set tempMaterials = Vital3Utils.initM2MCollections(Assignment.class, assignments, Material.class);
                    tempMaterials.removeAll(materials);
                    materials.addAll(tempMaterials);
                    tempMaterials = Vital3Utils.initM2MCollections(Question.class, questions, Material.class);
                    tempMaterials.removeAll(materials);
                    materials.addAll(tempMaterials);
                    
                    // sort materials and assignments:
                    Collections.sort(assignments);
                    Collections.sort(materials, materialTitleComparator);
                    
                    // remove those materials from the loose set:
                    looseMaterials.removeAll(materials);
                    
                    // create the unit map and add it to the list:
                    unitMapList.add(makeUnitMap(assignments, materials, unit));
                    
                }
                
                if (looseMaterials.size() > 0) {
                    // create new unit, id -1, with loose materials
                    Unit looseMaterialsUnit = new Unit();
                    looseMaterialsUnit.setId(new Long(-1));
                    looseMaterialsUnit.setTitle("Loose Materials");
                    //Unit finalUnit = (Unit) ((Map) unitMapList.get(unitMapList.size()-1)).get("unit");
                    // set the start date so it will fall after the final unit... is this even needed?
                    //looseMaterialsUnit.setStartDate(new Date(finalUnit.getStartDate().getTime() + 10000));
                    // sort:
                    Collections.sort(looseMaterials, materialTitleComparator);
                    unitMapList.add(makeUnitMap(new ArrayList(), looseMaterials, looseMaterialsUnit));
                }
                
                sourceOrTarget++;
            }
            
            model.put("sourceUnits", unitMapLists[0]);
            model.put("targetUnits", unitMapLists[1]);
            
            model.put("worksiteId", targetWorksite.getId());
            model.put("sourceWorksiteId", sourceWorksite.getId());
            model.put("sourceWorksite", sourceWorksite);
            
            String message = request.getParameter("message");
            if (message != null) model.put("message", message);
            
            return new ModelAndView("import", model);
            
            /////////////////////////
        } else {
            // import
            
            
            // make sure the user has proper access to source worksite:
            ucm.decorateUser(currentUser, true);
            VitalParticipant participant = currentUser.getParticipantForWorksiteId(sourceWorksite.getId());
            if (participant == null || participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) < 0)
                return errorMAV(errors, "error.import.source.auth");
            
            if (units.size() == 0 && command.importLooseMaterials == false) {
                // there was nothing selected!
                return new ModelAndView(new RedirectView("import.smvc?mode=displayItems&sourceWorksiteId="+sourceWorksite.getId()+"&worksiteId="+targetWorksite.getId()+"&message=Nothing+was+selected"), null);
            }
            
            // get all assignments and questions in each unit:
            Set assignments = Vital3Utils.initCollections(Unit.class, units, "assignments", Assignment.class);
            Set questions = Vital3Utils.initCollections(Assignment.class, assignments, "questions", Question.class);
            
            // get all materials from units and assignments and questions:
            Set materials = Vital3Utils.initM2MCollections(Unit.class, units, Material.class);
            materials.addAll(Vital3Utils.initM2MCollections(Assignment.class, assignments, Material.class));
            materials.addAll(Vital3Utils.initM2MCollections(Question.class, questions, Material.class));
            
            // add loose materials if requested:
            if (command.importLooseMaterials) {
                materials.addAll(getLooseMaterials(sourceWorksite));
            }
            
            // get all custom fields and values:
            Set customFields = sourceWorksite.getCustomFields();
            Set customFieldValues = Vital3Utils.initCollections(Material.class, materials, "customFieldValues", CustomFieldValue.class);
            
            // ===== make copies of all units, assignments, and materials =====
            
            // this is how we will keep track of which new objects correspond to which old ones:
            // each map has old objects as keys and the new ones as values
            HashMap materialMap = new HashMap();
            HashMap unitMap = new HashMap();
            HashMap assignmentMap = new HashMap();
            HashMap questionMap = new HashMap();
            HashMap customFieldMap = new HashMap();
            HashMap customFieldValueMap = new HashMap();
            
            // hashSets for storing the new entities for insert (note newCFs may not contain all target CFs):
            Set newUnits = new HashSet();
            Set newAssignments = new HashSet();
            Set newQuestions = new HashSet();
            Set newMaterials = new HashSet();
            Set newCustomFields = new HashSet();
            Set newCustomFieldValues = new HashSet();
            Set newAMAs = new HashSet();
            Set newQMAs = new HashSet();
            Set newUMAs = new HashSet();
            
            // adjust the unit/assignment due/start/end dates by measuring the offset of the two terms and adding 
            // this offset to each date. Modified dates are set to "right now" for all new Materials
            
            RawUCMTerm oldTerm = sourceWorksite.getTerm();
            RawUCMTerm newTerm = targetWorksite.getTerm();
            
            // Big pain in the butt... need to use Calendar to do the Date offsetting.
            // afterwards, use private method "offsetDate" to create new Dates from old ones.
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(newTerm.getStartDate());
            int newMonth = calendar.get(Calendar.MONTH);
            int newYear = calendar.get(Calendar.YEAR);
            calendar.setTime(oldTerm.getStartDate());
            int monthOffset = newMonth - calendar.get(Calendar.MONTH);
            int yearOffset = newYear - calendar.get(Calendar.YEAR);
            
            // construct new units
            Iterator iter = units.iterator();
            while (iter.hasNext()) {
                Unit oldUnit = (Unit) iter.next();
                
                Date newStartDate = offsetDate(oldUnit.getStartDate(), calendar, monthOffset, yearOffset);
                Date newEndDate = offsetDate(oldUnit.getEndDate(), calendar, monthOffset, yearOffset);
                
                Unit newUnit = new Unit(targetWorksite, oldUnit.getDescription(), newEndDate, newStartDate, oldUnit.getTitle(), oldUnit.getVisibility().intValue());
                unitMap.put(oldUnit, newUnit);
                newUnits.add(newUnit);
            }
            
            // construct new assignments
            iter = assignments.iterator();
            while (iter.hasNext()) {
                Assignment oldAss = (Assignment) iter.next();
                
                Date oldDateDue = oldAss.getDateDue();
                Date newDateDue = null;
                if (oldDateDue != null) newDateDue = offsetDate(oldDateDue, calendar, monthOffset, yearOffset);
                Unit parentUnit = (Unit) unitMap.get(oldAss.getUnit());
                
                Assignment newAss = new Assignment(parentUnit, oldAss.getCustomType(), newDateDue, oldAss.getInstructions(), oldAss.getOrdinalValue().intValue(), oldAss.getTitle(), oldAss.getType());
                assignmentMap.put(oldAss, newAss);
                newAssignments.add(newAss);
            }
            
            // construct new questions
            iter = questions.iterator();
            while (iter.hasNext()) {
                Question oldQ = (Question) iter.next();
                
                Assignment parentAss = (Assignment) assignmentMap.get(oldQ.getAssignment());
                
                Question newQ = new Question(parentAss, oldQ.getOrdinalValue().intValue(), oldQ.getText());
                
                questionMap.put(oldQ, newQ);
                newQuestions.add(newQ);
            }
            
            
            // construct new materials and associations
            
            // first save references to the existing materials:
            Set existingMaterials = new HashSet(targetWorksite.getMaterials());
            
            iter = materials.iterator();
            while (iter.hasNext()) {
                Material oldMat = (Material) iter.next();
                
                Material newMat = new Material(targetWorksite, oldMat.getAccessLevel().intValue(), new Date(), oldMat.getText(), oldMat.getThumbUrl(), oldMat.getTitle(), oldMat.getType(), oldMat.getUrl());
                
                materialMap.put(oldMat, newMat);
                newMaterials.add(newMat);
                
                // iterate through each old assignment Association
                Iterator iter2 = oldMat.getAssignments().iterator();
                while (iter2.hasNext()) {
                    // get the old assignment, use as a key to get the new assignment. May not exist if we're not importing that assignment.
                    Assignment oldAss = (Assignment) iter2.next();
                    Assignment newAss = (Assignment) assignmentMap.get(oldAss);
                    if (newAss != null) {
                        // create a new AMA
                        AssignmentMaterialAssoc newAMA = new AssignmentMaterialAssoc(newAss, newMat);
                        newAMAs.add(newAMA);
                    }
                }
                // iterate through each question Association
                iter2 = oldMat.getQuestions().iterator();
                while (iter2.hasNext()) {
                    // get the old question, use as a key to get the new question. May not exist if we're not importing that question.
                    Question oldQ = (Question) iter2.next();
                    Question newQ = (Question) questionMap.get(oldQ);
                    if (newQ != null) {
                        // create a new QMA
                        QuestionMaterialAssoc newQMA = new QuestionMaterialAssoc(newMat, newQ);
                        newQMAs.add(newQMA);
                    }
                }
                // iterate through each unit Association
                iter2 = oldMat.getUnits().iterator();
                while (iter2.hasNext()) {
                    // get the old unit, use as a key to get the new unit. May not exist if we're not importing that unit.
                    Unit oldUnit = (Unit) iter2.next();
                    Unit newUnit = (Unit) unitMap.get(oldUnit);
                    if (newUnit != null) {
                        // create a new UMA
                        UnitMaterialAssoc newUMA = new UnitMaterialAssoc(newMat, newUnit);
                        newUMAs.add(newUMA);
                    }
                }
                
                
            }
            
            
            // construct new custom fields (possibly... re-use CFs if they have the same name as the old ones)
            // if there are any duplicate CF names in the source, make sure to create the right number of CFs in the target
            // NOTE: newCustomFields set will not contain references to re-used target CFs... Only brand new ones.
            // Also, CFVs for the existing target CFs will need to be constructed. We'll keep track of which ones with unusedTargetCFs:
            TreeSet unusedTargetCFs = new TreeSet(targetWorksite.getCustomFields());
            // baseOrdinalValue will be used as a base/offset for adding new custom field values:
            int baseOrdinalValue = 0;
            if (unusedTargetCFs.size() > 0) baseOrdinalValue = ((CustomField) unusedTargetCFs.last()).getOrdinalValue().intValue();
            
            iter = customFields.iterator();
            while (iter.hasNext()) {
                CustomField oldCF = (CustomField) iter.next();
                CustomField newCF = null;
                int ordinalValue = oldCF.getOrdinalValue().intValue();
                
                // try to find a CF in the target worksite with the same name:
                Iterator cfIter = targetWorksite.getCustomFields().iterator();
                while (cfIter.hasNext()) {
                    CustomField targetCF = (CustomField) cfIter.next();
                    if (targetCF.getName().equals(oldCF.getName())) {
                        newCF = targetCF;
                        break;
                    }
                }
                if (newCF != null) {
                    // see if this field has a unique name in the SOURCE worksite:
                    cfIter = sourceWorksite.getCustomFields().iterator();
                    int timesFound = 0;
                    while (cfIter.hasNext()) {
                        CustomField sourceCF = (CustomField) cfIter.next();
                        if (sourceCF.getName().equals(oldCF.getName())) timesFound++;
                    }
                    if (timesFound < 1) throw new RuntimeException("Field not found in source worksite? should be impossible.");
                    else if (timesFound > 1) {
                        // found duplicates. Do not re-use target CF or the CFVs will overwrite eachother.
                        newCF = null;
                    } else {
                        // we will re-use that CF. Remove it from the 'unused' set:
                        unusedTargetCFs.remove(newCF);
                        ordinalValue = newCF.getOrdinalValue().intValue();
                    }
                }
                if (newCF == null) {
                    // offset the ordinalValue (will also be used for CFVs) and create a new CF:
                    ordinalValue += baseOrdinalValue;
                    newCF = new CustomField(targetWorksite, oldCF.getName(), ordinalValue, oldCF.getVisibility().intValue());
                    newCustomFields.add(newCF);
                }
                customFieldMap.put(oldCF, newCF);
                
                // construct new cfvs for materials we're importing:
                cfIter = oldCF.getValues().iterator();
                while (cfIter.hasNext()) {
                    CustomFieldValue oldCFV = (CustomFieldValue) cfIter.next();
                    
                    Material parentMaterial = (Material) materialMap.get(oldCFV.getMaterial());
                    
                    if (parentMaterial != null) {
                        CustomFieldValue newCFV = new CustomFieldValue(newCF, parentMaterial, ordinalValue, oldCFV.getValue());
                        customFieldValueMap.put(oldCFV, newCFV);
                        newCustomFieldValues.add(newCFV);
                    }
                }
            }
            
            // construct new blank cfvs for unused existing cfs and new materials... 
            Iterator cfIter = unusedTargetCFs.iterator();
            while (cfIter.hasNext()) {
                
                CustomField existingCF = (CustomField) cfIter.next();
                
                iter = newMaterials.iterator();
                while (iter.hasNext()) {
                    
                    Material parentMaterial = (Material) iter.next();
                    CustomFieldValue newCFV = new CustomFieldValue(existingCF, parentMaterial, existingCF.getOrdinalValue().intValue(), "");
                    newCustomFieldValues.add(newCFV);
                }
            }
            // ...and for existing materials and new cfs
            cfIter = newCustomFields.iterator();
            while (cfIter.hasNext()) {
                
                CustomField newCF = (CustomField) cfIter.next();
                
                iter = existingMaterials.iterator();
                while (iter.hasNext()) {
                    
                    Material existingMaterial = (Material) iter.next();
                    CustomFieldValue newCFV = new CustomFieldValue(newCF, existingMaterial, newCF.getOrdinalValue().intValue(), "");
                    newCustomFieldValues.add(newCFV);
                }
            }
            
                        
            // ===== insert all new objects ===== 
            vital3DAO.saveCollection(Unit.class, newUnits);
            vital3DAO.saveCollection(Assignment.class, newAssignments);
            vital3DAO.saveCollection(Question.class, newQuestions);
            vital3DAO.saveCollection(Material.class, newMaterials);
            vital3DAO.saveCollection(CustomField.class, newCustomFields);
            vital3DAO.saveCollection(CustomFieldValue.class, newCustomFieldValues);
            vital3DAO.saveCollection(AssignmentMaterialAssoc.class, newAMAs);
            vital3DAO.saveCollection(QuestionMaterialAssoc.class, newQMAs);
            vital3DAO.saveCollection(UnitMaterialAssoc.class, newUMAs);
            
            
            return new ModelAndView(new RedirectView("courseHome.smvc?worksiteId="+targetWorksite.getId()+"&message=Import+successful"), null);
        }
        
        
    }
    
    private Set getLooseMaterials(VitalWorksite worksite) {
        
        // get all materials in the worksite:
        Set looseMaterials = new HashSet();
        looseMaterials.addAll(worksite.getMaterials());
        logger.debug("finding loose materials... looking through all " + looseMaterials.size() + " materials in the worksite.");
        
        // iterate through them and remove the ones which have assocs with units or assignments or questions
        Iterator looseIter = looseMaterials.iterator();
        while (looseIter.hasNext()) {
            Material material = (Material) looseIter.next();
            if (material.getUnitAssociations().size() > 0 ||
                material.getAssignmentAssociations().size() > 0 ||
                material.getQuestionAssociations().size() > 0) {
                
                looseIter.remove();
            }
        }
        logger.debug("ended up with " + looseMaterials.size() + " loose materials.");
        return looseMaterials;
    }
    
    // This generates a hashmap which will be in the sourceUnits and targetUnits lists:
    private Map makeUnitMap(List assignments, List materials, Unit unit) {
        
        Map unitMap = new HashMap();
        unitMap.put("assignments", assignments);
        unitMap.put("materials", materials);
        unitMap.put("unit", unit);
        return unitMap;
    }
    
    // creates a new Date based on a different date and offset information.
    // the Calendar you pass does not need to be preset to your Date's time, and it will be set
    // to the resulting time after this method is called.
    private Date offsetDate(Date originalDate, Calendar calendar, int monthOffset, int yearOffset) {
        calendar.setTime(originalDate);
        calendar.add(Calendar.MONTH, monthOffset);
        calendar.add(Calendar.YEAR, yearOffset);
        return calendar.getTime();
    }
    
    private ModelAndView errorMAV(BindException errors, String errorCode) {
        errors.reject(errorCode);
        return Vital3Utils.createErrorMAV(errors, this.messageSourceAccessor);
    }
}

