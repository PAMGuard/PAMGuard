package tethys;

abstract public class CollectionHandler {

	private Collection collection;
	
	protected TethysControl tethysControl;

	/**
	 * @param tethysControl
	 * @param collection
	 */
	public CollectionHandler(TethysControl tethysControl, Collection collection) {
		this.tethysControl = tethysControl;
		this.collection = collection;
	}
	
	public String collectionName() {
		return collection.collectionName();
	}

	/**
	 * @return the collection
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @return the tethysControl
	 */
	public TethysControl getTethysControl() {
		return tethysControl;
	}
	
	public abstract String getHelpPoint();
	
}
