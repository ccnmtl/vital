package ccnmtl.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HormelClient {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    // the url of the hormel server
    private String hormelServerUrl;
    
    // the default "from" address:
    private String defaultFromAddress;
    
    /**
     * Constructor
     *@param hormelServerUrl    The FULL url to the hormel server with no trailing slash. e.g. http://hormel.ccnmtl.columbia.edu
     */
    public HormelClient(String hormelServerUrl, String defaultFromAddress) {
        this.hormelServerUrl = hormelServerUrl;
        this.defaultFromAddress = defaultFromAddress;
    }
    
    /**
     * 3 argument shortcut which uses the default "from" address
     */
    public void sendEmail(String toAddress, String subject, String body) throws Exception {
        
        sendEmail(this.defaultFromAddress, toAddress, subject, body);
    }
    

    public void sendEmail(String fromAddress, String toAddress, String subject, String body) throws Exception {
        
        if (fromAddress == null) throw new Exception("fromAddress was null");
        if (toAddress == null) throw new Exception("toAddress was null");
        if (subject == null) throw new Exception("subject was null");
        if (body == null) throw new Exception("body was null");
        
        String paramString = "from_address="+this.defaultFromAddress+"&to_address="+toAddress+"&subject="+subject+"&body="+body;
        post(this.hormelServerUrl, paramString);
    }
    

    public void post(String urlString, String paramString) throws Exception {
        sendRequest("POST", urlString, paramString);
    }
    

    private String sendRequest(String requestMethod, String urlString, String data) throws Exception {
        
        logger.debug("HormelClient sending: " + requestMethod + " " + urlString);
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        
        if (requestMethod.equals("POST")) {
            connection.setDoOutput(true);
            connection.setRequestMethod(requestMethod);
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(data);
            osw.flush();
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        
        String buffer;
        String result = "";
        while ((buffer = in.readLine()) != null)
            result += buffer;
        in.close();
        
        logger.debug("HormelClient received: " + result + "(end)");
        return result;
        
    }
    
    
}

