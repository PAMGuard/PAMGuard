package decimator.layoutFX;

import PamController.SettingsPane;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * 
 * Settings for the decimator.
 * 
 * @author Jamie Macaulay
 */
public class DecimatorSettingsPane extends SettingsPane<DecimatorParams> {
	
	private PamBorderPane mainPane;

	public DecimatorSettingsPane(DecimatorControl aquisitionControl) {
		super(null);
		
		mainPane= new PamBorderPane();
		
		mainPane.setCenter(new Label("Hello Decimator Pane"));
		
	}


	@Override
	public DecimatorParams getParams(DecimatorParams currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(DecimatorParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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
