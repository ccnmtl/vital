package ccnmtl.vital3.commands;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import ccnmtl.vital3.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;

/**
 * SpecialActionsValidator operates on BasicAdminCommand objects. It requires an id parameter and retrieves
 * the assignment response with that id and confirms that it is a guided lesson. "error.missing.id" is
 * registered as both global and field errors if it was not found.
 */
public class DiscussionEntryValidator extends Vital3Validator {
    
    // validates BasicAdminCommand objects
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    static Set actionsNeedingAssignments;
    static Set actionsNeedingAssignmentResponses;
    static Set actionsNeedingEntries;
    
    static {
        
        actionsNeedingAssignments = new HashSet();
        actionsNeedingAssignmentResponses = new HashSet();
        actionsNeedingEntries = new HashSet();
        
        actionsNeedingAssignments.add("addDiscussionEntry");
        actionsNeedingAssignments.add("deleteDiscussionEntry");
        
        actionsNeedingAssignmentResponses.add("addDiscussionEntry");
        actionsNeedingAssignmentResponses.add("deleteDiscussionEntry");
        
        actionsNeedingEntries.add("deleteDiscussionEntry");
        
    }
    
    public void validate(Object commandObj, Errors errors) {        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String action = command.getAction();
        
        
        Assignment assignment;
        AssignmentResponse assignmentResponse;
        
        if (actionsNeedingAssignments.contains(command.getAction())) {
             logger.debug("assignmentNeeded... about to call validateAndFind");
             validateAndFind("assignment", Assignment.class, command, errors, true);
        }
        if (actionsNeedingAssignmentResponses.contains(command.getAction())) {
             logger.debug("assignmentNeeded... about to call validateAndFind");
             validateAndFind("assignmentResponse", AssignmentResponse.class, command, errors, true);
        }
        if (actionsNeedingEntries.contains(command.getAction())) {
             logger.debug("assignmentNeeded... about to call validateAndFind");
             validateAndFind("comment", Comment.class, command, errors, true);
        }
        logger.debug("Done with SpecialActionsValidator");
    }
}
