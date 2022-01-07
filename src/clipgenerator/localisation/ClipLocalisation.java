package clipgenerator.localisation;

import whistlesAndMoans.WhistleBearingInfo;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamguardMVC.PamDataUnit;

/**
 * Use the same localisation information as for whistles. It's 
 * pretty generic. 
 * @author Doug Gillespie
 *
 */
public class ClipLocalisation extends WhistleBearingInfo {

	public ClipLocalisation(PamDataUnit pamDataUnit,
			BearingLocaliser bearingLocaliser, int hydrophones,
			double[][] anglesAndErrors) {
		super(pamDataUnit, bearingLocaliser, hydrophones, anglesAndErrors);
	}


}
