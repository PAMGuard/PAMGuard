package clickTrainDetector.dataselector;

import PamDetection.LocContents;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.CTDetectionGroupDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.classification.CTClassification;
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

	private boolean[] useSpeciesList;

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
		getDialogPanel().getParams(ctSelectParams);
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
		
		if (!isClassified(ctDataUnit)) return 0; 
	
		if (ctSelectParams.needsLoc && pamDataUnit.getLocalisation()==null) return 0; 
		
		if (ctSelectParams.needsLoc && !pamDataUnit.getLocalisation().hasLocContent(LocContents.HAS_RANGE)) return 0; 

		return 1;
		
	}

	/**
	 * Check whether a click train passes the data selector classification criteria. 
	 * @param ctDataUnit - the click train data unit to test.
	 * @return true of the click train passes detection criterea. 
	 */
	private boolean isClassified(CTDetectionGroupDataUnit ctDataUnit) {

		if (ctSelectParams.allowAnyClassification) return true; 
		
		if (ctDataUnit instanceof CTDataUnit) {

			CTDataUnit clickTrain = (CTDataUnit) ctDataUnit; 

			if (clickTrain.ctClassifications==null) return false; 
			
			if (ctSelectParams.classifier == null) {
				return false;
			}

			int nClass = clickTrain.ctClassifications.size();
			if (ctSelectParams.allowMultipleChoices == false) {
				int clsInd = clickTrain.getClassificationIndex();
				if (clsInd < 0) {
					return false;
				}
				if (clsInd >= clickTrain.ctClassifications.size()) {
					return false;
				}
				CTClassification singleClass = clickTrain.ctClassifications.get(clsInd);
				for (int i = 0; i < ctSelectParams.classifier.length; i++) {
					if (ctSelectParams.classifier[i] == singleClass.getSpeciesID()) {
						return true;
					}
				}
				return false;
			}
			else {
				//iterate through all the classifiers and allowed classification types. 
				for (int i=0; i<ctSelectParams.classifier.length; i++) {
					for (int j=0; j<nClass; j++) {
						if (clickTrain.ctClassifications.get(j).getSpeciesID()==ctSelectParams.classifier[i]) {
							return true; 
						}
					}
				}
			}
		}
		return false;
	}

}
