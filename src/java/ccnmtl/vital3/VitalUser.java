package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.ucm.RawUCMUser;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.RawWrapper;

/** 
*    This class represents a person who uses Vital.
* 	@author Eric Mattes	
*/
public class VitalUser implements Persistable, RawWrapper, Serializable {
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "user";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String userIdString;
    /** nullable persistend field */
    private String authMethod;
    /** nullable persistend field */
    private Integer accessLevel;
    /** nullable persistent field */
    private String password;
    
    /** The raw user which this VitalUser object wraps. **/
    private RawUCMUser raw;
    // Set of VitalParticipants:
    private Set participants;
    
    /** convenient constructor: will construct a raw user and wrap it */
    public VitalUser(String userIdString, String authMethod, Integer accessLevel, String email, String password, String firstName, String lastName) {
        this(userIdString, authMethod, accessLevel, password);
        
        // create a raw and wrap it:
        RawUCMUser raw = new RawUCMUser(userIdString, email, firstName, lastName);
        this.wrap(raw);
        
        // give it a new empty set for participants:
        this.participants = new HashSet();
    }
    
    /** full constructor... DO NOT USE */
    public VitalUser(String userIdString, String authMethod, Integer accessLevel, String password) {
        this.userIdString = userIdString;
        this.authMethod = authMethod;
        this.accessLevel = accessLevel;
        this.password = password;
		this.participants = new HashSet();
    }
    
    // add a participant, without needing to fear the null collection:
    public void addParticipant(VitalParticipant vParticipant) {
        
        if (participants == null) participants = new HashSet();
        participants.add(vParticipant);
        
    }
    
    /** default constructor */
    public VitalUser() {
    }
    
    /**
     * Returns true if this.authMethod equals "vital" or if authMethod is null
     */
    public boolean usesVitalAuth() {
        
        return (this.authMethod == null || this.authMethod.equals("vital"));
    }
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        throw new UnsupportedOperationException("a VitalUser does not have a single resolvable related worksite");
    }
    
    
    /**
     * Gets the participant object connecting this user to a certain worksite. This user must have a
     * non-null participants set.
     */
    public VitalParticipant getParticipantForWorksiteIdString(String idString) {
        
        if (participants == null) throw new RuntimeException("Participants property is null on user " + userIdString);
        if (id == null) throw new RuntimeException("idString was null!");
        Iterator iter = participants.iterator();
        while (iter.hasNext()) {
            VitalParticipant vp = (VitalParticipant) iter.next();
            if (vp.getWorksite().getWorksiteIdString().equals(idString)) return vp;
        }
        return null;
    }
    
    /**
     * Gets the participant object connecting this user to a certain worksite (by VitalWorksite id). 
     * This user must have a non-null participants set.
     */
    public VitalParticipant getParticipantForWorksiteId(Long id) {
        
        if (participants == null) throw new RuntimeException("Participants property is null on user " + userIdString);
        if (id == null) throw new RuntimeException("id was null!");
        Iterator iter = participants.iterator();
        while (iter.hasNext()) {
            VitalParticipant vp = (VitalParticipant) iter.next();
            if (vp.getWorksite().getId().equals(id)) return vp;
        }
        return null;
    }
    
    
    /** 
    * The user's e-mail address
    */
    public String getEmail() {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot get email.");
        return raw.getEmail();
    }
    public void setEmail(String email) {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot set email.");
        raw.setEmail(email);
    }
    
    
    /** 
    * The user's first name
    */
    public String getFirstName() {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot get firstName.");
        return raw.getFirstName();
    }
    public void setFirstName(String firstName) {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot set firstName.");
        raw.setFirstName(firstName);
    }
    
    
    /** 
    * The user's last name
    */
    public String getLastName() {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot get lastName.");
        return raw.getLastName();
    }
    public void setLastName(String lastName) {
        if (raw == null) throw new RuntimeException("VitalUser did not contain a RawUCMUser. Cannot set lastName.");
        raw.setLastName(lastName);
    }
    
    /**
     * The user's first and then last names, separated by a space. Example: "Frank Moretti"
     */
    public String getFullName() {
        String fName = getFirstName();
        String lName = getLastName();
        if (fName == null || lName == null ) return getUserIdString();
        return (fName + " " + lName);
    }
    
    /**
     * The user's last and then first names, separated by a comma and a space. Example: "Moretti, Frank"
     */
    public String getFullNameReversed() {
        String fName = getFirstName();
        String lName = getLastName();
        if (fName == null || lName == null ) return getUserIdString();
        return (lName + ", " + fName);
    }
    
    /**
    * The VitalParticipants belonging to this VitalUser.
    */
    public Set getParticipants() {
        return this.participants;
    }
    public void setParticipants(Set participants) {
        this.participants = participants;
    }
    public boolean isParticipantsNull() {
        return (participants == null);
    }
    
    /////////// STANDARD GETTERS AND SETTERS ////////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    /** 
    * A string uniquely identifying this user at the institution or within vital. At columbia, it would be the uni.
    */
    public String getUserIdString() {
        return this.userIdString;
    }
    public void setUserIdString(String userIdString) {
        this.userIdString = userIdString;
        if (this.raw !=null) { raw.setUserIdString(userIdString); }
    }
    
    /**
     * How do we authenticate this user? "vital" for built-in method, and any other string for a different method.
     */
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }
    public String getAuthMethod() {
        return this.authMethod;
    }
    
    /**
    * The general access level for this user. This is typically null, but for admins it is UserCourseManager.ADMIN_ACCESS.
    * Your general access level is overridden by participant access levels for each worksite. Your general access level
    * is meant to apply to all worksites, so if you are ADMIN_ACCESS, you have that level of access to all worksites.
    * Even ones you do not have a participant record for. If you do have a participant record, your permissions are
    * unioned with the permissions from that participant record when you are doing things in that worksite.
    */
    public Integer getAccessLevel() {
        return this.accessLevel;
    }
    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    /**
     * Gets the user's access level to a worksite.
     */
    public String getAccessLevelLabelForWorksite(VitalWorksite worksite) {
		
        Integer userAccess = this.getAccessLevelForWorksite(worksite);
        if (userAccess == null) return null;
        return UserCourseManager.getLabelForAccessLevel(userAccess);
    }
    
    
    /**
     * Gets the user's access level to a worksite.
     */
    public Integer getAccessLevelForWorksite(VitalWorksite worksite) {
		if (worksite == null) throw new RuntimeException("Null worksite passed.");
		String worksiteIdString = worksite.getWorksiteIdString();
		if (this.getParticipants() == null) throw new RuntimeException("Participants not initialized");
		Iterator participantIter = this.getParticipants().iterator();
		while (participantIter.hasNext()) {
			VitalParticipant vParticipant = (VitalParticipant) participantIter.next();
			if ((vParticipant.getWorksite().getWorksiteIdString()).equals(worksiteIdString)) {
				return vParticipant.getAccessLevel();
			}
		}
        // did not find participant... check for user's sitewide access level:
        Integer userAccess = this.getAccessLevel();
        if (userAccess != null && userAccess.compareTo(UserCourseManager.PUBLIC_ACCESS) > 0)
            return userAccess;
        return null;
    }
    
    
    /** 
    * The vital password for this non-institutional user. This is for people without unis.
    */
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Set the raw user object.
     */
    public void setRaw(RawUCMUser raw) {
        this.raw = raw;
    }
    public RawUCMUser getRaw() {
        return this.raw;
    }
    
    /**
     * For generic collections operations. Implemented for RawWrapper compliance.
     */
    public Object getRawObject() {
        return this.raw;
    }
    
    /**
     * Wrap the given Raw object.
     *@param raw         the RawUCMUser object to wrap.
     */
    public void wrap(RawUCMUser raw) {
        this.raw = raw;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
        .append("id", getId())
        .toString();
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new VitalUser[]{ this } );
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        // nothing
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        return children;
    }
    
    /**
     * Implemented for MockDB
     */
    public void resetUCM() {
        this.raw = null;
        this.participants = null;
    }
    
}
