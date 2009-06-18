package ccnmtl.vital3.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;

/**
 * This is a slightly chaotic implementation of a command class used for BasicAdminController.
 * This command has properties (and getters and setters) for every entity that can be used by basicAdmin!
 * By doing it this way, this one file is kind of overwhelmingly large, but on the other hand, it is
 * a single file/class which does it all. Splitting functionality into separate files would require lots
 * of duplicate code. For now, I'm happy with this style, but if it gets too much maybe I'll split out
 * the entities into separate command classes.
 * In the hopes of reducing clutter, this class should have minimal intelligence. All complex logic should
 * be done elsewhere.
 *
 * This command object is meant to represent the entity being managed, and it therefore has all the properties
 * which that entity would have. It also has a property where it can hold the entity itself. The validator reads
 * the 'id' property of this object and fetches the entity. It then uses the appropriate setter method to set this
 * property here. Because html forms cannot hold objects, related entities are referred to by their ids.
 * Those ids are in form fields (and therefore also in properties) called 'xxxxId' where 'xxxx' is the name of
 * the entity. The validator reads those ids, fetches the entities, and then sets the appropriate properties on
 * this object using the appropriate setter methods.
 * 
 */
public class BasicAdminCommand extends Vital3Command {
    
    
    /************* ENTITY PROPERTY TRANSFERS **************/
    
    /**
     * NOTE: For all transferToXXXX methods:
     * Transfers the relevant non-null properties from this command object to the
     * object you pass. Entity reference properties will not get transferred. For those, get the ids
     * from this command object and then retrieve them separately.
     * Doesn't transfer null properties because blank fields would contain empty-strings, not nulls.
     */


    public Assignment transferToAssignment(Assignment target) {
        // worksite needs to be set manually
        target.setCustomType(customType);
        target.setDateDue(dateDue);
        target.setInstructions(instructions);
        target.setOrdinalValue(ordinalValue);
        target.setTitle(title);
        target.setType(type);
        return target;
    }
    
    /**
     * For all transferFromXXXX methods:
     * Transfers all properties from the object you pass to this command object.
     * Used during display & update actions.
     */

    public void transferFromAssignment(Assignment source) {
        // entity references and ids:
        this.setAssignment(source);
        Unit unit = source.getUnit();
        if (unit != null) {
            this.setUnit(unit);
            this.setUnitId(unit.getId());
        }
        // regular properties:
        this.setCustomType(source.getCustomType());
        this.setDateDue(source.getDateDue());
        this.setInstructions(source.getInstructions());
        this.setOrdinalValue(source.getOrdinalValue());
        this.setTitle(source.getTitle());
        this.setType(source.getType());
    }

    //private Calendar cal = Calendar.getInstance();
    
    public Material transferToMaterial(Material target) {
        // worksite needs to be set manually
        target.setAccessLevel(accessLevel);
        //target.setDateModified(cal.getTime());
        target.setDateModified(new Date());
        //target.setText(text);
        target.setTitle(title);
        target.setType(type);
        target.setUrl(url);
        target.setThumbUrl(thumbUrl);
        return target;
    }

    public void transferFromMaterial(Material source) {
        // entity references and ids:
        this.setMaterial(source);
        VitalWorksite worksite = source.getWorksite();
        if (worksite != null) {
            this.setWorksite(worksite);
            this.setWorksiteId(worksite.getId());
        }
        // regular properties:
        this.setAccessLevel(source.getAccessLevel());
        this.setDateModified(source.getDateModified());
        //this.setText(source.getText());
        this.setThumbUrl(source.getThumbUrl());
        this.setTitle(source.getTitle());
        this.setType(source.getType());
        this.setUrl(source.getUrl());
        // fill out entities array for CFVS:
        this.setChildEntities(0, source.getCustomFieldValues());
    }
    
    
    

    public VitalParticipant transferToParticipant(VitalParticipant target) {
        target.setAccessLevel(accessLevel);
        return target;
    }
    
    public void transferFromParticipant(VitalParticipant source) {
        // entity references and ids:
        this.setParticipant(source);
        VitalUser user = source.getUser();
        if (user != null) {
            this.setUser(user);
            this.setUserId(user.getId());
        }
        VitalWorksite worksite = source.getWorksite();
        if (worksite != null) {
            this.setWorksite(worksite);
            this.setWorksiteId(worksite.getId());
        }
        // regular properties:
        this.setAccessLevel(source.getAccessLevel());
    }
    
    
    
    public VitalWorksite transferToWorksite(VitalWorksite target) {
        // term needs to be set manually
        target.setAnnouncement(announcement);
        target.setTitle(title);
        return target;
    }
    
    public void transferFromWorksite(VitalWorksite source) {
        // entity references and ids:
        this.setWorksite(source);
        RawUCMTerm term = source.getTerm();
        if (term != null) {
            this.setTerm(term);
            this.setTermId(term.getId());
        }
        // regular properties:
        this.setAnnouncement(source.getAnnouncement());
        this.setTitle(source.getTitle());
    }
    

    
    public Unit transferToUnit(Unit target) {
        // worksite needs to be set manually
        target.setDescription(description);
        target.setEndDate(endDate);
        target.setStartDate(startDate);
        target.setTitle(title);
        target.setVisibility(visibility);
        return target;
    }
    
    public void transferFromUnit(Unit source) {
        // entity references and ids:
        this.setUnit(source);
        VitalWorksite worksite = source.getWorksite();
        if (worksite != null) {
            this.setWorksite(worksite);
            this.setWorksiteId(worksite.getId());
        }
        // regular properties:
        this.setDescription(source.getDescription());
        this.setEndDate(source.getEndDate());
        this.setStartDate(source.getStartDate());
        this.setTitle(source.getTitle());
        this.setVisibility(source.getVisibility());
    }
    
    
    public CustomField transferToCustomField(CustomField target) {
    	
        target.setName(name);
        target.setOrdinalValue(ordinalValue);
        target.setVisibility(visibility);
        return target;
    }
    
    public void transferFromCustomField(CustomField source) {
        // entity references and ids:
        this.setCustomField(source);
        VitalWorksite worksite = source.getWorksite();
        if (worksite != null) {
            this.setWorksite(worksite);
            this.setWorksiteId(worksite.getId());
        }
        // regular properties:
        this.setName(source.getName());
        this.setOrdinalValue(source.getOrdinalValue());
        this.setVisibility(source.getVisibility());
        
    }
    
    public Question transferToQuestion(Question target) {
        target.setText(text);
        target.setOrdinalValue(ordinalValue);
        return target;
    }

    public void transferFromQuestion(Question source) {
        // entity references and ids:
        this.setQuestion(source);
        Assignment assignment = source.getAssignment();
        if (assignment != null) {
            this.setAssignment(assignment);
            this.setAssignmentId(assignment.getId());
        }
        // regular properties:
        this.setText(source.getText());
        this.setOrdinalValue(source.getOrdinalValue());
        
    }
    
    
   public VitalUser transferToUser(VitalUser target) {
       target.setUserIdString(userIdString);
       target.setAuthMethod(authMethod);
       target.setAccessLevel(accessLevel);
       target.setEmail(email);
       target.setPassword(password);
       target.setFirstName(firstName);
       target.setLastName(lastName);
       return target;
    }
    
    public void transferFromUser(VitalUser source) {
        this.setUserIdString(source.getUserIdString());
        this.setAuthMethod(source.getAuthMethod());
        this.setAccessLevel(source.getAccessLevel());
        this.setEmail(source.getEmail());
        this.setPassword(source.getPassword());
        this.setFirstName(source.getFirstName());
        this.setLastName(source.getLastName());
    }
    
    
    /************** DUMMY METHODS *******************/
    
    /**
     * Note for all dummy methods: These dummies come in handy if you are setting up a "new entity" form and you need a parent
     * entity to stick into the model (usually for dropdown-items).
     * UCM Entity types should use "convenient" constructors. Entity types with no children can use empty constructors.
     * NOTE: You don't HAVE to write one of these for every entity type... do it as the need arises.
     */

    
    public Material setupDummyMaterial() {
        Material dummy = new Material(null, null, null, null, null, null, null, null, new HashSet(), new HashSet(), new TreeSet(), new HashSet(), new HashSet());
        dummy.setId(new Long(-1));
        this.setMaterial(dummy);
        return dummy;
    }

    
    public RawUCMTerm setupDummyTerm() {
        RawUCMTerm dummy = new RawUCMTerm(null, null, null, new HashSet());
        dummy.setId(new Long(-1));
        this.setTerm(dummy);
        return dummy;
    }
    
    public Unit setupDummyUnit() {
        Unit dummy = new Unit(null, null, null, null, null, null, new TreeSet(), new HashSet());
        dummy.setId(new Long(-1));
        this.setUnit(dummy);
        return dummy;
    }
    
    public VitalUser setupDummyUser() {
        VitalUser dummy = new VitalUser(null, null, null, null, null, null, null);
        dummy.setId(new Long(-1));
        this.setUser(dummy);
        return dummy;
    }
    
    public VitalWorksite setupDummyWorksite() {
        VitalWorksite dummy = new VitalWorksite(null, null, setupDummyTerm(), null);
        dummy.setId(new Long(-1));
        this.setWorksite(dummy);
        return dummy;
    }
    

    
    /*********** ENTITY GETTERS AND SETTERS ********/
    
    protected String entity;
    
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity=entity; }
    
    
    public Annotation getAnnotation() {
        return (Annotation) mapGet("annotation"); 
    }
    public void setAnnotation(Annotation obj) {
        mapSet("annotation", obj);
    }
    
    public Answer getAnswer() {
        return (Answer) mapGet("answer"); 
    }
    public void setAnswer(Answer obj) {
        mapSet("answer", obj);
    }
    
    public Assignment getAssignment() {
        return (Assignment) mapGet("assignment"); 
    }
    public void setAssignment(Assignment obj) {
        mapSet("assignment", obj);
    }
    
    public AssignmentResponse getAssignmentResponse() {
        return (AssignmentResponse) mapGet("assignmentResponse"); 
    }
    public void setAssignmentResponse(AssignmentResponse obj) {
        mapSet("assignmentResponse", obj);
    }
    
    public Comment getComment() {
        return (Comment) mapGet("comment"); 
    }
    public void setComment(Comment obj) {
        mapSet("comment", obj);
    }
    
    public CustomField getCustomField() {
        return (CustomField) mapGet("customField"); 
    }
    public void setCustomField(CustomField obj) {
        mapSet("customField", obj);
    }
    
    public Material getMaterial() {
        return (Material) mapGet("material"); 
    }
    public void setMaterial(Material obj) {
        mapSet("material", obj);
    }
    
    public VitalParticipant getParticipant() {
        return (VitalParticipant) mapGet("participant"); 
    }
    public void setParticipant(VitalParticipant obj) {
        mapSet("participant", obj);
    }
    
    public Question getQuestion() {
		logger.debug ("Retrieving question from the command.");
        return (Question) mapGet("question");
    }
    public void setQuestion(Question obj) {
		logger.debug ("Setting Question to " + obj);
        mapSet("question", obj);
    }
    
    public RawUCMTerm getTerm() {
        return (RawUCMTerm) mapGet("term"); 
    }
    public void setTerm(RawUCMTerm obj) {
        mapSet("term", obj);
    }
    
    public Unit getUnit() {
        return (Unit) mapGet("unit"); 
    }
    public void setUnit(Unit obj) {
        mapSet("unit", obj);
    }
    
    public VitalUser getUser() {
        return (VitalUser) mapGet("user"); 
    }
    public void setUser(VitalUser obj) {
        mapSet("user", obj);
    }
    
    /********** RELATED ENTITY ID GETTERS AND SETTERS ************/
    
    // (ViewMaterialController)
    public Long getAnnotationId() {
        return (Long) mapGet("annotationId");
    }
    public void setAnnotationId(Long value) {
		logger.debug ("Setting annotation ID to " + value);
        mapSet("annotationId", value);
    }
    
    // Comment, Answer
    public Long getAnswerId() {
        return (Long) mapGet("answerId");
    }
    public void setAnswerId(Long value) {
        logger.debug ("Setting answer ID to " + value);
        mapSet("answerId", value);
    }
    
    // AssignmentResponse
    public Long getAssignmentId() {
        return (Long) mapGet("assignmentId");
    }
    public void setAssignmentId(Long value) {
		logger.debug ("Setting assignment ID to " + value);
        mapSet("assignmentId", value);
    }
    
    // Comment, Answer
    public Long getAssignmentResponseId() {
        return (Long) mapGet("assignmentResponseId");
    }
    public void setAssignmentResponseId(Long value) {
		logger.debug ("Setting assignment response ID to " + value);
        mapSet("assignmentResponseId", value);
    }
    
    // Comment
    public Long getCommentId() {
        return (Long) mapGet("commentId");
    }
    public void setCommentId(Long value) {
		logger.debug ("Setting comment ID to " + value);
        mapSet("commentId", value);
    }
    
    // CustomFieldValue
    public Long getMaterialId() {
        return (Long) mapGet("materialId");
    }
    public void setMaterialId(Long value) {
        mapSet("materialId",value);
    }
    
    // Annotation
    public Long getParticipantId() {
        return (Long) mapGet("participantId");
    }
    public void setParticipantId(Long value) {
		logger.debug ("Setting participant ID to " + value);
        mapSet("participantId", value);
    }
    
    // AssignmentResponse (guided lessons only)
    public Long getQuestionId() {
        return (Long) mapGet("questionId");
    }
    public void setQuestionId(Long value) {
		logger.debug ("Setting question ID to " + value);
        mapSet("questionId", value);
    }
    
    // Worksite
    public Long getTermId() {
        return (Long) mapGet("termId");
    }
    public void setTermId(Long value) {
        mapSet("termId", value);
    }
    
    // Assignment
    public Long getUnitId() {
        return (Long) mapGet("unitId");
    }
    public void setUnitId(Long value) {
        mapSet("unitId", value);
    }
    
    // Participant
    public Long getUserId() {
        return (Long) mapGet("userId");
    }
    public void setUserId(Long value) {
        mapSet("userId", value);
    }
    
    /************** PROPERTY GETTERS AND SETTERS ***************/
    
    private String authMethod;
    private Integer accessLevel;
    private String announcement;
    private String clipBegin;
    private String clipEnd;
    private String courseIdString;
    private String courseIdStringDisplay;
    private String customType;
    private Date dateDue;
    private Date dateModified;
    private Date dateSubmitted;
    private String description;
    private Date endDate;
    private String email;
    private String firstName;
    private String instructions;
    private String lastName;
    private String name;
    private Integer ordinalValue;
    private String participantIdString;
    private String password;
    private Date startDate;
    private String text;
    private String thumbUrl;
    private String title;
    private String type;
    private String url;
    private String userIdString;
    private Integer visibility;
    private Integer status;
    
    // *= Not a real property of this entity type
    
    // Material, Participant, User
    public Integer getAccessLevel() { return accessLevel; }
    public void setAccessLevel(Integer value) { this.accessLevel=value; }
    
    // Worksite
    public String getAnnouncement() { return announcement; }
    public void setAnnouncement(String value) { this.announcement = value; }
    
    // User*
    public String getAuthMethod() { return authMethod; }
    public void setAuthMethod(String value) { this.authMethod = value; }
   
    // Annotation
    public String getClipBegin() { return clipBegin; }
    public void setClipBegin(String value) { this.clipBegin = value; }
    
    // Annotation
    public String getClipEnd() { return clipEnd; }
    public void setClipEnd(String value) { this.clipEnd = value; }
    
    // Worksite*
    public String getCourseIdString() { return courseIdString; }
    public void setCourseIdString(String value) { this.courseIdString = value; }
    
    // Worksite*
    public String getCourseIdStringDisplay() { return courseIdStringDisplay; }
    public void setCourseIdStringDisplay(String value) { this.courseIdStringDisplay = value; }
    
    // Assignment
    public String getCustomType() { return customType; }
    public void setCustomType(String value) { this.customType = value; }
    
    // Assignment
    public Date getDateDue() { return dateDue; }
    public void setDateDue(Date value) { this.dateDue = value; }

    // AssignmentResponse
    public Date setDateSubmitted() { return dateSubmitted; }
    public void setDateSubmitted(Date value) { this.dateSubmitted = value; }
    
    // Material
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date value) { this.dateModified = value; }
    
    // Unit
    public String getDescription() { return description; }
    public void setDescription(String value) { this.description = value; }
    
    // Unit
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date value) { this.endDate = value; }
    
    // User
    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
    
    // User
    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { this.firstName = value; }
    
    // Assignment
    public String getInstructions() { return instructions; }
    public void setInstructions(String value) { this.instructions = value; }    
    
    // User
    public String getLastName() { return lastName; }
    public void setLastName(String value) { this.lastName = value; }
    
    // CustomField
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    
    // Assignment, CustomField, Question
    public Integer getOrdinalValue() { return ordinalValue; }
    public void setOrdinalValue(Integer value) { this.ordinalValue=value; }
    
    // Participant
    public String getParticipantIdString() { return participantIdString; }
    public void setParticipantIdString(String value) { this.participantIdString = value; }
    
    // User
    public String getPassword() { return password; }
    public void setPassword(String value) { this.password = value; }
    
    // Unit
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date value) { this.startDate = value; }
    
    // Material, AssignmentResponse, Question
    public String getText() { return text; }
    public void setText(String value) { this.text = value; }
    
    // Material
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String value) { this.thumbUrl = value; }
    
    // Assignment, Material, Worksite, Unit
    public String getTitle() { return title; }
    public void setTitle(String value) { this.title = value; }
    
    // Assignment, Material, (ViewMaterialController)
    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
    
    // Material
    public String getUrl() { return url; }
    public void setUrl(String value) { this.url = value; }
    
    // User
    public String getUserIdString() { return userIdString; }   
    public void setUserIdString(String value) { this.userIdString = value; }
    
    // Unit, CustomField
    public Integer getVisibility() { return visibility; }
    public void setVisibility(Integer value) { this.visibility=value; }

    // AssignmentResponse
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { this.status=value; }
    
}
