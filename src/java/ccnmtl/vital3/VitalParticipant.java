package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.ucm.RawUCMParticipant;
import ccnmtl.vital3.ucm.RawUCMUser;
import ccnmtl.vital3.ucm.RawUCMWorksite;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.RawWrapper;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
*   This class represents the relationship between a VitalUser and a VitalWorksite. It is tied to all classes
*   which represent something belonging to a user in a worksite, and it stores the user's access level for that worksite.
* 	@author Eric Mattes
* 		
*/
public class VitalParticipant implements Persistable, RawWrapper, Serializable {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "participant";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String participantIdString;
    /** nullable persistent field */
    private Integer accessLevel;
    /** persistent field */
    private Set annotations;
    /** persistent field */
    private Set assignmentResponses;
    /** persistent field */
    private Set comments;
    
    /** The raw participant which this VitalParticipant object wraps. **/
    private RawUCMParticipant raw;
    // vital version of raw's user:
    private VitalUser user;
    // vital version of raw's worksite:
    private VitalWorksite worksite;
    
    
    /** full constructor */
    public VitalParticipant(String participantIdString, Integer accessLevel, Set annotations, Set assignmentResponses, Set comments) {
        this.participantIdString = participantIdString;
        this.accessLevel = accessLevel;
        this.annotations = annotations;
        this.assignmentResponses = assignmentResponses;
        this.comments = comments;
	}
    
    /** default constructor */
    public VitalParticipant() {
    }
    
    /** convenient constructor: will construct and wrap a RawUCMParticipant. **/
    public VitalParticipant(String participantIdString, Integer accessLevel, VitalUser user, VitalWorksite worksite) {
        this(participantIdString, accessLevel, new HashSet(), new HashSet(), new HashSet());
        
        // construct raw and wrap:
        RawUCMParticipant raw = new RawUCMParticipant(participantIdString, user.getRaw(), worksite.getRaw());
        this.wrap(raw, user, worksite);
        
        user.getParticipants().add(this);
        worksite.getParticipants().add(this);
    }
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        if (worksite == null) throw new RuntimeException("worksite was null. You need to decorate this participant.");
        return worksite;
    }
    
    /**
     * Use this to update the user property, and it will be transfered to the raw participant too
     */
    public void updateUser(VitalUser newUser) {
        
        RawUCMUser newRawUser = newUser.getRaw();
        if (newRawUser == null) throw new RuntimeException("user must be decorated!");
        
        // update the original raw user:
        RawUCMUser originalRawUser = this.user.getRaw();
        originalRawUser.getParticipants().remove(this.raw);
        
        // update the new raw user:
        newRawUser.getParticipants().add(this.raw);
        
        // update this:
        this.user = newUser;
        this.raw.setUser(newRawUser);
    }
    
    
    public VitalUser getUser() {
        return this.user;
    }
    public void setUser(VitalUser user) {
        this.user = user;
    }
    
    public VitalWorksite getWorksite() {
        return this.worksite;
    }
    public void setWorksite(VitalWorksite worksite) {
        this.worksite = worksite;
    }
    
    /**
     * Like getCertainAnnotations, but optimized for using a set of materials as criteria.
     * Returns an empty set if no annotations fit your criteria. Will defer to getCertainAnnotations if your
     * materials set is null or contains only one element. If both parameters are null, will
     * return ALL annotations for this participant. If your materials set is EMPTY, this will return an empty list.
     * <p>IMPORTANT: This method should not be used for a production environment due to the need to initialize
     * the potentially very large number of annotations belonging to this participant. Instead, use Vital3DAO.getAnnotations.
     *
     *@param materials  A set of materials - one of which annotations must be related to to be returned. May be null (see above).
     *@param minDate    A Date after which annotations must have been modified to be returned. May be null (see above).
     */
    public List getAnnotationsForMaterials(Set materials, Date minDate) {
        
        // in case both params are null:
        if (materials == null && minDate == null) return new LinkedList(getAnnotations());
        
        // calculate numMaterials. -1 means null.
        int numMaterials = -1;
        if (materials != null) numMaterials = materials.size();
        
        logger.debug("getAnnotationsForMaterials: " + numMaterials + " materials were passed in (-1 means null). minDate is " + minDate);
        
        // in case the materials set is non-null and empty, shortcut:
        if (numMaterials == 0) return new LinkedList();
        
        // build the set of ids:
        HashSet ids = new HashSet();
        if (numMaterials > 0) {
            Iterator matIter = materials.iterator();
            while (matIter.hasNext()) {
                Material material = (Material) matIter.next();
                ids.add(material.getId());
            }
        }
        LinkedList results = new LinkedList();
        Iterator annoIter = annotations.iterator();
        
        if (minDate != null) {
            
            if (materials == null) {
                // search by minDate criteria only
                while (annoIter.hasNext()) {
                    Annotation anno = (Annotation) annoIter.next();
                    if (minDate.before(anno.getDateModified())) results.add(anno);
                }
            } else {
                // search by materials and minDate criteria
                while (annoIter.hasNext()) {
                    Annotation anno = (Annotation) annoIter.next();
                    if (ids.contains(anno.getMaterial().getId()) && minDate.before(anno.getDateModified())) results.add(anno);
                }
            }
        } else {
            while (annoIter.hasNext()) {
                // search by materials criteria only
                Annotation anno = (Annotation) annoIter.next();
                if (ids.contains(anno.getMaterial().getId()))
                    results.add(anno);
            }
            
        }
        return results;
    }
    
    
    /**
     * The user's first and then last names, separated by a space. Example: "Frank Moretti"
     */
    public String getFullName() {
        if (user == null) throw new RuntimeException("VitalParticipant did not contain a user reference. Cannot get full name.");
        return user.getFullName();
    }
    
    /**
     * The user's last and then first names, separated by a comma and a space. Example: "Moretti, Frank"
     */
    public String getFullNameReversed() {
        if (user == null) throw new RuntimeException("VitalParticipant did not contain a user reference. Cannot get full name (rev).");
        return user.getFullNameReversed();
    }
    
    public String getFirstName() {
        if (user == null) throw new RuntimeException("VitalParticipant did not contain a user reference. Cannot get first name.");
        String value = user.getFirstName();
        if (value == null) return "";
        else return value;
    }
    
    public String getLastName() {
        if (user == null) throw new RuntimeException("VitalParticipant did not contain a user reference. Cannot get last name.");
        String value = user.getLastName();
        if (value == null) return "";
        else return value;
    }
    
    public String getUserIdString() {
        if (user == null) throw new RuntimeException("VitalParticipant did not contain a user reference. Cannot get user id string.");
        return user.getUserIdString();
    }
    
    public String getLabelForAccessLevel() {
        return UserCourseManager.getLabelForAccessLevel(accessLevel);
    }
    
    /////////// STANDARD GETTERS AND SETTERS ////////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    /** 
     * A string uniquely identifying this participant, for use outside of the database. This will be a concatenation
     * of the userIdString, the pipe character "|", and the courseIdString. Call the 'makeParticipantIdString' method to make it.
     */
    public String getParticipantIdString() {
        return this.participantIdString;
    }
    public void setParticipantIdString(String participantIdString) {
        this.participantIdString = participantIdString;
    }
    
    /** 
     * An integer representing a level or type of access granted to this user on this worksite.
     */
    public Integer getAccessLevel() {
        return this.accessLevel;
    }
    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }
    
    
    public Set getAnnotations() {
        return this.annotations;
    }
    
    public void setAnnotations(Set annotations) {
        this.annotations = annotations;
    }
    
    
    public Set getAssignmentResponses() {
        return this.assignmentResponses;
    }
    
    public void setAssignmentResponses(Set assignmentResponses) {
        this.assignmentResponses = assignmentResponses;
    }
    
    
    public Set getComments() {
        return this.comments;
    }
    
    public void setComments(Set comments) {
        this.comments = comments;
    }
    
    /**
     * Set the raw worksite object.
     */
    public void setRaw(RawUCMParticipant raw) {
        this.raw = raw;
    }
    public RawUCMParticipant getRaw() {
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
     *@param raw         the RawUCMParticipant object to wrap.
     *@param user        the VitalUser to use on the surface.
     *@param worksite    the VitalWorksite to use on the surface.
     */
    public void wrap(RawUCMParticipant raw, VitalUser user, VitalWorksite worksite) {
        this.raw = raw;
        this.user = user;
        this.worksite = worksite;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
        .append("id", getId())
        .toString();
    }
    
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new VitalParticipant[]{ this } );
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        Vital3Utils.removeMultipleFromCollections(getAllPersistableChildren());
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(annotations);
        children.addAll(assignmentResponses);
        children.addAll(comments);
        return children;
    }
    
    /**
     * Implemented for MockDB
     */
    public void resetUCM() {
        this.raw = null;
    }
    
}
