package clickTrainDetector.localisation;

import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.CTDetectionGroupDataUnit;
import clickTrainDetector.ClickTrainControl;

/**
 * Basic localisation info for the CTDataUnit.
 * <p>
 * The click train detector is generic, thus, there may not be any bearing
 * information for localisations and each CTDataUnnit only deals with grouped
 * channels so large aperture localisation is irrelevant.
 * <p>
 * If grouped detections with bearing info then the CTLoclaisation will store
 * standard target motion results and also stores a summary of angles over the
 * duration of the click train, i.e. calculates median angles every unit time or
 * if the angle change is greater than a specified value.
 *
 * @author Jamie Macaulay
 *
 */
public class CTLocalisation extends GroupLocalisation {
	
	/**
	 * Reference to localisation params. 
	 */
	private CTLocParams ctLocParams; 



	public CTLocalisation(CTDetectionGroupDataUnit pamDataUnit, GroupLocResult targetMotionResult, CTLocParams ctLocParams) {
		super(pamDataUnit, targetMotionResult);
		//calcSummaryUnits(pamDataUnit); 
		this.ctLocParams=ctLocParams; 
	}

	/**
	 *
	 * @return the click train control. 
	 */
	private ClickTrainControl getClickTrainControl() {
		return ((ClickTrainDataBlock) this.getParentDetection().getParentDataBlock()).getClickTrainControl(); 
	}
	

}
