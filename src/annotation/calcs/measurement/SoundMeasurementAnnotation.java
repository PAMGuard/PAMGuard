package annotation.calcs.measurement;

import annotation.DataAnnotation;

/**
 * The value of one sound measurement for a data unit.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementAnnotation extends DataAnnotation<SoundMeasurementAnnotationType> {

	private double value = Double.NaN;

	public SoundMeasurementAnnotation(SoundMeasurementAnnotationType annotationType) {
		super(annotationType);
	}

	/**
	 * @return the measured value, or NaN if it couldn't be measured.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the measured value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * @return the measurement.
	 */
	public SoundMeasurement getMeasurement() {
		return getDataAnnotationType().getMeasurement();
	}

	@Override
	public String toString() {
		return getMeasurement().format(value);
	}

}
