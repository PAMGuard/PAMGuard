package qa.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

import PamController.PamController;
import PamView.dialog.PamLabel;
import PamView.help.PamHelp;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamNorthPanel;
import PamView.panel.PamPanel;
import PamView.panel.PamSplitPane;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.ScrollPaneAddon;
import qa.QAControl;

public class QAMainPanel {

	private QAControl qaControl;
	
	private PamPanel mainPanel;

	private QACrossMatchPanel crossMatchPanel;
	
	private QATestManagerPanel testManagerPanel;
	
	private QAOpsTable qaOpsTable;
	
	private JButton helpButton;
	
	private String helpPoint = "utilities.SIDEModule.docs.SIDE_Overview";


	public QAMainPanel(QAControl qaControl) {
		this.qaControl = qaControl;
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BorderLayout());
		
		QATestTable testTable = new QATestTable(qaControl, qaControl.getQaGeneratorProcess().getTestsDataBlock());
//		if (qaControl.isViewer()) {
//			qaOpsTable = new QAOpsTable(qaControl, qaControl.getQaAnalyser().getOpsDataBlock());
//		}
//		else {
			qaOpsTable = new QAOpsTable(qaControl, qaControl.getQaGeneratorProcess().getOpsDataBlock());
//		}
		PamPanel opsPanel = new PamPanel(new BorderLayout());
		opsPanel.add(BorderLayout.CENTER, qaOpsTable.getComponent());
		opsPanel.add(BorderLayout.NORTH, new PamLabel(" Operations State"));
		
		PamPanel testPanel = new PamPanel(new BorderLayout());
		testPanel.add(BorderLayout.CENTER, testTable.getComponent());
		testPanel.add(BorderLayout.NORTH, new PamLabel(" Test History"));
		
		JSplitPane splitPane = new PamSplitPane(JSplitPane.VERTICAL_SPLIT, testPanel, opsPanel, "QA Ops Panels");
		
		splitPane.setDividerLocation(200);
		mainPanel.add(BorderLayout.CENTER, splitPane);
		
		crossMatchPanel = new QACrossMatchPanel(qaControl);
		HidingPanel hideCrossMatch = new HidingPanel(mainPanel, crossMatchPanel.getPanel(), HidingPanel.HORIZONTAL, true, "QA Reporting", "QAReporting");
		mainPanel.add(BorderLayout.EAST, hideCrossMatch);
		
		testManagerPanel = new QATestManagerPanel(qaControl);
		helpButton = new JButton("Help ...");
		helpButton.addActionListener(new HelpButtonPressed());
		helpButton.setVisible(true);
		helpButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		PamPanel settingsPanel = new PamPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		settingsPanel.add(new PamAlignmentPanel(testManagerPanel.getComponent(), BorderLayout.NORTH, true));
		settingsPanel.add(Box.createVerticalGlue());
		PamPanel helpPanel = new PamPanel(new BorderLayout());
		helpPanel.add(BorderLayout.SOUTH, helpButton);
		settingsPanel.add(helpPanel);
		mainPanel.add(BorderLayout.WEST, settingsPanel);
		
		// alternate layout - help button across the bottom of the entire panel
//		mainPanel.add(BorderLayout.SOUTH, helpButton);
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	class HelpButtonPressed implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			PamHelp.getInstance().displayContextSensitiveHelp(helpPoint);
		}
	}



}
