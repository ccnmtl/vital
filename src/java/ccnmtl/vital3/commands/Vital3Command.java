package ccnmtl.vital3.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.web.util.WebUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.VitalWorksite;

public class Vital3Command {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    /********* BASIC FACILITIES ****************/
    
    protected Long id;
    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    
    protected String action;
    public String getAction() { return action; }
    public void setAction(String action) { this.action=action; }
    
    protected String mode;
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode=mode; }
    
    
    /*********** MULTIPLE-ENTITY FACILITIES ***********/
    
    
    // these are for the entity ids:
    protected ArrayList ids;
    public ArrayList getIds() { return ids; }
    public void setIds(ArrayList values) { this.ids=values; }
    
    // these are for the entity instances:
    protected ArrayList entityList;
    public ArrayList getEntityList() { return entityList; }
    public void setEntityList(ArrayList list) { this.entityList = list; }
    
    
    
    /*********** DYNAMIC "PSEUDO-PROPERTIES" *************/
    // IMPORTANT: we keep the hardwired pojo properties for stuff that we won't ever use
    // in dynamic code. Only entity-type-related properties will be in the hashmap.
    // this hashmap might contain keys such as "material", "worksite", "assignment", etc which
    // map to the entity of the same name. It might also contains keys such as "materialId", "worksiteId",
    // and "assignmentId" which map to Long id values for those entities.
    // The getter and setter methods for these 'psuedo-properties' all utilize this map.
    // worksite and worksiteId are in this superclass because they are used in
    // Vital3CommandController's default security code.
    
    protected HashMap valueMap = new HashMap();
    
    public Object mapGet(String propertyName) {
        return valueMap.get(propertyName);
    }
    
    public void mapSet(String propertyName, Object value) {
        valueMap.put(propertyName, value);
    }
    
    
    // worksite instance:
    public VitalWorksite getWorksite() {
        return (VitalWorksite) mapGet("worksite"); 
    }
    public void setWorksite(VitalWorksite obj) {
        mapSet("worksite", obj);
    }
    
    // worksiteId:
    public Long getWorksiteId() {
        return (Long) mapGet("worksiteId");
    }
    public void setWorksiteId(Long value) {
		logger.debug ("Setting worksite ID to " + value);
        mapSet("worksiteId", value);
    }
    
    
    /*********** CHILD ENTITY STUFF *************/
    
    // NOTE: This was written long ago for Materials and their CustomFieldValues...
    //       It is confusing but it does the trick for now. Maybe in the future we can think of a better way...
    // child lists are for cases when the form contains information about an entity AND related entities
    // (e.g. Material and CustomFieldValues). "data" Lists contain HashMaps which hold the properties and values
    // from the form. Form fields should be named "childX-Y-Z" where:
    // X = an arbitrary number signifying an entity type;
    // Y = a number which uniquely identifies this particular entity among the others in the form (will be the list index);
    // Z = a property name of the entity.
    // For update forms, the id property must be on the form (hidden input).
    // "entities" lists contain the actual entities referenced by the data.
    // These two Lists are "parallel" (each index corresponds to the same thing in each)
    protected List child0data = new ArrayList();
    protected List child0Entities = new ArrayList();
    
    /**
     * Parses the request object for parameters corresponding to child entities.
     * So far, does not report any errors.
     */
    public void parseRequestForChildEntities(HttpServletRequest request, Errors errors) {
        // NOTE: This leaves a lot of room for performance improvements!!!
        Map parameterMap = WebUtils.getParametersStartingWith(request, "child0-");
        if (!parameterMap.isEmpty()) {
            List child0 = new ArrayList();
            // The map should be sorted (spring uses TreeMap)...
            // So iterate through them assuming the numbers will change
            Iterator iter = parameterMap.keySet().iterator();
            // instanceNumber is the second token in the field name:
            int instanceNumber = -99;
            Map instanceMap = null;
            while (iter.hasNext()) {
                String param = (String) iter.next();
                int dividerIndex = param.indexOf('-');
                String prefixNumber = param.substring(0,dividerIndex);
                String propertyName = param.substring(dividerIndex+1);
                int newInstanceNumber = Integer.parseInt(prefixNumber);
                if (instanceNumber != newInstanceNumber) {
                    instanceNumber = newInstanceNumber;
                    // add the old map to the list and create a new one
                    if (instanceMap != null) child0.add(instanceMap);
                    instanceMap = new HashMap();
                }
                // add the propertyname and value to the instance map
                String value = (String) parameterMap.get(param);
                if (value == null) {
                    System.out.println("value was null for param " + param);
                    value = "";
                }
                instanceMap.put(propertyName, value);
            }
            if (instanceMap != null) child0.add(instanceMap);
            // set the object:
            this.setChildData(0,child0);
        }
    }
    
    
    /**
     * Getter and setter for Child Data Lists
     * note: Spring thinks this is an actual property-getter
     */
    public List getChildData(int index) {
        if (index == 0) return child0data;
        else throw new RuntimeException("index was not zero and there is no other index supported!");
    }
    public void setChildData(int index, List listOfInstanceMaps) {
        if (index == 0) child0data = listOfInstanceMaps;
        else throw new RuntimeException("index was not zero and there is no other index supported!");
    }
    /**
     * Getters and setters for Child Entities Lists
     * note: Spring thinks this is an actual property-getter
     */
    public List getChildEntities(int index) {
        if (index == 0) return child0Entities;
        else throw new RuntimeException("index was not zero and there is no other index supported!");
    }
    public void setChildEntities(int index, Collection entities) {
        ArrayList entitiesList = new ArrayList(entities);
        if (index == 0) child0Entities = entitiesList;
        else throw new RuntimeException("index was not zero and there is no other index supported!");
    }
    
    /**
     * needed for springbind usage in velocity templates:
     */
    public List getChild0Entities() {
        return child0Entities;
    }
    
    
    /********** FILE UPLOADING (NOT USED) **************/
    
    /*
     protected byte[] file;
     
     public void setFile(byte[] file) { this.file = file; }
     public byte[] getFile() { return file; }
     */
    
    
}