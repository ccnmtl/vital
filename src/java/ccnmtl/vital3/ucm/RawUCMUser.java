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
 *     This class represents a person in the context of the educational institution. It is used
 *     exclusively within the UCM package.
*/
public class RawUCMUser implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "user";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String userIdString;
    /** nullable persistent field */
    private String email;
    /** nullable persistent field */
    private String firstName;
    /** nullable persistent field */
    private String lastName;
    /** persistent field */
    private Set participants;

    /** full constructor */
    public RawUCMUser(String userIdString, String email, String firstName, String lastName, Set participants) {
        this.userIdString = userIdString;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.participants = participants;
    }

    /** default constructor */
    public RawUCMUser() {
    }
    
    /** convenient constructor **/
    public RawUCMUser(String userIdString, String email, String firstName, String lastName) {
        this(userIdString, email, firstName, lastName, new HashSet());
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
     * This person's institutional ID. At Columbia, this corresponds to the UNI.
     */
    public String getUserIdString() {
        return this.userIdString;
    }
    public void setUserIdString(String userIdString) {
        this.userIdString = userIdString;
    }

    /** 
    * This person's email address.
    */
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    
    /** 
     * This person's first name, as provided by the institution's records
     */
    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** 
     * This person's last name, as provided by the institution's records
     */
    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** 
     * Associations with Worksites.
     */
    public Set getParticipants() {
        return this.participants;
    }
    public void setParticipants(Set participants) {
        this.participants = participants;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new RawUCMUser[]{ this } );
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        Vital3Utils.removeMultipleFromCollections(participants);
    }
    
    /**
        * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(participants);
        return children;
    }

}
