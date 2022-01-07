package likelihoodDetectionModule;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * The TargetConfigurationImporter is used to import a target configuration from an ASCII
 * file that have been saved by the TargetConfigurationExporter.
 * 
 * @see TargetConfigurationExporter
 * 
 */
public class TargetConfigurationImporter {

	/** The settings. */
	private AcquisitionSettings settings;
	
	/** The channel map. */
	private int channelMap;
	
	/** The filename. */
	private String filename;
	
	/**
	 * Instantiates a new target configuration importer.
	 * 
	 * @param fileName the file name
	 * @param settings the settings
	 * @param channelMap the channel map
	 */
	TargetConfigurationImporter( String fileName, AcquisitionSettings settings, int channelMap ) {
		this.settings = settings;
		this.channelMap = channelMap;
		this.filename = fileName;
	}
	
	/**
	 * Do import.
	 * 
	 * @return the target configuration
	 */
	public TargetConfiguration doImport() {
		
		BufferedReader reader;
		
		try {
			reader = new BufferedReader( new FileReader( filename ) );
		}
		catch ( FileNotFoundException e ) {
			return null;	
		}
		
		TargetConfiguration config = new TargetConfiguration( this.settings, this.channelMap );
		
		try {
			String line = new String();
			line = reader.readLine();

			Scanner scanner = new Scanner( line );
			
			// Eat target token.
			String targetToken = scanner.next();
			
			// Consume the name of the target configuration.
			String configIdentifier = new String();
			while ( ! scanner.hasNextDouble() ) {
				configIdentifier += scanner.next();
				configIdentifier += " ";
			}
			configIdentifier = configIdentifier.trim();
			
			config.setIdentifier( configIdentifier );
			config.setTimeResolution( scanner.nextDouble() );
			config.setFrequencyResolution( scanner.nextDouble() );
			
			String algorithmString = new String();
			while ( ! scanner.hasNextDouble() ) {
				algorithmString += scanner.next();
				algorithmString += " ";
			}
			algorithmString = algorithmString.trim();

			config.setAlgorithm(TargetConfiguration.algorithmFromString(algorithmString));
			
			config.setSecondsBetweenDetections( scanner.nextDouble() );
			
			int numberSignalBands = scanner.nextInt();
			int numberGuardBands = scanner.nextInt();
			
			ArrayList<SignalBand> signalBands = new ArrayList<SignalBand>();
			
			for ( int i = 0; i < numberSignalBands; ++i ) {
				SignalBand signal = new SignalBand( this.settings, config.getFFTParameters() );
				String bandLine = new String();
				bandLine = reader.readLine();
				scanner = new Scanner( bandLine );
				
				String idString = new String();
				while ( ! scanner.hasNextDouble() ) {
					idString += scanner.next();
					idString += " ";
				}
				idString = idString.trim();
				
				signal.identifier = idString;
				signal.inBandThresholdDb = scanner.nextDouble();
				signal.guardBandThresholdDb = scanner.nextDouble();
				signal.startFrequencyHz = scanner.nextDouble();
				signal.endFrequencyHz = scanner.nextDouble();
				signal.backgroundSeconds = scanner.nextDouble();
				signal.signalSeconds = scanner.nextDouble();
				
				signalBands.add( signal );	
			}
			
			config.setSignalBands(signalBands);
			
			ArrayList<GuardBand> guardBands = new ArrayList<GuardBand>();
			
			for ( int i = 0; i < numberGuardBands; ++i ) {
				GuardBand guard = new GuardBand( this.settings, config.getFFTParameters() );
				String bandLine = new String();
				bandLine = reader.readLine();
				scanner = new Scanner( bandLine );
				
				String idString = new String();
				while ( ! scanner.hasNextDouble() ) {
					idString += scanner.next();
					idString += " ";
				}
				idString = idString.trim();
				
				guard.identifier = idString;
				guard.startFrequencyHz = scanner.nextDouble();
				guard.endFrequencyHz = scanner.nextDouble();
				
				String assString = new String();
				while ( ! scanner.hasNextDouble() ) {
					assString += scanner.next();
					assString += " ";
				}
				assString = assString.trim();
				guard.associatedSignalBandIdentifier = assString;
				guard.backgroundSeconds = scanner.nextDouble();
				guard.signalSeconds = scanner.nextDouble();
				
				guardBands.add( guard );
			}
			
			config.setGuardBands( guardBands );
			
		}
		catch ( IOException e ) {
			return null;	
		}
		
		return config;
	}
}
