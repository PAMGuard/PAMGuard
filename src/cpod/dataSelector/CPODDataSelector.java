package cpod.dataSelector;

import PamController.PamControlledUnit;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import clickDetector.alarm.ClickAlarmParameters;
import cpod.CPODClick;
import cpod.CPODClickDataBlock;
import cpod.CPODControl;
import cpod.fx.CPODDataSelectorPane;
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
	private CPODDataSelectorPane cPODDataSelectorPane;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (cPODDataSelectorPane==null) {
			cPODDataSelectorPane = new CPODDataSelectorPane(this);
		}
		return cPODDataSelectorPane;
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

		return 1;
	}


}
