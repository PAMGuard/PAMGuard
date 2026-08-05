/**
 * 
 */
package IshmaelDetector;

/**
 * @author Hisham Qayum and Dave Mellinger
 */


import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import IshmaelDetector.layoutFX.IshSpecCorrPaneFX;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public class SgramCorrControl extends IshDetControl implements PamSettings 
{
	
	/**
	 * JavaFX based seettings dialog (Should be in a swing GUI class but here for now)
	 */
	private PamDialogFX2AWT settingsDialog;
	
	/**
	 * Settings pane for the Ishamel Correlation (should be in a Swing GUI class but here for now). 
	 */
	private IshSpecCorrPaneFX settingsPane;

	public SgramCorrControl(String unitName) {
		super("Spectrogram Correlation Detector", unitName, new SgramCorrParams());
	}
	
	@Override
	public PamDataBlock getDefaultInputDataBlock() {
		return PamController.getInstance().getFFTDataBlock(0);
	}

	@Override
	public IshDetFnProcess getNewDetProcess(PamDataBlock defaultDataBlock) {
		return new SgramCorrProcess(this, defaultDataBlock);
	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamControlledUnit#SetupControlledUnit()
	 */
	//@Override
	//public void setupControlledUnit() {
	//	super.setupControlledUnit();
	//	//have it find its own data block - for now, just take the 
	//	//first fft block that can be found.
	//}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return super.createDetectionMenu(parentFrame, getUnitName() + " Settings...");
	}
	
	class menuSmoothingDetection implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
//			KernelSmoothingParameters newParams = KernelSmoothingDialog.show(smoothingParameters, smoothingProcess.getOutputDataBlock(0));
//			if (newParams != null) {
//				smoothingParameters = newParams.clone();
////				edgeSettings.prepareProcess();
//				newSettings();
//				PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
//			}
		}
	}

	@Override
	public void showParamsDialog1(Frame parentFrame) {
//		SgramCorrParams newParams =
//			SgramCorrParamsDialog.showDialog2(parentFrame, (SgramCorrParams)ishDetParams);
		
		//the FX GUI is new and shiny but a little complicated to get working with Swing. 
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			IshSpecCorrPaneFX setPane = getSettingsPane();
			settingsDialog = new PamDialogFX2AWT<IshDetParams>(parentFrame, setPane, false);
			settingsDialog.setResizable(false);
		}
//		
		SgramCorrParams newParams = (SgramCorrParams) settingsDialog.showDialog(ishDetParams);
//		
		installNewParams(parentFrame, newParams);
	}
	
	/**
	 * Get the settings pane. 
	 * @return the settings pane. 
	 */
	public IshSpecCorrPaneFX getSettingsPane(){
		if (this.settingsPane==null){
			settingsPane= new IshSpecCorrPaneFX(this); 
		}
		return settingsPane; 
	}
	
//	public long getSettingsVersion() {
//		return KernelSmoothingParameters.serialVersionUID;
//	}

	//This is called after a settings file is read.  Copy the newly read settings
	//to sgramCorrParams.
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		SgramCorrParams newParams = 
			(SgramCorrParams)pamControlledUnitSettings.getSettings();
		ishDetParams = newParams.clone();
		return super.restoreSettings(pamControlledUnitSettings);
	}

	@Override
	public Serializable getSettingsReference() {
		return ishDetParams;
	}
	 
	/**
	 * @return An integer version number for the settings
	 */
	@Override
	public long getSettingsVersion() {
		return SgramCorrParams.serialVersionUID;
	}

	@Override
	public PamRawDataBlock getRawInputDataBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getHelpPoint() {
		return "detectors.ishmael.docs.ishmael_speccorrelation";
	}
}
