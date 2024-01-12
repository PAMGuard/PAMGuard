package Localiser.algorithms.genericLocaliser.MCMC;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

public class MCMCParams2 implements Serializable, Cloneable, ManagedParameters  {
	
	public static final long serialVersionUID = 3L;
	
	/**
	 * Constructor for MCMC params. 
	 */
	public MCMCParams2(){
	
	}

	//chain
	/**
	 * The size of Gaussian distribution for the jump in each dimension; 
	 */
	public double[] jumpSize= new double[] {1,1,1}; //default - jump size for three dimensions
	
	/**
	 * The number of jumps in each Markov chain to attempt. 
	 * (not the total successful jumps that should be reached before the chain ends)
	 */
	public int numberOfJumps=250000; //int
	
	/**
	 * Where in paramter space the chains should start. Each value in the array is for one dimension. When
	 * chains start a random number between the two numbers is chosen as the start position in that dimension for the
	 * chain. 
	 */
	public double[][] chainStartDispersion = setChainDispersion(100,3) ;  //meters
	
	/**
	 * The number of seperate chains to use in the simulation
	 */
	public int numberOfChains=4; //int 
	
	/**
	 * Use cyclindrical jumps. 
	 */
	public boolean cylindricalCoOrdinates = false; 
	/**
	 * Set the chain jump size- sets the jump size of all dimensions to the input jumpsize
	 * @param jumpsize the jumpsize for all dimensions. 
	 * @param nDim the number of dimensions. 
	 */
	public void setJumpSize(double jumpsize, int nDim){
		jumpSize=new double[nDim];
		for (int i=0; i<nDim; i++){
			this.jumpSize[i]=jumpsize; 
		}
	}
	
	/**
	 * Set chain start dispersion to one value for all dimensions. The start dispersion is a random number centered
	 * on zero and from a distribution with a size defined by the dispersion input paramater. 
	 * @param dispersion the chain start dispersion for all dimensions. element 1 is th
	 */
	public double[][] setChainDispersion(double dispersion, int nDim){
		chainStartDispersion=new double[nDim][2];
		double[] dispersionVal=new double[2];
		dispersionVal[0]=-dispersion;
		dispersionVal[1]=dispersion; 
		for (int i=0; i<nDim; i++){
			this.chainStartDispersion[i]=dispersionVal; 
		}
		return chainStartDispersion; 
	}
	
	/**Parameters for analysis of chains**/
		
	//Type of chain analysis 
	public int chainAnalysis=IGNORE_PERCENTAGE;
	
	public static final int GELMAM_RUBIN_DIAGNOSIS=0;
	
	public static final int IGNORE_PERCENTAGE=1;
	
	public static final int MEDIAN=2;
	
	public double percentageToIgnore=0.7; //%

	
	//cluster analysis;
	public int clusterAnalysis=K_MEANS; 
	
	public static final int NONE=0;
			
	public static final int K_MEANS=1;
	
	public Integer nKMeans=2; //int
	
	public double maxClusterSize=5; //meters

	/**
	 * The number of times to perform a kmeans algorithm on results The algorithm starts clusters at random locations. 
	 */
	public int kmeanAttempts=10;

	/**
	 * The number of iterations for each kmeans attempt. Kmeans converges to a result but requires a certain number of iteration to 
	 * do so. 
	 */
	public int kmeansIterations=20;

	/**
	 * Timing error to add to time delays. This is here for legacy reasons.
	 */
	//public double timeError=1; 

	
	@Override
	public MCMCParams2 clone() {
		try {
			return (MCMCParams2) super.clone();
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
