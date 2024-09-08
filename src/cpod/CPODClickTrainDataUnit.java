package cpod;

import java.util.List;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;
import cpod.CPODClassification.CPODSpeciesType;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Base class for a click train data unit. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODClickTrainDataUnit extends DetectionGroupDataUnit implements PamDetection {
	
	CPODClassification cpodClassification;


	public CPODClickTrainDataUnit(long timeMilliseconds, List<PamDataUnit> list, CPODClassification cpodClassification) {
		super(timeMilliseconds, null);
		this.cpodClassification=cpodClassification;
	}

	public CPODSpeciesType getSpecies() {
		return cpodClassification.species;
	}

	public int getConfidence() {
		return cpodClassification.qualitylevel;
	}
	

	public boolean isEcho() {
		return cpodClassification.isEcho;
	}

	/**
	 * Gte information on the click train
	 * @return
	 */
	public String getStringInfo() {
		return String.format("Species: %s Confidence %d is echo? %b", getSpecies(), getConfidence(), isEcho());
	}
	
}