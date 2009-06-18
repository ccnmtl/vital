package ccnmtl.vital3.controllers;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Vital3Utils;

public class StaticContentController extends Vital3CommandController {
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String path = request.getParameter("path");
        try {
            if (path == null) throw new RuntimeException("Missing path parameter");
            // make sure the path is safe:
            //if (path.indexOf(path.indexOf("..") != -1 ))
                //throw new RuntimeException("Request may have been for protected area. The FBI is on the way to your house!");
            
        } catch (Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }
        
        HashMap model = new HashMap();
        UserContextInfo userInfo = getUserContextInfo(request);
        if (userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM)) model.put("admin", new Boolean(true));
        model.put("currentUser", userInfo.getUser());
        model.put("path", path);

        return new ModelAndView("staticTemplate", model);
    }
}

