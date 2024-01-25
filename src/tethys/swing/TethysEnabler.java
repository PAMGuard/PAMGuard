package tethys.swing;

import java.awt.Component;
import java.util.ArrayList;

import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.TethysStateObserver;

/**
 * Handle enabling / disabling of any buttons / controls on the GUI. 
 * @author dg50
 *
 */
public class TethysEnabler implements TethysStateObserver {

	private TethysControl tethysControl;
	
	private ArrayList<Component> components = new ArrayList<>();

	/**
	 * @param tethysControl
	 */
	public TethysEnabler(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
		tethysControl.addStateObserver(this);
	}
	
	/**
	 * Add a component to the enable list. 
	 * @param aComponent
	 */
	public synchronized void addComponent(Component aComponent) {
		components.add(aComponent);
	}
	
	public synchronized void removeComponent(Component aComponent) {
		components.remove(aComponent);
	}

	@Override
	public void updateState(TethysState tethysState) {
//		if (tethysState.stateType == StateType.UPDATESERVER) {
//			boolean ok = tethysControl.getDbxmlConnect().pingServer()
//		}
	}
	
	public synchronized void enableControls(boolean enable) {
		for (Component c : components) {
			c.setEnabled(enable);
		}
	}
	
	
}
