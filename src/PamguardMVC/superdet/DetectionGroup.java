package PamguardMVC.superdet;

import GPS.GpsData;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.PamDetection;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import annotation.detectiongroup.EventIdAnnotation;
import annotation.detectiongroup.EventIdAnnotationType;
import pamMaths.PamVector;

/**
 * Base class for any group of data units - the modern base for all types of
 * event, click train, detection group, etc. Holds the group localisation,
 * an event id (carried as an annotation so that it can use standard database
 * and display functionality) and an open / closed status for groups which are
 * built up in real time.
 * <p>
 * Functionality which is specific to groups of acoustic detections (average
 * waveforms, inter-detection-intervals, bearings) is in the subclass
 * {@link AcousticDetectionGroup}.
 *
 * @author Jamie Macaulay, Doug Gillespie
 */
public class DetectionGroup<T extends PamDataUnit> extends SuperDetection<T> implements PamDetection {

	static public final int STATUS_OPEN = 1;
	static public final int STATUS_CLOSED = 2;

	private int status = STATUS_OPEN;

	private GroupLocalisation groupDetectionLocalisation;

	public DetectionGroup(T firstDetection) {
		super(firstDetection.getTimeMilliseconds(), firstDetection.getChannelBitmap(),
				firstDetection.getBasicData().getStartSample(), firstDetection.getBasicData().getSampleDuration());
		this.setSequenceBitmap(firstDetection.getSequenceBitmapObject());
		makeLocalisation();
		addSubDetection(firstDetection);
	}

	/**
	 * Note that if using this constructor, the sequence map will have to be set
	 * explicitly by the calling class. No localisation is created until
	 * {@link #makeLocalisation()} or {@link #getGroupDetectionLocalisation()} is
	 * called.
	 */
	public DetectionGroup(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	public DetectionGroup(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	/**
	 * Create the group localisation if it does not already exist and initialise it
	 * from the first sub detection which has localisation information.
	 */
	public void makeLocalisation() {
		if (groupDetectionLocalisation == null) {
			groupDetectionLocalisation = new GroupLocalisation(this, null);
			groupDetectionLocalisation.setReferenceHydrophones(this.getChannelBitmap());
			for (int i = 0; i < 2; i++) {
				groupDetectionLocalisation.addGroupLocaResult(new GroupLocResult(null, i, 0));
			}
		}

		/*
		 * Initially set the axis of that localisation to be the same as the first sub
		 * detection which has localisation data ...
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

	@Override
	public int addSubDetection(T subDetection) {
		if (subDetection == null) return 0;
		if (groupDetectionLocalisation != null &&
				(getSubDetectionsCount() == 0 || groupDetectionLocalisation.getLocContents().getLocContent() == 0)) {
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

	/**
	 * Get the event id for the group. This is held in an {@link EventIdAnnotation}
	 * so that it can be written to the database using standard annotation logging.
	 * @return the event id, or 0 if none has been set.
	 */
	public int getEventId() {
		EventIdAnnotation an = (EventIdAnnotation) findDataAnnotation(EventIdAnnotation.class);
		return an == null ? 0 : an.getEventId();
	}

	/**
	 * Set the event id for the group. The id is held in an
	 * {@link EventIdAnnotation}.
	 * @param eventId the event id.
	 */
	public void setEventId(int eventId) {
		EventIdAnnotation an = (EventIdAnnotation) findDataAnnotation(EventIdAnnotation.class);
		if (an == null) {
			addDataAnnotation(new EventIdAnnotation(EventIdAnnotationType.getInstance(), eventId));
		}
		else {
			an.setEventId(eventId);
		}
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
	}

	/**
	 * Get the group localisation, creating it if it does not yet exist.
	 * @return the group localisation.
	 */
	public GroupLocalisation getGroupDetectionLocalisation() {
		if (groupDetectionLocalisation == null) {
			makeLocalisation();
		}
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
		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(LocContents.HAS_PERPENDICULARERRORS);
		tmResult.setFirstBearing(PamVector.fromHeadAndSlant(bearing,0) );
		if (detectionLatLong != null) groupDetectionLocalisation.addLocContents(LocContents.HAS_BEARING);
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
