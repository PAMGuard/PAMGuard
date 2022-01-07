package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.normalizer.NormalizedDataUnit;

/**
 * 
 * Implements the FilteredConsumer interface.  This is used only for the UnitTest for this package.
 * 
 * @author Dave Flogeras
 *
 */
public class TestConsumer implements FilteredConsumer {

	/**
	 * Simple class to collect some info about update events for the UnitTest.
	 * 
	 * @author Dave Flogeras
	 *
	 */
	public class EventInfo {
		private int bandIndex;
		private long timeStamp;
		private int channelMask;
		
		public EventInfo( int bandIndex,
						  long timeStamp,
						  int channelMask ) {
			this.bandIndex = bandIndex;
			this.timeStamp = timeStamp;
			this.channelMask = channelMask;
		}
		
		public int bandIndex() {
			return this.bandIndex;
		}
		
		public long timeStamp() {
			return this.timeStamp;
		}
		
		public int channelMask() {
			return this.channelMask;
		}
	}
	
	private java.util.Queue< EventInfo > filteredEvents;
	
	TestConsumer() {
		filteredEvents = new java.util.LinkedList< EventInfo >();
	}
	
	public void startDetection( DetectionKey key ) {
		
	}
	
	public boolean updateDetection( DetectionKey key, NormalizedDataUnit ndu ) {
		
		filteredEvents.add( new EventInfo( key.bandIndex(), ndu.getTimeMilliseconds(), key.channelMask() ));
		return true;
	}
	
	public void endDetection( DetectionKey key ) {
		
	}
	
	public java.util.Queue< EventInfo > events() {
		return filteredEvents;
	}
}
