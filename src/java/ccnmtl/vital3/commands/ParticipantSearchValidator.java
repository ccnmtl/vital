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
 * Very basic. Search doesn't do that much- just trigger basicadmincommand validation on the participant.
 */
public class ParticipantSearchValidator extends Vital3Validator {
    
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    public void validate(Object commandObj, Errors errors) {        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        // only check worksite affils if user is not admin:
        //logger.debug("Starting validate here with user check");
        
        validateAndFind("user", VitalUser.class, command, errors, true);
        //logger.debug(command.getUser());
        
        //logger.debug("Now checking worksite");
        validateAndFind("worksite", VitalWorksite.class, command, errors, true);
        
        //logger.debug("Done with validation.");
        
        }
}
