package tethys;

/**
 * information about a document AND the nilus object to go with it. 
 * @author dg50
 *
 * @param <T>
 */
public class DocumentNilusObject<T extends Object> extends DocumentInfo {

	private T nilusObject;

	public DocumentNilusObject(Collection collection, String documentName, String documentId, T nilusObject) {
		super(collection, documentName, documentId);
		this.nilusObject = nilusObject;		
	}

	/**
	 * @return the nilusObject
	 */
	public T getNilusObject() {
		return nilusObject;
	}

	/**
	 * @param nilusObject the nilusObject to set
	 */
	public void setNilusObject(T nilusObject) {
		this.nilusObject = nilusObject;
	}

}
