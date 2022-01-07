package gpl.contour;

import PamModel.parametermanager.EnumToolTip;

/**
 * What to do when multiple contour patches are found in a single detection. 
 * options are to keep multiple patches in the same detection, keep only the
 * biggest one (based on area) or to separate them into separate detections.  
 * @author dg50
 *
 */
public enum ContourMerge implements EnumToolTip {

	BIGGEST, SEPARATE, MERGE;

	@Override
	public String toString() {
		switch (this) {
		case BIGGEST:
			return "Biggest contour only";
		case MERGE:
			return "Merge all contours";
		case SEPARATE:
			return "Separate all contours";
		default:
			return null;		
		}
	}

	@Override
	public String getToolTip() {
		switch (this) {
		case BIGGEST:
			return "Keep the biggest contour patch only";
		case MERGE:
			return "Merge all contour patches into one big countour with several patches";
		case SEPARATE:
			return "Separate all contour patches into separate PAMGuard data units";
		default:
			return null;		
		}
	}
	
	
}
