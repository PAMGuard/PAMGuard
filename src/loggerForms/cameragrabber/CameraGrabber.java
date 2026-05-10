package loggerForms.cameragrabber;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamTabPanel;
import loggerForms.actions.ActionOwner;
import loggerForms.actions.LoggerAction;
import loggerForms.actions.LoggerActions;
import loggerForms.cameragrabber.GrabberNotification.Type;
import loggerForms.cameragrabber.logger.GrabberAction;
import loggerForms.cameragrabber.swing.GrabberDialog;
import loggerForms.cameragrabber.swing.GrabberTabPanel;

public class CameraGrabber extends PamControlledUnit implements PamSettings, ActionOwner {

	public static String unitType = "Camera Grabber";
		
	private GrabberParams grabberParams = new GrabberParams();
	
	private ArrayList<GrabberObserver> observers = new ArrayList<>();

	private GrabberTabPanel grabberTabPanel;
	
	private GrabberProcess grabberProcess;
	
	
	public CameraGrabber(PamConfiguration pamConfiguration, String unitName) {
		super(pamConfiguration, unitType, unitName);
		
		PamSettingManager.getInstance().registerSettings(this);
		
		grabberProcess = new GrabberProcess(this);
		addPamProcess(grabberProcess);
		
	}

	/**
	 * @return the grabberProcess
	 */
	public GrabberProcess getGrabberProcess() {
		return grabberProcess;
	}

	@Override
	public Serializable getSettingsReference() {
		return grabberParams;
	}

	@Override
	public long getSettingsVersion() {
		return GrabberParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.grabberParams = (GrabberParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public PamTabPanel getTabPanel() {
		if (grabberTabPanel == null) {
			grabberTabPanel = new GrabberTabPanel(this);
		}
		return grabberTabPanel;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(this.getUnitName() + " settings ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showGrabberParams(parentFrame);
			}
		});
		return menuItem;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			notifyObservers(new GrabberNotification(Type.NEWCONFIG));
			setupGrabber();
		}
	}

	protected void showGrabberParams(Frame parentFrame) {
		GrabberParams newParams = GrabberDialog.showDialog(parentFrame, grabberParams);
		if (newParams != null) {
			grabberParams = newParams;
			notifyObservers(new GrabberNotification(Type.NEWCONFIG));
			setupGrabber();
		}
	}

	public GrabberParams getGrabberParams() {
		return grabberParams;
	}

	private void setupGrabber() {
		// prepare the cameras
		grabberProcess.prepareProcessOK();
		// and register all possible actions. 
		int nCam = grabberParams.nCameras;
		LoggerActions loggerActions = LoggerActions.getInstance();
		loggerActions.removeAllOwnersActions(this);
		LoggerAction action;
		for (int i = 0; i < nCam; i++) {
			action = GrabberAction.createAction(this, i, GrabberAction.GRABTYPE.FRAME);
			loggerActions.registerAction(action);
			action = GrabberAction.createAction(this, i, GrabberAction.GRABTYPE.SEQUENCE);
			loggerActions.registerAction(action);
		}
	}

	public void addObserver(GrabberObserver grabberObserver) {
		observers.add(grabberObserver);
	}
	
	public void notifyObservers(GrabberNotification grabberNotification) {
		for (GrabberObserver obs : observers) {
			obs.notify(grabberNotification);
		}
	}
}
