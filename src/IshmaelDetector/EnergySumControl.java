/**
 * 
 */
package IshmaelDetector;

/**
 * 
 * @author Dave Mellinger and Hisham Qayum
 */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import IshmaelDetector.layoutFX.IshEnergyPaneFX;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

@SuppressWarnings("rawtypes")
public class EnergySumControl extends IshDetControl implements PamSettings {
	
	/**
	 * Dialog to insert FX pane into Swing dialog. 
	 */
	private PamDialogFX2AWT<IshDetParams> settingsDialog;
	
	/**
	 * FX settings Pane. 
	 */
	private IshEnergyPaneFX settingsPane; 
	
	
	public EnergySumControl(String unitName) {
		super("Energy Sum Detector", unitName, new EnergySumParams());
	}
	
	@Override
	public PamDataBlock getDefaultInputDataBlock() {
		return PamController.getInstance().getFFTDataBlock(0);
	}

	@Override
	public IshDetFnProcess getNewDetProcess(PamDataBlock defaultDataBlock) {
		return new EnergySumProcess(this, defaultDataBlock);
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
	
//		//the swing GUI is old, has no units, no channel grouping and no upodated param controls. 
//		EnergySumParams newParams =
//			EnergySumParamsDialog.showDialog2(parentFrame, (EnergySumParams)ishDetParams);
		
		//the FX GUI is new and shiny but a little complicated to get working with Swing. 
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			IshEnergyPaneFX setPane = getSettingsPane();
			settingsDialog = new PamDialogFX2AWT<IshDetParams>(parentFrame, setPane, false);
			settingsDialog.setResizable(false);
		}
		
		EnergySumParams newParams = (EnergySumParams) settingsDialog.showDialog(ishDetParams);
				
		installNewParams(parentFrame, newParams);
	}
	
	/**
	 * Get the settings pane. 
	 * @return the settings pane. 
	 */
	public IshEnergyPaneFX getSettingsPane(){
		if (this.settingsPane==null){
			settingsPane= new IshEnergyPaneFX(this); 
		}
		return settingsPane; 
	}
	
//	public long getSettingsVersion() {
//		return KernelSmoothingParameters.serialVersionUID;
//	}

	//This is called after a settings file is read.  Copy the newly read settings
	//to energySumParams.
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		EnergySumParams newParams = 
			(EnergySumParams)pamControlledUnitSettings.getSettings();
		//I can't figure out why inputDataSource is not in the newParams
		//returned by getSettings, but sometimes it's not. 
//		if (newParams.inputDataSource == null)
//			newParams.inputDataSource = ishDetParams.inputDataSource;
		ishDetParams = newParams.clone();
		return super.restoreSettings(pamControlledUnitSettings);
	}

	public Serializable getSettingsReference() {
		return ishDetParams;
	}
	 
	/**
	 * @return An integer version number for the settings
	 */
	public long getSettingsVersion() {
		return EnergySumParams.serialVersionUID;
	}

	@Override
	public PamRawDataBlock getRawInputDataBlock() {
		// TODO Auto-generated method stub
		return null;
	}
}
