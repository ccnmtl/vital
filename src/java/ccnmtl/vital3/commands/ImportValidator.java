package ccnmtl.vital3.commands;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import ccnmtl.vital3.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;

/**
 * ImportValidator operates on ImportCommand objects intended for ImportController.
 */
public class ImportValidator extends Vital3Validator {
    
    // validates ImportCommand objects
    public boolean supports(Class clazz) {
        return clazz.equals(ImportCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        ImportCommand command = (ImportCommand) commandObj;
        
        command.setUnits(new HashSet());
        
        String mode = command.getMode();
        validateString(mode, new String[]{"chooseSource", "displayItems", "import"}, false, errors, "mode");
        if (mode == null) {
            mode = "chooseSource";
            command.setMode(mode);
        }
        
        VitalWorksite targetWorksite = (VitalWorksite) validateAndFind("worksite", VitalWorksite.class, command, errors, true);
        if (targetWorksite == null) errors.reject("error.missing.worksiteId");
        
        if (!mode.equals("chooseSource")) {
            
            VitalWorksite sourceWorksite = (VitalWorksite) validateAndFind("sourceWorksite", VitalWorksite.class, command, errors, true);
            
            if (mode.equals("import")) {
                
                if (!errors.hasGlobalErrors()) {
                    
                    Collection unitIds = command.getIds();
                    if (unitIds != null) {
                        
                        // get rid of unit id -1:
                        if (unitIds.remove(new Long(-1))) command.importLooseMaterials = true;
                        
                        if (unitIds.size() > 0) {
                            try {
                                List units = vital3DAO.findBySetOfPropertyValues(Unit.class, "id", unitIds);
                                command.setUnits(new HashSet(units));
                                
                            } catch (DataRetrievalFailureException e) {
                                errors.reject("error.nosuch.unit");
                            }
                        }
                        
                    }
                }
            }
        }
        
    }
    
}
