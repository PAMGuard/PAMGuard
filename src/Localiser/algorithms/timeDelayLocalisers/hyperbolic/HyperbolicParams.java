package Localiser.algorithms.timeDelayLocalisers.hyperbolic;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;


/**
 * Holds settings for hyperbolic loclaiser. 
 * @author Jamie Macaulay
 *
 */
public class HyperbolicParams implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 3D localisation. 
	 */
	final public static int LOC_3D=0x1;
	/**
	 * 2D on the x,y plane
	 */
	final public static int LOC_2D_Z=0x2;
	
	/**
	 * 2D on the y,z plane
	 */
	final public static int LOC_2D_X=0X3;
	
	/**
	 * 2D on the x,z plane. 
	 */
	final public static int LOC_2D_Y=0X4;
	
	/**
	 * The current type of hyperbolic localiser to use. 
	 */
	public int currentType=LOC_3D;

	/**
	 * Calculates errors from a random distribution of time delay errors
	 */
	 public boolean calcErrors=false; 
	
	/**
	 * Number of iterations to calculate error. More=more computational time per localisation; 
	 */
	 public int bootStrapN=500;
	
	/**
	 * The cross correlation error in bins. 
	 */
	 public double crossCorrError=0; 
	 
	 @Override
	 public HyperbolicParams clone()  {
		 try {

			 return (HyperbolicParams) super.clone();
		 } catch (CloneNotSupportedException e) {
			 e.printStackTrace();
			 return null;
		 }
	 }
	
		@Override
		public PamParameterSet getParameterSet() {
			PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
			return ps;
		}

}
