package ccnmtl.vital3.utils;

import java.util.Set;
import ccnmtl.vital3.VitalWorksite;

/** 
 * This interface guarantees the existence of an id getter+setter and a few more methods.
 * This should be implemented by all hibernated Vital3 classes. It was designed specifically for
 * my MockDB implementation, but now it is used by both Hibernate and Mock DAOs.
 *
 * I wanted to avoid making Persistable a superclass because I was not sure if Hibernate would
 * mind if I had a superclass that I was not mapping. This was easier to implement.
 *@author Eric Mattes
 *
**/
public interface Persistable {
    
    public void setId(Long id);
    
    public Long getId();
    
    public void removeFromCollections();
    
    public Set getAllPersistableChildren();
    
    /**
     * Returns the object's parent Worksite, even if it is a distant relative.
     * Not supported for VitalUser or for any of the Raw classes.
     */
    public VitalWorksite getRelatedWorksite();
    
}