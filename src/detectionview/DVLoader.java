package detectionview;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.RawDataUnavailableException;

public class DVLoader {

	private DVControl dvControl;

	private DVProcess dvProcess;

	private volatile boolean loading;

	private LoadWorker loadWorker;

	private Object loadSynch = new Object();

	public DVLoader(DVControl dvControl, DVProcess dvProcess) {
		super();
		this.dvControl = dvControl;
		this.dvProcess = dvProcess;
	}

	/**
	 * Clear and reload everything. 
	 * @param cancelCurrent
	 * @return
	 */
	public boolean reloadEverything(boolean cancelCurrent) {
		if (cancelCurrent) {
			cancelLoad(200);
		}
		else {
			if (isLoading()) {
				return false;
			}
		}
		dvProcess.getDvDataBlock().clearAll();
		synchronized (loadSynch) {
			LoadWorker newWorker = new LoadWorker();
			loadWorker = newWorker;
			newWorker.execute();		
		}

		return true;
	}

	private boolean isLoading() {
		return loadWorker != null;
	}

	/**
	 * Cancel any existing load process, waiting if asked for the
	 * process to end for up to waitMillis
	 * @param waitMillis
	 * @return true if not loading or loading ended within given wait. false if it seems to be loading still. 
	 */
	private boolean cancelLoad(long waitMillis) {
		loading = false;
		if (waitMillis > 0) {
			long stop = System.currentTimeMillis() + waitMillis;
			while (System.currentTimeMillis() < stop) {
				if (isLoading() == false) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}
		return isLoading();
	}

	private Integer doLoad(LoadWorker loadWorker) {
		PamDataBlock detectorData = dvProcess.getDetectorDataBlock();
		if (detectorData == null) {
			loadWorker.publish(LoadProgress.LOAD_FAILED, 0, 0, 0, "Error: No detector data selected");
			return 1;
		}
		/**
		 * will need to add data selectors to this sometime soon. 
		 */
		ArrayList<PamDataUnit> data = detectorData.getDataCopy();
		int nUnits = data.size();
		int nDone = 0;
		int nFail = 0;
		loadWorker.publish(LoadProgress.LOAD_RUNNING, nUnits, nDone, nFail);
		for (PamDataUnit aData : data) {
			if (loading == false) {
				break;
			}
			try {
				boolean ok = createDateClip(aData);
				if (ok) {
					nDone++;
				}
				else {
					nFail++;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			loadWorker.publish(LoadProgress.LOAD_RUNNING, nUnits, nDone, nFail);
		}
		loadWorker.publish(LoadProgress.LOAD_DONE, nUnits, nDone, nFail);
		return null;
	}

	/**
	 * Create a data clip by accessing the raw data which will be loaded
	 * from file in viewer mode and may still be available in real time 
	 * (if we ever allow that). 
	 * @param aData 
	 * @return true if data found and unit created. 
	 */
	private boolean createDateClip(PamDataUnit aData) {
		DVParameters params = dvControl.getDvParameters();
		PamRawDataBlock raw = dvProcess.getRawSourceDataBlock();
		if (raw == null) {
			return false;
		}
		float fs = raw.getSampleRate();
		long t1 = aData.getTimeMilliseconds() - (long) (params.preSeconds * 1000.);
		long t2 = aData.getEndTimeInMilliseconds() + (long) (params.postSeconds * 1000.);
//		raw.loadViewerData(t1, t2, null);
		double[][] rawData = null;
		try {
			RawDataUnit[] loadedData = raw.getAvailableSamples(t1-1000, t2-t1+2000, aData.getChannelBitmap(), true);
			rawData = raw.getSamplesForMillis(t1, t2-t1, aData.getChannelBitmap());
		}
		catch (RawDataUnavailableException e) {
			System.out.println(e.getMessage());
		}
		long startSamp = aData.getStartSample() - (long) (params.preSeconds/fs);
		long nSamp = (long) ((t2-t1) * fs / 1000);
		if (rawData != null && rawData[0] != null) {
			nSamp = rawData[0].length;
		}
		/*
		 * 	public DVDataUnit(long timeMilliseconds, long triggerMilliseconds, long startSample, int durationSamples,
			int channelMap, String fileName, String triggerName, double[][] rawData, float sourceSampleRate) {
		super(timeMilliseconds, triggerMilliseconds, startSample, durationSamples, channelMap, fileName, triggerName,
				rawData, sourceSampleRate);
		 */
		DVDataUnit dvDataUnit = new DVDataUnit(t1, aData.getTimeMilliseconds(), startSamp, (int) nSamp, aData.getChannelBitmap(), null, aData.getParentDataBlock().getDataName(), rawData, fs);
		dvProcess.getDvDataBlock().addPamData(dvDataUnit);
		return rawData != null;
	}

	private class LoadWorker extends SwingWorker<Integer, LoadProgress> {

		@Override
		protected Integer doInBackground() throws Exception {
			loading = true;
			Integer ans = doLoad(this);
			loading = false;
			return ans;
		}

		public void publish(int loadState, int nTotal, int nLoaded, int nFailed, String message) {
			super.publish(new LoadProgress(loadState, nTotal, nLoaded, nFailed, message));
		}

		public void publish(int loadState, int nTotal, int nLoaded, int nFailed) {
			super.publish(new LoadProgress(loadState, nTotal, nLoaded, nFailed));
		}

		@Override
		public void process(List<LoadProgress> chunks) {
			for (LoadProgress aP : chunks) {
				dvControl.updateLoadProgressObs(aP);
			}
		}

		@Override
		protected void done() {
			synchronized (loadSynch) {
				loadWorker = null;
			}
		}

	}

}
