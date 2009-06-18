package ccnmtl.vital3.commands;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import ccnmtl.vital3.Annotation;
import ccnmtl.vital3.Assignment;
import ccnmtl.vital3.Material;
import ccnmtl.vital3.Unit;
import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.commands.AnnotationCommand;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.dao.Vital3DAO;

/**
* There are a few different types of requests that may come in to AnnotationController:
 * If action is null, this is interpreted as a request for the manage my notes page.
 * If action is noteQuery, this is interpreted as an ajax request for notes.
 * If action is submitNote, this is interpreted as an ajax request to insert/update a note.
 * If action is deleteNote, this is interpreted as an ajax request to delete a note.
 * 
 * For noteQuery actions, your request must contain parameters for "template" and "groupBy".
 * Optional parameters are "recent" and "limitBy" which both default to empty string.
 * Since the "manage my notes" page sends both time-related and limit-related info under "limitBy",
 * this annotationValidator resolves that confusion and in case of time-related limitBy, it will
 * re-assign that value to the "recent" parameter and assign "none" to the "limitBy" parameter.
 * If limitBy is empty, it will be assigned the value "none".
 */
public class AnnotationValidator extends Vital3Validator {
    
    public boolean supports(Class clazz) {
        return clazz.equals(AnnotationCommand.class);
    }
    
    public void validate(Object commandObj, Errors errors) {
        
        if (errors.hasErrors()) return;
        
        AnnotationCommand command = (AnnotationCommand) commandObj;
        
        String action = command.getAction();
        Long id = command.getId();
        
        if (action == null) {
            // require worksite for security
            validateAndFind("worksite", VitalWorksite.class, command, errors, true);
            
        } else {
            
            if (action.equals("noteQuery")) {
                
                // validate template, groupBy, and recent parameters:
                String template = command.getTemplate();
                if (!validateString(template, new String[]{"assetPopup","myNotes","essay"}, true, errors, "template")) return;
                String groupBy = command.getGroupBy();
                if (!validateString(groupBy, new String[]{"noteTitle","modificationDate","tag","materialTitle"}, true, errors, "groupBy")) return;
                String recent = command.getRecent();
                if (!validateString(recent, new String[]{"","week","today"}, false, errors, "recent")) return;
                
                // validate and parse limitBy
                // retrieve unit or assignment if appropriate
                
                Long limitId = null;
                String limitType = null;
                
                String limitBy = command.getLimitBy();
                
                if (limitBy == null || limitBy.equals("")) {
                    limitType = "none";
                } else if (limitBy.equals("today") || limitBy.equals("week")) {
                    // it doesn't quite make sense to limitBy a date, when that's what "recent" is for...
                    // so if limitBy is a time period, change "recent" to that time period and make limitBy "none":
                    limitType = "none";
                    command.setRecent(limitBy);
                    recent = limitBy;
                } else {
                    try {
                        int index = limitBy.indexOf("materialId_");
                        if (index > -1) {
                            limitType = "material";
                            limitId = new Long(limitBy.substring(11));
                            command.setId(limitId);
                            validateAndFindPrimary("material", Material.class, command, errors);
                        } else {
                            index = limitBy.indexOf("unitId_");
                            if (index > -1) {
                                limitType = "unit";
                                limitId = new Long(limitBy.substring(7));
                                command.setId(limitId);
                                validateAndFindPrimary("unit", Unit.class, command, errors);
                            } else {
                                index = limitBy.indexOf("assignmentId_");
                                if (index > -1) {
                                    limitType = "assignment";
                                    limitId = new Long(limitBy.substring(13));
                                    command.setId(limitId);
                                    validateAndFindPrimary("assignment", Assignment.class, command, errors);
                                } else throw new NumberFormatException();
                            }
                        }
                    } catch(NumberFormatException nfe) {
                        logger.info("Could not parse id string from value " + limitBy);
                        errors.reject("error.invalid.limitBy");
                    }
                }
                
                // require worksiteId for security:
                if (limitType.equals("none")) validateAndFind("worksite", VitalWorksite.class, command, errors, true);
                
                // set "limitBy" on command obj:
                command.setLimitBy(limitType);
                
                
            } else if (action.equals("submitNote")) {
                
                // validate and fetch parent Material
                Material parent = (Material) validateAndFind("material", Material.class, command, errors, true);
                logger.debug("Material is " + parent);
                    
                String clipBegin = command.getClipBegin();
                String clipEnd = command.getClipEnd();
                
                // default type is "clip". set whatever it is to lowercase.
                String type = command.getType();
                if (type == null) type = "clip";
                else type = type.toLowerCase();
                command.setType(type);
                
                if (clipBegin  == null) errors.reject("error.missing.clipBegin");
                
                if (type.equals("clip")) {
                    if (clipEnd == null) errors.reject("error.missing.clipEnd");
                    
                    // TODO: validate clipBegin < clipEnd
                    
                } else errors.reject("error.invalid.annotation.type");
                
                if (id != null) {
                    
                    // validate and fetch Annotation
                    Annotation note = (Annotation) validateAndFindPrimary("annotation", Annotation.class, command, errors);
                }
                
                // validate tags:
                Set tags = command.getTags();
                Set safeTags = new HashSet();
                if (tags != null) {
                    Iterator iter = tags.iterator();
                    while (iter.hasNext()) {
                        String tag = (String) iter.next();
                        // strip whitespace:
                        tag = StringUtils.stripToNull(tag);
                        if (tag == null) logger.debug("ignored empty tag.");
                        else safeTags.add(tag);
                    }
                }
                command.setTags(safeTags);
                
            } else if (action.equals("deleteNote")) {
                
                // validate and fetch Annotation.
                Annotation note = (Annotation) validateAndFindPrimary("annotation", Annotation.class, command, errors);
                
            } else if (action.equals("deleteNotes")) {
                
                // retrieve annotation list by ids in Vital3Validator 
                validateAndFindPrimary("annotation", Annotation.class, command, errors);
            
            } else errors.reject("error.invalid.action");
            
        }
        
    }
    

}
        
        