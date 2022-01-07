package group3dlocaliser;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import group3dlocaliser.algorithm.crossedbearing.CrossedBearingGroupLocaliser;
import group3dlocaliser.algorithm.gridsearch.TOADGridSearch;
import group3dlocaliser.algorithm.hyperbolic.HyperbolicLocaliser;
import group3dlocaliser.algorithm.toadsimplex.ToadSimplexLocaliser;
import group3dlocaliser.dialog.GroupLocSettingPaneFX;
import group3dlocaliser.offline.Group3DOfflineTask;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public class Group3DLocaliserControl extends PamControlledUnit implements PamSettings {
	
	public static final String unitType = "Group 3D Localiser";
	
	private Group3DProcess group3dProcess;
	
	private Group3DParams group3dParams = new Group3DParams();

	private PamDialogFX2AWT<Group3DParams> settingsDialog;
	
	private ArrayList<LocaliserAlgorithm3D> algorithms3D;
	
	private Group3DOfflineTask g3DOfflineTask;

	public Group3DLocaliserControl(String unitName) {
		super(unitType, unitName);
		algorithms3D = new ArrayList<>();
		algorithms3D.add(new CrossedBearingGroupLocaliser());
		algorithms3D.add(new HyperbolicLocaliser(this));
		algorithms3D.add(new ToadSimplexLocaliser(this, 2));
		algorithms3D.add(new ToadSimplexLocaliser(this, 3));
//		algorithms3D.add(new TOADGridSearch(this));
		
		group3dProcess = new Group3DProcess(this);
		addPamProcess(group3dProcess);
		PamSettingManager.getInstance().registerSettings(this);
		
		/*
		 * only used in viewer, but no hard in creating it. 
		 */
		g3DOfflineTask = new Group3DOfflineTask(this);
		addOfflineTask(g3DOfflineTask);
	}

	@Override
	public Serializable getSettingsReference() {
		return group3dParams;
	}

	@Override
	public long getSettingsVersion() {
		return Group3DParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		group3dParams = ((Group3DParams) pamControlledUnitSettings.getSettings()).clone();
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
				showSettingsMenu(parentFrame);
			}
		});
		if (isViewer() == false) {
			return menuItem;
		}
		// Otherwise make a more complex menu.
		JMenu menu = new JMenu(getUnitName());
		menuItem.setText("Settings...");
		menu.add(menuItem);
		menuItem = new JMenuItem("Offline Processing ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runOfflineProcessing();
			}
		});
		menu.add(menuItem);
		return menu;
	}

	protected void runOfflineProcessing() {
		Group3DOfflineTask offlineTask = new Group3DOfflineTask(this);
		OfflineTaskGroup otg = new OfflineTaskGroup(this, getUnitName());
		otg.addTask(offlineTask);
		offlineTask.setParentDataBlock(group3dProcess.getParentDataBlock());
		otg.setPrimaryDataBlock(group3dProcess.getParentDataBlock());
		OLProcessDialog olpd = new OLProcessDialog(getGuiFrame(), otg, getUnitName());
		olpd.setVisible(true);
	}

	public boolean showSettingsMenu(Frame parentFrame) {
//		Group3DParams newParams = Group3DSwingDialog.showDialog(parentFrame, this);
//		if (newParams != null) {
//			grid3dParams = newParams;
//			grid3dProcess.prepareProcess();
//		}
		if (settingsDialog == null) {
			GroupLocSettingPaneFX groupLocSettingPane = new GroupLocSettingPaneFX(this, parentFrame);
			settingsDialog = new PamDialogFX2AWT<>(parentFrame,groupLocSettingPane,false);
		}
		Group3DParams newParams = settingsDialog.showDialog(group3dParams);
		if (newParams != null) {
			group3dParams = newParams;
			group3dProcess.prepareProcess();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @return the grid3dParams
	 */
	public Group3DParams getGrid3dParams() {
		return group3dParams;
	}

	/**
	 * @param grid3dParams the grid3dParams to set
	 */
	public void setGrid3dParams(Group3DParams grid3dParams) {
		this.group3dParams = grid3dParams;
	}
	
	/**
	 * Get the parameters for a specific algorithm type
	 * @param algoProvider algorithm provider
	 * @return algorithm params or null if not yet set
	 */
	public LocaliserAlgorithmParams getLocaliserAlgorithmParams(LocaliserAlgorithm3D algoProvider) {
		return group3dParams.getAlgorithmParams(algoProvider);
	}
	
	/**
	 * Set the parameters for a specific algorithm type
	 * @param algoProvider algorithm provider
	 * @param localiserAlgorithmParams specific params, or null to remove them from the list. 
	 */
	public void setAlgorithmParams(LocaliserAlgorithm3D algoProvider, LocaliserAlgorithmParams localiserAlgorithmParams) {
		group3dParams.setAlgorithmParams(algoProvider, localiserAlgorithmParams);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
	}

	/**
	 * Find an algorithm provider with the given name. 
	 * @param algorithmName Algorithm name
	 * @return Algorithm Provider
	 */
	public LocaliserAlgorithm3D findAlgorithm(String algorithmName) {
		if (algorithmName == null) {
			return null;
		}
		for (LocaliserAlgorithm3D algoProvider: algorithms3D) {
			if (algoProvider.getName().equals(algorithmName)) {
				return algoProvider;
			}
		}
		return null;
	}

	/**
	 * @return the algorithmProviders
	 */
	public ArrayList<LocaliserAlgorithm3D> getAlgorithmProviders() {
		return algorithms3D;
	}
	
	
	public String getDataSelectorName() {
		return this.getUnitName();
	}

	/**
	 * @return the group3dProcess
	 */
	public Group3DProcess getGroup3dProcess() {
		return group3dProcess;
	}

	/**
	 * @return the g3DOfflineTask
	 */
	public Group3DOfflineTask getG3DOfflineTask() {
		return g3DOfflineTask;
	}
}
