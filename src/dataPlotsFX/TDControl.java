package dataPlotsFX;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObserver;
import dataPlots.TDParameters;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;

/**
 * The main class for the TDDisplay. 
 * @author Jamie Macaulay
 *
 */
public abstract class TDControl implements PamSettings {

	/**
	 * Settings for the display. 
	 */
	private TDParametersFX tdParams;

	/**
	 * Reference to the main node for the graph. 
	 */
	protected TDDisplayFX tdMainDisplay;


	/**
	 * Boolean to indicate whether PAMGUARD is in viewer mode or not. 
	 */
	private boolean isViewer;

	/**
	 * The unique name of the display
	 */
	private String uniqueDisplayName;
	
	/**
	 * Flag for development features. 
	 */
	// replace with a command line option. 
//	public static boolean isJamieDev = false;


	public TDControl(String uniqueDisplayName){
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			isViewer = true;
		}
		setUniqueName(uniqueDisplayName);
		
		this.tdParams=new TDParametersFX() ;
		PamSettingManager.getInstance().registerSettings(this);

	}

	/**
	 * Remove a TDDataInfo which is currently being displayed. 
	 * @param tdDataInfoFX
	 */
	protected void removeTDDataInfo(TDDataInfoFX tdDataInfoFX){
		//System.out.println(" REMOVE FROM DISPLAY: "+tdDataInfoFX.getDataName());
		this.tdMainDisplay.removeTDDataInfo(tdDataInfoFX, true);

	}

	/**
	 * Add a data block to be displayed.
	 * @param pamDataBlock - the data block to add to the display
	 */
	protected void addDataBlock(PamDataBlock pamDataBlock){
		//System.out.println(" ADD TO DISPLAY: "+pamDataBlock.getDataName());
		this.tdMainDisplay.addDataBlock(pamDataBlock, null);
	}


	@Override
	public String getUnitName() {
		return getUniqueName();
	}

	@Override
	public String getUnitType() {
		return "TD Display FX";
	}

	@Override
	public Serializable getSettingsReference() {
		Serializable set = prepareSerialisedSettings();
		return set;
	}

	@Override
	public long getSettingsVersion() {
		return TDParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try{
			return TDControl.this.restoreSettings((TDParametersFX) pamControlledUnitSettings.getSettings());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Called when settings loaded. 
	 * @param settings - the new settings 
	 * @return true of the new settings are cloned. 
	 */
	private boolean restoreSettings(TDParametersFX settings) {
//		System.out.println("Settings: "+settings);
		if (settings == null) {
			return false;
		}
		this.tdParams = settings.clone();
//		tdParams.scrollableTimeRange=300000L;
	
//		System.out.println("Settings: "+settings.graphParameters.size());
		return true;
	}


	/**
	 * Called just before settings are saved. Will have to go 
	 * through all the graphs and get them to provide updated settings
	 * information to add to this since it's not kept up to date on the fly. 
	 * @return object to serialise.
	 */
	protected Serializable prepareSerialisedSettings() {
		if (tdMainDisplay == null) return null;
		tdParams.scrollableTimeRange = tdMainDisplay.getScrollableTime(); 
		tdParams.visibleTimeRange = tdMainDisplay.getVisibleTime();
		tdParams.startMillis = tdMainDisplay.getTimeStart();
		tdParams.graphParameters = new ArrayList<>();
		tdParams.splitHeights = tdMainDisplay.getSplitHeights();
		for (TDGraphFX aGraph:tdMainDisplay.getTDGraphs()) {
			tdParams.graphParameters.add(aGraph.prepareGraphParameters());
		}
//		System.out.println("Settings close: "+tdParams.graphParameters.size() + " scrollable range " +tdParams.scrollableTimeRange  );
		return tdParams;
	}


	public TDParametersFX getTdParameters() {
		return tdParams;
	}


	protected void setTDDisplay(TDDisplayFX tdDisplayFX) {
		this.tdMainDisplay=tdDisplayFX;

	}

	protected TDDisplayFX getTDDisplay() {
		return tdMainDisplay;

	}

	
	public boolean isPaused() {
		if (tdMainDisplay.needPaused()) {
			return true;
		}
		return false;
	}

	public void dataModelToDisplay(){

	}

	/**
	 * Check whether PAMGUARD is in viewer mode. 
	 * @return true if in viewer mode. 
	 */
	public boolean isViewer() {
		return isViewer;
	}

	public abstract PamObserver getDataObserver();


	public boolean isStopped() {
		return PamController.getInstance().getPamStatus()==PamController.PAM_IDLE;
	}


	public boolean isRunning() {
		return PamController.getInstance().getPamStatus()==PamController.PAM_RUNNING;
	}


	//	/**
	//	 * @return the uniqyeDisplayName
	//	 */
	//	public String getUniqueDisplayName() {
	//		return uniqueDisplayName;
	//	}
	//
	//
	//	/**
	//	 * @param uniqyeDisplayName the uniqyeDisplayName to set
	//	 */
	//	public void setUniqyeDisplayName(String uniqyeDisplayName) {
	//		this.uniqueDisplayName = uniqyeDisplayName;
	//	}

	public String getUniqueName() {
		return uniqueDisplayName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueDisplayName = uniqueName;
	}


}
