package IshmaelDetector;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

//import pamDatabase.SQLLogging;
//import PamguardMVC.RecyclingDataBlock;
import generalDatabase.PamDetectionLogging;

public class IshLogger extends PamDetectionLogging {
	IshDetControl ishDetControl;
	PamTableDefinition tableDefinition;
	PamTableItem systemDate, durationSecs, secSinceStart, peakHeight;
	// Peak is more important than start time for matched filter & spectrogram correlation
	PamTableItem peakSample, peakDelaySecs; 
	
	public IshLogger(IshDetControl ishDetControl, PamDataBlock pamDataBlock) 
	{
		super(pamDataBlock, UPDATE_POLICY_WRITENEW);
		this.ishDetControl = ishDetControl;
		
		tableDefinition = getTableDefinition();
		
//		PamTableItem tableItem;
//		setUpdatePolicy(UPDATE_POLICY_WRITENEW);
//		tableDefinition = new PamTableDefinition(ishDetControl.getUnitName(), getUpdatePolicy());
////		tableDefinition.addTableItem(tableItem = new PamTableItem("GpsIndex", Types.INTEGER));
////		tableItem.setCrossReferenceItem("GpsData", "Id");
//		tableDefinition.addTableItem(systemDate    = new PamTableItem("SystemDate",    Types.TIMESTAMP));
		tableDefinition.addTableItem(peakSample = new PamTableItem("Peak Sample", Types.INTEGER));
		tableDefinition.addTableItem(peakHeight = new PamTableItem("PeakHeight", Types.DOUBLE));
		tableDefinition.addTableItem(peakDelaySecs = new PamTableItem("PeakDelaySeconds", Types.DOUBLE));
		tableDefinition.addTableItem(secSinceStart = new PamTableItem("SecSinceStart", Types.DOUBLE));
		tableDefinition.addTableItem(durationSecs      = new PamTableItem("DurationSeconds",      Types.DOUBLE));
//		setTableDefinition(tableDefinition);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);

		IshDetection detUnit = (IshDetection)pamDataUnit;

		long dur = detUnit.getSampleDuration();					//in det samples (e.g., slices)
		float dRate = ishDetControl.ishDetFnProcess.getSampleRate();
		peakDelaySecs.setValue(detUnit.getPeakDelaySec());
		peakHeight.setValue(detUnit.getPeakHeight());
		peakSample.setValue(detUnit.getPeakTimeSam());
		durationSecs.setValue((double)dur / dRate);
		secSinceStart.setValue((double)detUnit.getStartSample() / ishDetControl.ishDetFnProcess.getSampleRate());

	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		long duration = getDuration().getIntegerValue();
		double durationS = durationSecs.getDoubleValue();
		long endMillis = timeMilliseconds + (long) (durationS*1000);
		int chanMap = getChannelMap().getIntegerValue(); 
		long startSam = getStartSample().getIntegerValue();
		long durationSam = getDuration().getIntegerValue();
		double pHeight = peakHeight.getDoubleValue();
		long pTimeSam = peakSample.getIntegerValue();
		IshDetection id = new IshDetection(timeMilliseconds, endMillis, (float)getLowFreq().getDoubleValue(), 
				(float)getHighFreq().getDoubleValue(), pTimeSam, pHeight, getPamDataBlock(), chanMap, startSam, durationSam);
		id.setDatabaseIndex(databaseIndex);
		getPamDataBlock().addPamData(id);
		return id;
	}

}
