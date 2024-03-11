package PamController.soundMedium;

import java.io.Serializable;

import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Stores parameters for the current medium. 
 * @author Jamie Macaulay
 *
 */
public class GlobalMediumParams implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	static final long serialVersionUID = 1L;
	
	/**
	 * The current medium. 
	 */
	public SoundMedium currentMedium = SoundMedium.Water;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GlobalMediumParams clone() {
//		try {
//			return (GlobalTimeParameters) super.clone();
		try {
			GlobalMediumParams clonedParams = (GlobalMediumParams) super.clone();
			return clonedParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}