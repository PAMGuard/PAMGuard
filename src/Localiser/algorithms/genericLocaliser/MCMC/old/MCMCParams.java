package Localiser.algorithms.genericLocaliser.MCMC.old;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;


public class MCMCParams implements Serializable, Cloneable, ManagedParameters  {
	
	public static final long serialVersionUID = 1L;
	
//	these are default settings for a porpoise;
	//Errors
	public double timeError=1.0;
	
	//chain
	public double jumpSize=1;
	
	public boolean cylindricalCoOrdinates=false;
	
	public Integer numberOfJumps=250000; //int
	
	public double chainStartDispersion=100; //meters
	
	public Integer numberOfChains=4; //int 
	
	//result analysis
		
	//Type of chain analysis 
	public int chainAnalysis=IGNORE_PERCENTAGE;
	
	public static final int GELMAM_RUBIN_DIAGNOSIS=0;
	
	public static final int IGNORE_PERCENTAGE=1;
	
	public static final int MEDIAN=2;
	
	public double percentageToIgnore=50; //%

	
	//cluster analysis;
	public int clusterAnalysis=K_MEANS; 
	
	public static final int NONE=0;
			
	public static final int K_MEANS=1;
	
	public Integer nKMeans=2; //int
	
	public double maxClusterSize=5; //meters

	
	
	@Override
	public MCMCParams clone() {
		try {
			return (MCMCParams) super.clone();
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