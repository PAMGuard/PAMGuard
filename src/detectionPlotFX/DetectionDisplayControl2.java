package detectionPlotFX;

import java.util.ArrayList;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;


/**
 * 
 * Detection plot which can display single or groups of detections.
 *  
 * @author Jamie Macaulay
 *
 */
public class DetectionDisplayControl2 extends UserDisplayControlFX {


	/**
	 * The display
	 */
	private DetectionGroupDisplayFX detectionDisplay;

	/**
	 * A list of displays-> really redundant as there is only ever one display. 
	 */
	private ArrayList<UserDisplayNodeFX> displays;

	/**
	 * The data observer that
	 */
	private DetectionDataObserver detectionDataObserver;
	
	
	private PamInternalPane internalFrame;; 


	public DetectionDisplayControl2(String unitName) {
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
		super.addCompatibleUnit(PamDataUnit.class);
		//also add data unit
	}


	@Override
	public void notifyModelChanged(int type){
		//			System.out.println("NOTIFICATION: " + type); 
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
		if (this.detectionDisplay!=null) this.detectionDisplay.notifyModelChanged(type);
	}

	
	/**
	 * Set the display to show detections form the data block set in the data model- 
	 * i.e. show it's parent data block. 
	 */
	protected void displayToDataModel(PamDataBlock parentDataBlock){
		//remove any TDDataInfos which are not present in the data block list 
		//if the datablock the same do nothing
		newDataBlockAdded(parentDataBlock); 
	}

	
	/**
	 * Called whenever a new data block is added to the display.
	 *  Removes the observer from all other data blocks in the model
	 * and then the adds the observer to that data block;
	 * @return true if the parent has successfully been added. 
	 */
	public boolean newDataBlockAdded(PamDataBlock pamDataBlock){

		//System.out.println("NEW DATA BLOCK DETECTION DISPLAY: " + pamDataBlock); 

		//if null then no parent- simply set the DDataInfo to null; 
		if (pamDataBlock==null) {
			this.detectionDisplay.clearDisplay();
			return true; 
		}

		//now add the observer to the data block
		//System.out.println("DetectionDisplayControl: Adding observer to data block: "+ pamDataBlock.getDataName());
		pamDataBlock.addObserver(this.detectionDataObserver);

		return true; 
	}

	
	@Override
	public ArrayList<UserDisplayNodeFX> getDisplays(){
		if (displays==null){
			detectionDisplay=new DetectionGroupDisplayFX(this);
			//set the paramters. 
			displays=new ArrayList<UserDisplayNodeFX>();
			displays.add(detectionDisplay);
			//TODO
//			detectionDisplay.setEnableScrollBar(false); //make this an option
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
			//System.out.println("DetectionDisplay: INCOMING data unit: "+ arg);
			//send the data unit to the display. 
			detectionDisplay.setDataUnit(arg);
		}

		@Override
		public String getObserverName() {
			return "Detection Display FX";
		}
	}



}
