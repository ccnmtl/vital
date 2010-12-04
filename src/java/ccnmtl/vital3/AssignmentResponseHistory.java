package ccnmtl.vital3;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.vital3.utils.Persistable;

public class AssignmentResponseHistory implements Persistable, Serializable {
   
   protected final Log logger = LogFactory.getLog(getClass());

   // a name which is the property name other entities use for holding a reference to this type of entity.
   public static final String simpleName = "assignmentResponseHistory";
   
   /** identifier field */
   private Long id;
   
   /** nullable persistent field */
   private Long assignmentId;

   /** nullable persistent field */
   private Long participantId;
   
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
   
   private Date dateCreated;
   
   public AssignmentResponseHistory() {
   }
   
   public AssignmentResponseHistory(AssignmentResponse ar) {
       setAssignmentId(ar.getAssignment().getId());
       setParticipantId(ar.getParticipant().getId());
       setDateSubmitted(ar.getDateSubmitted());
       setStatus(ar.getStatus());
       setText0(ar.getText0());
       setText1(ar.getText1());
       setText2(ar.getText2());
       setText3(ar.getText3());
       setText4(ar.getText4());
       setText5(ar.getText5());
       setText6(ar.getText6());
       setText7(ar.getText7());
       setText8(ar.getText8());
       setText9(ar.getText9());
       setText10(ar.getText10());
       setText11(ar.getText11());
       setText12(ar.getText12());
       setText13(ar.getText13());
       setText14(ar.getText14());
       setText15(ar.getText15());
       setDateCreated(new Date());
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Long getAssignmentId() {
      return assignmentId;
   }

   public void setAssignmentId(Long assignmentId) {
      this.assignmentId = assignmentId;
   }

   public Long getParticipantId() {
      return participantId;
   }

   public void setParticipantId(Long participantId) {
      this.participantId = participantId;
   }

   public Date getDateSubmitted() {
      return dateSubmitted;
   }

   public void setDateSubmitted(Date dateSubmitted) {
      this.dateSubmitted = dateSubmitted;
   }

   public Integer getStatus() {
      return status;
   }

   public void setStatus(Integer status) {
      this.status = status;
   }

   public String getText0() {
      return text0;
   }

   public void setText0(String text0) {
      this.text0 = text0;
   }

   public String getText1() {
      return text1;
   }

   public void setText1(String text1) {
      this.text1 = text1;
   }

   public String getText2() {
      return text2;
   }

   public void setText2(String text2) {
      this.text2 = text2;
   }

   public String getText3() {
      return text3;
   }

   public void setText3(String text3) {
      this.text3 = text3;
   }

   public String getText4() {
      return text4;
   }

   public void setText4(String text4) {
      this.text4 = text4;
   }

   public String getText5() {
      return text5;
   }

   public void setText5(String text5) {
      this.text5 = text5;
   }

   public String getText6() {
      return text6;
   }

   public void setText6(String text6) {
      this.text6 = text6;
   }

   public String getText7() {
      return text7;
   }

   public void setText7(String text7) {
      this.text7 = text7;
   }

   public String getText8() {
      return text8;
   }

   public void setText8(String text8) {
      this.text8 = text8;
   }

   public String getText9() {
      return text9;
   }

   public void setText9(String text9) {
      this.text9 = text9;
   }

   public String getText10() {
      return text10;
   }

   public void setText10(String text10) {
      this.text10 = text10;
   }

   public String getText11() {
      return text11;
   }

   public void setText11(String text11) {
      this.text11 = text11;
   }

   public String getText12() {
      return text12;
   }

   public void setText12(String text12) {
      this.text12 = text12;
   }

   public String getText13() {
      return text13;
   }

   public void setText13(String text13) {
      this.text13 = text13;
   }

   public String getText14() {
      return text14;
   }

   public void setText14(String text14) {
      this.text14 = text14;
   }

   public String getText15() {
      return text15;
   }

   public void setText15(String text15) {
      this.text15 = text15;
   }

   public Date getDateCreated() {
      return dateCreated;
   }

   public void setDateCreated(Date dateCreated) {
      this.dateCreated = dateCreated;
   }

   public Log getLogger() {
      return logger;
   }


   public Set getAllPersistableChildren() {
      Set children = new HashSet();
      return children;
   }

   public VitalWorksite getRelatedWorksite() {
      // TODO Auto-generated method stub
      return null;
   }

   public void removeFromCollections() {
      // nothing to do
   }
   
   static private final int TOTALSEGMENTS = 16;
   public String getText() {
       StringBuffer textBuffer = new StringBuffer();
       for (int segment = 0; segment < AssignmentResponseHistory.TOTALSEGMENTS; segment++) {
           try {
               Method getter = (AssignmentResponseHistory.class).getMethod("getText" + segment, new Class[] {});
               Object obj = getter.invoke(this, new Object[] {});
               if (obj != null)
                   textBuffer.append ((String) obj);
           } catch (IllegalAccessException e) {
               throw new RuntimeException("That segment doesn't exist.");
           } catch (InvocationTargetException e) { 
               throw new RuntimeException("Unable to invoke that method..");
           } catch (NoSuchMethodException e) {
               throw new RuntimeException("That segment doesn't exist.");
           } 
       }

       return textBuffer.toString();
   }
}
