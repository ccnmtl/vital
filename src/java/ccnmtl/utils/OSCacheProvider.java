package ccnmtl.utils;

import java.util.Properties;
import org.hibernate.util.PropertiesHelper;
import org.hibernate.util.StringHelper;
import org.hibernate.cache.*;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ccnmtl.utils.OSCache;

/**
* Support for OpenSymphony OSCache. This implementation assumes
 * that identifiers have well-behaved <tt>toString()</tt> methods.
 * NOTE: this does NOT currently store a link to the underlying cache, and calls to start and stop are
 * ignored completely.
 */
public class OSCacheProvider implements CacheProvider {
    
    
	/** 
    * The <tt>OSCache</tt> refresh period property suffix. 
    */
	public static final String OSCACHE_REFRESH_PERIOD = "refresh.period";
	/** 
    * The <tt>OSCache</tt> CRON expression property suffix. 
    */
	public static final String OSCACHE_CRON = "cron";
	
	private static final Properties OSCACHE_PROPERTIES = new Config().getProperties();
    
    
    protected final Log logger = LogFactory.getLog(getClass());
	
    
	/**
        * Builds a new {@link Cache} instance, and gets it's properties from the OSCache {@link Config}
	 * which reads the properties file (<code>oscache.properties</code>) from the classpath.
	 * If the file cannot be found or loaded, an the defaults are used.
	 *
	 * @param region
	 * @param properties
	 * @return
	 * @throws CacheException
	 */
	public Cache buildCache(String region, Properties properties) throws CacheException {
        
        
        
		int refreshPeriod = PropertiesHelper.getInt(
                                                    StringHelper.qualify(region, OSCACHE_REFRESH_PERIOD), 
                                                    OSCACHE_PROPERTIES, 
                                                    CacheEntry.INDEFINITE_EXPIRY
                                                    );
		String cron = OSCACHE_PROPERTIES.getProperty( StringHelper.qualify(region, OSCACHE_CRON) );
        
		// construct the cache        
        return new OSCache(refreshPeriod, cron, region);
	}
    
	public long nextTimestamp() {
		return Timestamper.next();
	}
    
	public boolean isMinimalPutsEnabledByDefault() {
		return false;
	}
    
	/**
        * Callback to perform any necessary cleanup of the underlying cache implementation
	 * during SessionFactory.close().
	 */
	public void stop() {
	}
    
	/**
        * Callback to perform any necessary initialization of the underlying cache implementation
	 * during SessionFactory construction.
	 *
	 * @param properties current configuration settings.
	 */
	public void start(Properties properties) throws CacheException {
	}    
	

}
