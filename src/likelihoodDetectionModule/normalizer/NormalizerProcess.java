package likelihoodDetectionModule.normalizer;

import likelihoodDetectionModule.LikelihoodDetectionUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObservable;
import java.util.ArrayList;
import likelihoodDetectionModule.GuardBand;
import likelihoodDetectionModule.SignalBand;
import likelihoodDetectionModule.spectralEti.SpectralEtiDataUnit;

/**
 *  This PamProcess implements the normalizer of the likelihood detector.  The user can select either a DecayingAverage
 *  (exponential), or SplitWindow algorithm.
 *  
 *  Using the signal/background time constants from the bands passed in, the normalizer produces an estimate of the
 *  signal/background levels present in the input spectral band data. 
 * 
 * @author Dave Flogeras
 *
 */
public class NormalizerProcess extends PamProcess {

	public enum NormalizerAlgorithm {
		DecayingAverage,
		SplitWindow
	}
	
	private java.util.Map< Integer, Normalizer > normalizers;
	
	private LikelihoodDetectionUnit likelihoodDetectionUnit;
	
	private PamDataBlock<SpectralEtiDataUnit> spectralEtiDataBlock;

	private PamDataBlock< NormalizedDataUnit > normalizedDataBlock;
	
	final private ArrayList< SignalBand > signalBands;
	final private ArrayList< GuardBand > guardBands;
	final double timeResolution;
	final NormalizerAlgorithm normalizer;
	final double referenceGain;
	
	// :: -----------------------------------------------------------------------
	// :: Object Construction
	
	public NormalizerProcess( LikelihoodDetectionUnit ldu,
			                  PamDataBlock pdb,
			                  double referenceGain,
			                  NormalizerAlgorithm normalizer,
			                  ArrayList< SignalBand > signalBands,
			                  ArrayList< GuardBand > guardBands,
			                  double timeResolution
			                  ) {
		super( ldu, null );
		
		setCanMultiThread(false);
				
		this.referenceGain = referenceGain;
		normalizers = new java.util.HashMap< Integer, Normalizer >();
	
		this.signalBands = signalBands;
		this.guardBands = guardBands;
		this.timeResolution = timeResolution;
		this.normalizer = normalizer;
		
		likelihoodDetectionUnit = ldu;
		
		// Create the normalized signal output data block.
		normalizedDataBlock 
			= new PamDataBlock< NormalizedDataUnit >( NormalizedDataUnit.class, 
				likelihoodDetectionUnit.getUnitName(), this, 0 );
		normalizedDataBlock.setChannelMap( pdb.getChannelMap() );
		
	    spectralEtiDataBlock = pdb;
	    setParentDataBlock( spectralEtiDataBlock ); 
	    
		// Add the data block for the overall data block list so that other
		// modules are able to subscribe for the data.
		addOutputDataBlock( normalizedDataBlock );
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
	

	@Override
	public String getProcessName() {
		return "Likelihood Detector Normalizer";
	}
	
   @Override
@SuppressWarnings("unchecked")
   public void prepareProcess() {
    
	   //NormalizerProcessTimeConstants[] timeConstants = new NormalizerProcessTimeConstants[ 2 ];
	   //timeConstants[ 0 ] = new NormalizerProcessTimeConstants( 0.1, 0.3 );
	   //timeConstants[ 1 ] = new NormalizerProcessTimeConstants( 0.1, 0.9 );
	   ArrayList< NormalizerProcessTimeConstants > timeConstantPairs = new ArrayList< NormalizerProcessTimeConstants >();
	   for( SignalBand s : signalBands ) {
		   timeConstantPairs.add( new NormalizerProcessTimeConstants( s.signalSeconds, s.backgroundSeconds ));
	   }
	   for( GuardBand g : guardBands ) {
		   timeConstantPairs.add( new NormalizerProcessTimeConstants( g.signalSeconds, g.backgroundSeconds ));
	   }
    
    
	   // Must call this before using sampleRate
	   spectralEtiDataBlock.addObserver( this , false); // DG stop rethreading 24/11/2009   

	   // Calculate the SplitWindow widths from the time constants
	   java.util.ArrayList< SplitWindowNormWidths > splitWidths = new java.util.ArrayList<SplitWindowNormWidths>();
	   for( NormalizerProcessTimeConstants c : timeConstantPairs ) {
		   splitWidths.add( new SplitWindowNormWidths( c.signalTimeConstant,
				   										c.backgroundTimeConstant,
				   										timeResolution ));
	   }
    
	   // Calculate the exponential average alphas from the time Constants
	   java.util.ArrayList< DecayingAverageNormAlphas > decayAlphas = new java.util.ArrayList< DecayingAverageNormAlphas >();
	   for( NormalizerProcessTimeConstants c : timeConstantPairs ) {
		   decayAlphas.add( new DecayingAverageNormAlphas( c.signalTimeConstant,
				   											c.backgroundTimeConstant,
				   											timeResolution ));
	   }  
    
	   int bit = 0;
	   spectralEtiDataBlock.clearAll();
	   while( bit < 32 ) {
		
		   int key = 1 << bit++; 
		   if(( spectralEtiDataBlock.getChannelMap() & key ) != 0 ) {

			   if( normalizer == NormalizerAlgorithm.SplitWindow ) {
				   normalizers.put( key, new SplitWindowNorm( splitWidths ) );
			   }
			   else {
				   normalizers.put( key, new DecayingAverageNorm( decayAlphas, referenceGain ));
			   }
		   }
	   }
	
  }

  @Override
public void newData( PamObservable o, PamDataUnit unit ) {
	
    SpectralEtiDataUnit dataUnit = (SpectralEtiDataUnit) unit;
	
	PamDataUnit output = normalizers.get( unit.getChannelBitmap() ).Process( dataUnit );
	if( null != output ) {
      output.setChannelBitmap( unit.getChannelBitmap() );
	  normalizedDataBlock.addPamData( (NormalizedDataUnit)output );
	}

  }

  @Override
public long getRequiredDataHistory( PamObservable o, Object arg ) {
	  return 0;
  }

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		this.sampleRate = sampleRate;
	}
}
