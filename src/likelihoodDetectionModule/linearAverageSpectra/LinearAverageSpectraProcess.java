package likelihoodDetectionModule.linearAverageSpectra;

import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import likelihoodDetectionModule.LikelihoodDetectionUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * This is the PamProcess implementing the Linear Averaged Spectra processing block for the likelihoodDetectionModule.
 * 
 * @author Dave Flogeras
 *
 */
public class LinearAverageSpectraProcess extends PamProcess {

	// :: ---------------------------------------------------------------------
	// :: Private data

	private LikelihoodDetectionUnit likelihoodDetectionUnit;

	// A pointer to the raw audio data block.
	private FFTDataBlock fftDataBlock;
	
	// The output block of averaged spectra data.
	private PamDataBlock<AveragedSpectraDataUnit> spectraDataBlock;
	
	private java.util.Map< Integer, RealBlockAverage > averages;
	
	final int nAvg;
	final int fftHop;
	// :: ---------------------------------------------------------------------
	// :: Construction
	
	@SuppressWarnings("unchecked")
	public LinearAverageSpectraProcess( LikelihoodDetectionUnit ldu, PamDataBlock pdb, int nAvg, int fftHop ) {
		super( ldu, null );
		
		setCanMultiThread(false);
		
		likelihoodDetectionUnit = ldu;
		
		this.nAvg = nAvg;
		this.fftHop = fftHop;
		
		// Subscribe to parent
		
		fftDataBlock = (FFTDataBlock)pdb;
		setParentDataBlock( fftDataBlock ); 
		
		// Create the averaged spectra output data block.
		spectraDataBlock 
			= new PamDataBlock<AveragedSpectraDataUnit>( AveragedSpectraDataUnit.class, 
				likelihoodDetectionUnit.getUnitName(), this, 0 );
		spectraDataBlock.setChannelMap( pdb.getChannelMap() );
		
		// Add the data block for the overall data block list so that other 
		// modules are able to subscribe for the data.
		addOutputDataBlock( spectraDataBlock );
		
		averages = new java.util.HashMap< Integer, RealBlockAverage >();
	}
	
	// ---------------------------------------------------------------------
	// The pamStart() method is called by the controller to start the
	// process.

	@Override
	public void pamStart() {
        // Even though there is no functionality needed here, it must be
		// specified because it is abstract in the base class.				
	}

	// ---------------------------------------------------------------------
	// The pamStop() method is called by the controller to stop the
	// process.

	@Override
	public void pamStop() {
		// Even though there is no functionality needed here, it must be
		// specified because it is abstract in the base class.
	}

	@Override
	public String getProcessName() {
		return "Linear Average Spectra";
	}
	// ------------------------------------------------------------------------
	
	@Override
	public void prepareProcess() {
		
		// Obtain a pointer to the raw audio data.
		//rawDataBlock = PamController.getInstance().getRawDataBlock( 0 );
		
		if ( fftDataBlock == null ) {
			// what happens if this doesn't work?
			return;
		}
		
		int bit = 0;
		averages.clear();
		while( bit < 32 ) {
			
			int key = 1 << bit++; 
			if(( fftDataBlock.getChannelMap() & key ) != 0 ) {
				averages.put( key, new RealBlockAverage( this.nAvg ) ); 
			}
		}
		
		// Add this process as an observer of the raw data.
		fftDataBlock.addObserver( this , false ); // DG stop rethreading 24/11/2009

	}
	
	// ------------------------------------------------------------------------
	// Return the time in milliseconds required by this data process. This 
	// method is called by a data blocks that are registered with so that they
	// can tell when to purge old historical data.
	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		
		// Say for the sake of argument that we need 5 seconds of data.
		//return 5 * 1000;
	    return 0;
	}

	// --------------------------------------------------------------------------
	// Informs the PamObserver that new data have been added to the Observable 
	// class
  
	@Override
	public void newData( PamObservable o, PamDataUnit arg ) {
		
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		
		RealBlockAverage blockAverage = averages.get( arg.getChannelBitmap() );
		
		if ( blockAverage == null ) return;
		
		blockAverage.processData( arg.getTimeMilliseconds(),
						 fftDataUnit.getStartSample(),
				         fftDataUnit.getFftData() 
				         );
		if( blockAverage.averageReady() ) {
			// Create data block.
			AveragedSpectraDataUnit unit
				= new AveragedSpectraDataUnit( blockAverage.averageTimestamp(),
											   arg.getChannelBitmap(),
											   blockAverage.averageStartSample(),
											   blockAverage.NAvg() * fftHop );
			unit.setData( averages.get( arg.getChannelBitmap() ).average() );
			spectraDataBlock.addPamData( unit );
		}
		
	}
	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		this.setSampleRate(sampleRate, false);
	}
	
}
