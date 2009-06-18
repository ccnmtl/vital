package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.lang.reflect.*;

import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 * 			The answer to a question in a Guided Lesson assignment. Note: Essay assignment responses have null for their set of answers.
 * 			@auther Eddie Rubeiz
 * 		
*/
public class Answer implements Persistable, Serializable {

    protected final Log logger = LogFactory.getLog(getClass());
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "answer";
        
    /** identifier field */
    private Long id;

    /** persistent field */
    private Set comments;

    /** nullable persistent field (WE ALWAYS USE NULL FOR NOW) */
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
    static public final int GL_ANSWER_CAPACITY = CHARSPERSEGMENT * TOTALSEGMENTS;
    
    

    /** nullable persistent field */
    private AssignmentResponse assignmentResponse;

    /** nullable persistent field */
    private Question question;

    /** full constructor */
    public Answer(AssignmentResponse assignmentResponse, Question question, Integer status, String text, Set comments) {
		if (question == null) throw new RuntimeException("Attempt to create an answer to a null question.");
		if (assignmentResponse == null) throw new RuntimeException("Attempt to create an answer but no assignmentResponse passed.");
        if (comments == null) comments = new HashSet();
		this.assignmentResponse = assignmentResponse;
        this.comments = comments;
        this.question = question;
        this.status = status;
        this.setText(text);
        
        assignmentResponse.getAnswers().add(this);
        question.getAnswers().add(this);
    }

    /** convenient constructor: automatically puts in a null status */
    public Answer(AssignmentResponse assignmentResponse, Question question, String text) {
		this(assignmentResponse, question, null, text, new HashSet());
    }
    
    /** default constructor */
    public Answer() {    
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return question.getRelatedWorksite();
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


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
            method = (Answer.class).getMethod("getText" + index, new Class[] {});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("That segment doesn't exist.");
        }
        return method;
    }
    
    private Method getTextSetter (int index) {
        checkSegment(index);
        Method method = null;
        try {
            method = (Answer.class).getMethod("setText" + index, new Class[] {String.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("That segment doesn't exist.");
        }
        return method;
    }
    ////////////
    ////////////
    
    public Set getComments() {
        return this.comments;
    }

    public boolean hasComments() {
        return (this.comments != null && this.comments.size() > 0);
    }
    
    public boolean hasSubmittedComments() {
        if (this.hasComments()) {
            Iterator iter = this.comments.iterator();
            while (iter.hasNext()) {
                Comment comment = (Comment) iter.next();
                if (comment.isSubmitted()) return true;
            }
        }
        return false;
    }

    /**
     * Which participant created this answer ?
     */
    public VitalParticipant getParticipant() {
		return this.getAssignmentResponse().getParticipant();
    }
	
    /** 
     * Retrieves a comment submitted by a specific participant.
     */
    public Comment getComment(VitalParticipant participant) {
        try {
			Long id =  participant.getId();
			Iterator iter = this.comments.iterator();
			while(iter.hasNext()) {
				Comment c = (Comment) iter.next();
				if (c.getParticipant().getId() == id) return c;
			}
        } catch(Exception e) {
			logger.warn("getComment for VitalParticipant threw " + e);
		}
		return null;
    }

    public void setComments(Set comments) {
        this.comments = comments;
    }

    /** 
     * Indicates whether this answer has been submitted yet... WE ALWAYS USE NULL. This field is never really referenced.
     */
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /** 
     * The text of the answer.
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
            
            if (obj != null ) {
                String  stringToAppend = (String) obj;
                textBuffer.append (stringToAppend);
                //  logger.debug ("Appending a string of length " + stringToAppend.length());
            }
        }
        // logger.debug("getText returned " + textBuffer.length() + "chars." );
        return textBuffer.toString();
    }

    public void setText(String text) {
        List segments = Vital3Utils.segmentString (text, this.CHARSPERSEGMENT, this.TOTALSEGMENTS);
        Iterator iter = segments.iterator();
        int segment;
        for (segment = 0; segment < this.TOTALSEGMENTS; segment++) {
            String temp = null;
            if (segment <  segments.size()) temp = (String) segments.get(segment);
            // if (temp != null ) { logger.debug ("Adding  a segment of length " + temp.length());}
            Method setter = this.getTextSetter(segment);
            try {
                setter.invoke(this, new Object[] {temp});
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
     * The guided lesson response this answer is part of.
     */
    public AssignmentResponse getAssignmentResponse() {
        return this.assignmentResponse;
    }

    public void setAssignmentResponse(AssignmentResponse assignmentResponse) {
        this.assignmentResponse = assignmentResponse;
    }

    /** 
     * The question this answer answers.
     */
    public Question getQuestion() {
        return this.question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /**
     * Removes this object from any parent collections and recursively calls it on children
     */
    public void removeFromCollections() {
        
        if (assignmentResponse != null) {
            Set parentCollection = assignmentResponse.getAnswers();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (question != null) {
            Set parentCollection = question.getAnswers();
            if (parentCollection != null) parentCollection.remove(this);
        }
        Vital3Utils.removeMultipleFromCollections(comments);
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        Set children = new HashSet();
        children.addAll(comments);
        return children;
    }


}
