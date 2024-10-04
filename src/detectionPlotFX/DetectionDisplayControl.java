package detectionPlotFX;

import java.util.ArrayList;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.data.DDPlotRegister;
import detectionPlotFX.layout.DetectionPlotDisplay;
import javafx.application.Platform;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;

/**
 * 
 * The controlled unit for the detection display. A detection display can only have one parent data block.
 *  
 * @author Jamie Macaulay
 *
 */
@Deprecated
public class DetectionDisplayControl extends UserDisplayControlFX  {

	/**
	 * The display
	 */
	private DetectionPlotDisplay detectionDisplay;
	
	/**
	 * A list of displays-> really redundant as there is only ever one display. 
	 */
	private ArrayList<UserDisplayNodeFX> displays;
	
	/**
	 * The data observer that
	 */
	private DetectionDataObserver detectionDataObserver;
	

	public DetectionDisplayControl(String unitName) {
		super(unitName);
		//set which data blocks can connect as parents. 
		setCompatibleDataUnits();
		//create the observer for the parent data block. 
		detectionDataObserver=new DetectionDataObserver();
		setMultiParent(false);
		
	
	}
	
	/**
	 * Set compatible data units in the process for this display. This allows the data model to determine if connections can
	 * be made to the display. 
	 */
	@SuppressWarnings("unchecked")
	private void setCompatibleDataUnits(){
		super.removeCompatibleDataUnits();
		ArrayList<DDDataProvider> ddPlotPorviders=DDPlotRegister.getInstance().getDataInfos();
		for (int i=0; i<ddPlotPorviders.size(); i++){
			super.addCompatibleUnit(ddPlotPorviders.get(i).getDataBlock().getUnitClass());
//			System.out.println("DDDisplayController: Display compatible units "+ddPlotPorviders.get(i).getDataBlock().getUnitClass());
		}
	}
	

	@Override
	public void notifyModelChanged(int type){
		super.notifyModelChanged(type);
		switch (type){
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			setCompatibleDataUnits();
			break; 
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			setCompatibleDataUnits();
			break; 
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			//this is were the data block may have been added. Need to add an observer to this data block to say when the thing has 
			//thing has a new detection. 
			displayToDataModel(this.getUserDisplayProcess().getParentDataBlock());
			break;
		}
		//this.detectionDisplay.notifyModelChanged(type);
	}
	
	/**
	 * Get currently displayed data blocks in the display and set them as the parent data 
	 * blocks in the display process.  This will change the datamodel to show the correct connections. 
	 */
	public void dataModelToDisplay() {
		DDDataInfo displayDataBlock=this.detectionDisplay.getCurrentDataInfo();
		this.getUserDisplayProcess().setParentDataBlock(displayDataBlock.getDataBlock()); 
//		PamController.getInstance().notifyModelChanged(PamControllerInterface.UPDATE_DATA_MODEL);
	}

	/**
	 * Set the display to show detections form the data block set in the data model- 
	 * i.e. show it's parent data block. 

	 */
	protected void displayToDataModel(PamDataBlock parentDataBlock){
		//remove any TDDataInfos which are not present in the data block list 
		DDDataInfo displayDataBlock=detectionDisplay.getCurrentDataInfo();
		//if the datablock the same do nothing
		if (displayDataBlock==null || displayDataBlock.getDataBlock()!=parentDataBlock){
			newDataBlockAdded(parentDataBlock); 
		}

	}
	
	/**
	 * Called whenever a new data block is added to the display.
	 *  Removes the observer from all other data blocks in the model
	 * and then the adds the observer to that data block;
	 * @return true if the parent has successfully been added. 
	 */
	public boolean newDataBlockAdded(PamDataBlock pamDataBlock){
		
		System.out.println("New datablock added: " + pamDataBlock.getDataName());

		//now must find the data provider. 
		//first get the list of dataInfos. 
		
		//22/05/206. Need to check we have the correct datablock before doing anything else. The datablock should
		//be null or of the correct type. If not need to keep the last connection. 
		DDDataProvider newDataProviderFX=DDPlotRegister.getInstance().findDataProvider(pamDataBlock);
		if (newDataProviderFX==null && pamDataBlock!=null){
			//something pretty seriously wrong has occured. 
			if (pamDataBlock!=null) System.err.println("DetectionDisplay: could not find a DDDataProvider for the datablock: "+pamDataBlock.getDataName());
			return false; 
		}
		
		//remove all the observers, remember that only one parent can be added to the detection 
		//display at any one time. 
		ArrayList<PamDataBlock> dataBlocks=PamController.getInstance().getDataBlocks();
		for (int i=0; i<dataBlocks.size(); i++){
			dataBlocks.get(i).deleteObserver(detectionDataObserver);
		}
		
		//if null then no parent- simply set the DDataInfo to null; 
		if (pamDataBlock==null) {
			this.detectionDisplay.removeDataInfo(); 
			return true; 
		}
		
		//add the data info to the display- the detection display only ever has one data info. 
		this.detectionDisplay.setDataInfo(newDataProviderFX); 
		
		//now add the observer to the data block
		//System.out.println("DetectionDisplayControl: Adding observer to data block: "+ pamDataBlock.getDataName());
		pamDataBlock.addObserver(this.detectionDataObserver);

		return true; 
	}
	
	@Override
	public ArrayList<UserDisplayNodeFX> getDisplays(){
		if (displays==null){
			detectionDisplay=new DetectionPlotDisplay();
			displays=new ArrayList<UserDisplayNodeFX>();
			//displays.add(detectionDisplay);
		}
		return displays;
	}
	

	
	/**
	 * 
	 * The data observer monitors incoming data from data blocks. 
	 * 
	 * @author Doug Gillespie and Jamie Macaulay
	 *
	 */
	private class DetectionDataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 1000; //no data histroy required for this click. 
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			System.out.println("DetectionDisplay: Incoming data unit: "+arg.getParentDataBlock().getDataName());
			//send the data unit to the display. 
			//TODO-why?
			Platform.runLater(()->{
				detectionDisplay.newDataUnit(arg); 
			}); 
		}

		@Override
		public String getObserverName() {
			return "Detection Display FX";
		}

	}
}
