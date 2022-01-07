package clickTrainDetector.classification.templateClassifier;

import PamguardMVC.PamDataUnit;
import matchedTemplateClassifer.MatchTemplate;

/**
 * A data unit wrapper for a spectrum template. This is a PAM data unit to make it compatible with some displays. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SpectrumTemplateDataUnit extends PamDataUnit {

	public MatchTemplate spectrumTemplate; 

	public SpectrumTemplateDataUnit(MatchTemplate spectrumTemplate) {
		super(0L);
		this.spectrumTemplate=spectrumTemplate;
	
	}

}
