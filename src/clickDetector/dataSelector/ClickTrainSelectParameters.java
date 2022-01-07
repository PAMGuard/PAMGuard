package clickDetector.dataSelector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamguardMVC.dataSelector.DataSelectParams;
import clickDetector.ClickParameters;

public class ClickTrainSelectParameters extends DataSelectParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public boolean showManualTrains = true;
	public boolean showAutoTrains = true;

	public int showShortTrains = ClickParameters.LINES_SHOW_SOME;
	public double minTimeSeparation = 60;
	public double minBearingSeparation = 5;
	public double defaultRange = 5000;
	public boolean plotIndividualBearings = false;

	@Override
	public ClickTrainSelectParameters clone() {
		try {
			return (ClickTrainSelectParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
