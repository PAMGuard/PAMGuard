package loc3d_Thode;
import fftManager.FFT;
import Localiser.*;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.TimeDelayData;

/* Match detections between two station queues. */
public class CrossStationMatcher {

	StationQueue focalStation;
	StationQueue stationList[];
	int linked_indicies[], nstation, focal_index;
	double focal_ici_list[], focal_arrival_time,  focal_echo_time, focal_bearing,tdd_maxtime;
	double linked_TOA[], linked_echo_times[],linked_bearings[],ICI_err_thresh;
	boolean yes_focal_list;
	private DelayMeasurementParams delayParams = new DelayMeasurementParams();
	private TowedArray3DProcess towedArray3DProcess;
	/**
	 * @param towedArray3DProcess 
	 * @param focalStation
	 * @param stationList
	 */
	public CrossStationMatcher(int maxCues, TowedArray3DProcess towedArray3DProcess, double tdd_maxtime, double ICI_err_thresh) {
		this.towedArray3DProcess = towedArray3DProcess;
		this.ICI_err_thresh=ICI_err_thresh;
		this.tdd_maxtime=tdd_maxtime;
		focalStation = null;
		stationList = new StationQueue[maxCues];
		linked_indicies = new int[maxCues];
		linked_TOA = new double[maxCues];
		linked_echo_times = new double[maxCues];
		linked_bearings=new double[maxCues];
		focal_index=-1;
		nstation=0;
		for (int i = 0; i < linked_indicies.length; i++){
			linked_indicies[i]=-1;
			linked_TOA[i]=0;
			linked_echo_times[i]=-1.0;
			linked_bearings[i]=-1.0;
		}
	}

	public StationQueue getFocalStation() {
		return focalStation;
	}
	public void setFocalStation(StationQueue focalStation) {
		this.focalStation = focalStation;
	}

	/* Add queue to QueueList.  The focal Queue cannot be part of this list..*/

	public void addQueueList(StationQueue que){
		if (nstation<stationList.length)
			stationList[nstation++]=que;
	}

	/* Find the last detection in the focal cue and recover the linked ICI list*/
	public void setLastFocalICIList(){
		focal_index=focalStation.getLastIndex();
		focal_ici_list= focalStation.getLinkedICI(focal_index);
		focal_arrival_time=focalStation.getLastTime();
		focal_echo_time=focalStation.getLastEchoTime();
		focal_bearing=focalStation.getLastBearing();
		yes_focal_list=false;
		if (focal_ici_list[focal_ici_list.length-1]>0)
			yes_focal_list=true;
	}

	/* Find the last detection in the focal cue with an echo time and recover the linked ICI list*/
	public void setLastFocalWithEchoICIList(){
		focal_index=focalStation.getLastIndexWithEcho();
		yes_focal_list=false;
		if (focal_index>=0){
			focal_ici_list= focalStation.getLinkedICI(focal_index);
			focal_arrival_time=focalStation.getLastTimeWithEcho();
			focal_echo_time=focalStation.getEchoTime(focal_index);
			focal_bearing=focalStation.getBearing(focal_index);
			if (focal_ici_list[focal_ici_list.length-1]>0)
				yes_focal_list=true;
		}

	}

	/*May not be needed, but return the linked ICI from Ith detection in focal queue*/
	public void setFocalICIList(int I){
		yes_focal_list=false;
		if (focalStation.getLastIndex()>=I){
			focal_ici_list= focalStation.getLinkedICI(I);
			focal_arrival_time=focalStation.getTime(I);
			focal_echo_time=focalStation.getEchoTime(I);
			focal_bearing=focalStation.getBearing(I);
			if (focal_ici_list[focal_ici_list.length-1]>0)
				yes_focal_list=true;
		}
	}

	/* A simple cross-link procedure that does not use ICI matching*/

	public boolean crossLinkSimple(){
		focal_arrival_time=focalStation.getLastTime();
		for (int i = 0; i < nstation; i++) { //For every station
			linked_indicies[i]=-1;
			for (int j = stationList[i].getLastIndex(); j >-1 ; j--) { //Start with most recent time in queue
				double dt = (focal_arrival_time-stationList[i].getTime(j));
				if(Math.abs(dt)<tdd_maxtime){
					linked_indicies[i]=j;
					linked_TOA[i]=dt;
					linked_echo_times[i]=stationList[i].getEchoTime(j);
					linked_bearings[i]=stationList[i].getBearing(j);
					break;
				}//end iff tdd_maxtime

			}//j detection list

		} // i station list
		return true;
	}//yes_focal_list



	/* if focal_ici_list has been set, find matching indicies in QueueList
	 * for all detections regardless of whether echotimes exist
	 * Set superDetection[1] variable to cross-channel match*/

	public boolean crossLink(){
		boolean crosslink_good = true;
		double current_error=1e6;
		double best_error=current_error;
		if (yes_focal_list){
			for (int i = 0; i < nstation; i++) { //For every station
				linked_indicies[i]=-1;
				for (int j = stationList[i].getLastIndex(); j >-1 ; j--) { //Start with most recent time in queue
					double dt = (focal_arrival_time-stationList[i].getTime(j));
					if(Math.abs(dt)<tdd_maxtime){
						current_error=mean_square_percent_error(focal_ici_list,stationList[i].getLinkedICI(j));
						if(current_error<best_error){
							best_error=current_error;
							linked_indicies[i]=j;
							linked_TOA[i]=dt;
							linked_echo_times[i]=stationList[i].getEchoTime(j);
							linked_bearings[i]=stationList[i].getBearing(j);
						}
					}//end iff tdd_maxtime

				}//j detection list
				if (best_error>=ICI_err_thresh)
					crosslink_good = false;
			} // i station list
			return crosslink_good;
		}//yes_focal_list
		else{
			return false;
		}
	}

	/* Given an index to focalStation, find matching indicies in stationList
	 * restricting search to detections with echotimes in target stations
	 * Set superDetection[1] variable to cross-channel match
	 * Return true if a match is found...
	 * */

	public boolean crossLinkWithEcho(){
		boolean crosslink_good = true;
		double current_error=1e6;
		double best_error=current_error;
		if (yes_focal_list){
			for (int i = 0; i < nstation; i++) { //For every station
				linked_indicies[i]=-1;
				for (int j = stationList[i].getLastIndexWithEcho(); j >-1 ; j--) { //Start with most recent time in queue
					if (stationList[i].getEchoTime(j)>0){
						double dt = (focal_arrival_time-stationList[i].getTime(j));
						if(Math.abs(dt)<tdd_maxtime){
							current_error=mean_square_percent_error(focal_ici_list,stationList[i].getLinkedICI(j));
							if(current_error<best_error){
								best_error=current_error;
								linked_indicies[i]=j;
								linked_TOA[i]=dt;
								linked_echo_times[i]=stationList[i].getEchoTime(j);
								linked_bearings[i]=stationList[i].getBearing(j);
							}
						}//end iff tdd_maxtime
						else
							break;
					}//if echotime
				}//j : station index
				if (best_error>=ICI_err_thresh)
					crosslink_good = false;
			} //i nstation loop
			return crosslink_good;
		}//yes_focal_list
		else{
			return false;
		}
	}

	private double mean_square_percent_error(double[] a, double[] b){
		//Compute the fractional error for each ICI and return average deviation
		double err =0;
		for (int i = 0; i < a.length; i++) {
			if (a[i]<0 | b[i]<0) //if either linked list is incomplete
				return 1e6;
			err+=Math.pow(1-b[i]/a[i], 2);
		}
		return Math.sqrt(err)/a.length;
	}

	public double[] getLinked_echo_times() {
		return linked_echo_times;
	}

	public double[] getTOA(boolean fine_tune_tdd) {
		if (fine_tune_tdd) //If you wish to use cross-correlation to fine-tune estimate..
			return getPreciseTOA();
		else
			return linked_TOA;
	}

	public double[] getPreciseTOA() {
		double [] focal_data,other_data;
		if (focal_index>-1) {
			focal_data=focalStation.getRawData(focal_index);
			for (int i = 0; i < nstation; i++) {
				other_data=stationList[i].getRawData(linked_indicies[i]);
				Correlations correlations = new Correlations();
				int nFFT = 2 * FFT.nextBinaryExp(Math.max(other_data.length,focal_data.length));
				TimeDelayData td = correlations.getDelay(other_data, 
						focal_data, delayParams, towedArray3DProcess.getSampleRate(), nFFT);
				double lagSams = td.getDelay();
				double lagSec = 1000*lagSams / focalStation.getFs();
				linked_TOA[i]+=lagSec;
			}
		}
		//For every station
		return linked_TOA;
	}


	public boolean isYes_focal_list() {
		return yes_focal_list;
	}

	public void setICI_err_thresh(double ici_err_thresh) {
		ICI_err_thresh = ici_err_thresh;
	}


	public double getFocal_bearing() {
		return focal_bearing;
	}

	public double getFocal_echo_time() {
		return focal_echo_time;
	}

	public double[] getLinked_bearings() {
		return linked_bearings;
	}

	public int[] getLinked_indicies() {
		return linked_indicies;
	}




}
