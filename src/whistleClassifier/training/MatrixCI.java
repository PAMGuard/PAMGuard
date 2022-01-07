package whistleClassifier.training;

import java.util.Arrays;

import Jama.Matrix;

/**
 * Work out confidence intervals for an
 * array of matrixes. 
 * @author Doug
 *
 */
public class MatrixCI {

	private Matrix[] matrixList;
	
	Matrix l95, u95;

	public MatrixCI(Matrix[] matrixList) {
		super();
		this.matrixList = matrixList;
		workCI();
	}

	private void workCI() {
		int n = matrixList.length;
		// will need to do some sort of weighted mean
		// between two points to get the true 2.5% and 97.5% points 
		// of the CI. 
		double ld = (2.5 * n) / 100 - 1;
		int ln = (int) Math.floor(ld); // first bin.
		double lw = 1.;
		if (ln <= 0) { // only use the first bin. 
			ln = 0;
			lw = 1.;
		}
		else {
			lw = 1.-(ld-ln); // use weighting lw from first bin and 1-lw from bin ln+1;
		}
		
		double ud = (97.5 * n) / 100;
		int un = (int) Math.ceil(ud);
		double uw = 1.;
		if (un >= n-1) {
			un = n-1;
			uw = 1.;
		}
		else {
			uw = 1.-(un-ud);
		}
		
		
		l95 = matrixList[0].times(0);
		u95 = matrixList[0].times(0);
		double[] orderData = new double[n];
		int nC = l95.getColumnDimension();
		int nR = l95.getRowDimension();
		for (int iC = 0; iC < nC; iC++) {
			for (int iR = 0; iR < nR; iR++) {
				for (int i = 0; i < n; i++) {
					orderData[i] = matrixList[i].get(iR, iC);
				}
				Arrays.sort(orderData);
				l95.set(iR, iC, orderData[ln]*lw + orderData[ln+1]*(1-lw));
				u95.set(iR, iC, orderData[un]*uw + orderData[un-1]*(1-uw));
			}
		}
		
	}

	/**
	 * @return the lower 95% confidence interval matrix. 
	 */
	public Matrix getL95() {
		return l95;
	}

	/**
	 * @return the upper 95% confidence interval matrix. 
	 */
	public Matrix getU95() {
		return u95;
	}
	
	
}
