package ccnmtl.vital3.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.validation.MessageCodesResolver;

/**
 * custom modified implementation of the DefaultMessageCodesResolver.
 * this will translate the typical programmer-centric error codes to more user-centric error codes.
 * Following is the original documentation for DefaultMessageCodesResolver:
 *
 * <p>Will create 2 message codes for an object error, in the following order:
 * <ul>
 * <li>1.: code + "." + object name
 * <li>2.: code
 * </ul>
 *
 * <p>Will create 4 message codes for a field specification, in the following order:
 * <ul>
 * <li>1.: code + "." + object name + "." + field
 * <li>2.: code + "." + field
 * <li>3.: code + "." + field type
 * <li>4.: code
 * </ul>
 *
 * <p>E.g. in case of code "typeMismatch", object name "user", field "age":
 * <ul>
 * <li>1. try "typeMismatch.user.age"
 * <li>2. try "typeMismatch.age"
 * <li>3. try "typeMismatch.int"
 * <li>4. try "typeMismatch"
 * </ul>
 *
 * <p>Thus, this resolution algorithm can be leveraged for example to show
 * specific messages for binding errors like "required" and "typeMismatch":
 * <ul>
 * <li>at the object + field level ("age" field, but only on "user");
 * <li>at the field level (all "age" fields, no matter which object name);
 * <li>or at the general level (all fields, on any object).
 * </ul>
 *
 * <p>In case of array, List or Map properties, both codes for specific
 * elements and for the whole collection are generated. Assuming a field
 * "name" of an array "groups" in object "user":
 * <ul>
 * <li>1. try "typeMismatch.user.groups[0].name"
 * <li>2. try "typeMismatch.user.groups.name"
 * <li>3. try "typeMismatch.groups[0].name"
 * <li>4. try "typeMismatch.groups.name"
 * <li>5. try "typeMismatch.name"
 * <li>6. try "typeMismatch.java.lang.String"
 * <li>7. try "typeMismatch"
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 */
public class Vital3MessageCodesResolver implements MessageCodesResolver, Serializable {
    
	public static final String CODE_SEPARATOR = ".";
    
	public String[] resolveMessageCodes(String errorCode, String objectName) {
		return new String[] {errorCode + CODE_SEPARATOR + objectName, errorCode};
	}
    
	/**
     * This slightly modified version of this method will change a typeMismatch error
     * on a "Long" fieldType into a "error.choose.xxxx" error where xxxx is the field name.
     * This assumes that these errors are happening on id-selection dropdowns. All other
     * typeMismatch errors are translated into "error.invalid.xxxx"
     * original docs follow:
     *
     * Build the code list for the given code and field: an object/field-specific code,
	 * a field-specific code, a plain error code. Arrays, Lists and Maps are resolved
	 * both for specific elements and the whole collection.
	 * <p>See class javadoc for details on the generated codes.
	 * @return the list of codes
	 */
	public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class fieldType) {
        
        String newErrorCode = errorCode;
        if (errorCode.equals("typeMismatch")) {
            if (fieldType.equals(Long.class)) newErrorCode = "error.choose." + field;
            else newErrorCode = "error.invalid." + field;
        }
        
		List codeList = new ArrayList();
		List fieldList = new ArrayList();
		buildFieldList(field, fieldList);
		for (Iterator it = fieldList.iterator(); it.hasNext();) {
			String fieldInList = (String) it.next();
			codeList.add(newErrorCode + CODE_SEPARATOR + objectName + CODE_SEPARATOR + fieldInList);
		}
		int dotIndex = field.lastIndexOf('.');
		if (dotIndex != -1) {
			buildFieldList(field.substring(dotIndex + 1), fieldList);
		}
		for (Iterator it = fieldList.iterator(); it.hasNext();) {
			String fieldInList = (String) it.next();
			codeList.add(newErrorCode + CODE_SEPARATOR + fieldInList);
		}
		if (fieldType != null) {
			codeList.add(newErrorCode + CODE_SEPARATOR + fieldType.getName());
		}
		codeList.add(newErrorCode);
		return (String[]) codeList.toArray(new String[codeList.size()]);
	}
    
	/**
     * Add both keyed and non-keyed entries for the given field to the field list.
	 */
	protected void buildFieldList(String field, List fieldList) {
		fieldList.add(field);
		String plainField = field;
		int keyIndex = plainField.lastIndexOf('[');
		while (keyIndex != -1) {
			int endKeyIndex = plainField.indexOf(']', keyIndex);
			if (endKeyIndex != -1) {
				plainField = plainField.substring(0, keyIndex) + plainField.substring(endKeyIndex + 1);
				fieldList.add(plainField);
				keyIndex = plainField.lastIndexOf('[');
			}
			else {
				keyIndex = -1;
			}
		}
	}
    
}
