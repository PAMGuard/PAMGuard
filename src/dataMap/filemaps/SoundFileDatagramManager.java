package dataMap.filemaps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import Spectrogram.WindowFunction;
import dataGram.Datagram;
import dataGram.DatagramDataPoint;
import dataGram.DatagramManager;
import dataGram.DatagramProgress;
import dataGram.DatagramWorkMonitor;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import fftManager.FastFFT;

/**
 * Datagram manager for raw sound files.
 * <p>
 * Unlike the standard {@link DatagramManager}, which reloads data units from a data
 * store and passes them to a DatagramProvider, this reads the sound files directly and
 * summarises them as it goes. Loading raw audio back through the data block would use a
 * huge amount of memory and would also throw away whatever data the user currently has
 * loaded in the viewer.
 * <p>
 * Two summaries are made in the same pass through each file, since reading the files is
 * by far the expensive part: a waveform summary (rms and peak) and a long term spectral
 * average. The user picks which one to look at from the data map, and neither has to be
 * recalculated when they switch.
 * <p>
 * Datagrams are attached to the {@link FileDataMapPoint}s and therefore get saved with
 * the rest of the sound file map in serialisedSoundFileMap.data, so they only ever have
 * to be calculated once.
 * 
 * @author Jamie Macaulay
 *
 */
public class SoundFileDatagramManager extends DatagramManager {

	/**
	 * Default datagram bin size in seconds, used until we've seen the sound files and can
	 * work out something better.
	 */
	public static final int DEFAULT_DATAGRAM_SECONDS = 5;

	/**
	 * Roughly how many points we want in each sound file for the waveform summary. A
	 * fixed bin size doesn't work across the range of file lengths people use: 5s bins
	 * are about right for a minute long file but would make three quarters of a million
	 * points for a folder of a thousand hour long files, which is far too much to hold in
	 * memory and save.
	 */
	private static final int WAVEFORM_POINTS_PER_FILE = 100;

	/**
	 * The LTSA holds 256 values per point rather than 2, so it has to be a lot coarser in
	 * time or the saved map becomes enormous. That suits an LTSA anyway - they're
	 * conventionally drawn with a column every minute or so.
	 */
	private static final int LTSA_POINTS_PER_FILE = 10;

	/**
	 * Shortest LTSA bin, in seconds.
	 */
	private static final int MIN_LTSA_SECONDS = 10;

	/**
	 * Bin size limits in seconds.
	 */
	private static final int MIN_DATAGRAM_SECONDS = 1, MAX_DATAGRAM_SECONDS = 600;

	/**
	 * Sanity limit on the number of bins in a single file, in case a file has a corrupt
	 * end time far in the future.
	 */
	private static final int MAX_POINTS_PER_FILE = 10000;

	/**
	 * Save the map every so often so that a cancelled or crashed run doesn't lose
	 * everything that's been calculated.
	 */
	private static final int SAVE_INTERVAL = 50;

	/**
	 * How often, in milliseconds, to re-search the map for datagrams once we've found
	 * that there aren't any.
	 */
	private static final long EMPTY_CHECK_INTERVAL_MILLIS = 1000;

	/**
	 * Minimum time in milliseconds between data map repaints while datagrams are being
	 * created.
	 */
	private static final long REPAINT_INTERVAL_MILLIS = 1000;

	private OfflineFileServer<?> offlineFileServer;

	private int channel = 0;

	/**
	 * Which summary is currently being displayed - see the MODE_ constants in
	 * {@link SoundFileDatagramProvider}.
	 */
	private int datagramMode = SoundFileDatagramProvider.MODE_WAVEFORM;

	/**
	 * Bin sizes actually used by the stored datagrams, worked out from the sound files
	 * when they're created. Zero until they're known.
	 */
	private int waveformSeconds, ltsaSeconds;

	private int filesSinceSave = 0;

	private long lastRepaintRequest;

	/**
	 * True once we know there is at least one datagram. Latched, since datagrams are only
	 * ever added, never removed.
	 */
	private volatile boolean anyDatagram;

	/**
	 * Time the map was last searched for a datagram and found none.
	 */
	private volatile long lastEmptyCheck;

	/**
	 * Progress of the current creation run, or null if nothing is running.
	 */
	private volatile SoundFileDatagramProgress progress;

	private List<SoundFileDatagramListener> listeners = new ArrayList<>();

	private FastFFT fastFFT = new FastFFT();

	private double[] fftWindow;

	public SoundFileDatagramManager(OfflineFileServer<?> offlineFileServer, String settingsName) {
		super((OfflineDataStore) offlineFileServer, settingsName);
		this.offlineFileServer = offlineFileServer;
		/*
		 * The sound file datagram is created on demand and works out its own bin sizes
		 * from the file lengths, so there is nothing for the user to choose and the bin
		 * size dialog should never appear.
		 */
		setDefaultDatagramSeconds(DEFAULT_DATAGRAM_SECONDS);
		getDatagramSettings().validDatagramSettings = true;
	}

	/**
	 * Listener for progress of sound file datagram creation, so that the data map can
	 * show what's going on without a modal dialog getting in the way.
	 */
	public interface SoundFileDatagramListener {
		/**
		 * @param progress current progress, or null when nothing is running.
		 */
		public void datagramProgress(SoundFileDatagramProgress progress);
	}

	/**
	 * Progress of a sound file datagram creation run.
	 */
	public static class SoundFileDatagramProgress {
		/**
		 * 1 based index of the file currently being read, and the total number of files
		 * to read.
		 */
		public final int filesDone, filesTotal;

		/**
		 * Name of the file currently being read, may be null.
		 */
		public final String currentFile;

		/**
		 * How far through the current file we are, 0-1.
		 */
		public final double withinFile;

		public SoundFileDatagramProgress(int filesDone, int filesTotal, String currentFile, double withinFile) {
			this.filesDone = filesDone;
			this.filesTotal = filesTotal;
			this.currentFile = currentFile;
			this.withinFile = withinFile;
		}

		/**
		 * @return fraction complete in the range 0-1, or -1 if it isn't known.
		 */
		public double getFraction() {
			if (filesTotal <= 0) {
				return -1;
			}
			double done = Math.max(0, filesDone - 1) + Math.max(0, Math.min(1, withinFile));
			return Math.max(0, Math.min(1, done / filesTotal));
		}
	}

	public void addListener(SoundFileDatagramListener listener) {
		synchronized (listeners) {
			if (listeners.contains(listener) == false) {
				listeners.add(listener);
			}
		}
	}

	public void removeListener(SoundFileDatagramListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void notifyListeners() {
		SoundFileDatagramProgress p = progress;
		List<SoundFileDatagramListener> copy;
		synchronized (listeners) {
			copy = new ArrayList<>(listeners);
		}
		for (SoundFileDatagramListener listener : copy) {
			listener.datagramProgress(p);
		}
	}

	/**
	 * @return progress of the current creation run, or null if nothing is running.
	 */
	public SoundFileDatagramProgress getProgress() {
		return progress;
	}

	/**
	 * @return the summary currently being displayed, one of the MODE_ constants in
	 * {@link SoundFileDatagramProvider}.
	 */
	public int getDatagramMode() {
		return datagramMode;
	}

	/**
	 * @param datagramMode the summary to display, one of the MODE_ constants in
	 * {@link SoundFileDatagramProvider}.
	 */
	public void setDatagramMode(int datagramMode) {
		this.datagramMode = datagramMode;
	}

	/**
	 * Pick out the datagram for whichever summary is currently being displayed.
	 */
	@Override
	protected Datagram getDatagram(OfflineDataMapPoint dmp) {
		if (dmp instanceof FileDataMapPoint == false) {
			return super.getDatagram(dmp);
		}
		FileDataMapPoint fmp = (FileDataMapPoint) dmp;
		return datagramMode == SoundFileDatagramProvider.MODE_LTSA ? fmp.getLtsaDatagram()
				: fmp.getWaveformDatagram();
	}

	/**
	 * A map point needs processing unless it has <i>both</i> summaries, since they're
	 * made together in a single read of the file. The bin size isn't checked - it's
	 * worked out from the sound files rather than being something the user sets, so
	 * whatever is stored is what we use.
	 */
	@Override
	protected boolean isDatagramValid(OfflineDataMapPoint dmp, Datagram datagram) {
		if (dmp instanceof FileDataMapPoint == false) {
			return super.isDatagramValid(dmp, datagram);
		}
		FileDataMapPoint fmp = (FileDataMapPoint) dmp;
		return hasPoints(fmp.getWaveformDatagram(), SoundFileDatagramProvider.WAVEFORM_POINTS)
				&& hasPoints(fmp.getLtsaDatagram(), SoundFileDatagramProvider.LTSA_POINTS);
	}

	/**
	 * @return true if the datagram exists and has the expected number of values in each
	 * of its lines.
	 */
	private boolean hasPoints(Datagram datagram, int nPoints) {
		if (datagram == null) {
			return false;
		}
		if (datagram.getNumDataPoints() == 0) {
			return false;
		}
		return datagram.getDataPoint(0).getData().length == nPoints;
	}

	/**
	 * Bin size of whichever summary is being displayed. The two have quite different time
	 * resolutions, so the base class's single settings value is no use here.
	 */
	@Override
	protected long getDatagramMillis() {
		int seconds = datagramMode == SoundFileDatagramProvider.MODE_LTSA ? ltsaSeconds : waveformSeconds;
		if (seconds <= 0) {
			seconds = findStoredInterval();
		}
		if (seconds <= 0) {
			return super.getDatagramMillis();
		}
		return seconds * 1000L;
	}

	/**
	 * Find the bin size of the stored datagrams for the current mode. Needed after a
	 * restart, when the datagrams have come back from the serialised map but we haven't
	 * calculated anything this session.
	 * @return bin size in seconds, or 0 if there are no datagrams.
	 */
	private int findStoredInterval() {
		int seconds = findStoredInterval(datagramMode);
		if (seconds > 0) {
			if (datagramMode == SoundFileDatagramProvider.MODE_LTSA) {
				ltsaSeconds = seconds;
			} else {
				waveformSeconds = seconds;
			}
		}
		return seconds;
	}

	/**
	 * Find the bin size of the stored datagrams for a particular mode.
	 * @param mode one of the MODE_ constants in {@link SoundFileDatagramProvider}
	 * @return bin size in seconds, or 0 if there are no datagrams of that type.
	 */
	private int findStoredInterval(int mode) {
		OfflineDataMap dataMap = offlineFileServer.getDataMap();
		if (dataMap == null) {
			return 0;
		}
		synchronized (dataMap) {
			Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
			while (it.hasNext()) {
				OfflineDataMapPoint dmp = it.next();
				if (dmp instanceof FileDataMapPoint == false) {
					continue;
				}
				FileDataMapPoint fmp = (FileDataMapPoint) dmp;
				Datagram datagram = mode == SoundFileDatagramProvider.MODE_LTSA ? fmp.getLtsaDatagram()
						: fmp.getWaveformDatagram();
				if (datagram != null && datagram.getIntervalSeconds() > 0) {
					return datagram.getIntervalSeconds();
				}
			}
		}
		return 0;
	}

	/**
	 * Work out sensible bin sizes from the length of the sound files and then start
	 * making the datagrams. Call this rather than updateDatagrams() when the user asks
	 * for the sound files to be summarised.
	 * 
	 * @param dataBlock the raw data block
	 */
	public void createDatagrams(PamDataBlock dataBlock) {
		if (getStatus()) {
			// already running - the GUI stays live while we work, so the user can ask twice.
			return;
		}
		/*
		 * If some files have already been summarised - after new recordings have been
		 * added to the folder, say - stick with the bin sizes they used, otherwise the
		 * old and new parts of the data map would be drawn at different resolutions.
		 */
		double meanSeconds = meanFileSeconds(dataBlock);
		waveformSeconds = findStoredInterval(SoundFileDatagramProvider.MODE_WAVEFORM);
		if (waveformSeconds <= 0) {
			waveformSeconds = clamp(meanSeconds/WAVEFORM_POINTS_PER_FILE, MIN_DATAGRAM_SECONDS);
		}
		ltsaSeconds = findStoredInterval(SoundFileDatagramProvider.MODE_LTSA);
		if (ltsaSeconds <= 0) {
			ltsaSeconds = clamp(meanSeconds/LTSA_POINTS_PER_FILE, MIN_LTSA_SECONDS);
		}
		updateDatagrams();
	}

	private int clamp(double seconds, int minimum) {
		int rounded = (int) Math.round(seconds);
		return Math.max(minimum, Math.min(MAX_DATAGRAM_SECONDS, rounded));
	}

	/**
	 * @return the mean length of the sound files in seconds, or the default bin size if
	 * there aren't any.
	 */
	private double meanFileSeconds(PamDataBlock dataBlock) {
		OfflineDataMap dataMap = dataBlock.getOfflineDataMap(getOfflineDataStore());
		if (dataMap == null) {
			return DEFAULT_DATAGRAM_SECONDS * WAVEFORM_POINTS_PER_FILE;
		}
		long totalMillis = 0;
		int nPoints = 0;
		synchronized (dataMap) {
			Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
			while (it.hasNext()) {
				OfflineDataMapPoint dmp = it.next();
				long len = dmp.getEndTime() - dmp.getStartTime();
				if (len <= 0) {
					continue;
				}
				totalMillis += len;
				nPoints++;
			}
		}
		if (nPoints == 0) {
			return DEFAULT_DATAGRAM_SECONDS * WAVEFORM_POINTS_PER_FILE;
		}
		return totalMillis / (double) nPoints / 1000.;
	}

	@Override
	protected void processDataMapPoint(PamDataBlock dataBlock, OfflineDataMapPoint dmp,
			DatagramWorkMonitor workMonitor) {
		if (dmp instanceof FileDataMapPoint == false) {
			return;
		}
		FileDataMapPoint fileMapPoint = (FileDataMapPoint) dmp;
		long startTime = dmp.getStartTime();
		long endTime = dmp.getEndTime();
		if (endTime < startTime) {
			return;
		}
		if (waveformSeconds <= 0) {
			waveformSeconds = DEFAULT_DATAGRAM_SECONDS;
		}
		if (ltsaSeconds <= 0) {
			ltsaSeconds = Math.max(MIN_LTSA_SECONDS, waveformSeconds);
		}

		int nWaveBins = binCount(startTime, endTime, waveformSeconds);
		int nLtsaBins = binCount(startTime, endTime, ltsaSeconds);
		if (nWaveBins < 0 || nLtsaBins < 0) {
			System.err.printf("Sound file %s appears to be %d seconds long - not making a datagram for it\n",
					fileMapPoint.getName(), (endTime - startTime) / 1000);
			return;
		}

		double[] peak = new double[nWaveBins];
		double[] sumSquares = new double[nWaveBins];
		long[] sampleCount = new long[nWaveBins];

		int nFreq = SoundFileDatagramProvider.LTSA_POINTS;
		double[][] ltsaSum = new double[nLtsaBins][nFreq];
		int[] ltsaCount = new int[nLtsaBins];

		SoundFileSampleReader reader = new SoundFileSampleReader(fileMapPoint, channel);
		if (reader.open() == false) {
			System.err.println("Unable to open sound file for datagram: " + fileMapPoint.getName());
			return;
		}
		try {
			float sampleRate = reader.getSampleRate();
			if (sampleRate <= 0) {
				return;
			}
			double waveSamplesPerBin = sampleRate * waveformSeconds;
			double ltsaSamplesPerBin = sampleRate * ltsaSeconds;
			/*
			 * Anything past the end of the last bin can't be stored, so there's no point
			 * reading it. This only really matters when a map point has no proper end
			 * time, in which case there's a single bin and most of the file is of no use
			 * to us.
			 */
			long maxSamples = (long) Math.max(waveSamplesPerBin * nWaveBins, ltsaSamplesPerBin * nLtsaBins);
			long totalSamples = Math.min(maxSamples, (long) ((endTime - startTime) * sampleRate / 1000.));

			int fftLength = SoundFileDatagramProvider.FFT_LENGTH;
			double[] fftBuffer = new double[fftLength];
			int fftFill = 0;
			int[] freqMap = createFrequencyMap(dataBlock, sampleRate);
			if (fftWindow == null || fftWindow.length != fftLength) {
				fftWindow = WindowFunction.hann(fftLength);
			}

			long sampleIndex = 0;
			double[] block;
			long lastUpdate = System.currentTimeMillis();
			while ((block = reader.readNextBlock()) != null) {
				int n = reader.getLastReadCount();
				for (int i = 0; i < n; i++) {
					double v = block[i];
					long sample = sampleIndex + i;

					int iBin = (int) (sample / waveSamplesPerBin);
					if (iBin >= 0 && iBin < nWaveBins) {
						double absV = v < 0 ? -v : v;
						if (absV > peak[iBin]) {
							peak[iBin] = absV;
						}
						sumSquares[iBin] += v * v;
						sampleCount[iBin]++;
					}

					fftBuffer[fftFill++] = v;
					if (fftFill == fftLength) {
						/*
						 * Attribute the spectrum to the bin holding the start of the FFT,
						 * so an FFT straddling a bin boundary doesn't get split.
						 */
						int lBin = (int) ((sample - fftLength + 1) / ltsaSamplesPerBin);
						addSpectrum(fftBuffer, freqMap, ltsaSum, ltsaCount, lBin, nLtsaBins);
						fftFill = 0;
					}
				}
				sampleIndex += n;
				if (sampleIndex >= maxSamples) {
					break;
				}
				long now = System.currentTimeMillis();
				if (now - lastUpdate > 500) {
					lastUpdate = now;
					workMonitor.publishProgress(new DatagramProgress(DatagramProgress.STATUS_UNITCOUNT,
							(int) Math.min(totalSamples, Integer.MAX_VALUE),
							(int) Math.min(sampleIndex, Integer.MAX_VALUE)));
				}
				if (workMonitor.isWorkCancelled()) {
					return;
				}
			}
		} finally {
			reader.close();
		}

		fileMapPoint.setWaveformDatagram(
				buildWaveformDatagram(startTime, waveformSeconds, nWaveBins, peak, sumSquares, sampleCount));
		fileMapPoint.setLtsaDatagram(buildLtsaDatagram(startTime, ltsaSeconds, nLtsaBins, ltsaSum, ltsaCount));
		anyDatagram = true;

		if (++filesSinceSave >= SAVE_INTERVAL) {
			filesSinceSave = 0;
			offlineFileServer.saveSerialisedMap();
		}
		requestRepaint();
	}

	/**
	 * Number of datagram bins needed to cover a map point.
	 * <p>
	 * This rounds up rather than using the +1 the standard DatagramManager uses. Sound
	 * data is continuous, so a file whose length is an exact multiple of the bin size
	 * would otherwise always end with an empty bin, which the data map draws as a gap.
	 * 
	 * @return the number of bins, or -1 if the map point is implausibly long.
	 */
	private int binCount(long startTime, long endTime, int binSeconds) {
		long binMillis = binSeconds * 1000L;
		long bins = (endTime - startTime + binMillis - 1) / binMillis;
		if (bins > MAX_POINTS_PER_FILE) {
			return -1;
		}
		return (int) Math.max(1, bins);
	}

	/**
	 * Window, transform and accumulate one chunk of samples into an LTSA bin.
	 */
	private void addSpectrum(double[] fftBuffer, int[] freqMap, double[][] ltsaSum, int[] ltsaCount, int iBin,
			int nBins) {
		if (iBin < 0 || iBin >= nBins) {
			return;
		}
		double[] windowed = new double[fftBuffer.length];
		for (int i = 0; i < fftBuffer.length; i++) {
			windowed[i] = fftBuffer[i] * fftWindow[i];
		}
		ComplexArray spectrum = fastFFT.rfft(windowed, fftBuffer.length);
		double[] sum = ltsaSum[iBin];
		int n = Math.min(spectrum.length(), freqMap.length);
		for (int i = 0; i < n; i++) {
			int out = freqMap[i];
			if (out >= 0 && out < sum.length) {
				sum[out] += spectrum.magsq(i);
			}
		}
		ltsaCount[iBin]++;
	}

	/**
	 * The frequency axis of the datagram is fixed by the data block's sample rate, but
	 * individual files may have been recorded at a different rate. This maps the file's
	 * own FFT bins onto the display's frequency bins; when the rates match, as they
	 * almost always do, it's just the identity.
	 * 
	 * @param dataBlock the raw data block, giving the reference sample rate
	 * @param fileSampleRate the sample rate of the file being read
	 * @return for each FFT bin of the file, the display bin it belongs in, or -1 if it
	 * falls outside the displayed frequency range.
	 */
	private int[] createFrequencyMap(PamDataBlock dataBlock, float fileSampleRate) {
		int nBins = SoundFileDatagramProvider.LTSA_POINTS;
		int[] map = new int[nBins];
		float refSampleRate = dataBlock.getSampleRate();
		if (refSampleRate <= 0 || refSampleRate == fileSampleRate) {
			for (int i = 0; i < nBins; i++) {
				map[i] = i;
			}
			return map;
		}
		double scale = fileSampleRate / refSampleRate;
		for (int i = 0; i < nBins; i++) {
			int out = (int) Math.round(i * scale);
			map[i] = (out >= 0 && out < nBins) ? out : -1;
		}
		return map;
	}

	/**
	 * Turn the accumulated rms and peak values into a datagram. Bins with no data are
	 * left as all zeros, which the data map draws as a gap.
	 */
	private Datagram buildWaveformDatagram(long startTime, int binSeconds, int nBins, double[] peak,
			double[] sumSquares, long[] sampleCount) {
		Datagram datagram = new Datagram(binSeconds);
		long binMillis = binSeconds * 1000L;
		long currentStart = startTime;
		for (int iBin = 0; iBin < nBins; iBin++) {
			DatagramDataPoint point = new DatagramDataPoint(datagram, currentStart, currentStart + binMillis,
					SoundFileDatagramProvider.WAVEFORM_POINTS);
			if (sampleCount[iBin] > 0) {
				float[] data = point.getData();
				/*
				 * The data map treats an all zero point as 'no data' so that gaps between
				 * files aren't drawn as spikes down to zero. Digitally silent audio would
				 * look exactly the same, so floor the values at something too small to
				 * see but not actually zero.
				 */
				data[SoundFileDatagramProvider.RMS_INDEX] = Math.max(Float.MIN_NORMAL,
						(float) Math.sqrt(sumSquares[iBin] / sampleCount[iBin]));
				data[SoundFileDatagramProvider.PEAK_INDEX] = Math.max(Float.MIN_NORMAL, (float) peak[iBin]);
				point.setData(data, (int) Math.min(sampleCount[iBin], Integer.MAX_VALUE));
			}
			datagram.addDataPoint(point);
			currentStart += binMillis;
		}
		return datagram;
	}

	/**
	 * Turn the accumulated power spectra into a datagram of mean power per bin.
	 */
	private Datagram buildLtsaDatagram(long startTime, int binSeconds, int nBins, double[][] ltsaSum,
			int[] ltsaCount) {
		Datagram datagram = new Datagram(binSeconds);
		long binMillis = binSeconds * 1000L;
		long currentStart = startTime;
		int nFreq = SoundFileDatagramProvider.LTSA_POINTS;
		for (int iBin = 0; iBin < nBins; iBin++) {
			DatagramDataPoint point = new DatagramDataPoint(datagram, currentStart, currentStart + binMillis, nFreq);
			if (ltsaCount[iBin] > 0) {
				float[] data = point.getData();
				double[] sum = ltsaSum[iBin];
				/*
				 * No need for the small non-zero floor used by the waveform summary: the
				 * 3D datagram painters draw a zero as 'no data' anyway, and flooring at
				 * Float.MIN_NORMAL would stretch the log colour scale over 38 decades and
				 * make everything the same colour.
				 */
				for (int i = 0; i < nFreq; i++) {
					data[i] = (float) (sum[i] / ltsaCount[iBin]);
				}
				point.setData(data, ltsaCount[iBin]);
			}
			datagram.addDataPoint(point);
			currentStart += binMillis;
		}
		return datagram;
	}

	/**
	 * Reading the sound files doesn't touch anything the user has loaded, so there is no
	 * reason to lock the GUI while it happens. It can easily run for an hour or more over
	 * a large deployment and the user needs to be able to carry on working meanwhile - the
	 * data map draws its own progress bar, with a cancel button, for anyone who wants to
	 * stop it.
	 */
	@Override
	protected boolean isGuiBlocking() {
		return false;
	}

	/**
	 * The data map shows its own progress indicator for sound files, complete with a
	 * cancel button, so the modal progress dialog the base class would otherwise put up
	 * is suppressed.
	 */
	@Override
	protected boolean handleProgress(DatagramProgress datagramProgress) {
		SoundFileDatagramProgress current = progress;
		switch (datagramProgress.getStatus()) {
		case DatagramProgress.STATUS_STARTINGBLOCK:
			progress = new SoundFileDatagramProgress(0, datagramProgress.pointsToUpdate, null, 0);
			break;
		case DatagramProgress.STATUS_STARTINGFILE:
			int total = current == null ? 0 : current.filesTotal;
			String name = datagramProgress.dataMapPoint == null ? null : datagramProgress.dataMapPoint.getName();
			progress = new SoundFileDatagramProgress(datagramProgress.currentPoint, total, name, 0);
			break;
		case DatagramProgress.STATUS_UNITCOUNT:
			/*
			 * How far through the current file we are. Worth using: a folder of hour long
			 * files would otherwise only move the bar once per file.
			 */
			if (current == null || datagramProgress.totalUnits <= 0) {
				return true;
			}
			double within = datagramProgress.processedUnits / (double) datagramProgress.totalUnits;
			progress = new SoundFileDatagramProgress(current.filesDone, current.filesTotal, current.currentFile,
					within);
			break;
		default:
			return true;
		}
		notifyListeners();
		return true;
	}

	@Override
	protected void datagramsComplete() {
		filesSinceSave = 0;
		progress = null;
		notifyListeners();
		/*
		 * This is called on the AWT thread and serialising the map for a large deployment
		 * takes a noticeable time, so don't do it here.
		 */
		Thread saveThread = new Thread(() -> offlineFileServer.saveSerialisedMap(), "Sound file map save");
		saveThread.start();
	}

	/**
	 * Ask the data map to redraw so that the user can watch the datagram appear rather
	 * than waiting for the whole lot to finish. Throttled, and always marshalled onto the
	 * event thread since this is called from a worker.
	 */
	private void requestRepaint() {
		long now = System.currentTimeMillis();
		if (now - lastRepaintRequest < REPAINT_INTERVAL_MILLIS) {
			return;
		}
		lastRepaintRequest = now;
		SwingUtilities.invokeLater(() -> {
			PamController.getInstance().notifyModelChanged(PamControllerInterface.OFFLINE_DATA_LOADED);
		});
	}

	/**
	 * @return true if any of the sound file map points already have a datagram.
	 * <p>
	 * This gets called from paint methods, so a negative answer is only recalculated once
	 * a second - searching a map of tens of thousands of sound files on every repaint
	 * would be far too slow. It can't be cached outright because map points arrive with
	 * their datagrams already attached when the serialised sound file map is read in,
	 * which happens well after this manager is created.
	 */
	public boolean hasAnyDatagram(PamDataBlock dataBlock) {
		if (anyDatagram) {
			return true;
		}
		long now = System.currentTimeMillis();
		if (now - lastEmptyCheck < EMPTY_CHECK_INTERVAL_MILLIS) {
			return false;
		}
		lastEmptyCheck = now;
		OfflineDataMap dataMap = dataBlock.getOfflineDataMap(getOfflineDataStore());
		if (dataMap == null) {
			return false;
		}
		synchronized (dataMap) {
			Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
			while (it.hasNext()) {
				if (getDatagram(it.next()) != null) {
					anyDatagram = true;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return true if any sound file is missing one or both of its summaries, so there is
	 * something for "create summaries" to do. Covers the case of a map built by an
	 * earlier version which has the waveform summary but no LTSA.
	 */
	public boolean needsCreating(PamDataBlock dataBlock) {
		OfflineDataMap dataMap = dataBlock.getOfflineDataMap(getOfflineDataStore());
		if (dataMap == null || dataMap.getNumMapPoints() == 0) {
			return false;
		}
		synchronized (dataMap) {
			Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
			while (it.hasNext()) {
				OfflineDataMapPoint dmp = it.next();
				if (isDatagramValid(dmp, getDatagram(dmp)) == false) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the channel being summarised.
	 */
	public int getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to summarise.
	 */
	public void setChannel(int channel) {
		this.channel = channel;
	}

}
