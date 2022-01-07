package annotation.localise.targetmotion;

import java.io.Serializable;

import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import annotation.handler.AnnotationOptions;
import clickDetector.localisation.ClickLocParams;

/**
 * 
 * Options for target motion analysis. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TMAnnotationOptions extends AnnotationOptions implements Serializable, DetectionGroupOptions, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private ClickLocParams localisationParams = new ClickLocParams();
	
	public TMAnnotationOptions(AnnotationOptions otherOptions) {
		super(otherOptions);
	}

	public TMAnnotationOptions(String annotationName) {
		super(annotationName);
	}

	/**
	 * @return the localisationParams
	 */
	public ClickLocParams getLocalisationParams() {
		return localisationParams;
	}

	/**
	 * @param localisationParams the localisationParams to set
	 */
	public void setLocalisationParams(ClickLocParams localisationParams) {
		this.localisationParams = localisationParams;
	}

	@Override
	public int getMaxLocalisationPoints() {
		return localisationParams.getMaxLocalisationPoints();
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
