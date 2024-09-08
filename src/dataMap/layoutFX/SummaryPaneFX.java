package dataMap.layoutFX;

import java.util.ArrayList;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamUtils.PamCalendar;
import dataMap.DataMapControl;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;

/**
 * Display for summary data inserted at top of DataMap
 * 
 * @author Jamie Macaulay
 *
 */
public class SummaryPaneFX extends PamBorderPane {
	
	/**
	 * The time at the start of the scrolling pane
	 */
	private Label windowStart;
	
	/**
	 * The time at the end of the scrolling pane. 
	 */
	private Label windowEnd;
	
	/*
	 *The current  cursor time.  
	 */
	private Label cursorTime;
	
	/**
	 * The maximum number of PAMGuard data stores. Just used to preallocate arrays to a reasonable size. 
	 */
	private final int maxDataSources = 12;

	/**
	 * List of offline data store name labels
	 */
	private Label[] dataNames;

	/**
	 * List of " to " labels 
	 */
	private Label[] toLabels;

	/**
	 * List of data start labels for offline data stores 
	 */
	private Label[] dataStarts;

	/**
	 * List of data end labels. 
	 */
	private Label[] dataEnds;

	/*
	 * Reference to the datamap control.
	 */
	private DataMapControl dataMapControl;

	/**
	 * Grid pane which holds labels. 
	 */
	private PamGridPane dataStorePane;

	/**
	 * Reference to the data map pane. 
	 */
	private DataMapPaneFX dataMapPaneFX;


	public SummaryPaneFX(DataMapControl dataMapControl, DataMapPaneFX dataMapPaneFX){ 
			this.dataMapControl = dataMapControl;
			this.dataMapPaneFX=dataMapPaneFX;
			this.setMinHeight(0);
			this.toBack();
			this.setCenter(createSummaryPane());
	}
	
	/**
	 * Create the summary pane. 
	 */
	private Node createSummaryPane(){
		
		// data stream info. 	
		PamGridPane currentTimePane=new PamGridPane();
		currentTimePane.setHgap(5);
		currentTimePane.setHgap(5);
		
		Label cursor=new Label("Cursor: ");
		cursor.setAlignment(Pos.CENTER_RIGHT);
		currentTimePane.add(new Label("Cursor: "), 0, 0);
		currentTimePane.add(new Label("Loaded data start : "), 0, 1);
		currentTimePane.add(new Label("Loaded Data End: "), 0, 2);
	
		currentTimePane.add(cursorTime=new Label(""), 1, 0);
		currentTimePane.add(windowStart=new Label(""), 1, 1);
		currentTimePane.add(windowEnd=new Label(""), 1, 2);
		
		PamVBox dataStreamInfoHolder=new PamVBox();
		dataStreamInfoHolder.setSpacing(5);
		Label dataSTreamTitle=new Label("Selected Data");
//		dataSTreamTitle.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(dataSTreamTitle);

		dataStreamInfoHolder.getChildren().addAll(dataSTreamTitle, currentTimePane); 
		
		//pane to show info on data store.  
		dataNames = new Label[maxDataSources];
		toLabels = new Label[maxDataSources];
		dataStarts = new Label[maxDataSources];
		dataEnds = new Label[maxDataSources];
		
		dataStorePane=new PamGridPane();
		dataStorePane.setHgap(5);
		dataStorePane.setHgap(5);
		//create label for all data stores. 
		for (int i = 0; i < maxDataSources; i++) {
			dataNames[i] = new Label();
			dataNames[i].setMinHeight(0);
			dataStarts[i] = new Label();
			dataStarts[i].setMinHeight(0);
			toLabels[i] = new Label(" to ");
			toLabels[i].setMinHeight(0);
			dataEnds[i] = new Label();
			dataEnds[i].setMinHeight(0);
		}
		
		PamVBox dataStoreInfoHolder=new PamVBox();
		dataStoreInfoHolder.setSpacing(5);
		Label dataStoreTitle=new Label("Data Sources");
//		dataStoreTitle.setFont(PamGuiManagerFX.titleFontSize2);
		PamGuiManagerFX.titleFont2style(dataStoreTitle);

		dataStoreInfoHolder.getChildren().addAll(dataStoreTitle, dataStorePane); 		
		
		//holds all panes
		PamHBox holder=new PamHBox();
		holder.setSpacing(20);
		holder.getChildren().addAll(dataStreamInfoHolder, dataStoreInfoHolder);
				
		return holder;
	}
	
	public void newDataSources() {
		ArrayList<OfflineDataStore> offlineDataStores = PamController.getInstance().findOfflineDataStores();
		OfflineDataStore aSource;
		int n = Math.min(offlineDataStores.size(), maxDataSources);
		long[] dataExtent;
		dataStorePane.getChildren().clear(); 
		
		for (int i = 0; i < n; i++) {
			dataStorePane.add( dataNames[i], 0, i);
			dataStorePane.add( dataStarts[i] , 1, i);
			dataStorePane.add( toLabels[i], 2, i);
			dataStorePane.add( dataEnds[i] , 3, i);
			aSource = offlineDataStores.get(i);
			dataNames[i].setText(aSource.getDataSourceName());
			dataExtent = dataMapControl.getDataExtent(aSource);
			if (dataExtent[0] == Long.MAX_VALUE) {
				dataStarts[i].setText(" ---No data---");
				dataEnds[i].setText(" ---No data---");
			}
			else {
			dataStarts[i].setText(PamCalendar.formatDateTime2(dataExtent[0]));
			dataEnds[i].setText(PamCalendar.formatDateTime(dataExtent[1]));
			}
		}
		for (int i = offlineDataStores.size(); i < maxDataSources; i++) {
			
		}

	}
	
	/**
	 * Show the current time that corresponds to the position of the cursor on the data map. 
	 * @param timeMillis - the position of the cursor in millis. 
	 */
	public void setCursorTime(Long timeMillis) {
		if (timeMillis == null) {
			cursorTime.setText("");
		}
		else {
			cursorTime.setText(PamCalendar.formatDateTime2(timeMillis));
		}
	}

	/**
	 * Set the labels to show the current start and end time of loaded data in the current data stream pane. 
	 * @param timeStart - start of loaded data in millis. 
	 * @param timeEnd - end of loaded data in millis. 
	 */
	public void setSelectedDataTime(Long timeStart, Long timeEnd) {
		if (timeStart == null || timeStart==0 || timeStart==Long.MAX_VALUE) {
			windowStart.setText("---No Data Loaded---");
		}
		else {
			windowStart.setText(PamCalendar.formatDateTime2(timeStart));
		}
		if (timeEnd == null || timeEnd==0 || timeEnd==Long.MAX_VALUE) {
			windowEnd.setText("---No Data Loaded---");
		}
		else {
			windowEnd.setText(PamCalendar.formatDateTime2(timeEnd));
		}
	}




}
