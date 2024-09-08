package PamUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Create a list of files of a given type within a folder structure.
 *  
 * @author Doug Gillespie
 *
 */
public class FileList {

	public FileList() {
		
	}
	
	public ArrayList<File> getFileList(String folderName, String fileEnd, boolean includeSubfolders) {
		String[] fileEnds = {fileEnd};
		return getFileList(folderName, fileEnds, includeSubfolders);
	}
	
	public ArrayList<File> getFileList(String folderName, String[] fileEnds, boolean includeSubfolders) {

		PamFileFilter fileFilter;
		
		
		if (folderName == null || fileEnds == null || fileEnds.length < 1) {
			return null;
		}
		fileFilter = new PamFileFilter("", fileEnds[0]);
		for (int i = 1; i < fileEnds.length; i++) {
			fileFilter.addFileType(fileEnds[i]);
		}
		
		return getFileList(folderName, fileFilter, includeSubfolders);
		
	}
	
	public ArrayList<File> getFileList(String folderName, FileFilter fileFilter, boolean includeSubFolders) {
		ArrayList<File> fileList;
		if (folderName == null) {
			return null;
		}
		File rootFolder = new File(folderName);
		if (!rootFolder.exists()) {
			return null;
		}
		fileList = new ArrayList<File>();
		
		fileList = addFiles(rootFolder, fileList, fileFilter, includeSubFolders);
		
		return fileList;
	}
	
	private ArrayList<File> addFiles(File folder, ArrayList<File> fileList, 
			FileFilter fileFilter, boolean includeSubFolders) {

//		System.out.println("Adding files from folder " + folder.getAbsolutePath());
		// first go through all the files in this folder
		File[] newFiles = folder.listFiles(fileFilter);
		if (newFiles == null) {
			return fileList; // nothing to do here. 
		}
		for (int i = 0; i < newFiles.length; i++) {
			if (!newFiles[i].isDirectory()) {
				fileList.add(newFiles[i]);
				//				System.out.println("  adding file " + newFiles[i].getName());
			}
			else if (includeSubFolders) {
				fileList = addFiles(newFiles[i], fileList, fileFilter, includeSubFolders);
			}
		}

		return fileList;
	}
}
