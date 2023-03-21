package tethys.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
	
	public Color getNormalColour() {
		return new JPanel().getBackground();
	}
	
	public Color getErrorColour() {
		return Color.ORANGE;
	}

	public void colourBackground(int iCol) {
		Color col = iCol == 0 ? getNormalColour() : getErrorColour();
		colourPanels(getComponent(), col);
	}

	private void colourPanels(JComponent component, Color col) {
		component.setBackground(col);
		int nChild = component.getComponentCount();
		for (int i = 0; i < nChild; i++) {
			Component aChild = component.getComponent(i);
			if (aChild instanceof JPanel) {
				colourPanels((JComponent) aChild, col);
			}
		}
		
	}
}
