package clickTrainDetector.layout;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.PamSidePanel;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import javafx.embed.swing.JFXPanel;

/**
 * Swing holder for the CT side panel 
 * @author Jamie Macaulay 
 *
 */
public class CTSidePanelSwing implements PamSidePanel {
	
	/**
	 * The main holder pane. 
	 */
	private JPanel holder;
	
	/**
	 * Component which holds the FX panel 
	 */
	private JFXPanel jfxPanel; 
	
	/**
	 * Constructor for the swing side pane. 
	 */
	public CTSidePanelSwing(ClickTrainControl clickTrainControl) {
		createPanel();
	}

	private void createPanel() {
		holder  = new JPanel(new BorderLayout()); 
		holder.add(new JLabel("BURP"), BorderLayout.NORTH); 
	}

	@Override
	public JComponent getPanel() {
		return holder; 
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub

	}

	/**
	 * Update flag sent whenever an algorithm is changed, and so side pane needs changed too. 
	 * @param currentCTAlgorithm
	 */
	public void update(ClickTrainAlgorithm currentCTAlgorithm) {
		holder.removeAll();
		if (currentCTAlgorithm.getClickTrainGraphics()!=null && currentCTAlgorithm.getClickTrainGraphics().getCTSidePaneSwing()!=null) {
			holder.add(currentCTAlgorithm.getClickTrainGraphics().getCTSidePaneSwing(), BorderLayout.CENTER); 
		}
	}
	
	
//	/**
//	 * Create the jfx panel 
//	 * @return
//	 */
//	private JFXPanel createJFXPane(){
//
//		//this has to be called in order to initialise the FX toolkit. Otherwise will crash if no other 
//		//FX has been called. 
//		PamJFXPanel dlgContent = new PamJFXPanel();
//
//		Platform.runLater(()->{
//			try {
//			holder = new PamBorderPane();
//
////			VBox.setVgrow(holder, Priority.ALWAYS);
//			dlgContent.setScene(dlgContent.setRoot(holder));
//			holder.setTop(new Label("HELLLOOOO2"));
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		});
//		
//		return dlgContent; 
//	}

}
