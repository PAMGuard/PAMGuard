package nidaqdev.networkdaq;

/**
 * A List of strings returned by the cRio which indicate an error of some sort. 
 * @author Doug Gillespie
 *
 */
public enum CRioErrorStrings {
	
	TCP__BIND_ERROR ("Error binding TCP socket on port", 2),
	QUEUE_DUMP_ERROR ("chunks from data queue", 1),
	DOUBLE_FREE ("double free or corruption", 2),
	C_ERROR("*** Error in `./cRioTestC'", 2);
	
    private final String name;
	private final int severity;       
	private int errorCount;

    private CRioErrorStrings(String name, int severity) {
        this.name = name;
        this.severity = severity;
        errorCount = 0;
    }
    
    public boolean contains(String cRioString) {
    	return cRioString == null ? false : cRioString.contains(name);
    }
    
    public static void clearErrors(){
    	CRioErrorStrings[] vals = CRioErrorStrings.values();
    	for (int i = 0; i < vals.length; i++) {
    		vals[i].errorCount = 0;
    	}
    }
    
    public static CRioErrorStrings getEnum(String cRioString) {
    	CRioErrorStrings[] vals = CRioErrorStrings.values();
    	for (int i = 0; i < vals.length; i++) {
    		if (vals[i].contains(cRioString)) {
    			return vals[i];
    		}
    	}
    	return null;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the severity
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * @return the errorCount
	 */
	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * @param errorCount Increment the error count by 1. 
	 */
	public void addErrorCount() {
		this.errorCount++;
	}
}
