package IshmaelDetector.layoutFX;

import IshmaelDetector.IshDetParams;
import IshmaelDetector.SgramCorrControl;
import PamController.SettingsPane;

/**
 * Spectrogram correlation pane. 
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class IshSpecCorrPaneFX extends IshPaneFX {

	private  SpecCorrPane specCorrelationPane;

	private SgramCorrControl specIshDetControl; 

	public IshSpecCorrPaneFX(SgramCorrControl specIshDetControl) {
		super(specIshDetControl);
		this.specIshDetControl=specIshDetControl; 
		// TODO Auto-generated constructor stub
	}

	@Override
	public SettingsPane<IshDetParams> getDetectorPane() {
		if (specCorrelationPane==null) {
			specCorrelationPane= new SpecCorrPane(specIshDetControl); 
		}
		return specCorrelationPane;
	}



}
