package ccnmtl.vital3.utils;

import java.io.Serializable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


//HttpSessionListener lets us know when sessions are created/destroyed
//HttpSessionActivationListener lets us know when sessions are activated/passivated (app reload, server restart)
//  note: this only applies if the app engine is configured to maintain persistent sessions
//Serializable is needed for all session objects

public class Vital3SessionListener implements Serializable, HttpSessionListener, HttpSessionActivationListener {
    protected final Log logger = LogFactory.getLog(getClass());
    
    //our counter, static so that serialization does not occur
    //each session will have change this variable through an event handler
    private static int numActiveSessions;
    private static Map sessionMap;
    
    static {
        numActiveSessions = 0;
        sessionMap = new WeakHashMap();
    }
  
    public Vital3SessionListener() {
        logger.debug("VitalSessionListener: calling constructor");    
    }
    
    // for active session count, get the servlet context's attribute sessionMap and get its size.
    
    public int getActiveSessionCount() {
         return numActiveSessions;
    }
    
    public int getSessionCount() {
        return sessionMap.size();
    }
    
    public Iterator getSessionIterator() {
        return sessionMap.keySet().iterator();
    }
    
    //fired when browser sends initial request
    public synchronized void sessionCreated(HttpSessionEvent se) {
        numActiveSessions++;
        
        HttpSession session = se.getSession();
        ServletContext application = session.getServletContext();
        //rudimentary singleton pattern: add myself to the app context the first time anyone starts a session.
        Vital3SessionListener sessionListener = (Vital3SessionListener) application.getAttribute("sessionListener");
        
        if (sessionListener == null) {
            logger.debug("Didn't find sessionListener, so adding myself.");
            application.setAttribute("sessionListener", this ); 
        }
        addToMap (session);
        logger.debug(session.getId() + " created, timeout=" + session.getMaxInactiveInterval() + ", now " + numActiveSessions);
    }

    //just decrement the counter, session is over (timeout or invalidation)
    public synchronized void sessionDestroyed(HttpSessionEvent se) {
        if ( numActiveSessions > 0)   numActiveSessions--; // ignore sessions created before the server was restarted.
        logger.debug( "A session has been destroyed. The number of active sessions is now " + numActiveSessions);
        // no need to call removeFromMap: since this is a weakHashMap, the session will magically disappear
        // from the map once it gets garbage collected.
    }
  
    public void sessionDidActivate(HttpSessionEvent se) {
        numActiveSessions++;
        logger.debug(se.getSession().getId() + " did activate, number of active sessions is now " + numActiveSessions);
    }
    
    public void sessionWillPassivate(HttpSessionEvent se) {
        numActiveSessions--;
        logger.debug(se.getSession().getId() + " will passivate, number of active sessions is now " + numActiveSessions);
    }
    
    private void addToMap (HttpSession se) {
        sessionMap.put(se, "");
    }
}
