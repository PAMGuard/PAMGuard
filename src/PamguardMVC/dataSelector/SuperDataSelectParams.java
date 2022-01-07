package PamguardMVC.dataSelector;

public class SuperDataSelectParams extends DataSelectParams {

	public static final long serialVersionUID = 1L;

	/**
	 * Allow sub detections which don't actually have this type of super detection. 
	 */
	private boolean useUnassigned;

	/*
	 * Params for the actual super detection type.
	 */
	private DataSelectParams dataSelectParams;
	
	/**
	 * Allow sub detections which don't actually have this type of super detection. 
	 * @return the useUnassigned
	 */
	public boolean isUseUnassigned() {
		return useUnassigned;
	}

	/**
	 * Allow sub detections which don't actually have this type of super detection. 
	 * @param useUnassigned the useUnassigned to set
	 */
	public void setUseUnassigned(boolean useUnassigned) {
		this.useUnassigned = useUnassigned;
	}

	/**
	 * @return the dataSelectParams
	 */
	public DataSelectParams getDataSelectParams() {
		return dataSelectParams;
	}

	/**
	 * @param dataSelectParams the dataSelectParams to set
	 */
	public void setDataSelectParams(DataSelectParams dataSelectParams) {
		this.dataSelectParams = dataSelectParams;
	}

}
