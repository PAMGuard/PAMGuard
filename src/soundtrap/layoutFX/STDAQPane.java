package soundtrap.layoutFX;

import Acquisition.layoutFX.DAQSettingsPane;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * JavaFX settings pane for the SoundTrap DAQ system.
 * The SoundTrap has no user-configurable DAQ settings,
 * so this pane simply displays a message.
 * 
 * @author Jamie Macaulay
 */
public class STDAQPane extends DAQSettingsPane {

	private PamBorderPane mainPane;

	public STDAQPane() {
		mainPane = new PamBorderPane();
		mainPane.setCenter(new Label("No additional settings required for SoundTrap."));
	}

	@Override
	public void setParams() {
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public Object getParams(Object currParams) {
		return null;
	}

	@Override
	public void setParams(Object input) {
	}

	@Override
	public String getName() {
		return "SoundTrap settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
	}
}
