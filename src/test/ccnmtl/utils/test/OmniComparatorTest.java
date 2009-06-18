package ccnmtl.utils.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.test.AbstractTestCase;

public class OmniComparatorTest extends AbstractTestCase {
    
    private Thingy t1;
    private Thingy t2;
    private Thingy t3;
    private Gizmo g1;
    private Gizmo g2;
    private Gizmo g3;
    
    /**
    * Create the test case
     *
     * @param testName name of the test case
     */
    public OmniComparatorTest (String testName){
        super(testName);
    }
    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite(OmniComparatorTest.class);
    }
    
    public void setUp() throws ParseException{
        // create some thingies and gizmos:
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy/M/d-H:mm:ss");
                
        g1 = new Gizmo(1);
        g2 = new Gizmo(2);
        g3 = new Gizmo(3);
        
        Date myDate = fullDateFormat.parse("2005/1/1-1:01:03");
        t1 = new Thingy("Abecedarian", myDate, g3);
        
        myDate = fullDateFormat.parse("2005/1/1-1:02:02");
        t2 = new Thingy("aardvark", myDate, g2);
        
        myDate = fullDateFormat.parse("2005/1/1-1:03:01");
        t3 = new Thingy("aardvark", myDate, g1);
    }
    
    public void testOmniComparator() {
        
        // thingyStringComp compares by String value, using case-insensitive ordering (on by default)
        OmniComparator thingyStringComp = new OmniComparator(Thingy.class, "getString");
        // thingyDateComp compares by Date value
        OmniComparator thingyDateComp = new OmniComparator(Thingy.class, "getDate");
        
        // test symmetry on equal values:
        assertEquals(0, thingyStringComp.compare(t2,t3));
        assertEquals(0, thingyStringComp.compare(t3,t2));
        
        // test reflexivity on equal values:
        assertEquals(0, thingyStringComp.compare(t1,t1));
        
        // test non-equal values:
        assertTrue(thingyStringComp.compare(t1,t3) > 0);
        assertTrue(thingyStringComp.compare(t3,t1) < 0);
        
        // test multi-criteria ordering:
        thingyStringComp.setSecondaryComparator(thingyDateComp);
        
        assertTrue(thingyStringComp.compare(t2,t3) < 0);
        
        // test case-sensitivity:
        thingyStringComp.useCompareToIgnoreCase(false);
        assertTrue(thingyStringComp.compare(t1,t2) < 0);
        
        // test helper comparator overriding natural order:
        OmniComparator dateSecondsComp = new OmniComparator(Date.class, "getSeconds", null);
        OmniComparator thingyDateSecondsComp = new OmniComparator(Thingy.class, "getDate", dateSecondsComp);
        assertTrue(thingyDateSecondsComp.compare(t1,t2) > 0);
        assertTrue(thingyDateSecondsComp.compare(t3,t2) < 0);
        assertEquals(0, thingyDateSecondsComp.compare(t2,t2));
        
        // test using helper comparator for a non-comparable class (using a primitive as the compare value):
        OmniComparator gizmoComp = new OmniComparator(Gizmo.class, "getInteger", null);
        OmniComparator thingyComp = new OmniComparator(Thingy.class, "getGizmo", gizmoComp);
        
        assertTrue(thingyComp.compare(t2,t3) > 0);
        assertTrue(thingyComp.compare(t2,t1) < 0);
        
        // test reversing sort order:
        thingyComp.reverseSortOrder();
        assertTrue(thingyComp.compare(t2,t3) < 0);
        assertTrue(thingyComp.compare(t2,t1) > 0);
        
        // test trying to construct an omnicomparator using a non-comparable property and not passing a helper
        try{
            OmniComparator badComp = new OmniComparator(Thingy.class, "getGizmo", null);
            fail("OmniComparator constructor allowed a non-comparable comparison property-getter without a helper");
        } catch(IllegalArgumentException e) {}
    }
    
    // inner classes for testing purposes
    public class Thingy {
        private String string;
        private Date date;
        private Gizmo gizmo;
        public Thingy(String string, Date date, Gizmo gizmo) {
            this.string = string;
            this.date = date;
            this.gizmo = gizmo;
        }
        public String getString() {
            return string;
        }
        public Date getDate() {
            return date;
        }
        public Gizmo getGizmo() {
            return gizmo;
        }
    }
    public class Gizmo {
        private int integer;
        public Gizmo(int integer) {
            this.integer = integer;
        }
        public int getInteger() {
            return integer;
        }
    }
    
}
