package clickDetector;

import java.util.ListIterator;

import pamScrollSystem.ViewLoadObserver;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;
//import staticLocaliser.StaticLocaliserControl;
//import staticLocaliser.StaticLocaliserProvider;
//import staticLocaliser.panels.AbstractLocaliserControl;
//import staticLocaliser.panels.ClickLocaliserControl;
import alarm.AlarmCounterProvider;
import alarm.AlarmDataSource;
import binaryFileStorage.BinaryStore;
import clickDetector.ClickClassifiers.ClickBlockSpeciesManager;
import clickDetector.dataSelector.ClickDataSelectCreator;
import clickDetector.offlineFuncs.OfflineClickLogging;
import clickDetector.tethys.ClickTethysDataProvider;
import clickDetector.toad.ClickTOADCalculator;
import dataMap.OfflineDataMap;
import fftManager.fftorganiser.FFTDataOrganiser;
import fftManager.fftorganiser.OrganisedFFTData;
import generalDatabase.SQLLogging;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.LocContents;
import PamUtils.PamUtils;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.AcousticDataBlock;
import PamguardMVC.DataAutomation;
import PamguardMVC.DataAutomationInfo;
import PamguardMVC.FFTDataHolderBlock;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.toad.TOADCalculator;

public class ClickDataBlock extends AcousticDataBlock<ClickDetection>  implements AlarmDataSource, 
	GroupedDataSource, OrganisedFFTData, FFTDataHolderBlock {

	protected ClickControl clickControl;
	
	private OfflineClickLogging offlineClickLogging;
	
	private boolean isViewer;
	
	private ClickBlockSpeciesManager clickBlockSpeciesManager;
	
	
	public ClickDataBlock(ClickControl clickControl, PamProcess parentProcess, int channelMap) {

		super(ClickDetection.class, clickControl.getDataBlockPrefix() + "Clicks", parentProcess, channelMap);

		this.clickControl = clickControl;
		addLocalisationContents(LocContents.HAS_BEARING);
		isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		//if (isViewer) {
			offlineClickLogging = new OfflineClickLogging(clickControl, this);
		//}
	}
//
//	@Override
//	public boolean getShouldLog(PamDataUnit pamDataUnit) {
//		return (super.getShouldLog(pamDataUnit) && clickControl.clickParameters.saveAllClicksToDB);
//	}

	private boolean firstLoad = true;

	private ClickDataSelectCreator clickDataSelectCreator;

	private ClickTOADCalculator clickTOADCalculator;

	private ClickTethysDataProvider clickTethysDataProvider;

	/**
	 * Click detector loading has to be a bit different to normal - first 
	 * data are loaded from the binary store, then a subset of these data
	 * are loaded from the Offline database. These database clicks are then
	 * matched to the data in the  
	 */
	@Override
	synchronized  public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
//		if (firstLoad) {
//			// make sure that offline events are already loaded. 
//			OfflineEventDataBlock eventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
//			if (eventDataBlock != null) {
//				eventDataBlock.loadViewerData(dataStart, dataEnd, loadObserver);
//				firstLoad = false;
//			}
//		}
		/*
		 * default load should always load the binary data. 
		 */
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		/*
		 * then force it to load the database stuff too. 
		 */
//		OfflineClickLogging offlineClickLogging = 
//			clickControl.getClickDetector().getClickDataBlock().getOfflineClickLogging();
//		if (offlineClickLogging != null) {
//			offlineClickLogging.loadViewerData(dataStart, dataEnd, null);
//		}
		
//		matchDatabaseAndBinaryData();
		return loadOk; 
	}

	@Override
	public boolean saveViewerData() {

		/**
		 * Save of data to database.
		 * The events MUST be saved first so that they get the correct event id's from the database. 
		 */
//		OfflineEventDataBlock offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
//		offlineEventDataBlock.saveViewerData();
//		boolean ok = offlineClickLogging.saveViewerData();
//		offlineClickLogging.checkSuspectEventTimes(offlineEventDataBlock);
//		
		/**
		 * Normal save of data to binary tore
		 */
		return super.saveViewerData();
	}

	@Override
	public OfflineDataMap getPrimaryDataMap() {
		/*
		 * Try really hard to get the binary data source, not the database one. 
		 */
		int n = getNumOfflineDataMaps();
		OfflineDataMap aMap;
		for (int i = 0; i < n; i++) {
			aMap = getOfflineDataMap(i);
			if (aMap.getOfflineDataSource().getClass() == BinaryStore.class) {
				return aMap;
			}
		}
		return super.getPrimaryDataMap();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.DATA_LOAD_COMPLETE:
//			matchClickstoEvents();

			// We've removed the OfflineClickLogger from TrackedClickDataBlock, so now that data
			// will no longer be stored in it's own database.  But it only ever held references
			// to Click Events anyway, and that information is now stored as a subtable.  At this
			// point in the code, all of that information should have been loaded and the subdetections
			// should have been properly reattached to the Click Events.  Therefore, we should be
			// able to recreate the TrackClickDataBlock here to maintain compatibility with any
			// code that relies on it.
			
			// clear TrackedClickDataBlock object
			PamDataBlock<PamDataUnit> trackedClicks = ((ClickDetector) this.getParentProcess()).getTrackedClicks();
			trackedClicks.clearAll();
//			long t = System.currentTimeMillis();
			// run through list of all Clicks - for any that have a superdetection (part of a click event)
			// add them to the TrackedClickDataBlock to maintain compatibility.  Make sure to pass in the
			// existing click UID so it doesn't get changed
//			for (int i=0; i<this.getUnitsCount(); i++) {
//				if (this.getDataUnit(i, REFERENCE_CURRENT).getSuperDetectionsCount()!=0) {
//					trackedClicks.addPamData(this.getDataUnit(i, REFERENCE_CURRENT), this.getDataUnit(i, REFERENCE_CURRENT).getUID());
//				}
//			}
			ListIterator<ClickDetection> cIt = this.getListIterator(0);
			while (cIt.hasNext()) {
				ClickDetection aClick = cIt.next();
				if (aClick.getSuperDetectionsCount()!=0) {
					trackedClicks.addPamData(aClick, aClick.getUID());
				}
				
			}
//			t = System.currentTimeMillis() - t;
//			System.out.printf("Time to sort out tracked clicks = %3.3fs\n", (double) t / 1000.);
		}
	}

//	private void matchClickstoEvents() {
////		System.out.println("Match clicks to events");		
//		/**
//		 * Now also load all the data from the offlineclickLogging
//		 * (which is not registered as the official logger for click 
//		 * data)
//		 */
//		offlineClickLogging.loadViewerData(getCurrentViewDataStart(), getCurrentViewDataEnd(), null);
//	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getUIDRepairLogging()
	 */
	@Override
	public SQLLogging getUIDRepairLogging() {
		return getOfflineClickLogging();
	}

	/**
	 * @return the offlineClickLogging
	 */
	public OfflineClickLogging getOfflineClickLogging() {
		return offlineClickLogging;
	}

	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		int r =  super.removeOldUnitsS(mastrClockSample);
		int n = getUnitsCount();
		ClickDetection click;
		if (n > 200) {
			ListIterator<ClickDetection> iter = getListIterator(n-50);
			while (iter.hasPrevious()) {
				click = iter.previous();
				if (click.hasComplexSpectrum() == false) {
					break; // probably no need to go further down the list 
				}
				click.freeClickMemory();
			}
		}
		return r;
	}


//	/**
//	 * Get the control panel for clicks for the staticlocaliser module.
//	 */
//	@Override
//	public AbstractLocaliserControl getSLControlDialog(StaticLocaliserControl staticLocaliserControl) {
//		AbstractLocaliserControl clickControl=new ClickLocaliserControl(staticLocaliserControl);
//		return clickControl;
//	}


	/* (non-Javadoc)
	 * @see alarm.AlarmDataSource#getAlarmCounter()
	 */
	@Override
	public AlarmCounterProvider getAlarmCounterProvider() {
		return clickControl.getAlarmCounterProvider();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
	@Override
	public synchronized  DataSelectorCreator getDataSelectCreator() {
		if (clickDataSelectCreator == null) {
			clickDataSelectCreator = new ClickDataSelectCreator(clickControl, this);
		}
		return clickDataSelectCreator;
	
	}

	@Override
	public GroupedSourceParameters getGroupSourceParameters() {
		return clickControl.getClickParameters().getGroupedSourceParameters();
	}
	
	/**
	 * Get a reference to the click control which owns this data block 
	 * @return click control. 
	 */
	public ClickControl getClickControl() {
		return this.clickControl; 
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getTOADCalculator()
	 */
	@Override
	public TOADCalculator getTOADCalculator() {
		if (clickTOADCalculator == null) {
			clickTOADCalculator = new ClickTOADCalculator(getClickControl());
		}
		return clickTOADCalculator;
	}

	@Override
	public FFTDataOrganiser getFFTDataOrganiser() {
		return clickControl.getFFTDataOrganiser();
	}

	@Override
	public double[] getDurationRange() {
		return clickControl.getClickDetector().getDurationRange();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.DataBlockForFFTDataHolder#getFFTparams()
	 */
	@Override
	public int[] getFFTparams() {
		int[] fftParams = new int[2];
		fftParams[0] = PamUtils.getMinFftLength(clickControl.getClickParameters().maxLength);
		fftParams[1] = fftParams[0]/2;
		return fftParams;
	}

	/**
	 * Set a forced click amplitude update. This means the next time getAmplitude is called 
	 * the amplitude value is recalculated using latest sensitivity/gain values. 
	 * TODO - this could be moved into AcousticDataBlock?
	 */
	public void setForceAmplitudeUpdate() {
		ListIterator<ClickDetection> cIt = this.getListIterator(0);
		while (cIt.hasNext()) {
			ClickDetection aClick = cIt.next();
			aClick.setForceAmpRecalc(true);
		}
	}

	@Override
	public DataBlockSpeciesManager<ClickDetection> getDatablockSpeciesManager() {
		if (clickBlockSpeciesManager == null) {
			clickBlockSpeciesManager = new ClickBlockSpeciesManager(clickControl, this);
		}
		return clickBlockSpeciesManager;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (clickTethysDataProvider == null) {
			clickTethysDataProvider = new ClickTethysDataProvider(tethysControl, this);
		}
		return clickTethysDataProvider;
	}

	@Override
	public DataAutomationInfo getDataAutomationInfo() {
		return new DataAutomationInfo(DataAutomation.AUTOMATIC);
	}


}
