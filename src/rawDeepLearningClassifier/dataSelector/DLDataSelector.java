package rawDeepLearningClassifier.dataSelector;

import java.util.ArrayList;
import java.util.List;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;
import annotation.dataselect.AnnotationDataSelector;
import javafx.scene.Node;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.logging.DLAnnotation;

/**
 * Data selector of DL data units. Note that data selectors are for deep leanring annotations
 * rather than deep learning data units. 
 * <p>
 * The data selector can have different types of data selectors which can 
 * depend on the classifier used and user choice. 
 * <p>
 * Note that this is slightly different from DLPredicitoDecision
 * as it deals with data units that may have a more than one prediction. 
 * i.e. 
 */
public class DLDataSelector extends AnnotationDataSelector<DLAnnotation> {


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
	public DLDataSelector(DLControl dlcontrol, DataAnnotationType<DLAnnotation> annotationType, PamDataBlock pamDataBlock,
			String selectorName, boolean allowScores) {
		super(annotationType, pamDataBlock, selectorName, allowScores);
		/****New data filters go here****/
		dataFilters.add(new DLPredictionFilter(dlcontrol)); 
		
		//create default params
		dlDataSelectParams = new DLDataSelectorParams(); 
		dlDataSelectParams.dataSelectorParams = new DataSelectParams[dataFilters.size()];
		for (int i=0; i<dataFilters.size() ; i++) {
			dlDataSelectParams.dataSelectorParams[i] = dataFilters.get(i).getParams();
		}

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
				dataFilters.get(i).setParams((DataSelectParams) dlDataSelectParams.dataSelectorParams[i]); 
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
	public DLDataSelectorParams getParams() {
		//get the paramters from the pane. 
		
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

	@Override
	protected double scoreData(PamDataUnit pamDataUnit, DLAnnotation annotation) {
		int score = dataFilters.get(dlDataSelectParams.dataSelectorIndex).scoreDLData(pamDataUnit);
		
		//the score is the index of the class that scores highest or -1 if it does not pass threshold prediciton.
		//Need to make more simple here as scores in PG are 0 for not passed rather than negative. 
		
		return score>=0 ? 1 : 0;
	}

	public DLDataFilter getCurrentDataSelector() {
		return dataFilters.get(dlDataSelectParams.dataSelectorIndex);
	}
	
	public  List<DLDataFilter> getDataSelectors() {
		return dataFilters;
	}

}
