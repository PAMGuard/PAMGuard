package warnings;

/**
 * Used when something might repeat a lot. Will dump messages to the terminal 
 * once or twice, but otherwise will just use the title bar warning system. 
 * @author dg50
 *
 */
public class RepeatWarning {

	private String warningSource;
	private int maxPrints = 10;
	private int maxStackTraces = 1;
	private int nPrints;
	private int nStackTraces;
	private PamWarning pamWarning;
	private String warningTip;
	
	/**
	 * Constructor 
	 * @param warningSource text for warning source
	 * @param maxPrints max number of times message will be printed to the terminal
	 * @param maxStackTraces max number of stack trace dumps when the warning is an exception
	 */
	public RepeatWarning(String warningSource, int maxPrints, int maxStackTraces) {
		super();
		this.warningSource = warningSource;
		this.maxPrints = maxPrints;
		this.maxStackTraces = maxStackTraces;
		pamWarning = new PamWarning(warningSource, "", 0);
	}

	/**
	 * 
	 * @param warningSource text for warning source
	 */
	public RepeatWarning(String warningSource) {
		super();
		this.warningSource = warningSource;
	}
	
	/**
	 * Set tool tip text for the warning
	 * @param tip tip text
	 */
	public void setToolTip(String tip) {
		this.warningTip = tip;
		if (pamWarning != null) {
			pamWarning.setWarningTip(warningTip);
		}
	}
	
	/**
	 * Show a warning text. 
	 * @param warningMsg message text
	 * @param warningLevel warning level, 0, 1 or 2
	 */
	public synchronized void showWarning(String warningMsg, int warningLevel) {
		if (pamWarning == null) {
			pamWarning = new PamWarning(warningSource, warningMsg, warningLevel);
			WarningSystem.getWarningSystem().addWarning(pamWarning);
			if (warningTip != null) {
				pamWarning.setWarningTip(warningTip);
			}
		}
		else {
			pamWarning.setWarningMessage(warningMsg);
			pamWarning.setWarnignLevel(warningLevel);
			WarningSystem.getWarningSystem().updateWarning(pamWarning);
		}
		if (nPrints++ <= maxPrints) {
			System.out.println(warningMsg);
		}
	}

	/**
	 * show a warning based on an exception
	 * @param e Exception. 
	 * @param warningLevel warning level, 0, 1 or 2
	 */
	public synchronized void showWarning(Exception e, int warningLevel) {
		showWarning(e.getMessage(), warningLevel);
		if (nStackTraces++ <= maxStackTraces) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Clear warning from the displayed list. 
	 */
	public synchronized void clearWarning() {
		if (pamWarning != null) {
			WarningSystem.getWarningSystem().removeWarning(pamWarning);
		}
	}

	/**
	 * @return the maxPrints
	 */
	public int getMaxPrints() {
		return maxPrints;
	}

	/**
	 * @param maxPrints the maxPrints to set
	 */
	public void setMaxPrints(int maxPrints) {
		this.maxPrints = maxPrints;
	}

	/**
	 * @return the maxStackTraces
	 */
	public int getMaxStackTraces() {
		return maxStackTraces;
	}

	/**
	 * @param maxStackTraces the maxStackTraces to set
	 */
	public void setMaxStackTraces(int maxStackTraces) {
		this.maxStackTraces = maxStackTraces;
	}

	/**
	 * @return the nPrints
	 */
	public int getnPrints() {
		return nPrints;
	}

	/**
	 * @param nPrints the nPrints to set
	 */
	public void setnPrints(int nPrints) {
		this.nPrints = nPrints;
	}

	/**
	 * @return the nStackTraces
	 */
	public int getnStackTraces() {
		return nStackTraces;
	}

	/**
	 * @param nStackTraces the nStackTraces to set
	 */
	public void setnStackTraces(int nStackTraces) {
		this.nStackTraces = nStackTraces;
	}
	
	
	
}
