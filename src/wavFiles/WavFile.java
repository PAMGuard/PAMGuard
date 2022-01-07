package wavFiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;

//import org.kc7bfi.jflac.util.ByteData;
import org.jflac.util.ByteData;

import clickDetector.WindowsFile;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavFile {

//	protected File file;

	protected String fileName;

	protected String fileMode;

	protected WindowsFile windowsFile;

	protected WavHeader wavHeader;

	protected ByteConverter byteConverter;

	protected AudioFormat currentFormat;

	/**
	 * Open a wav file for reading. 
	 * <p>
	 * @param fileName file name with full path
	 * @param fileMode mode = "r" for read or "w" for write
	 */
	protected WavFile(String fileName, String fileMode) {
		this.fileName = new String(fileName);
		this.fileMode = fileMode;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}


	/**
	 * Read the wav header - assumes that file ref is already at the start of the file.  
	 * @return the Wav header. 
	 */
	public WavHeader readWavHeader() {
		wavHeader = new WavHeader();
		if (wavHeader.readHeader(windowsFile)) {
			return wavHeader;
		}
		else {
			return null;
		}
	}

	/**
	 * @return the wavHeader
	 */
	public WavHeader getWavHeader() {
		return wavHeader;
	}

	public boolean positionAtData() {
		if (wavHeader == null) {
			readWavHeader();
		}
		if (wavHeader.isHeaderOk()) {
			try {
				windowsFile.seek(wavHeader.getDataStart());
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Set the file pointer at a specific sample.
	 * Only for files open in read mode !
	 * @param sampleNumber sample number
	 * @return true if OK, false if it failed. 
	 */
	public boolean setPosition(long sampleNumber) {
		if (wavHeader == null) {
			readWavHeader();
		}
		if (wavHeader.isHeaderOk()) {
			try {
				windowsFile.seek(wavHeader.getDataStart() + sampleNumber * wavHeader.getBlockAlign());
			} catch (IOException e) {
				System.err.println("Error positioning wav file " + fileName + ": " + e.getMessage());
				return false;
			}
			return true;
		}
		return false;

	}

	protected boolean reportError(String warningText) {
		JOptionPane.showMessageDialog(null, warningText, "Wav file", JOptionPane.ERROR_MESSAGE);
		return false;
	}

	public void close() {
		if (windowsFile != null) {
			try {
				windowsFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			windowsFile = null;
		}
	}


//	/**
//	 * @return the file
//	 */
//	public File getFile() {
//		return file;
//	}


	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @return the currentFormat
	 */
	public AudioFormat getCurrentFormat() {
		return currentFormat;
	}



	
	//	private ByteConverter getByteConverter(AudioFormat format) {
	//		if (currentBCOK(format)) {
	//			return byteConverter;
	//		}
	//		byteConverter = ByteConverter.createByteConverter(format);
	//
	//		currentFormat = format;
	//
	//		return byteConverter;
	//	}
	//
	//	private boolean currentBCOK(AudioFormat format) {
	//		if (currentFormat == null || byteConverter == null) {
	//			return false;
	//		}
	//		if (currentFormat.getEncoding() != format.getEncoding()) {
	//			return false;
	//		}
	//		if (currentFormat.isBigEndian() != format.isBigEndian()) {
	//			return false;
	//		}
	//		if (currentFormat.getSampleSizeInBits() != format.getSampleSizeInBits()) {
	//			return false;
	//		}
	//		return true;
	//	}

}
