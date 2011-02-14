package ccnmtl.vital3.controllers;

import java.util.*;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;

public class MyCoursesController extends Vital3CommandController {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.LOGGED_IN_ACCESS;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command command, BindException errors) throws Exception {
        
        UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
        
        String viewBy = "all";
        if ( request.getParameter("viewBy") != null ) viewBy =  request.getParameter("viewBy");
        logger.debug ("viewBy was: " + viewBy);
        
        
        // need to init participants for user:
        ucm.decorateUser(currentUser, true);
        
        Map model = new HashMap();
		model.put("currentUser", currentUser);
		List worksites = null;
        
        /*
        
        public List decorateWorksites(Collection worksites, boolean initCourseAffils, boolean initParticipants){
        public abstract List findAllWorksites(boolean initCourseAffils, boolean initParticipants);
        // for this page, courseAffils are totally unneccessary and should never be initialized.    
        */
        
        if (userContextInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITES)) {
            if (viewBy.equals("term")) {
                worksites = ucm.worksitesForTerm(ucm.currentTermName());
                ucm.decorateWorksites(worksites, false, false);
            }
            else if (viewBy.equals("academicyear")) {
                worksites = ucm.worksitesForTerms(ucm.currentAcademicYearTermNames());
                ucm.decorateWorksites(worksites, false, false);
            }
            else if (viewBy.equals("calendaryear")) {
                worksites = ucm.worksitesForTerms(ucm.currentCalendarYearTermNames());
                ucm.decorateWorksites(worksites, false, false);
            }
            else {
                logger.debug ("Admin: unfiltered...");
                worksites = ucm.findAllWorksites(false, false);
            }
            model.put("admin", "true");
        } else {
            //logger.debug ("Student view");
            worksites = new ArrayList();
            Set participants = currentUser.getParticipants();
            //logger.debug("The user has " + participants.size() + " participants");
            Iterator participantsIt =  participants.iterator();
            while (participantsIt.hasNext()) {
                VitalParticipant participant = (VitalParticipant) participantsIt.next();
                VitalWorksite worksite = participant.getWorksite();
                if ((viewBy.equals("term")) && !currentTerm(worksite.getTerm())) continue;
                if ((viewBy.equals("year")) && !currentAcademicYear(worksite.getTerm())) continue;
                worksites.add(worksite);
            }        
            ucm.decorateWorksites(worksites, false, false);
        }
        /////////////
        List instructors = ucm.getInstructors(new HashSet(worksites));
        Map instructorNames = new HashMap();
        Iterator instructorIt = instructors.iterator();
        while (instructorIt.hasNext()) {
            VitalParticipant instructor = (VitalParticipant) instructorIt.next();
            String id = instructor.getWorksite().getWorksiteIdString();
            String name = instructor.getUser().getFirstName() + " " + instructor.getUser().getLastName();
            HashSet theSet;
            if (instructorNames.containsKey (id)) {
                theSet = (HashSet)instructorNames.get(id);
            }  else {
                theSet = new HashSet();
            }
            theSet.add(name);
            instructorNames.put(id, theSet);
        }
        
        Map instructorNameStrings = new HashMap ();
        Iterator worksiteIt = instructorNames.keySet().iterator ();
        while (worksiteIt.hasNext()) {
            String id = (String) worksiteIt.next();
            Set allNames = (HashSet) instructorNames.get(id);
            Iterator nameIter = allNames.iterator();
            StringBuffer names = new StringBuffer();
            while (nameIter.hasNext()) {
                names.append((String) nameIter.next());
                if (nameIter.hasNext()) names.append (", ");
            }
            instructorNameStrings.put (id, names.toString());
        }
        /////////////
        
        
        model.put ("instructorNames", instructorNameStrings);
        model.put("viewBy", viewBy);
		model.put("TA_ACCESS", UserCourseManager.TA_ACCESS);
        model.put("worksites", worksites);
        return new ModelAndView("myCourses", model);
    }
    
	private boolean currentTerm(RawUCMTerm term) {
		return (term.getName().equals(ucm.currentTermName()));
	}

	private boolean currentAcademicYear(RawUCMTerm term) {
		return ucm.currentAcademicYearTermNames().contains(term.getName());
	}
	
    private boolean currentCalendarYear(RawUCMTerm term) {
		return ucm.currentCalendarYearTermNames().contains(term.getName());
	}
	
}
