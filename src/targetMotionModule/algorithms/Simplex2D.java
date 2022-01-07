package targetMotionModule.algorithms;


import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import pamMaths.PamVector;
import targetMotionModule.AbstractTargetMotionInformation;
import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionResult;
import GPS.GpsData;
import PamDetection.AbstractLocalisation;
import PamDetection.PamDetection;
import PamUtils.LatLong;

public class Simplex2D extends AbstractTargetMotionModel {

	private PamVector[] subDetectionOrigins;
	private PamVector[] subDetectionHeadings;
	//	private double[] subDetectionAngles;
	private double[] subDetectionAngleErrors;
	private boolean[] faultPoints;
	private PamVector[][] worldVectors;

	private int nSubDetections;
	private PamVector[] usedWorldVectors;
	private ChiSquaredDistribution chi2Dist;
	private TargetMotionInformation targetMotionInformation;
	private double[] startPoint;
	private double[] firstStep;

	public Simplex2D(TargetMotionLocaliser targetMotionLocaliser) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "2D simplex optimisation";
	}

	@Override
	public boolean hasParameters() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parametersDialog() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TargetMotionResult[] runModel(TargetMotionInformation targetMotionInformation) {
		if (targetMotionInformation == null) {
			return null;
		}

		this.targetMotionInformation=targetMotionInformation;

		nSubDetections = targetMotionInformation.getNDetections();
		subDetectionOrigins = targetMotionInformation.getOrigins();
		//TODO- change algorithm to cope with Euler angles rather than just headings
		subDetectionHeadings = AbstractTargetMotionInformation.getHeadingVectors(targetMotionInformation.getEulerAngles());
		worldVectors = targetMotionInformation.getWorldVectors();

		//		subDetectionAngles = new double[nSubDetections];
		subDetectionAngleErrors = new double[nSubDetections];
		faultPoints = new boolean[nSubDetections];

		AbstractLocalisation localisation;
		double angle;
		double angleError;
		double[] angles;
		double[] angleErrors;
		//double rotationAngle = targetMotionInformation.getReferenceAngle();
		int minVectors = Integer.MAX_VALUE;
		int maxVectors = 0;
		int totalVectors = 0;
		int nVectors;
		//need to split the vectors up. 
		for (int i = 0; i < nSubDetections; i++) {
			localisation = targetMotionInformation.getCurrentDetections().get(i).getLocalisation();
			angles = localisation.getAngles();
			angleErrors = localisation.getAngleErrors();
			//			angleError = localisation.get
			if (angles == null || angles.length < 1 || worldVectors[i].length == 0) {
				faultPoints[i] = true;
				continue;
			}
			faultPoints[i] = false;
			angle = angles[0];
			if (angleErrors != null && angleErrors.length >= 1) {
				angleError = angleErrors[0]; 
			}
			else {
				angleError = Double.NaN;
			}
			if (Double.isNaN(angleError)){
				angleError = 3 * Math.PI / 180 / Math.pow(Math.sin(angle), 2);
				//				angleError = 2. * Math.PI/180; // use a one degree error
			}
			angleError = Math.max(angleError, 1.e-6);

			subDetectionAngleErrors[i] = angleError;
			nVectors = worldVectors[i].length;
			minVectors = Math.min(minVectors, nVectors);
			maxVectors = Math.max(maxVectors, nVectors);
			totalVectors += nVectors;
		}
		usedWorldVectors = new PamVector[nSubDetections];
		PamVector notRot;
		int n;
		int nDegreesFreedom = nSubDetections - 2;
		TargetMotionResult[] tmResults = new TargetMotionResult[maxVectors];
		for (int side = 0; side < maxVectors; side++) {
			for (int i = 0; i < nSubDetections; i++) {
				if (worldVectors[i] == null || worldVectors[i].length == 0) {
					continue;
				};
				n = worldVectors[i].length;
				if (side >= n) {
					continue;
				}
				/*
				 * Now rotate the vector by the array heading at that point
				 * so that this operation does not have to be done repeatedly
				 * during the analysis. These rotations should be small since the 
				 * subDetectionHeadings are already pretty well aligned with x = 0;
				 */
				notRot = worldVectors[i][side];
				usedWorldVectors[i] = worldVectors[i][side];
			}
			Chi2 chi2 = new Chi2();

			NelderMead optimiser = new NelderMead();
			//			optimiser.setConvergenceChecker(new StopEarly(1));
			RealPointValuePair result;
			try {
				double[] start = getStartPoint();
				double[] firstStep = getFirstStep();
				optimiser.setStartConfiguration(firstStep);
				result = optimiser.optimize(chi2, GoalType.MINIMIZE, start);
				double[] point = result.getPoint();
				/**
				 * Now want to find the confidence interval around that point which comes from 
				 * the curvature of the log likelihood function. 
				 * Should be able to get a CI by finding the values of point with p[0] constant
				 * and p[1] varying until the chi2 value increases by e.
				 */
				double[] ci = new double[2];
				ci[0] = findCIInterval(chi2, point, -1, 1, 1);
				ci[1] = findCIInterval(chi2, point, 1, 1, 1);

				/*
				 * Check the chi2
				 */
				chi2Dist = new ChiSquaredDistributionImpl(Math.max(nDegreesFreedom, 1));
				double p = chi2Dist.cumulativeProbability(result.getValue());		

				//				System.out.println(String.format("Location side %d [%3.1f,%3.1f] Chi2=%3.1f, CI = [%3.1f to %3.1f]",
				//						side, point[0], point[1], result.getValue(), ci[0], ci[1]));
				PamVector pos = new PamVector(point[0], point[1], 0.);
				LatLong ll = targetMotionInformation.metresToLatLong(pos);
				PamVector origin = new PamVector(0, 0, 0);
				LatLong oll = targetMotionInformation.metresToLatLong(origin);
				//long beamTime = targetMotionInformation.metresToTime(origin);
				tmResults[side] = new TargetMotionResult(targetMotionInformation.getTimeMillis(),this, ll, side, result.getValue());
				GpsData beamPos=targetMotionInformation.getBeamLatLong(ll);
				tmResults[side].setBeamLatLong(new GpsData(beamPos));
				tmResults[side].setBeamTime(targetMotionInformation.getBeamTime(beamPos));
				tmResults[side].setStartLatLong(targetMotionInformation.getCurrentDetections().get(0).getOriginLatLong(false));
				tmResults[side].setEndLatLong(targetMotionInformation.getCurrentDetections().get(targetMotionInformation.getCurrentDetections().size()-1).getOriginLatLong(false));
				//tmResults[side].setBeamTime(beamTime);
				tmResults[side].setPerpendicularDistance(ll.distanceToMetres(tmResults[side].getBeamLatLong()));
				double err;
				for (int e = 0; e < 2; e++) {
					err = getLLCurvature(chi2, result.getPoint(), e);
					tmResults[side].setError(e, err);
				}
				tmResults[side].setProbability(p);
				tmResults[side].setnDegreesFreedom(nDegreesFreedom);				tmResults[side].setReferenceHydrophones(targetMotionInformation.getReferenceHydrophones());
				//				int nDF = nSubDetections - 2;
				/**
				 * log likelihood is -Chi2/2. aic is 2*LL + 2*df, so aic = chi2+2df
				 */
				double aic = result.getValue() + 4.;
				tmResults[side].setAic(aic);
			} catch (OptimizationException e) {
				System.out.println("Simplex 2D optimisation Exception: " + e.getMessage());
				//				e.printStackTrace();
			} catch (FunctionEvaluationException e) {
				System.out.println("Simplex 2D Function Evaluation Exception: " + e.getMessage());
				//				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println("Simplex 2D Illegal Argument Exception: " + e.getMessage());
				//				e.printStackTrace();
			} catch (MathException e) {
				System.out.println("Simplex 2D Math Exception: " + e.getMessage());
				// TODO Auto-generated catch block
				//				e.printStackTrace();
			}
		}

		return tmResults;
	}

	/**
	 * Get the start point for the simplex
	 * @return two element double array. 
	 */
	public double[] getStartPoint() {
		if (startPoint == null || startPoint.length != 2) {
			startPoint = new double[2];
		}
		return startPoint.clone();
	}

	/**
	 * Set the start point for the simplex. 
	 * @param startPoint two eleemnt double array. 
	 */
	public void setStartPoint(double[] startPoint) {
		if (startPoint == null || startPoint.length != 2) {
			startPoint = new double[2];
		}
		this.startPoint = startPoint;
	}

	/**
	 * Get the curvature of a log likelihood surface along any dimension
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
			System.out.println("FunctionEvaluationException estimating LogLikelihood curvature: " + e.getMessage());
			return Double.NaN;
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("IllegalArgumentException estimating LogLikelihood curvature: " + e.getMessage());
			return Double.NaN;
		}
		return err;
	}

	/**
	 * Find the position of the point which sets a side of a confidence interval. 
	 * @param chi2 function containing a Chi2 distribution
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
			System.out.println("FunctionEvaluationException estimating LogLikelihood curvature: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println("IllegalArgumentException estimating LogLikelihood curvature: " + e.getMessage());
		}
		return Double.NaN;
	}

	@Override
	public String getToolTipText() {
		return "<html>Simplex optimisation assuming animal is at surface</html>";
	}

	/**
	 * Calculate a Chi2 for the simple case of a two element array
	 * @author Doug Gillespie
	 *
	 */
	class Chi2 implements MultivariateRealFunction {

		public double logLikelihood(double[] location) throws FunctionEvaluationException, 
		IllegalArgumentException {
			return -value(location) / 2;
		}
		@Override
		public double value(double[] location) throws FunctionEvaluationException,
		IllegalArgumentException {
			if (nSubDetections < 2) {
				throw new FunctionEvaluationException(location);
			}
			int nUsed = 0;
			PamVector pointVec = null;
			PamVector subDetOrigin;
			PamVector angleVec = null;
			double[] vecData;
			double angle;
			double chiTot = 0.;
			for (int i = 0; i < nSubDetections; i++) {
				if (faultPoints[i] || usedWorldVectors[i] == null) {
					continue;
				}
				nUsed++;
				subDetOrigin = subDetectionOrigins[i];
				vecData = subDetOrigin.getVector();
				/*
				 * Calculate a vector pointing from the origin of this detection to the 
				 * current point in the optimisation. 
				 */
				pointVec = new PamVector(location[0] - vecData[0], location[1] - vecData[1], 0);
				if (pointVec.normalise() == 0) {
					/*
					 * we're right on the origin for that point,so the angle has to be zero 
					 * so no need to add anything to the Chi2.
					 */
					continue; 
				}
				/*
				 * Get the vector representing the angle from the 
				 */
				angleVec = usedWorldVectors[i];
				angle = Math.acos(limUnity(angleVec.dotProd(pointVec)));
				angle /= subDetectionAngleErrors[i];
				angle *= angle;
				chiTot += angle;
			}
			if (nUsed == 0) {
				throw new FunctionEvaluationException(location);
			}
			if (Double.isNaN(chiTot)) {
				//				System.out.println("NaN exception in Simplex model");
				//				System.out.println("angleVec = " + angleVec);
				//				System.out.println("pointVec = " + pointVec);
				//				System.out.println("Dot product = " + angleVec.dotProd(pointVec));
				//				System.out.println("Limited Dot product = " + limUnity(angleVec.dotProd(pointVec)));
				//				System.out.println("aCos of Limited Dot product = " + Math.acos(limUnity(angleVec.dotProd(pointVec))));
				throw new FunctionEvaluationException(location);
			}

			return chiTot;
		}

		/**
		 * Limit a value to +/1.
		 * @param dotProd
		 * @return
		 */
		private double limUnity(double dotProd) {
			return Math.max(-1.0, Math.min(1.0, dotProd));
		}

	}

	class StopEarly implements RealConvergenceChecker {

		private double maxDist;
		int smallCount = 0;
		StopEarly(double maxDist) {
			this.maxDist = maxDist*maxDist;
		}

		@Override
		public boolean converged(int iteration, RealPointValuePair previous,
				RealPointValuePair current) {
			double[] p1 = current.getPoint();
			double[] p2 = previous.getPoint();
			double dist = (p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1]);
			if (dist < maxDist) {
				if (++smallCount > 3) {
					return true;
				}
			}
			else {
				smallCount = 0;
			}
			return false;
		}

	}

	private Color symbolColour = new Color(255,170,0);
	@Override
	Color getSymbolColour() {
		return symbolColour;
	}

	/**
	 * @return the firstStep
	 */
	protected double[] getFirstStep() {
		if (firstStep == null || firstStep.length != 2) {
			firstStep = new double[2];
			firstStep[0] = firstStep[1] = 10;
		}
		return firstStep.clone();
	}

	/**
	 * @param firstStep the firstStep to set
	 */
	protected void setFirstStep(double[] firstStep) {
		if (firstStep == null || firstStep.length != 2) {
			firstStep = new double[2];
			firstStep[0] = firstStep[1] = 10;
		}
		this.firstStep = firstStep;
	}


}
