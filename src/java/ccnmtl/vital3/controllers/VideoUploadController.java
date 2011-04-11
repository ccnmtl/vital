package ccnmtl.vital3.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import ccnmtl.utils.HormelClient;
import ccnmtl.utils.VideoUploadClient;
import ccnmtl.vital3.Material;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.RawUCMUser;
import ccnmtl.vital3.ucm.UserCourseManager;


import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
// import javax.activation.*;


public class VideoUploadController extends WebApplicationObjectSupport implements Controller {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    protected UserCourseManager ucm;
    protected Vital3DAO vital3DAO;

    private VideoUploadClient _videoUploadClient;     
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ApplicationContext context = getApplicationContext();

        String name = request.getParameter("name");
        String url = request.getParameter("url");
        String thumbUrl = request.getParameter("thumbUrl");
        Long worksiteId = new Long(request.getParameter("course"));
        String nonce = request.getParameter("nonce");

        // @todo -- authenticate
        // key, name, url, thumbUrl, nonce concatenated together
        // rehash & compare

        VitalWorksite worksite = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, worksiteId);
        if (worksite != null) {
            ucm.decorateWorksite(worksite, false, false);
        }

        Material video = Material.newVideo(worksite, Material.UNLISTED_ACCESS.intValue(), null, thumbUrl, name, url);
        vital3DAO.save(Material.class, video);

        String result="{\"success\": \"true\"}";

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
