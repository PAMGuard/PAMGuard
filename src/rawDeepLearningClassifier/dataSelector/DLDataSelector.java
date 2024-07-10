package rawDeepLearningClassifier.dataSelector;

import java.util.ArrayList;
import java.util.List;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import rawDeepLearningClassifier.DLControl;

/**
 * Data selector of DL data units. Note that data selectors are really data 
 * unit specific and not annotation specific. 
 * <p>
 * The data selector can have different types of data selectors which can 
 * depend on the classifer used and user choice. 
 * <p>
 * Note that this is slightly different from DLPredicitoDecision
 * as it deals with data units that may have a more than one prediction. 
 * i.e. 
 */
public class DLDataSelector extends DataSelector {


	/**
	 * Swing panel for the data selector. 
	 */
	private DLSelectPanel dlSelectPanel;


	/**
	 * FX panel for the data selector. 
	 */
	private DLSelectPaneFX dlSelectPaneFX;

	/**
	 * Data filter for filtering. 
	 */
	private List<DLDataFilter> dataFilters = new ArrayList<DLDataFilter> ();


	private DLDataSelectorParams dlDataSelectParams;

	/**
	 * Constructor for the data selector. 
	 * @param dlcontrol - reference to the DLControl.
	 * @param pamDataBlock - the data block. 
	 * @param selectorName - the selector name. 
	 * @param allowScores - allow all the scores. 
	 * @param selectorType - the selector type. 
	 */
	public DLDataSelector(DLControl dlcontrol, PamDataBlock pamDataBlock, String selectorName, boolean allowScores, String selectorType) {
		super(pamDataBlock, selectorName, allowScores);
		/****New data filters go here****/
		dataFilters.add(new DLPredictionFilter(dlcontrol)); 
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (dlSelectPanel == null) {
			dlSelectPanel = new DLSelectPanel(this);
		}
		return dlSelectPanel;
	}
	
	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (dlSelectPaneFX == null) {
			dlSelectPaneFX = new DLSelectPaneFX(this);
		}
		return dlSelectPaneFX;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		
		if (dataSelectParams instanceof DLDataSelectorParams) {
			dlDataSelectParams = (DLDataSelectorParams) dataSelectParams; 

			checkDataFilterParamsSize(dlDataSelectParams); 

			//set paramters for all data filters. 
			for (int i=0; i<dlDataSelectParams.dataSelectorParams.length; i++) {
				dataFilters.get(i).setParams((DLDataSelectorParams) dataSelectParams); 
			}
		}

	}

	/**
	 * Check that the data filters are the corret size.
	 * @param dataSelectParams - the apramters to set. 
	 */
	private void checkDataFilterParamsSize(DLDataSelectorParams dataSelectParams) {
		if (dataSelectParams.dataSelectorParams==null || dataSelectParams.dataSelectorParams.length!=dataFilters.size()) {
			dataSelectParams.dataSelectorParams = new DataSelectParams[dataFilters.size()]; 
			for (int i=0; i<dataSelectParams.dataSelectorParams.length; i++) {
				dataSelectParams.dataSelectorParams[i] = dataFilters.get(i).getParams();
			}
		}
	}

	@Override
	public DataSelectParams getParams() {
		for (int i=0; i<dlDataSelectParams.dataSelectorParams.length; i++) {
			dlDataSelectParams.dataSelectorParams[i] = dataFilters.get(i).getParams();
		}
		return dlDataSelectParams;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		int score = dataFilters.get(dlDataSelectParams.dataSelectorIndex).scoreDLData(pamDataUnit);
		return score>=0 ? 1 : 0;
	}

}
