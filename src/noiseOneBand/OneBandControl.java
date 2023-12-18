package noiseOneBand;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import noiseOneBand.offline.OneBandSummaryTask;
import offlineProcessing.DataCopyTask;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamTabPanel;

public class OneBandControl extends PamControlledUnit implements PamSettings {

	public static final int NMEASURES = 4;
	public static final String[] measureNames = {"RMS", "0-Peak", "Peak-Peak", "Integrated SEL"};
	private OneBandProcess oneBandProcess;
	private OneBandPulseProcess pulseProcess;
	protected OneBandParameters oneBandParameters = new OneBandParameters();
	private OneBandTabPanel dBHtTabPanel;
	private OfflineTaskGroup offlineTaskGroup;
	private OLProcessDialog olProcessDialog;
	
	public OneBandControl(String unitName) {
		super("NoiseBand", unitName);
		addPamProcess(oneBandProcess = new OneBandProcess(this));
		addPamProcess(pulseProcess = new OneBandPulseProcess(this));
		dBHtTabPanel = new OneBandTabPanel(this);
		PamSettingManager.getInstance().registerSettings(this);
		oneBandProcess.setupProcess();
		if (isViewer) {
			setToolbarComponent(new DbHtToolbar());
		}
	}

	/**
	 * @return the dbHtProcess
	 */
	public OneBandProcess getOneBandProcess() {
		return oneBandProcess;
	}

	/**
	 * @return the dbHtParameters
	 */
	public OneBandParameters getParameters() {
		return oneBandParameters;
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
			offlineTaskGroup.setPrimaryDataBlock(oneBandProcess.getMeasureDataBlock());
			offlineTaskGroup.addTask(new DataCopyTask<OneBandDataUnit>(oneBandProcess.getMeasureDataBlock()));
			OneBandSummaryTask task = new OneBandSummaryTask(this, oneBandProcess.getMeasureDataBlock());
			offlineTaskGroup.addTask(task);
		}
		if (olProcessDialog == null) {
			olProcessDialog = new OLProcessDialog(getPamView().getGuiFrame(), offlineTaskGroup, "Noise Data Export");
		}
		olProcessDialog.setVisible(true);
	}

	public void settingsMenuAction(Frame parentFrame) {
		OneBandParameters newParams = OneBandDialog.showDialog(this, parentFrame);
		if (newParams != null) {
			oneBandParameters = newParams.clone();
			oneBandProcess.setupProcess();
			dBHtTabPanel.newParams();
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return oneBandParameters;
	}

	@Override
	public long getSettingsVersion() {
		return OneBandParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		oneBandParameters = ((OneBandParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	@Override
	public String getModuleSummary() {
		// TODO Auto-generated method stub
		return super.getModuleSummary();
	}

	@Override
	public Object getShortUnitType() {
		return "Noise";
	}

	/**
	 * @return the dbhtPulseProcess
	 */
	public OneBandPulseProcess getPulseProcess() {
		return pulseProcess;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			dBHtTabPanel.newParams();
			break;
		}
	}
}
