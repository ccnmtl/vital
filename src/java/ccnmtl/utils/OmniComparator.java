package ccnmtl.utils;

import java.lang.Comparable;
import java.util.ArrayList;
import java.util.Comparator;
import java.lang.reflect.Method;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This comparator can be used as a comparator for any Class. OmniComparator will compare two objects based
 * on a "comparison property" of your choice, which must be accessible via a getter method. This comparison
 * property must belong to a Comparable class or else you must supply a Comparator for it.
 * You may string together OmniComparators to achieve multi-criteria sorting. To do this, assign a secondary
 * comparator using "setSecondaryComparator(Comparator)". The secondary comparator will be
 * consulted in the event of a tie. You may use any Class that implements Comparable as the
 * secondary comparator (including this one!). Your secondary comparator may be configured with its
 * own secondary comparator, and so-on. See constructor doc for more important details.
 * 
 *@author Eric Mattes
 *@version 1.0
 */

public class OmniComparator implements Comparator{
    
    // which Class is this Comparator going to be comparing?
    public Class objectClass;
    // which Method will we use to get comparison data?
    public Method propertyGetter;
    // whether to compare Strings using "compareToIgnoreCase"
    public boolean useCompareToIgnoreCase = true;
    //  if comparison properties don't implement Comparable, we use another comparator for them:
    private Comparator helperComparator;
    // if comparison properties are equal, we pass them to a secondary comparator:
    private Comparator secondaryComparator;
    // the class of object that the propertyGetter returns:
    private Class propertyGetterReturnType;
    // whether the comparison property implements Comparable:
    private boolean returnTypeIsComparable;
    // whether to sort by the natural ascending order or not:
    private boolean ascending = true;
    
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * Constructs an OmniComparator object. For example:
     * <code>OmniComparator thingyComp = new OmniComparator(Thingy.class, "getName", null);</code>
     *
     * Here is an example of an OmniComparator constructed with a helper comparator:
     * <code>OmniComparator gizmoComp = new OmniComparator(Gizmo.class, "getWidget", widgetComp);</code>
     *
     * To assign a tiebreaker comparator, use setSecondaryComparator and pass it a comparator:
     * <code>OmniComparator gizmoComp2 = new OmniComparator(Gizmo.class, "getDoohickey", null);</code>
     * <code>gizmoComp.setSecondaryComparator(gizmoComp2);</code>
     *
     *@param objectClass The Class of object this will be comparing.
     *@param propertyGetterName A String holding the name of the method that will return the comparison property.
     * This method must be defined for the class (obviously) and the return value must EITHER belong to a class
     * that implements Comparable OR you must pass a helperComparator that will compare Objects of that Class.
     *@param helperComparator A Comparator that will be used to compare the comparison properties of each object.
     * If you pass a helperComparator, it will be used instead of the compareTo method even if the Class implements
     * Comparable, so you can use this feature to override the natural order of a Comparable class. You may use
     * another OmniComparator as a helper.
     */
    public OmniComparator(Class objectClass, String propertyGetterName, Comparator helperComparator) {
        if (objectClass == null || propertyGetterName == null) throw new IllegalArgumentException("objectClass or getterName was null. Cannot construct comparator.");
        this.objectClass = objectClass;
        // get the comparison property getter method and its return type:
        try {
            this.propertyGetter = objectClass.getMethod(propertyGetterName, null);
        } catch(Exception e) {
            throw new IllegalArgumentException("Error during Method retrieval. The method '"+objectClass.getName()+"."+propertyGetterName+"()' doesn't exist.");
        }
        this.propertyGetterReturnType = this.propertyGetter.getReturnType();
        // see if the return type is comparable:
        if (this.propertyGetterReturnType.isPrimitive()) {
            this.returnTypeIsComparable = true;
        } else {
            this.returnTypeIsComparable = false;
            Class[] interfaces = this.propertyGetterReturnType.getInterfaces();
            for (int i=0; i<interfaces.length; i++) {
                if(interfaces[i].equals(Comparable.class)) {
                    this.returnTypeIsComparable = true;
                    break;
                }
            }
        }
        // enforce helperComparator rule:
        if (returnTypeIsComparable == false && helperComparator == null) {
            throw new IllegalArgumentException("Your getter method returns an object which does not implement comparable, so you must provide a helperComparator");
        }
        this.helperComparator = helperComparator;
    }
    
    /**
     * Two-argument constructor for when no helperComparator is needed
     */
    public OmniComparator(Class objectClass, String propertyGetterName) {
        this(objectClass, propertyGetterName, null);
    }
    
    /**
     * Compare objects using the propertyGetter Method, (using the helperComparator if supplied) and
     * using the secondaryComparator as a tiebreaker (if that is supplied). See constructor doc for 
     * some technicalities.
     *
     *@param o1 The first object in the comparison
     *@param o2 The second object in the comparison
     *
     *@return an integer which will indicate o2's order relative to o1. Negative indicates less-than, Zero indicates
     *  equal, and positive indicates greater-than.
     */
    public int compare(Object o1, Object o2) {
        
        if (o1 == null || o2 == null) throw new NullPointerException("One or both objects were null. Cannot compare.");
        if ( !(this.objectClass.isInstance(o1) && this.objectClass.isInstance(o2)) ) {
            throw new IllegalArgumentException("One or both objects were not of the proper class: " + this.objectClass.getName());
        }
        Object prop1 = null;
        Object prop2 = null;
        try {
            prop1 = this.propertyGetter.invoke(o1, null);
            prop2 = this.propertyGetter.invoke(o2, null);
        } catch(Exception e) {
            throw new RuntimeException("Error during property-getter method invocation.", e);
        }
        // compare the comparison properties:
        int result = 0;
        if (helperComparator != null) {
            result = helperComparator.compare(prop1, prop2);
        } else if (returnTypeIsComparable) {
            if (propertyGetterReturnType.equals(String.class) && useCompareToIgnoreCase) {
                String s1 = (String) prop1;
                String s2 = (String) prop2;
                result = s1.compareToIgnoreCase(s2);
            } else {
                Comparable c1 = (Comparable) prop1;
                Comparable c2 = (Comparable) prop2;
                
                
                if (c1 == null) {
                    logger.warn ("Compare method failed because " + this.propertyGetter.getName() + " called on " +  o1 + " returned null.");
                }
                if (c2 == null) {
                    logger.warn ("Compare method failed because " + this.propertyGetter.getName() + " called on " +  o2 + " returned null.");
                }
                result = c1.compareTo(c2);
            }
        } else {
            // this should never happen unless someone really tries to break this:
            throw new IllegalStateException("Comparison property is not Comparable and no helperComparator was supplied!");
        }
        // tiebreaker if result is equal and secondaryComparator was supplied:
        if (result == 0 && secondaryComparator != null) {
            result = secondaryComparator.compare(o1, o2);
        }
        if (!ascending) result = 0 - result;
        return result;
    }
    
    /**
     * Set whether or not to ignore case when comparing Strings. True by default.
     */
    public void useCompareToIgnoreCase(boolean yesOrNo) {
        this.useCompareToIgnoreCase = yesOrNo;
    }
    
    /**
     * Call this toggle-style method (before sorting) to reverse the sort order.
     */
    public void reverseSortOrder() {
        this.ascending = !ascending;
    }
    
    /**
     * Assign a secondary comparator, which will be consulted if this comparison results in a tie.
     */
    public void setSecondaryComparator(Comparator comp) {
        this.secondaryComparator = comp;
    }
    
    public Comparator getSecondaryComparator() {
        return secondaryComparator;
    }
    

}

