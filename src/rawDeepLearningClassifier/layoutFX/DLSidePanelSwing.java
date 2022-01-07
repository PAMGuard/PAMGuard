package rawDeepLearningClassifier.layoutFX;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.PamSidePanel;
import PamView.panel.PamPanel;
import rawDeepLearningClassifier.DLControl;

/**
 * The DL side panel for the Swing GUI. Shows information on the current DL
 * algorithm being used. This side panel is simply a holder for the model
 * specific side panel. The model specific side panel is defined in the
 * DLClassifierModelUI class for the DLClassifierModel.
 * 
 * @author Jamie Macaulay
 *
 */
public class DLSidePanelSwing implements PamSidePanel {

	private DLControl dlControl;
	private JComponent mainPanel;

	public DLSidePanelSwing(DLControl dlControl) {
		this.dlControl =dlControl; 
		this.mainPanel   = new PamPanel(); 
		mainPanel.setLayout(new BorderLayout());
		setupPanel(); 
	}

	/**
	 * Setup the side panel so it shows the model specific side panel. 
	 */
	public void setupPanel() {
		//System.out
		if (dlControl.getDLModel().getModelUI()!=null && dlControl.getDLModel().getModelUI().getSidePanel()!=null) {
			mainPanel.add(dlControl.getDLModel().getModelUI().getSidePanel(), BorderLayout.CENTER);
//			mainPanel.add(new JLabel("Hello"), BorderLayout.WEST);
			mainPanel.validate();
		}
		else {
			//blank
			mainPanel.removeAll();
		}
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		
	}

}
