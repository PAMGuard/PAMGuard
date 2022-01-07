package clickTrainDetector.classification.templateClassifier;

import PamModel.parametermanager.ManagedParameters;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates.SpectrumTemplateType;
import matchedTemplateClassifer.MatchTemplate;

public class TemplateCorrParams extends CTClassifierParams implements ManagedParameters {
	
	/**
	 * Match FFT template. 
	 */
	public MatchTemplate spectrumTemplate = DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.BEAKED_WHALE);

	/**
	 * The correlation threshold. 
	 */
	public Double corrThreshold = 0.5; 

}
