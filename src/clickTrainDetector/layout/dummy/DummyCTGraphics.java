package clickTrainDetector.layout.dummy;

import java.awt.Component;

import clickTrainDetector.layout.CTDetectorGraphics;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * GUI components for the dummy (do-nothing) click train algorithm. There are no
 * settings - the pane simply explains what the algorithm is for.
 *
 * @author Jamie Macaulay
 */
public class DummyCTGraphics implements CTDetectorGraphics {

	private VBox settingsPane;

	@Override
	public Pane getCTSettingsPane() {
		if (settingsPane == null) {
			Label label = new Label("This detector does nothing. Run it in viewer mode to delete "
					+ "all existing click trains within the processed data without creating any new ones.");
			label.setWrapText(true);
			settingsPane = new VBox(label);
			settingsPane.setPadding(new Insets(10));
		}
		return settingsPane;
	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public Pane getCTSidePane() {
		return null;
	}

	@Override
	public Component getCTSidePaneSwing() {
		return null;
	}

	@Override
	public void notifyUpdate(int flag, Object object) {
		// no settings to update.
	}

}
