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
public class IshSpecCorrelationPaneFX extends IshPaneFX {

	private  SpecCorrelationPane specCorrelationPane;

	private SgramCorrControl specIshDetControl; 

	public IshSpecCorrelationPaneFX(SgramCorrControl specIshDetControl) {
		super(specIshDetControl);
		this.specIshDetControl=specIshDetControl; 
		// TODO Auto-generated constructor stub
	}

	@Override
	public SettingsPane<IshDetParams> getDetectorPane() {
		if (specCorrelationPane==null) {
			specCorrelationPane= new SpecCorrelationPane(specIshDetControl); 
		}
		return specCorrelationPane;
	}



}
