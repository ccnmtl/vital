package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.utils.Persistable;

/** 
 * 			A comment on a student's response to an assignment. For now, only instructors are allowed to make comments.
 * 			@author Eddie Rubeiz
 * 		
*/
public class Comment implements Persistable, Serializable, Comparable {
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "comment";
    
    public static final String FEEDBACK = "feedback";
    
    public static final String DISCUSSIONENTRY = "discussion_entry";
    
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    /** identifier field */
    private Long id;

	/** nullable persistent field */
    private Answer answer;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private Integer status;

    /** nullable persistent field */
    private String text;

    /** nullable persistent field */
    private String type;

    /** nullable persistent field */
    private AssignmentResponse assignmentResponse;

    /** nullable persistent field */
    private VitalParticipant participant;

    /** full constructor */
    public Comment(Answer answer, AssignmentResponse assignmentResponse,  VitalParticipant participant, Date dateModified, Integer status, String text, String type) {

		if (assignmentResponse == null && answer == null) throw new RuntimeException("Attempt to create a comment without providing the AssignmentRepsonse or Answer it pertains to.");
        
        if (participant == null ) throw new RuntimeException("Attempt to create a comment without providing a participant.");
        if (participant.getComments() == null ) throw new RuntimeException("Participant's comments were null.");


        this.assignmentResponse = assignmentResponse;
		this.answer = answer;
        this.participant = participant;
        this.dateModified = dateModified;
        this.status = status;
        this.text = text;
        this.type = type;
        

		if (answer != null) answer.getComments().add(this);
        if (assignmentResponse != null) assignmentResponse.getComments().add(this);
        participant.getComments().add(this);
    }

    /** default constructor */
    public Comment() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
		if (assignmentResponse != null) {
			return assignmentResponse.getRelatedWorksite();
		} else if (answer != null) {
			return answer.getRelatedWorksite();
		} else {
			throw new RuntimeException("Attempt to find the worksite for an assignemt.");
		}
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Answer getAnswer() {
        return this.answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    /** 
     * When this comment was last updated.
     */
    public Date getDateModified() {
        return this.dateModified;
    }

	/** 
     * Retrieve a date that can be used to sort responses by when last updated.
	 */
    public Date getDateModifiedForSorting () {
		Date d = getDateModified();
		//there's a bug comparing dates to timestamps, so we're returning a timestamp rather than a plain vanilla date.
		if (d == null ) return new java.sql.Timestamp(0);
		return d;
	}

    // defines the natural order based on ordinalValue property
    public int compareTo(Object obj) {
        Comment o = (Comment)obj;
        return text.compareTo( o.text);
    }
    

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }


    public String getGroupString (String groupType) {
        if (groupType.equals("participant_id")) {
            logger.debug(Long.toString(this.getParticipant().getId().longValue()));
            return Long.toString(this.getParticipant().getId().longValue());
        }
        
        if (groupType.equals("date")) {
            return this.getDateModified().toString().substring(0, 10);
        }
        return "group";
    }

    /** 
     * whether the comment is a draft or submitted.
     */
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public boolean isSubmitted() {
        return this.status != null && this.status.intValue() == 1;
    }

    public boolean isNotSubmitted() {
        return !this.isSubmitted();
    }


    /** 
     * The text of the comment.
     */
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /** 
     * The type of the comment (for now, always set to "feedback"- could change in future.)
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** 
     * The response this comment is about.
     */
    public AssignmentResponse getAssignmentResponse() {
        return this.assignmentResponse;
    }

    public void setAssignmentResponse(AssignmentResponse assignmentResponse) {
        this.assignmentResponse = assignmentResponse;
    }

    /** 
     * The response this comment is about.
     */
    public VitalParticipant getParticipant() {
        return this.participant;
    }

    public void setParticipant(VitalParticipant participant) {
        this.participant = participant;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /** 
     * The response this comment is about.
     */
    public String getEntityType() {
        if (this.assignmentResponse != null ) return "AssignmentResponse";
        if (this.answer != null ) return "Answer";
		return null;
    }

    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (assignmentResponse != null) {
            Set parentCollection = assignmentResponse.getComments();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (participant != null) {
            Set parentCollection = participant.getComments();
            if (parentCollection != null) parentCollection.remove(this);
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
