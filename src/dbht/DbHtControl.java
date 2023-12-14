package dbht;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dbht.offline.DbHtSummaryTask;
import offlineProcessing.DataCopyTask;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamTabPanel;

public class DbHtControl extends PamControlledUnit implements PamSettings {

	public static final int NMEASURES = 3;
	public static final String[] measureNames = {"RMS", "0-Peak", "Peak-Peak"};
	private DbHtProcess dbHtProcess;
	protected DbHtParameters dbHtParameters = new DbHtParameters();
	private DbHtTabPanel dBHtTabPanel;
	private OfflineTaskGroup offlineTaskGroup;
	private OLProcessDialog olProcessDialog;
	
	public DbHtControl(String unitName) {
		super("DbHt", unitName);
		addPamProcess(dbHtProcess = new DbHtProcess(this));
		dBHtTabPanel = new DbHtTabPanel(this);
		PamSettingManager.getInstance().registerSettings(this);
		dbHtProcess.setupProcess();
		if (isViewer) {
			setToolbarComponent(new DbHtToolbar());
		}
	}

	/**
	 * @return the dbHtProcess
	 */
	public DbHtProcess getDbHtProcess() {
		return dbHtProcess;
	}

	/**
	 * @return the dbHtParameters
	 */
	public DbHtParameters getDbHtParameters() {
		return dbHtParameters;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getTabPanel()
	 */
	@Override
	public PamTabPanel getTabPanel() {
		return dBHtTabPanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		return menuItem;
	}
	
	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		return dBHtTabPanel.createDisplayMenu(parentFrame);
	}

	private class SettingsMenu implements ActionListener {

		Frame parentFrame;
		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			settingsMenuAction(parentFrame);
		}
		
	}
	
	private class DbHtToolbar extends JPanel {

		public DbHtToolbar() {
			super();
			setLayout(new FlowLayout(FlowLayout.LEFT));
			JButton b = new JButton("Export Data ...");
			add(b);
			b.addActionListener(new OfflineTaskAction());
		}
		
	}
	
	private class OfflineTaskAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			runOfflineTasks();
		}
	}
	
	private void runOfflineTasks() {
		if (offlineTaskGroup == null) {
			offlineTaskGroup = new OfflineTaskGroup(this, getUnitName());
			offlineTaskGroup.setPrimaryDataBlock(dbHtProcess.getMeasureDataBlock());
			offlineTaskGroup.addTask(new DataCopyTask<DbHtDataUnit>(dbHtProcess.getMeasureDataBlock()));
			DbHtSummaryTask task = new DbHtSummaryTask(this, dbHtProcess.getMeasureDataBlock());
			offlineTaskGroup.addTask(task);
		}
		if (olProcessDialog == null) {
			olProcessDialog = new OLProcessDialog(getGuiFrame(), offlineTaskGroup, "dBHt Data Export");
		}
		olProcessDialog.setVisible(true);
	}

	public void settingsMenuAction(Frame parentFrame) {
		DbHtParameters newParams = DbHtDialog.showDialog(this, parentFrame);
		if (newParams != null) {
			dbHtParameters = newParams.clone();
			dbHtProcess.setupProcess();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return dbHtParameters;
	}

	@Override
	public long getSettingsVersion() {
		return DbHtParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		dbHtParameters = ((DbHtParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public String getModuleSummary() {
		// TODO Auto-generated method stub
		return super.getModuleSummary();
	}

	@Override
	public Object getShortUnitType() {
		return "DBHT";
	}
}
