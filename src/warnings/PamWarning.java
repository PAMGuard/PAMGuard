package warnings;

/**
 * A warning message to put into the warning system displayed in the title area
 * @author dg50
 *
 */
public class PamWarning {
	
	private long endOfLife = Long.MAX_VALUE;
	
	private String warningSource;
	
	private String warningMessage;
	
	private String warningTip;
	
	private int warnignLevel;

	private boolean requestRestart;

	/**
	 * Construct a Pamguard Warning
	 * @param warningSource source of the warning (generally a module name)
	 * @param warningMessage Warning message text
	 * @param warnignLevel warning level (1 or 2)
	 */
	public PamWarning(String warningSource, String warningMessage,
			int warnignLevel) {
		super();
		this.warningSource = warningSource;
		this.warningMessage = warningMessage;
		this.warnignLevel = warnignLevel;
	}

	/**
	 * @return the endOfLife
	 */
	public long getEndOfLife() {
		return endOfLife;
	}

	/**
	 * Set an end of life for a warning. By default warnings have no end of life. If 
	 * you want a warning to only appear for a few seconds, set this to 
	 * PamCalender.getTimeMillis() + nSecs*1000 where nSecs is the number of 
	 * seconds you want the warning to apear for.  
	 * @param endOfLife the endOfLife to set
	 */
	public void setEndOfLife(long endOfLife) {
		this.endOfLife = endOfLife;
	}

	/**
	 * @return the warningSource
	 */
	public String getWarningSource() {
		return warningSource;
	}

	/**
	 * @param warningSource the warningSource to set
	 */
	public void setWarningSource(String warningSource) {
		this.warningSource = warningSource;
	}

	/**
	 * @return the warningMessage
	 */
	public String getWarningMessage() {
		return warningMessage;
	}

	/**
	 * @param warningMessage the warningMessage to set
	 */
	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	/**
	 * @return the warnignLevel
	 */
	public int getWarnignLevel() {
		return warnignLevel;
	}

	/**
	 * @param warnignLevel the warnignLevel to set
	 */
	public void setWarnignLevel(int warnignLevel) {
		this.warnignLevel = warnignLevel;
	}

	/**
	 * Get a tip which some displays may show as a tooltip text when the mouse
	 * moves over the display. 
	 * @return tip text. 
	 */
	public String getWarningTip() {
		return warningTip;
	}

	/**
	 * Set a tip which some displays may show as a tooltip text when the mouse
	 * moves over the display. 
	 * @param warningTip tip text. 
	 */
	public void setWarningTip(String warningTip) {
		this.warningTip = warningTip;
	}

	/**
	 * Set a flag to say that the warning message is requesting that 
	 * PAMGuard be restarted. By restart, this means stopping and starting
	 * the modules NOT exiting the entire program !
	 * @param requestRestart
	 */
	public void setRequestRestart(boolean requestRestart) {
		this.requestRestart = requestRestart;
	}

	/**
	 * @return the requestRestart
	 */
	public boolean isRequestRestart() {
		return requestRestart;
	} 

	

}
