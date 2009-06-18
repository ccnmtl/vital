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
*  This class represents the raw essentials of a worksite, outside of any specific context of use. It is used
*  exclusively within the UCM package.
*@author Eric Mattes
*/
public class RawUCMWorksite implements Persistable, Serializable {
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "worksite";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String worksiteIdString;
    /** nullable persistent field */
    private RawUCMTerm term;
    /** nullable persistent field */
    private String title;
    /** persistent field */
    private Set courseAffils;
    /** persistent field */
    private Set participants;
    
    /** full constructor */
    public RawUCMWorksite(String worksiteIdString, RawUCMTerm term, String title, Set courseAffils, Set participants) {
        this.worksiteIdString = worksiteIdString;
        this.term = term;
        this.title = title;
        this.courseAffils = courseAffils;
        this.participants = participants;
        
        term.getWorksites().add(this);
    }
    
    /** default constructor */
    public RawUCMWorksite() {
    }
    
    /** convenience constructor for manual creation (with courseAffils) **/
    public RawUCMWorksite(String worksiteIdString, RawUCMTerm term, String title, Set courseAffils) {
        this(worksiteIdString, term, title, courseAffils, new HashSet());
    }
    /** convenience constructor for manual creation **/
    public RawUCMWorksite(String worksiteIdString, RawUCMTerm term, String title) {
        this(worksiteIdString, term, title, new HashSet(), new HashSet());
    }
    
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        throw new UnsupportedOperationException("Method not supported for Raw classes.");
    }
    
    ////// STANDARD GETTERS AND SETTERS //////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * A string which uniquely identifies this worksite.
     */
    public String getWorksiteIdString() {
        return this.worksiteIdString;
    }
    public void setWorksiteIdString(String worksiteIdString) {
        this.worksiteIdString = worksiteIdString;
    }
    
    /** 
     * The title of this worksite.
     */
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    /** 
     * The term during which this worksite takes place.
     */
    public RawUCMTerm getTerm() {
        return this.term;
    }
    public void setTerm(RawUCMTerm term) {
        this.term = term;
    }
    
    /** 
     * Associations with CourseWorksiteAffils.
     */
    public Set getCourseAffils() {
        return this.courseAffils;
    }
    public void setCourseAffils(Set courseAffils) {
        this.courseAffils = courseAffils;
    }
    
    /** 
     * Associations with Participants.
     */
    public Set getParticipants() {
        return this.participants;
    }
    public void setParticipants(Set participants) {
        this.participants = participants;
    }
    
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new RawUCMWorksite[]{ this } );
    }
    
    public String toString() {
        return new ToStringBuilder(this)
        .append("worksiteIdString", getWorksiteIdString())
        .append("title", getTitle())
        .toString();
    }
    
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (term != null) {
            Set worksites = term.getWorksites();
            if (worksites != null) worksites.remove(this);
        }
        Vital3Utils.removeMultipleFromCollections(getAllPersistableChildren());
    }
    
    /**
        * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(courseAffils);
        children.addAll(participants);
        return children;
    }
    
}