package ccnmtl.vital3.utils;

/** 
* This interface guarantees the existence of a "getRaw" method which will return the wrapped raw object.
*@author Eric Mattes
*
**/
public interface RawWrapper {
    
    public Object getRawObject();
    
    public void resetUCM();
}
