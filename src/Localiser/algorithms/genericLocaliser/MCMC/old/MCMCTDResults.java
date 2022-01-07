package Localiser.algorithms.genericLocaliser.MCMC.old;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import Localiser.algorithms.locErrors.ErrorEllipse;
import Localiser.algorithms.locErrors.LocaliserError;

public class MCMCTDResults {
	
	public Point3d location;
	
	public Point3d errors; 
	
	public ArrayList<ArrayList<Point3f>> chainJumps; 
	
	/**
	 * The range
	 */
	public double range;
	
	/**
	 * The range error
	 */
	public double rangeError;
	
	/**
	 * The mean chi^2 value
	 */
	public double meanChi; 
	
	
	public MCMCTDResults(){
		
	}
	
	public double getChi() {
		return meanChi;
	}


	public void setMeanChi(double meanChi) {
		this.meanChi = meanChi;
	}
	


	public Point3d getLocation() {
		return location;
	}


	public Point3d getErrors() {
		return errors;
	}


	public ArrayList<ArrayList<Point3f>> getChainJumps() {
		return chainJumps;
	}


	public double getRange() {
		return range;
	}


	public double getRangeError() {
		return rangeError;
	}


	public void setLocation(Point3d location) {
		this.location = location;
	}


	public void setErrors(Point3d errors) {
		this.errors = errors;
	}


	public void setChainJumps(ArrayList<ArrayList<Point3f>> chainJumps) {
		this.chainJumps = chainJumps;
	}


	public void setRange(double range) {
		this.range = range;
	}


	public void setRangeError(double rangeError) {
		this.rangeError = rangeError;
	}
	
	

}
