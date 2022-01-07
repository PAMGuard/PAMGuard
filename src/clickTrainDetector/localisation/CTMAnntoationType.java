package clickTrainDetector.localisation;

import annotation.localise.targetmotion.TMAnnotationType;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.layout.localisation.CTMSettingsPanel;

/**
 * Sub class of TM annotation type which has some extra functionality for 
 * filtering click trains before target motion localisation.
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTMAnntoationType extends TMAnnotationType {
	
	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	public CTMAnntoationType(ClickTrainControl clickTrainControl) {
		super();
		this.clickTrainControl = clickTrainControl; 
	}
	
	@Override
	public CTMSettingsPanel getSettingsPanel() {
		if (clickLocDialogPanel == null) {
			clickLocDialogPanel = new CTMSettingsPanel(this, tmGroupLocaliser);
		}
		return (CTMSettingsPanel) clickLocDialogPanel;
	}
	
	

}
