package whistleClassifier.training;

import pamMaths.STD;
import Jama.Matrix;

/**
 * Get the mean and eventually some other parameters from a list of
 * matrixes. 
 * @author Doug Gillespie
 *
 */
public class MatrixMean {

	private Matrix[] matrixList;
	
	private Matrix mean;

	public MatrixMean(Matrix[] matrixList) {
		super();
		this.matrixList = matrixList;
	}
	
	public MatrixMean(Matrix[] matrixList, int firstInd, int nMatrixes) {
		super();
		this.matrixList = new Matrix[nMatrixes];
		for (int i = 0; i < nMatrixes; i++) {
			this.matrixList[i] = matrixList[firstInd+i];
		}
	}
	
	public Matrix getMean() {
		if (mean != null) {
			return mean;
		}
		double r = 1./matrixList.length;
		mean = matrixList[0].times(r);
		for (int i = 1; i < matrixList.length; i++) {
			mean.plusEquals(matrixList[i].times(r));
		}
		return mean;
	}
	
	public Matrix getSTD() {
		Matrix s = matrixList[0].copy();
		STD std = new STD();
		double vals[] = new double[matrixList.length]; 
		int nR, nC;
		nR = matrixList[0].getRowDimension();
		nC = matrixList[0].getColumnDimension();
		for (int iR = 0; iR < nR; iR++) {
			for (int iC = 0; iC < nC; iC++) {
				for (int i = 0; i < matrixList.length; i++) {
					vals[i] = matrixList[i].get(iR,iC);
				}
				s.set(iR, iC, std.getSTD(vals));
			}
		}
			
		return s;
	}
	
}
