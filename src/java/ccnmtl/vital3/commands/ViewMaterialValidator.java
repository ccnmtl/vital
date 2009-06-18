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
 * Validates requests for ViewMaterialController. Uses a BasicAdminCommand object because all
 * the fields are there. Parameters expected:
 * mode: should be "edit" or "viewOnly". Default (in case of null) is "viewOnly". "edit" is for editing an annotation,
 *       or creating a new one. If it is accompanied by a valid annotationId parameter, that annotation will be edited.
 *       If no annotationId parameter is passed, the annotation form will be displayed in "new" mode. "viewOnly" will
 *       display only the UI for viewing the material, without any UI for annotating.
 * type: "video" is the only option and null will default to "video".
 * id: the material id. Required.
 */
public class ViewMaterialValidator extends Vital3Validator {
    
    // this validates BasicAdminCommand objects intended for ViewMaterialController
    public boolean supports(Class clazz) {
        return clazz.equals(BasicAdminCommand.class);
    }
    
    
    public void validate(Object commandObj, Errors errors) {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        // validate basic parameter stuff:
        
        Long id = command.getId();
        if (id == null) errors.reject("error.missing.id");
        
        String mode = command.getMode();
        validateString(mode, new String[]{"edit","new","viewonly"}, false, errors, "mode");
        if (mode == null) mode = "viewonly";
        
        String type = command.getType();
        validateString(type, new String[]{"video"}, false, errors, "type");
        if (type == null) type = "video";
        
        if (errors.hasGlobalErrors()) return;
        
        // retrieve the material:
        validateAndFindPrimary("material", Material.class, command, errors);
        
        // if its there, retrieve the annotation:
        Annotation note = (Annotation) validateAndFind("annotation", Annotation.class, command, errors, false);
        
        // set mode to "new" if mode was "edit" and there was no annotationId.
        if (note == null && mode.equals("edit")) mode = "new";
        
    }
    
    
    
    
}
