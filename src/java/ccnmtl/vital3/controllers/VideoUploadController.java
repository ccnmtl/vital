package ccnmtl.vital3.controllers;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import ccnmtl.utils.VideoUploadClient;
import ccnmtl.vital3.Material;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.UserCourseManager;

public class VideoUploadController extends WebApplicationObjectSupport implements Controller {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;

    private VideoUploadClient _videoUploadClient;  
    
    private boolean nullOrEmpty(String s) {
        return s == null || s.length() < 1;
    }
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result;
        
        if (!request.getMethod().equals("POST")) {
            result="{\"success\": \"false\", \"message\":\"Please POST your request\"}";
        } else {
            
            String title = request.getParameter("title");
            String url = request.getParameter("url");
            String thumb = request.getParameter("thumb");
            String course = request.getParameter("set_course");
            String userIdentifier = request.getParameter("as"); // Unsure this will be used?
            // String nonce = request.getParameter("nonce");
            
            if (nullOrEmpty(title)) {
                result="{\"success\": \"false\", \"message\":\"Video title is missing\"}";
            } else if (nullOrEmpty(url)) {
                result="{\"success\": \"false\", \"message\":\"Video url is missing\"}";
            } else if (nullOrEmpty(course)) {
                result="{\"success\": \"false\", \"message\":\"Course identifier is missing\"}";
            } else {
                
                // @todo -- authenticate
                
                // @todo -- error messaging for invalid course
                Long worksiteId = new Long(course);
                VitalWorksite worksite = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, worksiteId);
                if (worksite != null) {
                    ucm.decorateWorksite(worksite, false, false);
                }
        
                Material video = Material.newVideo(worksite, Material.UNLISTED_ACCESS.intValue(), null, thumb, title, url);
                vital3DAO.save(Material.class, video);
               
                // @todo -- send the user an email?
        
                result="{\"success\": \"true\"}";
            }
        }

        response.setContentType("application/json");
        response.setContentLength(result.length());
        
        byte[] b = result.getBytes();
        
        ServletOutputStream ouputStream = response.getOutputStream();
        ouputStream.write(b);
        ouputStream.flush();
        ouputStream.close(); 
        
        return null;
    }
    
    public UserCourseManager getUserCourseManager() { return this.ucm; }
    public void setUserCourseManager(UserCourseManager userCourseManager) { this.ucm = userCourseManager; }
    
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }
    

    public void setVideoUploadClient(VideoUploadClient tc) {
        this._videoUploadClient = tc;
    }
    public VideoUploadClient getUploadClient() {
        return this._videoUploadClient;
    }
}
