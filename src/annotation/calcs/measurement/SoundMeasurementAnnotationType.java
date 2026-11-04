package annotation.calcs.measurement;

import java.util.EnumMap;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;
import annotation.AnnotationSettingsPanel;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationOptions;
import generalDatabase.SQLLoggingAddon;

/**
 * Annotation type for one sound measurement ({@link SoundMeasurement}), automatically
 * measured from the sound within the time and frequency limits of a data unit. Each
 * measurement has its own database column. The measurements are made together by a
 * {@link SoundMeasurementGroup}, which also holds their shared spectrogram settings.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementAnnotationType extends DataAnnotationType<SoundMeasurementAnnotation> {

	private SoundMeasurementGroup group;

	private SoundMeasurement measurement;

	private SoundMeasurementOptions options;

	private SoundMeasurementSqlAddon sqlAddon;

	SoundMeasurementAnnotationType(SoundMeasurementGroup group, SoundMeasurement measurement) {
		this.group = group;
		this.measurement = measurement;
		options = new SoundMeasurementOptions(getAnnotationName(), group.getParameters());
		sqlAddon = new SoundMeasurementSqlAddon(this);
	}

	/**
	 * @return the measurement this annotation type makes.
	 */
	public SoundMeasurement getMeasurement() {
		return measurement;
	}

	/**
	 * @return the group of measurements this belongs to.
	 */
	public SoundMeasurementGroup getGroup() {
		return group;
	}

	@Override
	public String getAnnotationName() {
		return measurement.getDisplayName();
	}

	@Override
	public String getAnnotationGroup() {
		return measurement.getCategory().getTitle() + " measurements";
	}

	@Override
	public String getAnnotationDescription() {
		String unit = measurement.getUnit().isEmpty() ? "sample units" : measurement.getUnit();
		return String.format("%s. Units: %s", measurement.getDescription(), unit);
	}

	@Override
	public String getShortIdCode() {
		// names such as "Peak Frequency" and "Peak Power" would clash in the default code.
		return String.format("M%03d", measurement.ordinal());
	}

	@Override
	public Class getAnnotationClass() {
		return SoundMeasurementAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return AcousticDataUnit.class.isAssignableFrom(dataUnitType);
	}

	@Override
	public boolean canAutoAnnotate() {
		return true;
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return sqlAddon;
	}

	/**
	 * Find this measurement's annotation on a data unit. Measurements share an
	 * annotation class so they are found by name.
	 * @param pamDataUnit the data unit.
	 * @return the annotation or null.
	 */
	public SoundMeasurementAnnotation findAnnotation(PamDataUnit pamDataUnit) {
		return (SoundMeasurementAnnotation) pamDataUnit.findDataAnnotation(SoundMeasurementAnnotation.class,
				getAnnotationName());
	}

	@Override
	public SoundMeasurementAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
		if (pamDataUnit == null || !canAnnotate(pamDataUnit.getClass())) {
			return null;
		}
		EnumMap<SoundMeasurement, Double> values = group.measure(pamDataUnit);
		Double val = values == null ? null : values.get(measurement);
		if (val == null) {
			return null;
		}
		SoundMeasurementAnnotation an = findAnnotation(pamDataUnit);
		if (an == null) {
			an = new SoundMeasurementAnnotation(this);
			pamDataUnit.addDataAnnotation(an);
		}
		an.setValue(val);
		return an;
	}

	/**
	 * No dialog panel of its own - read only measurements are shown together, see
	 * {@link SoundMeasurementSummaryPanel}.
	 */
	@Override
	public AnnotationDialogPanel getDialogPanel() {
		return null;
	}

	@Override
	public boolean hasSettingsPanel() {
		return true;
	}

	@Override
	public AnnotationSettingsPanel getSettingsPanel() {
		return new SoundMeasurementSettingsPanel();
	}

	/**
	 * @return this measurement's options.
	 */
	public SoundMeasurementOptions getMeasurementOptions() {
		return options;
	}

	@Override
	public AnnotationOptions getAnnotationOptions() {
		return options;
	}

	@Override
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
		if (annotationOptions instanceof SoundMeasurementOptions) {
			options = (SoundMeasurementOptions) annotationOptions;
			// the spectrogram settings are shared, so apply them to every measurement.
			group.setParameters(options.getParameters());
		}
	}

}
