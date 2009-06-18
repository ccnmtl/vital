package ccnmtl.vital3;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import ccnmtl.vital3.utils.Persistable;

/** 
 * 			An annotation to a Material made by a Participant in a vital Worksite.
 * 			@author Eddie Rubeiz
 * 		
*/
public class Annotation implements Comparable, Persistable, Serializable {

    // a name which is the property name other entities use for holding a reference to this type of entity.
    public static final String simpleName = "annotation";
    
    /** identifier field */
    private Long id;

    /** nullable persistent field */
    private String clipBegin;

    /** nullable persistent field */
    private String clipEnd;

    /** nullable persistent field */
    private Date dateModified;

    /** nullable persistent field */
    private String text;

    /** nullable persistent field */
    private String title;

    /** nullable persistent field */
    private String type;

    /** nullable persistent field */
    private Material material;

    /** nullable persistent field */
    private VitalParticipant participant;

    /** full constructor */
    public Annotation(Material material, VitalParticipant participant, String clipBegin, String clipEnd, Date dateModified, String text, String title, String type) {
        this.material = material;
        this.participant = participant;
        this.clipBegin = clipBegin;
        this.clipEnd = clipEnd;
        this.dateModified = dateModified;
        this.text = text;
        this.title = title;
        this.type = type;
        
        material.getAnnotations().add(this);
        participant.getAnnotations().add(this);
    }

    /** default constructor */
    public Annotation() {
    }

    
    /**
     * Will return the worksite to which this belongs.
     */
    public VitalWorksite getRelatedWorksite() {
        return material.getRelatedWorksite();
    }
    
    /**
     * This will pull the tag Set out of the tagMap. Assumes that each tag set is stored via String key which
     * corresponds to the id of this annotation. If no tags are found, will return empty set.
     */
    public Set getTags(Map tagMap) {
        
        if (tagMap == null) throw new RuntimeException("tagMap was null");
        
        Set tags = (Set) tagMap.get(this.id.toString());
        if (tags != null) return tags;
        else return new HashSet();
    }
    
    /**
     * This will return a comma-separated list of each tag found for this note in the tagMap. See getTags for more info.
     * If this note has no tags, it will return null.
     */
    public String getTagsAsString(Map tagMap) {
        
        Set tags = this.getTags(tagMap);
        if (tags.size() > 0) return StringUtils.join(tags.iterator(), ", ");
        else return null;
        
    }
    
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    /** 
     * The offset of the start of the clip.
     */
    public String getClipBegin() {
        return this.clipBegin;
    }

    public void setClipBegin(String clipBegin) {
        this.clipBegin = clipBegin;
    }

    /** 
     * The offset of the end of the clip.
     */
    public String getClipEnd() {
        return this.clipEnd;
    }

    public void setClipEnd(String clipEnd) {
        this.clipEnd = clipEnd;
    }

    /** 
     * When this annotation was last updated.
     */
    public Date getDateModified() {
        return this.dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    /** 
     * The text of the annotation.
     */
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /** 
     * The title of the annotation.
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /** 
     * The type of annotation.
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** 
     * The material that was annotated.
     */
    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    /** 
     * The participant who made the annotation.
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
    

    
    /*
     *new version: natural order of annotations is reverse chronologcal.
     */
    
    public int compareTo(Object obj) {
       Annotation o = (Annotation)obj;
       int result = - dateModified.compareTo(o.dateModified);
       
        /*
    
        //old version
        // this function, however confusing, IS "consistent with equals". Since the default implementation
        // of equals uses identity (==), this compareTo method double checks identity equality before returning 1.
        // The "forced inequality" here is done so that Annotations will not appear to be equal in an ordered set.
        // when I used to use "return title.compareTo(o.title)", only one annotation would appear when more
        // than one had the same title.
    
        int result = title.compareToIgnoreCase(o.title);
        if (result == 0 && this != o) {
            // if the titles are equal, try to find some other field which is not equal:
            if (dateModified != null && o.dateModified != null)
                result = dateModified.compareTo(o.dateModified);
            
            if (result == 0 && text != null && o.text != null)
                result = text.compareToIgnoreCase(o.text);
            
            if (result == 0 && clipBegin != null && o.clipBegin != null)
                result = clipBegin.compareTo(o.clipBegin);
            
            if (result == 0 && clipEnd != null && o.clipEnd != null)
                result = clipEnd.compareTo(o.clipEnd);
            
            // give up if they were all equal and return the arbitrary 1:
            if (result == 0) return 1;
        }
        */
        return result;
    }
    
    
    /**
     * Removes this object from any parent collections.
     */
    public void removeFromCollections() {
        
        if (material != null) {
            Set parentCollection = material.getAnnotations();
            if (parentCollection != null) parentCollection.remove(this);
        }
        if (participant != null) {
            Set parentCollection = participant.getAnnotations();
            if (parentCollection != null) parentCollection.remove(this);
        }
    }
    
    /**
     * Returns a Set of every member of every persistable collection in this instance. If none (or if not applicable) returns an empty set.
     * Never returns null.
     */
    public Set getAllPersistableChildren() {
        
        return new HashSet();
    }

}
