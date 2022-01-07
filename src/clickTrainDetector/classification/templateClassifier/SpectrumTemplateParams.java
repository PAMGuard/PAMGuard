package clickTrainDetector.classification.templateClassifier;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * 
 * Parameters class for the spectrum template. This is used because a PamDataUnit is not
 * a good idea to clone and serialise. Only used for saving, not used throughout program. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SpectrumTemplateParams implements Serializable, Cloneable, ManagedParameters {
	
	public SpectrumTemplateParams(String name, double[] template, float sR) {
		this.name=name;
		this.template=template;
		this.sampleRate=sR;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	
	/**
	 * The sample rate in Hz
	 */
	public float sampleRate = 1000;
	
	/**
	 * The spectrum template data. 
	 */
	public double [] template = null;

	/**
	 * The name of the template. 
	 */
	public String name = "";
	
	public SpectrumTemplateParams clone() {
		try {
			return (SpectrumTemplateParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		} 
		
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
