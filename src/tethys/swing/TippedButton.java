package tethys.swing;

import javax.swing.JButton;

public class TippedButton extends JButton {

	private static final long serialVersionUID = 1L;
	private String enabledTip;
	private String disabledTip;

	/**
	 * Create a button with standard tips which will be used for enabled state
	 * @param text
	 * @param enabledTip
	 */
	public TippedButton(String text, String enabledTip) {
		this(text, enabledTip, null);
	}
	
	/**
	 * Create a button with standard tips which will be used for enabled and disabled state
	 * @param text
	 * @param enabledTip
	 * @param disabledTip
	 */
	public TippedButton(String text, String enabledTip, String disabledTip) {
		super(text);
		this.enabledTip = enabledTip;
		this.disabledTip = disabledTip;
		setToolTipText(enabledTip);
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		setToolTipText(enable ? enabledTip : disabledTip);
	}
	
	/*
	 * Call to disable the button and at the same time 
	 * set a tooltip giving the reason. 
	 */
	public void disable(String newTip) {
		disabledTip = newTip;
		setEnabled(false);
	}

	/**
	 * @return the enabledTip
	 */
	public String getEnabledTip() {
		return enabledTip;
	}

	/**
	 * @param enabledTip the enabledTip to set
	 */
	public void setEnabledTip(String enabledTip) {
		this.enabledTip = enabledTip;
	}

	/**
	 * @return the disabledTip
	 */
	public String getDisabledTip() {
		return disabledTip;
	}

	/**
	 * @param disabledTip the disabledTip to set
	 */
	public void setDisabledTip(String disabledTip) {
		this.disabledTip = disabledTip;
	}
	

}
