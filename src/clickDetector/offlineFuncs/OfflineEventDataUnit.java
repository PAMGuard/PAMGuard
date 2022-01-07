package clickDetector.offlineFuncs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import clickDetector.ClickDetection;
import GPS.GpsData;
import Localiser.detectionGroupLocaliser.GroupDetection;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * OfflineEventDataUnit replicates the RainbowClick functionality in 
 * OfflineRCEvent. 
 * <p>
 * A lot of the functionality required to associate clicks with 
 * an event is already wrapped up in the base classes of PamDataBlock 
 * and PamDataUnit. 
 * <p>
 * March 2016. Am merging online and offline click tracking functionality, so this has now been declared 
 * abstract to stop them being created directly and TrackedClickGroup has been made to subclass this, so that 
 * both now have the same functionality. May at a later date remove trackedclickGroup and go back to just having the
 * one class of OfflineEventDataUnit - or better, EventDataUnit.
 * 
 * 
 * @author Doug Gillespie
 *
 */
public class OfflineEventDataUnit extends GroupDetection<PamDataUnit> {

	private String eventType;
	
	private String comment;
	
//	private int nClicks;
	
	private boolean isViewer;
	
	/**
	 * Flag to say that event times may be a bit dodgy. 
	 * This will happen if an event is only partially loaded 
	 * into memory when some clicks are deleted and it may 
	 * be impossible to work out what the true start and end times
	 * are. 
	 */
	private boolean suspectEventTimes = false;
	
//	private int eventNumber; // will use the database index instead 
	
	/**
	 * specific colour index - can be null.
	 */
	private Integer colourIndex;
	
	/**
	 * Minimum number of animals in event
	 * <p>(can be null)
	 */
	private Short minNumber;
	/**
	 * Best estimate of number of animals in event
	 * <p>(can be null)
	 */
	private Short bestNumber;
	/**
	 * Maximum number of animals in event
	 * <p>(can be null)
	 */
	private Short maxNumber;
	
	public OfflineEventDataUnit(ClickDetection firstClick) {
		super(firstClick.getTimeMilliseconds(),firstClick.getChannelBitmap(),firstClick.getStartSample(),firstClick.getSampleDuration());
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
//		super("Online", 0, firstClick);
//		this.eventType 
		setEventId(firstClick.getEventId());
		setColourIndex(firstClick.getEventId());
		addSubDetection(firstClick);
		// TODO Auto-generated constructor stub
	}


	public OfflineEventDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
//		super("Online", 0, null);
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		this.setTimeMilliseconds(timeMilliseconds);
		this.setChannelBitmap(channelBitmap);
		this.setStartSample(startSample);
		this.setSampleDuration(duration);
	}
	
	/**
	 * Called whena  new event is created. 
	 * @param eventType
	 * @param colourIndex
	 * @param firstClick
	 */
	public OfflineEventDataUnit(String eventType, Integer nominalEventId, ClickDetection firstClick) {
//		super(firstClick.getTimeMilliseconds(), firstClick.getChannelBitmap(), 
//				firstClick.getStartSample(), firstClick.getDuration());
		super(0,0,0,0);
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		this.eventType = eventType;
		if (nominalEventId != null) {
			setEventId(nominalEventId);
			setColourIndex(nominalEventId);
		}
		if (firstClick != null) {
			this.addSubDetection(firstClick);
		}
	}

//	/**
//	 * Add a list of clicks to an event. 
//	 * 
//	 * @param markedClicks List of marked clicks. 
//	 */
//	public void addClicks(List<PamDataUnit> markedClicks) {
//		if (markedClicks == null) {
//			return;
//		}
//		for (int i = 0; i < markedClicks.size(); i++) {
//			addSubDetection(markedClicks.get(i));
//		}
//	}
	
//	/**
//	 * Add a sub detection with the option of not increasing the click
//	 * count. This is used when setting up data as it's read from the 
//	 * database, not when adding new clicks under normal operation
//	 * @param subDetection click to add to event
//	 * @param countClick true if click count should be increased. 
//	 */
//	public void addSubDetection(PamDataUnit subDetection, boolean countClick) {
//		if (addSubDetection(subDetection) > 0 && countClick == false) {
//			nClicks--;
//		}
//	}
	
//	/**
//	 * Add a new click to the event. As each click is added, 
//	 * some checks are done to make sure that the click is not
//	 * already part of some other event, and if all events are 
//	 * removed from some other event, make sure that that other 
//	 * event get's deleted. 
//	 * @param subDetection a new click to add. 
//	 *  
//	 */
//	@Override
//	public int addSubDetection(PamDataUnit subDetection) {
//		/*
//		 * First check that the click is not already part of this event. 
//		 * If it is, then it's super detection will point at this, so 
//		 * just return true and get on with it. 
//		 */
////		if (subDetection.getSuperDetection(OfflineEventDataUnit.class) == this) {
////			return 0;
////		}
//		
//		// search for the subdetection using the UID - should be faster
////		PamDataUnit existingSubDetection = findSubDetection(subDetection.getTimeMilliseconds(), 
////				subDetection.getStartSample(), subDetection.getChannelBitmap(), subDetection.getClass());
//		PamDataUnit existingSubDetection = findSubDetection(subDetection.getUID());
//		if (existingSubDetection != null) {
//			
//			// not sure why this line was here, replacing a unit with itself.  But it was
//			// causing duplicate clicks to be logged to the database, so commented out
//			replaceSubDetection(existingSubDetection, subDetection);
//			
//			// instead, flag the subDetection and event data unit as updated so that they get rewritten to the database
//			// (in case there is new information in either unit)
//			subDetection.updateDataUnit(System.currentTimeMillis());
//			this.updateDataUnit(System.currentTimeMillis());
//			checkEventICIData(subDetection.getParentDataBlock().getSampleRate());
//			return 0;
//			
//		}
//		
//		if (getSubDetectionsCount() == 0) {
//			setStartSample(subDetection.getStartSample());
//		}
//		setChannelBitmap(subDetection.getChannelBitmap() | getChannelBitmap());
//		if (getTimeMilliseconds() == 0) {
//			setTimeMilliseconds(subDetection.getTimeMilliseconds());
//			setEventEndTime(subDetection.getTimeMilliseconds());
//		}
//		else {
//			setEventEndTime(Math.max(getEventEndTime(), subDetection.getTimeMilliseconds()));
//			setTimeMilliseconds(Math.min(getTimeMilliseconds(), subDetection.getTimeMilliseconds()));
//		}
//		if (setUniqueEvent(subDetection, this) == 0) {
//			return 0; // sub det already there, so nothing added
//		}
//		if (getParentDataBlock() != null) {
//			if (isViewer) {
//				/*
//				 * Only issue these updates when in viewer mode. They get called elsewhere in real time mode. 
//				 */
//				getParentDataBlock().updatePamData(this, System.currentTimeMillis());
//			}
//		}
//		if (isViewer) {
//		// only seems to get written once even if this is called here - perhaps it's because it's 
//		// going back to the click, not the tracked click data block at this point which has no logging. 
//			subDetection.getParentDataBlock().updatePamData(subDetection, System.currentTimeMillis());
//		}
//		nClicks++;
//		
//		checkEventICIData(subDetection.getParentDataBlock().getSampleRate());
//		
//		return super.addSubDetection(subDetection);
//	}

//	public PamDataUnit findSubDetection(long timeMillis, long startSample, int channelBitmap, Class dataClass) {
//		// TODO Auto-generated method stub
//		PamDataUnit click = super.findSubDetection(timeMillis, channelBitmap, dataClass);
//		if (click == null) {
//			return null;
//		}
//		Long startSamp = click.getStartSample();
//		if (startSamp == null) return null;
//		if (click != null && click.getStartSample() == startSample) {
//			return click;
//		}
//		else {
//			return null;
//		}
//	}
	
//	/**
//	 * Find the first sub detection that's a click 
//	 * @return
//	 */
//	private ClickDetection findFirstClickDetection() {
//		int nSub = getSubDetectionsCount();
//		for (int i = 0; i < nSub; i++) {
//			PamDataUnit subDet = getSubDetection(i);
//			if (subDet.getClass() == ClickDetection.class) {
//				return (ClickDetection) subDet;
//			}
//		}
//		return null;
//	}

	private void checkEventICIData(double sampleRate) {
		int nSub = getSubDetectionsCount();
		if (nSub <= 0) {
			return;
		}
		ClickDetection aClick;
		Hashtable<Integer, ClickDetection> groupClicks = new Hashtable<Integer, ClickDetection>();
//		ClickDetection aClick = findFirstClickDetection();
//		if (aClick == null) {
//			return;
//		}
//		aClick.setICI(0);
//		if (nSub == 1) {
//			return;
//		}
		/*
		 * Try to do this with sample, but if it's in a different 
		 * file that may not work ! IF ici from sample number
		 * is > a second different from that from milliseconds, use the
		 * milliseconds. 
		 * 
		 * Also need to do by channel group, so make a hash table for channel groups ...
		 */
//		long lastTime = aClick.getTimeMilliseconds();
//		long lastSample = aClick.getStartSample();
//		long thisTime;
//		double sampleRate = getParentDataBlock().getSampleRate();
		double ici, ici2, iciDiff;
		for (int i = 0; i < nSub; i++) {
			PamDataUnit subDet = getSubDetection(i);
			if (subDet.getClass() != ClickDetection.class) {
				continue;
			}
			/**
			 * this can put and get at the same time. Returns last value in lastclick or null
			 */
			aClick = (ClickDetection) subDet;
			ClickDetection lastClick = groupClicks.put(subDet.getChannelBitmap(), aClick);
			if (lastClick == null) {
				aClick.setICI(0);
				continue;
			}
			ici = (aClick.getTimeMilliseconds()-lastClick.getTimeMilliseconds()) / 1000.;
			ici2 = (aClick.getStartSample()-lastClick.getStartSample()) / sampleRate;
			iciDiff = Math.abs(ici2-ici);
			if (iciDiff < 1.) {
				aClick.setICI(ici2);
//				aClick.setICI((thisTime-lastTime) / 1000.);
			}
			else {
				aClick.setICI(ici);
			}
		}
	}

//	@Override
//	public void removeSubDetection(PamDataUnit subDetection) {
//		super.removeSubDetection(subDetection);
//		PamDataBlock clickDataBlock = subDetection.getParentDataBlock();
//		nClicks--;
//		if (nClicks <= 0) {
////			getParentDataBlock().remove(this);
//		}
//		else {
//			/*
//			 * Need to get new start and end times - problem is that 
//			 * it's just possible not all data are loaded in memory
//			 * so it may be impossible to tell if it was the first or
//			 * last click that got loaded. 
//			 */
//			boolean haveStart = false, haveEnd = false;
//			if (getSubDetectionsCount() == nClicks) {
//				// easy - everything in memory. 
//				haveStart = haveEnd = true;
//				suspectEventTimes = false;
//			}
//			else if (clickDataBlock.getCurrentViewDataStart() > getTimeMilliseconds()) {
//				haveStart = false;
//				suspectEventTimes = true;
//			}
//			else if (clickDataBlock.getCurrentViewDataEnd() < getEventEndTime()) {
//				haveEnd = false;
//				suspectEventTimes = true;
//			}
//			else {
//				// this should never happen !
//				suspectEventTimes = true;
//				System.out.printf("Some confusion as to which data are loaded for event %d removal of click at %s."
//						+ " Current event limits are %s to %s\n",
//						getEventId(), PamCalendar.formatTime(subDetection.getTimeMilliseconds(), true),
//						PamCalendar.formatDateTime(getTimeMilliseconds()), PamCalendar.formatDateTime(getEventEndTime()));
//			}
//			if (haveStart) {
//				setTimeMilliseconds(getSubDetection(0).getTimeMilliseconds());
//			}
//			if (haveEnd) {
//				setEventEndTime(getSubDetection(getSubDetectionsCount()-1).getTimeMilliseconds());
//			}
//			if (getParentDataBlock() != null && isViewer) {
//				getParentDataBlock().updatePamData(this, System.currentTimeMillis());
//			}	
//		}
//		if (isViewer) {
//			subDetection.getParentDataBlock().updatePamData(subDetection, System.currentTimeMillis());
//		}
//		
//	}

	/**
	 * Ensure that there is only one event superdetection
	 * per click. 
	 * @param clickDetection
	 * @param event
	 */
	private int setUniqueEvent(PamDataUnit clickDetection, OfflineEventDataUnit event) {
		return clickDetection.setUniqueSuperDetection(event);
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}
	
	@Override
	public int getColourIndex() {
		int col = getDatabaseIndex();
		if (colourIndex != null && colourIndex != 0) {
			col = colourIndex;
		}
		setColourIndex(col);
		return col;
	}
	
	public void setColourIndex(int colourIndex){
		this.colourIndex  = PamColors.getInstance().getWhaleColourIndex(colourIndex);
	}

	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	
	/**
	 * @return the eventNumber which is the
	 * same as the database index.
	 */
	public int getEventNumber() {
		return getDatabaseIndex();
	}
//
//	/**
//	 * @param eventNumber the eventNumber to set
//	 */
//	public void setEventNumber(int eventNumber) {
//		this.eventNumber = eventNumber;
//	}

	public Short getMinNumber() {
		return minNumber;
	}

	public void setMinNumber(Short minNumber) {
		this.minNumber = minNumber;
	}

	public Short getBestNumber() {
		return bestNumber;
	}

	public void setBestNumber(Short bestNumber) {
		this.bestNumber = bestNumber;
	}

	public Short getMaxNumber() {
		return maxNumber;
	}

	public void setMaxNumber(Short maxNumber) {
		this.maxNumber = maxNumber;
	}

	public int getNClicks() {
		return getSubDetectionsCount();
	}

//	public void setNClicks(int nClicks) {
//		this.nClicks = nClicks;
//	}

	/**
	 * @return the suspectEventTimes
	 */
	public boolean isSuspectEventTimes() {
		return suspectEventTimes;
	}

	/**
	 * @param suspectEventTimes the suspectEventTimes to set
	 */
	public void setSuspectEventTimes(boolean suspectEventTimes) {
		this.suspectEventTimes = suspectEventTimes;
	}

	/**
	 * Quick way for events to tell observers of the data block that they 
	 * have updated. 
	 */
	public void notifyUpdate() {
		if (getParentDataBlock() != null) {
			getParentDataBlock().updatePamData(this, System.currentTimeMillis());
		}
	}

}
