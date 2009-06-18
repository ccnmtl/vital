package ccnmtl.vital3.utils;

import netscape.ldap.*;
import java.util.*;


/**
* This class contains methods that search Columbia'd LDAP system.
* The searches are performed with "filters" which are LDAP-special
* query formats.
* 
* About the LDAP in General:
* The data in LDAP is organized into a tree, called a Directory Information Tree (DIT).  
* Each "leaf" in the tree is called an entry.  
* An entry is comprised of a unique Distinguished Name (DN) and any number of attribute/value pairs.  
* The DN is the name of an entry.  The DN is like the primary key of a relational database.  
* A DN also shows the relation of the entry to the rest f the DIT, in a manner similar to 
* the way the full path name of a file shows the realtion of a particular file on your hard-drive 
* to the rest of the files on your system.  A path to a file on your system reads left to right 
* when reading from root to file.  A DN reads right to left when reading from root to entry.
* 
* LDAP attributes are in the form of mnemonics such as cn meaning common name, 
* and o for organization.  Usual attribute names are:
* cn = common name
* sn = surname
* givenname = first name
* uid = userid
* dn = Distinguished Name
* mail = email address
*
* The leftmost part of a DN is called a Relative Distinguished Name (RDN), 
* and is made up of an attribute/value that is in the entry.
* 
* Any attribute can have one or more values, if defined by the schema.
* 
* About our LDAP connections in particular:
* I wrote in an email to benno:
* >As far as I understand, I need to know the port, the Base DN, the scope,
* >and the entries' and attributes' names for students' entries in the
* >directory.  I am assuming that the host is "ldap.columbia.edu", and that I
* >should be accessing the ldap server anonymously.
* 
* Benno answered:
* >The port is the standard port (389).  The Base DN is either "" or 
* >"o=Columbia University, c=US".  The DN is constructed as "uni=xyz12, 
* >o=Columbia University, c=uS".  Aside from uni, the other fields you'll 
* >probably need follow the standard person schema definitions.
* >The hostname is correct and the binding is correct.
* 
* Note:  Benno also said:  
* >A privacy flagged user will look the same as a non-existent user: no 
* >results are returned.  Similarly, private fields within otherwise public 
* >entries will simply not be returned.
* 
* >In order to get "private" information, you must get a Kerberos ticket for 
* >the user (which involves having the user type their password) and bind to 
* >the LDAP server as the user with the Kerberos ticket.  However, if you are 
* >writing a web-based application, we may shortly have a tool that will make 
* >this much easier for you.
* 
* This tool Benno prophesied is WIND.  However, we cannot obtain private information 
* for a WIND-logged in user either. 
* 
* Here is an example of an LDAP record:
* $ java FilterSearch ldap.columbia.edu 389 "" "" "" "(uni=hsp7)"
* Search filter=(uni=hsp7)
* DN: uni=hsp7, o=Columbia University, c=US
* cn: Harish Sai Peri
* sn: Peri
* givenname: Harish Sai
* uid not present
* dn not present
* mail: hsp7@columbia.edu
* telephoneNumber: +1 212-853-2393
* 
* 
*@author  dc
*@since   1.0
*/
public class LDAPLookup {
  /**
  * Test that we can connect to LDAP and retrieve a user's information by his UNI.
  * Calls connectToLDAP().
  *
  *@param uni  the identifyer of the user we want to look up
  *@return     some of the information LDAP has on this user
  */
  public static String[] Test(String uni) {
    // java FilterSearch ldap.columbia.edu 389 "" "" "" "(uni=dc2033)"
    String host = "ldap.columbia.edu";
    int port = 389;
    String authid = "";
    String authpw = "";
    String base = "";
    String filter = "(ou=CCNMTL*)";
    // String[] ATTRS = {"cn","sn","givenname","uid","dn","mail","telephoneNumber"};
    String[] ATTRS = {"givenname", "sn", "mail", "ou"};
    String[] memInfo = new String[4];
    memInfo[0]=null;  memInfo[1]=null;  memInfo[2]=null;
    memInfo[3]=null;  
    
    connectToLDAP(memInfo, host, port, authid, authpw, base, filter, ATTRS);
    
    if (memInfo != null) {
      for (int i=0; i<memInfo.length; i++) {
        System.out.println("memInfo["+i+"]="+memInfo[i]+"---");
      }
    }
    
    return memInfo;  // hopefully, it's not null
  } // getTest



  /**
  * Does a subtree search using a specified filter.
  * Connects to LDAP and retrieves a user's information by his UNI.
  * Calls connectToLDAP().
  *
  *@param uni  the identifyer of the user we want to look up
  *@return     the user's first name, last name, and email
  */
  public static String[] getStudentInfo(String uni) {
    // java FilterSearch ldap.columbia.edu 389 "" "" "" "(uni=dc2033)"
    String host = "ldap.columbia.edu";
    int port = 389;
    String authid = "";
    String authpw = "";
    String base = "";
    String filter = "(uni=" + uni + ")";
    String[] ATTRS = {"cn","sn","givenname","uid","dn","mail","telephoneNumber"};
    // cn == common name,  sn == surname, givenname == first name
    // String[] ATTRS = {"givenname", "sn", "mail"};
    String[] memInfo = new String[7];
    memInfo[0]=null; 
	memInfo[1]=null;
	memInfo[2]=null;
	memInfo[3]=null;
	memInfo[4]=null;
	memInfo[5]=null;
	memInfo[6]=null;
    
    connectToLDAP(memInfo, host, port, authid, authpw, base, filter, ATTRS);
    
    return memInfo;  // hopefully, it's not null
  } // getStudentInfo



  /**
  * Performs the actual connecting to LDAP and talking to LDAP.
  *
  *@param memInfo    the array to fill up with found user data (calls getAttr() to do the actual filling)
  *@param host       the address of the ldap server
  *@param port       the port to talk to the server through
  *@param authid     leave blank
  *@param authpw     leave blank
  *@param base       leave blank
  *@param filter     constraints in the "find me this(these) user(s)" query
  *@param ATTRS      data fields to read about a user
  */
  private static void connectToLDAP(String[] memInfo, String host, int port, String authid, String authpw, String base, String filter, String[] ATTRS) {
    
    LDAPConnection ld = new LDAPConnection();
    try {
      // Connect to server and authenticate
      ld.connect(host, port, authid, authpw);
      
      /*
      The results of an LDAP search operation, represented as 
      an enumeration. Note that you can only iterate through this 
      enumeration once: if you need to use these results more 
      than once, make sure to save the results in a separate location.
      You can also use the results of a search in progress to abandon 
      that search operation. 
      */
      LDAPSearchResults res = ld.search(base, ld.SCOPE_SUB, filter, ATTRS, false );

      // Loop on results until complete
      /** WHILE (res.hasMoreElements())  TO GET A LIST OF MULTIPLE RESULTS */ 
      if (res.hasMoreElements()) {
        try {
          // Next directory entry
          LDAPEntry entry = res.next();
          // System.out.println("entry="+entry);
          getAttr(entry, memInfo, ATTRS);
        } 
        catch (LDAPReferralException e) {
          // System.err.println(e.toString());
        } 
        catch (LDAPException e) {
          // System.err.println(e.toString());
        }
      }
    }
    catch (LDAPException e) {
      // System.err.println(e.toString());
    }
    
    // Done, so disconnect
    if ((ld != null) && ld.isConnected()) {
      try {
        ld.disconnect();
      } catch (LDAPException e) {
        // System.err.println(e.toString());
      }
    }
  }// connectToLDAP
  
  
  
  
  /**
  * Gets called by connectToLDAP() to read the LDAP-returned results and fill the memInfo array with them.
  * 
  *@param entry      LDAP entry containing attributes
  *@param memInfo    the array to fill up with found user data 
  *@param ATTRS      array of attribute names to read out of entry and put into memInfo
  */
  public static void getAttr(LDAPEntry entry, String[] memInfo, String[] ATTRS) {
    LDAPAttribute attr;
    Enumeration enumVals;
    
    for (int i=0; i<ATTRS.length; i++) {
      attr = entry.getAttribute(ATTRS[i]);
      
      if (attr != null) {
        enumVals = attr.getStringValues();
        // Enumerate on values for this attribute
        if ((enumVals != null) && enumVals.hasMoreElements() ) {
          memInfo[i] = (String)enumVals.nextElement();
        }
      }
      
    } // for
  }// getAttr
  
  
  
  /**
  * Calls the Test() method with UNI supplied at command line.
  */
  public static void main(String[] args) {
    // String[] studinfo = LDAPLookup.getStudentInfo(args[0]);
    String[] studinfo = LDAPLookup.Test(args[0]);
    
    System.out.println(studinfo[0]);
    System.out.println(studinfo[1]);
    System.out.println(studinfo[2]);
    System.out.println(studinfo[3]);
    
  } // main
} // class




