package Localiser.algorithms.genericLocaliser.simplex.old;

import java.util.ArrayList;
import java.util.Arrays;

import javax.vecmath.Point3f;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import Localiser.algorithms.genericLocaliser.MCMC.MCMC;
import Localiser.algorithms.genericLocaliser.MCMC.old.MCMCLocaliser;
import Localiser.algorithms.timeDelayLocalisers.TimeDelayLocaliserModel;

/**
 * Time delay based simplex method for localisation. Note that a lot of the code here has been referenced directly from static MCMC functions. This is because both MCMC and Simplex are based on the forward time delay problem
 * hence are mathematically very similar. Simplex is much faster than MCMC but does not provide a full 3D probability distribution. 
 * @author Jamie Macaulay
 *
 */
public class Simplex implements TimeDelayLocaliserModel {
	
	//input
	private ArrayList<ArrayList<Double>> timeDelaysObs;
	
	private ArrayList< ArrayList<Double>> timeDelayErrors;
	
	private ArrayList<ArrayList<Point3f>> hydrophoneArray;
	
	private float sampleRate;
	
	private double speedOfSound;

	//results
	private double[] result;
	
	private double[] resultErrors;
	
	private double finalChi2;

	private long algorithmTime;
	
	public Chi2 chi2;
	
	private double crossCorrError=1;
	
	private boolean cylindrical=false; 
	

	public Simplex(ArrayList<ArrayList<Point3f>> hydrophoneArray ,ArrayList<ArrayList<Double>> timeDelays, ArrayList< ArrayList<Double>> timeDelayErrors, float sampleRate, float speedOfSound){
		
		this.sampleRate=sampleRate;
		this.speedOfSound=speedOfSound;
	
		this.timeDelaysObs=timeDelays;
		this.hydrophoneArray=hydrophoneArray;
		
		//add cross correlation error to time delay errors		
		this.timeDelayErrors=MCMCLocaliser.addCrossCorError(timeDelayErrors, crossCorrError, sampleRate);
		
		this.chi2=new Chi2();
		
	}

	public Simplex(){
		this.chi2=new Chi2();
	}

	/**
	 * Calculate chi2 for different jump locations. 
	 * @author Jamie Macaulay
	 *
	 */
	public class Chi2 implements MultivariateRealFunction {
		
		Point3f chainPos;
		ArrayList<ArrayList<Double>> timeDelays;
		double chi2;

		public double logLikelihood(double[] location) throws FunctionEvaluationException, 
		IllegalArgumentException {
			return -value(location) / 2;
		}
		
		@Override
		public double value(double[] location) throws FunctionEvaluationException{
			float x;
			float y;
			float z;
			if (cylindrical){
				 x=(float) (location[0]*Math.cos(Math.toRadians(location[1])));
				 z=(float) (location[2]);
				 y=(float) (location[0]*Math.sin(Math.toRadians(location[1])));
			}
			else{
				 x=(float) (location[0]);
				 y=(float) (location[1]);
				 z=(float) (location[2]);
						 
			}
			
			chainPos=new Point3f(x,y,z);
			timeDelays=MCMCLocaliser.getTimeDelays(chainPos, hydrophoneArray, speedOfSound);
			chi2=MCMCLocaliser.chiSquared(timeDelaysObs,  timeDelays, timeDelayErrors);
			 
			return chi2;
		}

	}

	@Override
	public void runAlgorithm() {
		
		this.result =new double[3];
		this.resultErrors=new double[3];
		this.finalChi2=Double.NaN;
		
		long time1=System.currentTimeMillis();
		
		NelderMead optimiser = new NelderMead();
		//			optimiser.setConvergenceChecker(new StopEarly(1));
		RealPointValuePair result;
		try {
			
			double[] start = {0, 0, -100};
			double[] firstStep = {10, 100, 10};
			optimiser.setStartConfiguration(firstStep);
			result = optimiser.optimize(chi2, GoalType.MINIMIZE, start);
			double[] point = result.getPoint();
			//localisation result
			this.result=point;

		//calc chi squared.
//			ChiSquaredDistributionImpl chi2Dist = new ChiSquaredDistributionImpl();
//			double p = chi2Dist.cumulativeProbability(result.getValue());
			this.finalChi2=chi2.value(point);

		/*
		 * Now we must calculate errors. Need to look at the log likilihood surface;
		 */
			double errorX=getLLCurvature( chi2,  point, 0);
			double errorY=getLLCurvature( chi2,  point, 1);
			double errorZ=getLLCurvature( chi2,  point, 2);
			double[] resultErrors={errorX,errorY,errorZ};
			this.resultErrors=resultErrors;
			
	
		} catch (OptimizationException e) {
			System.out.println("Simplex 3D optimisation Exception: " + e.getMessage());
		} catch (FunctionEvaluationException e) {
			System.out.println("Simplex 3D Function Evaluation Exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println("Simplex 3D Illegal Argument Exception: " + e.getMessage());
		} catch (MathException e) {
			System.out.println("Simplex 3D Math Exception: " + e.getMessage());
		}
		
		long time2=System.currentTimeMillis();
		this.algorithmTime=time2-time1;
	}
	
	/**
	 * Get the curvature of a log likelihood surface along any dimension.
	 * @param chi2 Chi2 surface
	 * @param point starting point
	 * @param dim dimension to move in
	 * @return curvature, expressed as 1 SD error
	 */
	private double getLLCurvature(Chi2 chi2, double[] point, int dim) {

		double dis = 10;
		double err = 0;
		int nE = 0;
		try {
			while (true) {
				double ll1, ll2, ll3;
				double[] pos2 = Arrays.copyOf(point, point.length);
				ll2 = chi2.logLikelihood(pos2);
				pos2[dim] -= dis;
				ll1 = chi2.logLikelihood(pos2);
				pos2 = Arrays.copyOf(point, point.length);
				pos2[dim] += dis;
				ll3 = chi2.logLikelihood(pos2);
				double q = (ll1 + ll3 - 2*ll2) / (dis*dis);
				err = 1./Math.sqrt(-q);
				double change = dis/err;
				if (++nE > 4 || (change > 0.95 && change < 1.05)) {
					break;
				}
				dis = err;
			}
		} catch (FunctionEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return err;
	}
		
	

	@Override
	public Boolean changeSettings() {
		return null;
	}
	
	
	public void setTimeDelays(ArrayList<ArrayList<Double>> timeDelays){
		this.timeDelaysObs=timeDelays;
	}
	
	public void setTimeDelaysErrors(ArrayList<ArrayList<Double>> timeDelayErrors) {
		if (sampleRate<=0){
			return;
		}
		this.timeDelayErrors=MCMCLocaliser.addCrossCorError(timeDelayErrors, crossCorrError, sampleRate);
	}
	
	public void setHydrophonePos(ArrayList<ArrayList<Point3f>> hydrophonePos){
		this.hydrophoneArray=hydrophonePos;
	}
	
	public void setSampleRate(float sampleRate) {
		this.sampleRate=sampleRate;
	}

	public void setSoundSpeed(double speedOfSound) {
		this.speedOfSound=speedOfSound;
	}

	public double[] getLocation() {
		return result;
	}
	
	public double[] getLocationErrors() {
		return resultErrors;
	}
	
	public long getRunTime() {
		return algorithmTime;
	}
	
	public double getChi2() {
		return finalChi2;
	}
	
	public Chi2 getChi2Function() {
		return chi2;
	}

	@Override
	public void stop() {
		//too fast to need a stop command. 
	}

	

}
