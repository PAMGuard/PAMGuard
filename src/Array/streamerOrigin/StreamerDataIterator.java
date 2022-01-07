package Array.streamerOrigin;

import java.util.ListIterator;

import Array.ArrayManager;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import GPS.GpsData;
import PamguardMVC.PamDataBlock;

/**
 * Iterator for moving through streamer data. Hooks itself to the 
 * streamer data block, but mucks with the iteration a bit to only return
 * data for a specific streamer.  
 * <p>
 * Gets used by the StaticOriginMethod, which only uses the streamer position data
 * but also can be use by the GPSOriginMethod which may want to pull in additional 
 * streamer information to add to the GPS data. 
 * @author Doug Gillespie
 *
 */
public class StreamerDataIterator extends OriginIterator {

	private StreamerDataBlock streamerDataBlock;
	private int chanMap;
	private long firstUnitTime = Long.MAX_VALUE; // start with these at opp ends so that
	private long lastUnitTime = Long.MIN_VALUE;  // if there are no data, hasNext and hasPrev both fail. 
	private long currentUnitTime = 0;
	private ListIterator<StreamerDataUnit> listIterator;

	public StreamerDataIterator(int whereFrom, Streamer streamer) {
		super(streamer);
		// need to know the first and last times of data items in the streamer datablock 
		// that associate with this streamer. 
		streamerDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		chanMap = 1<<streamer.getStreamerIndex();
		StreamerDataUnit sdu = streamerDataBlock.getFirstUnit(chanMap);
		if (sdu != null) {
			firstUnitTime = sdu.getTimeMilliseconds();
		}
		sdu = streamerDataBlock.getLastUnit(chanMap);
		if (sdu != null) {
			lastUnitTime = sdu.getTimeMilliseconds();
		}
		listIterator = streamerDataBlock.getListIterator(whereFrom);
		if (whereFrom == PamDataBlock.ITERATOR_END) {
			currentUnitTime = Long.MAX_VALUE;
		}
	}

	@Override
	public boolean hasNext() {
		return (listIterator.hasNext() && currentUnitTime < lastUnitTime);
	}

	@Override
	public GpsData next() {
		StreamerDataUnit streamerDataUnit;
		while (listIterator.hasNext()) {
			streamerDataUnit = listIterator.next();
			if (streamerDataUnit.getStreamerData().getStreamerIndex() == streamer.getStreamerIndex()) {
				if (streamerDataUnit.getStreamerData().getOriginSettings().originMethodClass == StaticOriginMethod.class) {
//					return ((StaticOriginSettings)streamerData.getOriginSettings()).getStaticPosition();
//					return completeOriginPosition(streamerData);
					return streamerDataUnit.getGpsData();
				}
				else {
					return null;
				}
			}
		}
		return null;
	}
	
	/**
	 * Create a complete origin position. This basically means adding in heading pitch and roll 
	 * information to the gps data unit that was returned from the main streamer settings.
	 * @param streamerData streamer data (a Streamer)
	 * @return a GPS data unit complete with head, pitch and roll data. 
	 */
//	private StreamerDataUnit completeOriginPosition(Streamer streamerData) {
//		GpsDataUnit gpsDataUnit;
//		try {
//			gpsDataUnit = ((StaticOriginSettings) streamerData.getOriginSettings()).getStaticPosition();
//		}
//		catch (ClassCastException e) {
//			return null;
//		}
//		/**
//		 * Have to add in the heading pitch and roll since they are in with the main streamer data
//		 */
//		StreamerDataUnit gpsData = gpsDataUnit.getGpsData();
//		gpsData.setTrueHeading(streamerData.getHeading());
//		gpsData.setRoll(streamerData.getRoll());
//		gpsData.setPitch(streamerData.getPitch());
//		return gpsDataUnit;
//	}

	@Override
	public boolean hasPrevious() {
		return (listIterator.hasPrevious() && currentUnitTime > firstUnitTime);
	}

	@Override
	public GpsData previous() {
		StreamerDataUnit streamerDataUnit;
		while (listIterator.hasPrevious()) {
			streamerDataUnit = listIterator.previous();
			if (streamerDataUnit.getStreamerData().getStreamerIndex() == streamer.getStreamerIndex()) {
				if (streamerDataUnit.getStreamerData().getOriginSettings() == null) {
//					System.out.println("Null origin settings");
					continue;
				}
//				if (streamerDataUnit.getStreamerData().getOriginSettings().originMethodClass == StaticOriginMethod.class) {
//					return ((StaticOriginSettings)streamerData.getOriginSettings()).getStaticPosition();
//					return completeOriginPosition(streamerDataUnit);
					return streamerDataUnit.getGpsData();
//				}
//				else {
//					return null;
//				}
			}
		}
		return null;
	}

	@Override
	public int nextIndex() {
		return listIterator.nextIndex();
	}

	@Override
	public int previousIndex() {
		return listIterator.previousIndex();
	}

	@Override
	public void remove() {
		listIterator.remove();
	}

	@Override
	public void set(GpsData e) {
//		listIterator.set(e);
	}

	@Override
	public void add(GpsData e) {
//		listIterator.add(e);
	}

}
