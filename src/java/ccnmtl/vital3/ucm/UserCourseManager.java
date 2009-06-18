package ccnmtl.vital3.ucm;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import ccnmtl.vital3.*;
import ccnmtl.vital3.dao.Vital3DAO;
import ccnmtl.vital3.utils.*;

/**
 * Provides methods that allow a modular way for vital3 to manage courses, users, and related data.
 * We plan to release one implementation for Columbia-only usage followed by one for Sakai.
 * Implementations of this class contain methods which deal in vital-versions of User, Participant,
 * etc... It should be able to simply extract the
 * Raw entity and then insert/update/delete the shell via Hibernate. The Raw versions are only used
 * internally within the UCM implementation.
 * See specific implementation for detailed docs (ColumbiaUCM.java)
 * This is an abstract class rather than an interface because it offers static methods, and interfaces
 * cannot declare static methods.
 */


public abstract class UserCourseManager {
    
    protected Vital3DAO vital3DAO;
    
    // general access level constants:
    public static final Integer PUBLIC_ACCESS = new Integer(0);
    public static final Integer LOGGED_IN_ACCESS = new Integer(5);
    // worksite access level constants:
    public static final Integer STUDENT_ACCESS = new Integer(10);
    public static final Integer GUEST_ACCESS = new Integer(20);
    public static final Integer TA_ACCESS = new Integer(30);
    public static final Integer INSTRUCTOR_ACCESS = new Integer(40);
    // administrator access level constants:
    public static final Integer ADMIN_ACCESS = new Integer(100);
    
    
    // permissions constants:
    
    // can this user view other people's assignments before submitting their own?
    public static final Integer CAN_VIEW_OTHERS_ASSIGNMENTS = new Integer(0);
    // will this user appear on the assignments page?
    public static final Integer DISPLAY_ON_ASSIGNMENTS_PAGE = new Integer(1);
    // will this user appear on the student's list of participants?
    public static final Integer DISPLAY_IN_STUDENT_PARTICIPANT_LIST = new Integer(2);
    // will this user appear on the instructor's list of participants?
    public static final Integer DISPLAY_IN_INSTRUCTOR_PARTICIPANT_LIST = new Integer(3);
    // can this user administrate worksite users?
    public static final Integer CAN_ADMINISTRATE_WORKSITE_USERS = new Integer(4);
    // can this user administrate worksite info, materials, assignments, units, etc?
    public static final Integer CAN_ADMINISTRATE_WORKSITE_CURRICULUM = new Integer(5);
    // can this user administrate worksites (e.g. add/delete them)?
    public static final Integer CAN_ADMINISTRATE_WORKSITES = new Integer(6);
    // can this user administrate other administrators (e.g. add/delete them)?
    public static final Integer CAN_ADMINISTRATE_ADMINISTRATORS = new Integer(7);
    
    protected static TreeMap accessLevelLabelMap;
    protected static TreeMap permissionsMap;
    
    static {
        
        // this can be used anytime an access level needs to be displayed on a template.
        accessLevelLabelMap = new TreeMap();
        accessLevelLabelMap.put(PUBLIC_ACCESS, "Public");
        accessLevelLabelMap.put(GUEST_ACCESS, "Guest");
        accessLevelLabelMap.put(STUDENT_ACCESS, "Student");
        accessLevelLabelMap.put(TA_ACCESS, "T.A.");
        accessLevelLabelMap.put(INSTRUCTOR_ACCESS, "Instructor");
        accessLevelLabelMap.put(ADMIN_ACCESS, "Administrator");
        
        // this reference is just to save typing:
        Boolean tru = Boolean.TRUE;
        
        // this map is loaded with String keys which map to Map values. See below for definitions.
        permissionsMap = new TreeMap();
        
        // public access is the baseline access level:
        TreeMap publicMap = new TreeMap();
        permissionsMap.put(PUBLIC_ACCESS, publicMap);
        
        // logged-in access means you must be logged in:
        TreeMap loggedInMap = new TreeMap();
        permissionsMap.put(LOGGED_IN_ACCESS, loggedInMap);
        
        // guests are the most basic level. They are not visible to other non-privileged users:
        TreeMap guestMap = new TreeMap();
        guestMap.put(CAN_VIEW_OTHERS_ASSIGNMENTS, tru);
        guestMap.put(DISPLAY_IN_INSTRUCTOR_PARTICIPANT_LIST, tru);
        permissionsMap.put(GUEST_ACCESS, guestMap);
        
        // students are like guests except they cannot view others' assignments until they submit their own:
        TreeMap studentMap = new TreeMap();
        studentMap.put(DISPLAY_ON_ASSIGNMENTS_PAGE, tru);
        studentMap.put(DISPLAY_IN_STUDENT_PARTICIPANT_LIST, tru);
        studentMap.put(DISPLAY_IN_INSTRUCTOR_PARTICIPANT_LIST, tru);
        permissionsMap.put(STUDENT_ACCESS, studentMap);
        
        // for TAs, see below...
        
        // instructors can see everyone in the worksite and can administrate all aspects of their worksites:
        TreeMap instructorMap = new TreeMap();
        instructorMap.put(CAN_VIEW_OTHERS_ASSIGNMENTS, tru);
        instructorMap.put(DISPLAY_IN_STUDENT_PARTICIPANT_LIST, tru);
        instructorMap.put(DISPLAY_IN_INSTRUCTOR_PARTICIPANT_LIST, tru);
        instructorMap.put(CAN_ADMINISTRATE_WORKSITE_USERS, tru);
        instructorMap.put(CAN_ADMINISTRATE_WORKSITE_CURRICULUM, tru);
        permissionsMap.put(INSTRUCTOR_ACCESS, instructorMap);
        
        // TAs are exactly like instructors (for now)
        permissionsMap.put(TA_ACCESS, instructorMap);
        
        // administrators can do anything, and do not appear on any lists:
        TreeMap adminMap = new TreeMap();
        adminMap.put(CAN_VIEW_OTHERS_ASSIGNMENTS, tru);
        adminMap.put(CAN_ADMINISTRATE_WORKSITE_USERS, tru);
        adminMap.put(CAN_ADMINISTRATE_WORKSITE_CURRICULUM, tru);
        adminMap.put(CAN_ADMINISTRATE_WORKSITES, tru);
        adminMap.put(CAN_ADMINISTRATE_ADMINISTRATORS, tru);
        permissionsMap.put(ADMIN_ACCESS, adminMap);
    }
    
    // access level label-retrieval method:
    public static String getLabelForAccessLevel(Integer accessLevel) {
        
        if (accessLevel == null) return "null";
        return (String) accessLevelLabelMap.get(accessLevel);
    }
    
    /**
     * permissions-retrieval method: will return the proper permissions map for the access level you pass
     *@param accessLevel   An Integer corresponding to one of the access level constants listed above
     *@return              A map of permissions, where the keys are Integers corresponding to the permissions constants,
     *                     and the values are either a true Boolean or null.
     */
    public static Map getPermissions(Integer accessLevel) {
        
        return (Map) permissionsMap.get(accessLevel);
    }
    
    /**
     * quick permissions-retrieval method: will return whether a participant has a particular permission.
     *@param permission    An Integer corresponding to one of the permission constants listed above
     *@return              true or false: whether the participant had a particular permission or not.
     */
    public static boolean hasPermission(VitalParticipant participant, Integer permission) {
        
        return ( ((Map) permissionsMap.get(participant.getAccessLevel())).get(permission) != null );
    }
    
    // methods for dealing with currently-logged-in user, session, auth, etc:
    public abstract String getExternalAuthPasswordChangeUrl();
    public abstract String getExternalAuthForgotPasswordUrl();
    public abstract VitalUser loadUser(HttpSession session, String authMethod);
    public abstract void logout(HttpSession session);
    public abstract VitalUser getCLIU(HttpSession session, boolean initParticipants);
    public abstract String generateEmailAddressFor(String username);
    
    // course id string parsing and formatting:
    public abstract String formatCourseIdStringForDisplay(String internalIdString) throws ParseException;
    public abstract String parseCourseIdString(String displayIdString) throws ParseException;
    
    // decorate a collection of shell entities (fill in the raw parts):
    public abstract List decorateParticipants(Collection participants);
    public abstract List decorateUsers(Collection users, boolean initParticipants);
    public abstract List decorateWorksites(Collection worksites, boolean initCourseAffils, boolean initParticipants);
    
    // decorate a single shell entity (fill in the raw part):
    public abstract VitalParticipant decorateParticipant(VitalParticipant participant);
    public abstract VitalUser decorateUser(VitalUser user, boolean initParticipants);
    public abstract VitalWorksite decorateWorksite(VitalWorksite worksite, boolean initCourseAffils, boolean initParticipants);

    // methods for dealing with Courses
    public abstract void affiliateWorksiteWithCourse(VitalWorksite worksite, String courseIdString, boolean exclusively);
    public abstract void removeCourseAffilsForWorksite(VitalWorksite worksite);
        
    // methods for dealing with Terms
    public abstract RawUCMTerm findTermById(Long id);
    //public abstract List findTermsAfter(Date startDate);
    //public abstract List findTermsBetween(Date startDate, Date endDate);
    
    
    
    public abstract List worksitesForTerm (String termName);
    public abstract List worksitesForTerms (Set termNames);
    public abstract Set currentAcademicYearTermNames();
    public abstract Set currentCalendarYearTermNames();
    public abstract String currentTermName ();
    
    /*Pass a in a set of VitalWorksites. Get back a list of already-decorated VitalParticipants, each of which has Instructor permissions to one of the vitalWorksites passed. If the set is empty, this returns all instructors. */
    public abstract List getInstructors( Set vWorksites);
    

    // methods for dealing with Users
    public abstract VitalUser findUserByIdString(String userIdString, boolean initParticipants);
    public abstract VitalUser findUserByEmail(String email, boolean initParticipants);
    

    // construction methods:
    public abstract VitalParticipant constructParticipant(VitalUser vUser, VitalWorksite vWorksite, Integer accessLevel);
    public abstract VitalWorksite constructWorksite(RawUCMTerm term, String announcement, String title);
    
    public abstract VitalParticipant findParticipant(VitalUser user, VitalWorksite worksite) throws Vital3AuthViolationException;
    
    
    public abstract List searchForUsers(String substring);
        
    // get all entities of a given type (decorated)
    public abstract List findAllParticipants();
    public abstract List findAllUsers(boolean initParticipants);
    public abstract List findAllTerms();
    public abstract List findAllWorksites(boolean initCourseAffils, boolean initParticipants);
    //public abstract List findAllCourses();
    
    // insert a collection of entities
    public abstract void insertUsers(Collection users);
    public abstract void insertParticipants(Collection participants);
    public abstract void insertWorksites(Collection worksites);
    
    // update a collection of entities
    public abstract void updateUsers(Collection users);
    public abstract void updateParticipants(Collection participants);
    public abstract void updateWorksites(Collection worksites);
    
    // delete a collection of entities
    public abstract void deleteUsers(Collection users);
    public abstract void deleteParticipants(Collection participants);
    public abstract void deleteWorksites(Collection worksites);
    
    // insert a single entity
    public abstract void insertUser(VitalUser user);
    public abstract void insertParticipant(VitalParticipant participant);
    public abstract void insertWorksite(VitalWorksite worksite);
    
    // update a single entity
    public abstract void updateUser(VitalUser user);
    public abstract void updateParticipant(VitalParticipant participant);
    public abstract void updateWorksite(VitalWorksite worksite);
    
    // delete a single entity
    public abstract void deleteUser(VitalUser user);
    public abstract void deleteParticipant(VitalParticipant participant);
    public abstract void deleteWorksite(VitalWorksite worksite);
    
    
    /******** GETTERS AND SETTERS FOR DEPENDENCY INJECTION **********/
    
    public Vital3DAO getVital3DAO() { return this.vital3DAO; }
    public void setVital3DAO(Vital3DAO vital3DAO) { this.vital3DAO = vital3DAO; }


}



