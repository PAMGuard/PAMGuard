package difar.dataSelector;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import difar.crossings.DifarCrossing;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Hides crossings whose geometry makes their location untrustworthy: a small
 * crossing angle, a large location error, or a location on one of their own
 * buoys. A crossing whose angle is not known, from a row written before the
 * angle was kept, is shown, since nothing is known against it.
 */
public class CrossingDataSelector extends DataSelector {

	private CrossingSelectParams params = new CrossingSelectParams();

	private CrossingSelectPanel panel;

	public CrossingDataSelector(PamDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (!(pamDataUnit instanceof DifarCrossing)) {
			return 1;
		}
		return passes((DifarCrossing) pamDataUnit, params) ? 1 : 0;
	}

	/**
	 * Whether a crossing passes the settings. Kept apart from the selector so
	 * the rule can be read and checked on its own.
	 */
	public static boolean passes(DifarCrossing crossing, CrossingSelectParams params) {
		if (params.hideOnBuoy && crossing.isOnBuoy()) {
			return false;
		}
		double angle = crossing.getCrossingAngle();
		if (!Double.isNaN(angle) && angle < params.minAngle) {
			return false;
		}
		if (params.useMaxError) {
			double error = Math.max(crossing.getXError(), crossing.getYError());
			if (!Double.isNaN(error) && error > params.maxErrorKm * 1000.) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof CrossingSelectParams) {
			params = (CrossingSelectParams) dataSelectParams;
		}
	}

	@Override
	public CrossingSelectParams getParams() {
		return params;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (panel == null) {
			panel = new CrossingSelectPanel(this);
		}
		return panel;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return null;
	}
}
