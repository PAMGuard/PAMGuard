package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.GuardBand;
import likelihoodDetectionModule.normalizer.NormalizedDataUnit;
import likelihoodDetectionModule.normalizer.NormalizedData;

/**
 *  This class performs the actual max likelihood detector logic.  There is one per SignalBand.
 *  Basically, it checks that the SNR is above the inBandThreshold (this comparison is done in linear units, even though
 *  thresholds are specified in dB).
 * 
 *  Next, a secondary test is performed.  The signal estimate in each associated GuardBand (if any were specified) is taken,
 *  and averaged.  If this average level is greater than the guardBandThreshold, a detection is initiated.  The detection remains
 *  active for as long as these two tests pass.
 *  
 *  When a detection begins, the startDetection() method of DetectionFilter is called, and an updateDetection() is immediately called
 *  with the data of the first data point.  Any subsequent data points are passed to the DetectionFilter by calling its
 *  updateDetection() method.  Finally when the detetction finished, the DetectionFilter's endDetction() method is called. 
 * 
 * @author Dave Flogeras
 *
 */
public class Threshold {

	private DetectionFilter detectionFilter;
	private int signalBandIndex;
	private SignalBand signalBand;
	private java.util.Map< Integer, GuardBand > associatedGuardBands;
	private java.util.Set< Integer > activeDetections;
	
	/**
	 * Constructor.
	 * 
	 * @param detectionFilter The post detection filter to use.
	 * @param signalBandIndex The index (within the data blocks) of the signal band of interest for this threshold
	 * @param signalBand The SignalBand object defining this thresholds band of interest
	 * @param guardBands The mapping of indices (into the data blocks) and respective guardBands associated with
	 * the signalBand. This may be an empty list or null if there are no associated guardBands. 
	 */
	public Threshold( DetectionFilter detectionFilter,
					  int signalBandIndex,
					  SignalBand signalBand,
					  java.util.Map< Integer, GuardBand > guardBands ) {
	
		this.detectionFilter = detectionFilter;
		this.signalBandIndex = signalBandIndex;
		this.signalBand = signalBand;
		this.associatedGuardBands = guardBands;
		
		this.activeDetections = new java.util.HashSet< Integer >();
		
	}
	
	void process( NormalizedDataUnit ndu ) {
		
		NormalizedData[] data = ndu.getData();

		boolean inBandTest = false;
		if( data[ this.signalBandIndex ].snr() > this.signalBand.InBandAsRatio() ) {
			inBandTest = true;
		}
		
		
		boolean guardTest = false;
		if( associatedGuardBands == null || associatedGuardBands.isEmpty() ) {
			// It passes if there are no guard bands
			guardTest = true;
		}
		else if( inBandTest == false ) {
			// No point in checking the guardBand threshold if the inBandTest failed, we wont be starting anything
			guardTest = false;
		}
		else {
			
			double avgGuardSignal = 0;
			java.util.Iterator<java.util.Map.Entry<Integer, GuardBand>> it = associatedGuardBands
					.entrySet().iterator();
			while (it.hasNext()) {
				avgGuardSignal += data[ it.next().getKey() ].signal;
			}
			avgGuardSignal /= associatedGuardBands.size();
			if( data[ this.signalBandIndex ].signal / avgGuardSignal > this.signalBand.GuardBandAsRatio() ) {
				guardTest = true;
			}
		}

		
		DetectionKey k = new DetectionKey( ndu.getChannelBitmap(), signalBandIndex );
		if( inBandTest == true && guardTest == true ) {
			
			if( ! activeDetections.contains( ndu.getChannelBitmap() ) ) {
				// This is a new detection, start it
				activeDetections.add( ndu.getChannelBitmap() );
				detectionFilter.startDetection( k, ndu.getTimeMilliseconds() );
				
			}
			detectionFilter.updateDetection( k, ndu );
			
		}
		else {
			// Stop the detection
			if( activeDetections.contains( ndu.getChannelBitmap() ) ) {
				activeDetections.remove( ndu.getChannelBitmap() );
				detectionFilter.endDetection( k );
			}
		}
		 
	}

}
