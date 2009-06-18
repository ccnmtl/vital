package ccnmtl.vital3.commands;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.commands.Vital3Command;
import ccnmtl.vital3.*;
import ccnmtl.vital3.ucm.*;

/**
 * This is a slightly chaotic implementation of a command class used for ResponseController.
 * This command has properties (and getters and setters) for every entity that can be used by Response!
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
public class ResponseCommand extends Vital3Command {


    /************* ENTITY PROPERTY TRANSFERS **************/

    /**
     * NOTE: For all transferToXXXX methods:
     * Transfers the relevant non-null properties from this command object to the
     * object you pass. Entity reference properties will not get transferred. For those, get the ids
     * from this command object and then retrieve them separately.
     * Doesn't transfer null properties because blank fields would contain empty-strings, not nulls.
     */
    protected final Log logger = LogFactory.getLog(getClass());
    

    public AssignmentResponse transferToAssignmentResponse(AssignmentResponse target) {
        target.setDateSubmitted(dateSubmitted);
        target.setStatus(status);
        target.setText(text);
        return target;
    }

    public void transferFromAssignmentResponse(AssignmentResponse source) {
        // entity references and ids:
        this.setAssignmentResponse(source);
        Assignment assignment = source.getAssignment();
        if (assignment != null) {
            this.setAssignment(assignment);
            this.setAssignmentId(assignment.getId());
        }
        VitalParticipant participant = source.getParticipant();
        if (participant != null) {
            this.setParticipant(participant);
            this.setParticipantId(participant.getId());
        }
        // regular properties:
        this.setDateSubmitted(source.getDateSubmitted());
        this.setStatus(source.getStatus());
        this.setText(source.getText());
    }

    public Comment transferToComment(Comment target) {
        target.setStatus(status);
        target.setText(text);
        target.setType(Comment.FEEDBACK);
        target.setDateModified(new Date());
        return target;
    }

    public void transferFromComment (Comment source) {
        // entity references and ids:
        AssignmentResponse assignmentResponse = source.getAssignmentResponse();
        if (assignmentResponse != null) {
            this.setAssignmentResponse(assignmentResponse);
            this.setAssignmentResponseId(assignmentResponse.getId());
        }
        Answer answer = source.getAnswer();
        if (answer != null) {
            this.setAnswer(answer);
            this.setAnswerId(answer.getId());
        }
        VitalParticipant participant = source.getParticipant();
        if (participant != null) {
            this.setParticipant(participant);
            this.setParticipantId(participant.getId());
        }
        // regular properties:
        this.setDateModified(source.getDateModified());
        this.setStatus(source.getStatus());
        this.setText(source.getText());
        this.setType(source.getType());
    }



    /*********** ENTITY GETTERS AND SETTERS ********/

    protected String entity;

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity=entity; }

    
    public VitalUser getUser() {
        return (VitalUser) mapGet("user");
    }
    public void setUser(VitalUser obj) {
        mapSet("user", obj);
    }


    // AssignmentResponse, Comment
    public VitalParticipant getParticipant() {
        return (VitalParticipant) mapGet("participant");
    }
    public void setParticipant(VitalParticipant obj) {
        mapSet("participant", obj);
    }


    // Comment
    public Answer getAnswer() {
        return (Answer) mapGet("answer");
    }
    public void setAnswer(Answer obj) {
        mapSet("answer", obj);
    }


    // AssignmentResponse
    public Assignment getAssignment() {
        return (Assignment) mapGet("assignment");
    }
    public void setAssignment(Assignment obj) {
        mapSet("assignment", obj);
    }


    // Answer
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


    // Answer
    public Question getQuestion() {
        return (Question) mapGet("question");
    }
    public void setQuestion(Question obj) {
        mapSet("question", obj);
    }


    public RawUCMTerm getTerm() {
        return (RawUCMTerm) mapGet("term");
    }
    public void setTerm(RawUCMTerm obj) {
        mapSet("term", obj);
    }
    

    /********** RELATED ENTITY ID GETTERS AND SETTERS ************/


    public Long getAnswerId() {
        return (Long) mapGet("answerId");
    }
    public void setAnswerId(Long value) {
        mapSet("answerId", value);
    }


    public Long getAssignmentId() {
        return (Long) mapGet("assignmentId");
    }
    public void setAssignmentId(Long value) {
        mapSet("assignmentId", value);
    }


    public Long getAssignmentResponseId() {
        return (Long) mapGet("assignmentResponseId");
    }
    public void setAssignmentResponseId(Long value) {
        mapSet("assignmentResponseId", value);
    }


    public Long getCommentId() {
        return (Long) mapGet("commentId");
    }
    public void setCommentId(Long value) {
        mapSet("commentId", value);
    }


    public Long getParticipantId() {
        return (Long) mapGet("participantId");
    }
    public void setParticipantId(Long value) {
        mapSet("participantId", value);
    }


    public Long getQuestionId() {
        return (Long) mapGet("questionId");
    }
    public void setQuestionId(Long value) {
        mapSet("questionId", value);
    }
    

    /************** PROPERTY GETTERS AND SETTERS ***************/

    
    
    // don't correspond to any entity properties; just used by the essay and gl workspaces to toggle between the normal and "export/print" skins.
    // see essayWorkspace.vm
    protected String export;
    public String getExport() {
        return export;
    }
    public void setExport(String export) {
        logger.debug("Setting export:" + export);
        this.export=export;
    }
    
    // don't correspond to any entity properties; just used by the essay and gl workspaces to toggle between the normal and "Preview" skins.
    // see essayWorkspace.vm
    protected String preview;
    public String getPreview() {
        return preview;
    }
    public void setPreview(String preview) {
        logger.debug("Setting preview:" + preview);
        this.preview=preview;
    }
    
    
    // groupby and filter don't correspond to any entity properties; just used by the discussion workspace to specify which responses to show and how to group them.
    // see fiscussionWorkspace.vm and Assignment.java for details.
    
    protected String groupBy;
    public String getGroupBy() {
        return groupBy;
    }
    public void setGroupBy(String groupBy) {
        logger.debug("Setting groupBy:" + groupBy);
        this.groupBy=groupBy;
    }
    
    
    protected String filter;
    public String getFilter() {
        return filter;
    }
    public void setFilter(String filter) {
        logger.debug("Setting filter:" + filter);
        this.filter=filter;
    }
    
    
    private Date dateSubmitted;
    private Date dateModified;
    private String text;
    private String type;
    private Integer status;
    
    
    // Comment
    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date value) { this.dateModified = value; }

    // AssignmentResponse
    public Date getDateSubmitted() { return dateSubmitted; }
    public void setDateSubmitted(Date value) { this.dateSubmitted = value; }

    // Answer, AssignmentResponse, Comment
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { this.status=value; }

    // Answer, AssignmentResponse, Comment
    public String getText() { return text; }
    public void setText(String value) { this.text = value; }

    // AssignmentResponse, Comment
    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
}
