package GPS;

import java.util.ListIterator;

import GPS.effort.GpsEffortProvider;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import nmeaEmulator.EmulatedData;
import nmeaEmulator.NMEAEmulator;
import pamScrollSystem.ViewLoadObserver;

/**
 * Add a bit of extra functionality to GPSDataBlock so 
 * that it can check new GPS data are 'reasonable'
 * and flag bad ones. 
 * 
 * @author Doug
 *
 */
public class GPSDataBlock extends PamDataBlock<GpsDataUnit> implements NMEAEmulator {

	/**
	 * Max reasonable speed in km per hour
	 */
	private double reasonableSpeed = 100; 

	/**
	 * Reasonable time to wait before believing anything
	 */
	private double reasonableResetTime = 120;

	/**
	 * Max number of objects to look at before deciding it's OK anyway. 
	 */
	private int reasonableTries = 10;

	public GPSDataBlock(PamProcess process) {
		super(GpsDataUnit.class, process.getPamControlledUnit().getUnitName(), process, 1);
		setSynchLock(NavDataSynchronisation.getSynchobject());
		setEffortProvider(new GpsEffortProvider(this));
	}

	@Override
	public void addPamData(GpsDataUnit gpsDataUnit) {
		boolean r = isReasonable(gpsDataUnit);
		if (!r) {
			gpsDataUnit.getGpsData().setDataOk(false);
		}
		else {
			/*
			 * Only sortDistanceFromLast when sure we're going to keep this unit
			 * and that we think its contents are OK. 
			 */
			gpsDataUnit.getGpsData().sortDistanceFromLast();
		}
		super.addPamData(gpsDataUnit);
	}

	/**
	 * Check a GPS entry is reasonable - i.e. doesn't jump a huge
	 * distance from the preceeding entry.
	 * @param gpsDataUnit
	 * @return true if reasonable. 
	 */
	public boolean isReasonable(GpsDataUnit gpsDataUnit) {

		GpsData thisData, thatData;
		thisData = gpsDataUnit.getGpsData();

		if (!thisData.isDataOk()) {
			return false;
		}
		
		synchronized (getSynchLock()) {
			int nTries = 0;
			double speed, timeSecs;
			ListIterator<GpsDataUnit> gpsIterator = getListIterator(PamDataBlock.ITERATOR_END);
			while (gpsIterator.hasPrevious()) {
				thatData = gpsIterator.previous().getGpsData();
				// ok if we've tried to many data. 
				if (nTries++ > reasonableTries) {
					return true;
				}
				// ignore gps points flagged as bad. 
				if (!thatData.isDataOk()) {
					continue;
				}
				// OK if it's gone too far back in time
				timeSecs = (thisData.getTimeInMillis() - thatData.getTimeInMillis()) / 1000.; 
				if (timeSecs > reasonableResetTime) {
					return true;
				}
				speed = thisData.distanceToMetres(thatData) / timeSecs * 3.6; // speed in km / hr
				if (speed > reasonableSpeed) {
					return false;
				}
				else {
					return true;
				}
			}
		}
		return true;
	}
	
	private ListIterator<GpsDataUnit> emulatorIterator;
	private long emulatorTimeOffset;
	private EmulatedData readyGGAData;
	@Override
	public EmulatedData getNextData() {
		/*
		 * If the last time this was called, it returned RMC data
		 * this time it will return the pre prepared GGA data before
		 * the next call which will return RMC data again. 
		 */
		if (readyGGAData != null) {
			EmulatedData dd = readyGGAData;
			readyGGAData = null;
			return dd;
		}
		if (!emulatorIterator.hasNext()) {
			return null;
		}
		GpsDataUnit gpsUnit =  emulatorIterator.next();
		GpsData gpsData = gpsUnit.getGpsData().clone();
		long dataTime = gpsData.getTimeInMillis();
		long simTime = dataTime + emulatorTimeOffset;
		gpsData.setTimeInMillis(simTime);
		readyGGAData = new EmulatedData(dataTime, simTime, gpsData.gpsDataToGGA(4));
		return new EmulatedData(dataTime, simTime, gpsData.gpsDataToRMC(4));
	}

	@Override
	public boolean prepareDataSource(long[] timeLimits, long timeOffset) {
		emulatorIterator = this.getListIterator(0);
		emulatorTimeOffset = timeOffset;
		return true;
	}

	/**
	 * Find the closest GPS data to the given point. 
	 * @param latLong
	 * @return closest GPS data
	 */
	public GpsDataUnit findClosestGPS(LatLong latLong) {
		return findClosestGPS(latLong, Double.MAX_VALUE);
	}
	
	/**
	 * Find the closest GPS unit, with an optional maximum where it will
	 * return null if nothing is that close. 
	 * @param latLong
	 * @paran maxR max distance in metres. 
	 * @return closest GPS data
	 */
	public GpsDataUnit findClosestGPS(LatLong latLong, double maxR) {
		double minR = maxR;
		GpsDataUnit gpsData = null;
		synchronized (getSynchLock()) {
			ListIterator<GpsDataUnit> it = getListIterator(0);
			while (it.hasNext()) {
				GpsDataUnit current = it.next();
				double r = current.getGpsData().distanceToMetres(latLong);
				if (r <= minR) {
					minR = r;
					gpsData = current;
				}
			}
		}
		return gpsData;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#loadViewerData(long, long, pamScrollSystem.ViewLoadObserver)
	 */
	@Override
	public boolean loadViewerData(long dataStart, long dataEnd, ViewLoadObserver loadObserver) {
		// TODO Auto-generated method stub
		return super.loadViewerData(dataStart, dataEnd, loadObserver);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#loadViewerData(PamguardMVC.dataOffline.OfflineDataLoadInfo, pamScrollSystem.ViewLoadObserver)
	 */
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		// TODO Auto-generated method stub
		return super.loadViewerData(offlineDataLoadInfo, loadObserver);
	}

}
