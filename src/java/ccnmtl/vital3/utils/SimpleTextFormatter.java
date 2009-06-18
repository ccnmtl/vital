package ccnmtl.vital3.utils;

import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringEscapeUtils;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import java.util.Date;

/**
* This class provides thread-safe formatting and parsing services for Dates.
* See fields for the formatting conventions used. See the TextFormatter 
* interface for method documentation.
*/
public class SimpleTextFormatter implements TextFormatter {
    
    // format strings:
    public static final String FULL_DATE = "yyyy/M/d-H:mm:ss";
    //public static final String DATE_ONLY = "M/d/yyyy";
    public static final String DATE_ONLY = "MMM. d, yyyy";
    
    // thread-local class for each SimpleDateFormat:
    private static class TLFullDateFormat extends ThreadLocal {
        // initialValue is called when a thread calls "get" for the first time
        protected Object initialValue() {
            // construct a new SimpleDateFormat per thread.
            // This will happen only once per thread.
            return new SimpleDateFormat(FULL_DATE);
        }
    }
    private static class TLDateOnlyFormat extends ThreadLocal {
        // initialValue is called when a thread calls "get" for the first time
        protected Object initialValue() {
            // construct a new SimpleDateFormat per thread.
            // This will happen only once per thread.
            return new SimpleDateFormat(DATE_ONLY);
        }
    }
    
    // static instance of thread-local class:
    private static TLFullDateFormat fullDateFormat;
    private static TLDateOnlyFormat dateOnlyFormat;
    
    // static instance of apostrophe Pattern:
    private static Pattern aposRegEx;
    
    // static instance of alphanumeric CharSet String:
    //private static String alphaNumericString;
    
    // constructor
    public SimpleTextFormatter() {
        fullDateFormat = new TLFullDateFormat();
        dateOnlyFormat = new TLDateOnlyFormat();
        aposRegEx = Pattern.compile("'");
        //CharSet alphaNumeric = new CharSet( new String[]{ CharSet.ASCII_ALPHA.toString(), CharSet.ASCII_NUMERIC.toString() } );
        //CharSet alphaNumeric = CharSet.getInstance("a-zA-Z0-9");
        //alphaNumericString = alphaNumeric.toString(); 
        
    }
    
    public Date getDateNow() {
        return new Date();
    }
    
    public Date parse(String dateString) throws ParseException {
        // get the thread-local SimpleDateFormat:
        SimpleDateFormat format = (SimpleDateFormat) fullDateFormat.get();
        return format.parse(dateString);
    }
        
    public String dateToString(Date date) {
        if (date == null) return "(no date/time)";
        // get the thread-local SimpleDateFormat:
        SimpleDateFormat format = (SimpleDateFormat) fullDateFormat.get();
        return format.format(date);
    }
    
    public String dateToDateOnlyString(Date date) {
        if (date == null) return "(no date)";
        // get the thread-local SimpleDateFormat:
        SimpleDateFormat format = (SimpleDateFormat) dateOnlyFormat.get();
        return format.format(date);
    }
    
    public DateFormat getDateFormat() {
        // get and return the thread-local SimpleDateFormat:
        return (DateFormat) fullDateFormat.get();
    }
    
    /**
     * Escapes any string for use within a javascript string.
     */
    public String escapeForJavascript(String original) {
        
        return StringEscapeUtils.escapeJavaScript(original);
        //return aposRegEx.matcher(original).replaceAll("\\\\'");
    }
    
    /**
     * Escapes text so it may be safely used inside html.
     */
    public String escapeForHTML(String original) {
    
        return StringEscapeUtils.escapeHtml(original);
    }
    
    
    
    /**
     * Escapes text so it may be safely used inside XML.
     */
    public String escapeForXML(String original) {
        return StringEscapeUtils.escapeXml(original);
    }
    
    
    /**
     * Convenience method to double-escape text for use in XML
     */
    public String doubleEscapeForXML(String original) {
        return escapeForXML(escapeForXML(original));
    }
    
    
     /**
     * The converse of escapeForXML.
     */
    public String unescapeForXML(String original) {
        return StringEscapeUtils.unescapeXml(original);
    }
    
    
    
    /**
     * Deletes any non alphanumeric characters (not a-zA-Z0-9) and returns the remains.
     */
    public String deleteNonAlphaNumeric(String original) {
        
        return CharSetUtils.keep(original, "a-zA-Z0-9");
    }
    
}
