package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.ucm.RawUCMWorksite;
import ccnmtl.vital3.ucm.RawUCMTerm;
import ccnmtl.vital3.ucm.RawUCMCourseWorksiteAffil;
import ccnmtl.vital3.ucm.RawUCMParticipant;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.RawWrapper;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 * A Worksite is a self-sufficient area of VITAL. It must have a defined duration and a specified list of members who may use the worksite.
 * If this worksite will be used for a course, an external course provider may be used (in the future) to provide the course info. The
 * "worksiteIdString" property should be a globally unique string which identifies this worksite throughout the institution in the real world,
 * not simply in this database.
 * NOTE: Instances have a method "getCourseIdStringList" which gets a list of courseIdStrings of all related courses. To modify these relationships,
 * use the appropriate UCM methods.
 *
 * @author Eric Mattes
 * 		
*/
public class VitalWorksite implements Persistable, RawWrapper, Serializable {


    protected final Log logger = LogFactory.getLog(getClass());

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "worksite";
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private String worksiteIdString;
    /** nullable persistent field */
    private String announcement;
    /** persistent field */
    private Set customFields;
    /** persistent field */
    private Set materials;
    
    /** The raw worksite which this VitalWorksite object wraps. **/
    private RawUCMWorksite raw;
    // set of VitalCourseWorksiteAffils:
    // commented out because we are using getCourseIdStringList instead
    //private Set courseAffils;
    // set of VitalParticipants:
    private Set participants;

	private Set units;
    
    // ArrayList of courseIdStrings for affiliated Courses:
    private ArrayList courseIdStrings;
        
    
	/** full constructor */
    public VitalWorksite(String worksiteIdString, String announcement, Set customFields, Set materials, Set units) {
        this.worksiteIdString = worksiteIdString;
        this.announcement = announcement;
        this.customFields = customFields;
        this.materials = materials;
        
        this.raw = null;
    }

    /** convenient constructor: designed for manual VitalWorksite creation. Will construct and wrap a new RawUCMWorksite. **/
    public VitalWorksite(String worksiteIdString, String announcement, RawUCMTerm term, String title) {
        
        this(worksiteIdString, announcement, new TreeSet(), new HashSet(), new TreeSet());
        // set up the rawWorksite:
        RawUCMWorksite raw = new RawUCMWorksite(worksiteIdString, term, title);
        this.wrap(raw);
        
        // give it a new empty set for participants, units and materials:
        this.participants = new HashSet();
        this.units = new HashSet();
        this.materials = new HashSet();

    }
    
    /** default constructor */
    public VitalWorksite() {
    }
    
    
    // add a participant, without needing to fear the null collection:
    public void addParticipant(VitalParticipant vParticipant) {
        
        if (participants == null) participants = new HashSet();
        participants.add(vParticipant);
        
    }
    
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        throw new UnsupportedOperationException("This is a VitalWorksite.");
    }
    
    /**
     * Gets the courseIdString of the first affiliated course.
     * Returns null if no courses are affiliated.
     */
    public String getCourseIdString() {
        String courseIdString = null;
        Set courseAffils = this.raw.getCourseAffils();
        Iterator affilIter = courseAffils.iterator();
        if (affilIter.hasNext()) {
            RawUCMCourseWorksiteAffil affil = (RawUCMCourseWorksiteAffil) affilIter.next();
            courseIdString = affil.getCourse().getCourseIdString();
        }
        return courseIdString;
    }
    
    /**
     * Gets the user's access level to this worksite.
     */
    public String getAccessLevel(VitalUser user) {
		if (user == null) throw new RuntimeException("Null user passed.");
		String uid = user.getUserIdString();
		if (this.getParticipants() == null) throw new RuntimeException("Participants not initialized");
		Iterator participantIter = this.getParticipants().iterator();
		while (participantIter.hasNext()) {
			VitalParticipant vParticipant = (VitalParticipant) participantIter.next();
			if ((vParticipant.getUser().getUserIdString()).equals(uid)) {
				return (UserCourseManager.getLabelForAccessLevel(vParticipant.getAccessLevel()));
			}
		}
        // did not find participant... check for user's sitewide access level:
        Integer userAccess = user.getAccessLevel();
        if (userAccess != null && userAccess.compareTo(UserCourseManager.PUBLIC_ACCESS) > 0)
            return UserCourseManager.getLabelForAccessLevel(userAccess);
        return null;
    }
    

    /**
     * Gets a List of courseIdStrings which are taken from the raw worksite's course affiliations
     */
    public ArrayList getCourseIdStringList() {
        // put together a List:
        ArrayList courseIdStrings = new ArrayList();
        Iterator courseIter = this.raw.getCourseAffils().iterator();
        while (courseIter.hasNext()) {
            RawUCMCourseWorksiteAffil affil = (RawUCMCourseWorksiteAffil) courseIter.next();
            courseIdStrings.add(affil.getCourse().getCourseIdString());
        }
        return courseIdStrings;
    }
    
    /** 
    * Returns the materials sorted by title. 75% sure that they reference the original materials rather than copies.
    */
    public ArrayList getMaterialsSortedByTitle() {
        OmniComparator materialComp = new OmniComparator(Material.class, "getTitle");
        ArrayList sortedMaterials = new ArrayList(this.materials);
        Collections.sort(sortedMaterials, materialComp);
        return sortedMaterials;
    }
    
    
    /** 
	   * Sort the units inside a worksite by startDate, endDate.
	   * @return The units sorted 
	   */
    public ArrayList getUnitsSortedByDate() {

        OmniComparator unitComp_first = new OmniComparator(Unit.class, "getStartDate", null);
        
        OmniComparator unitComp_second = new OmniComparator(Unit.class, "getEndDate", null);
        unitComp_first.setSecondaryComparator(unitComp_second);
   			
        // Sort the units by StartDate property, if tie, sort by EndDate property.
        ArrayList sortedUnits = new ArrayList(this.units);
        Collections.sort(sortedUnits, unitComp_first);

        return sortedUnits;
    }
    
    
    /**
     * Returns the number of custom fields for this worksite. Requires custom fields to have already been initialized.
     *@return The number of custom fields for this worksite.
     */
    public int getNumCustomFields() {
        return this.customFields.size();
    }
    
    /**
     * Returns a list of Custom Field names (Strings) in the proper order
     */
    public ArrayList getCustomFieldNames() {
        ArrayList list = new ArrayList();
        Iterator iter = customFields.iterator();
        while (iter.hasNext()) {
            CustomField cf = (CustomField) iter.next();
            list.add(cf.getName());
        }
        return list;
    }


    /**
     * Returns a list of Custom Field names sorted by ordinalValue
     * @return sorted CustomFieldNames
     
     public ArrayList getSortedCustomFieldNames() {
        ArrayList sortedCustomFieldNames = new ArrayList();

        ArrayList customFields = getCustomFieldList();
        OmniComparator cfnComp = new OmniComparator(CustomField.class, "getOrdinalValue");
        Collections.sort(customFields, cfnComp);
        
        Iterator iter = customFields.iterator();
        while (iter.hasNext()) {
            CustomField cf = (CustomField) iter.next();
            sortedCustomFieldNames.add(cf.getName());
        }

        return sortedCustomFieldNames;
    }
    */
    
    /**
     * Returns Custom Fields as a List (because templates don't like Sets)
     */
    public ArrayList getCustomFieldList() {
        ArrayList list = new ArrayList(customFields);
        return list;
    }
    
    /**
     * Get a related customField by its id. If none of the related cfs have that id, null will be returned.
     */
    public CustomField getCustomFieldById(Long id) {
        Iterator iter = customFields.iterator();
        while (iter.hasNext()) {
            CustomField cf = (CustomField) iter.next();
            if (cf.getId().equals(id)) return cf;
        }
        return null;
    }
    
    /**
     * Create CFVs which are blank and all refer to a dummy material. Used for "new material" form.
     * The CFV constructor will add these CFVs to each CF's "values" collection, but that won't be persisted.
     */
    public ArrayList createFakeCFVs() {
        
        ArrayList cfvList = new ArrayList();
        
        Material dummyMaterial = new Material();
        dummyMaterial.setCustomFieldValues(new TreeSet());
        int ordinalVal = 1;
        
        Iterator cfIter = customFields.iterator();
        while (cfIter.hasNext()) {
            CustomField cf = (CustomField) cfIter.next();
            cfvList.add(new CustomFieldValue(cf, dummyMaterial, ordinalVal, ""));
            ordinalVal++;
        }
        
        return cfvList;
    }
    
    /** 
     * The term during which this worksite is held. May not apply to all worksites.
     * Refers to the RawUCMWorksite's RawUCMTerm.
     */
    public RawUCMTerm getTerm() {
        if (raw == null) throw new RuntimeException("VitalWorksite did not contain a Raw Worksite. Cannot get term.");
        return raw.getTerm();
    }
    public void setTerm(RawUCMTerm term) {
        if (raw == null) throw new RuntimeException("VitalWorksite did not contain a Raw Worksite. Cannot set term.");
        raw.setTerm(term);
    }
    
    /** 
     * The title of the worksite
     */
    public String getTitle() {
        if (raw == null) throw new RuntimeException("VitalWorksite did not contain a Raw Worksite. Cannot get title.");
        return raw.getTitle();
    }
    public void setTitle(String title) {
        if (raw == null) throw new RuntimeException("VitalWorksite did not contain a Raw Worksite. Cannot set title.");
        raw.setTitle(title);
    }
    
    
    /**
     * Gets the name of the associated Term.
     */
	public String getTermName() {
		if (this.getTerm() == null) throw new RuntimeException("Term not initialized");
		return this.getTerm().getName();
	}
    
    /** 
    * The VitalParticipants belonging to this Worksite.
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
    
	/** 
    * The Units belonging to this Worksite.
    */
    public Set getUnits() {
        return this.units;
    }
    public void setUnits(Set units) {
        this.units = units;
    }
    
    
    /////////// STANDARD GETTERS AND SETTERS ////////////
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * A message which will be displayed prominently on the worksite page.
     */
    public String getAnnouncement() {
        return this.announcement;
    }
    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }
    
    /** 
     * A string uniquely identifying this worksite, potentially for use outside of the database. This is NOT the course id string!
     */
    public String getWorksiteIdString() {
        return this.worksiteIdString;
    }
    public void setWorksiteIdString(String worksiteIdString) {
        this.worksiteIdString = worksiteIdString;
    }
    
    /** 
     * CustomFields that belong to this Worksite.
     */
    public Set getCustomFields() {
        return this.customFields;
    }
    public void setCustomFields(Set customFields) {
        this.customFields = customFields;
    }
    
    /** 
     * The Materials belonging to this Worksite.
     */
    public Set getMaterials() {
        return this.materials;
    }
    public void setMaterials(Set materials) {
        this.materials = materials;
    }
    
    /**
     * Set the raw worksite object.
     */
    public void setRaw(RawUCMWorksite raw) {
        this.raw = raw;
    }
    public RawUCMWorksite getRaw() {
        return this.raw;
    }
    
    /**
     * For generic collections operations. Implemented for RawWrapper compliance.
     */
    public Object getRawObject() {
        return this.raw;
    }
    
    /**
     * Wrap the given Raw object, populating with courseIdStrings too.
     *@param raw         the RawUCMWorksite object to wrap.
     */
    public void wrap(RawUCMWorksite raw) {
        this.raw = raw;
    }
    
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("title", getTitle()).toString();
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new VitalWorksite[]{ this } );
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
        children.addAll(customFields);
        children.addAll(materials);
        children.addAll(units);
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
