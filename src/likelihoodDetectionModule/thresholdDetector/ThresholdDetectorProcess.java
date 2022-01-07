package likelihoodDetectionModule.thresholdDetector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObservable;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import autecPhones.AutecGraphics;
import likelihoodDetectionModule.normalizer.NormalizedDataUnit;
import likelihoodDetectionModule.LikelihoodDetectionUnit;
import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.GuardBand;

/**
 *  This PamProcess is the main maximum likelihood threshold detector logic.
 *  
 *  Its main features are: Initial signal/noise thresholding, secondary signal->guard band thresholding, and
 *  limiting the number of detections (per channel) by forcing a configurable amount of time to pass between
 *  consecutive detections.
 *  
 *  The output data are derivatives of PamDetection, and the data is logged to an SQL database as well (if the
 *  user has configured one).
 *  
 * @author Dave Flogeras
 *
 */
public class ThresholdDetectorProcess extends PamProcess {
	
	// :: -----------------------------------------------------------------------
	// :: Object Construction
	private PamDataBlock< NormalizedDataUnit > normalizedDataBlock;
	
	private PamDataBlock< ThresholdDetectorDataUnit > outputDataBlock;
	
	private ArrayList< Threshold > thresholds;
	
	final private ArrayList< SignalBand > signalBands;
	private ArrayList< Map< Integer, GuardBand>> guardBandMappings;
	
	final private double secondsBetweenDetections;
	final String targetIdentifier;
	final double estimatedPeakTimeAccuracy;
	
	private static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CROSS, 12, 12, true, Color.BLUE, Color.BLUE);
	
	/**
	 *  Constructor
	 * 
	 * @param ldu Parent module
	 * @param targetIdentifier The free form string (user defined) describing the overall target.
	 * @param estimatedPeakTimeAccuracy
	 * @param signalEstimate The input data block
	 * @param secondsBetweenDetections The time, in seconds, that must pass between detections on a channel.
	 * @param signalBands List of all defined signal bands, must contain at least one.
	 * @param guardBands List of all defined noise bands, can be empty.
	 */
	public ThresholdDetectorProcess( LikelihoodDetectionUnit ldu,
									 String targetIdentifier,
									 double estimatedPeakTimeAccuracy,
			                         PamDataBlock signalEstimate,
			                         double secondsBetweenDetections,
			                         ArrayList< SignalBand > signalBands,
			                         ArrayList< GuardBand > guardBands ) {
		super( ldu, null );
		
		setCanMultiThread(false);
	
		this.setProcessName( ldu.getUnitName() + " - " + targetIdentifier );
		
		this.estimatedPeakTimeAccuracy = estimatedPeakTimeAccuracy;
		this.targetIdentifier = targetIdentifier;
		this.secondsBetweenDetections = secondsBetweenDetections;
		this.signalBands = signalBands;
		this.guardBandMappings = new ArrayList< Map< Integer, GuardBand >>();
		this.thresholds = new ArrayList< Threshold >();
		
		// Create a mapping table of associated guard bands, and their indices in the data.
		int signalIndex = 0;
		for( SignalBand s : signalBands ) {
			
			Map< Integer, GuardBand > map = new java.util.HashMap< Integer, GuardBand >();
			// Guard bands start after signal bands, in terms of indexes into the data.
			int guardIndex = signalBands.size();
			for( GuardBand g : guardBands ) {
				if( g.associatedSignalBandIdentifier == s.identifier ) {
					map.put( guardIndex, g );
				}
				++guardIndex;
			}
			guardBandMappings.add( map );
			++signalIndex;
		}
		
	    normalizedDataBlock = signalEstimate;
	    setParentDataBlock( normalizedDataBlock );
	    
		/* create an output data block and add to the output block list so that other detectors
		 * can see it. Also set up an overlay graphics class for use with the data block. 
		 */ 
		outputDataBlock = new PamDataBlock< ThresholdDetectorDataUnit >( ThresholdDetectorDataUnit.class, 
				this.getProcessName(), this, 0);
		PamDetectionOverlayGraphics detectionOverlayGraphics = new PamDetectionOverlayGraphics(outputDataBlock, new PamSymbol(defaultSymbol));
		detectionOverlayGraphics.setLineColor( java.awt.Color.BLUE );
		outputDataBlock.setOverlayDraw(detectionOverlayGraphics);
		StandardSymbolManager symbolManager = new StandardSymbolManager(outputDataBlock, defaultSymbol, true);
		symbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_AND_LENGTH);
		outputDataBlock.setPamSymbolManager(symbolManager);
		outputDataBlock.SetLogging(new DetectorSQLLogging( ldu, this, outputDataBlock ));
		outputDataBlock.setCanClipGenerate(true);
		
		addOutputDataBlock(outputDataBlock);	
	}

	// ---------------------------------------------------------------------
	// The pamStart() method is called by the controller to start the
	// process.

	@Override
	public void pamStart() {
				
	}

	// ---------------------------------------------------------------------
	// The pamStop() method is called by the controller to stop the
	// process.

	@Override
	public void pamStop() {
		// Even though there is no functionality needed here, it must be
		// specified because it is abstract in the base class.
	}
	
	// ------------------------------------------------------------------------
	// This class is-a PamObserver, which enables this object 
	// to subscribe for notification of new data. The PAMGUARD profiles 
	// monitors the CPU usage of each module, so when the method
	// PamObserver.update() is called, the PamProcess handles profiling tasks
	// and calls this newData() method when it is done.
	
	@Override
	public void newData( PamObservable o, PamDataUnit arg ) {
		
		NormalizedDataUnit input = (NormalizedDataUnit) arg;

		for( Threshold t : thresholds ) {
			t.process( input );
		}
		
	}
	
	// ------------------------------------------------------------------------
	// The PAMGUARD modules are not guaranteed to be instantiated in any 
	// order. Therefore it is good practice to look for dependent data modules
	// in the prepareProcess() method.

	@Override
	public String getProcessName() {
		return "Likelihood Threshold Detector";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void prepareProcess() {
	    
	    FilteredDetectionWriter fdw = new FilteredDetectionWriter( outputDataBlock, signalBands, targetIdentifier,
	    															this.estimatedPeakTimeAccuracy );
		DetectionFilter detFilter = new DetectionFilter( fdw, (int) ( 1000. * this.secondsBetweenDetections ) );
		
		// Create a Threshold object for each detection band (SignalBand), and associated guard bands.
		thresholds.clear();
		for( int signalIndex = 0; signalIndex < signalBands.size(); ++signalIndex ) {
			thresholds.add( new Threshold( detFilter, signalIndex,
											signalBands.get( signalIndex ),
											this.guardBandMappings.get( signalIndex )));
		}
		
	    normalizedDataBlock.addObserver( this, false ); // DG stop rethreading 24/11/2009
	}
	
	// ------------------------------------------------------------------------
	// Return the time in milliseconds required by this data process. This 
	// method is called by a data blocks that are registered with so that they
	// can tell when to purge old historical data.
	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		
		// Say for the sake of argument that we need 0 seconds of data.
		return 0;		
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		this.sampleRate = sampleRate;
	}
	
}
