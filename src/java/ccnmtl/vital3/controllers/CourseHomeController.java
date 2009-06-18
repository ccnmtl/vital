package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.dao.Vital3DAO;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.*;

public class CourseHomeController extends Vital3CommandController {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.STUDENT_ACCESS;
    }
    
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String message = request.getParameter("message");
        Map discussionEntryCounts = new HashMap();
        UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
        VitalParticipant participant = userContextInfo.getParticipant();
        VitalWorksite currentWorksite = participant.getWorksite();
        logger.debug("CourseHome controller.handle: user " + currentUser.getUserIdString() + " is here");	
        if (currentWorksite.getUnits().size() == 0) logger.info("This worksite has no units.");
        
        Iterator unitsIt =  currentWorksite.getUnits().iterator();
        while (unitsIt.hasNext()) {
            Unit unit = (Unit) unitsIt.next();
            Set unitMaterials = Vital3Utils.initM2MCollection(Unit.class, unit, Material.class);
            Set assignments = unit.getAssignments();
            Vital3Utils.initM2MCollections(Assignment.class, assignments, Material.class);
            Iterator assignmentsIt = assignments.iterator();
            while (assignmentsIt.hasNext()) {
                Assignment assignment = (Assignment) assignmentsIt.next();
        		if  (assignment.isGuidedLesson()) {
        		    Set questions = Vital3Utils.initCollection(Assignment.class, assignment, "questions", Question.class);
        		}
        		if  (assignment.isDiscussion()) {
        		    
        		
                    discussionEntryCounts.put(assignment.getId(), new Integer( assignment.getDiscussionEntries().size()));
        		}
        		
                // Initialize status of the assignment response.
                String dummyString = assignment.getStatus(participant);
                
                //For faculty users, find out how many students have submitted responses.
                if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0) {
                    Set responses = assignment.getResponses();
                    Set answers = null;
                    if (responses.size() > 0) {
                        if (assignment.isGuidedLesson()) {
                			answers =  Vital3Utils.initCollections(AssignmentResponse.class, responses, "answers", Answer.class);
            		    }
                    }
                }
            }
        }
        ArrayList units = currentWorksite.getUnitsSortedByDate();
        Map model = new HashMap();
        model.put("title", currentWorksite.getTitle());
        model.put("currentUser", currentUser);
        model.put("worksite", currentWorksite);
        model.put("units", units);
        model.put("participant", participant);
        model.put("message", message);
        model.put("discussionEntryCounts", discussionEntryCounts);
        TextFormatter dp = this.getTextFormatter();
        //if (dp == null) logger.error("TextFormatter is null!");
        model.put("textFormatter", dp);
        
        if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0)
            model.put("admin", "true");
        
        return new ModelAndView("courseHome", model);
    }
}
