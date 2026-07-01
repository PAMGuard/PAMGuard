package AIS;

import java.time.Instant;
import java.util.HashMap;
import java.util.ListIterator;

import NMEA.AcquireNmeaData;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import generalDatabase.DBControlUnit;
import generalDatabase.SQLTypes;
import nmeaEmulator.EmulatedData;
import nmeaEmulator.NMEAEmulator;

public class AISDataBlock extends PamDataBlock<AISDataUnit> implements NMEAEmulator {
	
	private AISControl aisControl;
	
	//private HashMap<Integer,AISDataUnit> aisVesselMap;
	private Object vesselMapSyncLock = new Object();

	public AISDataBlock(AISControl aisControl, PamProcess parentProcess) {
		super(AISDataUnit.class, "AIS Data", parentProcess, 0);
		this.aisControl = aisControl;
		/*synchronized(vesselMapSyncLock) {
			aisVesselMap = new HashMap<Integer,AISDataUnit>();
		}*/
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
	public boolean addAISData(AISDataUnit newAISUnit) {
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
			long t = newAISUnit.getTimeMilliseconds();
			aisDataUnit.setTimeMilliseconds(t);
			//This was transmitting updates twice, calling addData via notifyObservers. 
			aisDataUnit.updateDataUnit(t);
			updatePamData(aisDataUnit, t); // need this to get it to save. 
		}
		else {
			addPamData(newAISUnit);
		}
		return newUnit;
	}
	
	/**
	 * Need to copy this to support slightly different behaviour, of <br>
	 * a) knowing that units are not in order, since their time get's updated
	 * to that of the latest position <br>
	 * b) trim position reports of units not deleted since in a static monitoring
	 * situation, a vessel that is there permanently and is running AIS will generate
	 * an infinite list of position reports 
	 */
	protected int removeOldUnitsT(long currentTimeMS) {
		// will have to do something to see if any of the blocks are still
		// referenced !
		if (pamDataUnits.isEmpty()) {
			return 0;
		}
		AISDataUnit pamUnit;
		long firstWantedTime = currentTimeMS - Math.max(this.getNaturalLifetimeMillis(), 1000);
		firstWantedTime = Math.min(firstWantedTime, currentTimeMS - getRequiredHistory());
		
		// tail time for position reports. Earliest of data keep time and now - tail time. 
		long firstPosTime2 = currentTimeMS - aisControl.aisParameters.tailLength * 60000;
		firstPosTime2 = Math.min(firstPosTime2, firstWantedTime);

		int unitsJustRemoved = 0;
		synchronized (getSynchLock()) {
			ListIterator<AISDataUnit> it = pamDataUnits.listIterator();
			while (it.hasNext()) {
				pamUnit = it.next();
				if (pamUnit.getTimeMilliseconds() > firstWantedTime) {
					pamUnit.trimPositionReports(firstPosTime2);
					continue; // don't break since we want to check all units. 
				}
				// System.out.printf("%s Remove %s at %s, first wanted is %s\n",
				// PamCalendar.formatDateTime(System.currentTimeMillis()),
				// getDataName(),
				// PamCalendar.formatDateTime(pamUnit.getTimeMilliseconds()),
				// PamCalendar.formatDateTime(firstWantedTime) );
//				AISDataUnit removed = pamDataUnits.remove(0);
				//				if (isdebug()) {
				//					Debug.out.println("Removing data unit " + removed);
				//				}

				it.remove();

				// unitsRemoved++;
				unitsJustRemoved++;
			}
			unitsRemoved += unitsJustRemoved;
		}
		// TODO check this.
		// return unitsRemoved; //should this be the count of unitsJustRemoved not total
		// units removed?
		return unitsJustRemoved;
	}
	
	private boolean wantData(AISDataUnit existingUnit, AISDataUnit newUnit) {
		if (!aisControl.aisParameters.limitRange) {
			return true;
		}
		double range = 0;
		AISPositionReport positionReport = newUnit.getPositionReport();
		if (positionReport != null) {
			LatLong refPos = MasterReferencePoint.getLatLong();
			if (refPos == null) { // very occasional error if AIS arrives before first GPS.
				return true;
			}
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
	public AISDataUnit findAISDataUnit(int mmsiNumber) {
		// speed up March 09 to do faster search using correct iterator. 
		AISDataUnit aisDataUnit;
		synchronized (getSynchLock()) {
			ListIterator<AISDataUnit> listIterator = getListIterator(ITERATOR_END);		
			while (listIterator.hasPrevious()) {
				aisDataUnit = listIterator.previous();
				if (aisDataUnit.mmsiNumber == mmsiNumber) {
					return aisDataUnit;
				}
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
		if (!getLogging().readNextEmulation(sqlTypes)) {
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
