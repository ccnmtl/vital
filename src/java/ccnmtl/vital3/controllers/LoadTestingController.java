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
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.commands.*;


public class LoadTestingController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());


    /**
    * Users have to be TA's or higher in the class represented by worksite.
    */
    
    protected Integer getMinAccessLevel(Vital3Command commandObj) {
        return UserCourseManager.ADMIN_ACCESS;
    }




    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {

        BasicAdminCommand command = (BasicAdminCommand) commandObj;

        logger.debug ("Starting handle in LoadTestingController");
        String action = null;
        String testIdString = null;
        Integer userCount = null;
        
        Long participantId = null;
        try {
            action = request.getParameter("action");
            testIdString = request.getParameter("testIdString");
            userCount = new Integer(request.getParameter("userCount"));
            puke (userCount == null, "Missing required parameter: userCount");
            puke (action == null, "Missing required parameter: action");
            puke (testIdString == null, "Missing required parameter: testIdString");
            
        } catch(Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        logger.info("In LoadTestingController; the action is " +  action);


        
        Map model = new HashMap();
        model.put("title", "blank");
        model.put("action", action);
        
        if(action.equals("setup"))
        {
            logger.debug("In setup.");
            RawUCMTerm newTerm = new RawUCMTerm(null, testIdString, null);
            VitalWorksite newWorksite = new VitalWorksite(testIdString, testIdString, newTerm, testIdString);
            ucm.insertWorksite(newWorksite);
            newTerm.getWorksites().add(newWorksite);
            vital3DAO.save(RawUCMTerm.class, newTerm);
            List usersToSave = new ArrayList();
            List participantsToSave = new ArrayList();
            Iterator idIter = generateIds (testIdString, userCount).iterator();
            while(idIter.hasNext()) {
                String nextId = (String) idIter.next();
                VitalUser newUser = new VitalUser(nextId, "vital",  new Integer(100), nextId, nextId, nextId, nextId);
                VitalParticipant newParticipant = new VitalParticipant ( nextId, new Integer(100), newUser, newWorksite);
                usersToSave.add(newUser);
                participantsToSave.add(newParticipant);
            }
            ucm.insertParticipants(participantsToSave);
            ucm.insertUsers(usersToSave);
            
        }
        
        else if(action.equals("status"))
        {
            logger.debug("In status.");
            List ids = generateIds (testIdString, userCount);
            
            
            List foundUsers = vital3DAO.findBySetOfPropertyValues(VitalUser.class, "userIdString", ids); 
            List foundRawUsers = vital3DAO.findBySetOfPropertyValues(RawUCMUser.class, "userIdString", ids); 
            
            List foundWorksites = vital3DAO.findBySetOfPropertyValues(VitalWorksite.class, "worksiteIdString", ids); 
            List foundRawWorksites = vital3DAO.findBySetOfPropertyValues(RawUCMWorksite.class, "worksiteIdString", ids); 
            
            List foundParticipants = vital3DAO.findBySetOfPropertyValues(VitalParticipant.class, "participantIdString", ids); 
            List foundRawParticipants = vital3DAO.findBySetOfPropertyValues(RawUCMParticipant.class, "participantIdString", ids);
            
            
            
            model.put ("foundUsers", new Integer (foundUsers.size()));
            model.put ("foundRawUsers", new Integer (foundRawUsers.size()));
            
            model.put ("foundParticipants", new Integer (foundParticipants.size()));
            model.put ("foundRawParticipants", new Integer (foundRawParticipants.size()));
            
            model.put ("foundWorksitesCount", new Integer(foundWorksites.size()));
            model.put ("foundRawWorksites", new Integer(foundRawWorksites.size()));
        }
        
        else if(action.equals("teardown"))
        {
            logger.debug("In teardown.");
            VitalWorksite foundWorksite = null;
            List ids = generateIds (testIdString, userCount);
            
            Collection foundWorksites = vital3DAO.findByPropertyValue(VitalWorksite.class, "worksiteIdString", testIdString);
            
            
            Collection foundUsers = vital3DAO.findBySetOfPropertyValues(VitalUser.class, "userIdString", ids);
            Collection foundParticipants = vital3DAO.findBySetOfPropertyValues(VitalParticipant.class, "participantIdString", ids);
            
            
            /*
            2006-09-07 19:04:08,080 DEBUG [ccnmtl.vital3.controllers.LoadTestingController] - Found  1 worksites
            2006-09-07 19:04:08,080 DEBUG [ccnmtl.vital3.controllers.LoadTestingController] - Found  0 users
            2006-09-07 19:04:08,080 DEBUG [ccnmtl.vital3.controllers.LoadTestingController] - Found  0 participants
            */
            
            logger.debug("Found  " + foundWorksites.size() +  " worksites");
            logger.debug("Found  " + foundUsers.size() +  " users" );
            logger.debug("Found  " + foundParticipants.size() +  " participants");
            
            
            ucm.deleteParticipants(foundParticipants);
            ucm.deleteUsers(foundUsers);            
            ucm.deleteWorksites(foundWorksites);
            
            vital3DAO.deleteCollection(RawUCMTerm.class, vital3DAO.findByPropertyValue(RawUCMTerm.class, "name", testIdString));
        }
        
        else {
            throw new Exception ("Sorry, can't perform that action.");
        }
        
        //buhbye
        return new ModelAndView("loadTesting", model);
    }

    
    private List generateIds (String testIdString, Integer howMany) {
        List result = new ArrayList();
        puke (howMany == null, "No number passed");
        
        int max = howMany.intValue();
        puke (max < 1, "Number has to be larger than 1.");
        for (int i = 0; i < max; i++) {
            result.add ( testIdString + "_" + i);
            logger.debug ("Adding " + testIdString + "_" + i);
        }
        
        return result;     
    }
    
    private void puke ( boolean cause, String message) throws RuntimeException {
        if (cause) { logger.warn (message); throw new RuntimeException(message); }
    }


} /// end class
   
   
   