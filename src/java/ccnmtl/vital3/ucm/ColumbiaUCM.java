package ccnmtl.vital3.ucm;

import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;

import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;
import ccnmtl.vital3.utils.*;
import ccnmtl.vital3.dao.Vital3DAO;


/**
 * Columbia's implementation of UserCourseManager.
 * This class exists in order to make the usage of Users, Worksites, and Participants simple. Any time you want
 * to update, insert, find, construct, or decorate any of the UCM Classes (User, Participant, Worksite, Term), you
 * MUST use this class!
 *
 * <p>GUIDE TO USAGE:<br>
 * Scenario 1: You want a list of all VitalWorksites, and that's it. Nothing fancy.<br>
 * - just call ucm.findAllWorksites(false, false). The boolean "initXXXX" parameters to methods such as this are used
 * to provide means of being efficient in case you do not want to fetch every single related entity. For example...<br>
 * Scenario 2: You want a list of all VitalWorksites, and you want them to have their "participants" collections.<br>
 * - just call ucm.findAllWorksites(false, true). This will allow you to get all those worksites and iterate through all
 * of their participants collections. Each participant will also have its "user" property assigned too.<br>
 * Scenario 3: You want a list of all Vitalworksites because you are checking to see which courses are affiliated with them.<br>
 * - just call: ucm.findAllWorksites(true, false). This will not initialize the "participants", but it will allow you to
 * call myWorksite.getCourseIdStringList() to check those course affils.<br>
 * Scenario 4: You retrieved a VitalWorksite using Vital3DAO (e.g. based on an id value) and you need the worksite's title.<br>
 * - If you want any of the information which is contained on the 'UCM' side of the entity relationship diagram (e.g. worksite title)
 * you need to decorate that Vital object to get it. To do this, call ucm.decorateVitalWorksite(vWorksite, false, false). Now
 * you can access the title and Term of the worksite. If you don't care about the title or Term, you can use the VitalWorksite
 * without decorating it (e.g. to get its units or materials).<br>
 * Scenario 5: You want to make a new VitalWorksite.<br>
 * - You need to call ucm.constructWorksite(myTerm, "announcement", "title"). This will generate a WorksiteIdString for you, which
 * is needed internally, so you should NEVER construct a VitalWorksite without using the constructWorksite method.
 * <p>
 * This class uses Vital3DAO for all its database work.
 */

public class ColumbiaUCM extends UserCourseManager {
    
    protected static TreeMap accessLevelConversionMap;
    
    static {
        
        accessLevelConversionMap = new TreeMap();
        accessLevelConversionMap.put(UserCourseManager.STUDENT_ACCESS, "STUDENT");
        accessLevelConversionMap.put(UserCourseManager.GUEST_ACCESS, "GUEST");
        accessLevelConversionMap.put(UserCourseManager.INSTRUCTOR_ACCESS, "INSTRUCTOR");
        accessLevelConversionMap.put(UserCourseManager.ADMIN_ACCESS, "ADMIN");
    }
    
    public String ldapDisabled;
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    
    
    /*************** COURSE-RELATED METHODS ******************/
    
    
    /**
     * Ensures that the worksite you pass is affiliated with the course you indicate.
     * If it is not, it will create the RawUCMCourse if needed and will create the
     * affiliation.
     *@param worksite         The VitalWorksite you want to affiliate with the course
     *@param courseIdString   The course id string of the course you want affiliated. (Internal format)
     *@param exclusively      Whether you want this to be the ONLY course affiliated with this Worksite...
     *                         if this is true, it will delete all other affiliations.
     */
    public void affiliateWorksiteWithCourse(VitalWorksite worksite, String courseIdString, boolean exclusively) {
        affiliateWorksiteWithCourse(worksite.getRaw(), courseIdString, exclusively);
    }
    
    private void affiliateWorksiteWithCourse(RawUCMWorksite worksite, String courseIdString, boolean exclusively) {
        if (worksite == null) throw new RuntimeException("Cannot affiliate null worksite with course " + courseIdString);
        if (courseIdString == null || courseIdString.equals("")) throw new RuntimeException("Cannot affiliate worksite with null course id");
        if (!courseIdString.equals(courseIdString.trim())) throw new RuntimeException("courseIdString contained leading or trailing whitespace");
        
        logger.debug("Beginning affiliateWorksiteWithCourse for worksite " + worksite.getTitle() + " and course " + courseIdString);
        
        // search existing affils
        Set affils = worksite.getCourseAffils();
        Iterator affilIter = affils.iterator();
        boolean found = false;
        while (affilIter.hasNext()) {
            RawUCMCourseWorksiteAffil affil = (RawUCMCourseWorksiteAffil) affilIter.next();
            String existingIdString = affil.getCourse().getCourseIdString();
            if (existingIdString.equals(courseIdString)) {
                // this worksite is already affiliated with this course:
                found = true;
                logger.debug("that worksite is already affiliated with that course.");
            } else if (exclusively) {
                // if this is supposed to be an exclusive affiliation, delete all other existing ones
                logger.debug("deleting affiliation with course " + existingIdString);
                affils.remove(affil);
                // this next line commented out because that collection would need to be initialized... and it's probably never going to be an issue.
                //affil.getCourse().getWorksiteAffils().remove(affil);
                vital3DAO.delete(RawUCMCourseWorksiteAffil.class, affil);
            }
        }
        
        if (!found) {
            logger.debug("that worksite was not yet affiliated with that course.");
            // "summon" course: will create it (and save it) if needed:
            RawUCMCourse course = summonCourse(courseIdString);
            // even if the course existed already, we know there is no affil yet.
            // create the RawUCMCourseWorksiteAffil and link it up and save it
            RawUCMCourseWorksiteAffil affil = new RawUCMCourseWorksiteAffil(course, worksite);
            vital3DAO.save(RawUCMCourseWorksiteAffil.class, affil);
            logger.debug("created affiliation.");
        }
    }
    
    /**
     * Permanently deletes all course affiliations for a particular worksite.
     * Courses remain in the database, but affils are deleted.
     */
    public void removeCourseAffilsForWorksite(VitalWorksite worksite) {
        removeCourseAffilsForWorksite(worksite.getRaw());
    }
    
    private void removeCourseAffilsForWorksite(RawUCMWorksite worksite) {
        
        Set affils = worksite.getCourseAffils();
        if (affils != null && affils.size() > 0) {
            logger.debug("removing course affiliations for worksite " + worksite.getTitle());
            vital3DAO.deleteCollection(RawUCMCourseWorksiteAffil.class, affils);
        } else logger.debug("there were no course affiliations to remove");
        
    }
    
    /**
     * Translates a course id string from the internal format to one which will be displayed to the user.
     * The displayed format is the one used in the Directory of classes.
     *
     * Maybe in the future we can subclass Java's Format class for this.
     *
     *@param internalIdString      The course id string, in the internally-used format
     *@return                      The course id string, in the display-friendly format
     */
    public String formatCourseIdStringForDisplay(String internalIdString) throws ParseException {
        
        // internal: t3.y2006.s003.cv1201.ital.st.course:columbia.edu
        //           012345678901234567890123456789012345
        // display:  20063ITAL1201V003
        
        try {
            String termNumber = internalIdString.substring(1,2);
            String year = internalIdString.substring(4,8);
            String section = internalIdString.substring(10,13);
            String termCharacter = internalIdString.substring(15,16);
            String courseNumber = internalIdString.substring(16,20);
            String department = internalIdString.substring(21,25);
            
            String displayString = year + termNumber + department + courseNumber + termCharacter + section;
            return displayString.toUpperCase();
        
        } catch(Exception e){
            logger.info("Could not parse course id string: " + e.getMessage());
			throw new ParseException("Could not parse course id string", 0);
		}
    }
    
    /**
     * Parses a course id string in the display-friendly format to the internally used format.
     * The string is expected to be in the format used in the directory of classes.
     * IMPORTANT: Not really sure if this works for Barnard classes!
     * Maybe in the future we can subclass Java's Format class for this.
     *
     *@param displayIdString       The course id string, in the display-friendly format
     *@return                      The course id string, in the internal format
     */
    public String parseCourseIdString(String displayIdString) throws ParseException {
        
        // display:  20063ITAL1201V003
        //           012345678901234567890123456789012345
        // internal: t3.y2006.s003.cv1201.ital.st.course:columbia.edu
        
        try{
            String parseString = displayIdString.trim().toLowerCase();
            
            String year = parseString.substring(0,4);
            String termNumber = parseString.substring(4,5);
            String department = parseString.substring(5,9);
            String courseNumber = parseString.substring(9,13);
            String termCharacter = parseString.substring(13,14);
            String section = parseString.substring(14,17);
        
            return "t" + termNumber + ".y" + year + ".s" + section + ".c" + termCharacter + courseNumber + "." + department + ".st.course:columbia.edu";
            
		} catch(Exception e){
            logger.info("Could not parse course id string: " + e.getMessage());
			throw new ParseException("Could not parse course display string", 0);
		}
    }
	
    
    public String generateEmailAddressFor(String username) {
        
        return username + "@columbia.edu";
        
    }
    
    
    /************** METHODS FOR DECORATING VITAL ENTITIES (FILLING IN THE RAW PARTS) **************/
    
    
    /**
     * Given a VitalUser, initializes and retrieves its VitalParticipants and VitalWorksites.
     * IMPORTANT NOTE: When passing true for initParticipants, each of the VitalUsers gets its "participants" collection assigned,
     * but each VitalWorksite will NOT get more than one participant in its "participants" collection. This is because if it did,
     * every single worksite member's other participants & worksites would be loaded too!
     */
    public VitalUser decorateUser (VitalUser vUser, boolean initParticipants) {
        logger.debug ("Decorating user " + vUser );
		if (initParticipants) logger.debug ("Request is to initialize participants.");
		else logger.debug ("Request is NOT to initialize participants.");
        RawUCMUser rUser = vUser.getRaw();
        // if there is no raw, find it:
        if (rUser == null) {
	        logger.debug ("User passed was an empty shell, so finding raw and wrapping it. " + vUser);
            rUser = findRawForVitalUser(vUser);
            vUser.wrap(rUser);
        }
		else logger.debug("User passed already had a raw.");
		
        if (initParticipants) {
            
			if (vUser.getParticipants() != null) {
				logger.info("User " + vUser.getUserIdString() + " already had a participant array. Removing it and re-initing...");
            }
            vUser.setParticipants(new HashSet());
					
            Set rawParticipants = rUser.getParticipants();
            if (rawParticipants.size() > 0) {
                
                // find all vitalParticipants for these raw participants:
                List vParticipants = findVitalsForRaws(RawUCMParticipant.class, rawParticipants);
                
                decorateParticipants(vParticipants, rawParticipants);
            }
        }
        return vUser;
    }
    
    
    
    /**
     * Given a VitalWorksite, initializes and retrieves its VitalParticipants and VitalUsers.
     * IMPORTANT NOTE: When passing true for initParticipants, each of the VitalWorksites gets its "participants" collection assigned,
     * but each VitalUser will NOT get more than one participant in its "participants" collection. This is because if it did,
     * every single worksite member's other participants & worksites would be loaded too!
     */
    public VitalWorksite decorateWorksite (VitalWorksite vWorksite, boolean initCourseAffils, boolean initParticipants) {
        
        logger.debug("Decorating worksite " + vWorksite.getWorksiteIdString());
        
        RawUCMWorksite rWorksite = vWorksite.getRaw();
        // if there is no raw, find it:
        if (rWorksite == null) {
            //logger.debug("vitalworksite " + vWorksite.getWorksiteIdString() + " had no raw. Finding...");
            rWorksite = findRawForVitalWorksite(vWorksite);
            vWorksite.wrap(rWorksite);
        } //else logger.debug("vitalworksite " + vWorksite.getWorksiteIdString() + " already had a raw");
        
        logger.debug("Worksite title is " + vWorksite.getTitle());
        
        if (initCourseAffils)
            Vital3Utils.initCollection(RawUCMWorksite.class, rWorksite, "courseAffils", RawUCMCourseWorksiteAffil.class);
        
        if (initParticipants) {
            
            vWorksite.setParticipants(new HashSet());
            
            Set rawParticipants = rWorksite.getParticipants();
            
            if (rawParticipants.size() > 0) {
                // find all vitalParticipants for these raw participants:
                logger.debug("found " + rawParticipants.size() + " participants");
                List vParticipants = findVitalsForRaws(RawUCMParticipant.class, rawParticipants);
                decorateParticipants(vParticipants, rawParticipants);
            }
        }
        return vWorksite;
    }
    
    
    
    /**
     * Decorates a VitalParticipant and assigns it a VitalUser and a VitalWorksite.
     * IMPORTANT NOTE: For both the "user" and "worksite", their "participants" collections will
     * most likely not be fully populated, and will only contain this one participant.
     */
	public VitalParticipant decorateParticipant(VitalParticipant vParticipant) {

        logger.debug("UCM.decorateParticipant beginning for participant id " + vParticipant.getId());
        
        RawUCMParticipant rParticipant = vParticipant.getRaw();
        // if there is no raw, find it:
        if (rParticipant == null) {
            rParticipant = findRawForVitalParticipant(vParticipant);
        }
        
		// make sure the raw participant is connected to a raw user and a raw participant and they're valid:
        RawUCMUser rUser = rParticipant.getUser();
		RawUCMWorksite rWorksite = rParticipant.getWorksite();
		if (rUser == null) throw new RuntimeException("No RawUCMUser found in RawUCMParticipant with idString '" + rParticipant.getParticipantIdString() + "'");
		if (rWorksite == null) throw new RuntimeException("No RawUCMWorksite found in RawUCMParticipant with idString '" + rParticipant.getParticipantIdString() + "'");

		// find vitalUser & worksite, wrap their raws:
		VitalUser vUser = findVitalUserForRaw(rUser);
        vUser.wrap(rUser);
        VitalWorksite vWorksite = findVitalWorksiteForRaw(rWorksite);
        vWorksite.wrap(rWorksite);
        
        vParticipant.wrap(rParticipant, vUser, vWorksite);
        
        // add to user and worksite participant sets:
        vUser.addParticipant(vParticipant);
        vWorksite.addParticipant(vParticipant);
        
        return vParticipant;
	}
    
    
    // decorate a collection of shell entities (fill in the raw parts):
    
    /**
     * Decorates a collection of vitalparticipants. Note that this will not bother to check if they are already decorated...
     * It will do the work of decorating them regardless. The resulting List contains your VitalParticipants, which have been
     * fully decorated and associated with fully decorated users and worksites, each having this participant added to their
     * (probably otherwise empty) participants collections.
     */
    public List decorateParticipants(Collection participants){
        logger.debug("beginning decoration of participants collection of size " + participants.size());
        
        if (participants.size() == 0) return new ArrayList();
        
        // find all raw participants:
        List rParticipants = findRawsForVitals(VitalParticipant.class, participants);
        
        return decorateParticipants(participants, rParticipants);
	}
    
    
    /**
     * Private function for decorating is used by a few other functions. You must pass a collection of VitalParticipants and
     * a collection containing every corresponding raw participant! Returns the decorated VitalParticipants collection.
     */
    private List decorateParticipants(Collection vitalParticipants, Collection rParticipants){
        
        if (vitalParticipants.size() != rParticipants.size()) {
            logger.warn("### WARNING: decorateParticipants: collections were different sizes!");
        }
        
        if (vitalParticipants.size() == 0) return new ArrayList();
        
        // convert collection to ArrayList:
        ArrayList vParticipants = new ArrayList(vitalParticipants);
        
        // organize all raw users and worksites
        Set rUsers = new HashSet();
        Set rWorksites = new HashSet();
        Iterator rawIter = rParticipants.iterator();
        while (rawIter.hasNext()) {
            
            RawUCMParticipant rParticipant = (RawUCMParticipant) rawIter.next();
            RawUCMUser rUser = rParticipant.getUser();
            RawUCMWorksite rWorksite = rParticipant.getWorksite();
            
            if (rUser == null) throw new RuntimeException("No RawUCMUser found in RawUCMParticipant with idString " + rParticipant.getParticipantIdString());
            if (rWorksite == null) throw new RuntimeException("No RawUCMWorksite found in RawUCMParticipant with idString " + rParticipant.getParticipantIdString());
            
            rUsers.add(rUser);
            rWorksites.add(rWorksite);
        }
        
        // find vitals for all raw users and worksites:
        List vUsers = findVitalsForRaws(RawUCMUser.class, rUsers);
        List vWorksites = findVitalsForRaws(RawUCMWorksite.class, rWorksites);
        
        // put it all together:
        rawIter = rParticipants.iterator();
        while (rawIter.hasNext()) {
            
            RawUCMParticipant rParticipant = (RawUCMParticipant) rawIter.next();
            RawUCMUser rUser = rParticipant.getUser();
            RawUCMWorksite rWorksite = rParticipant.getWorksite();
            
            // find vWorksite, vUser, and vParticipant in their respective lists:
            VitalUser vUser = findVitalUserInCollection(vUsers, rUser.getUserIdString());
            VitalWorksite vWorksite = findVitalWorksiteInCollection(vWorksites, rWorksite.getWorksiteIdString());
            VitalParticipant vParticipant = findVitalParticipantInCollection(vParticipants, rParticipant.getParticipantIdString());
            
            // wrap everything:
            vUser.wrap(rUser);
            vWorksite.wrap(rWorksite);
            vParticipant.wrap(rParticipant, vUser, vWorksite);
            
            // add to user and worksite participant sets:
            vUser.addParticipant(vParticipant);
            vWorksite.addParticipant(vParticipant);
        }
        
        return vParticipants;
    }
    
    
    public List decorateUsers(Collection users, boolean initParticipants){
        logger.debug("beginning decoration of users collection of size " + users.size());
		ArrayList decorated =  new ArrayList();
		Iterator it = users.iterator();
        while (it.hasNext()) {
			VitalUser nextUser = (VitalUser) it.next();
			decorateUser(nextUser, initParticipants);
			decorated.add (nextUser);
		}
        
		return decorated;
	}
    
    
    public List decorateWorksites(Collection worksites, boolean initCourseAffils, boolean initParticipants){
        logger.debug("beginning decoration of worksites collection of size " + worksites.size());
		ArrayList decorated =  new ArrayList();
		Iterator it = worksites.iterator();
        while (it.hasNext()) {
			VitalWorksite nextWorksite = (VitalWorksite) it.next();
            // pass false for initCourseAffils because it's faster to do it using the single collection call below
			decorateWorksite(nextWorksite, false, initParticipants);
			decorated.add (nextWorksite);
		}
        if (initCourseAffils) {
            Set raws = Vital3Utils.getSetOfRaws(decorated);
            Vital3Utils.initCollections(RawUCMWorksite.class, raws, "courseAffils", RawUCMCourseWorksiteAffil.class);
        }
		return decorated;
	}
    
    /*public List decorateWorksites(Collection worksites, boolean initCourseAffils, boolean initParticipants){
    
        // convert collection to ArrayList:
        ArrayList vWorksites = new ArrayList(worksites);
        
        // find all raw worksites
        // find all raw participants
        // find all vital participants
        // aggregate all raw users
        // aggregate all vital users
        
        
    }*/
    
    
    /**************** CONSTRUCTOR METHODS *****************/
    
    /**
     * Given a VitalUser and a VitalWorksite, this will create a VitalParticipant linking the two together.
     * It comes complete with a wrapped RawUCMParticipant which is linked to the raw versions of the VitalUser
     * and VitalWorksite you passed.
     *@param vUser          A VitalUser to link. Must be decorated or this will throw an Exception.
     *@param vWorksite      A VitalWorksite to link. Must be decorated or this will throw an Exception.
     *@param accessLevel    The access level this user is going to have for this worksite. See constants defined in UserCourseManager.
     *@return               A complete, decorated VitalParticipant all linked up.
     */
    public VitalParticipant constructParticipant(VitalUser vUser, VitalWorksite vWorksite, Integer accessLevel) {
        
        RawUCMUser rUser = vUser.getRaw();
        RawUCMWorksite rWorksite = vWorksite.getRaw();
        if (rUser == null) throw new RuntimeException("Cannot construct VitalParticipant with an undecorated VitalUser");
        if (rWorksite == null) throw new RuntimeException("Cannot construct VitalParticipant with an undecorated VitalWorksite");
        // ensure that there is not already a participant for this user and worksite pair:
        List results = vital3DAO.findByTwoPropertyValues(RawUCMParticipant.class, "user", rUser, "worksite", rWorksite);
        if (results.size() > 0) throw new UnsupportedOperationException("This user is already participating in this course.");
        // generate the participantIdString:
        String idString = generateParticipantIdString(rUser, rWorksite);
        // construct the new vitalParticipant:
        if (vUser.isParticipantsNull()) vUser.setParticipants(new HashSet());
        if (vWorksite.isParticipantsNull()) vWorksite.setParticipants(new HashSet());
        VitalParticipant vParticipant = new VitalParticipant(idString, accessLevel, vUser, vWorksite);
        // return the new vitalParticipant:
        logger.debug("UCM constructed participant with idString " + idString);
        return vParticipant;
    }
 
    /**
     * Use this to create VitalWorksites.
     * It will come complete with a wrapped RawUCMWorksite linked to the same RawUCMTerm you pass.
     * 
     *@param term           A RawUCMTerm indicating when this worksite is held.
     *@param announcement   An announcement to go on the course home page.
     *@param title          The title of the worksite.
     *@return               A complete, decorated VitalWorksite all linked up.
     */
    public VitalWorksite constructWorksite(RawUCMTerm term, String announcement, String title) {
     
        RawUCMTerm rTerm = term;
        // generate the worksiteIdString:
        String idString = generateWorksiteIdString(rTerm);
        // construct the new VitalWorksite:
        VitalWorksite vWorksite = new VitalWorksite(idString, announcement, rTerm, title);
        // return the new VitalWorksite:
        logger.debug("UCM constructed worksite with idString " + idString);
        return vWorksite;
    }
    
    // NOTE: There is no constructUser method, because it is not necessary. Just use the "convenient" VitalUser constructor.

    /**
     * Use this to find and create RawUCMCourses. It will ensure that you don't create duplicates.
     * Pass the course id string and if the DB record needs to be created it will be. If it
     * was already in the DB, you'll get the existing one back in return with its worksiteAffils initialized.
     */
    private RawUCMCourse summonCourse(String courseIdString) {
        
        RawUCMCourse course = null;
        List results = vital3DAO.findByPropertyValue(RawUCMCourse.class, "courseIdString", courseIdString);
        
        if (results.size() == 0) {
            logger.debug("summonCourse: did not find course with id " + courseIdString + ". Creating it.");
            course = new RawUCMCourse(courseIdString);
            vital3DAO.save(RawUCMCourse.class, course);
            
        } else {
            logger.debug("summonCourse: found course id " + courseIdString);
            course = (RawUCMCourse) results.get(0);
            Vital3Utils.initCollection(RawUCMCourse.class, course, "worksiteAffils", RawUCMCourseWorksiteAffil.class);
        }
        return course;
    }
    
    
    /***************** METHODS FOR FINDING STUFF *******************/
    

    /**
     * Returns the participant which associates a given user to a given worksite. If no such participant
     * exists, two things might happen. If the user is an administrator, a participant is created, inserted, and returned.
     * If the user is not admin, Vital3AuthViolationException is thrown. The returned participant is automatically added
     * to the "participants" set of both worksite and user. This method never returns null.
     */
    public VitalParticipant findParticipant(VitalUser user, VitalWorksite worksite) throws Vital3AuthViolationException {
        
        logger.debug("findParticipant called. User is " + user.getUserIdString() + ", worksite is " + worksite.getTitle());
        
        VitalParticipant vParticipant = null;
        RawUCMParticipant rParticipant = null;
        
        List results = vital3DAO.findByTwoPropertyValues(RawUCMParticipant.class, "user", user.getRaw(), "worksite", worksite.getRaw());
        if (results.size() > 0) rParticipant = (RawUCMParticipant) results.get(0);
        
        if (rParticipant == null) {
            
            // if user is admin, construct and insert new participant
            Integer accessLevel = user.getAccessLevel();
            logger.debug("findParticipant: could not find participant. User's accessLevel is " + accessLevel);
            if (accessLevel == null || accessLevel.compareTo(UserCourseManager.ADMIN_ACCESS) < 0)
                throw new Vital3AuthViolationException();
            
            vParticipant = constructParticipant(user, worksite, accessLevel);
            saveParticipant(vParticipant);
            logger.debug("findParticipant: inserted new participant for this user.");
            
        } else {
            
            // decorate raw
            vParticipant = findVitalParticipantForRaw(rParticipant);
            // wrap the rParticipant using the vWorksite and vUser
            vParticipant.wrap(rParticipant, user, worksite);
            
            logger.debug("findParticipant: found participant!");
            // connect to user and worksite for consistency (this is the result in the previous branch)
            if (user.isParticipantsNull()) {
                Set tempSet = new HashSet();
                tempSet.add(vParticipant);
                user.setParticipants(tempSet);
            } else {
                user.getParticipants().add(vParticipant);
            }
            if (worksite.isParticipantsNull()) {
                Set tempSet = new HashSet();
                tempSet.add(vParticipant);
                worksite.setParticipants(tempSet);
            } else {
                user.getParticipants().add(vParticipant);
            }
            
        }
        
        return vParticipant;
        
    }
    

    /**
     * Retrieves a decorated VitalUser from a userIDString.
     *
     */
	public VitalUser findUserByIdString(String userIdString, boolean initParticipants) {
        if (userIdString == null) throw new RuntimeException("Can't find user based on a null string.");
        List results = vital3DAO.findByPropertyValue(VitalUser.class, "userIdString", userIdString);
        if (results.size() == 0) {
            logger.debug("No user found with idString " +  userIdString);
            return null;
        }
        VitalUser vitalUser = (VitalUser) results.get(0);
        return decorateUser(vitalUser, initParticipants);
	}
    
    /**
     * Retrieves a decorated VitalUser from an e-mail address.
     *
     */
	public VitalUser findUserByEmail(String email, boolean initParticipants) {
        if (email == null || email.equals("")) throw new RuntimeException("Can't find user based on a null/empty string.");
        List results = vital3DAO.findByPropertyValue(RawUCMUser.class, "email", email);
        if (results.size() == 0) {
            logger.debug("No user found with email " +  email);
            return null;
        } else if (results.size() > 1) logger.warn("Duplicate email address in database! " + email);
        
        RawUCMUser rUser = (RawUCMUser) results.get(0);
        VitalUser vUser = findVitalUserForRaw(rUser);
        vUser.wrap(rUser);
        return decorateUser(vUser, initParticipants);
	}
    
    
    /**
     * Returns a list of decorated VitalParticipants.
     */
    public List findAllParticipants() {
        logger.debug("UCM: finding all participants");
		List participants = vital3DAO.findAll(VitalParticipant.class);
        return decorateParticipants(participants);
    }
    
    /**
     * Returns a list of decorated VitalWorksites.
     */
    public List findAllWorksites(boolean initCourseAffils, boolean initParticipants) {
        logger.debug("UCM: finding all worksites");
        List worksites = vital3DAO.findAll(VitalWorksite.class);
        return decorateWorksites(worksites, initCourseAffils, initParticipants);
    }
    
    /**
     * Returns a list of RawUCMTerms.
     */
    public List findAllTerms() {
        logger.debug("UCM: finding all terms");
        List raws = vital3DAO.findAll(RawUCMTerm.class);
        return raws;
    }
    
    /**
     * Returns a list of decorated VitalUsers.
     */
	public List findAllUsers(boolean initParticipants) {
        logger.debug("UCM: finding all users");
		List users = vital3DAO.findAll(VitalUser.class);
        return decorateUsers(users, initParticipants);
    }
    
    
    /**
     * Finds a term based on its id. Needed because forms refer to terms this way (in dropdown menus, etc).
     */
    public RawUCMTerm findTermById(Long id) {
        logger.debug("UCM: finding term with id " + id );
        return (RawUCMTerm) vital3DAO.findById(RawUCMTerm.class, id);
    }
    
    
    
    public String currentTermName () {
        return "Spring 2010";
    }
    
    public Set currentAcademicYearTermNames() {
        Set result = new HashSet();
        result.add ("Fall 2009");
        result.add ("Spring 2010");
        result.add ("Summer 2010");
        return result;
    }

    public Set currentCalendarYearTermNames() {
        Set result = new HashSet();
        result.add ("Spring 2010");
        result.add ("Summer 2010");
        result.add ("Fall 2010");
        return result;
    }
    
    /*
    NOTE: useful queries for finding and adding terms:
    select ucm_term_id, start_date, end_date, name from ucm_terms order by start_date;
    insert into ucm_terms (ucm_term_id, start_date, end_date, name  )values (14220, '30-AUG-09', '31-DEC-09', 'Fall \
2009');
    insert into ucm_terms (ucm_term_id, start_date, end_date, name  )values (14215, '11-MAY-09', '30-AUG-09', 'Summe\
r 2009');
   COMMIT;
    */

    //List of Vital worksites associated with a term:
    public List worksitesForTerm (String termName) {
        logger.debug("Looking up worksites for term " + termName);
        List terms = vital3DAO.findByPropertyValue(RawUCMTerm.class, "name", termName);
        if (terms.size() == 0) throw new RuntimeException("No Term could be found with name '" + termName + "'");
        RawUCMTerm term =  (RawUCMTerm) terms.get(0);
        Set termWorksites = term.getWorksites();
        if  (termWorksites.size() == 0) return new ArrayList();
        else {
            List vWorksites = findVitalsForRaws(RawUCMWorksite.class, termWorksites);
            List results = new ArrayList (vWorksites);
            return results;
        }
    }
    
    //List of raw worksites associated with a term:
    public List worksitesForTerms (Set termNames) {
        List results = new ArrayList();
        Iterator it = termNames.iterator();
        while (it.hasNext()) {
            String termName = (String) it.next();
            results.addAll (worksitesForTerm(termName));
        }
        return results;
    }
    
    /*
    Pass in a set of VitalWorksites. (No need to decorate them in any way.)
    Get back a list of already-decorated VitalParticipants,
    each of which has Instructor permissions to one of the vitalWorksites passed.
    If the set is null, you get back all the instructors.
    */
    public List getInstructors( Set vWorksites) {
        Set instructorLevels = new HashSet();
        
        // TODO: get these levels directly from the UCM interface OR move them here.
        // instructorLevels.add (new Integer(30)); // TA
        instructorLevels.add (new Integer(40)); // Instructor
        
        List instructors = decorateParticipants(vital3DAO.findBySetOfPropertyValues(VitalParticipant.class, "accessLevel", instructorLevels));
        if (vWorksites == null || vWorksites.size() == 0) {
            return instructors;
        }
        else {
            List associatedInstructors = new ArrayList();
            Iterator instructorIt = instructors.iterator();
            while (instructorIt.hasNext()) {
                VitalParticipant instructor =  ((VitalParticipant) instructorIt.next());
                VitalWorksite vWorksite = instructor.getWorksite();
                if (vWorksites.contains(vWorksite)) associatedInstructors.add (instructor);
            }
            return associatedInstructors;
        }
    }
    
    public List searchForUsers(String substring) {
        Set rawUsers = new HashSet();
        List results = new ArrayList();
        rawUsers.addAll(vital3DAO.findByPropertyValueSubstring(RawUCMUser.class, "firstName", substring));
        rawUsers.addAll(vital3DAO.findByPropertyValueSubstring(RawUCMUser.class, "lastName", substring));
        rawUsers.addAll(vital3DAO.findByPropertyValueSubstring(RawUCMUser.class, "userIdString", substring));
        Iterator it = rawUsers.iterator();
        RawUCMUser rUser;
        VitalUser vUser;
        while (it.hasNext()) {
            rUser = (RawUCMUser) it.next();
            vUser = findVitalUserForRaw(rUser);
            vUser.wrap(rUser);
            results.add(vUser);
        }
        return results;
    
    }
    
    
    /****************** SESSION AND USER - RELATED METHODS ***********************/
    
    
    // For auth systems external to vital's built-in one, this provides a url for the user to
    // to change their password.
    public String getExternalAuthPasswordChangeUrl() {
        return "https://www1.columbia.edu/sec/acis/manageaccount/passwd.html";
    }
    
    // For auth systems external to vital's built-in one, this provides a url for the user to
    // retrieve their forgotten password.
    public String getExternalAuthForgotPasswordUrl() {
        return "https://www1.columbia.edu/sec/acis/manageaccount/forgot.html";
    }
    
    
    /**
     * The goal of loaduser is to see if the user is allowed into vital.
     * First we must add any participant records they do not yet have for their affiliated courses.
     * Then we see if they a) have any participant records, or b) have a global access level. If so...
     * then we call setCLIU and return the user. Otherwise, it will return null.
     */
    public VitalUser loadUser(HttpSession session, String authMethod) {
        
        // get the username from session (came from the auth code):
		String userIdString = (String) session.getAttribute(Vital3Utils.usernameSessionAttributeName);
        logger.info("User " + userIdString + " is attempting to log in.");
        // try to find the user from the database (fully init)
        VitalUser user = findUserByIdString(userIdString, true);
        
        // look through authorities for affiliated courses:
        List affilList = (ArrayList) session.getAttribute(Vital3Utils.affilListSessionAttributeName);
        if (affilList != null) {
            Iterator iter = affilList.iterator();
            while (iter.hasNext()) {
                
                String courseIdString = Vital3Utils.replace ((String) iter.next(), "ROLE_", "");
                logger.info("User has authority " + courseIdString);
                
                // only look at course affiliation authorities
                if (courseIdString.indexOf(".course:") < 0 ) continue;
                
                // get corresponding RawUCMCourse from DB. There should either be 0 or 1, but we double-check for mistakes here
                List courseList =  vital3DAO.findByPropertyValue(RawUCMCourse.class, "courseIdString", courseIdString);
                if (courseList.size() == 1) {
                    RawUCMCourse course = (RawUCMCourse) courseList.get(0);
                    
                    // get the raw worksites affiliated with this course:
                    Set rWorksites = new HashSet();
                    Iterator ucmAffilIter = course.getWorksiteAffils().iterator();
                    while (ucmAffilIter.hasNext()) {
                        RawUCMCourseWorksiteAffil cwAffil = (RawUCMCourseWorksiteAffil) ucmAffilIter.next();
                        rWorksites.add(cwAffil.getWorksite());
                    }
                    
                    Iterator rWorksiteIter = rWorksites.iterator();
                    while (rWorksiteIter.hasNext()) {
                        
                        RawUCMWorksite rWorksite = (RawUCMWorksite)rWorksiteIter.next();
                        logger.info("This course is affiliated with worksite id " + rWorksite.getTitle());
                        // we need to give the user a participant for this worksite if they don't already have one
                        VitalParticipant vParticipant = null;
                        
                        if (user == null) {
                            // if user is null, we need to create the user
                            String firstName = "";
                            String lastName = "";
                            // Turn LDAP lookup off by setting:
                            // <property name="ldapDisabled" value="true"/>
                            // in the Spring bean definition for ColumbiaUCM ( in the Vital3-servlet.xml definition file. )
                            
                            if (useLdap()) {
                                String[] ldapInfo = LDAPLookup.getStudentInfo(userIdString);
                                if ( ldapInfo != null) {
                                    firstName = ldapInfo[2];
                                    lastName = ldapInfo[1];
                                }
                            } 
                            
                            logger.info("This user wasn't in our DB. Creating user with firstname " + firstName + " and lastname " + lastName);
                            user = new VitalUser(userIdString, authMethod, new Integer(0), generateEmailAddressFor(userIdString), null, firstName, lastName);
                            saveUser(user);
                            
                        } else {
                            // see if the user had a participant for this worksite.
                            vParticipant = user.getParticipantForWorksiteIdString(rWorksite.getWorksiteIdString());
                        }
                        
                        // if they don't have a participant, give them one
                        if (vParticipant == null) {
                            
                            // decorate that raw worksite and init participants
                            VitalWorksite vWorksite = findVitalWorksiteForRaw(rWorksite);
                            decorateWorksite(vWorksite, false, true);
                            
                            // construct new participant and save it
                            vParticipant = constructParticipant(user, vWorksite, UserCourseManager.STUDENT_ACCESS);
                            logger.info("This user needs a participant created... creating participant with idString " + vParticipant.getParticipantIdString());
                            saveParticipant(vParticipant);
                        }
                    }
                } else if (courseList.size() > 1 ) throw new RuntimeException("Multiple RawUCMCourses exist for " + courseIdString + "!");
            }
        }
        // Now allow or deny this user:
        if (user != null && (user.getAccessLevel() != null || user.getParticipants().size() > 0)) {
            
            logger.info("User's global access level is " + UserCourseManager.getLabelForAccessLevel(user.getAccessLevel()));
            logger.info("User has " + user.getParticipants().size() + " participant records");
            
            // set timeout to 240 minutes:
            session.setMaxInactiveInterval( 60 * 240 );
            session.setAttribute(Vital3Utils.userIdSessionAttributeName, user.getId());
            return user;
        }
        
        logger.info("user is not allowed in!");
        return null;
    }
    
    
    /**
     * Will perform all necessary operations to clear the user's session information.
     */
    public void logout(HttpSession session) {
        
        logger.debug("logging out user");
        Vital3Utils.clearSessionAttributes(session);
        
        // expire session right away: (suspected to have caused problems, so commented out)
        // session.setMaxInactiveInterval(1);
        
    }
    
    
    /**
     * Gets the currently logged in user from the id stored in the tomcat session. Returns null if there is no CLIU.
     */
    public VitalUser getCLIU(HttpSession mySession, boolean initParticipants) {
        logger.debug("UCM: getCLIU called. initParticipants = " + (initParticipants ? "true" : "false"));
        Long id = (Long) mySession.getAttribute(Vital3Utils.userIdSessionAttributeName);
        if (id == null) {
            logger.debug("CLIU not found.");
            return null;
        }
        VitalUser vUser = (VitalUser) vital3DAO.findById(VitalUser.class, id);
        if (vUser == null) throw new RuntimeException("Could not find user id " + id);
        logger.debug("UCM: getCLIU is calling decorateUser");
        return decorateUser(vUser, initParticipants);
        
    }
    
    
    
    /**************** PRIVATE UTILITY METHODS *********************/
    
    
    private VitalUser findVitalUserInCollection(Collection collection, String idString) {
        VitalUser candidate = null;
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            candidate = (VitalUser) iter.next();
            if (idString.equals(candidate.getUserIdString())) return candidate;
        }
        return null;
    }
    private VitalWorksite findVitalWorksiteInCollection(Collection collection, String idString) {
        VitalWorksite candidate = null;
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            candidate = (VitalWorksite) iter.next();
            if (idString.equals(candidate.getWorksiteIdString())) return candidate;
        }
        return null;
    }
    private VitalParticipant findVitalParticipantInCollection(Collection collection, String idString) {
        VitalParticipant candidate = null;
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            candidate = (VitalParticipant) iter.next();
            if (idString.equals(candidate.getParticipantIdString())) return candidate;
        }
        return null;
    }
    
    /**
     * Use this method to generate the participant id string from a user and a worksite.
     * Originally, this used the userIdString and the worksiteIdString, but in the
     * interest of making this id meaningless, it now uses a completely meaningless unique string.
     */
    private static String generateParticipantIdString(RawUCMUser user, RawUCMWorksite worksite) {
        return Vital3Utils.generateRandomIdString();
    }
    
    /**
     * Use this method to generate the participant id string from a user and a worksite
     * Originally, this used the term name (plus a random string) but in the interest
     * of making this id totally meaningless, it now uses a completely meaningless unique string.
     */
    private static String generateWorksiteIdString(RawUCMTerm term) {
        return Vital3Utils.generateRandomIdString();
    }
    
    
    /***** PRIVATES FOR FINDING A VITAL VERSION FROM A RAW *********/

    /**
     * Will return the undecorated VitalWorksite which corresponds to this raw worksite.
     * In case of error or not found, throws RuntimeException.
     */
    private VitalWorksite findVitalWorksiteForRaw(RawUCMWorksite raw) {
        
        String idString = raw.getWorksiteIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate RawUCMWorksite with null worksiteIdString");
        List results = vital3DAO.findByPropertyValue(VitalWorksite.class, "worksiteIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No VitalWorksite could be found with idString '" + idString + "'");
        return (VitalWorksite) results.get(0);
    }
    
    /**
     * Will return the undecorated VitalParticipant which corresponds to this raw participant.
     * In case of error or not found, throws RuntimeException.
     */
    private VitalParticipant findVitalParticipantForRaw(RawUCMParticipant raw) {
        
        String idString = raw.getParticipantIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate RawUCMParticipant with null participantIdString");
        List results = vital3DAO.findByPropertyValue(VitalParticipant.class, "participantIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No VitalParticipant could be found with idString '" + idString + "'");
        return (VitalParticipant) results.get(0);
    }
    
    /**
     * Will return the undecorated VitalUser which corresponds to this raw user.
     * In case of error or not found, throws RuntimeException.
     */
    private VitalUser findVitalUserForRaw(RawUCMUser raw) {
        
        String idString = raw.getUserIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate RawUCMUser with null userIdString");
        List results = vital3DAO.findByPropertyValue(VitalUser.class, "userIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No VitalUser could be found with idString '" + idString + "'");
        return (VitalUser) results.get(0);
    }
    

    /**
     * Will return the list of vital objects corresponding to each object in your set of raws.
     * If you pass an empty set, this will return an empty set.
     */
    private List findVitalsForRaws(Class rawClass, Collection raws) {
        
        HashSet ids = new HashSet();
        Iterator iter = raws.iterator();
        String idString = null;
        List results = new ArrayList();
        
        if (rawClass.equals(RawUCMWorksite.class)) {
            while (iter.hasNext()) {
                idString = ((RawUCMWorksite)iter.next()).getWorksiteIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate RawUCMWorksite with null worksiteIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(VitalWorksite.class, "worksiteIdString", ids);
            
        } else if (rawClass.equals(RawUCMUser.class)) {
            while (iter.hasNext()) {
                idString = ((RawUCMUser)iter.next()).getUserIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate RawUCMUser with null userIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(VitalUser.class, "userIdString", ids);
            
        } else if (rawClass.equals(RawUCMParticipant.class)) {
            while (iter.hasNext()) {
                idString = ((RawUCMParticipant)iter.next()).getParticipantIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate RawUCMParticipant with null participantIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(VitalParticipant.class, "participantIdString", ids);
            
        } else throw new RuntimeException("invalid class!");
        
        return results;
    }
    
    
    
    /*********** PRIVATES FOR FINDING A RAW VERSION FROM A VITAL VERSION *********/
    
    
    /**
     * Will return the undecorated VitalWorksite which corresponds to this raw worksite.
     * In case of error or not found, throws RuntimeException.
     */
    private RawUCMWorksite findRawForVitalWorksite(VitalWorksite vWorksite) {
        
        String idString = vWorksite.getWorksiteIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate VitalWorksite with null worksiteIdString");
        List results = vital3DAO.findByPropertyValue(RawUCMWorksite.class, "worksiteIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No RawUCMWorksite could be found with idString '" + idString + "'");
        return (RawUCMWorksite) results.get(0);
    }
    
    /**
     * Will return the undecorated VitalParticipant which corresponds to this raw participant.
     * In case of error or not found, throws RuntimeException.
     */
    private RawUCMParticipant findRawForVitalParticipant(VitalParticipant vParticipant) {
        
        String idString = vParticipant.getParticipantIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate VitalParticipant with null participantIdString");
        List results = vital3DAO.findByPropertyValue(RawUCMParticipant.class, "participantIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No RawUCMParticipant could be found with idString '" + idString + "'");
        return (RawUCMParticipant) results.get(0);
    }
    
    /**
     * Will return the undecorated VitalUser which corresponds to this raw user.
     * In case of error or not found, throws RuntimeException.
     */
    private RawUCMUser findRawForVitalUser(VitalUser vUser) {
        
        String idString = vUser.getUserIdString();
        if (idString == null) throw new RuntimeException("Cannot decorate VitalUser with null userIdString");
        List results = vital3DAO.findByPropertyValue(RawUCMUser.class, "userIdString", idString);
        if (results.size() == 0) throw new RuntimeException("No RawUCMUser could be found with idString '" + idString + "'");
        return (RawUCMUser) results.get(0);
    }
    
    /**
     * Will return the list of raws corresponding to each vital object in your set of vitals.
     * If you pass an empty set, this will return an empty set.
     */
    private List findRawsForVitals(Class vitalClass, Collection vitals) {
        
        HashSet ids = new HashSet();
        Iterator iter = vitals.iterator();
        String idString = null;
        List results = new ArrayList();
        
        if (vitalClass.equals(VitalWorksite.class)) {
            while (iter.hasNext()) {
                idString = ((VitalWorksite)iter.next()).getWorksiteIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate worksite with null worksiteIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(RawUCMWorksite.class, "worksiteIdString", ids);
        
        } else if (vitalClass.equals(VitalUser.class)) {
            while (iter.hasNext()) {
                idString = ((VitalUser)iter.next()).getUserIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate user with null userIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(RawUCMUser.class, "userIdString", ids);
            
        } else if (vitalClass.equals(VitalParticipant.class)) {
            while (iter.hasNext()) {
                idString = ((VitalParticipant)iter.next()).getParticipantIdString();
                if (idString == null) throw new RuntimeException("Cannot decorate participant with null participantIdString");
                ids.add(idString);
            }
            results = vital3DAO.findBySetOfPropertyValues(RawUCMParticipant.class, "participantIdString", ids);
       
        } else throw new RuntimeException("invalid class!");
        
        return results;
    }
    
	// AND NOW... FOR THE CRUD.
	
    // vital3dao uses "save" for both updates and inserts, but we want to hide this from the inner Vital classes

    /***** COLLECTION INSERT/UPDATE ******/
	public void insertWorksites(Collection worksites) {
		saveCollection(VitalWorksite.class, worksites);
	}
	public void updateWorksites(Collection worksites) {
		saveCollection(VitalWorksite.class, worksites);
	}
	public void insertUsers(Collection users) {
		saveCollection(VitalUser.class, users);
	}
	public void updateUsers(Collection users) {
		saveCollection(VitalUser.class, users);
	}
	public void insertParticipants(Collection participants) {
		saveCollection(VitalParticipant.class, participants);
	}
	public void updateParticipants(Collection participants) {
		saveCollection(VitalParticipant.class, participants);
	}
    
    /****** SINGULAR INSERT/UPDATE *******/
	public void insertUser(VitalUser user) {
		saveUser( user);
	}
	public void updateUser(VitalUser user) {
		saveUser( user);
	}
	public void insertParticipant(VitalParticipant participant) {
		saveParticipant( participant);
	}
	public void updateParticipant(VitalParticipant participant) {
		saveParticipant( participant);
	}
	public void insertWorksite(VitalWorksite worksite) {
		saveWorksite( worksite);
	}
	public void updateWorksite(VitalWorksite worksite) {
		saveWorksite( worksite);
	}

    /******* PRIVATE SAVE FUNCTIONS *********/
    
    // multi-save
    
    private void saveCollection(Class entityClass, Collection collection) {
        Class rawClass = null;
        if (entityClass.equals(VitalUser.class)) {
            rawClass = RawUCMUser.class;
        } else if (entityClass.equals(VitalParticipant.class)) {
            rawClass = RawUCMParticipant.class;
        } else if (entityClass.equals(VitalWorksite.class)) {
            rawClass = RawUCMWorksite.class;
        }
        Set raws = Vital3Utils.getSetOfRaws(collection);
        vital3DAO.saveCollection(rawClass, raws);
        vital3DAO.saveCollection(entityClass, collection);
        logger.info("saved collection");
    }
    
    // single-saves
    
    private void saveUser(VitalUser user) {
        if (user == null) throw new RuntimeException("cannot save null user");
        RawUCMUser raw = user.getRaw();
        String idString = user.getUserIdString();
        if (idString == null || idString.equals("")) throw new RuntimeException("userIdString required");
        if (!idString.equals(raw.getUserIdString())) throw new RuntimeException("userIdStrings not equal");
        
        vital3DAO.save(RawUCMUser.class, user.getRaw());
        vital3DAO.save(VitalUser.class, user);
		logger.info("saved user "  + user.getFirstName() + " " + user.getLastName());
	}
    
    private void saveParticipant(VitalParticipant participant) {
        if (participant == null) throw new RuntimeException("cannot save null participant");
        RawUCMParticipant raw = participant.getRaw();
        String idString = participant.getParticipantIdString();
        if (idString == null || idString.equals("")) throw new RuntimeException("participantIdString required");
        if (!idString.equals(raw.getParticipantIdString())) throw new RuntimeException("participantIdStrings not equal");
        
        vital3DAO.save(RawUCMParticipant.class, participant.getRaw());
        vital3DAO.save(VitalParticipant.class, participant);
		logger.info("saved participant id " + participant.getParticipantIdString());
	}
    
    private void saveWorksite(VitalWorksite worksite) {
        if (worksite == null) throw new RuntimeException("cannot save null worksite");
        RawUCMWorksite raw = worksite.getRaw();
        String idString = worksite.getWorksiteIdString();
        if (idString == null || idString.equals("")) throw new RuntimeException("worksiteIdString required");
        if (!idString.equals(raw.getWorksiteIdString())) throw new RuntimeException("worksiteIdStrings not equal");
        
        vital3DAO.save(RawUCMWorksite.class, worksite.getRaw());
        vital3DAO.save(VitalWorksite.class, worksite);
		logger.info("saved worksite id " + worksite.getWorksiteIdString());
	}
    
    // all deletes will cascade in the database.
    
    // multi-delete
    
	public void deleteUsers(Collection users) {
	    int number = users.size();
        Iterator iter = users.iterator();
        while (iter.hasNext()) {
            deleteUser((VitalUser)iter.next());
        }
        logger.info("Deleted " + number + " users");
	}
    
	public void deleteParticipants(Collection participants) {
       
        int number = participants.size();
        Iterator iter = participants.iterator();
        while (iter.hasNext()) {
            deleteParticipant((VitalParticipant)iter.next());
        }
        logger.info("Deleted " + number + " participants");
	}
    
    public void deleteWorksites(Collection worksites) {
        int number = worksites.size();
        Iterator iter = worksites.iterator();
        while (iter.hasNext()) {
            deleteWorksite((VitalWorksite)iter.next());
        }
        logger.info("Deleted " + number + " worksites");
	}
    
    // single-delete
    
    
    public void deleteUser(VitalUser user) {
        // fully decorate the vital user:
        decorateUser(user,true);
        RawUCMUser raw = user.getRaw();
        if (raw == null) raw = findRawForVitalUser(user);
        
        // raw participants will be automatically deleted via cascade, but vital participants must be manually deleted
        Set participants = user.getParticipants();
        vital3DAO.deleteCollection(VitalParticipant.class, participants);
        
        vital3DAO.delete(RawUCMUser.class, raw);
        vital3DAO.delete(VitalUser.class, user);
        logger.info("Deleted vitaluser id " + user.getUserIdString());
    }
    
    public void deleteParticipant(VitalParticipant participant) {
        vital3DAO.delete(VitalParticipant.class, participant);
        RawUCMParticipant raw = participant.getRaw();
        if (raw == null) raw = findRawForVitalParticipant(participant);
        vital3DAO.delete(RawUCMParticipant.class, raw);
        logger.info("Deleted participant id " + participant.getParticipantIdString());
    }
    
    public void deleteWorksite(VitalWorksite worksite) {
        // fully decorate the vital worksite:
        decorateWorksite(worksite, true, true);
        RawUCMWorksite raw = worksite.getRaw();
        if (raw == null) raw = findRawForVitalWorksite(worksite);
        
        // delete soon-to-be-orphaned courses:
        Set orphanedCourses = new HashSet();
        Iterator iter = raw.getCourseAffils().iterator();
        while (iter.hasNext()) {
            RawUCMCourseWorksiteAffil cwa = (RawUCMCourseWorksiteAffil) iter.next();
            RawUCMCourse course = cwa.getCourse();
            if (course.getWorksiteAffils().size() == 1) orphanedCourses.add(course);
        }
        vital3DAO.deleteCollection(RawUCMCourse.class, orphanedCourses);
        
        // raw participants will be automatically deleted via cascade, but vital participants must be manually deleted
        Set participants = worksite.getParticipants();
        vital3DAO.deleteCollection(VitalParticipant.class, participants);
        
        vital3DAO.delete(RawUCMWorksite.class, raw);
        vital3DAO.delete(VitalWorksite.class, worksite);
        logger.info("Deleted worksite id " + worksite.getWorksiteIdString());
    }
    
    
    
    
    
    
    ///// integrity checks:
    
    public Set phantomVitalUsers () {
        List vusers = vital3DAO.findAll(VitalUser.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator rit = vital3DAO.findAll(RawUCMUser.class).iterator();
        while (rit.hasNext()) {
            stringSet.add(((RawUCMUser)rit.next()).getUserIdString());
        }
        Iterator it = vusers.iterator();
        while (it.hasNext()) {
            VitalUser nextUser = (VitalUser)it.next();
            if (!stringSet.contains(nextUser.getUserIdString())) result.add(nextUser);
        }
        return result;
    }
    public Set phantomVitalWorksites () {
        List vworksites = vital3DAO.findAll(VitalWorksite.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator rit = vital3DAO.findAll(RawUCMWorksite.class).iterator();
        while (rit.hasNext()) {
            stringSet.add(((RawUCMWorksite)rit.next()).getWorksiteIdString());
        }
        Iterator it = vworksites.iterator();
        while (it.hasNext()) {
            VitalWorksite nextWorksite = (VitalWorksite)it.next();
            if (!stringSet.contains(nextWorksite.getWorksiteIdString())) result.add(nextWorksite);
        }
        return result;
    }
    public Set phantomVitalParticipants () {
        List vparticipants = vital3DAO.findAll(VitalParticipant.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator rit = vital3DAO.findAll(RawUCMParticipant.class).iterator();
        while (rit.hasNext()) {
            stringSet.add(((RawUCMParticipant)rit.next()).getParticipantIdString());
        }
        Iterator it = vparticipants.iterator();
        while (it.hasNext()) {
            VitalParticipant nextParticipant = (VitalParticipant)it.next();
            if (!stringSet.contains(nextParticipant.getParticipantIdString())) result.add(nextParticipant);
        }
        return result;
    }
    public Set phantomRawUCMUsers () {
        List rusers = vital3DAO.findAll(RawUCMUser.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator vit = vital3DAO.findAll(VitalUser.class).iterator();
        while (vit.hasNext()) {
            stringSet.add(((VitalUser)vit.next()).getUserIdString());
        }
        Iterator it = rusers.iterator();
        while (it.hasNext()) {
            RawUCMUser nextUser = (RawUCMUser)it.next();
            if (!stringSet.contains(nextUser.getUserIdString())) result.add(nextUser);
        }
        return result;
    }

    public Set phantomRawUCMParticipants () {
        List rparticipants = vital3DAO.findAll(RawUCMParticipant.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator vit = vital3DAO.findAll(VitalParticipant.class).iterator();
        while (vit.hasNext()) {
            stringSet.add(((VitalParticipant)vit.next()).getParticipantIdString());
        }
        Iterator it = rparticipants.iterator();
        while (it.hasNext()) {
            RawUCMParticipant nextParticipant = (RawUCMParticipant)it.next();
            if (!stringSet.contains(nextParticipant.getParticipantIdString())) result.add(nextParticipant);
        }
        return result;
    }

    public Set phantomRawUCMWorksites () {
        List rworksites = vital3DAO.findAll(RawUCMWorksite.class);
        Set stringSet = new HashSet();
        Set result = new HashSet();
        Iterator vit = vital3DAO.findAll(VitalWorksite.class).iterator();
        while (vit.hasNext()) {
            stringSet.add(((VitalWorksite)vit.next()).getWorksiteIdString());
        }
        Iterator it = rworksites.iterator();
        while (it.hasNext()) {
            RawUCMWorksite nextWorksite = (RawUCMWorksite)it.next();
            if (!stringSet.contains(nextWorksite.getWorksiteIdString())) result.add(nextWorksite);
        }
        return result;
    }
    
    public String getLdapDisabled() { return this.ldapDisabled; }
    public void setLdapDisabled(String ldapDisabled) { this.ldapDisabled = ldapDisabled; }
    private boolean useLdap() { return !this.getLdapDisabled().toLowerCase().equals("true"); }
    
    }

   
    
