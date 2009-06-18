package ccnmtl.vital3.controllers;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.*;
import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;

public class MaterialsLibController extends Vital3CommandController {

    // people must be at least students to view this page. Admin view requires TA permissions (see "handle" method).
    protected Integer getMinAccessLevel(Vital3Command command) {
        return UserCourseManager.STUDENT_ACCESS;
    }

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {

        HashMap model = new HashMap();

        // parse and/or validate parameters:
        String message = request.getParameter("message");

        UserContextInfo userInfo = getUserContextInfo(request);
        VitalWorksite worksite = userInfo.getWorksite();
        // load up the model:
        if (userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM)) model.put("admin", new Boolean(true));
        model.put("currentUser", userInfo.getUser());
        model.put("worksite", worksite);

        String limitBy = null;
        Long theId = null;

        try {
            if (request.getParameter("id") != null) theId = new Long(request.getParameter("id"));
            limitBy = request.getParameter("limitby");
        } catch(Exception e) {
            logger.info("Error parsing parameters!", e);
            return Vital3Utils.redirectModelAndView("error.smvc", e);
        }

        // init materials and CFVs
        // Init customFields and Values:
        Set allCustomFields = Vital3Utils.initCollection(VitalWorksite.class, worksite, "customFields", CustomField.class);
        Vital3Utils.initCollections(CustomField.class, allCustomFields, "values", CustomFieldValue.class);

        Set allUnits = Vital3Utils.initCollection(VitalWorksite.class, worksite, "units", Unit.class);
        Vital3Utils.initCollections(Unit.class, allUnits, "assignments", Assignment.class);


        List materials = new ArrayList();
        if (theId == null && limitBy == null) {
            materials.addAll(worksite.getMaterials());
        }
        else if (limitBy.equals("assignment")){
            Assignment assignment = (Assignment) vital3DAO.findById(Assignment.class, theId);
            if (assignment == null) {
                logger.info("Assignment " + theId + "not found.");
                Exception nex = new Exception ("Assignment " + theId + "not found.");
                return Vital3Utils.redirectModelAndView("error.smvc", nex);
            }
            materials.addAll(assignment.getMaterials());

        }
        else if (limitBy.equals("unit")){
            Unit unit = (Unit) vital3DAO.findById(Unit.class, theId);
            if (unit == null) {
                logger.info("Unit " + theId + "not found.");
                Exception nex = new Exception ("Unit " + theId + "not found.");
                return Vital3Utils.redirectModelAndView("error.smvc", nex);
            }
            materials.addAll(unit.getMaterials());
            // add materials of related assignments too:
            Set assignments = unit.getAssignments();
            Iterator iter = assignments.iterator();
            while (iter.hasNext()) {
                Assignment assignment = (Assignment) iter.next();
                materials.addAll(assignment.getMaterials());
            }
        }
        else  {
                Exception nex = new Exception ("Invalid parameters passed... limit by was" + limitBy + " and id was " + theId);
                return Vital3Utils.redirectModelAndView("error.smvc", nex);
        }
        
        // hide the unlisted materials from those who should not view them:
        if (!userInfo.hasPermission(UserCourseManager.CAN_ADMINISTRATE_WORKSITE_CURRICULUM)) {
            
            Iterator iter = materials.iterator();
            while (iter.hasNext()) {
                Material material = (Material)iter.next();
                if (material.isUnlisted()) iter.remove();
            }
        }
        

        Vital3Utils.initCollections(Material.class, materials, "customFieldValues", CustomFieldValue.class);
        
        VitalParticipant participant = userInfo.getParticipant();

        if (participant == null ) {
            Exception nex = new Exception ("No participant was found");
            return Vital3Utils.redirectModelAndView("error.smvc", nex);
        }
        
        //Which materials have annotations?
        Set materialsSet = new HashSet();
        materialsSet.addAll(materials);
        Set materialsWithAnnotations = new HashSet();
        Iterator iter = vital3DAO.getAnnotations( participant, materialsSet, null).iterator();
        while(iter.hasNext()) {
            materialsWithAnnotations.add(((Annotation) iter.next()).getMaterial().getId());
        }

        OmniComparator materialComp = new OmniComparator(Material.class, "getTitle");
        ArrayList sortedMaterials = new ArrayList(materials);
        Collections.sort(sortedMaterials, materialComp);

        model.put("materials", sortedMaterials);
        model.put("id", theId);
        model.put("limitBy", limitBy);
        model.put("materialsWithAnnotations", materialsWithAnnotations);
        model.put("textFormatter", this.getTextFormatter());

        if (message != null) model.put("message", message);

        return new ModelAndView("materialsLib", model);
    }
}

