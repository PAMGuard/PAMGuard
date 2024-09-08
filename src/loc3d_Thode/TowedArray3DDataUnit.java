package loc3d_Thode;

import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.PamDetection;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class TowedArray3DDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements PamDetection {

	private TowedArray3DLocalization towedArray3DLocalization;

	private PamDataUnit parentDetection;

	private boolean hasAngle = false;

	private double angle[];

	private boolean hasAngleAmbiguity = true;

	private boolean hasRange = false;

	private double range[]; // meters !

	private boolean hasDepth;

	private double depth;

	private double heading[],tilt[];
	
	private LatLong latlong=null;
	
	private boolean hasLatLong;

	/*
	 * Constructor to use during normal operation
	 */
	public TowedArray3DDataUnit(PamDataUnit parentDetection,
			int locContents, int referenceHydrophones, int Nstation, boolean hasAngle, boolean hasRange, 
			boolean hasDepth) {
			super(parentDetection.getTimeMilliseconds(), parentDetection.getChannelBitmap(), 
					parentDetection.getStartSample(), parentDetection.getSampleDuration());
		setLocalisation(towedArray3DLocalization = new TowedArray3DLocalization(this, locContents, referenceHydrophones));
		this.parentDetection = parentDetection;


		this.angle=new double[Nstation+1];
		this.range=new double[Nstation+1];
		this.heading=new double[Nstation+1];
		this.tilt=new double[Nstation+1];
		for (int i=0;i<=Nstation; i++){
			this.angle[i]=0;
			this.heading[i]=0;
			this.tilt[i]=0;
			this.range[i]=0;
		}
		this.hasAngle=hasAngle;
		this.hasDepth=hasDepth;
		this.hasRange=hasRange;
		this.hasLatLong=false;
	}
	
	/*
	 * constructor  to use during read back from database
	 */
	public TowedArray3DDataUnit(int Nstation) {
		super(0,0,0,0);
		setLocalisation(towedArray3DLocalization = new TowedArray3DLocalization(this, 0, 0));
		this.angle=new double[Nstation+1];
		this.range=new double[Nstation+1];
		this.heading=new double[Nstation+1];
		this.tilt=new double[Nstation+1];
		for (int i=0;i<=Nstation; i++){
			this.angle[i]=0;
			this.heading[i]=0;
			this.tilt[i]=0;
			this.range[i]=0;
		}
		this.hasAngle=false;
		this.hasDepth=false;
		this.hasRange=false;
		this.hasLatLong=false;
	}


	public double[] getAngle() {
		return angle;
	}

	public void setAngles(double[] angle) {
		this.angle = angle;
	}

	public void setAngle(double angle, int i) {
		this.angle[i] = angle;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public boolean HasLatLong(){
		return hasLatLong;
	}
	
	public boolean isHasAngles() {
		return hasAngle;
	}

	public void setHasAngles(boolean hasAngle) {
		this.hasAngle = hasAngle;
	}

	public boolean isHasAngleAmbiguity() {
		return hasAngleAmbiguity;
	}

	public void setHasAngleAmbiguity(boolean hasAngleAmbiguity) {
		this.hasAngleAmbiguity = hasAngleAmbiguity;
	}

	public boolean isHasDepth() {
		return hasDepth;
	}

	public void setHasDepth(boolean hasDepth) {
		this.hasDepth = hasDepth;
	}

	public boolean isHasRanges() {
		return hasRange;
	}

	public void setHasRanges(boolean hasRange) {
		this.hasRange = hasRange;
	}

	public double[] getHeadings() {
		return heading;
	}

	public void setHeadings(double[] heading) {
		this.heading = heading;
	}

	public void setHeadings(double heading, int i) {
		this.heading[i] = heading;
	}

	public double[] getRanges() {
		return range;
	}

	public void setRanges(double[] range) {
		this.range = range;
	}

	public void setRanges(double range, int i) {
		this.range[i] = range;
	}

	public double[] getTilts() {
		return tilt;
	}

	public void setTilts(double[] tilt) {
		this.tilt = tilt;
	}
	public void setTilts(double tilt,int i) {
		this.tilt[i] = tilt;
	}

	public void setLatLong() {
		LatLong centreLatLong=getOriginLatLong(true);
		double referenceBearing=localisation.getBearingReference();
		latlong=(centreLatLong.travelDistanceMeters((referenceBearing + angle[0]) * 180. / Math.PI, range[0]));
		hasLatLong=true;
	}
	
	public LatLong getLatlong(int iSide) {
		return latlong;
	}
//	@Override
//	public GpsData getOriginLatLong(boolean recalculate) {
//		if (oLL == null || recalculate) {
//			calcOandAngles();
//		}
//		return oLL;
//	}

//	public void calcOandAngles() {
//
//		ArrayManager arrayManager = ArrayManager.getArrayManager();
//		PamArray array = arrayManager.getCurrentArray();
//		HydrophoneLocator hydrophoneLocator = array.getHydrophoneLocator();
//		// turn channel numbers into hydrophone numbers.
//		// to do this we need the parameters from the acquisition process !
//		// if this is not available, then assume 1:1 mapping and get on with it.
//		LatLong phoneLatLong = hydrophoneLocator.getPhoneLatLong(getTimeMilliseconds(), 0);
//		oLL = new GpsData(phoneLatLong.getLatitude() , phoneLatLong.getLongitude(), 0, getTimeMilliseconds());
//
//	}
	/**
	 * USed when no hydrophone information is specified to get the nearest ships GPS position. 
	 * @param pamDataUnit
	 * @return
	 */
	@Override
	protected GpsData getGpsPosition() {
		PamDataBlock<GpsDataUnit> gpsDataBlock = PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
		if (gpsDataBlock == null) return null;
		GpsDataUnit gpsDataUnit =  gpsDataBlock.findDataUnit(getTimeMilliseconds(), 0);
		if (gpsDataUnit == null) return null;
		return gpsDataUnit.getGpsData();
	}

	

	
}
