package ccnmtl.vital3.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.BaseCommandController;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.dao.Vital3DAO;


/**
 * Vital3CommandController is meant to be the superclass for all controllers used in Vital3. Since it extends BaseCommandController, it
 * has all the capabilities of a BaseCommandController (see Spring docs for details). The handleRequestInternal method has a
 * try/catch block which is designed to standardize behavior of handling Vital3AuthViolationExceptions. If the Exception indicates
 * that the user was logged in, it directs them to an error page. If the Exception indicates that the user was not logged in,
 * it directs them to a login page.
 */
public abstract class Vital3CommandController extends BaseCommandController {
    
    protected final Log logger = LogFactory.getLog(getClass());
    protected MessageSourceAccessor messageSourceAccessor;
    
    protected TextFormatter textFormatter;
    protected MessageSource messageSource;
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;
    
    /**
     * Your subclass should call this method to get at the info about the current user and their relationship
     * to the current worksite. See UserContextInfo api for details. The UCI object is constructed piece-by-piece
     * in Vital3CommandController's handleRequestInternal method before it calls the handle method.
     */
    protected UserContextInfo getUserContextInfo(HttpServletRequest request) {
        return (UserContextInfo) request.getAttribute(UserContextInfo.ATTRIBUTE_NAME);
    }
    
    /**
     * IMPORTANT: You must override this method if you wish to provide any access control to your subclass. The 
     * default implementation will allow anyone (even if they are not logged in) to access your controller.
     * This method should never return null.
     *@see getRequestedWorksite
     */
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.PUBLIC_ACCESS;
    }
    
    /**
     * IMPORTANT: You must override this method if you wish to provide any access control to your subclass. The purpose
     * of this method is to return the worksite with which the user's request is concerned. Since the user's session is
     * essentially stateless, there is no real notion of a "current" worksite. Instead, the worksite which the user is
     * "currently inside" must be derived from each request. Sometimes it is as simple as passing a worksiteId parameter.
     * Other times, the worksite must be derived from some other parameter by, for example, following related instances
     * of some other entity up the object tree until worksite is reached.
     * <p>The Default implementation looks for the worksite on the command object using "getWorksite". If it is not there,
     * it will try "getWorksiteId" and if it is there it will find and instantiate the worksite, storing it using
     * "setWorksite". If it does not find a worksite using these methods, this method will return null.
     * <p>If you override this method, you MUST ensure that the worksite gets decorated by UCM! Note that if your
     * controller does not always need a worksite, it's okay to return null in those cases.
     * <p>For detailed info on security-enforcement, see the docs for handleRequestInternal and the class docs.
     * 
     *@see getMinAccessLevel
     */
    protected VitalWorksite getRequestedWorksite(Vital3Command command) throws Exception {
        
        VitalWorksite worksite = null;
        if (command != null) {
            // look for worksite on command object:
            worksite = command.getWorksite();
            if (worksite == null) {
                Long id = command.getWorksiteId();
                if (id != null) {
                    // find the worksite by id:
                    logger.debug("looking for worksite by id " + id);
                    worksite = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, id);
                    if (worksite != null) {
                        ucm.decorateWorksite(worksite, false, false);
                        command.setWorksite(worksite);
                    }
                }
            }
        }
        return worksite;
    }
    
    /**
     * The primary method of Vital3CommandController, which will catch and handle Vital3AuthViolationExceptions.
     * You should not override this method. See "handle" method docs for more info.
     * <p>This method provides a security architecture for all subclasses through the use of two "hook" methods:
     * getMinAccessLevel and getRequestedWorksite. The default behavior of these methods provides no security
     * whatsoever, but you may override one or both of them (depending on your needs) in order to easily take
     * advantage of the security architecture.
     * <p>The first thing this method does is call your validator if you provided one. If there were any global
     * errors during validation (e.g. invalid object id, database failure) then a redirect to the error page will 
     * be sent immediately. Next, the getRequestedWorksite method is called. See that method's docs for details.
     * Next, the logged-in-user's participant record for that worksite is found. If there is no logged in user or
     * there was no worksite found, this does not take place. Next, a UserContextInfo object is constructed using
     * the user, participant, and worksite values.
     * <p>Next, the getMinAccessLevel method is called. See that method's docs for details. The value it returns
     * is compared to the value returned from UserContextInfo.getAccessLevel and if the user does not have proper
     * access, a Vital3AuthViolationException is thrown. This will be caught by the surrounding try block - details
     * in a moment. Finally, the handle method is called. Your subclass must override the handle method.
     * <p>Any auth exceptions or dataretrieval exceptions will be caught and handled by this method. RuntimeExceptions
     * will be thrown up the stack. Caught exceptions are handled by redirecting the user to an Error page.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
        ModelAndView mav = null;
        BindException errors = null;
        UserContextInfo userInfo = null;
        VitalUser user = null;
        logger.debug("######## Vital3CommandController beginning handleRequestInternal...");
    
        TimeLog timeLog = new TimeLog();
        timeLog.setBefore();
    
        String userStringForLog = "n/a";
        String participantStringForLog = "n/a";
       
        HttpSession session = request.getSession(true);
        try {
            
            // If command and validator are specified, validate the command:
            Vital3Command command = null;
            if (getCommandClass() != null) {
                command = (Vital3Command) getCommand(request);
                logger.debug("validating request...");
                ServletRequestDataBinder binder = bindAndValidate(request, command);
                errors = binder.getErrors();
            }
            
            // get the user (might be used in the error template)
            user = ucm.getCLIU(session, false);
            
            // check for global validation errors before continuing with security:
            if (errors != null && errors.hasGlobalErrors()) {
                logger.debug (errors.toString());
                logger.debug("There were major validation errors. Creating error redirect MAV.");
                mav = Vital3Utils.createErrorMAV(errors, this.messageSourceAccessor);
                Map model = mav.getModel();
                model.put("currentUser", user);
                
            } else {
                
                // get worksite and participant:
                logger.debug("Vital3CommandController: about to try to find worksite and participant...");
                VitalWorksite worksite = getRequestedWorksite(command);
                
                VitalParticipant participant = null;
                if (user != null) {
                    if (worksite != null) participant = ucm.findParticipant(user, worksite);
                    // in case no worksite, make sure user's participants array is not null:
                    else user.setParticipants(new HashSet());
                }
                
                // construct and set up UserContextInfo:
                logger.debug("Vital3CommandController: setting up userContextInfo. User = " + user + ", participant = " + participant);
                userInfo = new UserContextInfo(user, participant);
                request.setAttribute(UserContextInfo.ATTRIBUTE_NAME, userInfo);
                
                // enforce access level requirement for this controller:
                Integer minAccessLevel = getMinAccessLevel(command);
                Integer userAccessLevel = userInfo.getAccessLevel();
                logger.debug("minAccessLevel is " + minAccessLevel + " and userAccessLevel is " + userAccessLevel);
                if (userAccessLevel.compareTo(minAccessLevel) < 0) {
                    // if the user was not logged in, throw the appropriate V3AVEx:
                    if (user == null) throw new Vital3AuthViolationException(false);
                    else throw new Vital3AuthViolationException();
                }
                if (user!= null) userStringForLog = user.getFullName();
                if (participant != null) participantStringForLog = participant.getWorksite().getTitle();
                
                logger.debug("finished with Vital3CommandController pre-loading code. Calling subclass 'handle' method...");
                // now call the "handle" method, which must be overridden by subclasses.

                mav = handle(request, response, command, errors);
            }
        
        } catch(Vital3AuthViolationException e) {
            
            try {
                // save this location for after the login
                // getServletPath returns the info after the context name and before query string, substring removes the leading "/"
                logger.debug("getServletPath = " + request.getServletPath());
                String formerUrl = request.getServletPath().substring(1);
                // get query parameters and their values. We must use this instead of request.getQueryString because of MockRequest's implementation.
                Enumeration parameterNames = request.getParameterNames();
                if (parameterNames.hasMoreElements()) {
                    formerUrl += "?";
                    while(parameterNames.hasMoreElements()) {
                        String pName = (String)parameterNames.nextElement();
                        String pValue = request.getParameter(pName);
                        formerUrl += pName + "=" + pValue;
                        if (parameterNames.hasMoreElements()) formerUrl += "&";
                    }
                }
                // this will be retrieved in loginProcessingController:
                session.setAttribute(Vital3Utils.afterLoginUrlSessionAttributeName, formerUrl);
                logger.debug("set afterLoginUrl = " + formerUrl);
            } catch (Exception doesntMatter) {
                //squelch exceptions
            }
            mav = Vital3Utils.handleAuthViolation(e);
            
        } catch(DataRetrievalFailureException e) {
            mav = Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        finally {
            ////////////////
            timeLog.setAfter( this.getClass().getName(), userStringForLog, participantStringForLog);
        }
        
        // add baseUrl to the model (e.g. "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/")
        View theView = mav.getView();
        if (!(theView instanceof RedirectView)) {
            ApplicationContext context = getApplicationContext();
            String baseUrl = (String) context.getBean("baseUrl");
            Map model = mav.getModel();
            model.put("baseUrl", baseUrl);
        }
        return mav;
	}
    
    /**
     * You must override one of these methods.
     */
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command command, BindException errors) throws Exception {
        
        return handle(request, response);
    }
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        return null;
    }
    
    /**
     * called to register custom property editors for the ServletRequestDataBinder.
     */
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        // register the customized Date property editor:
        TextFormatter dp = this.getTextFormatter();
        if (dp != null) binder.registerCustomEditor(Date.class, new CustomDateEditor(dp.getDateFormat(), true));
        // register string-trimmer for string properties. NOTE: The "true" means this will convert empty strings into null values!
        // note that oracle treats empty strings as nulls too.
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        // register the customized Collection property editor for binding "ids" as an ArrayList of Longs:
        binder.registerCustomEditor(ArrayList.class, "ids", new CustomCollectionEditor(ArrayList.class) {
            // Strangely, this log output does not always appear in the logs. I was seeing inconsistent behavior
            // at one point (would not work for deleting single elements at a time), but it now seems to be working every time.
            protected Object convertElement(Object element) {
                Log logger = LogFactory.getLog(getClass());
                if (element == null){
                    logger.debug("CustomCollectionEditor: Element is: null");
                    return null;
                } else {
                    logger.debug("CustomCollectionEditor: Element is: " + element.toString());
                    return new Long(element.toString());
                }
                
            }
        });
        // register the message (error) code resolver:
        binder.setMessageCodesResolver(new Vital3MessageCodesResolver());
    }
    
    public TextFormatter getTextFormatter() { return this.textFormatter; }
    public void setTextFormatter(TextFormatter textFormatter) { this.textFormatter = textFormatter; }
    
    public MessageSource getMessageSource() { return this.messageSource; }
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.messageSourceAccessor = new MessageSourceAccessor(messageSource);
    }
    
    public UserCourseManager getUserCourseManager() { return this.ucm; }
    public void setUserCourseManager(UserCourseManager userCourseManager) { this.ucm = userCourseManager; }
    
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }
    
}

class TimeLog {
    protected final Log logger = LogFactory.getLog(getClass());
    
    private long before, after;
    
    public void setBefore() {
        before = new GregorianCalendar().getTimeInMillis();
    }
    
    public void setAfter( String className, String userStringForLog,  String participantStringForLog ) {
        after = new GregorianCalendar().getTimeInMillis();
        logger.debug ( className + " took " + (after - before) + "ms for " + userStringForLog + " in " + participantStringForLog);
    }
}
