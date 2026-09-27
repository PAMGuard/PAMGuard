package clickTrainDetector.layout.adaptive;

import java.awt.Component;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveCTParams;
import clickTrainDetector.clickTrainAlgorithms.adaptive.AdaptiveClickTrainAlgorithm;
import clickTrainDetector.layout.CTDetectorGraphics;
import javafx.scene.layout.Pane;

/**
 * GUI components for the adaptive click train algorithm.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTGraphics implements CTDetectorGraphics {

	private final AdaptiveClickTrainAlgorithm adaptiveClickTrainAlgorithm;

	private AdaptiveCTSettingsPane settingsPane;

	public AdaptiveCTGraphics(AdaptiveClickTrainAlgorithm adaptiveClickTrainAlgorithm) {
		this.adaptiveClickTrainAlgorithm = adaptiveClickTrainAlgorithm;
	}

	@Override
	public Pane getCTSettingsPane() {
		if (settingsPane == null) {
			settingsPane = new AdaptiveCTSettingsPane(adaptiveClickTrainAlgorithm);
		}
		settingsPane.setParams(adaptiveClickTrainAlgorithm.getParams());
		return (Pane) settingsPane.getContentNode();
	}

	@Override
	public boolean getParams() {
		// the pane is only created once it has been shown. getAlgorithmParams() is
		// called for every algorithm, so guard against an unshown pane.
		if (settingsPane == null) {
			return true;
		}
		AdaptiveCTParams params = settingsPane.getParams(adaptiveClickTrainAlgorithm.getParams());
		if (params == null) {
			System.err.println("Adaptive algorithm returned null params");
			return false;
		}
		adaptiveClickTrainAlgorithm.setParams(params);
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
		// forward parent-data-block changes so the feature toggles can be greyed out
		// when bearing or waveform data is not available.
		if (flag == ClickTrainControl.NEW_PARENT_DATABLOCK && settingsPane != null) {
			settingsPane.notifyChange(flag, object);
		}
	}

}
