package ccnmtl.vital3;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.Ordinal;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 *             A question posed to a Participant as part of an  Assignment whose type is "Guided Lesson". (Essay assignments may not contain questions.)
 * 			@author Eddie Rubeiz
 * 		
*/
public class Question implements Comparable, Ordinal, Persistable, Serializable {
    
    protected final Log logger = LogFactory.getLog(getClass());

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "question";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Integer ordinalValue;

    /** nullable persistent field */
    private String text;

    /** nullable persistent field */
    private Assignment assignment;

    /** persistent field */
    private Set materialAssociations;

    /** persistent field */
    private Set answers;

    /** full constructor */
    public Question(Assignment assignment, Integer ordinalValue, String text, Set materialAssociations, Set answers) {
        this.assignment = assignment;
        this.ordinalValue = ordinalValue;
        this.text = text;
        this.materialAssociations = materialAssociations;
        this.answers = answers;
        
        assignment.getQuestions().add(this);
    }
    
    /** convenient constructor */
    public Question(Assignment assignment, int ordinalValue, String text) {
        this(assignment, new Integer(ordinalValue), text, new HashSet(), new HashSet());        
    }
    
    /** default constructor */
    public Question() {
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
     * Determines the order in which the questions are asked.
     */
    public Integer getOrdinalValue() {
        return this.ordinalValue;
    }
    /*public void setOrdinalValue(Integer ordinalValue) {
        this.ordinalValue = ordinalValue;
    }*/
    public void setOrdinalValue(Integer ordinalValue) {
        
        if (this.ordinalValue != null && !this.ordinalValue.equals(ordinalValue) && this.assignment != null) {
            
            // must remove and re-insert into parent collection to maintain order... this is a problem inherent with sorted sets:
            Set parentSet = this.assignment.getQuestions();
            boolean found = parentSet.remove(this);
            this.ordinalValue = ordinalValue;
            if (found) parentSet.add(this);
            
        } else this.ordinalValue = ordinalValue;
    }
    
    /** 
     * The text of the question.
     */
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /** 
     * The guided lesson this question is part of.
     */
    public Assignment getAssignment() {
        return this.assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /** 
     * Used to look up materials that pertain to this question.
     */
    public Set getMaterialAssociations() {
        return this.materialAssociations;
    }

    public void setMaterialAssociations(Set materialAssociations) {
        this.materialAssociations = materialAssociations;
    }

    /** 
     * Answers to this question.
     */
    public Set getAnswers() {
        return this.answers;
    }

    public void setAnswers(Set answers) {
        this.answers = answers;
    }

   /** 
     * Answer to this question by a particular participant
     */
	public Answer getAnswer (VitalParticipant participant) {
		try {
			Long id =  participant.getId();
			Iterator iter = this.answers.iterator();
			while(iter.hasNext()) {
				Answer a = (Answer) iter.next();
				if (a.getParticipant().getId() == id) return a;
			}
        } catch(Exception e) {
			logger.warn("getAnswer threw " + e);	
		}
		return null;
	}


	/** 
     * Returns the associated materials.
     */
    public Set getMaterials() {
        Set materials = new HashSet();
		try {
			Iterator iter = this.materialAssociations.iterator();
			while(iter.hasNext()) {
				QuestionMaterialAssoc qma = (QuestionMaterialAssoc) iter.next();
				materials.add(qma.getMaterial());
			}
        } catch(Exception e) {
			logger.warn("Error during getMaterials, " + e);	
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    // defines the natural order based on ordinalValue property
    public int compareTo(Object obj) {
        Question o = (Question)obj;
        return ordinalValue.compareTo(o.ordinalValue);
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (assignment != null) {
            Set parentCollection = assignment.getQuestions();
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
        children.addAll(answers);
        return children;
    }

}
