package dataMap.filemaps;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import Acquisition.pamAudio.PamAudioFileLoader;
import Acquisition.pamAudio.PamAudioFileManager;
import PamUtils.worker.filelist.WavFileType;
import wavFiles.ByteConverter;

/**
 * Reads the samples from a single sound file map point so that they can be summarised
 * into a datagram.
 * <p>
 * This deliberately uses the same {@link PamAudioFileManager} route as
 * {@link Acquisition.pamAudio.WavAudioFile#loadAudioData}, so it works for wav, aif,
 * flac and sud files without any format specific code here. Unlike loadAudioData it
 * doesn't create any PamDataUnits and doesn't remove the DC component (which would
 * distort the waveform envelope we're trying to draw).
 * <p>
 * Samples are only returned for a single channel since a data map summary of every
 * channel would be unreadable.
 * 
 * @author Jamie Macaulay
 *
 */
public class SoundFileSampleReader {

	private FileDataMapPoint mapPoint;

	private int channel;

	private AudioInputStream audioInputStream;

	private AudioFormat audioFormat;

	private ByteConverter byteConverter;

	private byte[] inputBuffer;

	private double[][] doubleData;

	private int frameSize;

	private int nChannels;

	/**
	 * Number of samples left that we're allowed to read. Only meaningful for HARP x.wav
	 * files, where a single file holds many duty cycles, each of which is a separate map
	 * point. Long.MAX_VALUE for everything else.
	 */
	private long samplesRemaining = Long.MAX_VALUE;

	/**
	 * @param mapPoint the sound file map point to read
	 * @param channel channel number to summarise
	 */
	public SoundFileSampleReader(FileDataMapPoint mapPoint, int channel) {
		this.mapPoint = mapPoint;
		this.channel = channel;
	}

	/**
	 * Open the sound file ready for reading.
	 * @return true if the file opened and looks sane.
	 */
	public boolean open() {
		File soundFile = mapPoint.getSoundFile();
		if (soundFile == null || soundFile.exists() == false) {
			return false;
		}
		PamAudioFileLoader audioLoader = PamAudioFileManager.getInstance().getAudioFileLoader(soundFile);
		if (audioLoader == null) {
			return false;
		}
		audioInputStream = audioLoader.getAudioStream(soundFile, null);
		if (audioInputStream == null) {
			return false;
		}
		audioFormat = audioInputStream.getFormat();
		nChannels = audioFormat.getChannels();
		frameSize = audioFormat.getFrameSize();
		if (frameSize < 0) {
			frameSize = nChannels * audioFormat.getSampleSizeInBits() / 8;
		}
		if (frameSize <= 0 || nChannels <= 0) {
			close();
			return false;
		}
		if (channel >= nChannels) {
			channel = 0;
		}
		byteConverter = ByteConverter.createByteConverter(audioFormat);
		if (byteConverter == null) {
			close();
			return false;
		}
		int blockSamples = Math.max((int) audioFormat.getSampleRate() / 10, 1000);
		inputBuffer = new byte[blockSamples * frameSize];
		doubleData = new double[nChannels][blockSamples];

		/*
		 * HARP x.wav files contain many duty cycles, each of which is a separate map
		 * point within the same file, so skip to the right place and note how far we're
		 * allowed to read.
		 */
		if (soundFile instanceof WavFileType) {
			WavFileType wavFile = (WavFileType) soundFile;
			long samplesOffset = wavFile.getSamplesOffset();
			if (wavFile.getMaxSamples() > 0) {
				samplesRemaining = wavFile.getMaxSamples();
			}
			if (samplesOffset > 0) {
				try {
					audioInputStream.skip(samplesOffset * frameSize);
				} catch (IOException e) {
					System.err.println("Unable to skip to the start of " + soundFile.getName() + ": " + e.getMessage());
					close();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return the sample rate of the open file, or 0 if it isn't open.
	 */
	public float getSampleRate() {
		return audioFormat == null ? 0 : audioFormat.getSampleRate();
	}

	/**
	 * Read the next block of samples for the selected channel.
	 * @return the samples read, or null at the end of the file. The returned array is
	 * reused between calls, and {@link #getLastReadCount()} says how much of it is valid.
	 */
	private int lastReadCount;

	public double[] readNextBlock() {
		if (audioInputStream == null || samplesRemaining <= 0) {
			return null;
		}
		int maxBytes = inputBuffer.length;
		if (samplesRemaining < Long.MAX_VALUE) {
			maxBytes = (int) Math.min(maxBytes, samplesRemaining * frameSize);
		}
		int bytesRead;
		try {
			bytesRead = audioInputStream.read(inputBuffer, 0, maxBytes);
		} catch (IOException e) {
			System.err.println("Error reading " + mapPoint.getName() + ": " + e.getMessage());
			return null;
		}
		if (bytesRead <= 0) {
			return null;
		}
		byteConverter.bytesToDouble(inputBuffer, doubleData, bytesRead);
		lastReadCount = bytesRead / frameSize;
		if (samplesRemaining < Long.MAX_VALUE) {
			samplesRemaining -= lastReadCount;
		}
		return doubleData[channel];
	}

	/**
	 * @return the number of valid samples in the array returned by the last call to
	 * {@link #readNextBlock()}
	 */
	public int getLastReadCount() {
		return lastReadCount;
	}

	/**
	 * Close the sound file.
	 */
	public void close() {
		if (audioInputStream != null) {
			try {
				audioInputStream.close();
			} catch (IOException e) {
				// nothing useful to do about it.
			}
			audioInputStream = null;
		}
	}

}
