package fftManager.layoutFX;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.SettingsPane;
import fftManager.FFTParameters;
import fftManager.PamFFTControl;
import pamViewFX.PamControlledGUIFX;

/**
 * The FX GUI for the FFT module. 
 * @author Jamie Macaulay 
 *
 */
public class FFTGuiFX extends PamControlledGUIFX {

	/**
	 * Reference to FFT control. 
	 */
	private PamFFTControl fftControl;

	/**
	 * The FFT Pane.
	 */
	private FFTPaneFX fftPane;


	public FFTGuiFX(PamFFTControl fftControl) {
		this.fftControl=fftControl; 
	}

	public SettingsPane<FFTParameters> getSettingsPane(){
		if (fftPane==null) fftPane= new FFTPaneFX(fftControl); 
		fftPane.setParams(fftControl.getFftParameters());
		return fftPane;
	}

	@Override
	public void updateParams() {
		//System.out.println("FFT Params input: " + fftControl.getFftParameters());
		FFTParameters newParams=fftPane.getParams(fftControl.getFftParameters()); 
		//System.out.println("NEW PARAMS: " + newParams);
		//System.out.println("FFT Length: " + newParams.fftLength);
		if (newParams!=null) fftControl.setFFTParameters(newParams);
		//setup the controlled unit. 
		fftControl.setupControlledUnit(); 
	}

	@Override
	public void notifyGUIChange(int changeType) {
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			break;
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//data source may have potentially changed. e.g. by a datamodelfx Need to set in params.
			//System.out.println("FFTControl: CHANGED_PROCESS_SETTINGS : " +fftProcess.getParentDataBlock());
					if (fftControl.getFFTProcess().getParentDataBlock()!=null){
						fftControl.getFftParameters().dataSourceName=fftControl.getFFTProcess().getParentDataBlock().getDataName();
					}
					else fftControl.getFftParameters().dataSourceName="";
			break;
		}
	}
}
