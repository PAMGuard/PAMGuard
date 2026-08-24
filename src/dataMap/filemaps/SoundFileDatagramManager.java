package dataMap.filemaps;

import javax.swing.SwingUtilities;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamguardMVC.PamDataBlock;
import dataGram.Datagram;
import dataGram.DatagramDataPoint;
import dataGram.DatagramManager;
import dataGram.DatagramPoint;
import dataGram.DatagramProgress;
import dataGram.DatagramWorkMonitor;
import dataMap.OfflineDataMapPoint;

/**
 * Datagram manager for raw sound files.
 * <p>
 * Unlike the standard {@link DatagramManager}, which reloads data units from a data
 * store and passes them to a DatagramProvider, this reads the sound files directly and
 * summarises the waveform as it goes. Loading raw audio back through the data block
 * would use a huge amount of memory and would also throw away whatever data the user
 * currently has loaded in the viewer.
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
	 * work out something better. Much shorter than the 600s used for detector datagrams
	 * since a waveform summary needs finer time resolution to be of any use.
	 */
	public static final int DEFAULT_DATAGRAM_SECONDS = 5;

	/**
	 * Roughly how many points we want in each sound file. A fixed bin size doesn't work
	 * across the range of file lengths people use: 5s bins are about right for a minute
	 * long file but would make three quarters of a million points for a folder of
	 * thousand hour long files, which is far too much to hold in memory and save.
	 */
	private static final int TARGET_POINTS_PER_FILE = 100;

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

	private OfflineFileServer<?> offlineFileServer;

	private int channel = 0;

	private int filesSinceSave = 0;

	/**
	 * Time of the last data map repaint request, so that a folder of thousands of short
	 * files doesn't flood the event thread with repaints.
	 */
	private long lastRepaintRequest;

	/**
	 * Minimum time in milliseconds between data map repaints while datagrams are being
	 * created.
	 */
	private static final long REPAINT_INTERVAL_MILLIS = 1000;

	public SoundFileDatagramManager(OfflineFileServer<?> offlineFileServer, String settingsName) {
		super((OfflineDataStore) offlineFileServer, settingsName);
		this.offlineFileServer = offlineFileServer;
		/*
		 * Settings are restored inside the super constructor, so by now datagramSettings
		 * is either something the user has already configured (validDatagramSettings
		 * true) or a brand new default. The default of 600s is aimed at detector
		 * datagrams and is far too coarse to see the shape of a sound file, so replace it
		 * the first time round.
		 */
		if (getDatagramSettings().validDatagramSettings == false
				|| getDatagramSettings().datagramSeconds <= 0) {
			getDatagramSettings().datagramSeconds = DEFAULT_DATAGRAM_SECONDS;
		}
		/*
		 * Never ask the user for a bin size on start up - the sound file datagram is
		 * created on demand, not automatically.
		 */
		getDatagramSettings().validDatagramSettings = true;
	}

	/**
	 * As well as the standard interval check, make sure the datagram has the right
	 * number of values in each line, in case it was made by an earlier version.
	 */
	@Override
	protected boolean isDatagramValid(OfflineDataMapPoint dmp, Datagram datagram) {
		if (super.isDatagramValid(dmp, datagram) == false) {
			return false;
		}
		if (datagram.getNumDataPoints() == 0) {
			return true;
		}
		return datagram.getDataPoint(0).getData().length == WaveformDatagramProvider.NUM_POINTS;
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
		int datagramSeconds = getDatagramSettings().datagramSeconds;
		long datagramMillis = datagramSeconds * 1000L;
		/*
		 * Round up rather than the +1 the standard DatagramManager uses. Sound data is
		 * continuous, so a file whose length is an exact multiple of the bin size would
		 * otherwise always end with an empty bin, which the data map draws as a gap.
		 */
		int nPoints = (int) ((endTime - startTime + datagramMillis - 1) / datagramMillis);
		if (nPoints < 1) {
			nPoints = 1;
		}
		if (nPoints > MAX_POINTS_PER_FILE) {
			System.err.printf("Sound file %s appears to be %d seconds long - not making a datagram for it\n",
					fileMapPoint.getName(), (endTime - startTime) / 1000);
			return;
		}

		double[] peak = new double[nPoints];
		double[] sumSquares = new double[nPoints];
		long[] sampleCount = new long[nPoints];

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
			double samplesPerBin = sampleRate * datagramSeconds;
			/*
			 * Anything past the end of the last bin can't be stored, so there's no point
			 * reading it. This only really matters when a map point has no proper end
			 * time, in which case there's a single bin and most of the file is of no use
			 * to us.
			 */
			long maxSamples = (long) (samplesPerBin * nPoints);
			long totalSamples = Math.min(maxSamples, (long) ((endTime - startTime) * sampleRate / 1000.));
			long sampleIndex = 0;
			double[] block;
			long lastUpdate = System.currentTimeMillis();
			while ((block = reader.readNextBlock()) != null) {
				int n = reader.getLastReadCount();
				for (int i = 0; i < n; i++) {
					int iBin = (int) ((sampleIndex + i) / samplesPerBin);
					if (iBin < 0 || iBin >= nPoints) {
						continue;
					}
					double v = block[i];
					double absV = v < 0 ? -v : v;
					if (absV > peak[iBin]) {
						peak[iBin] = absV;
					}
					sumSquares[iBin] += v * v;
					sampleCount[iBin]++;
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

		/*
		 * Build the datagram itself. Deliberately the same shape of loop as the standard
		 * DatagramManager so that getImageData sees exactly what it expects. Bins with no
		 * data are left as all zeros, which the data map draws as a gap.
		 */
		Datagram datagram = new Datagram(datagramSeconds);
		long currentStart = startTime;
		for (int iBin = 0; iBin < nPoints; iBin++) {
			DatagramDataPoint datagramPoint = new DatagramDataPoint(datagram, currentStart,
					currentStart + datagramMillis, WaveformDatagramProvider.NUM_POINTS);
			if (sampleCount[iBin] > 0) {
				float[] gramData = datagramPoint.getData();
				/*
				 * The data map treats an all zero point as 'no data' so that gaps between
				 * files aren't drawn as spikes down to zero. Digitally silent audio would
				 * look exactly the same, so floor the values at something too small to
				 * see but not actually zero.
				 */
				gramData[WaveformDatagramProvider.RMS_INDEX] = Math.max(Float.MIN_NORMAL,
						(float) Math.sqrt(sumSquares[iBin] / sampleCount[iBin]));
				gramData[WaveformDatagramProvider.PEAK_INDEX] = Math.max(Float.MIN_NORMAL, (float) peak[iBin]);
				datagramPoint.setData(gramData, (int) Math.min(sampleCount[iBin], Integer.MAX_VALUE));
			}
			datagram.addDataPoint(datagramPoint);
			currentStart += datagramMillis;
		}
		fileMapPoint.setDatagram(datagram);
		anyDatagram = true;

		if (++filesSinceSave >= SAVE_INTERVAL) {
			filesSinceSave = 0;
			offlineFileServer.saveSerialisedMap();
		}
		requestRepaint();
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
	 * Work out a sensible bin size from the length of the sound files and then start
	 * making the datagrams. Call this rather than updateDatagrams() when the user asks
	 * for a datagram to be created.
	 * 
	 * @param dataBlock the raw data block
	 */
	public void createDatagrams(PamDataBlock dataBlock) {
		int interval = chooseDatagramSeconds(dataBlock);
		if (interval != getDatagramSettings().datagramSeconds) {
			getDatagramSettings().datagramSeconds = interval;
		}
		updateDatagrams();
	}

	/**
	 * Choose a bin size which gives roughly TARGET_POINTS_PER_FILE points in a file of
	 * average length, clamped to something sensible.
	 * 
	 * @param dataBlock the raw data block
	 * @return bin size in seconds
	 */
	public int chooseDatagramSeconds(PamDataBlock dataBlock) {
		dataMap.OfflineDataMap dataMap = dataBlock.getOfflineDataMap(getOfflineDataStore());
		if (dataMap == null) {
			return DEFAULT_DATAGRAM_SECONDS;
		}
		long totalMillis = 0;
		int nPoints = 0;
		synchronized (dataMap) {
			java.util.Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
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
			return DEFAULT_DATAGRAM_SECONDS;
		}
		double meanSeconds = totalMillis / (double) nPoints / 1000.;
		int seconds = (int) Math.round(meanSeconds / TARGET_POINTS_PER_FILE);
		return Math.max(MIN_DATAGRAM_SECONDS, Math.min(MAX_DATAGRAM_SECONDS, seconds));
	}

	@Override
	protected void datagramsComplete() {
		filesSinceSave = 0;
		offlineFileServer.saveSerialisedMap();
	}

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
		dataMap.OfflineDataMap dataMap = dataBlock.getOfflineDataMap(getOfflineDataStore());
		if (dataMap == null) {
			return false;
		}
		synchronized (dataMap) {
			java.util.Iterator<OfflineDataMapPoint> it = dataMap.getListIterator();
			while (it.hasNext()) {
				OfflineDataMapPoint dmp = it.next();
				if (dmp instanceof DatagramPoint && ((DatagramPoint) dmp).getDatagram() != null) {
					anyDatagram = true;
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
