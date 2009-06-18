package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.*;
import ccnmtl.vital3.utils.Persistable;
import ccnmtl.vital3.utils.Vital3Utils;

/** 
 * A representation of a particular Material in the context of a particular Worksite. Data relating to this particular
 * contextual relationship such as custom field values, annotations, keywords, etc. are stored as properties on this Material.
 * Performance could be improved slightly if customFields were internally managed as an array, but this might be tricky.
 * @author Eric Mattes
 * 		
*/
public class Material implements Persistable, Serializable {

    protected final Log logger = LogFactory.getLog(getClass());
    
    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "material";
    
    
    public static final Integer PUBLIC_ACCESS = new Integer(0);
    public static final Integer INSTRUCTORS_AND_ADMINS_ACCESS = new Integer(1);
    public static final Integer UNLISTED_ACCESS = new Integer(30);
    
    /** identifier field */
    private Long id;
    /** nullable persistent field */
    private VitalWorksite worksite;    
    /** nullable persistent field */
    private Integer accessLevel;
    /** nullable persistent field */
    private Date dateModified;
    /** nullable persistent field */
    private String text;
    /** nullable persistent field */
    private String thumbUrl;
    /** nullable persistent field */
    private String title;
    /** nullable persistent field */
    private String type;
    /** nullable persistent field */
    private String url;
    /** persistent field */
    private Set annotations;
    /** persistent field */
    private Set assignmentAssociations;
    /** persistent field */
    private Set customFieldValues;
    /** persistent field */
    private Set questionAssociations;
    /** persistent field */
    private Set unitAssociations;
    
    // sorted customFieldValues by ordinalValue
    //private List sortedCustomFieldValues;

    /** full constructor */
    public Material(VitalWorksite worksite, Integer accessLevel, Date dateModified, String text, String thumbUrl, String title, String type, String url, Set annotations, Set assignmentAssociations, Set customFieldValues, Set questionAssociations, Set unitAssociations) {
        this.worksite = worksite;
        this.accessLevel = accessLevel;
        this.dateModified = dateModified;
        this.text = text;
        this.thumbUrl = thumbUrl;
        this.title = title;
        this.type = type;
        this.url = url;
        this.annotations = annotations;
        this.assignmentAssociations = assignmentAssociations;
        this.customFieldValues = customFieldValues;
        this.questionAssociations = questionAssociations;
        this.unitAssociations = unitAssociations;
        
        worksite.getMaterials().add(this);
    }

    /** convenient constructor: designed for manual Material creation. **/
    public Material(VitalWorksite worksite, int accessLevel, Date dateModified, String text, String thumbUrl, String title, String type, String url) {
        
        this(worksite, new Integer(accessLevel), dateModified, text, thumbUrl, title, type, url, new HashSet(), new HashSet(), new TreeSet(), new HashSet(), new HashSet());        
    }

    
    /** default constructor */
    public Material() {
    }

    /** static "constructor" for a Video Material **/
    public static Material newVideo(VitalWorksite worksite, int accessLevel, String text, String thumbUrl, String title, String url) {
        return new Material(worksite, accessLevel, new Date(), text, thumbUrl, title, "video", url);
    }
    
    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return worksite;
    }
    
    
    /**
     * Returns the custom field values as Strings, in order. Uses CFV.getNonNullValue().
     *@return A List of custom field values as Strings, in order.
     */
    public List getCustomFieldValuesStringList() {
        ArrayList list = new ArrayList();
        Iterator iter = customFieldValues.iterator();
        while(iter.hasNext()) {
            CustomFieldValue cfv = (CustomFieldValue) iter.next();
            list.add(cfv.getNonNullValue());
        }
        return list;
    }
    
    
    public boolean isUnlisted() {
        return (this.accessLevel != null && this.accessLevel.equals(UNLISTED_ACCESS));
    }
    
    /**
     * Returns a list of the CustomFieldValues for this material in order.
     */
    public List getCustomFieldValuesAsList() {
        return new ArrayList(customFieldValues);
    }
    
    /**
     * Designed to be passed to a template.
     *@return A List of Maps, each with the keys "name" and "value" corresponding to custom field names and values, in order.
     */
    public List getCustomFieldsAndValues() {
        ArrayList list = new ArrayList();
        Iterator iter = customFieldValues.iterator();
        while(iter.hasNext()) {
            CustomFieldValue cfv = (CustomFieldValue) iter.next();
            HashMap map = new HashMap();
            map.put("name", cfv.getName());
            map.put("value", cfv.getNonNullValue());
            list.add(map);
        }
        return list;
    }
    
    /**
     * Get a related customFieldValue by its id. If none of the related cfvs have that id, null will be returned.
     */
    public CustomFieldValue getCustomFieldValueById(Long id) {
        Iterator iter = customFieldValues.iterator();
        while (iter.hasNext()) {
            CustomFieldValue cfv = (CustomFieldValue) iter.next();
            if (cfv.getId().equals(id)) return cfv;
        }
        return null;
    }
     
    //////////// STANDARD GETTERS AND SETTERS ////////////
    
    
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    /** 
     * The minimum "access key" level required for access to this Material. 0 means unrestricted.
     */
    public Integer getAccessLevel() {
        return this.accessLevel;
    }
    public void setAccessLevel(Integer accessLevel) {
        this.accessLevel = accessLevel;
    }

    /** 
     * When this Material was last updated.
     */
    public Date getDateModified() {
        return this.dateModified;
    }
    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /** 
     * For videos: a brief description of the video.
     */
    public String getText() {
        return this.text;
    }
    public void setText(String text) {
        this.text = text;
    }

    /** 
     * a URL for the thumbnail image. Must be local.
     */
    public String getThumbUrl() {
        return this.thumbUrl;
    }
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }
    
    /** 
     * A title for the material.
     */
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    /** 
     * "video": A video material. "clip": a clip from a video.
     */
    public String getType() {
        return this.type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    /** 
     * A URL for accessing the material.
     */
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    
    /** 
     * The Worksite to which this material belongs.
     */
    public VitalWorksite getWorksite() {
        return this.worksite;
    }
    public void setWorksite(VitalWorksite worksite) {
        this.worksite = worksite;
    }

    /** 
    * The set of annotations.
    */
    public Set getAnnotations() {
        return this.annotations;
    }
    public void setAnnotations(Set annotations) {
        this.annotations = annotations;
    }
    
    /** 
     * The set of associated assignments.
     */
    public Set getAssignmentAssociations() {
        return this.assignmentAssociations;
    }
    public void setAssignmentAssociations(Set assignmentAssociations) {
        this.assignmentAssociations = assignmentAssociations;
    }
    
    /**
     * Returns associated material-assignments
     */
    public Set getAssignments() {

        Set assignments = new HashSet();
        Iterator iter = this.assignmentAssociations.iterator();
        while(iter.hasNext()) {
            AssignmentMaterialAssoc ama = (AssignmentMaterialAssoc) iter.next();
            assignments.add(ama.getAssignment());
        }  
        return assignments;
    }
    
    /**
     * Returns associated assignments (including those with associated questions)
     */
    public Set getAllAssociatedAssignments() {
        
        Set assignments = getAssignments();
        Set questions = getQuestions();
        Iterator iter = questions.iterator();
        while(iter.hasNext()) {
            Question question = (Question) iter.next();
            assignments.add(question.getAssignment());
        }
        return assignments;
    }
        
    
    /** 
     * Custom field values given to this Material. Should be in order
     */
    public Set getCustomFieldValues() {
        return this.customFieldValues;
    }
    public void setCustomFieldValues(Set customFieldValues) {
        this.customFieldValues = customFieldValues;
    }
    
    /** 
    * The set of associated gl questions.
    */
    public Set getQuestionAssociations() {
        return this.questionAssociations;
    }
    public void setQuestionAssociations(Set questionAssociations) {
        this.questionAssociations = questionAssociations;
    }
    
    /**
     * Returns associated questions
     */
    public Set getQuestions() {
        
        Set questions = new HashSet();
        Iterator iter = this.questionAssociations.iterator();
        while(iter.hasNext()) {
            QuestionMaterialAssoc qma = (QuestionMaterialAssoc) iter.next();
            questions.add(qma.getQuestion());
        }  
        return questions;
    }
    
        
    /** 
     * The set of associated units.
     */
    public Set getUnitAssociations() {
        return this.unitAssociations;
    }
    public void setUnitAssociations(Set unitAssociations) {
        this.unitAssociations = unitAssociations;
    }
    
    /**
     * Returns associated material-units
     */
    public Set getUnits() {

        Set units = new HashSet();
        Iterator iter = this.unitAssociations.iterator();
        while(iter.hasNext()) {
            UnitMaterialAssoc uma = (UnitMaterialAssoc) iter.next();
            units.add(uma.getUnit());
        }  
        return units;
    }

    /**
     * Returns the CustomFieldValue related to this material which corresponds to the CustomField
     * you pass in. This requires that you first initialize customFieldValues. Will return null if not found.
     */
    public CustomFieldValue getCustomFieldValueForCustomField(CustomField cf) {
        Long cfId = cf.getId();
        Iterator cfvIter = customFieldValues.iterator();
        while (cfvIter.hasNext()) {
            CustomFieldValue cfv = (CustomFieldValue) cfvIter.next();
            if (cfv.getCustomField().getId().equals(cfId)) return cfv;
        }
        return null;
    }
    
    /**
     * Create CFVs which are blank and relate them to this material. This requires that there be a
     * related worksite which has its customfields initialized! Call this when creating a new material.
     */
    public void createCFVs() {
        Iterator cfIter = worksite.getCustomFieldList().iterator();
        while (cfIter.hasNext()) {
            CustomField cf = (CustomField) cfIter.next();
            Integer ordinalVal = cf.getOrdinalValue();
            CustomFieldValue cfv = new CustomFieldValue(cf, this, ordinalVal, "");
        }
    }
    
    /** Makes an ArrayList containing just this object **/
    public List toList() {
        return Arrays.asList( new Material[]{ this } );
    }
    
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("text", getText()).toString();
    }
    

    /**
     * Clones the object, but all collections and references to related entities are made null in the clone.
     * String, Integer, Long are all immutable and may be reference-copied. Date, however, is cloned.
     *
    public Object clone() {
        try {
            Material result = (Material) super.clone();
            result.worksite = null;
            if (dateModified != null) result.dateModified = (Date)this.dateModified.clone();
            result.annotations = null;
            result.assignmentAssociations = null;
            result.customFieldValues = null;
            result.questionAssociations = null;
            result.unitAssociations = null;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError("Cloning error");
        }
    }*/
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (worksite != null) {
            Set parentCollection = worksite.getMaterials();
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
        children.addAll(annotations);
        children.addAll(assignmentAssociations);
        children.addAll(customFieldValues);
        children.addAll(questionAssociations);
        children.addAll(unitAssociations);
        return children;
    }
    
    
}
