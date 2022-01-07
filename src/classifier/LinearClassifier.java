package classifier;


import Jama.Matrix;
import Jama.QRDecomposition;
import Jama.SingularValueDecomposition;

/**
 * Train and run linear classification 
 * 
 * @author Doug Gillespie
 *
 */
public class LinearClassifier extends Classifier {


    private Matrix rMatrix, rInverse;
//    
    private double logDetSigma = 0;
    
    private GroupMeans groupMeans;
    
    private Matrix logLikelyhood;
    

	@Override
	public String trainClassification(Matrix matrix, int[] truth) {
		/*
		 * first need to get the means of each row
		 */
		rMatrix = null;
		rInverse = null;
		groupMeans = null;
		
		int n = truth.length;
		int d = matrix.getColumnDimension();
		/*
		 * Check for Infinite or NaN values.
		 */
		int nNaN = 0;
		int nInf = 0;
		int nMinusInf = 0;
		double[][] a = matrix.getArray();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < d; j++) {
				if (Double.isNaN(a[i][j])) {
					nNaN++;
				}
				else if (Double.isInfinite(a[i][j])) {
					nInf++;
				}
			}
		}
		if (nNaN > 0 || nInf > 0) {
			System.out.println(String.format("Training Matrix contains %d NaN and %d infinite values"
					, nNaN, nInf));
			return null;
		}
		
		
		groupMeans = new GroupMeans(matrix, truth);
		
		int[] uniqueGroups = groupMeans.getUniqueGroups();
		int nGroups = uniqueGroups.length;
		
		if (n <= nGroups) {
			return "LinearFragmentClassifier.TrainClassification Training data " +
			"must have more observations than the number of groups";
		}
		Matrix means = groupMeans.getGroupMeans(truth);
		Matrix minusMeans = matrix.minus(means);
		QRDecomposition qr = new QRDecomposition(minusMeans);
		
		double scale = Math.sqrt(n - nGroups);
		rMatrix = qr.getR().times(1./scale);
//		System.out.println(String.format(
//				"Start SingularValueDecomposition at %s on Matrix with %d rows and %d columns", 
//				PamCalendar.formatDateTime(System.currentTimeMillis()), 
//						rMatrix.getRowDimension(), rMatrix.getColumnDimension()));
		SingularValueDecomposition svd = new SingularValueDecomposition(rMatrix);
//		System.out.println("End SingularValueDecomposition at " + 
//				PamCalendar.formatDateTime(System.currentTimeMillis()));
		Matrix s = svd.getS();
		
		double maxS = s.get(0, 0);
		for (int i = 0; i < s.getRowDimension(); i++) {
			maxS = Math.max(maxS, s.get(i, i));
		}
		double eps = Math.ulp(maxS);
		eps *= Math.max(n, d);
		for (int i = 0; i < s.getRowDimension(); i++) {
			if (s.get(i, i) <= eps) {
				return("LinearFragmentClassifier.trainClassification: " +
						"The pooled covariance matrix of TRAINING must be positive definite.");
			}
		}

		logDetSigma = 0;
		for (int i = 0; i < d; i++) {
			logDetSigma += Math.log(s.get(i,i));
		}
		logDetSigma *= 2;
	
		rInverse = rMatrix.inverse();
		
		return null;
	}


	@Override
	public int[] runClassification(Matrix data) {
		if (groupMeans == null || rInverse == null) {
			return null;
		}
		
		int[] uniqueGroups = groupMeans.getUniqueGroups();
		int nGroups = uniqueGroups.length;
		Matrix means;
		int nData = data.getRowDimension();
		int nParam = data.getColumnDimension();
		Matrix A;
		logLikelyhood = new Matrix(nData, nGroups);
		double sumA;
		double d;
		for (int iG = 0; iG < nGroups; iG++) {
			means = groupMeans.getGroupMeans(uniqueGroups[iG], nData);
			A = data.minus(means).times(rInverse);
			for (int i = 0; i < nData; i++) {
				sumA = 0;
				for (int a = 0; a < nParam; a++) {
					sumA += Math.pow(A.get(i, a), 2);
				}
				d = -0.5*(sumA + logDetSigma);
				logLikelyhood.set(i, iG, d);
			}
		}
		
		/*
		 * Now take the biggest D value as the correct class. 
		 */
		int[] species = new int[nData];
		double maxVal;
		int maxInd;
		double total;
		Matrix normProbabilities = getProbabilitiesM();
		for (int i = 0; i < nData; i++) {
			total = maxVal = normProbabilities.get(i, 0);
			maxInd = 0;
			for (int iG = 1; iG < nGroups; iG++) {
				total += (d=normProbabilities.get(i, iG));
				if (d > maxVal) {
					maxVal = d;
					maxInd = iG;
				}
			}
			if (maxVal < minimumProbability) {
				species[i] = -1;
			}
			else {
				species[i] = uniqueGroups[maxInd];
			}
		}
		
		return species;
	}


	@Override
	public Matrix getLogLikelihoodsM() {
		return logLikelyhood;
	}


	@Override
	public Matrix getProbabilitiesM() {
		/*
		 * simply the inverse log of the log likelihoods matrix. 
		 */
		if (logLikelyhood == null) {
			return null;
		}
		int nData = logLikelyhood.getRowDimension();
		int nGroups = logLikelyhood.getColumnDimension();
		Matrix p = new Matrix(nData, nGroups);
		double rowSum, rowMax;
		/**
		 * Need to go through three times. Once to sub of the maximum. 
		 * (though probably not necessary since logDetSigma should have taken care of this)
		 * once to sum all the probabilities and finally once to normalise. 
		 */
		double[] temp = new double[nGroups]; // quicker than in and out of matrix all the time
		
		for (int iD = 0; iD < nData; iD++) {
			rowMax = temp[0] = logLikelyhood.get(iD, 0);
			for (int iG = 0; iG < nGroups; iG++) {
				rowMax = Math.max(rowMax, temp[iG] = logLikelyhood.get(iD, iG));
			}
			rowSum = 0;
			for (int iG = 0; iG < nGroups; iG++) {
				temp[iG] = Math.exp(temp[iG] - rowMax);
				rowSum += temp[iG];
			}
			for (int iG = 0; iG < nGroups; iG++) {
				p.set(iD, iG, temp[iG] / rowSum);
			}
		
		}
		
		return p;
	}


	@Override
	public ProbabilityType getProbabilityType() {
		return ProbabilityType.NORMALISED;
	}


	@Override
	public ClassifierParams getClassifierParams() {

		LinearClassifierParams params = new LinearClassifierParams();
		
		params.rMatrix = rMatrix;
		if (groupMeans != null) {
			params.groupMeans = groupMeans.clone();
		}
		params.logDetSigma = logDetSigma;
		
		return params;
	}


	@Override
	public boolean setClassifierParams(ClassifierParams classifierParams) {
		if (classifierParams.getClassifierClass() != this.getClass()) {
			return false;
		}
		LinearClassifierParams params = (LinearClassifierParams) classifierParams;
		if (params.groupMeans == null || params.rMatrix == null || params.logDetSigma == 0) {
			return false;
		}
		groupMeans = params.groupMeans.clone();
		rMatrix = params.rMatrix;
		rInverse = rMatrix.inverse();
		logDetSigma = params.logDetSigma;
		return true;
	}


	@Override
	public String getClassifierName() {
		return "Linear Discriminant Analysis";
	}

	

}
