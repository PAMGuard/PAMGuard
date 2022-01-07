package Azigram;

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
import fftManager.FFTDataBlock;
import fftManager.FFTPluginPanelProvider;

/**
 * Module that implements the Azigram algorithm from Thode et al 2019 J. Acoust.
 * Soc. Am. Vol 146(1) pp 95-102 (doi: 10.1121/1.5114810).
 * This module also includes the methods described in that paper for 
 * frequency domain demultiplexing of directional signals from DIFAR sonobuoys. 
 * 
 * This module is just a prototype, and has not been designed for efficiency. 
 * For quick prototyping it has been based on SpectrogramNoise and FFTDataUnit
 * super-classes, and should plot on the User Display Spectrogram (Swing). 
 *  
 * @author brian_mil
 *
 */
public class AzigramControl extends PamControlledUnit implements PamSettings {

	protected AzigramProcess azigramProcess;
	
	protected AzigramParameters azigramParameters = new AzigramParameters();

	private FFTPluginPanelProvider fFTPluginPanelProvider;
	
	public AzigramControl(String unitName) {
		super("Azigram",unitName);

		PamSettingManager.getInstance().registerSettings((PamSettings) this);
		
		FFTDataBlock defaultInputDataBlock = getDefaultInputDataBlock();
		
		azigramProcess = new AzigramProcess(this, defaultInputDataBlock);
		addPamProcess(azigramProcess);
		azigramProcess.setParentDataBlock(defaultInputDataBlock);
		
		fFTPluginPanelProvider = new FFTPluginPanelProvider(azigramProcess.getOutputDataBlock());
		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDisplayMenu(java.awt.Frame)
	 */
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
			displayDialog(parentFrame);	
		}

	}

	private void displayDialog(Frame parentFrame) {
		AzigramParameters newSettings = AzigramDisplayDialog.showDialog(this, parentFrame);
		if (newSettings != null) {
			azigramParameters =  newSettings.clone();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return azigramParameters;
	}

	@Override
	public long getSettingsVersion() {
		return AzigramParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		azigramParameters = ((AzigramParameters) pamControlledUnitSettings.getSettings()).clone();
		return (azigramParameters != null);
	}
	
	public FFTDataBlock getDefaultInputDataBlock() {
		return (FFTDataBlock) PamController.getInstance().getFFTDataBlock(0);
	}
	
	
}
