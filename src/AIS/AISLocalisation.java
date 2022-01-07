package AIS;

import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;

public class AISLocalisation extends AbstractLocalisation {

	AISDataUnit aisDataUnit;
	
	public AISLocalisation(AISDataUnit aisDataUnit) {
		super(aisDataUnit, 0, 0);
		this.aisDataUnit = aisDataUnit;
		aisDataUnit.setLocalisation(this);
	}

	@Override
	public boolean hasLocContent(int requiredContent) {
		if (aisDataUnit.getPositionReport() != null) {
			return true;
		}
		return false;
	}

	@Override
	public double getBearing(int side) {
		GpsData gpsData = findGpsData();
		if (gpsData == null) return super.getBearing(side);
		AISPositionReport aisPositionReport = aisDataUnit.getPositionReport();
		if (aisPositionReport == null) return super.getBearing(side);
		double ang =  gpsData.bearingTo(aisPositionReport.latLong) - gpsData.getCourseOverGround();
		return ang * Math.PI/180.;
	}

	@Override
	public double getDepth() {
		return 0;
	}

	@Override
	public double getRange(int iSide) {
		GpsData gpsData = findGpsData();
		if (gpsData == null) return super.getRange(iSide);
		AISPositionReport aisPositionReport = aisDataUnit.getPositionReport();
		if (aisPositionReport == null) return super.getRange(iSide);
		return gpsData.distanceToMetres(aisPositionReport.latLong);
	}
	
	// need a slightly fast way of getting appropriate GPS data. 
	GpsData findGpsData() {
		return findGpsData(PamCalendar.getTimeInMillis());
	}
	
	GpsData lastFoundGpsData = null;
	long lastGpsFindTime = 0;
	
	GpsData findGpsData(long timeMillisecods) {
		if (timeMillisecods == lastGpsFindTime && lastFoundGpsData != null) {
			return lastFoundGpsData;
		}
		PamDataBlock<GpsDataUnit> gpsDataBlock = PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsDataBlock == null) return null;
		GpsDataUnit gpsDataUnit =  gpsDataBlock.getClosestUnitMillis(timeMillisecods);
		if (gpsDataUnit == null) return null;
		lastGpsFindTime = timeMillisecods;
	
		return (lastFoundGpsData = gpsDataUnit.getGpsData());
	}

}
