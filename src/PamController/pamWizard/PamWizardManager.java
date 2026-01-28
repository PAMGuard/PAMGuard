package PamController.pamWizard;

import java.io.File;
import java.util.List;

import PamController.PamController;

/**
 * Manages the creation of automatic PAMGaurcd configurations. 
 */
public class PamWizardManager {

	public PamWizardManager(PamController pamController) {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Called whenever new files are imported into PamGuard via drag and drop or other methods. 
	 * @param files - a folder or file. These should be checked to see if they are audio files 
	 * that can be used to create a PAMGuard configuration.
	 */
	public void newImportedFiles(List<File> files) {
		
	}

}
