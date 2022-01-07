package PamController;

import java.io.File;

import PamUtils.FileFunctions;

/**
 * Some static functions to handle PAMGuad default folder and 
 * to create them as and when necessary. 
 * @author dg50
 *
 */
public class PamFolders {

	/**
	 * Check and create a full folder name. 
	 * @param folderName full folder name
	 * @param create create if necessary
	 * @return true if folder exists and is OK.(can only return true, since exception thrown otherwise) 
	 */
	public static boolean checkFolder(String fullName, boolean create) throws PamFolderException{
		if (fullName == null) {
			throw  new PamFolderException("null folder name. You must specify a name. Try calling PamFolders.getDefaultProjectFolder()");
		}
		File fullFile = new File(fullName);
		if (create) {
			if (fullFile.exists() == false) {
				if (fullFile.mkdir() == false) {
					throw  new PamFolderException("Unable to create folder " + fullFile);
				};
			}
		}
		if (fullFile.exists() == false) {
			throw  new PamFolderException("Folder " + fullFile + " does not exist");
		};
		return true;
	}
	
	/**
	 * Get and create a sub-folder in the main project folder directory.
	 * @param subFolderName name of sub folder (without any separator characters). 
	 * @param create flag to say create the folder if it doesn't exist. 
	 * @return full path name for the created folder. 
	 */
	public static String getProjectFolder(String subFolderName, boolean create) throws PamFolderException{
		String fullName = getDefaultProjectFolder() + File.separator + subFolderName;
		if (create) {
			checkFolder(fullName, create);
		}
		return fullName;
	}


	/**
	 * Get a file or folder to pass to a file chooser, which may be an existing file (e.g.
	 * if opening the database dialog and the database already exists), otherwise works backwards
	 * down the tree until at least something exists, otherwise starts in the folder containing the 
	 * configuration file or database.
	 * @param fileName Initial hopes of what the file or folder is. 
	 * @return closest possible path based on startPlan, or the folder with the psf. 
	 */
	public static File getFileChooserPath(String fileName) {
		File startFile = null;
		if (fileName != null) {
			startFile = new File(fileName);
		}
		return getFileChooserPath(startFile);
	}
	
	/**
	 * Get a file or folder to pass to a file chooser, which may be an existing file (e.g.
	 * if opening the database dialog and the database already exists), otherwise works backwards
	 * down the tree until at least something exists, otherwise starts in the folder containing the 
	 * configuration file or database.
	 * @param startPlan Initial hopes of what the file or folder is. 
	 * @return closest possible path based on startPlan, or the folder with the psf. 
	 */
	public static File getFileChooserPath(File startPlan) {
		while (startPlan != null) {
			if (startPlan.exists()) {
				return startPlan;
			}
			else {
				startPlan = startPlan.getParentFile();
			}
		}
		
		startPlan = new File(getDefaultProjectFolder());
		while (startPlan != null) {
			if (startPlan.exists()) {
				return startPlan;
			}
			else {
				startPlan = startPlan.getParentFile();
			}
		}
		/*
		 * Will get here if there was no psfx file. 
		 * I don't really see how this can happen, but ...
		 */
		return new File(PamFolders.getHomeFolder());
	}

	
	/**
	 * Get a default folder for the project, that can be used
	 * when other folder names are null. It will try to set itself to
	 * the folder containing the psf or database file, failing that it will 
	 * return the 'user.home'/pamguard folder. 
	 * @return a default folder for the configuation. 
	 */
	public static String getDefaultProjectFolder() {
		PamController pamController = PamController.getInstance();
		if (pamController != null) {
			String psf = pamController.getPSFNameWithPath();
			if (psf != null) {
				File setFile = new File(psf);
				String parent = setFile.getParent();
				if (parent != null) {
					File parentFile = new File(parent);
					if (parentFile.isDirectory()) {
						return parent;
					}
				}
			}
		}
		/*
		 *  if we get here, it failed to get a parent from the psf / database
		 *  so go for a default location...
		 */
		return getHomeFolder();
	}

	/**
	 * Get and / or create the PAMGuard home folder. This is 
	 * in user/Pamguard, e.g. C:\Users\**username**\Pamguard
	 */
	public static String getHomeFolder() {
		String homeFolder = System.getProperty("user.home");
		homeFolder += File.separator + "Pamguard";
		// now check that folder exists
		File f = FileFunctions.createNonIndexedFolder(homeFolder);
		return homeFolder;
	}



}
