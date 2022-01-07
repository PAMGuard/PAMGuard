package likelihoodDetectionModule.thresholdDetector;

import PamguardMVC.PamDataBlock;
import java.util.ArrayList;
import java.util.HashMap;
import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.normalizer.NormalizedDataUnit;

/**
 * This class tracks detections (after the DetectionFilter has done its business) when they are active, collecting statistics such as
 * startTime, duration, and the peak time/energy values.  When a detection has finished, we output a ThresholdDetectorDataUnit.
 * 
 *  
 * @author Dave Flogeras
 *
 */
public class FilteredDetectionWriter implements FilteredConsumer {

	private PamDataBlock<  ThresholdDetectorDataUnit > outputBlock;
	private SignalBand[] signalBands;
	private HashMap< DetectionKey, DetectionTracker > detectionTrackers;
	final private String targetIdentifier;
	final private double estimatedPeakTimeAccuracy;
	
	/**
	 * Constructor
	 * 
	 * @param outputBlock The output block we will put our ThresholdDetectionDataUnits into
	 * @param signalBands A list of SignalBands.  Their order is the same order that the data[] field in the NormalizedDataUnit.
	 * @param targetIdentifier The identifier for this target.  This is concatenated with the band identifier to create a detection type.
	 * @param estimatedPeakTimeAccuracy
	 */
	public FilteredDetectionWriter( PamDataBlock< ThresholdDetectorDataUnit > outputBlock,
									ArrayList< SignalBand > signalBands,
									String targetIdentifier,
									double estimatedPeakTimeAccuracy ) {
		
		this.targetIdentifier = targetIdentifier;
		this.outputBlock = outputBlock;
		this.estimatedPeakTimeAccuracy = estimatedPeakTimeAccuracy;
		
		this.detectionTrackers = new HashMap< DetectionKey, DetectionTracker >();
		
		// Make a straight array of signal bands for indexing later.
		this.signalBands = new SignalBand[ signalBands.size() ];
		this.signalBands = signalBands.toArray( this.signalBands );


	}
	
	public void startDetection( DetectionKey key ) {
		
		// Don't really need to do anything here in this case
		
	}
	
	public boolean updateDetection( DetectionKey key, NormalizedDataUnit ndu ) {

		if( detectionTrackers.containsKey( key )) {
			detectionTrackers.get( key ).update( ndu );
		}
		else {
			detectionTrackers.put( key, new DetectionTracker( key.bandIndex(), ndu ));
		}
		return false;
	}
	
	public void endDetection( DetectionKey key ) {

		DetectionTracker tracker = detectionTrackers.remove( key );

		// We can use either the ratioPeakTracker or the rawEnergyPeakTracker.  We chose the former
		CurvePeakTracker peakTracker = tracker.ratioPeakTracker();
		
		ThresholdDetectorDataUnit t = new ThresholdDetectorDataUnit( tracker.startTime(),
																	 key.channelMask(),
																	 tracker.startSample(),
																	 tracker.duration() );
		
		t.setPeakTime( peakTracker.peakSample() / this.outputBlock.getParentProcess().getSampleRate() );
		t.setMeasuredAmplitude( 10.0 * Math.log10( peakTracker.peakValue() ));
		
		double frequency[] = new double[2];
		SignalBand s = signalBands[ key.bandIndex() ];
		
		frequency[0] = s.startFrequencyHz;
		frequency[1] = s.endFrequencyHz;
		
		t.setFrequency(frequency);
		t.setDetectionType( this.targetIdentifier + " - " + signalBands[ key.bandIndex() ].identifier );
		t.setEstimatedPeakTimeAccuracy( this.estimatedPeakTimeAccuracy );
		
		outputBlock.addPamData( t );
		
		
	}
	
}
