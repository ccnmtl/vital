package ccnmtl.vital3.ucm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 *   This class represents a course offered at an institution. It is used
 *   exclusively within the UCM package.
 * 	@author Eric Mattes
 * 		
*/
public class RawUCMCourse implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "course";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String courseIdString;
    /** persistent field */
    private Set worksiteAffils;

    /** full constructor */
    public RawUCMCourse(String courseIdString, Set worksiteAffils) {
        this.courseIdString = courseIdString;
        this.worksiteAffils = worksiteAffils;
    }

    /** default constructor */
    public RawUCMCourse() {
    }

    /** convenient constructor **/
    public RawUCMCourse(String courseIdString) {
        this(courseIdString, new HashSet());
    }
    
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        throw new UnsupportedOperationException("Method not supported for Raw classes.");
    }
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The institution-unique id string for this course. Should be identical to or uniquely
     *             and easily derivable from course id strings which come in from the registrar feed.
     */
    public String getCourseIdString() {
        return this.courseIdString;
    }
    public void setCourseIdString(String courseIdString) {
        this.courseIdString = courseIdString;
    }

    /** 
     * The affiliations which signify that members of this course are allowed access to
     *             a particular worksite.
     */
    public Set getWorksiteAffils() {
        return this.worksiteAffils;
    }
    public void setWorksiteAffils(Set worksiteAffils) {
        this.worksiteAffils = worksiteAffils;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new RawUCMCourse[]{ this } );
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        Vital3Utils.removeMultipleFromCollections(worksiteAffils);
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(worksiteAffils);
        return children;
    }
    
}
