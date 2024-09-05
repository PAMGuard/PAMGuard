package cpod.dataSelector;

import PamController.PamControlledUnit;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetection;
import clickDetector.alarm.ClickAlarmParameters;
import cpod.CPODClick;
import cpod.CPODClickDataBlock;
import cpod.CPODClickTrainDataUnit;
import cpod.CPODControl;
import cpod.fx.CPODDataSelectorPane;
import cpod.fx.CPODDataSelectorPanel;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Data selector for CPOD data. Allows users to set the range of CPOD paramters e.g. peak frequency. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODDataSelector extends DataSelector {


	/**
	 * Get the CPOD data selector params. 
	 */
	private CPODDatSelectorParams dataSelectorParams; 

	/**
	 * Filters CPOD data. 
	 */
	private StandardCPODataFilter standardCPODataFilter; 

	/**
	 * The cpod data selector pane. 
	 */
	private CPODDataSelectorPane cPODDataSelectorPaneFX;

	private CPODDataSelectorPanel cPODDataSelectorPanel;

	public CPODDataSelector(PamControlledUnit cpodControl, CPODClickDataBlock cpodDataBlock, String selectorName,
			boolean allowScores) {
		super(cpodDataBlock, selectorName, allowScores);

		dataSelectorParams = new CPODDatSelectorParams(); 

		standardCPODataFilter = new StandardCPODataFilter(); 
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof CPODDatSelectorParams) {
			this.dataSelectorParams = (CPODDatSelectorParams) dataSelectParams; 
		}
	}

	@Override
	public CPODDatSelectorParams getParams() {
		return dataSelectorParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (cPODDataSelectorPanel==null) {
			cPODDataSelectorPanel = new CPODDataSelectorPanel(this);
		}
		return cPODDataSelectorPanel;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (cPODDataSelectorPaneFX==null) {
			cPODDataSelectorPaneFX = new CPODDataSelectorPane(this);
		}
		return cPODDataSelectorPaneFX;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {

		CPODClick cpodClick = ((CPODClick) pamDataUnit);

		for (int i=0; i<dataSelectorParams.cpodDataFilterParams.size(); i++) {
			if (standardCPODataFilter.scoreData(cpodClick, dataSelectorParams.cpodDataFilterParams.get(i))==0) {
				return 0;
			};
			//could add an if statement for CPOD parameters type here to use a more sophisticated filter 
			//e.g. the FPOD data has waveforms so when that is implemented may want extra bits and pieces. 
		}
		
		if (dataSelectorParams.selectClickTrain) {
			CPODClickTrainDataUnit cpodClickTrain = cpodClick.getCPODClickTrain();
			if (cpodClickTrain==null) return 0;
			
			if (dataSelectorParams.speciesID != null && !dataSelectorParams.speciesID.equals(cpodClickTrain.getSpecies())) {
				return 0;
			}
			
		}

		return 1;
	}


}
