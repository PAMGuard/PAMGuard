package clickTrainDetector.layout.classification.standardClassifier;

import PamController.SettingsPane;
import clickTrainDetector.classification.standardClassifier.StandardClassifier;
import clickTrainDetector.classification.standardClassifier.StandardClassifierParams;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;


/**
 * Pane for the standard classifier. Essentially holds all the panes of the other classifiers. 
 * @author Jamie Macaulay
 *
 */
public class StandardClassifierPane extends  SettingsPane<StandardClassifierParams> {

	private PamBorderPane mainPane;

	private StandardClassifier standardClassifier;

	private PamToggleSwitch[] enableSwitch;

	private Label[] labels;

	public StandardClassifierPane(StandardClassifier standardClassifier) {
		super(null);
		this.standardClassifier = standardClassifier; 
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(createPane());
	}

	private Pane createPane() {
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);

		labels = new Label[standardClassifier.getClassifiers().size()];

		enableSwitch = new PamToggleSwitch[standardClassifier.getClassifiers().size()]; 
		for (int i=0; i<standardClassifier.getClassifiers().size(); i++) {

			enableSwitch[i] = new PamToggleSwitch(""); 
			
			labels[i] = new Label(standardClassifier.getClassifiers().get(i).getName());
			PamGuiManagerFX.titleFont2style(labels[i]);


			final int ii = i; 
			enableSwitch[i].selectedProperty().addListener((obsVal, oldVal, newVal)->{
				standardClassifier.getClassifiers().get(ii).getCTClassifierGraphics().getCTClassifierPane().setDisable(!enableSwitch[ii].isSelected());
				labels[ii].setDisable(!enableSwitch[ii].isSelected());

			});

			PamHBox hBox = new PamHBox(); 
			hBox.setSpacing(5);

			hBox.getChildren().addAll(enableSwitch[i], labels[i]);

			vBox.getChildren().addAll(hBox, standardClassifier.getClassifiers().get(i).getCTClassifierGraphics().getCTClassifierPane());
		}

		return vBox; 
	} 

	private void disableClassifierPane(int ii) {
		standardClassifier.getClassifiers().get(ii).getCTClassifierGraphics().getCTClassifierPane().setDisable(!enableSwitch[ii].isSelected());
		labels[ii].setDisable(!enableSwitch[ii].isSelected()); 
	}


	@Override
	public StandardClassifierParams getParams(StandardClassifierParams currParams) {
		for (int i=0; i<standardClassifier.getClassifiers().size(); i++) {
			currParams.ctClassifierParams[i] = standardClassifier.getClassifiers().get(i).getCTClassifierGraphics().getParams(); 
			currParams.enable[i] = enableSwitch[i].isSelected();
		}
		return currParams; 
	}

	@Override
	public void setParams(StandardClassifierParams input) {
		for (int i=0; i<standardClassifier.getClassifiers().size(); i++) {
			standardClassifier.getClassifiers().get(i).getCTClassifierGraphics().setParams(input.ctClassifierParams[i]);
			enableSwitch[i].setSelected(input.enable[i]);
			disableClassifierPane(i);

		}
	}

	@Override
	public String getName() {
		return "Standard Classifier";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

}
