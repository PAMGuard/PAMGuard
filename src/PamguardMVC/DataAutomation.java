package PamguardMVC;

/**
 * @author dg50
 * Levels of automation for the various datas in PAMGuard. 
 * Should be used within DataAutomationInfo to perhaps combine with other info in the future. 
 *
 */
public enum DataAutomation {

	AUTOMATIC, MANUAL, MANUALANDAUTOMATIC;

	@Override
	public String toString() {
		switch (this) {
		case AUTOMATIC:
			return "Automatic";
		case MANUAL:
			return "Manual";
		case MANUALANDAUTOMATIC:
			return "Manual and automatic";
		default:
			break;
		
		}
		return null;
	}
	
}
