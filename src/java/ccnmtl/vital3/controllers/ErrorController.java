package ccnmtl.vital3.controllers;

import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.utils.*;

public class ErrorController extends Vital3CommandController {
    
    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        // parse and/or validate parameters:
        String message = request.getParameter("message");
        
        // if there was no message, put this lame apology on the screen:
        if (message == null) message = "Sorry, an error occurred.";
        
        Map model = new HashMap();
        model.put("message", message);
        
        UserContextInfo userInfo = getUserContextInfo(request);
        VitalUser user = userInfo.getUser();
        if (user != null) model.put("currentUser", user);
        
        return new ModelAndView("error", model);
        
    }
    
}
