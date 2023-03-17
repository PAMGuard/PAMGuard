package tethys.swing;

import javax.swing.JComponent;

import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysStateObserver;

public abstract class TethysGUIPanel implements TethysStateObserver {

	private TethysControl tethysControl;

	public TethysGUIPanel(TethysControl tethysControl) {
		super();
		this.tethysControl = tethysControl;
		tethysControl.addStateObserver(this);
	}

	public TethysControl getTethysControl() {
		return tethysControl;
	}
	
	public abstract JComponent getComponent();
	
	@Override
	public void updateState(TethysState tethysState) {
		
	}
	
	
}
