package wavFiles;

public class ByteConvTest {

	public static void main(String[] args) {
		new ByteConvTest().run();
	}

	private void run() {
		ByteConverter bc = new ByteConverterWavInt16();
		byte[] dataIn = {-1, -1, -1, 0, 0, -1};
		double[][] dataOut = new double[1][dataIn.length/2];
		bc.bytesToDouble(dataIn, dataOut, dataIn.length);
		for (int i = 0, j = 0; i < dataOut[0].length; i++, j+=2) {
			System.out.printf("%d,%d -> %3.1f\n", dataIn[j], dataIn[j+1], dataOut[0][i]*32768);
		}
	}

}
