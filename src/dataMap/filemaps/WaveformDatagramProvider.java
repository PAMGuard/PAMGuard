package dataMap.filemaps;

import PamDetection.RawDataUnit;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

/**
 * Datagram provider for raw sound data. Rather than a spectral summary (which is what
 * most datagrams are) this summarises the waveform, storing two values per time bin:
 * the RMS and the peak amplitude, both in linear full scale units.
 * <p>
 * Both values are positive so that they don't clash with the -1 "no data" value which
 * {@link dataGram.DatagramManager#getImageData} writes into empty bins.
 * <p>
 * Modelled on {@link noiseOneBand.offline.OneBandDatagramProvider}, which does much the
 * same thing with rms, zero-peak and peak-peak measures.
 * 
 * @author Jamie Macaulay
 *
 */
public class WaveformDatagramProvider implements DatagramProvider {

	/**
	 * Index of the rms value within each datagram line.
	 */
	public static final int RMS_INDEX = 0;

	/**
	 * Index of the peak value within each datagram line.
	 */
	public static final int PEAK_INDEX = 1;

	/**
	 * Number of values stored for each datagram time bin.
	 */
	public static final int NUM_POINTS = 2;

	private DatagramScaleInformation scaleInfo;

	public WaveformDatagramProvider() {
		/*
		 * NaN min and max make the data map auto scale the axis to whatever is
		 * currently on screen.
		 */
		scaleInfo = new DatagramScaleInformation(Double.NaN, Double.NaN, "Amplitude", false,
				DatagramScaleInformation.PLOT_2D);
	}

	@Override
	public int getNumDataGramPoints() {
		return NUM_POINTS;
	}

	/**
	 * Add data from a raw data unit. Note that datagrams for sound files are normally
	 * made by reading the files directly in
	 * {@link SoundFileDatagramManager#processDataMapPoint}, which is a great deal faster
	 * than making data units, so this will rarely get called. It's implemented anyway so
	 * that the DatagramProvider contract is honoured.
	 */
	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		if (dataUnit instanceof RawDataUnit == false) {
			return 0;
		}
		double[] rawData = ((RawDataUnit) dataUnit).getRawData();
		if (rawData == null || rawData.length == 0) {
			return 0;
		}
		double peak = 0, sumSq = 0;
		for (int i = 0; i < rawData.length; i++) {
			peak = Math.max(peak, Math.abs(rawData[i]));
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
		return NUM_POINTS;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return scaleInfo;
	}

}
