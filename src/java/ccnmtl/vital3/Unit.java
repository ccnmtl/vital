package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;
import ccnmtl.utils.OmniComparator;


/** 
 * 			A subset of a Vital Worksite that contains zero or more Vital Assignments. It typically refers to a portion of the course's syllabus.
 * 			@author Eddie Rubeiz
 * 		
*/
public class Unit implements Comparable, Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "unit";

    // values for "visibility"
    public static final Integer INVISIBLE = new Integer(0);
    public static final Integer VISIBLE = new Integer(1);
    
    /** identifier field */
    private Long id;
    
    /** nullable persistent field */
    private VitalWorksite worksite;
    
    /** nullable persistent field */
    private String title;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private Date startDate;

    /** nullable persistent field */
    private Date endDate;

    /** nullable persistent field */
    private Integer visibility;

    /** persistent field */
    private Set assignments;

    /** persistent field */
    private Set materialAssociations;

    /** full constructor */
    public Unit(VitalWorksite worksite, String title, String description, Date startDate, Date endDate, Integer visibility, Set assignments, Set materialAssociations) {
        this.worksite = worksite;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visibility = visibility;
        this.assignments = assignments;
        this.materialAssociations = materialAssociations;
        
        worksite.getUnits().add(this);
    }

    /** default constructor */
    public Unit() {
    }
    
    /** convenient constructor for easy manual creation */
    public Unit(VitalWorksite worksite, String description, Date endDate, Date startDate, String title, int visibility) {
        
        this(worksite, title, description, startDate, endDate, new Integer(visibility), new TreeSet(), new HashSet());
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return worksite;
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The title of the unit.
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** 
     * A description of the unit.
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** 
     * The start date for this Unit.
     */
    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /** 
     * The end date for this Unit.
     */
    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /** 
     */
    public Integer getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    /** 
     * The Worksite this Unit is part of.
     */
    public VitalWorksite getWorksite() {
        return this.worksite;
    }

    public void setWorksite(VitalWorksite worksite) {
        this.worksite = worksite;
    }

    /** 
     * Assigments that form the unit.
     */
    public Set getAssignments() {
        return this.assignments;
    }

    public void setAssignments(Set assignments) {
        this.assignments = assignments;
    }

    /** 
     * Materials associated with this unit. They're not specifically tied to any one assignment.
     */
    public Set getMaterialAssociations() {
        return this.materialAssociations;
    }

    public void setMaterialAssociations(Set materialAssociations) {
        this.materialAssociations = materialAssociations;
    }

    /**
     * Returns size of materials collection (used for showing/hiding blocks of HTML in template)
     */
    public int getNumberOfMaterials() {
        return this.materialAssociations.size();
    }


    /**
     * Returns associated unit-materials
     */
    public Set getMaterials() {

        Set materials = new HashSet();
        Iterator iter = this.materialAssociations.iterator();
        while(iter.hasNext()) {
            UnitMaterialAssoc uma = (UnitMaterialAssoc) iter.next();
            materials.add(uma.getMaterial());
        }  
        return materials;
    }
    
    
    /**
     * Returns sorted materials
     * @return A list of sorted materials
     */ 
     public ArrayList getSortedMaterials() {

       ArrayList sortedMaterials = new ArrayList(getMaterials());
       
       // sorted by material title   
       OmniComparator materialComp = new OmniComparator(Material.class, "getTitle", null);  
       Collections.sort(sortedMaterials, materialComp);

       return sortedMaterials;	
    }
    

    /**
     * Returns associated assignment-materials
     */
    public Set getAssignmentMaterials() {
        
        Set result = new TreeSet();
        Iterator assIter = this.assignments.iterator();
        while(assIter.hasNext()) {
            
            Assignment assignment = (Assignment) assIter.next();
            
            Iterator assMatIter = assignment.getMaterialAssociations().iterator();
            while (assMatIter.hasNext()) {
                
                AssignmentMaterialAssoc ama = (AssignmentMaterialAssoc) assMatIter.next();
                result.add(ama.getMaterial());
            }
        }
        
        return result;
    }

    
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    // defines the natural order based on startDate property
    public int compareTo(Object obj) {
        Unit o = (Unit)obj;
        return startDate.compareTo(o.startDate);
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (worksite != null) {
            Set parentCollection = worksite.getUnits();
            if (parentCollection != null) parentCollection.remove(this);
        }
        Vital3Utils.removeMultipleFromCollections(getAllPersistableChildren());
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(assignments);
        children.addAll(materialAssociations);
        return children;
    }

}
