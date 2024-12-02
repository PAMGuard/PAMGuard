package Localiser.detectionGroupLocaliser;

import pamMaths.PamVector;
import GPS.GpsData;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class GroupDetection<T extends PamDataUnit> extends SuperDetection<T> implements PamDetection {

	private int status = STATUS_OPEN;

	private int eventId;

	//		same as in ClickTRainDetection
	//		static public final int STATUS_STARTING = 0;
	static public final int STATUS_OPEN = 1;
	static public final int STATUS_CLOSED = 2;


	//		static private int usedPositions = 0;

	//static public final int STATUS_BINME = 3;

	private GroupLocalisation groupDetectionLocalisation;

	private SuperDetection<T> subDetectionManager;

	public GroupDetection(T firstDetection) {

		super(firstDetection.getTimeMilliseconds(), firstDetection.getChannelBitmap(), 
				firstDetection.getBasicData().getStartSample(), firstDetection.getBasicData().getSampleDuration());
		this.setSequenceBitmap(firstDetection.getSequenceBitmapObject());
		makeLocalisation();
		addSubDetection(firstDetection);

	}


	/**
	 * Note that if using this constructor, the sequence map will have to be set explicitly by the calling class
	 * 
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param duration
	 */
	public GroupDetection(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		makeLocalisation();
	}

	public void makeLocalisation() {
		if (groupDetectionLocalisation == null) {
			groupDetectionLocalisation = new GroupLocalisation(this, null);
			groupDetectionLocalisation.setReferenceHydrophones(this.getChannelBitmap());
			for (int i = 0; i < 2; i++) {
				groupDetectionLocalisation.addGroupLocaResult(new GroupLocResult(null, i, 0));
			}
		}
		
		/*
		 * 
		 * Initially set the axis of that localisation to be the same as the 
		 * first sub detection which has localisation data ...
		 */
		synchronized (getSubDetectionSyncronisation()) {
			int n = getSubDetectionsCount();
			T subDetection;
			for (int i = 0; i < n; i++) {
				subDetection = (T) getSubDetection(i);
				if (subDetection.getLocalisation() != null) {
					groupDetectionLocalisation.setArrayAxis(subDetection.getLocalisation().getArrayOrientationVectors());
				}
			}
		}

		setLocalisation(groupDetectionLocalisation);

	}

	public int addSubDetection(T subDetection) {
		if (subDetection == null) return 0;
		if (getSubDetectionsCount() == 0 || groupDetectionLocalisation.getLocContents().getLocContent() == 0) {
			if (subDetection.getLocalisation() != null) {
				int currentLocContent = subDetection.getLocalisation().getLocContents().getLocContent();
				LocContents newLocContent = new LocContents(currentLocContent);
				groupDetectionLocalisation.setLocContents(newLocContent);
				groupDetectionLocalisation.setSubArrayType(subDetection.getLocalisation().getSubArrayType());
				PamVector[] angles = subDetection.getLocalisation().getWorldVectors(); 
				for (int i = 0; i < angles.length; i++) {
					GroupLocResult tmResult = groupDetectionLocalisation.getGroupLocaResult(i);
					if (tmResult == null) continue;
					tmResult.setFirstBearing(angles[i]);
					tmResult.setFirstHeading(subDetection.getLocalisation().getBearingReference());
					groupDetectionLocalisation.addLocContents(LocContents.HAS_BEARING);
				}
				if (angles.length > 1 || subDetection.getLocalisation().bearingAmbiguity()) {
					groupDetectionLocalisation.addLocContents(LocContents.HAS_AMBIGUITY);
				}
			}
		}
		return super.addSubDetection(subDetection);
	}


	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}



	public Double getPredictedBearing(long predictionTime) {
		if (getSubDetectionsCount() <= 0) {
			return null;
		}
		T lastSubDet = (T) getSubDetection(getSubDetectionsCount()-1);
		AbstractLocalisation loc = lastSubDet.getLocalisation();
		if (loc == null) {
			return null;
		}
		double[] planarAngles = loc.getPlanarAngles();
		if (planarAngles == null) {
			return loc.getBearing(0);
		}
		return planarAngles[0];

		//			return null;
	}


	/**
	 * Get the 
	 * @return
	 */
	public GroupLocalisation getGroupDetectionLocalisation() {
		return groupDetectionLocalisation;
	}


	@Deprecated
	public void addFitData(int iSide, GpsData originLatLong, LatLong detectionLatLong, double bearing, double range,
			double referenceHeading, double perpendicularError, double parallelError, double referenceHeading2) {
		if (groupDetectionLocalisation == null) {
			makeLocalisation();
		}
		GroupLocResult tmResult = groupDetectionLocalisation.getGroupLocaResult(iSide);
		tmResult.setLatLong(detectionLatLong);
		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(LocContents.HAS_LATLONG | LocContents.HAS_RANGE);
		//tmResult.setErrorX(perpendicularError);
		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(LocContents.HAS_PERPENDICULARERRORS);
		//tmResult.setErrorY(parallelError);
//		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(AbstractLocalisation.HAS_);
		tmResult.setFirstBearing(PamVector.fromHeadAndSlant(bearing,0) );
		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(LocContents.HAS_BEARING);
//		tmResult.set
//		tmResult.set
//		groupDetectionLocalisation.addFitData(originLatLong, detectionLatLong, bearing, range, referenceHeading, perpendicularError, parallelError, referenceHeading2);
	}
	
	public void setEventEndTime(long eventEndTime) {
		super.setDurationInMilliseconds(eventEndTime-getTimeMilliseconds());
	}

	public long getEventEndTime() {
		Double duration = getDurationInMilliseconds();
		long endtime = getTimeMilliseconds();
		if (duration != null) {
			endtime += duration.longValue();
		}
		return endtime;
	}


}
