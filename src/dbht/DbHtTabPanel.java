package dbht;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import PamView.PamTabPanel;

public class DbHtTabPanel implements PamTabPanel {

	private DbHtDisplayPanel dbHtDisplayPanel;
	
	private DbHtControl dDHtControl;
	
	/**
	 * @param dDHtControl
	 */
	public DbHtTabPanel(DbHtControl dDHtControl) {
		super();
		this.dDHtControl = dDHtControl;
		dbHtDisplayPanel = new DbHtDisplayPanel(dDHtControl, this);
	}

	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem mi = new JMenuItem(dDHtControl.getUnitName() + " Display Options ...");
		mi.addActionListener(new OptionsMenu(parentFrame));
		return mi;
	}

	class OptionsMenu implements ActionListener {
		Frame parentFrame;
		public OptionsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			optionsMenu(parentFrame);
		}
	}
	
	public boolean optionsMenu(Frame parentFrame) {
		DbHtDisplayParams newParams = DbHtDisplayDialog.showDialog(parentFrame, dDHtControl, dbHtDisplayPanel.displayParams);
		if (newParams != null) {
			 dbHtDisplayPanel.displayParams = newParams.clone();
			 dbHtDisplayPanel.newParams();
			 return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public JComponent getPanel() {
		return dbHtDisplayPanel.getDisplayPanel();
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

}
