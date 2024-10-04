package patchPanel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class PatchPanelControl extends PamControlledUnit implements PamSettings {

	protected PatchPanelProcess patchPanelProcess;
	
	protected PatchPanelParameters patchPanelParameters = new PatchPanelParameters();
	
	public PatchPanelControl(String unitName) {
		
		super("Patch Panel", unitName);
		
		addPamProcess(patchPanelProcess = new PatchPanelProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);
		
		patchPanelProcess.noteNewSettings();
	}
	
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem playbackMenu;
		playbackMenu = new JMenuItem(getUnitName() + " ...");
		playbackMenu.addActionListener(new PatchPanelSettings(this, parentFrame));
		return playbackMenu;
	}

	class PatchPanelSettings implements ActionListener {

		Frame parentFrame;
		
		PatchPanelControl patchPanelControl;
		
		public PatchPanelSettings(PatchPanelControl patchPanelControl, Frame parentFrame) {
			this.patchPanelControl = patchPanelControl;
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			PatchPanelParameters newParams = PatchPanelDialog.showDialog(parentFrame, 
					patchPanelParameters, patchPanelControl);
			if (newParams != null) {
				newSettings(newParams);
			}
			
		}
		
	}
	protected void newSettings(PatchPanelParameters patchPanelParameters) {
		this.patchPanelParameters = patchPanelParameters.clone();
		patchPanelProcess.noteNewSettings();
	}

	@Override
	public Serializable getSettingsReference() {
		return patchPanelParameters;
	}

	@Override
	public long getSettingsVersion() {
		return PatchPanelParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		patchPanelParameters = ((PatchPanelParameters) pamControlledUnitSettings.getSettings()).clone();
		
		return true;
	}
}
