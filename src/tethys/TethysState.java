package tethys;

import tethys.detection.DetectionExportProgress;

/**
 * Basis for a message system which will get passed around whenever something happens in 
 * Tethys, whether it be a new connection, progress during data output, etc. 
 * @author dg50
 *
 */
public class TethysState {

	public enum StateType {UPDATESERVER, // Server connection or status has changed
		TRANSFERDATA, // data have been transferred from PAMGuard to Tethys
		NEWPROJECTSELECTION, // a new Tethys project has been selected in the GUI
		NEWPAMGUARDSELECTION, // new PAMGuard data are available (called once on first load)
		UPDATEMETADATA, // META Data being prepared for output have changed (so may be able to enable output!)
		EXPORTING // currently exporting data.  may be a while ...
		};
	
	public StateType stateType;
	private Object stateObject;

	public TethysState(StateType stateType) {
		super();
		this.stateType = stateType;
	}

	public TethysState(StateType stateType, Object stateObject) {
		this.stateType = stateType;
		this.stateObject = stateObject;
	}

	public Object getStateObject() {
		return stateObject;
	}
}
