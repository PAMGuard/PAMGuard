package dataMap.filemaps;

import PamDetection.RawDataUnit;
import PamUtils.FrequencyFormat;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

/**
 * Datagram provider for raw sound files, which can serve up two quite different
 * summaries of the same data:
 * <ul>
 * <li><b>Waveform</b> - two values per time bin, the RMS and the peak amplitude, drawn
 * as a pair of lines. Good for seeing the overall level and where the files are.</li>
 * <li><b>LTSA</b> - a long term spectral average, one power spectrum per time bin, drawn
 * as the usual colour datagram. Good for seeing what's actually in the recordings.</li>
 * </ul>
 * Both are calculated in a single pass through the sound files by
 * {@link SoundFileDatagramManager} and stored side by side on the
 * {@link FileDataMapPoint}s, so the user can flip between them without anything being
 * recalculated. This provider reports whichever the manager is currently set to show.
 * 
 * @author Jamie Macaulay
 *
 */
public class SoundFileDatagramProvider implements DatagramProvider {

	/**
	 * Show the waveform summary (rms and peak).
	 */
	public static final int MODE_WAVEFORM = 0;

	/**
	 * Show the long term spectral average.
	 */
	public static final int MODE_LTSA = 1;

	/**
	 * Index of the rms value within a waveform datagram line.
	 */
	public static final int RMS_INDEX = 0;

	/**
	 * Index of the peak value within a waveform datagram line.
	 */
	public static final int PEAK_INDEX = 1;

	/**
	 * Number of values in a waveform datagram line.
	 */
	public static final int WAVEFORM_POINTS = 2;

	/**
	 * FFT length used for the LTSA. Gives LTSA_POINTS frequency bins.
	 */
	public static final int FFT_LENGTH = 512;

	/**
	 * Number of frequency bins in an LTSA datagram line.
	 */
	public static final int LTSA_POINTS = FFT_LENGTH/2;

	private SoundFileDatagramManager datagramManager;

	private PamDataBlock rawDataBlock;

	private DatagramScaleInformation waveformScale;

	/**
	 * Cached LTSA scale information, rebuilt if the sample rate changes.
	 */
	private DatagramScaleInformation ltsaScale;

	private float ltsaScaleSampleRate;

	public SoundFileDatagramProvider(SoundFileDatagramManager datagramManager, PamDataBlock rawDataBlock) {
		this.datagramManager = datagramManager;
		this.rawDataBlock = rawDataBlock;
		/*
		 * NaN min and max make the data map auto scale the amplitude axis to whatever is
		 * currently on screen.
		 */
		waveformScale = new DatagramScaleInformation(Double.NaN, Double.NaN, "Amplitude", false,
				DatagramScaleInformation.PLOT_2D);
	}

	/**
	 * @return the number of values in a datagram line for a given mode.
	 */
	public static int getNumPoints(int mode) {
		return mode == MODE_LTSA ? LTSA_POINTS : WAVEFORM_POINTS;
	}

	@Override
	public int getNumDataGramPoints() {
		return getNumPoints(datagramManager.getDatagramMode());
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		if (datagramManager.getDatagramMode() != MODE_LTSA) {
			return waveformScale;
		}
		float sampleRate = rawDataBlock.getSampleRate();
		if (sampleRate <= 0) {
			// nothing sensible to draw an axis against yet.
			sampleRate = 1;
		}
		if (ltsaScale == null || sampleRate != ltsaScaleSampleRate) {
			double maxFreq = sampleRate/2;
			FrequencyFormat ff = FrequencyFormat.getFrequencyFormat(maxFreq);
			ltsaScale = new DatagramScaleInformation(0, maxFreq/ff.getScale(), ff.getUnitText(), false,
					DatagramScaleInformation.PLOT_3D);
			ltsaScaleSampleRate = sampleRate;
		}
		return ltsaScale;
	}

	/**
	 * Add data from a raw data unit. Note that sound file datagrams are normally made by
	 * reading the files directly in
	 * {@link SoundFileDatagramManager#processDataMapPoint}, which is a great deal faster
	 * than making data units, so this will rarely get called. It's implemented for the
	 * waveform mode anyway so that the DatagramProvider contract is honoured.
	 */
	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		if (datagramManager.getDatagramMode() == MODE_LTSA) {
			return 0;
		}
		if (dataUnit instanceof RawDataUnit == false) {
			return 0;
		}
		double[] rawData = ((RawDataUnit) dataUnit).getRawData();
		if (rawData == null || rawData.length == 0) {
			return 0;
		}
		double peak = 0, sumSq = 0;
		for (int i = 0; i < rawData.length; i++) {
			double absV = Math.abs(rawData[i]);
			if (absV > peak) {
				peak = absV;
			}
			sumSq += rawData[i]*rawData[i];
		}
		/*
		 * peaks combine by taking the largest; rms values have to be combined in power,
		 * but since we've no idea how many samples went into the existing value, just
		 * add in quadrature which is close enough for a data map summary.
		 */
		dataGramLine[PEAK_INDEX] = (float) Math.max(dataGramLine[PEAK_INDEX], peak);
		double rms = Math.sqrt(sumSq/rawData.length);
		dataGramLine[RMS_INDEX] = (float) Math.sqrt(dataGramLine[RMS_INDEX]*dataGramLine[RMS_INDEX] + rms*rms);
		return WAVEFORM_POINTS;
	}

}
