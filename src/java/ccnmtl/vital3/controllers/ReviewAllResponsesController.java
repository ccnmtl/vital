package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.ucm.ColumbiaUCM;
import ccnmtl.vital3.commands.*;

/**
 * ReviewAllResponsesController lists all the submitted responses to an assignment (essay or GL) along with the feedback for each.
 * It is shown only to faculty, and is accessed by clicking the "View All" button on the Course Home page.
 */
public class ReviewAllResponsesController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());

    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        return UserCourseManager.TA_ACCESS; // students never see this controller.
    }


    /**
    * Provide worksite for the security check.
    */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        Assignment assignment = (Assignment) vital3DAO.findById(Assignment.class, command.getId());
        puke (assignment == null, "No assignment found... so can't display the responses.");
        VitalWorksite worksite = assignment.getUnit().getWorksite();
        ucm.decorateWorksite(worksite, false, false);
        command.setAssignment(assignment);
        return worksite;
    }


	private void puke ( boolean cause, String message) throws RuntimeException {
		if (cause) { logger.warn (message); throw new RuntimeException(message); }
	}

	
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, DataRetrievalFailureException, Exception {
		logger.info("ReviewAllResponsesController");

		UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
		puke (currentUser == null, "User not found.");
		VitalWorksite currentWorksite;
		VitalParticipant participant;

		ucm.decorateUser(currentUser, true);
		Long id;
		String message = null;
        try {
			id = new Long(request.getParameter("id"));		
			message =  request.getParameter("message");
			puke (id == null, "Missing required parameter: id");
        } catch(Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }

		Assignment assignment = (Assignment) vital3DAO.findById(Assignment.class, id);
		puke (assignment == null, "The assignment response didn't have an assignment.");
		
		boolean guidedLesson = assignment.getType().equals(Assignment.GUIDED_LESSON);
		boolean essay = assignment.getType().equals(Assignment.ESSAY);

        currentWorksite = userContextInfo.getWorksite();
        puke (currentWorksite == null, "Worksite not found.");
        participant = userContextInfo.getParticipant();
        puke (participant == null, "Participant not found.");

		Set allResponses = Vital3Utils.initCollection(Assignment.class, assignment, "responses", AssignmentResponse.class);
        
        // if gl
        Set allQuestions = assignment.getQuestions();
        Set answers = Vital3Utils.initCollections(Question.class,allQuestions, "answers", Answer.class);
              
		List responses = new ArrayList();      
        
		// retrieve the students who submitted the assignments:
		Iterator responsesIter = allResponses.iterator();
		boolean show;
		String accessLevel = null;
		while (responsesIter.hasNext()) {
			show = true;
			AssignmentResponse ar = (AssignmentResponse) responsesIter.next();
			if (ar.getStatus().intValue() == 0) show = false;
			accessLevel = ar.getParticipant().getLabelForAccessLevel();
			puke(accessLevel == null, "No access level found.");

			if (show) {                
                ucm.decorateParticipant(ar.getParticipant());                
				responses.add(ar);
			}
		}
		           	
		// sort the submitted responses according to each student's lastname, then firstname
		OmniComparator responseComp = new OmniComparator(AssignmentResponse.class, "getLastName");
		OmniComparator secondComp = new OmniComparator(AssignmentResponse.class, "getFirstName");
		responseComp.setSecondaryComparator(secondComp);
		
        Collections.sort(responses, responseComp);
	

		Map model = new HashMap();
		model.put("currentUser", currentUser);
		model.put("worksite", currentWorksite);
		model.put("participant", participant);
		model.put("assignment", assignment);
		model.put("responses", responses);

		if (message!= null) model.put("message", message);

        // put textFormatter into model:
        TextFormatter dp = this.getTextFormatter();
        model.put("textFormatter", dp);

		return new ModelAndView("reviewAllResponses", model);
	}
}
