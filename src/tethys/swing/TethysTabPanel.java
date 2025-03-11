package tethys.swing;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;
import tethys.TethysControl;

public class TethysTabPanel implements PamTabPanel {

	private TethysControl tethysContol;
	
	private TethysMainPanel tethysMainPanel;
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		return tethysContol.createTethysMenu(parentFrame);
	}

	public TethysTabPanel(TethysControl tethysContol) {
		super();
		this.tethysContol = tethysContol;
		tethysMainPanel = new TethysMainPanel(tethysContol);
	}

	@Override
	public JComponent getPanel() {
		return tethysMainPanel.getMainPanel();
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

}
