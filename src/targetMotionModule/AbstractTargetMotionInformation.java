package targetMotionModule;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import Array.ArrayManager;
import Array.PamArray;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import targetMotionModule.TMManager.TMInfoWorker;

@SuppressWarnings("rawtypes") 
@Deprecated 
// replaced by GroupDetectionInfo
public class AbstractTargetMotionInformation implements TargetMotionInformation {
	
	/**
	 * List of current detections to be used for target motion localisation. 
	 */
	private ArrayList<PamDataUnit> currentDetections;
	
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
	int nChannels;
	
	/**
	 * Speed of sound in sea water
	 */
	double speedOfSound;
	
	/**
	 * Hydrophone positions relative to origin point (x, y,z)
	 */
	private ArrayList<ArrayList<Point3f>> hydrophonePos;

	/**
	 * The vector to the center of the array with respect to the origin point. 
	 */
	private PamVector[] detectionOrigins;
	
    /**
	 * We need to know the position and depth of the array but we also must know the rotation of the array. It would be nice to store the rotation as athree element vector, however a unit
	 * vector cannot describe fully the rotation of the array. e.g say we have the unit vector (-0.71 0.0 0.71), this would describe a heading of 270 and pitch of 45 degrees. However 
	 * this unit vector tells us nothing about the roll (tilt) of the array. This is because we had to start off with a unit vector (0,1,0) to rotate to begin with. 
	 * We can transform co-ordinate systems all we want but a unit vector will only ever tell us two Euler angles. Hence we have to store euler angles either as angles or, as we have done here 
	 * we can use a 4 element quaternion. 
	 */
	private PamQuaternion[] detectionEulerAngles;

	/**
	 * The vectors which point towards the source location, can be a 2D approximation of a hyperbolic cone (2+ linear element array), a degenerate vector (3+ element array on a plane) or a single vector pointing towards the animal in 3D (4+ volumetric array). 
	 */
	private PamVector[][] realWorldVectors;

	/**
	 * Usually the calculation of target motion information will take place on a seperate thread. This thread is referenced here so progress updates can be sent. 
	 * Note: this thread can be null! Be careful when sub classing. 
	 */
	private TMInfoWorker observerThread;

	
	public AbstractTargetMotionInformation(ArrayList<PamDataUnit> detections, PamDataBlock pamDataBlock){
		currentDetections=detections; 
	  	currentArray = arrayManager.getCurrentArray();
//	  	nChannels=PamUtils.getChannelArray(pamDataBlock.getChannelMap()).length;
	  	nChannels=PamUtils.getChannelArray(pamDataBlock.getSequenceMap()).length;
		speedOfSound = currentArray.getSpeedOfSound();
	}
	
	@Override
	public int getNDetections(){
		if (currentDetections==null) return 0;
		return currentDetections.size();
	}
	
	/**
	 * Calculate the time delays for each pmaDetection. This should cope with different groups which have a different number of hydrophones
	 */
	protected void calculateTimeDelays() {
		
		if (currentDetections==null) return;
		
		timeDelaysAll=new ArrayList<ArrayList<Double>>();
		ArrayList<Double> timeDelays;
		double[] tDs;
		for (int i=0; i< currentDetections.size(); i++){
			timeDelays=new ArrayList<Double>();
			tDs=currentDetections.get(i).getLocalisation().getTimeDelays();
			for (int j=0; j<tDs.length; j++){
				timeDelays.add(tDs[j]);
			}
			timeDelaysAll.add(timeDelays);
		}
		
	}
	
	
	/**
	 * Calculate the time delay errors for each pmaDetection. 
	 */
	protected void calculateTimeDelayErrors() {
		
		if (currentDetections==null) return;
		
		timeDelaysErrorsAll=new ArrayList<ArrayList<Double>>();
		ArrayList<Double> timeDelays;
		double[] tDs;
		for (int i=0; i< currentDetections.size(); i++){
			timeDelays=new ArrayList<Double>();
			tDs=currentDetections.get(i).getLocalisation().getTimeDelayErrors();
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
	 * Gets the origins of each detection. This is the centroid of the group that the pamDetection belongs to. The detection origins may also contain infromation on
	 * heading, pitch and roll. 
	 */
	protected void calcDetectionOrigins(){
//		System.out.println("AbstractDetectionMatch.calcDetexctionOrigins: ");
		int nSubDetections=getNDetections();
		
		if (nSubDetections == 0) {
			return;
		}
		PamDataUnit pd = currentDetections.get(0);
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
			pd = currentDetections.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			detOrigin = pd.getOriginLatLong(false);
			if (detOrigin==null){
				subDetectionOrigins[i]=new PamVector(0,0,0);
				detectionEulerAngles[i]=new PamQuaternion(1,0,0,0); //pointing straight ahead...note must square to make one. 
			}
			else{
				subDetectionOrigins[i]=latLongToMetres(detOrigin);
//				TODO- here we convert from degrees (which euler angles should be not stored as) to radians- eventually when we fix this in PAMGUARD this will need changed. 
				PamQuaternion eulerAngles=new PamQuaternion(Math.toRadians(detOrigin.getHeading()), Math.toRadians(detOrigin.getPitch()), Math.toRadians(detOrigin.getRoll()));
//				System.out.println("AbstractTargetMotionInfo: Headings: "+detOrigin.getHeading()+ " Pitch: "+detOrigin.getPitch()+" Roll: "+detOrigin.getRoll()+ " Height: "+detOrigin.getHeight());
				detectionEulerAngles[i]=eulerAngles;
			}
		}
		
		this.detectionEulerAngles=detectionEulerAngles;
		this.detectionOrigins=subDetectionOrigins;
	}	
	
	@Override
	public PamQuaternion[] getEulerAngles() {
		if (detectionEulerAngles==null) calcDetectionOrigins();
		return detectionEulerAngles;
	}
	

	@Override
	public ArrayList<ArrayList<Double>> getTimeDelays() {
		if (timeDelaysAll==null){
			calculateTimeDelays();
		}
		return timeDelaysAll;
	}
	
	@Override
	public ArrayList<ArrayList<Double>> getTimeDelayErrors() {
		if (timeDelaysErrorsAll==null){
			calculateTimeDelayErrors();
		}
		return timeDelaysErrorsAll;
	}
	
	

	/**
	 * We need to calculate the position of all hydrophones in a cartesian (x,y,z) co-ordinate system. To do this we need GPS Co-Ordinates, the heading of the array and dimensions of the array. 
	 */
	protected void calcHydrophonePositions(){
		
		hydrophonePos=new ArrayList<ArrayList<Point3f>>();
		//check that the detection origins have been calculated.
		if (detectionOrigins==null) calcDetectionOrigins();
		if (detectionOrigins==null) return;

		/*now loop over every hydrophone in every subdetection to rotate the positions and essentially convert into one large multi element array in cartesian space. 
		 * The number of elements in this array will be nSubDFetections*numberOfHydrophones
		 * */
		double[] hPos;
		double[] hErrors;
		int[] hydrophoneMap;
		ArrayList<Point3f> hydrophonePositions;
		ArrayList<Point3f> hydrophonePositionErrors;
		double totalHx;
		double totalHy;
		double totalHz;
		Point3d arrayOriginPt;
		PamVector relElement;
		
		float x, y, z;
		
		for (int i=0; i<currentDetections.size(); i++  ){
			//TODO- must be sorted in terms of channel grouping. 
			/*
			 * We must calculate the position of the hydrophone for each detection as someone could have imported hydrophone positions which can change for different detections. 
			 */
			hydrophoneMap=PamUtils.getChannelArray(currentDetections.get(i).getChannelBitmap());
			hydrophonePositions=new ArrayList<Point3f>();
			hydrophonePositionErrors=new ArrayList<Point3f>();
			
			//get a point3f array of the hydrophone positions
			for (int n=0; n<hydrophoneMap.length;n++){
				hPos=currentArray.getHydrophoneCoordinates(hydrophoneMap[n], currentDetections.get(i).getTimeMilliseconds());
				hErrors=currentArray.getHydrophoneCoordinateErrors(hydrophoneMap[n],currentDetections.get(i).getTimeMilliseconds());
				hydrophonePositions.add(new Point3f((float) hPos[0],(float) hPos[1], (float)hPos[2]));
				hydrophonePositionErrors.add(new Point3f( (float) hErrors[0], (float)  hErrors[1], (float) hErrors[2]));
			}
			
			/*
			 * Find the origin point of the array. In the case of a paired towed array this will be halfway between the elements but in the case of more complicated towed array 
			 * this will be the average positions of all the hydrophones
			 */
			//so this needs to be sorted in terms of channnel grouping. 
			totalHx=0;
			totalHy=0;
			totalHz=0;
			for (int p=0; p<hydrophoneMap.length;p++){
				totalHx+=hydrophonePositions.get(p).getX();
				totalHy+=hydrophonePositions.get(p).getY();
				totalHz+=hydrophonePositions.get(p).getZ();
			}
			
			arrayOriginPt=new Point3d(totalHx/hydrophoneMap.length,totalHy/hydrophoneMap.length,totalHz/hydrophoneMap.length);
			
			ArrayList<Point3f> hRotatedPositions=new ArrayList<Point3f>();

			//System.out.println("HeadingAngle: "+Math.toDegrees(HeadingAngle));
			//work out the change in position for each element due to heading, puitch and roll;
			
			Point3f hPos3dNew;
			//quaternion describing the direction of the hydrophone array.
			PamQuaternion direction;
			for (int k=0; k<hydrophoneMap.length; k++){
				
				direction=detectionEulerAngles[i];
				if (direction==null) direction=new PamQuaternion(1,0,0,0);
				
				//work out the position of the element if the origin is assumed to by the centroid of the hydrophone array
				relElement=new PamVector(hydrophonePositions.get(k).getX()-arrayOriginPt.getX(),hydrophonePositions.get(k).getY()-arrayOriginPt.getY(),hydrophonePositions.get(k).getZ()-arrayOriginPt.getZ() );
				
				//Rotate by heading pitch and roll. 
				relElement=PamVector.rotateVector(relElement, direction);
				Debug.out.println(" AbstractTargetMotionInfo: Quaternion: Heading: "+Math.toDegrees(direction.toHeading())+" Pitch: "+Math.toDegrees(direction.toPitch())+" Roll: "+Math.toDegrees(direction.toRoll())+" depth: "+detectionOrigins[i].getVector()[2]);
				 
				x=(float) relElement.getVector()[0];
				y=(float) relElement.getVector()[1];
				z=(float) relElement.getVector()[2];

				//add into the frame of reference with respect to the gps
				hPos3dNew=new Point3f(x +(float) (detectionOrigins[i].getVector()[0]), y+(float) (detectionOrigins[i].getVector()[1]), z+(float) (detectionOrigins[i].getVector()[2]));
				hRotatedPositions.add(hPos3dNew);
				
			}
//			System.out.println("AbstractTM: "+i+" :"+ hRotatedPositions);
			this.hydrophonePos.add(hRotatedPositions);
		}

	}
	
	protected void calculateWorldVectors() {		
		int nSubDetections=getNDetections();
		realWorldVectors = new PamVector[nSubDetections][];
		PamVector[] v;
		PamDataUnit pd;
		AbstractLocalisation localisation;
		for (int i = 0; i < nSubDetections; i++) {
			pd = currentDetections.get(i);
			localisation = pd.getLocalisation();
			if (localisation == null) {
				continue;
			}
			v = localisation.getRealWorldVectors();

			realWorldVectors[i] = v;
		}

	}
	

	@Override
	public synchronized ArrayList<ArrayList<Point3f>> getHydrophonePos() {
		if (this.hydrophonePos==null){
			calcHydrophonePositions();
		}
		return hydrophonePos;
	}
	
	
	@Override
	public ArrayList<PamDataUnit> getCurrentDetections() {
		return currentDetections;
	}

	@Override
	public PamVector[] getOrigins() {
		if (detectionOrigins==null) calcDetectionOrigins();
		return detectionOrigins;
	}

	@Override
	public LatLong getGPSReference() {
		return currentDetections.get(0).getOriginLatLong(false);
	}
	
	@Override
	public PamVector[][] getWorldVectors() {
		if (realWorldVectors==null) calculateWorldVectors();
		return realWorldVectors;
	}
	
	
	@Override
	public LatLong metresToLatLong(PamVector pt) {
		if (getGPSReference() == null) {
			return null;
		}
		LatLong ll = getGPSReference().addDistanceMeters(pt.getElement(0), pt.getElement(1));
		ll.setHeight(pt.getElement(2));
		return ll;
	}
	
	@Override
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
	
	@Override
	public int getReferenceHydrophones(){
		int referenceHydrophones=0; 
		for (int i=0; i<currentDetections.size(); i++){
			referenceHydrophones |= currentDetections.get(i).getLocalisation().getReferenceHydrophones();
		}
		return referenceHydrophones;
	}

	/**
	 * Return average time for the target motion event. 
	 */
	@Override
	public Long getTimeMillis(){
		if (timeMillis==null){
			long time=0; 
			for (int i=0; i<currentDetections.size(); i++){
				time+=currentDetections.get(i).getTimeMilliseconds();
			}
			timeMillis=time/currentDetections.size();
		}
		return timeMillis;
	}
	
	/**
	 * This is a piece of code designed to bridge the gap between using Euler angles and heading angles. Heading has been used a lot without cnsideration. 
	 * @param eulerAngles
	 * @return
	 */
	public static PamVector[] getHeadingVectors(PamQuaternion[] eulerAngles){
		PamVector[] detectionHeadings=new PamVector[eulerAngles.length];
		for (int i=0; i<eulerAngles.length; i++){
				detectionHeadings[i] = new PamVector(Math.cos(Math.PI/2-eulerAngles[i].toHeading()), Math.sin(Math.PI/2-eulerAngles[0].toHeading()), 0);
		}
		return detectionHeadings;
	}
	
	/**
	 * Check which streamer we have in the group of detections
	 * @return array for streamer indexes
	 */
	public static ArrayList<Integer> checkStreamers(ArrayList<PamDataUnit> currentDetections){
		
		ArrayList<Integer> streamerIndices=new ArrayList<Integer>();
		int[] channelArray;
		for (int i=0; i<currentDetections.size(); i++){
			channelArray=PamUtils.getChannelArray(currentDetections.get(i).getChannelBitmap());
			if (channelArray==null) continue; 
			
			for (int j=0; j<channelArray.length; j++){
				int streamerIndex=ArrayManager.getArrayManager().getCurrentArray().getStreamerForPhone(channelArray[j]);
				if (!streamerIndices.contains(streamerIndex)) streamerIndices.add(streamerIndex);
			}
			
		}
		
		return streamerIndices;
		
	}
	
	
	public TMInfoWorker getObserverThread() {
		return observerThread;
	}

	public void setObserverThread(TMInfoWorker observerThread) {
		this.observerThread = observerThread;
	}
	
	
	/**
	 * Calculate the beam latitude and longitude. The beam lat long is basically the point on the GPS path which is closest to the 
	 * localised position of the animal 
	 * @param localised- localisation result to find beam lat long for. 
	 * @return lat long
	 */
	long millisOver=90000; 
	@Override
	public GpsData getBeamLatLong(LatLong localised){
		
		GPSDataBlock gpsDataBlock = (GPSDataBlock) PamController.getInstance().getDataBlock(GpsDataUnit.class, 0);
	
		if (gpsDataBlock==null){
			System.err.println("TargetMotionModule: No GPS data to get beam lat long:");
			return null;
		}
		
		long startTime=currentDetections.get(0).getTimeMilliseconds()-millisOver;
		long endTime=currentDetections.get(currentDetections.size()-1).getTimeMilliseconds()+millisOver;
		
		//find gps units to search through
		ArrayList<GpsDataUnit> gpsUnits=gpsDataBlock.findUnitsinInterval(startTime, endTime);
		
		if (gpsUnits==null || gpsUnits.size()==0){
			System.err.println("TargetMotionModule: No GPS data to get beam lat long:");
			return null; 
		}
		
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
	
	/**
	 * Calculate the beam time. This is not the time that the vessel was at the beam lat long but the time the streamer passed that point. Note that if there are 
	 * multiple streamers passing the point  the streamer lengths are averaged and used as our beam lat long. We use the model for each streamer to to work out when it passed the 
	 * beam lat long point. 
	 * @param beamLatLong- the beam lat long. 
	 * @return the time at which the hydrophone array passed the beam lat long point. 
	 */
	@Override
	public long getBeamTime(GpsData beamLatLong){	
		
		try{
		//calculate minimum speed- halve the current speed:
		double minSpeed=0.5*beamLatLong.getSpeed();
		//calculate the max streamer length
		ArrayList<Integer> streamerIndex= checkStreamers(currentDetections);
		
		double maxLength=-Double.MAX_VALUE;
		double length;
		for (int i=0; i<streamerIndex.size(); i++){
			length = ArrayManager.getArrayManager().getCurrentArray().getStreamerData(streamerIndex.get(i), beamLatLong.getTimeInMillis()).getY();
			if (length>maxLength){
				maxLength=length; 
			}
		}
		//work out the time we need to look back in millis
		long time=(long) ((maxLength/minSpeed)*1000.0);
		long millisStart=beamLatLong.getTimeInMillis();
		long millisEnd=beamLatLong.getTimeInMillis();
		//need to make sure we set times right if the array is in front of the boat (unlikely but could happen)
		if (time>0) millisStart=millisStart-time;
		else millisEnd=millisEnd-time;

		//calculate the streamer path between the start and end time. 
		ArrayList<ArrayList<GpsData>> streamerPos=TargetMotionControl.calcStreamerPath( this, millisStart,  millisEnd, null);
		
		double dist;
		double minDist;
		GpsData pos;
		long beamTime = 0; 
		for (int i=0; i<streamerPos.size(); i++){
			minDist=Double.MAX_VALUE;
			pos=null;
			for (int j=0; j<streamerPos.get(i).size(); j++){
				dist=beamLatLong.distanceToMetres(streamerPos.get(i).get(j));
				if (dist<minDist){
					minDist=dist; 		
					pos=streamerPos.get(i).get(j);
				}
			}
			beamTime+=pos.getTimeInMillis();
		}
		
		beamTime=beamTime/streamerPos.size(); 

		return beamTime; 
		}
		catch (Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	

}
