package ccnmtl.vital3.controllers;

import java.net.URLEncoder;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.Controller;
import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.dao.Vital3DAO;


/**
 * LoginProcessingController is called after logging in. If the user uses WIND, WIND directs them to this
 * controller after successful login. The login form also points at this controller, which checks the
 * username and password against the database.
 * This controller then loads all the users' worksites and redirects the user to the my courses page.
 */
public class LoginProcessingController implements Controller {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;
    
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
        String passwordAuthFailedUrl = "/login.smvc?message="+ URLEncoder.encode("Sorry, your username and password don't match any user in our records.", "UTF-8");
        String loginFailedUrl = "/login.smvc?message="+ URLEncoder.encode("Sorry, you are not an authorized member of any Vital courses.", "UTF-8");
        String loginFailedGenericUrl = "/login.smvc?message="+ URLEncoder.encode("Sorry, we were unable to log you in.", "UTF-8");
        String loginSuccessUrl = null;
        String logoutUrl = "/login.smvc?message=" + URLEncoder.encode("Successfully logged out", "UTF-8");
        
        String loginFailedWindOutageUrl = "/login.smvc?message="+ URLEncoder.encode("Sorry, we were unable to log you in due to an outage in the authentication service. Please try again in a few minutes.", "UTF-8");
        
        String username, password, authMethod;
        
        HttpSession session = request.getSession(true);        
        
        logger.info("LoginProcessingController is beginning...");
        
        String logout = request.getParameter("logout");
        if (logout != null) {
            
            ucm.logout(session);
            return Vital3Utils.redirectModelAndView(logoutUrl);
            
        } else {
            logger.debug("LoginProcessingController beginning login.");

            try {
                username = request.getParameter("username");
                password = request.getParameter("password");
                authMethod = request.getParameter("authMethod");
            } catch(Exception e) {
                logger.debug("Exception getting params. Login has failed.");
                return Vital3Utils.redirectModelAndView(loginFailedGenericUrl);
            }
            
            if (authMethod == null ) {
                
                logger.debug("Auth method not specified. Login has failed.");
                return Vital3Utils.redirectModelAndView(loginFailedGenericUrl);
                
            } else if (authMethod.equals("vital")) {
                
                logger.debug ("Vital auth starting for username " + username);
                
                if (username == null || password ==  null || username.equals ("") || password.equals("")) {
                    logger.debug("Username and password were blank. Login has failed.");
                    return Vital3Utils.redirectModelAndView(loginFailedGenericUrl);
                }
                
                session.removeAttribute(Vital3Utils.affilListSessionAttributeName);
                session.removeAttribute(Vital3Utils.usernameSessionAttributeName);
                
                List results = vital3DAO.findByTwoPropertyValues(VitalUser.class, "userIdString", username, "password", password);
                if (results != null && results.size() > 0) {
                    logger.debug("Password matches. Adding " + username + " to session.");
                    session.setAttribute(Vital3Utils.usernameSessionAttributeName, username);
                } else {
                    logger.warn("Password doesn't match.");
                    return Vital3Utils.redirectModelAndView(passwordAuthFailedUrl);
                }
            }
            String userIdString = (String) session.getAttribute(Vital3Utils.usernameSessionAttributeName);
            
            if (userIdString == null ) {
                logger.debug("Wind outage, probably. Login has failed.");
                return Vital3Utils.redirectModelAndView(loginFailedWindOutageUrl);
            }
            // this sets up the session:
            VitalUser user = ucm.loadUser(session, authMethod);
            
            if (user == null) {
                // if user is null, then loadUser could not find any reason for the user to be allowed in.
                return Vital3Utils.redirectModelAndView(loginFailedUrl);
            } else {
                
                if (user.usesVitalAuth()) {
                    if (!authMethod.equals("vital")) {
                        logger.debug("User was not allowed to use non-vital auth.");
                        return Vital3Utils.redirectModelAndView(loginFailedGenericUrl);
                    }
                } else {
                    if (authMethod.equals("vital")) {
                        logger.debug("User was not allowed to use vital auth.");
                        return Vital3Utils.redirectModelAndView(loginFailedGenericUrl);                
                    }
                }
                
                logger.debug("Successful login for user " + user.getId());
                        
                // if the user was attempting to access someplace before they logged in, assign it to successUrl.
                String afterLoginUrl = (String) session.getAttribute(Vital3Utils.afterLoginUrlSessionAttributeName);
                if (afterLoginUrl != null) {
                    
                    loginSuccessUrl = afterLoginUrl; 
                    session.removeAttribute(Vital3Utils.afterLoginUrlSessionAttributeName);
                    logger.debug("retrieved afterLoginUrl: " + loginSuccessUrl);
                    
                } else {
                    
                    if (user.getParticipants().size() == 1) {
                        VitalParticipant p = (VitalParticipant) user.getParticipants().iterator().next();
                        loginSuccessUrl= "courseHome.smvc?worksiteId=" + p.getWorksite().getId(); 
                    } else {
                        loginSuccessUrl= "/myCourses.smvc?viewBy=term";
                    }
                }
                // they are allowed in. send them to my courses, or course home if there's only one worksite.
                return Vital3Utils.redirectModelAndView(loginSuccessUrl);
            }
        }
	}
    

    public UserCourseManager getUserCourseManager() { return this.ucm; }
    public void setUserCourseManager(UserCourseManager userCourseManager) { this.ucm = userCourseManager; }
    
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }
    
}
