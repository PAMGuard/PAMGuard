package bearinglocaliser.toad.display;

import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamguardMVC.PamDataUnit;
import bearinglocaliser.display.BearingDataDisplay;

public interface TOADPlot extends BearingDataDisplay {

	void plotData(PamDataUnit pamDataUnit, BearingLocaliser bearingLocaliser, double[][] locBearings);

}
