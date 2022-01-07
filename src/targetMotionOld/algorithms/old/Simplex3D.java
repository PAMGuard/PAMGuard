//package targetMotionOld.algorithms.old;
//
//import java.awt.Color;
//import java.util.Arrays;
//
//import org.apache.commons.math.FunctionEvaluationException;
//import org.apache.commons.math.MathException;
//import org.apache.commons.math.analysis.MultivariateRealFunction;
//import org.apache.commons.math.distribution.ChiSquaredDistribution;
//import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
//import org.apache.commons.math.optimization.GoalType;
//import org.apache.commons.math.optimization.OptimizationException;
//import org.apache.commons.math.optimization.RealConvergenceChecker;
//import org.apache.commons.math.optimization.RealPointValuePair;
//import org.apache.commons.math.optimization.direct.NelderMead;
//
//import pamMaths.PamVector;
//import targetMotionOld.EventRotator;
//import targetMotionOld.TargetMotionLocaliser;
//import targetMotionOld.algorithms.AbstractTargetMotionModel;
//import Localiser.LocaliserPane;
//import Localiser.algorithms.LocContents;
//import Localiser.detectionGroupLocaliser.GroupDetection;
//import Localiser.detectionGroupLocaliser.GroupLocResult;
//import Localiser.detectionGroupLocaliser.GroupLocalisation;
//import PamDetection.AbstractLocalisation;
//import PamUtils.LatLong;
//
///**
// * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
// * with Jamie's new one, but keep this one until Jamie's is working. 
// * @author Doug Gillespie
// *
// * @param <T>
// */
//@SuppressWarnings("deprecation")
//public class Simplex3D <T extends GroupDetection> extends AbstractTargetMotionModel<T> {
//
//	private EventRotator eventRotator;
//	private PamVector[] subDetectionOrigins;
//	private PamVector[] subDetectionHeadings;
//	//	private double[] subDetectionAngles;
//	private double[] subDetectionAngleErrors;
//	private boolean[] faultPoints;
//	private PamVector[][] worldVectors;
//
//	private int nSubDetections;
//	private PamVector[] usedWorldVectors;
//	private double[] usedWorldVectorAngles;
//	private ChiSquaredDistribution chi2Dist;
//
//	public Simplex3D(TargetMotionLocaliser<T> targetMotionLocaliser) {
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public String getName() {
//		return "3D simplex optimisation";
//	}
//
//
//	public GroupLocResult[] runModel(T pamDetection) {
//		if (pamDetection == null) {
//			return null;
//		}
//		eventRotator = new EventRotator(pamDetection);
//
//		nSubDetections = eventRotator.getnSubDetections();
//		subDetectionOrigins = eventRotator.getRotatedOrigins();
//		subDetectionHeadings = eventRotator.getRotatedHeadings();
//		worldVectors = eventRotator.getRotatedWorldVectors();
//
//		//		subDetectionAngles = new double[nSubDetections];
//		subDetectionAngleErrors = new double[nSubDetections];
//		faultPoints = new boolean[nSubDetections];
//
//		AbstractLocalisation localisation;
//		double angle;
//		double angleError;
//		double[] angles;
//		double[] angleErrors;
//		double rotationAngle = eventRotator.getReferenceAngle();
//		int minVectors = Integer.MAX_VALUE;
//		int maxVectors = 0;
//		int totalVectors = 0;
//		int nVectors;
//		for (int i = 0; i < nSubDetections; i++) {
//			localisation = pamDetection.getSubDetection(i).getLocalisation();
//			angles = localisation.getAngles();
//			angleErrors = localisation.getAngleErrors();
////			angleError = localisation.get
//			if (angles == null || angles.length < 1 || worldVectors[i].length == 0) {
//				faultPoints[i] = true;
//				continue;
//			}
//			faultPoints[i] = false;
//			angle = angles[0];
//			if (angleErrors != null && angleErrors.length >= 1) {
//				angleError = angleErrors[0]; 
//			}
//			else {
//				angleError = Double.NaN;
//			}
//			if (Double.isNaN(angleError)){
//				angleError = 3 * Math.PI / 180 / Math.pow(Math.sin(angle), 2);
//				//				angleError = 2. * Math.PI/180; // use a one degree error
//			}
//			angleError = Math.max(angleError, 1.e-6);
//			//			subDetectionAngles[i] = angle;
//			subDetectionAngleErrors[i] = angleError;
//			nVectors = worldVectors[i].length;
//			minVectors = Math.min(minVectors, nVectors);
//			maxVectors = Math.max(maxVectors, nVectors);
//			totalVectors += nVectors;
//		}
//		maxVectors = 1;
//		usedWorldVectors = new PamVector[nSubDetections];
//		usedWorldVectorAngles = new double[nSubDetections];
//		PamVector notRot;
//		int n;
//		int nDegreesFreedom = nSubDetections - 2;
//		GroupLocResult[] tmResults = new GroupLocResult[maxVectors];
//		for (int side = 0; side < maxVectors; side++) {
//			for (int i = 0; i < nSubDetections; i++) {
//				if (worldVectors[i] == null || worldVectors[i].length == 0) {
//					continue;
//				};
//				n = worldVectors[i].length;
//				if (side >= n) {
//					continue;
//				}
//				/*
//				 * Now rotate the vector by the array heading at that point
//				 * so that this operation does not have to be done repeatedly
//				 * during the analysis. These rotations should be small since the 
//				 * subDetectionHeadings are already pretty well aligned with x = 0;
//				 */
//				notRot = worldVectors[i][side];
//				usedWorldVectors[i] = worldVectors[i][side];
//				
//				
//				/**
//				 * Go back to knowing the angle from the trackline to the detection relative to the 
//				 * track at that point. 
//				 */
//				usedWorldVectorAngles[i] = Math.acos(usedWorldVectors[i].dotProd(subDetectionHeadings[i]));
//				/**
//				 * Dump some information
//				 */
//				if (false) {
//					System.out.println(String.format("Simplex side %d-%d, origin %s, array Head %s, "+
//							"detection angle notrot = %s rot = %s",
//							side, i, subDetectionOrigins[i].toString(), 
//							subDetectionHeadings[i].toString(), notRot.toString(), usedWorldVectors[i].toString()));
//				}
//			}
//			Chi2 chi2 = new Chi2();
//
//			NelderMead optimiser = new NelderMead();
//			//			optimiser.setConvergenceChecker(new StopEarly(1));
//			RealPointValuePair result;
//			try {
//				double[] start = {0, 0, -100};
//				double[] firstStep = {10, 100, 10};
//				optimiser.setStartConfiguration(firstStep);
//				result = optimiser.optimize(chi2, GoalType.MINIMIZE, start);
//				double[] point = result.getPoint();
//				/**
//				 * Now want to find the confidence interval around that point which comes from 
//				 * the curvature of the log likelihood function. 
//				 * Should be able to get a CI by finding the values of point with p[0] constant
//				 * and p[1] varying until the chi2 value increases by e.
//				 */
//				double[] ci = new double[2];
//				ci[0] = findCIInterval(chi2, point, -1, 1, 1);
//				ci[1] = findCIInterval(chi2, point, 1, 1, 1);
//
//				/*
//				 * Check the chi2
//				 */
//				chi2Dist = new ChiSquaredDistributionImpl(nDegreesFreedom);
//				double p = chi2Dist.cumulativeProbability(result.getValue());
//
//				/**
//				 * Try to get the curvature of the log likelihood function
//				 * 
//				 */
////				double dis = 10;
////				double err = 0;
////				int nE = 0;
////				while (true) {
////					double ll1, ll2, ll3;
////					double[] pos2 = result.getPoint();
////					ll2 = chi2.logLikelihood(pos2);
////					pos2[1] -= dis;
////					ll1 = chi2.logLikelihood(pos2);
////					pos2 = result.getPoint();
////					pos2[1] += dis;
////					ll3 = chi2.logLikelihood(pos2);
////					double q = (ll1 + ll3 - 2*ll2) / (dis*dis);
////					err = 1./Math.sqrt(-q);
////					double change = dis/err;
////					if (++nE > 4 || (change > 0.95 && change < 1.05)) {
////						break;
////					}
////					dis = err;
////				}
//				double err = getLLCurvature(chi2, result.getPoint(), 1);
//
//
//				//				System.out.println(String.format("Location side %d [%3.1f,%3.1f] Chi2=%3.1f, CI = [%3.1f to %3.1f]",
//				//						side, point[0], point[1], result.getValue(), ci[0], ci[1]));
//				PamVector pos = new PamVector(point[0], point[1], point[2]);
//				LatLong ll = eventRotator.metresToLatLong(pos, true);
//				PamVector origin = new PamVector(point[0], 0, 0);
//				LatLong oll = eventRotator.metresToLatLong(origin, true);
//				long beamTime = eventRotator.metresToTime(origin, true);
//
//
//				tmResults[side] = new GroupLocResult(this, ll, side, result.getValue());
//				tmResults[side].setBeamLatLong(oll);
//				tmResults[side].setBeamTime(beamTime);
//				tmResults[side].setPerpendicularDistance(point[1]);
//				for (int e = 0; e < 3; e++) {
//					err = getLLCurvature(chi2, result.getPoint(), e);
//					tmResults[side].setError(e, err);
//				}
//				tmResults[side].setProbability(p);
//				tmResults[side].setnDegreesFreedom(nDegreesFreedom);
//				tmResults[side].setReferenceHydrophones(eventRotator.getReferenceHydrophones());
//				//				int nDF = nSubDetections - 2;
//				/**
//				 * log likelihood is -Chi2/2. aic is 2*LL + 2*df, so aic = chi2+2df
//				 */
//				double aic = result.getValue() + 6.;
//				tmResults[side].setAic(aic);
//			} catch (OptimizationException e) {
//				System.out.println("Simplex 3D optimisation Exception: " + e.getMessage());
//				//				e.printStackTrace();
//			} catch (FunctionEvaluationException e) {
//				System.out.println("Simplex 3D Function Evaluation Exception: " + e.getMessage());
//				//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				System.out.println("Simplex 3D Illegal Argument Exception: " + e.getMessage());
//				//				e.printStackTrace();
//			} catch (MathException e) {
//				System.out.println("Simplex 3D Math Exception: " + e.getMessage());
//				// TODO Auto-generated catch block
//				//				e.printStackTrace();
//			}
//		}
//
//		return tmResults;
//	}
//
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
//
//	/**
//	 * Find the position of the point which sets a side of a confidence interval. 
//	 * @param chi2 function contianing a Chi2 distribution
//	 * @param point starting point 
//	 * @param dir direction (-1 or +1);
//	 * @param dim dimension (0 for x, 1 for y)
//	 * @param nSig
//	 */
//	private double findCIInterval(Chi2 chi2, double[] point, int dir, int dim, double nSig) {
//		point = Arrays.copyOf(point, point.length);
//		double step = 100;
//		double val = point[dim];
//		double target;
//		int nStep = 0;
//		try {
//			double bestVal = chi2.value(point);
//			target = bestVal + Math.E * nSig;
//			while (step > Math.ulp(val) && nStep++ < 1000) {
//				point[dim] = val + step*dir;
//				if (chi2.value(point) > target) {
//					step /= 2;
//				}
//				else {
//					val = point[dim];
//				}
//			}
//			return val;
//
//		} catch (FunctionEvaluationException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		}
//		return Double.NaN;
//	}
//
//	@Override
//	public String getToolTipText() {
//		return "<html>Simplex optimisation assuming animal is at surface</html>";
//	}
//
//	/**
//	 * Calculate a Chi2 for the simple case of a two element array
//	 * @author Doug Gillespie
//	 *
//	 */
//	class Chi2 implements MultivariateRealFunction {
//
//		public double logLikelihood(double[] location) throws FunctionEvaluationException, 
//		IllegalArgumentException {
//			return -value(location) / 2;
//		}
//		@Override
//		public double value(double[] location) throws FunctionEvaluationException,
//		IllegalArgumentException {
//			if (nSubDetections < 2) {
//				throw new FunctionEvaluationException(location);
//			}
//			int nUsed = 0;
//			PamVector pointVec;
//			PamVector subDetOrigin;
//			PamVector angleVec;
//			double[] vecData;
//			double angle;
//			double chiTot = 0.;
//			for (int i = 0; i < nSubDetections; i++) {
//				if (faultPoints[i] || usedWorldVectors[i] == null) {
//					continue;
//				}
//				nUsed++;
//				subDetOrigin = subDetectionOrigins[i];
//				vecData = subDetOrigin.getVector();
//				/*
//				 * Calculate a vector pointing from the origin of this detection to the 
//				 * current point in the optimisation. 
//				 */
//				pointVec = new PamVector(location[0]-vecData[0], location[1]-vecData[1], location[2]-vecData[2]);
//				if (pointVec.normalise() == 0) {
//					/*
//					 * we're right on the origin for that point,so the angle has to be zero 
//					 * so no need to add anything to the Chi2.
//					 */
//					continue; 
//				}
//				
//				/**
//				 * Now that the pointVec vector is in three dimensions we have to use a slightly different
//				 * method for calculating chi2. 1) work out the angle between pointVex and the 
//				 * rotated heading at that point along the track, then work out
//				 * the angle of the worldVector, then simply look at the angle difference. 
//				 */
//				angle = Math.acos(pointVec.dotProd(subDetectionHeadings[i]));
//				/*
//				 * Get the vector representing the angle from the 
//				 */
//				angle -= usedWorldVectorAngles[i];
//				angle /= subDetectionAngleErrors[i];
//				angle *= angle;
//				chiTot += angle;
//			}
//			if (nUsed == 0) {
//				throw new FunctionEvaluationException(location);
//			}
//			if (Double.isNaN(chiTot)) {
//				throw new FunctionEvaluationException(location);
//			}
//
//			// penalise flying !
//			if (location[2] > 0) {
//				chiTot *= (location[2]+1);
//			}
//
//			return chiTot;
//		}
//
//	}
//
//	class StopEarly implements RealConvergenceChecker {
//
//		private double maxDist;
//		int smallCount = 0;
//		StopEarly(double maxDist) {
//			this.maxDist = maxDist*maxDist;
//		}
//
//		@Override
//		public boolean converged(int iteration, RealPointValuePair previous,
//				RealPointValuePair current) {
//			double[] p1 = current.getPoint();
//			double[] p2 = previous.getPoint();
//			double dist = (p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1]);
//			if (dist < maxDist) {
//				if (++smallCount > 3) {
//					return true;
//				}
//			}
//			else {
//				smallCount = 0;
//			}
//			return false;
//		}
//
//	}
//
//	private Color symbolColour = new Color(199,0,215);
//
//	@Override
//	Color getSymbolColour() {
//		return symbolColour;
//	}
//
//	@Override
//	public LocContents getLocContents() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public LocaliserPane<?> getSettingsPane() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean hasParams() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public AbstractLocalisation runModel(T pamDataUnit, boolean addLoc) {
//		GroupLocResult[] result=runModel(pamDataUnit);
//		//HACK to make old code compatible for testing purposes. 
//		GroupLocalisation groupLocalisation=new GroupLocalisation(pamDataUnit, null); 
//		
//		for (int i=0; i<result.length; i++){
//			groupLocalisation.addTargetMotionResult(result[i]); 
//		}
//		return groupLocalisation;
//	}
//
//	@Override
//	public void notifyModelProgress(double progress) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	
//	
//	/**public TransformGroup getPlotSymbol3D(Vector3f vector, Double[] sizeVector, double minSize){
//		
//		TransformGroup trg=new TransformGroup();
//		Transform3D posStretch=new Transform3D();
//		Appearance app=new Appearance();
//		
//		for (int i=0; i<sizeVector.length;i++){
//			if (sizeVector[i]<minSize){
//				sizeVector[i]=minSize;
//			}
//		}
//		
//		
//		ColoringAttributes colour=new ColoringAttributes();
//		colour.setColor(new Color3f(1f,1f,0f));
//		app.setColoringAttributes(colour);
//	
//		Sphere sphr=new Sphere(5f);
//		sphr.setAppearance(app);
//		trg.addChild(sphr);
//		
//		posStretch.setTranslation(vector);
//		posStretch.setScale(new Vector3d(sizeVector[0],sizeVector[1],sizeVector[2] ));
//		
//		trg.setTransform(posStretch);
//		
//		
//		return trg;
//
//	}**/
//
//	
//}
