package decimator.layoutFX;


import PamController.SettingsPane;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import pamViewFX.PamControlledGUIFX;

/**
 * FX GUI for the SoundAquisition module. 
 * @author Jamie Macaulay
 *
 */
public class DecimatorUIFX extends PamControlledGUIFX {
	
	/**
	 * The main settings pane for the aquisition control. 
	 */
	private DecimatorSettingsPane decimatorPane;
	
	/**
	 * Reference to the Sound Aquisition control. 
	 */
	private DecimatorControl decimatorControl;

	public DecimatorUIFX(DecimatorControl aquisitionControl) {
		this.decimatorControl=aquisitionControl; 
	}

	@Override
	public SettingsPane<DecimatorParams> getSettingsPane(){
		if (decimatorPane==null){
			decimatorPane=new DecimatorSettingsPane(decimatorControl);
		}
		decimatorPane.setParams(decimatorControl.getDecimatorParams());
		return decimatorPane;
	}
	

	/**
	 * This is called whenever a settings pane is closed. If a pamControlledUnit has
	 * settings pane then this should be used to update settings based on info input
	 * into settings pane.
	 */
	@Override
	public void updateParams() {
		DecimatorParams newParams=decimatorPane.getParams(decimatorControl.getDecimatorParams()); 
		if (newParams!=null) {
			decimatorControl.setDecimatorParams(newParams);
		}
		//setup the controlled unit. 
		decimatorControl.setupControlledUnit(); 
	}
	
	

}