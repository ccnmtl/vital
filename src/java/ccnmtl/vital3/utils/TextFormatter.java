package ccnmtl.vital3.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * This interface is designed to provide formatting and parsing services for Dates.
 */
public interface TextFormatter {
    
    /**
     * Gets a Date object representing the current date and time
     */
    public Date getDateNow();
    
    /**
     * Parses a String and returns the Date which it is meant to represent.
     * The string must adhere to the "fullDate" format defined by the implementation.
     */
    public Date parse(String dateString) throws ParseException;
    
    /**
     * Parses a String and returns the Date which it is meant to represent.
     * The string must adhere to the "dateOnly" format defined by the implementation.
     */
    //public Date parseDateOnly(String dateString) throws ParseException;
    
    /**
     * Parses a String and returns the Date which it is meant to represent.
     * The string must adhere to the "timeOnly" format defined by the implementation.
     */
    //public Date parseTimeOnly(String dateString) throws ParseException;
    
    /**
     * Formats and returns a given Date according to the "fullDate" format
     * defined by the implementation.
     */
    public String dateToString(Date date);
    
    /**
     * Formats and returns a given Date according to the "dateOnly" format
     * defined by the implementation.
     */
    public String dateToDateOnlyString(Date date);
    
    /**
     * Formats and returns a given Date according to the "timeOnly" format
     * defined by the implementation.
     */
    //public String dateToTimeOnlyString(Date date);
    
    /**
     * Get the underlying "fullDate" DateFormat instance used by this TextFormatter
     */
    public DateFormat getDateFormat();
    
    /**
     * Escapes any string for use within a javascript string.
     */
    public String escapeForJavascript(String original);
    
    /**
     * Escapes text so it may be safely used inside html.
     */
    public String escapeForHTML(String original);
    
    /**
     * Deletes any non alphanumeric characters (not a-zA-Z0-9) and returns the remains.
     */
    public String deleteNonAlphaNumeric(String original);
}