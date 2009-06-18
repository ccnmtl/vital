package ccnmtl.utils.test;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import ccnmtl.utils.URLString;
import ccnmtl.utils.test.AbstractTestCase;

public class URLStringTest extends AbstractTestCase {
    
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public URLStringTest (String testName){
        super(testName);
    }
    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(URLStringTest.class);
    }

    
    public void testURLString() {
        
        String google = "http://www.google.com";
        String vital3 = "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc";
        String vital3materialsLib = "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc";
        String vital3materialsLibParams = "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc?admin=true";
        String vital3asset = "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/basicAdmin.smvc";
        String vital3assetParams = "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/basicAdmin.smvc?action=edit&id=4&title=popcorn+maker";
        
        HashMap materialsLibMap = new HashMap();
        materialsLibMap.put("admin","true");
        
        HashMap assetMap = new HashMap();
        assetMap.put("action","edit");
        assetMap.put("id", new Long(4));
        assetMap.put("title", "popcorn maker");
        
        URLString url = new URLString(google);
        assertEquals(url.toString(), "http://www.google.com");
        
        URLString url2 = new URLString(google, new HashMap());
        assertEquals(url2.toString(), "http://www.google.com");
        assertEquals(url, url2);
        
        url = new URLString(vital3materialsLib);
        assertEquals(url.toString(), "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc");
        url.addParameter("action","edit");
        assertEquals(url.toString(), "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc?action=edit");
        url.addParameter("id", "4");
        assertEquals(url.toString(), "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc?action=edit&id=4");
        url.addParameter("title", "popcorn maker");
        assertEquals(url.toString(), "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc?action=edit&id=4&title=popcorn+maker");
        
        url = new URLString(vital3materialsLib);
        url.addParameters(new String[]{"action","edit","id","4","title","popcorn maker"});
        assertEquals(url.toString(), "http://xnoybis.ccnmtl.columbia.edu:4080/emattes/vital3/materialsLib.smvc?action=edit&id=4&title=popcorn+maker");
    }
    
}
