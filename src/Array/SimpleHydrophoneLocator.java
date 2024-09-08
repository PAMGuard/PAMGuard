package Array;

import java.util.ArrayList;

import pamMaths.PamVector;

/**
 * Some abstract class function for hydrophone location that are used by
 * more concrete locators, in particular the StaticHydrophoneLocator
 * and StraightHydrophoneLocators
 * @author Doug Gillespie
 * @see Array.StraightHydrophoneLocator
 * @see Array.StaticHydrophoneLocator
 *
 */

abstract public class SimpleHydrophoneLocator extends HydrophoneLocator {

	private static final long serialVersionUID = -8197030971819764600L;

	public SimpleHydrophoneLocator(PamArray pamArray, Streamer streamer) {
		super(pamArray, streamer);
	}

//	/**
//	 * 
//	 */
//	public double getPairAngle(long timeMilliseconds, int phone1, int phone2, int angleType) {
//		PamVector v1, v2;
//		
//		v1=new PamVector(getHydrophoneCoordinates(phone1,timeMilliseconds));
//		v2=new PamVector(getHydrophoneCoordinates(phone2,timeMilliseconds));
//		
//		Streamer s1 = pamArray.getStreamer(pamArray.getHydrophone(phone1).getID());
//		Streamer s2 = pamArray.getStreamer(pamArray.getHydrophone(phone2).getID());
//		
//		if (s1 != null && s1.getCoordinateVector() != null) {
//			v1 = v1.add(s1.getCoordinateVector());
//		}
//		if (s2 != null && s2.getCoordinateVector() != null) {
//			v2 = v2.add(s2.getCoordinateVector());
//		}
//		
////		v1 = pamArray.getAbsHydrophoneVector(phone1);
////		v2 = pamArray.getAbsHydrophoneVector(phone2);
//		double ang = Math.atan2(v2.getElement(1) - v1.getElement(1), v2.getElement(0) - v1.getElement(0));
//		ang = 90 - (ang * 180/Math.PI);
//		if (ang < 0) ang += 360;
//		return ang;
//		
////		Hydrophone h1 = pamArray.getHydrophone(phone1);
////		Hydrophone h2 = pamArray.getHydrophone(phone2);
////		
////		return getPairAngle(h1, h2);
//	}
	
	/**
	 * Gets the angle between a pair of hydrophones in degrees 
	 * in navigation units, i.e. clockwise from North
	 * @param h1 first hydrophone
	 * @param h2 second hydrophone
	 * @return angle from north in degrees
	 */
//	private double getPairAngle(Hydrophone h1, Hydrophone h2) {
//		// angle up from x axis ...
//		double x1, x2, y1, y2;
//		x1 = pamArray.get
//		double ang = Math.atan2(h2.getY() - h1.getY(), h2.getX() - h1.getX());
//		ang = 90 - (ang * 180/Math.PI);
//		if (ang < 0) ang += 360;
//		return ang;
//	}

	public double getPairSeparation(long timeMilliseconds, int phone1, int phone2) {
		PamVector v1, v2;
		v1 = pamArray.getAbsHydrophoneVector(phone1,timeMilliseconds);
		v2 = pamArray.getAbsHydrophoneVector(phone2,timeMilliseconds);
		return v1.dist(v2);
//		Hydrophone h1 = pamArray.getHydrophone(phone1);
//		Hydrophone h2 = pamArray.getHydrophone(phone2);
//		return getPairSeparation(h1, h2);
	}
	
//	private double getPairSeparation(Hydrophone h1, Hydrophone h2) {
//		if (h1 == null || h2 == null) return 1;
//		return Math.sqrt(Math.pow(h2.getY() - h1.getY(), 2) + 
//				Math.pow(h2.getX() - h1.getX(), 2) + 
//				Math.pow(h2.getZ() - h1.getZ(), 2));
//	}
	

//	public double getPhoneHeight(long timeMilliseconds, int phoneNo) {
//		if (pamArray == null || pamArray.getHydrophone(phoneNo) == null) return 0;
//		return pamArray.getHydrophone(phoneNo).getZ();
//	}
//
//	public double getPhoneTilt(long timeMilliseconds, int phoneNo) {
//		if (pamArray == null || pamArray.getHydrophone(phoneNo) == null) return 0;
//		return pamArray.getHydrophone(phoneNo).getTilt();
//	}
//	
//	public double getPhoneHeading(long timeMilliseconds, int phoneNo) {
//		if (pamArray == null || pamArray.getHydrophone(phoneNo) == null) return 0;
//		return pamArray.getHydrophone(phoneNo).getHeading();
//	}
//
//	public LatLong getPhoneLatLong(long timeMilliseconds, int phoneNo) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void notifyModelChanged(int changeType, boolean initComplete) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isStatic() {
		return false;
	}

//	@Override
//	public double[] getHydrophoneCoordinates(int iHydrophone, long timeMilliseconds) {
////		System.out.println("iHydrophone: "+iHydrophone+ " timeMillis: "+timeMilliseconds);
////		System.out.println("pamArray: "+pamArray);
////		System.out.println(pamArray.getHydrophone(iHydrophone, timeMilliseconds));
//		Hydrophone	h = pamArray.getHydrophone(iHydrophone, timeMilliseconds);
//		
//		if (h == null) {
//			return null;
////			 h = pamArray.getHydrophone(iHydrophone, timeMilliseconds);
//		}
//			
////		System.out.println(" C: "+ pamArray.getHydrophone(iHydrophone, timeMilliseconds).getCoordinates());
//		return h.getCoordinates();
//	}
//	
//	@Override
//	public double[] getHydrophoneCoordinateErrors(int iHydrophone, long timeMilliseconds) {
//		return pamArray.getHydrophone(iHydrophone, timeMilliseconds).getCoordinateErrors();
//	}
//	
	/**
	 * Find the origin point of the array. In the case of a paired towed array this will be halfway between the elements but in the case of more complicated towed array this will be the average positions of all the hydrophones
	 * @param hydrophones
	 * @return
	 */
	protected static double[] getArrayCenter(ArrayList<Hydrophone> hydrophones){
		
		double totalHx=0;
		double totalHy=0;
		double totalHz=0;
		int nChannels=hydrophones.size();
		for (int p=0; p<nChannels;p++){
			totalHx+=hydrophones.get(p).getCoordinates()[0];
			totalHy+=hydrophones.get(p).getCoordinates()[1];
			totalHz+=hydrophones.get(p).getCoordinates()[2];
		}
		
		double[] arrayOriginPt={totalHx/nChannels,totalHy/nChannels,totalHz/nChannels};
		
		return arrayOriginPt;
		
	}
	
//	@Override
//	public Double getArrayHeading(GpsData gpsData){
//		if (gpsData == null) {
//			return null;
//		}
//		return gpsData.getHeading();
//	}
	
	@Override
	public boolean isChangeable(){
		return false;
	}



	
}
