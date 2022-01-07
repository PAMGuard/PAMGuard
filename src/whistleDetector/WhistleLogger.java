package whistleDetector;

import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class WhistleLogger extends PamDetectionLogging {
	
	WhistleControl whistleControl;
	
	
	PamTableItem sliceCount, length, minFreq, maxFreq;
	
	public WhistleLogger(WhistleControl whistleControl, PamDataBlock pamDataBlock) {
		
		super(pamDataBlock, SQLLogging.UPDATE_POLICY_WRITENEW);
		
		this.whistleControl = whistleControl;
		
		PamTableDefinition tableDefinition = getTableDefinition();
		tableDefinition.addTableItem(sliceCount = new PamTableItem("SliceCount", Types.INTEGER));
		tableDefinition.addTableItem(length = new PamTableItem("WhistleLength", Types.DOUBLE));
		tableDefinition.addTableItem(minFreq = new PamTableItem("MinFreq", Types.DOUBLE));
		tableDefinition.addTableItem(maxFreq = new PamTableItem("MaxFreq", Types.DOUBLE));
		
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		
		super.setTableData(sqlTypes, pamDataUnit);
		
		ShapeDataUnit shapeDataUnit = (ShapeDataUnit) pamDataUnit;
		WhistleShape whistle =shapeDataUnit.getWhistleShape();
		
		WhistleDetector whistleDetector = whistleControl.whistleDetector;

//		double secsPerSlice = 
//			whistleControl.whistleDetector.fftHop / whistleControl.whistleDetector.getSampleRate();
		
		sliceCount.setValue(whistle.getSliceCount());
		length.setValue(whistleDetector.binsToSeconds(
				whistle.getLastPeak().getSliceNo() - whistle.GetPeak(0).getSliceNo() + 1));
		minFreq.setValue(whistleDetector.binsToHz(whistle.getMinFreq()));
		maxFreq.setValue(whistleDetector.binsToHz(whistle.getMaxFreq()));
		
	}

}
