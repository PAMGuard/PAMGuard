package annotation.calcs.spl;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;
import noiseOneBand.OneBandDataUnit;

public class SPLSqlAddon implements SQLLoggingAddon {

	private PamTableItem spl, channel, rms, zeroPeak, peakPeak, sel;
	
	private SPLAnnotationType splAnnotationType;
	
	public SPLSqlAddon(SPLAnnotationType splAnnotationType) {
		super();
		this.splAnnotationType = splAnnotationType;
		rms =  new PamTableItem("RMS", Types.DOUBLE, "RMS Level");
		zeroPeak  = new PamTableItem("ZeroPeak", Types.DOUBLE, "Zero to Peak Level");
		peakPeak  = new PamTableItem("PeakPeak", Types.DOUBLE, "Peak to Peak Level");
		sel = new PamTableItem("SEL", Types.DOUBLE, "Sound Exposure Level");
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(rms);
		pamTableDefinition.addTableItem(zeroPeak);
		pamTableDefinition.addTableItem(peakPeak);
		pamTableDefinition.addTableItem(sel);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		SPLAnnotation splAnnotation = (SPLAnnotation) pamDataUnit.findDataAnnotation(SPLAnnotation.class);
		if (splAnnotation == null) {
			rms.setValue(null);
			zeroPeak.setValue(null);
			peakPeak.setValue(null);
			sel.setValue(null);
		}
		else {
			rms.setValue(splAnnotation.getRms());
			zeroPeak.setValue(splAnnotation.getZeroPeak());
			peakPeak.setValue(splAnnotation.getPeakPeak());
			sel.setValue(splAnnotation.getIntegratedSEL());
		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		SPLAnnotation splAnnotation = new SPLAnnotation(splAnnotationType);
		if (splAnnotation != null) {
			splAnnotation.setRms(rms.getDoubleValue());
			splAnnotation.setZeroPeak(zeroPeak.getDoubleValue());
			splAnnotation.setPeakPeak(peakPeak.getDoubleValue());
			splAnnotation.setSEL(sel.getDoubleValue());
			pamDataUnit.addDataAnnotation(splAnnotation);
		}
		return true;
	}

	@Override
	public String getName() {
		return splAnnotationType.getAnnotationName();
	}

}
