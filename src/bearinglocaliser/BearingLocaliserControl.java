package bearinglocaliser;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import Localiser.LocalisationAlgorithm;
import Localiser.LocalisationAlgorithmInfo;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.LocContents;
import PamUtils.SimpleObservable;
import PamguardMVC.PamDataUnit;
import beamformer.algorithms.BeamAlgorithmProvider;
import bearinglocaliser.algorithms.BearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
import bearinglocaliser.annotation.BearingAnnotationType;
import bearinglocaliser.beamformer.BeamFormBearingWrapper;
import bearinglocaliser.dialog.BearingLocSettingsPane;
import bearinglocaliser.display.BearingDisplayProvider;
import bearinglocaliser.offline.BLOfflineTask;
import bearinglocaliser.toad.TOADBearingProvider;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;
import userDisplay.UserDisplayControl;

public class BearingLocaliserControl extends PamControlledUnit implements PamSettings, LocalisationAlgorithm, LocalisationAlgorithmInfo {
	
	public static final String unitType = "Bearing Calculator";
	
	private ArrayList<BearingAlgorithmProvider> bearingAlgorithmProviders = new ArrayList<>();
	
	private BearingLocaliserParams bearingLocaliserParams = new BearingLocaliserParams();
	
	private BearingProcess bearingProcess;
	
	private DetectionMonitor detectionMonitor;
	
	private BearingAnnotationType bearingAnnotationType = new BearingAnnotationType();
	
	private BearingDisplayProvider bearingDisplayProvider;
	
	private SimpleObservable<PamDataUnit> configObservable = new SimpleObservable<>(); 
	
	private OLProcessDialog olProcessDialog;
	
	public BearingLocaliserControl(String unitName) {
		super(unitType, unitName);

		detectionMonitor = new DetectionMonitor(this);
		addPamProcess(detectionMonitor);
		bearingProcess = new BearingProcess(this);
		addPamProcess(bearingProcess);
		
		bearingAlgorithmProviders.add(new TOADBearingProvider(this));
		BeamFormBearingWrapper beamFormBearingWrapper = new BeamFormBearingWrapper(this);
		List<BearingAlgorithmProvider> bfAlgoList = beamFormBearingWrapper.getWrappedAlgorithms();
		bearingAlgorithmProviders.addAll(bfAlgoList);
		
		bearingDisplayProvider = new BearingDisplayProvider(this);
		UserDisplayControl.addUserDisplayProvider(bearingDisplayProvider);
		
		PamSettingManager.getInstance().registerSettings(this);
		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		UserDisplayControl.removeDisplayProvider(bearingDisplayProvider);
		return super.removeUnit();
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
				showDetectionMenu(parentFrame);
			}
		});
		if (isViewer() == false) {
			return menuItem;
		}
		JMenu menu = new JMenu(getUnitName());
		menu.add(menuItem);
		menuItem = new JMenuItem("offline ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showOfflineDialog(parentFrame);
			}
		});
		menu.add(menuItem);
		return menu;
	}
	
	protected void showOfflineDialog(Frame parentFrame) {
//		if (olProcessDialog == null) {
			OfflineTaskGroup otg = new OfflineTaskGroup(this, this.getUnitName());
			otg.addTask(new BLOfflineTask(this));
			otg.setPrimaryDataBlock(detectionMonitor.getParentDataBlock());
			olProcessDialog = new OLProcessDialog(parentFrame, otg, getUnitName());
//		}
		olProcessDialog.setVisible(true);		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			detectionMonitor.prepareProcess();
			bearingProcess.prepareBearingGroups();
			break;
		}
		super.notifyModelChanged(changeType);
	}

	private PamDialogFX2AWT<BearingLocaliserParams> settingsDialog;

	private static String helpPoint = "localisation.bearingLocaliser.docs.BL_Overview";
	
	public void showDetectionMenu(Frame parentFrame) {
//		if (settingsDialog == null) {
			BearingLocSettingsPane setPane = new BearingLocSettingsPane(parentFrame, this);
			settingsDialog = new PamDialogFX2AWT<>(parentFrame, setPane, false);
//		}
		BearingLocaliserParams newParams = settingsDialog.showDialog(bearingLocaliserParams);
		if (newParams != null) {
			bearingLocaliserParams = newParams;
			bearingProcess.prepareBearingGroups();
		}
	}

	public List<BearingAlgorithmProvider> getAlgorithmList() {
		return bearingAlgorithmProviders;
	}

	/**
	 * Find an algorithm provider by name. 
	 * @param algoName algorithm name
	 * @return
	 */
	public BearingAlgorithmProvider findAlgorithmByName(String algoName) {
		for (BearingAlgorithmProvider bap:bearingAlgorithmProviders) {
			if (bap.getStaticProperties().getName().equals(algoName)) {
				return bap;
			}
		}
		return null;
	}

	@Override
	public Serializable getSettingsReference() {
		return bearingLocaliserParams;
	}

	@Override
	public long getSettingsVersion() {
		return BearingLocaliserParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		bearingLocaliserParams = ((BearingLocaliserParams) pamControlledUnitSettings.getSettings()).clone();
		return bearingLocaliserParams != null;
	}

	/**
	 * @return the bearingLocaliserParams
	 */
	public BearingLocaliserParams getBearingLocaliserParams() {
		return bearingLocaliserParams;
	}

	public void estimateBearings(PamDataUnit triggerData) {
		bearingProcess.estimateBearings(triggerData);
	}

	/**
	 * @return the bearingProcess
	 */
	public BearingProcess getBearingProcess() {
		return bearingProcess;
	}

	/**
	 * @return the detectionMonitor
	 */
	public DetectionMonitor getDetectionMonitor() {
		return detectionMonitor;
	}

	/**
	 * @return the bearingAnnotationType
	 */
	public BearingAnnotationType getBearingAnnotationType() {
		return bearingAnnotationType;
	}

	/**
	 * @return the configObservable
	 */
	public SimpleObservable<PamDataUnit> getConfigObservable() {
		return configObservable;
	}

	public void addDownstreamLocalisationContents(int localisationContents) {
		// TODO Auto-generated method stub
		
	}

	public String getHelpPoint() {
		return helpPoint;
	}

	@Override
	public LocalisationAlgorithmInfo getAlgorithmInfo() {
		return this;
	}

	
	private BearingAlgorithm findAlgorithm() {
		BearingAlgorithmGroup[] groups = bearingProcess.getBearingAlgorithmGroups();
		if (groups == null) {
			return null;
		}
		for (int i = 0; i < groups.length; i++) {
			BearingAlgorithm ba = groups[i].bearingAlgorithm;
			if (ba != null) {
				return ba;
			}
		}
		return null;
	}
	
	@Override
	public int getLocalisationContents() {
		int cont = LocContents.HAS_BEARING | LocContents.HAS_BEARINGERROR;
		// work out if we should also add ambiguity. How to work that out ? 
		return cont;
	}

	@Override
	public String getAlgorithmName() {
//		BearingAlgorithm ba = findAlgorithm();
//		if (ba == null) {
//			return null;
//		}
//		ba.getParams().
		return getUnitType();
	}

	@Override
	public Serializable getParameters() {
		return bearingLocaliserParams;
	}
}
