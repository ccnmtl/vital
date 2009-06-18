package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.json.*;

import ccnmtl.utils.*;
import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.ucm.ColumbiaUCM;
import ccnmtl.vital3.commands.*;
import ccnmtl.utils.OmniComparator;

public class ParticipantSearchController extends Vital3CommandController  {
    protected final Log logger = LogFactory.getLog(getClass());
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.TA_ACCESS;
    }
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command commandObj, BindException errors) throws Exception {
        String searchString = "";
        try {
            searchString = request.getParameter("searchString");
        } catch(Exception e) {
                // do nothing. This is an ajax controller; we'll just return an emtpy Json object.
        }
        JSONArray allResults = new JSONArray();
        List results = ucm.searchForUsers(searchString);
        Collections.sort(results, new OmniComparator(VitalUser.class, "getFirstName"));
        Iterator it = results.iterator();
        while (it.hasNext()) {
            VitalUser vUser = (VitalUser) it.next();
            JSONObject nextResult = new JSONObject();
            nextResult.put ("id", vUser.getId());
            nextResult.put ("firstName", vUser.getFirstName());
            nextResult.put ("lastName", vUser.getLastName());
            nextResult.put ("userIdString", vUser.getUserIdString());
            logger.debug(vUser.getUserIdString());
            allResults.put (nextResult);
        }
        String responseString = allResults.toString();
        Map model = new HashMap();
        model.put ("body", responseString);
        return new ModelAndView("ajaxResponse", model);
    }
}
