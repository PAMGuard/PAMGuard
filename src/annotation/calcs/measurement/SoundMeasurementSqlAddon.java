package annotation.calcs.measurement;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

/**
 * Database column for one sound measurement.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementSqlAddon implements SQLLoggingAddon {

	private SoundMeasurementAnnotationType annotationType;

	private PamTableItem tableItem;

	public SoundMeasurementSqlAddon(SoundMeasurementAnnotationType annotationType) {
		this.annotationType = annotationType;
		SoundMeasurement m = annotationType.getMeasurement();
		String description = m.getDescription();
		if (!m.getUnit().isEmpty()) {
			description += " (" + m.getUnit() + ")";
		}
		tableItem = new PamTableItem(m.getColumnName(), Types.DOUBLE, description);
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(tableItem);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		SoundMeasurementAnnotation an = annotationType.findAnnotation(pamDataUnit);
		double val = an == null ? Double.NaN : an.getValue();
		tableItem.setValue(Double.isNaN(val) || Double.isInfinite(val) ? null : val);
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		double val = tableItem.getDoubleValue();
		if (!Double.isNaN(val)) {
			SoundMeasurementAnnotation an = new SoundMeasurementAnnotation(annotationType);
			an.setValue(val);
			pamDataUnit.addDataAnnotation(an);
		}
		return true;
	}

	@Override
	public String getName() {
		return annotationType.getAnnotationName();
	}

}
