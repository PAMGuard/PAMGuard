package tethys;

/**
 * Basis for a message system which will get passed around whenever something happens in 
 * Tethys, whether it be a new connection, progress during data output, etc. 
 * @author dg50
 *
 */
public class TethysState {

	public enum StateType {UPDATESERVER, TRANSFERDATA};
	
	public StateType stateType;

	public TethysState(StateType stateType) {
		super();
		this.stateType = stateType;
	}
}
