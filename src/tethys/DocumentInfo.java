package tethys;

/**
 * Basic information about a document that can be used to 
 * make document lists. 
 * @author dg50
 *
 */
public class DocumentInfo implements Comparable<DocumentInfo> {

	private Collection collection;
	private String documentName;
	private String documentId;

	/**
	 * @param collection
	 * @param documentName
	 * @param documentId
	 */
	public DocumentInfo(Collection collection, String documentName, String documentId) {
		this.collection = collection;
		this.documentName = documentName;
		this.documentId = documentId;
	}
	@Override
	public int compareTo(DocumentInfo o) {
		return this.documentName.compareTo(o.documentName);
	}
	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}
	/**
	 * @return the documentName
	 */
	public String getDocumentName() {
		return documentName;
	}
	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}
	
	
}
