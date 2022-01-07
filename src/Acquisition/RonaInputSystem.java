package Acquisition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.filechooser.FileFilter;

//import org.kc7bfi.jflac.FLACDecoder;
//import org.kc7bfi.jflac.PCMProcessor;
//import org.kc7bfi.jflac.metadata.StreamInfo;
//import org.kc7bfi.jflac.util.ByteData;
import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;

import wavFiles.ByteConverter;
import Acquisition.pamAudio.PamAudioSystem;
import PamDetection.RawDataUnit;
import PamUtils.FileParts;
import PamUtils.PamAudioFileFilter;
import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import PamUtils.PamUtils;

/**
 * Bespoke system for handling data from the Rona hydrophone array which 
 * consists of sets of seven files, each with different ends in th ename. 
 * @author Doug
 *
 */
public class RonaInputSystem extends FolderInputSystem {

	private static final int RONACHANNELS = 0x21; // channels 5 and 0
	
	private static final int NCHANNELS = PamUtils.getNumChannels(RONACHANNELS); 

	private AudioInputStream[] audioStreams = new AudioInputStream[NCHANNELS];

	private FlacThread[] flacThreads = new FlacThread[NCHANNELS];

	private volatile RawDataUnit[] readyDataUnits = new RawDataUnit[NCHANNELS];

	private volatile int readyUnits = 0;

	private int readyMask = PamUtils.makeChannelMap(NCHANNELS);

	private int runningChannels;
	
	private long lastFileTime = 0;
	
	public static final String systemType = "Rona File Folders";

	public RonaInputSystem(AcquisitionControl acquisitionControl) {
		super(acquisitionControl);
		this.acquisitionControl = acquisitionControl;
		setAudioFileFilter(new RonafileFilter());
	}

	@Override
	public String getSystemType() {
		return systemType;
	}
	@Override
	public String getUnitName() {
//		return "Rona File Folder Analysis";
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getSystemType();
	}

	@Override
	public PamFileFilter getFolderFileFilter() {
		return new RonafileFilter();
	}

	@Override
	protected int fudgeNumChannels(int nChannels) {
		return NCHANNELS;
	}

	class RonafileFilter extends PamAudioFileFilter {

		@Override
		public boolean accept(File f) {
			if (super.accept(f) == false) {
				return false;
			}
			if (f.isDirectory()) {
				return true;
			}
			// now check that the last character before the final . is a 1 !
			String name = f.getName();
			int lastDot = name.lastIndexOf('.');
			if (lastDot < 0) return false;
			char ch = name.charAt(lastDot-1); 
			if ('1' != ch) {
				return false;
			}
			// now check all other files in the set exist. 
			for (int i = 1; i < NCHANNELS; i++) {
				File chanFile = findChannelFile(f, i, 2);
				if (chanFile == null) {
					return false;
				}
			}
			return true;
		}
	}


	@Override
	public int getMaxChannels() {
		return NCHANNELS;
	}

	@Override
	public int getChannels() {
		return NCHANNELS;
	}

	@Override
	public int getInputChannelMap(AcquisitionParameters acquisitionParameters) {
		acquisitionParameters.nChannels = NCHANNELS;
		return PamUtils.makeChannelMap(acquisitionParameters.nChannels);
	}

	@Override
	public boolean prepareInputFile() {
		// need to prepare a whole set of audio input streams !
		File currentFile = getCurrentFile();
		if (currentFile == null) return false;
		try {
			for (int i = 0; i < NCHANNELS; i++) {

				if (audioStreams[i] != null) {
					audioStreams[i].close();
				}

				audioStreams[i] = PamAudioSystem.getAudioInputStream(findChannelFile(currentFile, i, 2));

				audioFormat = audioStreams[i].getFormat();

				//			fileLength = currentFile.length();
				fileSamples = audioStreams[i].getFrameLength();
				readFileSamples = 0;

				acquisitionControl.getAcquisitionProcess().setSampleRate(audioFormat.getSampleRate(), true);
				sampleRate = audioFormat.getSampleRate();

				if (i == 0) {
					byteConverter = ByteConverter.createByteConverter(audioFormat);
				}
			}

		} catch (UnsupportedAudioFileException ex) {
			ex.printStackTrace();
			return false;
		} catch (FileNotFoundException ex) {
			System.out.println("Input filename: '" + fileNameCombo + "' not found");
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}		

		return true;
	}

	/**
	 * swap the last digit in a file name for the higher channel 
	 * number - rememebr the channels are one indexed. 
	 * @param baseFile base file
	 * @param index 0 based file index
	 * @return new file with the 1 replaced by another number
	 */
	public File getChannelFile(File baseFile, int index) {
		index = PamUtils.getNthChannel(index, RONACHANNELS);
		String oldBit = "1.flac";
		String newEnd = String.format("%d.flac", index+1);
		String oldName = baseFile.getAbsolutePath();
		String newName = oldName.replace(oldBit, newEnd);
		return new File(newName);
	}

	/**
	 * Search for a nearby file with the same name, but secondds may differ 
	 * by one or two secs - may need to generate complete new file names !
	 * @param baseFile
	 * @param index
	 * @param searchRange
	 * @return
	 */
	public File findChannelFile(File baseFile, int index, int searchRange) {
		File normal = getChannelFile(baseFile, index);
		if (normal.exists()) {
			return normal;
		}
		index = PamUtils.getNthChannel(index, RONACHANNELS);
		/*
		 * 		files are in the format 20051209-185714-02.flac
		 * so get the time and add an offset to make a new file name. 
		 */
		long fileTime = getFileStartTime(baseFile);
		if (fileTime <= 0) {
			return null;
		}

		FileParts fileParts = new FileParts(baseFile);
		for (int i = 1; i <= searchRange; i++) {
			for (int j = 1; j >= -1; j-=2) {
				long t = fileTime + i*j*1000;
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(t);
				c.setTimeZone(PamCalendar.defaultTimeZone);

				DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
				df.setTimeZone(PamCalendar.defaultTimeZone);
				Date d = c.getTime();
				String newName = fileParts.getFolderName() + FileParts.getFileSeparator() + df.format(d) + String.format("-%02d.flac", index+1);
				File newFile = new File(newName);
				if (newFile.exists()) {
					return newFile;
				}
			}
		}
		return null;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		this.acquisitionControl = daqControl;
		if (prepareInputFile() == false) {
			return false;
		}
		this.newDataUnits = acquisitionControl.getDaqProcess().getNewDataQueue();
		if (this.newDataUnits == null) return false;

		File baseFile = getCurrentFile();

		PamCalendar.setSoundFile(true);
		PamCalendar.setSoundFileTimeInMillis(0);
		long fileTime = getFileStartTime(baseFile);
		if (fileTime > 0) {
			PamCalendar.setSessionStartTime(fileTime);
		}
		else {
			PamCalendar.setSessionStartTime(System.currentTimeMillis());
		}


		for (int i = 0; i < NCHANNELS; i++) {
			File chanFile = findChannelFile(baseFile, i, 2);
			if (chanFile == null) {
				System.err.println(String.format("Unable to find matching file for channel %d for %s", i, baseFile));
				return false;
			}
			flacThreads[i] = new FlacThread(chanFile, i);
		}


		return true;
	}

	@Override
	synchronized public boolean startSystem(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		this.dontStop = true;
		readyUnits = 0;

		currentFileStart = System.currentTimeMillis();
		fileStartTime = currentFileStart;
		
		for (int i = 0; i < NCHANNELS; i++) {
			Thread thread = new Thread(flacThreads[i]);
			thread.start();
		}

		setStreamStatus(STREAM_RUNNING);

		return true;
	}

	 synchronized public void setChannelRunning(int channel, boolean running) {
		int was = runningChannels;
		if (running) {
			runningChannels |= 1<<channel;
		}
		else {
			runningChannels &= ~(1<<channel);
		}
		
		System.out.println(String.format("Set channel %d run status to %s overall status was %d, is now %d (%s)", channel, new Boolean(running).toString(),
				was, runningChannels, PamUtils.getChannelList(runningChannels)));

		if (runningChannels == 0) {
			System.out.println("No more running channels, so stream has ended");
			setStreamStatus(STREAM_ENDED);
		}
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		dontStop = false;

		for (int i = 0; i < 20; i++) {
			if (runningChannels == 0) {
				break;
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ex){
				ex.printStackTrace();
			}
		}
		calculateETA();
		systemHasStopped(runningChannels > 0);
	}
	
	@Override
	protected void calculateETA() {
		long now = System.currentTimeMillis();
		lastFileTime = now - currentFileStart;
		eta = lastFileTime * (allFiles.size()-(currentFile));
		eta += now;
	}

	synchronized private void addRawDataUnit(RawDataUnit rawDataUnit, int channel) {
		readyDataUnits[channel] = rawDataUnit;
		readyUnits |= 1<<channel;
		if (readyUnits != readyMask) {
			return;
		}
		/*
		 * If it gets here, then all units are in place
		 * so can send them all off for processing
		 */
		long firstDataLen = readyDataUnits[0].getSampleDuration();
		for (int i = 0; i < NCHANNELS; i++) {
			if (firstDataLen != readyDataUnits[i].getSampleDuration()) {
				System.out.println(String.format("Different data lengths bewtween chan 0 and %d - %d and %d samples",
						i, firstDataLen, readyDataUnits[i].getSampleDuration()));
				double[] newRaw = Arrays.copyOf(readyDataUnits[i].getRawData(), (int) firstDataLen);
				readyDataUnits[i].setRawData(newRaw);
				/**
				 * Will also need to stop the run at this point since it's evident that one file is longer than others 
				 * and will now go into a wait state while it tries to match it's data with other channels. 
				 */
				dontStop = false;
			}

			newDataUnits.addNewData(readyDataUnits[i]);
		}
		while (newDataUnits.getQueueSize() > NCHANNELS) {
			if (dontStop == false) break;
			try {
				Thread.sleep(2);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (int i = 0; i < NCHANNELS; i++) {
			readyDataUnits[i] = null;
		}
		readyUnits = 0;
	}

	synchronized private boolean waitingDataUnit(int channel) {
		return ((readyUnits & 1<<channel) != 0);
	}

	private class FlacPCM implements PCMProcessor {

		private ByteConverter byteConverter;
		private FileInputStream fileStream;
		private int frameSize;
		private long totalSamples;
		private long lastProgressUpdate;
		private long lastProgressTime;
		private int channelOffset;
		private int theseFileSamples;

		public FlacPCM(FileInputStream fileStream, int channelOffset) {
			this.fileStream = fileStream;
			this.channelOffset = channelOffset;
		}

		@Override
		public void processPCM(ByteData byteData) {
			if (dontStop == false) {
				try {
					fileStream.close(); // will make the flac reader bomb out !
				}
				catch (IOException e) {

				}
				return;
			}
			//				System.out.println("processPCM(ByteData arg0)");
			int newSamples = byteData.getLen() / frameSize;
			double[][] doubleData = new double[1][newSamples];
			byteConverter.bytesToDouble(byteData.getData(), doubleData, byteData.getLen());

			long ms = acquisitionControl.getAcquisitionProcess().absSamplesToMilliseconds(totalSamples);
			RawDataUnit newDataUnit = null;
			for (int ichan = 0; ichan < 1; ichan++) {

				newDataUnit = new RawDataUnit(ms, 1 << (ichan+channelOffset), totalSamples, newSamples);
				newDataUnit.setRawData(doubleData[ichan]);

				addRawDataUnit(newDataUnit, ichan + channelOffset);
				//				newDataUnits.add(newDataUnit);

				// GetOutputDataBlock().addPamData(pamDataUnit);
			}

			//				System.out.println(String.format("new samps %d at %s", newSamples, PamCalendar.formatTime(ms, 3)));

			totalSamples += newSamples;
			if (channelOffset == 0) {
				readFileSamples += newSamples;
			}
			theseFileSamples += newSamples;

			if (channelOffset == 0) {
			long blockMillis = (int) ((newDataUnit.getStartSample() * 1000) / sampleRate);
			//				newDataUnit.timeMilliseconds = blockMillis;
			PamCalendar.setSoundFileTimeInMillis(blockMillis);
			if (fileSamples > 0 && totalSamples - lastProgressUpdate >= getSampleRate()*2) {
				int progress = (int) (1000. * (float)theseFileSamples / (float) fileSamples);
				fileProgress.setValue(progress);
				sayEta();
				long now = System.currentTimeMillis();
				if (lastProgressTime > 0 && totalSamples > lastProgressUpdate) {
					double speed = (double) (totalSamples - lastProgressUpdate) / 
							getSampleRate() / ((now-lastProgressTime)/1000.);
					speedLabel.setText(String.format(" (%3.1f X RT)", speed));
				}
				lastProgressTime = now;
				lastProgressUpdate = totalSamples;
			}
			}

			/**
			 * Sit and wait until all threads have put there data units into the 
			 * ready array, then they will release at the same time and this thread
			 * can continue round. 
			 */
			for (int ichan = 0; ichan < 1; ichan++) {
				while (waitingDataUnit(ichan+channelOffset)) {
					if (dontStop == false) break;
					try {
						Thread.sleep(2);
					} catch (Exception ex) {
						ex.printStackTrace();
					}					
				}
			}

		}

		@Override
		public void processStreamInfo(StreamInfo streamInfo) {
			frameSize = audioFormat.getChannels() * audioFormat.getSampleSizeInBits() / 8;
			byteConverter = ByteConverter.createByteConverter(audioFormat.getSampleSizeInBits()/8, false, Encoding.PCM_SIGNED);
			fileSamples = streamInfo.getTotalSamples();
		}

	}

	private class FlacThread implements Runnable {

		private int channel;
		private File inputFile;
		private FileInputStream fileStream;
		private FLACDecoder flacDecoder;
		public FlacThread(File inputFile, int channel) {
			super();
			this.inputFile = inputFile;
			this.channel = channel;

			try {
				fileStream = new FileInputStream(inputFile);
				System.out.println("Open flac file " + inputFile.getName());
			} catch (FileNotFoundException e) {
				//				e.printStackTrace();
				return;
			}
			flacDecoder = new FLACDecoder(fileStream);
			flacDecoder.addPCMProcessor(new FlacPCM(fileStream, channel));

		}

		@Override
		public void run() {

			setChannelRunning(channel, true);

			try {
				//				while (!flacDecoder.isEOF()) {
				flacDecoder.decode();
//				System.out.println("Flac decode call complete channel " + channel);
				//					flacDecoder.decode(new SeekPoint(decodePoint, 0, 0), new SeekPoint(decodePoint+blockSamples, 0, 0));
				//					decodePoint += blockSamples;
				//					flacFrame = flacDecoder.readNextFrame();
				//					byteData = flacDecoder.decodeFrame(flacFrame, byteData);
				//				}
			} catch (IOException e) {
				// don't print this since it happens naturally when we press the stop button. 
				//								e.printStackTrace();
			}
			try {
				fileStream.close();
			} catch (IOException e) {
			}


			try {
				fileStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			setChannelRunning(channel, false);
		}


	}

}
