package fileOfflineData;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Make a list of files with the given file filter. 
 * @author dg50
 *
 */
public class OfflineFileList {
	
	private ArrayList<File> files = new ArrayList<>();
	private String folder;
	private FileFilter fileFilter;
	private boolean includeSubFolders;

	public OfflineFileList(String folder, FileFilter fileFilter, boolean includeSubFolders) {
		this.folder = folder;
		this.fileFilter = fileFilter;
		this.includeSubFolders = includeSubFolders;
		updateCatalog();
	}
	
	public int updateCatalog() {
		files.clear();
		File current = new File(this.folder);
		addFiles(current);
		return files.size();
	}

	private void addFiles(File current) {
		if (current.exists() == false) {
			return;
		}
		if (current.isFile() && checkFilter(current)) {
			/*
			 * This can only really happen if the root passed in is a file, not a 
			 * folder, since files within the folder structure will get 
			 * added from the loop below. 
			 */
			files.add(current);
		}
		else if (current.isDirectory()) {
			File[] filesList = current.listFiles();
			if (filesList == null) {
				return;
			}
			for (int i = 0; i < filesList.length; i++) {
				File aFile = filesList[i];
				if (aFile.isFile() && checkFilter(aFile)) {
					files.add(aFile);
				}
				else if (aFile.isDirectory() && includeSubFolders) {
					addFiles(aFile);
				}
			}
		}
	}

	/**
	 * Check that if there is a filter, the file is accepted. 
	 * @param aFile
	 * @return
	 */
	private boolean checkFilter(File aFile) {
		if (fileFilter == null) {
			return true;
		}
		return fileFilter.accept(aFile);
	}

	/**
	 * Get a list of files in the catalog as a simple string list. 
	 * @return files as strings
	 */
	public String[] asStringList() {
		if (files == null) {
			return null;
		}
		String[] str = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			str[i] = files.get(i).getAbsolutePath();
		}
		return str;
	}
}
