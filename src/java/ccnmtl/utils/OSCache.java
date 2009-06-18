package ccnmtl.utils;

import java.util.*;

import org.hibernate.util.PropertiesHelper;
import org.hibernate.util.StringHelper;
import org.hibernate.cache.*;

import com.opensymphony.oscache.extra.*;
import com.opensymphony.oscache.base.Config;
import com.opensymphony.oscache.base.CacheEntry;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
* Adapter for the OSCache implementation
 */
public class OSCache implements Cache {
    
    /** 
    * The <tt>OSCache</tt> cache capacity property suffix. 
    */
    
    public static final String OSCACHE_CAPACITY = "cache.capacity";
    
    private static final Properties OSCACHE_PROPERTIES = new Config().getProperties();
	/** 
        * The OSCache 2.0 cache administrator. 
        */
	private static GeneralCacheAdministrator cache = new GeneralCacheAdministrator();
    
    private static Integer capacity = PropertiesHelper.getInteger(OSCACHE_CAPACITY,
                                                                  OSCACHE_PROPERTIES);
  
    private static CacheEntryEventListenerImpl stats = new com.opensymphony.oscache.extra.CacheEntryEventListenerImpl();
    
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    
      
    static {
        if (capacity != null) cache.setCacheCapacity(capacity.intValue());
        cache.getCache().addCacheEventListener (stats , com.opensymphony.oscache.extra.CacheEntryEventListenerImpl.class);
    }
    
    
    private final int refreshPeriod;
	private final String cron;
	private final String regionName;
    private final String[] regionGroups;
	
	private String toString(Object key) {
		return String.valueOf(key) + "." + regionName;
	}
    
	public OSCache(int refreshPeriod, String cron, String region) {
		this.refreshPeriod = refreshPeriod;
		this.cron = cron;
		this.regionName = region;
        this.regionGroups = new String[] {region};
        
        
	}
    
	public Object get(Object key) throws CacheException {
		try {
			return cache.getFromCache( toString(key), refreshPeriod, cron );
		}
		catch (NeedsRefreshException e) {
			cache.cancelUpdate( toString(key) );
			return null;
		}
	}
    
	public void put(Object key, Object value) throws CacheException {
		cache.putInCache( toString(key), value, regionGroups );
	}
    
	public void remove(Object key) throws CacheException {
		cache.flushEntry( toString(key) );
	}
    
	public void clear() throws CacheException {
		cache.flushGroup(regionName);
	}
    
	public void destroy() throws CacheException {
		synchronized (cache) {
		    cache.destroy();
        }
	}
    
	public void lock(Object key) throws CacheException {
		// local cache, so we use synchronization
	}
    
	public void unlock(Object key) throws CacheException {
		// local cache, so we use synchronization
	}
    
	public long nextTimestamp() {
		return Timestamper.next();
	}
    
	public int getTimeout() {
		return Timestamper.ONE_MS * 60000; //ie. 60 seconds
	}
    
	public Map toMap() {
	    logger.warn ("Warning! ToMap called. Returning blank map.");
		// throw new UnsupportedOperationException();
		return new HashMap ();
	}    
    
	public long getElementCountOnDisk() {
	    logger.debug ("GetElementCount on Disk called. Returning -1.");
		return -1;
	}
    
	public long getElementCountInMemory() {
	    logger.debug ("Dummy function getElementCountInMemory called");
		// this works:
		return cache.getCache().getNbEntries();
	}
    
	public long getSizeInMemory() {
	    logger.debug ("Dummy function getSizeInMemory called-- returning -1.");
		return -1;
	}
    
	public String getRegionName() {
		return regionName;
	}
    
	public void update(Object key, Object value) throws CacheException {
		put(key, value);
	}    
    
	public Object read(Object key) throws CacheException {
		return get(key);
	}
	
	public String stats() {
	    return stats.toString();
	}
	
	public void printStats() {
	    logger.debug (stats.toString());
	}
	
	/*
	// http://jsourcery.com/api/opensymphony/oscache/2.3.2/com/opensymphony/oscache/base/Cache.html
    public int getSize() {
        return cache.getCache().getSize();
    }
	
    public Map getMap() {
        return cache.getCache().cacheMap();
    }
    */
    	
}
