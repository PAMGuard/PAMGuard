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

}
