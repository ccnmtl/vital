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
 *     This class represents the relationship between a RawUCMUser and a RawUCMWorksite, outside of any specific context of use.
 *     It is used exclusively within the UCM package.
*/
public class RawUCMParticipant implements Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "participant";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String participantIdString;
    /** nullable persistent field */
    private RawUCMUser user;
    /** nullable persistent field */
    private RawUCMWorksite worksite;

    /** full constructor */
    public RawUCMParticipant(String participantIdString, RawUCMUser user, RawUCMWorksite worksite) {
        this.participantIdString = participantIdString;
        this.user = user;
        this.worksite = worksite;
        
        user.getParticipants().add(this);
        worksite.getParticipants().add(this);
    }

    /** default constructor */
    public RawUCMParticipant() {
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
     * Uniquely identifies this participant record, and provides a link from Vital's internal participant record.
     */
    public String getParticipantIdString() {
        return this.participantIdString;
    }
    public void setParticipantIdString(String participantIdString) {
        this.participantIdString = participantIdString;
    }

    /** 
     * The user associated with this participant record.
     */
    public RawUCMUser getUser() {
        return this.user;
    }
    public void setUser(RawUCMUser user) {
        this.user = user;
    }

    /** 
     * The worksite associated with this participant record.
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
        return Arrays.asList( new RawUCMParticipant[]{ this } );
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (user != null) {
            Set participants = user.getParticipants();
            if (participants != null) participants.remove(this);
        }
        if (worksite != null) {
            Set participants = worksite.getParticipants();
            if (participants != null) participants.remove(this);
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
