package ccnmtl.vital3;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;

/** 
 * 		Assigns a material to a particular unit. Typically created by the faculty member to designate certain materials
 *      as relevant to an entire unit, rather than any particular assignment in that unit.
 * 		@author Eddie Rubeiz
 * 		
*/
public class UnitMaterialAssoc implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "unitMaterialAssoc";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Material material;

    /** nullable persistent field */
    private Unit unit;

    /** full constructor */
    public UnitMaterialAssoc(Material material, Unit unit) {
        this.material = material;
        this.unit = unit;
        
        material.getUnitAssociations().add(this);
        unit.getMaterialAssociations().add(this);
    }

    /** default constructor */
    public UnitMaterialAssoc() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return unit.getRelatedWorksite();
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The Material
     */
    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    /** 
     * The Unit the material is part of.
     */
    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (material != null) {
            Set parentCollection = material.getUnitAssociations();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (unit != null) {
            Set parentCollection = unit.getMaterialAssociations();
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
