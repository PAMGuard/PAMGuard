package wavFiles;

public class ByteConverterWavInt16 extends ByteConverter {

	@Override
	public int bytesToDouble(byte[] byteData, double[][] doubleData, int numBytes) {
		int nChan = doubleData.length;
		int nSamples = numBytes/nChan/2;
		int iChan = 0, iSamp = 0;
		int iPos;
		int blockSize = nChan*2;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		try {
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*2;
			for (iSamp = 0; iSamp < nSamples; iSamp++) {
				doubleData[iChan][iSamp] = (short)((byteData[iPos+1]&0xFF)<<8 |
						(byteData[iPos]&0xFF))/32768.;
				iPos += blockSize;
			}
		}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return nChan*nSamples;
	}

	@Override
	public int doubleToBytes(double[][] doubleData, byte[] byteData, int numSamples) {
		int nChan = doubleData.length;
		int iChan = 0, iSamp = 0;
		int iPos;
		int blockSize = nChan*2;
		short shortVal;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*2;
			for (iSamp = 0; iSamp < numSamples; iSamp++) {
				shortVal = (short)(doubleData[iChan][iSamp]*32768.);
				byteData[iPos] = (byte) (shortVal & 0xFF);
				byteData[iPos+1] = (byte) (shortVal>>8);
				iPos += blockSize;
			}
		}
		return nChan*numSamples*2;		
	}

}
