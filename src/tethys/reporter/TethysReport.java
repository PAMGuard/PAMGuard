package tethys.reporter;

import tethys.Collection;

public class TethysReport {

	private boolean success;
	
	private Collection collection;
	 
	private String docName;
	
	private String docId;

	/**
	 * @param success
	 * @param collection
	 * @param docName
	 * @param docId
	 */
	public TethysReport(boolean success, Collection collection, String docName, String docId) {
		this.success = success;
		this.collection = collection;
		this.docName = docName;
		this.docId = docId;
	}

	/**
	 * @return the success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @return the docName
	 */
	public String getDocName() {
		return docName;
	}

	/**
	 * @return the docId
	 */
	public String getDocId() {
		return docId;
	}
	
}
