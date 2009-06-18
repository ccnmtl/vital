package ccnmtl.vital3.commands;

import java.util.Set;

import ccnmtl.vital3.*;
import ccnmtl.vital3.commands.Vital3Command;

public class ImportCommand extends Vital3Command {
    
    private String mode;
    private Long sourceWorksiteId;
    private VitalWorksite sourceWorksite;
    //private Set unitIds; using "ids" now
    private Set units;
    
    // public property:
    public boolean importLooseMaterials = false;

    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode=mode;
    }
    
    public VitalWorksite getSourceWorksite() {
        return (VitalWorksite) mapGet("sourceWorksite"); 
    }
    public void setSourceWorksite(VitalWorksite obj) {
        mapSet("sourceWorksite", obj);
    }
    
    public Long getSourceWorksiteId() {
        return (Long) mapGet("sourceWorksiteId");
    }
    public void setSourceWorksiteId(Long value) {
        mapSet("sourceWorksiteId", value);
    }
    
    public Set getUnits() {
        return units;
    }
    public void setUnits(Set units) {
        this.units=units;
    }
        
}