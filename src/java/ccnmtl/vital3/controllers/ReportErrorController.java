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

public class ReportErrorController extends Vital3CommandController {
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HashMap model = new HashMap();
        UserContextInfo userInfo = getUserContextInfo(request);
        if (userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM)) model.put("admin", new Boolean(true));
        model.put("currentUser", userInfo.getUser());
        
        return new ModelAndView("reportErrorForm", model);
    }
}

