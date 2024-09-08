package ltsa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import fftManager.FFTPluginPanelProvider;

public class LtsaControl extends PamControlledUnit implements PamSettings {

	LtsaProcess ltsaProcess;
	
	LtsaParameters ltsaParameters = new LtsaParameters();
	
	public LtsaControl(String unitName) {
		super("LTSA", unitName);
		ltsaProcess = new LtsaProcess(this);
		addPamProcess(ltsaProcess);
		new FFTPluginPanelProvider(ltsaProcess.getLtsaDataBlock());
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new DetectionMenu(parentFrame));
		return menuItem;
	}
	
	class DetectionMenu implements ActionListener {

		private Frame parentFrame;
		
		/**
		 * @param parentFrame
		 */
		public DetectionMenu(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			detectionMenu(parentFrame);
		}
		
	}

	public void detectionMenu(Frame parentFrame) {
		LtsaParameters newParams = LtsaDialog.showDialog(parentFrame, this, ltsaParameters);
		if (newParams != null) {
			ltsaParameters = newParams.clone();
			ltsaProcess.setupProcess();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return ltsaParameters;
	}

	@Override
	public long getSettingsVersion() {
		return LtsaParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		ltsaParameters = ((LtsaParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	public LtsaParameters getLtsaParameters() {
		return ltsaParameters;
	}

}
