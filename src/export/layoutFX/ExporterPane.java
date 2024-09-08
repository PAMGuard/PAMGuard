package export.layoutFX;

import PamController.SettingsPane;
import export.ExportParams;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Create a pane to export data. 
 * @author Jamie Macaulay
 *
 */
public class ExporterPane extends SettingsPane<ExportParams>{
	
	PamBorderPane mainPane;

	public ExporterPane(Object ownerWindow) {
		super(ownerWindow);
		mainPane = new PamBorderPane();
		
		mainPane.setTop(new Label("Hello")); 
		mainPane.setCenter(new Label("Hello")); 
	}

	@Override
	public ExportParams getParams(ExportParams currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(ExportParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Export Settings";
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
