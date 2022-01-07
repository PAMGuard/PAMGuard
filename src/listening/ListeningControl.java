package listening;

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
import PamView.PamSymbol;

public class ListeningControl extends PamControlledUnit implements PamSettings {

	protected ListeningProcess listeningProcess;
	
	protected ThingHeardTabPanelControl tabPanelControl;
	
	protected ListeningParameters listeningParameters = new ListeningParameters();
	
	public static int COMMENT_LENGTH = 50;  
	public static int SPECIES_LENGTH = 50;  
	
	public ListeningControl(String unitName) {
		super("Listening", unitName);
		listeningProcess = new ListeningProcess(this);
		addPamProcess(listeningProcess);

		PamSettingManager.getInstance().registerSettings(this);
		
		tabPanelControl = new ThingHeardTabPanelControl(this);
		
		setTabPanel(tabPanelControl);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Settings...");
		menuItem.addActionListener(new AuralSettings(parentFrame));
		return menuItem;
	}
	
	private class AuralSettings implements ActionListener {

		private Frame frame;
		
		public AuralSettings(Frame frame) {
			super();
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			auralSettings(frame);
		}
		
	}
	
	private void auralSettings(Frame frame) {
		ListeningParameters newParams = ListeningDialog.showDialog(this, frame, listeningParameters);
		if (newParams != null) {
			listeningParameters = newParams.clone();
			newSettings();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return listeningParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ListeningParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		listeningParameters = ((ListeningParameters) pamControlledUnitSettings.getSettings()).clone();
		newSettings();
		return true;
	}
	
	private void newSettings() {
		if (tabPanelControl == null) {
			return;
		}
		tabPanelControl.newSettings();
	}

	/**
	 * Try to find the correct symbol for a species
	 * this gets used when data are read back from the database. 
	 * @param strSpecies species string
	 * @return symbol or null
	 */
	protected PamSymbol getSpeciesSymbol(String strSpecies) {
		SpeciesItem si;
		for (int i = 0; i < listeningParameters.speciesList.size(); i++) {
			si = listeningParameters.speciesList.get(i);
			if (si.getName().equals(strSpecies)) {
				return si.getSymbol();
			}
		}
		return null;
	}
	
//	@Override
//	public PamTabPanel getTabPanel() {
//		return tabPanelControl;
//	}

	protected void effortButton(int index, int hydrophones){
		listeningProcess.effortButton(index, hydrophones);
	}
	
	protected void buttonPress(int speciesIndex, int volume, int hydrophones, String comment) {
		listeningProcess.buttonPress(speciesIndex, volume, hydrophones, comment);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch (changeType) {
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			tabPanelControl.historyPanel.tableData.fireTableDataChanged();
		}
	}
	
}
