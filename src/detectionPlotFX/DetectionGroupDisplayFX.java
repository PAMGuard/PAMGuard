package detectionPlotFX;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import javafx.geometry.Side;
import javafx.scene.layout.Region;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;

/**
 * A group detection display with all the bits added to allow the display to be used in the FX GUI as
 * a stand-alone user display. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DetectionGroupDisplayFX extends DetectionGroupDisplay  implements UserDisplayNodeFX, PamSettings{
	
	private DetectionPlotParams  detectionPlotParams = new DetectionPlotParams();

	/**
	 * Reference to the internal frame that migfth hold this graph. 
	 */
	private PamInternalPane internalFrame;

	private DetectionDisplayControl2 displayControl;

	private PamDataUnit<?, ?> currentDetection; 
	
	public DetectionGroupDisplayFX(DetectionDisplayControl2 displayControl){
		super(DetectionGroupDisplay.DISPLAY_COMPACT);
		this.displayControl = displayControl; 
		//register the settings. 
		PamSettingManager.getInstance().registerSettings(this);		
	}

	@Override
	public String getName() {
		return "Detection Dsiplay";
	}

	@Override
	public Region getNode() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void openNode() {
		// TODO Auto-generated method stub
		
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
	public boolean isMinorDisplay() {
		return true;
	}

	@Override
	public void closeNode() {};
	
	@Override
	public DetectionPlotParams getDisplayParams() {		
		
		return this.detectionPlotParams;
	}
	
	private void prepareDisplayParams() {
		if (displayControl.getUserDisplayProcess().getParentDataBlock()!=null) {
			detectionPlotParams.dataSource = displayControl.getUserDisplayProcess().getParentDataBlock().getLongDataName();
		}
		else detectionPlotParams.dataSource = null;
		
		detectionPlotParams.showScrollBar = this.isEnableScrollBar();
		
		if (this.internalFrame!=null) {
			 //need to use the parent node because inside an internal pane. 
			detectionPlotParams.positionX=internalFrame.getInternalRegion().getLayoutX();
			detectionPlotParams.positionY=internalFrame.getInternalRegion().getLayoutY();
			detectionPlotParams.sizeX=internalFrame.getInternalRegion().getWidth();
			detectionPlotParams.sizeY=internalFrame.getInternalRegion().getHeight();
		}
	}



	@Override
	public void setFrameHolder(PamInternalPane internalFrame) {
		this.internalFrame=internalFrame;
		
	}

	@Override
	public boolean requestNodeSettingsPane() {
		this.showSettingsPane(true);
		
		return true;
	}
	

	private void showSettingsPane(boolean b) {
		this.detectionDisplay.getHidingPane(Side.RIGHT).showHidePane(b);;
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(detectionPlotParams.dataSource); 
			//set the correct parent data block if on exists
			displayControl.getUserDisplayProcess().setParentDataBlock(dataBlock);	
			displayControl.displayToDataModel(dataBlock);
		
			break;
		}
		
	}

	/**
	 * Set the display parameterts. 
	 * @param detectionPlotParams
	 */
	public void setDisplayParams(DetectionPlotParams detectionPlotParams) {
		this.detectionPlotParams = detectionPlotParams;
		
	}
	
	@Override
	public Serializable getSettingsReference() {
		Serializable set = prepareSerialisedSettings();
		return set;
	}

	/**
	 * Prepare settings for saving. 
	 * @return
	 */
	private Serializable prepareSerialisedSettings() {
		if (detectionDisplay==null) return null; 
		prepareDisplayParams();
		detectionPlotParams = getDisplayParams();
		
//		System.out.println("SAVE DETECTION DISPLAY DATA SOURCE: " + detectionPlotParams.dataSource);
//		System.out.println("SAVE DETECTION DISPLAY TAB NAME: " + detectionPlotParams.tabName);
	
		return detectionPlotParams;	
	}
	

	@Override
	public long getSettingsVersion() {
		 return DetectionPlotParams.serialVersionUID;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try{
			return restoreSettings((DetectionPlotParams) pamControlledUnitSettings.getSettings());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}


	private boolean restoreSettings(DetectionPlotParams settings) {
		if (settings == null) {
			return false;
		}
		
//		System.out.println("LOAD DETECTION DISPLAY DATA SOURCE: " + settings.dataSource);
//		System.out.println("LOAD DETECTION DISPLAY DATA SOURCE: " + settings.tabName);
		
		this.detectionPlotParams = settings.clone();	
		
		
		this.setEnableScrollBar(detectionPlotParams.showScrollBar);
		
		return true;
	}

	@Override
	public String getUnitName() {
		return displayControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return displayControl.getUnitType();
	}

	@Override
	public UserDisplayControlFX getUserDisplayControl() {
		return displayControl;
	}
	
	@Override
	public boolean setDataUnit(PamDataUnit<?, ?> dataUnit){
		
		/**
		 * The extra stuff here is to make sure that the plot types for a specific detection are saved. So for example 
		 * if viewing click spectrum then the spectrum plot is selected whenever 1) PAMGuard is opened again or 2) switching from
		 * one type of detection to another e.g. whistle to click, then the click does not revert to showing a waveform instead 
		 * of spectrum. 
		 */
		if (currentDetection!=null) {
			//save the current selected detection plot for the particular type of data unit.
			String detectionPlotName = 	this.getDetectionDisplay().getCurrentDataInfo().getCurrentDetectionPlot().getName();
			//System.out.println("SET CURRENT DETECTION PLOT TO USE IS: " + detectionPlotName);
			detectionPlotParams.dataAxisMap.put(currentDetection.getParentDataBlock().getLongDataName(), detectionPlotName);
		}
		
		this.currentDetection = dataUnit;

		//setup the new data unit
		boolean newDataInfo = super.setDataUnit(dataUnit);
		
		if (newDataInfo && dataUnit!=null) {
		//if there's a new data info we may want to set the detection back to it's most recent selection
			String detectionPlotName = 	detectionPlotParams.dataAxisMap.get(dataUnit.getParentDataBlock().getLongDataName());
//			System.out.println("THE CURRENT DETECTION PLOT TO USE IS: " + detectionPlotName);
			setDetectionPlot(detectionPlotName);
		}
		
		return newDataInfo;
	}

}
