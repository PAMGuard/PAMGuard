package whistleDetector;

import java.util.ListIterator;

import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;

public class WhistleEventDetector extends PamProcess {

	WhistleControl whistleControl;
	
	PamDataBlock<EventDataUnit> eventDataBlock;
	
	WhistleDetector whistleDetector;
	
	PamDataBlock<ShapeDataUnit> whistleDataBlock;
	
	EventDataUnit whistleEvent;
	
	long closeChecks = 0;
	
	WhistleRecorderTrigger whistleRecorderTrigger;
	
	WhistleEventLogger whistleEventLogger;
	
	public WhistleEventDetector(WhistleControl whistleControl, WhistleDetector whistleDetector) {
		
		super(whistleControl, whistleDetector.whistleDataBlock);
		
		this.setProcessName("Whistle Event Detector");
		
		this.whistleControl = whistleControl;
		
		this.whistleDetector = whistleDetector;
		
		addOutputDataBlock(eventDataBlock = new PamDataBlock<EventDataUnit>(EventDataUnit.class, "Whistle Events", 
				this, whistleControl.whistleParameters.channelBitmap)); 
		eventDataBlock.setNaturalLifetime(60);
		eventDataBlock.setOverlayDraw(new WhistleEventGraphics(this));
		eventDataBlock.setPamSymbolManager(new StandardSymbolManager(eventDataBlock, WhistleEventGraphics.defaultSymbol, true));
		eventDataBlock.SetLogging(whistleEventLogger = new WhistleEventLogger(this, eventDataBlock));
		eventDataBlock.setRecordingTrigger(whistleRecorderTrigger = new WhistleRecorderTrigger());
		
		whistleDataBlock = whistleControl.whistleDetector.whistleDataBlock;
		whistleDataBlock.addObserver(this);
		
		
	}


	@Override
	public void clearOldData() {
		super.clearOldData();
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		if (o == whistleDataBlock) {
			return (long) (whistleControl.whistleParameters.eventIntegrationTime * 1000.);
		}
		return 0;
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (o == whistleDataBlock) {
			// check events.
			newWhistle((ShapeDataUnit) arg);
		}
		else if (o == whistleDetector.getParentDataBlock() && ++closeChecks%100 == 0)  {
			// check to see if events shoudl close
			if (checkCloseEvent(arg.getTimeMilliseconds())) {
				closeEvent();
			}
		}
		
	}
	
	private void newWhistle(ShapeDataUnit dataUnit) {
		if (whistleEvent != null) {
			if (checkCloseEvent(dataUnit.getTimeMilliseconds())) {
				closeEvent();
			}
			else {
				whistleEvent.addWhistle(dataUnit);
			}
			return;
		}
		// otherwise, see if we should start a new event.
		int totalUnits = whistleDataBlock.getUnitsCount();
		int unitsCount = 0;
		ShapeDataUnit shapeUnit, lastUnit = null;
		synchronized (whistleDataBlock.getSynchLock()) {
			ListIterator<ShapeDataUnit> wslIterator = whistleDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			long firstOkTime = dataUnit.getTimeMilliseconds() - (long) (whistleControl.whistleParameters.eventIntegrationTime * 1000);
			if (totalUnits >= whistleControl.whistleParameters.eventMinWhistleCount) {
				// do a more serious check of units in the last integration time before going 
				// ahead and starting an event 
				totalUnits = 0;
				while (wslIterator.hasPrevious()) {
					shapeUnit = wslIterator.previous();
					totalUnits++;
					if (shapeUnit.getTimeMilliseconds() < firstOkTime) {
						break;
					}
					lastUnit = shapeUnit;
				}
				shapeUnit = whistleDataBlock.getFirstUnitAfter(firstOkTime);
				lastUnit = whistleDataBlock.getLastUnit();

				if (totalUnits >= whistleControl.whistleParameters.eventMinWhistleCount) {
					whistleEvent = new EventDataUnit(shapeUnit);
					while (wslIterator.hasNext()) {
						shapeUnit = wslIterator.next();
						whistleEvent.addWhistle(shapeUnit);
					}
					eventDataBlock.addPamData(whistleEvent);
				}
			}
		}
	}
	
	private boolean checkCloseEvent(long timeNow) {
		if (whistleEvent == null) return false;
		if (timeNow - whistleEvent.endTimeMillis > (long) (whistleControl.whistleParameters.eventMaxGapTime * 1000)) {
			return true;
		}
		return false;
	}
	
	private void closeEvent() {
		whistleEvent.setStatus(EventDataUnit.STATUS_CLOSED);
		whistleEventLogger.logData(whistleEvent);
		whistleEvent = null;		
	}

	@Override
	public void pamStart() {
		// also subscribe to the source data to get regular 
		// hits so as to check closing on events. #
//		now done from whistleDetector
//		whistleDetector.getParentDataBlock().addObserver(this);
		
	}
	
	@Override
	public void pamStop() {
		if (whistleEvent != null) closeEvent();
	}

	class WhistleRecorderTrigger extends RecorderTrigger {

		public WhistleRecorderTrigger() {
			super(eventDataBlock);
			// TODO Auto-generated constructor stub
		}

		RecorderTriggerData recorderTriggerData = new RecorderTriggerData(whistleControl.getUnitName() + " Events",
				30, 60);
		
		@Override
		public RecorderTriggerData getDefaultTriggerData() {
			return recorderTriggerData;
		}

		@Override
		public boolean triggerDataUnit(PamDataUnit dataUnit, RecorderTriggerData rtData) {
			return true;
		}
		
	}
}
