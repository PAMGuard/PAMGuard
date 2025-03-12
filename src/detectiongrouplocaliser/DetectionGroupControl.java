package detectiongrouplocaliser;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import Localiser.LocalisationAlgorithm;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import detectiongrouplocaliser.dialogs.DetectionGroupDialog;
import detectiongrouplocaliser.dialogs.DetectionGroupTableProvider;
import detectiongrouplocaliser.dialogs.DisplayOptionsHandler;
import detectiongrouplocaliser.tethys.DetectionGroupSpeciesManager;
import tethys.species.DataBlockSpeciesManager;
import userDisplay.UserDisplayControl;

/**
 * Class for grouping any type of data together. Not really a localiser, but 
 * does include some localisation options.<br> 
 * Will attempt to offer a variety of localisation options and will store data for each group
 * in a pair of database tables. 
 * @author dg50
 *
 */
public class DetectionGroupControl extends PamControlledUnit implements PamSettings {
	
	final static public String unitType = "Detection Group Localiser";
	
	private DetectionGroupSettings detectionGroupSettings = new DetectionGroupSettings();

	private DetectionGroupProcess detectionGroupProcess;

	private ArrayList<DetectionGroupObserver> groupObservers = new ArrayList<>();
	
	private DisplayOptionsHandler displayOptionsHandler;
	
	private DetectionGroupSpeciesManager detectionGroupSpeciesManager;
	
	public DetectionGroupControl(String unitName) {
		super(unitType, unitName);
		this.detectionGroupProcess = new DetectionGroupProcess(this);
		this.addPamProcess(detectionGroupProcess);
		
		displayOptionsHandler = new DisplayOptionsHandler(this);
		
		UserDisplayControl.addUserDisplayProvider(new DetectionGroupTableProvider(this));
		
		PamSettingManager.getInstance().registerSettings(this);
		getDetectionGroupSettings().getAnnotationChoices().setAllowMany(true);
	}
	

	@Override
	public Serializable getSettingsReference() {
		return detectionGroupSettings;
	}

	@Override
	public long getSettingsVersion() {
		return DetectionGroupSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		detectionGroupSettings = ((DetectionGroupSettings) pamControlledUnitSettings.getSettings()).clone();
		return detectionGroupSettings != null;
	}

	/**
	 * @return the detectionGroupSettings
	 */
	public DetectionGroupSettings getDetectionGroupSettings() {
		return detectionGroupSettings;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings...");
		menuItem.setToolTipText("Data selection and annotation configuration");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsMenu(parentFrame);
			}
		});
		return menuItem;
	}

	protected void settingsMenu(Frame parentFrame) {
		boolean ok = DetectionGroupDialog.showDialog(parentFrame, this);
		if (ok) {
			notifyGroupModelChanged();
			detectionGroupProcess.sortSQLLogging();
		}
	}

	/**
	 * @return the detectionGroupProcess
	 */
	public DetectionGroupProcess getDetectionGroupProcess() {
		return detectionGroupProcess;
	}

	/**
	 * Add an observer of group structure or data changes. 
	 * @param groupObserver
	 */
	public void addGroupObserver(DetectionGroupObserver groupObserver) {
		groupObservers.add(groupObserver);
	}
	/**
	 * Remove an observer of group changes
	 * @param groupObserver
	 */
	public void removeGroupObserveR(DetectionGroupObserver groupObserver) {
		groupObservers.remove(groupObserver);
	}
	
	/**
	 * Notify any observers that data have changed. 
	 */
	public void notifyGroupDataChanged() {
		for (DetectionGroupObserver obs:groupObservers) {
			obs.dataChanged();
		}
	}
	/**
	 * Notify any observers that the group model has changed 
	 * (effectively this means a change in annotations). 
	 */
	public void notifyGroupModelChanged() {
		for (DetectionGroupObserver obs:groupObservers) {
			obs.modelChanged();
		}
	}

	public SymbolData getSymbolforMenuItems(PamDataUnit dgdu) {
		if (dgdu == null) {
			return null;
		}
		Color col = PamColors.getInstance().getWhaleColor((int) dgdu.getUID());
		return new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 16, 16, true, col, col);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.DATA_LOAD_COMPLETE:
			notifyGroupDataChanged();
		}
	}


	/**
	 * @return the displayOptionsHandler
	 */
	public DisplayOptionsHandler getDisplayOptionsHandler() {
		return displayOptionsHandler;
	}


	public LocalisationAlgorithm getLocalisationAlgorithm() {
		GroupAnnotationHandler annotationHandler = detectionGroupProcess.getAnnotationHandler();
		List<DataAnnotationType<?>> usedAnnots = annotationHandler.getUsedAnnotationTypes();
		for (DataAnnotationType<?> annotType : usedAnnots) {
			if (annotType instanceof LocalisationAlgorithm) {
				return (LocalisationAlgorithm) annotType;
			}
		}
		return null;
	}


	public DataBlockSpeciesManager<DetectionGroupDataUnit> getDataBlockSpeciesManager() {
		DetectionGroupDataBlock dataBlock = detectionGroupProcess.getDetectionGroupDataBlock();
		if (detectionGroupSpeciesManager == null) {
			detectionGroupSpeciesManager = new DetectionGroupSpeciesManager(dataBlock);
		}
		// see if any of the annotations have a species manager and use that by preference. 
		GroupAnnotationHandler annHandler = detectionGroupProcess.getAnnotationHandler();
		if (annHandler == null) {
			return detectionGroupSpeciesManager;
		}
		List<DataAnnotationType<?>> usedAnnotations = annHandler.getUsedAnnotationTypes();
		for (DataAnnotationType<?> aType : usedAnnotations) {
			DataBlockSpeciesManager sppManager = aType.getDataBlockSpeciesManager();
			if (sppManager != null) {
				return sppManager;
			}
		}
		return detectionGroupSpeciesManager;
	}
}
