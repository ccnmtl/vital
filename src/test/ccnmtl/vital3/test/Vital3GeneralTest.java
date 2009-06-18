package ccnmtl.vital3.test;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.*;

import ccnmtl.utils.test.AbstractTestCase;
import ccnmtl.vital3.*;
import ccnmtl.vital3.controllers.*;
import ccnmtl.vital3.commands.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.ColumbiaUCM;
import ccnmtl.vital3.ucm.RawUCMTerm;
import ccnmtl.vital3.ucm.RawUCMWorksite;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.TextFormatter;
import ccnmtl.vital3.utils.Vital3Utils;

import ccnmtl.jtasty.*;
import ccnmtl.jtasty.dao.JTastyDAO;
import ccnmtl.jtasty.test.*;
import ccnmtl.jtasty.utils.*;
import ccnmtl.utils.test.*;
import ccnmtl.utils.*;

public class Vital3GeneralTest extends AbstractTestCase {

    private ApplicationContext ac;
    private TextFormatter textFormatter;
    private ColumbiaUCM ucm;
    private Vital3MockDAO vital3DAO;


    private String BacErrKey = "org.springframework.validation.BindException.basicAdminCommand";
    private String responseErrKey = "org.springframework.validation.BindException.responseCommand";

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public Vital3GeneralTest (String testName){
        super(testName);
    }
    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(Vital3GeneralTest.class);
    }

    public void setUp() throws Exception {

        // set private vars:
        ac = new FileSystemXmlApplicationContext("target/classes/Vital3-testContext.xml");
        textFormatter = (TextFormatter)ac.getBean("textFormatter");
        ucm = (ColumbiaUCM) ac.getBean("ucm");
        vital3DAO = (Vital3MockDAO) ac.getBean("vital3DAO");

        // Procedure for mock data setup:
        // 1) create and insert "parent" objects (ones with collections)
        // 2) create and insert "child" objects (ones inside collections)

        // Each object type has its own id generator, starting at 1.


        // construct raw term:
        RawUCMTerm rt1 = new RawUCMTerm(textFormatter.parse("2006/5/10-00:00:00"), "Spring 2006", textFormatter.parse("2006/1/17-00:00:00"));
        // insert raw term directly (there is no UCM method for this):
        vital3DAO.save(RawUCMTerm.class, rt1);

        // construct some VitalWorksites using ucm:
        VitalWorksite c1 = ucm.constructWorksite(rt1, "Announcement for course 1", "Astrophysics for Dummies");
        VitalWorksite c2 = ucm.constructWorksite(rt1, "Announcement for course 2", "Advanced Mad Cow Disease");
        VitalWorksite c3 = ucm.constructWorksite(rt1, "Announcement for course 3", "Apocryphal Eponymy");
        // insert using UCM:
        ucm.insertWorksites(Arrays.asList(new VitalWorksite[] {c1,c2,c3}));

        // Construct VitalUsers:
        VitalUser u1 = new VitalUser("em2140", "vital", UserCourseManager.PUBLIC_ACCESS, "em2140@columbia.edu", "password", "Eric", "Mattes");
        VitalUser u2 = new VitalUser("bg2000", "vital", UserCourseManager.PUBLIC_ACCESS, "bill@microsoft.com", "money", "Bill", "Gates");
        VitalUser u3 = new  VitalUser("bn579", "wind", UserCourseManager.ADMIN_ACCESS, "birgit@panix.com", null, "Birgit", "Nilsson");
        // insert using UCM:
        ucm.insertUsers(Arrays.asList(new VitalUser[] {u1,u2,u3}));

        // Construct & insert VitalParticipants using ucm:
        VitalParticipant vp1 = ucm.constructParticipant(u1, c1, UserCourseManager.STUDENT_ACCESS);
        VitalParticipant vp2 = ucm.constructParticipant(u2, c1, UserCourseManager.STUDENT_ACCESS);
        VitalParticipant vp3 = ucm.constructParticipant(u3, c1, UserCourseManager.ADMIN_ACCESS);
        VitalParticipant vp4 = ucm.constructParticipant(u3, c2, UserCourseManager.ADMIN_ACCESS);
        ucm.insertParticipants(Arrays.asList(new VitalParticipant[] {vp1,vp2,vp3,vp4}));

        // Insert Materials:
        Date myDate = textFormatter.parse("2005/9/29-5:01:02");
        Material m1 = Material.newVideo(c1, 0, null, "fake/url/to/thumb", "title 1", "fake/url/to/video"); m1.setDateModified(myDate);
        Material m2 = new Material(c1, 0, myDate, null, "fake/url/to/thumb", "title 2", "video", "fake/url/to/video");
        Material m3 = new Material(c2, 0, myDate, null, "fake/url/to/thumb", "title 3", "video", "fake/url/to/video");
        Material m4 = new Material(c2, 0, myDate, null, "fake/url/to/thumb4", "title 4", "video", "fake/url/to/video");
        vital3DAO.saveCollection(Material.class, Arrays.asList( new Material[]{ m1,m2,m3,m4 } ));

        // Insert some CustomFields:
        CustomField c1cf1 = new CustomField(c1,"c1-cf1",1,1);
        CustomField c2cf1 = new CustomField(c2,"c2-cf1",1,1);
        CustomField c2cf2 = new CustomField(c2,"c2-cf2",2,1);
        vital3DAO.saveCollection(CustomField.class, Arrays.asList( new CustomField[]{ c1cf1,c2cf1,c2cf2 } ));

        // Insert some CustomFieldValues:
        CustomFieldValue c1cf1m1 = new CustomFieldValue(c1cf1, m1, 1, "c1-cf1-m1");
        CustomFieldValue c1cf1m2 = new CustomFieldValue(c1cf1, m2, 1, "c1-cf1-m2");
        CustomFieldValue c2cf1m3 = new CustomFieldValue(c2cf1, m3, 1, "c2-cf1-m3");
        CustomFieldValue c2cf1m4 = new CustomFieldValue(c2cf1, m4, 1, "c2-cf1-m4");
        CustomFieldValue c2cf2m3 = new CustomFieldValue(c2cf2, m3, 2, "c2-cf2-m3");
        CustomFieldValue c2cf2m4 = new CustomFieldValue(c2cf2, m4, 2, "c2-cf2-m4");
        vital3DAO.saveCollection(CustomFieldValue.class, Arrays.asList( new CustomFieldValue[]{ c1cf1m1,c1cf1m2,c2cf1m3,c2cf1m4,c2cf2m3,c2cf2m4 } ));

        // Insert a unit:
        Date startDate = textFormatter.parse("2005/9/29-5:01:02");
        Date endDate = textFormatter.parse("2006/9/29-5:01:02");
        Unit unit1 = new Unit(c2, "test unit for unit test", endDate, startDate, "test unit", 0);
        vital3DAO.save(Unit.class, unit1);


        // Insert Questions
        Question q1 = new Question();
        q1.setText("What's your favorite color?");
        q1.setOrdinalValue(new Integer(4));
        q1.setAnswers(new HashSet());
        q1.setMaterialAssociations(new HashSet());
        vital3DAO.save(Question.class, q1);

        Question q2 = new Question();
        q2.setText("Who's your favorite person?");
        q2.setOrdinalValue(new Integer(5));
        q2.setAnswers(new HashSet());
        q2.setMaterialAssociations(new HashSet());
        vital3DAO.save(Question.class, q2);

        Question q3 = new Question();
        q3.setText("What's your favorite food?");
        q3.setOrdinalValue(new Integer(6));
        q3.setAnswers(new HashSet());
        q3.setMaterialAssociations(new HashSet());
        vital3DAO.save(Question.class, q3);

        Set questions = new HashSet();
        questions.add(q1);
        questions.add(q2);
        questions.add(q3);

        // Insert an Assignment (assignmentId = 1)
        Assignment ass1 = new Assignment();
        ass1.setUnit(unit1);
        ass1.setInstructions("instructions");
        ass1.setTitle("guidedlesson");
        ass1.setOrdinalValue(new Integer(4));
        ass1.setType("gl");
        ass1.setQuestions(questions);
        ass1.setMaterialAssociations(new TreeSet());
        ass1.setResponses(new TreeSet());
        vital3DAO.save(Assignment.class, ass1);

        // Set up the assignment field for Questions
        q1.setAssignment(ass1);
        q2.setAssignment(ass1);
        q3.setAssignment(ass1);


        
        // Insert a discussion assignment (assignmentId = 2)
        Assignment newAssignment = new Assignment();
        newAssignment.setUnit(unit1);
        newAssignment.setInstructions("instructions");
        newAssignment.setTitle("Discussion!");
        newAssignment.setOrdinalValue(new Integer(5));
        newAssignment.setType("discussion");
        newAssignment.setMaterialAssociations(new TreeSet());
        newAssignment.setResponses(new TreeSet());
        vital3DAO.save(Assignment.class, newAssignment);
        
        
        // test out the various discussion functions:
        //newAssignment.
        
        
        ///logger.debug ("ass2's id is " + newAssignment.getId());

    }

    public void testStuff() throws Exception {

        ///////////////////////////////////////////////////////////////////////
        //#####################################################################
        // Test Mock methods
        ///////////////////////////////////////////////////////////////////////


        // Test MockDAO retrieval methods and Material Equality:
        List mockMaterialList = vital3DAO.findAll(Material.class);
        Material m1 = (Material) mockMaterialList.get(0);
        assertEquals("title 1", m1.getTitle());
        assertEquals(new Long(1), m1.getId());
        Material m2 = (Material) mockMaterialList.get(1);
        assertEquals("title 2", m2.getTitle());
        Material m3 = (Material) mockMaterialList.get(2);
        assertEquals("title 3", m3.getTitle());

        Long m1id = m1.getId();
        Material m1twin = (Material) vital3DAO.findById(Material.class, m1id);
        // Test reference equality of retrieved results:
        assertTrue(m1 == m1twin);

        // Test MockDAO findBy methods:
        m1 = (Material) vital3DAO.findByPropertyValue(Material.class, "title", "title 1").get(0);
        m3 = (Material) vital3DAO.findByPropertyValue(Material.class, "type", "video").get(2);
        assertEquals("title 1", m1.getTitle());
        assertEquals("title 3", m3.getTitle());

        // Test MockDAO save & delete:
        // (first find a Worksite, then INSERT TWO NEW MATERIALS):
        VitalWorksite w1 = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, new Long(1));
        assertEquals("Announcement for course 1", w1.getAnnouncement());
        m1 = new Material(w1, 0, null, null, null, "about to be deleted 1", "video", null);
        m2 = new Material(w1, 0, null, null, null, "about to be deleted 2", "video", null);
        vital3DAO.save(Material.class, m1);
        vital3DAO.save(Material.class, m2);
        vital3DAO.delete(Material.class, m1);
        mockMaterialList = vital3DAO.findAll(Material.class);
        assertEquals(mockMaterialList.size(),5);
        m2 = (Material) mockMaterialList.get(4);
        assertEquals("about to be deleted 2", m2.getTitle());
        vital3DAO.delete(Material.class, m2);


        //////////////////////////////////////////////
        // ########## LOGIN #####################
        /////////////////////////////////

        LoginProcessingController login;
        MockHttpSession session;
        MockHttpServletRequest mockRequest;
        ModelAndView mav;
        RedirectView rv;
        VitalUser loggedInUser;
        
        
        //Normal login:
        vital3DAO.resetUCM();
        login = (LoginProcessingController) ac.getBean("loginProcessingController");
        session = new MockHttpSession();
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("authMethod","vital");
        mockRequest.addParameter("username","bg2000");
        mockRequest.addParameter("password","money");
        mav = login.handleRequest(mockRequest, (HttpServletResponse) null);
        rv = (RedirectView) mav.getView();
        assertEquals("courseHome.smvc?worksiteId=1", rv.getUrl());
        loggedInUser = ucm.getCLIU(session, false);
        assertEquals (loggedInUser.getFirstName(), "Bill");
        

        //WIND login:
        vital3DAO.resetUCM();
        login = (LoginProcessingController) ac.getBean("loginProcessingController");
        // NOTE: This session will be used throughout the following tests
        session = new MockHttpSession();
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("authMethod","wind");
        session.setAttribute(Vital3Utils.usernameSessionAttributeName, "bn579");
        mav = login.handleRequest(mockRequest, (HttpServletResponse) null);
        rv = (RedirectView) mav.getView();
        assertEquals("/myCourses.smvc?viewBy=term", rv.getUrl());
        loggedInUser = ucm.getCLIU(session, false);
        assertEquals (loggedInUser.getFirstName(), "Birgit");
        
        
        // My Courses:
        vital3DAO.resetUCM();
        MyCoursesController myCourses = (MyCoursesController) ac.getBean("myCoursesController");
        mockRequest = newMockRequest(session, "GET", null);
        mav = login.handleRequest(mockRequest, (HttpServletResponse) null);
        Map model = mav.getModel();
        Collection worksites = (Collection) model.get("worksites");
        
        
        
        ///////////////////////////////////////////////
        // ########## UTILS TESTS ###################
        ////////////////////////////////////////
        
        
        /************* General-purpose garbage string *****************/
        Random rand = new Random();
        char[] goodChar = {  'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
            'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '@', };
        
        int goodCharSize = goodChar.length;
        int garbageLength = 63523;
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < garbageLength; i++) { sb.append(goodChar[rand.nextInt(goodCharSize)]); }
        String garbage = sb.toString();
        assertEquals(garbage.length(), garbageLength);
        

        
        // 1) Sanity check for the text splitter: create random string, segment, reassemble, and compare to original.
        
        
        byte[] bytes;
        String randomString;
        
        assertEquals (0, Vital3Utils.segmentString(null, 3, 5).size());
        assertEquals (0, Vital3Utils.segmentString("", 3, 5).size());
        
        /////
        bytes = new byte[7];
        rand.nextBytes(bytes);
        randomString = new String(bytes);
        List segments = Vital3Utils.segmentString(randomString,10,10);
        assertEquals(1, segments.size());
        
        Iterator segmentIter = segments.iterator();
        StringBuffer buf = new StringBuffer();
        while (segmentIter.hasNext()) {
            buf.append((String) segmentIter.next());
        }
        assertEquals(buf.toString(), randomString);
        
        /////
        bytes = new byte[73];
        rand.nextBytes(bytes);
        randomString = new String(bytes);
        //System.out.println (randomString);
        System.out.println ( randomString.length());
        segments = Vital3Utils.segmentString(randomString,10,10);
        
        segmentIter = segments.iterator();
        buf = new StringBuffer();
        while (segmentIter.hasNext()) {
            buf.append((String) segmentIter.next());
        }
        assertEquals(buf.toString(), randomString);
        
        /////
        bytes = new byte[100];
        rand.nextBytes(bytes);
        randomString = new String(bytes);
        segments = Vital3Utils.segmentString(randomString,10,10);
        //assertEquals(10, segments.size());
        
        segmentIter = segments.iterator();
        buf = new StringBuffer();
        while (segmentIter.hasNext()) {
            buf.append((String) segmentIter.next());
        }
        assertEquals(buf.toString(), randomString);
        
        
                

        //////////////////////////////////////////////////
        // ####### CONTROLLER TESTS ####################
        ////////////////////////////////////////////
        
        vital3DAO.resetUCM();

        // sanity check:
        List allVitalWorksites = vital3DAO.findAll(VitalWorksite.class);
        assertEquals(3, allVitalWorksites.size());
        List allRawWorksites = vital3DAO.findAll(RawUCMWorksite.class);
        assertEquals(3, allRawWorksites.size());

        /************* MaterialsLibController *****************/

        // Run the controller:
        MaterialsLibController materialsLib = (MaterialsLibController) ac.getBean("materialsLibController");
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("worksiteId","1");
        mav = materialsLib.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        assertEquals("materialsLib", mav.getViewName());

        VitalWorksite worksite = (VitalWorksite) model.get("worksite");
        assertEquals(1, worksite.getNumCustomFields());
        List customFields = (List) worksite.getCustomFieldNames();
        assertEquals("c1-cf1", customFields.get(0));
        assertEquals("Announcement for course 1", worksite.getAnnouncement());
        assertEquals("Astrophysics for Dummies", worksite.getTitle());

        List materials = (List) worksite.getMaterialsSortedByTitle();
        assertEquals(2, materials.size());
        Material material = (Material) materials.get(0);
        assertEquals("title 1", material.getTitle());
        assertEquals(new Long(1), material.getId());
        List customFieldValues = material.getCustomFieldValuesStringList();
        assertEquals(1, customFieldValues.size());
        assertEquals("c1-cf1-m1", customFieldValues.get(0));

        assertTrue( model.get("admin") != null );

        vital3DAO.resetUCM();


        /******** Course Home **********/

        // Run the controller:
        CourseHomeController courseHome = (CourseHomeController) ac.getBean("courseHomeController");
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("worksiteId","1");
        mav = courseHome.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        assertEquals("courseHome", mav.getViewName());
        TextFormatter dp = courseHome.getTextFormatter();
        assertTrue(dp != null);
        assertEquals(dp, model.get("textFormatter"));

        vital3DAO.resetUCM();

        /******* BasicAdminController and responseController ********/
        BasicAdminController basicAdmin = (BasicAdminController) ac.getBean("basicAdminController");
	    ResponseController responseController = (ResponseController) ac.getBean("responseController");
        
        
        //String tastyServerUrl="http://kang.ccnmtl.columbia.edu:4090/eddie/jtasty/rest";
	    String tastyServerUrl="";
        
        String time = String.valueOf((new Date()).getTime()).substring(8);
        String random = String.valueOf(Math.random()).substring(2);
        String unique = (time+random);
        String serviceName = "javaTastyTest-" + unique;

        TastyBean tb = new TastyBean();
        JTastyMockDAO tdao = new JTastyMockDAO();
        tb.setDao(tdao);
        TastyClient tc = new TastyClient(tastyServerUrl, serviceName);
        tc.setTastyBean (tb);
        AnnotationController annotationController = (AnnotationController) ac.getBean("annotationController");
        annotationController.setTastyClient(tc);
        
        
        SpecialActionsController specialActionsController = (SpecialActionsController) ac.getBean("specialActionsController");
        
        //========================Material===========================//
        vital3DAO.resetUCM();

        // Test error-handling by passing a request for a nonexistent material:
        mockRequest = newMockRequestAEI(session, "GET","display","material","666");
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertEquals("error", mav.getViewName());
         assertEquals("The requested video was not found.<br>", model.get("message"));

        vital3DAO.resetUCM();

        // display an existing material:
        mockRequest = newMockRequestAEI(session, "GET","display","material","1");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertNoErrors(mav, BacErrKey);
        assertEquals("display", model.get("action"));
        // verify that the worksite was properly decorated:
        BasicAdminCommand command = (BasicAdminCommand) model.get("basicAdminCommand");
        m1 = command.getMaterial();
        worksite = (VitalWorksite) m1.getWorksite();
        assertEquals("Astrophysics for Dummies", worksite.getTitle());
        // verify the material title:
        assertEquals("title 1", m1.getTitle());

        vital3DAO.resetUCM();

        // update a material (and its cfvs):
        mockRequest = newMockRequestAEI(session, "POST","update","material","4");
        mockRequest.addParameter("accessLevel", Material.INSTRUCTORS_AND_ADMINS_ACCESS.toString());
        mockRequest.addParameter("thumbUrl", "fake/url/to/thumb4");
        mockRequest.addParameter("title","Material 4 updated");
        mockRequest.addParameter("type","video");
        mockRequest.addParameter("url","fake/url/to/video");
        mockRequest.addParameter("worksiteId", "1");
        mockRequest.addParameter("child0-0-id", "4");
        mockRequest.addParameter("child0-0-value", "c2-cf1-m4 updated");
        mockRequest.addParameter("child0-1-id", "6");
        mockRequest.addParameter("child0-1-value", "c2-cf2-m4 updated");        
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);

        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("materialsLib.smvc?worksiteId=2&message=update+successful", rv.getUrl());
        // make sure the title changed:
        Material m4 = (Material) vital3DAO.findById(Material.class, new Long(4));
        assertEquals("Material 4 updated", m4.getTitle());
        assertEquals("fake/url/to/thumb4", m4.getThumbUrl());
        // make sure customfieldvalues changes:
        CustomFieldValue cfv = (CustomFieldValue) vital3DAO.findById(CustomFieldValue.class, new Long(4));
        assertEquals("c2-cf1-m4 updated", cfv.getValue());
        cfv = (CustomFieldValue) vital3DAO.findById(CustomFieldValue.class, new Long(6));
        assertEquals("c2-cf2-m4 updated", cfv.getValue());

        vital3DAO.resetUCM();

        // request a 'new' material form:
        mockRequest = newMockRequestAEI(session, "GET","new","material",null);
        mockRequest.addParameter("worksiteId","2");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertNoErrors(mav, BacErrKey);
        assertEquals("new", model.get("action"));

        vital3DAO.resetUCM();

        // insert a new material, but use whitespace title + bad date:
        mockRequest = newMockRequestAEI(session, "POST","insert","material",null);
        mockRequest.addParameter("accessLevel", Material.INSTRUCTORS_AND_ADMINS_ACCESS.toString());
        // note: this date format is very strict (e.g. must use three-part time):
        mockRequest.addParameter("dateModified","xxxx");
        mockRequest.addParameter("type","video");
        mockRequest.addParameter("worksiteId","2"); // should also try testing forgetting the worksiteId
        mockRequest.addParameter("title","   ");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        assertEquals("adminMaterial", mav.getViewName());
        model = mav.getModel();
        BindException bex = (BindException) model.get(BacErrKey);
        ObjectError err = bex.getFieldError("title");
        assertEquals("error.missing.title",err.getCode());
        err = bex.getFieldError("dateModified");
        assertEquals("error.invalid.dateModified",err.getCode());



        vital3DAO.resetUCM();

        // insert a new material (with cfvs):
        mockRequest = newMockRequestAEI(session, "POST","insert","material",null);
        mockRequest.addParameter("accessLevel", Material.INSTRUCTORS_AND_ADMINS_ACCESS.toString());

        // System will provide the Date // note: this date format is very strict (e.g. must use three-part time):
        //mockRequest.addParameter("dateModified","2006/1/19-11:05:00");

        // test string-trimmer:
        mockRequest.addParameter("thumbUrl","   fake/thumb/url  ");
        mockRequest.addParameter("title","Newly inserted material");
        mockRequest.addParameter("type","video");
        mockRequest.addParameter("url","fake/url/to/video");
        // see if numbers get trimmed:
        mockRequest.addParameter("worksiteId"," 2 ");
        mockRequest.addParameter("child0-0-id","2");
        mockRequest.addParameter("child0-0-value","cf1value");
        mockRequest.addParameter("child0-1-id","3");
        mockRequest.addParameter("child0-1-value","");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("materialsLib.smvc?worksiteId=2&message=insert+successful", rv.getUrl());
        // find the new material and verify:
        List results = vital3DAO.findByPropertyValue(Material.class, "title", "Newly inserted material");
        Material m5 = (Material) results.get(0);
        assertEquals("fake/thumb/url", m5.getThumbUrl());
        assertEquals(new Long(7), m5.getId());
        assertEquals(new Long(2), m5.getWorksite().getId());
        // check the material's customfieldvalues:
        Vital3Utils.initCollection(Material.class, m5, "customFieldValues", CustomFieldValue.class);
        List cfvList = m5.getCustomFieldValuesStringList();
        assertEquals("cf1value", cfvList.get(0));
        assertEquals("", cfvList.get(1));

        vital3DAO.resetUCM();

        // delete a material (and its cfvs):
        mockRequest = newMockRequestAEI(session, "POST","delete","material","7");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("materialsLib.smvc?worksiteId=2&message=delete+successful", rv.getUrl());
        // try to find the material, verify it's gone:
        m5 = (Material) vital3DAO.findById(Material.class, new Long(7));
        assertTrue(m5 == null);
        // try to find its CFVs and verify they're gone:
        results = vital3DAO.findByPropertyValue(CustomFieldValue.class, "value", "cf1value");
        assertTrue(results.isEmpty());

        vital3DAO.resetUCM();

        //==========================User================================//
        // request a 'new' user form:
        mockRequest = newMockRequestAEI(session, "GET","new","user",null);
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertNoErrors(mav, BacErrKey);
        assertEquals("new", model.get("action"));
        
        vital3DAO.resetUCM();
        
        // insert a user:
        mockRequest = newMockRequestAEI(session, "POST","insert","user",null);
        mockRequest.addParameter("userIdString", "unitTestUser");
        mockRequest.addParameter("firstName", "Joe");
        mockRequest.addParameter("lastName", "Cavalieri");
        mockRequest.addParameter("password", "rosa");
        mockRequest.addParameter("accessLevel", "0");
        mockRequest.addParameter("authMethod", "vital");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("listing.smvc?mode=user&message=insert+successful", rv.getUrl());
        // find the new user and verify:
        results = vital3DAO.findAll(VitalUser.class);
        VitalUser user = (VitalUser) results.get(results.size()-1);
        assertEquals(new Long(4), user.getId());
        assertEquals(UserCourseManager.PUBLIC_ACCESS, user.getAccessLevel());
        
        // display user form:
        mockRequest = newMockRequestAEI(session, "GET","display","user","4");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        
        BasicAdminCommand bacomm = (BasicAdminCommand) model.get("basicAdminCommand");
        user = bacomm.getUser();
        assertEquals("unitTestUser", user.getUserIdString());
                     
        
        //========================Participant===========================//
        // request a 'new' participant form:
        mockRequest = newMockRequestAEI(session, "GET","new","participant",null);
        mockRequest.addParameter("worksiteId","2");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertNoErrors(mav, BacErrKey);
        assertEquals("new", model.get("action"));


        vital3DAO.resetUCM();

        // insert a participant:
        mockRequest = newMockRequestAEI(session, "POST","insert","participant",null);
        mockRequest.addParameter("accessLevel", UserCourseManager.STUDENT_ACCESS.toString());
        mockRequest.addParameter("userId", "1");
        mockRequest.addParameter("worksiteId", "2");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("listing.smvc?mode=roster&id=2&message=insert+successful", rv.getUrl());
        // find the new participant and verify:
        results = vital3DAO.findAll(VitalParticipant.class);
        VitalParticipant p1 = (VitalParticipant) results.get(results.size()-1);
        assertEquals(new Long(1), p1.getUser().getId());
        assertEquals(new Long(2), p1.getWorksite().getId());
        assertEquals(UserCourseManager.STUDENT_ACCESS, p1.getAccessLevel());

        vital3DAO.resetUCM();

        // delete multiple participants:
        mockRequest = newMockRequestAEI(session, "POST","delete","participant",null);
        mockRequest.addParameter("ids", new String[]{"1","2"});
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("listing.smvc?mode=roster&id=1&message=delete+successful", rv.getUrl());
        // check number of participants to verify:
        results = vital3DAO.findAll(VitalParticipant.class);
        assertEquals(3, results.size());

        vital3DAO.resetUCM();
        
        
        // insert a participant and a user at once:
        mockRequest = newMockRequestAEI(session, "POST","insert","participant",null);
        mockRequest.addParameter("accessLevel", UserCourseManager.STUDENT_ACCESS.toString());
        mockRequest.addParameter("userId", "-1");
        mockRequest.addParameter("worksiteId", "2");
        mockRequest.addParameter("userIdString", "jaysee");
        mockRequest.addParameter("firstName", "Jim");
        mockRequest.addParameter("lastName", "Cramer");
        mockRequest.addParameter("password", "actionalerts");
        mockRequest.addParameter("email", "jim@thestreet.com");
        mockRequest.addParameter("authMethod", "vital");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        Vital3Utils.debugMap(model);
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("listing.smvc?mode=roster&id=2&message=insert+successful", rv.getUrl());
        // find the new participant and user and verify:
        results = vital3DAO.findAll(VitalParticipant.class);
        p1 = (VitalParticipant) results.get(results.size()-1);
        assertEquals("Cramer", p1.getUser().getLastName());
        assertEquals(new Long(2), p1.getWorksite().getId());
        assertEquals(UserCourseManager.STUDENT_ACCESS, p1.getAccessLevel());
        


       //========================Assignment===========================//
        vital3DAO.resetUCM();
        // request a new assignment form:
        mockRequest = newMockRequestAEI(session, "GET","new","assignment",null);
        mockRequest.addParameter("unitId","1");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertNoErrors(mav, BacErrKey);
        assertEquals("new", model.get("action"));


        vital3DAO.resetUCM();
        // insert a new assignment (assignmentId = 2, type = essay)
        mockRequest = newMockRequestAEI(session, "POST","insert","assignment",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("title","lalala");
        mockRequest.addParameter("ordinalValue", "7");
        mockRequest.addParameter("type","essay");
        mockRequest.addParameter("instructions", "Be careful");
        mockRequest.addParameter("dateDue", "2006/10/01-23:59:59");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("courseHome.smvc?worksiteId=2&message=insert+successful", rv.getUrl());

        // find the new assignment and verify:
        results = vital3DAO.findAll(Assignment.class);
        Assignment ass2 = (Assignment) results.get(results.size()-1);
        assertEquals(new Long(3), ass2.getId());
        assertEquals(new Long(1), ass2.getUnit().getId());
        assertEquals(new Long(2), ass2.getUnit().getWorksite().getId());
        assertEquals("lalala", ass2.getTitle());


        vital3DAO.resetUCM();
        // insert a new assignment (assignmentId = 3, type = guided lesson)
        mockRequest = newMockRequestAEI(session, "POST","insert","assignment",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("title","GL assignment");
        mockRequest.addParameter("ordinalValue", "17");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("dateDue", "2007/05/01-23:59:59");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("listing.smvc?mode=glQuestions&id=4&message=insert+successful. Now+you+may+add+questions.", rv.getUrl());

        // find the new assignment and verify:
        results = vital3DAO.findAll(Assignment.class);
        Assignment ass3 = (Assignment) results.get(results.size()-1);
        assertEquals(new Long(4), ass3.getId());
        assertEquals(new Long(1), ass3.getUnit().getId());
        assertEquals(new Long(2), ass3.getUnit().getWorksite().getId());
        assertEquals("GL assignment", ass3.getTitle());


        /// STARTING DISCUSSION TEST:
        
        vital3DAO.resetUCM();
        // insert a new assignment (assignmentId = 5, type = discussion)
        mockRequest = newMockRequestAEI(session, "POST","insert","assignment",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("title","Discussion");
        mockRequest.addParameter("ordinalValue", "25");
        mockRequest.addParameter("type","discussion");
        mockRequest.addParameter("dateDue", "2007/05/01-23:59:59");
        // logger.debug( rv.getUrl());
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
                
       	assertEquals("courseHome.smvc?worksiteId=2&message=insert+successful", rv.getUrl());
        
        
        Assignment a5 = (Assignment) vital3DAO.findById(Assignment.class, new Long(5));
        // logger.debug ("assignment " + a5.getId() + " is a " + a5.getType());
        
        
        // find the new assignment and verify:
        results = vital3DAO.findAll(Assignment.class);
        Assignment ass5 = (Assignment) results.get(results.size()-1);
        assertEquals(new Long(5), ass5.getId());
        assertEquals(new Long(1), ass5.getUnit().getId());
        assertEquals(new Long(2), ass5.getUnit().getWorksite().getId());
        assertEquals("Discussion", ass5.getTitle());
        assertEquals(true, ass5.isDiscussion());

        mockRequest = newMockRequestAEI(session, "GET","new","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","5");
        mockRequest.addParameter("participantId","4");
        mockRequest.addParameter("type","discussion");
        mav = responseController.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        assertNoErrors(mav, responseErrKey);
        assertEquals("new", model.get("action"));
        
        

        //========================AssignmentResponse===========================//
        //EDDIE ADDING
        vital3DAO.resetUCM();
        // request a 'new' assignmentResponse form (type:Discussion)
        mockRequest = newMockRequestAEI(session, "GET","new","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","5");
        mockRequest.addParameter("participantId","4");
        mockRequest.addParameter("type","discussion");
        mav = responseController.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        assertNoErrors(mav, responseErrKey);
        assertEquals("new", model.get("action"));
        
        rv = (RedirectView) mav.getView();

        assertEquals (null, rv);
        
        /*
        vital3DAO.resetUCM();
        // request a 'new' assignmentResponse form (type:Essay)
        mockRequest = newMockRequestAEI(session, "GET","new","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","2");
        mockRequest.addParameter("participantId","4");
        mockRequest.addParameter("type","essay");
        mav = responseController.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        assertNoErrors(mav, responseErrKey);
        assertEquals("new", model.get("action"));
        */

        vital3DAO.resetUCM();


        


        results = vital3DAO.findAll(Assignment.class);
        
        /*
        Assignment a1 = (Assignment) vital3DAO.findById(Assignment.class, new Long(1));
        
        logger.debug ("assignment " + a1.getId() + " is a " + a1.getType());
        Assignment a2 = (Assignment) vital3DAO.findById(Assignment.class, new Long(2));
        logger.debug ("assignment " + a2.getId() + " is a " + a2.getType());
        
        Assignment a3 = (Assignment) vital3DAO.findById(Assignment.class, new Long(3));
        logger.debug ("assignment " + a3.getId() + " is a " + a3.getType());
        
        */
        
        
        // insert a new assignmentResponse (type:Essay, assignmentResponseId=1)
        mockRequest = newMockRequestAEI(session, "POST","insert","assignmentResponse",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("assignmentId","3");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("type","essay");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "To get to the other slide.");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=1", rv.getUrl());

        // find the new assignment and verify (type:Essay)
        results = vital3DAO.findAll(AssignmentResponse.class);
        AssignmentResponse ar = (AssignmentResponse) results.get(results.size()-1);
        assertEquals(new Long(1), ar.getId());
        assertEquals(new Long(1), ar.getAssignment().getUnit().getId());
        assertEquals(new Long(2), ar.getAssignment().getUnit().getWorksite().getId());
        assertEquals("essay", ar.getType());
        assertEquals(new Integer(0), ar.getStatus());

        vital3DAO.resetUCM();

        // insert a new assignmentResponse (type:Essay, assignmentResponseId=2)
        mockRequest = newMockRequestAEI(session, "POST","insert","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","3");
        mockRequest.addParameter("participantId", "5");
        mockRequest.addParameter("type","essay");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "why is the sky purple?");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=2", rv.getUrl());

        // find the new assignment and verify (type:Essay)
        ar = (AssignmentResponse) vital3DAO.findById(AssignmentResponse.class, new Long(2));
        assertEquals(new Long(1), ar.getAssignment().getUnit().getId());
        assertEquals(new Long(2), ar.getAssignment().getUnit().getWorksite().getId());
        assertEquals("essay", ar.getType());
        assertEquals(new Integer(0), ar.getStatus());

        vital3DAO.resetUCM();

        // update an assignmentResponse (type:Essay, assignmentResponseId=1), making it submitted
        mockRequest = newMockRequestAEI(session, "POST","update","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","3");
        mockRequest.addParameter("id","1");
        mockRequest.addParameter("status", "1");
        mockRequest.addParameter("text", garbage);
        mockRequest.addParameter("type", "essay");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=1", rv.getUrl());
       	
        // find the answer and verify:
        AssignmentResponse essayResponse = (AssignmentResponse) vital3DAO.findById(AssignmentResponse.class, new Long(1));
        assertEquals(garbage, essayResponse.getText());
       	// System.out.println(garbage);
       	

        // find the new assignment and verify:
        results = vital3DAO.findAll(AssignmentResponse.class);
        ar = (AssignmentResponse) vital3DAO.findById(AssignmentResponse.class, new Long(1));
        assertEquals(new Long(3), ar.getAssignment().getId());
        assertEquals(new Long(2), ar.getAssignment().getUnit().getWorksite().getId());
        assertEquals("essay", ar.getType());
        assertEquals(new Integer(1), ar.getStatus());
        vital3DAO.resetUCM();

        // request a 'new' assignmentResponse form (type:Guided Lessons)
        mockRequest = newMockRequestAEI(session, "GET","new","assignmentResponse",null);
        mockRequest.addParameter("assignmentId","1");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("participantId", "5");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        assertNoErrors(mav, responseErrKey);
        model = mav.getModel();
        assertEquals("new", model.get("action"));

        vital3DAO.resetUCM();   


        // insert a new assignmentResponse (type:Guided Lessons, assignmentResponseId=3)
        // when submitting the first question answer, it's an INSERT action and it will set up the objects Answer and AssignmentResponse
        mockRequest = newMockRequestAEI(session, "POST","insert","assignmentResponse",null);
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("assignmentId","1");
        mockRequest.addParameter("questionId", "1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "1.1");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        if (mav == null ) fail("Null mav.");
        rv = (RedirectView) mav.getView();
       
        if (rv == null ) fail("Null rv.");
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=3&type=gl", rv.getUrl());

        // find the new answer and verify:
        Answer result = (Answer)vital3DAO.findById(Answer.class, new Long(1));
        assertEquals("What's your favorite color?", result.getQuestion().getText());
        assertEquals("1.1", result.getText());

        ar = (AssignmentResponse)vital3DAO.findById(AssignmentResponse.class, new Long(3));


        vital3DAO.resetUCM();
        // display an assignmentResponse (type:Guided Lessons)
        mockRequest = newMockRequestAEI(session, "GET","display","assignmentResponse","3");
        mav = responseController.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        assertNoErrors(mav, responseErrKey);
        assertEquals("display", model.get("action"));


        vital3DAO.resetUCM();

        // update an assignmentResponse (type:Guided Lessons, assignmentResponseId=3)
        // When submitting the second until the last question, it's an UPDATE action
        mockRequest = newMockRequestAEI(session, "POST","update","assignmentResponse","3");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("assignmentId","1");
        mockRequest.addParameter("questionId", "2");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "answer:question2");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=3&type=gl", rv.getUrl());

        // find the answer and verify:
        result = (Answer)vital3DAO.findById(Answer.class, new Long(2));
        assertEquals("Who's your favorite person?", result.getQuestion().getText());
        assertEquals("answer:question2", result.getText());;

    
        vital3DAO.resetUCM();
        
        /// Insert 24K of garbage into the last question of the guided lesson and check that it comes out the same.
        /// Also, regardless of the 'status' parameter, this will SUBMIT the assignment response, as it is the final question
        mockRequest = newMockRequestAEI(session, "POST","update","assignmentResponse","3");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("assignmentId","1");
        mockRequest.addParameter("questionId", "3");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", garbage);

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();

        //make sure the last question makes it go to review page:
        assertEquals("reviewGuidedLesson.smvc?id=3", rv.getUrl());
        

        //assertEquals("response.smvc?action=display&entity=assignmentResponse&id=3&type=gl", rv.getUrl());

        // find the answer and verify:
        result = (Answer)vital3DAO.findById(Answer.class, new Long(3));
        assertEquals("What's your favorite food?", result.getQuestion().getText());
        
        assertEquals(garbage, result.getText());
        
        // did the assignment get submitted ?
        assertEquals(1, result.getAssignmentResponse().getStatus().intValue());
        
        
        /*
        *
        * comment on questions 1 and 3 and save them as draft
        */
    
    

        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","insertComments");
        mockRequest.addParameter("status","0");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("assignmentResponseId","3");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("newComment1", "Comment 1.1");
        mockRequest.addParameter("newComment2", "");
        mockRequest.addParameter("newComment3", "Comment 3.1");

        mav = specialActionsController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        
        assertEquals("reviewGuidedLesson.smvc?id=3", rv.getUrl());
        
        VitalParticipant birgit = (VitalParticipant) vital3DAO.findById(VitalParticipant.class, new Long(4));
        Answer answer1 = (Answer) vital3DAO.findById(Answer.class, new Long(1));
        Answer answer2 = (Answer) vital3DAO.findById(Answer.class, new Long(2));
        Answer answer3 = (Answer) vital3DAO.findById(Answer.class, new Long(3));
        
        /*
        confirm the comments went in 
        confirm they have status zero
        confirm a comment on 2 was created
        confirm it has status zero and blank text
        */
        assertEquals( answer1.getComment(birgit).getText() , "Comment 1.1" );
        assertEquals( answer2.getComment(birgit).getText() , "" );
        assertEquals( answer3.getComment(birgit).getText() , "Comment 3.1" );
        
        assertEquals( answer1.getComment(birgit).getStatus().intValue() , 0);
        assertEquals( answer2.getComment(birgit).getStatus().intValue() , 0);
        assertEquals( answer3.getComment(birgit).getStatus().intValue() , 0);
        

        // now let's submit the comments

        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","updateComments");
        mockRequest.addParameter("status","1");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("assignmentResponseId","3");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("updateComment1", "Comment 1.2");
        mockRequest.addParameter("updateComment2", "Comment 2.2");
        mockRequest.addParameter("updateComment3", "Comment 3.2");

        mav = specialActionsController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        
        assertEquals(rv.getUrl() , "reviewGuidedLesson.smvc?id=3" );
        
        
        ///make sure these new comments are in fact persisted.
        Comment comment1 = (Comment) vital3DAO.findById(Comment.class, new Long(1));
        Comment comment2 = (Comment) vital3DAO.findById(Comment.class, new Long(2));
        Comment comment3 = (Comment) vital3DAO.findById(Comment.class, new Long(3));
        assertEquals( comment1.getText() , "Comment 1.2");
        assertEquals( comment2.getText() , "Comment 2.2");
        assertEquals( comment3.getText() , "Comment 3.2");
        
        // make sure the answers are still in there.
        answer1 = (Answer) vital3DAO.findById(Answer.class, new Long(1));
        answer2 = (Answer) vital3DAO.findById(Answer.class, new Long(2));
        answer3 = (Answer) vital3DAO.findById(Answer.class, new Long(3));
        assertEquals( answer1.getComment(birgit).getStatus().intValue() , 1);
        assertEquals( answer2.getComment(birgit).getStatus().intValue() , 1);
        assertEquals( answer3.getComment(birgit).getStatus().intValue() , 1);




        // reset 2 of the 3 questions.
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","resetGuidedLessonResponse");
        mockRequest.addParameter("assignmentResponseId","3");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("firstAnswerToDelete", "2");
        
        mav = specialActionsController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        
        assertEquals ("reviewResponses.smvc?id=1&sortOrder=lastName&ascending=true&viewBy=all&message=Birgit Nilsson's response to this assignment has been reset.", rv.getUrl());


        /*
        confirm answers 2 and 3 have been deleted.
        confirm the comments on all the questions have been deleted
        confirm that answer 1 is still there 
        confirm that comment on answer 1 is still there 
        confirm that comment on answer 1 still has status 1
        */

        // find the answer and verify:
        answer1 = (Answer) vital3DAO.findById(Answer.class, new Long(1));
        answer2 = (Answer) vital3DAO.findById(Answer.class, new Long(2));
        answer3 = (Answer) vital3DAO.findById(Answer.class, new Long(3));
        assertEquals(answer2, null);
        assertEquals(answer3, null);
        assertEquals(answer1.getText(), "1.1");
        assertEquals(1, answer1.getComments().size());
        Comment comment = (Comment) answer1.getComments().iterator().next();
        assertEquals( new Integer(1), comment.getStatus());
        
        
        /*
        * attempt to submit a comment on the assignment in its unsubmitted state:
        */
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","updateComments");
        mockRequest.addParameter("assignmentResponseId","3");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("status","1");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("updateComment3","this+should+break.");
        
        Exception badness = null;
        
        try {
            mav = specialActionsController.handleRequest(mockRequest, mockResponse);
        }
        catch (Exception e) {
            badness = e;
        }
        assertEquals (badness.getMessage(), "Assignment response has not yet been submitted, so not submitting any comments.");


        /*
        * answer all the questions again.
        */
        
        
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("questionId", "2");
        mockRequest.addParameter("text","2.2");
        
        mockRequest.addParameter("action","update");
        mockRequest.addParameter("entity","assignmentResponse");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("id","3");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("status","0");
        
        mav = responseController.handleRequest(mockRequest, mockResponse);
                
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("questionId", "3");
        mockRequest.addParameter("text","3.2");
        
        mockRequest.addParameter("action","update");
        mockRequest.addParameter("entity","assignmentResponse");
        mockRequest.addParameter("type","gl");
        mockRequest.addParameter("id","3");
        mockRequest.addParameter("assignmentId", "1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("status","1");
        
        mav = responseController.handleRequest(mockRequest, mockResponse);
        
        answer1 = (Answer) vital3DAO.findById(Answer.class, new Long(1));
        answer2 = (Answer) vital3DAO.findById(Answer.class, new Long(4));
        answer3 = (Answer) vital3DAO.findById(Answer.class, new Long(5));
        
        assertEquals ("1.1", answer1.getText());
        assertEquals ("2.2", answer2.getText());
        assertEquals ("3.2", answer3.getText());

        
        //========================Unit===========================//
        vital3DAO.resetUCM();

        // insert a new unit
        mockRequest = newMockRequestAEI(session, "POST","insert","unit",null);
        mockRequest.addParameter("worksiteId","2");
        mockRequest.addParameter("title","llaallaa");
		mockRequest.addParameter("startDate","2006/1/19-11:05:00");
		mockRequest.addParameter("endDate","2005/1/19-11:05:00");


        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        bex = (BindException) model.get(BacErrKey);
        FieldError err2 = bex.getFieldError("startDate");
        assertEquals("error.invalid.startDate.outoforder",err2.getCode());



        vital3DAO.resetUCM();

        // attempt to insert a new unit, using missing startDate
        mockRequest = newMockRequestAEI(session, "POST","insert","unit",null);
        mockRequest.addParameter("worksiteId","2");
        mockRequest.addParameter("title","llaallaa");
        mockRequest.addParameter("startDate","");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();

        bex = (BindException) model.get(BacErrKey);
        err2 = bex.getFieldError("startDate");
        assertEquals("error.missing.startDate",err2.getCode());


        //========================CustomField===========================//
        vital3DAO.resetUCM();

        // insert a new customField
        mockRequest = newMockRequestAEI(session, "POST","insert","customField",null);
        mockRequest.addParameter("worksiteId","2");
        mockRequest.addParameter("name","llaallaa");
        mockRequest.addParameter("ordinalValue","7");
        mockRequest.addParameter("visibility","1");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();

        assertEquals("listing.smvc?mode=customField&id=2&message=insert+successful", rv.getUrl());

        vital3DAO.resetUCM();

        // find the new customField and verify:
        results = vital3DAO.findAll(CustomField.class);
        CustomField cf = (CustomField) results.get(results.size()-1);
        assertEquals(new Long(4), cf.getId());
        assertEquals("llaallaa", cf.getName());
        assertEquals(new Integer(7), cf.getOrdinalValue());
        assertEquals(new Integer(1), cf.getVisibility());

        // find the new customFieldValue and verify
        results = vital3DAO.findAll(CustomFieldValue.class);
        cfv = (CustomFieldValue) results.get(results.size()-1);
        assertEquals(new Long(10), cfv.getId());
        assertEquals(new Integer(7), cfv.getOrdinalValue());

        vital3DAO.resetUCM();

        // update a customField
        mockRequest = newMockRequestAEI(session, "POST","update","customField","4");
        mockRequest.addParameter("worksiteId","2");
        mockRequest.addParameter("name","llaa");
        mockRequest.addParameter("ordinalValue","70");
        mockRequest.addParameter("visibility","1");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();

        assertEquals("listing.smvc?mode=customField&id=2&message=update+successful", rv.getUrl());

        // find the cutomField and verify:
        results = vital3DAO.findAll(CustomField.class);
       	cf = (CustomField) results.get(results.size()-1);
        assertEquals(new Long(4), cf.getId());
        assertEquals("llaa", cf.getName());
        assertEquals(new Integer(70), cf.getOrdinalValue());
        assertEquals(new Integer(1), cf.getVisibility());

        // find the customFieldValue and verify
        results = vital3DAO.findAll(CustomFieldValue.class);
        cfv = (CustomFieldValue) results.get(results.size()-1);
        assertEquals(new Long(10), cfv.getId());
        assertEquals(new Integer(70), cfv.getOrdinalValue());


        //========================Question===========================//
        vital3DAO.resetUCM();
        // Add a new Question for Guided Lesson (Assignment type) (questionId = 4)
        mockRequest = newMockRequestAEI(session, "POST", "insert", "question", null);
        mockRequest.addParameter("assignmentId", "3");
        mockRequest.addParameter("text", "what's up?");
        mockRequest.addParameter("ordinalValue", "2");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView)mav.getView();
        assertEquals("listing.smvc?mode=glQuestions&id=3&message=insert+successful", rv.getUrl());
        // Find the question and verify
        Question res = (Question)vital3DAO.findById(Question.class, new Long(4));
        assertEquals(new Long(3), res.getAssignment().getId());
        assertEquals(new Integer(2), res.getOrdinalValue());
        assertEquals("what's up?", res.getText());


        vital3DAO.resetUCM();
        // Request an update/delete form for a specific question (questionId = 2)
        mockRequest = newMockRequestAEI(session, "GET", "display", "question", "2");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertEquals("2", model.get("questionId"));


         vital3DAO.resetUCM();
        // Update a Question for Guided Lesson (questionId = 4)
        mockRequest = newMockRequestAEI(session, "POST", "update", "question", "4");
        // If it's update, we don't need to provide worksiteId and assignmentId
        mockRequest.addParameter("text", "Changed content");
        mockRequest.addParameter("ordinalValue", "3");

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView)mav.getView();
        assertEquals("listing.smvc?mode=glQuestions&id=3&message=update+successful", rv.getUrl());

        // Find the question and verify
        res = (Question)vital3DAO.findById(Question.class, new Long(4));
        assertEquals(new Long(3), res.getAssignment().getId());
        assertEquals(new Integer(3), res.getOrdinalValue());
        assertEquals("Changed content", res.getText());


        vital3DAO.resetUCM();
        // Delete a Question for Guided Lesson (questionId = 4)
        mockRequest = newMockRequestAEI(session, "POST", "delete", "question", "4");
        // If it's delete, we don't need to provide worksiteId and assignmentId

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView)mav.getView();

        assertEquals("listing.smvc?mode=glQuestions&id=3&message=delete+successful", rv.getUrl());



        // Find the left question number and verify
        results = vital3DAO.findAll(Question.class);
        assertEquals(3, results.size());


        vital3DAO.resetUCM();
        // Delete a Question for Guided Lesson (questionId = 2)
        mockRequest = newMockRequestAEI(session, "POST", "delete", "question", "2");
        // If it's delete, we don't need to provide worksiteId and assignmentId

        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView)mav.getView();

        assertEquals("listing.smvc?mode=glQuestions&id=1&message=delete+successful", rv.getUrl());

        // Find the left question number and verify
        results = vital3DAO.findAll(Question.class);
        assertEquals(2, results.size());



        vital3DAO.resetUCM();

        /********* ListingController and AssocController **************/
        ListingController listing = (ListingController) ac.getBean("listingController");

        // request the roster management screen for a worksite:
        mockRequest = newMockRequest(session, "GET", "2");
        mockRequest.addParameter("message", "Hello World");
        mockRequest.addParameter("mode", "roster");

        mav = listing.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        assertEquals("rosterManagement", mav.getViewName());
        worksite = (VitalWorksite) model.get("worksite");
        assertEquals("Advanced Mad Cow Disease", worksite.getTitle());
        List participants = (List) model.get("participants");
        assertEquals(3, participants.size());
        // this p1 is used later!
        p1 = (VitalParticipant) participants.get(0);
        assertEquals("Jim", p1.getFirstName());
        String message = (String) model.get("message");
        assertEquals("Hello World", message);

        vital3DAO.resetUCM();

        // request the assignment-materials assoc screen:
        mockRequest = newMockRequest(session, "GET", "2");
        mockRequest.addParameter("mode", "amAssoc");
        mav = listing.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        //Vital3Utils.debugMap(model);
        assertEquals("assocManagement", mav.getViewName());
        materials = (List) model.get("materials");
        Map wrapper = (Map) materials.get(0);
        material = (Material) wrapper.get("material");
        assertEquals("Material 4 updated", material.getTitle());
        assertTrue(wrapper.get("isAssoc") == null);
        wrapper = (Map) materials.get(1);
        material = (Material) wrapper.get("material");
        assertEquals("title 3", material.getTitle());
        assertTrue(wrapper.get("isAssoc") == null);

        vital3DAO.resetUCM();

        // request that Material 4 be associated with assignment 2:
        AssocController assocController = (AssocController) ac.getBean("assocController");
        mockRequest = newMockRequest(session, "GET", "2");
        mockRequest.addParameter("entity", "assignment");
        mockRequest.addParameter("ids", new String[]{"4"});
        mav = assocController.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        rv = (RedirectView) mav.getView();
        assertEquals("courseHome.smvc?worksiteId=2&message=operation+successful", rv.getUrl());
        results = vital3DAO.findAll(AssignmentMaterialAssoc.class);
        AssignmentMaterialAssoc ama = (AssignmentMaterialAssoc) results.get(0);
        assertEquals(new Long(2), ama.getAssignment().getId());
        assertEquals(new Long(4), ama.getMaterial().getId());

        vital3DAO.resetUCM();

        // re-request the assignment-materials assoc screen:
        mockRequest = newMockRequest(session, "GET", "2");
        mockRequest.addParameter("mode", "amAssoc");
        mav = listing.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        //Vital3Utils.debugMap(model);
        assertEquals("assocManagement", mav.getViewName());
        materials = (List) model.get("materials");
        wrapper = (Map) materials.get(0);
        material = (Material) wrapper.get("material");
        assertEquals("Material 4 updated", material.getTitle());
        assertEquals(Boolean.TRUE, wrapper.get("isAssoc"));
        wrapper = (Map) materials.get(1);
        material = (Material) wrapper.get("material");
        assertEquals("title 3", material.getTitle());
        assertTrue(wrapper.get("isAssoc") == null);

        // should test an add+delete assoc


        vital3DAO.resetUCM();
        // request a question-materials list for a specific assignment
        mockRequest = newMockRequest(session, "GET", "1");
        mockRequest.addParameter("mode", "glQuestions");

        mav = listing.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        assertEquals("questionManagement", mav.getViewName());


        //EDDIE ADDING
        mockRequest = newMockRequestAEI(session, "POST","insert","assignmentResponse",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("assignmentId","5");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("type","discussion");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "{}");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=4", rv.getUrl());
        
        
        //now adding a SECOND response from another participant
        mockRequest = newMockRequestAEI(session, "POST","insert","assignmentResponse",null);
        mockRequest.addParameter("unitId", "1");
        mockRequest.addParameter("assignmentId","5");
        mockRequest.addParameter("participantId", "3");
        mockRequest.addParameter("type","discussion");
        mockRequest.addParameter("status", "0");
        mockRequest.addParameter("text", "{}");

        mav = responseController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
       	assertEquals("response.smvc?action=display&entity=assignmentResponse&id=5", rv.getUrl());
        
        
        
        // find the new assignment and verify (type:Discussion.)
        results = vital3DAO.findAll(AssignmentResponse.class);
        AssignmentResponse ar2 = (AssignmentResponse) results.get(results.size()-1);
        assertEquals(new Long(5), ar2.getId());
        assertEquals(new Long(1), ar2.getAssignment().getUnit().getId());
        assertEquals(new Long(2), ar2.getAssignment().getUnit().getWorksite().getId());
        //logger.debug(ar2.getDiscussionEntries());
        
        assertEquals("discussion", ar2.getType());
        assertEquals(new Integer(0), ar2.getStatus());
        
        
        vital3DAO.resetUCM();


        
        // deleting one participant at a time using the regular format
        
        mockRequest = newMockRequestAEI(session, "POST","delete","participant",null);
        mockRequest.addParameter("id", "3");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        // make sure redirect worked:
        rv = (RedirectView) mav.getView();
        assertEquals("listing.smvc?mode=roster&id=1&message=delete+successful", rv.getUrl());
        // check number of participants to verify:
        results = vital3DAO.findAll(VitalParticipant.class);
        assertEquals(3, results.size());
        vital3DAO.resetUCM();
        


        /************* ANNOTATIONS ******************/

        vital3DAO.resetUCM();

        // insert a new material for worksite 2:
        Date modDate = textFormatter.parse("2005/9/29-5:01:02");
        VitalWorksite w2 = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, new Long(2));
        Material m8 = new Material(w2, 0, modDate, "annotate me", null, "notes material", "video", null);
        vital3DAO.save(Material.class, m8);

        // uncomment these if we change the variables before here
        VitalParticipant p4 = (VitalParticipant) vital3DAO.findById(VitalParticipant.class, new Long(4));
        //Material m4 = (Material) vital3DAO.findById(Material.class, new Long(4));

        // Insert Annotations:
        Date modDate1 = textFormatter.parse("2005/10/29-5:01:02");
        Date modDate2 = textFormatter.parse("2006/10/30-5:01:02");

        Annotation note1 = new Annotation(m8, p4, "00:11:22", "11:22:33", modDate1, "this is a great material", "compliment", "clip");
        Annotation note2 = new Annotation(m8, p4, "01:12:23", "12:23:34", modDate2, "this is a stupid material", "insult", "clip");
        //Annotation note3 = new Annotation(m4, p4, "00:11:22", "16:22:33", modDate3, "this is a red material", "observation", "clip");
        vital3DAO.saveCollection(Annotation.class, Arrays.asList(new Annotation[]{note1,note2}));

        vital3DAO.resetUCM();

        // simulate ajax call to submitNote to insert a new annotation:
        AnnotationController noteControl = (AnnotationController) ac.getBean("annotationController");
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("action", "submitNote");
        mockRequest.addParameter("materialId", "4");
        mockRequest.addParameter("clipBegin", "00:11:22");
        mockRequest.addParameter("clipEnd", "16:22:33");
        mockRequest.addParameter("dateModified", "2006/10/30-6:01:02");
        mockRequest.addParameter("text", "this is a red material");
        mockRequest.addParameter("title", "observation");
        mockRequest.addParameter("type", "clip");
        mockRequest.addParameter("stickytags", "   tag1   ");
        mockRequest.addParameter("stickytags", "tag2");
        mockRequest.addParameter("stickytags", "    ");
        mockRequest.addParameter("stickytags", "tag3");
        mav = noteControl.handleRequest(mockRequest, (HttpServletResponse) null);
        assertEquals("ajaxResponse", mav.getViewName());
        assertEquals("{id:\"3\"}", (String) mav.getModel().get("body"));
        // find the new annotation in the db:
        Annotation note = (Annotation) vital3DAO.findById(Annotation.class, new Long(3));
        assertEquals("observation", note.getTitle());
        Date modDate3 = textFormatter.parse("2006/10/30-6:01:02");
        assertEquals(modDate3, note.getDateModified());
        
        vital3DAO.resetUCM();

        // manage my notes call:
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("worksiteId", "2");

        mav = noteControl.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify the results:
        model = mav.getModel();
        //Vital3Utils.debugMap(model);
        List groupList = (List) model.get("groupList");
        Map groupMap = (Map) groupList.get(0);
        assertEquals("Material 4 updated", groupMap.get("groupTitle"));
        assertEquals("mId-4", groupMap.get("groupId"));
        Set notes = (Set) groupMap.get("notes");
        Iterator iter = notes.iterator();
        assertEquals(new Long(3), ((Annotation)iter.next()).getId());
        groupMap = (Map) groupList.get(1);
        assertEquals("notes material", groupMap.get("groupTitle"));
        assertEquals("mId-8", groupMap.get("groupId"));
        notes = (Set) groupMap.get("notes");
        iter = notes.iterator();
        
        // Natural order of annotations is reverse creation date (most recent first.)
        assertEquals(new Long(2), ((Annotation)iter.next()).getId());
        assertEquals(new Long(1), ((Annotation)iter.next()).getId());
        vital3DAO.resetUCM();

        vital3DAO.resetUCM();

        // simulate noteQuery AJAX call:
        noteControl = (AnnotationController) ac.getBean("annotationController");
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("action", "noteQuery");
        mockRequest.addParameter("worksiteId", "2");
        mockRequest.addParameter("template", "myNotes");
        mockRequest.addParameter("limitBy", "");
        mockRequest.addParameter("groupBy", "modificationDate");
        mockRequest.addParameter("recent", "");

        mockResponse = new MockHttpServletResponse();
        mav = noteControl.handleRequest(mockRequest, mockResponse);
        // verify the results:
        //assertEquals("text/xml", mockResponse.getContentType());
        model = mav.getModel();
        //Vital3Utils.debugMap(model);
        groupList = (List) model.get("groupList");
        groupMap = (Map) groupList.get(0);
        assertEquals("Oct. 29, 2005", groupMap.get("groupTitle"));
        assertEquals("modDate-Oct292005", groupMap.get("groupId"));
        notes = (Set) groupMap.get("notes");
        iter = notes.iterator();
        assertEquals(new Long(1), ((Annotation)iter.next()).getId());
        groupMap = (Map) groupList.get(1);
        assertEquals("Oct. 30, 2006", groupMap.get("groupTitle"));
        assertEquals("modDate-Oct302006", groupMap.get("groupId"));
        notes = (Set) groupMap.get("notes");
        iter = notes.iterator();

        note = (Annotation)iter.next();
        assertEquals(new Long(3), note.getId());
        
        Map tagMap = (Map) model.get("tagMap");
        assertEquals("tag1, tag2, tag3", note.getTagsAsString(tagMap));
        
        note = (Annotation)iter.next();
        
        assertEquals(new Long(2), note.getId());
  
        vital3DAO.resetUCM();

        // simulate ajax call to deleteNote to delete an annotation:
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("action", "deleteNote");
        mockRequest.addParameter("id", "3");
        mav = noteControl.handleRequest(mockRequest, (HttpServletResponse) null);
        assertEquals("ajaxResponse", mav.getViewName());
        // the annotation should no longer be in the db:
        note = (Annotation) vital3DAO.findById(Annotation.class, new Long(3));
        assertTrue(note == null);


        /************* ReviewResponsesController ******************/
        vital3DAO.resetUCM();

        ReviewResponsesController reviewControl = (ReviewResponsesController) ac.getBean("reviewResponsesController");

        // assignmentId=3 only has one assignmentResponseId=1 submitted
        mockRequest = newMockRequest(session, "GET", "3");
        mockRequest.addParameter("sortOrder", "lastName" );
        mockRequest.addParameter("ascending", "false");
        mockRequest.addParameter("viewBy", "all");

        mav = reviewControl.handleRequest(mockRequest, mockResponse);
        assertEquals("reviewResponses", mav.getViewName());

        model = mav.getModel();
        Assignment assignment = (Assignment)model.get("assignment");
        assertEquals(new Long(3), assignment.getId());

        assertEquals("lastName", model.get("sortOrder"));

        VitalParticipant participant = (VitalParticipant)model.get("participant");

        // Only submitted assignmentResponse will be shown
        List sortedResponses = (List)model.get("sortedResponses");
        assertEquals(1, sortedResponses.size());


        /************* ReviewAllResponsesController******************/
        vital3DAO.resetUCM();
        ReviewAllResponsesController reviewAllControl = (ReviewAllResponsesController) ac.getBean("reviewAllResponsesController");

        // assignmentId = 1, type = gl, 1 assignmentResponse submitted
        mockRequest = newMockRequest(session, "GET", "1");
        
        mav = reviewAllControl.handleRequest(mockRequest, mockResponse);
        assertEquals("reviewAllResponses", mav.getViewName());
        
        model = mav.getModel();
        assignment = (Assignment)model.get("assignment");
        assertEquals(new Long(1), assignment.getId());
        
        List allResponses = (List)model.get("responses");
        
        assertEquals(1, allResponses.size());
        

        /*************** SEARCHING TESTS ****************/
        /*vital3DAO.resetUCM();

        SearchController search = (SearchController) ac.getBean("searchController");
        
        newMockRequest(session, "GET", null);
        
        mockRequest.addParameter("unitId","1");
        mockRequest.addParameter("worksiteId","2");
        mav = search.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
//        rv = (RedirectView) mav.getView();
//        assertEquals("login.smvc?message=You+must+be+logged+in+to+access+this+area", rv.getUrl());

*/



        /*************** SECURITY TESTS ****************/

        // log out
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("logout","true");
        mav = login.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify redirect
        rv = (RedirectView) mav.getView();
        assertEquals("/login.smvc?message=" + URLEncoder.encode("Successfully logged out", "UTF-8"), rv.getUrl());

        vital3DAO.resetUCM();

        // try to access secure area while logged out
        // request a 'new' assignment form:
        mockRequest = newMockRequestAEI(session, "GET","new","assignment",null);
        mockRequest.addParameter("unitId","1");
        mockRequest.setServletPath("/basicAdmin.smvc");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();
        rv = (RedirectView) mav.getView();
        assertEquals("login.smvc?message=You+must+be+logged+in+to+access+this+area", rv.getUrl());

        // log in as Eric (a student)
        mockRequest = newMockRequest(session, "GET", null);
        mockRequest.addParameter("authMethod","vital");
        mockRequest.addParameter("username", "em2140");
        mockRequest.addParameter("password", "password");
        mav = login.handleRequest(mockRequest, (HttpServletResponse) null);
        // verify results. This should redirect to the url we were trying to get to before:
        rv = (RedirectView) mav.getView();
        assertEquals("basicAdmin.smvc?action=new&entity=assignment&unitId=1", rv.getUrl());
        loggedInUser = ucm.getCLIU(session, false);
        assertEquals (loggedInUser.getFirstName(), "Eric");

        // try to access an admin area:
        // insert a participant:
        mockRequest = newMockRequestAEI(session, "POST","insert","participant",null);
        mockRequest.addParameter("accessLevel", UserCourseManager.STUDENT_ACCESS.toString());
        mockRequest.addParameter("userId", "1");
        mockRequest.addParameter("worksiteId", "2");
        mav = basicAdmin.handleRequest(mockRequest, mockResponse);
        model = mav.getModel();


        rv = (RedirectView) mav.getView();
        assertEquals("error.smvc?message=You+are+not+authorized+to+access+this+area", rv.getUrl());
        


        //========================JTasty Tests===========================//
        // dao is called tdao
        // tastyBean is called tb
        // tastyClient is called tc
        
        
        ccnmtl.jtasty.Service service = new ccnmtl.jtasty.Service ("testservice");
        ccnmtl.jtasty.User jtasty_user = new ccnmtl.jtasty.User("testuser", service);
        ccnmtl.jtasty.Item jtasty_item = new ccnmtl.jtasty.Item("testitem", service);
        ccnmtl.jtasty.Tag jtasty_tag =  new ccnmtl.jtasty.Tag("testtag", service);
        
        
        Map users = new HashMap();
        users.put ( "anders",           new ccnmtl.jtasty.User( "anders", service));
        users.put ( "jonah",            new ccnmtl.jtasty.User( "jonah", service));
        users.put ( "eric",             new ccnmtl.jtasty.User( "eric", service));
        users.put ( "sky",              new ccnmtl.jtasty.User( "sky", service));
        users.put ( "maurice",          new ccnmtl.jtasty.User( "maurice", service));
        users.put ( "marc",             new ccnmtl.jtasty.User( "marc", service));
        
        Map items = new HashMap();
        items.put ( "fernandez fierro", new ccnmtl.jtasty.Item( "fernandez fierro", service));
        items.put ( "in flames",        new ccnmtl.jtasty.Item( "in flames", service));
        items.put ( "mastodon",         new ccnmtl.jtasty.Item( "mastodon", service));
        items.put ( "covenant",         new ccnmtl.jtasty.Item( "covenant", service));
        
        Map tags = new HashMap();
        tags.put ( "tango",             new ccnmtl.jtasty.Tag( "tango", service));
        tags.put ( "metal",             new ccnmtl.jtasty.Tag( "metal", service));
        tags.put ( "swedish",           new ccnmtl.jtasty.Tag( "swedish", service));
        tags.put ( "argentinian",       new ccnmtl.jtasty.Tag( "argentinian", service));
        tags.put ( "industrial",        new ccnmtl.jtasty.Tag( "industrial", service));
        
        ///////////
        // dao works
    
        tdao.save(User.class, jtasty_user);
        Long id = jtasty_user.getId();
        ccnmtl.jtasty.User newUser = (ccnmtl.jtasty.User) tdao.findById(User.class, id);
        assert (newUser != null);
        tdao.delete(User.class, jtasty_user);
        jtasty_user = newUser;
        
        ///////////
        // basic sanity
        assertEquals(service.getName(), "testservice");
        assertEquals(jtasty_user.getName(), "testuser");
        assertEquals(jtasty_item.getName(), "testitem");
        assertEquals(jtasty_tag.getName(), "testtag");

        ///////////
        // unicode testing
        Service service1 = new Service ("\u738b\u83f2");
        assertEquals (service1.getName(), "\u738b\u83f2");
        User u1 = new User("\u738b\u83f2", service1);
        assertEquals (u1.getName(), "\u738b\u83f2");
        Item i1 = new Item("\u738b\u83f2", service1);
        assertEquals (i1.getName(), "\u738b\u83f2");
        Tag t1 = new Tag("\u738b\u83f2", service1);
        assertEquals (u1.getName(), "\u738b\u83f2");
        
        ///////////
        // setup
        
        Map u = users;
        Map i = items;
        Map t = tags;
        Set anders = new HashSet(); anders.add((User) u.get("anders"));
        Set fernandez = new HashSet(); fernandez.add((Item) i.get("fernandez fierro"));
        Set tango = new HashSet(); tango.add((Tag) t.get("tango"));
        
        assertEquals(((User) u.get("anders")).getClass(), User.class);
        assertEquals(((Item) i.get("fernandez fierro")).getClass(), Item.class);
        assertEquals(((Tag)  t.get("tango")).getClass(), Tag.class);
        
        ///////////
        // tastyBean tests (could add more here for, e.g., delete.)
        
		tb.buildAllRelationships( anders, fernandez, tango );
        
        Collection andersItems = getItems ((User) u.get("anders"), tb);
        Collection fernandezUsers = getUsers ((Item) items.get("fernandez fierro"), tb);
        Collection fernandezTags = getTags ((Item) items.get("fernandez fierro") , tb);
        Collection tangoUsers = getUsers  ((Tag) t.get("tango"), tb);
        Collection tangoItems = getItems  ((Tag) t.get("tango"), tb);

        assertTrue (andersItems.contains((Item) i.get("fernandez fierro")));
        assertTrue (fernandezUsers.contains((User) u.get("anders")));
        assertTrue (fernandezTags.contains((Tag) t.get("tango")));
        assertTrue (tangoUsers.contains((User) u.get("anders")));
        assertTrue (tangoItems.contains((Item) i.get("fernandez fierro")));
        
        ///////////
        // request tests
        
        JSONObject j;
        String s;
        
        //logger.debug(((User) (tdao.findAll(User.class).iterator().next())).getName());
        //logger.debug(((Item) (tdao.findAll(Item.class).iterator().next())).getName());
        //logger.debug(((Tag) (tdao.findAll(Tag.class).iterator().next())).getName());
        
        //logger.debug("Now starting testing of actual controllers:");
        
        
         // logger.debug("*** Remove previous test data");
        s = tb.tastyDelete("/service/testservice/user/anders/item/fernandez fierro/tag/tango");
        assertEquals( "ok", s);
        
         // logger.debug("*** create a service named testservice");
        s = tb.tastyPost("/service/testservice");
        assertEquals( "ok", s);
        
         // logger.debug("*** 'eddie' tags 'pie' with tag 'yum'.");
        s = tb.tastyPost("/service/testservice/user/eddie/item/pie/tag/yum");
        assertEquals( "ok", s);
        
         // logger.debug("*** Getting dimensions:");
        User eddie = (User) tdao.findByPropertyValue(User.class, "name", "eddie").get(0); 
        Item pie = (Item) tdao.findByPropertyValue(Item.class, "name", "pie").get(0); 
        Tag yum = (Tag) tdao.findByPropertyValue(Tag.class, "name", "yum").get(0); 
        
         // logger.debug("*** Check that ties get created.");
        assertEquals (1, tdao.findByTwoPropertyValues(UserItem.class, "user", eddie, "item", pie ).size());
        assertEquals (1, tdao.findByTwoPropertyValues(ItemTag.class,  "item", pie,   "tag",  yum ).size());
        assertEquals (1, tdao.findByTwoPropertyValues(UserTag.class,  "user", eddie, "tag",  yum ).size());
        
         //  // logger.debug("*** Check that the UIT gets created.");
        assertEquals (1, tdao.findByThreePropertyValues(UserItemTag.class, "user", eddie, "item", pie, "tag", yum).size());
        
         // logger.debug("*** GET the items of tag 'yum' for user 'eddie'");
        j = new JSONObject (tb.tastyGet("/service/testservice/user/eddie/tag/yum"));
        assertEquals ("pie", j.getJSONArray("items").getJSONObject(0).getString("item"));
        
         // logger.debug("*** GET the tags of item 'pie' for user 'eddie'");
        j = new JSONObject (tb.tastyGet("/service/testservice/user/eddie/item/pie"));
        assertEquals ("yum", j.getJSONArray("tags").getJSONObject(0).getString("tag"));
        
        
         // logger.debug("*** GET the items, tags, and item_tags of 'eddie'");
        j = new JSONObject (tb.tastyGet("/service/testservice/user/eddie"));
        assertEquals ("pie", j.getJSONArray("items").getJSONObject(0).getString("item"));
        assertEquals ("yum", j.getJSONArray("tags").getJSONObject(0).getString("tag"));
        assertEquals ("yum", j.getJSONArray("tag_items").getJSONArray(0).getJSONObject(0).getString("tag"));
        
         // logger.debug("*** Getting service:");
        Service service2 = (Service) tdao.findByPropertyValue(Service.class, "name", "testservice").get(0); 
        //logger.debug (service);
        
         // logger.debug("*** 'eddie' tags 'cake' with tag 'gross'.");
        s = tb.tastyPost("/service/testservice/user/eddie/item/cake/tag/gross");
        assertEquals( "ok", s);
        
         // logger.debug("*** Getting dimensions:");
        eddie = (User) tdao.findByPropertyValue(User.class, "name", "eddie").get(0); 
        pie = (Item) tdao.findByPropertyValue(Item.class, "name", "pie").get(0); 
        yum = (Tag) tdao.findByPropertyValue(Tag.class, "name", "yum").get(0);
        Item cake = (Item) tdao.findByPropertyValue(Item.class, "name", "cake").get(0); 
        Tag gross = (Tag) tdao.findByPropertyValue(Tag.class, "name", "gross").get(0); 
        
         // logger.debug("*** Checking they're all still there:");
        assertEquals ("eddie", eddie.getName());
        assertEquals ("cake", cake.getName());
        assertEquals ("pie", pie.getName());
        assertEquals ("gross", gross.getName());
        assertEquals ("yum", yum.getName());
        
         // logger.debug("*** Getting all uits:");
        
         // logger.debug("*** Eddie now has two UITs.");
        List mylist = tdao.findByPropertyValue(UserItemTag.class, "user", eddie); 
        assertEquals(2, mylist.size()); 
        
         // logger.debug("*** One of these UITs points to pie....");
        mylist = tdao.findByTwoPropertyValues(UserItemTag.class, "user", eddie, "item", pie ); 
        assertEquals(1, mylist.size()); 

         // logger.debug("*** One of these UITs points to cake.");
        mylist = tdao.findByTwoPropertyValues(UserItemTag.class, "user", eddie, "item", cake); 
        assertEquals(1, mylist.size()); 
        
         // logger.debug("*** eddie has two UIT's, so he should not be deleted when one of his tags is removed.");
        assertTrue (!(tb.isOrphan (eddie)));
        
         // logger.debug("*** 'eddie' removes tag 'yum' from item 'pie'");
        s = tb.tastyDelete("/service/testservice/user/eddie/item/pie/tag/yum");
        assertEquals( "ok", s);
        
         // logger.debug("*** Making sure everything was deleted correctly.");
        
         // logger.debug("*** Eddie and Cake should still be linked by the gross item:");
        assertEquals (1, tdao.findByTwoPropertyValues(UserItemTag.class, "user", eddie, "item", cake).size());
        
         // logger.debug("*** The UIT linking eddie-pie-yum also gets deleted.");
        assertEquals (0, tdao.findByTwoPropertyValues(UserItemTag.class, "user", eddie, "item", pie ).size());
        
         // logger.debug("*** All ties linking eddie-pie-yum also get deleted.");
        assertEquals (0, tdao.findByTwoPropertyValues(UserItem.class, "user", eddie, "item", pie ).size());
        assertEquals (0, tdao.findByTwoPropertyValues(ItemTag.class,  "item", pie,   "tag",  yum ).size());
        assertEquals (0, tdao.findByTwoPropertyValues(UserTag.class,  "user", eddie, "tag",  yum ).size());
        
         // logger.debug("*** Eddie only should have one UIT left :");
        assertEquals (1, tdao.findByPropertyValue(UserItemTag.class, "user", eddie).size());
         // logger.debug("*** Cake was not orphaned and is still around:");
        assertEquals (1, tdao.findByPropertyValue(Item.class, "name", "cake").size());
         // logger.debug("*** Eddie was not orphaned and is still around:");
        assertEquals (1, tdao.findByPropertyValue(User.class, "name", "eddie").size());
         // logger.debug("*** Since Pie has no more tags associated with it, it becomes an orphan and gets deleted:");
        assertEquals (0, tdao.findByPropertyValue(Item.class, "name", "pie").size());
        
         // logger.debug("*** 'eddie' removes tag 'gross' from item 'cake'");
        s = tb.tastyDelete("/service/testservice/user/eddie/item/cake/tag/gross");
        assertEquals( "ok", s);
        
         // logger.debug("*** All dimensions are now zero:....");
        assertEquals (0,  tdao.findAll(User.class).size());
        assertEquals (0,  tdao.findAll(Item.class).size());
        assertEquals (0,  tdao.findAll(Tag.class).size());
        assertEquals (0,  tdao.findAll(UserItemTag.class).size());
        assertEquals (0,  tdao.findAll(UserItem.class).size());
        assertEquals (0,  tdao.findAll(ItemTag.class).size());
        assertEquals (0,  tdao.findAll(UserTag.class).size());
        DiscussionEntryController discussionEntryController = (DiscussionEntryController) ac.getBean("discussionEntryController");
        
        
        // Add a disucssion entry to this assignment:
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","addDiscussionEntry");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("assignmentId", "5");
        mockRequest.addParameter("assignmentResponseId","4");
        mockRequest.addParameter("text","hello");
        mav = discussionEntryController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        results = vital3DAO.findAll(Comment.class);
        assertEquals (2,  vital3DAO.findAll(Comment.class).size());
        Comment entry = (Comment) vital3DAO.findById(Comment.class, new Long(4));
        assertTrue(entry != null);
        assertEquals ("hello",  entry.getText());
        
        ar  = (AssignmentResponse) vital3DAO.findById(AssignmentResponse.class, new Long(4));
        
        //Add another discussion entry to this assignment-- same participant
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","addDiscussionEntry");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("participantId", "4");
        mockRequest.addParameter("assignmentId", "5");
        mockRequest.addParameter("assignmentResponseId","4");
        mockRequest.addParameter("text", "hello again");
        mav = discussionEntryController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        results = vital3DAO.findAll(Comment.class);
        assertEquals (3,  vital3DAO.findAll(Comment.class).size());
        Comment entry2 = (Comment) vital3DAO.findById(Comment.class, new Long(5));
        assertTrue(entry2 != null);
        assertEquals ("hello again",  entry2.getText());
        
        
        //Add another discussion entry to this assignment-- Birgit
        mockRequest = newMockRequest(session, "POST", null);
        mockRequest.addParameter("action","addDiscussionEntry");
        mockRequest.addParameter("worksiteId","1");
        mockRequest.addParameter("participantId", "5");
        mockRequest.addParameter("assignmentId", "5");
        //mockRequest.addParameter("assignmentResponseId","4");
        mockRequest.addParameter("text", "This is someone else.");
        mav = discussionEntryController.handleRequest(mockRequest, mockResponse);
        rv = (RedirectView) mav.getView();
        results = vital3DAO.findAll(Comment.class);
        assertEquals (4,  results.size());
        
        Comment entry6 = (Comment) vital3DAO.findById(Comment.class, new Long(6));
        assertTrue(entry6 != null);
        assertEquals ("This is someone else.", entry6.getText());
        
        Iterator commentIter = results.iterator();
        //StringBuffer buf = new StringBuffer();
        while (commentIter.hasNext()) {
            Comment c = (Comment) commentIter.next();
            //logger.debug (c.getId());
            //logger.debug (c.getText());
            
        }
        

        // Find out whether they posted:
        //logger.debug ("%%%%%%%%%%%%%");
        //logger.debug (ar.getDiscussionEntries());
        //logger.debug (ar.getAssignment().getDiscussionEntries());
        assertEquals (3, ar.getAssignment().getDiscussionEntries().size());
        
        Assignment discussionAssignment = (Assignment) vital3DAO.findById(Assignment.class, new Long(5));
        // logger.debug (discussionAssignment.getDiscussionEntries());
        
        Object[] newset = new TreeSet (discussionAssignment.getDiscussionEntries()).toArray();
        assertEquals (((Comment) newset[0]).getText(),"This is someone else.");
        assertEquals (((Comment) newset[1]).getText(),"hello");
        assertEquals (((Comment) newset[2]).getText(),"hello again");
        
        
        
        Set responses = discussionAssignment.getResponses();
        
        ///////////////////////
        ///////////////////////
        ///////////////////////
        
/*

assertEquals("reviewGuidedLesson.smvc?id=3", rv.getUrl());

VitalParticipant birgit = (VitalParticipant) vital3DAO.findById(VitalParticipant.class, new Long(4));
Answer answer1 = (Answer) vital3DAO.findById(Answer.class, new Long(1));
Answer answer2 = (Answer) vital3DAO.findById(Answer.class, new Long(2));
Answer answer3 = (Answer) vital3DAO.findById(Answer.class, new Long(3));


assertEquals( answer1.getComment(birgit).getText() , "Comment 1.1" );
assertEquals( answer2.getComment(birgit).getText() , "" );
assertEquals( answer3.getComment(birgit).getText() , "Comment 3.1" );

assertEquals( answer1.getComment(birgit).getStatus().intValue() , 0);
assertEquals( answer2.getComment(birgit).getStatus().intValue() , 0);
assertEquals( answer3.getComment(birgit).getStatus().intValue() , 0);


// now let's submit the comments

mockRequest = newMockRequest(session, "POST", null);
mockRequest.addParameter("action","updateComments");
mockRequest.addParameter("status","1");
mockRequest.addParameter("worksiteId","1");
mockRequest.addParameter("assignmentResponseId","3");
mockRequest.addParameter("participantId", "4");
mockRequest.addParameter("assignmentId", "1");
mockRequest.addParameter("updateComment1", "Comment 1.2");
mockRequest.addParameter("updateComment2", "Comment 2.2");
mockRequest.addParameter("updateComment3", "Comment 3.2");

*/


        //========================End JTasty Tests===========================//

        /////////////////// TEST VARIOUS CLASS METHODS ///////////////////////

        // test getNumCustomFields:
        VitalWorksite c2 = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, new Long(2));
        assertEquals(3, c2.getNumCustomFields());
        Set customFieldSet = c2.getCustomFields();
        assertEquals("c2-cf1", ((CustomField) customFieldSet.iterator().next()).getName());

        // test the onBind code:
        mockRequest = newMockRequestAEI(session, "POST","update","material",null);
        mockRequest.addParameter("child0-1-test","01test");
        mockRequest.addParameter("child0-1-id","01id");
        mockRequest.addParameter("child0-2-test","02test");
        mockRequest.addParameter("child0-2-id","02id");
        command = new BasicAdminCommand();
        BasicAdminController bac = new BasicAdminController();
        bac.onBindTest(mockRequest, command, (BindException) null);
        // examine the command object to see if it worked:
        List child0 = command.getChildData(0);
        Map inst = (Map) child0.get(0);
        assertEquals("01id", inst.get("id"));
        assertEquals("01test", inst.get("test"));
        inst = (Map) child0.get(1);
        assertEquals("02id", inst.get("id"));
        assertEquals("02test", inst.get("test"));


        // test course-worksite affiliations:
        w1 = (VitalWorksite) vital3DAO.findById(VitalWorksite.class, new Long(1));
        ucm.decorateWorksite(w1, true, false);
        // make sure there are no affils yet:
        assertEquals(null, w1.getCourseIdString());
        // affiliate a worksite with a nonexistent course:
        ucm.affiliateWorksiteWithCourse(w1,"eric_test_course",true);
        // confirm:
        assertEquals("eric_test_course", w1.getCourseIdString());
        // remove affiliation:
        ucm.removeCourseAffilsForWorksite(w1);
        // confirm:
        assertEquals(null, w1.getCourseIdString());

        // test course id string formatting and parsing:
        String internalString = "t3.y2006.s003.cv1201.ital.st.course:columbia.edu";
        String displayString = ucm.formatCourseIdStringForDisplay(internalString);
        assertEquals("20063ITAL1201V003", displayString);
        internalString = ucm.parseCourseIdString(displayString);
        assertEquals("t3.y2006.s003.cv1201.ital.st.course:columbia.edu", internalString);

        // test apostrophe escaping (note that java strings require escaping, so "\\" = "\"):
        String aposTest = "Eric's test";
        String aposResult = "Eric\\'s test";
        //System.out.println("aposResult: " + aposResult);
        assertEquals(aposResult, textFormatter.escapeForJavascript(aposTest));

    }

    
    private Set getUsers( Dimension d, TastyBean tb) {
        return tb.getKids (d.getClass(), d, User.class);
    }
    
    private Set getItems( Dimension d, TastyBean tb) {
        return tb.getKids (d.getClass(), d, Item.class);
    }
    
    private Set getTags( Dimension d, TastyBean tb) {
        return tb.getKids (d.getClass(), d, Tag.class);
    }
    


    // Only use this for models resulting from a GET request or a failed POST. Redirect views will not contain error key.
    // Also, so far this only works for basicAdminCommand or ResponseCommand
    private void assertNoErrors(ModelAndView mav, String errorKey) {
        Map model = mav.getModel();
        // the model should contain an error key... if not, then it's probably a redirectview
        if (model.containsKey(errorKey)) {
            BindException bex = (BindException) model.get(errorKey);
            if (bex.hasErrors()) {
                System.out.println(bex.getMessage());
                assertTrue("Model contained errors", !bex.hasErrors());
            }
        } else {
            System.out.println("Model did not contain error key");
            RedirectView rv = (RedirectView) mav.getView();
            System.out.println("View name: " + rv.getUrl());
            fail("see System.out for message");
        }
    }

    private MockHttpServletRequest newMockRequestAEI(HttpSession session, String method, String action, String entity, String id) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod(method);
		mockRequest.setSession(session);
        mockRequest.addParameter("action",action);
        mockRequest.addParameter("entity",entity);
        if (id != null) mockRequest.addParameter("id",id);
        return mockRequest;
    }

    private MockHttpServletRequest newMockRequest(HttpSession session, String method, String id) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod(method);
		mockRequest.setSession(session);
        if (id != null) mockRequest.addParameter("id",id);
        return mockRequest;
    }
    

}
