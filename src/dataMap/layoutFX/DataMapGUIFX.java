package dataMap.layoutFX;

import java.util.ArrayList;

import dataMap.DataMapControl;
import dataMap.DataMapControlGUI;
import javafx.application.Platform;
import pamViewFX.PamControlledGUIFX;
import userDisplayFX.UserDisplayNodeFX;

/**
 * The GUI for the data map. 
 */
public class DataMapGUIFX extends PamControlledGUIFX implements DataMapControlGUI {

	private DataMapControl dataMapControl;
	
	private ArrayList<UserDisplayNodeFX> dataMapDisplays;

	private DataMapPaneFX dataMapPaneFX;

	public DataMapGUIFX(DataMapControl dataMapControl) {
		this.dataMapControl=dataMapControl;
	}
	
	/**
	 * The data map displays to add. 
	 */
	public ArrayList<UserDisplayNodeFX> getDisplays(){
		try {
		if (dataMapDisplays==null){
			dataMapPaneFX = new DataMapPaneFX(dataMapControl);
			dataMapDisplays=new ArrayList<UserDisplayNodeFX>();
			dataMapPaneFX.newSettings();
			dataMapDisplays.add(dataMapPaneFX);
		}
		return dataMapDisplays;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void newDataSources() {
		dataMapPaneFX.newDataSources();
	}

	@Override
	public void createDataGraphs() {
		dataMapPaneFX.createDataGraphs();
	}

	@Override
	public void repaintAll() {
		dataMapPaneFX.repaintAll();	
	}
	
	public void notifyModelChanged(int changeType) {
		Platform.runLater(()->{
			dataMapPaneFX.notifyModelChanged(changeType);
		}); 
	}

}
