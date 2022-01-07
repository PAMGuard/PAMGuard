package detectionPlotFX.layout;

import java.util.ArrayList;

import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.data.DDPlotRegister;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane which allows users to select parent data blocks. 
 * @author Jamie Macaulay
 *
 */
public class DDDataPane extends PamBorderPane {

	/**
	 * Data block selection
	 */
	private ComboBox<String> dataBlockSelBox;


	/**
	 * Reference to the detection plot display. 
	 */
	private DetectionPlotDisplay detectionPlotDisplay;

	/**
	 * Pane which allows users to select a plot. 
	 */
	private PamVBox plotSelPane;

	/**
	 * The mian holder pane. 
	 */
	private VBox holderPane;

	/**
	 * True if selecting parent datablocks from the display is enabled
	 */
	private boolean isDataBlockSelect;

	/**
	 * Create the DDdataPane. 
	 * @param detectionPlotDisplay - the associated DetectionPlotDisplay
	 * @param userControl - true to allow user to control parent data block with drop down menu.
	 */
	public DDDataPane (DetectionPlotDisplay detectionPlotDisplay, boolean userControl){
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

		holderPane=new PamVBox(); 
		holderPane.setSpacing(5);
		holderPane.setPadding(new Insets(5,5,5,5));

		Label title=new Label(); 
		title.setText("Select Plot");
		PamGuiManagerFX.titleFont2style(title);

//		title.setFont(PamGuiManagerFX.titleFontSize2);

		plotSelPane=new PamVBox();
		plotSelPane.setSpacing(5);
		
		if (userControl) {
			//add the combobox which allows for data block selection
			dataBlockSelBox=new ComboBox<String>();
			dataBlockSelBox.prefWidthProperty().bind(this.widthProperty());

			dataBlockSelBox.valueProperty().addListener( (ov, bef, aft)->{
				int sel=dataBlockSelBox.getSelectionModel().getSelectedIndex();
				if (sel>=0){
					//set a new data info in the display
					detectionPlotDisplay.setDataInfo(DDPlotRegister.getInstance().
							getDataInfos().get(dataBlockSelBox.getSelectionModel().getSelectedIndex()));

					detectionPlotDisplay.newDataBlockAdded(DDPlotRegister.getInstance().
							getDataInfos().get(dataBlockSelBox.getSelectionModel().getSelectedIndex()).getDataBlock());
					//send notification to data model to change
					detectionPlotDisplay.dataModelToDisplay();
					//generate a plot selection pane. 
					populuatePlotSelection(detectionPlotDisplay.getCurrentDataInfo());
				}
			});
			
			holderPane.getChildren().addAll(title, dataBlockSelBox, plotSelPane);
		}
		else {
			holderPane.getChildren().addAll(title, plotSelPane);
		}

		return holderPane; 
	}

	/**
	 * When a data block is selected, creates a pane which allows users to select how
	 * a detection is plotted. 
	 * @param ddDataInfo 
	 */
	private void populuatePlotSelection(DDDataInfo ddDataInfo){

		plotSelPane.getChildren().clear();

		//		System.out.println("Radio button START: " + ddDataInfo);

		if (ddDataInfo==null){
			return;
		}

		//		System.out.println("Radio button START 1: " + ddDataInfo.getDetectionPlotCount());
		final ToggleGroup group = new ToggleGroup();

		RadioButton rb; 
		for ( int i=0; i< ddDataInfo.getDetectionPlotCount(); i++){
			rb=new RadioButton(ddDataInfo.getDetectionPlot(i).getName());
			rb.setToggleGroup(group); //set the toggle group. 
			rb.setUserData(ddDataInfo.getDetectionPlot(i)); //set plot using JavaFX convenience method. 
			group.selectedToggleProperty().addListener((ov,
					old_toggle,  new_toggle) ->{
						if (group.getSelectedToggle() != null) {
							//set the plot to that value. 
							ddDataInfo.setCurrentDetectionPlot((DetectionPlot) group.getSelectedToggle().getUserData());

							if (ddDataInfo.getCurrentDetectionPlot().getSettingsPane()!=null) {
								detectionPlotDisplay.setMinHidePaneHeight(ddDataInfo.getCurrentDetectionPlot().getSettingsPane().getMinHeight());

							}
							//don't want the hide button if there's no settings pane. 
							//detectionPlotDisplay.setEnableSettingsButton(ddDataInfo.getCurrentDetectionPlot().getSettingsPane()!=null); 
							
							//System.out.println()
							detectionPlotDisplay.drawCurrentUnit();
							detectionPlotDisplay.setupScrollBar(); //must setup the scroll bar after the current unit has been so axis are correct
							//detectionPlotDisplay.drawCurrentUnit(); //redraw the unit again. 
						}                
					});
			if (ddDataInfo.getCurrentDetectionPlot()==ddDataInfo.getDetectionPlot(i)) {
				//select this radio button
				rb.setSelected(true);
				detectionPlotDisplay.drawCurrentUnit();
			}
			//			System.out.println("Add radio button for: " + ddDataInfo.getDetectionPliot(i).getName());
			plotSelPane.getChildren().add(rb);
		}

		//if only one plot is available indicate to the user that this pane does not need to be used. 
		if (ddDataInfo.getDetectionPlotCount()==1) plotSelPane.setDisable(true);
		else plotSelPane.setDisable(false);
	}

	/**
	 * Populate the combo box with the correct list of data blocks which are available in the data model. 
	 */
	private void populateComboBox(ArrayList<DDDataProvider> dataPlotsInfo){
		if (dataBlockSelBox == null) return;

		dataBlockSelBox.getItems().clear();
		int selIndex=-1; 
		for (int i=0; i<dataPlotsInfo.size(); i++){
			dataBlockSelBox.getItems().add(dataPlotsInfo.get(i).getName());
			if (detectionPlotDisplay.getCurrentDataInfo()!=null &&
					dataPlotsInfo.get(i).getDataBlock()==detectionPlotDisplay.getCurrentDataInfo().getDataBlock()){
				selIndex=i; 
			}
		}
		//now select the correct item in the list
		if (selIndex==-1) dataBlockSelBox.getSelectionModel().clearSelection();
		else{
			dataBlockSelBox.getSelectionModel().select(selIndex);
		}
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
		populateComboBox(DDPlotRegister.getInstance().getDataInfos());
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
