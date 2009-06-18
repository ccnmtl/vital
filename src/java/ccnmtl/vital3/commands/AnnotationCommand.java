package ccnmtl.vital3.commands;

import java.util.Date;
import java.util.Set;

import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.Vital3Command;

public class AnnotationCommand extends Vital3Command {
    
    private String clipBegin;
    private String clipEnd;
    private Date dateModified;
    private String groupBy;
    private String limitBy;
    private String recent;
    private Set tags;
    private String title;
    private String template;
    private String text;
    private String type;
    
    /**
     * Note: if dateModified is set, it will be transferred. Otherwise, it will be set to
     * the current date/time.
     */
    public void transferToAnnotation(Annotation note) {
        if (clipBegin != null) note.setClipBegin(clipBegin);
        if (clipEnd != null) note.setClipEnd(clipEnd);
        if (dateModified != null) note.setDateModified(dateModified);
        else note.setDateModified(new Date());
        if (text != null) note.setText(text);
        if (title != null) note.setTitle(title);
        if (type != null) note.setType(type);
    }
    
    /*********** ENTITY GETTERS AND SETTERS ********/
    
    public Annotation getAnnotation() {
        return (Annotation) mapGet("annotation"); 
    }
    public void setAnnotation(Annotation obj) {
        mapSet("annotation", obj);
    }
    
    public Assignment getAssignment() {
        return (Assignment) mapGet("assignment"); 
    }
    public void setAssignment(Assignment obj) {
        mapSet("assignment", obj);
    }
    
    public Material getMaterial() {
        return (Material) mapGet("material"); 
    }
    public void setMaterial(Material obj) {
        mapSet("material", obj);
    }
    
    public Long getMaterialId() {
        return (Long) mapGet("materialId"); 
    }
    public void setMaterialId(Long obj) {
        mapSet("materialId", obj);
    }
    
    public Unit getUnit() {
        return (Unit) mapGet("unit"); 
    }
    public void setUnit(Unit obj) {
        mapSet("unit", obj);
    }
    
    public Long getWorksiteId() {
        return (Long) mapGet("worksiteId");
    }
    public void setWorksiteId(Long value) {
        mapSet("worksiteId", value);
    }
    
    
    /******** REGULAR GETTERS AND SETTERS ********************/
    
    public String getClipBegin() {
        return clipBegin;
    }
    public void setClipBegin(String clipBegin) {
        this.clipBegin=clipBegin;
    }
    
    public String getClipEnd() {
        return clipEnd;
    }
    public void setClipEnd(String clipEnd) {
        this.clipEnd=clipEnd;
    }
    
    public Date getDateModified() {
        return dateModified;
    }
    public void setDateModified(Date dateModified) {
        this.dateModified=dateModified;
    }
    
    public String getGroupBy() {
        return groupBy;
    }
    public void setGroupBy(String groupBy) {
        this.groupBy=groupBy;
    }
    
    public String getLimitBy() {
        return limitBy;
    }
    public void setLimitBy(String limitBy) {
        this.limitBy=limitBy;
    }
    
    public String getRecent() {
        return recent;
    }
    public void setRecent(String recent) {
        this.recent=recent;
    }
    
    //// stickytags is an alias for tags:
    public Set getStickytags() {
        return tags;
    }
    public void setStickytags(Set tags) {
        this.tags=tags;
    }
    public Set getTags() {
        return tags;
    }
    public void setTags(Set tags) {
        this.tags=tags;
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title=title;
    }
    
    public String getTemplate() {
        return template;
    }
    public void setTemplate(String template) {
        this.template=template;
    }
    
    //// note is an alias for text:
    public String getNote() {
        return text;
    }
    public void setNote(String text) {
        this.text=text;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text=text;
    }
    
    //// clipType is an alias for type:
    public String getClipType() {
        return type;
    }
    public void setClipType(String type) {
        this.type=type;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type=type;
    }
    
}