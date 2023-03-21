package tethys.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import tethys.TethysControl;

public class TethysMainPanel extends TethysGUIPanel {

	private TethysControl tethysControl;

	private JPanel mainPanel;
	
	private TethysConnectionPanel connectionPanel;
	
	private DatablockSynchPanel datablockSynchPanel;
	
	private DeploymentsPanel deploymentsPanel;
	
	public TethysMainPanel(TethysControl tethysControl) {
		super(tethysControl);
		this.tethysControl = tethysControl;
		mainPanel = new JPanel(new BorderLayout());
		connectionPanel = new TethysConnectionPanel(tethysControl);
		datablockSynchPanel = new DatablockSynchPanel(tethysControl);
		deploymentsPanel = new DeploymentsPanel(tethysControl);
		
		mainPanel.add(BorderLayout.NORTH, connectionPanel.getComponent());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.set
		mainPanel.add(BorderLayout.CENTER, splitPane);
//		mainPanel.add(BorderLayout.CENTER, datablockSynchPanel.getComponent());
		splitPane.add(deploymentsPanel.getComponent());
		splitPane.add(datablockSynchPanel.getComponent());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				splitPane.setDividerLocation(0.5);
			}
		});
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}

	@Override
	public JComponent getComponent() {
		return getMainPanel();
	}
	
	
}
