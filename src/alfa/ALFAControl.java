package alfa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import Acquisition.AcquisitionControl;
import GPS.GPSControl;
import Map.MapController;
import NMEA.NMEAControl;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.status.ModuleStatus;
import PamView.PamSidePanel;
import PamView.PamTabPanel;
import alfa.clickmonitor.ClickMonitorProcess;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import alfa.comms.MessageProcess;
import alfa.effortmonitor.AngleHistogram;
import alfa.effortmonitor.EffortMonitor;
import alfa.logger.LoggerMonitor;
import alfa.status.StatusMonitor;
import alfa.status.StatusObserver;
import alfa.swinggui.ALFADialog;
import alfa.swinggui.ALFAGUITransformer;
import alfa.swinggui.ALFASidePanel;
import alfa.swinggui.BBSwingTabPanel;
import alfa.utils.MiraScreen;
import binaryFileStorage.BinaryStore;
import clickDetector.ClickControl;
import clickTrainDetector.ClickTrainControl;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import generalDatabase.DBControlUnit;
import rockBlock.RockBlockControl;

/**
 * ALFA module shows status of various modules in PAMGuard for real time operation detecting 
 * sperm whales for the Alaska Fishermen's Association. 
 * 
 * @author Doug Gillespie
 *
 */
public class ALFAControl extends PamControlledUnit implements PamSettings {

	private static String unitType = "ALFA Sperm Tracker";

	private StatusMonitor statusMonitor;

	private BBSwingTabPanel bbSwingTabPanel;

	private boolean initialisationComplete;

	private ClickMonitorProcess clickMonitorProcess;

	private EffortMonitor effortMonitor;

	private LoggerMonitor loggerMonitor;

	private MessageProcess messageProcess;

	private ALFAParameters alfaParameters = new ALFAParameters();

	private ALFASidePanel alfaSidePanel;

	private ALFAGUITransformer alfaGuiTransformer;

	public ALFAControl(String unitName) {
		super(unitType, unitName);
		ArrayList<ControlledModuleInfo> controlledModulesList = new ArrayList<>();
		controlledModulesList.add(new ControlledModuleInfo(DBControlUnit.class));
		controlledModulesList.add(new ControlledModuleInfo(BinaryStore.class));
		controlledModulesList.add(new ControlledModuleInfo(NMEAControl.class));
		controlledModulesList.add(new ControlledModuleInfo(GPSControl.class));
		controlledModulesList.add(new ControlledModuleInfo(MapController.class));
		controlledModulesList.add(new ControlledModuleInfo(AcquisitionControl.class));
		controlledModulesList.add(new ControlledModuleInfo(ClickControl.class));
		controlledModulesList.add(new ControlledModuleInfo(ClickTrainControl.class));
		//		controlledModulesList.add(new ControlledModuleInfo(FormsControl.class));
		controlledModulesList.add(new ControlledModuleInfo(RockBlockControl.class, "Sat Comms"));
		controlledModulesList.add(new ControlledModuleInfo(ALFAControl.class));
		statusMonitor = new StatusMonitor(false, controlledModulesList);

		statusMonitor.addObserver(new StatusObserver() {

			@Override
			public void newStatus() {
				statusUpdated();
			}

			@Override
			public void newModuleList() {
			}
		});

		clickMonitorProcess = new ClickMonitorProcess(this);
		addPamProcess(clickMonitorProcess);
		effortMonitor = new EffortMonitor(this);
		addPamProcess(effortMonitor);
		messageProcess = new MessageProcess(this);
		addPamProcess(messageProcess);
		addPamProcess(loggerMonitor = new LoggerMonitor(this));

		PamSettingManager.getInstance().registerSettings(this);

		if (alfaParameters.autoScreenMirror) {
			MiraScreen.startMirror();
		}

		alfaSidePanel = new ALFASidePanel(this);

		if (!this.isViewer()) {
			//GUI alterations for real time mode. 
			alfaGuiTransformer = new ALFAGUITransformer(this); 
		}
	}

	protected void statusUpdated() {
		if (alfaSidePanel != null) {
			alfaSidePanel.statusUpdate();
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getTabPanel()
	 */
	@Override
	public PamTabPanel getTabPanel() {
		if (bbSwingTabPanel == null) {
			bbSwingTabPanel = new BBSwingTabPanel(this);
		}
		return bbSwingTabPanel;
	}

	public MapController findMapController() {
		return (MapController) PamController.getInstance().findControlledUnit(MapController.unitType);
	}

	/**
	 * @return the statusMonitor
	 */
	public StatusMonitor getStatusMonitor() {
		return statusMonitor;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			// probably need to do something ? 
			initialisationComplete = true;
			bbSwingTabPanel.updateUnits();
			findClickTrainDetector();
		}
		if (initialisationComplete) {
			switch (changeType) {
			case PamController.ADD_CONTROLLEDUNIT:
			case PamController.REMOVE_CONTROLLEDUNIT:
				bbSwingTabPanel.updateUnits();			
				findClickTrainDetector();	
			}
		}
		if (alfaGuiTransformer!=null) this.alfaGuiTransformer.notifyModelChanged(changeType); 
		//this.alfaSidePanel.notifyModelChanged(changeType); 
	}

	/**
	 * Called when the model changes. 
	 */
	private void findClickTrainDetector() {
		ClickTrainControl clickControl = (ClickTrainControl) PamController.getInstance().findControlledUnit(ClickTrainControl.class, null);
		clickMonitorProcess.setClickTrainDetector(clickControl);
	}

	/**
	 * @return the clickMonitorProcess
	 */
	public ClickMonitorProcess getClickMonitorProcess() {
		return clickMonitorProcess;
	}

	/**
	 * @return the effortMonitor
	 */
	public EffortMonitor getEffortMonitor() {
		return effortMonitor;
	}

	/**
	 * @return the messageProcess
	 */
	public MessageProcess getMessageProcess() {
		return messageProcess;
	}

	/**
	 * Generic updated information from the click monitor and aggregator which can 
	 * be used to update the effort data. 
	 * @param aggregateEvent
	 * @param detectionGroupDataUnit
	 */
	public void updateClickInformation(ClickEventAggregate aggregateEvent,
			DetectionGroupDataUnit detectionGroupDataUnit) {
		effortMonitor.updateClickInformation(aggregateEvent, detectionGroupDataUnit);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName() + " options ...");
		//		menu.add(loggerMonitor.getMenuItem(parentFrame));
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Options ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAlfaDialog(parentFrame);
			}
		});
		menu.add(menuItem);
		return menu;
	}


	public void showAlfaDialog() {
		showAlfaDialog(getGuiFrame());
	}

	public void showAlfaDialog(Frame parentFrame) {
		ALFAParameters newParams = ALFADialog.showDialog(parentFrame, this);
		if (newParams != null) {
			setAlfaParameters(newParams);
		}
	}

	/**
	 * @return the alfaParameters
	 */
	public ALFAParameters getAlfaParameters() {
		return alfaParameters;
	}

	/**
	 * @param alfaParameters the alfaParameters to set
	 */
	public void setAlfaParameters(ALFAParameters alfaParameters) {
		this.alfaParameters = alfaParameters;
		if (alfaParameters.autoScreenMirror) {
			MiraScreen.startMirror();
		}
		this.setupControlledUnit();
	}

	/**
	 * @return the loggerMonitor
	 */
	public LoggerMonitor getLoggerMonitor() {
		return loggerMonitor;
	}

	@Override
	public Serializable getSettingsReference() {
		return alfaParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ALFAParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		alfaParameters = ((ALFAParameters) pamControlledUnitSettings.getSettings()).clone();
		return alfaParameters != null;
	}

	@Override
	public ModuleStatus getModuleStatus() {
		return statusMonitor.getSummaryStatus();
	}

	@Override
	public PamSidePanel getSidePanel() {
		return alfaSidePanel;
	}

	/**
	 * Get a status object (for display in the side panel) which 
	 * summarises the number of whales recently detected. 
	 * @return whale summary. 
	 */
	public ModuleStatus getWhaleStatus() {
		return effortMonitor.getWhaleStatus();
	}

	public AngleHistogram getStatusAngleHistogram() {
		return effortMonitor.getAveragedAngleHistogram();
	}

	/**
	 * called just before data acquisition starts. Note that
	 * PamObservers get a call to setSampleRate anyway so this mainly needs
	 * to be used for display elements that may need their scales 
	 * adjusted before startup.
	 *
	 */
	@Override
	public void pamToStart() {
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
				alfaSidePanel.notifyModelChanged(PamController.PAM_RUNNING); 
//			}
//		});
	}

	/**
	 * Called for all controlled units after Pam acquisition has stopped
	 *
	 */
	@Override
	public void pamHasStopped() {
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
				alfaSidePanel.notifyModelChanged(PamController.PAM_IDLE); 
//			}
//		});
	}

}
