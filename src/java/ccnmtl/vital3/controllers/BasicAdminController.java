package ccnmtl.vital3.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.VideoUploadClient;
import ccnmtl.vital3.Assignment;
import ccnmtl.vital3.CustomField;
import ccnmtl.vital3.CustomFieldValue;
import ccnmtl.vital3.Material;
import ccnmtl.vital3.Question;
import ccnmtl.vital3.Unit;
import ccnmtl.vital3.VitalParticipant;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.commands.BasicAdminCommand;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.ucm.RawUCMTerm;
import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.TextFormatter;
import ccnmtl.vital3.utils.Vital3Utils;

/**
 * Performs display, update, insert, and delete commands.
 * This is for general TA/instructor/admin stuff, not for students.
 */
public class BasicAdminController extends Vital3CommandController {
    
    private static HashMap templateNames;
    
    static {
        templateNames = new HashMap();
        templateNames.put("assignment", "adminAssignment");
        templateNames.put("customField", "adminCustomField");
        templateNames.put("essay", "essayWorkspace");
        templateNames.put("gl", "glWorkspace");
        templateNames.put("material", "adminMaterial");
        templateNames.put("participant", "adminParticipant");
        templateNames.put("question", "adminQuestion");
        templateNames.put("unit", "adminUnit");
        templateNames.put("user", "adminUser");
        templateNames.put("worksite", "adminWorksite");
        // foreach entity...
    }
    
    /**
     * Should return null in case entity is "user" OR (in case entity is "worksite" AND action is "new"/"delete"/"insert")
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        VitalWorksite worksite = null;
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String entity = command.getEntity();
        String action = command.getAction();
        
        logger.info ("BAC.getRequestedWorksite started. Entity = " + entity + ", action = " + action);		
        
        if (entity.equals("worksite")) {
            if (action.equals("new") || action.equals("delete") || action.equals("insert")) return null;
            worksite = command.getWorksite();
            
        } else if (entity.equals("user")) {
            return null;
            
        } else {
            
            if (action.equals("delete") || action.equals("display") || action.equals("update")) {
				
                // for actions involving an existing entity, derive its parent worksite:
                Persistable targetObj = (Persistable) command.mapGet(entity);
                
				if (targetObj == null) {
                    logger.debug("target object was null. Checking for multiple entities..." );
                    ArrayList entityList = command.getEntityList();
                    if (entityList != null) targetObj = (Persistable) entityList.get(0);
                    if (targetObj == null) throw new RuntimeException("No entities were found on the command object!");
                    logger.debug("found them");
                    // we need to decorate vitalparticipants
                    if (targetObj instanceof VitalParticipant) ucm.decorateParticipant((VitalParticipant) targetObj);
                }
                if (targetObj != null) worksite = targetObj.getRelatedWorksite();
            } else {
                // action = insert or new... require knowing which param to look for in each entity case
                if (entity.equals("unit") || entity.equals("customField") || entity.equals("material") || entity.equals("participant"))
                    worksite = command.getWorksite();
                else if (entity.equals("assignment"))
                    worksite = command.getUnit().getRelatedWorksite();
                else if (entity.equals("question"))
                    worksite = command.getAssignment().getRelatedWorksite();
            }
        }
        
        if (worksite != null) {
            ucm.decorateWorksite(worksite, false, false);
            command.setWorksite(worksite);
        }
        return worksite;
    }
    
    
    /**
     * Requires TA_ACCESS for everything except entity="user" OR (entity="worksite" AND action="new"/"delete"/"insert")
     */
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String entity = command.getEntity();
        
        // look for special cases (anything that isn't TA_ACCESS):
        if (entity.equals("worksite")) {
            String action = command.getAction();
            if (action.equals("new") || action.equals("delete") || action.equals("insert")) return UserCourseManager.ADMIN_ACCESS;
        } else if (entity.equals("user")) {
            return UserCourseManager.ADMIN_ACCESS;
        }
        // default case:
        return UserCourseManager.TA_ACCESS;
    }
    
  
    /**
     * Callback for custom post-processing in terms of binding.
	 * Called on each submit, after standard binding but before validation.
     * Overridden to allow for binding of "child" form fields (e.g. as CFVs are to Materials).
	 */
	protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
        
        BasicAdminCommand bac = (BasicAdminCommand) command;
        // This is complicated, see documentation in BasicAdminCommand:
        bac.parseRequestForChildEntities(request, errors);
    }
    // A loophole for unit-testing this method:
    public void onBindTest(HttpServletRequest request, Object command, BindException errors) throws Exception {
        onBind(request, command, errors);
    }
    
    
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
        logger.debug("BasicAdminController.handle beginning...");
        ModelAndView mav = null;
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String action = command.getAction();
        
        if (errors.hasErrors()) {
            
            if (action.equals("update")) command.setAction("display");
            else if (action.equals("insert")) command.setAction("new");
            
            logger.debug("calling showForm with errors");
            mav = showForm(request, response, errors);
            
        } else if (action.equals("display") || action.equals("new")) {
            
            // route "display" and "new" actions to the showForm method:
            logger.debug("calling showForm...");
            mav = showForm(request, response, errors);
        } else if (action.equals("upload")) {
            command.setAction("upload");
            mav = showUploadForm(request, response, command, errors);
        } else {
            
            logger.debug("No errors -> processing submit");
            // route "delete", "update", and "insert" actions to onSubmit:
            mav = onSubmit(request, response, command, errors);
        }
        
        logger.info("Done with handle.");
        
        return mav;
    }
    
    
    /**
     * This method is called either when the form will be displayed for the first time, or when there were submission errors,
     * or after a redirect following a successful insert or update.
     * This is what happens when a GET request is received for this controller. Requires parameters for "entity" and "id".
     * This delegates responsibility to the rest of the superclass showForm method chain, which merges referenceData into
     * the model and constructs the ModelAndView.
     * errors.getMap() yields a map which contains a key whose name is the command name, which holds the command object.
     */

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        
        logger.debug("BasicAdminController.showForm beginning...");
        ModelAndView mav = null;
        
        // errors.getModel() puts the error key into the model under the name of the command object (as specified in the bean config)
        Map controlModel = errors.getModel();
        
        BasicAdminCommand command = (BasicAdminCommand) errors.getModel().get("basicAdminCommand");
        UserContextInfo userInfo = getUserContextInfo(request);
        String entity = command.getEntity();
        String action = command.getAction();
        
        String targetTemplate = getTargetTemplate(entity, errors);
        
        if (!errors.hasGlobalErrors()) {
            
            logger.debug("There were no global errors.");
            
            VitalWorksite worksite = userInfo.getWorksite();
                
            if (!(entity.equals("user") || (entity.equals("worksite") && action.equals("new")))) {
                // add worksite to model
                controlModel.put("worksite", worksite);
                logger.debug("put worksite  '" + worksite.getTitle() + "' into the model.");
            }
            
            // add "action" to the model:
            controlModel.put("action",command.getAction());

            if (entity.equals("assignment")) {
                
                Assignment assignment = command.getAssignment();
                controlModel.put("assignment", assignment);
                
                Unit unit = command.getUnit();
                List ordinalValues = Vital3Utils.getOrdinalValues(unit.getAssignments());
                controlModel.put("ordinalValues", ordinalValues);
                
                ArrayList units = worksite.getUnitsSortedByDate();
                controlModel.put("units", units);
                
            } else if (entity.equals("customField")) {
	          	
                List ordinalValues = Vital3Utils.getOrdinalValues(worksite.getCustomFields());
                controlModel.put("ordinalValues", ordinalValues);
                
            } else if (entity.equals("material")) {
                
                // get the custom field values
                List customFieldValues = command.getChildEntities(0);
                controlModel.put("cfvs", customFieldValues);
                
                if (action.equals("display")) {
                    // get associations for template
                    Material material = command.getMaterial();
                    material.getAllAssociatedAssignments();
                    material.getUnits();
                }
                
            } else if (entity.equals("participant")) {
            
                // add the users for the dropdown:
                /*
                List users = ucm.findAllUsers(false);
                Collections.sort(users, new OmniComparator(VitalUser.class, "getUserIdString"));
                controlModel.put("allUsers", users);
                */
                
            } else if (entity.equals("question")) {
                
                Assignment assignment = command.getAssignment();
                controlModel.put("assignment", assignment);
                
                String questionId = request.getParameter("id");
                controlModel.put("questionId", questionId);
                
                List ordinalValues = Vital3Utils.getOrdinalValues(assignment.getQuestions());
                controlModel.put("ordinalValues", ordinalValues);
                
            } else if (entity.equals("unit")) {
                
                // nothing to do
            
            } else if (entity.equals("user")) {
                
                VitalUser user = command.getUser();
                if (action.equals("display")){ 
                    ucm.decorateUser(user, true);
                }
                controlModel.put("user", user);
               
            } else if (entity.equals("worksite")) {
                
                // add the terms for the dropdown:
                List terms = ucm.findAllTerms();
                Collections.sort(terms, new OmniComparator(RawUCMTerm.class, "getStartDate"));
                controlModel.put("allTerms", terms);
                
                /*
                 // convert course id string from internal format to display format:
                 String displayString = command.getCourseIdStringDisplay();
                 String internalString = command.getCourseIdString();
                 if (displayString == null && internalString != null) {
                     displayString = ucm.formatCourseIdStringForDisplay(command.getCourseIdString(internalString));
                     command.setCourseIdStringDisplay(displayString);
                 }*/
                
            }

            // foreach entity... a showForm routine
            
        } else {
            
            logger.debug("There were global errors! Preparing the Model & View for error page...");
            // put errors into the model:
            String errorString = Vital3Utils.convertErrorsToString(errors, this.messageSourceAccessor);
            controlModel.put("message", errorString);
        }
        
        // put "currentUser", "participant" "worksite", and "admin" into the model if available:
        Vital3Utils.putUserInfoIntoModel(userInfo, controlModel);
        
        // put textFormatter into model:
        TextFormatter dp = this.getTextFormatter();
        controlModel.put("textFormatter", dp);
        
        logger.debug("BasicAdminController.showForm complete.");
        return new ModelAndView(targetTemplate, controlModel);

    }

    /*
     * Redirect users to a 3rd-party video upload utility
     * Authentication scheme: 
     * - Generate random salt (nonce) in the form with Vital specific app string -- "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'vtl'" 
     * - Concatenate uni, redirectBack url, notifyUrl & nonce 
     * - Create Sha1 hash using secret key & the concatenated string
     * - Redirect to wardenclyffe/?uni=$UNI;salt=$SALT;hash=$HASH;redirect_url=$REDIRECT_URL;notify_url=$NOTIFY_URL
     */
    protected ModelAndView showUploadForm(HttpServletRequest request, HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        logger.debug("BasicAdminController.showUploadForm beginning...");
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String userIdString = getUserContextInfo(request).getUser().getUserIdString();
        
        String nonce = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'vtl'").format(new java.util.Date());
        
        // (one to tell Wardenclyffe where to redirect the user to after they have uploaded their video,
        StringBuffer back = new StringBuffer("http://");
        back.append(request.getServerName());
        if (request.getServerPort() > 0) {
            back.append(":");
            back.append(request.getServerPort());
        }
        back.append(request.getContextPath());
        back.append("/materialsLib.smvc?worksiteId=");
        back.append(command.getWorksiteId());

        // and another to tell Wardenclyffe where to send the "upload finished" notification later).
        // 
        StringBuffer notify = new StringBuffer("http://");
        notify.append(request.getServerName());
        if (request.getServerPort() > 0) {
            notify.append(":");
            notify.append(request.getServerPort());
        }        
        notify.append(request.getContextPath());
        notify.append("/videoUpload.smvc");
        
        logger.debug("back: " + back);
        logger.debug("notify: " + notify);
                
        String url = _videoUploadClient.getHost() + 
                     "/?set_course=" + command.getWorksiteId() +
                     "&as=" + userIdString + 
                     "&redirect_url=" + back + 
                     "&notify_url=" + notify +
                     "&nonce=" + nonce +
                     "&hmac=" +_videoUploadClient.getHash(userIdString, back.toString(), notify.toString(), nonce);

        return Vital3Utils.redirectModelAndView(url);
    }
    
    
    /**
     * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator, or on every submit if no validator.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors Errors instance without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
     */
	protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response, Object commandObj, BindException errors) throws Exception {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        String action = command.getAction();
        String entity = command.getEntity();
        VitalWorksite worksite = command.getWorksite();

        logger.debug("BasicAdminController.onSubmit beginning... (action=" +action+ " entity=" +entity+ ")");
        try {
            
            if (!entity.equals("user")) {
                if (!entity.equals("worksite") || (entity.equals("worksite") && action.equals("update"))) {
                    // set worksite id for redirect
                    command.setWorksiteId(worksite.getId());
                    logger.debug("set worksiteId = " + worksite.getId() + " (" + worksite.getTitle() + ") for redirecting.");
                }
            }
            
            ///// Branch on entity, then on action
            //////##############################################################
            if (entity.equals("assignment")) {
                
                Assignment assignment = command.getAssignment();
                
                if (action.equals("delete")) {
                    vital3DAO.delete(Assignment.class, assignment);
                
                } else if (action.equals("insert")) {
                    
                    // construct empty assignment and transfer to it:
                    Unit unit = command.getUnit();
                    assignment = new Assignment(unit, null, null, null, 0, null, null);
                    command.transferToAssignment(assignment);
                    
                    vital3DAO.save(Assignment.class, assignment);
                    // put it on command object for redirect:
                    command.setAssignment(assignment);
                    
                } else {
                    // update
                    command.transferToAssignment(assignment);
                    
                    // set unit if needed:
                    Unit unit = command.getUnit();
                    if (unit != null) assignment.updateUnit(unit);
                    
                    // save:
                    vital3DAO.save(Assignment.class, assignment);
                }
                
                
                //////##############################################################
            } else if (entity.equals("customField")) {
                
                CustomField customField = command.getCustomField();
                
                if (action.equals("delete")) {
                    vital3DAO.delete(CustomField.class, customField);
                
                } else if (action.equals("insert")) {
                    
                    // construct an empty customField and transfer name, ordinalValue, and visibility values to it:
                    customField = new CustomField(worksite, null, 0, 0);
                    command.transferToCustomField(customField);
                    
                    vital3DAO.save(CustomField.class, customField);
                    
                    // save a blank CFV for each material of this CF
                    Set materials = worksite.getMaterials();
                    Iterator matIter = materials.iterator();
                    Set cfvs = new HashSet();
                    while (matIter.hasNext()) {
                        Material material = (Material)matIter.next();
                        CustomFieldValue cfv = new CustomFieldValue(customField, material, customField.getOrdinalValue(), null);
                        vital3DAO.save(CustomFieldValue.class, cfv);
                        cfvs.add(cfv);
                        
                        // add to material's collection:
                        material.getCustomFieldValues().add(cfv);
                    }
                    customField.setValues(cfvs);
                    
                } else {
                    // update
                    Integer originalOrdinalValue = customField.getOrdinalValue();
                    command.transferToCustomField(customField);
                    if (!originalOrdinalValue.equals(customField.getOrdinalValue())) {
                        
                        // modify all the ordinalValues of the children CFVs
                        if (customField.getValues().size() != 0) {
                            ArrayList cfvs = new ArrayList(customField.getValues());                
                            Iterator cfvIter = cfvs.iterator();
                            while (cfvIter.hasNext()) {
                                CustomFieldValue cfv = (CustomFieldValue)cfvIter.next();
                                cfv.setOrdinalValue(command.getOrdinalValue());
                            }
                        }
                    }
                    vital3DAO.save(CustomField.class, customField);
                }
                
                
                //////##############################################################
            } else if (entity.equals("material")) {
                
                Material material = command.getMaterial();
                
                if (action.equals("delete")) {
                    vital3DAO.delete(Material.class, material);
                    
                } else {
                    
                    List entityList = null;
                    
                    if (action.equals("insert")) {
                        
                        // construct empty material and transfer to it:
                        material = new Material(worksite, 0, null, null, null, null, null, null);
                        command.transferToMaterial(material);
                        
                        // create empty CFVs and transfer values:
                        material.createCFVs();
                        Iterator cfIter = command.getChildEntities(0).iterator();
                        Iterator dataIter = command.getChildData(0).iterator();
                        while (dataIter.hasNext()) {
                            HashMap formCFV = (HashMap) dataIter.next();
                            CustomField cf = (CustomField) cfIter.next();
                            CustomFieldValue cfv = material.getCustomFieldValueForCustomField(cf);
                            cfv.setValue((String)formCFV.get("value"));
                        }
                        //command.setChildEntities(0, material.getCustomFieldValuesAsList());
                        entityList = material.getCustomFieldValuesAsList();
                    } else {
                        // update
                        
                        // update CFVs:
                        entityList = command.getChildEntities(0);
                        Iterator cfvIter = entityList.iterator();
                        Iterator dataIter = command.getChildData(0).iterator();
                        while (cfvIter.hasNext()) {
                            HashMap newValueMap = (HashMap) dataIter.next();
                            String newValue = (String) newValueMap.get("value");
                            CustomFieldValue cfv = (CustomFieldValue) cfvIter.next();
                            cfv.setValue(newValue);
                        }
                        // transfer new properties:
                        command.transferToMaterial(material);
                    }
                    
                    // save material and CFVs:
                    vital3DAO.save(Material.class, material);
                    if (!entityList.isEmpty()) vital3DAO.saveCollection(CustomFieldValue.class, entityList);
                }
                
                //////##############################################################
            } else if (entity.equals("participant")) {
                
                VitalParticipant participant = command.getParticipant();
                
                if (action.equals("delete")) {
                    
                    if (participant == null) {
                        // we're deleting multiple participants:
                        ArrayList entityList = command.getEntityList();
                        ucm.deleteParticipants(entityList);
                    } else {
                        ucm.deleteParticipant(participant);
                    }
                    
                } else {
                    
                    // decorate user for duplicate-check:
                    VitalUser user = command.getUser();
                    if (user != null) ucm.decorateUser(user, true);
                    
                    if (action.equals("insert")) {
                        
                        if (user == null) {
                            // we are creating a new user simultaneously
                            // construct an empty user using convenient VitalUser constructor which will instantiate a RawUCMUser object and transfer to it            
                            user = new VitalUser(null, null, null, null, null, null, null);
                            command.transferToUser(user);
                            // prevent hacking:
                            user.setAccessLevel(new Integer(0));
                            
                            ucm.insertUser(user);
                            logger.debug("inserted user... preparing to insert participant...");
                            
                        }
                        
                        participant = ucm.constructParticipant(user, worksite, null);
                        command.transferToParticipant(participant);
                        ucm.insertParticipant(participant);
                        
                        
                    } else {
                        // update
                        
                        // Check for duplicate participants:
                        Long worksiteId = worksite.getId();
                        Long participantId = participant.getId();
                        Iterator iter = user.getParticipants().iterator();
                        while (iter.hasNext()) {
                            VitalParticipant vp = (VitalParticipant) iter.next();
                            if (!vp.getId().equals(participantId) && vp.getWorksite().getId().equals(worksiteId))
                                throw new RuntimeException("This user is already participating in this course.");
                        }
                        
                        command.transferToParticipant(participant);
                        participant.updateUser(user);
                        ucm.updateParticipant(participant);
                    }
                }
                    
                
                //////##############################################################
            } else if (entity.equals("question")) {
                
                Question question = command.getQuestion();
                
                if (action.equals("delete")) {
                    vital3DAO.delete(Question.class, question);
                    
                } else if (action.equals("insert")) {
                    
                    // construct an empty question and transfer to it:
                    Assignment assignment = command.getAssignment();
                    question = new Question(assignment, 0, null);
                    command.transferToQuestion(question);
                    
                    vital3DAO.save(Question.class, question);
                    
                } else {
                    // update
                    command.transferToQuestion(question);
                    vital3DAO.save(Question.class, question);
                }
                
                
                //////##############################################################
            } else if (entity.equals("unit")) {
                
                Unit unit = command.getUnit();
                
                if (action.equals("delete")) {
                    vital3DAO.delete(Unit.class, unit);
                    
                } else if (action.equals("insert")) {
                    
                    // construct empty unit and transfer to it:
                    unit = new Unit(worksite, null, null, null, null, 0);
                    command.transferToUnit(unit);
                    
                    vital3DAO.save(Unit.class, unit);                    
                    
                } else {
                    // update
                    command.transferToUnit(unit);
                    vital3DAO.save(Unit.class, unit);
                    
                }
                
                
                //////##############################################################
            } else if(entity.equals("user")) {
                
                VitalUser user = command.getUser();
                
                if (action.equals("delete")) {
                    ucm.deleteUser(user);
                    
                } else if (action.equals("insert")) {
                    
                    // construct an empty user using convenient VitalUser constructor which will instantiate a RawUCMUser object and transfer to it            
                    user = new VitalUser(null, null, null, null, null, null, null);
                    command.transferToUser(user);
                    ucm.insertUser(user);
                    
                } else {
                    // update
                    command.transferToUser(user);  
                    ucm.updateUser(user);
                }
                
                
                //////##############################################################
            } else if (entity.equals("worksite")) {
                
                if (action.equals("delete")) {
                    
                    ucm.deleteWorksite(worksite);
                    
                } else if (action.equals("insert")) {
                    
                    RawUCMTerm term = command.getTerm();
                    
                    worksite = ucm.constructWorksite(term, null, null);
                    command.transferToWorksite(worksite);
                    
                    ucm.insertWorksite(worksite);
                    
                    // affiliate worksite with course:
                    String courseIdString = command.getCourseIdString();
                    if (courseIdString != null && !courseIdString.equals("")) ucm.affiliateWorksiteWithCourse(worksite, courseIdString, true);
                    
                } else {
                    // update
                    command.transferToWorksite(worksite);
                    
                    // update term
                    RawUCMTerm relatedTerm = command.getTerm();
                    if (relatedTerm != null) worksite.setTerm(relatedTerm);
                    
                    ucm.updateWorksite(worksite);
                    
                    // affiliate worksite with course:
                    String courseIdString = command.getCourseIdString();
                    if (courseIdString != null && !courseIdString.equals("")) ucm.affiliateWorksiteWithCourse(worksite, courseIdString, true);
                    else ucm.removeCourseAffilsForWorksite(worksite);
                    
                }
                
            }
            
            
            
        } catch(DataIntegrityViolationException dive) {
            // this happens when attempting to delete an entity before its children
            logger.info("DataIntegrityViolationException during " + action + " of " + entity, dive);
            errors.reject("error.database.integrity."+entity);
            
        } catch(DataAccessException dae) {
            // this could be anything... there are numerous subclasses which are more specific
            logger.info("DataAccessException during " + action + " of " + entity, dae);
            errors.reject("error.database");
        
        } catch (UnsupportedOperationException e) {
            // so far this only happens when a participant was about to be created for an existing user/worksite pair
            logger.info("User tried to create participant for existing user/worksite pair");
            errors.reject("error.duplicate.participant");
        }
        // if errors occur, delegate to showForm with errors.
        if (errors.hasErrors()) {

            // uncomment this next line for debugging:
            logger.debug(errors.getMessage());
            return showForm(request, response, errors);
        }
        
        String successUrl = null;
        if (entity.equals("participant")) {
            successUrl = "listing.smvc?mode=roster&id=" + command.getWorksiteId() + "&message=" + action + "+successful";
            
        } else if (entity.equals("worksite")) {
            if (action.equals("update"))
                successUrl = "courseHome.smvc?worksiteId=" + command.getWorksiteId() + "&message=" + action + "+successful";
            else successUrl = "myCourses.smvc?message=" + action + "+successful";
            
        } else if (entity.equals("customField")) {
            successUrl = "listing.smvc?mode=customField&id=" + command.getWorksiteId() + "&message=" + action + "+successful";
            
		} else if (entity.equals("question")) {
            Long assignmentId = null;
            if (action.equals("insert")) assignmentId = command.getAssignmentId();
            else assignmentId = command.getQuestion().getAssignment().getId();
            successUrl = "listing.smvc?mode=glQuestions&id=" + assignmentId + "&message=" + action + "+successful"; 
            
        } else if (entity.equals("user")) {
            successUrl = "listing.smvc?mode=user&message="+ action +"+successful";
        
        } else if (entity.equals("material")) {
            successUrl = "materialsLib.smvc?worksiteId=" + command.getWorksiteId() + "&message=" + action + "+successful";
        
        } else if (entity.equals("assignment")) {
            if (action.equals("insert") && command.getType().equals(Assignment.GUIDED_LESSON))
                successUrl = "listing.smvc?mode=glQuestions&id=" + command.getAssignment().getId() + "&message=insert+successful. Now+you+may+add+questions.";
            else successUrl = "courseHome.smvc?worksiteId=" + command.getWorksiteId() + "&message=" + action + "+successful";
            
        } else { //if (entity.equals("unit")) {
            successUrl = "courseHome.smvc?worksiteId=" + command.getWorksiteId() + "&message=" + action + "+successful";
        }
        
        logger.debug("BasicAdminController.onSubmit completed! Sending redirect: " + successUrl);
        return Vital3Utils.redirectModelAndView(successUrl);
    }
        
    
    // Provides the name of the template appropriate for each entity:
    private String getTargetTemplate(String nickname, Errors errors) {
        if (errors.hasGlobalErrors()) logger.debug("There were global errors");
        if (!(errors.hasGlobalErrors() || nickname == null)) {
            String templateName = (String) templateNames.get(nickname);
            if (templateName != null) return templateName;
        }
        return "error";
    }


    private VideoUploadClient _videoUploadClient; 
    public void setVideoUploadClient(VideoUploadClient tc) {
        this._videoUploadClient = tc;
    }
    public VideoUploadClient getUploadClient() {
        return this._videoUploadClient;
    }


}

