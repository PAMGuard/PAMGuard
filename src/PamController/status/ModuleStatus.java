package PamController.status;

import PamController.PamControlledUnit;

public class ModuleStatus {
	
	/**
	 * Put the sates in as ints so that we can use > in order 
	 * to determine the highest state if multiple checks are
	 * carried out within a module. 
	 */
	public static final int STATUS_OK = 0;
	public static final int STATUS_WARNING = 1;
	public static final int STATUS_ERROR = 2;

	private int status;
	
	private String message;
	
	private RemedialAction remedialAction;
	
	private PamControlledUnit pamControlledUnit;
	
	private String name;

	/**
	 * 
	 * @param status
	 * @param message
	 */
	public ModuleStatus(int status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * @param status
	 */
	public ModuleStatus(int status) {
		super();
		this.status = status;
	}

	/**
	 * @param status
	 * @param message
	 * @param remedialAction
	 */
	public ModuleStatus(int status, String message, RemedialAction remedialAction) {
		super();
		this.status = status;
		this.message = message;
		this.remedialAction = remedialAction;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = getStatusString();
		if (message != null) {
			str += ": " + message;
		}
		return str;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the remedialAction
	 */
	public RemedialAction getRemedialAction() {
		return remedialAction;
	}

	/**
	 * @param remedialAction the remedialAction to set
	 */
	public void setRemedialAction(RemedialAction remedialAction) {
		this.remedialAction = remedialAction;
	}

	/**
	 * 
	 * @return The status as a string
	 */
	public String getStatusString() {
		return getStatusString(status);
	}
	
	/**#
	 * 
	 * @param stat The Status as an integer
	 * @return The status as a String
	 */
	public static String getStatusString(int stat) {
		switch (stat) {
		case 0:
			return "OK";
		case 1:
			return "Warning";
		case 2:
			return "Error";
		default:
			return "Unknown";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
