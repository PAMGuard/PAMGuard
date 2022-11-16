package soundtrap.sud;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudDataInputStream;

import Acquisition.AcquisitionControl;
import Acquisition.sud.SUDNotificationHandler;
import Acquisition.sud.SUDNotificationManager;
import PamController.PamController;
import PamUtils.PamCalendar;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickDetector.ChannelGroupDetector;
import soundtrap.STAcquisitionControl;
import soundtrap.STClickControl;
import wavFiles.ByteConverter;

/**
 * Class to handle BCL and DWV data from a SUD file during sud file processing.
 * SUD files are the compressed data files from soundtraps. The latest PAMGuard
 * version contains a audioinputstream for SUD files, making it possible to
 * process them withoug having to first inflate them to wav files. During
 * 'normal mode' processing, this class will subscribe to packets from the SUD
 * files, unpack what would normally be BCL and DWV data and generate
 * appropriate binary output files.
 * 
 * @author dg50
 *
 */
public class SudFileDWVHandler implements SUDNotificationHandler {

	private STClickControl stClickControl;
	private SudAudioInputStream sudAudioInputStream;

	private int txtCount = 0;

	private List<Chunk> clickChunks = new LinkedList();

	private List<BCLDetectionChunk> bclChunks = new LinkedList();

	int bclDetCount, bclNoiseCount, dwvCount, processedChunks;
	private long effortStart, fileStartMicros;
	private double sampleRate;
	private ByteConverter byteConverter;
	private ClickDetector clickDetector;
	private ChannelGroupDetector channelGroupDetector;

	public SudFileDWVHandler(STClickControl stClickControl) {
		this.stClickControl = stClickControl;
		clickDetector = stClickControl.getClickDetector();
	}

	@Override
	public void newSudInputStream(SudAudioInputStream sudAudioInputStream) {
		this.sudAudioInputStream = sudAudioInputStream;
		interpretNewFile(null, sudAudioInputStream);
		bclDetCount = bclNoiseCount = dwvCount = processedChunks = 0;
		bclChunks.clear();
		clickChunks.clear();
		byteConverter = ByteConverter.createByteConverter(sudAudioInputStream.getFormat());
	}

	@Override
	public void interpretNewFile(String newFile, SudAudioInputStream sudAudioInputStream) {
		// this is the wav sample rate, not the detector sample rate, so don't use it
//		sampleRate = sudAudioInputStream.getFormat().getFrameRate();
		// this is the right one
		sampleRate = sudAudioInputStream.getSudMap().clickDetSampleRate;
		fileStartMicros = sudAudioInputStream.getSudMap().getFirstChunkTimeMicros();
		stClickControl.findRawDataBlock().setChannelMap(1);
		stClickControl.findRawDataBlock().setSampleRate((float) sampleRate, true);
		stClickControl.getSTAcquisition().acquisitionParameters.sampleRate = (float) sampleRate;
		stClickControl.getSTAcquisition().acquisitionParameters.voltsPeak2Peak = STAcquisitionControl.SOUNDTRAPVP2P;
		stClickControl.getSTAcquisition().getAcquisitionProcess().setSampleRate((float) sampleRate, true);
//		System.out.printf("Open input stream fs = %3.1f\n", sampleRate);
		
	}

	@Override
	public void sudStreamClosed() {
//		System.out.printf("SUD input stream closed, %d DWV, %d bcl Detectins and %d BCL Noise, %d chunks processed\n",
//				dwvCount, bclDetCount, bclNoiseCount, processedChunks);
	}

	@Override
	public void progress(double arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void chunkProcessed(int chunkID, Chunk sudChunk) {
		if (sudAudioInputStream == null) {
			return;
		}
		String chunkName = "Unknown";
		int chunkSize = sudChunk.buffer.length;
		if (sudAudioInputStream.getChunkIDString(chunkID).equals("wav")) {
			
			long millis = sudChunk.getChunkHeader().getMillisTime();
			stClickControl.updateDisplayScrollers(millis);
			
			if (sudAudioInputStream.isChunkIDWav(chunkID)) {
//				chunkName = "RECORDINGS";
				// System.out.printf("Chunk ID %d, size %d, type %s\n", chunkID, chunkSize,
				// chunkName);
				// System.out.println("ID: " + chunkID + " This is raw data from detected
				// CLICKS: " + sudAudioInputStream.getChunkIDString(chunkID));
			} else {
				chunkName = "CLICKS";
				processClickChunk(chunkID, sudChunk);
			}
		}

		if (sudAudioInputStream.getChunkIDString(chunkID).equals("csv")) {
//			System.out.println("CSV data the bytes convert directly to comma delimted data");
		}
		if (sudAudioInputStream.getChunkIDString(chunkID).equals("txt")) {
			processTextChunk(chunkID, sudChunk);
			txtCount++;
		}

	}

	/**
	 * Pairwise byte swap, i.e. change endianness of int16's. Won't work for
	 * anything else.
	 * 
	 * @param data
	 */
	public void swapEndian(byte[] data) {
		for (int i = 0; i < data.length; i += 2) {
			byte b = data[i];
			data[i] = data[i + 1];
			data[i + 1] = b;
		}
	}

	private void processTextChunk(int chunkID, Chunk sudChunk) {
		SudDataInputStream dis = new SudDataInputStream(new ByteArrayInputStream(sudChunk.getBuffer()));

		try {
			while (dis.available() > 0) {
				long rtime = (dis.readInt()) & 0xffffffffL;
				long mticks = (dis.readInt()) & 0xffffffffL;
				int n = dis.readUnsignedShort();

				long javaMillis = (long) rtime * 1000 + mticks / 1000;
				long javaMicros = (long) rtime * 1000000 + mticks;

				byte[] b = new byte[(n % 2 == 1) ? n + 1 : n];
				dis.read(b);

				swapEndian(b);

				String s = new String(b, "UTF-8");
				String[] bits = s.split(",");
				if (bits[0].equals("E")) {
					// start or end record.
					boolean isStart = bits[1].equals("1");
					if (isStart) {
						effortStart = javaMicros;
					}
					dwvEffort(javaMillis, isStart);
				} else if (bits[0].equals("D")) {
					boolean isDet = bits[1].equals("1");
					if (isDet) {
						bclDetCount++;
						String bclText = String.format("%9d,%6d,%s has %d bits", rtime, mticks, s, bits.length);
						BCLDetectionChunk bclChunk = new BCLDetectionChunk(javaMillis, javaMicros, isDet, bclText,
								sudChunk);
						bclChunks.add(bclChunk);
					} else {
						bclNoiseCount++;
					}
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		convertChunks();

	}

	/**
	 * Convert bcl and dwv data to click detections.
	 */
	private void convertChunks() {
		int nProc = 0;
		while (bclChunks.size() > 0 && clickChunks.size() > 0) {
			BCLDetectionChunk bclChunk = bclChunks.remove(0);
			Chunk dwvChunk = clickChunks.remove(0);
			makeClick(bclChunk, dwvChunk);
			nProc++;
			processedChunks++;
		}
	}

	private void makeClick(BCLDetectionChunk bclChunk, Chunk dwvChunk) {
		long elapsedSamples = (long) ((bclChunk.getJavaMicros() - fileStartMicros) * (sampleRate / 1e6));
		long millis = bclChunk.getJavaMillis();
		byte[] rawData = dwvChunk.getBuffer();
		int nBytes = rawData.length;
		int nSamples = nBytes / Short.BYTES;
		double[][] wavData = new double[1][nSamples];
		byteConverter.bytesToDouble(rawData, wavData, nBytes);

//		channelGroupDetector = clickDetector.getChannelGroupDetector(0);
		ClickDetection click = new ClickDetection(1, elapsedSamples, nSamples, clickDetector, channelGroupDetector, 1);
		click.setTimeMilliseconds(millis);
		click.setWaveData(wavData);

//		if (groupDetector != null) {
//			groupDetector.
//		}
		if (clickDetector.completeClick(click)) {
			clickDetector.getClickDataBlock().addPamData(click);
		}
	}

	private void dwvEffort(long javaMillis, boolean isStart) {
		System.out.printf("DWV Effort %s at %s\n", isStart ? "Start" : "End", PamCalendar.formatDBDateTime(javaMillis));
	}

	private void processClickChunk(int chunkID, Chunk sudChunk) {
		clickChunks.add(sudChunk);
		dwvCount++;
	}

	public void pamStart() {
		subscribeSUD();
	}

	public void pamStop() {
		// TODO Auto-generated method stub

	}

	public boolean subscribeSUD() {
		if (stClickControl.isViewer()) {
			return false;
		}
		AcquisitionControl daq = (AcquisitionControl) PamController.getInstance()
				.findControlledUnit(AcquisitionControl.unitType);
		if (daq == null) {
			return false;
		}
		SUDNotificationManager sudManager = daq.getSUDNotificationManager();
		if (sudManager == null) {
			return false;
		}
		sudManager.addNotificationHandler(this);
		return true;
	}

}
