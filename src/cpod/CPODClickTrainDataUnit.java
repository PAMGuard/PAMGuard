package cpod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import PamDetection.PamDetection;
import PamUtils.PamCalendar;
import PamUtils.avrgwaveform.AverageWaveform;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import cpod.CPODClassification.CPODSpeciesType;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * Base class for a click train data unit.
 *
 * @author Jamie Macaulay
 *
 */
public class CPODClickTrainDataUnit extends DetectionGroupDataUnit implements PamDetection {

	/**
	 * The FFT length used for the average spectrum of a click train.
	 */
	private static final int AVERAGE_FFT_LEN = 1024;

	CPODClassification cpodClassification;

	/**
	 * Summary measurements of the clicks within the train. Calculated lazily because
	 * sub detections are added one at a time, both on import and when data are
	 * re-loaded in viewer mode.
	 */
	private CPODTrainInfo trainInfo;

	/**
	 * The number of sub detections which were in memory the last time trainInfo was
	 * calculated. Used to work out when the summary measurements need re-calculating.
	 */
	private int lastSubDetCount = -1;


	public CPODClickTrainDataUnit(long timeMilliseconds, List<PamDataUnit> list, CPODClassification cpodClassification) {
		super(timeMilliseconds, null);
		this.cpodClassification=cpodClassification;
	}

	public CPODSpeciesType getSpecies() {
		return cpodClassification.species;
	}

	public int getConfidence() {
		return cpodClassification.qualitylevel;
	}

	/**
	 * Get the id of the click train. This is unique within the CP3 or FP3 file the
	 * train was imported from.
	 * @return the click train id.
	 */
	public int getClickTrainID() {
		return cpodClassification.clicktrainID;
	}

	public boolean isEcho() {
		return cpodClassification.isEcho;
	}

	/**
	 * Get the classification of the click train.
	 * @return the classification.
	 */
	public CPODClassification getClassification() {
		return cpodClassification;
	}

	/**
	 * Get the average spectrum of the clicks in the train.
	 * <p>
	 * Most POD clicks have no waveform, in which case this is an amplitude weighted
	 * histogram of the frequency limits of the clicks rather than a true spectrum. If
	 * the train does contain clicks with waveforms, e.g. from an FPOD, then those
	 * waveforms are used and this is a genuine average spectrum.
	 * @return the average spectrum, or null if there are no clicks in memory.
	 */
	public double[] getAverageSpectra() {
		CPODTrainInfo info = checkTrainInfo();
		if (info.averageWaveform == null) {
			return null;
		}
		return info.averageWaveform.getAverageSpectra();
	}

	/**
	 * Get the sample rate which the average spectrum from {@link #getAverageSpectra()}
	 * corresponds to. This is not the same for waveform and frequency limit based
	 * spectra, since FPOD waveforms are sampled far faster than the nominal POD
	 * sample rate.
	 * @return the sample rate in samples per second.
	 */
	public float getAverageSpectraSampleRate() {
		CPODTrainInfo info = checkTrainInfo();
		return info.fromWaveforms ? FPODReader.FPOD_WAV_SAMPLERATE : CPODClickDataBlock.CPOD_SR;
	}

	/**
	 * Get information on the click train. This is the text which is appended to the
	 * tool tip of every click within the train.
	 * @return a summary of the click train, in html.
	 */
	public String getStringInfo() {
		CPODTrainInfo info = checkTrainInfo();

		String str = String.format("Click train %d: %s, quality %d/3%s<p>", getClickTrainID(), getSpecies(),
				getConfidence(), isEcho() ? ", echo" : "");
		str += trainMeasurementString(info);

		return str;
	}

	@Override
	public String getSummaryString() {
		CPODTrainInfo info = checkTrainInfo();

		String str = "<html>";
		if (getParentDataBlock() != null) {
			str += "<i>" + getParentDataBlock().getDataName() + "</i><p>";
		}
		str += String.format("UID: %d, click train %d<p>", getUID(), getClickTrainID());
		str += String.format("%s %s<p>", PamCalendar.formatDate(getTimeMilliseconds()),
				PamCalendar.formatTime(getTimeMilliseconds(), 3));
		str += String.format("Species: %s<p>", getSpecies());
		str += String.format("Quality: %d of 3%s<p>", getConfidence(), isEcho() ? " (echo train)" : "");
		str += trainMeasurementString(info);

		return str;
	}

	/**
	 * Create the part of the tool tip which describes the clicks within the train, i.e.
	 * everything which is measured rather than read from the file.
	 * @param info - the train measurements.
	 * @return an html string of the measurements.
	 */
	private String trainMeasurementString(CPODTrainInfo info) {
		if (info.nClicks == 0) {
			//in viewer mode the clicks are only loaded if the CPOD detections are within the
			//loaded time period.
			return "No clicks loaded<p>";
		}

		String str = String.format("Clicks: %d", info.nClicks);
		if (info.nWaveforms > 0) {
			str += String.format(" (%d with waveforms)", info.nWaveforms);
		}
		str += "<p>";

		Double duration = getDurationInMilliseconds();
		if (duration != null) {
			str += String.format("Duration: %s<p>", formatMillis(duration));
		}

		if (info.medianICI != null) {
			str += String.format("Median ICI: %s (%.1f clicks/s)<p>", formatMillis(info.medianICI),
					1000./info.medianICI);
			str += String.format("ICI range: %s to %s<p>", formatMillis(info.minICI), formatMillis(info.maxICI));
		}

		str += String.format("Peak freq: %.0fkHz (%.0f to %.0fkHz)<p>", info.meanKHz, info.minKHz, info.maxKHz);
		str += String.format("Bandwidth: %.0fkHz<p>", info.meanBw);
		str += String.format("Amplitude: %.1fdB mean, %.1fdB max<p>", info.meanAmplitude, info.maxAmplitude);
		str += String.format("N cycles: %.1f<p>", info.meanNCyc);

		return str;
	}

	/**
	 * Format a time in milliseconds, using units which suit the size of the number.
	 * @param millis - the time in milliseconds.
	 * @return the formatted time.
	 */
	private String formatMillis(double millis) {
		if (millis < 1000) {
			return String.format("%.1fms", millis);
		}
		return String.format("%.2fs", millis/1000.);
	}

	/**
	 * Get the summary measurements of the train, re-calculating them if sub detections
	 * have been added or removed since they were last worked out.
	 * @return the train measurements. Never null.
	 */
	private synchronized CPODTrainInfo checkTrainInfo() {
		int nSubDets = getLoadedSubDetectionsCount();
		if (trainInfo != null && nSubDets == lastSubDetCount) {
			return trainInfo;
		}
		lastSubDetCount = nSubDets;
		trainInfo = calcTrainInfo();
		return trainInfo;
	}

	/**
	 * Work through the clicks in the train, measuring everything which goes into the
	 * tool tip and building the average spectrum.
	 * @return the train measurements. Never null.
	 */
	private CPODTrainInfo calcTrainInfo() {
		CPODTrainInfo info = new CPODTrainInfo();

		ArrayList<PamDataUnit<?,?>> subDets = getSubDetections();
		if (subDets == null || subDets.size() == 0) {
			return info;
		}

		/*
		 * If any of the clicks have waveforms then the average spectrum is made from those
		 * alone. Waveform spectra and frequency limit histograms are on completely different
		 * frequency scales, so mixing the two within a single train would be meaningless.
		 */
		for (PamDataUnit<?,?> subDet : subDets) {
			if (hasWaveform(subDet)) {
				info.nWaveforms++;
			}
		}
		info.fromWaveforms = info.nWaveforms > 0;

		AverageWaveform averageWaveform = new AverageWaveform();
		double[] icis = new double[subDets.size()-1];
		int nICI = 0;
		PamDataUnit<?,?> prevDet = null;

		for (PamDataUnit<?,?> subDet : subDets) {
			info.nClicks++;

			if (subDet instanceof CPODClick) {
				CPODClick cpodClick = (CPODClick) subDet;
				info.meanKHz += cpodClick.getkHz();
				info.minKHz = Math.min(info.minKHz, cpodClick.getkHz());
				info.maxKHz = Math.max(info.maxKHz, cpodClick.getkHz());
				info.meanBw += cpodClick.getBw();
				info.meanNCyc += cpodClick.getnCyc();
			}
			info.meanAmplitude += subDet.getAmplitudeDB();
			info.maxAmplitude = Math.max(info.maxAmplitude, subDet.getAmplitudeDB());

			if (prevDet != null) {
				icis[nICI++] = interDetectionInterval(prevDet, subDet);
			}
			prevDet = subDet;

			addToAverageSpectrum(averageWaveform, subDet, info.fromWaveforms);
		}

		info.meanKHz /= info.nClicks;
		info.meanBw /= info.nClicks;
		info.meanNCyc /= info.nClicks;
		info.meanAmplitude /= info.nClicks;
		info.averageWaveform = averageWaveform;

		if (nICI > 0) {
			Arrays.sort(icis);
			info.minICI = icis[0];
			info.maxICI = icis[nICI-1];
			info.medianICI = icis[nICI/2];
		}

		return info;
	}

	/**
	 * Add a click to the average spectrum for the train.
	 * @param averageWaveform - the average waveform to add to.
	 * @param subDet - the click to add.
	 * @param useWaveforms - true to use waveforms, in which case clicks without a
	 * waveform are skipped. False to use the frequency limits of every click.
	 */
	private void addToAverageSpectrum(AverageWaveform averageWaveform, PamDataUnit<?,?> subDet, boolean useWaveforms) {
		if (useWaveforms) {
			if (hasWaveform(subDet)) {
				averageWaveform.addWaveform(((RawDataHolder) subDet).getWaveData()[0],
						FPODReader.FPOD_WAV_SAMPLERATE, AVERAGE_FFT_LEN, true);
			}
			return;
		}
		double[] freq = subDet.getFrequency();
		if (freq == null || freq.length < 2) {
			return;
		}
		averageWaveform.addWaveform(freq[0], freq[1], subDet.getAmplitudeDB(),
				CPODClickDataBlock.CPOD_SR, AVERAGE_FFT_LEN);
	}

	/**
	 * Check whether a click has waveform data. Only some FPOD clicks do.
	 * @param subDet - the click to check.
	 * @return true if the click has a waveform.
	 */
	private boolean hasWaveform(PamDataUnit<?,?> subDet) {
		if (!(subDet instanceof RawDataHolder)) {
			return false;
		}
		double[][] waveData = ((RawDataHolder) subDet).getWaveData();
		return waveData != null && waveData.length > 0 && waveData[0] != null;
	}

	/**
	 * Get the interval between two clicks in milliseconds. POD click times are only
	 * stored to the nearest millisecond, which is far too coarse for the inter click
	 * intervals of a NBHF train, so use the start samples if they are available.
	 * @param prevDet - the earlier click.
	 * @param subDet - the later click.
	 * @return the interval in milliseconds.
	 */
	private double interDetectionInterval(PamDataUnit<?,?> prevDet, PamDataUnit<?,?> subDet) {
		Long prevSample = prevDet.getStartSample();
		Long sample = subDet.getStartSample();
		if (prevSample != null && sample != null && sample > prevSample) {
			return 1000.*(sample-prevSample)/CPODClickDataBlock.CPOD_SR;
		}
		return subDet.getTimeMilliseconds() - prevDet.getTimeMilliseconds();
	}

	/**
	 * Summary measurements of the clicks within a click train.
	 *
	 * @author Jamie Macaulay
	 *
	 */
	private static class CPODTrainInfo {

		/**
		 * The number of clicks which are in memory.
		 */
		int nClicks = 0;

		/**
		 * The number of clicks which have waveform data.
		 */
		int nWaveforms = 0;

		/**
		 * True if the average spectrum was made from waveforms rather than from the
		 * frequency limits of the clicks.
		 */
		boolean fromWaveforms = false;

		/**
		 * The average spectrum of the clicks. Null if there are no clicks in memory.
		 */
		AverageWaveform averageWaveform = null;

		double meanKHz = 0;
		double minKHz = Double.MAX_VALUE;
		double maxKHz = -Double.MAX_VALUE;
		double meanBw = 0;
		double meanNCyc = 0;
		double meanAmplitude = 0;
		double maxAmplitude = -Double.MAX_VALUE;

		/**
		 * Inter click intervals in milliseconds. Null if there are fewer than two clicks
		 * in memory.
		 */
		Double medianICI = null;
		double minICI = 0;
		double maxICI = 0;
	}

}
