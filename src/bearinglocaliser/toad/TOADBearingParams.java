package bearinglocaliser.toad;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import bearinglocaliser.algorithms.BearingAlgorithmParams;

public class TOADBearingParams extends BearingAlgorithmParams implements ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * A 3 element vector with the minimum azimuth angle (index 0), maximum azimuth angle (index 1), and step
	 * size (index 2) to use for the bearing sweep.  Initialises to 0deg to 180deg, with a 2 degree step.  Note
	 * that the term azimuth is used for familiarity, and is appropriate for horizontal linear arrays.  A more
	 * generic term would be main angle, in the direction of the primary array axis
	 */
	protected int[] bearingHeadings = new int[]{0, 180, 2};
	
	/**
	 * A 3 element vector with the minimum slant angle (index 0), maximum slant angle (index 1), and step
	 * size (index 2) to use for the bearing sweep.  For a horizontal linear array, 0 deg is horizontal and -90 deg
	 * is straight down.  Initialises to zero slant (0 deg x 0 deg x 1 deg step - step size cannot be 0 or else it will
	 * cause div-by-0 error later). 
	 * Note that the term slant is used for familiarity, and is appropriate for horizontal linear arrays.  A more
	 * generic term would be secondary angle, relative to the perpendicular to the array axis primary array axis.
	 */
	protected int[] bearingSlants = new int[]{0, 0, 1};
	
	/**
	 * Constructor
	 * @param groupNumber
	 * @param channelMap
	 */
	public TOADBearingParams(int groupNumber, int channelMap) {
		super(groupNumber, channelMap);
	}

	public int[] getBearingHeadings() {
		return bearingHeadings;
	}

	public void setBearingHeadings(int[] bearingHeadings) {
		this.bearingHeadings = bearingHeadings;
	}

	public int[] getBearingSlants() {
		return bearingSlants;
	}

	public void setBearingSlants(int[] bearingSlants) {
		this.bearingSlants = bearingSlants;
	}

	@Override
	protected TOADBearingParams clone() {
		try {
			return (TOADBearingParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
