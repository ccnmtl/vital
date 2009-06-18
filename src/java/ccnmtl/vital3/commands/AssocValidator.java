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
 * AssocValidator validates and carries information intended for AssocController. It validates
 * BasicAdminCommand objects (BasicAdminCommand has everything we need, so we are repurposing it).
 * The idea is that it holds information used to manage associations between one entity (entity)
 * and potentially several instances of materials. The entity property is defined in BasicAdminCommand.
 * The id of the entity instance
 * is held in the "id" property inherited from Vital3Command. The ids of the material instances are held
 * in the "ids" property inherited from Vital3Command. AssocValidator will pull the entity instance
 * out of the database and store it in the appropriate property according to its entity type (e.g. Assignment).
 * AssocValidator will also pull out the material instances and store them in the entityList property
 * inherited from Vital3Command.
 */
public class AssocValidator extends Vital3Validator {
    
    // this validates BasicAdminCommand objects intended for AssocController
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        // validate basic parameter stuff:
        
        Long id = command.getId();
        if (id == null) errors.reject("error.missing.id");
        
        ArrayList ids = command.getIds();
        
        String entity = command.getEntity();
        validateString(entity, new String[]{"assignment","question","unit"}, true, errors, "entity");
        
        if (errors.hasGlobalErrors()) return;
        
        
        // set entityClass:
        Class entityClass;
        if (entity.equals("assignment")) entityClass = Assignment.class;
        else if (entity.equals("question")) entityClass = Question.class;
        else entityClass = Unit.class;
        
        // retrieve the primary entity:
        validateAndFindPrimary(entity, entityClass, command, errors);
        
        if (ids != null) {
            // trick validateAndFindPrimary into looking for the "ids" parameter by temporarily nullifying the "id" parameter:
            command.setId(null);
            // retrieve all the materials:
            validateAndFindPrimary("material", Material.class, command, errors);
            // reset the id parameter:
            command.setId(id);
        }
        
    }
        

    
    
}
