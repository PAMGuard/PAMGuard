package annotation.calcs.snr;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class SnrSqlAddon implements SQLLoggingAddon {

	private PamTableItem snr;
	
	private SNRAnnotationType snrAnnotationType;
	
	public SnrSqlAddon(SNRAnnotationType snrAnnotationType) {
		super();
		this.snrAnnotationType = snrAnnotationType;
		snr  = new PamTableItem("snr", Types.DOUBLE, "Signal to Noise ratio");
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(snr);
		
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		SNRAnnotation snrAnnotation = (SNRAnnotation) pamDataUnit.findDataAnnotation(SNRAnnotation.class);
		if (snrAnnotation == null) {
			snr.setValue(null);
		}
		else {
			snr.setValue(snrAnnotation.getSnr());
		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		Double snrVal = (Double) snr.getValue();
		if (snrVal != null) {
			SNRAnnotation snrAnnotation = new SNRAnnotation(snrAnnotationType);
			snrAnnotation.setSnr(snrVal);
			pamDataUnit.addDataAnnotation(snrAnnotation);
		}
		return true;
	}

	@Override
	public String getName() {
		return snrAnnotationType.getAnnotationName();
	}

}
