package amplifier;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class AmpControl extends PamControlledUnit implements PamSettings {

	AmpProcess ampProcess;
	
	AmpParameters ampParameters = new AmpParameters();

	private boolean initialisationComplete;
	
	public AmpControl(String unitName) {
		super("Signal Amplifier", unitName);
		
		addPamProcess(ampProcess = new AmpProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return ampParameters;
	}

	@Override
	public long getSettingsVersion() {
		return AmpParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		ampParameters = ((AmpParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem playbackMenu;
		playbackMenu = new JMenuItem(getUnitName() + " ...");
		playbackMenu.addActionListener(new AmpSettings(this, parentFrame));
		return playbackMenu;
	}
	
	class AmpSettings implements ActionListener {

		Frame parentFrame;
		
		AmpControl ampControl;
		
		public AmpSettings(AmpControl ampControl, Frame parentFrame) {
			this.ampControl = ampControl;
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			AmpParameters newParams = AmpDialog.showDialog(parentFrame, ampParameters, ampControl);
			if (newParams != null) {
				newSettings(newParams);
			}
			
		}
		
	}
	
	private void newSettings(AmpParameters ampParameters) {
		this.ampParameters = ampParameters.clone();
		ampProcess.noteNewSettings();
	}
	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			this.initialisationComplete = true;
			newSettings(ampParameters);
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			if (initialisationComplete) {
				newSettings(ampParameters);
			}
			break;
		}
	}
	
}
