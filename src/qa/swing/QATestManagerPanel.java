package qa.swing;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import qa.QAControl;

/**
 * Panel to control immediate tests and possibly also some of the 
 * longer term tests.  
 * @author dg50
 *
 */
public class QATestManagerPanel {

	private JPanel mainPanel;
		
	private String[] testTypes = {QAControl.quickTestName, QAControl.randomTestName};

	private TestSelectPanel testSelectPanel;

	private QAControl qaControl;
	
	public QATestManagerPanel(QAControl qaControl) {
		this.qaControl = qaControl;
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//		mainPanel.setBorder(new TitledBorder("Test Management"));
		
		testSelectPanel = new TestSelectPanel(qaControl, testTypes);
		mainPanel.add(testSelectPanel.getComponent());
		
		QAOperationsDisplays qaOpsDisplays = qaControl.getQaOperationsStatus().getQaOpsDisplays();
		JPanel opsBit = new PamPanel(new BorderLayout());
		opsBit.add(BorderLayout.NORTH, qaOpsDisplays.getOpsStatusLookup());
		opsBit.add(BorderLayout.CENTER, qaOpsDisplays.getOpsOptions());
		HidingPanel hp = new HidingPanel(mainPanel, opsBit, HidingPanel.VERTICAL, false, "Operations Status", "Operations Status");
		hp.setBorder(new TitledBorder("Operations Status"));
		mainPanel.add(hp);
		
	}
	
	public JComponent getComponent() {
		return mainPanel;
	}
	
	

}
