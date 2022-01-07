package Acquisition;

import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

import java.sql.Types;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.CTDataUnit;

/**
 * Very simple concrete implementation of SQLLogging to log the starts 
 * and stops of PAMGUARD in the database.
 * @author Doug Gillespie
 *
 */
public class AcquisitionLogging extends SQLLogging {

	AcquisitionControl acquisitionControl;
	
	PamTableDefinition tableDef;
	
	PamTableItem adcClockTime, adcClockMillis, rawADCTime, status, reason, daqSystemType, sampleRate, nChannels, voltsPeak2Peak, 
	gain, duration, clockError, samples, gSamples, GPSPPSTime, serverTime, daqSystemName;
	
	public AcquisitionLogging(PamDataBlock pamDataBlock, AcquisitionControl acquisitionControl) {
		super(pamDataBlock);
		this.acquisitionControl = acquisitionControl;
		
		tableDef = new PamTableDefinition(pamDataBlock.getDataName(), UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(adcClockTime = new PamTableItem("ADC Clock", Types.TIMESTAMP));
		tableDef.addTableItem(adcClockMillis = new PamTableItem("ADC Clock millis", Types.INTEGER));
		tableDef.addTableItem(rawADCTime = new PamTableItem("RAW ADC Clock", Types.TIMESTAMP));
		tableDef.addTableItem(gSamples = new PamTableItem("GigaSamples", Types.INTEGER));
		tableDef.addTableItem(samples = new PamTableItem("Samples", Types.INTEGER));
		tableDef.addTableItem(GPSPPSTime = new PamTableItem("GPSPPSTime", Types.TIMESTAMP));
		tableDef.addTableItem(status = new PamTableItem("Status", Types.CHAR, 20));
		tableDef.addTableItem(reason = new PamTableItem("Reason", Types.CHAR, 50));
		tableDef.addTableItem(daqSystemType = new PamTableItem("SystemType", Types.CHAR, 50));
		tableDef.addTableItem(daqSystemName = new PamTableItem("SystemName", Types.CHAR, 50));
		tableDef.addTableItem(sampleRate = new PamTableItem("sampleRate", Types.INTEGER));
		tableDef.addTableItem(nChannels = new PamTableItem("nChannels", Types.INTEGER));
		tableDef.addTableItem(voltsPeak2Peak = new PamTableItem("voltsPeak2Peak", Types.DOUBLE));
		tableDef.addTableItem(gain = new PamTableItem("gain", Types.REAL));
		tableDef.addTableItem(duration = new PamTableItem("duration", Types.DOUBLE));
		tableDef.addTableItem(clockError = new PamTableItem("clockError", Types.DOUBLE));
		tableDef.addTableItem(serverTime = new PamTableItem("Server Time", Types.TIMESTAMP));
		
		setTableDefinition(tableDef);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		DaqStatusDataUnit ds = (DaqStatusDataUnit) pamDataUnit;
		AcquisitionParameters daqParameters = ds.getDaqParameters();
		adcClockTime.setValue(sqlTypes.getTimeStamp(ds.getAdcMilliseconds()));
		adcClockMillis.setValue((int)(ds.getAdcMilliseconds()%1000));
		rawADCTime.setValue(sqlTypes.getTimeStamp(ds.getRawADCMillis()));
		long s = ds.getSamples();
		gSamples.setValue((long)(s/1000000000));
		samples.setValue((long) (s%1000000000));
		Long gT = ds.getGpsPPSMilliseconds();
		if (gT == null) {
			GPSPPSTime.setValue(null);
		}
		else {
			GPSPPSTime.setValue(sqlTypes.getTimeStamp(gT));
		}
		status.setValue(ds.getStatus());
		reason.setValue(ds.getReason());
		daqSystemType.setValue(ds.getDaqSystemType());
		daqSystemName.setValue(ds.getSystemName());
		sampleRate.setValue(ds.getSampleRate());
		nChannels.setValue(ds.getNChannels());
		voltsPeak2Peak.setValue(ds.getVoltsPeak2Peak());
		gain.setValue((float) daqParameters.getPreamplifier().getGain()); 
		duration.setValue(ds.getSampleDuration());
		clockError.setValue(ds.clockError);
		Long sTime = ds.getServerTime();
		if (sTime == null){
			serverTime.setValue(null);
		}
		else {
			serverTime.setValue(sqlTypes.getTimeStamp(sTime));
		}
	}
	
	@Override
	protected DaqStatusDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		
//	Debug.out.println("DaqStatusDataUnit sql Logging: " + PamCalendar.formatDateTime(timeMilliseconds) + "  ADC: " +adcClockTime.getDeblankedStringValue() + " Samples: " +  samples.getLongValue()); 
	
		long adcClock ; 
		if (adcClockTime.getDeblankedStringValue()!=null) {
			adcClock =  SQLTypes.millisFromTimeStamp(adcClockTime.getValue()); 
		}
		else {
			adcClock =-1; 
		}
		
		Long adcRawClock = SQLTypes.millisFromTimeStamp(rawADCTime.getValue());
		if (adcRawClock == null) {
			adcRawClock = 0L;
		}
		//long adcClockMillisVal = adcClockMillis.getLongValue(); 
		long gpsPPSClock ; 
		if (GPSPPSTime.getDeblankedStringValue()!=null) {
			gpsPPSClock =  SQLTypes.millisFromTimeStamp(GPSPPSTime.getValue()); 
		}
		else {
			gpsPPSClock=1; 
		}

		Long samplesVal = samples.getLongValue(); 
		
		String reasonVal = reason.getDeblankedStringValue(); 
		String statusVal = status.getDeblankedStringValue(); 

		double clockErrVal = clockError.getDoubleValue(); 
		double durationValue = duration.getDoubleValue();
		
		String systemName = daqSystemName.getStringValue();
		
//	 DaqStatusDataUnit(long timeMilliseconds, long adcMilliseconds, long samples, Long gpsPPSMillis, 
//				String status, String reason, 
//				AcquisitionParameters daqParameters, double duration, double clockError)
		
//		Debug.out.println("DaqStatusDataUnit sql Logging: " + samplesVal); 
		
		DaqStatusDataUnit dataUnit = new DaqStatusDataUnit(timeMilliseconds,  adcClock,  adcRawClock, samplesVal,  gpsPPSClock, 
				statusVal,  reasonVal, null,  systemName, durationValue,  clockErrVal) ; 
		
		return dataUnit; 
		
	}
	

}
