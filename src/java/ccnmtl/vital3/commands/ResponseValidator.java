package ccnmtl.vital3.commands;

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
public class ResponseValidator extends Vital3Validator {
    
    public static final HashSet allowedActions = new HashSet(Arrays.asList(new String[]{"display","update","new","insert","delete"}));
    public static final HashSet allowedEntities = new HashSet(Arrays.asList(new String[]{"assignmentResponse","comment"}));
    
    public boolean supports(Class clazz) {
        return clazz.equals(ResponseCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        logger.debug("Starting validate in RV");

        ResponseCommand command = (ResponseCommand) commandObj;

        // validate basic parameters (will register global errors if anything is wrong):
        String action = validateActionEntityId(command, errors);

        // NOTE: binding errors cause Field Errors, not Global Errors
        if (errors.hasGlobalErrors()) return;
        
        String entity = command.getEntity();
        
        
        
        // This block gets executed when a student starts an essay, then clicks "Save Draft", then:
        // a) clicks the browser "back" button once, edits the essay, and clicks "Save Draft" again,
        // b) clicks the browser "back" button twice, then attempts to start the essay again. 
        // The rest of the request is handled as an Update to the already-persisted essay, which is what the user expects.
        // REFACTOR NOTE: a less hacky way to do this would be to throw an error here, catch it in the handle function of
        // ResponseController, then redirect the request to a brand new update.
        
        
        if (entity.equals("assignmentResponse") && action.equals("insert")) {
            Assignment assignment = (Assignment) validateAndFind("assignment", Assignment.class, command, errors, true);
            if (assignment.isEssay()) {
                VitalParticipant participant = (VitalParticipant) validateAndFind("participant", VitalParticipant.class, command, errors, true);
                List results = vital3DAO.findByTwoPropertyValues(AssignmentResponse.class, "assignment", assignment, "participant", participant);
                if (results.size() > 0) {
                    // turn this into an update instead of an insert:
                    command.setAction("update");
                    AssignmentResponse existingResponse = (AssignmentResponse) results.iterator().next();
                    command.setId(existingResponse.getId());
                    //rerun basic sanity check:
                    action = validateActionEntityId(command, errors);        
                    if (errors.hasGlobalErrors()) return;
                }
            }
        }
        
        
        
        if (action.equals("delete") || action.equals("display") || action.equals("update")) {

            // this will put the entity on the command obj and (when action == display) transfer its properties:
            
            if (entity.equals("assignmentResponse")) {
                
                AssignmentResponse obj = (AssignmentResponse) validateAndFindPrimary("assignmentResponse", AssignmentResponse.class,  command, errors);
                if (obj != null && action.equals("display")) {
                    command.transferFromAssignmentResponse(obj);
                    command.setType(obj.getType());
                }
                
            } else if (entity.equals("comment")) {
                
                Comment obj = (Comment) validateAndFindPrimary("comment", Comment.class,  command, errors);
                if (obj != null && action.equals("display")) {
                    command.transferFromComment(obj);
                }
            }
            
        }

        if (errors.hasGlobalErrors()) return;

        // perform case-specific validation and retrievals. "delete" and "display" don't need any further validation.
        logger.debug("ResponseValidator.validate: beginning case-specific validation. Entity = " + entity + ", action = " + action);
        
        if (action.equals("insert") || action.equals("new") || action.equals("update")) {
            // these tend to follow the pattern:
            // if (new){ stuff for new; } else { stuff for both insert and update; if(insert) { stuff only for insert; } else { stuff only for update; }}
            
            //////##############################################################
            if (entity.equals("assignmentResponse")) {
                
                // validate and retrieve the assignment to which the student is responding
                Assignment assignment = (Assignment) validateAndFind("assignment", Assignment.class, command, errors, true);
                
                if (action.equals("new")) {
                    
                    if (assignment == null) {
                        throw new RuntimeException("Assignment not found.");
                    }
                    Set assignmentMaterials = Vital3Utils.initM2MCollection(Assignment.class, assignment, Material.class);
                    Set unitMaterials = Vital3Utils.initM2MCollection(Unit.class, assignment.getUnit(), Material.class);
                    
                    VitalParticipant participant = (VitalParticipant) validateAndFind("participant", VitalParticipant.class, command, errors, true);
                    
                    
                    if (assignment.isGuidedLesson()) {                    
                        List results = vital3DAO.findByTwoPropertyValues(AssignmentResponse.class, "assignment", assignment, "participant", participant);
                        if (results.size() > 0) errors.reject("error.duplicate.assignmentresponse");    
                        
                        Set questions = assignment.getQuestions();
                        Vital3Utils.initCollections(Question.class, questions, "answers", Answer.class);
                        Set questionMaterials = Vital3Utils.initM2MCollections(Question.class, questions, Material.class);
                    }
                    
                } else {
                    // stuff for both insert and update:
                    Question question = null;
                    if (assignment.isGuidedLesson() ) {
                        
                        question = (Question) validateAndFind("question", Question.class, command, errors, true);
                        
                        if (command.getText() != null && command.getText().length() > Answer.GL_ANSWER_CAPACITY) {
                            errors.reject("error.toomanychars.glresponse");
                        }
                        
                    } else if (assignment.isEssay()) {
                        
                        if (command.getText() != null && command.getText().length() > AssignmentResponse.ESSAY_CAPACITY) {
                            errors.reject("error.toomanychars.essayresponse");
                        }
                    }
                    
                    
                    if (action.equals("insert")) {
                        
                        VitalParticipant participant = (VitalParticipant) validateAndFind("participant", VitalParticipant.class, command, errors, true);
                        
                        if (!errors.hasErrors()) {
                            List results = vital3DAO.findByTwoPropertyValues(AssignmentResponse.class, "assignment", assignment, "participant", participant);
                            if (results.size() > 0) errors.reject("error.duplicate.assignmentresponse");
                        }
                        
                    } else {
                        // update
                        
                        AssignmentResponse assignmentResponse = command.getAssignmentResponse();
                        if (!assignmentResponse.getAssignment().equals(assignment)) throw new RuntimeException("AssignmentResponse.getAssignment not equal to command.getAssignment");
                        
                        // Can't update assignments once they're submitted:
                        if (assignmentResponse.isSubmitted()) errors.reject("error.assignmentResponse.submitted");
                        
                        if (!errors.hasErrors()) {
                            if (assignment.isGuidedLesson()) {
                                
                                // Don't let the same person submit more than one answer to the same question:
                                List duplicateAnswers = vital3DAO.findByTwoPropertyValues(Answer.class, "assignmentResponse", assignmentResponse, "question", question);
                                if (duplicateAnswers.size() > 0) {
                                    Iterator answerIterator = duplicateAnswers.iterator();
                                    while (answerIterator.hasNext()) {
                                        logger.warn ( "Attempted to submit duplicate Answer. Old one has id: " +  ((Answer) answerIterator.next()).getId());
                                    }
                                    errors.reject("error.duplicate.answer");
                                }
                                
                            }
                        }
                    }
                    
                }
                
                //////##############################################################
            } else if (entity.equals("comment")) {
                
                if (action.equals("new")) throw new RuntimeException("'new' is not supported for comments");
                
                // (the validation for insert and update is identical)
                
                validateAndFind("participant", VitalParticipant.class, command, errors, true);
                
                if (command.getAnswerId()!= null) {
                    //guided lesson comment
                    validateAndFind("answer", Answer.class, command, errors, true);
                    
                } else if (command.getAssignmentResponseId() != null) {
                    //essay comment
                    validateAndFind("assignmentResponse", AssignmentResponse.class, command, errors, true);
                    
                } else {
                    throw new RuntimeException("Neither an answer nor an assignment response was found in validateForInsert.");
                }
                
            }
            
            
        }

        logger.debug("ResponseValidator.validate: completed validation!");
    }


    /**
     * Full validation of entity, action, and id parameters.
     *@param command          The ResponseCommand object.
     *@param errors           The Errors object, which will be used to report errors in this method.
     *@param validActions     Which actions are permissable, or null if you will allow any action. If this
     *                        is not null, and action equals something else, an error will be reported.
     *@return                 Returns the action string.
     */
    public String validateActionEntityId(ResponseCommand command, Errors errors) {

        String action = command.getAction();
        String entity = command.getEntity();
		Long id = command.getId();
        
        logger.debug ("## RV.validateActionEntityId: Action is " + action + " Entity is " + entity + " id is " + id);
        
        if (action == null) errors.reject("error.missing.action");
        else {
            
            if (!allowedActions.contains(action)) errors.reject("error.nosuch.action");
            
            if (id == null) {
                // "id" parameter required for display & update & delete requires "id" OR "ids":
                if (action.equals("display") || action.equals("update") || action.equals("delete")) errors.reject("error.missing.id");
            }
        }
        
		if (entity == null) errors.reject("error.missing.entity");
        if (!allowedEntities.contains(entity)) errors.reject("error.nosuch.entity");

        return action;

    }
    
    
}

