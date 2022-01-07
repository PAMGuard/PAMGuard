package difar.trackedGroups;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import GPS.GpsData;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import targetMotionModule.TargetMotionInformation;

public class TrackedGroupTargetMotionInformation implements TargetMotionInformation {

	private TrackedGroupProcess trackedGroupProcess;
	
	private ArrayList<PamDataUnit> trackedGroupDataUnits;

	private GpsData systemOrigin;
	
	private PamVector[] origins;

	private PamVector[][] worldVectors;
	
	private PamQuaternion[] eulerAngles;
	
	private ArrayList<ArrayList<Point3f>> hydrophonePos = new ArrayList<>();
	
	private double[] meanPos = new double[2];
	
	public TrackedGroupTargetMotionInformation(TrackedGroupProcess trackedGroupProcess,
			ArrayList<PamDataUnit> detectionList) {
		super();
		this.trackedGroupProcess = trackedGroupProcess;
		this.trackedGroupDataUnits = detectionList;
		
		// need to work out quite a few things here about origins, etc. 
		// a lot of stuff will be relative to the first data unit. 
		systemOrigin = detectionList.get(0).getOriginLatLong(false);
		origins = new PamVector[detectionList.size()];
		worldVectors = new PamVector[detectionList.size()][];
		eulerAngles = new PamQuaternion[detectionList.size()];
		for (int i = 0; i < detectionList.size(); i++) {
			PamDataUnit aUnit = detectionList.get(i);
			GpsData anOrigin = aUnit.getOriginLatLong(false);
			origins[i] = new PamVector(systemOrigin.distanceToMetresX(anOrigin), systemOrigin.distanceToMetresY(anOrigin), 0);
			meanPos[0] += origins[i].getElement(0);
			meanPos[1] += origins[i].getElement(1);
			worldVectors[i] = aUnit.getLocalisation().getWorldVectors();
			eulerAngles[i] = new PamQuaternion(anOrigin.getHeading(), anOrigin.getPitch(), anOrigin.getRoll());
			ArrayList<Point3f> hPos = new ArrayList<>();
			hPos.add(new Point3f((float)origins[i].getElement(0),(float)origins[i].getElement(1),(float)origins[i].getElement(2)));
			hydrophonePos.add(hPos);
		}
		meanPos[0] /= detectionList.size();
		meanPos[1] /= detectionList.size();
	}

	/**
	 * 
	 * @return the mean position of all buoys in x,y coordinates. 
	 */
	public double[] getMeanPosition() {
		return meanPos;
	}
	
	@Override
	public ArrayList<PamDataUnit> getCurrentDetections() {
		return trackedGroupDataUnits;
	}

	@Override
	public int getNDetections() {
		return trackedGroupDataUnits.size();
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelays() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelayErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamVector[] getOrigins() {
		return origins;
	}

	@Override
	public PamVector[][] getWorldVectors() {
		return worldVectors;
	}

	@Override
	public PamQuaternion[] getEulerAngles() {
		return eulerAngles;
	}

	@Override
	public ArrayList<ArrayList<Point3f>> getHydrophonePos() {
		return hydrophonePos;
	}

	@Override
	public LatLong getGPSReference() {
		return systemOrigin;
	}

	@Override
	public PamVector latLongToMetres(LatLong ll) {
		return new PamVector(systemOrigin.distanceToMetresX(ll), systemOrigin.distanceToMetresY(ll), 0);
	}

	@Override
	public LatLong metresToLatLong(PamVector pt) {
		return systemOrigin.addDistanceMeters(pt.getElement(0), pt.getElement(1));
	}

	@Override
	public int getReferenceHydrophones() {
		return trackedGroupDataUnits.get(0).getChannelBitmap();
	}

	@Override
	public Long getTimeMillis() {
		return trackedGroupDataUnits.get(0).getTimeMilliseconds();
	}

	@Override
	public GpsData getBeamLatLong(LatLong localised) {
		// TODO Auto-generated method stub
		return new GpsData(localised);
	}

	@Override
	public long getBeamTime(GpsData beamPos) {
		// TODO Auto-generated method stub
		return 0;
	}


}
