package annotation.calcs.measurement;

import annotation.handler.AnnotationOptions;

/**
 * Annotation options for one sound measurement. Each measurement has its own options (so
 * it can be selected on its own) but they all refer to the same spectrogram parameters.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementOptions extends AnnotationOptions {

	private static final long serialVersionUID = 1L;

	private SoundMeasurementParameters parameters;

	public SoundMeasurementOptions(String annotationName, SoundMeasurementParameters parameters) {
		super(annotationName);
		this.parameters = parameters;
	}

	/**
	 * @return the spectrogram parameters shared by all the measurements.
	 */
	public SoundMeasurementParameters getParameters() {
		if (parameters == null) {
			parameters = new SoundMeasurementParameters();
		}
		return parameters;
	}

	/**
	 * @param parameters the spectrogram parameters shared by all the measurements.
	 */
	public void setParameters(SoundMeasurementParameters parameters) {
		this.parameters = parameters;
	}

}
