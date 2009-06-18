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
 * ReviewResponsesController simply lists the submitted responses to an assignment (essay or GL) along with the feedback for each.
 * It is shown only to faculty, and is accessed by clicking the "View Responses" button on the Course Home page.
 */
public class ReviewResponsesController extends Vital3CommandController  {
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

	static Map sortOrderLabelMap = new HashMap();
	static Map viewByLabelMap = new HashMap();
	static Map ascendingLabelMap = new HashMap();

	static {
		sortOrderLabelMap.put("lastName", new OmniComparator(AssignmentResponse.class, "getLastName", null));
		sortOrderLabelMap.put("firstName", new OmniComparator(AssignmentResponse.class, "getFirstName", null));
		sortOrderLabelMap.put("comments", new OmniComparator(AssignmentResponse.class, "getCommentsDateForSorting", null));
		sortOrderLabelMap.put("submitted", new OmniComparator(AssignmentResponse.class, "getDateSubmittedForSorting", null));
		sortOrderLabelMap.put("accessLevel", new OmniComparator(AssignmentResponse.class, "getAccessLevel", null));

		viewByLabelMap.put ("all", "All submissions");
		viewByLabelMap.put ("students", "By Students");
		viewByLabelMap.put ("tas", "By Ta's");
		viewByLabelMap.put ("guests", "By guests");
		viewByLabelMap.put ("withfeedback", "With Feedback");
		viewByLabelMap.put ("nofeedback", "Without Feedback");

		ascendingLabelMap.put ("true", "true");
		ascendingLabelMap.put ("false", "false");
	}

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, DataRetrievalFailureException, Exception {
		logger.info("ReviewResponsesController");

		UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
		puke (currentUser == null, "User not found.");
		VitalWorksite currentWorksite;
		VitalParticipant participant;

		ucm.decorateUser(currentUser, true);
		Long id;
		String sortOrder = "lastName";
		String ascending = "true";
		String viewBy = "all";
		String message = null;
        try {
			id = new Long(request.getParameter("id"));
			if (request.getParameter("sortOrder") != null) {
				sortOrder = request.getParameter("sortOrder");
			}
			if (request.getParameter("ascending") != null) {
				ascending = request.getParameter("ascending");
			}
			if (request.getParameter("viewBy") != null) {
				viewBy = request.getParameter("viewBy");
			}
			ascending =  request.getParameter("ascending");
			viewBy =  request.getParameter("viewBy");
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

		// if essay
		Set allComments = Vital3Utils.initCollections(AssignmentResponse.class, allResponses, "comments", Comment.class);
		// if gl
		Set allAnswers =     Vital3Utils.initCollections(AssignmentResponse.class, allResponses, "answers", Answer.class);
		Set allGLComments =  Vital3Utils.initCollections(Answer.class, allAnswers, "comments", Comment.class);

		List sortedResponses = new ArrayList();
        
        Set participantsForDecoration = new HashSet();
        
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
			if( viewBy.equals( "students" ) && !(accessLevel.equals("Student"))) show = false;
			if( viewBy.equals( "guests" ) && !(accessLevel.equals("Guest"))) show = false;
			if( viewBy.equals( "withfeedback" ) && !ar.hasComments()) show = false;
			if( viewBy.equals( "nofeedback" ) && ar.hasComments()) show = false;
			if (show) {
                participantsForDecoration.add(ar.getParticipant());
				//logger.info ("Found a submitted response by user " + ar.getParticipant().getUser().getLastName());
				sortedResponses.add(ar);
			}
		}
        
        if (participantsForDecoration.size() > 0)
            ucm.decorateParticipants(participantsForDecoration);
            
		// sorting:
		if (sortOrder != null && sortOrderLabelMap.containsKey(sortOrder)) {
			logger.info("Sort order is " + sortOrder);
			OmniComparator c =  (OmniComparator) sortOrderLabelMap.get(sortOrder);
			if (ascending != null && ascending.equals ("false")) c.reverseSortOrder();
			Collections.sort(sortedResponses, c);
		}

		Map model = new HashMap();
		model.put("currentUser", currentUser);
		model.put("worksite", currentWorksite);
		model.put("participant", participant);
		model.put("assignment", assignment);
		model.put("sortedResponses", sortedResponses);
		if (message!= null) model.put("message", message);

        // put textFormatter into model:
        TextFormatter dp = this.getTextFormatter();
        model.put("textFormatter", dp);

		logger.debug ("ViewBy is " + viewBy);
		logger.debug ("ascending is " + ascending);
		logger.debug ("sortOrder is " + sortOrder);

		if (viewBy != null && (viewByLabelMap.containsKey(viewBy))) model.put("viewBy", viewBy);
		if (ascending != null && (ascendingLabelMap.containsKey(ascending))) model.put("ascending", ascending);
		if (sortOrder != null && (sortOrderLabelMap.containsKey(sortOrder))){
			model.put("sortOrder", sortOrder);
		}
		else {
			logger.debug ("sortOrder not added.");
		}
		return new ModelAndView("reviewResponses", model);
	}
}
