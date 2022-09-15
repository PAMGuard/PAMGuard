package whistleClassifier.dataselect;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import whistleClassifier.WhistleClassificationDataUnit;
import whistleClassifier.WhistleClassifierControl;

/**
 * Species selector for whistle classifier. Currently only does yes / no, will 
 * maybe one day be extended to allow scores as well. 
 * @author dg50
 *
 */
public class WslClsDataSelector extends DataSelector {

	private WhistleClassifierControl wslClassifierControl;
	
	private WslClsSelectorParams wcsParams = new WslClsSelectorParams();

	public WslClsDataSelector(WhistleClassifierControl wslClassifierControl, PamDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.wslClassifierControl = wslClassifierControl;
	}
	

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof WslClsSelectorParams) {
			wcsParams = (WslClsSelectorParams) dataSelectParams;
		}
	}

	@Override
	public WslClsSelectorParams getParams() {
		return wcsParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new WslClsDialogPanel(wslClassifierControl, this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		WhistleClassificationDataUnit wcdu = (WhistleClassificationDataUnit) pamDataUnit;
		String species = wcdu.getSpecies();
//		score = wcdu.get
		SppClsSelectParams sppParams = wcsParams.getSppParams(species);
//		if ()
//		if (sppParams.selected == false) {
//			return 0;
//		}
//		if (isAllowScores()) {
//			return sppP
//		}
//		wslClassifierControl.getWhistleClassificationParameters().
		return sppParams.selected ? 1 : 0;
	}

}
