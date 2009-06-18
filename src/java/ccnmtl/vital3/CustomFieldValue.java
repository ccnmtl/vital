package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 * Custom fields are connected to Materials through CustomFieldValues, which hold the value of each field for each corresponding
 * Material. The CustomField itself does not contain the field value, that is what a CustomFieldValue is for. When a Material is
 * created, this should be accompanied by the creation of a CustomFieldValue for each relevant CustomField. Likewise, when a
 * CustomField is created, this should be accompanied by the creation of a CustomFieldValue for every Material in the Worksite.
 * @author Eric Mattes
 * 		
*/
public class CustomFieldValue implements Comparable, Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "customFieldValue";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private CustomField customField;
    /** nullable persistent field */
    private Material material;
    /** nullable persistent field */
    private Integer ordinalValue;
    /** nullable persistent field */
    private String value;
    
    /** full constructor */
    public CustomFieldValue(CustomField customField, Material material, Integer ordinalValue, String value) {
        this.customField = customField;
        this.material = material;
        this.ordinalValue = ordinalValue;
        this.value = value;
        
        customField.getValues().add(this);
        material.getCustomFieldValues().add(this);
    }

    /** convenient constructor: designed for manual CustomFieldValue creation. **/
    public CustomFieldValue(CustomField customField, Material material, int ordinalValue, String value) {
        
        this(customField, material, new Integer(ordinalValue), value);        
    }
    
    /** default constructor */
    public CustomFieldValue() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return customField.getRelatedWorksite();
    }
    
    
    /**
     * Get the name of the CustomField to which this value belongs.
     */
    public String getName() {
        return customField.getName();
    }
    
    /**
     * For when a null value will be problematic, this will return empty-string instead.
     */
    public String getNonNullValue() {
        if (this.value == null) return "";
        else return this.value;
    }
    
    ////// STANDARD GETTERS AND SETTERS //////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The order of the custom field for this value.
     */
    public Integer getOrdinalValue() {
        return this.ordinalValue;
    }
    public void setOrdinalValue(Integer ordinalValue) {
        
        if (this.ordinalValue != null && !this.ordinalValue.equals(ordinalValue) && (this.customField != null || this.material != null)) {
            
            boolean found1 = false;
            boolean found2 = false;
            
            // must remove and re-insert into parent collection to maintain order... this is a problem inherent with sorted sets:
            if (this.customField != null)
                found1 = this.customField.getValues().remove(this);
            if (this.material != null)
                found2 = this.material.getCustomFieldValues().remove(this);
            
            this.ordinalValue = ordinalValue;
            
            if (found1) this.customField.getValues().add(this);
            if (found2) this.material.getCustomFieldValues().add(this);
            
        } else this.ordinalValue = ordinalValue;
        
    }

    /** 
     * The value of this custom field for the corresponding Material.
     */
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    /** 
     * The CustomField to which this value belongs.
     */
    public CustomField getCustomField() {
        return this.customField;
    }
    public void setCustomField(CustomField customField) {
        this.customField = customField;
    }

    /** 
     * The Material to which this value corresponds.
     */
    public Material getMaterial() {
        return this.material;
    }
    public void setMaterial(Material material) {
        this.material = material;
    }

    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new CustomFieldValue[]{ this } );
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("value", getValue()).toString();
    }

    
    // defines the natural order based on ordinalValue property
    public int compareTo(Object obj) {
        CustomFieldValue o = (CustomFieldValue)obj;
        return ordinalValue.compareTo(o.ordinalValue);
    }
    
    /**
     * Clones the object, but all collections and references to related entities are made null in the clone.
     * String, Integer, Long are all immutable and may be reference-copied. Date, however, is cloned.
     *
    public Object clone() {
        try {
            CustomFieldValue result = (CustomFieldValue) super.clone();
            result.customField = null;
            result.material = null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Cloning error");
        }
    }*/
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (customField != null) {
            Set parentCollection = customField.getValues();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (material != null) {
            Set parentCollection = material.getCustomFieldValues();
            if (parentCollection != null) parentCollection.remove(this);
        }
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        return children;
    }

}
