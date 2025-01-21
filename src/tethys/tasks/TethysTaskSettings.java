package tethys.tasks;

import java.io.Serializable;

import tethys.niluswraps.NilusSettingsWrapper;

/**
 * Settings controlling output of a Tethys stream. These are primarily used in
 * the batch processor to set up export jobs and move settings between the 
 * batch controller and the running jobs, but they might find use in other areas. <br>
 * contains sufficient information for simple exports, though may need more for 
 * more complex exports where PAMGuard gathers additional parameters not captured within
 * the sample nilus object.
 * @author dg50
 *
 */
public class TethysTaskSettings<T extends Object> implements Serializable {


	public static final long serialVersionUID = 1L;

	/**
	 * Copy of the name that will be used in the hashmap, but why not ? 
	 */
	private String taskLongName;
	
	private NilusSettingsWrapper<T> wrappedSample;
	
	private boolean overwrite = false;

	public TethysTaskSettings(String taskLongName) {
		super();
		this.taskLongName = taskLongName;
	}

	/**
	 * @return the wrappedSample
	 */
	public NilusSettingsWrapper<T> getWrappedSample() {
		return wrappedSample;
	}

	/**
	 * @param wrappedSample the wrappedSample to set
	 */
	public void setWrappedSample(NilusSettingsWrapper<T> wrappedSample) {
		this.wrappedSample = wrappedSample;
	}

	/**
	 * @return the taskLongName
	 */
	public String getTaskLongName() {
		return taskLongName;
	}

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	

}
