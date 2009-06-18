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


public class DiscussionEntryController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());
    private int MAX_DISCUSSION_ENTRY_LENGTH = 4000;
    
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        return UserCourseManager.STUDENT_ACCESS;
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
    *   This controller handles creating and deleting discussion entries. The display of the entries is done by the Assignment Response controller, to which our controller redirects.
    
    
    *
    *
    *   The "action" parameter is one of:
    *
    *       "addDiscussionEntry" for an AssignmentResponse  (triggered by "Reset All" on the reviewResponses page)
    *              *Inserts a comment object representing the discussion entry
    
    *               * Redirects to the reviewResponses page
    *
    *       "deleteDiscussionEntry" for an Comment   (triggered by "reset" on the reviewResponses page)
    *               * deletes the Comment representing the discussion entry
    *               * Redirects to the reviewResponses page
    *
    */

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {

        BasicAdminCommand command = (BasicAdminCommand) commandObj;

        logger.debug ("Starting handle in DiscussionEntryController");
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
        logger.info("In discussionEntryController; the action is " +  action);

        VitalUser currentUser = getUserContextInfo(request).getUser();
        participantId = command.getParticipantId();

        
        
        AssignmentResponse assignmentResponse = null;
        Assignment assignment = null;
        Comment entry = null;
        
        Long assignmentResponseId = null, assignmentId = null, entryId = null;
        
        if (command.getAssignmentResponse() != null)  {
            assignmentResponse = command.getAssignmentResponse();
            assignmentResponseId = command.getAssignmentResponse().getId();
        }
        if (command.getAssignment() != null ) {
            assignment = command.getAssignment();
            assignmentId = command.getAssignment().getId();
        }
        
        
        
        if(action.equals("addDiscussionEntry"))
        {
            logger.debug("Will now add discussion entry.");

            // logger.debug("Will now add discussion entry to " + assignmentResponse.getId());
            logger.debug("Assignment response is :");
            logger.debug(command.getAssignmentResponse());
            
            logger.debug("Assignment is :");
            logger.debug(command.getAssignment());
            
            //logger.debug("Text is :");
            //logger.debug(command.getText());
            
            //logger.debug("LENGTH is:");
            //logger.debug(Integer.toString( command.getText().length()));
            
            
            
            puke(command.getText().length() > MAX_DISCUSSION_ENTRY_LENGTH, "We can't record this discussion entry because it contains too many characters.");
            //logger.debug(command.getAssignment().getResponses());

            
            if (assignmentResponse == null) {
            logger.debug("Creating new assignment response.");
                AssignmentResponse otherAssignmentResponse = assignment.getParticipantResponse(command.getParticipant());
                if( otherAssignmentResponse != null) {
                    logger.debug("A participant can't submit more than one response to a given assignment. Not allowing the creation of a new response.");
                    throw new RuntimeException("A participant can't submit more than one response to a given assignment. Not allowing the creation of a new response..");
                }
                
                assignmentResponse = new AssignmentResponse(
                    command.getAssignment(),
                    command.getParticipant(),
                    new Date(),
                    1,
                    null
                );
                command.setAssignmentResponse(assignmentResponse);
                
                /*
                Assignment assignment,
                VitalParticipant participant,
                Date dateSubmitted,
                int status,
                String text
                */
                
                vital3DAO.save(AssignmentResponse.class, assignmentResponse);
            }
            
            VitalParticipant participant = assignmentResponse.getParticipant();
            
            puke (participant == null, "participant not found.");
            ucm.decorateParticipant(participant);
            VitalUser participantUser = participant.getUser(); puke (participantUser == null, "User not found.");
            ucm.decorateUser(participantUser, false);

            Comment comment = new Comment(null, command.getAssignmentResponse(), participant, new Date(), null, command.getText(), Comment.DISCUSSIONENTRY);
            vital3DAO.save(Comment.class, comment);
            //just for paranoia's sake:
            vital3DAO.save(AssignmentResponse.class, assignmentResponse);
            assignmentResponseId = assignmentResponse.getId();

           
            
            destination = "response.smvc?action=display&entity=assignmentResponse&id="+ assignmentResponseId  + "&participantId=" + participantId;
            
        }
        
        else if(action.equals("deleteDiscussionEntry"))
        {
            logger.info("Will now delete the discussion entry.");
            /*
            RULES:
            Can only delete your own comment.
            Check that the deleted comment is in fact on a discussion assignment.
            vital3DAO.delete(AssignmentResponse.class, assignmentResponse);
            
        */
            entry = (Comment) vital3DAO.findById(Comment.class, command.getCommentId());
            puke (entry == null, "Discussion entry not found.");
            
            vital3DAO.delete(Comment.class, entry);
       
            destination = "response.smvc?action=display&entity=assignmentResponse&id="+ assignmentResponseId  + "&participantId=" + participantId;
        }
       
        else {
            throw new Exception ("Sorry, can't perform that action.");
        }
        puke(destination == null, "No destination specified.");
        logger.debug( "redirecting to " + destination );
        //buhbye
        return Vital3Utils.redirectModelAndView(destination );
    }
    
    
    private void puke ( boolean cause, String message) throws RuntimeException {
        if (cause) { logger.warn (message); throw new RuntimeException(message); }
    }


} /// end class
   
   
   
