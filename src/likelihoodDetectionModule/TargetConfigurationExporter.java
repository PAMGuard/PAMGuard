package likelihoodDetectionModule;

import java.util.*;
import java.io.*;

/**
 * The Class TargetConfigurationExporter. This is used to export a target configuration
 * to disk as an ASCII file.
 */
public class TargetConfigurationExporter {

	/** The file to save to. */
	private File file;
	
	/** The filename. */
	String filename;
	
	/** The formatter used to generate the ASCII file. */
	Formatter formatter;
	
	/**
	 * Instantiates a new target configuration exporter.
	 * 
	 * @param absoluteFileName The absolute file name.
	 */
	public TargetConfigurationExporter( String absoluteFileName ) {
		this.filename = absoluteFileName;
	}

	// ------------------------------------------------------------------------
	
	/**
	 * Do export.
	 * 
	 * @param config The configuration to export.
	 * 
	 * @return true, if successful
	 */
	public boolean doExport( TargetConfiguration config ) {
		file = new File( this.filename );
		if ( file == null ) return false;
		
		try {
			formatter = new Formatter( file );	
		}
		catch( FileNotFoundException e ) {
			return false;	
		}
		
		formatter.format("target %30s %9.5f %9.5f %30s %9.5f %9d %9d %n",
				config.getIdentifier(), 
				config.getTimeResolution(),
				config.getFrequencyResolution(),
				TargetConfiguration.algorithmToString( config.getAlgorithm() ),
				config.getSecondsBetweenDetections(),
				config.getSignalBands().size(),
				config.getGuardBands().size() );
		
		for ( int i = 0; i < config.getSignalBands().size(); i++ ) {
			SignalBand signal = config.getSignalBands().get(i);
			formatter.format( "%30s %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %n",
					signal.identifier,
					signal.inBandThresholdDb,
					signal.guardBandThresholdDb,
					signal.startFrequencyHz,
					signal.endFrequencyHz,
					signal.backgroundSeconds,
					signal.signalSeconds );
		}
		
		for ( int i = 0; i < config.getGuardBands().size(); i++ ) {
			GuardBand guard = config.getGuardBands().get(i);
			formatter.format( "%30s %9.5f %9.5f %30s %9.5f %9.5f %n",
					guard.identifier,
					guard.startFrequencyHz,
					guard.endFrequencyHz,
					guard.associatedSignalBandIdentifier,
					guard.backgroundSeconds,
					guard.signalSeconds );
		}
		
		formatter.flush();
		formatter.close();
		
		return true;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * Export a bunch of target configurations at once.
	 * 
	 * @param configs the configs
	 * 
	 * @return true, if successful
	 */
	public boolean export ( ArrayList<TargetConfiguration> configs ) {
		for ( TargetConfiguration config : configs ) {
			if ( ! doExport( config ) ) return false;	
		}
		
		return true;	
	}
}
