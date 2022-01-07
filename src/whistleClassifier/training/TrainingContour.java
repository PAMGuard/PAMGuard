package whistleClassifier.training;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import whistleClassifier.WhistleContour;

public class TrainingContour implements Serializable, WhistleContour, ManagedParameters {
	
	static public final long serialVersionUID = 0;

	private double[] timeSeconds;
	
	private double[] frequencyHz;

	public TrainingContour(double[] timeSeconds, double[] frequencyHz) {
		super();
		this.timeSeconds = timeSeconds;
		this.frequencyHz = frequencyHz;
	}

	public double[] getTimesInSeconds() {
		return timeSeconds;
	}

	public void setTimesInSeconds(double[] timeSeconds) {
		this.timeSeconds = timeSeconds;
	}

	public double[] getFreqsHz() {
		return frequencyHz;
	}

	public void setFreqsHz(double[] frequencyHz) {
		this.frequencyHz = frequencyHz;
	}
	
	/**
	 * Get the length of the contour in time bins.
	 * @return
	 */
	public int getLength() {
		if (timeSeconds == null) {
			return 0;
		}
		return timeSeconds.length;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("timeSeconds");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return timeSeconds;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("frequencyHz");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return frequencyHz;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
