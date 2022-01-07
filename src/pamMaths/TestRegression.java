package pamMaths;

import java.util.Random;

public class TestRegression {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Random rand = new Random();
		
		double noise = 0.1;

//		double[] eq = {-4, -6, 80, 12, 7};
		double[] eq = {-4, 6.9, 80};

		double[] x = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 40, 60, 90, 110 -6};
		
		double[] y = new double[x.length];
		
		for (int i = 0; i < x.length; i++) {
			y[i] = eq[0] + eq[1]*x[i]; // + eq[2]*x[i]*x[i] + rand.nextGaussian()*1;
			for (int c = 2; c < eq.length; c++) {
				y[i] += eq[c] * Math.pow(x[i], c);
			}
			y[i] += rand.nextGaussian() * noise;
		}
		
		Regressions r = new Regressions();
		
		double[] ans = Regressions.polyFit(x, y, eq.length-1);
		ans = Regressions.squareFit(x, y);
		
		System.out.print("Fit coefficients = " + ans[0]);
		for (int i = 1; i < ans.length; i++) {
			System.out.print(", "  + ans[i]);
		}
		System.out.println();
		
	}

}
