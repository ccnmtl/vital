package ccnmtl.utils.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import junit.framework.Test;
import junit.framework.TestSuite;
import ccnmtl.utils.test.AbstractTestCase;
import ccnmtl.utils.TastyClient;
import ccnmtl.jtasty.*;
import ccnmtl.jtasty.dao.JTastyDAO;
import ccnmtl.jtasty.test.*;

// src/test/ccnmtl/jtasty/test/JTastyMockDAO.java

import org.apache.commons.logging.*;

/**
 * It's too difficult to make this a unit test, since the TastyClient is purely an integration element. So this is basically an integration test.
 * It's dependent on the Tasty server being available, and uses the service "javaTastyTest-(randomnumber)". Since other people may be testing simulataneously,
 * the random number ensures that people do not interfere with eachother.
 */
public class TastyClientTest extends AbstractTestCase {


    protected final Log logger = LogFactory.getLog(getClass());

    private TastyClient tc;
    //    private String tastyServerUrl="http://kang.ccnmtl.columbia.edu:4090/eddie/jtasty/rest";
    private String tastyServerUrl="";
    private String serviceName;
    private String unique;
    
    /**
     * Constructor
     * @param testName name of the test case
     */
    public TastyClientTest (String testName){
        super(testName);
    }
    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(TastyClientTest.class);
    }
    
    public void setUp() throws Exception {
        
        String time = String.valueOf((new Date()).getTime()).substring(8);
        String random = String.valueOf(Math.random()).substring(2);
        unique = (time+random);
        serviceName = "javaTastyTest-" + unique;

        JTastyDAO dao = new JTastyMockDAO();
        
        TastyBean tb = new TastyBean();
        tb.setDao(dao);
        tc = new TastyClient(tastyServerUrl, serviceName);
        tc.setTastyBean (tb);
        
        logger.debug (tastyServerUrl + "/service/" + serviceName);
        // create the service:
        tc.post(tastyServerUrl + "/service/" + serviceName);
        // add the user:
        
        tc.beanstatus();
    }
    
    
    
    public void testTasty() throws Exception {
        
        try {
            System.out.println(tc.getRawOutput());
            
        } catch (UnknownHostException e) {
            fail("Either you didn't add tasty to your /etc/hosts file or tasty is down! See docs for info");
        }
        
        // set up some sets of tags to be reused:
        HashSet ac = new HashSet(Arrays.asList(new String[]{"a","c"}));
        HashSet cd = new HashSet(Arrays.asList(new String[]{"c","d"}));
        HashSet abc = new HashSet(Arrays.asList(new String[]{"a","b","c"}));
        HashSet bcd = new HashSet(Arrays.asList(new String[]{"b","c","d"}));
        HashSet abcd = new HashSet(Arrays.asList(new String[]{"a","b","c","d"}));
        
        // should come back empty:
        Set allUserTags = tc.getAllTagsForUser("testUser");
        assertEquals(0, allUserTags.size());
        
        
        // add some tags (and items):
        tc.addTagsToItem("testUser", "testItem", abc);
        tc.addTagsToItem("testUser", "testItem2", cd);
        
        
        // should come back with 4 tags:
        Set tags = tc.getAllTagsForUser("testUser");
        assertEquals(abcd, tags);
        
        // should return 3 tags:
        tags = tc.getTagsForItem("testUser", "testItem");
        assertEquals(abc, tags);
        
        // should return 2 tags:
        tags = tc.getTagsForItem("testUser", "testItem2");
        assertEquals(cd, tags);
        
        // get all items and their tags:
        Map taggedItems = tc.getAllItemsAndTagsForUser("testUser");
        tags = (Set) taggedItems.get("testItem");
        assertEquals(abc, tags);
        tags = (Set) taggedItems.get("testItem2");
        assertEquals(cd, tags);
        
        
        // delete all tags:
        tc.deleteTagsFromItem("testUser", "testItem", abc);
        tc.deleteTagsFromItem("testUser", "testItem2", cd);
        
        
       // verify that the tag pool is empty:
        tags = tc.getAllTagsForUser("testUser");
        assertEquals(0, tags.size());
        
        // verify that the items are tagless:
        tags = tc.getTagsForItem("testUser", "testItem");
        assertEquals(0, tags.size());
        tags = tc.getTagsForItem("testUser", "testItem2");
        assertEquals(0, tags.size());
        
        // try syncing (add-only):
        tc.setTagsForItem("testUser", "testItem", abc);
        // verify:
        tags = tc.getTagsForItem("testUser", "testItem");
        assertEquals(abc, tags);
        
        
        // another sync (delete-only):
        tc.setTagsForItem("testUser", "testItem", ac);
        // verify:
        tags = tc.getTagsForItem("testUser", "testItem");
        assertEquals(ac, tags);
        
        // one more sync (add and delete):
        tc.setTagsForItem("testUser", "testItem", bcd);
        // verify:
        tags = tc.getTagsForItem("testUser", "testItem");
        assertEquals(bcd, tags);
        
        
    }
    
    public void tearDown() throws Exception {
        
        // delete the service:
        tc.delete(tastyServerUrl + "/service/" + serviceName);
        
    }
    
    
    
    
}

