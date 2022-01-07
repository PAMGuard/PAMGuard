package wavFiles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class ByteConverterFloat32 extends ByteConverter {

	@Override
	public int bytesToDouble(byte[] byteData, double[][] doubleData,
			int numBytes) {
		int nChan = doubleData.length;
		int nSamples = numBytes/nChan/4;
		int iChan = 0, iSamp = 0;
		int iPos;
		FloatBuffer bb = ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

		for (iSamp = 0; iSamp < nSamples; iSamp++) {
			for (iChan = 0; iChan < nChan; iChan++) {
				doubleData[iChan][iSamp] = bb.get();
			}
		}
		return nChan*nSamples;
	}

	@Override
	public int doubleToBytes(double[][] doubleData, byte[] byteData,
			int numSamples) {
		// TODO Auto-generated method stub
		return 0;
	}

}
