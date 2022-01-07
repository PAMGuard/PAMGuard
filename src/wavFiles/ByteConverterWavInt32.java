package wavFiles;

public class ByteConverterWavInt32 extends ByteConverter {

	double scale = Math.pow(2,31);
	
	@Override
	public int bytesToDouble(byte[] byteData, double[][] doubleData, int numBytes) {
		int nChan = doubleData.length;
		int nSamples = numBytes/nChan/4;
		int iChan = 0, iSamp = 0;
		int iPos;
		int blockSize = nChan*4;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		try {
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*4;
			for (iSamp = 0; iSamp < nSamples; iSamp++) {
				doubleData[iChan][iSamp] = ((byteData[iPos+3]&0xFF)<<24 |
						(byteData[iPos+2]&0xFF)<<16 |
						(byteData[iPos+1]&0xFF)<<8 |
						(byteData[iPos]&0xFF))/scale;
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
		int blockSize = nChan*4;
		int intVal;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*4;
			for (iSamp = 0; iSamp < numSamples; iSamp++) {
				intVal = (int)(doubleData[iChan][iSamp]*scale);
				byteData[iPos] = (byte) (intVal & 0xFF);
				byteData[iPos+1] = (byte) ((intVal & 0xFF00) >> 8);
				byteData[iPos+2] = (byte) ((intVal & 0xFF0000) >> 16);
				byteData[iPos+3] = (byte) (intVal>>24);
				iPos += blockSize;
			}
		}
		return nChan*numSamples*4;		
	}

}
