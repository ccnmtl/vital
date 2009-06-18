package ccnmtl.vital3.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.RawWrapper;

/**
 * MockDBTable was designed for unit-testing in Vital3 to simulate a basic database table.
 * #### Each table has its own Id generator, which begins at 1. ####
 * MockDBTable is a very simple table-simulator, which attempts nothing other than basic storage and retrieval.
 **/

public class MockDBTable {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    // items simulates a database table of Persistable items
    private List items;
    
    // this stuff simulates an id-generator:
    private long highestId = 0;
    private Long getNextId() { return new Long(++highestId); }
    private Long getLastId() { return new Long(highestId); }
    protected void setNextId(Long id) { highestId = id.longValue()-1; }
    
    // which Class does this MockDBTable represent?
    protected Class itemClass;
    
    // constructor:
    protected MockDBTable(Class itemClass){
        this.items = new ArrayList();
        this.itemClass = itemClass;
    }
    
    // reset UCM-related properties. This is necessary to simulate multiple requests in your test.
    // because normally, the properties which are set up after ucm decoration are thrown in the garbage
    // after a request ends, in your test code, you must call this so that the decorated classes do not
    // remain decorated after a request has 'completed'.
    // NOTE: Only use this on RawWrapper classes!
    protected void resetUCM() {
        
        ListIterator iter = items.listIterator();
        while (iter.hasNext()) {
            RawWrapper wrapper = (RawWrapper) iter.next();
            wrapper.resetUCM();
        }
    }
    
    // setter and getter for the items list:
    protected void setItems(List items) {
        this.items = items;
    }
    protected List getItems() {
        return this.items;
    }
    
    
    /**
    * Simulates an insert, returns the item's new id
    **/
    protected Long insert(Persistable item) throws Exception {

        Long newId = getNextId();
        item.setId(newId);
        items.add(item);
        return newId;
    }
    
    /**
    * Simulates an update
    **/
    protected void update(Persistable item) throws Exception {
        
        // updates don't even need to be specified, they happen automatically
        if (!items.contains(item)) throw new Exception("Tried to update item, but it was not found");
    }
    
    /**
    * Simulates a delete
    **/
    protected void delete(Persistable item) throws Exception {
        
        if (!items.remove(item)) throw new Exception("Tried to delete item, but it was not found");
    }
    
    /**
     * Simulates a multiple delete
     **/
    protected void deleteCollection(Collection collection) throws Exception {
        
        if (!items.removeAll(collection)) throw new Exception("Tried to delete items, but they were not found");
    }
    
    /**
    * Simulates a select-by-id, returns a single item (or null if not found).
    **/
    protected Persistable selectById(Long id) {
        
		//if (id == null) logger.debug("Looking for a null id in table for " + itemClass);
        if (id == null) throw new NullPointerException("Cannot select null id");
        Persistable item = null;
        ListIterator iter = items.listIterator();
        while (iter.hasNext()) {
            item = (Persistable)iter.next();
            if (item.getId().equals(id)) return item;
        }
        return null;
    }
    
    /**
    * Simulates a select with a single-item where clause.
    *
    *@param getter    The property-getter method.
    *@param value     A value you are looking for.
    *@return          A List of items which, when the getter method was called, returned an object equal to the value you passed.
    *                 If no items matched, an empty List will be returned.
    **/
    protected List selectByPropertyValue(Method getter, Object value) {
        List list = new ArrayList();		        
        ListIterator iter = items.listIterator();
        try {

            while (iter.hasNext()) {
                Object item = iter.next();
                if (value.equals(getter.invoke(item, null))) {
                    list.add(item);
                }
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        return list;
    }
    
    /**
     * Simulates a select with a two-item "AND" where clause.
     *
     *@param getter1    The first property-getter method.
     *@param value1     A value you are looking for.
     *@param getter2    The second property-getter method.
     *@param value2     A value you are looking for.
     *@return          A List of items which, when the getter methods were called, both returned an object equal to the values you passed.
     *                 If no items matched, an empty List will be returned.
     **/
    protected List selectByTwoPropertyValues(Method getter1, Object value1, Method getter2, Object value2) {
        List list = new ArrayList();
        
        ListIterator iter = items.listIterator();
        try {
            while (iter.hasNext()) {
                Object item = iter.next();
                if (value1.equals(getter1.invoke(item, null)) && value2.equals(getter2.invoke(item, null))) {
                    list.add(item);
                }
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        return list;
    }
    
    
    
    //
    protected List selectBySetOfPropertyValues(Method getter, Collection values) {
        List list = new ArrayList();		        
        ListIterator iter = items.listIterator();
        try {
            
            while (iter.hasNext()) {
                Object item = iter.next();
                if (values.contains(getter.invoke(item, null))) {
                    list.add(item);
                }
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        return list;
        
    }
    
    
    /**
     * For finding foreign-keyed objects.
     *
     *@param getter    The property-getter method for retrieving each item's foreign key (a Persistable Object).
     *@param id        The id you are looking for.
     *@return          A List of items which, when the getter method was called, returned an object whose id was equal to 
     *                 The one you passed. If no items matched, an empty List will be returned.
     **/
    protected List selectByForeignKeyId(Method getter, Long id) {
        List list = new ArrayList();
        
        ListIterator iter = items.listIterator();
        try {
            while (iter.hasNext()) {
                Persistable item = (Persistable) iter.next();
                Persistable fkItem = (Persistable) getter.invoke(item, null);
                
                // throw exception if it was null. by doing this we are basically forbidding null values in foreign key columns. Is this a bad idea?
                if (fkItem == null) throw new RuntimeException(itemClass.getName() + "." + getter.getName() + 
                                                               "() returned null. Couldn't search for foreign key relationship on item id " +
                                                               item.getId());
                
                if (id.equals(fkItem.getId())) {
                    list.add(item);
                }
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        return list;
    }
    
    /**
    * Simulates a select-all: returns the entire List of items, even if it is empty
    **/
    protected List selectAll() {
        
        return items;
    }
    
    
}


