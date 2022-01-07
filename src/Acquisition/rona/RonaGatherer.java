package Acquisition.rona;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RequestCancellationObject;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;

/**
 * Gathers data from multiple threads unpacking Rona flac files
 * and put them in order and sends off to a datablock. 
 */
public class RonaGatherer {

	private PamRawDataBlock rawDataBlock;
	private int nChannels;
	private OfflineDataLoadInfo offlineLoadDataInfo;
	private int wantedChannels;
	private volatile int readyChannels;
	private volatile RawDataUnit[] readyUnits;
	private List<RawDataUnit> interThreadList = new LinkedList<>();

	public RonaGatherer(PamRawDataBlock rawDataBlock, int nChannels, OfflineDataLoadInfo offlineLoadDataInfo) {
		this.rawDataBlock = rawDataBlock;
		this.nChannels = nChannels;
		this.offlineLoadDataInfo = offlineLoadDataInfo;
		wantedChannels = PamUtils.makeChannelMap(nChannels);
		readyChannels = 0;
		readyUnits = new RawDataUnit[nChannels];
	}
	
	public boolean shouldCancel() {
		return (offlineLoadDataInfo.cancel);
	}

	synchronized public void addRawData(RawDataUnit newDataUnit, int channelOffset) {
		readyUnits[channelOffset] = newDataUnit;
		readyChannels |= 1<<channelOffset;
//		System.out.println(String.format("Add unit chan %d now have %d/%d", channelOffset, readyChannels, wantedChannels));
		if (readyChannels == wantedChannels) {
//			System.out.println("Adding raw data at " + PamCalendar.formatDateTime(readyUnits[0].getTimeMilliseconds()));
			long firstDataLen = readyUnits[0].getSampleDuration();
			for (int i = 1; i < nChannels; i++) {
				if (readyUnits[i].getSampleDuration() != firstDataLen) {
					double[] newData = Arrays.copyOf(readyUnits[i].getRawData(), (int) firstDataLen);
					readyUnits[i].setRawData(newData);
				}
			}
			
			
			for (int i = 0; i < nChannels; i++) {
//				rawDataBlock.addPamData(readyUnits[i]);
				interThreadList.add(readyUnits[i]);
			}
			readyChannels = 0;
//			this.notifyAll();
		}
	}
	
	public synchronized int readQueue() {
		int nRead = 0;
		while(interThreadList.size() > 0) {
			rawDataBlock.addPamData(interThreadList.remove(0));
			nRead++;
		}
		return nRead;
	}

	public boolean waitingDataUnits(int channelOffset) {
		return (readyChannels & 1<<channelOffset) != 0;
	}

}
