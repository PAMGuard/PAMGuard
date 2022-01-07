package Acquisition.layoutFX;

import PamController.SettingsPane;
import PamUtils.worker.filelist.WavFileType;
import javafx.collections.ObservableList;

/**
 * Pane which allows settings for a FileDate to be changes. 
 * @author Jamie Macaulay
 *
 * @param <T>
 */
public abstract class FileDatePane<T> extends SettingsPane<T>{

	public FileDatePane() {
		super(null);
	}

	/**
	 * Set parameters in the file date pane. 
	 */
	public abstract void setParams(); 
	
	/**
	 * Get the parameters in the file date pane. 
	 */
	public abstract void getParams();

	/**
	 * Set the current list of files
	 * @param fileList - the current list of wave files. 
	 */
	protected abstract void setFilelist(ObservableList<WavFileType> fileList);


}
