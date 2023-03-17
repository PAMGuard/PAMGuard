package tethys.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import tethys.TethysControl;

public class TethysMainPanel extends TethysGUIPanel {

	private TethysControl tethysControl;

	private JPanel mainPanel;
	
	private TethysConnectionPanel connectionPanel;
	
	public TethysMainPanel(TethysControl tethysControl) {
		super(tethysControl);
		this.tethysControl = tethysControl;
		mainPanel = new JPanel(new BorderLayout());
		connectionPanel = new TethysConnectionPanel(tethysControl);
		mainPanel.add(BorderLayout.NORTH, connectionPanel.getComponent());
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}

	@Override
	public JComponent getComponent() {
		return getMainPanel();
	}
	
	
}
