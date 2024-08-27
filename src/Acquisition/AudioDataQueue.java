package Acquisition;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;

/**
 * Better management of new data list.
 * <p>
 * This is a queue that sits between individual acquisition systems and
 * the main Acquisition process. DAQ systems write data units into this list
 * in their own thread and data are read out in a different thread.
 * @author Doug Gillespie.
 *
 */
public class AudioDataQueue {

	private volatile List<RawDataUnit> newDataUnits;

	private long samplesIn[] = new long[PamConstants.MAX_CHANNELS];
	private long samplesOut[] = new long[PamConstants.MAX_CHANNELS];

	public AudioDataQueue() {
		newDataUnits = Collections.synchronizedList(new LinkedList<RawDataUnit>());
	}

	public synchronized void clearList() {
		newDataUnits.clear();
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			samplesIn[i] = samplesOut[i] = 0;
		}
	}

	public synchronized boolean hasData() {
		return !newDataUnits.isEmpty();
	}

	public synchronized int getQueueSize() {
		return newDataUnits.size();
	}

	public synchronized RawDataUnit removeOldest() {
		RawDataUnit ru = newDataUnits.remove(0);
		if (ru != null) {
			int chan = PamUtils.getSingleChannel(ru.getChannelBitmap());
			samplesOut[chan] += ru.getSampleDuration();
		}
		return ru;
	}

	public void addNewData(RawDataUnit newDataUnit) {
		addNewData(newDataUnit, PamUtils.getSingleChannel(newDataUnit.getChannelBitmap()));

	}

	public synchronized void addNewData(RawDataUnit newDataUnit, int channel) {
		samplesIn[channel] += newDataUnit.getSampleDuration();
		newDataUnits.add(newDataUnit);
	}

	/**
	 * Get the total number of samples in the data queue based on the difference between
	 * the last sample put in and the last taken out.
	 * @param channel channel number
	 * @return number of stored samples in queue
	 */
	public synchronized long getQueuedSamples(int channel) {
		return samplesIn[channel] - samplesOut[channel];
	}

	/**
	 * @return the samplesIn
	 */
	public long getSamplesIn(int channel) {
		return samplesIn[channel];
	}

	/**
	 * @return the samplesOut
	 */
	public long getSamplesOut(int channel) {
		return samplesOut[channel];
	}
}
