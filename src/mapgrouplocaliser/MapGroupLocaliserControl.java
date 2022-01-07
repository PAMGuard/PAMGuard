package mapgrouplocaliser;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import annotation.handler.AnnotationChoiceHandler;
import generalDatabase.DBControlUnit;
import generalDatabase.SQLLogging;

public class MapGroupLocaliserControl extends PamControlledUnit implements PamSettings {
	
	public static final String unitType = "Map Group Localiser";
	
	private MarkGroupProcess markGroupProcess;
	
	private MapGroupOverlayManager mapGroupOverlayManager;
	
	private  ParameterType[] mapOverlayParameters = {ParameterType.LATITUDE, ParameterType.LONGITUDE};
	
	private ParameterUnits[] mapOverlayParameterUnits = {ParameterUnits.DECIMALDEGREES, ParameterUnits.DECIMALDEGREES};
	
	private MapGrouperSettings mapGrouperSettings = new MapGrouperSettings();
	
	private AnnotationChoiceHandler annotationHandler;

	public MapGroupLocaliserControl(String unitName) {
		super(unitType, unitName);
		mapGroupOverlayManager = new MapGroupOverlayManager(this);
		markGroupProcess = new MarkGroupProcess(this);
		addPamProcess(markGroupProcess);
		OverlayMarkObservers.singleInstance().addObserver(markGroupProcess);
		annotationHandler = new MapGroupAnnotationHandler(this, markGroupProcess.getOutputDataBlock(0));
		
		PamSettingManager.getInstance().registerSettings(this);
		
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			subscribeMarkObserver();
			annotationHandler.loadAnnotationChoices();
			sortSQLLogging();
			break;
		}
	}

	/**
	 * Check all the SQL Logging additions are set up correctly. 
	 */
	private void sortSQLLogging() {
		MarkGroupSQLLogging markGroupSQLLogging = markGroupProcess.getMarkGroupSQLLogging();
		markGroupSQLLogging.setTableDefinition(new MarkGroupTableDefinition(getUnitName(), 
				SQLLogging.UPDATE_POLICY_OVERWRITE));
		if (annotationHandler.addAnnotationSqlAddons(markGroupSQLLogging) > 0) {
			// will have to recheck the table in the database. 
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc != null) {
				dbc.getDbProcess().checkTable(markGroupSQLLogging.getTableDefinition());
			}
		}
	}

	private void subscribeMarkObserver() {
//		ArrayList<OverlayMarker> obsList = OverlayMarkProviders.singleInstance().getMarkProviders(mapOverlayParameters);
		MarkRelationships markRelationships = MarkRelationships.getInstance();
		markRelationships.subcribeToMarkers(getMarkGroupProcess());
//		for (int i = 0; i < obsList.size(); i++) {
//			OverlayMarker overlayMarker = obsList.get(i);
//			boolean linked = markRelationships.getRelationship(overlayMarker, getMarkGroupProcess());
//			if (linked) {
//				overlayMarker.addObserver(markGroupProcess);
//			}
//			else {
//				overlayMarker.removeObserver(markGroupProcess);
//			}
////			MarkSelectionData msd = mapGrouperSettings.getMarkerSelectionData(overlayMarker.getMarkerName());
////			overlayMarker.removeObserver(markGroupProcess);
////			if (msd.selectMark) {
////				overlayMarker.addObserver(markGroupProcess);
////			}
//		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " options...");
		menuItem.addActionListener(new MenuOptions(parentFrame));
		
		return menuItem;
	}
	
	private class MenuOptions implements ActionListener {

		private Frame parentFrame;
		
		public MenuOptions(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showSettingsDialog(parentFrame);
		}
		
	}
	
	private boolean showSettingsDialog(Window parentFrame) {
		boolean ans = MapGrouperDialog.showDialog(parentFrame, this, getUnitName());
		if (ans) {
			subscribeMarkObserver();
			sortSQLLogging();
		}
		return ans;
	}

	/**
	 * @return the mapGroupOverlayManager
	 */
	public MapGroupOverlayManager getMapGroupOverlayManager() {
		return mapGroupOverlayManager;
	}

	/**
	 * @return the mapOverlayParameters
	 */
	public ParameterType[] getMapOverlayParameters() {
		return mapOverlayParameters;
	}

	/**
	 * @return the mapGrouperSettings
	 */
	public MapGrouperSettings getMapGrouperSettings() {
		return mapGrouperSettings;
	}

	@Override
	public Serializable getSettingsReference() {
		return mapGrouperSettings;
	}

	@Override
	public long getSettingsVersion() {
		return MapGrouperSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mapGrouperSettings = ((MapGrouperSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the annotationHandler
	 */
	protected AnnotationChoiceHandler getAnnotationHandler() {
		return annotationHandler;
	}

	/**
	 * @return the mapOverlayParameterUnits
	 */
	public ParameterUnits[] getMapOverlayParameterUnits() {
		return mapOverlayParameterUnits;
	}

	/**
	 * @return the markGroupProcess
	 */
	protected MarkGroupProcess getMarkGroupProcess() {
		return markGroupProcess;
	}

}
