package classifier;

import Jama.Matrix;
import Jama.QRDecomposition;
import Jama.SingularValueDecomposition;

public class MahalanobisClassifier extends Classifier {

	private MahalanobisParams mhParams = new MahalanobisParams();
	
	private Matrix logLikelyhood;
	
	@Override
	public String getClassifierName() {
		return "Mahalanobis Distances Classifier";
	}

	@Override
	public ClassifierParams getClassifierParams() {
		return mhParams;
	}

	@Override
	public Matrix getLogLikelihoodsM() {
		return logLikelyhood;
	}

	/**
	 * This need to be rewritten with real probabilities fromt he chi2 distribution
	 * Maniana !
	 */
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
			for (int iG = 1; iG < nGroups; iG++) {
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
		// TODO Auto-generated method stub
		return ProbabilityType.ABSOLUTE;
	}

	@Override
	public int[] runClassification(Matrix data) {
		if (mhParams.groupMeans == null || mhParams.rInverse == null) {
			return null;
		}
		
		int[] uniqueGroups = mhParams.groupMeans.getUniqueGroups();
		int nGroups = uniqueGroups.length;
		Matrix means;
		int nData = data.getRowDimension();
		int nParam = data.getColumnDimension();
		Matrix A;
		logLikelyhood = new Matrix(nData, nGroups);
		double sumA;
		double d;
		for (int iG = 0; iG < nGroups; iG++) {
			means = mhParams.groupMeans.getGroupMeans(uniqueGroups[iG], nData);
			A = data.minus(means).times(mhParams.rInverse[iG]);
			for (int i = 0; i < nData; i++) {
				sumA = 0;
				for (int a = 0; a < nParam; a++) {
					sumA += Math.pow(A.get(i, a), 2);
				}
				d = -sumA;
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
		Matrix trueProbabilities = getProbabilitiesM();
		for (int i = 0; i < nData; i++) {
			total = maxVal = trueProbabilities.get(i, 0);
			maxInd = 0;
			for (int iG = 1; iG < nGroups; iG++) {
				total += (d=trueProbabilities.get(i, iG));
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
	public boolean setClassifierParams(ClassifierParams classifierParams) {
		if (classifierParams.getClassifierClass() != this.getClass()) {
			return false;
		}
		mhParams = (MahalanobisParams) classifierParams;
		return (mhParams.groupMeans != null && mhParams.logDetSigma != null && mhParams.rInverse != null); 
	}

	@Override
	public String trainClassification(Matrix matrix, int[] truth) {

		int n = truth.length;
		int d = matrix.getColumnDimension();
		mhParams.groupMeans = new GroupMeans(matrix, truth);
		
		int[] uniqueGroups = mhParams.groupMeans.getUniqueGroups();
		int nGroups = uniqueGroups.length;
		int[] groupSizes = mhParams.groupMeans.getGroupSize();
		mhParams.rInverse = new Matrix[nGroups];
		mhParams.logDetSigma = new double[nGroups];
		
		for (int i = 0; i < nGroups; i++) {
			if (groupSizes[i] < 2) {
				return ("Each group in the training data must have at least two entries.");
			}
		}
		
		Matrix gm; // means for one group
		Matrix g; //one group
		Matrix r, s;
		QRDecomposition qr;
		SingularValueDecomposition svd;
		double maxS;
		
		for (int iG = 0; iG < nGroups; iG++) {
			gm = mhParams.groupMeans.getGroupMeans(iG, groupSizes[iG]);
			g = oneTrainingSet(matrix, iG, truth).minus(gm);
			qr = new QRDecomposition(g);
			r = qr.getR(); // sometimes crashes here. 
			r.timesEquals(Math.sqrt(1./(groupSizes[iG]-1.)));
			svd = new SingularValueDecomposition(r);
			s = svd.getS();
			
			// check s is OK
			maxS = s.get(0, 0);
			for (int i = 1; i < s.getRowDimension(); i++) {
				maxS = Math.max(maxS, s.get(i, i));
			}
			double eps = Math.ulp(maxS);
			eps *= Math.max(groupSizes[iG], d);
			for (int i = 0; i < s.getRowDimension(); i++) {
				if (s.get(i, i) <= eps) {
					return("MahalanobisClassifier.trainClassification: " +
							"The covariance matrix of each group in he training data must be positive definite.");
				}
			}
			double v = 0;
			for (int i = 1; i < s.getRowDimension(); i++) {
				v += 2 * Math.log(s.get(i, i));
			}
			mhParams.logDetSigma[iG] = v;
			mhParams.rInverse[iG] = r.inverse();
		}
		
		return null;
	}
	
	/**
	 * Makea matrix of data containing only one group. 
	 * @param allGroups
	 * @param iGroup
	 * @param truth
	 * @return
	 */
	private Matrix oneTrainingSet(Matrix allGroups, int iGroup, int[] truth) {
		// get the group id, not the index
		int g = mhParams.groupMeans.getUniqueGroups()[iGroup];
		int n = mhParams.groupMeans.getGroupSize()[iGroup];
		int nRow = allGroups.getRowDimension();
		int nCol = allGroups.getColumnDimension();
		Matrix m = new Matrix(n, nCol);
		int rowOut = 0;
		for (int iR = 0; iR < nRow; iR++) {
			if (truth[iR] != g) {
				continue;
			}
			for (int iC = 0; iC < nCol; iC++) {
				m.set(rowOut, iC, allGroups.get(iR, iC));
			}
			rowOut++;
		}
		return m;
	}

}
