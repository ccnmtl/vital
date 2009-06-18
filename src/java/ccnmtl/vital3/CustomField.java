package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.utils.Ordinal;
import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
* A custom metadata field for Materials. Custom fields are per-worksite, and are connected to Materials through CustomFieldValues,
* which hold the value of each field for each corresponding Material. The CustomField itself does not contain the field value,
* that is what a CustomFieldValue is for.
* @author Eric Mattes
* 		
*/
public class CustomField implements Comparable, Ordinal, Persistable, Serializable {
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "customField";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String name;
    /** nullable persistent field */
    private Integer ordinalValue;
    /** nullable persistent field */
    private Integer visibility;
    /** nullable persistent field */
    private VitalWorksite worksite;
    /** persistent field */
    private Set values;
    
    /** full constructor */
    public CustomField(VitalWorksite worksite, String name, Integer ordinalValue, Integer visibility, Set values) {
        this.worksite = worksite;
        this.name = name;
        this.ordinalValue = ordinalValue;
        this.visibility = visibility;
        this.values = values;
        
        worksite.getCustomFields().add(this);
    }
    
    /** convenient constructor: designed for manual CustomField creation. **/
    public CustomField(VitalWorksite worksite, String name, int ordinalValue, int visibility) {
        
        this(worksite, name, new Integer(ordinalValue), new Integer(visibility), new HashSet());
    }
    
    /** default constructor */
    public CustomField() {
    }
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return worksite;
    }
    
    
    /**
     * Will change the ordinalValue property of this customField and all of its customFieldValues.
     * The regular setOrdinalValue method does not propagate this value to the customFieldValues, so
     * you should always use this method instead. When you save this object via the Vital3DAO, you should also
     * save its customFieldValues!
     */
    public void changeOrdinalValue(int newValue) {
        this.setOrdinalValue(new Integer(newValue));
        Iterator valueIter = values.iterator();
        while (valueIter.hasNext()) {
            CustomFieldValue value = (CustomFieldValue) valueIter.next();
            value.setOrdinalValue(new Integer(newValue));
        }
    }
    
    
    ////// STANDARD GETTERS AND SETTERS ///////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    /** 
     * The name of this custom field.
     */
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /** 
     * An integer value used for ordering the custom fields for display.
     */
    public Integer getOrdinalValue() {
        return this.ordinalValue;
    }
    public void setOrdinalValue(Integer ordinalValue) {
        
        if (this.ordinalValue != null && !this.ordinalValue.equals(ordinalValue) && this.worksite != null) {
            
            // must remove and re-insert into parent collection to maintain order... this is a problem inherent with sorted sets:
            Set parentSet = this.worksite.getCustomFields();
            boolean found = parentSet.remove(this);
            this.ordinalValue = ordinalValue;
            if (found) parentSet.add(this);
            
        } else this.ordinalValue = ordinalValue;
    }
    
    /** 
     * A binary integer which reflects whether or not the field should be visible to non-administrative
     * users.
     */
    public Integer getVisibility() {
        return this.visibility;
    }
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    /** 
     * The Worksite in which this CustomField is used.
     */
    public VitalWorksite getWorksite() {
        return this.worksite;
    }
    public void setWorksite(VitalWorksite worksite) {
        this.worksite = worksite;
    }
    
    /** 
     * All CustomFieldValues for this custom field. Each CustomFieldValue corresponds to a particular Material.
     */
    public Set getValues() {
        return this.values;
    }
    public void setValues(Set values) {
        this.values = values;
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new CustomField[]{ this } );
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("name", getName()).toString();
    }
    
    // defines the natural order based on ordinalValue property
    public int compareTo(Object obj) {
        CustomField o = (CustomField)obj;
        return ordinalValue.compareTo(o.ordinalValue);
    }
    
    /**
     * Clones the object, but all collections and references to related entities are made null in the clone.
     * String, Integer, Long are all immutable and may be reference-copied. Date, however, is cloned.
     *
    public Object clone() {
        try {
            CustomField result = (CustomField) super.clone();
            result.worksite = null;
            result.values = null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Cloning error");
        }
    }*/
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (worksite != null) {
            Set parentCollection = worksite.getCustomFields();
            if (parentCollection != null) parentCollection.remove(this);
        }
        Vital3Utils.removeMultipleFromCollections(values);
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(values);
        return children;
    }
    
}
