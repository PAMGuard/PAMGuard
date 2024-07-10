package rawDeepLearningClassifier.dataSelector;

import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * JavaFX pane for the deep learning data selector. 
 * 
 * @author Jamie Macaulay
 */
public class DLSelectPaneFX extends DynamicSettingsPane<Boolean>{

	public DLSelectPaneFX(Object ownerWindow) {
		super(ownerWindow);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Boolean getParams(Boolean currParams) {
		// TODO Auto-generated method stub
		return currParams;
	}

	@Override
	public void setParams(Boolean input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Deep Learning Data Selector:";
	}

	@Override
	public Node getContentNode() {
		return new Label("Hello data selector");
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
