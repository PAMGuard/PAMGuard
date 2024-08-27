package eventCounter;

import java.util.LinkedList;
import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;

/**
 * Class for counting events - e.g. clicks, whistles, etc.
 * <br> Does not hold references to original dataunits but just
 * holds a time reference for each event so that not too much memory
 * is used. The click counter was causing problems since it would hold 
 * far too many clicks in memory.
 *   
 * @author Doug Gillespie
 *
 */
public class DataCounter extends PamObserverAdapter {
	
	private String name, shortName;

	private PamDataBlock sourceData;
	
	private int dataCountSeconds;
	
	private int eventTriggerSeconds, eventTriggerCount;
	
	Long currentEventTime;
	
	private List<Long> dataTimes = new LinkedList<Long>();
	
	private List<Long> eventTriggerTimes = new LinkedList<Long>();
	
	private List<Long> eventTimes = new LinkedList<Long>();
	
	private int channelMap = 0xFFFFFFFF;
	
	private EventCounterSidePanel sidePanel; 
	
	private long lastClockUpdate;
	
	private boolean eventOn;	
	
	private EventCounterMonitor eventCounterMonitor;
	
	private PamDataUnit lastDataUnit;

	public DataCounter(String name, PamDataBlock sourceData, int countSeconds) {
		super();
		setName(name);
		this.setSourceData(sourceData);
		this.dataCountSeconds = countSeconds;
	}
	
	public int getDataCountSeconds() {
		return dataCountSeconds;
	}

	public void setDataCountSeconds(int countSeconds) {
		this.dataCountSeconds = countSeconds;
		if (sidePanel != null) {
			sidePanel.setCountSeconds();
		}
	}

	@Override
	public String getObserverName() {
		return name + " counter";
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		if (lastClockUpdate > milliSeconds) {
			lastClockUpdate = 0;
		}
		if (milliSeconds - lastClockUpdate > 1000) {
			performActions(milliSeconds);
			lastClockUpdate = milliSeconds;
		}
	}
	
	synchronized private void performActions(long milliSeconds) {
		deleteOldData(milliSeconds);
		triggerEvents(milliSeconds);
		updateSidePanel();
	}
	
	private void deleteOldData(long now) {
		deleteOldData(dataTimes, now-1000*dataCountSeconds);
		deleteOldData(eventTimes, now-1000*dataCountSeconds);
		deleteOldData(eventTriggerTimes, now-1000*eventTriggerSeconds);
	}
	
	private int deleteOldData(List<Long> list, long firstKeepTime) {
		int nDel = 0;
		while (list.size() > 0 && list.get(0) < firstKeepTime) {
			list.remove(0);
			nDel++;
		}
		return nDel;
	}
	
	private void triggerEvents(long timeNow) {
		int n = eventTriggerTimes.size();
		if (!eventOn & n > eventTriggerCount) {
			startEvent(timeNow, n);
		}
		else if (eventOn & n < eventTriggerCount) {
			endEvent(timeNow);
		}
		else if (eventOn & n > eventTriggerCount) {
			continueEvent(timeNow);
		}
	}
	
	private void startEvent(long timeNow, int n) {
		currentEventTime = new Long(timeNow);
		eventTimes.add(currentEventTime);
		if (eventCounterMonitor != null) {
			eventCounterMonitor.startEvent(timeNow, lastDataUnit);
		}
		eventOn = true;
	}
	
	private void endEvent(long timeNow) {
		currentEventTime = null;
		if (eventCounterMonitor != null) {
			eventCounterMonitor.endEvent(timeNow, lastDataUnit);
		}
		eventOn = false;
	}
	
	private void continueEvent(long timeNow) {
		if (currentEventTime != null) {
			currentEventTime = timeNow;
			int n = eventTimes.size();
			if (n > 0) {
				eventTimes.set(n-1, currentEventTime);
			}
		}
		if (eventCounterMonitor != null) {
			eventCounterMonitor.continueEvent(timeNow, lastDataUnit);
		}
	}

	private void updateSidePanel() {
		if (sidePanel == null) {
			return;
		}
		sidePanel.updateCounts(dataTimes.size(), eventTimes.size());
	}
	
	@Override
	synchronized public void addData(PamObservable o, PamDataUnit arg) {
//		System.out.println("update " + getName());
		
//		if (arg.getChannelBitmap() == 0 || (arg.getChannelBitmap() & channelMap) != 0) {
		if (arg.getSequenceBitmap() == 0 || (arg.getSequenceBitmap() & channelMap) != 0) {
			lastDataUnit = arg;
			dataTimes.add(arg.getTimeMilliseconds());
			eventTriggerTimes.add(arg.getTimeMilliseconds());
		}
//		else {
//			System.out.println(String.format("Wrong channel map: %d, %d", arg.getChannelBitmap(), channelMap));
//		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (sidePanel != null) {
			sidePanel.rename(name);
		}
		if (shortName == null || shortName.equals(name)) 
			setShortName(name);
	}
	

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
		if (sidePanel != null) {
			sidePanel.setShortName(shortName);
		}
	}

	public PamDataBlock getSourceData() {
		return sourceData;
	}

	public void setSourceData(PamDataBlock sourceData) {
		this.sourceData = sourceData;
		sourceData.addObserver(this);
	}
	
	public void setEventTrigger(int eventTriggerSeconds, int eventTriggerCount) {
		this.eventTriggerSeconds = eventTriggerSeconds;
		this.eventTriggerCount = eventTriggerCount;
	}

	public int getChannelMap() {
		return channelMap;
	}

	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}

	public EventCounterSidePanel getSidePanel() {
		if (sidePanel == null) {
			sidePanel = new EventCounterSidePanel(this);
		}
		return sidePanel;
	}

	/**
	 * @param eventCounterMonitor the eventCounterMonitor to set
	 */
	public void setEventCounterMonitor(EventCounterMonitor eventCounterMonitor) {
		this.eventCounterMonitor = eventCounterMonitor;
	}

	/**
	 * @return the eventCounterMonitor
	 */
	public EventCounterMonitor getEventCounterMonitor() {
		return eventCounterMonitor;
	}

	
	
}
