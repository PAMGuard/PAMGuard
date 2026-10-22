package dataMap.filemaps;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;

public class OfflineFileParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public static final int MAX_RECENT_FILES = 20;
	
	/**
	 * Enable offline file access
	 */
	public boolean enable;
	
	/**
	 * include sub folders
	 */
	public boolean includeSubFolders;
	
	/**
	 * Reference to wherever the offline files are. This is always a folder, and is
	 * used both as the search root when no explicit selection has been made and as
	 * the place the serialised file map is written to.
	 */
	public String folderName;

	/**
	 * Explicit list of selected files and / or folders. If this is null or empty
	 * then the whole of folderName is used, which is the historic behaviour. If it
	 * is set then only these files (and the contents of any folders in the list)
	 * are mapped, so that a user who selected twenty files out of a folder of a
	 * hundred gets twenty.
	 */
	private String[] selectedFiles;

	/**
	 * Recently used folders, most recent first, so that the dialog can offer them
	 * in a drop down list in the same way the real time file panel does.
	 */
	private ArrayList<String> recentFiles;

	@Override
	public OfflineFileParameters clone()  {
		try {
			OfflineFileParameters newParams = (OfflineFileParameters) super.clone();
			if (recentFiles != null) {
				newParams.recentFiles = new ArrayList<String>(recentFiles);
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * The list of recently used folders, most recent first. Never null, though it
	 * may be empty, since settings saved before this list existed won't have one.
	 * @return list of recently used folders.
	 */
	public ArrayList<String> getRecentFiles() {
		if (recentFiles == null) {
			recentFiles = new ArrayList<String>();
		}
		return recentFiles;
	}

	/**
	 * Move a folder to the top of the list of recently used folders.
	 * @param folder folder name. Null and empty names are ignored.
	 */
	public void addRecentFile(String folder) {
		if (folder == null || folder.length() == 0) {
			return;
		}
		ArrayList<String> recent = getRecentFiles();
		recent.remove(folder);
		recent.add(0, folder);
		while (recent.size() > MAX_RECENT_FILES) {
			recent.remove(recent.size()-1);
		}
	}

	/**
	 * Get the list of explicitly selected files and folders.
	 * @return list of file paths, or null if the whole of folderName is to be used.
	 */
	public String[] getSelectedFiles() {
		return selectedFiles;
	}

	/**
	 * Set the list of explicitly selected files and folders.
	 * @param selectedFiles list of file paths. Null or empty to use the whole folder.
	 */
	public void setSelectedFiles(String[] selectedFiles) {
		if (selectedFiles != null && selectedFiles.length == 0) {
			selectedFiles = null;
		}
		this.selectedFiles = selectedFiles;
	}

	/**
	 * Set the list of explicitly selected files and folders. Note that these are
	 * stored as strings since the file choosers return subclasses of File which are
	 * not serialisable.
	 * @param files selected files and folders.
	 */
	public void setSelectedFiles(File[] files) {
		if (files == null || files.length == 0) {
			this.selectedFiles = null;
			return;
		}
		String[] names = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			names[i] = files[i].getAbsolutePath();
		}
		this.selectedFiles = names;
	}

	/**
	 * Get the list of explicitly selected files and folders, converted back to File
	 * objects.
	 * @return list of files, or null if the whole of folderName is to be used.
	 */
	public File[] getSelectedFileFiles() {
		if (selectedFiles == null) {
			return null;
		}
		File[] files = new File[selectedFiles.length];
		for (int i = 0; i < selectedFiles.length; i++) {
			files[i] = new File(selectedFiles[i]);
		}
		return files;
	}

	/**
	 * Is there an explicit list of files and folders to use ?
	 * @return true if a selection has been made.
	 */
	public boolean hasSelection() {
		return selectedFiles != null && selectedFiles.length > 0;
	}

	/**
	 * Does the selection contain individual files rather than only folders ? Sub
	 * folder searching is meaningless for a list of individual files.
	 * @return true if at least one entry in the selection is not a folder.
	 */
	public boolean isFileSelection() {
		if (!hasSelection()) {
			return false;
		}
		for (int i = 0; i < selectedFiles.length; i++) {
			if (new File(selectedFiles[i]).isDirectory() == false) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the list of search roots, i.e. the explicit selection if there is one,
	 * otherwise the single folder name.
	 * @return list of files and folders to search, or null if nothing is set.
	 */
	public String[] getSearchList() {
		if (hasSelection()) {
			return selectedFiles;
		}
		if (folderName == null) {
			return null;
		}
		return new String[]{folderName};
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		try {
			Field field = this.getClass().getDeclaredField("selectedFiles");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return selectedFiles;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
	
}
