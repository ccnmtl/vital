package ccnmtl.vital3.controllers;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;


public class InsertUsersController extends Vital3CommandController {
   
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        String firstname = request.getParameter("firstName");
        String lastname = request.getParameter("lastName");
        String userIdString = request.getParameter("userIdString");
        Integer accessLevel = new Integer(0);
        String password = "lala";
   
        
        // construct an empty user using convinient VitalUser constructor which will instantiate a RawUCMUser object and transfer to it            
        VitalUser targetObj = new VitalUser(userIdString, "vital", accessLevel, null, password, firstname, lastname);
        
        // insert user using ColumbiaUCM.java           
        ucm.insertUser(targetObj);
        
        Map model = new HashMap();
        model.put("user", targetObj);
    
        return new ModelAndView("insertUser", model);
    }         
    
    
     

} 

