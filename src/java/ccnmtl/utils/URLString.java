package ccnmtl.utils;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.net.URLEncoder;

/**
 * URL class for holding URLs and allowing you to easily add parameters to the URL string.
 * I looked around a bit for something that would have this functionality at a relatively high-performance,
 * but couldn't find it...
 * The java.net.URL class carried with it a high construction cost and didn't have methods for fine-grained
 * modification of the parameters like I wanted.
 */
public class URLString {
 
    // a name/value String Map of parameters
    private HashMap params;
    
    // everything before the "?"
    private String firstPart;
    
    // the full string representation
    private String cachedStringRepresentation;
        
    // construct with a url string
    public URLString(String url) {
        
        HashMap params = new HashMap();
        this.firstPart = url;
        
        int paramIndex = url.indexOf('?');
        if (paramIndex != -1) {
            
            // parse out the params:
            this.firstPart = url.substring(0,paramIndex);
            String paramstring = url.substring(paramIndex+1);
            
            StringTokenizer st = new StringTokenizer(paramstring, "&=");
            while (st.hasMoreTokens()) {
                String parameterName = st.nextToken();
                String parameterValue = st.nextToken();
                params.put(parameterName, parameterValue);
            }
        }
        
        this.params = new HashMap(params);
        this.cachedStringRepresentation = this.generateStringRepresentation();
    }
    
    // construct with a url string and a Map of parameters. The url must not contain parameters!
    public URLString(String url, Map params) {
        
        this.firstPart = url;
        this.params = new HashMap(params);
        this.cachedStringRepresentation = this.generateStringRepresentation();
    }
    
    // construct with a url (that may have some parameters)
    public URLString(String url, String[] additionalNamesAndValues) {
        
        this(url);
        this.addParameters(additionalNamesAndValues);
    }
    
    // add a parameter. Both name and value must not be null.
    public void addParameter(String parameterName, String parameterValue) {
        
        if (parameterName == null || parameterValue == null) throw new IllegalArgumentException("cannot pass null parameters");
        
        this.addParameters( new String[]{parameterName,parameterValue} );
        
    }
    
    // pass an array of the form: new String[]{"name1","val1","name2","val2", ... }
    // the names + values will be encoded for you.
    public void addParameters(String[] namesAndVals) {
        try {
            for (int i=0; i<namesAndVals.length; i+=2) {
                String parameterName = namesAndVals[i];
                String parameterValue = namesAndVals[i+1];
                // add unencoded names+values to the map:
                params.put(parameterName, parameterValue);
                
                // add encoded names+values to the cached string:
                if (params.size() == 1){
                    this.cachedStringRepresentation += "?" + URLEncoder.encode(parameterName, "UTF-8") + "=" + URLEncoder.encode(parameterValue, "UTF-8");
                } else {
                    this.cachedStringRepresentation += "&" + URLEncoder.encode(parameterName, "UTF-8") + "=" + URLEncoder.encode(parameterValue, "UTF-8");
                }
                
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String generateStringRepresentation() {
        String paramstring = new String();
        try{
            Iterator iter = params.keySet().iterator();
            if (iter.hasNext()) {
                String parameterName = (String) iter.next();
                String parameterValue = (String) params.get(parameterName);
                paramstring = "?" + URLEncoder.encode(parameterName, "UTF-8") + "=" + URLEncoder.encode(parameterValue, "UTF-8");
            }
            while (iter.hasNext()) {
                String parameterName = (String) iter.next();
                String parameterValue = (String) params.get(parameterName);
                paramstring += "&" + URLEncoder.encode(parameterName, "UTF-8") + "=" + URLEncoder.encode(parameterValue, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return firstPart + paramstring;
    }
        
    // output the url as a string
    public String toString() {
        return this.cachedStringRepresentation;
    }
    
    // equals override
    public boolean equals(Object obj) {
        if (!(obj instanceof URLString)) return false;
        URLString urlObj = (URLString) obj;
        return (urlObj.toString().equals(this.toString()));
    }
    
}