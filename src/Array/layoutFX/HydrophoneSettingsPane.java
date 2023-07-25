package Array.layoutFX;

import Array.Hydrophone;
import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;

public class HydrophoneSettingsPane extends SettingsPane<Hydrophone> {
	
	PamBorderPane mainPane; 

	public HydrophoneSettingsPane() {
		super(null);
	}

	
	@Override
	public Hydrophone getParams(Hydrophone currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(Hydrophone input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getContentNode() {
		// TODO Auto-generated method stub
		return new Label("Hello Hydrophone");
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
