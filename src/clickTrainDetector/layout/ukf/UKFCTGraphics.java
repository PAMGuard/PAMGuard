package clickTrainDetector.layout.ukf;

import java.awt.Component;

import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFClickTrainAlgorithm;
import clickTrainDetector.clickTrainAlgorithms.ukf.UKFParams;
import clickTrainDetector.layout.CTDetectorGraphics;
import javafx.scene.layout.Pane;

/**
 * GUI components for the UKF click train algorithm.
 *
 * @author Jamie Macaulay
 */
public class UKFCTGraphics implements CTDetectorGraphics {

	private final UKFClickTrainAlgorithm ukfClickTrainAlgorithm;

	private UKFCTSettingsPane settingsPane;

	public UKFCTGraphics(UKFClickTrainAlgorithm ukfClickTrainAlgorithm) {
		this.ukfClickTrainAlgorithm = ukfClickTrainAlgorithm;
	}

	@Override
	public Pane getCTSettingsPane() {
		if (settingsPane == null) {
			settingsPane = new UKFCTSettingsPane(ukfClickTrainAlgorithm);
		}
		settingsPane.setParams(ukfClickTrainAlgorithm.getParams());
		return (Pane) settingsPane.getContentNode();
	}

	@Override
	public boolean getParams() {
		// getAlgorithmParams() is called for every algorithm, including ones whose
		// settings pane was never shown (and so never created). Guard against that.
		if (settingsPane == null) {
			return true;
		}
		UKFParams params = settingsPane.getParams(ukfClickTrainAlgorithm.getParams());
		if (params == null) {
			System.err.println("UKF algorithm returned null params");
			return false;
		}
		ukfClickTrainAlgorithm.setParams(params);
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
		if (flag == ClickTrainControl.NEW_PARENT_DATABLOCK && settingsPane != null) {
			settingsPane.notifyChange(flag, object);
		}
	}

}
