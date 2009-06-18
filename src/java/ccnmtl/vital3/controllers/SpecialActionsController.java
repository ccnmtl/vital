package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import ccnmtl.utils.*;
import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.ucm.ColumbiaUCM;
import ccnmtl.vital3.commands.*;


public class SpecialActionsController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());



    /**
    * Users have to be TA's or higher in the class represented by worksite.
    */
    
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        return UserCourseManager.TA_ACCESS;
    }


    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        logger.debug ("Starting getRequestedWorksite in SpecialActionsController");
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        VitalParticipant participant = (VitalParticipant) vital3DAO.findById(VitalParticipant.class, command.getParticipantId());
        puke (participant == null, "Unable to find participant by ID " + command.getParticipantId());
        command.setParticipant(participant);
        if (participant == null)  throw new RuntimeException("No participant found.");
        ucm.decorateParticipant(participant);
        VitalWorksite worksite = participant.getWorksite();
        if (worksite == null)  throw new RuntimeException("No worksite found.");
        ucm.decorateWorksite(worksite, false, false);
        logger.debug ("Ending getRequestedWorksite in SpecialActionsController");
        return worksite;
    }

    /*
    *
    *   This controller helps controllers reviewGuidedLessonController and reviewResponsesController handle CRUD
    *   operations on comments, as well as resetting the status of assignments to 'unsubmitted'. (these tend to go together).
    *
    *
    *   The "action" parameter is one of:
    *
    *       "resetAllResponses" for an Assignment  (triggered by "Reset All" on the reviewResponses page)
    *               * set the status to zero on all assignment responses for a given assignemnt
    *               * Redirects to the reviewResponses page
    *
    *       "resetEssayResponse" for an assignmentResponse   (triggered by "reset" on the reviewResponses page)
    *               * set the status to zero on one assignmentResponse (Essay type).
    *               * Redirects to the reviewResponses page
    *
    *       "resetGuidedLessonResponse"
    *               * set the status to zero on one assignmentResponse (Guided Lesson type)
    *               * delete one or more answers and their comments
    *               * Redirects to the reviewResponses page
    *
    *       "submitComments"
    *               * Set the status to 1 for all comments on answers in the assignmentResponse from a given participant.
    *               * Redirects to the reviewGuidedLesson page.
    *
    *       "insertComments"
    *               * Inserts a set of comments for a Guided Lesson
    *               * Redirects to the reviewGuidedLesson page.
    *
    *       "updateComments"
    *               * Updates a set of comments for a Guided Lesson
    *               * Redirects to the reviewGuidedLesson page.
    *
    */

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {

        BasicAdminCommand command = (BasicAdminCommand) commandObj;

        logger.debug ("Starting handle in SpecialActionsController");
        String action;
        String destination = null;
        Long participantId = null;
        try {
            action = request.getParameter("action");
            puke (action == null, "Missing required parameter: action");
        } catch(Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        logger.info("In specialActionsController; the action is " +  action);

        VitalUser currentUser = getUserContextInfo(request).getUser();
        participantId = command.getParticipantId();

        
        
        AssignmentResponse assignmentResponse = null;
        Assignment assignment = null;
        Long assignmentResponseId = null, assignmentId = null;
        
        if (command.getAssignmentResponse() != null)  {
            assignmentResponse = command.getAssignmentResponse();
            assignmentResponseId = command.getAssignmentResponse().getId();
        }
        if (command.getAssignment() != null ) {
            assignment = command.getAssignment();
            assignmentId = command.getAssignment().getId();
        }
        
        
        if(action.equals("resetAllResponses"))
        {
            
            logger.debug("Will now unsubmit all responses to assigment" + assignment.getId());
            
            List assignmentResponses = vital3DAO.findByPropertyValue(AssignmentResponse.class, "assignment", assignment);

            Iterator allResponsesIter = assignmentResponses.iterator();
            while (allResponsesIter.hasNext()) {
                AssignmentResponse nextAssignmentResponse = (AssignmentResponse) allResponsesIter.next();
                logger.debug("Examining response " + nextAssignmentResponse);
                nextAssignmentResponse.setStatus(new Integer(0));
                vital3DAO.save(AssignmentResponse.class, nextAssignmentResponse);
                resetAllCommentsAndSave(nextAssignmentResponse);
            }
            String message = "All responses to this assignment have been reset.";
            destination = "reviewResponses.smvc?id=" + assignment.getId() + "&sortOrder=lastName&ascending=true&viewBy=all&message=" + message;
        }
        
        else if(action.equals("resetEssayResponse"))
        {
            logger.info("Will now reset the response for assignmentresponse " + assignmentResponseId);
            puke (assignmentResponse.getAssignment().isGuidedLesson(), "Attempt to call resetEssayResponse on a guided lesson");
            assignmentResponse.setStatus(new Integer(0));
            vital3DAO.save(AssignmentResponse.class, assignmentResponse);
            resetAllCommentsAndSave(assignmentResponse);
            VitalParticipant submitter = assignmentResponse.getParticipant(); puke (submitter == null, "participant not found.");
            ucm.decorateParticipant(submitter);
            VitalUser submitterUser = submitter.getUser(); puke (submitterUser == null, "User not found.");
            ucm.decorateUser(submitterUser, false);
            String message = submitterUser.getFirstName() +  " " + submitterUser.getLastName() + "'s response to this assignment has been reset.";
            destination = "reviewResponses.smvc?id=" + assignmentId + "&sortOrder=lastName&ascending=true&viewBy=all&message=" + message;
        }
        
        else if(action.equals("resetGuidedLessonResponse"))
        {
            Integer firstAnswerToDelete = null;
            try {
                firstAnswerToDelete = new Integer(request.getParameter("firstAnswerToDelete"));
                puke (firstAnswerToDelete == null, "Missing required parameter: firstAnswerToDelete");
            } catch(Exception e) {
                logger.info("Error parsing parameters!", e);
                return Vital3Utils.redirectModelAndView("error.smvc", e);
            }
            logger.info("Will now reset the response for assignmentresponse " + assignmentResponseId);
            logger.info("Deleting all answers starting with  " + firstAnswerToDelete);
            puke (assignmentResponse.getAssignment().isEssay(), "Attempt to call resetGuidedLessonResponse on an essay.");
            VitalParticipant submitter = assignmentResponse.getParticipant(); puke (submitter == null, "participant not found.");
            ucm.decorateParticipant(submitter);
            VitalUser submitterUser = submitter.getUser(); puke (submitterUser == null, "User not found.");
            ucm.decorateUser(submitterUser, false);
            String userName = submitterUser.getFullName();
            
            resetAndDeleteAnswers(assignmentResponse, firstAnswerToDelete);
            resetAllCommentsAndSave(assignmentResponse);
            
            String message = userName + "'s response to this assignment has been reset.";
            destination = "reviewResponses.smvc?id=" + assignmentId + "&sortOrder=lastName&ascending=true&viewBy=all&message=" + message;
        }
        
        else if(action.equals("insertComments"))
        {
            Integer status = null;
            try {
                status = new Integer(request.getParameter("status"));
                puke (status == null, "Missing required parameter: status");
            } catch(Exception e) {
                logger.info("Error parsing parameters!", e);
                return Vital3Utils.redirectModelAndView("error.smvc", e);
            }
            logger.debug("Will now insert all comments for assigment " + assignmentId);
            puke (assignmentResponse.isNotSubmitted(), "Assignment response has not yet been submitted, so not submitting any comments.");
            Set allAnswers = assignmentResponse.getAnswers();
            Iterator allAnswersIter = allAnswers.iterator();
            String text;
            VitalParticipant participant = command.getParticipant();
            if (participant == null) throw new RuntimeException("null participant");
            while (allAnswersIter.hasNext()) {
                text = null;
                Answer answer = (Answer) allAnswersIter.next();
                logger.debug("Examining answer " + answer);
                // double-check that there are no existing Comments for this answer from this participant.
                if (vital3DAO == null) logger.debug("vital3DAO not found.");
                if (command.getParticipant() == null) logger.debug("participant not found.");
                if (answer == null) logger.debug("answer  not found.");
                List results = vital3DAO.findByTwoPropertyValues(Comment.class, "answer", answer, "participant", command.getParticipant());
                if (results.size() != 0) {
                    logger.warn("Comment already found.");
                    throw new Exception("There were " + results.size() + " comments on this response by this participant!");
                }
                
                text = request.getParameter("newComment" + answer.getId());
                Comment comment = new Comment(answer, null, participant, new Date(), status, text, Comment.FEEDBACK);
                vital3DAO.save(Comment.class, comment);
            }
            destination = "reviewGuidedLesson.smvc?id=" + assignmentResponseId;
        }
        
        else if(action.equals("updateComments"))
        {
            Integer status = null;
            try {
                status = new Integer(request.getParameter("status"));
                puke (assignmentId == null, "Missing required parameter: status");
                
            } catch(Exception e) {
                logger.info("Error parsing parameters!", e);
                return Vital3Utils.redirectModelAndView("error.smvc", e);
            }
            logger.debug("Will now update all comments for assigment" + assignmentId);
            logger.debug("Status is " + status);
            puke (assignmentResponse.isNotSubmitted(), "Assignment response has not yet been submitted, so not submitting any comments.");
            Set allAnswers = Vital3Utils.initCollection(AssignmentResponse.class, assignmentResponse, "answers", Answer.class);
            Iterator allAnswersIter = allAnswers.iterator();
            String text;
            while (allAnswersIter.hasNext()) {
                text = null;
                Answer answer = (Answer) allAnswersIter.next();
                logger.debug("Examining answer " + answer);
                // double-check that there are that the comment exists for this answer from this participant.                
                List results = vital3DAO.findByTwoPropertyValues(Comment.class, "answer", answer, "participant", command.getParticipant());
                if (results.size() > 1) {
                    // this is prohibited by the database now, but leaving it in for paranoia's sake.
                    throw new Exception("There were " + results.size() + " comments on this answer by this participant!");
                }
                else if (results.size() == 1) {
                    // normal case: update the comment
                    Comment comment = (Comment) results.iterator().next();
                    text = request.getParameter("updateComment" + comment.getId());
                    comment.setText(text);
                    comment.setStatus( status);
                    comment.setAssignmentResponse(null);
                    comment.setDateModified(new Date());
                    comment.setType(Comment.FEEDBACK);
                    vital3DAO.save(Comment.class, comment);
                }
                else { 
                        // special case: this comment used to exist,
                        // but disappeared when some of the questions and their comments were reset.
                        // To rectify this, we'll just insert a comment:
                    logger.debug("No comment found to update on " + answer.getId() );
                    logger.debug("Inserting a comment instead.");
                    text = request.getParameter("newComment" + answer.getId());
                    Comment comment = new Comment( answer, null, command.getParticipant(), new Date(), status, text, Comment.FEEDBACK);
                    vital3DAO.save(Comment.class, comment);
                    
                }
                

            }
            destination = "reviewGuidedLesson.smvc?id=" + assignmentResponseId;
        }
        
        else if (action.equals("submitComments"))
        {
            logger.debug("Will now submit all feedback for assigment response " + assignmentResponseId);
            puke (!assignmentResponse.getAssignment().getType().equals( Assignment.GUIDED_LESSON), "submitComments only works on guided lesson responses.");
            Set allAnswers = Vital3Utils.initCollection(AssignmentResponse.class, assignmentResponse, "answers", Answer.class);
            Set allComments = Vital3Utils.initCollections(Answer.class, allAnswers, "comments", Comment.class);

            Iterator allCommentsIter = allComments.iterator();
            while (allCommentsIter.hasNext()) {
                Comment comment = (Comment) allCommentsIter.next();
                logger.debug("Examining comment " + comment);
                if (comment.getParticipant().getId().intValue() == participantId.intValue()) {
                    logger.debug("It does belong to this participant, so saving...");
                    comment.setStatus(new Integer(1));
                    vital3DAO.save(Comment.class, comment);
                }
                else {
                    // logger.debug( "Submitted by " + c.getParticipant().getId() + " but viewed by " +  participantId);
                }
            }
            destination = "reviewGuidedLesson.smvc?id=" + assignmentResponseId;
        }
        
        else {
            throw new Exception ("Sorry, can't perform that action.");
        }
        puke(destination == null, "No destination specified.");
        logger.debug( "redirecting to " + destination );
        //buhbye
        return Vital3Utils.redirectModelAndView(destination );
    }
    
    
     
     /** 
     * Resets a guided lesson response to unsubmitted (zero) status;
     * this treats question numbers as being in a sequence 1,2,3,4 regardless of what their ordinalvalues are!
     * deletes any comments on those questions;
     * Currently DOES NOT change the status on comments for questions that are not deleted.
     */

    public void resetAndDeleteAnswers(AssignmentResponse assignmentResponse,  Integer firstAnswerToDelete) {
        puke (firstAnswerToDelete.intValue() <= 0, "firstAnswerToDelete should be positive and nonzero.");
        puke (assignmentResponse == null , "Attempt to call resetGuidedLessonResponse on a null assignmentResponse.");
        puke (assignmentResponse.getAssignment().isEssay(), "Attempt to call resetGuidedLessonResponse on an essay.");
        int targetAnswer = firstAnswerToDelete.intValue();
        
        if (targetAnswer == 1) {
            logger.debug("deleting entire assignment response.");
            vital3DAO.delete(AssignmentResponse.class, assignmentResponse);
            //Note: this implicitly deletes associated answers and any comments on them.
            
        } else {
            logger.debug("rewinding assignment response to question " + targetAnswer);
            Set answersToDelete = new HashSet();
            //not necessary
            //Set commentsToDelete = new HashSet();
            List sortedAnswers = new ArrayList(assignmentResponse.getAnswers());
            
            OmniComparator questionOrdinalValueComp = new OmniComparator(Question.class, "getOrdinalValue");
            OmniComparator answerQuestionComp = new OmniComparator(Answer.class, "getQuestion", questionOrdinalValueComp);
            
            Collections.sort(sortedAnswers, answerQuestionComp );
            Iterator i = sortedAnswers.iterator();
            int skippedAnswers = 1;
            while (i.hasNext() && skippedAnswers < targetAnswer) {
                Answer a = (Answer) i.next();
                logger.debug("Skipping answer ID " + a.getId() + " which is an answer to question id " + a.getQuestion().getOrdinalValue() + " with ordinal value " + a.getQuestion().getOrdinalValue());
                skippedAnswers++;
            }
            while (i.hasNext()) {
                Answer a = (Answer) i.next();
                answersToDelete.add(a);
                logger.debug("Deleting answer ID " + a.getId() + " which is an answer to question id " + a.getQuestion().getId() + " with ordinal value " + a.getQuestion().getOrdinalValue());
                //not necessary
                //commentsToDelete.addAll(a.getComments());
            }
            // cascade delete automatically deletes comments on delete answers.
            //vital3DAO.deleteCollection(Comment.class, commentsToDelete);
            vital3DAO.deleteCollection(Answer.class, answersToDelete);
            assignmentResponse.setStatus(new Integer(0));
            
            vital3DAO.save(AssignmentResponse.class, assignmentResponse);
        }
    }
    
    private void resetAllCommentsAndSave (AssignmentResponse assignmentResponse) {
        assignmentResponse.resetAllComments();
        vital3DAO.saveCollection(Comment.class, assignmentResponse.allComments());       
    }
    
    
    private void puke ( boolean cause, String message) throws RuntimeException {
        if (cause) { logger.warn (message); throw new RuntimeException(message); }
    }


} /// end class
   
   
   