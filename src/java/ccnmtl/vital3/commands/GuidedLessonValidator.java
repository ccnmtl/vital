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
 * GuidedLessonValidator operates on BasicAdminCommand objects. It requires an id parameter and retrieves
 * the assignment response with that id and confirms that it is a guided lesson. "error.missing.id" is
 * registered as both global and field errors if it was not found.
 */
public class GuidedLessonValidator extends Vital3Validator {
    
    // validates BasicAdminCommand objects
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        AssignmentResponse response = (AssignmentResponse) validateAndFindPrimary("assignmentResponse", AssignmentResponse.class, command, errors);
        
        if (response == null) errors.reject("error.missing.id");
        else {
            String type = response.getType();
            if (type == null || !type.equals(Assignment.GUIDED_LESSON)) errors.reject("error.assignment.notgl");
        }
    }
    
}
