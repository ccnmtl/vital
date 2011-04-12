package ccnmtl.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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

