package clickTrainDetector.layout;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainParams;
import clickTrainDetector.layout.classification.simplechi2classifier.SimpleCTClassifierPane;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * The pre-classifier pane holds settings for the pre-classifier. 
 * @author Jamie Macualay 
 *
 */
public class PreClassifierPane extends PamBorderPane {
	
	/**
	 * Click train control.
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Simple ct classifier pane. 
	 */
	private SimpleCTClassifierPane simpleCTClassifierPane; 

	public PreClassifierPane(ClickTrainControl clickTrainControl) {
		this.clickTrainControl = clickTrainControl; 
		
		
		this.setCenter(createClassifierPane());
		this.setPadding(new Insets(5,5,5,5));
	}

	
	private Pane createClassifierPane() {
		simpleCTClassifierPane = new SimpleCTClassifierPane(null); 
		return (Pane) simpleCTClassifierPane.getContentNode();
	}
	
	/**
	 * Set parameters for the pane. 
	 * @param clickTrainParams - the parameters. 
	 */
	public void setParams(ClickTrainParams clickTrainParams) {
		simpleCTClassifierPane.setParams(clickTrainParams.simpleCTClassifier);
	}
	
	/**
	 * Get parameters
	 * @param clickTrainParams - the click trian parameters. 
	 * @return the updated parameters. 
	 */
	public ClickTrainParams getParams(ClickTrainParams clickTrainParams) {
		clickTrainParams.simpleCTClassifier=simpleCTClassifierPane.getParams(clickTrainParams.simpleCTClassifier);
		return clickTrainParams; 
	}

}
