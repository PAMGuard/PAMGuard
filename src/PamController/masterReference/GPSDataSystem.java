package PamController.masterReference;

import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class GPSDataSystem extends PamObserverAdapter implements MasterReferenceSystem {

	private GPSControl gpsControl;
	private GPSDataBlock gpsDataBlock;
	private GpsDataUnit latestGpsData;
	private boolean isViewer;
	
	public GPSDataSystem(GPSControl gpsControl) {
		this.gpsControl = gpsControl;
		gpsDataBlock = gpsControl.getGpsDataBlock();
		gpsDataBlock.addObserver(this, false);
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}

	@Override
	public LatLong getLatLong() {
		if (latestGpsData == null) {
			return null;
		}
		return latestGpsData.getGpsData();
	}

	@Override
	public Long getFixTime() {
		if (latestGpsData == null) {
			return null;
		}
		return latestGpsData.getGpsData().getTimeInMillis();
	}

	@Override
	public Double getCourse() {
		if (latestGpsData == null) {
			return null;
		}
		return latestGpsData.getGpsData().getCourseOverGround();
	}

	@Override
	public Double getHeading() {
		if (latestGpsData == null) {
			return null;
		}
		return latestGpsData.getGpsData().getHeading(true);
	}

	@Override
	public Double getSpeed() {
		if (latestGpsData == null) {
			return null;
		}
		return latestGpsData.getGpsData().getSpeed();
	}

	/* (non-Javadoc)
	 * @see PamController.masterReference.MasterReferenceSystem#getName()
	 */
	@Override
	public String getName() {
		return "GPS data";
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		latestGpsData = (GpsDataUnit) arg;
		PamController.getInstance().notifyModelChanged(PamController.MASTER_REFERENCE_CHANGED);
	}

	@Override
	public String getObserverName() {
		return "Master location system";
	}

	@Override
	public String getError() {
		long now = PamCalendar.getTimeInMillis();
		if (latestGpsData == null) {
			return "No GPS data received";
		}
		long diffT = Math.abs(latestGpsData.getTimeMilliseconds() - now);
		long maxInt = Math.max(gpsControl.getGpsParameters().readInterval+5, 10) * 1000;
		if (diffT < maxInt) {
			return null;
		}
		else {
			return String.format("Time difference between now and latest GPS data is > %ds", maxInt / 1000);
		}
	}

	@Override
	public void setDisplayTime(long displayTime) {
		gpsDataBlock = gpsControl.getGpsDataBlock();
		if (gpsDataBlock == null) {
			return;
		}
		latestGpsData = gpsDataBlock.getClosestUnitMillis(displayTime);
		PamController.getInstance().notifyModelChanged(PamController.MASTER_REFERENCE_CHANGED);
	}

}
