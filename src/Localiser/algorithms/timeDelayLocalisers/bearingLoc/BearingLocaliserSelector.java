package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import Array.ArrayManager;
import Array.PamArray;
import PamModel.SMRUEnable;
import PamUtils.PamUtils;

/**
 * Class to automatically select  / create the most appropriate type of 
 * bearing localiser. 
 * @author Doug Gillespie
 *
 */
public class BearingLocaliserSelector {

	public static BearingLocaliser createBearingLocaliser(int hydrophoneMap, double timingError) {
		ArrayManager arrayManager = ArrayManager.getArrayManager();
		PamArray currentArray = arrayManager.getCurrentArray();
		int arrayType = arrayManager.getArrayType(hydrophoneMap);
		switch(arrayType) {
		case ArrayManager.ARRAY_TYPE_NONE:
		case ArrayManager.ARRAY_TYPE_POINT:
			return null;
		case ArrayManager.ARRAY_TYPE_LINE:
			int nPhones = PamUtils.getNumChannels(hydrophoneMap);
			if (nPhones > 2 && SMRUEnable.isEnable()) {
				return new MLLineBearingLocaliser2(hydrophoneMap, -1, timingError);
			}
			else {
				return new PairBearingLocaliser(hydrophoneMap, -1, timingError);
			}
		case ArrayManager.ARRAY_TYPE_PLANE:
			return new MLGridBearingLocaliser2(hydrophoneMap, -1, timingError);
//			return new CombinedBearingLocaliser(new MLGridBearingLocaliser(hydrophoneMap, -1, timingError), 
//					hydrophoneMap, -1, timingError);
//			return new LSQBearingLocaliser(hydrophoneMap, -1, timingError);
//			return new SimplexBearingLocaliser(hydrophoneMap, -1, timingError);
		case ArrayManager.ARRAY_TYPE_VOLUME:
			return new MLGridBearingLocaliser2(hydrophoneMap, -1, timingError);
//			return new LSQBearingLocaliser(hydrophoneMap, -1, timingError);
//			return new CombinedBearingLocaliser(hydrophoneMap, -1, timingError);
		}
		return null;
	}
	
	
}
