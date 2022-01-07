package group3dlocaliser.dataselector;

import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import group3dlocaliser.Group3DDataBlock;
import group3dlocaliser.Group3DDataUnit;
import group3dlocaliser.Group3DLocaliserControl;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class Group3DDataSelector extends DataSelector {

	private Group3DDataSelectParams params = new Group3DDataSelectParams();
	
	private Group3DDataSelPanel group3dDataSelPanel;

	private Group3DLocaliserControl groupLocControl;

	public Group3DDataSelector(Group3DLocaliserControl groupLocControl, Group3DDataBlock group3dDataBlock, String selectorName, boolean allowScores) {
		super(group3dDataBlock, selectorName, allowScores);
		this.groupLocControl = groupLocControl;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		try {
			this.params = (Group3DDataSelectParams) dataSelectParams;
		}
		catch (ClassCastException e) {
			if (this.params == null) {
				this.params = new Group3DDataSelectParams();
			}
			e.printStackTrace();
		}
	}

	@Override
	public DataSelectParams getParams() {
		return params;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (group3dDataSelPanel == null) {
			group3dDataSelPanel = new Group3DDataSelPanel(groupLocControl, this);
		}
		return group3dDataSelPanel;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
			return 1;
		}
		Group3DDataUnit g3dDataUnit = (Group3DDataUnit) pamDataUnit;
		AbstractLocalisation loc = g3dDataUnit.getLocalisation();
		if (loc instanceof GroupLocalisation == false) {
			return 0;
		}
		int nChan = PamUtils.getNumChannels(g3dDataUnit.getChannelBitmap());
		int nDF = (nChan*(nChan-1))/2-3;
		GroupLocalisation gLoc = (GroupLocalisation) loc;
		GroupLocResult res = gLoc.getGroupLocaResult(0);
		if (res == null) return 0;
		Double chi2 = res.getChi2();

		if  (chi2/nDF > params.maxChi2) {
			return 0;
		}
		LocaliserError error = res.getLocError();
		if (error != null && error.getErrorMagnitude() > params.maxError) {
			return 0;
		}
		if (params.minDF > 0) { 
			Integer ndf = res.getnDegreesFreedom();
			if (ndf == null) {
				return 0;
			}
			if (ndf < params.minDF) {
				return 0;
			}
		}
		// otherwise it all seems OK.
		return 1.;
	}


}
