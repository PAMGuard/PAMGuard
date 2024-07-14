package rawDeepLearningClassifier.dataSelector;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * JavaFX pane for the deep learning data selector. This simply selects the rype
 * of filter to use and sets that as the controls. 
 * 
 * @author Jamie Macaulay
 */
public class DLSelectPaneFX extends DynamicSettingsPane<Boolean>{
	
	private PamVBox mainPane;
	
	/**
	 * Refrence to the deep learning data selector. 
	 */
	private DLDataSelector dlDataSelector;
	
	private int currentIndex = 0;

	public DLSelectPaneFX(DLDataSelector dlDataSelector) {
		super(null);
		this.dlDataSelector=dlDataSelector;

		//there is currently one implemented fitler so no
		//need for a comboBox etc. to select. 
		 createPane();
		 
//		 mainPane.getChildren().add(dlDataSelector.getDataSelectors().getSettingsPane().getContentNode());
	
	}
	
	private void createPane() {
		mainPane = new PamVBox();
		mainPane.setSpacing(5);
	}

	
	
	@Override
	public Boolean getParams(Boolean currParams) {
		dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPane().getParams(null);
		
		
		return currParams;
	}

	@Override
	public void setParams(Boolean input) {
		dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPane().getParams(null);
	}

	@Override
	public String getName() {
		return "Filter by deep learning result";
	}

	@Override
	public Node getContentNode() {
		return new Label("Hello DL data selector");
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
