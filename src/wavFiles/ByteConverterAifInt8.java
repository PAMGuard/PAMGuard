package wavFiles;

public class ByteConverterAifInt8 extends ByteConverter {

	@Override
	public int bytesToDouble(byte[] byteData, double[][] doubleData, int numBytes) {
		int nChan = doubleData.length;
		int nSamples = numBytes/nChan;
		int iChan = 0, iSamp = 0;
		int iPos;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan;
			for (iSamp = 0; iSamp < nSamples; iSamp++) {
				doubleData[iChan][iSamp] = byteData[iPos]/128.;
				iPos += nChan;
			}
		}
		return nChan*nSamples;
	}

	@Override
	public int doubleToBytes(double[][] doubleData, byte[] byteData, int numSamples) {
		int nChan = doubleData.length;
		int iChan = 0, iSamp = 0;
		int iPos;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan;
			for (iSamp = 0; iSamp < numSamples; iSamp++) {
				byteData[iPos] = (byte)(doubleData[iChan][iSamp]*128.);
				iPos += nChan;
			}
		}
		return nChan*numSamples;		
	}

}
