package ccnmtl.vital3;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.utils.Persistable;

/** 
 * 		Ties a material to a question in a guided lesson.
 * 		@author Eddie Rubeiz
 * 		
*/
public class QuestionMaterialAssoc implements Persistable, Serializable {
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "questionMaterialAssoc";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private Question question;

    /** nullable persistent field */
    private Material material;

    /** full constructor */
    public QuestionMaterialAssoc(Material material, Question question) {
        this.material = material;
        this.question = question;
        
        material.getQuestionAssociations().add(this);
        question.getMaterialAssociations().add(this);
    }

    /** default constructor */
    public QuestionMaterialAssoc() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return material.getRelatedWorksite();
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The guided lesson question.
     */
    public Question getQuestion() {
        return this.question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    /** 
     * The Material
     */
    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (question != null) {
            Set parentCollection = question.getMaterialAssociations();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (material != null) {
            Set parentCollection = material.getQuestionAssociations();
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
