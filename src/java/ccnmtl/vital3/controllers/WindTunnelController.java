package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.dao.Vital3DAO;


public class WindTunnelController extends WebApplicationObjectSupport implements Controller {
    
    
    protected final Log logger = LogFactory.getLog(getClass());

    private static Pattern windUserPattern;
    private static Pattern windAffilPattern;
    
    static {
        
        try {
            windUserPattern = Pattern.compile("<wind:user>(.*)</wind:user>");
            windAffilPattern = Pattern.compile("<wind:affil>(.*)</wind:affil>");
        } catch ( Exception e ) {
            throw new RuntimeException("error compiling regex patterns!");
        }
    }
        
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, DataRetrievalFailureException, Exception {

        Map model = new HashMap();
        
        ApplicationContext context = getApplicationContext();
        String baseUrl = (String) context.getBean("baseUrl");
        
        String service = null;
        String ticketid = null;
        String pgtUrl = null;
        String ticket = null;
        String wind_server = "wind.columbia.edu";
        String wind_login_uri = "/login";
        String wind_validate_uri = "/validate";
        String wind_realm = "cnmtl_full_np";
        String ticketTargetUrl = baseUrl + "windTunnel.smvc?ticket=";
        String windTunnelUrl = baseUrl + "windTunnel.smvc";
        String loginFailedUrl = "/login.smvc?message="+ URLEncoder.encode("Sorry, you are not an authorized member of any Vital courses.", "UTF-8");
        
        
        try {
            service = request.getParameter("service");
            ticketid = request.getParameter("ticketid");
            pgtUrl = request.getParameter("pgtUrl");
            ticket = request.getParameter("ticket");
            String error = request.getParameter("error");
        } catch(Exception e) {
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        
        //Send a user to Wind for authentication.        
        if(!(service == null) && (ticketid == null) && (pgtUrl == null) ) {
            String r = "https://" + wind_server + wind_login_uri + "?service=" + wind_realm + "&destination=" + URLEncoder.encode(windTunnelUrl, "UTF-8");
            
            logger.info("Forwarding user to Wind for authentication" );
            logger.info("The location we're going to is " + r  );
            
            return Vital3Utils.redirectModelAndView( r );
        }
        

        // Send a user to Wind to check their ticket.
        if((ticketid != null) || (!(ticket == null) && !(service == null) && !(pgtUrl == null))) {
            logger.info("Checking a new ticket with Wind.\n" );
            try {
                
                HttpSession session = request.getSession(true);
                session.removeAttribute(Vital3Utils.usernameSessionAttributeName);
                session.removeAttribute(Vital3Utils.affilListSessionAttributeName);


                // Ask Wind to validate the new ticket. 
                URL myurl = new URL ("https://" + wind_server + wind_validate_uri + "?sendxml=1&ticketid=" + ticketid);
                BufferedReader in = new BufferedReader(new InputStreamReader(myurl.openStream()));
                String str;
                java.lang.StringBuffer windResponseBuffer = new java.lang.StringBuffer ();
                while ((str = in.readLine()) != null) {
                    windResponseBuffer.append(str).append("\n"); 
                }
                in.close();
                String windResponse = windResponseBuffer.toString();
                logger.info( "Got response: " + windResponse);
                String uni = null;
                List affilList = new ArrayList();
                if (windResponse.indexOf("authenticationSuccess") > 0)
                {            
                    logger.info("Wind auth succeeded. Writing the affils to the session.");
                    Matcher matcher = windUserPattern.matcher(windResponse);
                    while (matcher.find()) {					
                        uni = matcher.group(1);
                    }
                    matcher = windAffilPattern.matcher(windResponse);
                    while (matcher.find()) {			
                        affilList.add(matcher.group(1));
                    }
                    session.setAttribute(Vital3Utils.usernameSessionAttributeName, uni);
                    session.setAttribute(Vital3Utils.affilListSessionAttributeName, affilList);
                    logger.debug("Added affilList.");
                }
                else {
                    logger.info( "Couldn't find the auth success string.");
                    return Vital3Utils.redirectModelAndView(loginFailedUrl);
                }
            
            } catch (MalformedURLException e) {
                logger.info( "Malformed URL exception", e);
            } catch (IOException e) {
                logger.info( "IO exception.", e);
            }
        }
          //forward to authorization processing
          ModelAndView  destination = Vital3Utils.redirectModelAndView( "loginProcessing.smvc?authMethod=wind");
          return destination;
    }

    
    
    
}

