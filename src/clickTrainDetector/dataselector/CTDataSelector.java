package clickTrainDetector.dataselector;

import PamDetection.LocContents;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import clickTrainDetector.CTDetectionGroupDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.layout.dataselector.CTDataSelectPane;
import clickTrainDetector.layout.dataselector.CTDataSelectPanel;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Data selector for the click train detector.
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class CTDataSelector extends DataSelector {

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Swing panel for the data selector selector panel.  
	 */
	private CTDataSelectPanel clickSelectPanel;
	
	/**
	 * The JavaFX pane for the data selector pane. 
	 */
	private CTDataSelectPane clickSelectPaneFX;
	
	/**
	 * CT select params
	 */
	private CTSelectParams ctSelectParams = new CTSelectParams(); 
	

	private boolean allowScores;

	public CTDataSelector(ClickTrainControl clickTrainControl, ClickTrainDataBlock clickTrainDataBlock, 
			String selectorName, boolean allowScores) {
		super(clickTrainDataBlock, selectorName, allowScores);
		this.clickTrainControl = clickTrainControl;
		this.allowScores = allowScores;

	}

	@Override
	public CTDataSelectPanel getDialogPanel() {
		if (clickSelectPanel == null) {
			clickSelectPanel = new CTDataSelectPanel(this, allowScores);
		}
		return clickSelectPanel;
	}
	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (clickSelectPaneFX == null) {
			clickSelectPaneFX = new CTDataSelectPane(this, allowScores);
		}
		return clickSelectPaneFX;
	}


	/**
	 * Get the click train control. 
	 * @return the clickControl
	 */
	public ClickTrainControl getClickControl() {
		return clickTrainControl;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		ctSelectParams=(CTSelectParams) dataSelectParams;
		getDialogPanel().setParams(ctSelectParams);
	}

	@Override
	public DataSelectParams getParams() {
		return ctSelectParams;
	}
	
	/**
	 * Get selector parameters.
	 * @return the selector parameters.
	 */
	public CTSelectParams getCTSelectParams() {
		return ctSelectParams;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		CTDetectionGroupDataUnit ctDataUnit = (CTDetectionGroupDataUnit) pamDataUnit;
		
		if (ctDataUnit.getSubDetectionsCount()<ctSelectParams.minSubDetections) return 0; 
	
		if (ctSelectParams.needsLoc && pamDataUnit.getLocalisation()==null) return 0; 
		
		if (ctSelectParams.needsLoc && !pamDataUnit.getLocalisation().hasLocContent(LocContents.HAS_RANGE)) return 0; 

		
		return 1;
	}

}
