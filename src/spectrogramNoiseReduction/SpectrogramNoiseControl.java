package spectrogramNoiseReduction;

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


public class SpectrogramNoiseControl extends PamControlledUnit implements PamSettings {

//	private ArrayList<SpecNoiseMethod> methods = new ArrayList<SpecNoiseMethod>();
//	
//	private SpectrogramNoiseSettings noiseSettings = new SpectrogramNoiseSettings();
	
	protected SpectrogramNoiseProcess spectrogramNoiseProcess;
	
	public SpectrogramNoiseControl(String unitName) {
		super("Spectrogram Noise Reduction", unitName);
		
		spectrogramNoiseProcess = new SpectrogramNoiseProcess(this);
		addPamProcess(spectrogramNoiseProcess);
		
		PamSettingManager.getInstance().registerSettings(this);

		spectrogramNoiseProcess.setNoiseSettings(spectrogramNoiseProcess.getNoiseSettings());
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			spectrogramNoiseProcess.setupProcess();
		}
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName());
		menuItem.addActionListener(new DetectionSettings(parentFrame));
		return menuItem;
	}
	
	class DetectionSettings implements ActionListener {

		private Frame parentFrame;
		
		public DetectionSettings(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsDialog(parentFrame);	
		}
		
	}
	
	private void settingsDialog(Frame parentFrame) {
		SpectrogramNoiseSettings newSettings = SpectrogramNoiseDialog.showDialog(parentFrame, 
				spectrogramNoiseProcess, spectrogramNoiseProcess.getNoiseSettings());
		if (newSettings != null) {
			spectrogramNoiseProcess.setNoiseSettings(newSettings.clone());
			spectrogramNoiseProcess.setupProcess();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return spectrogramNoiseProcess.getNoiseSettings();
	}

	@Override
	public long getSettingsVersion() {
		return SpectrogramNoiseSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {

		SpectrogramNoiseSettings noiseSettings = ((SpectrogramNoiseSettings) (pamControlledUnitSettings.getSettings())).clone();

		spectrogramNoiseProcess.setNoiseSettings(noiseSettings);
		return false;
	}
//
//	public ArrayList<SpecNoiseMethod> getMethods() {
//		return methods;
//	}
//
//	public SpectrogramNoiseSettings getNoiseSettings() {
//		return noiseSettings;
//	}
}
