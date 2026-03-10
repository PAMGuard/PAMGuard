package mel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import mel.swing.MelDialog;

public class MelControl extends PamControlledUnit implements PamSettings {
	
	public static String unitType = "Mel Spectrogram";
	
	private MelProcess melProcess;
	
	private MelParameters melParameters = new MelParameters();
	
	public MelControl(String unitName) {
		super(unitType, unitName);
		melProcess = new MelProcess(this);
		addPamProcess(melProcess);
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return melParameters;
	}

	@Override
	public long getSettingsVersion() {
		return MelParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		melParameters = (MelParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(this.getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettings(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showSettings(Frame parentFrame) {
		MelParameters newParams = MelDialog.showDialog(this);
		if (newParams != null) {
			melProcess.prepareProcess();
		}
	}

	/**
	 * @return the melParameters
	 */
	public MelParameters getMelParameters() {
		return melParameters;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			melProcess.prepareProcess();
		}
	}

}
