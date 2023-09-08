package annotation.calcs.snr;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class SNRAnnotationParameters implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * Number of millis to measure over both before and after
	 * the sound of interest. 
	 */
	public int noiseMillis = 1000;
	
	/**
	 * Buffer between both the start and end of the sound being 
	 * measured and the noise measurement period
	 */
	public int bufferMillis = 1000;

	@Override
	protected SNRAnnotationParameters clone() {
		try {
			return (SNRAnnotationParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
