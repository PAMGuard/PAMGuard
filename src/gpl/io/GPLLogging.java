package gpl.io;

import java.sql.Types;

import PamDetection.AcousticSQLLogging;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import gpl.GPLControlledUnit;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import gpl.contour.GPLContour;

public class GPLLogging extends AcousticSQLLogging {

	private GPLControlledUnit gplControlledUnit;
	private PamTableItem peakValue, contourArea;

	public GPLLogging(GPLControlledUnit gplControlledUnit, GPLDetectionBlock gplDetectionBlock) {
		super(gplDetectionBlock, gplControlledUnit.getUnitName() + " Detections");
		this.gplControlledUnit = gplControlledUnit;
		
		PamTableDefinition pamTable = getTableDefinition();
		pamTable.addTableItem(peakValue = new PamTableItem("PeakValue", Types.REAL));
		pamTable.addTableItem(contourArea = new PamTableItem("ContourArea", Types.REAL));
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap, long duration,
			double[] f) {
		long samples = (long) (getPamDataBlock().getSampleRate()*duration/1000);
		GPLDetection gplDetection = new GPLDetection(timeMilliseconds, chanMap, 0, samples, 0, 0, null);
		gplDetection.setDurationInMilliseconds(duration);
		gplDetection.setPeakValue(peakValue.getFloatValue());
		gplDetection.setFrequency(f);
		return gplDetection;
	}

	/* (non-Javadoc)
	 * @see PamDetection.AcousticSQLLogging#setTableData(generalDatabase.SQLTypes, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);
		GPLDetection gplDetection = (GPLDetection) pamDataUnit;
		peakValue.setValue((float) gplDetection.getPeakValue()); 
		GPLContour contour = gplDetection.getContour();
		if (contour == null) {
			contourArea.setValue(0.F);
		}
		else {
			contourArea.setValue((float) contour.getArea());
		}
	}

}
