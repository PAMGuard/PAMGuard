package clickTrainDetector.classification.templateClassifier;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;

import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates.SpectrumTemplateType;
import matchedTemplateClassifer.MatchTemplate;

/**
 * Parameters for the template classifier
 * 
 * @author Jamie Macaulay
 *
 */
public class TemplateClassifierParams extends CTClassifierParams implements ManagedParameters  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 10L;
	
	public TemplateClassifierParams(){
		super.type=CTClassifierType.TEMPLATECLASSIFIER;
//		chi2ThresholdParams = new Chi2ThresholdParams();  
//		template = DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.BEAKED_WHALE);
	}
	
	

	/**
	 * Basic chi2 threshold params. 
	 */
	//public Chi2ThresholdParams chi2ThresholdParams = new Chi2ThresholdParams();  
	
	/**
	 * Bearing parameters 
	 */
	//public BearingClassifierParams bearingClassifierParams = new BearingClassifierParams();  
	
	/**
	 * Template correlation paramters. 
	 */

	
	/**
	 * Match FFT template. 
	 */
	public MatchTemplate spectrumTemplate = DefualtSpectrumTemplates.getTemplate(SpectrumTemplateType.BEAKED_WHALE);

	/**
	 * The correlation threshold. 
	 */
	public Double corrThreshold = 0.5; 
	
	@Override
	public TemplateClassifierParams clone() {
		TemplateClassifierParams clonedParams =(TemplateClassifierParams) super.clone();
		clonedParams.spectrumTemplate=spectrumTemplate.clone();
		//clonedParams.chi2ThresholdParams=chi2ThresholdParams.clone();
		return clonedParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
