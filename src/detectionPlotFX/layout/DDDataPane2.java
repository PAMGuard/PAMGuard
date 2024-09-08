package detectionPlotFX.layout;

import java.util.ArrayList;

import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.data.DDPlotRegister;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;

/**
 * ComboBox which holds the options for different plots to be displayed. This pane is much redcuded in szie
 * compared to DDataPane which uses radio buttons. 
 * 
 * 
 * @author Jamie Macaulay
 *
 */
public class DDDataPane2 extends PamBorderPane {
	
	
	/**
	 * Data block selection
	 */
	private ChoiceBox<String> dataPlots;


	/**
	 * Reference to the detection plot display. 
	 */
	private DetectionPlotDisplay detectionPlotDisplay;



	/**
	 * True if selecting parent datablocks from the display is enabled
	 */
	private boolean isDataBlockSelect;

	/**
	 * Create the DDdataPane. 
	 * @param detectionPlotDisplay - the associated DetectionPlotDisplay
	 * @param userControl - true to allow user to control parent data block with drop down menu.
	 */
	public DDDataPane2 (DetectionPlotDisplay detectionPlotDisplay, boolean userControl){
		this.detectionPlotDisplay=detectionPlotDisplay; 
		this.setCenter(createDataPane(userControl));
		this.setPrefWidth(200);
		this.setPadding(new Insets(5,20,5,5));

	}


	/**
	 * Creates the pane which allows selection of a parent data block and shows which 
	 * plots are available when a data block has been selected
	 */
	private Pane createDataPane(boolean userControl){

		PamHBox hBox = new PamHBox(); 
		
		hBox.getChildren().addAll(dataPlots = new ChoiceBox<String>()); 

		return hBox; 
	}

	/**
	 * When a data block is selected, creates a pane which allows users to select how
	 * a detection is plotted. 
	 * @param ddDataInfo 
	 */
	private void populuatePlotSelection(DDDataInfo ddDataInfo){

		dataPlots.getItems().clear();

		//		System.out.println("Radio button START: " + ddDataInfo);

		if (ddDataInfo==null){
			return;
		}
		
		int index = 0; 
		for ( int i=0; i< ddDataInfo.getDetectionPlotCount(); i++){
		
			dataPlots.getItems().add(ddDataInfo.getDetectionPlot(i).getName()); 
			
			if (ddDataInfo.getCurrentDetectionPlot()==ddDataInfo.getDetectionPlot(i)) {
				index=i; 
			}
		}
		
		
		dataPlots.setOnAction((action)->{
			
			if (dataPlots.getSelectionModel().getSelectedIndex()<0) {
				return;
			}
			
			DetectionPlot detPlot =  ddDataInfo.getDetectionPlot(dataPlots.getSelectionModel().getSelectedIndex()); 
			
			ddDataInfo.setCurrentDetectionPlot(detPlot); 
			
			if (ddDataInfo.getCurrentDetectionPlot().getSettingsPane()!=null) {
				detectionPlotDisplay.setMinHidePaneHeight(ddDataInfo.getCurrentDetectionPlot().getSettingsPane().getMinHeight());

			}
			
			//don't want the hide button if there's no settings pane. 
			//detectionPlotDisplay.setEnableSettingsButton(ddDataInfo.getCurrentDetectionPlot().getSettingsPane()!=null); 

			//System.out.println()
			 //must setup the scroll bar after the current unit has been so axis are correct
			detectionPlotDisplay.setupScrollBar();
			//detectionPlotDisplay.drawCurrentUnit(); //redraw the unit again. 
			//detectionPlotDisplay.drawCurrentUnit();
			
			//nudge the plot by firing a scroll bar change
			detectionPlotDisplay.scrollBarChanged();
			
			
			if (detectionPlotDisplay.isEnableScrollBar()) {
				//need this if the scroll bar is not used 
				detectionPlotDisplay.drawCurrentUnit();
			}

		}); 
		
		
		dataPlots.getSelectionModel().select(index);

		
		//if only one plot is available indicate to the user that this pane does not need to be used. 
		if (ddDataInfo.getDetectionPlotCount()==1) dataPlots.setDisable(true);
		else dataPlots.setDisable(false);
	}

//	/**
//	 * Populate the combo box with the correct list of data blocks which are available in the data model. 
//	 */
//	private void populateComboBox(ArrayList<DDDataProvider> dataPlotsInfo){
//		if (dataBlockSelBox == null) return;
//
//		dataBlockSelBox.getItems().clear();
//		int selIndex=-1; 
//		for (int i=0; i<dataPlotsInfo.size(); i++){
//			dataBlockSelBox.getItems().add(dataPlotsInfo.get(i).getName());
//			if (detectionPlotDisplay.getCurrentDataInfo()!=null &&
//					dataPlotsInfo.get(i).getDataBlock()==detectionPlotDisplay.getCurrentDataInfo().getDataBlock()){
//				selIndex=i; 
//			}
//		}
//		//now select the correct item in the list
//		if (selIndex==-1) dataBlockSelBox.getSelectionModel().clearSelection();
//		else{
//			dataBlockSelBox.getSelectionModel().select(selIndex);
//		}
//	}
	
	/**
	 * Get the dataplot box.
	 * @return the data plot box. 
	 */
	public ChoiceBox<String> getDetectionPlotBox() {
		return dataPlots;
	}


	/**
	 * 
	 */
	protected void updateDataBlockList(){
		final ArrayList<DDDataProvider> dataPlotsInfo=DDPlotRegister.getInstance().getDataInfos();
	}


	/**
	 * Called whenever a controlled unit has changed. 
	 */
	public void notifyDataChange() {
		//populateComboBox(DDPlotRegister.getInstance().getDataInfos());
		populuatePlotSelection(detectionPlotDisplay.getCurrentDataInfo());
	}

	//	/**
	//	 * Set whether the parent data block is selectable from a combo box
	//	 * @param allow - true to allow the data block to be selected 
	//	 */
	//	public void setDataBlockSelect(boolean allow){
	//		this.isDataBlockSelect = allow; 
	//		
	//		if (allow & !holderPane.getChildren().contains(dataBlockSelBox)){
	//			holderPane.getChildren().add(dataBlockSelBox); 
	//			this.setPrefWidth(200);
	//		}
	//		else{
	//			this.setPrefWidth(150);
	//			this.holderPane.getChildren().remove(dataBlockSelBox); 
	//		}
	////		this.dataBlockSelBox.setVisible(allow);
	//	}


	/**
	 * Check whether selection of data block is enabled. 
	 * @return true if selection of data blocks is selected. 
	 */
	public boolean isDataBlockSelect() {
		return this.isDataBlockSelect;
	}


}

