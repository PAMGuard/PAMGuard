package clickTrainDetector.classification.templateClassifier;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.classification.bearingClassifier.BearingClassifierParams;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;

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
	public Chi2ThresholdParams chi2ThresholdParams = new Chi2ThresholdParams();  
	
	/**
	 * Bearing parameters 
	 */
	//public BearingClassifierParams bearingClassifierParams = new BearingClassifierParams();  
	
	/**
	 * Template correlation paramters. 
	 */


	/**
	 * Use the median ICI measurements
	 */
	public boolean useMedianIDI = true;

	/**
	 * The minimim median ICI. 
	 */
	public Double minMedianIDI = 0.; // seconds

	/**
	 * The maximum median ICI
	 */
	public Double maxMedianIDI = 2.; // seconds

	/**
	 * Use the mean ICI measurements. 
	 */
	public boolean useMeanIDI = false;


	/**
	 * The minimum median ICI. 
	 */
	public Double minMeanIDI = 0.; // seconds

	/**
	 * The maximum median ICI
	 */
	public Double maxMeanIDI = 2.; // seconds


	/**
	 * Use the mean ICI measurements
	 */
	public boolean useStdIDI = false; 

	/**
	 * The minimum standard deviation in ICI. 
	 */
	public Double minStdIDI = 0.; // seconds

	/**
	 * The maximum standard deviation in ICI
	 */
	public Double maxStdIDI = 100.; // seconds
	
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
		clonedParams.chi2ThresholdParams=chi2ThresholdParams.clone();
		return clonedParams;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
