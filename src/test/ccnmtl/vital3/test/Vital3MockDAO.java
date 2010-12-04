package ccnmtl.vital3.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;

import ccnmtl.vital3.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.*;

/**
 * Vital3MockDAO implements the Vital3DAO interface using my own custom-made classes which
 * simulate a simple database.
 * Note: This must be implemented as a singleton! 
 */
public class Vital3MockDAO implements Vital3DAO {
    
    // Map whose keys are of type Class and values of type MockDBTable. Represents a mock database.
    private MockDB mockDB;
    
    // An on/off switch for testing purposes: setting this to false will cause data access failures (on purpose!)
    private boolean enabled;
    
    // logger to make up for the Spring DaoSupport logger (MockDBTable does not inherit from DaoSupport):
    protected final Log logger = LogFactory.getLog(getClass());
    
    
    // Constructors for TreeSet and HashSet:
    private Constructor treeSetConstructor;
    private Constructor hashSetConstructor;
    
    // constructor (where the tables are created)
    public Vital3MockDAO() {
        mockDB = new MockDB();
        mockDB.createTable(Annotation.class);
        mockDB.createTable(Answer.class);
        mockDB.createTable(Assignment.class);
        mockDB.createTable(AssignmentMaterialAssoc.class);
        mockDB.createTable(AssignmentResponse.class);
        mockDB.createTable(AssignmentResponseHistory.class);
        mockDB.createTable(Comment.class);
        mockDB.createTable(CustomField.class);
        mockDB.createTable(CustomFieldValue.class);
        mockDB.createTable(Material.class);
        mockDB.createTable(Question.class);
        mockDB.createTable(QuestionMaterialAssoc.class);
        mockDB.createTable(RawUCMCourse.class);
        mockDB.createTable(RawUCMCourseWorksiteAffil.class);
        mockDB.createTable(RawUCMParticipant.class);
        mockDB.createTable(RawUCMTerm.class);
        mockDB.createTable(RawUCMUser.class);
        mockDB.createTable(RawUCMWorksite.class);
        mockDB.createTable(Unit.class);
        mockDB.createTable(UnitMaterialAssoc.class);
        mockDB.createTable(VitalParticipant.class);
        mockDB.createTable(VitalUser.class);
        mockDB.createTable(VitalWorksite.class);
        
        this.enabled = true;
        
        try {
            treeSetConstructor = TreeSet.class.getConstructor(new Class[]{ Collection.class });
            hashSetConstructor = HashSet.class.getConstructor(new Class[]{ Collection.class });
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No such method!", e);
        }
    }
    
    
    // reset UCM-related properties. This is necessary to simulate multiple requests in your test.
    // because normally, the properties which are set up after ucm decoration are thrown in the garbage
    // after a request ends, in your test code, you must call this so that the decorated classes do not
    // remain decorated after a request has 'completed'.
    protected void resetUCM() {
        
        mockDB.resetUCM();
    }
    
    
    public void flushCache() {
        throw new RuntimeException("flushCache not implemented by mockDAO");
    }
    
    public Map getStatsFromHibernate () {
        throw new RuntimeException("getStatsFromHibernate not implemented by mockDAO");
    }
    
    public void logStatsFromHibernate() {
        throw new RuntimeException("flushCache not implemented by logStatsFromHibernate");
    }
    
    
    
    
    /**
     * For unpersisted objects, this will insert them into the database. For already-persisted objects,
     * this will update them in the database.
     *@param objClass  The Class that this object is supposed to be.
     *@param obj       The object to save/update
     */
    public void save(Class objClass, Object obj) throws DataRetrievalFailureException {
        if (obj == null) throw new IllegalArgumentException("tried to save null object");
        if (!objClass.isInstance(obj)) throw new IllegalArgumentException("object was not of type " + objClass.getName());
        try{
            Persistable pObj = (Persistable) obj;
            mockDB.saveOrUpdate(objClass, pObj);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not save/update object.",e);
        }
    }
    
    /**
     * For unpersisted objects, this will insert them into the database. For already-persisted objects,
     * this will update them in the database.
     *@param objClass        The Class that each object in the collection is supposed to be.
     *@param collection      The collection whose contents will be saved/updated. Should all be of the type
     *                       indicated by objClass.
     */
    public void saveCollection(Class objClass, Collection collection) throws DataRetrievalFailureException {
        
        if (collection == null) throw new IllegalArgumentException("tried to save null collection");
        if (collection.size() == 0) return;
        
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ( obj == null || (!objClass.isInstance(obj)) ) throw new IllegalArgumentException("object was null or not of type " + objClass.getName());
        }
        
        failIfNotEnabled();
        try{
            iter = collection.iterator();
            while (iter.hasNext()) {
                Persistable pObj = (Persistable) iter.next();
                mockDB.saveOrUpdate(objClass, pObj);
            }
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not save/update object.",e);
        }
        
    }
    
    /**
     * Remove an object from persistence permanently.
     */
    public void delete(Class objClass, Object obj) throws DataRetrievalFailureException {
        if (obj == null) throw new IllegalArgumentException("tried to delete null object");
        if (!objClass.isInstance(obj)) throw new IllegalArgumentException("object was not of type " + objClass.getName());
        try {
            
            Persistable pObj = (Persistable) obj;
            
            // cascade delete!
            // the children may be from different collections/classes!
            Set children = pObj.getAllPersistableChildren();
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                Object child = iter.next();
                delete(child.getClass(), child);
            }
            
            mockDB.delete(objClass, pObj);
            pObj.removeFromCollections();
            
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not delete object.",e);
        }
    }
    
    /**
     * Remove a collection of objects from persistence permanently.
     */
    public void deleteCollection(Class objClass, Collection collection) throws DataRetrievalFailureException {
        
        if (collection == null) throw new IllegalArgumentException("tried to delete null collection");
        if (collection.size() == 0) return;
        
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ( obj == null || (!objClass.isInstance(obj)) ) throw new IllegalArgumentException("object was null or not of type " + objClass.getName());
        }
        
        failIfNotEnabled();
        try{
            Iterator collectionIter = collection.iterator();
            while (collectionIter.hasNext()) {
                Object obj = collectionIter.next();
                delete(objClass, obj);
            }
            
        } catch (Exception e) {
            System.out.println("Exception thrown trying to delete a collection of " + objClass );
            throw new DataRetrievalFailureException("Could not delete object.",e);
        }
    }
    
    
    /**
     * Find a single persisted object of a class with the id you pass.
     *@return The object with the id you passed, or null if it wasn't found.
     */
    public Object findById(Class objClass, Long id) throws DataRetrievalFailureException {
        if (id == null) throw new IllegalArgumentException("id was null");
        try{
            return mockDB.findById(objClass, id);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not perform find.",e);
        }
    }
    
    
    /**
     * Find every persisted object of a particular class.
     *@return A List of objects that belong to the class. An empty List if none exist.
     */
    public List findAll(Class objClass) throws DataRetrievalFailureException {
        try{
            return mockDB.findAll(objClass);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not perform find.",e);
        }
        
    }
    
    
    /**
     * Find all persisted objects of a class where the value of a particular property is equal to that
     * which you pass. valueType is arbitrarily required so as to improve compile-time error-checking.
     *@return a List of Objects that corresponded to your query criteria. If no objects met that criteria,
     * an empty list is returned.
     */
    public List findByPropertyValue(Class objClass, String propertyName, Object value) throws DataRetrievalFailureException {
        if (objClass == null) throw new IllegalArgumentException("findByPropertyValue: objClass was null");
        if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("findByPropertyValue: propertyName was null or zero-length");
        
        try{
            return mockDB.findByPropertyValue(objClass, propertyName, value);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not perform find.",e);
        }
        
    }

    /**
     * Find all persisted objects of a class where the value of a particular property is equal to that
     * which you pass. valueType is arbitrarily required so as to improve error-checking.
     *@return a List of Objects that corresponded to your query criteria. If no objects met that criteria,
     * an empty list is returned.
     */
    public List findByTwoPropertyValues(Class objClass, String propertyName1, Object value1, String propertyName2, Object value2) throws DataRetrievalFailureException {
        
        if (objClass == null) throw new IllegalArgumentException("findByPropertyValue: objClass was null");
        if (propertyName1 == null || propertyName1.length() == 0) throw new IllegalArgumentException("findByPropertyValue: propertyName1 was null or zero-length");
        if (propertyName2 == null || propertyName2.length() == 0) throw new IllegalArgumentException("findByPropertyValue: propertyName2 was null or zero-length");
        
        try{
            return mockDB.findByTwoPropertyValues(objClass, propertyName1, value1, propertyName2, value2);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not perform find.",e);
        }
        
    }
    
    /**
     * Like find by property value, but it returns all records where the property matches any of the values in the set you pass.
     */
    public List findBySetOfPropertyValues(Class objClass, String propertyName, Collection values) throws DataRetrievalFailureException {
        
        if (objClass == null) throw new IllegalArgumentException("findBySetOfPropertyValues: objClass was null");
        if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("findBySetOfPropertyValues: propertyName was null or zero-length");
        if (values == null || values.size() == 0) throw new IllegalArgumentException("findBySetOfPropertyValues: values was null or empty");
        
        try{
            return mockDB.findBySetOfPropertyValues(objClass, propertyName, values);
        } catch (Exception e) {
            throw new DataRetrievalFailureException("Could not perform find.",e);
        }
    }
    
    /**
        * Selects annotations which meet certain criteria. This method exists because retrieving notes
     * proves to be a very performance-intensive operation. Even for selecting just a couple of notes, it
     * would otherwise mean the participant's entire annotations collection has to be loaded. Using this
     * method, the database does the work of finding the desired notes without having to initialize that
     * entire collection.
     *<p>If materials is null or empty, that is taken to mean that material will not be used as a
     * criteria for returning annotations. Likewise, if minDate is null, the date will not be a criteria.
     * If both are null, this returns ALL annotations for the participant you passed. Participant is required.
     * Returns an empty set if no annotations fit your criteria.
     *
     *@param participant  The participant to whom these annotations belong.
     *@param materials    A set of materials - one of which annotations must be related to to be returned.
     *@param minDate      A Date after which annotations must have been modified to be returned.
     */
    public List getAnnotations(VitalParticipant participant, Set materials, Date minDate) throws DataRetrievalFailureException {
        
        return participant.getAnnotationsForMaterials(materials,minDate);
    }
    
    public List findByPropertyValueSubstring(Class objClass, String propertyName, String substring) throws DataRetrievalFailureException {
        
        throw new DataRetrievalFailureException("The mock DAO currently doesn't support searching for items by substring.");
    
    }

    
    
    /**
     * Use to initialize a collection for a collection of items. Example: You have a collection of Assignments
     * and you want to initialize all of their Questions.
     * You would call: initCollections(Assignment.class, myAssignments, Question.class), and it would return a Set
     * containing each Question that belonged to each Assignment, with no duplicates.
     *@param parentClass          The class of each item in the "items" collection.
     *@param items                A collection of items, each of the class itemClass.
     *@param childClass           The class of each item in the collection to be initialized.
     */
    /*public List getAllCollections(Class parentClass, Collection items, Class childClass) throws DataRetrievalFailureException {
        
        String parentName = Vital3Utils.getSimpleName(parentClass);
        
        Set parentSet = null;
        if (items instanceof Set) parentSet = (Set) items;
        else parentSet = new HashSet(items);
        
        return findBySetOfPropertyValues(childClass, parentName, parentSet);
        
    }*/
    
    ///////////////////////////
    //// PRIVATES
    
    // Use this for testing data retrieval failure:
    private void failIfNotEnabled() throws DataRetrievalFailureException {
        
        if (!this.enabled) throw new DataRetrievalFailureException("Data Access Disabled For Testing Purposes");
    }
    
    
    //// ENABLED GETTER/SETTER: use this to test data-access-failure-handling
    public boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
