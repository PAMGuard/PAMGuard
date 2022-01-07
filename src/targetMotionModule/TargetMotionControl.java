package targetMotionModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JPopupMenu;
//import staticLocaliser.SLResult;
import targetMotionModule.TMManager.TMInfoWorker;
import targetMotionModule.offline.TMOfflineFunctions;
import targetMotionModule.panels.*;
import Array.ArrayManager;
import Array.Streamer;
import Array.StreamerDataUnit;
import Array.streamerOrigin.HydrophoneOriginMethod;
import GPS.GpsData;
import Localiser.DisplayLocaliserMenu;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Target motion module. Allows users to localise using target motion based algorithms. 
 * Utilises the hydrophone and streamer origin models in the @see Array package to determine the location of 
 * towed array hydrophones. 
 * @author Doug Gillespie and Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class TargetMotionControl extends PamControlledUnit implements PamSettings, DisplayLocaliserMenu {
	
	//these integer flags are used to update the static localiser. 
	/**
	 * used whenever the current detections selected by the user have been changed
	 */
	public static final int CURRENT_DETECTIONS_CHANGED = 0;
	
	/**
	 *used when the localiser has started 
	 */
	public final static int LOCALISATION_STARTED=1;
	
	/**
	 * The localiser is awaiting input form the user to save results, 
	 */
	public static final int LOCALISATION_WAITING = 2;
	
	/**
	 * The localiser is done
	 */
	public static final int LOCALISATION_DONE = 3;
	
	/**
	 * new localisation results are present and need added
	 */
	public final static int LOCALISATION_RESULTS_ADDED=4;
	
	/**
	 * used whenever the map range is changed
	 */
	public final static int RANGE_CHANGED=5;
	
	/**
	 * a selected algorithm has been changed.
	 */
	public final static int ALGORITHM_SELECTION_CHANGED=7;
	
	/**
	 *hydrophone or streamer positions have been changed. 
	 */
	public final static int HYDROPHONE_DATA_CHANGED=8;
	
	/**
	 * Started to calc the  target motion information
	 */
	public static final int DETECTION_INFO_CALC_START = 9;
	
	/**
	 * Threadc calculating the target motion information has changed. 
	 */
	public static final int DETECTION_INFO_CALC_END = 10;
	
	/**
	 * Update panels with thread progress. 
	 */
	public static final int DETECTION_INFO_CALC_PROGRESS = 11;;




	private ArrayList<PamDataBlock> dataBlocks;
	
	private TargetMotionMainPanel targetMotionMainPanel;
	
	private TargetMotionInformation currentTMinfo;
	
	private TargetMotionLocaliser targetMotionLocaliser;

	private TMOfflineFunctions offlineFunctions;

	private TargetMotionProcess targetMotionProcess;

	private TargetMotionDataBlock targetMotionDataBlock;

	private TargetMotionSQLLogging targetMotionSQLLogging;

	/**
	 * Handles the calculation of information required for target motion loclaisation.
	 */
	private TMManager taregtMotionManager;
	

	/**
	 * Parameters for target motion module
	 */
	private TargetMotionParams tmParams; 


	@SuppressWarnings("unchecked")
	public TargetMotionControl(String unitName) {
		
		super("Static Localiser", unitName);
		
		tmParams= new TargetMotionParams();
				
		findDataBlocks();
		
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			this.isViewer=true;
			offlineFunctions=new TMOfflineFunctions(this);
		}

		addPamProcess(targetMotionProcess=new TargetMotionProcess(this));
//		targetMotionDataBlock= new TargetMotionDataBlock(SLResult.class, "Static Localiser Data", targetMotionProcess, 0);
		targetMotionDataBlock.SetLogging(setTargetMotionSQLLogging(new TargetMotionSQLLogging(targetMotionDataBlock,this)));
		targetMotionProcess.addOutputDataBlock(targetMotionDataBlock);
		
		if (dataBlocks.size()==0 || dataBlocks==null) {
			return;
		}
	
		targetMotionLocaliser=new TargetMotionLocaliser(this, dataBlocks.get(0));
		taregtMotionManager=new TMManager(this);
		targetMotionMainPanel=new TargetMotionMainPanel(targetMotionLocaliser);
		
		targetMotionMainPanel.update(ALGORITHM_SELECTION_CHANGED);
				
		setTabPanel(targetMotionMainPanel);
		
	}
	
	
	private void findDataBlocks(){
		dataBlocks=new ArrayList<PamDataBlock>();
		ArrayList<PamDataBlock> rawDataBlock = PamController.getInstance().getDataBlocks();
		for (int i=0; i<rawDataBlock.size();i++){
			if (rawDataBlock.get(i) instanceof TargetMotionLocaliserProvider){
				dataBlocks.add(rawDataBlock.get(i));
//				System.out.println("Target Motion: Satisfied Data Blocks: "+rawDataBlock.get(i).toString());
			}
		}
	}
	
	public ArrayList<PamDataBlock> getDataBlocks(){
		return dataBlocks;
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch(changeType) {
		case PamController.INITIALIZATION_COMPLETE:
//			System.out.println("TargetMotionControl. notifyModelChanged: INITIALIZATION_COMPLETE");
			targetMotionMainPanel.updateCurrentControlPanel();
			targetMotionMainPanel.updateCurrentControlPanel();
			if (targetMotionMainPanel.getMap2D()==null || targetMotionMainPanel.getMap3D()==null){
				targetMotionMainPanel.createMaps();
			}
			
		break;
			
		case PamController.OFFLINE_DATA_LOADED:
//			System.out.println("TargetMotionControl. notifyModelChanged: OFFLINE_DATA_LOADED");
			targetMotionMainPanel.updateCurrentControlPanel();
			//need to make the streamer path null as gps data may have changed. 
			taregtMotionManager.setStreamerPath(null);
			if (targetMotionMainPanel.getMap2D()==null || targetMotionMainPanel.getMap3D()==null){
				targetMotionMainPanel.createMaps();
			}
		
			
		break;
			
			
		case PamController.ADD_DATABLOCK:
			
		break;
		
		case PamController.REMOVE_DATABLOCK:
			
		break;
		
		case PamController.HYDROPHONE_ARRAY_CHANGED:
//			System.out.println("TargetMotionControl. notifyModelChanged: HYDROPHONE_ARRAY_CHANGED");
			//the array may have changed or a different type of hydrophone locator used. Need to recalculate things
			//need to make the streamer path null as the model used to reconstruct the streamer location may have changed.
			taregtMotionManager.setStreamerPath(null);
			targetMotionMainPanel.getCurrentControlPanel().update(TargetMotionControl.HYDROPHONE_DATA_CHANGED);
		break;

		}
	
	}
	
	/**
	 * Calculates the targetMotiojnInfo for a set of detections. Called from the control panel which passes the selected detection information to this function. 
	 */
	public void calcTMDetectionInfo(ArrayList<PamDataUnit> currentDetections, 	TargetMotionLocaliserProvider dataBlock){
		
		taregtMotionManager.executeTMThread(currentDetections,dataBlock, tmParams.calcStreamerPath);
		 
	}
	
	
	 
	/**
	 * Notify other panels etc of update
	 * @param flag
	 */
	private void notifyUpdate(int flag) {
		
		targetMotionMainPanel.update(flag);

	switch (flag){
		
		case DETECTION_INFO_CALC_END:
			targetMotionMainPanel.update(CURRENT_DETECTIONS_CHANGED);
			break ;
		}
		
	}


	/**
	 * Update tmControl
	 * @param flag
	 */
	public void update(int flag){

		switch (flag){
		
		case CURRENT_DETECTIONS_CHANGED:
			this.currentTMinfo=taregtMotionManager.getTMinfo();
			notifyUpdate(flag);
			break ;
		case DETECTION_INFO_CALC_START:
			notifyUpdate(flag);
			break ;
		case DETECTION_INFO_CALC_END:
			this.currentTMinfo=taregtMotionManager.getTMinfo();
			notifyUpdate(flag);
			break ;
		case DETECTION_INFO_CALC_PROGRESS:
			 notifyUpdate(flag);
		}
	}
	
	
	
	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public PamDataBlock getCurrentDataBlock(){
		return dataBlocks.get(0);
	}


	public TargetMotionLocaliser getTargetMotionLocaliser() {
		return targetMotionLocaliser;
	}


	public TargetMotionMainPanel getTargetMotionMainPanel() {
		return targetMotionMainPanel;
	}


	@Override
	public void addLocaliserMenuItem(JPopupMenu menu,
			PamDataUnit selectedDetion) {
			targetMotionMainPanel.getCurrentControlPanel().addLocaliserMenuItem( menu,	selectedDetion);
	}
	
	public TMOfflineFunctions getOfflineFunctions(){
	 return offlineFunctions;
	}
	
	public TargetMotionDataBlock getTargetMotionDataBlock() {
		return targetMotionDataBlock;
	}


	public void setTargetMotionDataBlock(TargetMotionDataBlock targetMotionDataBlock) {
		this.targetMotionDataBlock = targetMotionDataBlock;
	}


	/**
	 * Save localisation data
	 * @param saveAll - save all localisation results if true. If false then save only the currently selected best result. 
	 */
	public void save(boolean saveAll) {
		if (targetMotionLocaliser.getResults()!=null){
			for (int i=0; i<targetMotionLocaliser.getResults().size(); i++){
				if (saveAll || i==targetMotionLocaliser.getBestResultIndex()){
					targetMotionMainPanel.getCurrentControlPanel().saveData((TargetMotionResult) targetMotionLocaliser.getResults().get(i));
				}
			}
		}
	}


	public TargetMotionSQLLogging getTargetMotionSQLLogging() {
		return targetMotionSQLLogging;
	}


	public TargetMotionSQLLogging setTargetMotionSQLLogging(TargetMotionSQLLogging targetMotionSQLLogging) {
		this.targetMotionSQLLogging = targetMotionSQLLogging;
		return targetMotionSQLLogging;
	}
	

	/**
	 * Calculate the path of all streamers over the loaded GPS data. Takes a long time to calculate as we have to iterate through database to find streamers and hydrophiones then model the 
	 * streamer position. 
	 * @param tmWorker - the thread this function is carried out on. Can be null
	 */
	public ArrayList<ArrayList<GpsData>> calcStreamerPath(TMInfoWorker tmWorker) {
		 return calcStreamerPath(currentTMinfo, Long.MIN_VALUE, Long.MAX_VALUE, tmWorker); 
	}
	

	/**
	 * Calculate the path of all streamers over the loaded GPS data. 
	 * Takes a long time to calculate as we have to iterate through database to find streamers and hydrophiones then model the 
	 * streamer position. 
	 * @param millisStart - calculate streamer path from millisStart
	 * @param millisEnd - calculate streamer path up to millisEnd
	 * @param tmWorker - the thread this function is carried out on. Can be null.
	 */
	public static ArrayList<ArrayList<GpsData>> calcStreamerPath(TargetMotionInformation tmInfo, long millisStart, long millisEnd, TMInfoWorker tmWorker) {

		//first check which streamer we have in out detection group
		ArrayList<Integer> streamerIndex=AbstractTargetMotionInformation.checkStreamers(tmInfo.getCurrentDetections());
		
		HydrophoneOriginMethod originMethod;
		ListIterator<GpsData> streamerIterator;
		Streamer streamer;
		ArrayList<GpsData> streamerPath;
		ArrayList<ArrayList<GpsData>> streamersAll=new ArrayList<ArrayList<GpsData>>();
		
		//units to keep track of progress; 
		int nCount=ArrayManager.getArrayManager().getGPSDataBlock().getUnitsCount();
		int streamerCount=streamerIndex.size();
		double progress; 

		for (int i=0; i<streamerCount; i++){		
			
			streamer=ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamerIndex.get(i));
			originMethod = ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamerIndex.get(i)).getHydrophoneOrigin();
			streamerIterator = originMethod.getGpsDataIterator(PamDataBlock.ITERATOR_END);
			
			int phoneBitmap=ArrayManager.getArrayManager().getCurrentArray().getPhonesForStreamer(streamer.getStreamerIndex());
			
			/**
			 * We're going to move through the GPS data but pick out the correct streamer for each 	GPS unit. 
			 */
			GpsData streamerGPS;
			RawDataUnit tempDataUnit;
			GpsData dataUnit;
			streamerPath=new ArrayList<GpsData>();
			int n=0;
			while(streamerIterator.hasPrevious()){
				
				if (tmWorker!=null && tmWorker.isCancelled()) return null;
				
				streamerGPS=streamerIterator.previous();
				if (streamerGPS==null) continue;
				if (streamerGPS.getTimeInMillis()<millisStart || streamerGPS.getTimeInMillis()>millisEnd) continue; 
				//now that's given us the latitude and longitude of the track. Now we need to find out where the end of that streamer is!
				tempDataUnit=new RawDataUnit(streamerGPS.getTimeInMillis(), phoneBitmap, 0, 0);
				dataUnit=tempDataUnit.getOriginLatLong(false);
//				if (n%100==0){
//					System.out.println(streamerIndex);
//					streamerIterator.next();
//;					System.out.println("n: "+n+" "+dataUnit+ " height: "+dataUnit.getHeight()+ "  "+streamerIterator.previous().getStreamerData().getStreamerIndex());
//				}
				streamerPath.add(dataUnit);
				n++;
				
				if (tmWorker!=null) {
					progress=100.0*((double) n/(double) nCount)/(double)streamerCount;
					tmWorker.setTMProgress(progress);
				}

			}
			
			streamersAll.add(streamerPath);
			
		}
		
		return streamersAll;
		
	}
	
	public TargetMotionInformation getCurrentTMinfo() {
		return currentTMinfo;
	}


	public void setCurrentTMinfo(TargetMotionInformation currentTMinfo) {
		this.currentTMinfo = currentTMinfo;
	}
	
	public TMManager getTaregtMotionManager() {
		return taregtMotionManager;
	}


	public void setTaregtMotionManager(TMManager taregtMotionManager) {
		this.taregtMotionManager = taregtMotionManager;
	}
	
}
