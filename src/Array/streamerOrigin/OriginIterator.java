package Array.streamerOrigin;

import java.util.ListIterator;

import Array.Streamer;
import Array.StreamerDataUnit;
import GPS.GpsData;
import GPS.GpsDataUnit;

abstract public class OriginIterator implements ListIterator<GpsData> {

	protected Streamer streamer;

	public OriginIterator(Streamer streamer) {
		super();
		this.streamer = streamer;
	}
	
	/**
	 * Move to a the last data unit that is at or immediately
	 * before the given time. Position the cursor such that a 
	 * call to next() will return the subsequent unit. 
	 * <p>
	 * Very useful in interpolating positions when two values
	 * which bracket a given time are required. 
	 * @param time time in milliseconds. 
	 * @return Gps unit at or immediately before that time. 
	 */
	public GpsData movePreceeding(long time) {
		/*
		 * allow for the fact that this may be called at any point - so may need to go backwards or forwards. 
		 * 
		 */
		GpsData prevUnit = null;
		while (hasPrevious()) {
			// often, this will return false, since we'll be at the start
			// of the iterator, so won't get far !
			prevUnit = previous();
			if (prevUnit == null) break;
			if (prevUnit.getTimeInMillis() <= time) {
				break;
			}
		}
		/**
		 * Most of the time, we'll just be going forwards, so to see if we're
		 * in the right place, have to get the next unit. 
		 */
		GpsData nextUnit = null;
		while (hasNext()) {
			nextUnit = next();
			if (nextUnit == null) break;
			if (nextUnit.getTimeInMillis() > time) {
				break;
			}
		}
		
		// may have found one or more or both !
		if (prevUnit == null && nextUnit == null) {
			return null;
		}
		else if (nextUnit != null) {
			/*
			 * This is the most common situation, either when we started at 
			 * the start or scrolled back to the preceding unit and then forwards one
			 * with the calls to next, so go back one more and return what's there. 
			 */
			if (hasPrevious()) {
				// can move cursor back and return value in one move. 
				return previous();
			}
			else {
				// was probably the first or only unit, so position the 
				// cursor before it again, but return this unit
				previous();
				return nextUnit;
			}
		}
		else {
			/*
			 * If next unit was null, then we can assume we were right at the end 
			 * of the list in any case. 
			 */
			return prevUnit;
		}
	}
	

}
