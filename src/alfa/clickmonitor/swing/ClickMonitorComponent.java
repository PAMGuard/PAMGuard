package alfa.clickmonitor.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import alfa.clickmonitor.ClickMonitorProcess;

public class ClickMonitorComponent {

	private ClickMonitorProcess clickMonitorProcess;
	
	private PamPanel mainPanel;
	
	private ClickAggregateTable clickAggregateTable;

	public ClickMonitorComponent(ClickMonitorProcess clickMonitorProcess) {
		this.clickMonitorProcess = clickMonitorProcess;
		clickAggregateTable = new ClickAggregateTable(clickMonitorProcess);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Sperm whale detection results"));
		mainPanel.add(clickAggregateTable.getComponent(), BorderLayout.CENTER);
	}

	public JComponent getComponent() {
		return mainPanel;
	}
}
