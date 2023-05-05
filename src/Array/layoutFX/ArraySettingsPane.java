package Array.layoutFX;

import Array.PamArray;
import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class ArraySettingsPane extends SettingsPane<PamArray >{

	public ArraySettingsPane() {
		super(null);
	}

	@Override
	public PamArray  getParams(PamArray  currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(PamArray  input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Array Parameters";
	}

	@Override
	public Node getContentNode() {
		return new Label("TODO: The Array Manager needs an FX GUI");
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
