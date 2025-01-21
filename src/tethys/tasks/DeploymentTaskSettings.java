package tethys.tasks;

import metadata.PamguardMetaData;

public class DeploymentTaskSettings extends TethysTaskSettings {


	/*
	 * No need to add the TethysExportParams here since they are the main
	 * Tethys parameters, so should be set anyway for every Tethys task prior
	 * to running. 
	 */
	
	private static final long serialVersionUID = 1L;
	
	private PamguardMetaData pamguardMetaData;

	public DeploymentTaskSettings(String taskLongName) {
		super(taskLongName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the pamguardMetaData
	 */
	public PamguardMetaData getPamguardMetaData() {
		return pamguardMetaData;
	}

	/**
	 * @param pamguardMetaData the pamguardMetaData to set
	 */
	public void setPamguardMetaData(PamguardMetaData pamguardMetaData) {
		this.pamguardMetaData = pamguardMetaData;
	}

}
