package levelMeter;

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
import PamView.PamSidePanel;

public class LevelMeterControl extends PamControlledUnit implements PamSettings  {

	protected LevelMeterParams levelMeterParams = new LevelMeterParams();
	protected LevelMeterSidePanel levelMeterSidePanel;
	
	public LevelMeterControl(String unitName) {
		super("Level Meter", unitName);
		PamSettingManager.getInstance().registerSettings(this);
		levelMeterSidePanel = new LevelMeterSidePanel(this);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			levelMeterSidePanel.setup();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return levelMeterParams;
	}

	@Override
	public long getSettingsVersion() {
		return LevelMeterParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		levelMeterParams = ((LevelMeterParams) pamControlledUnitSettings.getSettings()).clone();
		return levelMeterParams != null;
	}

	@Override
	public PamSidePanel getSidePanel() {
		return levelMeterSidePanel;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		return menuItem;
	}
	
	private class SettingsMenu implements ActionListener {

		private Frame parentFrame;

		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			settingsMenuAction(parentFrame);
		}
		
	}

	public void settingsMenuAction(Frame parentFrame) {
		LevelMeterParams newParams = LevelMeterDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			levelMeterParams = newParams.clone();
			levelMeterSidePanel.setup();
		}
	}

}
