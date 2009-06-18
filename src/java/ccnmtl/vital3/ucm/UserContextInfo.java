package ccnmtl.vital3.ucm;

import java.util.Map;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalParticipant;
import ccnmtl.vital3.VitalWorksite;

/**
 * This was designed for use with the Vital3 security architecture. It is constructed by
 * Vital3CommandController and placed in the request object so that
 * subclasses may retrieve it and make use of this information.
 */
public class UserContextInfo {
    
    public static final String ATTRIBUTE_NAME = "ccnmtl.vital3.userContextInfo";
    
    private VitalUser user;
    private VitalParticipant participant;
    private Map permissions;
    
    /**
     * Full constructor. Permissions map is retrieved according
     * to the access level returned from this.getAccessLevel().
     */
    public UserContextInfo(VitalUser user, VitalParticipant participant) {
        this.user = user;
        this.participant = participant;
        this.permissions = UserCourseManager.getPermissions(this.getAccessLevel());
    }
    
    
    /**
     * Analyzes this UserContextInfo instance and returns the context-sensitive access level derived
     * from the information contained. If it contains a participant, the participant's access level will
     * be returned. Else if it contains a user, that user's global access level is returned if it exists.
     * If the user has no global access level, LOGGED_IN_ACCESS is returned. If this object does not
     * contain a user, PUBLIC_ACCESS is returned.
     */
    public Integer getAccessLevel() {
        
        if (participant != null) {
            return participant.getAccessLevel();
            
        } else if (user != null) {
            
            Integer userAccess = user.getAccessLevel();
            if (userAccess != null && userAccess.compareTo(UserCourseManager.PUBLIC_ACCESS) > 0) return userAccess;
            else return UserCourseManager.LOGGED_IN_ACCESS;
            
        }
        return UserCourseManager.PUBLIC_ACCESS;
    }
    
    /**
     * See if this user has a certain permission in this context.
     */
    public boolean hasPermission(Integer permission) {
        // I throw the exception here because you should not be checking for permissions that might be null
        if (permissions == null) throw new RuntimeException("Permissions were null.");
        if (permissions.get(permission) != null) return true;
        else return false;
    }
        
    public VitalUser getUser() {
        return user;
    }
    
    public VitalParticipant getParticipant() {
        return participant;
    }
    
    public VitalWorksite getWorksite() {
        if (participant == null) return null;
        return participant.getWorksite();
    }
    
    public Map getPermissions() {
        return permissions;
    }
    
    
    
}