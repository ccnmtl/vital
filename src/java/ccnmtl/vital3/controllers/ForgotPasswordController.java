package ccnmtl.vital3.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.HormelClient;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.RawUCMUser;
import ccnmtl.vital3.ucm.UserCourseManager;


import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
// import javax.activation.*;


public class ForgotPasswordController extends WebApplicationObjectSupport implements Controller {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;
    
    public String disabled;
    public String smtpHostname;
    public String smtpPort;
    public String fromEmailAddress;
    public String problemEmailAddress;
    
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ApplicationContext context = getApplicationContext();
        String baseUrl = (String) context.getBean("baseUrl");
        
        String message = null;
        
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        
        if (action == null || action.equals("")) {
            // display the form
            // nothing to do!
        } else {
            // assume action == remind
            
            if (email == null || email.equals(""))
                message="Please go back and enter an e-mail address.";
            else {
                
                // determine if email exists in our database
                List results = vital3DAO.findByPropertyValue(RawUCMUser.class, "email", email);
                
                if (results.size() == 0) message = "Nobody with that e-mail address was found in the VITAL system.";
                else if (results.size() > 1) {
                    logger.warn("*** WARNING: duplicate e-mail address found: " + email);
                    message = "There were multiple users with that e-mail address. As a security precaution, no e-mail will be sent to you. To request your login information, inquire at ccnmtl-vital@ccnmtl.columbia.edu";
                         
                } else {
                
                    // if so, make sure user is set to use custom user auth (we can't help them with external auth)
                    
                    RawUCMUser rUser = (RawUCMUser) results.get(0);
                    String userIdString = rUser.getUserIdString();
                    VitalUser user = ucm.findUserByIdString(userIdString, false);
                    String password = user.getPassword();
                    String authMethod = user.getAuthMethod();
                    
                    // null implies "vital" as authMethod
                    if (authMethod != null && !authMethod.equals("vital"))
                        message = "This user is configured to use an external authentication system. See the instructions below.";
                    else {
                        ///this can be defined here:
                        String subject = "VITAL Login information";
                        String messageText = "Here is your login information for VITAL: \n\nusername: " + userIdString + "\npassword: " + password + "\n\nSee you soon.";
                        
                        if (isDisabled()) {
                            message = "Sorry, Vital is currently configured not to send mail. Please contact " + problemEmailAddress + " for help.";
                        }
                        else {
                            try {
                                send(smtpHostname, smtpPort, fromEmailAddress, email, subject, messageText);                 
                                message = "An e-mail with your login information was sent to " + email + ". You should receive it shortly.";
                            } catch (AddressException ae) {
                                message = "The address you entered does not look valid. Please try again.";
                            } catch (MessagingException me) {
                                message = "We were unable to send the message because of a problem with the server Please contact " + problemEmailAddress + " for help.";
                            }
                        }
                    }
                }
                
            }
        }
        
        HashMap model = new HashMap();
        model.put("externalAuthForgotPasswordUrl", ucm.getExternalAuthForgotPasswordUrl());
        model.put("baseUrl", baseUrl);
        model.put("message", message);
        model.put("action", action);
        return new ModelAndView("forgotPassword", model);   
    }
    
    public UserCourseManager getUserCourseManager() { return this.ucm; }
    public void setUserCourseManager(UserCourseManager userCourseManager) { this.ucm = userCourseManager; }
    
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }
    
    public String getDisabled() { return this.disabled; }
    public void setDisabled(String disabled) { this.disabled = disabled; }
    
    public String getSmtpHostname() { return this.smtpHostname; }
    public void setSmtpHostname(String smtpHostname) { this.smtpHostname = smtpHostname; }
    
    public String getSmtpPort() { return this.smtpPort; }
    public void setSmtpPort(String smtpPort) { this.smtpPort = smtpPort; }
    
    public String getFromEmailAddress() { return this.fromEmailAddress; }
    public void setFromEmailAddress(String fromEmailAddress) { this.fromEmailAddress = fromEmailAddress; }
    
    public String getProblemEmailAddress() { return this.problemEmailAddress; }
    public void setProblemEmailAddress(String problemEmailAddress) { this.problemEmailAddress = problemEmailAddress; }
    
    
    private boolean isDisabled() {
        return this.getDisabled().toLowerCase().equals("true");
    }
    
    
    /// Note: if you don't have sendmail or another common smtp server, try installing ssmtp.
    /// ssmtp is a simple mail server. (google it for more info.)
    
    public static void send(String smtpHost, String smtpPort,
                            String from, String to,
                            String subject, String content)
            throws AddressException, MessagingException {
            
        int smtpPortNumber;
        try {
            smtpPortNumber = Integer.parseInt(smtpPort.trim());
        } catch (NumberFormatException e){
            throw new RuntimeException("SMTP port number "  +  smtpPort + "is not an integer."); 
        }

            
        // Create a mail session
        java.util.Properties props = new java.util.Properties();
        
        
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", ""+smtpPort);
        
        Session session = Session.getDefaultInstance(props, null);

        // Construct the message
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(content);

        // Send the message
        Transport.send(msg);
    }
    
       
    
}
