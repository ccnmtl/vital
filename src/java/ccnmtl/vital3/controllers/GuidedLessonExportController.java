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
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.BasicAdminCommand;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.ucm.ColumbiaUCM;

/**
 * GuidedLessonExportController lists all the questions and answers for a given student.
 * 
 */
public class GuidedLessonExportController extends Vital3CommandController {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.STUDENT_ACCESS;
    }
    
    /**
     * Derive worksite from assignment response instance...
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        AssignmentResponse response = command.getAssignmentResponse();
        VitalWorksite worksite = response.getRelatedWorksite();
        
        if (worksite == null) throw new RuntimeException("worksite was null. something went wrong.");
        
        // decorate worksite:
        ucm.decorateWorksite(worksite, false, false);
        
        return worksite;
    }
    
	private void puke ( boolean cause, String message) throws RuntimeException {
		if (cause) { logger.warn (message); throw new RuntimeException(message); }
	}

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        
		UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
		VitalWorksite currentWorksite = userContextInfo.getWorksite();
		VitalParticipant participant = userContextInfo.getParticipant();
        
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        AssignmentResponse assignmentResponse = command.getAssignmentResponse();
        Assignment assignment = assignmentResponse.getAssignment();
		VitalParticipant submittingParticipant = assignmentResponse.getParticipant();
		
        if (!assignmentResponse.belongsTo(participant) && !assignmentResponse.isSubmitted()) {
            return Vital3Utils.redirectModelAndView("error.smvc", new RuntimeException("Sorry, this assignment is still in progress. You will be able to see it once it has been submitted."));
        }
        
        Set allResponses = assignment.getResponses();
        Set questions = assignment.getQuestions();
		Set answers = Vital3Utils.initCollections(Question.class, questions, "answers", Answer.class);
		Set comments = Vital3Utils.initCollections(Answer.class, answers, "comments", Comment.class);
        
        Set participantsForDecoration = new HashSet();
        
		Iterator responsesIter = allResponses.iterator();
		while (responsesIter.hasNext()) {
			AssignmentResponse ar = (AssignmentResponse) responsesIter.next();
            participantsForDecoration.add(ar.getParticipant());
		}
               
        if (participantsForDecoration.size() > 0) {
            ucm.decorateParticipants(participantsForDecoration);
		}
        
		Set questionMaterials = Vital3Utils.initM2MCollections(Question.class, questions, Material.class);
		Map model = new HashMap();
		model.put("assignmentResponse", assignmentResponse);
       
		// put "currentUser", "worksite", and "admin" into the model if available:
        Vital3Utils.putUserInfoIntoModel(userContextInfo, model);
        // put TextFormatter into model:
        model.put("textFormatter", this.getTextFormatter());
		
		puke (currentUser == null, "User not found.");	
		puke (currentWorksite == null, "Worksite not found.");	
		puke (participant == null, "Participant not found.");

        model.put("currentUser", currentUser);
        model.put("worksite", currentWorksite);
        model.put("participant", participant);
        
 		model.put("nextQuestion", new Integer(assignment.getNumberOfQuestionsAnswered(assignmentResponse.getParticipant()) + 1));

		if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0) {
			model.put("admin", "true");
		}

		return new ModelAndView("guidedLessonExport", model);
	}
}