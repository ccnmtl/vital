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
import org.json.*;

import ccnmtl.utils.*;
import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.ucm.ColumbiaUCM;
import ccnmtl.vital3.commands.*;
import ccnmtl.utils.OmniComparator;

public class ParticipantSearchController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.TA_ACCESS;
    }
    
    protected VitalWorksite getRequestedWorksite(Vital3Command commandObj) throws Exception {
        
        puke (commandObj == null, "Null command object passed." );
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        
        puke (command == null, "No command." );

        // This is the case where an admin user is looking for a user to edit from /listing.smvc?mode=user
        if ( command.getUser() != null && UserCourseManager.ADMIN_ACCESS.intValue() == command.getUser().getAccessLevel().intValue()) {
          return null;
        }
        
      // this is the case where an instructor, ta, or professor wants to add a user to the roster page. In this case we need to check their affils properly:
      
      puke (command.getParticipantId() == null, "Unable to find participant ID at all." );
      VitalParticipant participant = (VitalParticipant) vital3DAO.findById(VitalParticipant.class, command.getParticipantId());
      puke (participant == null, "Unable to find participant by ID " + command.getParticipantId());
      command.setParticipant(participant);
      if (participant == null)  throw new RuntimeException("No participant found.");
      ucm.decorateParticipant(participant);
      VitalWorksite worksite = participant.getWorksite();
      if (worksite == null)  throw new RuntimeException("No worksite found.");
      ucm.decorateWorksite(worksite, false, false);
      logger.debug ("Ending getRequestedWorksite in ParticipantSearchController");
      return worksite;
      
      
    }
    
    
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        logger.debug("I was here.");
        String searchString = "";
        try {
            searchString = request.getParameter("searchString");
        } catch(Exception e) {
                // do nothing. This is an ajax controller; we'll just return an emtpy Json object.
        }
        JSONArray allResults = new JSONArray();
        List results = ucm.searchForUsers(searchString);
        Collections.sort(results, new OmniComparator(VitalUser.class, "getFirstName"));
        Iterator it = results.iterator();
        while (it.hasNext()) {
            VitalUser vUser = (VitalUser) it.next();
            JSONObject nextResult = new JSONObject();
            nextResult.put ("id", vUser.getId());
            nextResult.put ("firstName", vUser.getFirstName());
            nextResult.put ("lastName", vUser.getLastName());
            nextResult.put ("userIdString", vUser.getUserIdString());
            logger.debug(vUser.getUserIdString());
            allResults.put (nextResult);
        }
        String responseString = allResults.toString();
        Map model = new HashMap();
        model.put ("body", responseString);
        return new ModelAndView("ajaxResponse", model);
    }
    
    
    private void puke ( boolean cause, String message) throws RuntimeException {
        if (cause) { logger.warn (message); throw new RuntimeException(message); }
    }
    
}
