package likelihoodDetectionModule.spectralEti;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamDataBlock;
import likelihoodDetectionModule.LikelihoodDetectionUnit;
import likelihoodDetectionModule.linearAverageSpectra.AveragedSpectraDataUnit;
import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.GuardBand;
import java.util.ArrayList;

/**
 * The PamProcess which implements the spectralEti signal processing.  This block is responsible for
 * calculating the average energy contained within a frequency range (or band).
 * 
 * @author Dave Flogeras
 *
 */
public class SpectralEtiProcess extends PamProcess {
	
	// :: ---------------------------------------------------------------------
	// :: Private data
	private LikelihoodDetectionUnit likelihoodDetectionUnit;
		
	private PamDataBlock<SpectralEtiDataUnit> etiDataBlock;
	
	// The output block of averaged spectra data.	
	private PamDataBlock<AveragedSpectraDataUnit> spectraDataBlock;
		
	private java.util.ArrayList< SpectralBand > etiBands;
	
	private ArrayList< SignalBand > signalBands;
	private ArrayList< GuardBand > guardBands;
	
	// :: ---------------------------------------------------------------------
	// :: Construction
	
	public SpectralEtiProcess( LikelihoodDetectionUnit ldu, PamDataBlock pdb,
								ArrayList< SignalBand > signalBands,
								ArrayList< GuardBand > guardBands
								) {
		super( ldu, null );
		
		setCanMultiThread(false);
		
		etiBands = new java.util.ArrayList< SpectralBand >();

		this.signalBands = signalBands;
		this.guardBands = guardBands;
		
		likelihoodDetectionUnit = ldu;
		
		// Create the eti output data block.
		etiDataBlock 
			= new PamDataBlock<SpectralEtiDataUnit>( SpectralEtiDataUnit.class, 
				likelihoodDetectionUnit.getUnitName(), this, 0 );
		etiDataBlock.setChannelMap( pdb.getChannelMap() );
		
		spectraDataBlock = pdb;
		
		// This is the immediate source of data.
		setParentDataBlock( spectraDataBlock ); 
		
		// Add the data block for the overall data block list so that other
		// modules are able to subscribe for the data.
		addOutputDataBlock( etiDataBlock );
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
	
	
	private double[] calculateEtis( double[] input ) {
		
		double[] ret = new double[ etiBands.size() ];
		for( int etiIndex = 0; etiIndex < etiBands.size(); ++etiIndex ) {
			ret[ etiIndex ] = etiBands.get( etiIndex ).calculate( input );
		}
		return ret;
		
	}
	
	// ------------------------------------------------------------------------
	// This class is-a PamObserver, which enables this object 
	// to subscribe for notification of new data. The PAMGUARD profiles 
	// monitors the CPU usage of each module, so when the method
	// PamObserver.update() is called, the PamProcess handles profiling tasks
	// and calls this newData() method when it is done.
	
	@Override
	public void newData( PamObservable o, PamDataUnit arg ) {
		
		AveragedSpectraDataUnit dataUnit = (AveragedSpectraDataUnit) arg;
		
		SpectralEtiDataUnit unit
		  = new SpectralEtiDataUnit( arg.getTimeMilliseconds(),
									 arg.getChannelBitmap(),
									 dataUnit.getStartSample(),
									 dataUnit.getSampleDuration() );

		unit.setData( calculateEtis( dataUnit.getData() ));
		etiDataBlock.addPamData( unit );
		
	}
	
	// ------------------------------------------------------------------------
	// The PAMGUARD modules are not guaranteed to be instantiated in any 
	// order. Therefore it is good practice to look for dependent data modules
	// in the prepareProcess() method.

	@Override
	public String getProcessName() {
		return "Likelihood Spectral Eti";
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void prepareProcess() {
		
		// Add this process as an observer of the raw data.
		spectraDataBlock.addObserver( this , false); // DG stop rethreading 24/11/2009
		
		etiBands.clear();
		// sampleRate is set when we addObserver, must do this after that call
		for( SignalBand s : signalBands ) {
			etiBands.add( new SpectralBand( s.startFrequencyHz, s.endFrequencyHz, sampleRate ));
		}
		for( GuardBand g : guardBands ) {
			etiBands.add( new SpectralBand( g.startFrequencyHz, g.endFrequencyHz, sampleRate ));
		}
	}
	
	// ------------------------------------------------------------------------
	// Return the time in milliseconds required by this data process. This 
	// method is called by a data blocks that are registered with so that they
	// can tell when to purge old historical data.
	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		
		return 0;
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		this.sampleRate = sampleRate;
	}
}
