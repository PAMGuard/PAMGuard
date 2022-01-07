package clickDetector;

import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import clickDetector.clicktrains.ClickTrainIdParams;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import pamMaths.PamVector;

/**
 * Automatic click train's from the click train detector. 
 * @author dg50
 *
 */
public class ClickTrainDetection extends OfflineEventDataUnit {
	
	ClickControl clickControl;
	
	static public final int STATUS_STARTING = 0;
	static public final int STATUS_OPEN = 1;
	static public final int STATUS_CLOSED = 2;
	static public final int STATUS_BINME = 3;
	
	private int trainStatus = STATUS_STARTING;
	private double minAngle, maxAngle;
	double runningICI = -1;
	long lastClickTime;
	double firstClickAngle, lastFittedAngle;
	int lastFittedUnitIndex = -1;
	ClickDetector clickDetector;
	private ClickDetection lastClick;
	
	private static int globalEventId;
	private int trainId;
	
	private boolean shouldPlot = true;
	
		
	ClickTrainDetection(ClickControl clickControl, ClickDetection click) {
		
		super(click);
				
		this.clickControl = clickControl;
		
		firstClickAngle = minAngle = maxAngle = getClickAngle(click);
		
		clickDetector = clickControl.getClickDetector();
		
//		int hydrophones = ((AcquisitionProcess) clickDetector.getSourceProcess()).		
//		getAcquisitionControl().ChannelsToHydrophones(getChannelBitmap());
	
//		addSubDetection(click);
		
		// need to get the array axis for this first click. 
//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		getLocalisation().setArrayAxis(click.getClickLocalisation().getArrayOrientationVectors());
		
		
		
	}
	
	/**
	 * Get the most useful angle to use in a click. 
	 * @param click
	 * @return angle in radians. 
	 */
	public static double getClickAngle(ClickDetection click) {
		ClickLocalisation loc = click.getClickLocalisation();
		if (loc == null) {
			return Double.NaN;
		}
		// try first with real world vectors if they exist. 
		PamVector[] vecs = loc.getRealWorldVectors();
		if (vecs != null && vecs.length >= 1) {
			return PamVector.vectorToSurfaceBearing(vecs[0]);
		}
		// then try with planar angles which should do. 		
		double[] surfaceAngle = loc.getPlanarAngles();
		if (surfaceAngle == null || surfaceAngle.length < 1) {
			return Double.NaN;
		}
		return surfaceAngle[0];
	}
	
	
	double testClick (ClickDetection click) {
		if (click.dataType == ClickDetection.CLICK_NOISEWAVE || 
				click.getLocalisation() == null) {
			return 0;
		}
		//System.out.println(String.format("channelMap = %d, new click map = %d", channelMap, dataUnit.getChannelBitmap()));
		
		if (trainStatus == STATUS_CLOSED) return 0;
		
		if (getChannelBitmap() != click.getChannelBitmap()) return 0;
		
		// make local copy of clickParameters.
		ClickTrainIdParams clickParameters = clickControl.clickTrainDetector.getClickTrainIdParameters();
		
		
		// check the angle difference
		double clickAngle = getClickAngle(click);
//		clickAngle = click.getLocalisation().getBearing(0) * -1 + 
//		(90-click.getPairAngle(0, false)) * Math.PI/180.;
		double exAngle = expectedAngle(click, 1);
		double angleError = clickAngle - exAngle;
		angleError = PamUtils.constrainedAngleR(angleError, Math.PI);
		
		double expectedAngleError = Math.toRadians(2.);
		
		if (Math.abs(angleError / expectedAngleError) > 2.) return 0;
		
		// check the ICI difference
//		double newICI = (double) (click.getStartSample() - lastClickTime) / (double) clickDetector.getSampleRate();
		double newICI = getICI(click);
//		System.out.printf("ICI = %3.2f for click %d into event %d\n", newICI, click.clickNumber, getEventId());
		if (newICI < 0) return 0;
		double iciRatio = clickParameters.maxIciChange;
		if (runningICI < 0) {
			if (newICI < clickParameters.iciRange[0] || newICI > clickParameters.iciRange[1]) return 0;
		}
		else {
			iciRatio = newICI / runningICI;
			if (iciRatio < 1.) iciRatio = 1./iciRatio;
		}
		if (iciRatio > clickParameters.maxIciChange) return 0;
		
		/*
		 * Had multiplied these two together, but that's not sensible since if there is no angle change
		 * the goodness would always come out at zero - changed to + on 12Feb 2008. 
		 */
		double badness = Math.pow(angleError / expectedAngleError, 2.) + Math.pow(iciRatio, 2.);
		
		return 1/badness;
	}
	
	double getICI(ClickDetection newClick) {
		ClickDetection exClick;
		PamDataUnit pamDataUnit;
		synchronized (getSubDetectionSyncronisation()) {
			for (int i = getSubDetectionsCount()-1; i >= 0; i--) {
				pamDataUnit = getSubDetection(i);
				if (ClickDetection.class.isAssignableFrom(pamDataUnit.getClass())) {
					exClick = (ClickDetection) pamDataUnit;
				}
				else {
					continue;
				}
				if (exClick.getChannelBitmap() == newClick.getChannelBitmap()) {
					return getICI(newClick, exClick);
				}
			}
		}
		return -1;
	}
	
	double getICI(ClickDetection secondClick, ClickDetection firstClick) {
		return (secondClick.getTimeMilliseconds() - firstClick.getTimeMilliseconds())/1000.;
	}
	
	// return the expected angle in radians.
	double expectedAngle(ClickDetection click, int side) {
		/*
		 * if a position already exists, then it's possible to work out the 
		 * expected angle.
		 * Otherwise, the expected angle is just the last angle for those channel 
		 * numbers.
		 * If no channels exist for those channel numbers, then it's NaN.
		 */   
		int iSide = 0;
		if (side != 1) {
			iSide = 1;
		}
		LatLong clickLatLong = click.getOriginLatLong(false);
		if (clickLatLong == null) return Double.NaN;
		if (getGroupDetectionLocalisation().getLatLong(iSide) != null) {
			double expBearing = clickLatLong.bearingTo(getGroupDetectionLocalisation().getLatLong(iSide));
//			System.out.println("Angle from localisatin");
			return (90 - expBearing) * Math.PI / 180.;
		}
		// if that didn't work, then find the last click that
		// has the same channel numbers and that's the angle. 
		ClickDetection lastClick;
		synchronized(getSubDetectionSyncronisation()) {
		for (int i = getSubDetectionsCount()-1; i >= 0; i--) {
			PamDataUnit pamDataUnit = getSubDetection(i);
			if (ClickDetection.class.isAssignableFrom(pamDataUnit.getClass())) {
				lastClick = (ClickDetection) pamDataUnit;
			}
			else {
				continue;
			}
			if (lastClick == null) {
				return Double.NaN;
			}
			if (lastClick.getChannelBitmap() == click.getChannelBitmap()) {
				double ang = getClickAngle(lastClick);
//				System.out.printf("Test click %d ang %3.1f on ev %d, Angle %3.1f from last click %d\n", 
//						click.clickNumber, Math.toDegrees(getClickAngle(click)), getEventId(), Math.toDegrees(ang), lastClick.clickNumber);
				return ang;
			}
		}
		}
		return Double.NaN;
//		double x = (double) (eT0 - t) * currentSpeed / 1000.;
//		return Math.atan2(eRange, x) * 180. / Math.PI;
	}
	
	public int addSubDetection(ClickDetection click) {
		
//		boolean ok =  clickList.add(click);
		int nSub = super.addSubDetection(click);
		if (clickControl == null) {
			// this happens in the constructor when the first click is added using addSubDetection().
			return nSub;
		}
		
		if (getLocalisation() != null && getLocalisation().getArrayOrientationVectors() == null) {
			if (click.getLocalisation() != null) {
				getLocalisation().setArrayAxis(click.getLocalisation().getArrayOrientationVectors());
			}
		}

		ClickTrainIdParams clickParameters = clickControl.clickTrainDetector.getClickTrainIdParameters();
//		eventEnd = click.getTimeMilliseconds();
		
		double clickBearing = getClickAngle(click);
		maxAngle = Math.max(maxAngle, clickBearing);
		minAngle = Math.min(minAngle, clickBearing);
//		System.out.printf("*********    Click train %d click %d, angle %3.1f, angles min %3.1f, max %3.1f, current %3.1f\n", 
//				this.absBlockIndex, click.clickNumber, Math.toDegrees(clickBearing), Math.toDegrees(minAngle), Math.toDegrees(maxAngle), Math.toDegrees(clickBearing));
		
		if (trainStatus == STATUS_OPEN) {
//			click.setEventId(trainId);
		}
		else if (trainStatus == STATUS_STARTING) {
			if (getSubDetectionsCount() >= clickParameters.minTrainClicks) {
				trainId = ++globalEventId;
				trainStatus = STATUS_OPEN;
				for (int i = 0; i < getSubDetectionsCount(); i++) {
//					(getSubDetection(i)).setEventId(trainId);
				}
			}
		}
		
		if (lastClickTime > 0) {
			double newICI = (double) (click.getStartSample() - lastClickTime) / (double) clickDetector.getSampleRate();
			click.setICI(newICI);
			if (runningICI > 0){
				runningICI = (1.0 - clickParameters.iciUpdateRatio) * runningICI + 
					clickParameters.iciUpdateRatio * newICI;
			}
			else {
				runningICI = newICI;
			}
		}
		else {
			click.setICI(0);
		}
				
		// handle clicks no longer being added in order (if reassignment takes place)
		if (click.getStartSample() > lastClickTime) {
			lastClickTime = click.getStartSample();
			lastClick = click;
		}
		
//		currentSpeed = clickSpeed(click);
		
		return nSub;
	}
	

	public int getTrainStatus() {
		return trainStatus;
	}


	public void setTrainStatus(int trainStatus) {
		this.trainStatus = trainStatus;
	}


	public int getTrainId() {
		return trainId;
	}


	public void setTrainId(int trainId) {
		this.trainId = trainId;
	}


	public double getMaxAngle() {
		return maxAngle;
	}


	public double getMinAngle() {
		return minAngle;
	}


	public long getLastClickTime() {
		return lastClickTime;
	}


	public ClickDetection getLastClick() {
		return lastClick;
	}


	public boolean isShouldPlot() {
		if (getGroupDetectionLocalisation().getNumLatLong() > 0) return true;
		return shouldPlot;
	}


	public void setShouldPlot(boolean shouldPlot) {
		this.shouldPlot = shouldPlot;
	}



}
