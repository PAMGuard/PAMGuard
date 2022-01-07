package PamUtils.worker.filelist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Information about a list of files. 
 * @author dg50
 *
 */
public class FileListData<T extends File> {

	private ArrayList<T> fileList = new ArrayList<T>();
	
	private int foldersSearched;

	private String[] rootList;

	private boolean subFolders;

	private boolean useOldIfPossible; 
	
	public FileListData(String[] rootList, boolean subFolders, boolean useOldIfPossible) {
		this.rootList = rootList;
		this.subFolders = subFolders;
		this.useOldIfPossible = useOldIfPossible;
	}

	public int addFile(T aFile) {
		fileList.add(aFile);
		return fileList.size();
	}
	
	public ListIterator<T> getFileIterator() {
		return fileList.listIterator();
	}
	
	public int getFileCount() {
		return fileList.size();
	}
	
	public ArrayList<T> getListCopy() {
		ArrayList<T> listCopy = (ArrayList<T>) fileList.clone();
		return listCopy;
	}
	
	public void addFolder() {
		foldersSearched++;
	}

	public int getFoldersSearched() {
		return foldersSearched;
	}

	public String[] getRootList() {
		return rootList;
	}

	public boolean isSubFolders() {
		return subFolders;
	}

	public boolean isUseOldIfPossible() {
		return useOldIfPossible;
	}

	/**
	 * Sort the file list, sorting only by name, not path.
	 */
	public void sortFileList() {
		Collections.sort(fileList, new FileNameCompare());
	}
	
	/**
	 * Sort the file list, sorting by the full path.
	 */
	public void sortFilePath() {
		Collections.sort(fileList, new FilePathCompare());
	}
	
	private class FileNameCompare implements Comparator<T> {

		@Override
		public int compare(T f1, T f2) {
			if (f1 == null) {
				return -1;
			}
			if (f2 == null) {
				return +1;
			}
			String n1 = f1.getName();
			String n2 = f2.getName();
			return n1.compareTo(n2);
		}
	}

	private class FilePathCompare implements Comparator<T> {

		@Override
		public int compare(T f1, T f2) {
			if (f1 == null) {
				return -1;
			}
			if (f2 == null) {
				return +1;
			}
			String n1 = f1.getPath();
			String n2 = f2.getPath();
			return n1.compareTo(n2);
		}
	}
}
