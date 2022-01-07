package pamMaths;

public class Mean {

	public static double getMean(double[] data) {
		int n = data.length;
		double a = 0;
		for (int i = 0; i < n; i++) {
			a += data[i];
		}
		return a/n;
	}
}
