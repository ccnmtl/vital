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

/**
 * ListingValidator operates on BasicAdminCommand objects intended for ListingController.
 */
public class ListingValidator extends Vital3Validator {
    
    // validates Vital3Command objects
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        Long id = command.getId();
        String mode = command.getMode();
        
        // mode and id are required for almost all requests:
        if (mode == null) errors.reject("error.missing.mode");
        if (id == null && mode != null && !mode.equals("user")) errors.reject("error.missing.id");
        
        if (!errors.hasGlobalErrors()) {
            
            if (mode.equals("amAssoc")) {
                validateAndFindPrimary("assignment", Assignment.class, command, errors);
                
            } else if (mode.equals("qmAssoc")) {
                validateAndFindPrimary("question", Question.class, command, errors);
                
            } else if (mode.equals("umAssoc")) {
                validateAndFindPrimary("unit", Unit.class, command, errors);
                
            } else if (mode.equals("glQuestions")) {
                validateAndFindPrimary("assignment", Assignment.class, command, errors);
                
            } else if (mode.equals("customField")) {
                validateAndFindPrimary("worksite", VitalWorksite.class, command, errors);
                
            } else if (mode.equals("roster")) {
                validateAndFindPrimary("worksite", VitalWorksite.class, command, errors);
                
            } else if (mode.equals("user")) {
                // nothing to do
                
            } else errors.reject("error.invalid.mode");
            
        }
    }
    
}
