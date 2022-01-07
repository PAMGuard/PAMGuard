package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import PamUtils.PamUtils;
import pamMaths.PamVector;

/**
 * Converts old angle pairs, which used system a below to new angle pairs which 
 * used angle system b. this is needed to convert old binary data from clicks, 
 * whistles and clip generator to new format when binary files are read. <p>
 * The change will affect very little data - only that collected using a volumetric array
 * and only when the animal is at significant depth when compared to it's distance from 
 * the array. 
 * @author Doug Gillespie
 *
 */
public class OldAngleConverter {

	public static double[] convertOldAnglePair(double[] oldAngles) {
		if (oldAngles == null || oldAngles.length < 2) {
			return oldAngles;
		}
		PamVector bearingVector = new PamVector();
		bearingVector.setElement(0, Math.cos(oldAngles[0]));
		bearingVector.setElement(1, Math.sin(oldAngles[0])*Math.cos(oldAngles[1]));
		bearingVector.setElement(2, Math.sin(oldAngles[0])*Math.sin(oldAngles[1]));
		
		double[] newAngles = bearingVector.toHeadAndSlantR();
		// need to convert back to angle and re-constrain. 
		newAngles[0] = PamUtils.constrainedAngleR(Math.PI/2.-newAngles[0], Math.PI);
		
		return newAngles;
	}
}
