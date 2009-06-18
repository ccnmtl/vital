package ccnmtl.vital3.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.*;

/**
 * This controller is responsible for presenting a material to the user. Currently it is only implemented as
 * a video viewer. See ViewMaterialValidator for info on parameters.
 */
public class ViewMaterialController extends Vital3CommandController {
    
    /**
     * Protect this controller
     */
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        return UserCourseManager.STUDENT_ACCESS;
    }
    
    /**
     * Derive worksite from material
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        Material material = command.getMaterial();
        VitalWorksite worksite = material.getWorksite();
        ucm.decorateWorksite(worksite, false, false);
        
        return worksite;
    }
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
        // NOTE: The validator changes the "mode" parameter from "edit" to "new" if no "annotationId" was passed with it.
        // mode may also be "viewOnly". The type parameter will always be "video".
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        UserContextInfo userInfo = getUserContextInfo(request);
        VitalParticipant participant = userInfo.getParticipant();
        VitalWorksite worksite = userInfo.getWorksite();
        
        ModelAndView mav = null;
        
        if (errors.hasErrors()) {
            logger.debug("ViewMaterialController beginning, but there were errors in validation. Creating errorMAV...");
            mav = Vital3Utils.createErrorMAV(errors, this.messageSourceAccessor); 
        } else {
            
            try {
                String mode = command.getMode();
                String type = command.getType();
                Annotation note = command.getAnnotation();
                Material material = command.getMaterial();
                
                Set allCustomFields = Vital3Utils.initCollection(VitalWorksite.class, worksite, "customFields", CustomField.class);
                Vital3Utils.initCollections(CustomField.class, allCustomFields, "values", CustomFieldValue.class);
                Vital3Utils.initCollection(Material.class, material, "customFieldValues", CustomFieldValue.class);

                Vital3Utils.initCollection(Material.class, material, "units", Unit.class);
                Vital3Utils.initCollection(Material.class, material, "assignments", Assignment.class);
                
                logger.debug("ViewMaterialController beginning. mode = " + mode + ", type = " + type);
                
                HashMap model = new HashMap();
                model.put("mode", mode);
                model.put("action", "none");
                model.put("textFormatter", textFormatter);
                model.put("material", material);
                
                if (note != null) model.put("note", note);

                if (!mode.equals("viewonly")) {
                    
                    // construct a command object for passing to annotationController:
                    AnnotationCommand noteCommand = new AnnotationCommand();
                    noteCommand.setGroupBy("modificationDate");
                    noteCommand.setLimitBy("material");
                    noteCommand.setMaterial(material);
                    // run the noteQuery and get the model:
                    Map notesModel = annotationController.noteQuery(participant, noteCommand);
                    
                    // fill out our model, stealing some things from notesModel
                    Map tagMap = (Map) notesModel.get("tagMap");
                    model.put("tagMap", tagMap);
                    model.put("groupList", notesModel.get("groupList"));
                    model.put("groupBy", notesModel.get("groupBy"));
                    
                    // "all available tags" can be derived from the TagMap:
                    TreeSet availableTags = new TreeSet();
                    Collection tagMapTags = tagMap.values();
                    Iterator tagIter = tagMapTags.iterator();
                    while (tagIter.hasNext()) {
                        Set tagSet = (Set) tagIter.next();
                        // merge the tagSet into the availableTags set
                        availableTags.addAll(tagSet);
                    }
                    
                    model.put("availableTags", availableTags);
                                                        
                }
                mav = new ModelAndView("videoViewer", model);
                
            } catch (Exception e) {
                mav = Vital3Utils.createErrorMAV(e);
            }
        }
        
        // put "currentUser", "worksite", and "admin" into the model if available:
        Vital3Utils.putUserInfoIntoModel(userInfo, mav.getModel());
        
        return mav;
        
    }
    
    // annotationController used for retrieving notes:
    private AnnotationController annotationController;
    public AnnotationController getAnnotationController() {
        return this.annotationController;
    }
    public void setAnnotationController(AnnotationController ac) {
        this.annotationController = ac;
    }
    
}