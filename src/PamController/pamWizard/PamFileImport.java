package PamController.pamWizard;

import java.io.File;
import java.util.List;

import Acquisition.pamAudio.PamAudioFileFilter;

/**
 * Holds information on imported files. 
 * 
 * @author Jamie Macaulay
 */
public class PamFileImport   {
	
	private List<File> importedFiles;
	
	private boolean validSOundFiles = false;
	
	PamAudioFileFilter filter = new PamAudioFileFilter();

	public PamFileImport(List<File> importedFiles) {
		this.importedFiles = importedFiles;
	}

	
	/**
	 * Get the list of files that's been imported. 
	 * @return the list of imported files. 
	 */
	public List<File> getImportedFiles() {
		return importedFiles;
	}
	
	/**
	 * Check whether the imported files contain valid sound files. 
	 * @return true if valid sound files are present. 
	 */
	public boolean isValidSoundsFiles() {
		return validSOundFiles; 
	}


}
