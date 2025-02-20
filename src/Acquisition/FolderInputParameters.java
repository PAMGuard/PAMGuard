package Acquisition;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;

/**
 * Control parameters for FolderInputSystem
 * @author Doug Gillespie
 * @see FolderInputSystem
 *
 */
public class FolderInputParameters extends FileInputParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 1;

	/**
	 * Default this to true. 
	 */
	public boolean subFolders = true;

	public boolean mergeFiles;

	private String[] selectedFileNames;

	/**
	 * @param systemType
	 */
	public FolderInputParameters(String systemType) {
		super(systemType);
	}

	@Override
	protected FolderInputParameters clone() {
		return (FolderInputParameters) super.clone();
	}

	/**
	 * Get the list of selected files as strings. If you want them as
	 * Files, call getselectedFileFiles()
	 * @return List of file paths in String format.
	 */
	public String[] getSelectedFiles() {
		return selectedFileNames;
	}

	/**
	 * Set the list of selected files
	 * @param selectedFiles
	 */
	public void setSelectedFiles(String[] selectedFiles) {
		this.selectedFileNames = selectedFiles;
	}

	/**
	 * Set the list of selected files. Note that these are now stored as strings
	 * to avoid some serialisation problems with some subclasses of io.File
	 * @param files
	 */
	public void setSelectedFiles(File[] files) {
		if (files == null) {
			this.selectedFileNames = null;
			return;
		}
		selectedFileNames = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			selectedFileNames[i] = files[i].getAbsolutePath();
		}
	}

	/**
	 * Get the list of selected Files, converted back to File objects
	 * from strings
	 * @return list of selected files.
	 */
	public File[] getSelectedFileFiles() {
		if (selectedFileNames == null) {
			return null;
		}
		File[] files = new File[selectedFileNames.length];
		for (int i = 0; i < selectedFileNames.length; i++) {
			files[i] = new File(selectedFileNames[i]);
		}
		return files;
	}

	@Override
	public PamParameterSet getParameterSet() {
		// if the user has not selected this system type, just return null
		if (!DaqSystemXMLManager.isSelected(systemType)) {
			return null;
		}

		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("selectedFileNames");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectedFileNames;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
}
