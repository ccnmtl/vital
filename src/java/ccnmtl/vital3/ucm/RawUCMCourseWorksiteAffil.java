package ccnmtl.vital3.ucm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.utils.Persistable;

/** 
 *     This class represents that a worksite is open to access from members of the associated course.
 *     It is used exclusively within the UCM package.
 * 	@author Eric Mattes
 * 		
*/
public class RawUCMCourseWorksiteAffil implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "courseWorksiteAffil";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private RawUCMCourse course;
    /** nullable persistent field */
    private RawUCMWorksite worksite;

    /** full constructor */
    public RawUCMCourseWorksiteAffil(RawUCMCourse course, RawUCMWorksite worksite) {
        this.course = course;
        this.worksite = worksite;
        
        course.getWorksiteAffils().add(this);
        worksite.getCourseAffils().add(this);
    }

    /** default constructor */
    public RawUCMCourseWorksiteAffil() {
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
     * The course which is allowed access to the associated worksite.
     */
    public RawUCMCourse getCourse() {
        return this.course;
    }
    public void setCourse(RawUCMCourse course) {
        this.course = course;
    }

    /** 
     * The worksite which is open to the associated course's members.
     */
    public RawUCMWorksite getWorksite() {
        return this.worksite;
    }
    public void setWorksite(RawUCMWorksite worksite) {
        this.worksite = worksite;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new RawUCMCourseWorksiteAffil[]{ this } );
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (course != null) {
            Set worksiteAffils = course.getWorksiteAffils();
            if (worksiteAffils != null) worksiteAffils.remove(this);
        }
        if (worksite != null) {
            Set courseAffils = worksite.getCourseAffils();
            if (courseAffils != null) courseAffils.remove(this);
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
