package ccnmtl.vital3.controllers;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.*;


import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.ServletContext;

import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.TastyClient;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.*;


public class CacheFlushController extends Vital3CommandController {
    
    protected final Log logger = LogFactory.getLog(getClass());
    private TastyClient tastyClient;
    
    private Vital3SessionListener listener;
    
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.ADMIN_ACCESS;
    }
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command command, BindException errors) throws Exception {
        
        
        HttpSession session = request.getSession(true);
        
        ServletContext application = session.getServletContext();
        
        
        String mode = command.getMode();
        String message = request.getParameter("message");
        
        ModelAndView mav = null;
        
        if (mode != null && mode.equals("flush")) {
            
            // flush
            logger.info("Flushing cache...");
            vital3DAO.flushCache();
            logger.info("Completed cache flush. Check the code to make sure the class/collection you needed to flush was specified");
            
            // redirect to display mode
            mav = Vital3Utils.redirectModelAndView("cacheFlush.smvc?message=Flush+successful");
            
        } else {
            
            // display the form
            HashMap model = new HashMap();
            ApplicationContext context = getApplicationContext();
            
            // DBCP stats:
            BasicDataSource dbcp = (BasicDataSource) context.getBean("dataSource");
            
            model.put("activeConnections", new Integer(dbcp.getNumActive()));
            model.put("idleConnections", new Integer(dbcp.getNumIdle()));
            model.put("maxActiveConnections", new Integer(dbcp.getMaxActive()));
            model.put("maxIdleConnections", new Integer(dbcp.getMaxIdle()));
            
            
            context.getBean("dataSource");
            
            
            //logger.debug(vital3DAO.getHibernateTemplate().getSessionFactory().getSecondLevelCacheRegionNames());
            
            // spring properties (may differ on each context):
            
            Map statMap =  vital3DAO.getStatsFromHibernate();
            model.put ("hibernateStats", statMap.remove("SessionFactoryStats"));
            model.put ("OSCacheStats", statMap);

            
            //UserContextInfo userInfo = getUserContextInfo();
            //VitalUser user = userInfo.getUser();
            
            String errorString = "Okey Dokey";
            try {
                // ping tasty:
                Map testMap = tastyClient.getAllItemsAndTagsForUser("12345");
                
            } catch (Exception e) {
                errorString = e.getMessage();
            }
            model.put("tastyURL", tastyClient.getTastyServerUrl());
            model.put("tastyStatus", errorString);
            
            
            Vital3SessionListener sessionListener = (Vital3SessionListener) application.getAttribute("sessionListener");
            
            if (sessionListener != null ) {
                model.put("activeSessions", new Integer(sessionListener.getActiveSessionCount()));
                model.put("totalSessions", new Integer(sessionListener.getSessionCount()));
                
                List LoggedInUserList = new ArrayList();
                long now = System.currentTimeMillis();
                
                Iterator sessionIt = sessionListener.getSessionIterator();
                while (sessionIt.hasNext()) {

                    HttpSession nextSession = (HttpSession) sessionIt.next();
                    String nextName;
                    try {
                        VitalUser u = ucm.getCLIU(nextSession, false);
                        nextName = u.getFullNameReversed();
                    }
                    catch (Exception e) {
                        nextName = "(Not logged in)";
                    }
                    
                    try {
                        nextName = nextName + "; time since created, in seconds: " +  ( now - nextSession.getCreationTime()) / 1000;
                        nextName = nextName + "; time since last accessed, in seconds: " + (now - nextSession.getLastAccessedTime() ) / 1000 ;                        
                    }
                    catch (Exception e) {
                        nextName = nextName +  " (No time info available.)";
                    }
                    
                    LoggedInUserList.add( nextName);
                }
                model.put("loggedInUserList", LoggedInUserList);
            
            }
            
            
            model.put("message", message);
            mav = new ModelAndView("cacheFlush", model);
        }
        
        return mav;
    }
    
    public void setTastyClient(TastyClient tc) {
        this.tastyClient = tc;
    }
    public TastyClient getTastyClient() {
        return this.tastyClient;
    }

    
    
    
}
