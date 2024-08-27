package loc3d_Thode;

import java.util.ArrayList;

import Localiser.DelayMeasurementParams;
import Localiser.algorithms.Correlations;
import fftManager.FFT;

/**
 * StationQueue--
 * @author Thode
 * Hold a sequence of detections from a clustered set of hydrophones, or a unit that holds bearing as well as timing information...
 *
 */
class StationQueue {

	/** Constants for storing feature information into detection queues **/
	public static final int ECHOTIME = 0;
	public static final int BEARING = 1;
	public static final int ICI = 2;

	/** Constants for interpreting StationQueue.newData **/
	public static final int NOECHO = -1;
	public static final int FIRSTDET = -2;

	/*
	 * Which station is this detector operating on
	 */
	int istation,Ilast_echo, minICI;
	long queue_maxtime;
	double percentICIErrorTOL;
	long tds_max;
	float fs;
	boolean detection_complete;

	// Collection of data in this code
	ArrayList<GroupDetDataUnit> queue = null;
	

	private DelayMeasurementParams delayParams = new DelayMeasurementParams();
	private TowedArray3DProcess towedArray3DProcess;


	public StationQueue(int station, TowedArray3DProcess towedArray3DProcess) {
		this.istation = station;
		this.towedArray3DProcess = towedArray3DProcess;
		queue=new ArrayList<GroupDetDataUnit>();
		Ilast_echo=-2;
	}

	public void pamStart() {
		//background = 0;
		//detectionOn = false; 
		//callCount = 0;
	}

	public boolean IsLastDetectionComplete(){
		if (Ilast_echo==queue.size()-1)
			return true;
		else
			return false;
	}
	public void cleanQueue(float current_time){

//		Note a detection arrival is defined as half way between startSample and EndSample.

		//Add detection to array list
		//System.out.println("Window parameter:"+towedArray3DController.findAnchorProcessParameters.maxTimeDelay);
		if (queue.size()>0) {
			/*  Clean out old queue times */
			//System.out.println("Current time-Oldest Time " + (current_time-olddetection.getStartSample()/fs));
			if ((current_time-getTime(0))>queue_maxtime) {
				//Remove queue older than a certain value
				//System.out.println("removing element");
				queue.remove(0);
				Ilast_echo= getLastIndexWithEcho();
			}	
		}
	}
	/**
	 * Performs the same function as newData in the outer class, but this

	 */

	public int newData(TowedArray3DProcessParameters params, float fs, long peakSample, double bearing, double[] rawdata) {
		/* fs is samples per millisecond
		 * duration and peakSample are samples, not time
		 * params are the parameters from the dialog box that may influence decisions to store data...
		 */
		this.fs=fs;
		float current_time = (float)1000*peakSample/fs;

		cleanQueue(current_time);
		if (queue.size()>0) {
			int Ilast=getLastIndex();//Index of end of cue (most recent detection)
			double last_time = getLastTime();
			double tds=(current_time-last_time);
			//System.out.println("tds:" + tds + " msec  tds_max:" + tds_max + " msec");
			/* If previous detection occurred within tds_max of current detection, then
			 * don't add this detection to the queue, but add information about echo time to the queue
			 */
			if (0<tds&tds<tds_max&!detection_complete){
				detection_complete=true;
				if (params.fine_tune_tds) //If we wish to use cross-correlation to refine estimate...
				{
					Correlations correlations = new Correlations();
					int nFFT = 2 * FFT.nextBinaryExp(Math.max(rawdata.length,getRawData(Ilast).length));
					double lagSams = correlations.getDelay(rawdata, 
							getRawData(Ilast), delayParams, towedArray3DProcess.getSampleRate(), nFFT).getDelay();
					double lagSec = 1000*lagSams / fs;
					tds+=lagSec;
				}
				setEchoTime(Ilast, tds);
					//setRawSurfaceData(Ilast,rawdata);
				double bearing_current=getBearing(Ilast);
				Ilast_echo=Ilast;
				setICI(Ilast,0);
				//Iolder is candidate previous detection to match with current detection..
				int Iolder = Ilast-1;
				/* Compute interpulse interval for cross-channel matching */
				while (Iolder>0)
				{
					double bearing_test=getBearing(Iolder);
					double score = Math.abs(1-(bearing_test/bearing_current));
					double tds_last_time=getEchoTime(Iolder);
					if (tds_last_time>0.0) //If echo time available, use to register score...
						score =0.5*score +0.5*Math.abs(1-(tds_last_time/tds));
					if (score<percentICIErrorTOL){
						setICI(Ilast,last_time-getTime(Iolder));
						//System.out.println("ICI in old click: "+dt);System.out.println();
						//Link this earlier detection to the current one...
						linkICI(Ilast,Iolder);
						break;
					}
					Iolder--;
				}
				return Ilast;
			}
			else{ 
				///Add new detection to GroupDetDataUnit
				//GroupDetDataUnit currentdetection=new GroupDetDataUnit(0,istation,peakSample,2, 3);
				queue.add(new GroupDetDataUnit(0,istation,peakSample,2, 3));
				detection_complete=false;
				setBearing(Ilast+1, bearing);
				setICI(Ilast+1, 0);
				setEchoTime(Ilast+1, -1);
				if (params.fine_tune_tdd) //If wish to use cross-correlation to improve tdd estimate later
					setRawData(Ilast+1,rawdata);
				/** 
				 * Routine for adding ICI estimate based on matched bearing alone...
				 */
				int i = Ilast;
				while (i>0)
				{
					if (Math.abs(1-(getBearing(i)/bearing))<percentICIErrorTOL){
						setICI(Ilast+1, current_time-getTime(i));
						linkICI(Ilast+1,i);
						//currentdetection.setFeature(dt, istation, ICI);
						break;
					}
					i--;
				}

				//System.out.println("Not preceeded by direct path, adding detection");
				return NOECHO;  //not proceeded by a direct path
			}
		}
		else{ //First detection to be added to cue
			queue.add(new GroupDetDataUnit(0,istation,peakSample,2, 3));
			setBearing(0,bearing);
			setICI(0,ICI);
			setEchoTime(0, -1);
			if (params.fine_tune_tdd)  //If wish to use cross-correlation to improve tdd estimate later
				setRawData(0,rawdata);
			detection_complete=false;
			//currentdetection.setFeature(bearing, istation, BEARING);  //Here istation represents a subarray
			//System.out.println("Not enough in queue, adding detection");
			return FIRSTDET;
		}

	}


	public int getLastIndex(){
		int temp;
		return queue.size()-1;
	}

	public int getLastIndexWithEcho(){
		for (int i = queue.size()-1; i >-1; i--) {
			if (getEchoTime(i)>0)
				return i;
		}
		return -1;
	}

	public void setMinICI(int minICI) {
		this.minICI = minICI;
	}
	public long getQueue_maxtime() {
		return queue_maxtime;
	}

	public void setQueue_maxtime(long queue_maxtime) {
		this.queue_maxtime = queue_maxtime;
	}

	public long getTds_max() {
		return tds_max;
	}

	public void setTds_max(long tds_max) {
		this.tds_max = tds_max;
	}

	public double getPercentICIErrorTOL() {
		return percentICIErrorTOL;
	}

	public void setPercentICIErrorTOL(double percentError) {
		this.percentICIErrorTOL = percentError;
	}



	public double getTime(int i){
		return queue.get(i).getStartSample()*1000/fs;
	}

	public double getLastTime(){
		if (queue.size()>0)
			return queue.get(queue.size()-1).getStartSample()*1000/fs;
		else
			return -1;
	}

	public double getLastTimeWithEcho(){
		if (Ilast_echo>=0)
			return queue.get(Ilast_echo).getStartSample()*1000/fs;
		else
			return -1;
	}
	
	public void setEchoTime(int i, double tds){
		queue.get(i).setFeature(tds, istation, ECHOTIME);
	}
	
	public void setRawData(int i, double[] rawdata){
		queue.get(i).setRawdata(rawdata);
	}
	
	public double[] getRawData(int i){
		return queue.get(i).getRawdata();
	}
	
	
	
	public double getEchoTime(int i){
		return queue.get(i).getFeature(istation, ECHOTIME);
	}

	public double getLastEchoTime(){
		if (Ilast_echo>=0)
			return queue.get(Ilast_echo).getFeature(istation, ECHOTIME);
		else
			return -1;
	}
	public double getICI(int i){
		return queue.get(i).getFeature(istation, ICI);
	}

	public void setICI(int i, double ici){
		queue.get(i).setFeature(ici, istation, ICI);
	}

	public double getLastICIWithEcho(){
		if (Ilast_echo>0)
			return queue.get(Ilast_echo).getFeature(istation, ICI);
		else
			return -1;
	}
	public double getBearing(int i){
		return queue.get(i).getFeature(istation, BEARING);
	}
	public double getLastBearing(){
		return queue.get(queue.size()-1).getFeature(istation,BEARING);
	}
	public void setBearing(int i, double bearing){
		queue.get(i).setFeature(bearing,istation, BEARING);
	}
	public double getLastBearingWithEcho(){
		if (Ilast_echo>=0)
			return queue.get(Ilast_echo).getFeature(istation, BEARING);
		else
			return -1;
	}
	
	public void linkICI(int icurrent, int ipast){
		queue.get(icurrent).addSuperDetection(queue.get(ipast));
		
	}
	
	public double[] getLinkedICI(int I){
		double[] ICIlist =new double[minICI];
		for (int i = 0; i < ICIlist.length; i++) {
			ICIlist[i]=-1;
		}
		GroupDetDataUnit oldunit = (GroupDetDataUnit) queue.get(I).getSuperDetection(0);
		if (oldunit==null)
			return ICIlist;
		for (int i = 0; i < minICI; i++) {
			ICIlist[i]=oldunit.getFeature(istation, ICI);
			oldunit=(GroupDetDataUnit) oldunit.getSuperDetection(0);
			if (oldunit==null)
				break;
		}
		
		return ICIlist;
	}

	public float getFs() {
		return fs;
	}

}


