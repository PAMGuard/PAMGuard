package gpl;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import Layout.DisplayProviderList;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import gpl.dialog.GPLDialog;
import gpl.swing.GPLSpectrogramPanelProvider;

public class GPLControlledUnit extends PamControlledUnit implements PamSettings {

	public static final String unitType = "GPL Detector";
	
	private GPLProcess gplProcess;
	
	private GPLParameters gplParameters = new GPLParameters();
	
	private ArrayList<GPLParameters> defaultSettingsList;
	
	public GPLControlledUnit(String unitName) {
		super(unitType, unitName);
		gplProcess = new GPLProcess(this);
		addPamProcess(gplProcess);
		
		DisplayProviderList.addDisplayPanelProvider(new GPLSpectrogramPanelProvider(this));
		
		PamSettingManager.getInstance().registerSettings(this);
		
		defaultSettingsList = new ArrayList<>();
		// decided against this. Will just have a decent import and export function rather than setting in stone. 
//		defaultSettingsList.add(new GPLParameters("Blue whale D", 35, 90, 75, 1.5, 2.5, .5, 50, 200, 2, 1, 10));
//		defaultSettingsList.add(new GPLParameters("Fin whale 20Hz", 12, 36, 75, 1.5, 2.5, .5, 25, 100, 2, 1, 10));
//		defaultSettingsList.add(new GPLParameters("Fin whale 40Hz", 35, 90, 75, 1.5, 2.5, .5, 24, 96, 2, 1, 10));
//		defaultSettingsList.add(new GPLParameters("Humpback whale", 150, 1000, 75, 1.5, 2.5, .5, 30, 90, 2, .35, 5));
//		defaultSettingsList.add(new GPLParameters("Bryde's whale", 10, 50, 60, 1.5, 2.5, .5, 50, 200, 2, .35, 6));
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
	}

	/**
	 * @return the gplParameters
	 */
	public GPLParameters getGplParameters() {
		return gplParameters;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gplSettingsDialog(parentFrame);
			}
		});
		
		return menuItem;
	}

	protected void gplSettingsDialog(Frame parentFrame) {
		GPLParameters newParams = GPLDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			gplParameters = newParams;
			gplProcess.prepareProcess();
		}		
	}

	/**
	 * @return the gplProcess
	 */
	public GPLProcess getGplProcess() {
		return gplProcess;
	}

	/**
	 * @return the defaultSettingsList
	 */
	public ArrayList<GPLParameters> getDefaultSettingsList() {
		return defaultSettingsList;
	}

	@Override
	public Serializable getSettingsReference() {
		return gplParameters;
	}

	@Override
	public long getSettingsVersion() {
		return GPLParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		gplParameters = ((GPLParameters) pamControlledUnitSettings.getSettings()).clone();
		return gplParameters != null;
	}
	

}
