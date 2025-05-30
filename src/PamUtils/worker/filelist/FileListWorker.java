package PamUtils.worker.filelist;

import java.awt.Window;
import java.io.File;
import javax.swing.SwingUtilities;

import PamUtils.PamFileFilter;
import PamUtils.worker.PamWorkProgressMessage;
import PamUtils.worker.PamWorkWrapper;
import PamUtils.worker.PamWorker;
import PamguardMVC.debug.Debug;

/**
 * Class to list files in one or more directories. 
 * 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public abstract class FileListWorker<T extends File> implements PamWorkWrapper<FileListData<T>>{

	private PamFileFilter fileFilter;
	private String[] fileList;
	private boolean subFolders;
	private FileListData<T> oldFileList;
	private FileListUser<T> fileListUser;
	private boolean useOldIfPossible;
	private long searchStartTime;
	
	/**
	 * The current PAM worker. 
	 */
	private PamWorker<FileListData<T>> worker;

	public FileListWorker(FileListUser<T> fileListUser, String fileType) {
		this(fileListUser, new PamFileFilter(fileType + " files", fileType));
	}

	public FileListWorker(FileListUser<T> fileListUser, PamFileFilter fileFilter) {
		this.fileFilter = fileFilter;
		this.fileListUser = fileListUser;
	}

	/**
	 * Make a list for a single file or root directory
	 * @param parentFrame
	 * @param root
	 * @param subFolders
	 * @param useOldIfPossible
	 */
	public final void startFileListProcess(Window parentFrame, String root, boolean subFolders, boolean useOldIfPossible) {
		String[] rootList = new String[1];
		rootList[0] = root;
		startFileListProcess(parentFrame, rootList, subFolders, useOldIfPossible);
	}
	
	/**
	 * Make a list from multiple files or root directories. 
	 * @param parentFrame
	 * @param rootList
	 * @param subFolders
	 * @param useOldIfPossible
	 */
	public final void startFileListProcess(Window parentFrame, String[] rootList, boolean subFolders, boolean useOldIfPossible) {
		this.fileList = rootList;
		this.subFolders = subFolders;
		this.useOldIfPossible = useOldIfPossible;
		for (int i = 0; i < rootList.length; i++) {
			Debug.out.println(">>>>>>>>Starting file search in " + rootList[i]);
		}
		if (noChange(rootList, subFolders, useOldIfPossible)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					taskFinished(oldFileList);
				}
			});
			return;
		}
		
		/**
		 * Creating the worker should immediately start the cataloguing process in a new thread, so this 
		 * returns, giving control back to the AWT thread. Result will appear later in taskFinished.  
		 */
		String name = "Listing " + fileFilter.getDescription();
		worker = new PamWorker<FileListData<T>>(this, parentFrame, 2, name);
	}
	
	
	/**
	 * Create a file list worker.  
	 * @param rootList - the list of folders to search in
	 * @param subFolders - true to look for files in sub folders
	 * @param useOldIfPossible
	 */
	public final PamWorker<FileListData<T>> makeFileListProcess (String[] rootList, boolean subFolders, boolean useOldIfPossible) {
		this.fileList = rootList;
		this.subFolders = subFolders;
		this.useOldIfPossible = useOldIfPossible;
		if (noChange(rootList, subFolders, useOldIfPossible)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					taskFinished(oldFileList);
				}
			});
			return null;
		}
		
		//the worker is returned and needs to be started programmatically 
		return worker = new PamWorker<FileListData<T>>(this);
	}

	private boolean noChange(String[] rootFolder, boolean subFolders, boolean useOldIfPossible) {
		if (rootFolder == null) {
			return false;
		}
		if (oldFileList == null) {
			return false;
		}
		if (useOldIfPossible == false) {
			return false;
		}
		if (oldFileList.getRootList() == null) {
			return false;
		}
		if (oldFileList.isSubFolders() != subFolders) {
			return false;
		}
		String[] oldRoots = oldFileList.getRootList();
		if (oldRoots.length != rootFolder.length) {
			return false;
		}
		for (int i = 0; i < oldRoots.length; i++) {
			if (oldRoots[i].equals(rootFolder[i]) == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final FileListData<T> runBackgroundTask(PamWorker<FileListData<T>> pamWorker) {
		fileFilter.setAcceptFolders(subFolders);
		FileListData<T> newFileList = new FileListData<T>(fileList, subFolders, useOldIfPossible);
		searchStartTime = System.currentTimeMillis();
		/**
		 * File list may be a single or list of files or folders or a mix of the two, 
		 * so check each and either add it or search it accordingly. 
		 */
		if (fileList != null) for (int i = 0; i < fileList.length; i++) {
			File rootFile = new File(fileList[i]);
			if (rootFile.isFile()) { // add just the one file
				T newFile = createFile(rootFile);
				eachFileTask(newFile);
				newFileList.addFile(newFile);
			}
			else { // add the entire folder. 
				addFiles(pamWorker, newFileList, rootFile);
			}
		}
		
		finaliseFileList(pamWorker, newFileList);

		return newFileList;
	}

	/**
	 * Do any final jobs to the file list. This is needed in listing of xwav 
	 * files since we have to go in and get the chunks. Might as well do the start
	 * times and audio formats for all files while we're at it to save time later. 
	 * By doing it here in the progress bar, we can at least show progress sensibly. 
	 * @param pamWorker
	 * @param newFileList
	 */
	public void finaliseFileList(PamWorker<FileListData<T>> pamWorker, FileListData<T> newFileList) {
		// default file listing does nothing here. 
	}

	private void addFiles(PamWorker<FileListData<T>> pamWorker, FileListData<T> newFileList, File folder) {
		newFileList.addFolder();
		pamWorker.update(new PamWorkProgressMessage(-1, "Searching folder " + folder.getAbsolutePath()));
		Debug.out.println(">>>> Searching for files in abs path " + folder.getAbsolutePath());
		//		System.out.println(folder.getAbsolutePath());
		File[] moreFiles = folder.listFiles(fileFilter);
		if (moreFiles == null) {
			return;
		}
		for (int i = 0; i < moreFiles.length; i++) {
			if (moreFiles[i].isDirectory()) {
				addFiles(pamWorker, newFileList, moreFiles[i]);
			}
			else {
				T newFile = createFile(moreFiles[i]);
				eachFileTask(newFile);
				newFileList.addFile(newFile);
				Debug.out.println("Adding file " + newFile.getAbsolutePath());
				if (i%100 == 0) {
					sayProgress(pamWorker, newFileList, folder);
				}
			}
		}
		sayProgress(pamWorker, newFileList, folder);
		
		//Now all files in this folder have been added, so do any final tasks including checking for duplicates
		String message = String.format("Found %d files - removing duplicates", newFileList.getFileCount());
		pamWorker.update(new PamWorkProgressMessage(null, null, message));
		newFileList.removeDuplicates("wav"); // remove duplicates and preferentially keep wav files
	}

	private void sayProgress(PamWorker<FileListData<T>> pamWorker, FileListData<T> newFileList, File folder) {
		String msg = String.format("%d folders searched; %d sound files found in %d seconds...", 
				newFileList.getFoldersSearched(), newFileList.getFileCount(), (System.currentTimeMillis()-searchStartTime)/1000);
		pamWorker.update(new PamWorkProgressMessage(null, null, msg));
	}


	/**
	 * Creates the file. Can override to make enhanced file types, e.g. with the 
	 * wav file audioinfo. 
	 * @param filePath
	 * @return
	 */
	abstract public T createFile(File baseFile);

	/**
	 * Called every time a file is discovered in the main search. Can check things like 
	 * consistency of sample rate in each file as progress continues.  
	 * @param aFile
	 * @return true if processing can continue
	 */
	public boolean eachFileTask(T aFile) {
		return true;
	}

	/**
	 * Called once the main list of files has been made so that additional operations can 
	 * be applied to every file, such as checking sample rates, etc. 
	 * @param fileList
	 * @return true if all OK. 
	 */
	public boolean allFilesTask(FileListData<T> fileList) {
		return true;
	}


	@Override
	public final void taskFinished(FileListData<T> result) {
		oldFileList = result;
		sortFiles(result);
		fileListUser.newFileList(result);
	}

	/**
	 * Sort files by name (not path, just name)
	 */
	private void sortFiles(FileListData<T> fileList) {
		fileList.sortFileList();		
	}

}
