package PamguardMVC;

/**
 * Returned by datablocks, though default is null, to give information on how
 * automatic the process was. 
 * @author dg50
 *
 */
public class DataAutomationInfo {

	
	private DataAutomation automation;

	/**
	 * @param automation
	 */
	public DataAutomationInfo(DataAutomation automation) {
		this.setAutomation(automation);
	}

	/**
	 * @return the automation
	 */
	public DataAutomation getAutomation() {
		return automation;
	}

	/**
	 * @param automation the automation to set
	 */
	public void setAutomation(DataAutomation automation) {
		this.automation = automation;
	}

	@Override
	public String toString() {
		if (automation == null) {
			return "Unknown data automation";
		}
		return automation.toString();
	}
	
	
}
