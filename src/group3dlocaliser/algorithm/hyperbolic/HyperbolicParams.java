package group3dlocaliser.algorithm.hyperbolic;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

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
	 * Calculates errors from a random distribution of time delay errors
	 */
	 public boolean calcErrors=false; 
	
	/**
	 * Number of iterations to calculate error. More=more computational time per localisation; 
	 */
	 public int bootStrapN=100;

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
			PamParameterSet ps = PamParameterSet.autoGenerate(this);
			return ps;
		}

}
