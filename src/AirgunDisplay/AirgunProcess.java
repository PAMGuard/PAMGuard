package AirgunDisplay;

import java.util.ArrayList;

import AIS.AISDataBlock;
import AIS.AISDataUnit;
import AIS.AISPositionReport;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.SingletonDataBlock;

public class AirgunProcess extends PamProcess {

	AirgunControl airgunControl;

	PamDataBlock<GpsDataUnit> gpsDataBlock;
	AISDataBlock aisDataBlock;
	PamDataBlock<AirgunDataUnit> outputDataBlock;

	public AirgunProcess(AirgunControl airgunControl) {
		super(airgunControl, null);
		this.airgunControl = airgunControl;
		outputDataBlock = new SingletonDataBlock(AirgunDataUnit.class, "Airgun Position", this, 0);
		outputDataBlock.setOverlayDraw(new AirgunGraphics(airgunControl));
		outputDataBlock.setClearAtStart(false);
		outputDataBlock.setPamSymbolManager(new StandardSymbolManager(outputDataBlock, AirgunGraphics.defaultSymbol, true));
		addOutputDataBlock(outputDataBlock);
		findSourceData();
	}

	@Override
	public void destroyProcess() {
		// TODO Auto-generated method stub
		super.destroyProcess();
	}

	@Override
	public void updateData(PamObservable o, PamDataUnit arg) {
		useData(o, arg);
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		useData(o, arg);
	}

	/**
	 * AIS data are generally updated rather than created new, so
	 * use newData and updateData in the same way,
	 * @param o PamObservable sent to updateData or newData
	 * @param arg PamDataUnit sent to updateData or newData
	 */
	private void useData(PamObservable o, PamDataUnit arg) {
		int refPos = airgunControl.airgunParameters.gunsReferencePosition;
		if (refPos == AirgunParameters.GUNS_FIXEDPOSITION) {
			return;
		}
		if (o == gpsDataBlock && refPos == AirgunParameters.GUNS_THIS_VESSEL){
			useGpsData((PamDataBlock) o, (GpsDataUnit) arg);
		}
		else if (o == aisDataBlock && refPos == AirgunParameters.GUNS_AIS_VESSEL) {
			useAisData((PamDataBlock) o, (AISDataUnit) arg);
		}
	}

	private void useGpsData(PamDataBlock dataBlock, GpsDataUnit gpsDataUnit) {
		if (gpsDataUnit == null) {
			return;
		}
		GpsData gpsData = gpsDataUnit.getGpsData();
		createAirgunData(gpsData);
	}

	private void useAisData(PamDataBlock dataBlock, AISDataUnit aisDataUnit) {
		if (aisDataUnit.mmsiNumber != airgunControl.airgunParameters.gunsMMSIVessel) return;
//		if (aisDataUnit.isComplete() == false) {
//			return;
//		}
		AISPositionReport positionReport = aisDataUnit.getPositionReport();
		if (positionReport == null) {
			return;
		}
		else {
			useAISPositionReport(aisDataUnit, positionReport);
		}
	}
	private void useAISPositionReport(AISDataUnit aisDataUnit, AISPositionReport positionReport) {
		GpsData gpsData = new GpsData();
		gpsData.setLatitude(positionReport.getLatitude());
		gpsData.setLongitude(positionReport.getLongitude());
		gpsData.setTrueHeading(positionReport.trueHeading);
		gpsData.setCourseOverGround(positionReport.trueHeading);
		gpsData.setSpeed(positionReport.getKnownSpeed());
		gpsData.setTimeInMillis(aisDataUnit.getTimeMilliseconds());
		createAirgunData(gpsData);
	}


	private void createAirgunData(GpsData gpsData) {
//		PamDataUnit nU = outputDataBlock.getNewUnit(0,0,0);
		AirgunDataUnit nU = new AirgunDataUnit(PamCalendar.getTimeInMillis(), gpsData);
		outputDataBlock.addPamData(nU);
	}

	@Override
	public void noteNewSettings() {
//		super.noteNewSettings();
		findSourceData();
	}
	synchronized public void findSourceData(){

		PamDataBlock newDataBlock = null;
		if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_THIS_VESSEL) {
			aisDataBlock = null;
			// find and subscribe to the GPS data block
			/*
			 * 4/3/13 Change this to ensure it get's the correct GPS data block. In past it was picking up itself if the
			 * airgun unit was before the GPS unit since the airgun data unit is a subclass of GPSDataUnit. Should now get
			 * the correct GPS data.
			 */
//			newDataBlock = PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
			ArrayList<PamDataBlock> gpsBlocks = PamController.getInstance().getDataBlocks(GpsDataUnit.class, false);
			if (gpsBlocks != null && gpsBlocks.size() > 0) {
				newDataBlock = gpsBlocks.get(0);
			}
			if (newDataBlock != null && newDataBlock != gpsDataBlock) {
				gpsDataBlock = newDataBlock;
				setParentDataBlock(gpsDataBlock);
			}
			else if (newDataBlock == null) {
				System.out.println("Unable to find GPS data block for airgun display");
			}
		}
		else if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_AIS_VESSEL){
			gpsDataBlock = null;
			newDataBlock = PamController.getInstance().getDataBlock(AISDataUnit.class, 0);
			if (newDataBlock != null) {
				aisDataBlock = (AISDataBlock) newDataBlock;
				setParentDataBlock(aisDataBlock);
			}
			else {

				System.out.println("Unable to find AIS data block for airgun display");
			}
		}
		else if (airgunControl.airgunParameters.fixedPosition != null){
			createAirgunData(new GpsData(airgunControl.airgunParameters.fixedPosition));
		}

	}

	/**
	 * Called when view times change so that the display can update it's
	 * location based on the new time.
	 */
	protected void newViewTime(){
		if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_THIS_VESSEL) {
			if (gpsDataBlock == null) {
				return;
			}
			GpsDataUnit gpsDataUnit = gpsDataBlock.getFirstUnitAfter(PamCalendar.getTimeInMillis());
			useGpsData(gpsDataBlock, gpsDataUnit);
		}
		else if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_AIS_VESSEL){ // use AIS data, slightly harder to find !
			if (aisDataBlock == null) {
				return;
			}
			AISDataUnit aisDataUnit = aisDataBlock.findAISDataUnit(airgunControl.airgunParameters.gunsMMSIVessel);
			if (aisDataUnit == null) {
				return;
			}
			AISPositionReport aisPositionReport = aisDataUnit.findPositionReport(PamCalendar.getTimeInMillis());
			useAISPositionReport(aisDataUnit, aisPositionReport);
		}
		else if (airgunControl.airgunParameters.fixedPosition != null){
			createAirgunData(new GpsData(airgunControl.airgunParameters.fixedPosition));
		}
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	/**
	 * Implementation of PositionReference. Will try to return a ref position
	 * for a given time.
	 * @param timeMillis
	 * @return
	 */
	public GpsData getReferencePosition(long timeMillis) {
		if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_THIS_VESSEL){
			return getGpsBasedPosition(timeMillis);
		}
		if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_AIS_VESSEL){
			return getAISBasedPosition(timeMillis);
		}
		if (airgunControl.airgunParameters.gunsReferencePosition == AirgunParameters.GUNS_FIXEDPOSITION){
			return getFixedPosition(timeMillis);
		}

		return null;
	}

	private GpsData getFixedPosition(long timeMillis) {
		return new GpsData(airgunControl.airgunParameters.fixedPosition);
	}

	private GpsData getGpsBasedPosition(long timeMillis) {
		if (gpsDataBlock == null) {
			return null;
		}
		GpsDataUnit gpsDataUnit = gpsDataBlock.getClosestUnitMillis(timeMillis);
		if (gpsDataUnit == null) {
			return null;
		}
		GpsData gpsData = gpsDataUnit.getGpsData();
		LatLong gunPos = gpsData.travelDistanceMeters(gpsData.getHeading() + 180,
				airgunControl.airgunParameters.dimE);
		gunPos = gunPos.travelDistanceMeters(gpsData.getHeading() + 90,
				airgunControl.airgunParameters.dimF);
		GpsData gunGPS = gpsData.clone();
		gunGPS.setLatitude(gunPos.getLatitude());
		gunGPS.setLongitude(gunPos.getLongitude());
		return gunGPS;
	}

	private GpsData getAISBasedPosition(long timeMillis) {
		if (aisDataBlock == null) {
			return null;
		}
		AISDataUnit aisDataUnit = aisDataBlock.findAISDataUnit(airgunControl.airgunParameters.gunsMMSIVessel);
		if (aisDataUnit == null) {
			return null;
		}
		AISPositionReport aisPositionReport = aisDataUnit.findPositionReport(PamCalendar.getTimeInMillis());
		if (aisPositionReport == null) {
			return null;
		}
		LatLong gunPos = aisPositionReport.latLong;
		gunPos = gunPos.travelDistanceMeters(aisPositionReport.courseOverGround + 180,
				airgunControl.airgunParameters.dimE);
		gunPos = gunPos.travelDistanceMeters(aisPositionReport.courseOverGround + 90,
				airgunControl.airgunParameters.dimF);
		GpsData gunGPS = new GpsData(gunPos);
		gunGPS.setCourseOverGround(aisPositionReport.courseOverGround);
		return gunGPS;
	}

}
