package ccnmtl.vital3.dao;

import java.lang.NullPointerException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.Query;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;
import org.hibernate.stat.*;

/**
 * Vital3HibernateDAO fufills the Vital3DAO Interface requirements using a Hibernate implementation.
 * It is intended to be the only method of retrieving data from the database, and that includes
 * initialization of Collections (which hold related objects). All initialization code should be in
 * this class, NOT in any of the persistent classes themselves.
 */
public class Vital3HibernateDAO extends HibernateDaoSupport implements Vital3DAO {
    
    private boolean enabled;
    
    public Vital3HibernateDAO() {
        super();
        this.enabled = true;
    }

    public void initDao() {
        //Turn on the stats!
        getHibernateTemplate().getSessionFactory().getStatistics().setStatisticsEnabled(true);
    
    }


    protected final Log logger = LogFactory.getLog(getClass());
	
    // FYI: Can use Hibernate.initialize(Object collection) to force init of a collection...
    
    /**
     * Overridden method from HibernateDaoSupport, doesn't actually do anything useful, but this is where
     * you would insert code if you want to customize the HibernateTemplate.
     *@return a HibernateTemplate.
     */
    /*protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
		
        HibernateTemplate ht = new HibernateTemplate(sessionFactory);
        //ht.setAlwaysUseNewSession(true);
        return ht;
	}*/
    
    public void flushCache() {
        
        try {
            SessionFactory sf = getHibernateTemplate().getSessionFactory();
            sf.evict(Annotation.class);
            sf.evict(Answer.class);
            sf.evict(Assignment.class);
            sf.evict(AssignmentMaterialAssoc.class);
            sf.evict(AssignmentResponse.class);
            sf.evict(Comment.class);
            sf.evict(CustomField.class);
            sf.evict(CustomFieldValue.class);
            sf.evict(Material.class);
            sf.evict(Question.class);
            sf.evict(QuestionMaterialAssoc.class);
            sf.evict(RawUCMCourse.class);
            sf.evict(RawUCMCourseWorksiteAffil.class);
            sf.evict(RawUCMParticipant.class);
            sf.evict(RawUCMTerm.class);
            sf.evict(RawUCMUser.class);
            sf.evict(RawUCMWorksite.class);
            sf.evict(Unit.class);
            sf.evict(UnitMaterialAssoc.class);
            sf.evict(VitalParticipant.class);
            sf.evict(VitalUser.class);
            sf.evict(VitalWorksite.class);
            
            sf.evictCollection("ccnmtl.vital3.Assignment.materialAssociations");
            sf.evictCollection("ccnmtl.vital3.Assignment.questions");
            sf.evictCollection("ccnmtl.vital3.CustomField.values");
            sf.evictCollection("ccnmtl.vital3.Material.customFieldValues");
            sf.evictCollection("ccnmtl.vital3.Question.materialAssociations");
            sf.evictCollection("ccnmtl.vital3.ucm.RawUCMCourse.worksiteAffils");
            sf.evictCollection("ccnmtl.vital3.ucm.RawUCMTerm.worksites");
            sf.evictCollection("ccnmtl.vital3.ucm.RawUCMUser.participants");
            sf.evictCollection("ccnmtl.vital3.ucm.RawUCMWorksite.participants");
            sf.evictCollection("ccnmtl.vital3.ucm.RawUCMWorksite.courseAffils");
            sf.evictCollection("ccnmtl.vital3.Unit.assignments");
            sf.evictCollection("ccnmtl.vital3.Unit.materialAssociations");
            sf.evictCollection("ccnmtl.vital3.VitalWorksite.materials");
            sf.evictCollection("ccnmtl.vital3.VitalWorksite.units");
            
        } catch (HibernateException he) {
            throw new RuntimeException(he);
        }
        
    }
    

    
    public void logStatsFromHibernate () {
        org.hibernate.stat.Statistics stats = getHibernateTemplate().getSessionFactory().getStatistics();
        
        if (stats.isStatisticsEnabled() ) {
            logger.debug("Stats are on.");
        }
        else {
            logger.debug("Stats are off.");
            return;
        }
        
        logger.debug(stats.getSuccessfulTransactionCount() +
        "The number of transactions we know to have been successful" );
        logger.debug(stats.getTransactionCount() +
        "The number of transactions we know to have completed" );
        logger.debug(stats.getQueryCacheHitCount() +
        "Global number of cached queries successfully retrieved from cache" );
        logger.debug(stats.getCloseStatementCount()  +
        "The number of prepared statements that were released" );
        logger.debug(stats.getCollectionFetchCount()  +
        "Global number of collections fetched" );
        logger.debug(stats.getCollectionLoadCount() +
        "Global number of collections loaded" );
        logger.debug(stats.getCollectionRecreateCount() +
        "Global number of collections recreated" );
        logger.debug(stats.getCollectionRemoveCount() +
        "Global number of collections removed" );
        logger.debug(stats.getCollectionUpdateCount() +
        "Global number of collections updated" );
        logger.debug(stats.getConnectCount() +
        "Get the global number of connections asked by the sessions (the actual number of connections used may be much smaller depending whether you use a connection pool or not)" );
        logger.debug(stats.getEntityDeleteCount() +
        "Get global number of entity deletes" );
        logger.debug(stats.getEntityFetchCount() +
        "Get global number of entity fetchs" );
        logger.debug(stats.getEntityInsertCount() +
        "Get global number of entity inserts" );
        logger.debug(stats.getEntityLoadCount() +
        "Get global number of entity loads" );
        logger.debug(stats.getEntityUpdateCount() +
        "Get global number of entity updates" );
        logger.debug(stats.getFlushCount() +
        "Get the global number of flush executed by sessions (either implicit or explicit)" );
        logger.debug(stats.getOptimisticFailureCount() +
        "The number of StaleObjectStateExceptions that occurred" );
        logger.debug(stats.getPrepareStatementCount() +
        "The number of prepared statements that were acquired" );
        logger.debug(stats.getQueryCacheHitCount() +
        "Get the global number of cached queries successfully retrieved from cache" );
        logger.debug(stats.getQueryCacheMissCount() +
        "Get the global number of cached queries *not* found in cache" );
        logger.debug(stats.getQueryCachePutCount() +
        "Get the global number of cacheable queries put in cache" );
        logger.debug(stats.getQueryExecutionCount() +
        "Get global number of executed queries" );
        logger.debug(stats.getQueryExecutionMaxTime() +
        "Get the time in milliseconds of the slowest query" );
        logger.debug(stats.getQueryExecutionMaxTimeQueryString() +
        "Get the query string for the slowest query" );
        logger.debug(stats.getSecondLevelCacheHitCount() +
        "Global number of cacheable entities/collections successfully retrieved from the second-level cache" );
        logger.debug(stats.getSecondLevelCacheMissCount() +
        "Global number of cacheable entities/collections not found in the second-level cache and loaded from the database." );
        logger.debug(stats.getSecondLevelCachePutCount() +
        "Global number of cacheable entities/collections put in the second-level cache" );
        logger.debug(stats.getSessionCloseCount() +
        "Global number of sessions closed" );
        logger.debug(stats.getSessionOpenCount() +
        "Global number of sessions opened" );
        logger.debug(stats.getStartTime() +
        "Start time" );
    
        Iterator cacheRegionNamesIter = Arrays.asList(stats.getSecondLevelCacheRegionNames()).iterator();
        while (cacheRegionNamesIter.hasNext()) {
            org.hibernate.stat.SecondLevelCacheStatistics cacheStats = stats.getSecondLevelCacheStatistics((String) cacheRegionNamesIter.next());

            logger.debug(
            cacheStats.getCategoryName() + ": " +
            cacheStats.getHitCount() + " Hits, " +
            cacheStats.getMissCount() + " Misses, " +
            cacheStats.getPutCount() + " Puts"
             );
            // these don't work for now:
            /*
            logger.debug(cacheStats.getElementCountInMemory() +
            "Count stored in memory" );

            logger.debug(cacheStats.getElementCountOnDisk() +
            "Count stored on disk" );

            logger.debug(cacheStats.getEntries() +
            "Entry count" );

            logger.debug(cacheStats.getSizeInMemory() +
            "Size in Memory" );
            */
        }
    
    }
    
    
    
    public Map getStatsFromHibernate () {
        org.hibernate.stat.Statistics stats = getHibernateTemplate().getSessionFactory().getStatistics();
        if (stats.isStatisticsEnabled() ) {
            logger.debug("Stats are on.");
        }
        else {
            logger.debug("Stats are off.");
            return null;
        }
        Map result = new HashMap();
        result.put ("SessionFactoryStats", stats);
        Iterator cacheRegionNamesIter = Arrays.asList(stats.getSecondLevelCacheRegionNames()).iterator();
        while (cacheRegionNamesIter.hasNext()) {
            SecondLevelCacheStatistics cacheStats = stats.getSecondLevelCacheStatistics((String) cacheRegionNamesIter.next());
            
            Map newStat = new HashMap();
            newStat.put ("name",   cacheStats.getCategoryName());
            newStat.put ("hits",   Long.toString(cacheStats.getHitCount()));
            newStat.put ("misses", Long.toString(cacheStats.getMissCount()));
            newStat.put ("puts",   Long.toString(cacheStats.getPutCount()));
            result.put (cacheStats.getCategoryName(), newStat);
        }
        return result;
    }
    
    
    /**
     * For unpersisted objects, this will insert them into the database. For already-persisted objects,
     * this will update them in the database.
     *@param objClass  The Class that this object is supposed to be.
     *@param obj       The object to save/update.
     */
    public void save(Class objClass, Object obj) throws DataRetrievalFailureException {
        
        if (obj == null) throw new IllegalArgumentException("tried to save null object");
        if (!objClass.isInstance(obj)) throw new IllegalArgumentException("object was not of type " + objClass.getName());
        logger.debug("Vital3HibernateDAO: saving object of class " + objClass.getName());
        failIfNotEnabled();
        
		getHibernateTemplate().saveOrUpdate(obj);
        
        /** Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(obj);
            transaction.commit();
        } catch(HibernateException he) {
            try {
                if (transaction != null) transaction.rollback();
            } catch (HibernateException he2) {
                throw new DataRetrievalFailureException("Could not save collection and could not roll back transaction", he2);
            }
            throw new DataRetrievalFailureException("Could not successfully save collection!", he);
        } finally {
            releaseSession(session);
        }
        //*/
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
        logger.debug("Vital3HibernateDAO: saving collection (size = " + collection.size() + ") of class " + objClass.getName());
        failIfNotEnabled();
        
        getHibernateTemplate().saveOrUpdateAll(collection);
        
        /** NOTE: This commented-out technique was NOT saving/updating! I don't know why. Maybe I needed to commit?
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            iter = collection.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                session.saveOrUpdate(obj);
            }
        } catch(HibernateException he) {
            try {
                if (transaction != null) transaction.rollback();
            } catch (HibernateException he2) {
                throw new DataRetrievalFailureException("Could not save collection and could not roll back transaction", he2);
            }
            throw new DataRetrievalFailureException("Could not successfully save collection!", he);
        } finally {
            releaseSession(session);
        }
        //**/
    }
    
    /**
     * Remove an object from persistence permanently.
     */
    public void delete(Class objClass, Object obj) throws DataRetrievalFailureException {
        
        if (obj == null) throw new IllegalArgumentException("tried to save null object");
        if (!objClass.isInstance(obj)) throw new IllegalArgumentException("object was not of type " + objClass.getName());
        logger.debug("Vital3HibernateDAO: deleting object of class " + objClass.getName());
        failIfNotEnabled();
        
        Persistable pObj = (Persistable) obj;
        
        //removeFromCollections must be called AFTER deleting, because otherwise hibernate won't know how to cascade...
        getHibernateTemplate().delete(obj);
        
        pObj.removeFromCollections();
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
        logger.debug("Vital3HibernateDAO: deleting collection (size = " + collection.size() + ") of class " + objClass.getName());
        failIfNotEnabled();
        
        // removeFromCollections must be called AFTER deleting, because otherwise hibernate won't know how to cascade...
        getHibernateTemplate().deleteAll(collection);
        
        Vital3Utils.removeMultipleFromCollections(collection);
    }
    
    
    /**
     * Find a single persisted object of a particular class with the id you pass.
     *@return The object with the id you passed, or null if it wasn't found.
     *        If more than one object had that id, the first is returned.
     */
    public Object findById(Class objClass, Long id) throws DataRetrievalFailureException {
        
        if (id == null) throw new IllegalArgumentException("id was null");
        
        String className = objClass.getName();
        logger.debug("Vital3HibernateDAO: finding object of class " + className + " with id " + id);
        failIfNotEnabled();
        
		List list = getHibernateTemplate().find("from "+ className +" x where x.id = ?", new Object[] { id } );
		
        /*  This code works, but it is appears to be no better than the HibernateTemplate code...
            List list = null;
        Session session = null;
        try {
            session = getSession();
            String queryString = "from "+ className +" x where x.id = ?";
            Query queryObject = session.createQuery(queryString);
            //prepareQuery(queryObject);
            queryObject.setParameter(0, id);
            
            list = queryObject.list();
        } catch(HibernateException he) {
            throw new DataRetrievalFailureException("Could not find due to hibernate/database problem", he);
        } finally {
            releaseSession(session);
        }*/
        
        Object obj = null;
		if(list.size() > 0){
            obj = list.get(0);
		}
		return obj;
    }
    
    /**
     * Find every persisted object of a particular class.
     *@return A List of objects that belong to the class. An empty List if none exist.
     */
    public List findAll(Class objClass) throws DataRetrievalFailureException {
        
        String className = objClass.getName();
        logger.debug("Vital3HibernateDAO: finding all objects of class " + objClass.getName());
        failIfNotEnabled();
        
		List list = getHibernateTemplate().find("from "+ className );
		return list;
    }
    
    /**
     * Find all persisted objects of a class where the value of a particular property is equal to that
     * which you pass. valueType is arbitrarily required so as to improve error-checking.
     *@return a List of Objects that corresponded to your query criteria. If no objects met that criteria,
     * an empty list is returned.
     */
    public List findByPropertyValue(Class objClass, String propertyName, Object value) throws DataRetrievalFailureException {
        
        if (objClass == null) throw new IllegalArgumentException("findByPropertyValue: objClass was null");
        if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("findByPropertyValue: propertyName was null or zero-length");
        
        // NOTE: Class.getName returns a FULLY-QUALIFIED classname.
        String className = objClass.getName();
        logger.debug("Vital3HibernateDAO: finding objects of class " + className + " with " + propertyName + " = " + value);
        failIfNotEnabled();
        
        List list = getHibernateTemplate().find("from " + className + " x where x." + propertyName + " = ?", new Object[] { value } );
        /*
         This code works, but it is appears to be no better than the HibernateTemplate code...
         List list = null;
        Session session = null;
        try {
            session = getSession();
            String queryString = "from "+ className +" x where x." + propertyName + " = ?";
            Query queryObject = session.createQuery(queryString);
            //prepareQuery(queryObject);
            queryObject.setParameter(0, value);
            
            list = queryObject.list();
        } catch(HibernateException he) {
            throw new DataRetrievalFailureException("Could not find due to hibernate/database problem", he);
        } finally {
            releaseSession(session);
        }*/
        
        
        return list;
        
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
        
        // NOTE: Class.getName returns a FULLY-QUALIFIED classname.
        String className = objClass.getName();
        logger.debug("Vital3HibernateDAO: finding objects of class " + className + " with " + propertyName1 + " = " + value1 + " and " + propertyName2 + " = " + value2);
        failIfNotEnabled();
        
        List list = getHibernateTemplate().find(
			" from " + className + " x " + 
			" where x." + propertyName1 + " = ? " + 
			" and x."   + propertyName2 + " = ? " ,
			new Object[] { value1, value2 } 
			
		);
        return list;   
    }
    
    /**
     * Like find by property value, but it returns all records where the property matches any of the values in the set you pass.
     */
    public List findBySetOfPropertyValues(Class objClass, String propertyName, Collection values) throws DataRetrievalFailureException {
        
        if (objClass == null) throw new IllegalArgumentException("findBySetOfPropertyValues: objClass was null");
        if (propertyName == null || propertyName.length() == 0) throw new IllegalArgumentException("findBySetOfPropertyValues: propertyName was null or zero-length");
        if (values == null || values.size() == 0) throw new IllegalArgumentException("findBySetOfPropertyValues: values was null or empty");
        
        // NOTE: Class.getName returns a FULLY-QUALIFIED classname.
        String className = objClass.getName();
        logger.debug("Vital3HibernateDAO: finding objects of class " + className + " with " + propertyName + " in (set of values)");
        failIfNotEnabled();
        
        int numValues = values.size();
        
        // assemble query string:
        String queryString = "from " + className + " x where x." + propertyName + " in (";
        for (int i=0; i< numValues; i++) {
            if (i != numValues-1) queryString += "?,";
            else queryString += "?";
        }
        queryString += ")";
        
        // put values in array:
        Object[] params = values.toArray();
        
        List list = getHibernateTemplate().find(queryString, params);
        return list;
        
    }    
    
    /**
     * Selects annotations which meet certain criteria. This method exists because retrieving notes
     * proves to be a very performance-intensive operation. Even for selecting just a couple of notes, it
     * would otherwise mean the participant's entire annotations collection has to be loaded. Using this
     * method, the database does the work of finding the desired notes without having to initialize that
     * entire collection.
     * <p>This will return annotations which meet the following criteria: its material is one of the materials
     * in the Set of materials you passed AND its dateModified is greater than minDate.
     * Therefore, if materials is EMPTY, no annotations will be returned. If materials is NULL, on the other hand,
     * minDate becomes the only criteria used to pick annotations. Likewise, if minDate is null, the date will not be a criteria.
     * However, If both minDate and materials are null, this returns ALL annotations for the participant you passed. Participant is required.
     * Returns an empty set if no annotations fit your criteria.
     *
     *@param participant  The participant to whom these annotations belong.
     *@param materials    A set of materials - one of which annotations must be related to to be returned.
     *@param minDate      A Date after which annotations must have been modified to be returned.
     */
    public List getAnnotations(VitalParticipant participant, Set materials, Date minDate) throws DataRetrievalFailureException {
        
        int numMaterials = -1;
        if (materials != null) numMaterials = materials.size();
        else if (minDate == null) {
            logger.debug("getAnnotations called. materials and minDate were both null. Returning all annotations.");
            return new ArrayList(participant.getAnnotations());
        }
        
        logger.debug("getAnnotations called. participant = " + participant.getUser().getFullName() + " materials set contains " + numMaterials + " (-1 = null), and date is " + minDate);
        
        // in case materials was empty, return empty list:
        if (numMaterials == 0) return new ArrayList();
        
        String queryString = "from Annotation a where a.participant = ?";
        
        List paramList = new ArrayList();
        paramList.add(participant);
        
        if (minDate != null) {
            queryString += " and a.dateModified >= ?";
            paramList.add(minDate);
        }
        
        if (numMaterials > 0) {
            
            queryString += " and a.material in (";
            
            for (int i=0; i< numMaterials; i++) {
                
                if (i != numMaterials-1) queryString += "?,";
                else queryString += "?";
            }
            queryString += ")";
            
            paramList.addAll(materials);
            
        }
        
        Object[] params = paramList.toArray();
        logger.debug("HQL query string is: " + queryString);
        
        List list = getHibernateTemplate().find(queryString, params);
        return list;
    }
    
    public List findByPropertyValueSubstring(Class objClass, String propertyName, String substring) throws DataRetrievalFailureException {
        /*
        Allows queries of the type:
        select first_name from ucm_users where lower(first_name) like lower('%z%');
        */
        String className = objClass.getName();
        //logger.debug("findByPropertyValueSubstring called..");
        String queryString = "from " + className + " x where lower( x." + propertyName + ") like ?";
        //logger.debug("HQL query string is: " + queryString);
        List list = getHibernateTemplate().find(queryString, new Object[] { "%" + substring.toLowerCase() + "%" } );
        //logger.debug (list.toString());
        return list;
        
    }
    
    
    
    
    /** COMMENTED OUT BECAUSE BATCH-FETCHING PRETTY MUCH MAKES UP FOR THIS
     * Use to initialize a collection for a collection of items. Example: You have a collection of Assignments
     * and you want to initialize all of their Questions.
     * You would call: initCollections(Assignment.class, myAssignments, Question.class), and it would return a Set
     * containing each Question that belonged to each Assignment, with no duplicates.
     * This is a performance-improved replacement for Vital3DAO.initCollections.
     *@param parentClass          The class of each item in the "items" collection.
     *@param items                A collection of items, each of the class itemClass.
     *@param childClass           The class of each item in the collection to be initialized.
     */
    /*public List getAllCollections(Class parentClass, Collection items, String collectionName, Class childClass) throws DataRetrievalFailureException {
        
        String parentName = Vital3Utils.getSimpleName(parentClass);
        
        Set parentSet = null;
        if (items instanceof Set) parentSet = (Set) items;
        else parentSet = new HashSet(items);
        
        List results = findBySetOfPropertyValues(childClass, parentName, parentSet);
        
        // even though we fetched all the records, the collections themselves must be initialized!!!
        Vital3Utils.initCollections(parentClass, items, collectionName, childClass);
        
        return results;
        
    }*/
    
    
    //// ENABLED GETTER/SETTER: use this to test data-access-failure-handling
    public boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /*************************************************
    **                PRIVATE METHODS               **
    *************************************************/

    // Use this for testing data retrieval failure:
    private void failIfNotEnabled() throws DataRetrievalFailureException {
        
        if (!this.enabled) throw new DataRetrievalFailureException("Data Access Disabled For Testing Purposes");
    }
    
    
    /*************************************************
    **              EXPERIMENTAL METHODS            **
    *************************************************/
    
    /**
     * Experimental! Use at your own risk! (in other words, don't use it!)
     */
   /* public List findAllPaginated(int beginIndex, int maxResults) {
        
        List list = null;
        Session session = null;
        try {
            session = getSession();
            
            Criteria searchCriteria = session.createCriteria(VitalWorksite.class).setFirstResult(beginIndex).setMaxResults(maxResults);
            list = searchCriteria.list();
            
        } catch(HibernateException he) {
            throw new DataRetrievalFailureException("Could not perform fetch", he);
        } finally {
            releaseSession(session);
        }
        return list;
    }*/
    
}
