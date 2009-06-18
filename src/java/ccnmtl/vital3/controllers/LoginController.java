package ccnmtl.vital3.controllers;

import java.util.HashMap;
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
import java.io.*;
import java.net.*;


public class LoginController extends WebApplicationObjectSupport implements Controller {

    protected final Log logger = LogFactory.getLog(getClass());

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApplicationContext context = getApplicationContext();
        String baseUrl = (String) context.getBean("baseUrl");
        String message = request.getParameter("message");
        HashMap model = new HashMap();
        model.put("baseUrl", baseUrl);
        model.put("message", message);
	    String downtime = getServletContext().getInitParameter("downtime");
	    
	    
	    //Check that the streaming server is working:
	    /*
        try {
            String str;
            //URL myurl1 = new URL ("http://flurst.blurg.blurst.com:1220/");
            URL myurl1 = new URL ("http://mineola.cc.columbia.edu:1220/");
            URL myurl2 = new URL ("http://bergamot.cc.columbia.edu:1220/");
            BufferedReader in1 = new BufferedReader(new InputStreamReader(myurl1.openStream()));
            BufferedReader in2 = new BufferedReader(new InputStreamReader(myurl2.openStream()));
            
            java.lang.StringBuffer responseBuffer = new java.lang.StringBuffer ();
            while ((str = in1.readLine()) != null) {
                    responseBuffer.append(str).append("\n"); 
            }
            in1.close();
            while ((str = in2.readLine()) != null) {
                    responseBuffer.append(str).append("\n"); 
            }
            in2.close();
            String serverResponse = responseBuffer.toString();
            logger.info( "Got response: " + serverResponse);
	    } catch (MalformedURLException e) {
            logger.info( "Malformed URL.");
        } catch (IOException e) {
            logger.info( "I/O Exception.");
            model.put("message", "We're experiencing difficulties with our Quicktime server. If you're having trouble viewing videos, please log in again later or contact us for more information at <a href=\"mailto:ccnmtl-vital@columbia.edu\">ccnmtl-vital@columbia.edu</a>.");
        }
	    */
	    
	    if (downtime != null && downtime.equals("true")) {
	        return new ModelAndView("downtimeLogin", model);   
	    }
	    else {
	        return new ModelAndView("login", model);   
	    }
    }
}
