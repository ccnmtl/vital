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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Provider;
import org.apache.commons.codec.binary.Hex;


public class VideoUploadClient {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    private String host;
    private String key;
    
    public VideoUploadClient(String host, String key) {
        this.host = host;
        this.key = key;
    }

    public String getHost() { return this.host; }
    public String getKey() { return this.key; }

    /**
      Generate hash of key, username, redirect_back & nonce
      * Concatenates the salt,
      ** the student's UNI,
      ** the shared secret key,
      ** and two URLS
      * The SHA1 hash of that concatenated string is calculated and the students browser is sent on a Redirect to
    **/
    public String getHash(String uni, String redirectBack, String notify, String nonce) {
        String params = uni + ":" + redirectBack + ":" + notify + ":" + nonce;

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(this.key.getBytes(),"HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(params.getBytes());
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        } catch (InvalidKeyException e) {
            logger.error(e);
        } catch(StackOverflowError e) {
            logger.error(e);
        }
        return "";
    }
}

