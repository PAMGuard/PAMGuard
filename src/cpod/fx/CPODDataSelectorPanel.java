package cpod.fx;

import javax.swing.JComponent;

import PamView.dialog.PamDialogPanel;
import PamView.panel.PamPanel;
import cpod.dataSelector.CPODDataSelector;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;

public class CPODDataSelectorPanel implements PamDialogPanel {

	private JComponent mainPane;

	private CPODDataSelector cpodDataSelelctor;

	private CPODDataSelectorPane cpodDataSlectorPane;

	public CPODDataSelectorPanel(CPODDataSelector cpodDataSelelctor) {
		this.cpodDataSelelctor=cpodDataSelelctor; 
		mainPane = new PamPanel();
		createFXPanel();
	}

	private void createFXPanel() {
		// This method is invoked on Swing thread
		final JFXPanel fxPanel = new JFXPanel();
		mainPane.add(fxPanel); 


		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}
		});
	}

	private void initFX(JFXPanel fxPanel) {
		cpodDataSlectorPane = new CPODDataSelectorPane(cpodDataSelelctor); 
		// This method is invoked on JavaFX thread
		Group  root  =  new  Group();
		Scene  scene  =  new  Scene(root);

		root.getChildren().add(cpodDataSlectorPane.getContentNode());

		fxPanel.setScene(scene);
	}




	@Override
	public JComponent getDialogComponent() {
		return mainPane;
	}

	@Override
	public void setParams() {
		Platform.runLater(()->{
		cpodDataSlectorPane.setParams(true);
		});

	}

	@Override
	public boolean getParams() {
		return cpodDataSlectorPane.getParams(true);
	}



}
