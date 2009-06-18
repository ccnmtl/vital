package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.utils.OmniComparator;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;
import java.lang.reflect.*;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

/** 
 * 			A student's response to an assignment. This will take the form either of:
 
 * an essay, the text of which is to be stored in the 'text' field

 * a guided lesson (in which case there will be at least an answer for each question in the guided lesson assignment.

 * a discussion response, which is used to store all the entries the participant has submitted to the discussion assignment, each of which corresponds to a Comment object.

 * 			@author Eddie Rubeiz
 * 		
*/
public class AssignmentResponse implements Persistable, Serializable {
    
    protected final Log logger = LogFactory.getLog(getClass());

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "assignmentResponse";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Date dateSubmitted;

    /** nullable persistent field */
    private Integer status;


    /** nullable persistent field */
    private String text0;
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private String text5;
    private String text6;
    private String text7;
    private String text8;
    private String text9;
    private String text10;
    private String text11;
    private String text12;
    private String text13;
    private String text14;
    private String text15;
    

    static private final int CHARSPERSEGMENT = 4000;
    static private final int TOTALSEGMENTS = 16;
    static public final int ESSAY_CAPACITY = CHARSPERSEGMENT * TOTALSEGMENTS;
    
    private void checkSegment ( int i) {
        if (i < 0 || i > this.TOTALSEGMENTS - 1) {
            logger.warn ("Illegal segment : requested index " + i + " and total number is " + (this.TOTALSEGMENTS - 1));
            throw new RuntimeException("That segment doesn't exist.");
        }
    }   

    private Method getTextGetter (int index) {
        checkSegment(index);
        Method method = null;
        if (index < 0 || index > this.TOTALSEGMENTS - 1) throw new RuntimeException("That segment doesn't exist.");
        try {
            method = (AssignmentResponse.class).getMethod("getText" + index, new Class[] {});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("That segment doesn't exist.");
        }
        return method;
    }

    private Method getTextSetter (int index) {
        checkSegment(index);
        Method method = null;
        try {
            method = (AssignmentResponse.class).getMethod("setText" + index, new Class[] {String.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("That segment doesn't exist.");
        }
        return method;
    }

    
  
    /** nullable persistent field */
    private Assignment assignment;

    /** nullable persistent field */
    private VitalParticipant participant;

    /** persistent field */
    private Set answers;

    /** persistent field */
    private Set comments;

    /** full constructor */
    public AssignmentResponse(Assignment assignment, VitalParticipant participant, Date dateSubmitted, Integer status, String text, Set answers, Set comments) {
        this.assignment = assignment;
        this.participant = participant;
        this.dateSubmitted = dateSubmitted;
        this.status = status;
        this.setText(text);
        this.answers = answers;
        this.comments = comments;
        
        assignment.getResponses().add(this);
        participant.getAssignmentResponses().add(this);
    }
    
    /** convenient constructor */
    public AssignmentResponse(Assignment assignment, VitalParticipant participant, Date dateSubmitted, int status, String text) {
        this(assignment, participant, dateSubmitted, new Integer(status), text, new HashSet(), new HashSet());
    }

    /** default constructor */
    public AssignmentResponse() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return assignment.getRelatedWorksite();
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    
    /** 
    * Indicates whether this is an essay response, a guided lesson response, or a discussion response.
    */
    public String getType() {
        return this.assignment.getType();
    }
    
    /** 
     * When this response was submitted.
     */
    public Date getDateSubmitted() {
        return this.dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }


    /** 
     * Whether the response has been submitted.
     */
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 
     * Convenience methods- make code much easier to read.
     */
    public boolean isSubmitted() {
        return this.status != null && this.status.intValue() == 1;
    }

    public boolean isNotSubmitted() {
        return !this.isSubmitted();
    }

    /** 
    * true if this assignmentresponse was submitted by student p.     
    */
    public boolean belongsTo (VitalParticipant p) {
    
        if (this.getParticipant() == null) throw new RuntimeException("No participant associated with this assignmentResponse.");
        if (p == null) throw new RuntimeException("Null participant passed");
        return this.getParticipant().getId().intValue() == p.getId().intValue();
    }


    /** 
     * In an essay assignment, this field contains the actual essay. If this is the response to a guided lesson, this field is null, and the response consists of a series of answers (see 'answers' below). If this is the response to a discussion assignment, again, the field is null, but the discussion entries are stored as Comment objects.
     */
     
    public String getText() {
        StringBuffer textBuffer = new StringBuffer();
        Object obj;
        int segment;
        for (segment = 0; segment < this.TOTALSEGMENTS; segment++) {
            obj = null;
            Method getter = this.getTextGetter(segment);
            try {
                obj =  getter.invoke(this, new Object[] {});
            } catch (IllegalAccessException e) {
                throw new RuntimeException("That segment doesn't exist.");
            } catch (InvocationTargetException e) { 
                throw new RuntimeException("Unable to invoke that method..");
            }
            if (obj != null ) textBuffer.append ((String) obj);
        }
        //  logger.debug("getText returned " + textBuffer.length() + "chars." );
        return textBuffer.toString();
    }

    public void setText(String text) {
        List segments = Vital3Utils.segmentString (text, this.CHARSPERSEGMENT, this.TOTALSEGMENTS);
        Iterator iter = segments.iterator();
        int segment;
        for (segment = 0; segment < this.TOTALSEGMENTS; segment++) {
            //  logger.debug ("Starting segment " + segment );
            String temp = null;
            if (segment <  segments.size()) temp = (String) segments.get(segment);
            Method setter = this.getTextSetter(segment);
            try {
                Object result = setter.invoke(this, new Object[] {temp});
            } catch (IllegalAccessException e) {
                throw new RuntimeException("That segment doesn't exist.");
            } catch (InvocationTargetException e) { 
                throw new RuntimeException("Unable to invoke that method..");
            }
        }
    }
    
    public String getText0() {
        return this.text0;
    }

    public void setText0(String text) {
        this.text0 = text;
    }

////

    public String getText1() {
        return this.text1;
    }

    public void setText1(String text) {
        this.text1 = text;
    }

////

    public String getText2() {
        return this.text2;
    }

    public void setText2(String text) {
        this.text2 = text;
    }

////

    public String getText3() {
        return this.text3;
    }

    public void setText3(String text) {
        this.text3 = text;
    }

////

    public String getText4() {
        return this.text4;
    }

    public void setText4(String text) {
        this.text4 = text;
    }

////

    public String getText5() {
        return this.text5;
    }

    public void setText5(String text) {
        this.text5 = text;
    }

////

    public String getText6() {
        return this.text6;
    }

    public void setText6(String text) {
        this.text6 = text;
    }

////


    public String getText7() {
        return this.text7;
    }

    public void setText7(String text) {
        this.text7 = text;
    }

////


    public String getText8() {
        return this.text8;
    }

    public void setText8(String text) {
        this.text8 = text;
    }

////


    public String getText9() {
        return this.text9;
    }

    public void setText9(String text) {
        this.text9 = text;
    }

////

    public String getText10() {
        return this.text10;
    }

    public void setText10(String text) {
        this.text10 = text;
    }

////

    public String getText11() {
        return this.text11;
    }

    public void setText11(String text) {
        this.text11 = text;
    }

////

    public String getText12() {
        return this.text12;
    }

    public void setText12(String text) {
        this.text12 = text;
    }

////

    public String getText13() {
        return this.text13;
    }

    public void setText13(String text) {
        this.text13= text;
    }

////

    public String getText14() {
        return this.text14;
    }

    public void setText14(String text) {
        this.text14 = text;
    }

////

    public String getText15() {
        return this.text15;
    }

    public void setText15(String text) {
        this.text15 = text;
    }

////






    /** 
     * The Assignment this is in response to.
     */
    public Assignment getAssignment() {
        return this.assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /** 
     * The participant who made the response.
     */
    public VitalParticipant getParticipant() {
        return this.participant;
    }

    public void setParticipant(VitalParticipant participant) {
        this.participant = participant;
    }

    /** 
     * If this is the response to a guided lesson, the set of answers.
     */
    public Set getAnswers() {
        return this.answers;
    }

    public void setAnswers(Set answers) {
        this.answers = answers;
    }

    /** 
     * Any comments other participants may have submitted in relation to this essay response.
     */
    public Set getComments() {
        return this.comments;
    }

    public void setComments(Set comments) {
        this.comments = comments;
    }

    /** 
     * Any comments other participants may have submitted in relation to this essay response.
     */
    public List getCommentsMostRecentFirst() {
        ArrayList l = new ArrayList(this.comments);
        if( l.size() > 1) {
			OmniComparator oc = new OmniComparator(Comment.class, "getDateModifiedForSorting", null);
			oc.reverseSortOrder();
			try {
				Collections.sort(l, oc);
			} catch(Exception e) {
				logger.warn("Attempt to sort comments by date triggered " + e);
			}
		}
        return l;
    }

     
    public Set getDiscussionEntries() {
        //check this is a discussion.
        if (! this.assignment.isDiscussion()){
			throw new RuntimeException("Attempt to get discussion entries for a non-Discussion assignment. Bad."); 
		}
        Set allEntries = new HashSet();
        Iterator iter = this.comments.iterator();
		while(iter.hasNext()) {
			Comment c = (Comment) iter.next();
			logger.debug(c);
			allEntries.add(c);
		}
        return allEntries;
    }
    

    /** 
     * Retrieves a comment submitted by a specific participant.
     */
    public Comment getComment(VitalParticipant participant) {
		if (this.assignment.isEssay()) {
			//logger.debug ("Examining comments for assignment response " + this + " and participant " + participant);
			if (participant == null) throw new RuntimeException("Participant passed to getComment for " + this +  "was null"); 
			try {
				Long id =  participant.getId();
				//if (this.comments == null ) logger.warn ("Comments were null.");
				Iterator iter = this.comments.iterator();
				while(iter.hasNext()) {
					Comment c = (Comment) iter.next();
					//if (c == null ) logger.warn ("Comment was null.");
					//if (c.getParticipant() == null ) logger.warn ("Participant was null.");
					if (c.getParticipant().getId() == id) return c;
				}
			} catch(Exception e) {
				logger.warn("getComment for VitalParticipant threw " + e);
			}
		}
		else {
			throw new RuntimeException("Attempt to get comment for a non-Essay response. Only Essay responses have comments."); 
		}
		return null;
    }


    
	/** 
     * Whether there are comments on this assignment.
     */
    public boolean hasComments () {
        
		if (this.assignment.isEssay()) {
            if (this.comments != null && this.comments.size() > 0) return true;
            
		} else if (this.assignment.isGuidedLesson()) {
			Iterator iter = this.answers.iterator();
			while(iter.hasNext()) {
                Answer answer = (Answer) iter.next();
                if (answer.hasComments()) return true;
			}
		}
        return false;
	}
    
    
    /** 
     * true if at least one of the comments on this assignmentResponse has been submitted.
     */
    public boolean hasSubmittedComments() {
        
        Iterator iter = this.allComments().iterator();
        while(iter.hasNext()) {
            Comment comment = (Comment) iter.next();
            if (comment.isSubmitted()) return true;
        }
        return false;
    }
    
    /** 
    * If an essay response, returns all comments on the response. If a guided lesson response, returns all comments on all answers.
    */
    public Set allComments() {
        if (this.assignment.isEssay()) {
            if (comments == null) throw new RuntimeException("No comments found.");
            return this.comments;
        }
        else {
            Set allComments = new HashSet();
            Iterator answersIterator = this.answers.iterator();
            while (answersIterator.hasNext()) {
                Answer answer = (Answer) answersIterator.next();
                allComments.addAll(answer.getComments());
            }
            return allComments;
        }
    }
    
    
    /**
     * Resets all assignments to a given assignment response to unsubmitted status (regardless of whether the assignment is a guided lesson or an essay)
     */
    public void resetAllComments() {
        Iterator i = this.getComments().iterator();
        while (i.hasNext()) {
            Comment comment = (Comment) i.next();
            comment.setStatus(new Integer(0));
        }
    }
    
    public boolean hasSubmittedCommentsFor(VitalParticipant p) {
        return (this.isSubmitted() && this.belongsTo(p) && comments != null && this.hasSubmittedComments());
    }
    
    
	/** 
     * When a comment was first submitted on an assignment. -- by anyone. (Useful for the student.)
	 * Returns null if response or no comments found.
	 */
    public Date getCommentsDate() {
		ArrayList l = new ArrayList(); // all the comments on this assignment.
		if (this.assignment.isEssay()) {
			if (!this.hasComments()) {
				return null;
			}
			l = new ArrayList(this.comments);
		}
		else if (this.assignment.isGuidedLesson()) {
			if (this.answers == null) {
				return null;
			}
			Iterator iter = this.answers.iterator();
			while(iter.hasNext()) {
				l.addAll(((Answer) iter.next()).getComments());
			}
		}
		else return null;

		// ok, now l contains all the comments for this assignment, regardless of its type.

		if( l.size() > 1) {
			OmniComparator oc = new OmniComparator(Comment.class, "getDateModifiedForSorting", null);
			try {
				Collections.sort(l, oc);
			} catch(Exception e) {
				logger.warn("Attempt to sort comments by date triggered " + e);
			}
		}
		Comment earliestComment = (Comment) l.get(0);
		return earliestComment.getDateModified();
	}
	
    
    /** 
     * Retrieve a date that can be used to sort responses by when comments submitted.
     */
    public Date getCommentsDateForSorting () {
		Date d = getCommentsDate();
		//there's a bug comparing dates to timestamps, so we're returning a timestamp rather than a plain vanilla date.
		if (d == null ) return new java.sql.Timestamp(0);
		return d;
	}
    
	/** 
     * Retrieve a date that can be used to sort responses by when they were submitted.
     */
    public Date getDateSubmittedForSorting () {
		if (dateSubmitted == null ) return new java.sql.Timestamp(0);
		return dateSubmitted;
	}
    

	/** 
     * Retrieve submitter's last name.
	 */
	public String getLastName() {
		return participant.getLastName();
	}

	/** 
     * Retrieve submitter's first name.
	 */
	public String getFirstName() {
		return participant.getFirstName();
	}

	/** 
     * Retrieve submitter's access level.
	 */
	public String getAccessLevel() {
		return participant.getLabelForAccessLevel();
	}


	/** 
     * When a comment was first submitted on an assignment.
	 */
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (assignment != null) {
            Set parentCollection = assignment.getResponses();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (participant != null) {
            Set parentCollection = participant.getAssignmentResponses();
            if (parentCollection != null) parentCollection.remove(this);
        }
        Vital3Utils.removeMultipleFromCollections(getAllPersistableChildren());
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(answers);
        children.addAll(comments);
        return children;
    }

}
