package beamformer.continuous;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseControl;
import beamformer.BeamFormerParams;
import beamformer.BeamformerSettingsPane;
import beamformer.algorithms.BeamAlgorithmProvider;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public class BeamFormerControl extends BeamFormerBaseControl implements PamSettings {

	public static String unitType = "Beamformer";
	
	/**
	 * A lookup table to link the algo-specific parameters to the algorithm itself
	 */
	private HashMap<BeamAlgorithmParams, BeamAlgorithmProvider> algoLUT = new HashMap<>();
	

	/**
	 * JavaFX version of beamformer settings pane. 
	 */
	private BeamformerSettingsPane beamformerSettingsPane; 

	/**
	 * Main constructor
	 * 
	 * @param unitName
	 */
	public BeamFormerControl(String unitName) {
		super(unitType, unitName);
		PamSettingManager.getInstance().registerSettings(this);
		
		BeamFormerProcess beamFormerProcess = new BeamFormerProcess(this);
		addPamProcess(beamFormerProcess);
		setBeamFormerProcess(beamFormerProcess);
	}

	@Override
	public Serializable getSettingsReference() {
		return getBeamFormerParams();
	}

	@Override
	public long getSettingsVersion() {
		return BeamFormerParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		BeamFormerParams params = ((BeamFormerParams) (pamControlledUnitSettings.getSettings())).clone();
		if (params!=null) {
			setBeamFormerParams(params);
		}
		return (params != null);
	}


	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new SettingsAction(parentFrame));
		return menuItem;
	}
	private class SettingsAction implements ActionListener {

		private Frame parentFrame;

		public SettingsAction(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
//			showSettings(parentFrame);
			showSettingsDialog(parentFrame);
		}

	}
	
	private PamDialogFX2AWT<BeamFormerParams> settingsDialog;
	
	protected void showSettingsDialog(Frame parentFrame) {
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			SettingsPane<BeamFormerParams> setPane = new BeamformerSettingsPane(getGuiFrame(), this);
			settingsDialog = new PamDialogFX2AWT<BeamFormerParams>(parentFrame, setPane, false);
		}
		BeamFormerParams newParams = settingsDialog.showDialog(this.getBeamFormerParams());
		if (newParams != null) {
			this.setBeamFormerParams(newParams);
			getBeamFormerProcess().prepareProcess();
		}
	}


	/**
	 * Similar to the getSettingsPane() method, but does not load the latest parameters into
	 * the window.  Only returns a reference to the settings pane.
	 * @return the beamformerSettingsPane
	 */
	public BeamformerSettingsPane getBeamformerSettingsPane() {
		return beamformerSettingsPane;
	}



}
