package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Options for the MHT algorithm 
 * @author Jamie Macualay 
 *
 */
public class MHTKernelParams implements Cloneable,  Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * The maximum number of unique branches past the pruneBack point to keep
	 */
	public int nHold = 20;
	
	/**
	 * The branches are pruned to nHold number of branches by going back 
	 * nPruneBack values. 
	 */
	public int nPruneback = 4;
	
	/**
	 * The minimum number of added detections to the probability matrix
	 *  allowed before a prune back of possibilities takes place. 
	 */
	public int nPruneBackStart = 5; 
	
	/**
	 * The maximum number of coasts i.e. missing clicks before a click train is 
	 * saved.  
	 */
	public int maxCoast = 3; //should be 5 
	
	@Override
	protected MHTKernelParams clone() {
		try {
			return (MHTKernelParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Print out settings. 
	 */
	public void printSettings() {
		System.out.println("N Prune Back: " + nPruneback);
		System.out.println("N Prune Back Start: " + nPruneBackStart);
		System.out.println("Max Coasts: " + maxCoast);
		System.out.println("N Hold: " + nHold);
		System.out.println("/****************************/");
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
