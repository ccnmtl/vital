package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.*;
import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.Ordinal;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;


/** 
 * 			Like, an assignment.
 * 			@author Eddie Rubeiz
 * 		
*/
public class Assignment implements Comparable, Ordinal, Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "assignment";
    
    // assignment type constants:
    public static final String ESSAY = "essay";
    public static final String GUIDED_LESSON = "gl";
    public static final String DISCUSSION = "discussion";
    

    protected final Log logger = LogFactory.getLog(getClass());

    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String customType;

    /** nullable persistent field */
    private Date dateDue;

    /** nullable persistent field */
    private String instructions;

    /** nullable persistent field */
    private Integer ordinalValue;

    /** nullable persistent field */
    private String title;

    /** nullable persistent field */
    private String type;

    /** nullable persistent field */
    private Unit unit;

    /** persistent field */
    private Set materialAssociations;

    /** persistent field */
    private Set questions;
    
    // special field for the number of questions
    private int numberOfQuestions;

    /** persistent field */
    private Set responses;

    /** full constructor */
    public Assignment(Unit unit, String customType, Date dateDue, String instructions, Integer ordinalValue, String title, String type, Set materialAssociations, Set questions, Set responses) {
        this.unit = unit;
        this.customType = customType;
        this.dateDue = dateDue;
        this.instructions = instructions;
        this.ordinalValue = ordinalValue;
        this.title = title;
        this.type = type;
        this.materialAssociations = materialAssociations;
        this.questions = questions;
        this.responses = responses;
        
        unit.getAssignments().add(this);
    }

    /** default constructor */
    public Assignment() {
    }
    
    /** convenient constructor */
    public Assignment(Unit unit, String customType, Date dateDue, String instructions, int ordinalValue, String title, String type) {
        this(unit, customType, dateDue, instructions, new Integer(ordinalValue), title, type, new HashSet(), new TreeSet(), new HashSet());
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return unit.getRelatedWorksite();
    }
    
    /**
     * Will return customType if it is not null, otherwise will return type.
     */
    public String getCustomTypeOrType() {
    
        if (this.customType == null) {
            if (this.isGuidedLesson()) return "Guided Lesson";
            if (this.isDiscussion()) return "Discussion";
            else return "Essay";
        }
        return this.customType;
    }
    
    
    /**
     * Will properly handle moving an assignment from one unit into another.
     */
    public void updateUnit(Unit newUnit) {
        
        // remove from old unit's collection:
        this.unit.getAssignments().remove(this);
        
        // add to new unit's collection:
        newUnit.getAssignments().add(this);
        
        // update this:
        this.unit = newUnit;
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * This field allows instructors to give a custom type name to the assignment. The real type is stored in the "type" property.
     */
    public String getCustomType() {
        return this.customType;
    }

    public void setCustomType(String customType) {
        this.customType = customType;
    }

    /** 
     * When this assignment is due.
     */
    public Date getDateDue() {
        return this.dateDue;
    }

    public void setDateDue(Date dateDue) {
        this.dateDue = dateDue;
    }

    /** 
     * Tell students how to complete the assignment.
     */
    public String getInstructions() {
        return this.instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /** 
     * Used to order assignments within the Unit.
     */
    public Integer getOrdinalValue() {
        return this.ordinalValue;
    }

    
    public void setOrdinalValue(Integer ordinalValue) {
        if (this.ordinalValue != null && !this.ordinalValue.equals(ordinalValue) && this.unit != null) {
            // must remove and re-insert into parent collection to maintain order... this is a problem inherent with sorted sets:
            Set parentSet = this.unit.getAssignments();
            boolean found = parentSet.remove(this);
            this.ordinalValue = ordinalValue;
            if (found) parentSet.add(this);
            
        } else this.ordinalValue = ordinalValue;
    }

    /** 
     * Title of the assignments
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** 
     * The type of the assignment (essay or guided lesson or discussion.) 
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** 
     * The Worksite in which this material is used.
     */
    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /** 
     * Materials associated for this Assignment.
     */
    public Set getMaterialAssociations() {
        return this.materialAssociations;
    }

    public void setMaterialAssociations(Set materialAssociations) {
        this.materialAssociations = materialAssociations;
    }

    /** 
     * Questions in this Assignment. (Only used if this is a guided lesson.)
     */
    public Set getQuestions() {
        return this.questions;
    }

    public void setQuestions(Set questions) {
        this.questions = questions;
    }

    /** 
     * Responses to the assignment.
     */
    public Set getResponses() {
        return this.responses;
    }

    public void setResponses(Set responses) {
        this.responses = responses;
    }
    
    
    public ArrayList getSortedResponses() {
       ArrayList sortedResponses = new ArrayList(getResponses());
       OmniComparator responseComp = new OmniComparator(AssignmentResponse.class, "getLastName", null);  
       Collections.sort(sortedResponses, responseComp);
       return sortedResponses;  
    }
    

    public Set getDiscussionEntries() {
        //TODO: check this is a discussion.
        Set results = new HashSet();
	    Iterator responseIter = this.getResponses().iterator();
	    while (responseIter.hasNext()) {
		    AssignmentResponse response = (AssignmentResponse) responseIter.next();
		    Set participantEntries = response.getDiscussionEntries();
		    results.addAll (participantEntries);
		    
	    }
	    return results;
	}

    
    
    
    /** 
     * Submitted Responses to the assignment.
     */
    public int getSubmittedResponseCount() {
		int i = 0;
		Iterator responseIter = this.getResponses().iterator();
		while (responseIter.hasNext()) {
			AssignmentResponse response = (AssignmentResponse) responseIter.next();	
			if ( response.getStatus().intValue() == 1) i++;
		}
		return i;
	}
    /** 
     * Returns the AssignmentResponse of a given Participant, if there is one.
     */
    public AssignmentResponse getParticipantResponse(VitalParticipant participant) {
		if (participant == null) throw new RuntimeException("Null participant passed.");
		//logger.info ("Looking through responses for assignment " + this.getId() + ". There are currently " + this.getResponses().size());
		Iterator responseIter = this.getResponses().iterator();
		while (responseIter.hasNext()) {
			AssignmentResponse response = (AssignmentResponse) responseIter.next();	
			if (response.getParticipant().getId().equals(participant.getId())) {
				return response;
			}
		}
		return null;
	}

	/** 
     * Returns the status of a participant's response to an assignment.
	 * Status can be one of 
	 * - Not Started (not saved, not submitted)
	 * - In Progress (saved, not submitted)
	 * - Submitted (submitted, no feedback)
	 * - Feedback Available (submitted, feedback)
	 */
	 
    public String getStatus(VitalParticipant participant) {
		AssignmentResponse response = this.getParticipantResponse(participant);
        if (response == null) return "Not Started";
        if (response.isNotSubmitted()) return "In progress";
        if (!response.hasSubmittedComments()) return "Submitted";
        return "Feedback Available";
	}


	/**
	*	given a guided lesson, return the number of questions. Throw unsupported operation exception
	*	if it is not a guided lesson.
	*/
	public int getNumberOfQuestions() {
		checkForGuidedLesson();
		return (numberOfQuestions = this.questions.size());
	}


	/**
	*	given a guided lesson assignment and a participant, return a set of the Answers already provided by the 
	*	Participant to the questions in this assignment.
	*/
	public Set answeredQuestions(VitalParticipant participant) {
		checkForGuidedLesson();
		AssignmentResponse ar = this.getParticipantResponse(participant);
		if (ar == null) return new HashSet();
		return ar.getAnswers();
	}

	/**
	*	given a guided lesson assignment and a participant, return the next question
	*	the participant needs to answer, or null if the assignment has been completed.
	*	Throw unsupported operation exception if it is not a guided lesson.
	*/
	public int getNumberOfQuestionsAnswered(VitalParticipant participant) {
		return this.answeredQuestions(participant).size();
	}

	/**
	*	Returns a given question, or null of the assignment is complete.
	*/
	public Question getQuestion(int i) {
		Question q = null;
		try
		{
			checkForGuidedLesson();
			q = (Question) this.getSortedQuestions().get(i);	
		}
		catch (Exception e)
		{
			logger.warn("getQuestion " + i + " caused " + e);
		}
		return q;
	}


    /** 
    *	Returns the questions in order.
    */
	public List getSortedQuestions() {
		//No need to sort. Try commenting out the sort code below.
		checkForGuidedLesson();
		OmniComparator questionComp = new OmniComparator(Question.class, "getOrdinalValue", null); //
		ArrayList sortedQuestions = new ArrayList(this.questions);
		Collections.sort(sortedQuestions, questionComp); //
		return sortedQuestions;
	}
    

    /** 
     * Returns the materials.
     */
    public Set getMaterials() {
        Set materials = new HashSet();
        Iterator iter = this.materialAssociations.iterator();
        while(iter.hasNext()) {
            AssignmentMaterialAssoc ama = (AssignmentMaterialAssoc) iter.next();
            materials.add(ama.getMaterial());
        }
        return materials;
    }


    /**
     * Returns sorted materials
     * @return A list of sorted materials
     */ 
     public ArrayList getSortedMaterials() {

       ArrayList sortedMaterials = new ArrayList(getMaterials());
       
       // sorted by material title   
       OmniComparator materialComp = new OmniComparator(Material.class, "getTitle", null);  
       Collections.sort(sortedMaterials, materialComp);

       return sortedMaterials;	
    }
    
    
    /**
     * Returns size of materials collection (used for showing/hiding blocks of HTML in template)
     */
    public int getNumberOfMaterials() {
        return this.materialAssociations.size();
    }
    

    /** 
     * Adds an AssignmentResponse to the set of responses.
     */
    public void addResponse(AssignmentResponse response) {
        this.responses.add(response);
    }

	
	/**
	*	Throws an error if this is not a guided lesson.
	*/
	private void checkForGuidedLesson() {
		if (!this.type.equals( GUIDED_LESSON)) {
			logger.warn ("This is not a guided lesson.");
			throw new UnsupportedOperationException("Only guided lessons are allowed to have questions.");
		}
	}

	/**
	*	Convenience methods - make code more readable.
	*/

	public boolean isGuidedLesson() {
		return this.type.equals(GUIDED_LESSON);
	}
	public boolean isEssay() {
		return this.type.equals(ESSAY);
	}
	public boolean isDiscussion() {
		return this.type.equals(DISCUSSION);
	}




    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    // defines the natural order based on ordinalValue property
    public int compareTo(Object obj) {
        Assignment o = (Assignment)obj;
        return ordinalValue.compareTo(o.ordinalValue);
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        if (unit != null) {
            Set parentCollection = unit.getAssignments();
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
        children.addAll(materialAssociations);
        children.addAll(questions);
        children.addAll(responses);
        return children;
    }

}
