package Localiser.algorithms.genericLocaliser.MCMC;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import Localiser.algorithms.genericLocaliser.MCMC.MCMC.ChainResult;
import Localiser.algorithms.locErrors.EllipticalError;
import PamUtils.PamArrayUtils;

public class MCMCResult {

	/**
	 * The mean location
	 */
	private double[] meanLoc;
	
	/**
	 * Mean standard deviation in the chains
	 */
	private double[] meanstd; 
	
	/**
	 * The results of each MCMC chanin
	 */
	private ArrayList<ChainResult> data;
	
	/**
	 * Error in elliptical format. 
	 */
	private EllipticalError locError;
	
	/**
	 * The chi2 value 
	 */
	private double chi2;

	/**
	 * Set the location of the chains
	 * @param meanloc - mean location of the chains
	 */
	public void setLocation(double[] meanloc) {
		this.meanLoc=meanloc; 
	}

	/**
	 * Set the standard deviation of the chains
	 * @param meanstd - the standard deviation of chains in x, y and z. 
	 */
	public void setError(double[] meanstd) {
		this.meanstd=meanstd; 
		
	}

	/**
	 * Set the chains 
	 * @param data - chain data for all MCMC chains
	 */
	public void setChains(ArrayList<ChainResult> data) {
		this.data=data;
	}

	/**
	 * Set the localisation error. 
	 * @param localiserError - the localisation error
	 */
	public void setLocError(EllipticalError localiserError) {
		this.locError=localiserError;
	}
	
	/**
	 * Get the mean location
	 * @return the mean location
	 */
	public double[] getMeanLoc() {
		return meanLoc;
	}

	/**
	 * Get the mean standard deviation
	 * @return - the mean std. 
	 */
	public double[] getMeanstd() {
		return meanstd;
	}

	/**
	 * Get the chain results. 
	 * @return - the chain data. 
	 */
	public ArrayList<ChainResult> getData() {
		return data;
	}

	/**
	 * Get the localisation error. 
	 * @return the localisation error. 
	 */
	public EllipticalError getLocError() {
		return locError;
	}
	
	/**
	 * Get the chi^2 value. 
	 * @return the chi^2 value
	 */
	public double getChi2() {
		return chi2;
	}

	/**
	 * Set the chi^2 value. 
	 * @param chi2 - the chi^2 value to set.
	 */
	public void setChi2(double chi2) {
		this.chi2 = chi2;
	}

	/**
	 * Get the jumps for the MCMC algorithm in Point3f format. This is for legacy code. 
	 * @return the MCMC jumps in Point3f format. 
	 */
	public ArrayList<ArrayList<Point3f>> getJumps() {
		ArrayList<ArrayList<Point3f>>  jumps=new ArrayList<ArrayList<Point3f>>(); 
		ArrayList<Point3f> chainJumps; 
		float[] ajump; 
		for (int i=0; i<this.data.size(); i++) {
			chainJumps= new ArrayList<Point3f>(); 
			for (int j=0; j<this.data.get(i).successJump.size(); j++) {
				ajump= this.data.get(i).successJump.get(j); 
				chainJumps.add(new Point3f(ajump[0],  ajump[1],  ajump[2])); 
			}
			jumps.add(chainJumps); 
		}
		return jumps;
	}
	
	
	public double[][] getJumpsd() {
		return  getJumpsd(1); 
	}
	
	public double[][] getJumpsf() {
		return  getJumpsd(1); 
	}

	
	/**
	 * Get the jumps for the MCMC algorithm in double[][] format. This is for legacy code. 
	 * @param div - reduce the data by div times (e.g. for plotting).
	 * @return the MCMC jumps from all chains. 
	 */
	public double[][] getJumpsd(int div) {
		
		if (div<1) div = 1; 
		
//		int nJumps = 0; 
//		for (int i=0; i<this.data.size(); i++) {
//			nJumps = (int) (nJumps + Math.floor(this.data.get(i).successJump.size()/div)+1); 
//		}
//		
		int nJumps = 0; 
		for (int i=0; i<this.data.size(); i++) {
			for (int j=0; j<this.data.get(i).successJump.size(); j=j+div) {
				nJumps++; 
			}
		}
		double[][] jumps = new double[nJumps][]; 
		
		double[] ajump; 
		int n=0; 
		for (int i=0; i<this.data.size(); i++) {
			for (int j=0; j<this.data.get(i).successJump.size(); j=j+div) {
				ajump= PamArrayUtils.float2Double(this.data.get(i).successJump.get(j)); 
				jumps[n] = ajump; 
				n++; 
			}
		}
		return jumps;
	}
	
	/**
	 * Get the jumps for the MCMC algorithm in double[][] format. This is for legacy code. 
	 * @param div - reduce the data by div times (e.g. for plotting).
	 * @return the MCMC jumps from all chains. 
	 */
	public float[][] getJumpsf(int div) {
		
		if (div<1) div =1; 
		
		int nJumps = 0; 
		for (int i=0; i<this.data.size(); i++) {
			nJumps = nJumps + this.data.get(i).successJump.size(); 
		}
		float[][] jumps = new float[(int) Math.floor(nJumps/div)][]; 

		float[] ajump; 
		int n=0; 
		for (int i=0; i<this.data.size(); i=i++) {
			for (int j=0; j<this.data.get(i).successJump.size(); j=j+div) {
				ajump= this.data.get(i).successJump.get(j); 
				jumps[n] = ajump; 
				n++; 
			}
		}
		return jumps;
	}


}


