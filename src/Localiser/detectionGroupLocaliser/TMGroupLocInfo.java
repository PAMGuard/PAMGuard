package Localiser.detectionGroupLocaliser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import Array.ArrayManager;
import Array.PamArray;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;


/**
 * Takes a PamDetection with sub detections and has functions to generate various types of information for a localiser.
 * Mainly useful for target motion localisation, but may be applied to other scenarios, e.g. a group of DIFAR buoys. 
 * <p>
 * <b>Information generated</b>
 * <br>
 * Time Delays - time delays of all detections. Visualised as a gigantic array 
 * where each detection represents a different set of hydrophones.
 * <br>
 * Hydrophone positions (Cartesian and latitude, longitude)
 * <br>
 * Vectors and bearings.  

 * @author Jamie Macaulay 
 *
 */
public class TMGroupLocInfo implements GroupLocInfo {
	
	/**
	 * The time of this detection.
	 */
	private Long timeMillis;
	
	/**
	 * Time delays in seconds. 
	 */
	private ArrayList<ArrayList<Double>> timeDelaysAll;
	
	/**
	 * The likely error in the time delay measurements in seconds. 
	 */
	private ArrayList<ArrayList<Double>> timeDelaysErrorsAll;
	
	/**
	 * Array Information
	 */
	private ArrayManager arrayManager = ArrayManager.getArrayManager();
	
	/**
	 * The current array 
	 */
	private PamArray currentArray;
	
	/**
	 * The number of channels. 
	 */
//	int nChannels;
	
	/**
	 * Speed of sound in sea water
	 */
	double speedOfSound;
	
	/**
	 * Hydrophone positions relative to origin point (x,y,z)
	 */
	private ArrayList<ArrayList<double[]>> hydrophonePos;

	/**
	 * The vector to the center of the array with respect to the origin point. 
	 */
	private PamVector[] detectionOrigins;
	
	/**
	 * We need to know the position and depth of the array but we also must know the
	 * rotation of the array. It would be nice to store the rotation as athree
	 * element vector, however a unit vector cannot describe fully the rotation of
	 * the array. e.g say we have the unit vector (-0.71 0.0 0.71), this would
	 * describe a heading of 270 and pitch of 45 degrees. However this unit vector
	 * tells us nothing about the roll (tilt) of the array. This is because we had
	 * to start off with a unit vector (0,1,0) to rotate to begin with. We can
	 * transform co-ordinate systems all we want but a unit vector will only ever
	 * tell us two Euler angles. Hence we have to store euler angles either as
	 * angles or, as we have done here we can use a 4 element quaternion.
	 */
	private PamQuaternion[] detectionEulerAngles;

	/**
	 * The vectors which point towards the source location, can be a 2D
	 * approximation of a hyperbolic cone (2+ linear element array), a degenerate
	 * vector (3+ element array on a plane) or a single vector pointing towards the
	 * animal in 3D (4+ volumetric array).
	 */
	private PamVector[][] rawRealWorldVectors;

	/**
	 * Vectors to use for localisation. Each row corresponds to an ambiguity. If
	 * there is no predefined ambiguity (the ambiguity might be determined by the
	 * localisation algortihm itself) then the length of ArrayList<PamVector[]> is
	 * 1.
	 */
	private ArrayList<PamVector[]> usedWorldVectors;
	
	
	/**
	 * Vector errors to use for localisation. Each row corresponds to an ambiguity.
	 * If there is no predefined ambiguity (the ambiguity might be determined by the
	 * localisation algortihm itself) then the length of ArrayList<PamVector[]> is
	 * 1.
	 */
	private ArrayList<PamVector[]> usedWorldVectorErrors;
	
	/**
	 * The nuymber of ambiguites to expect for expect for a localisation which attempts to solve the 2D problem
	 */
	private int ambiguityCount2D=1; 

	/**
	 * The nuymber of ambiguites to expect for expect for a localisation which attempts to solve the 3D problem
	 */
	private int ambiguityCount3D=1; 

	
	/**
	 * The parent data unit.  Contains all sub detections 
	 */
	private SuperDetection parentDetection;
	
	/**
	 * A list of sub detections used in the localisaion which may only 
	 * be a subset of what was in the original parentDetection. 
	 */
	private Vector<PamDataUnit> subDetectionList;

	
	public TMGroupLocInfo(SuperDetection pamDataUnit, DetectionGroupOptions detectionGroupOptions){
		this.parentDetection = pamDataUnit;
		/**
		 * Since a lot of the data extraction from the data unit doesn't happen until much later
		 * on, it may be necessary to set up a dummy data unit in order to limit the number 
		 * of points (clicks) included in the localisation. 
		 */
		copySubDetections(pamDataUnit, detectionGroupOptions);
	  	currentArray = arrayManager.getCurrentArray();
//	  	nChannels=PamUtils.getNumChannels(pamDataUnit.getParentDataBlock().getChannelMap());
		speedOfSound = currentArray.getSpeedOfSound();
	}
	
	
	private void copySubDetections(SuperDetection parentDataUnit, DetectionGroupOptions detectionGroupOptions) {
		ArrayList<PamDataUnit> subDets = parentDataUnit.getSubDetections();
		int totalUnits = subDets.size();
		int keptUnits = totalUnits;
		if (detectionGroupOptions != null) {
			if (detectionGroupOptions.getMaxLocalisationPoints() == 0 ||			
					detectionGroupOptions.getMaxLocalisationPoints() < parentDataUnit.getSubDetectionsCount()) {
				keptUnits = detectionGroupOptions.getMaxLocalisationPoints();
			}
		}
		subDetectionList = new Vector<>(keptUnits);
		float keepRat = (float) (totalUnits-1) / (float) (keptUnits-1);
		for (int i = 0; i < keptUnits; i++) {
			int unitIndex = Math.round(i*keepRat);
			subDetectionList.add(subDets.get(unitIndex));
		}
	}


	@Override
	public SuperDetection getParentDetection() {
		return parentDetection;
	}
	
	/**
	 * Get the number if sub detections
	 * @return the number of sub detections. 
	 */
	public int getDetectionCount(){
		if (subDetectionList==null) return 0;
		return subDetectionList.size();
	}
	
	/**
	 * Calculate the time delays for each pmaDetection.
	 * Time delays are added into a ragged array were each row 
	 * represents one detection and the time delays of that detection follow
	 * the indexM1 and indexM2 convention in AbstractLocalisation
	 * 
	 */
	protected void calculateTimeDelays() {
		
		if (getDetectionCount()==0) return;
		
		timeDelaysAll=new ArrayList<ArrayList<Double>>();
		ArrayList<Double> timeDelays;
		double[] tDs;
		for (int i=0; i< subDetectionList.size(); i++){
			timeDelays=new ArrayList<Double>();
			tDs=subDetectionList.get(i).getLocalisation().getTimeDelays();
			for (int j=0; j<tDs.length; j++){
				timeDelays.add(tDs[j]);
			}
			timeDelaysAll.add(timeDelays);
		}
		
	}
	
	
	/**
	 * Calculate the time delay errors for each pamDetection.
	 * The time delay errors are in the same format as time delays.  
	 * 
	 */
	protected void calculateTimeDelayErrors() {

		if (getDetectionCount()==0) return;
		
		timeDelaysErrorsAll=new ArrayList<ArrayList<Double>>();
		ArrayList<Double> timeDelays;
		double[] tDs;
		for (int i=0; i< subDetectionList.size(); i++){
			timeDelays=new ArrayList<Double>();
			tDs=subDetectionList.get(i).getLocalisation().getTimeDelayErrors();
			for (int j=0; j<tDs.length; j++){
				timeDelays.add(tDs[j]);
			}
			timeDelaysErrorsAll.add(timeDelays);
		}
	
	}
	
//	/**
//	 * Calculates the array heading for each pamDetection
//	 */
//	protected void calcDetectionHeadings(){
//		
//		int nSunDetections=getNDetections();
//		PamVector[] subDetectionHeadings = new PamVector[nSunDetections];
//		
//		AbstractLocalisation loc; 
//		double arrayAngle;
//		
//		for (int i = 0; i < nSunDetections; i++) {
//			loc=currentDetections.get(i).getLocalisation();
//			if (loc==null) continue;
//			arrayAngle = loc.getBearingReference();
//			
//			//System.out.println("Bearing Reference: "+Math.toDegrees(loc.getBearingReference()));
//			subDetectionHeadings[i] = new PamVector(Math.cos(Math.PI/2-arrayAngle), Math.sin(Math.PI/2-arrayAngle), 0);
//		}
//		
//		detectionHeadings=subDetectionHeadings;
//	}
	

	/**
	 * Gets the origins of each detection. This is the centroid of the group that the pamDetection belongs to. The detection origins may 
	 * also contain information on heading, pitch and roll. 
	 */
	protected void calcDetectionOrigins(){
		//System.out.println("AbstractDetectionMatch.calcDetexctionOrigins: ");
		int nSubDetections=getDetectionCount();
		
		if (nSubDetections == 0) {
			return;
		}
		PamDataUnit pd = subDetectionList.get(0);
		if (pd == null) {
			return;
		}
		
		LatLong plotOrigin = pd.getOriginLatLong(false);
		if (plotOrigin == null) {
			return;
		}
		
		PamVector[] subDetectionOrigins=new PamVector[nSubDetections];
		PamQuaternion[] detectionEulerAngles=new PamQuaternion[nSubDetections];
		AbstractLocalisation localisation;
		GpsData detOrigin;
		
		for (int i = 0; i < nSubDetections; i++) {
			pd = subDetectionList.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			detOrigin = pd.getOriginLatLong(true);
			if (detOrigin==null){
				subDetectionOrigins[i]=new PamVector(0,0,0);
				detectionEulerAngles[i]=new PamQuaternion(1,0,0,0); //pointing straight ahead...note must square to make one. 
			}
			else{
				subDetectionOrigins[i]=latLongToMetres(detOrigin);
//				TODO- here we convert from degrees (which euler angles should be not stored as) to radians- eventually when we fix this in PAMGUARD this will need changed. 
				PamQuaternion eulerAngles=new PamQuaternion(Math.toRadians(detOrigin.getHeading()), Math.toRadians(detOrigin.getPitch()), Math.toRadians(detOrigin.getRoll()));
				//System.out.println("TMGroupLocInfo: Headings: "+detOrigin.getHeading()+ " Pitch: "+detOrigin.getPitch()+" Roll: "+detOrigin.getRoll()+ " Height: "+detOrigin.getHeight());
				detectionEulerAngles[i]=eulerAngles;
			}
		}
		
		this.detectionEulerAngles=detectionEulerAngles;
		this.detectionOrigins=subDetectionOrigins;
	}	
	
	/**
	 * Get the rotation vector (a quaternion) for each detection
	 * @param force a recalculation of the angles. 
	 * @return the quaternion for each detection - usually represents the rotation of the array 
	 */
	public PamQuaternion[] getRotationVectors(boolean recalc) {
		if (detectionEulerAngles==null || recalc) calcDetectionOrigins();
		return detectionEulerAngles;
	}
	
	/**
	 * Get the time delays for each pmaDetection.
	 * Time delays are added into a ragged array were each row 
	 * represents one detection and the time delays of that detection follow
	 * the indexM1 and indexM2 convention in AbstractLocalisation
	 * @param force a recalculation of the time delays
	 * @return array of time delays for each detections
	 * 
	 */
	public ArrayList<ArrayList<Double>> getTimeDelays(boolean recalc) {
		if (timeDelaysAll==null || recalc){
			calculateTimeDelays();
		}
		return timeDelaysAll;
	}
	
	/**
	 * Get the time delays for each pmaDetection.
	 * Time delays are added into a ragged array were each row 
	 * represents one detection and the time delays of that detection follow
	 * the indexM1 and indexM2 convention in AbstractLocalisation
	 * @param force a recalculation of the time delays
	 * @return array of time delays for each detections
	 */
	public ArrayList<ArrayList<Double>> getTimeDelayErrors(boolean recalc) {
		if (timeDelaysErrorsAll==null || recalc){
			calculateTimeDelayErrors();
		}
		return timeDelaysErrorsAll;
	}
	

	/**
	 * Function to calculate the position of all hydrophones in a Cartesian (x,y,z) co-ordinate system. 
	 * To do this we need GPS Co-Ordinates, the heading of the array and dimensions of the array. 
	 */
	protected void calcHydrophonePositions(){
		
		hydrophonePos=new ArrayList<ArrayList<double[]>>();
		//check that the detection origins have been calculated.
		if (detectionOrigins==null) calcDetectionOrigins();
		if (detectionOrigins==null) return;

		/* 
		 * Now loop over every hydrophone in every sub-detection to rotate the positions and essentially 
		 * convert into one large multi-element array in Cartesian space. 
		 * The number of elements in this array will be nSubDFetections*numberOfHydrophones 
		 */
		double[] hPos;
		double[] hErrors;
		int[] hydrophoneMap;
		ArrayList<double[]> hydrophonePositions;
		ArrayList<double[]> hydrophonePositionErrors;
		double[] arrayOriginPt;
		PamVector relElement;
		
		for (int i=0; i<getDetectionCount(); i++  ){
			//TODO- must be sorted in terms of channel grouping. 
			/*
			 * We must calculate the position of the hydrophone for each detection as 
			 * someone could have imported hydrophone positions which can change for different detections. 
			 */
			hydrophoneMap=PamUtils.getChannelArray(subDetectionList.get(i).getChannelBitmap());
			hydrophonePositions=new ArrayList<double[]>();
			hydrophonePositionErrors=new ArrayList<double[]>();
			
			//get  array of the hydrophone positions
			for (int j=0; j<hydrophoneMap.length;j++){
				hPos=currentArray.getHydrophoneCoordinates(hydrophoneMap[j],  subDetectionList.get(i).getTimeMilliseconds());
				hErrors=currentArray.getHydrophoneCoordinateErrors(hydrophoneMap[j],subDetectionList.get(i).getTimeMilliseconds());
				hydrophonePositions.add(hPos);
				hydrophonePositionErrors.add(hErrors);
			}
			
			/*
			 * Find the origin point of the array. In the case of a paired towed array this 
			 * will be halfway between the elements but in the case of more complicated towed array 
			 * this will be the average positions of all the hydrophones
			 */
			//so this needs to be sorted in terms of channnel grouping. 
			arrayOriginPt=new double[3]; 
			for (int j=0; j<hydrophoneMap.length; j++){
				for (int k=0; k<arrayOriginPt.length; k++){
					arrayOriginPt[k]+=hydrophonePositions.get(j)[k];
					if (j==hydrophoneMap.length-1) arrayOriginPt[k]=arrayOriginPt[k]/hydrophoneMap.length;
				}
			}
			
			//work out the change in position for each element due to heading, pitch and roll;
			ArrayList<double[]> hRotatedPositions=new ArrayList<double[]>();

			double[] hPos3dNew;
			//Quaternion describing the direction of the hydrophone array.
			PamQuaternion direction;
			for (int j=0; j<hydrophoneMap.length; j++){
				
				direction=detectionEulerAngles[i];
				if (direction==null) direction=new PamQuaternion(1,0,0,0);
				
				//work out the position of the element if the origin is assumed to by the centroid of the hydrophone array
				relElement=new PamVector(hydrophonePositions.get(j)[0]-arrayOriginPt[0],hydrophonePositions.get(j)[1]-arrayOriginPt[1],hydrophonePositions.get(j)[2]-arrayOriginPt[2] );
				
				//Rotate by heading pitch and roll. 
				relElement=PamVector.rotateVector(relElement, direction);
				
//				System.out.println(" GroupLocInfo: Quaternion: Heading: "+
//				Math.toDegrees(direction.toHeading())+" Pitch: "+Math.toDegrees(direction.toPitch())+" "
//						+ "Roll: "+Math.toDegrees(direction.toRoll())+" depth: "+detectionOrigins[i].getVector()[2]);
				
				//now work out the new position of the element
				hPos3dNew=new double[3];
				for (int k=0; k<hPos3dNew.length ; k++){
					hPos3dNew[k]=relElement.getVector()[k]+detectionOrigins[i].getVector()[k];
				}
				
				hRotatedPositions.add(hPos3dNew);
				
			}
			
			this.hydrophonePos.add(hRotatedPositions);
		}

	}
	
	/**
	 * Calculate the real world vectors for all detections and presents in an array. 
	 * The real word vectors are relative to the earth surface. Thus they are the bearings vectors 
	 * from an array, rotated by the true heading, pitch and roll of the array.  
	 */
	protected void calculateWorldVectors() {	
		int nSubDetections=getDetectionCount();
		rawRealWorldVectors = new PamVector[nSubDetections][];
		PamVector[] v;
		PamDataUnit pd;
		AbstractLocalisation localisation;
		for (int i = 0; i < nSubDetections; i++) {
			pd = subDetectionList.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			v = localisation.getRealWorldVectors();

			rawRealWorldVectors[i] = v;
		}
	}
	
	
	/**
	 * Get the real world vectors for all detections and presents in an array. 
	 * The real word vectors are relative to the earth surface. Thus they are the bearings vectors 
	 * from an array, rotated by the true heading, pitch and roll of the array. 
	 * @param recalc - true to force recalculation of the vectors 
	 * @return 
	 */
	public PamVector[][] getRawWorldVectors(boolean recalc) {
		if (rawRealWorldVectors==null || recalc) calculateWorldVectors();
		return rawRealWorldVectors;
	}
	
	


	@Override
	public synchronized ArrayList<ArrayList<double[]>> getHydrophonePos() {
		if (this.hydrophonePos==null){
			calcHydrophonePositions();
		}
		return hydrophonePos;
	}
	
	@Override
	public PamVector[] getOrigins() {
		if (detectionOrigins==null) calcDetectionOrigins();
		return detectionOrigins;
	}

	@Override
	public LatLong getGPSReference() {
		if (getDetectionCount()<=0) return null; 
		return subDetectionList.get(0).getOriginLatLong(false);
	}
	

	
	
	public LatLong metresToLatLong(PamVector pt) {
		if (getGPSReference() == null) {
			return null;
		}
		LatLong ll = getGPSReference().addDistanceMeters(pt.getElement(0), pt.getElement(1));
		ll.setHeight(pt.getElement(2));
		return ll;
	}

	
	public PamVector latLongToMetres(LatLong ll) {
		PamVector v = new PamVector();
		if (getGPSReference() == null) {
			return v;
		}
		v.setElement(0, getGPSReference().distanceToMetresX(ll));
		v.setElement(1, getGPSReference().distanceToMetresY(ll));
		v.setElement(2, ll.getHeight());

		return v;
	}
	
	/**
	 * Get a bitmnap of reference hydrophones for the group
	 * @return a bitmap of the reference hydrophones 
	 */
	public int getReferenceHydrophones(){
		int referenceHydrophones=0; 
		for (int i=0; i<getDetectionCount(); i++){
			referenceHydrophones |= subDetectionList.get(i).getLocalisation().getReferenceHydrophones();
		}
		return referenceHydrophones;
	}

	/**
	 * Return average time for the target motion event. 
	 */
	public Long getTimeMillis(){
		if (timeMillis==null){
			long time=0; 
			for (int i=0; i<getDetectionCount(); i++){
				time+=subDetectionList.get(i).getTimeMilliseconds();
			}
			timeMillis=time/getDetectionCount();
		}
		return timeMillis;
	}
	
	/**
	 * Convenince codce for extracting the heading from an array of  quaternions. 
	 * @param eulerAngles - an array of rotation angles. 
	 * @return an array of vectors which contain vectors representing only the heading. 
	 */
	@Deprecated
	public static PamVector[] getHeadingVectors(PamQuaternion[] eulerAngles){
		PamVector[] detectionHeadings=new PamVector[eulerAngles.length];
		for (int i=0; i<eulerAngles.length; i++){
				detectionHeadings[i] = new PamVector(Math.cos(Math.PI/2-eulerAngles[i].toHeading()), Math.sin(Math.PI/2-eulerAngles[0].toHeading()), 0);
		}
		return detectionHeadings;
	}
	
//	/**
//	 * Check which streamers are present in all sub detections
//	 * @return array for streamer indexes
//	 */
//	public static ArrayList<Integer> checkStreamers(PamDetection parentDetection){
//		
//		ArrayList<Integer> streamerIndices=new ArrayList<Integer>();
//		int[] channelArray;
//		for (int i=0; i<parentDetection.getSubDetectionsCount(); i++){
//			channelArray=PamUtils.getChannelArray(parentDetection.getSubDetection(i).getChannelBitmap());
//			if (channelArray==null) continue; 
//			
//			for (int j=0; j<channelArray.length; j++){
//				int streamerIndex=ArrayManager.getArrayManager().getCurrentArray().getStreamerForPhone(channelArray[j]);
//				if (!streamerIndices.contains(streamerIndex)) streamerIndices.add(streamerIndex);
//			}
//			
//		}
//		
//		return streamerIndices;
//		
//	}
	
	/**
	 * Calculate the beam latitude and longitude. The beam latitude and longitude is  the point on the GPS path which is closest to the 
	 * localised position of an animal.
	 * @param localised- localisation result to find beam latitude and longitude for. 
	 * @return the beam latitude and longitude. 
	 */
	long millisOver=900000;
	
	@Override
	public GpsData getBeamLatLong(LatLong localised){
		
		GPSDataBlock gpsDataBlock = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
	
		if (gpsDataBlock==null){
//			System.err.println("TMGrioupLocInfo: No GPS data to get beam lat long:");
			// return the central hydrophone position. 
			return new GpsData(localised);
		}
		
		long startTime=subDetectionList.get(0).getTimeMilliseconds()-millisOver;
		long endTime=subDetectionList.get(subDetectionList.size()-1).getTimeMilliseconds()+millisOver;
		
		//find gps units to search through
		ArrayList<GpsDataUnit> gpsUnits=gpsDataBlock.findUnitsinInterval(startTime, endTime);
		
		if (gpsUnits==null || gpsUnits.size()==0){
//			System.err.println("TargetMotionModule: No GPS data to get beam lat long:");
			return new GpsData(localised);
		}
		
		//what if the locisation is ahead of the ship and there's no GPS data find an a beam lat long. ?
		//TODO
		
		
		//find the closest gps data unit to the localisation
		double min=Double.MAX_VALUE;
		double dist;
		GpsData beamLatLong=null;
		for (int i=0; i<gpsUnits.size(); i++){
			dist=gpsUnits.get(i).getGpsData().distanceToMetres(localised);
			if (dist<min){
//				System.out.println("Dist: "+dist+" i: "+i+ " of "+gpsUnits.size());
				min=dist;
				beamLatLong=gpsUnits.get(i).getGpsData();
			}
		}
				
		return beamLatLong; 
			
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelays() {
		return getTimeDelays(true);
	}

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelayErrors() {
		return getTimeDelayErrors(true);
	}

	@Override
	public PamVector[] getWorldVectors(int side) {
		if (usedWorldVectors==null) setupTMBearings(); 
		if (usedWorldVectors.size() < side+1 ) return null; 
		return usedWorldVectors.get(side);
	}


	@Override
	public PamVector[] getWorldVectorErrors(int side) {
		if (usedWorldVectorErrors==null) 
			setupTMBearings(); 
		try {
			if (usedWorldVectorErrors.size() < side-1) {
				return null; 
			}
			return usedWorldVectorErrors.get(side);
		}
		catch (IndexOutOfBoundsException e) {
			// some other thread must be stuffing the usedWorldVectors up somehow 
			// or this would never ahppen
			return null;
		}
	}
	
	@Override
	public int getAmbiguityCount(int dim) {

		if (usedWorldVectors==null) setupTMBearings(); 
		if (usedWorldVectors==null ) return -1; 
		
		if (dim==2){
			return this.ambiguityCount2D;
		}
		else{
			return this.ambiguityCount3D;
		}
	}

	@Override
	public PamQuaternion[] getRotationVectors() {
		return getRotationVectors(true);
	}

	
	 /**
	  * Calculate the beam time. This is not the time that the vessel was at the beam latitude and longitude but the time the 
	  * streamer passed that point. Note that if there are multiple streamers passing the point  the streamer lengths are averaged 
	  * and used as our beam latitude and longitude. We use the model for each streamer to to work out when it passed the 
	  * beam latitude and longitude point. 
	  * @param locLatLong- the latitude and longitude of the localisation. 
	  * @return the time at which the hydrophone array passed the beam latitude and longitude point. 
	 */
	public long getBeamTime(LatLong locLatLong){
		GpsData data= getBeamLatLong(locLatLong);
		if (data == null) {
			return 0;
		}
		return data.getTimeInMillis();
//		try{
//			//calculate minimum speed- halve the current speed:
//			double minSpeed=0.5*beamLatLong.getSpeed();
//			//calculate the max streamer length
//			ArrayList<Integer> streamerIndex= checkStreamers(parentDetection);
//
//			double maxLength=-Double.MAX_VALUE;
//			double length;
//			for (int i=0; i<streamerIndex.size(); i++){
//
//				length = ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamerIndex.get(i), beamLatLong.getTimeInMillis()).getY();
//				if (length>maxLength){
//					maxLength=length; 
//				}
//			}
//			//work out the time we need to look back in millis
//			long time=(long) ((maxLength/minSpeed)*1000.0);
//			long millisStart=beamLatLong.getTimeInMillis();
//			long millisEnd=beamLatLong.getTimeInMillis();
//			//need to make sure we set times right if the array is in front of the boat (unlikely but could happen)
//			if (time>0) millisStart=millisStart-time;
//			else millisEnd=millisEnd-time;
//
//			//calculate the streamer path between the start and end time. 
//			ArrayList<ArrayList<GpsData>> streamerPos=TargetMotionControl.calcStreamerPath( this, millisStart,  millisEnd, null);
//
//			double dist;
//			double minDist;
//			GpsData pos;
//			long beamTime = 0; 
//			for (int i=0; i<streamerPos.size(); i++){
//				minDist=Double.MAX_VALUE;
//				pos=null;
//				for (int j=0; j<streamerPos.get(i).size(); j++){
//					dist=beamLatLong.distanceToMetres(streamerPos.get(i).get(j));
//					if (dist<minDist){
//						minDist=dist; 		
//						pos=streamerPos.get(i).get(j);
//					}
//				}
//				beamTime+=pos.getTimeInMillis();
//			}
//
//			beamTime=beamTime/streamerPos.size(); 
//
//			return beamTime; 
//		}
//		catch (Exception e){
//			e.printStackTrace();
//			return 0;
//		}
	}

	 
	 /**
	  * If a target motion localisation is occurring from a stereo array there maybe two possible results. 
	  * For some localisation algorithms the bearings will need to be sorted before localisation. This functions separates the bearings 
	  * belonging to different ambiguities. 
	  * @return an ArrayList<PamVector[]> with each element comprising of an array of bearings for each ambiguity. 
	  */
	 public void setupTMBearings(){
		 
		//array to hold separated vectors 
		ArrayList<PamVector[]> vectorSeparated=new ArrayList<PamVector[]>(); 
		ArrayList<PamVector[]> vectorErrorsSeparated=new ArrayList<PamVector[]>(); 

		int  nSubDetections = this.getDetectionCount();
		 //TODO- change algorithm to cope with Euler angles rather than just headings
		PamVector[][] worldVectors = this.getRawWorldVectors(true);
		 //subDetectionAngles = new double[nSubDetections];
		double[][] subDetectionAngleErrors = new double[nSubDetections][];
		boolean[] faultPoints = new boolean[nSubDetections];

		
		 AbstractLocalisation localisation;
		 double angle;
//		 double angleError;
		 double[] angles;
		 double[] angleErrors;
		 //double rotationAngle = targetMotionInformation.getReferenceAngle();
		 int minVectors = Integer.MAX_VALUE;
		 int maxVectors = 0;
		 int totalVectors=0; 
		 int nVectors;
		 
		 /**
		  * Now split the vectors up.
		  */
		 
		 boolean linear = true; //only linear
		 boolean volumtric=false; //at least one volumetric array 
		 int[] singleSideCount = new int[2];
		 for (int i = 0; i < nSubDetections; i++) {
			 
			 localisation = getParentDetection().getSubDetection(i).getLocalisation();
			 if (localisation == null) {
				 continue;
			 }
			 angles = localisation.getAngles();
			 angleErrors = localisation.getAngleErrors();
			 
			 //angleError = localisation.get
			 if (angles == null || angles.length < 1 || worldVectors[i].length == 0) {
				 faultPoints[i] = true;
				 continue;
			 }
			 faultPoints[i] = false;
			 angle = angles[0];
//			 PamVector[] wV = localisation.getWorldVectors();
//			 for (int w = 0; w < wV.length; w++) {
//				 double angle1 = Math.atan2(wV[w].getCoordinate(1), wV[w].getCoordinate(0));
//				 System.out.printf("Data angle subDet %d wv %d  %3.1f or %3.1f from world vector\n", i, w, Math.toDegrees(angle), Math.toDegrees(angle1));
//			 }
			 if (localisation.getSubArrayType() == ArrayManager.ARRAY_TYPE_VOLUME) {
				 /*
				  * It should be an unambiguous angle. Work out which side it is 
				  */
				 double cosAng = Math.cos(angle);
				 if (cosAng > 0) {
					 singleSideCount[1]++;
				 }
				 else if (cosAng < 0) {
					 singleSideCount[0]++;
				 }
			 }
//			 
//			 if (angleErrors != null && angleErrors.length >= 1) {
//				 angleError = angleErrors[0]; 
//			 }
//			 else {
//				 angleError = Double.NaN;
//			 }
			 if (angleErrors == null || angleErrors.length < 1) {
				 angleErrors = new double[1];
				 angleErrors[0] = Double.NaN;
			 }
			 
			 for (int e = 0; e < angleErrors.length; e++) {
			 if (Double.isNaN(angleErrors[e])){
				 angleErrors[e] = 3 * Math.PI / 180 / Math.pow(Math.sin(angle), 2);
				 //angleError = 2. * Math.PI/180; // use a one degree error
			 }
			 }
			 //angleError = Math.max(angleError, 1.e-6); //FIXME - is this supposed to be here?
			 
			 subDetectionAngleErrors[i] = angleErrors;
			 nVectors = worldVectors[i].length;
			 if (nVectors==1){
				 //there is at least one measurement whihc is volumetric here. 
				 volumtric=true; 
				 linear=false;
			 }
			 else {
				 for (int j=0; j<worldVectors[i].length; j++){
					 //if ANY measurements are not a cone then purely linear array is false.
					 if (!worldVectors[i][j].isCone()){
//						 System.out.println("TMGroupLocInfo: Is cone: " + worldVectors[i][j].isCone());
						 linear=false; 
					 }
				 }
			 }

			 minVectors = Math.min(minVectors, nVectors);
			 maxVectors = Math.max(maxVectors, nVectors);
			 totalVectors += nVectors;
		 }
		 
		 if (totalVectors == 0) {
			 return;
		 }
		 
		 //now create the separate arrays for different ambiguities. 
		 /**
		  * 
		  * Therfe are potentially a few scenarious here:
		  * 1) Only paired array data. This means, if the the trackline moves enough there maybe one ambiguity. If the track line
		  * does not move enough then there is no 3D solution.
		  * ---> 2D: 2 solutions
		  * ---> 3D: 1 solution but may be impossible to solve. 
		  * 
		  * 2) Plan array or planar array +paired data. There may be two 3D solutions or just one 3D solution depending on trackline movement and the orientation of the plane. 
		  * ---> 2D and 3D: 2 solutions
		  * 
		  * 3) Tetrahedral array or tetrahedral +planar and/or + paired. There is only one solution. 
		  * ---> 2D and 3D: 1 solution.
		  * DG 19/2/2018. Not quite so, if there is a mix of volumetric & plane, then there are
		  * two possible combinations to try, but only one actual solution. 
		  */
		 PamVector[] usedWorldVectors;  
		 PamVector[] usedWorldVectorError;  
		 int n;
		 
		 for (int side = 0; side < maxVectors; side++) {
			 //create a new array to store detection in. 
			 usedWorldVectors= new PamVector[nSubDetections];
			 usedWorldVectorError= new PamVector[nSubDetections];
			 for (int i = 0; i < nSubDetections; i++) {
				 if (worldVectors[i] == null || worldVectors[i].length == 0) {
					 continue;
				 };
				 
				 n = worldVectors[i].length;
				 
				 if (side >= n) {
					 //still need to add the vector into the mix. 
					 usedWorldVectors[i] = worldVectors[i][0];
				 }
				 else usedWorldVectors[i] = worldVectors[i][side];

				 /**
				  * Want to inform localisers if they're using a vector which really is not a vector at all 
				  * but a hyperbolic surface. 
				  */
				 
				 //FIXME- I don't like this. Goes from angle to vector then back to angle...inefficient
				 /*
				  * The call to fromHeadAndSlant only ever made a unit vector, whatever the size of the original 
				  * error - which was daft. I think that what we want is the error as a vector, but the units are still 
				  * all in radians. Have done as much, the number of occupied elements in the vector being the 
				  * same as the number of errors (1 or 2 depending on type of array).  
				  */
//				 usedWorldVectorError[i] = PamVector.fromHeadAndSlant( subDetectionAngleErrors[i],  subDetectionAngleErrors[i]);
				 usedWorldVectorError[i] = new PamVector(Arrays.copyOf(subDetectionAngleErrors[i], 3));
			 }
			 vectorSeparated.add(usedWorldVectors);
			 vectorErrorsSeparated.add(usedWorldVectorError);

		 }
		 
		 //now work out ambiguities
		 if (linear){
			 //linear array only. 
			 this.ambiguityCount2D=2; 
			 this.ambiguityCount3D=1; //will have only one 3D result if trackline is wonky enough- otherwise problem is not solveable in 3D. 
		 }
		 else if (volumtric){
			 //at least one measurment from volumetric array.
			 if (singleSideCount[0] > 0 && singleSideCount[1] > 0) {
				 this.ambiguityCount2D = 2;
			 }
			 else {
				 this.ambiguityCount2D = 1;
				 if (singleSideCount[1] > singleSideCount[0] && vectorSeparated.size() > 1) {
					 // swap the two halves round - honest !
					 PamVector[] vecs2 = vectorSeparated.remove(0);
					 vectorSeparated.add(vecs2);
				 }
			 }
			 this.ambiguityCount3D=maxVectors>1 ? 2: 1; 
		 }
		 else {
			 //no volumetric data but at least one measurment from a planar array. 
			 this.ambiguityCount2D=2; 
			 this.ambiguityCount3D=2; 
		 }
		 
		 //System.out.println("Ambiguity 3D: " + ambiguityCount3D + " 2D: " +ambiguityCount2D);
		 
		 this.usedWorldVectors=vectorSeparated; 
		 this.usedWorldVectorErrors=vectorErrorsSeparated; 
		 
	 }

	
//	/**
//	 * Calculate the beam time. This is not the time that the vessel was at the beam lat long but the time the streamer passed that point. Note that if there are 
//	 * multiple streamers passing the point  the streamer lengths are averaged and used as our beam lat long. We use the model for each streamer to to work out when it passed the 
//	 * beam lat long point. 
//	 * @param beamLatLong- the beam latitude and longitude .
//	 * @return the time at which the hydrophone array passed the beam latitude and longitude point. 
//	 */
//	public long getBeamTime(GpsData beamLatLong){	
//		
//		try{
//		//calculate minimum speed- halve the current speed:
//		double minSpeed=0.5*beamLatLong.getSpeed();
//		//calculate the max streamer length
//		ArrayList<Integer> streamerIndex= checkStreamers(currentDetections);
//		
//		double maxLength=-Double.MAX_VALUE;
//		double length;
//		for (int i=0; i<streamerIndex.size(); i++){
//			
//			length = ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamerIndex.get(i), beamLatLong.getTimeInMillis()).getY();
//			if (length>maxLength){
//				maxLength=length; 
//			}
//		}
//		//work out the time we need to look back in millis
//		long time=(long) ((maxLength/minSpeed)*1000.0);
//		long millisStart=beamLatLong.getTimeInMillis();
//		long millisEnd=beamLatLong.getTimeInMillis();
//		//need to make sure we set times right if the array is in front of the boat (unlikely but could happen)
//		if (time>0) millisStart=millisStart-time;
//		else millisEnd=millisEnd-time;
//
//		//calculate the streamer path between the start and end time. 
//		ArrayList<ArrayList<GpsData>> streamerPos=TargetMotionControl.calcStreamerPath( this, millisStart,  millisEnd, null);
//		
//		double dist;
//		double minDist;
//		GpsData pos;
//		long beamTime = 0; 
//		for (int i=0; i<streamerPos.size(); i++){
//			minDist=Double.MAX_VALUE;
//			pos=null;
//			for (int j=0; j<streamerPos.get(i).size(); j++){
//				dist=beamLatLong.distanceToMetres(streamerPos.get(i).get(j));
//				if (dist<minDist){
//					minDist=dist; 		
//					pos=streamerPos.get(i).get(j);
//				}
//			}
//			beamTime+=pos.getTimeInMillis();
//		}
//		
//		beamTime=beamTime/streamerPos.size(); 
//
//		return beamTime; 
//		}
//		catch (Exception e){
//			e.printStackTrace();
//			return 0;
//		}
//	}

}
