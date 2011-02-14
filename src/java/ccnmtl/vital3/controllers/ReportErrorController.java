package ccnmtl.vital3.controllers;

import java.util.HashMap;
import java.util.*;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Vital3Utils;

public class ReportErrorController extends Vital3CommandController {
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        UserContextInfo userInfo = null;
        VitalUser currentUser = null;
        
        HashMap model = new HashMap();
        boolean user_logged_in = false;
        try {
            userInfo = getUserContextInfo(request);
            currentUser = userInfo.getUser();
            ucm.decorateUser(currentUser, true);
            user_logged_in = true;
        } catch (Exception e){
           user_logged_in= false;
           model.put("currentUser", null); 
        }


        if (user_logged_in) {
            if (userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM)) {
                model.put("admin", new Boolean(true));
                model.put ("student_worksites", null);
            }
            else {
                // If this is a student, get the classes they're in to show in the dropdown:
                ArrayList worksites = new ArrayList();
                Set participants = currentUser.getParticipants();
                logger.debug("The user has " + participants.size() + " participants");
                Iterator participantsIt =  participants.iterator();
                
                while (participantsIt.hasNext()) {
                    VitalParticipant participant = (VitalParticipant) participantsIt.next();
                    VitalWorksite worksite = participant.getWorksite();
                    worksites.add(worksite);
                    ucm.decorateWorksites(worksites, false, false);
                    model.put ("student_worksites", worksites);
                }        
            }
            model.put("currentUser", currentUser);
        }
        return new ModelAndView("reportErrorForm", model);
    }
}

