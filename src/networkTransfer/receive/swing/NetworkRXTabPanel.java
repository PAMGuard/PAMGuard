package networkTransfer.receive.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import PamView.PamTabPanel;
import PamView.panel.PamPanel;
import networkTransfer.receive.NetworkReceiver;

public class NetworkRXTabPanel implements PamTabPanel {

	private NetworkReceiver networkReceiver;

//	private RXTablePanel rxTablePanel;
	private RXTablePanel2 rxTablePanel;
	
	private JPanel mainPanel;

	private RXSummaryPanel summaryPanel;

	public NetworkRXTabPanel(NetworkReceiver networkReceiver) {
		this.networkReceiver = networkReceiver;
		mainPanel = new PamPanel(new BorderLayout());
		summaryPanel = new RXSummaryPanel(networkReceiver);
		mainPanel.add(summaryPanel.getComponent(), BorderLayout.NORTH);
		rxTablePanel = new RXTablePanel2(networkReceiver);
		mainPanel.add(rxTablePanel.getComponent(), BorderLayout.CENTER);
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		return null;
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public JToolBar getToolBar() {
		return null;
	}

	public void notifyModelChanged(int changeType) {
		rxTablePanel.notifyModelChanged(changeType);
	}

	public void configurationChange() {
		rxTablePanel.configurationChange();
	}

}
