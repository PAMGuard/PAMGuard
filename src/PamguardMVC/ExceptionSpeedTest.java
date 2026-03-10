package PamguardMVC;

/**
 * Quick test of how long exception handling really takes.
 * @author dg50
 *
 */
public class ExceptionSpeedTest {

	public ExceptionSpeedTest() {
	}

	public static void main(String[] args) {
		new ExceptionSpeedTest().run();
	}

	private void run() {
		long t0, t1, t2, t3;
		int n = 100000000;
		for (int j = 0; j < 5; j++) {
			t0 = System.nanoTime();
			int a = 0;
			for (int i = 0; i < n; i++) {
				a = addOne(a); 
			}
			t1 = System.nanoTime();
			int b = 0;
			for (int i = 0; i < n; i++) {
				try {
					b = addOne(b); 
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			t2 = System.nanoTime();
			int c = 0;
			for (int i = 0; i < n; i++) {
				try {
					c = addOne(c);
//					c = (int) Math.log(-c);
				}
				catch (Exception e) {
//					e.printStackTrace();
				}
			}
			t3 = System.nanoTime();
			System.out.printf("%8d, %8d, %8d\n", t1-t0, t2-t1, t3-t2);
		}
		
	}

	private int addOne(int a) {
		return a+1;
	}
}
