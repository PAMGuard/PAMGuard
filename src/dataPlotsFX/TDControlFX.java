package dataPlotsFX;

import java.io.Serializable;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;
import userDisplayFX.UserDisplayNodeParams;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import detectiongrouplocaliser.DetectionGroupSummary;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;

/**
 * 
 * The controller for the display if the main PAMGuard GUI is in JavaFX mode. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class TDControlFX extends TDControl implements UserDisplayNodeFX {

	/**
	 * The control FX. 
	 */
	private TDControlFX tdControlfx;

	/**
	 * Checks for incoming data. 
	 */
	private DataObserver dataObserver;

	/**
	 * Reference to the display controller. 
	 */
	private TDDisplayController tdDisplayController;

	/**
	 * 
	 * The internal frame holding the display. 
	 */
	private PamInternalPane internalFrame;

	/**
	 * Constructor for the TDControlFX. 
	 * @param tdDisplayController - reference to the display controller. 
	 * @param uniqueDisplayName - the unique display name
	 */
	public TDControlFX(TDDisplayController tdDisplayController, String uniqueDisplayName){
		super(uniqueDisplayName);
		this.setUniqueName(uniqueDisplayName);
		this.tdDisplayController=tdDisplayController; 
		tdControlfx=this;		
		//		this.getTdParameters().print(); //PRINT
		create();
	}

	/**
	 * Create the vital components for the display. 
	 */
	private void create(){
		dataObserver = new DataObserver();
	}


	@Override
	public Region getNode() {
		if (tdMainDisplay==null) {
			tdMainDisplay=new TDDisplayFX(tdControlfx);
			dataModelToDisplay(); //make sure data model shows the display- otherwise on the 
			//next notification the display will be overwritten with parent data blocks form the
			//data model. 
		}

		return tdMainDisplay;
	}

	public double getScrollableRange(){
		return tdMainDisplay.getTDParams().scrollableTimeRange;
	}

	public double getVisibleRange(){
		return tdMainDisplay.getTDParams().visibleTimeRange;
	}

	public void scrollDisplayEnd(final long milliSeconds) {
		if (tdMainDisplay!=null){
			tdMainDisplay.scrollDisplayEnd(milliSeconds);
		}
	}

	/**
	 * Get all parent data blocks- that is parent data blocks currently in the
	 * process. The first data block in the list is the main parent data block. The
	 * rest are multiplex data blocks. (For this module this difference is
	 * irrelevant).
	 * 
	 * @return list of PamDataBlocks for the display. Element(0) might be null.
	 */
	@SuppressWarnings("rawtypes")
	private ArrayList<PamDataBlock> getParentDataBlocks(){
		// so if the process has changed need to check that the display has been set up properly.
		ArrayList<PamDataBlock> dataBlocks=new ArrayList<PamDataBlock>();
		PamDataBlock dataBlock=this.tdDisplayController.getUserDisplayProcess().getParentDataBlock();
		if (TDDataProviderRegisterFX.getInstance().findDataProvider(dataBlock)!=null) dataBlocks.add(dataBlock);
		if (dataBlock!=null) {
			System.out.println("TDControldFX: parent datablock "+dataBlock.getDataName());
		}
		else{
			System.out.println("TDControldFX: parent datablock null"); 
			return dataBlocks; 
		}
		
		for (int i=0; i<tdDisplayController.getUserDisplayProcess().getNumMuiltiplexDataBlocks(); i++){
			dataBlock=this.tdDisplayController.getUserDisplayProcess().getMuiltiplexDataBlock(i); 
			if (TDDataProviderRegisterFX.getInstance().findDataProvider(dataBlock)!=null){
				dataBlocks.add(dataBlock);
			}
		}
		return dataBlocks;
	}

	/**
	 * Get all data blocks which are currently being displayed. 
	 * @return all TDDataInfoFX (which wrap a PamDataBlock) which are currently being displayed. 
	 */
	private ArrayList<TDDataInfoFX> getDisplayTDDataInfos(){
		ArrayList<TDDataInfoFX> dataInfoList=new ArrayList<TDDataInfoFX>();
		ArrayList<TDDataInfoFX> dataInfos;
		for (int i=0; i<this.getTDDisplay().getTDGraphs().size(); i++){
			dataInfos=this.getTDDisplay().getTDGraphs().get(i).getDataList();
			for (int j=0; j<dataInfos.size(); j++){
				dataInfoList.add(dataInfos.get(j));
			}
		}
		return dataInfoList;
	}

	/**
	 * Get currently displayed data blocks in the display and set them as the parent
	 * data blocks in the display process.
	 */
	public void dataModelToDisplay() {
		//
//		System.out.println("TDControlFX: dataModelToDisplay: " + tdDisplayController.allowProcessNotify);

		if (this.tdMainDisplay==null) return; 
		/**
		 * BUG: 13/07/2015- needed to add line below to stop weird loop which meant a node in the data model would connect,
		 * disconnect and then connect. 
		 */
		if (!tdDisplayController.allowProcessNotify) return;
		
		ArrayList<TDDataInfoFX> displayDataBlocks=getDisplayTDDataInfos(); 
//		for (int i=0; i<displayDataBlocks.size(); i++){
//			System.out.println("TDControlFX:+ Current display datablocks: "+displayDataBlocks.get(i).getDataName()); 
//		}
		
		tdDisplayController.setParentDataBlocks(displayDataBlocks); 
		//		PamController.getInstance().notifyModelChanged(PamControllerInterface.UPDATE_DATA_MODEL);
	}


	/**
	 * Set the display to show data blocks set in the data model- i.e. show it's
	 * parent data blocks. Iterates through all TDDataInfoFX (a PamDataBlock
	 * wrapper) currently displayed. If any TDDataInfo does not have a data block in
	 * the displayDataBlocks it is deleted. If any PamDataBlock in the list does not
	 * have a corresponding TDDataInfoFX then a new TDDataINfoFX is created and
	 * added to a new TDGraphFX in the main display.
	 */
	protected void displayToDataModel(ArrayList<PamDataBlock> displayDataBlocks){

//		System.out.println("TDControlFX: displayToDataModel");
//		System.out.println("*****************");
//		for (int i=0; i<displayDataBlocks.size(); i++) {
//			System.out.println(displayDataBlocks.get(i).getDataName());
//		}
//		System.out.println("*****************");

		
		//remove any TDDataInfos which are not present in the data block list 
		boolean found=false;
		ArrayList<TDDataInfoFX> dataInfos=getDisplayTDDataInfos();

		for (int i=0;i<dataInfos.size(); i++){
			found=false; 
			for (int j=0; j<displayDataBlocks.size(); j++){
				if (dataInfos.get(i).getDataBlock()==displayDataBlocks.get(j)){
					found=true;
					continue;
				}
			}
			if (!found){
				removeTDDataInfo(dataInfos.get(i)); 
			}
		}

		//now add any data blocks which are not present in TDDataInfo 
		for (int i=0; i<displayDataBlocks.size(); i++){
			found=false; 
			for (int j=0; j<dataInfos.size(); j++){
				if (dataInfos.get(j).getDataBlock()==displayDataBlocks.get(i)){
					found=true; 
					continue; 
				}
			}
			if (!found) addDataBlock(displayDataBlocks.get(i)); 
		}
		
//		tdDisplayController.printParentDataBlocks(); 
//		System.out.println("TDControlFX: displayToDataModel: finished: " + getParentDataBlocks().size());
//		for (int i=0; i<tdMainDisplay.getTDGraphs().size(); i++) {
//			tdMainDisplay.getTDGraphs().get(i).printDataInfos();
//		}

	}


	/**
	 * Called whenever process settings have changed
	 */
	public void processSettingsChanged() {
		//make sure display shows correct data blocks 
		if (tdMainDisplay!=null) displayToDataModel(getParentDataBlocks());
	}


	/**
	 * The data observer monitors only the raw data source in real time
	 * so that scrolling can take place. Need to set up a different
	 * set of observers to monitor data, primarily to set correct 
	 * data history requirements. 
	 * @author Doug Gillespie and Jamie Macaulay
	 *
	 */
	private class DataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			if (PamRawDataBlock.class == o.getClass()) {
				return 0;
			}
			//System.out.println("TDControlFX: Get required data history: "+ Math.max(getScrollableRange(), getVisibleRange()));
			long nomTime = (long) Math.max(getScrollableRange(), getVisibleRange());
			return nomTime;

		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			//			scrollDisplayEnd(arg.getTimeMilliseconds());
			//			System.out.println("Observer: " + o);
		}

		@Override
		public String getObserverName() {
			return tdControlfx.getUniqueName();
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			//			System.out.println("TDControlFX: masterClockUpdate");
			Platform.runLater(() -> {
				scrollDisplayEnd(milliSeconds);
			});
		}


	}

	/**
	 * Get the data observer- monitors incoming real time data an updates graphs. 
	 * @return data observer
	 */
	public DataObserver getDataObserver() {
		return dataObserver;
	}


	@Override
	public void openNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeNode() {
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if (tdMainDisplay!=null){
			tdMainDisplay.notifyModelChanged(changeType);
		}
	}

	/**
	 * In real time mode check if PAMGUARD is paused. 
	 * @return true if paused. 
	 */
	public boolean isPaused(){
		if (PamController.getInstance().getPamStatus()==PamController.PAM_RUNNING) return false; 
		else return true; 
	}


	@Override
	public boolean requestNodeSettingsPane() {
		/**
		 * Open all hiding tabs to show user where everything is. 
		 */
		//show the control pane.
		tdMainDisplay.showControlPane(true);
		//expand all settings panes. 
		for (int i=0; i<tdMainDisplay.getTDGraphs().size(); i++){
			tdMainDisplay.getTDGraphs().get(i).showSettingsPane(true); 
			tdMainDisplay.getTDGraphs().get(i).showAxisSettingsPane(true); 
		}
		return true;
	}

	@Override
	public boolean isStaticDisplay() {
		return false;
	}

	@Override
	public boolean isResizeableDisplay() {
		return true;
	}

	@Override
	public String getName() {
		return "Time Base Display";
	}

	@Override
	public boolean isMinorDisplay() {
		// these are generally smaller minor displays- only used for automatic resize. 
		return false;
	}

	/**
	 * Update the provider register. This removes any data block which no longer exist in the data model. 
	 */
	public void updateProviderRegister() {
		ArrayList<TDDataProviderFX> dataInfos=TDDataProviderRegisterFX.getInstance().getDataInfos();
		for (int i=0; i<dataInfos.size() ;i++){
			if (!PamController.getInstance().getDataBlocks().contains(dataInfos.get(i).getDataBlock())){
				//no datablock in model. must unregister!
				TDDataProviderRegisterFX.getInstance().unRegisterDataInfo(dataInfos.get(i));
			}
		}
	}
	
	/**
	 * Called just before settings are saved. Will have to go 
	 * through all the graphs and get them to provide updated settings
	 * information to add to this since it's not kept up to date on the fly. 
	 * @return object to serialise.
	 */
	protected Serializable prepareSerialisedSettings() {
		 super.prepareSerialisedSettings();
		 //prepare the serialised settings. 
//		 System.out.println("TDControlFX: Saving the position of the display: "
//				 + internalFrame.getInternalRegion().getLayoutX() + "  " + internalFrame.getInternalRegion().getLayoutY()); 
		 
		 //need to use the parent node because inside an internal pane. 
		 this.getTdParameters().displayProviderParams.positionX=internalFrame.getInternalRegion().getLayoutX();
		 this.getTdParameters().displayProviderParams.positionY=internalFrame.getInternalRegion().getLayoutY();
		 this.getTdParameters().displayProviderParams.sizeX=internalFrame.getInternalRegion().getWidth();
		 this.getTdParameters().displayProviderParams.sizeY=internalFrame.getInternalRegion().getHeight();

		return this.getTdParameters();
	}

	@Override
	public UserDisplayNodeParams getDisplayParams() {
		//the display provider params are stored in the settings. 
		return this.getTdParameters().displayProviderParams;
	}

	@Override
	public void setFrameHolder(PamInternalPane internalFrame) {
		this.internalFrame=internalFrame; 
	}
	

	@Override
	public void newSelectedDetectionGroup(DetectionGroupSummary detectionGroup, TDGraphFX tdGraph) {
//		System.out.println("New selected detection group: " + detectionGroup);
		
		tdDisplayController.getDisplayDataBlock().clearAll();
		if (detectionGroup==null || detectionGroup.getDataList().size()<=0) return;
		
//		System.out.println("Add pam data: " + detectionGroup + " " + tdDisplayController.getDisplayDataBlock().countObservers());
//		for (int i=0; i<tdDisplayController.getDisplayDataBlock().countObservers() ; i++) {
//			System.out.println("Observer : " + tdDisplayController.getDisplayDataBlock().getPamObserver(i));
//		}

		tdDisplayController.getDisplayDataBlock().addPamData(detectionGroup.getDataList().get(detectionGroup.getFocusedIndex()));
		if (isViewer()) tdDisplayController.getDisplayDataBlock().notifyNornalObservers(detectionGroup.getDataList().get(detectionGroup.getFocusedIndex()));
		
	}

	@Override
	public UserDisplayControlFX getUserDisplayControl() {
		return this.tdDisplayController;
	}
}
