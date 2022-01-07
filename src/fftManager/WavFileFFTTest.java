package fftManager;

public class WavFileFFTTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String wavFile = args[0];
		int fftLength = 1024;
		int fftHop = 512;
		int chans = 0;
		if (args.length >= 2) {
			fftLength = Integer.valueOf(args[1]);
			fftHop = fftLength/2;
		}
		if (args.length >= 3) {
			fftHop = Integer.valueOf(args[2]);
		}
		if (args.length >= 4) {
			chans = Integer.valueOf(args[3]);
		}
		
//		FileFFTTest fftTest = new FileFFTTest(wavFile, fftLength, fftHop,chans);
//		fftTest.run();

	}
	

}
