package ccnmtl.vital3.commands;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;

public abstract class Vital3Validator implements Validator {
    
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    // The required methods:
    public abstract boolean supports(Class clazz);
    public abstract void validate(Object commandObj, Errors errors);
    
    
    /************* VALIDATION UTILITY FUNCTIONS ***************/
    
    /**
     * Ensures that a String is equal to one out of an array of values you pass. If you pass "required" as true,
     * then the value may be null, but if it is not null, it must be one out of the values in the array!
     *@return  true if the validation passed. false otherwise. Errors are rejected in the errors object and are GLOBAL ERRORS.
     */
    public boolean validateString(String value, String[] validOptions, boolean required, Errors errors, String fieldName) {
                                  
        if (value == null) {
            if (required) {
                errors.reject("error.missing."+fieldName);
                return false;
            } else return true;
            
        } else {
            
            for (int i=0; i<validOptions.length; i++) {
                if (value.equals(validOptions[i])) return true;
            }
            errors.reject("error.invalid."+fieldName);
            return false;
        }
    }
    
    
    /**
     * Validate an integer according to a minimum and a maximum value
     * Incude Errors object for logging errors. Will use rejectValue(fieldName, "error.invalid."+fieldName).
     * Will return true if it existed and validation passed, false if it was missing or it failed.
     */
    public boolean validateInteger(Integer value, Integer minimum, Integer maximum, boolean required, Errors errors, String fieldName) {
        
        if (value != null) {
            if (value.compareTo(minimum) >= 0 && value.compareTo(maximum) <= 0) return true;
        }
        logger.debug("Failed integer validation for field '"+fieldName+"'. value was " + value + " min was " + minimum + " max was " + maximum);
        if (required) errors.rejectValue(fieldName,"error.invalid."+fieldName);
        return false;
    }
    
    
    /**
     * Validate the startDate/endDate which should 1. be required fields, 2. startDate comes after the endDate.
     * Include Errors object for logging errors. Will use rejectValue(fieldName, "error.invalid."+fieldName).
     * Will return true if it existed and validation passed, false if it was missing or it failed.
     */
    public boolean validateDate(Date startDate, Date endDate, Errors errors, String fieldName_first, String fieldName_second) {
        
        if (startDate == null) {
            errors.rejectValue(fieldName_first, "error.missing." + fieldName_first);
        	return false;
        
        } else if (endDate == null){       
            
            errors.rejectValue(fieldName_second, "error.missing." + fieldName_second);
        	return false;
        	
        } else if (startDate.compareTo(endDate) > 0) {
            errors.rejectValue(fieldName_first, "error.invalid." + fieldName_first + ".outoforder");
            return false;

        }
        
        return true;
    }
    
        
    /**
     * Puts the desired entity onto the command object, according to the data already there (xxxxId)
     * throws field error if the parameter was missing or global error if it could not be retrieved.
     * The parameter must be accessible through mapSet and mapGet for this to work.
     */
    protected Object validateAndFind(String entityName, Class entityClass, Vital3Command command, Errors errors, boolean required) {
        
        logger.debug("Vital3Validator.validateAndFind (" + entityName + ") beginning...");
        String entityIdName = entityName + "Id";
        Object obj = null;
        Long id = (Long) command.mapGet(entityIdName);
        
        if (id == null) {
            if (required) {
                if (!errors.hasFieldErrors(entityIdName)) errors.rejectValue(entityIdName,"error.choose."+entityName);
            }
        } else {
            obj = vital3DAO.findById(entityClass, id);
            if (obj == null) errors.reject("error.nosuch."+entityName);
            else {
                command.mapSet(entityName, obj);
                logger.debug("Vital3Validator.validateAndFind found and loaded it!");
            }
        }
        if (obj != null) {
            
            // preemptively decorate certain UCM classes... I might remove this if it is a performance lag
            if (entityName.equals("user")) {
                ucm.decorateUser((VitalUser)obj, false);
            } else if (entityName.equals("worksite")) {
                ucm.decorateWorksite((VitalWorksite)obj, false, false);
            }
        }
        return obj;
    }
    
    
    
    /**
     * Puts the desired entity onto the command object, according to the data already there (id, entity)
     * throws global error if it could not be found or the id parameter is missing
     */
    protected Object validateAndFindPrimary(String entityName, Class entityClass, Vital3Command command, Errors errors) {
        
        logger.debug("Vital3Validator.validateAndFindPrimary (" + entityName + ") beginning...");
        Object obj = null;
        Long id = command.getId();
        if (id == null) {
            // check for multiple ids. if there are, find them, put them on the command object. Always return null.
            ArrayList ids = command.getIds();
            // if there was no "id" or "ids" parameter, throw a global error:
            if (ids == null) errors.reject("error.missing.id");
            else {
                ArrayList entities = new ArrayList();
                Iterator idIter = ids.iterator();
                while (idIter.hasNext()) {
                    id = (Long) idIter.next();
                    obj = vital3DAO.findById(entityClass, id);
                    if (obj == null) {
                        // if the object could not be found, stop and throw a global error:
                        errors.reject("error.nosuch."+entityName);
                        logger.debug("Vital3Validator.validateAndFindPrimary (multi) could not find " + entityName + " id " + id);
                        return null;
                    } else {
                        entities.add(obj);
                        logger.debug("Vital3Validator.validateAndFindPrimary (multi) found and loaded it!");
                    }
                }
                command.setEntityList(entities);
            }
            logger.debug("Vital3Validator.validateAndFindPrimary (multi) is done");
            return null;
            
        } else {
            obj = vital3DAO.findById(entityClass, id);
            if (obj == null) errors.reject("error.nosuch."+entityName);
            else {
                command.mapSet(entityName, obj);
                logger.debug("Vital3Validator.validateAndFindPrimary found and loaded it!");
            }
        }
        return obj;
    }
    
    
    /************** MINI UTILITY FUNCTIONS ****************/
    
    /**
     * Gets a string value from a hashmap, creating an error if it was null.
     */
    protected String getHashedString(HashMap map, String key, Errors errors, String errorCode, String defaultError) {
        String param = (String) map.get(key);
        if (param == null) errors.reject(errorCode, defaultError);
        return param;
    }
    protected String getHashedString(HashMap map, String key, Errors errors, String errorCode) {
        String param = (String) map.get(key);
        if (param == null) errors.reject(errorCode);
        return param;
    }
    
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    
    public void setUserCourseManager(UserCourseManager ucm) { this.ucm = ucm; }
    public UserCourseManager getUserCourseManager() { return this.ucm; }
    
}