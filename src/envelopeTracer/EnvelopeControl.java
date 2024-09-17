package envelopeTracer;

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

public class EnvelopeControl extends PamControlledUnit implements PamSettings {

	private EnvelopeProcess envelopeProcess;
	
	protected EnvelopeParams envelopeParams = new EnvelopeParams();

	public EnvelopeControl(String unitName) {
		super("Envelope Tracer", unitName);
		addPamProcess(envelopeProcess = new EnvelopeProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new DetectionMenu(parentFrame));
		return menuItem;
	}
	

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			envelopeProcess.newSettings();
		}
	}

	class DetectionMenu implements ActionListener {

		private Frame parentFrame;
		public DetectionMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			envelopeSettings(parentFrame);
		}
		
	}

	public void envelopeSettings(Frame parentFrame) {
		EnvelopeParams newParams = EnvelopeDialog.showDialog(parentFrame, envelopeParams);
		if (newParams != null) {
			envelopeParams = newParams.clone();
			envelopeProcess.newSettings();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return envelopeParams;
	}

	@Override
	public long getSettingsVersion() {
		return EnvelopeParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		envelopeParams = ((EnvelopeParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
}
