package SoundRecorder;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import warnings.PamWarning;
import warnings.WarningSystem;
import wavFiles.ByteConverter;

/**
 * Implementation of RecorderStorage specific to audio files.
 * 
 * @author Doug Gillespie
 *@see SoundRecorder.RecorderStorage
 */
public class PamAudioFileStorage  implements RecorderStorage {

	private static final long WAV_HEAD_SIZE = 44;

	private String fileName = null;
	
	private RecorderControl recorderControl;
	
	private AudioFormat audioFormat;
	
	private AudioInputStream audioInputStream;
	
	private File audioFile;
		
	private PipedInputStream pipedInputStream;
	
	private PipedOutputStream pipedOutputStream;
	
	private PamWarning audioWriteWarning;
	
	private byte[] byteBuffer;
	
	AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
	
	long fileBytes;
	
	long fileStartMillis;
	
	long lastDataMillis;
	
	private Thread writeThread;
	
	long totalFrames;
	
	private ByteConverter byteConverter;
	
	public PamAudioFileStorage(RecorderControl recorderControl) {
		this.recorderControl = recorderControl;
		audioWriteWarning = new PamWarning(recorderControl.getUnitName(), "Audio Write problem", 2);
	}
	
	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	synchronized public boolean addData(long dataTimeMillis, double[][] newData) {
		if (newData == null || newData.length != audioFormat.getChannels()) return false;
		if (fileName == null) {
			return false;
		}
		int nFrames = newData[0].length;
		totalFrames += nFrames;
	
		int nChannels = newData.length;
		//byte[] byteData = new byte[nFrames * audioFormat.getFrameSize()];
		if (byteBuffer == null || byteBuffer.length != nFrames * audioFormat.getFrameSize()) {
			byteBuffer = new byte[nFrames * audioFormat.getFrameSize()];
		}
		byteConverter.doubleToBytes(newData, byteBuffer, nFrames);
		try {
			// this is the line throwing during Kyle field trip June 15.
			pipedOutputStream.write(byteBuffer);
		}
		catch (IOException Ex) {
//			Ex.printStackTrace();
			audioWriteWarning.setWarningMessage("Write Error to audio file: " + Ex.getLocalizedMessage());
			audioWriteWarning.setWarnignLevel(2);
			audioWriteWarning.setRequestRestart(true);
			WarningSystem.getWarningSystem().addWarning(audioWriteWarning);
			return false;
		}
		if (audioWriteWarning.getWarnignLevel() > 0) {
			audioWriteWarning.setWarnignLevel(0);
			WarningSystem.getWarningSystem().removeWarning(audioWriteWarning);
		}
		lastDataMillis = dataTimeMillis;
		fileBytes += byteBuffer.length;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see SoundRecorder.RecorderStorage#closeStorage()
	 */
	@Override
	synchronized public boolean closeStorage() {
		/*
		 * The exact order things happen in here is quite important. No more samples will be added
		 * but it is likely that there are stil samples in the output pipe, so fluch the pipe to make sure they all 
		 * get written to the rile. Then wait for the pipe to be empty, then close the storage, then wait for the 
		 * writing thread to have exited before finally closing the output stream.  
		 * 
		 * Data actually written to file in a separate thread with 
			 bytesWritten = AudioSystem.write(audioInputStream,	fileType, audioFile);
			 
			My calls write to pipedOutputStream.write(byteBuffer) which is coupled to 
			pipedInputStream. 
			the pipedInputStream then links to audioInputStream
		audioInputStream = new AudioInputStream(pipedInputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
		  Data actually written to file in a separate thread with 
			 bytesWritten = AudioSystem.write(audioInputStream,	fileType, audioFile);
			 
			 i.e. data flow is pipedOutputStream->pipedInputStream->audioInputStream->audioFile;
			 // need to try to flush everything through first three streams before closing audio file. 
		 */
		if (fileName == null) return false;
		fileName = null;
		try {
//			System.out.println("Available bytes in input stream: " + pipedInputStream.available() + " before flush");

			long t = System.nanoTime();
			pipedOutputStream.flush();
//			System.out.printf("\n");
			
			// then wait for the input stream to be empty - meaning that all data have been written to the file. 
//			System.out.println("Available bytes in input stream: " + pipedInputStream.available() + " after flush");

			int waitCount = 0;
			int a, b;
			while((a=pipedInputStream.available()) + (b=audioInputStream.available()) > 0) {
//				System.out.printf("Available bytes in pipedinputStream stream: %d, in audioInputStream: %d after %d millis\n",
//						a, b, waitCount);
				Thread.sleep(1);
				waitCount++;
				if (waitCount > 2000) break;
			}
//			long t2 = System.nanoTime();
//			System.out.printf("Call pipedOutputStream.close() %d nanos later\n", t2-t);
			pipedOutputStream.close();
//			t = System.nanoTime();
//			System.out.printf("Call audioInputStream.close() %d nanos later\n", t-t2);
			audioInputStream.close();

//			// then wait for the flag saying that the write thread has exited to be 0
//			t2 = System.nanoTime();
//			System.out.printf("Wait for write thread death. %d nanos later \n", t2-t);
			pipedInputStream.close();
			int threadWait = 0;
			while (writeThread.isAlive()) {
//				System.out.println("Wait for write thread to die " + threadWait);
				Thread.sleep(1);
				threadWait++;
				if (threadWait > 2000) break;
			}

//			t = System.nanoTime();
//			System.out.printf("Call pipedInputStream.close() %d nanos or %d millis later\n", t-t2, threadWait);
			pipedInputStream.close();
			
			audioInputStream = null;
			
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		recorderControl.recorderProcess.storageClosed();
		return true;
	}
	
	float sampleRate;
	AudioFileFormat.Type audioFileType;
	int nChannels, bitDepth;

	/**
	 * Write data to an audio file. 
	 * <p>
	 * Writing audio data is relatively straight forward. The actual writing is
	 * done in a separate thread. That thread needs an InputStream to read data from.
	 * This is one end of a pair of PipedInput and PipedOutput Streams. This thread
	 * writes data into the other end of the pipe as it arrives. 
	 */
	@Override
	synchronized public boolean openStorage(AudioFileFormat.Type fileType, long recordingStart, 
			float sampleRate, int nChannels, int bitDepth) {
		
		closeStorage();
		
		this.sampleRate = sampleRate;
		this.audioFileType = fileType;
		this.nChannels = nChannels;
		this.bitDepth = bitDepth;
		
		boolean isBigendian = (fileType != Type.WAVE);
		
		audioFormat = new AudioFormat(sampleRate, bitDepth, nChannels, true, isBigendian);
		
		byteConverter = ByteConverter.createByteConverter(bitDepth/8, isBigendian, Encoding.PCM_SIGNED);
		
		totalFrames = 0;
		
		lastDataMillis = fileStartMillis = recordingStart;
		
		fileBytes = 0;

		this.fileType = fileType;
		String fileExtension = "." + fileType.getExtension();
		File outFolder = FileFunctions.getStorageFileFolder(recorderControl.recorderSettings.outputFolder,
				recordingStart, recorderControl.recorderSettings.datedSubFolders, true);
		if (outFolder == null) {
			outFolder = new File(recorderControl.recorderSettings.outputFolder);
		}
		fileName = PamCalendar.createFileNameMillis(recordingStart, outFolder.getAbsolutePath(), 
				recorderControl.recorderSettings.fileInitials+"_", fileExtension);
//		System.out.println(fileName);
		
		audioFile = new File(fileName);
		
		byteBuffer = new byte[(int) sampleRate * audioFormat.getFrameSize()];
		try {
			pipedInputStream = new PipedInputStream();
			pipedOutputStream = new PipedOutputStream(pipedInputStream);
		}
		catch (IOException Ex) {
			Ex.printStackTrace();
			return false;
		}
		audioInputStream = new AudioInputStream(pipedInputStream, audioFormat, AudioSystem.NOT_SPECIFIED);
		
		writeThread = new Thread(new WriteThread());
		writeThread.start();
//		writeData();
		
		return true;
	}
	
	@Override
	synchronized public boolean reOpenStorage(long recordingStart) {
		/*
		 * Make sure it's not currently writing a thread.
		 * 
		 */
		closeStorage();
		return openStorage(audioFileType, recordingStart, sampleRate, nChannels, bitDepth);
	}

	/**
	 * WriteThread makes a single call to AudioSystem.write. This 
	 * function blocks and only returns when the audioInputStream is 
	 * closed. 
	 * @author Doug Gillespie
	 *
	 */
	class WriteThread implements Runnable {
	
		@Override
		public void run() {
			writeData();
		}
	}
	
	/**
	 * Called within the write thread, this does not return
	 * until the pipes get closed. 
	 */
	private void writeData() {
//		System.out.println("Enter write Data");
		long bytesWritten = 0;
		try
		{
			 bytesWritten = AudioSystem.write(audioInputStream,	fileType, audioFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
//		System.out.printf("Leave write Data thread after %d bytes\n", bytesWritten);
	}

	@Override
	public long getFileSizeBytes() {
		if (fileName == null) return -1;
		return fileBytes + WAV_HEAD_SIZE;
	}

	@Override
	public long getFileFrames() {
		return fileBytes/audioFormat.getFrameSize();
	}

	@Override
	public long getFileMilliSeconds() {
		if (fileName == null) return -1;
		return (lastDataMillis - fileStartMillis);
	}

	@Override
	public long getFileStartTime() {
		return fileStartMillis;
	}

	@Override
	public long getMaxFileSizeBytes() {
		return Integer.MAX_VALUE;
	}

}
