package likelihoodDetectionModule;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamguardMVC.PamRawDataBlock;

/**
 * The class LikelihoodFFTController manages the automatic creation of any
 * FFT processes that are required to support the target configurations. It will search
 * out in the PamGuard system for existing FFT processes that have parameters that
 * match what is required, but if it doesn't find any it will create one of its own
 * FFT processes to manage one or more processing streams. In order for the 
 * object to be able to add new processes to the PamGuard system, it has to inherit
 * the PamControlledUnit interface.
 */
public class LikelihoodFFTController extends PamControlledUnit {
	
	/** The fft processes. */
	private ArrayList<LikelihoodFFTProcess> fftProcesses;
	
	/** The block of raw acoustic data that the extra fft processes will need to
	 * subscribe to. */
	private PamRawDataBlock block;
	
	/**
	 * Instantiates a new likelihood fft controller.
	 * 
	 * @param controllerName the controller name
	 * @param block the block of raw audio data that the new fft processes will subscribe to
	 */
	public LikelihoodFFTController( String controllerName, PamRawDataBlock block ) {
		super( "LikelihoodFFTController", controllerName );
		fftProcesses = new ArrayList<LikelihoodFFTProcess>();
		this.block = block;
	}
	
	/**
	 * Request the controller to crete a new fft process based on the specified
	 * parameters.
	 * 
	 * @param params The required parameters.
	 */
	public void createFFTProcess( LikelihoodFFTParameters params ) {
		// Is there an FFT process running for these parameters already?
		for ( int i = 0; i < fftProcesses.size(); ++i ) {
			if ( fftProcesses.get( i ).getParameters().equals( params ) ) {
				//  Yes, there is already a process for this set of parameters running.
				fftProcesses.get( i ).setInUse( true );
				return;
			}
		}
		
		// Create an FFT process.
		LikelihoodFFTProcess fftProcess = new LikelihoodFFTProcess( this, block, params );
		addPamProcess( fftProcess );
		fftProcesses.add( fftProcess );
	}

	/**
	 * Get an fft process that matches the supplied parameters.
	 * 
	 * @param params The parameters for which an fft process is requested.
	 * 
	 * @return The matching fft process, or null if a match is not found.
	 */
	public LikelihoodFFTProcess getProcess( LikelihoodFFTParameters params ) {
		
		for( LikelihoodFFTProcess l : fftProcesses ) {
			if( l.getParameters().equals( params ) ) {
				return l;
			}
		}
		return null;
	}
	
	/**
	 * Removes the all fft processes.
	 */
	public void removeAllFFTProcesses() {
		for ( int i = 0; i < fftProcesses.size(); ++i ) {
			LikelihoodFFTProcess p = fftProcesses.get(i);
			// Destroying the process will set the parentDatablock of the FFT Process to null
			// and removes all of the output data blocks.
			p.destroyProcess();
			
			// Remove the process from the pam controlling unit.
			removePamProcess( p );	
		}
		
		// This actually clears the pointers from the container.
		fftProcesses.clear();
	}
	
	/**
	 * Mark fft processes unused.
	 */
	public void markFFTProcessesUnused() {
		for ( int i = 0; i < fftProcesses.size(); ++i ) {
			LikelihoodFFTProcess p = fftProcesses.get(i);
			p.setInUse( false );
		}
	}
	
	/**
	 * Removes the unused processes.
	 */
	public void removeUnusedProcesses() {
		for ( int i = 0; i < fftProcesses.size(); ++i ) {
			LikelihoodFFTProcess p = fftProcesses.get(i);
			if ( p.getInUse() == false ) {
				p.destroyProcess();
				removePamProcess( p );
				fftProcesses.remove( i );
				i = 0;
			}
		}
	}
}
