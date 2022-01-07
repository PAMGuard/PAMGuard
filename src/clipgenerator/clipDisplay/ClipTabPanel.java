package clipgenerator.clipDisplay;

import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import PamView.PamTabPanel;
import clipgenerator.ClipControl;

public class ClipTabPanel implements PamTabPanel {

	private ClipControl clipControl;
	
	private ClipDisplayPanel clipDisplayPanel;

	public ClipTabPanel(ClipControl clipControl) {
		super();
		this.clipControl = clipControl;
		clipDisplayPanel = new ClipDisplayPanel(clipControl);
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getPanel() {
		return clipDisplayPanel.getDisplayPanel();
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
