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
 * WorksiteIdValidator operates on Vital3Command objects. All it does is require a worksiteId parameter and retrieve
 * it if it was passed. "error.missing.worksiteId" is registered as both global and field errors if it was not found.
 */
public class WorksiteIdValidator extends Vital3Validator {
    
    // validates Vital3Command objects
    public boolean supports(Class clazz) {
        return clazz.equals(Vital3Command.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        Vital3Command command = (Vital3Command) commandObj;
        
        VitalWorksite worksite = (VitalWorksite) validateAndFind("worksite", VitalWorksite.class, command, errors, true);
        
        if (worksite == null) errors.reject("error.missing.worksiteId");
        
    }
    
}
