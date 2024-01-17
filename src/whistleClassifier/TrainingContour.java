package whistleClassifier;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class TrainingContour implements Serializable, ManagedParameters {
	
	static public final long serialVersionUID = 0;

	private double[] timeSeconds;
	
	private double[] frequencyHz;

	public TrainingContour(double[] timeSeconds, double[] frequencyHz) {
		super();
		this.timeSeconds = timeSeconds;
		this.frequencyHz = frequencyHz;
	}

	public double[] getTimeSeconds() {
		return timeSeconds;
	}

	public void setTimeSeconds(double[] timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

	public double[] getFrequencyHz() {
		return frequencyHz;
	}

	public void setFrequencyHz(double[] frequencyHz) {
		this.frequencyHz = frequencyHz;
	}
	
	public int getLength() {
		if (timeSeconds == null) {
			return 0;
		}
		return timeSeconds.length;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
