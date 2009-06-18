package ccnmtl.vital3.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MockDB was designed for unit-testing in Vital3 to simulate a simple database.
 * MockDB saves things into "persistence" by reference, so pretend that all modification and
 * all persistence operations are flushed immediately! So far there is no support for rollback.
 * IMPORTANT: Note that Hibernate uses transactions and MockDBTable does not! Be careful!
 **/

public class MockDB {
    
    // HashMap for the tables in this database. Class -> MockDBTable.
    private HashMap tables = new HashMap();
    

    protected final Log logger = LogFactory.getLog(getClass());

    
    // reset UCM-related properties. This is necessary to simulate multiple requests in your test.
    // because normally, the properties which are set up after ucm decoration are thrown in the garbage
    // after a request ends, in your test code, you must call this so that the decorated classes do not
    // remain decorated after a request has 'completed'.
    protected void resetUCM() {
        try {
            getTable(VitalUser.class).resetUCM();
            getTable(VitalParticipant.class).resetUCM();
            getTable(VitalWorksite.class).resetUCM();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Create a table for a particular class
     */
    public void createTable(Class itemClass) {
        tables.put(itemClass, new MockDBTable(itemClass));
    }
    
    /**
     * Simulates hibernate's saveOrUpdate behavior (assuming that the 'unsaved-value' for the hibernate mapping is 'null', the default)
     **/
    public void saveOrUpdate(Class itemClass, Persistable item) throws Exception{
        
        if (item == null) throw new NullPointerException("Cannot save null item");
        Long id = item.getId();
        if (id==null){
            save(itemClass, item);
        } else {
            update(itemClass, item);
        }
    }
    
    /**
     * Simulates a save/insert. Your item will inherit its new id.
     **/
    public void save(Class itemClass, Persistable item) throws Exception {
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        Long newId = table.insert(item);
        item.setId(newId);
    }
    
    /**
     * Simulates an update
     **/
    public void update(Class itemClass, Persistable item) throws Exception {
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        table.update(item);
    }
    
    /**
     * Simulates a delete
     **/
    public void delete(Class itemClass, Persistable item) throws Exception {
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        table.delete(item);
    }
    
    /**
     * Simulates a multiple delete
     **/
    public void deleteCollection(Class itemClass, Collection collection) throws Exception {
        
        if (collection.size() == 0) throw new RuntimeException("Collection was empty!");
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        table.deleteCollection(collection);
    }
    
    /**
     * Simulates a find-by-id, returns a single item (or null if not found).
     **/
    public Object findById(Class itemClass, Long id) {
        
        if (id == null) throw new NullPointerException("Cannot select null id");
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        Persistable item = table.selectById(id);
        
        if (item == null) return null;
        return item;
    }
    
    /**
     * Simulates a select with a single-item where clause.
     * ######### Incredibly inefficient!!!
     *
     *@param propertyName The name of the property (Method will be obtained via reflection).
     *@param value A value you are looking for.
     *@return A List of items which, when the getter method was called, returned an object equal to the value you passed.
     *        If no items matched, an empty List will be returned.
     **/
    public List findByPropertyValue(Class itemClass, String propertyName, Object value) throws Exception{
        
        Method getter = Vital3Utils.getterForProperty(itemClass, propertyName);
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        
        return table.selectByPropertyValue(getter, value);
    }
    
    /**
     * Simulates a select with a two-item "AND" where clause.
     * ######### Incredibly inefficient!!!
     *
     *@param propertyName1 The name of the first property (Method will be obtained via reflection).
     *@param value1 A value you are looking for.
     *@param propertyName2 The name of the second property (Method will be obtained via reflection).
     *@param value2 A value you are looking for.
     *@return A List of items which, when the getter methods were called, both returned an object equal to the values you passed.
     *        If no items matched, an empty List will be returned.
     **/
    public List findByTwoPropertyValues(Class itemClass, String propertyName1, Object value1, String propertyName2, Object value2) throws Exception {
        
        Method getter1 = Vital3Utils.getterForProperty(itemClass, propertyName1);
        Method getter2 = Vital3Utils.getterForProperty(itemClass, propertyName2);
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        
        return table.selectByTwoPropertyValues(getter1, value1, getter2, value2);
    }
    
    
    //
    public List findBySetOfPropertyValues(Class itemClass, String propertyName, Collection values) throws Exception {
        
        Method getter = Vital3Utils.getterForProperty(itemClass, propertyName);
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        
        return table.selectBySetOfPropertyValues(getter, values);
    }
    
    
    /**
     * Simulates a find-all: returns all items in a table
     * ######### Incredibly inefficient!!!
     **/
    public List findAll(Class itemClass) {
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        
        return table.selectAll();
    }
    
    /**
     * For finding all children of an item (initializing collections).
     *@param item            the item whose children you want to find.
     *@param relatedClass    the class of the child objects you want to find.
     *@param getterName      name of the method which will retrieve each object's parent.
     */
    public List findRelated(Persistable item, Class relatedClass, String getterName) {
        
        // prepare getter method
        Method getter = null;
        try {
            getter = relatedClass.getMethod(getterName, null);
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("No such Method!", e);
        }
        
        return findRelated(item, relatedClass, getter);
    }
    
    // package-internal method for finding related items. Also used by Vital3MockDAO.
    protected List findRelated(Persistable item, Class relatedClass, Method getter) {
        
        // get the table for the relatedClass
        MockDBTable table = (MockDBTable) tables.get(relatedClass);
        
        Long id = item.getId();
        if (id == null) throw new RuntimeException("Item had no id (null)! Could not locate in DB");
        
        // find the related objects!
        return table.selectByForeignKeyId(getter, id);
    }
    
    /************ PRIVATES **************/
    
    // retrieves an item out of a table without modifying it
    private Persistable getPersistedReference(Class itemClass, Persistable obj, boolean required) {
        
        MockDBTable table = (MockDBTable) tables.get(itemClass);
        Long id = obj.getId();
        Persistable result = table.selectById(id);
        if (required && result == null) throw new RuntimeException("Persisted " + itemClass.getName() + " reference id " + id + " not found. Could not retrieve.");
        return result;
    }
    
    // internal method for getting a table:
    private MockDBTable getTable(Class tableClass) throws Exception {
        
        MockDBTable table = (MockDBTable) tables.get(tableClass);
        if (table == null) throw new Exception("requested nonexistant table '" + tableClass.getName() + "'");
        return table;
    }
}


