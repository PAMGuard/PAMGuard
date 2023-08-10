package Array.layoutFX;

import Array.Streamer;
import PamController.SettingsPane;
import javafx.scene.Node;
import pamViewFX.fxNodes.PamBorderPane;
import javafx.scene.control.Label;


/**
 * A javaFX settings pane for a streamer. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StreamerSettingsPane extends SettingsPane<Streamer> {
	
	public PamBorderPane mainPane; 
	
	
	public StreamerSettingsPane() {
		super(null); 
		
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(new Label("Hello"));
	
	}

	@Override
	public Streamer getParams(Streamer currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(Streamer input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Streamer Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {

	}
	
}
