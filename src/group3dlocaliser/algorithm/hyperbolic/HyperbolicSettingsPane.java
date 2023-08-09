package group3dlocaliser.algorithm.hyperbolic;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Hyperbolic settings pane. 
 * @author Jamie Macaulay
 *
 */
public class HyperbolicSettingsPane extends SettingsPane<HyperbolicParams> {
	
	PamBorderPane mainPane;

	public HyperbolicSettingsPane() {
		super(null);
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(new Label("Hello"));
	}
	
	

	@Override
	public HyperbolicParams getParams(HyperbolicParams currParams) {
		return currParams;
	}

	@Override
	public void setParams(HyperbolicParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Hyperbolic parameters";
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
