package tethys;

/**
 * Basis for a message system which will get passed around whenever something happens in
 * Tethys, whether it be a new connection, progress during data output, etc.
 * @author dg50
 *
 */
public class TethysState {

	public enum StateType {UPDATESERVER, // Server connection or status has changed
		EXPORTRDATA, // data have been transferred from PAMGuard to Tethys
		NEWPROJECTSELECTION, // a new Tethys project has been selected in the GUI
		NEWPAMGUARDSELECTION, // new PAMGuard data are available (called once on first load)
		UPDATEMETADATA, // META Data being prepared for output have changed (so may be able to enable output!)
		EXPORTING, // currently exporting data.  may be a while ...
		DELETEDATA // data were deleted
		}

	public StateType stateType;
	
	public Collection collection;

	public TethysState(StateType stateType) {
		super();
		this.stateType = stateType;
		collection = Collection.OTHER;
	}

	public TethysState(StateType stateType, Collection collection) {
		this.stateType = stateType;
		this.collection =  collection;
		if (this.collection == null) {
			this.collection = Collection.OTHER;
		}
	}

	/**
	 * @return the collection associated with this notification. Note that there is 
	 * an OTHER category in Collections which is used for server / project updates, making 
	 * it easier to switch on the collection type when notifications are received. 
	 */
	public Collection getCollection() {
		return collection;
	}

	/**
	 * @return the stateType
	 */
	public StateType getStateType() {
		return stateType;
	}

}
