package ccnmtl.vital3.controllers;

import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.vital3.AssignmentResponse;
import ccnmtl.vital3.AssignmentResponseHistory;
import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.ucm.UserCourseManager;

public class CaptureResponseStateController extends Vital3CommandController {

   protected final Log logger = LogFactory.getLog(getClass());
   
   protected Integer getMinAccessLevel(Vital3Command command) {
       // @todo -- return UserCourseManager.ADMIN_ACCESS;
       return UserCourseManager.PUBLIC_ACCESS;
   }

   public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Vital3Command command, BindException errors) 
      throws Exception {
       List draftResponses = vital3DAO.findByPropertyValue(AssignmentResponse.class, "status", new Integer(0));
       Iterator iter = draftResponses.iterator();
       Comparator cmp = new AssignmentResponseHistoryComparator();
       int created = 0;
       int unchanged = 0;
       
       while (iter.hasNext()) {
          AssignmentResponse ar = (AssignmentResponse) iter.next();
          if (ar.getAssignment().isEssay()) {
          
              AssignmentResponseHistory arh = null;
              List lst = vital3DAO.findByTwoPropertyValues(AssignmentResponseHistory.class,
                   "assignmentId", ar.getAssignment().getId(), "participantId", ar.getParticipant().getId());
              
              if (lst.size() < 1) {
                  arh = new AssignmentResponseHistory(ar);
              } else {
                  // Grab the most recent history row for comparison purposes
                  Collections.sort(lst, cmp);
                  AssignmentResponseHistory arhExisting = (AssignmentResponseHistory) lst.get(0); 
                  
                  // See if the essay has changed.
                  if (!arhExisting.getText().equals(ar.getText())) {
                      arh = new AssignmentResponseHistory(ar);
                  }
              }
                
              if (arh != null) {
                  this.vital3DAO.save(AssignmentResponseHistory.class, arh);
                  created++;
              } else {
                  unchanged++;
              }
          }
       }
       
     Map model = new HashMap();
     model.put("created", Integer.toString(created));
     model.put("unchanged", Integer.toString(unchanged));
     
     return new ModelAndView("captureResponseState", model);
   }
   
    private class AssignmentResponseHistoryComparator implements Comparator {
    
        public int compare(Object o1, Object o2) {
            Date dt1 = ((AssignmentResponseHistory) o1).getDateCreated();
            Date dt2 = ((AssignmentResponseHistory) o2).getDateCreated();
            
            if (dt1.after(dt2)) return -1;
            if (dt1.before(dt2)) return 1;
            
            return 0;
        }
    }
}
