package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.normalizer.NormalizedDataUnit;
import likelihoodDetectionModule.normalizer.NormalizedData;

/**
 * This class is used to track statistics about a running detection.
 * 
 * Currently it remembers the startTime (in ms since 01-01-1970), the startSample number.
 * 
 * Then it calculates the duration (in samples), and uses two CurvePeakTracker objects to track
 * the peak, time, and sample number of the peak in the raw energy, as well as the peak in the SNR
 * 
 * @author Dave Flogeras
 *
 */
public class DetectionTracker {

	private long startTime;
	private long startSample;
	private long duration;
	private CurvePeakTracker rawEnergyPeakTracker;
	private CurvePeakTracker ratioPeakTracker;
	final private int dataIndex;
	
	/**
	 *  Constructor.  This constructs a DetectionTracker, and seeds its initial values.
	 *  
	 *  @param dataIndex The index into the NormalizedDataUnit's data[] that is associated with this Tracker.
	 *  @param ndu The first data update.
	 */
	public DetectionTracker( int dataIndex, NormalizedDataUnit ndu ) {
	
		this.dataIndex = dataIndex;
		
		NormalizedData[] d = ndu.getData();
		
		this.rawEnergyPeakTracker = new CurvePeakTracker( ndu.getTimeMilliseconds(),
													  ndu.getStartSample(),
													  d[ dataIndex ].signal );
		
		this.ratioPeakTracker = new CurvePeakTracker( ndu.getTimeMilliseconds(),
													  ndu.getStartSample(),
													  d[ dataIndex ].snr() );
		
		this.startTime = ndu.getTimeMilliseconds();
		this.startSample = ndu.getStartSample();
		this.duration = ndu.getSampleDuration();
		
	}

	/**
	 *  This updates the detections duration, and both the Raw energy and Ratio peak trackers.
	 *   
	 * @param ndu The data describing this update.
	 */
	public void update( NormalizedDataUnit ndu ) {
		
		this.duration += ndu.getSampleDuration();
		
		NormalizedData[] d = ndu.getData();
		
		rawEnergyPeakTracker.update( ndu.getTimeMilliseconds(), ndu.getStartSample(), d[ dataIndex ].signal );
		ratioPeakTracker.update( ndu.getTimeMilliseconds(), ndu.getStartSample(), d[ dataIndex ].snr() );
		
	}
	
	public long startTime() {
		return this.startTime;
	}
	
	public long duration() {
		return this.duration;
	}
	
	public CurvePeakTracker rawEnergyPeakTracker() {
		return this.rawEnergyPeakTracker;
	}
	
	public CurvePeakTracker ratioPeakTracker() {
		return this.ratioPeakTracker;
	}
	
	public long startSample() {
		return this.startSample;
	}
	
	
}
