package dataPlotsFX.layout;

import java.util.ArrayList;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import PamController.PamGUIManager;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTilePane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import dataPlotsFX.data.DataTypeInfo;
import dataPlotsFX.data.TDScaleInfo;

/**
 * A pane which allows users to;
 * <p>
 * 1) Select the values shown on the y axis 
 * <br>
 * 2) Select which added data blocks are displayed on the graph
 * <br>
 * 3) Allow users to quickly access settings associated with data blocks. //TODO
 * 
 * @author Jamie Macaulay
 *
 */
public class TDDataSelPaneFX extends PamVBox {

	/**
	 * Reference to the TDGraphFX this pane belongs to.
	 */
	private TDGraphFX tdGraph;

	/**
	 * List of radio buttons for axis types. 
	 */
	private ArrayList<TDRadioButton> axisRadioButtons = new ArrayList<TDRadioButton>();

	/**
	 * Text field showing current axis min.
	 */
	private TextField textFieldMin;

	/**
	 * Text field showing current axis max. 
	 */
	private TextField textFieldMax;

	/**
	 * Pane which contains min/max text fields 
	 */
	private Pane axisMinMax; 

	/**
	 * List of check boxes corresponding to current channels. 
	 */
	private CheckBox[] channelCheckBox= new CheckBox[PamConstants.MAX_CHANNELS];

	/**
	 * Holds all check boxes 
	 */
	private PamTilePane plotList;

	/**
	 * The main pane for holding a list of checkbox channels and labels etc. 
	 */
	private PamVBox plotListHolder; 

	/**
	 * The pop over. 
	 */
	private PopOver popOver;

	private Pane dataControlPane;

	private MenuButton addMenuButton;

	private MenuButton removeMenuButton;

	public TDDataSelPaneFX(TDGraphFX tdGraph){
		this.tdGraph=tdGraph; 

		//		//CSS styling- pane has the standard PAMGUARD settings pane styling. 		 
		//		this.getStylesheets().add(getClass().getResource("/dataPlotsFX/fxNodes/hidingPane/pamSettingsCSS.css").toExternalForm());

		// set u-p the vBox
		this.setPadding(new Insets(10,10,10,10));
		this.setSpacing(10);

		plotList=new PamTilePane(); 
		plotListHolder=new PamVBox(); 

		//create the data control pane. 
		this.dataControlPane = createDataControlPane(); 
		axisMinMax=createMinMaxPane();

		remakePane (); 

	}

	/**
	 * Refresh the list of available axis names and channel check boxes. 
	 */
	public void remakePane (){
		//first remove all children. 
		this.getChildren().clear();

		//the data control pane.
		//Label addRemoveLabel = new Label("Display Data"); 
		//PamGuiManagerFX.titleFont2style(addRemoveLabel);
//		addRemoveLabel.setFont(PamGuiManagerFX.titleFontSize2);

		//this.getChildren().add(addRemoveLabel);
		this.getChildren().add(dataControlPane);

		if (tdGraph.getDataList().size()>0) {
			//Label yAxisLabel  = new Label("Y-Axis"); 
			//PamGuiManagerFX.titleFont2style(yAxisLabel);
//			yAxisLabel.setFont(PamGuiManagerFX.titleFontSize2);
			//this.getChildren().add(new Separator());

			//this.getChildren().add(yAxisLabel);
			this.getChildren().add(createYAxisDataList());
			this.getChildren().add(axisMinMax);
			this.getChildren().add(new Separator());
			this.getChildren().add(createDataInfoList());
			this.getChildren().add(plotListHolder);
		}

		populatePlotListPane();
	}

	/**
	 * Create the data control pane. 
	 * @return the data control pane. 
	 */
	private Pane createDataControlPane() {
		
		PamHBox controlPane = new PamHBox();
		controlPane.setSpacing(5);
		controlPane.setAlignment(Pos.CENTER_LEFT);

		addMenuButton = new MenuButton("Data"); 
		//addMenuButton.setPrefHeight(PamGuiManagerFX.iconSize);
//		addMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, Color.WHITE, PamGuiManagerFX.iconSize));
		addMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		addMenuButton.showingProperty().addListener((obsVal, oldVal, newVal)->{
			addMenuButton.getItems().clear();
			TDControlPaneFX.createAddMenuItems(addMenuButton.getItems(), this.tdGraph);
		});
		TDControlPaneFX.createAddMenuItems(addMenuButton.getItems(), this.tdGraph);

		removeMenuButton = new MenuButton("Data");
		//removeMenuButton.setPrefHeight(PamGuiManagerFX.iconSize);
//		removeMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.REMOVE, Color.WHITE, PamGuiManagerFX.iconSize));
		removeMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-minus", Color.WHITE, PamGuiManagerFX.iconSize));
		removeMenuButton.showingProperty().addListener((obsVal, oldVal, newVal)->{
			removeMenuButton.getItems().clear();
			TDControlPaneFX.createRemoveMenuItems(removeMenuButton.getItems(), this.tdGraph);
		});
		TDControlPaneFX.createRemoveMenuItems(removeMenuButton.getItems(), this.tdGraph);


		PamButton button = new PamButton(); 

//		button.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.COGS, Color.WHITE, PamGuiManagerFX.iconSize));
		button.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cogs", Color.WHITE, PamGuiManagerFX.iconSize));
		controlPane.getChildren().addAll(addMenuButton, removeMenuButton, button); 
		button.prefHeightProperty().bind(removeMenuButton.heightProperty());

		button.setOnAction((action)->{
			showPopMenu(button);
		});
		return controlPane;
	}

	/**
	 * Create a pane which shows and controls the min, max value of the axis. 
	 * @return pane to show and set min/max values of y (data) axis. 
	 */
	private Pane createMinMaxPane(){
		PamGridPane gridPane=new PamGridPane();
		gridPane.setHgap(10);
		gridPane.setPadding(new Insets(10,10,10,10));

		textFieldMin = new TextField ();
		textFieldMin.setPrefColumnCount(5);
		textFieldMax= new TextField ();
		textFieldMax.setPrefColumnCount(6);

		textFieldMin.focusedProperty().addListener((observable, oldValue, newValue) -> {
			//			System.out.println("textFieldMin: Focused: " + newValue+  "			min: " + tdGraph.getCurrentScaleInfo().getMaxVal() +  " max: " + tdGraph.getCurrentScaleInfo().getMinVal());
			if (newValue) 
				textFieldMin.textProperty().unbind();//need to unbind so can edit text box.
			else {
				setMinVal();
				setTextFieldBinding();
			}
		});

		textFieldMin.setOnAction((event) -> {
			//			System.out.println("textFieldMin:: enter has been pressed"+  "		min: " + tdGraph.getCurrentScaleInfo().getMaxVal() +  " max: " + tdGraph.getCurrentScaleInfo().getMinVal());
			setMinVal();
		});

		textFieldMax.focusedProperty().addListener((observable, oldValue, newValue) -> {
			//			System.out.println("textFieldMax: Focused: " + newValue+	"		min: " + tdGraph.getCurrentScaleInfo().getMaxVal() +  " max: " + tdGraph.getCurrentScaleInfo().getMinVal());
			if (newValue) 
				textFieldMax.textProperty().unbind();//need to unbind so can edit text box.
			else {
				setMaxVal();
				setTextFieldBinding();
			}
		});

		textFieldMax.setOnAction((event) -> {
			//			System.out.println("textFieldMax:: enter has been pressed"	+	"	min: " + tdGraph.getCurrentScaleInfo().getMaxVal() +  " max: " + tdGraph.getCurrentScaleInfo().getMinVal());
			setMaxVal();
		});


		//TODO- cleverly add units to labels. 
		gridPane.add(new Label("Min"), 0, 0); 
		gridPane.add(textFieldMin, 0, 1); 
		gridPane.add(new Label("Max"), 1, 0); 
		gridPane.add(textFieldMax, 1, 1); 

		return gridPane;
	}

	/**
	 * Get the minimum value from text box and set tdGraph axis to that value.
	 */
	private void setMinVal(){
		try{
			double minVal=Double.valueOf(textFieldMin.getText());
			//			System.out.println("Set Min Val: min " + minVal +  " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
			if (ready) {
				if (minVal>tdGraph.getCurrentScaleInfo().getMaxVal()) {
					tdGraph.getCurrentScaleInfo().setMaxVal(minVal+10);
					PamUtilsFX.nodeFlashEffect(textFieldMax, Color.RED, 20d, 0.3);
				}
				tdGraph.getCurrentScaleInfo().setMinVal(minVal);
				tdGraph.repaint(0);
			}
			//			System.out.println("DONE: Set Min Val: min " + minVal +  " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
		}
		catch (Exception e){
			PamUtilsFX.nodeFlashEffect(textFieldMin, Color.RED, 20d, 0.3);
			//			System.err.println("TDDataSelPane: Min text box: invlaid value."); 
		}
	}

	/**
	 * Get maximum value from text box and set tdGraph axis to that value.
	 */
	private void setMaxVal(){
		try{
			double maxVal=Double.valueOf(textFieldMax.getText());
			//			System.out.println("Set Max Val: max " +  " min: " + tdGraph.getCurrentScaleInfo().getMinVal() + " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
			if (ready) {
				if (maxVal<tdGraph.getCurrentScaleInfo().getMinVal()) {
					tdGraph.getCurrentScaleInfo().setMinVal(maxVal-10);
					PamUtilsFX.nodeFlashEffect(textFieldMin, Color.RED, 20d, 0.3);
				}
				tdGraph.getCurrentScaleInfo().setMaxVal(maxVal);
				//				System.out.println("BeforeRepaint: Set Max Val: max " +  " min: " + tdGraph.getCurrentScaleInfo().getMinVal() + " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
				tdGraph.repaint(0);
				//				System.out.println("AfterRepaint: Set Max Val: max " +  " min: " + tdGraph.getCurrentScaleInfo().getMinVal() + " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
			}
			//			System.out.println("DONE: Set Max Val: max " +  " min: " + tdGraph.getCurrentScaleInfo().getMinVal() + " max: " + tdGraph.getCurrentScaleInfo().getMaxVal());
		}
		catch (Exception e){
			//			e.printStackTrace();
			PamUtilsFX.nodeFlashEffect(textFieldMax, Color.RED, 20d, 0.3);
			System.err.println("TDDataSelPane: Max text box: invlaid value."); 
			//			System.err.println("TDDataSelPane: Max text box: invlaid value."); 
		}
	}

	/**
	 * Set binding so that the text field min and max change with the current TDScaleInformation; 
	 */
	private void setTextFieldBinding(){
		textFieldMin.textProperty().unbind();
		textFieldMax.textProperty().unbind();
		if (tdGraph.getCurrentScaleInfo()!=null){
			//			Debug.out.println("TDDataSelPane: Bound to: " + tdGraph.getCurrentScaleInfo().getAxisName() + 
			//					" min: " + tdGraph.getCurrentScaleInfo().getMinVal() + " max: " + tdGraph.getCurrentScaleInfo().getMaxVal()); 

			textFieldMin.setText(String.format("%."+5+"G", tdGraph.getCurrentScaleInfo().getMinVal()));
			textFieldMin.setText(String.format("%."+5+"G", tdGraph.getCurrentScaleInfo().getMaxVal()));

			textFieldMin.textProperty().bind(tdGraph.getCurrentScaleInfo().minValProperty().asString("%."+5+"G"));
			textFieldMax.textProperty().bind(tdGraph.getCurrentScaleInfo().maxValProperty().asString("%."+5+"G"));
		}
	}

	/**
	 * Create plot list. 
	 * @return the plot 
	 */
	private void populatePlotListPane(){

		TDScaleInfo scaleInfo=tdGraph.getCurrentScaleInfo(); 

		plotListHolder.getChildren().clear();
		plotList.getChildren().clear();
		//clear all check boxes
		for (int i=0; i<channelCheckBox.length; i++){
			channelCheckBox[i]=null; 
		}

		plotList.setHgap(5);
		plotList.setVgap(5);

		if (scaleInfo == null) {
			// happens when all data removed from a plot. 
			return;
		}
		if (scaleInfo.getNPlots()<=1){
			plotListHolder.setVisible(false);
			return; 
		}
		else this.plotListHolder.setVisible(true);

		int[] panelChan=scaleInfo.getPlotChannels();
		boolean[] vis=scaleInfo.getVisibleChannels(); 


		int[] chan; 
		String chanS; 
		for (int i=0; i<panelChan.length; i++){
			chan=PamUtils.getChannelArray(panelChan[i]); 
			if (chan!=null && chan.length > 0){
				chanS=String.valueOf(chan[0]); 
				for (int j=1; j<chan.length; j++){
					chanS=", "+String.valueOf(chan[j]); 
				}; 
				plotList.getChildren().add(channelCheckBox[i]=new CheckBox(chanS)); 
				channelCheckBox[i].setSelected(vis[i]);

				channelCheckBox[i].setOnAction((action)->{
					plotChannelsSelected(); 
				});
			}
		}

		plotListHolder.setSpacing(5);
		plotListHolder.setPadding(new Insets(10,10,10,10));
		plotListHolder.getChildren().add(new Separator());
		Label channelTxt=new Label("Plot Channels");
		//		channelTxt.setFont(PamGuiManagerFX.titleFontSize2);
		plotListHolder.getChildren().add(channelTxt);
		plotListHolder.getChildren().add(plotList);

	}


	/**
	 * Called whenever different plot channels are selected. 
	 */
	private void plotChannelsSelected() {
		for (int i=0; i<channelCheckBox.length; i++){
			if (channelCheckBox[i]==null) continue;

			tdGraph.getCurrentScaleInfo().getVisibleChannels()[i]=channelCheckBox[i].isSelected(); 

			this.tdGraph.checkAxis();
			this.tdGraph.layoutTDGraph(this.tdGraph.getOrientation());
			//this.tdGraph.repaint(0);
		}

	}

	private boolean ready = false;


	/**
	 * Create the set of radio buttons to allow users to
	 * determine what the y axis of the tdGraph shows
	 */
	private Pane createYAxisDataList(){


		ArrayList<DataTypeInfo> axisNames=tdGraph.getAxisNames(); 
		PamVBox vBox=new PamVBox();
		vBox.setPadding(new Insets(10,10,10,10));
		vBox.setSpacing(10);

		//create a button group and add listeners for buttons changing. 
		final ToggleGroup axisNameGroup = new ToggleGroup();
		axisNameGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle old_toggle, Toggle new_toggle) {
				if (axisNameGroup.getSelectedToggle() != null) {
					//set the axis on the tdGraph.
					if (ready) {
						tdGraph.setAxisName((DataTypeInfo) axisNameGroup.getSelectedToggle().getUserData());
					}
					//bin axis to values in text box. 
					setTextFieldBinding();
					populatePlotListPane(); 
					//added here to ensure plot channel sort themselves out. Otherwise 
					//when switching between scale infos with multiple plot pains, repaint 
					//isssues occur. 
					plotChannelsSelected(); 
				}                
			}
		});

		//create radio buttons. 
		TDRadioButton axisRB;
		axisRadioButtons.clear(); 
		for (int i=0; i<axisNames.size(); i++){
			axisRB=new TDRadioButton(TDScaleInfo.getAxisName(axisNames.get(i)) + " (" + TDScaleInfo.getUnitName(axisNames.get(i))+")", axisNames.get(i)); 
			axisRadioButtons.add(axisRB);
			axisRB.setToggleGroup(axisNameGroup);
			//set the user data as the 
			axisRB.setUserData(axisNames.get(i));
			vBox.getChildren().add(axisRB);
		}

		//setTextFieldBinding();
		selectAxisType();
		ready = true;

		return vBox; 
	}

	/**
	 * Shows a list of data blocks on the graph. Allows users to select a check box for each list. 
	 * @return a panel which shows a list of selectable data blocks currently subscribed to the TDGraphFX. 
	 */
	private Pane createDataInfoList(){

		PamVBox vBox=new PamVBox();
		vBox.setPadding(new Insets(10,10,10,10));
		vBox.setSpacing(10);

		PamToggleSwitch dataCB; 
		for (int i=0; i<tdGraph.getDataList().size(); i++){
			dataCB = new PamToggleSwitch(tdGraph.getDataList().get(i).getDataName());
			dataCB.setSelected(tdGraph.getDataList().get(i).isShowing());
			final int list=i; //need a final variable for listener. 
			dataCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
						Boolean old_val, Boolean new_val) {
					tdGraph.getDataList().get(list).setShowing(new_val);
					tdGraph.repaint(0);
				}
			});
			dataCB.setTooltip(new Tooltip(tdGraph.getDataList().get(i).getDataName())); 
			vBox.getChildren().add(dataCB);
		}
		return vBox;
	} 

	/**
	 * Select the correct axis to show on the graph based on what's currently selected in the TDGraphFX. 
	 */
	public void selectAxisType() {		
		DataTypeInfo currAx = tdGraph.getGraphParameters().currentDataType;
		boolean foundAxis=false; 
		for (TDRadioButton aCB:axisRadioButtons) {
			if (aCB.getCurrAx().equals(currAx)){
				aCB.setSelected(true);
				foundAxis=true; 
				return; 
			}
		}	

		if (!foundAxis && axisRadioButtons.size()>0){
			axisRadioButtons.get(0).setSelected(true);
		}
	}


	/**
	 * Show pop up menu.
	 * @param button
	 */
	private void showPopMenu(PamButton button) {
		popOver=new PopOver(); 
		popOver.setFadeInDuration(new Duration(100));
		popOver.setFadeOutDuration(new Duration(100));
		popOver.setContentNode(tdGraph.getTDGraphSettingsPane().getContentNode()); 
		popOver.setArrowLocation(ArrowLocation.LEFT_TOP);
		popOver.setCornerRadius(5);

		//show the graph settings pane

		tdGraph.getTDGraphSettingsPane().setParams(tdGraph.getGraphParameters());

		Point2D loc = this.localToScreen(new Point2D(button.getLayoutX(), button.getLayoutY())); 

		//I have no idea why this works but it works- FIXME
		//In FX mode need to make the scene the owner- in Swing there is no scene. 
		if (PamGUIManager.isFX()) {
			popOver.show(button);
			//popOver.show(tdGraph.getScene().getWindow(), loc.getX()-button.getLayoutX()+button.getWidth()/2, loc.getY()+button.getHeight()/2);
		}
		else {
			//			popOver.show(tdGraph.getTDDisplay(), loc.getX()-button.getLayoutX()+button.getWidth()/2, loc.getY()+button.getHeight()/2);
			popOver.show(tdGraph.getTDDisplay(), loc.getX()+button.getWidth(), loc.getY()+button.getHeight());
		}

		//makes the arrow black
		((Parent) popOver.getSkin().getNode()).getStylesheets()
		.addAll(tdGraph.getTDDisplay().getCSSSettingsResource());

		// show the graph add and remove menu
		//		this.showingProperty().addListener(new ShowTDGraphMenu(this));
	}

	/**
	 * Quick class to allow radio buttons to store axis type. 
	 * @author Jamie Macaulay
	 *
	 */
	class TDRadioButton extends RadioButton {

		DataTypeInfo currAx=null;

		public TDRadioButton() {
			super();
		}

		public TDRadioButton(String arg0) {
			super(arg0);
		}

		public TDRadioButton(String arg0, DataTypeInfo currAx ) {
			super(arg0);
			this.currAx=currAx;
		}

		public DataTypeInfo getCurrAx() {
			return currAx;
		}

		public void setCurrAx(DataTypeInfo currAx) {
			this.currAx = currAx;
		} 

	}


}
