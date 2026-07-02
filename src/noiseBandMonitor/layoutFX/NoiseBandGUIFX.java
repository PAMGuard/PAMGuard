package noiseBandMonitor.layoutFX;

import PamController.SettingsPane;
import noiseBandMonitor.NoiseBandControl;
import noiseBandMonitor.NoiseBandSettings;
import pamViewFX.PamControlledGUIFX;

/**
 * JavaFX GUI for the Noise Band Monitor module.
 * Provides a settings pane that replicates the Swing NoiseBandDialog.
 * 
 * @author PAMGuard
 */
public class NoiseBandGUIFX extends PamControlledGUIFX {

	/**
	 * Reference to the noise band controller.
	 */
	private NoiseBandControl noiseBandControl;

	/**
	 * The settings pane.
	 */
	private NoiseBandSettingsPane settingsPane;

	public NoiseBandGUIFX(NoiseBandControl noiseBandControl) {
		this.noiseBandControl = noiseBandControl;
	}

	@Override
	public SettingsPane<NoiseBandSettings> getSettingsPane() {
		if (settingsPane == null) {
			settingsPane = new NoiseBandSettingsPane(noiseBandControl);
		}
		settingsPane.setParams(noiseBandControl.getNoiseBandSettings());
		return settingsPane;
	}

	@Override
	public void updateParams() {
		if (settingsPane == null) {
			return;
		}
		NoiseBandSettings newSettings = settingsPane.getParams(noiseBandControl.getNoiseBandSettings());
		if (newSettings != null) {
			noiseBandControl.setNoiseBandSettings(newSettings.clone());
			noiseBandControl.getNoiseBandProcess().setupProcess();
			noiseBandControl.sortBandEdges();
		}
	}
}
