package ltsa;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class LtsaProcess extends PamProcess {

	private LtsaDataBlock ltsaDataBlock;
	
	private LtsaDataBlock longerLtsaDataBlock;

	protected FFTDataBlock sourceDataBlock;

	private LtsaControl ltsaControl;

	private ChannelProcess[] channelProcesses = new ChannelProcess[PamConstants.MAX_CHANNELS];

	public LtsaProcess(LtsaControl ltsaControl) {
		super(ltsaControl, null);
		this.ltsaControl = ltsaControl;
		ltsaDataBlock = new LtsaDataBlock(ltsaControl.getUnitName(), this, false);
		ltsaDataBlock.setBinaryDataSource(new LtsaBinaryDataSource(ltsaControl, ltsaDataBlock));
		ltsaDataBlock.setDatagramProvider(new LtsaDatagramProvider(this));
		addOutputDataBlock(ltsaDataBlock);

//		longerLtsaDataBlock = new LtsaDataBlock("Longer LTSA", this, true);
//		addOutputDataBlock(longerLtsaDataBlock);
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		long now = PamCalendar.getTimeInMillis();
		int fftLen;
		if (sourceDataBlock == null) {
			return;
		}
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (channelProcesses[i] != null) {
				channelProcesses[i].prepare(now, sourceDataBlock.getFftLength());
			}
		}		
		getLtsaDataBlock().setFftLength(sourceDataBlock.getFftLength());
		int  ffthop = (int) (ltsaControl.ltsaParameters.intervalSeconds * getSampleRate());
		getLtsaDataBlock().setFftHop(ffthop);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#flushDataBlockBuffers(long)
	 */
	@Override
	public boolean flushDataBlockBuffers(long maxWait) {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (channelProcesses[i] != null) {
				channelProcesses[i].closePeriod();
			}
		}
		
		return super.flushDataBlockBuffers(maxWait);
	}

	@Override
	public void setupProcess() {
		super.setupProcess();
		sourceDataBlock = (FFTDataBlock) PamController.getInstance().getFFTDataBlock(ltsaControl.ltsaParameters.dataSource);
		setParentDataBlock(sourceDataBlock);
		if (sourceDataBlock == null) {
			return;
		}
//		getLtsaDataBlock().setChannelMap(ltsaControl.ltsaParameters.channelMap);
		getLtsaDataBlock().sortOutputMaps(sourceDataBlock.getChannelMap(), sourceDataBlock.getSequenceMapObject(), ltsaControl.ltsaParameters.channelMap);
		getLtsaDataBlock().setFftLength(sourceDataBlock.getFftLength());
		int  nFFT = (int) (ltsaControl.ltsaParameters.intervalSeconds * getSampleRate());
		getLtsaDataBlock().setFftHop(nFFT);

		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((ltsaControl.ltsaParameters.channelMap & (1<<i)) != 0) {
				if (channelProcesses[i] == null) {
					channelProcesses[i] = new ChannelProcess(i);
				}
			}
			else {
				channelProcesses[i] = null;
			}
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
//		if ((arg.getChannelBitmap() & ltsaControl.ltsaParameters.channelMap) == 0) {
		if ((arg.getSequenceBitmap() & ltsaControl.ltsaParameters.channelMap) == 0) {
			return;
		}
//		int aChan = PamUtils.getSingleChannel(arg.getChannelBitmap());
		int aChan = PamUtils.getSingleChannel(arg.getSequenceBitmap());
		channelProcesses[aChan].newData((FFTDataUnit) arg);
	}

	/**
	 * @param ltsaDataBlock the ltsaDataBlock to set
	 */
	public void setLtsaDataBlock(LtsaDataBlock ltsaDataBlock) {
		this.ltsaDataBlock = ltsaDataBlock;
	}

	/**
	 * @return the ltsaDataBlock
	 */
	public LtsaDataBlock getLtsaDataBlock() {
		return ltsaDataBlock;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getOfflineData(PamguardMVC.dataOffline.OfflineDataLoadInfo)
	 */
	@Override
	public int getOfflineData(OfflineDataLoadInfo offlineLoadInfo) {
		/**
		 * Because the LTSA data get loaded up like the spectrogram data, this need
		 * to override so that it can do it's own thing with binary data rather than 
		 * trying to go back to the raw data to recompute. 
		 */
		return ltsaDataBlock.getOfflineData(offlineLoadInfo);
	}

	class ChannelProcess {

		private int channel;

		/**
		 * times all in milliseconds. 
		 */
		private long interval, currentStart, currentEnd;
		
		private long startSample, lastSample;

		/**
		 * Something to accumulate the average data in
		 */
		double[] meanFftData;

		/**
		 * Count of it all. 
		 */
		int nFFT;

		int halfFFTLength;

		/**
		 * @param channel
		 */
		public ChannelProcess(int channel) {
			super();
			this.channel = channel;
		}

		public void prepare(long timeMillis, int fftLength) {
			interval = ltsaControl.ltsaParameters.intervalSeconds * 1000;
			currentStart = timeMillis / interval;
			currentStart *= interval;
			currentEnd = currentStart + interval;
			halfFFTLength = fftLength/2;
			meanFftData = new double[halfFFTLength];
		}

		public void newData(FFTDataUnit fftDataUnit) {
			ComplexArray fftData = fftDataUnit.getFftData();
			if (meanFftData == null || meanFftData.length != fftData.length()) {
				prepare(fftDataUnit.getTimeMilliseconds(), fftData.length()*2);
			}
			if (fftDataUnit.getTimeMilliseconds() >= currentEnd) {
				closePeriod();
			}
			/**
			 * accumulate ...
			 */
			for (int i = 0; i < halfFFTLength; i++) {
				meanFftData[i] += fftData.magsq(i);
			}
			lastSample = fftDataUnit.getStartSample();
			if (nFFT == 0) {
				startSample = lastSample;
			}
			lastSample += fftDataUnit.getSampleDuration();
			nFFT++;
		}

		/**
		 * Called when it's time to finish off an averaging period.<br>
		 * Average the data, and reset the counters, etc.  
		 */
		private void closePeriod() {
			if (nFFT == 0) {
				return;
			}
			LtsaDataUnit newDataUnit = new LtsaDataUnit(currentStart, currentEnd, nFFT, 1<<channel, startSample, lastSample-startSample);
			newDataUnit.sortOutputMaps(sourceDataBlock.getChannelMap(), sourceDataBlock.getSequenceMapObject(), 1<<channel);

			ComplexArray fftData = newDataUnit.getFftData();
			if (fftData == null) {
				fftData = new ComplexArray(halfFFTLength);
			}
			for (int i = 0; i < halfFFTLength; i++) {
				fftData.set(i, Math.sqrt(meanFftData[i]/nFFT), 0);
			}
			newDataUnit.setFftData(fftData);
			getLtsaDataBlock().addPamData(newDataUnit);
			
			currentStart = currentEnd;
			currentEnd += interval;
			for (int i = 0; i < halfFFTLength; i++) {
				meanFftData[i] = 0;
			}
			nFFT = 0;
		}

	}
}
