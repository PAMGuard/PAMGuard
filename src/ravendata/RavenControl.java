package ravendata;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import pamViewFX.PamSettingsMenuPane;
import ravendata.swing.RavenImportDialog;

/**
 * System for importing and displaying data from Raven selection tables. 
 * @author dg50
 *
 */
public class RavenControl extends PamControlledUnit implements PamSettings {
	
	private RavenProcess ravenProcess;
	
	private static final String unitType = "Raven Import";
	public static final String defaultName = unitType;
	
	private RavenParameters ravenParameters = new RavenParameters();

	public RavenControl(String unitName) {
		super(unitType, unitName);
		this.ravenProcess = new RavenProcess(this);
		addPamProcess(ravenProcess);
		PamSettingManager.getInstance().registerSettings(this);
	}

	public RavenControl(PamConfiguration pamConfiguration, String unitType, String unitName) {
		super(pamConfiguration, unitType, unitName);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Import Raven data ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importData(parentFrame);
			}
		});
		return menuItem;
	}

	protected void importData(Frame parentFrame) {
		RavenParameters newParams = RavenImportDialog.showDialog(parentFrame, ravenParameters);
		if (newParams == null) {
			return;
		}
		ravenParameters = newParams;
		// import the data. 1. Check for existing and delete, 2. Import, 3. rebuild datamap, 4. load data 
		RavenFileReader fileReader = null;
		ArrayList<RavenDataRow> ravenData = null;
		try {
			fileReader = new RavenFileReader(this, ravenParameters.importFile);
			ravenData = fileReader.readTable();
			
			fileReader.closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (ravenData != null) {
			createPAMGuardData(fileReader, ravenData);
		}
	}

	private void createPAMGuardData(RavenFileReader fileReader, ArrayList<RavenDataRow> ravenData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Serializable getSettingsReference() {
		return ravenParameters;
	}

	@Override
	public long getSettingsVersion() {
		return RavenParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		ravenParameters = (RavenParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

}
