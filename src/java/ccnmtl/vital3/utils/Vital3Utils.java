package ccnmtl.vital3.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.*;
import java.util.*;
import java.net.URLEncoder;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.context.support.MessageSourceAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ccnmtl.utils.OmniComparator;
import ccnmtl.utils.Ordinal;
import ccnmtl.utils.URLString;
import ccnmtl.vital3.ucm.UserContextInfo;
import ccnmtl.vital3.ucm.UserCourseManager;
import ccnmtl.vital3.VitalParticipant;
import ccnmtl.vital3.VitalUser;
import ccnmtl.vital3.VitalWorksite;

public class Vital3Utils {
    
    public static final String affilListSessionAttributeName = "ccnmtl.vital3.affilList";
    public static final String afterLoginUrlSessionAttributeName = "ccnmtl.vital3.afterLoginUrl";
    public static final String userIdSessionAttributeName = "ccnmtl.vital3.userId";
    public static final String usernameSessionAttributeName = "ccnmtl.vital3.username";
    
    protected final static Log logger = LogFactory.getLog(Vital3Utils.class);
    
    /**
     * Clears the HttpSession of all Vital-related attributes
     */
    public static void clearSessionAttributes(HttpSession session) {
        
        logger.debug("clearing all vital3-related session attributes");
        session.removeAttribute(affilListSessionAttributeName);
        session.removeAttribute(afterLoginUrlSessionAttributeName);
        session.removeAttribute(userIdSessionAttributeName);
        session.removeAttribute(usernameSessionAttributeName);
    }
    
    
    /**
     * Returns all ordinalValues of the Ordinal items in a collection... in order
     */
    public static List getOrdinalValues(Collection collection) {
        
        ArrayList results = new ArrayList();
        
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            
            Ordinal item = (Ordinal) iter.next();
            results.add(item.getOrdinalValue());
        }
        
        Collections.sort(results);
        return results;
    }
            
        
    
    /**
     * Calls removeFromCollections on every object in a collection. All objects in this collection
     * must implement Persistable.
     */
    public static void removeMultipleFromCollections(Collection collection) {
        // convert collection to array (because iterator complains when we removeFromCollections inside a loop)
        Persistable[] pArray = (Persistable[]) collection.toArray(new Persistable[0]);
        for (int i=0; i<pArray.length; i++) {
            Persistable pObj = pArray[i];
            pObj.removeFromCollections();
        }
    }
    
    /**
     * This method supplies a single code point for how Vital3AuthViolations are to be handled.
     * Returns a ModelAndView (which is actually a RedirectView).
     */
    public static ModelAndView handleAuthViolation(Vital3AuthViolationException e) {
        
        if (e.userIsLoggedIn()) {
            // for when the logged-in user tries to do something without proper authorization.
            return redirectModelAndView("error.smvc", e);
        } else {
            // when not logged-in but the requested area requires login, redirect to login page
            return new ModelAndView(new RedirectView("login.smvc?message=You+must+be+logged+in+to+access+this+area", true));
        }
    }
    
    /**
     * Creates a ModelAndView which uses the "error" template, using your Errors object to get
     * the error string. Errors will be converted to a string using the errors.properties file.
     */
    public static ModelAndView createErrorMAV(Errors errors, MessageSourceAccessor msa) {
        
        String errorString = Vital3Utils.convertErrorsToString(errors, msa);
        Map model = new HashMap();
        model.put("message", errorString);
        return new ModelAndView("error", model);
    }
    
    /**
     * Creates a ModelAndView which uses the "error" template, using your Exception's message as the message string.
     */
    public static ModelAndView createErrorMAV(Exception e) {
        
        String errorString = e.getMessage();
        Map model = new HashMap();
        model.put("message", errorString);
        return new ModelAndView("error", model);
    }
    
    /**
     * Takes global errors (not field errors) from your errors object and puts them into a String.
     */
    public static String convertErrorsToString(Errors errors, MessageSourceAccessor msa) {
        String errorString = "";
        List globalErrors = errors.getGlobalErrors();
        Iterator errIter = globalErrors.iterator();
        while (errIter.hasNext()) {
            ObjectError globalError = (ObjectError) errIter.next();
            errorString += msa.getMessage(globalError) + "<br>";
        }
        return errorString;
    }
    
    /**
     * Puts "currentUser", "participant", "worksite", and "admin" keys/values into your model.
     * "admin" is put in for users with a participant access level of "TA_ACCESS" or higher.
     */
    public static void putUserInfoIntoModel(UserContextInfo userInfo, Map model) {
        
        if (userInfo != null) {
            VitalUser user = userInfo.getUser();
            if (user != null) model.put("currentUser", user);
            VitalParticipant participant = userInfo.getParticipant();
            if (participant != null) {
                // TA_ACCESS and over gets the user the "admin" template variable
                if (participant.getAccessLevel().compareTo(UserCourseManager.TA_ACCESS) >= 0) model.put("admin", "true");
                model.put("worksite", participant.getWorksite());
				model.put("participant", participant);          
            }
        }
    }
    
    
    /**
    * Use to initialize a collection for a collection of items. Example: You have a collection of Assignments
     * and you want to initialize all of their Questions.
     * You would call: initCollections(Assignment.class, myAssignments, "questions", Question.class), and it would return a Set
     * containing each Question that belonged to each Assignment, with no duplicates.
     *@param parentClass          The class of each item in the "items" collection.
     *@param items                A collection of items, each of the class itemClass.
     *@param propertyName         The property in itemClass which corresponds to the collection to be initialized.
     *@param childClass           The class of each item in the collection to be initialized.
     */
    public static Set initCollections(Class parentClass, Collection items, String propertyName, Class childClass) {
        
        Set allInitialized = null;
        
        try {
            
            // get the collection-getter Method:
            Method getter = Vital3Utils.getterForProperty(parentClass, propertyName);
            
            allInitialized = new HashSet();
            Iterator iter = items.iterator();
            
            while(iter.hasNext()) {
                
                Object item = iter.next();
                
                // get the items in the collection (initializing them)
                Set initialized = (Set) getter.invoke(item, null);
                
                allInitialized.addAll(initialized);
            }
            
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        return allInitialized;
    }
    
    /**
    * Use to initialize a collection for a single item. Example: You have an Assignment and you want to initialize its Questions.
     * You would call: initCollection(Assignment.class, myAssignment, "questions", Question.class), and it would return a Set
     * containing each Question that belonged to that Assignment.
     *@param parentClass          The class of the "item" object.
     *@param item                 An object of the class itemClass.
     *@param propertyName         The property in itemClass which corresponds to the collection to be initialized.
     *@param childClass           The class of each item in the collection to be initialized.
     */
    public static Set initCollection(Class parentClass, Object item, String propertyName, Class childClass) {
        Collection items = Arrays.asList( new Object[]{ item } );
        return initCollections(parentClass, items, propertyName, childClass);
    }
    
    
    /**
     * Retrieves (and thereby initializes) all related entities in a particular many-to-many relationship. Returns the entities
     * on the other side (the "children") in a Set. This does not return "Assoc" entities!
     * For example, you have a Collection of Questions and you want to get all of the associated Materials. You pass:
     * initM2MCollections(Question.class, questionSet, Material.class) and you'll get a Set of all Materials which
     * belonged to all of the Questions.
     * IMPORTANT: This relies on a strict naming convention! The collection must be named xxxAssociations where xxx is the name of
     * the child class.
     */
    public static Set initM2MCollections(Class parentClass, Collection items, Class childClass) {
        Set allChildren = new HashSet();
        
        try { 
            
            // get the name of the child entity (e.g. "material")
            String childName = Vital3Utils.getSimpleName(childClass);
            
            // get the Method for getting the assoc (e.g. "getMaterialAssociations")
            Method assocGetter = Vital3Utils.getterForProperty(parentClass, childName + "Associations");
			Method childGetter = null;
            
            Iterator iter = items.iterator();
            
            while(iter.hasNext()) {
                Object item = iter.next();
                // get the associated items (initializing that collection):

                Set assocs = (Set) assocGetter.invoke(item, null);
                Iterator assocIter = assocs.iterator();
                while (assocIter.hasNext()) {
                    Object assoc = assocIter.next();
					if (childGetter == null) childGetter = Vital3Utils.getterForProperty(assoc.getClass(), childName);
                    // get the related child entity and add it to the collection:
                    Object child = childGetter.invoke(assoc, null);
                    if (child == null) throw new RuntimeException("Child element was null!");
                    allChildren.add(child);
                }
            }
            
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        logger.debug("initM2MCollections: returning " + allChildren.size() + " " + childClass.getName() + " objects related to " + parentClass.getName() + " collection");
        return allChildren;
    }
    
    /**
    * Like initM2MCollections, but takes a single object rather than a collection of objects.
    */
    public static Set initM2MCollection(Class parentClass, Object item, Class childClass) {
        Collection items = Arrays.asList( new Object[]{ item } );
        return initM2MCollections(parentClass, items, childClass);
        
    }
    
    
    /**
     * Pass a collection of items, and for each of them, this will call the property-getter for the property
     * you specify, and will return the property value (which must implement Persistable) if calling 'getId' on it returns an
     * id equal to the one you pass. Example: items is a collection of Materials, propertyName is "worksite", id is 4. This
     * will call "getWorksite" on every Material in the collection and return worksite id 4 if it was found.
     * returns null if the item was not found.
     */
    public static Object getPropertyById(Collection items, String propertyName, Long id) {
        
        try {
            
            Iterator itemIter = items.iterator();
            if (itemIter.hasNext()) {
                
                Object item = itemIter.next();
                Class itemClass = item.getClass();
                Method propertyGetter = Vital3Utils.getterForProperty(itemClass, propertyName);
                
                Persistable property = (Persistable) propertyGetter.invoke(item, null);
                if (property.getId().equals(id)) return property;
            
                while(itemIter.hasNext()) {
                    
                    item = itemIter.next();
                    property = (Persistable) propertyGetter.invoke(item, null);
                    if (property.getId().equals(id)) return property;           
                }
            }
            
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException("Invocation Target Exception!", e);
        }
        // if we got here, it was not found
        return null;
    }
    
    /**
     * Will retrieve the Method which gets the specified parent entity from an instance of the specified
     * child entity class. Example: To get the "getWorksite" method for the Material class, call:
     * getterForParentEntity(Material.class, VitalWorksite.class)
     */
    public static Method getterForParentEntity(Class childClass, Class parentClass) {
        return getterForProperty(childClass, getSimpleName(parentClass));
    }
    
    /**
     * This will retrieve a String which represents the class according to the naming conventions
     * used in entity-relationship for Vital3. Examples: for Materials, it returns "material".
     * For both VitalWorksites and RawUCMWorksites, it returns "worksite". See Entity Relationship
     * Diagram for usage or see each java class file for the string.
     */
    public static String getSimpleName(Class parentClass) {
        
        try {
            Field parentPropertyNameField = parentClass.getField("simpleName");
            return (String) parentPropertyNameField.get(null);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Illegal Access!", e);
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Illegal Argument!", e);
        } catch(NoSuchFieldException e) {
            throw new RuntimeException("No such Field!", e);
        }
    }
    
    /**
     * This will return a property-getter Method for a given property name.
     * This prepends "get" and capitalizes the first letter of the property name.
     */
    public static Method getterForProperty(Class clazz, String propertyName) {
        try {
            return clazz.getMethod(getterNameForProperty(propertyName), null);
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("No getter method found in class " + clazz.getName() + " called "  +  getterNameForProperty(propertyName) , e);
        }
    }
    
    /**
     * This will return a property-getter method name for a given property name.
     * This prepends "get" and capitalizes the first letter of the property name.
     */
    public static String getterNameForProperty(String propertyName) {
        return "get" + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
    }
    
    /**
     * This will return a property-setter method name for a given property name.
     * This prepends "set" and capitalizes the first letter of the property name.
     */
    public static String setterNameForProperty(String propertyName) {
        return "set" + propertyName.substring(0,1).toUpperCase() + propertyName.substring(1);
    }
    
    /**
     * Pass a collection of RawWrappers and get back a Set of the raw objects inside each one.
     * This will throw a ClassCastException if any of the Objects are not RawWrappers or a
     * RuntimeException if any of the RawWrappers.getRaw() methods return null.
     * In accordance with Set behavior, the resulting Set will not contain duplicates.
     */
    public static Set getSetOfRaws(Collection wrappers) {
        
        HashSet rawSet = new HashSet();
        return (Set) getCollectionOfRaws(wrappers, rawSet);
    }
    
    /**
     * Pass a collection of RawWrappers and get back a List of the raw objects inside each one.
     * This will throw a ClassCastException if any of the Objects are not RawWrappers or a
     * RuntimeException if any of the RawWrappers.getRaw() methods return null.
     * The list of raws will be in the same order as the collection's iterator method returns.
     */
    public static List getListOfRaws(Collection wrappers) {
        
        ArrayList rawList = new ArrayList();
        return (List) getCollectionOfRaws(wrappers, rawList);
    }
    
    public static Collection getCollectionOfRaws(Collection wrappers, Collection targetCollection) {
        
        Iterator wrapperIter = wrappers.iterator();
        while (wrapperIter.hasNext()) {
            RawWrapper wrapper = (RawWrapper) wrapperIter.next();
            Object raw = wrapper.getRawObject();
            if (raw == null) throw new RuntimeException("Raw object was null!");
            targetCollection.add(raw);
        }
        return targetCollection;
    }
    
    /**
     * Returns a string of about 30 digits, which is guaranteed to be completely unique (it will never occur again).
     */
    public static String generateRandomIdString() {
        String time = String.valueOf((new Date()).getTime());
        String random = String.valueOf(Math.random()).substring(2);
        return (time+random);
    }
    
    
    /**
     * A debugging method for printing the contents of a map to System.out.
     */
    public static void debugMap(Map map) {
        
        logger.debug("parsing map...");
        Set mapKeys = map.keySet();
        Iterator keyIter = mapKeys.iterator();
        while (keyIter.hasNext()){
            String key = (String)keyIter.next();
            Object valueObj = map.get(key);
            String value = (valueObj == null ? "null" : valueObj.toString());
            logger.debug("key: " + key + ", value: " + value);
        }
    }

    
    /**
     * Ensures that a Collection contains the class of object you think it does. Empty collections are ignored.
     * Throws an exception if the _FIRST_ object in the collection is not of the expected class. Only tests the
     * very first object! This is useful for debugging because it tells you what Class it was instead of throwing
     * an uninformative ClassCastException.
     */
    public static void verifyCollectionClass(Collection collection, Class desiredClass) throws Exception {
        Iterator iter = collection.iterator();
        if (iter.hasNext()){
            Object obj = iter.next();
            if (!desiredClass.isInstance(obj)) {
                String objClassName = obj.getClass().getName();
                throw new Exception("Expected collection to contain " + desiredClass.getName() + " but it contained " + objClassName);
            }
            
        }
        // if it is empty then exit regularly
    }
    
    /**
     * Used to put together a ModelAndView for returning when you want to redirect somewhere due to an error.
     * Assumes the error message should be appended to the url as a named parameter called "message".
     *@return a Spring MVC ModelAndView designed to be returned by a Controller upon error conditions.
     */
    public static ModelAndView redirectModelAndView(String destinationURL, Exception e) {

        URLString url = new URLString(destinationURL);
        return redirectModelAndView(url, e);
    }
    
    public static ModelAndView redirectModelAndView(URLString destinationURL, Exception e) {

        if (e == null) destinationURL.addParameter("message","An Error occurred and your request could not be completed.");
        else destinationURL.addParameter("message",e.getMessage());
        
        return redirectModelAndView(destinationURL.toString());
    }
    
    public static ModelAndView redirectModelAndView(String destinationURL) {
        
        return new ModelAndView(new RedirectView(destinationURL, true));
    }
    
	public static String replace(String str, String pattern, String replace) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();

		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e+pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}
    

    /**
     * Segments a string into a List of up to maxSegments strings of length numberOfCharsInEachSegment.
     */
    
    public static List segmentString (String stringToSegment, int numberOfCharsInEachSegment, int maxSegments) {
        List result = new ArrayList();
        if (stringToSegment == null) return result;
        int length = stringToSegment.length();
        if( length == 0) return result;
        
        int capacity =  numberOfCharsInEachSegment * maxSegments;
        int segmentsNeeded = (length + numberOfCharsInEachSegment - 1 ) / numberOfCharsInEachSegment;
        // ==  ceil ( length  /numberOfCharsInEachSegment). The proof is left as an exercise for the reader.
        //logger.debug("Capacity is " + capacity + "; length is " + length + "; segmentsNeeded is " + segmentsNeeded);
        if (length > capacity) throw new RuntimeException("String is too long."); 
        int segmentNumber, start, end;
        String segment;
        for(segmentNumber=0; segmentNumber < segmentsNeeded; segmentNumber++) {
            segment = null;
            //logger.debug("segmentNumber is " + segmentNumber);
            start = segmentNumber * numberOfCharsInEachSegment;
            //logger.debug("start is " + start);
            end = ( segmentNumber + 1 )  * numberOfCharsInEachSegment;
            if (end > length) end = length;
            //logger.debug("end is " + end);
            segment = stringToSegment.substring(start, end);
            result.add(segment);
            //logger.debug("Adding segment of length " + segment.length());
        }
        return result;
    }



    /**
    * Clone a Set of Cloneables
     */
    /*public static TreeSet cloneSet(Set oldSet) {
    TreeSet newSet = new TreeSet();
    Iterator iter = oldSet.iterator();
    while (iter.hasNext()) {
        Cloneable original = (Cloneable) iter.next();
        newSet.add(original.clone());
    }
    return newSet;
    }*/
    
    
    
    /************** FILE STUFF... NOT USED YET *************/
    
    
    /*
    // This path is relative to the Vital3 web application home, and serves as the root
    // directory for all public file storage:
    public static final String fileStorageRootPath = "files";
    
    public static Pattern fileSystemSafePattern = null;
    
    static {
        // compile pattern for filesystem-safe string checking:
        try {
            fileSystemSafePattern = Pattern.compile("\\w");
        } catch(PatternSyntaxException pse) {
            throw new RuntimeException(pse);
        }
    }
    */
    
    /**
    * Gets the contents of a particular saved file as a String.
     *@param worksiteIdString        The worksite's unique id string.
     *@param category                What is this file: "assignmentResponse" or ???
     *@param filename                The bare (i.e. no path) filename of the file, including extension.
     *@return                        The relative path, or null if the file didn't exist.
     */
    /*public static String getFileContents(String worksiteIdString, String category, String filename) throws IOException {
    
    File targetFile = getFileForWorksite(worksiteIdString, category, filename);
    if (!targetFile.exists()) throw new IOException("The file did not exist");
    BufferedReader reader = null;
    StringBuffer fileContent = new StringBuffer();
    try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
        String line = reader.readLine();
        while (line != null) {
            fileContent.append(line);
            line = reader.readLine();
        }
    } finally {
        reader.close();
    }
    return fileContent.toString();
    }*/
    
    /**
    * Gets a path to a particular saved file, relative to the application context root.
     *@param worksiteIdString        The worksite's unique id string.
     *@param category                What is this file: "assignmentResponse" or ???
     *@param filename                The bare (i.e. no path) filename of the file, including extension.
     *@return                        The relative path, or null if the file didn't exist.
     */
    /*public static String getPathToFile(String worksiteIdString, String category, String filename) throws IOException {
    
    File targetFile = getFileForWorksite(worksiteIdString, category, filename);
    if (!targetFile.exists()) return null;
    String url = targetFile.getPath();
    return url;
    }*/
    
    /**
    * Saves a file to its appropriate directory.
     *@param worksiteIdString        The worksite's unique id string.
     *@param category                What is this file: "assignmentResponse" or ???
     *@param filename                The bare (i.e. no path) filename of the file, including extension.
     *@param fileBytes               Byte array containing file data.
     *@param overwrite               If false, IOException will be thrown if file exists.
     */
    /*public static void saveUploadedFile(String worksiteIdString, String category, String filename, byte[] fileBytes, boolean overwrite) throws IOException {
    
    File targetFile = getFileForWorksite(worksiteIdString, category, filename);
    if (targetFile.exists() && !overwrite) throw new IOException("File already existed: " + targetFile.getAbsolutePath());
    FileCopyUtils.copy(fileBytes, targetFile);
    }*/
    
    
    /**
    * Gets a File reference for the specified filename for the specified worksite.
     * This abstracts the file storage methods to a certain extent.
     *@param worksiteIdString        The worksite's unique id string
     *@param category                What is this file: "assignmentResponse" or ???
     *@param filename                The bare (i.e. no path) filename of the file, including extension
     *@return                        A File representing the file you requested.
     */
    /*private static File getFileForWorksite(String worksiteIdString, String category, String filename) throws IOException {
    
    File dir = getWorksiteFileStore(worksiteIdString, true);
    File dir2 = new File(dir, category);
    return new File(dir2, filename);
    }

private static File getWorksiteFileStore(String worksiteIdString, boolean create) throws IOException {
    
    File worksiteFileStore = new File(fileStorageRootPath, worksiteIdString);
    if (!worksiteFileStore.exists()) {
        if (create) createWorksiteFileStore(worksiteIdString);
        else throw new IOException("The directory did not exist");
    }
    return worksiteFileStore;
}

private static File createWorksiteFileStore(String worksiteIdString) throws IOException {
    
    // rather than convert your worksiteIdString to a filesystem-safe name, this simply rejects
    // any worksiteIdString which is not already safe (only a-z, A-Z, 0-9, and _ are used).
    Matcher safeMatcher = fileSystemSafePattern.matcher(worksiteIdString);
    if (!safeMatcher.matches()) throw new RuntimeException("WorksiteIdString was not safe for filesystem use");
    
    File fileStorageRootDir = new File(fileStorageRootPath);
    if (!(fileStorageRootDir.exists() && fileStorageRootDir.isDirectory())) throw new RuntimeException("File storage root directory did not exist");
    if (!fileStorageRootDir.canWrite()) throw new RuntimeException("File storage root directory is not writable");
    
    File worksiteFileStore = new File(fileStorageRootPath, worksiteIdString);
    
    // assume that if the dir already exists that this method was called in error:
    if (worksiteFileStore.exists()) throw new RuntimeException("Directory already exists for this worksite");
    
    if (!worksiteFileStore.mkdir()) throw new RuntimeException("A filesystem error prevented the directory from being created");
    
    return worksiteFileStore;
}

*/
    
}


