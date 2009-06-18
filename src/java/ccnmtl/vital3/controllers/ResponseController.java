package ccnmtl.vital3.controllers;

import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
//import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.utils.*;

import ccnmtl.vital3.utils.TextFormatter;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3AuthViolationException;
import ccnmtl.vital3.utils.Vital3MessageCodesResolver;
import ccnmtl.vital3.utils.Vital3Utils;

import ccnmtl.vital3.dao.Vital3DAO;

/**
 * For assignment responses, essay-comments, and GL-answers. May be used by any member of a worksite.
 */
public class ResponseController extends Vital3CommandController {


    private static HashMap templateNames;

    static {
        templateNames = new HashMap();
        templateNames.put("essay", "essayWorkspace");
        templateNames.put("gl", "glWorkspace");
        templateNames.put("discussion", "discussionWorkspace");
    }

    
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * Should never return null.
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        VitalWorksite worksite = null;
        ResponseCommand command = (ResponseCommand) commandObj;
        String entity = command.getEntity();
        String action = command.getAction();
        
        logger.info ("RC.getRequestedWorksite started. Entity = " + entity + ", action = " + action);		
        
        if (action.equals("delete") || action.equals("display") || action.equals("update")) {
            
            // for actions involving an existing entity, derive its parent worksite:
            Persistable targetObj = (Persistable) command.mapGet(entity);
            
            if (targetObj == null) throw new RuntimeException("no entity found on command object");
            else worksite = targetObj.getRelatedWorksite();
            
        } else {
            // action = insert or new... require knowing which param to look for in each entity case
            if (entity.equals("assignmentResponse"))
                worksite = command.getAssignment().getRelatedWorksite();
            else if (entity.equals("comment")) {
                AssignmentResponse ar = command.getAssignmentResponse();
                if (ar != null) worksite = ar.getRelatedWorksite();
                else worksite = command.getAnswer().getRelatedWorksite();
            }
        }
        
        if (worksite != null) {
            ucm.decorateWorksite(worksite, false, false);
            command.setWorksite(worksite);
        }
        return worksite;
    }
    
    
    /**
     * Requires STUDENT_ACCESS for everything except comments
     */
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        
        ResponseCommand command = (ResponseCommand) commandObj;
        String entity = command.getEntity();
        
        if (entity.equals("comment")) {
            return UserCourseManager.TA_ACCESS;
        }
        // default case:
        return UserCourseManager.STUDENT_ACCESS;
    }
    

    /**
     * This method is called after binding occurs, and if the request was a POST.
     * This will either call onSubmit or showForm depending on certain factors (look at the code).
	 */
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
        logger.debug("ResponseController.handle beginning...");
        ModelAndView mav = null;
        
        ResponseCommand command = (ResponseCommand) commandObj;
        String action = command.getAction();
        
        if (errors.hasErrors()) {
            logger.debug("There were errors.");
            logger.debug (errors.toString());
            // if we only have a field error, change the action (the template logic needs this):
            if (!errors.hasGlobalErrors()) {
                if (action.equals("update")) command.setAction("display");
                if (action.equals("insert")) command.setAction("new");
            }
            
            logger.debug("calling showForm with errors");
            mav = showForm(request, response, errors);
            
        } else if (action.equals("display") || action.equals("new")) {
            
            // route "display" and "new" actions to the showForm method:
            logger.debug("calling showForm...");
            mav = showForm(request, response, errors);
            
        } else {
            
            logger.debug("No errors -> processing submit");
            // route "delete", "update", and "insert" actions to onSubmit:
            mav = onSubmit(request, response, command, errors);
        }
        
        logger.info("Done with handle.");
        
        return mav;
    }


    /**
     * This method is called either when the form will be displayed for the first time, or when there were submission errors,
     * or after a redirect following a successful insert or update.
     * This is what happens when a GET request is received for this controller. Requires parameters for "entity" and "id".
     * This delegates responsibility to the rest of the superclass showForm method chain, which merges referenceData into
     * the model and constructs the ModelAndView.
     * errors.getMap() yields a map which contains a key whose name is the command name, which holds the command object.
     * Note that this method is the four-argument version of showForm. The processFormSubmission method calls the three-argument
     * version of showForm, which checks security and then calls this method.
     */

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        
        logger.debug("ResponseController.showForm beginning...");
        ModelAndView mav = null;
        
        Map controlModel = errors.getModel();

        ResponseCommand command = (ResponseCommand) errors.getModel().get("responseCommand");
        UserContextInfo userInfo = getUserContextInfo(request);
        String entity = command.getEntity();
        String action = command.getAction();

        String targetTemplate = getTargetTemplate(entity, errors);

        logger.debug("**********************************");
        if (!errors.hasGlobalErrors()) {

            logger.debug("**********************************aaa");

            logger.debug("There were no global errors.");
            
            VitalWorksite worksite = userInfo.getWorksite();
            VitalUser user = userInfo.getUser();
           	VitalParticipant participant = userInfo.getParticipant();
            
            controlModel.put("worksite", worksite);
            logger.debug("put worksite '" + worksite.getTitle() + "' into the model.");
            
            
            String filter = command.getFilter();
            String groupBy = command.getGroupBy();
            logger.debug("Added filter " + filter);
            logger.debug("Added groupBy " + groupBy);
            controlModel.put("filter", filter);
            controlModel.put("groupBy", groupBy);

            
            // add "action" to the model:
            controlModel.put("action",command.getAction());

            
            Assignment assignment = command.getAssignment();
            AssignmentResponse assignmentResponse = null;
            controlModel.put("assignment", assignment);
            boolean export = command.getExport() != null && command.getExport().equals("true");
            boolean preview =  command.getPreview() != null &&  command.getPreview().equals("true");
            boolean isMyAssignment = false;
            boolean myAssignmentWasSubmitted = false;
            boolean canViewOthers = false;
            boolean myAssignmentIsNull = true;
            
            if (userInfo.hasPermission(UserCourseManager.CAN_VIEW_OTHERS_ASSIGNMENTS)) canViewOthers = true;
            
            // init custom fields and values for assignmentMaterials:
            Set assignmentMaterials = Vital3Utils.initM2MCollection(Assignment.class, assignment, Material.class);
            Set allCustomFields = worksite.getCustomFields();
            Vital3Utils.initCollections(CustomField.class, allCustomFields, "values", CustomFieldValue.class);
            Vital3Utils.initCollections(Material.class, assignmentMaterials, "customFieldValues", CustomFieldValue.class);
            
            Set participantsForDecoration = new HashSet();
            
            if (action.equals("new")) {
                
                // check that a response to this assignment by this participant doesn't already exist...
                //this case might happen, e.g., if someone hits the back button on their browser after entering one question on a guided lesson.
                AssignmentResponse ar = assignment.getParticipantResponse(participant);
                if( ar != null) {
                    logger.debug("A participant can't submit more than one response to a given assignment. Not allowing the creation of a new response.");
                    String successUrl = assignmentResponseRedirectURL( assignment, ar.getStatus(), ar.getId(), worksite.getId(), export, preview);
                    return new ModelAndView(new RedirectView(successUrl,true));
                }
                controlModel.put("responder", participant);
                controlModel.put("assignmentResponseStatus", assignment.getStatus(participant));
                isMyAssignment = true;
                
                if (assignment.isGuidedLesson()) {
                    controlModel.put ("questionIndex", new Integer (0));
                }
                
                if (assignment.isDiscussion()) {
                    /* Discussion assignments need to know the full name of
                    each participant, regardless of the type of user.
                    */
                    Set responses = assignment.getResponses();
                    Iterator responsesIter = responses.iterator();
                    while (responsesIter.hasNext()) {
                        ar = (AssignmentResponse) responsesIter.next();
                        participantsForDecoration.add(ar.getParticipant());
                        Set comments = Vital3Utils.initCollection(AssignmentResponse.class, ar, "comments", Comment.class);
                    }
                    //these are currently not used--
                    //the grouping and filtering happens
                    //exclusively on the front end.
                    //But they might be in the future.
                    groupBy = command.getGroupBy();
                    filter = command.getFilter();
                    logger.debug ("Groupby is: " + groupBy);
                    logger.debug ("Filter is: "  + filter);
                    if (groupBy !=  null)  {
                        controlModel.put("groupBy", groupBy);
                    }
                    if (filter !=  null)  {
                        controlModel.put("filter", filter);
                    }
                }
            } else if (action.equals("display")) {
                
                assignmentResponse = command.getAssignmentResponse();
                
                controlModel.put("thisAssignmentResponse", assignmentResponse);
                targetTemplate = getTargetTemplate(assignmentResponse.getType(), errors);
                logger.debug("switching targetTemplate to " + targetTemplate);
                
                // the CLIU's assignmentResponse (for reference):
                AssignmentResponse myAR = assignment.getParticipantResponse(participant);
                if (myAR != null) {
                    myAssignmentIsNull = false;
                    isMyAssignment = assignmentResponse.getId().equals(myAR.getId());
                    myAssignmentWasSubmitted = myAR.isSubmitted();
                    logger.debug("myAR was not null. isMyAss = " + isMyAssignment + ", myAssWasSubmitted = " + myAssignmentWasSubmitted);
                }
                VitalParticipant responder = assignmentResponse.getParticipant();
                controlModel.put("responder", responder);
                controlModel.put("assignmentResponseStatus", assignment.getStatus(responder));
                
                if (userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM))
                    controlModel.put("commentsform", "true");
                   
                logger.debug("CLIU's assignment is " + myAR);
                logger.debug("The assignment we're trying to look at is:" + assignmentResponse);
                
                ////
                // Obscure bug fix:
                // Unauthenticated users who load this page, are bounced back to login page, login, and are redirected to this controller
                // need to have their participant object and the responder's participant object decorated:
                if (participant.getUser() == null) {
                    logger.debug ("Decorating the participant.");
                    ucm.decorateParticipant(participant);
                }
                if (responder.getUser() == null) {
                    logger.debug ("Decorating the responder.");
                    ucm.decorateParticipant(responder);
                }
                ////
                // End obscure bug fix.
                
                
                // For discussion assignments, you only are allowed to see
                // your own response. (It displays *all* the discussion entries.)
                //
                
                /*
                if (assignment.isDiscussion() && !isMyAssignment ) {
                
                    logger.debug("Participant is trying to view someone else's response, which doesn't make sense for a discussion assignment. Redirecting to the participant's response.");
                    
                    if (myAssignmentIsNull) {
                        logger.debug("Participant has no response on record for this assignment, so creating one.");
                    }
                    
                    String r_action = myAssignmentIsNull ? "new" : "display";
                    String r_id = (myAssignmentIsNull ? assignment.getId() : myAR.getId()).toString();
                    String discussion_redirect = "response.smvc?entity=assignmentResponse";
                    logDebug ("BUT I  ALSO HERE");
                    
                    discussion_redirect += "&action=" + r_action;
                    discussion_redirect += "&id=" + r_id;
                    discussion_redirect += "&participantId=" + participant.getId();
                    return Vital3Utils.redirectModelAndView(discussion_redirect);
                
                
                }
                */
                if (!canViewOthers && !assignment.isDiscussion()) {
                    // You're  NOT allowed to view others' essay or guided lesson assignments before submitting your own.
                    if (!myAssignmentWasSubmitted && !isMyAssignment) {
                        logger.debug("Requesting to view another person's assignment, but CLIU is not submitted.");
                        logger.debug("CLIU participant is: " + participant.getUser().getUserIdString());
                        logger.debug("The participant who we're trying to look at is " + responder.getUser().getUserIdString());
                        
                        return Vital3Utils.redirectModelAndView("error.smvc", new RuntimeException("You may not view the responses of others until you have submitted your own."));
                    }
                }
                
                if (assignment.isGuidedLesson()) {
                    // init for template:
                    assignmentResponse.getAnswers().size();
                    
                    Set questions = assignment.getQuestions();
                    Vital3Utils.initCollections(Question.class, questions, "answers", Answer.class);
                    Set questionMaterials = Vital3Utils.initM2MCollections(Question.class, questions, Material.class);
                    // also need to initialize all the currently answered questions so we can count them...
                    Set answers = assignmentResponse.getAnswers();
                    controlModel.put ("questionIndex", new Integer (assignment.getNumberOfQuestionsAnswered(participant)));
                    
                } else if (assignment.isEssay()) {
                    
                    Set comments = Vital3Utils.initCollection(AssignmentResponse.class, command.getAssignmentResponse(), "comments", Comment.class);
                    Iterator commentsIter = comments.iterator();
                    while (commentsIter.hasNext()) {
                        Comment comment = (Comment) commentsIter.next();
                        participantsForDecoration.add(comment.getParticipant());
                    }
                    
                } else if (assignment.isDiscussion()) {
                    /* Discussion assignments need to know the full name of
                    each participant, regardless of the type of user.
                    */
                    Set responses = assignment.getResponses();
                    Iterator responsesIter = responses.iterator();
                    Set all_the_comments = new HashSet();
                    
                    while (responsesIter.hasNext()) {
                        AssignmentResponse ar = (AssignmentResponse) responsesIter.next();
                        participantsForDecoration.add(ar.getParticipant());
                     
                     // also init all the comments for this assignment response.
                     
                      Set comments = Vital3Utils.initCollection(AssignmentResponse.class, ar, "comments", Comment.class);
                      all_the_comments.add(comments);
                      
                    }
                    controlModel.put("all_the_comments", all_the_comments);
                    
                    Vital3Utils.initCollections(AssignmentResponse.class, responses, "comments", Comment.class);
                    if (!isMyAssignment ) {
                        logger.debug("Participant is trying to view someone else's response, which doesn't make sense for a discussion assignment. Redirecting to the participant's response.");
                        if (myAssignmentIsNull) {
                            logger.debug("Participant has no response on record for this assignment, so creating one.");
                        }
                        String r_action = myAssignmentIsNull ? "new" : "display";
                        String r_id = (myAssignmentIsNull ? assignment.getId() : myAR.getId()).toString();
                        String discussion_redirect = "response.smvc?entity=assignmentResponse&type=discussion";
                        
                        discussion_redirect += "&action=" + r_action;
                        discussion_redirect += "&assignmentId=" + r_id;
                        discussion_redirect += "&participantId=" + participant.getId();
                        
                        logger.debug ("Redirecting to " + discussion_redirect);
                        return Vital3Utils.redirectModelAndView(discussion_redirect);
                    }
                    
                }
                
                
                
                
                if (command.getAssignmentResponse().hasSubmittedCommentsFor(participant)) controlModel.put("hasSubmittedCommentsForParticipant", "true" );
                controlModel.put("assignmentResponseStatus", assignment.getStatus(participant));
                controlModel.put("participant", participant);
                
                if (command.getExport() !=  null && command.getExport().equals("true"))  {
                    controlModel.put("export", "true");
                }
                
                if (command.getPreview() != null && command.getPreview().equals("true")) {
                    controlModel.put("preview", "true");
                }
                
            }
            
            if (canViewOthers || myAssignmentWasSubmitted) {
                
                // if you are allowed to view others' assignments before submitting your own OR your assignment is already submitted,
                // load the info for the dropdown menu:
                Set responses = assignment.getResponses();
                Iterator responsesIter = responses.iterator();
                
                while (responsesIter.hasNext()) {
                    AssignmentResponse ar = (AssignmentResponse) responsesIter.next();
                    // the template needs each person's full name!
                    participantsForDecoration.add(ar.getParticipant());
                }
            }
            
            if (participantsForDecoration.size() > 0) {
                ucm.decorateParticipants(participantsForDecoration);
            }
            
            if (isMyAssignment && assignment.isEssay()) {
                
                logger.debug("Adding the notes...");
                // call the noteQuery method of AnnotationController object to get the model(including groupList)
                AnnotationCommand noteCommand = new AnnotationCommand();
                noteCommand.setGroupBy("materialTitle");
                noteCommand.setLimitBy("assignment");
                noteCommand.setAssignment(assignment);
                Map model = annotationController.noteQuery(participant, noteCommand);
                controlModel.put("groupList", model.get("groupList"));
                controlModel.put("tagMap", model.get("tagMap"));
                
                controlModel.put("ams", assignmentMaterials);
                // controlModel.put("ums", unitMaterials);
                
                //Which materials have annotations?
                Set materialsSet = new HashSet();
                materialsSet.addAll(assignmentMaterials);
                Set materialsWithAnnotations = new HashSet();
                Iterator iter = vital3DAO.getAnnotations( participant, materialsSet, null).iterator();
                logger.debug("Starting to add annos.");
                while(iter.hasNext()) {
                    materialsWithAnnotations.add(((Annotation) iter.next()).getMaterial().getId());
                    logger.debug("Now materialsWithAnnotations has " + materialsWithAnnotations.size() );
                }
                logger.debug("Done adding annos.");
                controlModel.put("materialsWithAnnotations", materialsWithAnnotations);
                
                
            }
            
            targetTemplate = getTargetTemplate(command.getType(), errors);
            logger.debug("setting targetTemplate to: " + targetTemplate);
            
            controlModel.put("myAssignmentIsNull", new Boolean(myAssignmentIsNull));
            controlModel.put("isMyAssignment", new Boolean(isMyAssignment));
            controlModel.put("canViewOthers", new Boolean(canViewOthers));
            controlModel.put("myAssignmentWasSubmitted", new Boolean(myAssignmentWasSubmitted));
            
        } else {
            
            logger.debug("There were global errors! Preparing the Model & View for error page...");
            // put errors into the model:
            String errorString = Vital3Utils.convertErrorsToString(errors, this.messageSourceAccessor);
            controlModel.put("message", errorString);
        }
        
        // put "currentUser", "participant" "worksite", and "admin" into the model if available:
        Vital3Utils.putUserInfoIntoModel(userInfo, controlModel);
        
        controlModel.put("textFormatter", this.getTextFormatter());
        
        logger.debug("ResponseController.showForm complete. Going into showForm and returning model & view...");
        return new ModelAndView(targetTemplate, controlModel);

    }


    /**
     * Submit callback with all parameters. Called in case of submit without errors
	 * reported by the registered validator, or on every submit if no validator.
	 * @param request current servlet request
	 * @param response current servlet response
	 * @param command form object with request parameters bound onto it
	 * @param errors Errors instance without errors (subclass can add errors if it wants to)
	 * @return the prepared model and view, or null
	 * @throws Exception in case of errors
     */


	protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response, Object commandObj, BindException errors) throws Exception {

        ResponseCommand command = (ResponseCommand) commandObj;
     	// test
        // parse and/or validate parameters:
        String action = command.getAction();
        String entity = command.getEntity();
        Map model = errors.getModel();
        VitalWorksite worksite = command.getWorksite();

        logger.debug("ResponseController.onSubmit beginning... (action=" +action+ " entity=" +entity+ ")");
        try {
            
            if (action.equals("insert") || action.equals("update")) {
                
                //////##############################################################
                if (entity.equals("assignmentResponse")) {
                    
                    AssignmentResponse assResponse = null;
                    Assignment assignment = command.getAssignment();
                    
                    // lift the answerText off the command, to use later for inserting the new answer:
                    String answerText = null;
                    if (assignment.isGuidedLesson()) {
                        // The Text field for guided lessons is always null (See AssignmentResponse.java for more info.)
                        answerText = command.getText();
                        command.setText(null);
                    }
                    
                    if (action.equals("insert")) {
                        
                        // set response status to 1 for one-question GL responses:
                        if (assignment.isGuidedLesson() && assignment.getQuestions().size() == 1) {
                            command.setStatus(new Integer(1));
                        }
                        
                        // create the AR: date is null, status comes from the command.
                        // Assignment assignment, VitalParticipant participant, Date dateSubmitted, int status, String text
                        // note: dateSubmitted = null.
                         
                        assResponse = new AssignmentResponse(assignment, command.getParticipant(), null, command.getStatus().intValue(), null);                        
                    } else {
                        // update
                        assResponse = command.getAssignmentResponse();
                        
                        // set response status to 1 if final GL answer:
                        if (assignment.isGuidedLesson() && assResponse.getAnswers().size() + 1 == assignment.getQuestions().size()) {
                            command.setStatus(new Integer(1));
                        }
                    }
                    
                    command.transferToAssignmentResponse(assResponse);
                    if (command.getStatus().intValue() == 1) assResponse.setDateSubmitted(new Date());

                    vital3DAO.save(AssignmentResponse.class, assResponse);
                    vital3DAO.save(Assignment.class, assignment); // save this to make sure its collections are updated.
                    
                    // insert the new answer using the answerText we took from the command obj:
                    if (assignment.isGuidedLesson()) {
                        Question answerQuestion = command.getQuestion();
                        
                        Answer answer = new Answer(assResponse, answerQuestion, answerText);
                        vital3DAO.save(Answer.class, answer);
                        vital3DAO.save(Question.class, answerQuestion); // save this to make sure its collections are updated.
                    }
                    
                    
                    //////##############################################################
                } else if (entity.equals("comment")) {
                    
                    Comment targetObj = null;
                    
                    if (action.equals("insert")) {
                        
                        if (command.getAnswer() != null) {
                            //guided lesson comment
                            targetObj = new Comment(command.getAnswer(), null,  command.getParticipant(), new Date(), null, null, null);
                        } else if (command.getAssignmentResponse() != null) {
                            //essay comment
                            targetObj = new Comment(null, command.getAssignmentResponse(),  command.getParticipant(), new Date(), null, null, null);
                        }
                        // set status, text, and type:
                        command.transferToComment(targetObj);
                        
                    } else {
                        // update:
                        targetObj = command.transferToComment(command.getComment());
                    }
                    
                    vital3DAO.save(Comment.class, targetObj);
                }
                
                
                
                
                
            }

        } catch(DataIntegrityViolationException dive) {
            // this happens when attempting to delete an entity before its children
            logger.info("DataIntegrityViolationException during " + action + " of " + entity, dive);
            errors.reject("error.database.integrity."+entity);

        } catch(DataAccessException dae) {
            // this could be anything... there are numerous subclasses which are more specific
            logger.info("DataAccessException during " + action + " of " + entity, dae);
            errors.reject("error.database");

        } catch (UnsupportedOperationException e) {
            // so far this only happens when a participant was about to be created for an existing user/worksite pair
            logger.info("User tried to create participant for existing user/worksite pair");
            errors.reject("error.duplicate.participant");
        }
        // if errors occur, delegate to showForm with errors.
        if (errors.hasErrors()) {
            
            logger.debug(errors.getMessage());
            System.out.println(errors.getMessage());
            return showForm(request, response, errors);
        }
        
        String successUrl = null;
        
        if (entity.equals("assignmentResponse")) {
            Long id = null;
            boolean export = command.getExport() != null && command.getExport().equals("true");
            boolean preview = command.getPreview() != null && command.getPreview().equals("true");
            if (action.equals("insert")) {
                List results = vital3DAO.findByTwoPropertyValues(AssignmentResponse.class, "assignment", command.getAssignment(), "participant", command.getParticipant());
                if (results.size() != 1) throw new Exception("There were " + results.size() + " responses with that assignment and participant!");
                AssignmentResponse newAR = (AssignmentResponse) results.get(0);
                id = newAR.getId();
                successUrl = "response.smvc?action=display&entity=assignmentResponse&id=" + id;
            } else {
            	id = command.getId();
            }
	        successUrl = assignmentResponseRedirectURL( command.getAssignment(), command.getStatus(), id, worksite.getId() , export, preview);
            
        } else if (entity.equals("comment")) {
             if (command.getAnswer() != null) {
                 successUrl = "reviewGuidedLesson.smvc?id=" + command.getAssignmentResponseId();
             }else if (command.getAssignmentResponse() != null){
                 successUrl = "response.smvc?action=display&entity=assignmentResponse&id=" + command.getAssignmentResponseId() + "&worksiteId=" + worksite.getId();
             } else {
                 throw new RuntimeException("Command had neither an answer nor an assignment response to redirect to.");
             }
             
         }
         else successUrl = "materialsLib.smvc?worksiteId=" + worksite.getId() + "&message=" + action + "+successful";
         
         logger.debug("ResponseController.onSubmit completed! Sending redirect: " + successUrl);
         
         return new ModelAndView(new RedirectView(successUrl,true));
    }


    private String assignmentResponseRedirectURL( Assignment assignment, Integer status, Long assignmentResponseId, Long worksiteId, boolean export, boolean preview) {

        if(assignment == null) throw new RuntimeException("No assignment passed.");
        ///Submitted GL responses are displayed in the special reviewGuidedLesson controller:
        if (assignment.isGuidedLesson() && status.intValue() == 1)
            return "reviewGuidedLesson.smvc?id=" + assignmentResponseId;

        // Essay responses and unsubmitted GL responses are displayed by this controller:
		String successUrl = "response.smvc?action=display&entity=assignmentResponse&id=" + assignmentResponseId;
        if (export)  successUrl += "&export=true";
        if (preview) successUrl += "&preview=true";
		if (assignment.isGuidedLesson()) successUrl += "&type=gl";
		logger.debug ("Returning " + successUrl);
		return successUrl;

    }
    

    
    // Provides the name of the template appropriate for each entity:
    private String getTargetTemplate(String nickname, Errors errors) {
        if (errors.hasGlobalErrors()) logger.debug("ResponseController: There were global errors");
        if (!(errors.hasGlobalErrors() || nickname == null)) {
            String templateName = (String) templateNames.get(nickname);
            if (templateName != null) return templateName;
        }
        return "error";
    }
    
    protected AnnotationController annotationController;
    public AnnotationController getAnnotationController() { return this.annotationController; }
    public void setAnnotationController(AnnotationController annotationController) { this.annotationController = annotationController; }
}
