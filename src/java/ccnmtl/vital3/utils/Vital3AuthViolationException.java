package ccnmtl.vital3.utils;




public class Vital3AuthViolationException extends Exception {
    
    private boolean userIsLoggedIn;
    
    // constructors:
    public Vital3AuthViolationException(boolean userIsLoggedIn) {
        this.userIsLoggedIn = userIsLoggedIn;
    }
    public Vital3AuthViolationException() {
        this(true);
    }
    
    // methods:
    public boolean userIsLoggedIn() {
        return this.userIsLoggedIn;
    }
    
    public String getMessage() {
        if (userIsLoggedIn) return "You are not authorized to access this area";
        else return "You must be logged in to access this area";
    }
    
}