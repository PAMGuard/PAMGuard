package dataPlotsFX;

import java.util.ArrayList;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import userDisplayFX.UserDisplayControlFX;
import userDisplayFX.UserDisplayNodeFX;

/**
 * The controller for the TD display. This is only used in the FX GUI where displays
 * tend to have equal parity with modules. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class TDDisplayController extends UserDisplayControlFX {
	
	
	/**
	 * List of the current displays. 
	 */
	private ArrayList<UserDisplayNodeFX> displays;

	/**
	 * Reference to the display control class- this is what really cont9rols the display. 
	 */
	private TDControlFX tdControlFX;
	
	private PamDataBlock selectedDataUnits;
	
	

	public TDDisplayController(String unitName) {
		super(unitName);
		//set the compatible data units. 
		 //indicate that this display can accept multiple parent data blocks at the same time. 
		this.setMultiParent(true);
		
		//set which PamDataUnits the display can show and therefore which data blocks it can accept as parents.
		setCompatibleDataUnits();
		
		selectedDataUnits = new PamDataBlock(PamDataUnit.class, "Selected Data Units", getUserDisplayProcess() , Integer.MAX_VALUE); //TODO
		
		getUserDisplayProcess().addOutputDataBlock(selectedDataUnits);
		

	}
	
	/**
	 * Set compatible data units in the process for this display. This allows the
	 * data model to determine if connections can be made to the display.
	 */
	private void setCompatibleDataUnits(){
		super.removeCompatibleDataUnits();
		
		ArrayList<TDDataProviderFX> tdPlotProviders=TDDataProviderRegisterFX.getInstance().getDataInfos();
		for (int i=0; i<tdPlotProviders.size(); i++){
			//System.out.println(tdPlotProviders.get(i).getDataBlock().getUnitClass());
			super.addCompatibleUnit(tdPlotProviders.get(i).getDataBlock().getUnitClass());
			//System.out.println("TDDisplayController: Display compatible units "+tdPlotProviders.get(i).getDataBlock().getUnitClass());
		}
	}
	
	/**
	 * Set the parent datablocks
	 * @param dataBlocks - the parent datablocks to set. 
	 */
	protected void setParentDataBlocks(ArrayList<TDDataInfoFX> dataBlocks){
		allowProcessNotify=false; 
		
		while (getUserDisplayProcess().getNumMuiltiplexDataBlocks()>0){
			getUserDisplayProcess().removeMultiPlexDataBlock(getUserDisplayProcess().getMuiltiplexDataBlock(0));
		}
		
		if (dataBlocks.size()==0) getUserDisplayProcess().setParentDataBlock(null);
		for (int i=0; i<dataBlocks.size(); i++){
			if (i==0) getUserDisplayProcess().setParentDataBlock(dataBlocks.get(0).getDataBlock());
			else getUserDisplayProcess().addMultiPlexDataBlock(dataBlocks.get(i).getDataBlock());
		}
		
		allowProcessNotify=true; 
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}
	
	//disables notifications. 
	boolean allowProcessNotify=true;
	
	@Override
	public void notifyModelChanged(int type){
//		System.out.println("---------------------------------" ); 
		//System.out.println("TDisplayController: flag: " + type); 
		super.notifyModelChanged(type);
		switch (type){
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			setCompatibleDataUnits();
			if (allowProcessNotify){
				allowProcessNotify=false; 
				tdControlFX.processSettingsChanged();
				allowProcessNotify=true;
			}
			break; 
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			setCompatibleDataUnits();
			break; 
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			//might need to update the register to remove any data info
			tdControlFX.updateProviderRegister();
			//need to make sure any graphs which have a now non existent data block
			//are removed.
			setCompatibleDataUnits();
			//now update the graphs! 04/02/2019 Added initialization complete here because otherwise
			//when the data model is starting it can send more than one message to the 
			//display whihc means it does not load the saved settings correctly. 
			if (PamController.getInstance().isInitializationComplete() && allowProcessNotify){
				allowProcessNotify=false; 
				tdControlFX.processSettingsChanged();
				allowProcessNotify=true;
			}
			break; 		
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			if (PamController.getInstance().isInitializationComplete() && allowProcessNotify){
				allowProcessNotify=false; 
				tdControlFX.processSettingsChanged();
				allowProcessNotify=true;
			}
			break; 
		}
		//send notification along to tdControlFX
		//tdGUI.getTDControl().notifyModelChanged(type);
//		System.out.println("---------------------------------" ); 
	}
	
	@Override 
	public ArrayList<UserDisplayNodeFX> getDisplays(){
		if (displays==null){
			tdControlFX=new TDControlFX(this, getUnitName());
			displays=new ArrayList<UserDisplayNodeFX>();
			displays.add(tdControlFX);
		}
		return displays;
	}


	/**
	 * Print the current parent data blocks. Used for debugging. 
	 */
	public void printParentDataBlocks() {
		ArrayList<TDDataProviderFX> tdPlotProviders=TDDataProviderRegisterFX.getInstance().getDataInfos();
		for (int i=0; i<tdPlotProviders.size(); i++){
			System.out.println(tdPlotProviders.get(i).getDataBlock().getUnitClass());
		}
	}

	/**
	 * Get the datablock for selected data units from the display. 
	 * @return datablock for selected data units. 
	 */
	public PamDataBlock getDisplayDataBlock() {
		return selectedDataUnits; 
	}
	

}
