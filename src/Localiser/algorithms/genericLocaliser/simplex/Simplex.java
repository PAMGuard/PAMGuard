package Localiser.algorithms.genericLocaliser.simplex;

import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.direct.NelderMead;

import pamMaths.PamVector;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MinimisationAlgorithm;
import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.locErrors.LikilihoodError;
import Localiser.algorithms.locErrors.LocaliserError;

/**
 * Time delay based simplex method for localisation. Note that a lot of the code here has been referenced directly from static MCMC functions. This is because both MCMC and Simplex are based on the forward time delay problem
 * hence are mathematically very similar. Simplex is much faster than MCMC but does not provide a full 3D probability distribution. 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("deprecation")
public class Simplex implements MinimisationAlgorithm {

	private double[][] result;

	private LocaliserError[] resultErrors;

	private double[] finalChi2;

	/**
	 * The algorithm time 
	 */
	private long algorithmTime;

	/**
	 * The minimisation function. 
	 */
	public MinimisationFunction chi2Minimisation;

	/**
	 * The chi2 function needed by the  apache.commons library. 
	 */
	private Chi2 chi2; 

	
	public Simplex(){
		//must set minimisation function. 
	}


	/**
	 * Constructor for the Simplex 
	 * @param chi2
	 */
	public Simplex(MinimisationFunction chi2){
		this.chi2Minimisation=chi2; 
		this.chi2=new Chi2(); 
	}
	

	@Override
	public void setMinimisationFunction(MinimisationFunction chi2) {
		this.chi2Minimisation=chi2; 
		this.chi2=new Chi2(); 
		
	}
	
	/**
	 * Wrapper for the Simplex minimisation function. 
	 * @author Jamie Macaulay
	 *
	 */
	public class Chi2 implements MultivariateRealFunction {

		public double logLikelihood(double[] location) throws FunctionEvaluationException, 
		IllegalArgumentException {
			return -value(location) / 2;
		}

		@Override
		public double value(double[] location) throws FunctionEvaluationException{
			return chi2Minimisation.value(location);
		}

	}
	
	public synchronized boolean runAlgorithm() {
		
		double[] result =new double[chi2Minimisation.getDim()];
		LocaliserError error = null;
		double finalChi2=Double.NaN;

		long time1=System.currentTimeMillis();

		NelderMead optimiser = new NelderMead();
		SimpleScalarValueChecker checker = new SimpleScalarValueChecker(1.E-20, 1.E-10);
		optimiser.setConvergenceChecker(checker);
		/*
		 * Typically it takes a few hundred, sometimes nearly 1000 iterations to optimise. 
		 * However, occasionally it gets stuck and just keep on going up into the millions 
		 * of iterations without convergence. Therefore have set a maximum number of 
		 * iterations at which point it will fail if it's not converged. 
		 */
		optimiser.setMaxIterations(1000);
//		RealConvergenceChecker convChecker = optimiser.getConvergenceChecker();
//		if (SimpleScalarValueChecker.class.isAssignableFrom(convChecker.getClass())) {
//			SimpleScalarValueChecker svChecker = (SimpleScalarValueChecker) convChecker;
//			svChecker.
//		}
//		convChecker.
//		optimiser.set
		//			optimiser.setConvergenceChecker(new StopEarly(1));
		RealPointValuePair resultVal;
		try {

			
			double[] start = chi2Minimisation.getStart(); 
			double[] firstStep = chi2Minimisation.getFirstStep();
			optimiser.setStartConfiguration(firstStep);
			resultVal = optimiser.optimize(chi2, GoalType.MINIMIZE, start);
//			System.out.println("Simplex returned after n evaluations: " + optimiser.getIterations());
			double[] point = resultVal.getPoint();
			//localisation result
			result=point;

			//calc chi2 squared.
//			ChiSquaredDistributionImpl chi2Dist = new ChiSquaredDistributionImpl();
//			double p = chi2Dist.cumulativeProbability(result.getValue());

			finalChi2=chi2Minimisation.value(point);
			/*
			 * Now we must calculate errors. Need to look at the log likelihood surface;
			 */
			error=new LikilihoodError(chi2Minimisation, point, chi2Minimisation.getDim());
			
			if (((LikilihoodError) error).getErrorEllipse()==null){
				//return a null result- clearly issue with localisation.
				result=null;
				error=null;
				return false; 
			}
				
//			//next error in direction of result. 
//			//this is a bit difficult to abstract to multiple dimensions...so
//			PamVector pointVec=new PamVector(point);
//			pointVec.normalise();
//			
//			//this only works for 2D and 3D problems; 
//			if (chi2Minimisation.getDim()>=2){
//				resultErrors[resultErrors.length/2]=getLLCurvature( chi2,  point, pointVec);
//				resultErrors[resultErrors.length/2+1]=getLLCurvature( chi2,  point, new PamVector(pointVec).rotate(Math.PI/2));
//			}
//			if (chi2Minimisation.getDim()==3) {
//				double[] rotation={0, Math.PI/2, 0};
//				resultErrors[resultErrors.length/2+2]=getLLCurvature( chi2,  point, PamVector.rotateVector(pointVec, rotation));
//			}

		} catch (OptimizationException e) {
			System.out.println(chi2Minimisation.getDim() + "D Simplex optimisation Exception: " + e.getMessage());
		} catch (FunctionEvaluationException e) {
			System.out.println(chi2Minimisation.getDim() + "D Simplex Function Evaluation Exception: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println(chi2Minimisation.getDim() + "D Simplex Illegal Argument Exception: " + e.getMessage());
		} catch (@SuppressWarnings("hiding") MathException e) {
			System.out.println("Simplex Math Exception: " + e.getMessage());
		}
		
		//now need to add to the final results which includes possible ambiguities. 
		
		//TODO - at the moment does not deal with ambiguities but could do if multiple Simplex result were run and averaged. 
		int ambiguity=1;
		this.result=new double[ambiguity][];
		this.resultErrors=new LocaliserError[ambiguity];
		this.finalChi2=new double[ambiguity];
		
		this.result[0]=result; 
		this.resultErrors[0]=error; 
		this.finalChi2[0]=finalChi2; 

		
		long time2=System.currentTimeMillis();
		this.algorithmTime=time2-time1;
		return true;
	}
	

//	/**
//	 * Get the curvature of a log likelihood surface along any dimension.
//	 * @param chi2 Chi2 surface
//	 * @param point starting point
//	 * @param dim dimension to move in
//	 * @return curvature, expressed as 1 SD error
//	 */
//	private double getLLCurvature(Chi2 chi2, double[] point, int dim) {
//
//		double dis = 10;
//		double err = 0;
//		int nE = 0;
//		try {
//			while (true) {
//				double ll1, ll2, ll3;
//				double[] pos2 = Arrays.copyOf(point, point.length);
//				ll2 = chi2.logLikelihood(pos2);
//				pos2[dim] -= dis;
//				ll1 = chi2.logLikelihood(pos2);
//				pos2 = Arrays.copyOf(point, point.length);
//				pos2[dim] += dis;
//				ll3 = chi2.logLikelihood(pos2);
//				double q = (ll1 + ll3 - 2*ll2) / (dis*dis);
//				err = 1./Math.sqrt(-q);
//				double change = dis/err;
//				if (++nE > 4 || (change > 0.95 && change < 1.05)) {
//					break;
//				}
//				dis = err;
//			}
//		} catch (FunctionEvaluationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return err;
//	}
	
	
	
	/**
	 * Find the position of the point which sets a side of a confidence interval. 
	 * @param chi2 function contianing a Chi2 distribution
	 * @param point starting point 
	 * @param dir direction (-1 or +1);
	 * @param dim dimension (0 for x, 1 for y)
	 * @param nSig
	 */
	private double findCIInterval(Chi2 chi2, double[] point, int dir, int dim, double nSig) {
		point = Arrays.copyOf(point, point.length);
		double step = 100;
		double val = point[dim];
		double target;
		int nStep = 0;
		try {
			double bestVal = chi2.value(point);
			target = bestVal + Math.E * nSig;
			while (step > Math.ulp(val) && nStep++ < 1000) {
				point[dim] = val + step*dir;
				if (chi2.value(point) > target) {
					step /= 2;
				}
				else {
					val = point[dim];
				}
			}
			return val;

		} catch (FunctionEvaluationException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return Double.NaN;
	}

	@Override
	public double[][] getResult() {
		return result;
	}
	
	@Override
	public LocaliserError[] getErrors() {
		return resultErrors;
	}
	
	public double[] getChi2() {
		return finalChi2;
	}

	public long getAlgorithmTime() {
		return algorithmTime;
	}

	@Override
	public void notifyStatus(int status, double progress) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean hasParams() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public LocaliserPane<?> getSettingsPane() {
		return null;
	}


	/**
	 * Get the simplex chi2 fucntion
	 * @return - the simplex chi2 function. 
	 */
	public MultivariateRealFunction getChi2Function() {
		return chi2;
	}




}
	

	