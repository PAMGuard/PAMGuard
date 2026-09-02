package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Subclass of parameters for bearing. An extra bearing jump factor has been added. The reason for this 
 * is that if there are click trains
 * 
 * @author Jamie Macaulay 
 *
 */
public class BearingChi2VarParams  extends SimpleChi2VarParams implements ManagedParameters {
	
	/**
	 * Simple Enum indicating the direction the jump can be. 
	 * @author Jamie Macaulay 
	 *
	 */
	public enum BearingJumpDrctn {BOTH, POSITIVE, NEGATIVE};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Whether the bearing jump is used. 
	 */
	public boolean bearingJumpEnable = false; 
	
	/**
	 * The maximum allowed bearing bearing jump in a click train in RADIANS
	 */
	public double maxBearingJump = Math.toRadians(20);

	/**
	 * Whether the bearing jump can be in both directions, positive or negative.
	 * Note that this is useful for towed hydrophone arrays were we would almost
	 * always expect bearing changes to move from positive to negative. If a jump
	 * is for example, positive, then only large positive jumps will incure a penalty. 
	 * Large negative jumps will be ignored (however will still create higher chi^2 
	 * values making them less diserable for the click train detector
	 * )
	 */
	public BearingJumpDrctn bearingJumpDrctn = BearingJumpDrctn.POSITIVE;
	

	public BearingChi2VarParams(String name, String unitString, double error, double minError, double errorScaleValue) {
		super(name, unitString, error, minError, errorScaleValue);
	}

	public BearingChi2VarParams(String name, String unitString, double error, double minError) {
		super(name, unitString, error, minError);
	}

	public BearingChi2VarParams(String name) {
		super(name);
	}

	public BearingChi2VarParams(String name, String units) {
		super(name, units);
	}
	

	public BearingChi2VarParams(SimpleChi2VarParams params) {
		this(params.name, params.getUnits(), params.error, params.minError, params.errorScaleValue);
	}

	
	@Override
	public BearingChi2VarParams clone() {
		return (BearingChi2VarParams) super.clone();
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

	/**
	 * The signed, wrapped difference between two bearings. Unlike a plain
	 * subtraction this handles the 0/2&pi; wrap-around, returning a value in
	 * (-&pi;, &pi;] which preserves the <i>direction</i> of the bearing change
	 * (positive or negative) - which is what makes {@link BearingJumpDrctn} work.
	 *
	 * @param a1 - the first (e.g. newer) bearing in RADIANS.
	 * @param a2 - the second (e.g. older) bearing in RADIANS.
	 * @return the signed difference {@code a1 - a2} wrapped to (-&pi;, &pi;].
	 */
	public static double signedBearingDiff(double a1, double a2) {
		return Math.atan2(Math.sin(a1 - a2), Math.cos(a1 - a2));
	}

	/**
	 * Test whether a bearing jump exceeds the allowed maximum in the given
	 * direction. This is the single definition of the max-bearing-jump rule shared
	 * by every click train algorithm (MHT, adaptive and UKF) so the meaning of
	 * {@link BearingJumpDrctn} is identical everywhere.
	 * <ul>
	 * <li>{@code BOTH} - a jump in either direction (its magnitude) is tested.</li>
	 * <li>{@code POSITIVE} - only positive jumps are tested (a large negative jump
	 * is ignored).</li>
	 * <li>{@code NEGATIVE} - only negative jumps are tested.</li>
	 * </ul>
	 *
	 * @param signedDeltaRad - the signed bearing change in RADIANS (see
	 *                       {@link #signedBearingDiff(double, double)}).
	 * @param maxJumpRad     - the maximum allowed jump in RADIANS.
	 * @param drctn          - the jump direction to police; a {@code null} value is
	 *                       treated as {@link BearingJumpDrctn#POSITIVE}.
	 * @return true if the jump should be penalised.
	 */
	public static boolean exceedsMaxJump(double signedDeltaRad, double maxJumpRad, BearingJumpDrctn drctn) {
		double delta;
		if (drctn == null) {
			drctn = BearingJumpDrctn.POSITIVE;
		}
		switch (drctn) {
		case BOTH:
			delta = Math.abs(signedDeltaRad);
			break;
		case NEGATIVE:
			delta = -signedDeltaRad;
			break;
		case POSITIVE:
		default:
			delta = signedDeltaRad;
			break;
		}
		return delta > maxJumpRad;
	}

}
