package ccnmtl.vital3;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;

/** 
 * 		Assigns a material to an assignment.
 * 		@author Eddie Rubeiz
 * 		
*/
public class AssignmentMaterialAssoc implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "assignmentMaterialAssoc";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Material material;

    /** nullable persistent field */
    private Assignment assignment;

    /** full constructor */
    public AssignmentMaterialAssoc(Assignment assignment, Material material) {        
        this.assignment = assignment;
        this.material = material;
        
        assignment.getMaterialAssociations().add(this);
        material.getAssignmentAssociations().add(this);
    }

    /** default constructor */
    public AssignmentMaterialAssoc() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return material.getRelatedWorksite();
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
     * The Assignment the material is part of.
     */
    public Assignment getAssignment() {
        return this.assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
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
        
        if (assignment != null) {
            Set parentCollection = assignment.getMaterialAssociations();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (material != null) {
            Set parentCollection = material.getAssignmentAssociations();
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
