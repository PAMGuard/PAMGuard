package noiseOneBand;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import PamView.PamTabPanel;

public class OneBandTabPanel implements PamTabPanel {

	private OneBandDisplayPanel oneBandDisplayPanel;
	
	private OneBandControl dDHtControl;
	
	/**
	 * @param dDHtControl
	 */
	public OneBandTabPanel(OneBandControl dDHtControl) {
		super();
		this.dDHtControl = dDHtControl;
		oneBandDisplayPanel = new OneBandDisplayPanel(dDHtControl, this);
	}

	public JMenuItem createDisplayMenu(Frame parentFrame) {
//		JMenuItem mi = new JMenuItem(dDHtControl.getUnitName() + " Display Options ...");
//		mi.addActionListener(new OptionsMenu(parentFrame));
//		return mi;
		return null;
	}

	class OptionsMenu implements ActionListener {
		Frame parentFrame;
		public OptionsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
//			optionsMenu(parentFrame);
		}
	}
	
	public boolean optionsMenu(Frame parentFrame, int plotType) {
		OneBandDisplayParams displayParams = oneBandDisplayPanel.getDisplayParams(plotType);
		OneBandDisplayParams newParams = OneBandDisplayDialog.showDialog(parentFrame, dDHtControl, displayParams);
		if (newParams != null) {
			oneBandDisplayPanel.setDisplayParams(plotType, newParams.clone());
			 return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public JComponent getPanel() {
		return oneBandDisplayPanel.getDisplayPanel();
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	public void newParams() {
		oneBandDisplayPanel.newParams();
	}

}
