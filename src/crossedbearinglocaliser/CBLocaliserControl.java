package crossedbearinglocaliser;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsPane;
import annotation.localise.targetmotion.TMAnnotationType;
import beamformer.localiser.BFLocaliserParams;
import beamformer.localiser.dialog.BFLocSettingsPane2;
import crossedbearinglocaliser.offline.CBOfflineTask;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

/**
 * Crossed Bearing localiser. Monitors groups of detections from a single detector which have bearings
 * from multiple widely spaced hydrophone groups and calculates the crossing point of bearings. 
 * @author Doug Gillespie
 * @deprecated Replaced by new 3D group localiser. 
 *
 */
public class CBLocaliserControl extends PamControlledUnit implements PamSettings {

	public static final String unitType = "Crossed Bearing Localiser";
	
	private CBLocaliserSettngs cbLocaliserSettngs = new CBLocaliserSettngs();
	
	private CBLocaliserProcess cbLocaliserProcess;

	private PamDialogFX2AWT<CBLocaliserSettngs> settingsDialog;
	
	private TMAnnotationType tmAnnotationType;

	private OLProcessDialog olProcessDialog;
	
	private CBLocaliserControl(String unitName) {
		super(unitType, unitName);
		
		cbLocaliserProcess = new CBLocaliserProcess(this);
		addPamProcess(cbLocaliserProcess);
		
		tmAnnotationType = new TMAnnotationType();
		
		PamSettingManager.getInstance().registerSettings(this);
		
		tmAnnotationType.setAnnotationOptions(cbLocaliserSettngs.getTmAnnotationOptions());
	}

	/**
	 * @return the tmAnnotationType
	 */
	public TMAnnotationType getTmAnnotationType() {
		return tmAnnotationType;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(this.getUnitName() + " settings...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		if (isViewer()) {
			JMenu menu = new JMenu(this.getUnitName());
			menu.add(menuItem);
			menuItem = new JMenuItem(this.getUnitName() + " offline ...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showOfflineDialog(parentFrame);
				}
			});
			menuItem.setEnabled(cbLocaliserProcess.getParentDataBlock() != null);
			menu.add(menuItem);
			return menu;
		}
		else {
			return menuItem;
		}
	}

	protected void showOfflineDialog(Frame parentFrame) {
//		OfflineTaskGroup otg = new OfflineTaskGroup(this, getUnitName());
		if (olProcessDialog == null) {
			OfflineTaskGroup oltg = new OfflineTaskGroup(this, this.getUnitName());
			oltg.addTask(new CBOfflineTask(this));
			olProcessDialog = new OLProcessDialog(parentFrame, oltg, this.getUnitName());
		}
		olProcessDialog.setVisible(true);
	}

	public void showSettingsDialog(Frame parentFrame) {		
		if (settingsDialog == null || parentFrame != settingsDialog.getOwner()) {
			CBSettingsPane setPane = new CBSettingsPane(parentFrame, this);
			settingsDialog = new PamDialogFX2AWT<CBLocaliserSettngs>(parentFrame, setPane, false);
		}
		CBLocaliserSettngs newParams = settingsDialog.showDialog(cbLocaliserSettngs);
		if (newParams != null) {
			cbLocaliserSettngs = newParams;
			cbLocaliserProcess.prepareProcess();
			tmAnnotationType.setAnnotationOptions(cbLocaliserSettngs.getTmAnnotationOptions());
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return cbLocaliserSettngs;
	}

	@Override
	public long getSettingsVersion() {
		return CBLocaliserSettngs.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.cbLocaliserSettngs = ((CBLocaliserSettngs) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			cbLocaliserProcess.prepareProcess();
		}
	}

	/**
	 * @return the cbLocaliserSettngs
	 */
	public CBLocaliserSettngs getCbLocaliserSettngs() {
		return cbLocaliserSettngs;
	}

	/**
	 * @return the cbLocaliserProcess
	 */
	public CBLocaliserProcess getCbLocaliserProcess() {
		return cbLocaliserProcess;
	}

	public String getDataSelectorName() {
		return getUnitType()+"_"+getUnitName();
	}

}
