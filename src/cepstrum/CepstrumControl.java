package cepstrum;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class CepstrumControl extends PamControlledUnit implements PamSettings {
	
	public static final String unitType = "Cepstrum"; 
	
	private CepstrumParams cepstrumParams = new CepstrumParams();
	
	private CepstrumProcess cepstrumProcess;

	public CepstrumControl(String unitName) {
		super(unitType, unitName);
		cepstrumProcess = new CepstrumProcess(this);
		addPamProcess(cepstrumProcess);
		
		PamSettingManager.getInstance().registerSettings(this);
		
	}

	@Override
	public Serializable getSettingsReference() {
		return cepstrumParams;
	}

	@Override
	public long getSettingsVersion() {
		return CepstrumParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		cepstrumParams = ((CepstrumParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		CepstrumParams newParams = CepstrumDialog.showDialog(parentFrame, cepstrumParams);
		if (newParams != null) {
			cepstrumParams = newParams;
			cepstrumProcess.prepareProcess();
		}
	}

	/**
	 * @return the cepstrumParams
	 */
	public CepstrumParams getCepstrumParams() {
		return cepstrumParams;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			cepstrumProcess.prepareProcess();
		}
	}
	
}
