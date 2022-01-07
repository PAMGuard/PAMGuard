package pamMaths;

import java.util.Random;

public class TestHistogram  {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int nP = 10000;
		double mean = 1.2;
		double sig = 1.3;
		
		Random r = new Random();
		
		PamHistogram h1 = new PamHistogram(-10, 10, 100, true);
		
		for (int i = 0; i < nP; i++) {
//			h1.addData(r.nextGaussian() * sig + mean);
		}

		printHistoStats(h1);
	}
	
	public static void printHistoStats(PamHistogram h) {
		System.out.print(" Histogram mean = " + h.getMean());
		System.out.print(" std = " + h.getSTD());
		System.out.print(" skew = " + h.getSkew());
		System.out.print(" kurtosis = " + h.getKurtosis());
		System.out.println();
	}

}
