package qa.operations;

import java.io.Serializable;

public class OpsStatusParams implements Serializable {

	public static final long serialVersionUID = 1L;

	private String statusCode; 
	
	private boolean allowRandomTesting;
	
	public OpsStatusParams(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * @return the allowRandomTesting
	 */
	public boolean isAllowRandomTesting() {
		return allowRandomTesting;
	}

	/**
	 * @param allowRandomTesting the allowRandomTesting to set
	 */
	public void setAllowRandomTesting(boolean allowRandomTesting) {
		this.allowRandomTesting = allowRandomTesting;
	}

	/**
	 * @return the statusCode
	 */
	public String getStatusCode() {
		return statusCode;
	}

}
