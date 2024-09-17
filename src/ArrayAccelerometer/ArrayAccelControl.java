package ArrayAccelerometer;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSidePanel;
import PamView.dialog.warn.WarnOnce;
import mcc.MccJniInterface;

public class ArrayAccelControl extends PamControlledUnit implements PamSettings {

	protected ArrayAccelParams accelParams = new ArrayAccelParams();
	protected ArrayAccelProcess accelProcess;
	ArrayAccelSidePanel sidePanel;
	
	public ArrayAccelControl(String unitName) {
		super("Array Accelerometer", unitName);
		PamSettingManager.getInstance().registerSettings(this);
		addPamProcess(accelProcess = new ArrayAccelProcess(this));
		sidePanel = new ArrayAccelSidePanel(this);
		showDeprecatedWarning();
	}
	
	private void showDeprecatedWarning() {
		String message = "<html>The Array Accelerometer module has been replaced by a better module - " +
				"The \"Analog Array Sensors\" module. We recommend that you switch to the newer module.";
		WarnOnce.showWarning(getGuiFrame(), getUnitName(), message, WarnOnce.WARNING_MESSAGE);
	}

	private MccJniInterface mccJni = new MccJniInterface();

	@Override
	public Serializable getSettingsReference() {
		return accelParams;
	}

	@Override
	public long getSettingsVersion() {
		return ArrayAccelParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		accelParams = ((ArrayAccelParams) pamControlledUnitSettings.getSettings()).clone();
		return (accelParams != null);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		return menuItem;
	}
	
	private class SettingsMenu implements ActionListener {

		private Frame parentFrame;

		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			settingsDialog(parentFrame);
		}
		
	}

	public boolean settingsDialog(Frame parentFrame) {
		ArrayAccelParams newParams = ArrayAccelDialog.showDialog(parentFrame, this, accelParams);
		if (newParams == null) {
			return false;
		}
		accelParams = newParams;
		accelProcess.setupTimer();
		return true;
	}

	/**
	 * @return the accelParams
	 */
	public ArrayAccelParams getAccelParams() {
		return accelParams;
	}

	/**
	 * @param accelParams the accelParams to set
	 */
	public void setAccelParams(ArrayAccelParams accelParams) {
		this.accelParams = accelParams;
	}

	/**
	 * @return the mccJni
	 */
	public MccJniInterface getMccJni() {
		return mccJni;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getSidePanel()
	 */
	@Override
	public PamSidePanel getSidePanel() {
		return sidePanel;
	}

}
