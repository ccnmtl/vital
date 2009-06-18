package ccnmtl.vital3.ucm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.VitalWorksite;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 *             This class represents a study term at an institution. It is used
 *             exclusively within the UCM package.
 * 			@author Eric Mattes
 * 		
*/
public class RawUCMTerm implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "term";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private Date endDate;
    /** nullable persistent field */
    private String name;
    /** nullable persistent field */
    private Date startDate;
    /** persistent field */
    private Set worksites;

    /** full constructor */
    public RawUCMTerm(Date endDate, String name, Date startDate, Set worksites) {
        this.endDate = endDate;
        this.name = name;
        this.startDate = startDate;
        this.worksites = worksites;
    }

    /** default constructor */
    public RawUCMTerm() {
    }
    
    /** convenience constructor */
    public RawUCMTerm(Date endDate, String name, Date startDate) {
        this(endDate, name, startDate, new HashSet());
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
     * The date at which the term ends.
     */
    public Date getEndDate() {
        return this.endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /** 
     * The name of this term, e.g. "Fall 2006".
     */
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /** 
     * The date at which the term starts.
     */
    public Date getStartDate() {
        return this.startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /** 
     * The worksites which take place during this term.
     */
    public Set getWorksites() {
        return this.worksites;
    }
    public void setWorksites(Set worksites) {
        this.worksites = worksites;
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new RawUCMTerm[]{ this } );
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
        
        Vital3Utils.removeMultipleFromCollections(worksites);
    }
    
    /**
        * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(worksites);
        return children;
    }

}
