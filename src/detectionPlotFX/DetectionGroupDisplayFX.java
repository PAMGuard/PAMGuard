package detectionPlotFX;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import javafx.scene.layout.Region;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;

/**
 * A group detection display with all the bits added to allow the display to be used in the FX GUI as
 * a stand alone user display. 
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
	
	public DetectionGroupDisplayFX(DetectionDisplayControl2 displayControl){
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
		// TODO Auto-generated method stub
		return false;
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
		
		System.out.println("SAVE DETECTION DISPLAY DATA SOURCE: " + detectionPlotParams.dataSource);
		System.out.println("SAVE DETECTION DISPLAY TAB NAME: " + detectionPlotParams.tabName);
		
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
		
		System.out.println("DETECTION DISPLAY DATA SOURCE: " + settings.dataSource);
		System.out.println("DETECTION DISPLAY DATA SOURCE: " + settings.tabName);

		
		this.detectionPlotParams = settings.clone();	
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

}
