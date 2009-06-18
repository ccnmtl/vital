package ccnmtl.vital3.dao;

import java.util.*;
import org.springframework.dao.DataRetrievalFailureException;

import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.*;

/**
 * Vital3DAO is the do-it-all DAO for Vital3. Use this for retrieving, inserting, updating, and deleting vital3 class instances.
 */
public interface Vital3DAO {
    
    /**
     * For cache emergencies
     */
    public void flushCache();
    
    
    /**
     * Get Stats from Hibernate.
     */
    public void logStatsFromHibernate ();
    
    public Map getStatsFromHibernate ();
    

    
    /**
     * For unpersisted objects, this will insert them into the database. For already-persisted objects,
     * this will update them in the database.
     *@param objClass  The Class that this object is supposed to be.
     *@param obj       The object to save/update
     */
    public void save(Class objClass, Object obj) throws DataRetrievalFailureException;
    
    /**
     * For unpersisted objects, this will insert them into the database. For already-persisted objects,
     * this will update them in the database.
     *@param objClass        The Class that each object in the collection is supposed to be.
     *@param collection      The collection whose contents will be saved/updated. Should all be of the type
     *                       indicated by objClass.
     */
    public void saveCollection(Class objClass, Collection collection) throws DataRetrievalFailureException;
    
    /**
     * Remove an object from persistence permanently.
     */
    public void delete(Class objClass, Object obj) throws DataRetrievalFailureException;
    
    /**
     * Remove a collection of objects from persistence permanently.
     */
    public void deleteCollection(Class objClass, Collection collection) throws DataRetrievalFailureException;
    
    /**
     * Find a single persisted object of a particular class with the id you pass.
     *@return The object with the id you passed, or null if it wasn't found.
     */
    public Object findById(Class objClass, Long id) throws DataRetrievalFailureException;
    
    /**
     * Find every persisted object of a particular class.
     *@return A List of objects that belong to the class. An empty List if none exist.
     */
    public List findAll(Class objClass) throws DataRetrievalFailureException;
    
    /**
     * Find all persisted objects of a class where the value of a particular property is equal to that
     * which you pass.
     *@return a List of Objects that corresponded to your query criteria. If no objects met that criteria,
     * an empty list is returned.
     */
    
    
    public List findByPropertyValue(Class objClass, String propertyName, Object value) throws DataRetrievalFailureException;
    public List findByTwoPropertyValues(Class objClass, String propertyName1, Object value1, String propertyName2, Object value2) throws DataRetrievalFailureException;
    public List findBySetOfPropertyValues(Class objClass, String propertyName, Collection values) throws DataRetrievalFailureException;
    //
    public List getAnnotations(VitalParticipant participant, Set materials, Date minDate) throws DataRetrievalFailureException;
    //
    //public List getAllCollections(Class parentClass, Collection items, String collectionName, Class childClass) throws DataRetrievalFailureException;


    public List findByPropertyValueSubstring(Class objClass, String propertyName, String substring) throws DataRetrievalFailureException;
    

    //// ENABLED GETTER/SETTER: use this to test data-access-failure-handling
    public boolean getEnabled();
    public void setEnabled(boolean enabled);

}




