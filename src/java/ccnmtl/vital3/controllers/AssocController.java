package ccnmtl.vital3.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import ccnmtl.utils.TastyClient;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.BasicAdminCommand;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.ucm.UserCourseManager;

/**
 * AssocController is responsible for managing associations between Materials and other
 * entity types: Units, Assignments, and Questions. Requests are expected to have parameters:
 * id: the id of the entity you want to manage.
 * ids: the ids of only and all the materials you want associated with that entity.
 * entity: the type of entity (assignment, question, or unit).
 * Any Materials already associated with that entity which are not specified in your ids parameter
 * will have their association with that entity deleted. Any materials specified which are not already
 * associated with that entity will have new associations created. Therefore, if you pass an empty
 * ids parameter, all existing material-associations with that entity will be deleted and none will
 * be added.
 */
public class AssocController extends Vital3CommandController {
    
    /**
     * Protect this controller
     */
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.TA_ACCESS;
    }
    
    /**
     * Derive worksite from entity instance...
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        VitalWorksite worksite = null;
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String entity = command.getEntity();
        
        if (entity.equals("assignment"))
            worksite = command.getAssignment().getRelatedWorksite();
        else if (entity.equals("question"))
            worksite = command.getQuestion().getRelatedWorksite();
        else if (entity.equals("unit"))
            worksite = command.getUnit().getWorksite();

        if (worksite == null) throw new RuntimeException("worksite was null. something went wrong.");
        
        // decorate worksite:
        ucm.decorateWorksite(worksite, false, false);
        
        // extra security: ensure chosen materials belong to the same worksite:
        ArrayList materials = command.getEntityList();
        if (materials != null && materials.size() > 0) {
            Long worksiteId = worksite.getId();
            Iterator iter = materials.iterator();
            while (iter.hasNext()) {
                Material material = (Material) iter.next();
                if (!material.getWorksite().getId().equals(worksiteId))
                    throw new RuntimeException("One or more materials you've selected do not belong to the correct worksite.");
            }
        }
        
        return worksite;
    }
    
    
    // the main method of this controller
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
        UserContextInfo userInfo = getUserContextInfo(request);
        VitalWorksite worksite = userInfo.getWorksite();
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String entity = command.getEntity();
        Class entityAssocClass = null;
        
        // only one of these will be used depending on the entity type, the rest will be null:
        Assignment assignment = null;
        Question question = null;
        Unit unit = null;
        
        // get the set of materials which should be associated:
        Set newMaterials;
        ArrayList checkedMaterials = command.getEntityList();
        if (checkedMaterials != null) newMaterials = new HashSet(checkedMaterials);
        else newMaterials = new HashSet();
        
        // oldAssocs will be a reference to the old assocs of entity1:
        Set oldAssocs = null;
        // assocsToDelete will eventually contain all assocs which need to be deleted:
        Set assocsToDelete = new HashSet();
        // assocsToAdd will eventually contain all assocs which need to be added:
        Set assocsToAdd = new HashSet();
        
        
        if (entity.equals("assignment")) {
            
            assignment = command.getAssignment();
            entityAssocClass = AssignmentMaterialAssoc.class;
            oldAssocs = assignment.getMaterialAssociations();
            if (newMaterials.size() == 0) {
                assocsToDelete = assignment.getMaterialAssociations();
                logger.debug("scheduling deletion of all existing assocs for assignment id " + assignment.getId());
            }
            
        } else if (entity.equals("question")) {
            
            question = command.getQuestion();
            entityAssocClass = QuestionMaterialAssoc.class;
            oldAssocs = question.getMaterialAssociations();
            if (newMaterials.size() == 0) {
                assocsToDelete = question.getMaterialAssociations();
                logger.debug("scheduling deletion of all existing assocs for question id " + question.getId());
            }
            
        } else {
            
            unit = command.getUnit();
            entityAssocClass = UnitMaterialAssoc.class;
            oldAssocs = unit.getMaterialAssociations();
            if (newMaterials.size() == 0) {
                assocsToDelete = unit.getMaterialAssociations();
                logger.debug("scheduling deletion of all existing assocs for unit id " + unit.getId());
            }
        }
        
        if (newMaterials.size() > 0) {
            
            // materialsToAddAssocs will contain only those which need assocs added:
            Set materialsToAddAssocs = new HashSet(newMaterials);
            
            Iterator oldIter = oldAssocs.iterator();
            while (oldIter.hasNext()) {
                
                Object assoc = null;
                Material material = null;
                
                if (entity.equals("assignment")) {
                    AssignmentMaterialAssoc ama = (AssignmentMaterialAssoc) oldIter.next();
                    material = ama.getMaterial();
                    assoc = ama;
                    
                } else if (entity.equals("question")) {
                    QuestionMaterialAssoc qma = (QuestionMaterialAssoc) oldIter.next();
                    material = qma.getMaterial();
                    assoc = qma;
                    
                } else {
                    UnitMaterialAssoc uma = (UnitMaterialAssoc) oldIter.next();
                    material = uma.getMaterial();
                    assoc = uma;
                }
                
                // assocs for materials not in newMaterials will be deleted:
                if (!newMaterials.contains(material)) {
                    assocsToDelete.add(assoc);
                    logger.debug("scheduling deletion for assoc for material id " + material.getId());
                } else {
                    // remove it from materialsToAddAssocs if it is also related to an oldAssoc...
                    materialsToAddAssocs.remove(material);
                    logger.debug("already have assoc for material id " + material.getId());
                }
            }
            // materialsToAddAssocs now only contains those materials from newMaterials which were not originally associated.
            
            // construct assocs for adding:
            Iterator addIter = materialsToAddAssocs.iterator();
            while (addIter.hasNext()) {
                
                Material material = (Material) addIter.next();
                
                if (entity.equals("assignment")) {
                    AssignmentMaterialAssoc assoc = new AssignmentMaterialAssoc(assignment, material);
                    assocsToAdd.add(assoc);
                    
                } else if (entity.equals("question")) {
                    QuestionMaterialAssoc assoc = new QuestionMaterialAssoc(material, question);
                    assocsToAdd.add(assoc);
                    
                } else {
                    UnitMaterialAssoc assoc = new UnitMaterialAssoc(material, unit);
                    assocsToAdd.add(assoc);
                }
                logger.debug("scheduling insert for assoc for material id " + material.getId());
            }
        
            
        }
        
        // delete assocs:
        logger.debug("deleting...");
        vital3DAO.deleteCollection(entityAssocClass, assocsToDelete);
        
        // add new assocs:
        logger.debug("inserting...");
        vital3DAO.saveCollection(entityAssocClass, assocsToAdd);
        
        String successUrl = null;
        if (entity.equals("question")) successUrl = "listing.smvc?mode=glQuestions&id=" + question.getAssignment().getId();
        else successUrl = "courseHome.smvc?worksiteId=" + worksite.getId();
        
        successUrl += "&message=operation+successful";
            
        logger.debug("AssocController completed! Sending redirect: " + successUrl);
        return new ModelAndView(new RedirectView(successUrl,true));
    }
    
}

