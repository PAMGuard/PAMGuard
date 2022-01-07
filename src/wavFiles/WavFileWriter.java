package wavFiles;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import clickDetector.WindowsFile;
import warnings.RepeatWarning;

public class WavFileWriter extends WavFile {


	private byte[] outputData;
	
	private static RepeatWarning repeatWarning;
	static {
		repeatWarning = new RepeatWarning("WAV File Writer");
	}
	
	public WavFileWriter(String fileName, AudioFormat audioFormat) {
		super(fileName, "rw");
		openForWriting(audioFormat);
	}


	/**
	 * Open a wav file for writing and write a header to the file. 
	 * @param audioFormat
	 */
	private void openForWriting(AudioFormat audioFormat) {
		try {
			windowsFile = new WindowsFile(fileName, "rw");
		} catch (IOException e) {
			windowsFile = null;
			if (repeatWarning.getnPrints() < repeatWarning.getMaxPrints()) {
				System.err.println("Unable to open audio file for writing: " + fileName);
			}
			repeatWarning.showWarning(e, 2);
//			System.err.println(e.getMessage());
		}
		this.currentFormat = audioFormat;
		byteConverter = ByteConverter.createByteConverter(audioFormat);
		wavHeader = new WavHeader(audioFormat);
		wavHeader.writeHeader(windowsFile);
	}
	/**
	 * Writes an array of double values to a WAV file.  This method only writes
	 * single channel data.
	 *
	 * @param format {@link AudioFormat} object describing the desired file
	 * @param doubleArray the array of double values to save
	 * @return boolean indicating success or failure of the write
	 */
	public static boolean writeSingleChannel(String file, AudioFormat format, double[] doubleArray) {

		double[][] data = new double[1][];
		data[0] = doubleArray;
		return write(file, format, data);
//		/* convert the double array to a byte array */
//		byte[] data = new byte[2 * doubleArray.length];
//		for (int i = 0; i < doubleArray.length; i++) {
//			int temp = (short) (doubleArray[i] * Short.MAX_VALUE);                  // DOES THIS ONLY WORK FOR 16BIT?
//			data[2*i + 0] = (byte) temp;
//			data[2*i + 1] = (byte) (temp >> 8);
//		}
//
//		/* try saving the file */
//		try {
//			ByteArrayInputStream bais = new ByteArrayInputStream(data);
//			AudioInputStream ais = new AudioInputStream
//					(bais, format, doubleArray.length);
//			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
//		}
//		catch (Exception e) {
//			System.out.println(e);
//			return false;
//		}
//		return true;
	}

	/* (non-Javadoc)
	 * @see wavFiles.WavFile#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}


	/**
	 * Writes an array of double values to an existing WAV file.  This method writes
	 * single and multi-channel data.
	 *
	 * This method writes the data with any bit depth, depending on how the 
	 * wav file object was created using an AudioFormat. 
	 *
	 * @param doubleArray the array of double values to save.  Note that this is
	 * defined as a 2D array.  If a single-channel 1D vector is passed, it
	 * doesn't seem to be a problem.
	 * @return boolean indicating success or failure of the write
	 */
	public boolean write(double[][] doubleArray) {
		if (windowsFile == null) {
			return false;
		}
		int nSamps = doubleArray[0].length;
		int dataSize = nSamps * currentFormat.getFrameSize();
		
		if (outputData == null || outputData.length != dataSize) {
			outputData = new byte[dataSize];
		}
		byteConverter.doubleToBytes(doubleArray, outputData, nSamps);
		try {
			// move pointer to the end of the file. 
			windowsFile.seekEnd();
			windowsFile.write(outputData);
			wavHeader.setDataSize(dataSize + wavHeader.getDataSize());
			
		} catch (IOException e) {
//			if (repeatWarning.getnPrints() < repeatWarning.getMaxPrints()) {
//			e.printStackTrace();
			repeatWarning.showWarning(e, 2);
			return false;
		}
		return true;
//		/* convert the double array to a byte array */
//		//		outputData = new byte[(2 * doubleArray[0].length) *    // 2 * the number of points in each channel
//		//		                      doubleArray.length];                            // times the number of channels
//		int outDataSize = currentFormat.getFrameSize() * doubleArray[0].length;
//		if (outputData == null || outputData.length != outDataSize) {
//			outputData = new byte[currentFormat.getFrameSize() * doubleArray[0].length];
//		}
//		int c = currentFormat.getChannels();
//		byteConverter.doubleToBytes(doubleArray, outputData, doubleArray[0].length);
//		//		for (int i = 0; i < doubleArray[0].length; i++) {
//		//			for (int j=0; j<doubleArray.length; j++) {
//		//				int temp = (short) (doubleArray[j][i] * Short.MAX_VALUE);                  // DOES THIS ONLY WORK FOR 16BIT?
//		//				outputData[2*(c*i+j) + 0] = (byte) temp;
//		//				outputData[2*(c*i+j) + 1] = (byte) (temp >> 8);
//		//			}
//		//		}
//		if (writtenBytes == 0) {
//			// need to write the header. 
//		}
//		//		windowsFile.
//	
//		/* try saving the file */
//		try {
//			ByteArrayInputStream bais = new ByteArrayInputStream(outputData);
//			AudioInputStream ais = new AudioInputStream
//					(bais, format, doubleArray[0].length);
//			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
//			ais.close();
//		}
//		catch (Exception e) {
//			System.out.println(e);
//			return false;
//		}
//		return true;
	}

	/**
	 * Append data to a file which may or may not be closed already. <br> If the file is still open
	 *  then this simply calls the write function, otherwise it has to reopen the file.
	 *  
	 * @param rawData Raw data to write. Must match audio format file was opened with. 
	 * @return true if write was OK. 
	 */
	public boolean append(double[][] rawData) {
		if (windowsFile != null) {
			return write(rawData);
		}
		// otherwise will have to reopen the file and close it again afterwards. 
		boolean writeOk = false;
		try {
			windowsFile = new WindowsFile(fileName, "rw");
			readWavHeader();
			windowsFile.seekEnd();
			writeOk = write(rawData);
			close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			repeatWarning.showWarning(e, 2);
			return false;
		}
		return writeOk;
	}


	/**
	 * Writes an array of double values to a WAV file.  This method writes
	 * single and multi-channel data.  This method always writes the data as
	 * 16-bit. It shoul dbe used only for one off writing of short files. 
	 *
	 * @param sampleRate The sample rate of the raw acoustic data
	 * @param numChannels The number of channels to save
	 * @param doubleArray the array of double values to save.  Note that this is
	 * defined as a 2D array.  If a single-channel 1D vector is passed, it
	 * doesn't seem to be a problem.
	 * @return boolean indicating success or failure of the write
	 */
	public static boolean write(String fileName, float sampleRate,
			double[][] doubleArray) {

		/* create a new AudioFormat object describing the wav file */
		int nChannels = doubleArray.length;
		AudioFormat af = new AudioFormat(sampleRate,
				16,
				nChannels,
				true,
				false);     // figure out where bit rate is stored
		return write(fileName, af, doubleArray);
	}
	/**
	 * Writes an array of double values to a WAV file.  This method writes
	 * single and multi-channel data.  This method always writes the data as
	 * 16-bit. It shoul dbe used only for one off writing of short files. 
	 * @param af Audio format object
	 * @param doubleArray array of data. Number of chans in data must match audio format. 
	 * @return true if written ok.
	 */
	private static boolean write(String fileName, AudioFormat af, double[][] doubleArray) {
		WavHeader wavHead = new WavHeader(af);
		ByteConverter bc = ByteConverter.createByteConverter(af);
		int nSamps = doubleArray[0].length;
		int dataSize = nSamps * af.getFrameSize();
		byte[] byteData = new byte[nSamps * af.getFrameSize()];
		bc.doubleToBytes(doubleArray, byteData, nSamps);
		wavHead.setDataSize(dataSize);
		WindowsFile wf = null;
		try {
			wf = new WindowsFile(fileName, "rw");
			wavHead.writeHeader(wf);
			wf.write(byteData);
			wf.close();
		} catch (IOException e) {
//			e.printStackTrace();
			repeatWarning.showWarning(e, 2);
			return false;
		}
		
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see wavFiles.WavFile#close()
	 */
	@Override
	public void close() {
		writeHeader(false);
		super.close();
	}


	/**
	 * Rewrite the wav header, optionally returning to the end of the file ready for the 
	 * next write. 
	 * @param returnEnd optionally move the file pointer back to the end of the file. 
	 * @return true if written sucessfully. 
	 */
	boolean writeHeader(boolean returnEnd) {
		if (windowsFile == null) {
			return false;
		}
		try {
			windowsFile.seek(0);
			wavHeader.writeHeader(windowsFile);
			if (returnEnd) {
				windowsFile.seekEnd();
			}
		} catch (IOException e) {
//			e.printStackTrace();
			repeatWarning.showWarning(e, 2);
			return false;
		}
		return true;
	}


	public long getFileFrames() {
		if (wavHeader == null) {
			return 0;
		}
		return wavHeader.getDataSize() / currentFormat.getFrameSize();
	}

	//	/**
	//	 * Append to an audio file. 
	//	 * <br>This implementation is pretty ugly since the current method of writing 
	//	 * seems to only send out a single block, so basically it's rewriting the same 
	//	 * data multiple times. 
	//	 * @param moreData data to add to the file. 
	//	 * @return true if successful, false if wrong format or other problem. 
	//	 */
	//	public boolean append(double[][] doubleArray) {        
	//
	//		int xtraLength = (2 * doubleArray[0].length) *  doubleArray.length;
	//		int ol = outputData.length;
	//		outputData = Arrays.copyOf(outputData, ol+xtraLength);
	//		int c = currentFormat.getChannels();
	//		if (c != doubleArray.length) {
	//			return false;
	//		}
	//		for (int i = 0; i < doubleArray[0].length; i++) {
	//			for (int j=0; j<doubleArray.length; j++) {
	//				int temp = (short) (doubleArray[j][i] * Short.MAX_VALUE);                  // DOES THIS ONLY WORK FOR 16BIT?
	//				outputData[2*(c*i+j) + 0 + ol] = (byte) temp;
	//				outputData[2*(c*i+j) + 1 + ol] = (byte) (temp >> 8);
	//			}
	//		}
	//
	//		int totalLength = outputData.length / currentFormat.getFrameSize();
	//		/* try saving the file */
	//		try {
	//			ByteArrayInputStream bais = new ByteArrayInputStream(outputData);
	//			AudioInputStream ais = new AudioInputStream
	//					(bais, currentFormat, totalLength);
	//			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
	//			ais.close();
	//		}
	//		catch (Exception e) {
	//			System.out.println(e);
	//			return false;
	//		}
	//		return true;
	//	}


}
