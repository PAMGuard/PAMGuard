package AIS;

import generalDatabase.DBControlUnit;
import generalDatabase.SQLTypes;

import java.util.ListIterator;

import pamScrollSystem.ViewLoadObserver;
import nmeaEmulator.EmulatedData;
import nmeaEmulator.NMEAEmulator;
import NMEA.AcquireNmeaData;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class AISDataBlock extends PamDataBlock<AISDataUnit> implements NMEAEmulator {
	
	private AISControl aisControl;

	public AISDataBlock(AISControl aisControl, PamProcess parentProcess) {
		super(AISDataUnit.class, "AIS Data", parentProcess, 0);
		this.aisControl = aisControl;
		setNaturalLifetime(600);
	}
	/**
	 * aisDataUnit is always a new unit extracted from a 
	 * group of AIS strings, but with a single lot of data.
	 * Depending on what type of data it was, it will contain
	 * either static or position data. If the transponder
	 * has already sent data, then update it. Otherwise,
	 * create a new unit.
	 * <p>
	 * Whether or not we actually want this data is a complicated question.
	 * <p>If there is no limitation to range, we want it.
	 * <p>If range is limited and the new data is a position report
	 * we want it if it's in range.
	 * <p>If it's static data and does not match an existing set of 
	 * data we don't want it since we don't know if it's in range or not. 
	 * <p>If it's static data and matches existing data, then we want it. 
	 * <p>
	 * If a new unit is created, notifications are sent out automatically.
	 * otherwise, generate one to notify observers.  
	 * @param newAISUnit new AIS data unit
	 * @return true if it's updating an old unit, false if it's a new one. 
	 */
	synchronized public boolean addAISData(AISDataUnit newAISUnit) {
		boolean newUnit = true;
		AISDataUnit aisDataUnit = findAISDataUnit(newAISUnit.mmsiNumber);
		boolean want = wantData(aisDataUnit, newAISUnit);
		if (aisDataUnit != null) {
			aisDataUnit.isInRange = want;
		}
		if (!want) {
			return false;
		}
		if (aisDataUnit != null) {
			aisDataUnit.update(newAISUnit);
			newUnit =  false;
//			long t = PamCalendar.getTimeInMillis();
			long t = newAISUnit.getTimeMilliseconds();
//			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//				t = newAISUnit.getTimeMilliseconds();
//			}
			aisDataUnit.setTimeMilliseconds(t);
			aisDataUnit.updateDataUnit(t);
			if (shouldNotify()) {
				notifyObservers(aisDataUnit);
			}
			updatePamData(aisDataUnit, t); // need this to get it to save. 
		}
		else {
			addPamData(newAISUnit);
		}
		return newUnit;
	}
	
	private boolean wantData(AISDataUnit existingUnit, AISDataUnit newUnit) {
		if (aisControl.aisParameters.limitRange == false) {
			return true;
		}
		double range = 0;
		AISPositionReport positionReport = newUnit.getPositionReport();
		if (positionReport != null) {
			LatLong refPos = MasterReferencePoint.getLatLong();
			range = refPos.distanceToMetres(positionReport.latLong);
//			System.out.println(String.format("Range to %d is %3.1fkm",
//					newUnit.mmsiNumber, range/1000));
			if (range / 1000. > aisControl.aisParameters.maxRange_km) {
				return false;
			}
			else {
				return true;
			}
		}
		/*
		 * If the position report was null, then it must be static data. 
		 * 
		 */
		if (existingUnit == null) {
			return false;
		}
		return existingUnit.isInRange;
	}
	/**
	 * Find and AIS unit based on it's MMSI Number.
	 * Returns the PamDataUnit rather than the AISData since
	 * that's needed in a wee while. 
	 * @param mmsiNumber
	 * @return An existing AIS dataunit with the same mmsi number
	 */
	synchronized public AISDataUnit findAISDataUnit(int mmsiNumber) {
		// speed up March 09 to do faster search using correct iterator. 
		AISDataUnit aisDataUnit;
		ListIterator<AISDataUnit> listIterator = getListIterator(ITERATOR_END);		
		while (listIterator.hasPrevious()) {
			aisDataUnit = listIterator.previous();
			if (aisDataUnit.mmsiNumber == mmsiNumber) {
				return aisDataUnit;
			}
		}
//		for (int i = 0; i < pamDataUnits.size(); i++) {
//			aisDataUnit = pamDataUnits.get(i);
//			if (aisDataUnit.mmsiNumber == mmsiNumber) {
//				return pamDataUnits.get(i);
//			}
//		}
		
		return null;
	}

	private long emulatorTimeOffset;
	@Override
	public EmulatedData getNextData() {
		SQLTypes sqlTypes = DBControlUnit.findConnection().getSqlTypes();
		if (getLogging().readNextEmulation(sqlTypes) == false) {
			return null;
		}
		// now all data should be in the loggers table definition.
		AISLogger aisLogger = (AISLogger) getLogging();
		String dataString = aisLogger.dataString.getDeblankedStringValue();
		int nBits = dataString.length() * 6;
		int fillBits = nBits % 8; 
//		int fillBits = 0;
//		if (dataString.length() > 28) {
//			fillBits = 2;
//		}
		long dataTime = aisLogger.getLastTime();
		String aivdm = String.format("!AIVDM,1,1,,A,%s,%d", dataString, fillBits);
		int checkSum = AcquireNmeaData.createStringChecksum(new StringBuffer(aivdm));
		aivdm += String.format("*%02X", checkSum);
		
		return new EmulatedData(dataTime, dataTime+emulatorTimeOffset, aivdm);
	}
	
	@Override
	public boolean prepareDataSource(long[] timeLimits, long timeOffset) {
		emulatorTimeOffset = timeOffset;
		return getLogging().prepareEmulation(timeLimits);
	}


}
