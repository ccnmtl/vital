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


public class UserProfileController extends Vital3CommandController  {

    private String acceptablePasswordDescription = "Please choose a new password of at least 4 characters.";
    
    private boolean acceptablePassword (String password) {
        return password.length() >= 4; 
    }

    protected final Log logger = LogFactory.getLog(getClass());

    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.LOGGED_IN_ACCESS;
    }



    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        BasicAdminCommand command = (BasicAdminCommand) commandObj;
        String action = null, message = null;
        Long participantId = null;
          
        try {
            action = request.getParameter("action");
            puke (action == null, "Missing required parameter: action");
        } catch(Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        
        logger.info("In UserProfileController; the action is " +  action);
        UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
        
        if (action.equals("changeName")) {
            String firstName = null, lastName = null;
            try {
                    firstName = request.getParameter("firstName");
                    lastName = request.getParameter("lastName");
                } catch(Exception e) {
                    logger.info("Error parsing parameters!", e);
                    return Vital3Utils.redirectModelAndView("error.smvc", e);
                }
                if (firstName == null || firstName.equals("")) message = "Please enter your first name";
                else if (lastName == null || lastName.equals("")) message = "Please enter your last name";
                else if (! updatePassword ( firstName, lastName, currentUser)) message = "Sorry, an error occurred. Please try again."; 
        }
        
        else if (action.equals("changePassword")){
            String old = null, new1=null, new2=null;
            try {
                old  = request.getParameter("old");
                new1 = request.getParameter("new1");
                new2 = request.getParameter("new2");
                puke (action == null, "Missing required parameter: old");
                puke (action == null, "Missing required parameter: new1");
                puke (action == null, "Missing required parameter: new2");
                message = passwordChange( currentUser, old, new1, new2);
                
            } catch(Exception e) {
                logger.info("Error parsing parameters!", e);
                return Vital3Utils.redirectModelAndView("error.smvc", e);
            }
        }
        
        String authMethod = currentUser.usesVitalAuth() ? "vital" : "external";
        
        
        Map model = new HashMap();
        TextFormatter dp = this.getTextFormatter();
        
        puke (dp == null, "no text formatter found");
        model.put("textFormatter", dp);
        
        
        model.put ("currentUser", currentUser);
        model.put ("message", message);
        model.put ("authMethod", authMethod);
        //Hardwired for now. Will be in props.
        model.put ("passwordChangeURL", ucm.getExternalAuthPasswordChangeUrl());

        return new ModelAndView("userProfile", model);
    }
    
    
    
    private void puke ( boolean cause, String message) throws RuntimeException {
        if (cause) { logger.warn (message); throw new RuntimeException(message); }
    }


    String passwordChange (VitalUser user, String old, String new1, String new2) {
        String message;
        puke (old == null, "Missing required parameter: old");
        puke (new1 == null, "Missing required parameter: new1");
        puke (new2 == null, "Missing required parameter: new2");
        
        if (old == "" || new1 == "" || new2 == "") {
            message = "Please enter both your current and new passwords.";
        } else if (!old.equals(user.getPassword()))  {
            message = "Please make sure you type your current password correctly.";
        } else if (!new1.equals(new2))  {
            message = "Please make sure you retype your new password correctly.";
        } else if (!acceptablePassword(new2)) {
            message = this.acceptablePasswordDescription;
        } else if ( !updatePassword (new2, user)) {
            message = "Sorry, there was a database error. Please try again.";  
        } else {
            message = "Your password has been updated."; 
        }
        return message;
    }
    
    boolean updatePassword (String password, VitalUser user) {
        try {
            user.setPassword(password);
            vital3DAO.save(VitalUser.class, user);
        } catch(Exception e) {
            logger.warn("Unable to update password!", e);
            return false;
        }
        return true;
    }
    
   boolean updatePassword (String firstName, String lastName , VitalUser user) {
        try {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            vital3DAO.save(VitalUser.class, user);
        } catch(Exception e) {
            logger.warn("Unable to update name!", e);
            return false;
        }
        return true;
    }
    
    
}