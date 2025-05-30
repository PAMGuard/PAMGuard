package PamUtils.worker.filelist;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

/**
 * Information about a list of files. 
 * @author dg50
 *
 */
public class FileListData<T extends File> implements Cloneable {

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
	
	/**
	 * Clear the file list. 
	 */
	public void clear() {
		fileList.clear();
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
	 * Remove duplicates from the file list, keeping only the first instance of each file name.
	 * If a preferred extension is provided, it will keep the file with that extension if duplicates exist.
	 * 
	 * @param preferredExtension - the preferred file extension to keep in case of duplicates.
	 */
	public void removeDuplicates(String preferredExtension) {
		fileList = (ArrayList<T>) filterDuplicateFiles(fileList,  preferredExtension);
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
	
	/**
	 * Sort using supplied comparator. 
	 * @param comparator
	 */
	public void sort(Comparator<T> comparator) {
		if (fileList == null) {
			return;
		}
		fileList.sort(comparator);
	}
	
	/**
	 * Sort using internal compartor of list type. 
	 */
	public void sort() {
		if (fileList == null) {
			return;
		}
		Collections.sort(fileList);
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

	@Override
	protected FileListData<T> clone() {
		try {
			// full clone of the array list (but not it's elements)
			FileListData<T> newList = (FileListData<T>) super.clone();
			newList.fileList = (ArrayList<T>) newList.fileList.clone();
			return newList;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	 /**
     * Filters a list of File objects to remove duplicates based on the name (excluding extension).
     * If duplicates exist, it keeps the one with the preferred extension. If none of the
     * duplicates have the preferred extension, it keeps the first encountered duplicate File object
     * from the original list.
     *
     * @param files              A list of File objects.
     * @param preferredExtension The preferred file extension (e.g., "pdf", "docx").
     * @return A new list of File objects with duplicates removed according to the specified rules.
     */
    public static List<? extends File> filterDuplicateFiles(List<? extends File> files, String preferredExtension) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>(); // Return an empty list if input is null or empty
        }

        String normalizedPreferredExtension = preferredExtension.startsWith(".")
                ? preferredExtension.substring(1)
                : preferredExtension;

        Map<String, List<File>> filesByBaseName = new HashMap<>();

        // Group File objects by their base name
        for (File file : files) {
            if (file == null) {
                continue; // Skip null File objects
            }
            String fileName = file.getName();
            if (fileName.trim().isEmpty()) {
                continue; // Skip files with empty names
            }

            String baseName = FilenameUtils.getBaseName(fileName);
            filesByBaseName.computeIfAbsent(baseName, k -> new ArrayList<>()).add(file);
        }

        List<File> result = new ArrayList<>();

        // Process each group of files
        for (Map.Entry<String, List<File>> entry : filesByBaseName.entrySet()) {
            List<File> duplicates = entry.getValue();

            if (duplicates.size() == 1) {
                result.add(duplicates.get(0)); // No duplicates, add the single file
            } else {
                File fileToKeep = null;
                // Check for preferred extension among duplicates
                for (File duplicateFile : duplicates) {
                    String currentFileName = duplicateFile.getName();
                    String extension = FilenameUtils.getExtension(currentFileName);
                    if (extension != null && extension.equalsIgnoreCase(normalizedPreferredExtension)) {
                        fileToKeep = duplicateFile;
                        break; // Found the preferred one
                    }
                }

                if (fileToKeep != null) {
                    result.add(fileToKeep);
                } else {
                    // No preferred extension found, keep the first one encountered in the original input list
                    // from this group of duplicates.
                    File firstInOriginalList = null;
                    int minIndex = Integer.MAX_VALUE;

                    for (File duplicateFileFromGroup : duplicates) {
                        int currentIndex = files.indexOf(duplicateFileFromGroup); // Find original index
                        if (currentIndex != -1 && currentIndex < minIndex) {
                            minIndex = currentIndex;
                            firstInOriginalList = duplicateFileFromGroup;
                        }
                    }

                    if (firstInOriginalList != null) {
                        result.add(firstInOriginalList);
                    } else if (!duplicates.isEmpty()){
                        // Fallback: Should not happen if duplicates list is populated from 'files'
                        // and 'files' is not modified concurrently.
                        // This could happen if a file in 'duplicates' was not in the original 'files' list
                        // (e.g. if 'files' was cleared or objects are not identical).
                        // As a robust fallback, add the first file from the current duplicates group.
                        result.add(duplicates.get(0));
                    }
                }
            }
        }
        return result;
    }

}
