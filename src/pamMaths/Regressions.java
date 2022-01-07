package pamMaths;

import Jama.Matrix;

/**
 * Class for performing various regressions. Relies heavily on the Matrixes and other utilities in the 
 * JAMA package. Developers must download JAMA and install the jar file into their JAVA path.
 * <p>
 * see  http://math.nist.gov/javanumerics/jama/
 * @author Douglas Gillespie
 *
 */
public class Regressions {

	public static double[] polyFit(double[] x, double[] y, int order) {
		switch(order) {
		case 0:
			return meanFit(y);
		case 1:
			return anyOrderFit(x, y, 1);
		case 2:
			return squareFit(x,y);
		}
		return anyOrderFit(x, y, order);
	}
	
	/**
	 * Return the mean of a set of points as a one element array for compatibility with 
	 * other, higher order fits. 
	 * @param y array ofvalues
	 * @return the mean value as a one element array
	 */
	public static double[] meanFit(double[] y) {
		double[] m = {getMean(y)};
		return m;
	}
	
	/**
	 * Get the mean of a set of values
	 * @param y array ofvalues
	 * @return the mean value
	 */
	public static double getMean(double[] y) {
		double tot = 0;
		for (int i = 0; i < y.length; i++) {
			tot += y[i];
		}
		return tot / y.length;
	}

	/**
	 * Fit a linear regression line to a set of points 
	 * @param x array of x coordinates
	 * @param y array of y coordinates
	 * @return the two coefficients for the fit 
	 */
	public static double[] linFit(double[] x, double y[]) {
		double meanX = getMean(x);
		double meanY = getMean(y);
		double t=0, b=0;
		for (int i = 0; i < x.length; i++) {
			t += (x[i]-meanX) * (y[i]-meanY);
			b += Math.pow(x[i]-meanX, 2);
		}
		double b1 = t/b;
		double b0 = meanY - b1*meanX;
		double[] ans = {b0, b1};
		return ans;
	}
	
	/**
	 * Fit a second order polynomial to a set of points 
	 * @param x array of x coordinates
	 * @param y array of y coordinates
	 * @return the three coefficients for the fit or null if a fit is not possible. 
	 */
	public static double[] squareFit(double[] x, double y[]) {
		double dt0=0, dt1=0, dt2=0;
		double t0=0, t1=0, t2=0, t3=0, t4=0;
		t0 = x.length;
		for (int i = 0; i < x.length; i++) {
			t1 += x[i];
			t2 += x[i] * x[i];
			t3 += Math.pow(x[i], 3);
			t4 += Math.pow(x[i], 4);
			dt0 += y[i];
			dt1 += y[i] * x[i];
			dt2 += y[i] * x[i] * x[i];
		}
		double[][] leftData = {{t0,t1,t2},{t1,t2,t3},{t2,t3,t4}};
		Matrix m = new Matrix(leftData);
		double[] rightData = {dt0,dt1,dt2};
		Matrix r = new Matrix(rightData,3);
		
		if (m.det() == 0) {
			// singlular matrix
			return null;
		}
		Matrix ans = m.solve(r);
		return ans.getColumnPackedCopy();
	}
	
	/**
	 * Fit a polynomial of any order
	 * @param x set of x coordinates
	 * @param y set of y coordinates
	 * @param order order of the fit
	 * @return array of fit coefficients. 
	 */
	private static double[] anyOrderFit(double[] x, double[] y, int order) {
		double[] dts = new double[order+1];
		double[] ts = new double[order*2+1];
		ts[0] = x.length;
		for (int i = 0; i < x.length; i++) {
			ts[1] += x[i];
			for (int pt = 2; pt < ts.length; pt++) {
				ts[pt] += Math.pow(x[i], pt);
			}
			dts[0] += y[i];
			dts[1] += y[i] * x[i];
			for (int pt = 2; pt < dts.length; pt++) {
				dts[pt] += y[i] * Math.pow(x[i], pt);
			}
		}
		Matrix m = new Matrix(order+1, order+1);
		Matrix r = new Matrix( order+1,1);
		for (int iR = 0; iR <= order; iR++) {
			for (int iC = 0; iC <= order; iC++) {
				m.set(iR, iC, ts[iC+iR]);
			}
			r.set(iR, 0, dts[iR]);
		}
		if (m.det() == 0) {
			// singlular matrix
			return null;
		}
		Matrix ans = m.solve(r);
		return ans.getColumnPackedCopy();
	}
	
	/**
	 * Use the parameters of the fit to calculate a value
	 * using the fitParams polynomial. 
	 * @param fitParams parameters of the fit
	 * @param x x value
	 * @return y value = fitParams[0] + fitParams[1]*x + fitParams[2]*x^2, etc...
	 */
	public static double value(double[] fitParams, double x) {
		double y = fitParams[0];
		for (int i = 1; i < fitParams.length; i++) {
			y += fitParams[i] * Math.pow(x, i);
		}
		return y;
	}
	
}
