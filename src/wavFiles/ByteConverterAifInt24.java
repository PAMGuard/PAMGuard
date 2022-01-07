package wavFiles;

public class ByteConverterAifInt24 extends ByteConverter {

	@Override
	public int bytesToDouble(byte[] byteData, double[][] doubleData, int numBytes) {
		int nChan = doubleData.length;
		int nSamples = numBytes/nChan/3;
		int iChan = 0, iSamp = 0;
		int iPos;
		int blockSize = nChan*3;
		double scale = 1<<31;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		/*
		 * Need to scale up by an extra 8 bytes to force the sign into the last
		 * bit of an integer number
		 */
		try {
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*3;
			for (iSamp = 0; iSamp < nSamples; iSamp++) {
				doubleData[iChan][iSamp] = ((byteData[iPos]&0xFF)<<24 |
						(byteData[iPos+1]&0xFF)<<16 |
						(byteData[iPos+2]&0xFF)<<8)/scale;
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
		int blockSize = nChan*3;
		int intVal;
		double scale = 1<<23;
		// try to minimise the number of times we start a loop by first 
		// looping over channels, then over samples. 
		for (iChan = 0; iChan < nChan; iChan++) {
			iPos = iChan*3;
			for (iSamp = 0; iSamp < numSamples; iSamp++) {
				intVal = (int)(doubleData[iChan][iSamp]*scale);
				byteData[iPos+2] = (byte) (intVal & 0xFF);
				byteData[iPos+1] = (byte) ((intVal & 0xFF00) >> 8);
				byteData[iPos] = (byte) (intVal>>16);
				iPos += blockSize;
			}
		}
		return nChan*numSamples*3;		
	}


}
