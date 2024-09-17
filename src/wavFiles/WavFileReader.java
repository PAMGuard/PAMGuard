package wavFiles;

import java.io.File;
import java.io.IOException;

import clickDetector.WindowsFile;

public class WavFileReader extends WavFile {

	private byte[] tempByteArray;
	
	public WavFileReader(String fileName) {
		super(fileName, "r");
		openForReading();
	}

	private boolean openForReading() {
		File file = new File(fileName);
		if (!file.exists()) {
			return reportError(String.format("file %s does not exist on the system", fileName));
		}
		try {
			windowsFile = new WindowsFile(file, fileMode);
		} catch (IOException e) {
			e.printStackTrace();
			return reportError(String.format("Unable to open file %s", fileName));
		}

		readWavHeader();
		currentFormat = wavHeader.getAudioFormat();
		byteConverter = ByteConverter.createByteConverter(currentFormat);
		return (wavHeader != null && wavHeader.isHeaderOk());
	}

	/**
	 * Read a number of bytes from the wav file. 
	 * 
	 * @param byteArray byte array preallocated to desired length
	 * @return number of bytes actually read. 
	 */
	private int readData(byte[] byteArray) {
		int bytesRead = 0;
		try {
			bytesRead = windowsFile.read(byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytesRead;
	}

	/**
	 * Read data into a preallocated double array. 
	 * The array should be 2 D, with the first dim being
	 * the number of channels, the second the number of samples to read. 
	 * @param doubleArray double array to receive data
	 * @return number of samples read (should be doubleArray[0].length if no EOF) 
	 */
	public int readData(double[][] doubleData) {
//		int bytes = wavHeader.getBitsPerSample()/8*wavHeader.getNChannels()*doubleArray[0].length;
		int numBytes = currentFormat.getFrameSize() * doubleData[0].length;
		if (tempByteArray == null || tempByteArray.length != numBytes) {
			tempByteArray = new byte[numBytes];
		}
		int bytesRead = readData(tempByteArray);
		int samplesRead = bytesRead/wavHeader.getBlockAlign();
		byteConverter.bytesToDouble(tempByteArray, doubleData, numBytes);
//		switch(wavHeader.getBitsPerSample()) {
//		case 16:
//			unpackInt16(doubleArray, tempByteArray, samplesRead);
//			break;
//		case 24:
//			unpackInt24(doubleArray, tempByteArray, samplesRead);
//			break;
//		case 32:
//			break;
//		}
		return samplesRead;
	}
	/**
	 * Read data into a preallocated double array. 
	 * The array should be 2 D, with the first dim being
	 * the number of channels, the second the number of samples to read. 
	 * @param doubleArray double array to receive data
	 * @return number of samples read (should be doubleArray[0].length if no EOF) 
	 */
	public int readData(short[][] shortArray) {
		int bytes = wavHeader.getBitsPerSample()/8*wavHeader.getNChannels()*shortArray[0].length;
		if (tempByteArray == null || tempByteArray.length != bytes) {
			tempByteArray = new byte[bytes];
		}
		int bytesRead = readData(tempByteArray);
		int samplesRead = bytesRead/wavHeader.getBlockAlign();
		switch(wavHeader.getBitsPerSample()) {
		case 16:
			unpackInt16(shortArray, tempByteArray, samplesRead);
			break;
		case 24:
			//			unpackInt24(shortArray, tempByteArray, samplesRead);
			break;
		case 32:
			break;
		}
		return samplesRead;
	}
//	/**
//	 * Unpack an array of 16 bit integer data in little endian format. 
//	 * @param doubleArray
//	 * @param tempByteArray
//	 * @param samplesRead
//	 */
//	private void unpackInt16(double[][] doubleArray, byte[] tempByteArray,
//			int samplesRead) {
//		int nChan = wavHeader.getNChannels();
//		int bytePointer = 0;
//		double max = 1<<15;
//		// the cast to short forces the first bit to become the sign again prior to
//		// conversion to double
//		for (int iSamp = 0; iSamp < samplesRead; iSamp++) {
//			for (int iChan = 0; iChan < nChan; iChan++) {
//				doubleArray[iChan][iSamp] = (short)((tempByteArray[bytePointer+1]&0xFF)<<8 | 
//						(tempByteArray[bytePointer]&0xFF))/max;
//				bytePointer += 2;
//			}
//		}
//	}
	/**
	 * Unpack an array of 16 bit integer data in little endian format. 
	 * @param doubleArray
	 * @param tempByteArray
	 * @param samplesRead
	 */
	private void unpackInt16(short[][] shortArray, byte[] tempByteArray,
			int samplesRead) {
		int nChan = wavHeader.getNChannels();
		int bytePointer = 0;
		// the cast to short forces the first bit to become the sign again prior to
		// conversion to double
		for (int iSamp = 0; iSamp < samplesRead; iSamp++) {
			for (int iChan = 0; iChan < nChan; iChan++) {
				shortArray[iChan][iSamp] = (short)((tempByteArray[bytePointer+1]&0xFF)<<8 | 
						(tempByteArray[bytePointer]&0xFF));
				bytePointer += 2;
			}
		}
	}
//	/**
//	 * Unpack a byte array of data in little endian format. 
//	 * @param doubleArray
//	 * @param tempByteArray
//	 * @param samplesRead
//	 */
//	private void unpackInt24(double[][] doubleArray, byte[] tempByteArray,
//			int samplesRead) {
//		int nChan = wavHeader.getNChannels();
//		int bytePointer = 0;
//		double max = 1<<31;
//		// need to boost data up to int32 so that the sign bit gets into the right place. 
//		for (int iChan = 0; iChan < nChan; iChan++) {
//			for (int iSamp = 0; iSamp < samplesRead; iSamp++) {
//				doubleArray[iChan][iSamp] = ((tempByteArray[bytePointer+2]&0xFF)<<24 |
//						(tempByteArray[bytePointer+1]&0xFF)<<16 + 
//						(tempByteArray[bytePointer]&0xFF)<<8)/max;
//				bytePointer += 3;
//			}
//		}
//	}

}
