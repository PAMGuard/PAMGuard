package PamUtils.worker.filelist;

import java.io.File;

/**
 * Has a single callback to get the results of a file list worker. 
 * @author dg50
 *
 */
public interface FileListUser<T extends File> {

	/**
	 * Callback when task is complete with a list of files, possibly with enhanced data fields. 
	 * @param fileListData
	 */
	public void newFileList(FileListData<T> fileListData);
	
}
