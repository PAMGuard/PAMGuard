package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.normalizer.NormalizedDataUnit;

/** 
 *  This class filters events on a channel if the time passed since the last detection on the same channel
 *  is less than a minimum interval.
 *  
 *  If enough time has passed since the last detection on a given channel, the new detection is not filtered, otherwise the entire
 *  detection is filtered.
 * 
 * @author Dave Flogeras
 *
 */
public class DetectionFilter {

	private java.util.Map< Integer, Long > lastEventTimes;
	private java.util.Map< Integer, java.util.List< DetectionKey >> allowedEvents;
	private long minimumIntervalMilliseconds;
	private java.util.List< FilteredConsumer > consumers;
	
	/** Constructor
	 * 
	 * @param consumers A list of consumers to attach to the filter
	 * @param minimumIntervalMilliseconds The minimum time between events on a channel.
	 */
	DetectionFilter( java.util.List< FilteredConsumer > consumers,
					 long minimumIntervalMilliseconds ) {
		commonInit( consumers, minimumIntervalMilliseconds );
	}
	
	/** Constructor for a single consumer
	 * 
	 * @param consumer The consumer to attach to the filter
	 * @param minumumIntervalMilliseconds The miniumum time between events per channel
	 */
	DetectionFilter( FilteredConsumer consumer,
					 long minimumIntervalMilliseconds ) {
		
		java.util.LinkedList< FilteredConsumer > l = new java.util.LinkedList< FilteredConsumer >();
		l.add( consumer );
		commonInit( l, minimumIntervalMilliseconds );
		
	}

	private void commonInit(java.util.List<FilteredConsumer> consumers,
			long minimumIntervalMilliseconds) {

		this.lastEventTimes = new java.util.HashMap<Integer, Long>();
		this.allowedEvents = new java.util.HashMap<Integer, java.util.List< DetectionKey >>();

		this.minimumIntervalMilliseconds = minimumIntervalMilliseconds;
		this.consumers = consumers;
	}
	
	
	/** Attempts to start a detection.  If enough time has not passed since the last detection on
	 * the same channelMask, the entire detection is filtered.
	 * precondition: A detection for the given channelMask/bandId is not currently running.
	 * 
	 * @param key The detection key for the new detection
	 * @param timeMilliseconds The start time of the detection
	 */
	void startDetection( DetectionKey key, long timeMilliseconds ) {
		
		Long lastTime = lastEventTimes.get( key.channelMask() );
		
		if( lastTime == null || ( lastTime + minimumIntervalMilliseconds <= timeMilliseconds )) {
			
			lastEventTimes.put( key.channelMask(), timeMilliseconds );
			
			// Create the list for this channel if this is the first detection on it
			if( allowedEvents.get( key.channelMask() ) == null ) {
				allowedEvents.put( key.channelMask(), new java.util.LinkedList< DetectionKey >() );
			}
			
			java.util.List< DetectionKey > l = allowedEvents.get( key.channelMask() );
			for( FilteredConsumer f : consumers ) {
				f.startDetection( key );
			}
			
			// If the user mistakenly starts twice without ending between, we do not want
			// double entries in the allowedEvents list.
			if( l.contains( key ) == false ) {
				l.add( key );
			}
			
		}
		
	}
	
	/** Updates a running detection with the given DetectionEvent
	 *  precondition: A detection has been started on the same channel/band with StartDetection()
	 * 
	 * @param key The key of the detection
	 * @param ndu The NormalizedDataUnit which describes the update
	 * @return true if the detection was passed, false if it was filtered
	 * @see StartDetection
	 */
	boolean updateDetection( DetectionKey key, NormalizedDataUnit ndu ) {
		
		java.util.List< DetectionKey > l = allowedEvents.get( key.channelMask() );
		
		if( l != null && l.contains( key )) {
			for( FilteredConsumer c : consumers ) {
				c.updateDetection( key, ndu );
			}
			return true;
		}
		
		return false;
	}
	
	/** End a detection.  Marks the end of a detection on a given channel/band.
	 *  Ignore the request if it was not previously started.
	 * 
	 * @param key The detection key for the detection to stop
	 */
	void endDetection( DetectionKey key ) {
		java.util.List< DetectionKey > l = allowedEvents.get( key.channelMask() );
		if( l == null ) {
			return;
		}
		int index = l.indexOf( key );
		if( index != -1 ) {
			l.remove( index );
			for( FilteredConsumer f : consumers ) {
				f.endDetection( key );
			}
		}
	}
	
	
	/**
	 * 
	 * @param channel The channel of interest
	 * @return The number of active detections on given channel
	 */
	int activeDetections( int channel ) {
		int ret = 0;
		java.util.List< DetectionKey > l = allowedEvents.get( channel );
		if( l != null ) {
			ret = l.size();
		}
		return ret;
	}
	
	/** 
	 * 
	 * @return The number of active detections on all channels
	 */
	int activeDetections() {
		int ret = 0;
		java.util.Iterator< java.util.Map.Entry< Integer, java.util.List< DetectionKey >>>  it = allowedEvents.entrySet().iterator();
		while( it.hasNext() ) {
			ret += it.next().getValue().size();
		}
		return ret;
	}
	
	
	/** Resets the filter timeouts on all channels.  This will allow the next new detection (per channel) no matter when it happens.
	 * 
	 */
	void reset() {
		this.lastEventTimes.clear();
	}
	
}
