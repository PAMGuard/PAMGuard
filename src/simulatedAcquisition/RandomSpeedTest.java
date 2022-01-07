package simulatedAcquisition;

import java.util.Random;

public class RandomSpeedTest {

	public RandomSpeedTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Random r = new Random();
		int n = 100000000;
		long t1 = System.nanoTime();
		double a;
		for (int i = 0; i < n; i++) {
			a = r.nextGaussian();
		}
		long t2 = System.nanoTime();
		double npc = (double) (t2-t1) / (double) n;
		System.out.printf("Time per call = %3.3fns = %3.1e calls per second", npc, 1.e9/npc);

	}

}
