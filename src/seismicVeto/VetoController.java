package seismicVeto;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class VetoController extends PamControlledUnit implements PamSettings {

	VetoProcess vetoProcess;
	
	VetoParameters vetoParameters = new VetoParameters();
	
	VetoPluginPanelProvider vetoPluginPanelProvider;
	
	public VetoController(String unitName) {
		
		super("Seismic Veto", unitName);
		
		addPamProcess(vetoProcess = new VetoProcess(this));
		
		vetoPluginPanelProvider = new VetoPluginPanelProvider(this);
		
		PamSettingManager.getInstance().registerSettings(this);
		
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new SetParameters(parentFrame));
		return menuItem;
	}

	class SetParameters implements ActionListener {

		Frame parentFrame;
		
		public SetParameters(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			VetoParameters newParams = VetoParametersDialog.showDialog(parentFrame, vetoParameters, vetoProcess.getFftOutputData());
			if (newParams != null) {
				vetoParameters = newParams.clone();
				useNewParams();
			}
		}
	}
	
	private void useNewParams() {
		vetoProcess.useNewParams();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			useNewParams();
			break;
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return vetoParameters;
	}

	@Override
	public long getSettingsVersion() {
		return VetoParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		vetoParameters = ((VetoParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

}
