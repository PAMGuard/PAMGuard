package nidaqdev.networkdaq;

/**
 * Log error strings based on information coming back from the cRio through 
 * the SSH window. 
 * @author Doug
 *
 */
public class CRioErrorLog {
			
	public CRioErrorStrings checkString(String string) {
		CRioErrorStrings err = CRioErrorStrings.getEnum(string);
		if (err == null) {
			return null;
		}
		err.addErrorCount();
		return err;
	}
	
	public void clearErrorLog() {
		CRioErrorStrings.clearErrors();
	}
	
}
