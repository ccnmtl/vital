package ccnmtl.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import ccnmtl.jtasty.*;
import ccnmtl.jtasty.dao.JTastyDAO;
//import ccnmtl.jtasty.test.*; /// REMOVE THIS

public class TastyClient {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    private TastyBean tastyBean;
    
    
    // I'm a bit worried about whether or not URLConnection methods will cause the program to stall while they wait for response. This should be multithreaded.
    // currently, PloneTasty fires off the outbound (post, delete) communication asynchronously and returns true without waiting for a response.
    // PloneTasty does not indicate whether it uses GET asynchronously. I suppose the thread which does the request HAS to wait for it to respond anyway.
    
    // the url of the tasty server
    private String tastyServerUrl;
    // the name of the tasty service (each service has a non-overlapping "space" on the server)
    private String tastyServiceName;
    // the base url to use for all communication with tasty
    private String tastyBaseUrl;
    
    private static boolean useHTTP = false; /// set to true if you want to use an external Tasty service.
    
    
    /**
     * Constructor
     *@param tastyServerUrl    The FULL url to the tasty server with no trailing slash. e.g. http://tasty.ccnmtl.columbia.edu
     *@param tastyServiceName  The name of the tasty service you are using.
     */
     
    public TastyClient(String tastyServerUrl, String tastyServiceName) {
        this.tastyServerUrl = tastyServerUrl;
        this.tastyServiceName = tastyServiceName;

        resetTastyBaseUrl();        
    }

    
    public void beanstatus () {
        if (tastyBean != null) {
            logger.debug ("Tasty Bean Exists.");
            if (tastyBean.getDao() != null) {
                logger.debug ("Tasty Bean Has DAO.");
            } else {
                logger.debug ("Tasty Bean Has No DAO.");
            } 
        } else {
            logger.debug ("Tasty Bean Doesn't Exist.");
        }
    }
    
    protected void resetTastyBaseUrl() {
        this.tastyBaseUrl = this.tastyServerUrl + "/service/" + this.tastyServiceName + "/";
    }
    
    /**
     * Will send the required add and delete commands in order to synchronize the server's state so that the item
     * will have only the tags you pass in the Set. You may pass null if you want to delete all the item's tags.
     */
    public void setTagsForItem(String userId, String itemId, Set newTags) throws Exception {
        logger.debug ("setTagsForItem.");
        
        if (userId == null) throw new Exception("userId was null");
        if (itemId == null) throw new Exception("itemId was null");
        
        Set oldTags = getTagsForItem(userId, itemId);
        
        if (newTags != null && newTags.size() > 0) {
            
            Set tagsToAdd = new HashSet(newTags);
            // tags to add: the new set minus the old set
            tagsToAdd.removeAll(oldTags);
            // tags to delete: the old set minus the new set
            oldTags.removeAll(newTags);
            
            // add new tags:
            addTagsToItem(userId, itemId, tagsToAdd);
        } 
        // delete old tags:
        deleteTagsFromItem(userId, itemId, oldTags);
        
    }
    
    
    public String getRawOutput() throws Exception {
    
        // returns a lot of info. Like this: {"items":[{"item":"testItem"}],"tags":[{"tag":"testTag"},{"tag":"a"},{"tag":"b"},{"tag":"c"}],"users":[{"user":"testUser"}],"user_item_tags":[[{"user":"testUser"},{"item":"testItem"},{"tag":"a"}],[{"user":"testUser"},{"item":"testItem"},{"tag":"b"}],[{"user":"testUser"},{"item":"testItem"},{"tag":"c"}]]}
        
        return get(tastyBaseUrl);
    }
    
    
    public Set getAllTagsForUser(String userId) throws Exception {
        
        logger.debug ("getAllTagsForUser.");
        if (userId == null) throw new Exception("userId was null");
        
        
        String urlString = this.tastyBaseUrl + "user/" + userId + "/cloud";
        
        
        // returns something like: {"items":[{"count":1,"item":"testItem"}],"tags":[{"count":1,"tag":"a"}]}

        
        if (this.useHTTP) {
            try {
                String jsonString = get(urlString);
                return parseJSONTags(jsonString);
            } catch (FileNotFoundException fnfe) {
                // FNFE gets thrown when a user has never been set up with the tasty service
                return new TreeSet();
            }
         }
         else {
             String jsonString = get(urlString);
             if (jsonString != null) {
                return parseJSONTags(jsonString);
             }
             else {
                return new TreeSet();
             } 
         }
    }
    
    /**
     * Returns a Map of all tagged items, each with its own Set of tags. The Map is keyed by item (String) and holds values which
     * are ordered Sets of tags (Strings). In the event that the user has no tagged items, an empty Map is returned.
     */
    public Map getAllItemsAndTagsForUser(String userId) throws Exception {
        
        logger.debug ("getAllItemsAndTagsForUser.");
        if (userId == null) throw new Exception("userId was null");
        
        String urlString = this.tastyBaseUrl + "user/" + userId;
        
        // returns something like: {"items":[{"item":"testItem"}],"tag_items":[[{"tag":"a"},{"item":"testItem"}]],"tags":[{"tag":"a"}]}
        
        if (this.useHTTP) {
            try {
                String jsonString = get(urlString);
                if (jsonString == "invalid") throw new FileNotFoundException();
                return parseJSONItemsAndTags(jsonString);
            } catch (FileNotFoundException fnfe) {
                // FNFE gets thrown when a user has never been set up with the tasty service
                return new HashMap();
            }
         }
         else {
             String jsonString = get(urlString);
             if (jsonString != null) {
                if (jsonString == "invalid") throw new FileNotFoundException();
                return parseJSONItemsAndTags(jsonString);
             }
             else {
                return new HashMap();
             } 
         }
        
        
    }
    
    
    public Set getTagsForItem(String userId, String itemId) throws Exception {
        
        logger.debug ("getTagsForItem.");
        if (userId == null) throw new Exception("userId was null");
        if (itemId == null) throw new Exception("itemId was null");
        
        String urlString = this.tastyBaseUrl + "user/" + userId + "/item/" + itemId;
        
        // returns something like: {"tags":[{"tag":"taggy"},{"tag":"eric"}]}
        if (this.useHTTP) {
            try {
                String jsonString = get(urlString);
                return parseJSONTags(jsonString);
            } catch (FileNotFoundException fnfe) {
                // FNFE gets thrown when a user has never been set up with the tasty service
                return new TreeSet();
            }
         }
         else {
             String jsonString = get(urlString);
             if (jsonString != null) {
                return parseJSONTags(jsonString);
             }
             else {
                return new TreeSet();
             } 
         }
        
    }
    
    
    public void addTagsToItem(String userId, String itemId, Set tags) throws Exception {
    
        logger.debug ("addTagsToItem.");
        if (userId == null) throw new Exception("userId was null");
        if (itemId == null) throw new Exception("itemId was null");
        if (tags == null) throw new Exception("tags was null");
        
        if (tags.size() == 0) return;
        
        String urlString = this.tastyBaseUrl + "user/" + userId + "/item/" + itemId + "/";
        
        Iterator tagIter = tags.iterator();
        while (tagIter.hasNext()) {
            String tag = (String) tagIter.next();
            String safeTag = URLEncoder.encode(tag, "UTF-8");
            urlString += "tag/" + safeTag + "/";
        }
        
        try {
            // if all goes well, returns: ok
            post(urlString);
        } catch (FileNotFoundException fnfe) {
            // assume the user was not set up with an 'account' yet and attempt to init them:
            String initUserUrl = this.tastyBaseUrl + "user/" + userId;
            post(initUserUrl);
            // try again:
            post(urlString);
        }
        
    }
    
    
    public void deleteTagsFromItem(String userId, String itemId, Set tags) throws Exception {

        logger.debug ("deleteTagsFromItem.");
        
        if (userId == null) throw new Exception("userId was null");
        if (itemId == null) throw new Exception("itemId was null");
        if (tags == null) throw new Exception("tags was null");
        
        if (tags.size() == 0) return;
        
        String urlString = this.tastyBaseUrl + "user/" + userId + "/item/" + itemId + "/";
        
        Iterator tagIter = tags.iterator();
        while (tagIter.hasNext()) {
            String tag = (String) tagIter.next();
            String safeTag = URLEncoder.encode(tag, "UTF-8");
            urlString += "tag/" + safeTag + "/";
        }
        
        // returns: ok
        delete(urlString);
    }
    
    
    
    public void delete(String urlString) throws Exception {
        //sendRequest("DELETE", urlString);
        logger.debug("TastyClient deleting:  " + urlString);
        tastyBean.tastyDelete(urlString);
    }
    
    public String get(String urlString) throws Exception {
        //return sendRequest("GET", urlString);
        if (tastyBean == null ) throw new Exception ("The bean is null.");
        if (tastyBean.getDao() == null ) throw new Exception ("The dao is null.");
        
        logger.debug("TastyClient getting:  " + urlString);
        String s = tastyBean.tastyGet(urlString);
        if (s != null ) logger.debug("TastyClient got: " + s);
        else logger.debug ("TastyClient got null.");
        return s;
    }
    
    public void post(String urlString) throws Exception {
        //sendRequest("POST", urlString);
        if (tastyBean == null ) throw new Exception ("The bean is null.");
        else logger.debug ("We have a bean.");
        if (tastyBean.getDao() == null ) throw new Exception ("The dao is null.");
        
        
        logger.debug("TastyClient posting:  " + urlString);
        tastyBean.tastyPost(urlString);
    }
    
    private String sendRequest(String requestMethod, String urlString) throws Exception {
        
        logger.debug("TastyClient sending: " + requestMethod + " " + urlString);
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(requestMethod);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
        String buffer;
        String result = "";
        while ((buffer = in.readLine()) != null)
            result += buffer;
        in.close();
        
        logger.debug("TastyClient received: " + result + "(end)");
        return result;
        
    }
    
    
    private Set parseJSONTags(String jsonString) throws JSONException, UnsupportedEncodingException {
        
        JSONObject jsonMain = new JSONObject(jsonString);
        JSONArray jsonTags = jsonMain.getJSONArray("tags");
        
        int numTags = jsonTags.length();
        TreeSet stringTags = new TreeSet();
        JSONObject jsonTag = null;
        
        for (int i=0; i < numTags; i++) {
            jsonTag = jsonTags.getJSONObject(i);
            stringTags.add(URLDecoder.decode(jsonTag.getString("tag"), "UTF-8"));
        }
        
        return stringTags;
    }
    
    
    private HashMap parseJSONItemsAndTags(String jsonString) throws JSONException, UnsupportedEncodingException {
        
        
        // {"items":[{"item":"testItem"}],"tag_items":[[{"tag":"a"},{"item":"testItem"}]],"tags":[{"tag":"a"}]}
        
        JSONObject jsonMain = new JSONObject(jsonString);
        JSONArray jsonTaggedItems = jsonMain.getJSONArray("tag_items");
        
        int numTagItems = jsonTaggedItems.length();
        HashMap taggedItems = new HashMap();
        TreeSet tags = null;
        JSONArray jsonTaggedItem = null;
        JSONObject jsonItem = null;
        JSONObject jsonTag = null;
        String itemName = null;
        
        for (int i=0; i < numTagItems; i++) {
            jsonTaggedItem = jsonTaggedItems.getJSONArray(i);
            jsonTag = jsonTaggedItem.getJSONObject(0);
            jsonItem = jsonTaggedItem.getJSONObject(1);
            itemName = jsonItem.getString("item");
            
            tags = (TreeSet) taggedItems.get(itemName);
            if (tags == null) {
                // if it's not already in the Map, put a new one in there:
                tags = new TreeSet();
                taggedItems.put(itemName, tags);
            }
            tags.add(URLDecoder.decode(jsonTag.getString("tag"), "UTF-8"));
            
        }
        return taggedItems;
    }
    

    
    
    /********* Getters and Setters *************/
    
    public String getTastyServerUrl() {
        return tastyServerUrl;
    }
    public void setTastyServerUrl(String value) {
        this.tastyServerUrl = value;
        resetTastyBaseUrl();
    }
    
    
    public TastyBean getTastyBean() {
        return tastyBean;
    }
    public void setTastyBean(TastyBean value) {
        logger.debug ("TastyBean setter was called. Value was: " + value );
        this.tastyBean = value;
    }
    
    
    public String getTastyServiceName() {
        return tastyServiceName;
    }
    public void setTastyServiceName(String value) {
        this.tastyServiceName = value;
        resetTastyBaseUrl();
    }
    
}

