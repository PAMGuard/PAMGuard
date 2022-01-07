package warnings;

/**
 * Class to provide a quick single warning that can be easily removed without having
 * to handle all the references in the owning class. 
 * @author dg50
 *
 */
public class QuickWarning {

	private PamWarning singleWarning;
	
	public QuickWarning(String source) {
		singleWarning = new PamWarning(source, "", 0);
	}
	
	/**
	 * Set and show the warning. If level is 0 it will hide, if >0 show.
	 * @param message warning message
	 * @param level warning level
	 */
	public void setWarning(String message, int level) {
		singleWarning.setWarningMessage(message);
		singleWarning.setWarnignLevel(level);
//		if (level > 0) {
//			System.out.println(message);
//		}
		if (level > 0) {
			WarningSystem.getWarningSystem().addWarning(singleWarning);
		}
		else {
			WarningSystem.getWarningSystem().removeWarning(singleWarning);
		}
	}
	
	/**
	 * Clear any warning
	 */
	public void clearWarning() {
		WarningSystem.getWarningSystem().removeWarning(singleWarning);
	}
	
}
