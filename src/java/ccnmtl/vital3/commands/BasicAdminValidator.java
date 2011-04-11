package ccnmtl.vital3.commands;

import java.text.ParseException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import ccnmtl.vital3.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.Vital3Utils;

/**
 * This validator does a fair amount of work:
 * 1) Examines the command for the validity of the basic part of the request (action, entity, and id).
 * 2) If needed, retrieves the entity with the indicated id and puts it onboard the command object.
 * 3) If needed, validates remaining command data and retrieves other indicated entities, putting them onboard the command object.
 * Throughout this process, any errors with command structure or with entity retrieval result in a "global error". This should
 * only happen due to URL-hacking, bad form design, bad program design, or database failure.
 * Errors with user-entered data result in a field error being applied to those fields.
 */
public class BasicAdminValidator extends Vital3Validator {
    
    public static final HashSet allowedActions = new HashSet(Arrays.asList(new String[]{"display","update","new","insert","delete","upload"}));
    public static final HashSet allowedEntities = new HashSet(Arrays.asList(new String[]{"assignment","customField","material","participant","question","unit","user","worksite"}));
                                                                                                              
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }

    
    /**
     * This branches validation by entity type. Within each entity case, there may be validation specific to each action (Insert/New/Update).
     * Following are the general strategies and guidelines used for validating each action:
     * For INSERT: validate/retrieve entities and put on command obj. Do not construct new entities.
     * For NEW: validate/retrieve entities referred to by the url, put on command obj.
     *     Dummy entities need to be set up on the command obj to stand in for related entities so that the template can use as much of the same
     *     code for "display" as it does for "new".
     * For UPDATE: validate user-entered data and fetch entities, put on command obj. Do not modify the live object!
     */
    public void validate(Object commandObj, Errors errors) {
        logger.debug("Starting validate in BAV");
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;

        // validate basic parameters (will register global errors if anything is wrong):
        String action = validateActionEntityId(command, errors);

        // NOTE: binding errors cause Field Errors, not Global Errors
        if (errors.hasGlobalErrors()) return;
        
        String entity = command.getEntity();
        
        if (action.equals("delete") || action.equals("display") || action.equals("update")) {
            
            // this will put the entity on the command obj (and when action == display, transfer its properties):
            findPrimaryCommandEntity(command, errors);
        }

        if (errors.hasGlobalErrors()) return;
        
        // perform case-specific validation and retrievals. "delete" and "display" don't need any further validation.
        logger.debug("BasicAdminValidator.validate: beginning case-specific validation. Entity = " + entity + ", action = " + action);
        if (action.equals("insert") || action.equals("new") || action.equals("update")) {
            
            // these tend to follow the pattern:
            // if (new){ stuff for new; } else { stuff for BOTH insert and update; if(insert) { stuff only for insert; } else { stuff only for update; }}
            
            //////##############################################################
            if (entity.equals("assignment")) {
                
                
                // validate and retrieve unit (required)
                Unit relatedUnit = (Unit) validateAndFind("unit", Unit.class,  command, errors, true);
                    
                if (!action.equals("new")) {
                    
                    // validate ordinal value (required and can't be duplicate):
                    Integer newOrdinalValue = command.getOrdinalValue();
                    validateInteger(newOrdinalValue, new Integer(-100000), new Integer(100000), true, errors, "ordinalValue");
                    
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "error.missing.title");
                    
                    if (!errors.hasErrors()) {
                        Integer originalOrdinalValue = null;
                        Unit originalUnit = null;
                        if (action.equals("update")) {
                            Assignment originalAssignment = (Assignment)vital3DAO.findById(Assignment.class, command.getId());
                            originalOrdinalValue = originalAssignment.getOrdinalValue();
                            originalUnit = originalAssignment.getUnit();
                        }
                        // this will test for duplicate ordinal values in two cases:
                        // 1. it is an insert
                        // 2. it's an update and either the user changed the unit or the user changed the ordinalValue (or both).
                        if (action.equals("insert") || (action.equals("update") && (!relatedUnit.getId().equals(originalUnit.getId()) || !newOrdinalValue.equals(originalOrdinalValue)))) {
                            List assignmentsWithSameOV = vital3DAO.findByTwoPropertyValues(Assignment.class, "ordinalValue", newOrdinalValue,"unit", relatedUnit);
                            if (!assignmentsWithSameOV.isEmpty()) {
                                logger.warn("Already found an ordinal value of " + newOrdinalValue + " in unit " + relatedUnit.getTitle());
                                errors.rejectValue("ordinalValue", "error.duplicate.ordinalValue");
                            }
                        }
                    }
                }
                
                //////##############################################################
            } else if (entity.equals("customField")) {
                
                
                // validate and retrieve related worksite (required):
                VitalWorksite worksite = (VitalWorksite) validateAndFind("worksite", VitalWorksite.class, command, errors, true);
                    
                if (!action.equals("new")) {
                    
                    // validate ordinalValue (required and can't be duplicate)
                    Integer newOrdinalValue = command.getOrdinalValue();
                    validateInteger(newOrdinalValue, new Integer(-100000), new Integer(100000), true, errors, "ordinalValue");
                    // validate visibility (required):
                    validateInteger(command.getVisibility(), Unit.INVISIBLE, Unit.VISIBLE, true, errors, "visibility");
                    
                    if (!errors.hasErrors()) {
                        Integer originalOrdinalValue = null;
                        if (action.equals("update")) {
                            CustomField originalCF = (CustomField)vital3DAO.findById(CustomField.class, command.getId());
                            originalOrdinalValue = originalCF.getOrdinalValue();
                        }
                        if (action.equals("insert") || (action.equals("update") && !newOrdinalValue.equals(originalOrdinalValue))) {
                            //Make sure we're not changing the ordinal value to an ordinal value that's already taken:
                            List cfsWithSameOV = vital3DAO.findByTwoPropertyValues(CustomField.class, "ordinalValue", newOrdinalValue,"worksite", worksite);
                            if (!cfsWithSameOV.isEmpty()) {
                                logger.warn("Already found an ordinal value of " + newOrdinalValue + " in worksite " + worksite);
                                errors.rejectValue("ordinalValue", "error.duplicate.ordinalValue");
                            }
                        }
                    }
                    
                }
                
                //////##############################################################
            } else if (entity.equals("material")) {
                
                // validate and retrieve related worksite (required):
                VitalWorksite relatedWorksite = (VitalWorksite) validateAndFind("worksite", VitalWorksite.class,  command, errors, true);
                
                if (action.equals("new")) {
                    
                    if (relatedWorksite != null) {
                        // create empty CFVs:
                        List fakeCFVs = relatedWorksite.createFakeCFVs();
                        command.setChildEntities(0, fakeCFVs);
                    }                
                    
                } else {
                    
                    // validate title (required):
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "error.missing.title");
                    // validate accessLevel (required):
                    validateInteger(command.getAccessLevel(), Material.PUBLIC_ACCESS, Material.UNLISTED_ACCESS, true, errors, "accessLevel");
                    
                    if (action.equals("insert")) {
                        
                        if (relatedWorksite != null) {
                            // validate and retrieve CustomFields, validate and save refs to CFs in childEntities:
                            List formDataList = command.getChildData(0);
                            if (!formDataList.isEmpty()) {
                                
                                List customFieldList = new ArrayList();
                                Iterator cfIter = relatedWorksite.getCustomFieldList().iterator();
                                Iterator dataIter = formDataList.iterator();
                                while (dataIter.hasNext()) {
                                    HashMap formCFV = (HashMap) dataIter.next();
                                    String cfIdString = getHashedString(formCFV, "id", errors, "error.missing.cf.id");
                                    if (cfIdString != null) {
                                        CustomField cf = relatedWorksite.getCustomFieldById(new Long(cfIdString));
                                        if (cf == null) errors.reject("error.nosuch.related.material.cf");
                                        else {
                                            // verify value, add CF:
                                            String newValue = getHashedString(formCFV, "value", errors, "error.missing.cfv.value");
                                            customFieldList.add(cf);
                                        }
                                    }
                                }
                                command.setChildEntities(0, customFieldList);
                            }
                        }
                        
                    } else {
                        // update
                        
                        Material material = command.getMaterial();
                        
                        // validate CustomFieldValue stuff:
                        List entityList = new ArrayList();
                        List formDataList = command.getChildData(0);
                        Iterator dataIter = formDataList.iterator();
                        while (dataIter.hasNext()) {
                            HashMap formCFV = (HashMap) dataIter.next();
                            String cfvIdString = getHashedString(formCFV, "id", errors, "error.missing.cfv.id");
                            if (cfvIdString != null) {
                                CustomFieldValue cfv = material.getCustomFieldValueById(new Long(cfvIdString));
                                if (cfv == null) errors.reject("error.nosuch.related.material.cfv");
                                else {
                                    // add to entityList and validate new value:
                                    entityList.add(cfv);
                                    String newValue = getHashedString(formCFV, "value", errors, "error.missing.cfv.value");
                                }
                            }
                        }
                        command.setChildEntities(0, entityList);
                    }
                }
                
                
                //////##############################################################
            } else if (entity.equals("participant")) {
                
                
                if (action.equals("new")) {
                    
                    // validate and retrieve related worksite (required):
                    validateAndFind("worksite", VitalWorksite.class,  command, errors, true);
                    
                    command.setupDummyUser();
                    
                } else {
                    
                    // validate and retrieve related worksite (required):
                    if (validateAndFind("worksite", VitalWorksite.class, command, errors, true) == null) command.setupDummyWorksite();
                    // validate accessLevel (required):
                    validateInteger(command.getAccessLevel(), UserCourseManager.STUDENT_ACCESS, UserCourseManager.ADMIN_ACCESS, true, errors, "accessLevel");
                    
                    if (action.equals("insert")) {
                        // validate and retrieve related user:
                        Long userId = command.getUserId();
                        
                        if (userId.equals(new Long(-1))) {
                            // we're creating a new user simultaneously
                            
                            // validate userIdString, which can't be empty or duplicate
                            if (command.getUserIdString() == null) errors.rejectValue("userIdString", "error.missing.userIdString");
                            else {
                                List results = vital3DAO.findByPropertyValue(VitalUser.class, "userIdString", command.getUserIdString());
                                if (!results.isEmpty()) errors.rejectValue("userIdString", "error.duplicate.userIdString");
                            }
                            // if user entered an email address, ensure that it is not a duplicate:
                            String email = command.getEmail();
                            if (email != null && !email.equals("")) {
                                VitalUser emailUser = ucm.findUserByEmail(email, false);
                                if (emailUser != null) errors.rejectValue("email", "error.duplicate.email");
                            }
                            // require authMethod param:
                            String authMethod = command.getAuthMethod();
                            if (authMethod == null) errors.reject("error.missing.authMethod");
                            else {
                                // make password blank so it is not misleading. password field is intended for vital-only auth.
                                if (!authMethod.equals("vital")) command.setPassword("");
                            }
                            if (errors.hasErrors()) command.setupDummyUser();
                        } else {
                            
                            // user is not null, so retrieve the existing user:
                            if (validateAndFind("user", VitalUser.class, command, errors, true) == null) command.setupDummyUser();
                        }
                        
                        
                    } else {
                        // update
                        
                        // validate user (required):
                        validateAndFind("user", VitalUser.class, command, errors, true);
                        
                    }
                }
                
                
                //////##############################################################
            } else if (entity.equals("question")) {
                
                
                // validate and retrieve related assignment
                Assignment assignment = (Assignment) validateAndFind("assignment", Assignment.class, command, errors, false);
                
                if (!action.equals("new")) {
                    
                    // validate ordinalValue (required)
                    Integer newOrdinalValue = command.getOrdinalValue();
                    validateInteger(newOrdinalValue, new Integer(-100000), new Integer(100000), true, errors, "ordinalValue");
                    
                    if (!errors.hasErrors()) {
                        Integer originalOrdinalValue = null;
                        if (action.equals("update")) {
                            Question originalQuestion = (Question)vital3DAO.findById(Question.class, command.getId());
                            originalOrdinalValue = originalQuestion.getOrdinalValue();
                        }
                        if (action.equals("insert") || (action.equals("update") && !newOrdinalValue.equals(originalOrdinalValue))) {
                            //Make sure we're not changing the ordinal value to an ordinal value that's already taken:
                            List questionsWithSameOV = vital3DAO.findByTwoPropertyValues(Question.class, "ordinalValue", newOrdinalValue,"assignment", assignment);
                            if (!questionsWithSameOV.isEmpty()) {
                                logger.warn("Already found an ordinal value of " + newOrdinalValue + " in assignment " + assignment);
                                errors.rejectValue("ordinalValue", "error.duplicate.ordinalValue");
                            }
                        }
                    }
                }
                
                //////##############################################################
            } else if (entity.equals("unit")) {
                
                
                // validate and retrieve related worksite (required):
                validateAndFind("worksite", VitalWorksite.class, command, errors, true);
                
                if (!action.equals("new")) {
                    
                    // validate startDate and endDate
                    validateDate(command.getStartDate(), command.getEndDate(), errors, "startDate", "endDate");
                    // validate visibility (required):
                    validateInteger(command.getVisibility(), Unit.INVISIBLE, Unit.VISIBLE, true, errors, "visibility");
                    // validate description length:
                    if (command.getDescription() != null && command.getDescription().length() > 4000) {
                        errors.reject("error.toomanychars.unit.description");
                    }
                }
                
                //////##############################################################
            } else if (entity.equals("user")) {
                
                // there is nothing to do when action = new
                if (!action.equals("new")) {
                    
                    // require authMethod param, set password to blank if using non-vital auth:
                    String authMethod = command.getAuthMethod();
                    if (authMethod == null) errors.reject("error.missing.authMethod");
                    else if (!authMethod.equals("vital")) command.setPassword("");
                    
                    // validate userIdString (required):
                    String userIdString = command.getUserIdString();
                    if (userIdString == null) errors.reject("error.missing.userIdString");
                    
                    String email = command.getEmail();
                    
                    if (!errors.hasErrors()) {
                        if (action.equals("insert")) {
                            // validate userIdString to ensure no duplicates:
                            List results = vital3DAO.findByPropertyValue(VitalUser.class, "userIdString", userIdString);
                            if (!results.isEmpty()) errors.reject("error.duplicate.userIdString");
                            
                            // if user entered an email address, ensure that it is not a duplicate:
                            if (email != null && !email.equals("")) {
                                VitalUser emailUser = ucm.findUserByEmail(email, false);
                                if (emailUser != null) errors.reject("error.duplicate.email");
                            }
                            
                            
                        } else {
                            // update
                            
                            // If the UserIdString is not changed, findByPropertyValue will have one result which is not duplicate value
                            // have to do the search this way because userIdString is the only thing linking vital and raw!
                            VitalUser persistedUser = (VitalUser)vital3DAO.findById(VitalUser.class, command.getId());
                            String originalUserIdString = persistedUser.getUserIdString();
                            
                            if (!userIdString.equals(originalUserIdString)) {
                                List results = vital3DAO.findByPropertyValue(VitalUser.class, "userIdString", userIdString);
                                if (!results.isEmpty()) errors.reject("error.duplicate.userIdString");
                            }
                            
                            // if user entered an email address and it's different from the old one, ensure that it does not already belong to someone else:
                            if (email != null && !email.equals("") && !email.equals(persistedUser.getEmail())) {
                                VitalUser emailUser = ucm.findUserByEmail(email, false);
                                if (emailUser != null) errors.reject("error.duplicate.email");
                            }
                        }
                            
                    }
                }
                
                //////##############################################################
            } else if (entity.equals("worksite")) {
                
                if (action.equals("new")) {
                    command.setupDummyTerm();
                    
                } else {
                    // identical validation for insert and update:
                    
                    // validate and retrieve related term (required):
                    if (validateAndFind("term", RawUCMTerm.class, command, errors, true) == null) command.setupDummyTerm();
                    // require title:
                    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "error.missing.title");
                    
                    if (command.getAnnouncement() != null &&  command.getAnnouncement().length() > 4000) {
                        logger.debug("Worksite announcement is too long.");
                        errors.reject("error.toomanychars.worksite.announcement");
                    }
                    
                    // courseIdString is not subject to validation, but courseIdStringDisplay is...
                    String displayString = command.getCourseIdStringDisplay();
                    if (displayString != null) {
                        try {
                            String idString = ucm.parseCourseIdString(displayString);
                            command.setCourseIdString(idString);
                        } catch (ParseException pe) {
                            errors.rejectValue("courseIdStringDisplay", "error.invalid.courseIdStringDisplay");
                        }
                    }
                }
            }
            
        }
        
        logger.debug("BasicAdminValidator.validate: completed validation!");
    }




    /**
     * Full validation of entity, action, and id parameters.
     *@param command          The BasicAdminCommand object.
     *@param errors           The Errors object, which will be used to report errors in this method.
     *@param validActions     Which actions are permissable, or null if you will allow any action. If this
     *                        is not null, and action equals something else, an error will be reported.
     *@return                 Returns the action string.
     */
    public String validateActionEntityId(BasicAdminCommand command, Errors errors) {

        String action = command.getAction();
        String entity = command.getEntity();
		Long id = command.getId();

        logger.debug ("## BAV.validateActionEntityId: Action is " + action + " Entity is " + entity + " id is " + id);

        if (action == null) errors.reject("error.missing.action");
        else {

            if (!allowedActions.contains(action)) errors.reject("error.nosuch.action");

            if (id == null) {
                // "id" parameter required for display & update... delete requires "id" OR "ids":
                if (action.equals("display") || action.equals("update")) errors.reject("error.missing.id");
                else if (action.equals("delete") && command.getIds() == null) errors.reject("error.missing.id");
            }
        }
        
		if (entity == null) errors.reject("error.missing.entity");
        if (!allowedEntities.contains(entity)) errors.reject("error.nosuch.entity");

        return action;

    }

    /**
     * Retrieves the entity and puts it on the command object. Rejects errors and returns the entity string.
     */
    public String findPrimaryCommandEntity(BasicAdminCommand command, Errors errors) {

        String entity = command.getEntity();
        logger.debug("BasicAdminValidator.findPrimaryCommandEntity beginning. Entity = " + entity);

        String action = command.getAction();

        // find the entity and put it on the command object:

        if (entity.equals("assignment")) {

            Assignment obj = (Assignment) validateAndFindPrimary("assignment", Assignment.class,  command, errors);
            if (obj != null) {

                // if action is "display", transfer properties to command:
                if (action.equals("display")) command.transferFromAssignment(obj);
            }

        } else if (entity.equals("customField")) {
            
            CustomField obj = (CustomField) validateAndFindPrimary("customField", CustomField.class,  command, errors);
            if (obj != null) {
                // if action is "display", transfer properties to command:
                if (action.equals("display")) command.transferFromCustomField(obj);
            }
            
        } else if (entity.equals("material")) {

            Material obj = (Material) validateAndFindPrimary("material", Material.class,  command, errors);
            if (obj != null) {
                // if action is "display", transfer properties to command:
                if (action.equals("display")) command.transferFromMaterial(obj);
            }

        } else if (entity.equals("participant")) {

            VitalParticipant obj = (VitalParticipant) validateAndFindPrimary("participant", VitalParticipant.class,  command, errors);
            if (obj != null) {
                // decorate the participant
                ucm.decorateParticipant(obj);
                // if action is "display", transfer properties to command:
                if (action.equals("display")) command.transferFromParticipant(obj);
            }

        } else if (entity.equals("question")) {

            Question obj = (Question) validateAndFindPrimary("question", Question.class, command, errors);
            if (obj != null) {
                if (action.equals("display")) command.transferFromQuestion(obj);
            }

        } else if (entity.equals("unit")) {
            
            Unit obj = (Unit) validateAndFindPrimary("unit", Unit.class,  command, errors);
            if (obj != null) {
                
                // if action is "display", transfer properties to command:
                if (action.equals("display")) command.transferFromUnit(obj);
            }
            
        } else if (entity.equals("user")) {
            
            VitalUser obj = (VitalUser) validateAndFindPrimary("user", VitalUser.class, command, errors);
            if (obj != null) {
                
                ucm.decorateUser(obj, false);
                if (action.equals("display")) command.transferFromUser(obj);
            }
            
            
        } else if (entity.equals("worksite")) {
            
            VitalWorksite obj = (VitalWorksite) validateAndFindPrimary("worksite", VitalWorksite.class,  command, errors);
            if (obj != null) {
                // decorate worksite and init courseaffils (but not participants):
                ucm.decorateWorksite(obj, true, false);
                // if action is "display", transfer properties to command:
                if (action.equals("display")) {
                    command.transferFromWorksite(obj);
                    command.setCourseIdString(obj.getCourseIdString());
                }
            }
            
        }


        // foreach entity... find the entity on the command object
        return entity;
    }
    
    
}
