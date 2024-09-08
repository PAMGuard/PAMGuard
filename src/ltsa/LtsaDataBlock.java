package ltsa;

import java.util.ListIterator;

import PamguardMVC.LoadObserver;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class LtsaDataBlock extends FFTDataBlock {

	private boolean moreAveraged;
	
	public LtsaDataBlock(String dataName, PamProcess parentProcess, boolean moreAveraged) {
		super(dataName, parentProcess, 0, 1, 1);
		this.moreAveraged = moreAveraged;
		
		
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getOfflineData(PamguardMVC.PamObserver, PamguardMVC.PamObserver, long, long, boolean, PamguardMVC.RequestCancellationObject)
	 */
	@Override
	public synchronized int getOfflineData(OfflineDataLoadInfo offlineLoadDataInfo) {

		/**
		 * This would normally be used in the FFT data block to regenerate off-line data from original
		 * raw data, and if allowed to do it's own thing and raw data were available, would probably attempt to
		 * do just that now !
		 * Clearly, that is unnecessary since data are stored in the binary data store for the LTSA, so call 
		 * that instead !
		 */
		loadViewerData(offlineLoadDataInfo, null);
		return REQUEST_DATA_LOADED;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#orderOfflineData(PamguardMVC.PamObserver, PamguardMVC.LoadObserver, long, long, int, boolean)
	 */
	@Override
	public void orderOfflineData(PamObserver dataObserver,
			LoadObserver loadObserver, long startMillis, long endMillis, int loadKeepLayers,
			int interrupt, boolean allowRepeats) {
		/*
		 *As for getOfflineData, overrride this.
		 */
		loadViewerData(new OfflineDataLoadInfo(startMillis, endMillis), null);
		// now send all the data to the observer (the spectrogram) 
		// so that it redraws in the same way it does for real fft data. 
		synchronized (getSynchLock()) {
			ListIterator<FFTDataUnit> it = getListIterator(0);
			while(it.hasNext()) {
				dataObserver.addData(this, it.next());
			}
		}
		if (loadObserver != null) {
			loadObserver.setLoadStatus(REQUEST_DATA_LOADED);
		}
	}

	/**
	 * Override the normal FFTDataBlock getDataGain method here and just return 1.  If we use the FFTDataBlock
	 * version without running the normal FFTDataBlock initialization routines, we will always get 0 gain.
	 */
	@Override
	public double getDataGain(int iChan) {
		return 1;
	}

	
}
