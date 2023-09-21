package tethys.database;

import PamguardMVC.PamDataUnit;

public class TethysLogDataUnit extends PamDataUnit {

	private String collection;
	private String documentId;
	private TethysActions action;
	private String comment;
	private boolean success;

	public TethysLogDataUnit(long timeMilliseconds, String collection, String documentId, TethysActions action, boolean success, String comment) {
		super(timeMilliseconds);
		this.collection = collection;
		this.documentId = documentId;
		this.action = action;
		this.success = success;
		this.comment = comment;
				
	}

	/**
	 * @return the collection
	 */
	public String getCollection() {
		return collection;
	}

	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}

	/**
	 * @return the action
	 */
	public TethysActions getAction() {
		return action;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}


}
