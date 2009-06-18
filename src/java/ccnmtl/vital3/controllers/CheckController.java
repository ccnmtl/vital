package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.lang.Object;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.dao.Vital3DAO;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.*;

public class CheckController extends Vital3CommandController {
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.ADMIN_ACCESS;
    }
    
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {

		AssignmentResponse assignmentResponse = (AssignmentResponse) vital3DAO.findById(AssignmentResponse.class, new Long(0));
		
		//Find the same User id string in RawUCMUser as the one in Vital User.
		List listOfRawUsers = vital3DAO.findAll(RawUCMUser.class);
		List listOfVitalUser = vital3DAO.findAll(VitalUser.class);

		ArrayList listOfRawUserStr = new ArrayList();
		ArrayList listOfVitalUserStr = new ArrayList();
//================= for Raw IDs ========================
		//	ArrayList listofRaw =
		Iterator itRid = listOfRawUsers.iterator(); //ok
		int cntRawId = 0;
		int cntRawElementID = 0;
		while(itRid.hasNext())
		{
			Object item = (Object)itRid.next();
			RawUCMUser rawuser = (RawUCMUser) item;
			//List rawList = rawuser.getUserIdString();
			//cntRawElementID = count(rawuser.getUserIdString());
			//logger.debug("test RawUser: "+rawuser.getUserIdString()+cntRawElementID);
			String str_rawuser = rawuser.getUserIdString();
			listOfRawUserStr.add(rawuser.getUserIdString());
			cntRawId++;
		}
		
//		for (cntRawId=0; cntRawId < listOfRawUserStr.size(); cntRawId++)
//		{
	/*		if (listOfRawUserStr.contains(listOfRawUserStr.get(cntRawId)))
			{
				cntRawId++;
				//logger.debug("test RawUser: "+rawuser.getUserIdString()+cntRawId);
			}
	*/			//logger.debug("test RawUser: "+rawuser.getUserIdString()+":"+cntRawId);
//		}
//		logger.debug("test RawUser: "+rawuser.getUserIdString()+":"+cntRawId);
/*
		if (listOfRawUserStr.equals(listOfRawUserStr))
		{
			logger.debug("There is a duplicate :"+str_rawuser);
		}
		else
		{
			logger.debug("no match an duplicate :"+ str_rawuser);
		}
*/
		
/*
		String[] listRawUserArray;
		listRawUserArray = listOfRawUserStr.toArray();
		logger.debug("cntRawId: " + cntRawId);
		logger.debug("list of raw: " + listOfRawUserStr);
		
		for(int i = 0; i < listRawUserArray.length; i++)
		{
			logger.debug("test toArray:"+listRawUserArray[i]);
		} 
*/
//================= for Vital IDs ========================
		Iterator itVid = listOfVitalUser.iterator(); //ok
		int equal_element = 0;
		while(itVid.hasNext())
		{
			Object item2 = (Object)itVid.next();
			VitalUser vitalUser = (VitalUser) item2;
			logger.debug("VitalUser: " + vitalUser.getUserIdString());
			String str_vitaluser = vitalUser.getUserIdString();
			//ArrayList
			listOfVitalUserStr.add(vitalUser.getUserIdString());
			
			if (listOfRawUserStr.contains(str_vitaluser))
			{
				equal_element++;
				logger.debug("There is a match for :"+str_vitaluser);
				logger.debug("match count:"+equal_element);
			}
			else
			{
				logger.debug("<<no match with :"+ str_vitaluser);
			}
			equal_element=0; //initialize for the next entry
		}
		int count = 0;
		Iterator itRaw = listOfRawUsers.iterator(); //ok
		while (itRaw.hasNext())
		{
			Object item = (Object)itRaw.next();
			RawUCMUser rawuser = (RawUCMUser) item;
			String str_rawuser = rawuser.getUserIdString();
			//listOfRawUserStr.add(rawuser.getUserIdString());
			//cntRawId++;
			if (listOfVitalUserStr.contains(str_rawuser))
			{
				count++;
				logger.debug("There is a match for :"+str_rawuser);
				logger.debug("match count :"+str_rawuser+":"+count);
			}
			else
			{
				logger.debug("<<no match with :"+ str_rawuser);
			}
			count = 0;
		}//while ends

		logger.debug("list of raw: " + listOfRawUserStr);
		logger.debug("list of Vital: " + listOfVitalUserStr);


//================= for Vital Participant String match ========================
/*
		List listOfRawParticipant = vital3DAO.findAll(RawUCMParticipant.class);
//		List listOfVitalParticipant = vital3DAO.findAll(VitalParticipant.class);
		ArrayList listOfRawParticipantStr = new ArrayList();
//		ArrayList listOfVitalParticipantStr = new ArrayList();
		
		Iterator itRawPar = listOfRawParticipant.iterator();
		int cnt = 0;
		while(itRawPar.hasNext())
		{
			Object itemRawPar = (Object)itRawPar.next();
			RawUCMParticipant rawParticipant = (RawUCMParticipant) itemRawPar;
			logger.debug("test RawParticipant: " + rawParticipant.getParticipantIdString());
			listOfRawParticipantStr.add(rawParticipant.getParticipantIdString());
			cnt++;
		}
		logger.debug("list of RawParticipant count: " + cnt);
		logger.debug("list of RawParticipant: " + listOfRawParticipantStr);

//================= for RAW Participant String match ========================
		List listOfVitalParticipant = vital3DAO.findAll(VitalParticipant.class);
		ArrayList listOfVitalParticipantStr = new ArrayList();
		
		Iterator itVitalPar = listOfVitalParticipant.iterator();
		while(itVitalPar.hasNext())
		{
			Object itemVitalPar = (Object)itVitalPar.next();
			VitalParticipant VitalParticipant = (VitalParticipant) itemVitalPar;
			logger.debug("test VitalParticipant: " + VitalParticipant.getParticipantIdString());
			listOfVitalParticipantStr.add(VitalParticipant.getParticipantIdString());
		}
		logger.debug("list of VitalParticipant: " + listOfVitalParticipantStr);

*/

//================= for Vital Participant String match ========================

        /*
		List mockMaterialList = vital3DAO.findAll(Material.class);
		Material m1 = (Material) mockMaterialList.get(0);
        assertEquals("title 1", m1.getTitle());
        assertEquals(new Long(1), m1.getId());
        Material m2 = (Material) mockMaterialList.get(1);
        assertEquals("title 2", m2.getTitle());
        Material m3 = (Material) mockMaterialList.get(2);
        assertEquals("title 3", m3.getTitle());
		*/
		/*

        String message = request.getParameter("message");
        
        UserContextInfo userContextInfo = getUserContextInfo(request);
        VitalUser currentUser = userContextInfo.getUser();
        VitalParticipant participant = userContextInfo.getParticipant();
        VitalWorksite currentWorksite = participant.getWorksite();
        logger.debug("CourseHome controller.handle: user " + currentUser.getUserIdString() + " is here");	
				
				if (currentWorksite.getUnits().size() == 0) logger.info("This worksite has no units.");
				
				Iterator unitsIt =  currentWorksite.getUnits().iterator();
				while (unitsIt.hasNext()) {
					Unit unit = (Unit) unitsIt.next();
					Set unitMaterials = Vital3Utils.initM2MCollection(Unit.class, unit, Material.class);
					Set assignments = Vital3Utils.initCollection(Unit.class, unit, "assignments", Assignment.class);
					Vital3Utils.initM2MCollections(Assignment.class, assignments, Material.class);
					Iterator assignmentsIt = assignments.iterator();
					while (assignmentsIt.hasNext()) {
						Assignment assignment = (Assignment) assignmentsIt.next();
						logger.info ("Initializing responses for assigment" + assignment);
						Set responses = Vital3Utils.initCollection(Assignment.class, assignment, "responses", AssignmentResponse.class);
						Vital3Utils.initCollections(AssignmentResponse.class, responses, "comments", CustomFieldValue.class);
					}				
				}
				ArrayList units = currentWorksite.getUnitsSortedByDate();
				*/
				Map model = new HashMap();
				/*
				model.put("title", currentWorksite.getTitle());
				model.put("currentUser", currentUser);
				model.put("worksite", currentWorksite);
				model.put("units", units);
				model.put("participant", participant);
				model.put("message", message);
                DateProvider dp = this.getDateProvider();
                //if (dp == null) logger.error("DateProvider is null!");
                model.put("dateProvider", dp);

				if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0)
					model.put("admin", "true");
		*/			
				return new ModelAndView("courseHome", model);
    }
}
