package clickDetector;

import java.io.Serializable;
import java.util.ListIterator;

import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser;
import Localiser.detectionGroupLocaliser.DetectionGroupLocaliser2;
import Localiser.detectionGroupLocaliser.GroupDetection;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.AbstractLocalisation;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import SoundRecorder.RecorderControl;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;
import clickDetector.clicktrains.ClickTrainIdParams;
import clickDetector.localisation.ClickGroupLocaliser;
import clickDetector.offlineFuncs.OfflineClickLogging;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.tdPlots.ClickEventSymbolManager;

public class ClickTrainDetector extends PamProcess implements PamSettings {

	private ClickControl clickControl;

	private ClickDetector clickDetector;

	private PamDataBlock<ClickDetection> clickDataBlock;

//	private ClickTrainDataBlock clickTrains;

	private OfflineEventDataBlock newClickTrains;

	private CTRecorderTrigger ctRecorderTrigger;

//	private final double minAngleForFit = 5 * Math.PI / 180.;

	//	ClickTrainLogger clickTrainLogger;

	private ClickGroupLocaliser detectionGroupLocaliser;

	private ClickTrainIdParams clickTrainIdParameters = new ClickTrainIdParams();

	public ClickTrainDetector(ClickControl clickControl, PamDataBlock<ClickDetection> clickDataBlock) {

		super(clickControl, clickDataBlock, "Click Train Detector");

		this.clickControl = clickControl;

		this.clickDetector = clickControl.getClickDetector();

		this.clickDataBlock = clickDataBlock;

		PamSettingManager.getInstance().registerSettings(this);

		clickDataBlock.addObserver(this);

//		addOutputDataBlock(clickTrains = new ClickTrainDataBlock(clickControl, clickControl.getUnitName() + " Click Trains", 
//				this, clickControl.clickParameters.channelBitmap));
//		clickTrains.setLocalisationContents(AbstractLocalisation.HAS_BEARING | AbstractLocalisation.HAS_RANGE);
//		clickTrains.addObserver(this);
//		ClickTrainGraphics clickTrainGraphics = new ClickTrainGraphics(clickControl, clickTrains);
//		clickTrains.setOverlayDraw(clickTrainGraphics);

//		ctRecorderTrigger = new CTRecorderTrigger(clickTrains);
//		clickTrains.setRecordingTrigger(ctRecorderTrigger);
		//		clickTrains.SetLogging(new OfflineClickLogging(clickControl, clickTrains));

		newClickTrains = new OfflineEventDataBlock(clickControl.getUnitName() + " New Click Trains", 
				clickControl.clickDetector, clickControl.clickParameters.getChannelBitmap());
		newClickTrains.setPamSymbolManager(new ClickEventSymbolManager(newClickTrains));

//		newClickTrains = new ClickTrainDataBlock(clickControl, clickControl.getUnitName() + " New Click Trains", 
//				this, clickControl.clickParameters.channelBitmap);
		newClickTrains.addObserver(this);
		//		addOutputDataBlock(newClickTrains);



		//		clickTrains.setOverlayDraw(new ClickTrainLocalisationGraphics(clickControl, clickTrains));
		//		clickTrains.SetLogging(clickTrainLogger = new ClickTrainLogger(clickTrains, clickControl, this));

		detectionGroupLocaliser = new ClickGroupLocaliser(clickControl);
	}

	public void clearAllTrains()
	{
		//clickTrains.();
	}
	
	/**
	 * Quick convenience method for getting the click train datablock. 
	 * @return click train datablock. 
	 */
	private OfflineEventDataBlock getClickTrainDataBlock() {
		return clickControl.clickDetector.getOfflineEventDataBlock();
	}
	
	/**
	 * Convenience method for getting the tracked click data block. 
	 * @return tracked click data block. 
	 */
	public PamDataBlock<PamDataUnit> getTrackedClickDataBlock() {
		return clickControl.clickDetector.getTrackedClicks();
	} 
	
	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		if (clickTrainIdParameters.runClickTrainId == false) return 0;
		if (o == getClickTrainDataBlock()) return (long) (clickTrainIdParameters.iciRange[1] * 1000);//3600 * 1000;
		if (o == newClickTrains) return (long) (clickTrainIdParameters.iciRange[1] * 1000);	
		else if (o == clickDataBlock) return 60000; // 60s of data for now
		return 0;		
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (clickTrainIdParameters.runClickTrainId == false) return;
		if (o == clickDataBlock){
			ClickDetection click = (ClickDetection) arg;
			if (click.dataType != ClickDetection.CLICK_CLICK) {
				return;
			}
			if (click.getLocalisation() == null) {
				return;
			}
			//			synchronized (clickTrains) {
			//				synchronized (newClickTrains) {
			processClick(click);
			closeOldTrains(getClickTrainDataBlock(), arg.getTimeMilliseconds());
			closeOldTrains(newClickTrains, arg.getTimeMilliseconds());
			//				}
			//			}
		}
	}

	protected double closeTrainTime() {
		return clickTrainIdParameters.iciRange[1] * 1000;
	}

	private void closeOldTrains(OfflineEventDataBlock offlineEventDataBlock, long now) {
		// close trains that have not had a click added for > 10s;
		ClickTrainDetection aTrain;
		//		long now = PamCalendar.getTimeInMillis();
		synchronized  (offlineEventDataBlock) {
			ListIterator<OfflineEventDataUnit> ctdIterator = offlineEventDataBlock.getListIterator(0);
			while (ctdIterator.hasNext()) {
				OfflineEventDataUnit nextTrain = ctdIterator.next();
				if (ClickTrainDetection.class.isAssignableFrom(nextTrain.getClass()) == false) {
					continue;
				}
				aTrain = (ClickTrainDetection) nextTrain;
				//			if (aTrain.getTrainStatus() == ClickTrain.STATUS_BINME) continue;
				if (now - aTrain.getLastClick().getTimeMilliseconds() > closeTrainTime()){
					if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_STARTING) {
						ctdIterator.remove();
						//					dataBlock.remove(aTrain);
						aTrain.setTrainStatus(ClickTrainDetection.STATUS_BINME);	
					}
					else if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_OPEN){
						aTrain.setTrainStatus(ClickTrainDetection.STATUS_CLOSED);
						offlineEventDataBlock.updatePamData(aTrain, now);
					}
				}
			}
		}

	}

	protected ClickTrainDetection processClick(ClickDetection newClick) {
		ClickTrainDetection clickTrain = matchClickIntoGroup(newClick);
		if (clickTrain == null) return null;
		/*
		 * Here we should localise the event and write it's data to the database. 
		 * Seems like this is already happening elsewhere though ! Beware too many 
		 * database updates !
		 */
		long now = PamCalendar.getTimeInMillis();
		long updateGap = now - clickTrain.getLastUpdateTime();
		boolean bigAngle = (clickTrain.getMaxAngle() - clickTrain.getMinAngle()) > Math.toRadians(clickTrainIdParameters.minAngleChange);
		if (updateGap > clickTrainIdParameters.minUpdateGap * 1000) {

			if (bigAngle) {
				localiseClickTrain(clickTrain);
			}
			getClickTrainDataBlock().updatePamData(clickTrain, now);

		}
		
		
		//		else {
		//			setBearingOnlyInfo(clickTrain);
		//		}
		return clickTrain;
	}

	protected ClickTrainDetection matchClickIntoGroup(ClickDetection newClick) {

		// look back in time for the most likely group of clicks. If there is no reasonable
		// group, create a new one - it may die quite soon if no clicks are added to it. 

		// make local copy of clickParameters.
		ClickParameters clickParameters = clickControl.clickParameters;

		ClickTrainDetection bestTrain = null;
		double bestScore = 0;
		double newScore;
		ClickTrainDetection aTrain;

		boolean startupTrain = false;
		ListIterator<OfflineEventDataUnit> ctdIterator;
		synchronized (getClickTrainDataBlock().getSynchLock()) {
			ctdIterator = getClickTrainDataBlock().getListIterator(0);
			while (ctdIterator.hasNext()) {
				OfflineEventDataUnit nextTrain = ctdIterator.next();
				if (ClickTrainDetection.class.isAssignableFrom(nextTrain.getClass()) == false) {
					continue;
				}
				aTrain = (ClickTrainDetection) nextTrain;
				//System.out.println("test train");
				if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_CLOSED) continue;
				//System.out.println("test train not closed");
				if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_BINME){
					continue;
				}
				//System.out.println("test train not for bin");
				newScore = aTrain.testClick(newClick);
				if (newScore > bestScore) {
					bestScore = newScore;
					bestTrain = aTrain;
					startupTrain = false;
				}
			}
		}
		//and do the same with new click trains
		synchronized (newClickTrains.getSynchLock()) {
			ctdIterator = newClickTrains.getListIterator(0);
			while (ctdIterator.hasNext()) {
				OfflineEventDataUnit nextTrain = ctdIterator.next();
				if (ClickTrainDetection.class.isAssignableFrom(nextTrain.getClass()) == false) {
					continue;
				}
				aTrain = (ClickTrainDetection) nextTrain;
				//System.out.println("test train");
				if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_CLOSED) continue;
				//System.out.println("test train not closed");
				if (aTrain.getTrainStatus() == ClickTrainDetection.STATUS_BINME){
					continue;
				}
				//System.out.println("test train not for bin");
				newScore = aTrain.testClick(newClick);
				if (newScore > bestScore) {
					bestScore = newScore;
					bestTrain = aTrain;
					startupTrain = true;
				}
			}
		}
		if (bestTrain != null) {
			// see if the quality of the fit was good enough ...
			bestTrain.addSubDetection(newClick);
			/*
			 * Every time a click is added to a train, a new message is sent to 
			 * the recorders. If they are not recording, they will start, taking 
			 * the specified buffer. IF they are already recording, they will continue
			 * until the specified time has elapsed after the last click update. So
			 * if lots of clicks keep getting added to trains, then you get one long recording.
			 * 
			 */
			if (startupTrain && (bestTrain.getTrainStatus() == ClickTrainDetection.STATUS_OPEN)) {
				newClickTrains.remove(bestTrain);
				bestTrain.setComment("Automatic Click Train Detection");
				/**
				 * Add to database as soon as it's ready so that it picks up a new database index. 
				 * This has to happen so that clicks can be assigned the right colour before they 
				 * are displayed. 
				 * Before doing this reset it's UID to zero so that it picks up the correct UID
				 * from the main datablock. 
				 */
				bestTrain.setUID(0);
				bestTrain.setParentDataBlock(null);
				getClickTrainDataBlock().addPamData(bestTrain);
				int newEventId = bestTrain.getDatabaseIndex();
				if (newEventId == 0) {
					newEventId = getClickTrainDataBlock().getLastEventId();
				}
				if (newEventId > 0) {
					bestTrain.setEventId(newEventId);
					bestTrain.setColourIndex(newEventId);
//					System.out.printf("New click event: Change click id from %d to %d\n", newClick.eventId, dbIndex);
//					newClick.eventId = newEventId;
				}
				// go through the clicks in that train and add them to the tracked clicks data block. 
				int nSub = bestTrain.getSubDetectionsCount();
				for (int i = 0; i < nSub; i++) {
					PamDataUnit aClick = bestTrain.getSubDetection(i);
//					aClick.setEventId(bestTrain.getEventId());
					getTrackedClickDataBlock().addPamData(aClick);
				}
			}
			else {
//				newClick.setEventId(bestTrain.getEventId());
				if (bestTrain.getTrainStatus() == ClickTrainDetection.STATUS_OPEN) {
					getTrackedClickDataBlock().addPamData(newClick);
				}
			}
			// otherwise, set bestTrain back to null
		}
		if (bestTrain == null) {
//			if (newClick.clickNumber > 10) {
//				matchClickIntoGroup(newClick);
//			}
			// sniff around to see if there is another click in about the right place
			// to start a train and begin a train with two clicks.
			PamDataBlock<ClickDetection> clickBlock = clickDetector.getClickDataBlock();
			ClickDetection lastClick =  newClick;
			double lastClickAngle = lastClick.getAngle();
			ClickDetection aClick;
			double dT;
			double clickAngle;
			synchronized (clickBlock.getSynchLock()) {
				ListIterator<ClickDetection> clickIterator = clickBlock.getListIterator(PamDataBlock.ITERATOR_END);
				while (clickIterator.hasPrevious()) {
					//			int iUnit = clickBlock.getUnitsCount() - 1;
					//			while (--iUnit >= 0) {
					aClick = clickIterator.previous();
					if (aClick == newClick) {
						continue; // this will happen the first time. 
					}
					if (aClick.dataType != ClickDetection.CLICK_CLICK) {
						continue;
					}
//					if (aClick.eventId != 0) continue; // already assigned
					dT = (newClick.getTimeMilliseconds() - aClick.getTimeMilliseconds()) / 1000.;
					if (dT > clickTrainIdParameters.iciRange[1]) break;
					clickAngle = aClick.getAngle();
					if (Math.abs(clickAngle - lastClickAngle) > clickTrainIdParameters.okAngleError) continue;
					if (dT < clickTrainIdParameters.iciRange[0] || dT > clickTrainIdParameters.iciRange[1]) continue;
					// if it gets here, its a good enough click to start a train
					bestTrain = new ClickTrainDetection(clickControl, aClick);
					//newDataUnit = clickTrains.getNewUnit(aClick.startSample, 0, aClick.channelList);
					bestTrain.addSubDetection(newClick);
					newClickTrains.addPamData(bestTrain);

				}
			}
		}
		return bestTrain;
	}

	protected void localiseClickTrain(ClickTrainDetection clickTrain) {
		/*
		 * clear existing localisation information, then let it re-add the data.
		 * There is a chance that a click train will go from good (has a track point) to 
		 * bad (not track) if the track wobbles badly, so this will alow the system to remove lousy 
		 * detections - should get rid of LR ambiguities if boat turns and one solution becomes impossible 
		 */ 
		
		
//		clickTrain.getGroupDetectionLocalisation().clearFitData();
		GroupLocalisation groupLoc = detectionGroupLocaliser.runModel(clickTrain, clickControl.getClickParameters().clickLocParams);
	}

	class CTRecorderTrigger extends RecorderTrigger {

		public CTRecorderTrigger(PamDataBlock dataBlock) {
			super(dataBlock);
			// TODO Auto-generated constructor stub
		}

		RecorderTriggerData recorderTriggerData = new RecorderTriggerData(clickControl.getUnitName() + " Click Trains", 
				10, 120);

		public RecorderTriggerData getDefaultTriggerData() {
			return recorderTriggerData;
		}

		@Override
		public boolean triggerDataUnit(PamDataUnit dataUnit, RecorderTriggerData rtData) {
			// TODO Auto-generated method stub
			return true;
		}

	}

	@Override
	public void pamStart() {
		newClickTrains.clearAll();
	}

	@Override
	public void pamStop() {
		/**
		 * Close any open click trains. 
		 */
		closeOldTrains(getClickTrainDataBlock(), Long.MAX_VALUE);
		closeOldTrains(newClickTrains, Long.MAX_VALUE);
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, false);
	}

//	/**
//	 * @return the clickTrains
//	 */
//	public ClickTrainDataBlock getClickTrains() {
//		return clickTrains;
//	}

	@Override
	public String getUnitName() {
		return clickControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Click Train Detector";
	}

	@Override
	public Serializable getSettingsReference() {
		return clickTrainIdParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ClickTrainIdParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			clickTrainIdParameters = ((ClickTrainIdParams) pamControlledUnitSettings.getSettings()).clone();
			return clickTrainIdParameters != null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public ClickGroupLocaliser getDetectionGroupLocaliser() {
		return detectionGroupLocaliser;
	}

	/**
	 * @return the clickTrainIdParameters
	 */
	public ClickTrainIdParams getClickTrainIdParameters() {
		return clickTrainIdParameters;
	}

	/**
	 * @param clickTrainIdParameters the clickTrainIdParameters to set
	 */
	public void setClickTrainIdParameters(ClickTrainIdParams clickTrainIdParameters) {
		this.clickTrainIdParameters = clickTrainIdParameters;
	}

}
