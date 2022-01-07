package Array.streamerOrigin;

import java.util.ListIterator;

import Array.Streamer;
import Array.StreamerDataUnit;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;

/**
 * This is more complicated than the stramerDataIterator since it needs to 
 * combine data from two sources. First it needs to find the most appropriate
 * streamer data for the given time, then it needs to find the closest GPS data to 
 * that time, then it needs to combine the two. 
 * The main iteration will actually be over GPS data, then for each GPS position 
 * find the closest (or interpolate) streamer data, then make a new streamer data
 * unit that contains the gps data to return to the loacliser. 
 * @author Doug Gillespie
 *
 */
public class GPSOriginIterator extends OriginIterator {

	private ListIterator<GpsDataUnit> gpsIterator;
//	private StreamerDataIterator streamerDataIterator;

	public GPSOriginIterator(Streamer streamer, int whereFrom, GPSDataBlock gpsDataBlock) {
		super(streamer);
		if (gpsDataBlock != null) {
			gpsIterator = gpsDataBlock.getListIterator(whereFrom);
		}
//		streamerDataIterator = new StreamerDataIterator(whereFrom, streamer);
		
	}

	@Override
	public boolean hasNext() {
		if (gpsIterator == null) {
			return false;
		}
		return gpsIterator.hasNext();
	}

	@Override
	public GpsData next() {
		if (gpsIterator == null || gpsIterator.hasNext() == false) {
			return null;
		}
		return gpsIterator.next().getGpsData();
//		GpsDataUnit gpsDataUnit = gpsIterator.next();
////		StreamerDataUnit streamerDataUnit = streamerDataIterator.movePreceeding(gpsDataUnit.getTimeMilliseconds());
//		if (streamerDataUnit == null) {
//			return null;
//		}
//		else {
//			return streamerDataUnit.makeCombinedGpsUnit(gpsDataUnit);
//		}
	}

	@Override
	public boolean hasPrevious() {
		if (gpsIterator == null) {
			return false;
		}
		return gpsIterator.hasPrevious();
	}

	@Override
	public GpsData previous() {
		if (gpsIterator == null || gpsIterator.hasPrevious() == false) {
			return null;
		}
		return gpsIterator.previous().getGpsData();
//		StreamerDataUnit streamerDataUnit = streamerDataIterator.movePreceeding(gpsDataUnit.getTimeMilliseconds());
//		if (streamerDataUnit == null) {
//			streamerDataUnit = new StreamerDataUnit(gpsDataUnit.getTimeMilliseconds(), streamer);
//		}
////		else {
//			return streamerDataUnit.makeCombinedGpsUnit(gpsDataUnit);
////		}
	}

	@Override
	public int nextIndex() {
		return gpsIterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return gpsIterator.previousIndex();
	}

	@Override
	public void remove() {
//		 gpsIterator.remove();

	}

	@Override
	public void set(GpsData e) {
//		gpsIterator.set(e);
	}

	@Override
	public void add(GpsData e) {
//		gpsIterator.add(e);
	}

}
