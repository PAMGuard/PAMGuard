package IshmaelDetector.layoutFX;

import IshmaelDetector.EnergySumControl;
import IshmaelDetector.IshDetControl;
import IshmaelDetector.IshDetParams;
import PamController.SettingsPane;

/**
 * Energy sum pane. 
 * @author Jamie Macaulay
 *
 */
public class IshEnergyPaneFX extends IshPaneFX {
	
	/**
	 * The energy sum pane. 
	 */
	private EnergySumPane energySumPane; 

	/**
	 * The energy pane. 
	 * @param ishDetControl -reference to the energy sum control. 
	 */
	public IshEnergyPaneFX(IshDetControl ishDetControl) {
		super(ishDetControl);
	}

	@Override
	public SettingsPane<IshDetParams> getDetectorPane() {
		if (energySumPane==null) {
			energySumPane= new EnergySumPane((EnergySumControl) super.ishDetControl); 
		}
		return energySumPane;
	}

}
