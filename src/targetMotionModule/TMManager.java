package targetMotionModule;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import Array.ArrayManager;
import GPS.GpsData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;

/**
 * This class handles the loading for data for the target motion module. There is a lot fo information to calculate before we attempt a target motion localisation. We must convert geo reference information into an x,y,z
 * co-ordinate system, use this to determine hydrophone locations  then process time delays and grid a series of arrays holding all the informations. @see AbstractTargetMotionInfromation and @see TargetMotionInformation 
 * for more info this. 
 * <p>
 * TMManager carries out these calculations on a separate thread, allowing for progress updates to take place.
 * @author spn1
 *
 */
public class TMManager {

	private TargetMotionControl targetmotionControl;
	
	private TMInfoWorker currentThread; 
	
	//data to hold
	ArrayList<ArrayList<GpsData>> streamerPath;
	
	
	public TMManager(TargetMotionControl targetmotionControl) {
		this.targetmotionControl=targetmotionControl;
	}
	

	/**
	 * Loading event data may take some time
	 * @author Jamie Maculay
	 *
	 */
	public class TMInfoWorker extends  SwingWorker<Object, Object>{

		TargetMotionInformation targetMotionInfo;
		private ArrayList<PamDataUnit> currentDetections;
		private TargetMotionLocaliserProvider localiserProvider;
		private boolean calcStreamerPath;
		
		
		private double totalCalcs=6;
		private double currentCalcTask=1;

		
		TMInfoWorker(ArrayList<PamDataUnit> currentDetections, TargetMotionLocaliserProvider localiserProvider, boolean calcstreamerPath){
			
			this.currentDetections=currentDetections;
			this.localiserProvider=localiserProvider; 
			this.calcStreamerPath=calcstreamerPath;
			
		}

		@Override
		protected Object doInBackground() throws Exception {
			
			Debug.out.println("TMWorker: No. Hydrophone Channel iterators: "+ ArrayManager.getArrayManager().getHydrophoneDataBlock().getChannelIteratorCount());
			targetMotionInfo=localiserProvider.getTMInfo(currentDetections);
			
			
			if (targetMotionInfo instanceof AbstractTargetMotionInformation) ((AbstractTargetMotionInformation) targetMotionInfo).setObserverThread(this);
			
			//now calculate all the stuff we need to. If the TargetMotionInformation class has been written well then it will save the data once all has been calulated 
			currentCalcString="Calculating array origin points:";
			targetMotionInfo.getOrigins();
			currentCalcTask++;
			setTMProgress(100);
			currentCalcString="Calculating array orientation:";
			Debug.out.println(currentCalcString);
			targetMotionInfo.getEulerAngles();
			currentCalcTask++;
			currentCalcString="Converting to cartesian:";
			Debug.out.println(currentCalcString);
			setTMProgress(100);
			targetMotionInfo.getHydrophonePos();
			currentCalcTask++;
			setTMProgress(100);
			currentCalcString="Calculating time delay matrix:";
			Debug.out.println(currentCalcString);
			targetMotionInfo.getTimeDelays();
			currentCalcTask++;
			setTMProgress(100);
			currentCalcString="Calculating time delay error matrix:";
			Debug.out.println(currentCalcString);
			targetMotionInfo.getTimeDelayErrors();
			currentCalcTask++;
			setTMProgress(100);
			
			if (isCancelled()){
				targetMotionInfo=null;
				return null;
			} 
			
			if (calcStreamerPath){
				//add the results to the map before we start a lonf streamer calc
				notifyUpdate(TargetMotionControl.CURRENT_DETECTIONS_CHANGED);
				currentCalcString="Reconstructing streamer path:";
				//we don't want to try this too many times 
				if (streamerPath==null) streamerPath=targetmotionControl.calcStreamerPath(this);
			}
			currentCalcTask++;
			setTMProgress(100);
			
			if (isCancelled()){
				targetMotionInfo=null;
				return null;
			} 
			return null;
		}
		
		//need to have this for rounding errors
		double progress=0.0;
		/*
		 *Set the progress between 0.0->100;  
		 */
		public void setTMProgress(double prog){
			double currProgress=(this.progress+(prog/(double) totalCalcs));
			//if we reach 100% then update the progress holder...this is essentially progress/totalCalcs
			if (prog==100) this.progress=currProgress;
//			System.out.println(currProgress+ " %" +" this.progress: "+ this.progress + " prog/(double) totalCalcs " + prog/(double) totalCalcs);
			super.setProgress((int) currProgress);
			notifyUpdate(TargetMotionControl.DETECTION_INFO_CALC_PROGRESS);
		}
		
		/**
		 * String which describes which calculation is currently taking place. 
		 */
		String currentCalcString="-";
		
		/**
		 * Get a string which describes which calculation is currently taking palce. 
		 */
		public String getCalcString(){
			return currentCalcString;
		}
		

		public int getTMProgress(){
			return getProgress();
		}
		
		@Override
		public void done(){
			super.done();
			Debug.out.println("TMManager:  Thread done;");
			notifyUpdate(TargetMotionControl.DETECTION_INFO_CALC_END);
		}
		
		public TargetMotionInformation getTMInfo( ){
			return targetMotionInfo;
		}


	}
	

	public void notifyUpdate(int flag){
		targetmotionControl.update(flag);
	}


	public synchronized void executeTMThread(ArrayList<PamDataUnit> currentDetections, TargetMotionLocaliserProvider dataBlock, boolean calcStreamer) {
		if (currentThread!=null) {
			currentThread.cancel(true);
			while (!currentThread.isDone()){
				//Do nothing until we've really got rid of this thread. 
			}
		}
		Debug.out.println("Execute TM Thread: ");
		currentThread=null;
		currentThread=new TMInfoWorker(currentDetections ,dataBlock, calcStreamer);
		targetmotionControl.update(TargetMotionControl.DETECTION_INFO_CALC_START);
		currentThread.execute();
	}


	public TargetMotionInformation getTMinfo() {
		if (currentThread==null) return null;
		Debug.out.println("Current Thread Info: "+currentThread.getTMInfo());
		return currentThread.getTMInfo();
	}


	public TMInfoWorker getCurrentThread() {
		return currentThread;
	}
	

	public ArrayList<ArrayList<GpsData>> getStreamerPath() {
		return streamerPath;
	}


	public void setStreamerPath(ArrayList<ArrayList<GpsData>> streamerPath) {
		this.streamerPath = streamerPath;
	}
	
	
	
	

}
