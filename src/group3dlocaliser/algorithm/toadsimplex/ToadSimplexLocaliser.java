package group3dlocaliser.algorithm.toadsimplex;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.direct.NelderMead;
import Array.ArrayManager;
import Array.SnapshotGeometry;
import Localiser.LocaliserPane;
import Localiser.algorithms.genericLocaliser.MinimisationFunction;
import Localiser.algorithms.locErrors.EllipticalError;
import Localiser.algorithms.locErrors.LikilihoodError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamDetection.LocContents;
import PamUtils.CPUMonitor;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.Chi2Data;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import group3dlocaliser.algorithm.LogLikelihoodData;
import group3dlocaliser.algorithm.crossedbearing.CrossedBearingSQLAddon;
import group3dlocaliser.algorithm.toadbase.TOADBaseAlgorithm;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import pamMaths.PamHistogram;
import pamMaths.PamVector;

public class ToadSimplexLocaliser extends TOADBaseAlgorithm {

//	private HyperbolicLocaliser hyperbolicLocaliser;
//
//	private TOADGridSearch toadGridLocaliser;
//
//	private double[][] lastGoodDelays;
//
//	private double[] lastPosition;
	
//	private double[][] startOffsets = {{0,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{1,0,0},{-1,0,0}};
	/*
	 * Seeing absolutely NO improvement whatsoever with pinger data after 2 or 3 trials, so might as well 
	 * stick with 
	 */
	int nStartPositions = 4;

	private Random random = new Random();

	int nCalls = 0;
	int nFails = 0;


	private int[] bestResults = new int[nStartPositions];
	private PamHistogram errHist = new PamHistogram(0,  200, 200);
	private PamHistogram bestTryHist = new PamHistogram(0,nStartPositions, nStartPositions+1);
	
	private CPUMonitor cpuGrid, cpuHyperbolic, cpuSimplex;

	private int nDimensions;


	public ToadSimplexLocaliser(Group3DLocaliserControl group3dLocaliser, int nDimensions) {
		super(group3dLocaliser);
		this.nDimensions = nDimensions;
//		hyperbolicLocaliser = new HyperbolicLocaliser(group3dLocaliser);
//		toadGridLocaliser = new TOADGridSearch(group3dLocaliser);
		cpuGrid = new CPUMonitor();
		cpuHyperbolic = new CPUMonitor();
		cpuSimplex = new CPUMonitor();
	}


	@Override
	public GroupLocalisation processTOADs(PamDataUnit groupDataUnit, SnapshotGeometry geometry, TOADInformation toadInformation) {
		// get the channel geometry. 
		nCalls++;
		
		boolean usell = false; 

	
//		/////////////////////////////
//		System.out.println("TOADInformation: " + toadInformation); 
//		PamArrayUtils.printArray(toadInformation.getToadSeconds()); 
//		//////////////////////
		
		PamVector centre = geometry.getGeometricCentre();

		double arraySize = geometry.getMaxSeparation();
		double stepSize = Math.max(0.1, arraySize/10.);
		double[] firstStep = {stepSize, stepSize, stepSize};
		/**
		 * Chi2 values are calculated for positions relative to the array centre, so 
		 * can always start at the array centre. 
		 */
		double[] start = {0., 0., 0.};
		RealPointValuePair[] results = new RealPointValuePair[nStartPositions];
		Chi2Data[] resultChiData = new Chi2Data[nStartPositions];
		cpuSimplex.start();
		
		MultivariateRealFunction chiFunc = null;

		for (int iStart = 0; iStart < nStartPositions; iStart ++) {
			/*
			 * Start the first iteration in the centre, then randomly jump about guided by the array size. 
			 */
//			start = geometry.getGeometricCentre().getVector();
			if (iStart > 0) {
				for (int i = 0; i < nDimensions; i++) {
					start[i] = random.nextGaussian()*arraySize*1.;
//					start[i] = arraySize*startOffsets[iStart][i];
//					start[i] = (random.nextDouble()-.5)*2.*arraySize*1.;
				}				
			}
//			if (start == null || start.length != 3) {
//				start = getStartLocation();
//				//			start = geometry.getGeometricCentre().getVector();
//				//			start[0] = 2000;
//			}
			if (nDimensions < 3) {
				// deal with 2D case
				start = Arrays.copyOf(start, nDimensions);
			}
			//MultivariateRealFunction chiFunc;
			GoalType goal;
			if (usell) {
				//This does not seem to work very well!
				chiFunc = new LogLikelihoodFunction(geometry, toadInformation);
				goal = GoalType.MAXIMIZE;
			}
			else {
				chiFunc = new Chi2Function(geometry, toadInformation);
				goal = GoalType.MINIMIZE;
			}


			NelderMead optimiser = new NelderMead();
			SimpleScalarValueChecker checker = new SimpleScalarValueChecker(1.E-6, 1.E-6);
			optimiser.setConvergenceChecker(checker);
			optimiser.setStartConfiguration(firstStep);
			//		optimiser.s
			optimiser.setMaxIterations(100000);
			RealPointValuePair result = null;
			try {
				result = optimiser.optimize(chiFunc, goal, start);
			} catch (OptimizationException | FunctionEvaluationException | IllegalArgumentException e) {
				System.out.println("Error in TOADSimplexLocaliser.processTOADs: " + e.getMessage());
				nFails++;
				System.out.printf("%d  fails out of %d = %3.2f%%\n", nFails, nCalls, (double) nFails * 100. / nCalls);
				resultChiData[iStart] = null;
				continue;
			}
			results[iStart] = result;
			//			double[] pos = result.getPoint();
			//			resultVectors[iStart] = new PamVector(pos);
			//		lastGoodDelays = delays;
			
//			System.out.println("Simplex result " + iStart); 
//			PamArrayUtils.printArray( result.getPoint());

			Chi2Data chiData = calcChi2(geometry, toadInformation, result.getPoint());
			resultChiData[iStart] = chiData; 
			
			
		}
		cpuSimplex.stop();

		int iBest = getBestResult(results, resultChiData);
		if (iBest < 0) {
			return null;
		}
		Chi2Data chiData = resultChiData[iBest];
		
		if (chiData.getChi2() / chiData.getDegreesOfFreedom() < 100) {
			bestTryHist.addData(iBest);
		}
		
		double[] posVec = results[iBest].getPoint();
		double r = new PamVector(posVec).norm(nDimensions);
		
		
		//		lastGoodDelays = delays;
		
//		System.out.println("Simplex best " + iBest); 
//		PamArrayUtils.printArray(posVec);

		if (chiData.getChi2() / chiData.getDegreesOfFreedom() < 100 && r < 30) {
			// some diagnostic book keeping for better results.
			bestResults[iBest]++;
//			if (hyperbolicStart != null) {
//				double distFromHyp = new PamVector(posVec).sub(new PamVector(hyperbolicStart)).norm();
////				distFromHyp = Math.abs(posVec[2]-hyperbolicStart[2]);
//				errHist.addData(distFromHyp);
//			}
		}


//		long subUID = groupDataUnit.getSubDetection(0).getUID();
		//				if (subUID >= 652015545 && subUID <= 652015547) {
		//				if (groupDataUnit.getSubDetectionsCount() == 3) {
		//					System.out.printf("UID %d: Grid: %s, Hyp %s, Simp %s\n", subUID, gv, hv, new PamVector(posVec));
		//					int a = 1+2;
		//				}

		if (chiData.getDegreesOfFreedom() <= 1) {
			return null;
		}
		// deal with 2D case
		if (posVec.length < 3) {
			posVec = Arrays.copyOf(posVec, 3);
		}
//		ChiSquaredDistribution chiDist = new ChiSquaredDistribution(chiData.getDegreesOfFreedom());
//		double cumProb = chiDist.cumulativeProbability(chiData.getChi2());
		//		for (int i = 0; i < 3; i++) {
		//			System.out.printf("Res %d = %3.5f, ", i, posVec[i]);
		//		}
//		SimpleError cartErr = estimateCartesianError(geometry, toadInformation, posVec);
		
		//FIXME - this return super weird results
		EllipticalError ellipErr = estimateEllipticalError(geometry, toadInformation, posVec);
//		if (usell) {
//			 ellipErr = estimateEllipticalError(geometry, toadInformation, posVec);
//		}
//		else {
//			 ellipErr = estimateEllipticalError(geometry, toadInformation, posVec);
//
////			SimpleMinimisation simpleMin  = new SimpleMinimisation(chiFunc, nDimensions, start, firstStep); 
////			ellipErr = new LikilihoodError(simpleMin, posVec); 
//		}
		
		

		//		System.out.printf(", Chi2 = %3.1f, p=%3.1f, ndf = %d, Err=%s\n", 
		//		chiData.getChi2(), cumProb, chiData.getNdf(), cartErr.getJsonErrorString());
		//		System.out.println(ellipErr.getJsonErrorString()); 

		/**
		 * Calculated position was relative to array centre, so now need to add the array
		 * centre to the estimated position and then reference this to the ref GPS position. 
		 */
		LatLong pos = geometry.getReferenceGPS().addDistanceMeters(new PamVector(posVec).add(centre));
		
		
		

		//		TargetMotionResult tmResult = new TargetMotionResult(geometry.getTimeMilliseconds(), null, pos, 0, 0);
		
//		System.out.println("New group localisation: height: " + pos.getHeight() + " ref height: " + 
//		geometry.getReferenceGPS().getHeight() +  " posVec: " + posVec[2]); 
		
		GroupLocResult glr = new GroupLocResult(pos, 0, chiData.getChi2());
		glr.setError(ellipErr);
		glr.setPerpendicularDistance(0.);
		glr.setModel(this);
		int nToads = countUsableTOADS(toadInformation);
		int nDF = nToads-3;
		glr.setnDegreesFreedom(nDF);
		glr.setDim(3);
		glr.setBeamLatLong(geometry.getReferenceGPS());
		glr.setBeamTime(groupDataUnit.getTimeMilliseconds());
		glr.setAic(chiData.getChi2()-6);
		ChiSquaredDistributionImpl chi2Dist = new ChiSquaredDistributionImpl(nDF);
		double p;
		try {
			p = chi2Dist.cumulativeProbability(results[iBest].getValue());
			glr.setProbability(p);
		} catch (MathException e) {
			glr.setProbability(null);
		}
		GroupLocalisation groupLocalisation = new GroupLocalisation(groupDataUnit, glr);
	
		return groupLocalisation;
	}

	public void printDiagnostics() {
//		Debug.out.println("Simplex localiser best position index count");
//		for (int i = 0; i < bestResults.length; i++) {
//			Debug.out.printf("Index %d n %d\n", i, bestResults[i]);
//			bestResults[i] = 0;
//		}
//		errHist.printSummary("Distance from hyperbolic result", Debug.out);
		errHist.clear();
//		bestTryHist.printSummary("Best start position index", Debug.out);
		bestTryHist.clear();
//		Debug.out.println(cpuGrid.getSummary("Grid Search"));
//		Debug.out.println(cpuHyperbolic.getSummary("hyperbolic localiser"));
//		Debug.out.println(cpuSimplex.getSummary("Simplex"));
	}

	/**
	 * Work out what's going on with the 'best' result. This may just be the lowest Chi2, or 
	 * may have to be come kind of clustering thing to find > 1 result with a similar location. 
	 * @param results
	 * @param resultChiData
	 * @return index of best result, or -1 if there isn't one. 
	 */
	private int getBestResult(RealPointValuePair[] results, Chi2Data[] resultChiData) {
		int iBest = -1;
		double bestChi = Double.MAX_VALUE;
		PamVector lastVec = null;
		int bigJumps = 0;
		for (int i = 0; i < results.length; i++) {
			if (results[i] == null) {
				continue;
			}
			if (resultChiData[i] == null) {
				continue;
			}
			PamVector thisVec = new PamVector(results[i].getPoint());
			if (lastVec != null) {
				double r = (thisVec.sub(lastVec)).norm(nDimensions);
				if (r > 2.) {
					bigJumps++;
					//					Debug.out.printf("Simplex Loc result %d jumped %3.1fm from %s to %s\n", i, r, lastVec.toString(), thisVec.toString());
				}
//				if (r < 2.) {
//					continue; // not interested in very small jumps. 
//				}
			}
			lastVec = thisVec;
			Chi2Data chiDat = resultChiData[i];
			double ch = chiDat.getChi2() / chiDat.getDegreesOfFreedom();
			if (ch < bestChi) {
				bestChi = ch;
				iBest = i;
			}
		}
		//		if (bigJumps > 0) {
		//			for (int i = 0; i < results.length; i++) {
		//				if (results[i] == null) continue;
		//				PamVector v = new PamVector(results[i].getPoint());
		//				Debug.out.printf("%d:, Chi2 %6.1f/%d, pos %s\n", i, resultChiData[i].getChi2(), 
		//						resultChiData[i].getDegreesOfFreedom(), v.toString());
		//			}
		//			int a = 6/3;
		//		}
		return iBest;
	}

	private class Chi2Function implements MultivariateRealFunction {

		private SnapshotGeometry geometry;
		private TOADInformation toadInformation;

		/**
		 * @param geometry
		 */
		public Chi2Function(SnapshotGeometry geometry, TOADInformation toadInformation) {
			super();
			this.geometry = geometry;
			this.toadInformation = toadInformation;
		}

		@Override
		public double value(double[] position) {
//			lastPosition = position;
			Chi2Data chiData = calcChi2(geometry, toadInformation, position);
			if (chiData == null) {
				return Double.NaN;
			}
			return chiData.getChi2();
		}

	}

	private class LogLikelihoodFunction implements MultivariateRealFunction {	
		private SnapshotGeometry geometry;
		private TOADInformation toadInformation;

		/**
		 * @param geometry
		 */
		public LogLikelihoodFunction(SnapshotGeometry geometry, TOADInformation toadInformation) {
			super();
			this.geometry = geometry;
			this.toadInformation = toadInformation;
		}

		@Override
		public double value(double[] position) {
//			lastPosition = position;
			LogLikelihoodData llData = calcLogLikelihood(geometry, toadInformation, position);
			if (llData == null) {
				return Double.NaN;
			}
			return llData.getTestValue();
		}
	}
//
//	private double[] getStartLocation() {
//
//
//		double[] start = {0., 0., 0.};
//
//		return start;
//	}

	@Override
	public String getName() {
		return String.format("Simplex %dD", nDimensions);
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		switch (arrayType) {
		case ArrayManager.ARRAY_TYPE_NONE:
		case ArrayManager.ARRAY_TYPE_POINT:
			return null;
		case ArrayManager.ARRAY_TYPE_LINE:
		case ArrayManager.ARRAY_TYPE_PLANE:
			return new CrossedBearingSQLAddon(2);
		case ArrayManager.ARRAY_TYPE_VOLUME:
			return new CrossedBearingSQLAddon(1);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Time of arival difference " + getName() + " search";
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getLocContents()
	 */
	@Override
	public LocContents getLocContents() {
		// TODO Auto-generated method stub
		return null;
	}



	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getSettingsPane()
	 */
	@Override
	public LocaliserPane<Serializable> getAlgorithmSettingsPane() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#hasParams()
	 */
	@Override
	public boolean hasParams() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#notifyModelProgress(double)
	 */
	@Override
	public void notifyModelProgress(double progress) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see group3dlocaliser.algorithm.LocaliserAlgorithm3D#canArrayShape(int)
	 */
	@Override
	public boolean canArrayShape(int arrayShape) {
		switch (nDimensions) {
		case 2:
			return arrayShape >= ArrayManager.ARRAY_TYPE_PLANE;
		case 3:
			return arrayShape >= ArrayManager.ARRAY_TYPE_VOLUME;	
		}
		return arrayShape >= ArrayManager.ARRAY_TYPE_PLANE;
	}
	
	
	/**
	 * Wrapper for converting a MultivariateRealFunction to a MinimisationFunction. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	class SimpleMinimisation implements MinimisationFunction {
		
		private MultivariateRealFunction fucntion;
		private int nDim;
		private double[] start;
		private double[] firstStep;

		public SimpleMinimisation(MultivariateRealFunction fucntion, int nDim, double[] start, double[] firstStep) {
			this.fucntion=fucntion; 
			this.nDim= nDim; 
			this.start= start; 
			this.firstStep= firstStep; 
		}

		@Override
		public double value(double[] location) {
			try {
				return fucntion.value(location);
			} catch (FunctionEvaluationException | IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return Double.NaN;

			}
		}

		@Override
		public int getDim() {
			return nDim;
		}

		@Override
		public double[] getStart() {
			return start;
		}

		@Override
		public double[] getFirstStep() {
			return firstStep;
		}
		
	}


}
