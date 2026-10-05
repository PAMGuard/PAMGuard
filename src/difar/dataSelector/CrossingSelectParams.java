package difar.dataSelector;

import java.io.Serializable;

import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.dataSelector.DataSelectParams;

/**
 * Settings for the crossing data selector: which crossings to show, by the
 * quality of their geometry. On by default, so poor crossings are hidden
 * unless a user chooses to see them.
 */
public class CrossingSelectParams extends DataSelectParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;

	/** Smallest crossing angle shown, degrees. */
	public double minAngle = 5.;

	/** Whether to hide crossings with a large location error. */
	public boolean useMaxError = false;

	/** Largest location error shown, the larger of x and y, kilometres. */
	public double maxErrorKm = 10.;

	/** Whether to hide crossings that lie on one of their own buoys. */
	public boolean hideOnBuoy = true;

	public CrossingSelectParams() {
		setCombinationFlag(DATA_SELECT_AND);
	}

	@Override
	public CrossingSelectParams clone() {
		try {
			return (CrossingSelectParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		return PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
	}
}
