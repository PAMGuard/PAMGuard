package dataPlotsFX.overlaymark.popUpMenu;

import java.util.ArrayList;
import org.controlsfx.control.ToggleSwitch;

import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuItem;
import dataPlotsFX.overlaymark.menuOptions.OverlayMenuManager;
import dataPlotsFX.overlaymark.menuOptions.wavExport.WavFileExportManager;
import detectiongrouplocaliser.DetectionGroupSummary;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTilePane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.flipPane.FlipPane;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * The pane for the adv. pop up menu. This shows both traditional menu functions and has a
 * detection display showing previews of detections e.g. clicks. 
 * 
 * @author Jamie Macaulay 
 */
public class TDMenuPane extends PamBorderPane {

	public static final int menuWidth=200; 

	/**
	 * The current data units. 
	 */
	public DetectionGroupSummary detectionSummary; 


	/**
	 * Reference to the TDgraph the pop up display belongs to. 
	 */
	private TDGraphFX tdGraphFX;


	/**
	 * The pane which holds the detection
	 */
	private Pane detectionPane; 

	/**
	 * Holds the menu items
	 */
	private Pane menuPane;

	/**
	 * The overlay menu manager. Handles menu items in the pop up display 
	 */
	private OverlayMenuManager overlayMenuManager;

	/**
	 * 
	 */
	private PamVBox dynamicMenuPane;

	/**
	 * The current overlay marker. 
	 */
	private OverlayMarker overlayMarker;

	/**
	 * The current mouse event. 
	 */
	private MouseEvent currentMouseEvent;

	/*
	 * Shows info of the data units
	 */
	private PamBorderPane infoPane;

	private TextArea infoTextLabel;

	/**
	 * Reference to the tdPlotPanel
	 */
	private TDPlotPane tdPlotPanel;

	/**
	 * Need to save the minimum width so that pop over data unit dimensions are restored 
	 */
	private double lastMinWidth;

	/**
	 * Need to save the minimum height so that pop over data unit dimensions are restored 
	 */
	private double lastMinHeight;

	/**
	 * Toggle which switches between the detection menu only and a menu with a preview of the detection(s). 
	 */
	private ToggleSwitch toggle;

	/**
	 * Display which plots and allows users to iterate through detections and super 
	 * detections. 
	 */
	private OverlayGroupDisplay groupDetectionDisplay;

	/**
	 * Main holder for the menu pane. 
	 */
	private PamBorderPane holder;

	private BorderPane topHolder;

	/**
	 * The currently selected data unit. Can be null if there is not selected data unit. 
	 */
	private PamDataUnit currentDataUnit; 

	/**
	 * Constructor for pop up menu. 
	 */
	public TDMenuPane(TDPlotPane tdPlotPanel){

		this.tdPlotPanel=tdPlotPanel; 
		this.tdGraphFX=tdPlotPanel.getTDGraph();
		this.holder=new PamBorderPane();
		this.setCenter(holder);

		groupDetectionDisplay = new OverlayGroupDisplay(); 
		groupDetectionDisplay.addDisplayListener((oldDataUnit, newDataUnit)->{
			//listener for changing data units 
			setCurrentDataUnit(newDataUnit);
			if (detectionSummary!=null && detectionSummary.getDataList()!=null) {
				detectionSummary.setFocusedIndex(this.detectionSummary.getDataList().indexOf(newDataUnit));
				tdGraphFX.repaintMarks();
			}
		}); 

		//handles men
		overlayMenuManager = new OverlayMenuManager(tdPlotPanel);

		topHolder = new PamBorderPane(); 
		menuPane= createMenuPane();

		layoutPane(true);
	}



	/**
	 * Layout the pane. Either just show the menu or show the menu with a detection
	 * @param showDetection - true to show a preview of the currently selected detection detections. 
	 */
	public void layoutPane(boolean showDetection){

		holder.setLeft(null);
		holder.setRight(null);
		holder.setCenter(null);

		if (!showDetection ){
			//simply show the men
			this.lastMinWidth=this.getMinWidth();
			this.lastMinHeight=this.getMinHeight();
			this.setMinSize(-1, -1);
			//System.out.println("Hide the pop over: " +  lastMinWidth + "  " + lastMinHeight);

			holder.setLeft(this.menuPane);
			menuPane.setVisible(true);
			//restore min size. 
		}
		else{
			groupDetectionDisplay.layoutPane();

			HidingPane hidingPaneLeft=new HidingPane(Side.LEFT, menuPane, this, false);
			hidingPaneLeft.showHidePane(true, false);

			PamButton showButtonLeft=hidingPaneLeft.getShowButton();
//			showButtonLeft.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.BARS, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			showButtonLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2m-menu", PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			showButtonLeft.setStyle(" -fx-background-radius: 0 0 0 0;");

			PamButton closeButtonLeft=hidingPaneLeft.getHideButton();
//			closeButtonLeft.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			closeButtonLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
			closeButtonLeft.setStyle(" -fx-background-radius: 0 0 0 0;");

			hidingPaneLeft.getTimeLineShow().setOnFinished((value)->{
				topHolder.setLeft(closeButtonLeft);
				menuPane.setVisible(true);
			});

			hidingPaneLeft.getTimeLineHide().setOnFinished((value)->{
				topHolder.setLeft(showButtonLeft);
				menuPane.setVisible(false);
			});

			closeButtonLeft.setVisible(true);
			topHolder.setLeft(closeButtonLeft);

			holder.setLeft(hidingPaneLeft);

			holder.setCenter(groupDetectionDisplay);
			holder.setLeft(hidingPaneLeft);

			//System.out.println("Show the pop over: " +  lastMinWidth + "  " + lastMinHeight);
			this.setMinSize(lastMinWidth, lastMinHeight);
		}
	}


	/**
	 * Set the summary text for the data unit in the menu. 
	 */
	private void setSummaryText() {
		
		//System.out.println(""); 
		if (currentDataUnit==null || detectionSummary==null) return; 

		String text; 
		if (detectionSummary.getDataList().size()>=1){
//			text= currentDataUnit.getSummaryString();
			
			text=currentDataUnit.getParentDataBlock().
					getHoverText(tdGraphFX.getGraphProjector(), currentDataUnit, 0);
		}
		else {
			//selected an area with no data units. 
			text= new String("No data units selected"); 
		}
		this.infoTextLabel.setText(PamUtilsFX.htmlToNormal(text));
	}


	public TextArea getInfoTextLabel() {
		return infoTextLabel;
	}



	/**
	 * Create the menu pane. 
	 * @return the menu pane. 
	 * 
	 */
	private Pane createMenuPane(){

		PamVBox menuPane = new PamVBox(); 
		menuPane.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
		menuPane.setStyle("-fx-background-color: -fx-darkbackground");

		//create info and toggle detection buttons that always sit at the top of the display

		PamButton infoButton = new PamButton();
		infoButton.getStyleClass().add("square-button-trans");
		infoButton.setTooltip(new Tooltip("Show detection info"));
//		infoButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.INFO, PamGuiManagerFX.iconSize));
		infoButton.setGraphic(PamGlyphDude.createPamIcon("mdi2i-information", PamGuiManagerFX.iconSize));
		//infoButton.setStyle(" -fx-background-radius: 0 0 0 0;");

		toggle= new ToggleSwitch(); 
		toggle.setSelected(true);
		toggle.setAlignment(Pos.CENTER);
		toggle.setTooltip(new Tooltip("Show a preview of the detection"));
		toggle.setMaxWidth(20);
		toggle.selectedProperty().addListener((obsVal, oldVal, newVal)->{
			this.layoutPane(newVal);
			this.getParent().layout(); //make sure the pop up actually lays out properly. 
		});

		PamBorderPane toggleHolder = new PamBorderPane(toggle);
		toggleHolder.getStyleClass().add("square-button-trans");

		toggleHolder.setPadding(new Insets(0,10,0,0));
		toggleHolder.prefWidthProperty().bind(menuPane.widthProperty());

		PamHBox topMenu = new PamHBox();
		topMenu.getChildren().addAll(infoButton, toggleHolder);
		topMenu.setAlignment(Pos.CENTER);

		menuPane.getChildren().add(topMenu);

		dynamicMenuPane = new PamVBox(); 
		menuPane.getChildren().add(dynamicMenuPane);


		//create the flip pane. 
		FlipPane flipPane=new FlipPane(); 
		flipPane.setFlipDirection(Orientation.HORIZONTAL);
		flipPane.setFlipTime(250); //default is 700ms- way too hig

		//create info pane
		PamButton reverseInfo = new PamButton();
		reverseInfo.getStyleClass().add("square-button-trans");
		reverseInfo.setTooltip(new Tooltip("Show menu"));
//		reverseInfo.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.MENU, PamGuiManagerFX.iconSize));
		reverseInfo.setGraphic(PamGlyphDude.createPamIcon("mdi2m-menu", PamGuiManagerFX.iconSize));
		infoPane= new PamBorderPane();
		infoPane.setTop(reverseInfo);
		infoPane.setCenter(infoTextLabel=new TextArea());
		infoTextLabel.setEditable(false);
		infoTextLabel.setWrapText(true);
		BorderPane.setAlignment(infoTextLabel, Pos.TOP_CENTER);

		infoButton.setOnAction((action)->{
			flipPane.flipToBack();
		});

		reverseInfo.setOnAction((action)->{
			flipPane.flipToFront();
		});

		flipPane.getFront().getChildren().add(menuPane); 
		flipPane.getBack().getChildren().add(infoPane); 
		flipPane.setPrefWidth(menuWidth);

		PamBorderPane holder = new PamBorderPane(flipPane); 
		holder.setPrefWidth(menuWidth);


		return holder;
	}

	/**
	 * Get the toggle switch which shows the detection preview. 
	 * @return the toggle switch. 
	 * 
	 */
	public ToggleSwitch getToggle() {
		return toggle;
	}

	/**
	 * Set up the items to go in the main menu
	 * @param detectionGroup- the detection group i.e. selected data units. 
	 * @param unitIndex- the currently selected data unit
	 * @param e - the mouse event associated with the selected
	 */
	public void setMenuItems(DetectionGroupSummary detectionGroup, int unitIndex, MouseEvent e){

		dynamicMenuPane.getChildren().clear();

		if (overlayMarker==null) {
			System.out.println("Adv pop up menu: overlayMarker is null: " + overlayMarker);
			//dynamicMenuPane.getChildren().add(new BorderPane(new Label("Nothing selected")));
			//return; 
		}

		//add TD specific overlays e.g. playback controls. 
		ArrayList<OverlayMenuItem> menuNodes = overlayMenuManager.getMenuNodes(detectionGroup, 
				overlayMarker==null ? null:overlayMarker.getCurrentMark(), e);
		PamVBox menuPane= new PamVBox();
		Control menuButton;

		//menu pane with options form other modules e.g. click event marking
		PamVBox externalMenuPane= new PamVBox();

		for (int i=0; i<menuNodes.size(); i++){
			if (menuNodes.get(i).getFlag()==OverlayMenuItem.NO_GROUP){
				//				int row=(int) Math.floor(i/3); 
				//				int column=n%nColumns;
				final OverlayMenuItem overlayItme=menuNodes.get(i); 
				menuButton=overlayItme.menuAction(detectionGroup, detectionGroup==null ? 0 : detectionGroup.getDataList().indexOf(currentDataUnit), 
						overlayMarker==null ? null:overlayMarker.getCurrentMark()); 
				//just add the node
				externalMenuPane.getChildren().add(menuButton);
				styleButton(menuButton, OverlayMenuItem.buttonWidthStandard);
			}
		}
		
		ScrollPane scrollPane = new ScrollPane(externalMenuPane); 
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);		
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);		
		scrollPane.setPrefHeight(200);

		menuPane.getChildren().add(scrollPane);


		//add data info specific stuff. 
		menuPane.getChildren().add(createDataInfoSpecificGroup(detectionGroup, menuNodes));

		//Export menu. no export if nothing selected 
		if (detectionGroup!=null || overlayMarker!=null) {
			//add separator for clarity
			menuPane.getChildren().add(new Separator(Orientation.HORIZONTAL));
			//add export menu items 
			Label exportLabel=new Label("Export to"); 
			exportLabel.setPadding(new Insets(0,10,0,0)); 
			exportLabel.setTextAlignment(TextAlignment.CENTER);
			exportLabel.setAlignment(Pos.CENTER);
			//exportLabel.setStyle("-fx-border-color: blue;");
			exportLabel.prefWidthProperty().bind(menuPane.widthProperty());
			//exportLabel.setAlignment(Pos.CENTER_LEFT);

			menuPane.getChildren().add(exportLabel);

			menuPane.getChildren().add(createExportMenuGroup(detectionGroup, menuNodes));
		}

		dynamicMenuPane.getChildren().add(menuPane);

	}


	private Pane createDataInfoSpecificGroup(DetectionGroupSummary detectionSummary, ArrayList<OverlayMenuItem> menuNodes){

		PamVBox vBox= new PamVBox(); 
		//get the export group
		Control menuButton;
		for (int i=0; i<menuNodes.size(); i++){
			if (menuNodes.get(i).getFlag()==OverlayMenuItem.DATAINFO){
				//				int row=(int) Math.floor(i/3); 
				//				int column=n%nColumns; 
				final OverlayMenuItem overlayItem=menuNodes.get(i); 
				//				System.out.println("Overlay Items: " +overlayItme.getNodeToolTip().getText());
				menuButton=overlayItem.menuAction(detectionSummary, detectionSummary.getDataList().indexOf(currentDataUnit), overlayMarker.getCurrentMark() );
				if (menuButton==null) continue; 
				styleButton(menuButton, OverlayMenuItem.buttonWidthStandard);
				vBox.getChildren().add(menuButton);
				//				n++; 
			}
		}
		return vBox; 
	}

	/**
	 * Create the export overlay group. These are arranged slightly differently in tiles.
	 * @param  detectionSummary - the selected data units. 
	 * @param menuNodes- a list of overlay menu items. Some of these will be the export actions. 
	 */
	private Pane createExportMenuGroup(DetectionGroupSummary detectionSummary, ArrayList<OverlayMenuItem> menuNodes){

		PamTilePane tilePane= new PamTilePane(); 
		tilePane.setPrefColumns(3);
		//get the export group#
		Control menuButton;
		for (int i=0; i<menuNodes.size(); i++){
			if (menuNodes.get(i).getFlag()==OverlayMenuItem.EXPORT_GROUP){
				//				int row=(int) Math.floor(i/3); 
				//				int column=n%nColumns; 
				final OverlayMenuItem overlayItme=menuNodes.get(i); 
				//				System.out.println("Overlay Items: " +overlayItme.getNodeToolTip().getText());
				menuButton=overlayItme.menuAction(detectionSummary, detectionSummary.getDataList().indexOf(currentDataUnit), overlayMarker.getCurrentMark() );
				if (menuButton==null) continue; 
				tilePane.getChildren().add(menuButton);
				styleButton(menuButton, 30);
				//				n++; 
			}
		}
		return tilePane; 
	}


	/**
	 * Style a button.
	 * @param control - the button.
	 * @param width - the width of button. 
	 */
	public static void styleButton(Control control, double width){
		if (control instanceof Labeled) {
			((Labeled) control).setAlignment(Pos.CENTER_LEFT);
		}
		control.getStyleClass().add("square-button-trans");
		control.setPrefHeight(30);
		control.prefWidthProperty().set(width);
	}

	/**
	 * Set the current data unit. 
	 * @param pamDataUnit - the current data unit. 
	 */
	private void setCurrentDataUnit(PamDataUnit pamDataUnit) {
		currentDataUnit = pamDataUnit; 
		setSummaryText();
	}


	/**
	 * Called whenever the pop up shows with a new set of data units. 
	 */
	public void prepareDisplay(){

		if (detectionSummary==null || detectionSummary.getDataList()==null) {
			currentDataUnit=null; 
		}
		else{ //show the first data unit;
			setCurrentDataUnit(currentDataUnit); 
			this.detectionSummary.setFocusedIndex(0); //set focused to highlight data unit. 
		}

		tdGraphFX.repaintMarks();

		//menu items depend on the data units selected. 
		setMenuItems(detectionSummary, -1, currentMouseEvent);

	}

	/**
	 * Set the data unit list. 
	 */
	public void setDataUnitList(DetectionGroupSummary detectionGroupSummary, OverlayMarker overlayMarker, MouseEvent e){		

		this.detectionSummary=detectionGroupSummary;
		this.overlayMarker=overlayMarker; 
		this.currentMouseEvent = e; 

		this.groupDetectionDisplay.clearDisplay();
		this.infoTextLabel.setText("");


		this.groupDetectionDisplay.setDetectionGroup(detectionSummary!=null ? detectionSummary.getDataList() : null); 


		//show the detection display. 
		boolean showDetDisplay = toggle.isSelected(); 
		this.toggle.setDisable(false);
		
		//some data has been selected but there are no contained data units - perhaps a mark on the spectrogram?
		if (overlayMarker!=null && (detectionSummary==null || detectionSummary.getNumDataUnits()<=0)) {
			//need to find the correct data source. This could be a little tricky...what if there are decimators etc. 
			//Must try to figure out what the TDGraph is mainly looking at...
			//System.out.println("Overlay Marker Limits: "  +PamArrayUtils.array2String(overlayMarker.getCurrentMark().getLimits(), 2)); 

			PamRawDataBlock pamRawBlock =  findRawSourceBlock(); 
			//			System.out.println("Pam raw data block: " +  pamRawBlock); 
			
			
			if (WavFileExportManager.haveRawData(pamRawBlock, (long) overlayMarker.getCurrentMark().getLimits()[0], (long) overlayMarker.getCurrentMark().getLimits()[1])) {
	
				//System.out.println("Overaly Marker start X: " +  overlayMarker.getCurrentMark().getLimits()[0] + "  end: " +  overlayMarker.getCurrentMark().getLimits()[1]);
				//System.out.println("Overlay Marker start Y: " +  overlayMarker.getCurrentMark().getLimits()[2] + "  end: " +  overlayMarker.getCurrentMark().getLimits()[3]);

				groupDetectionDisplay.showRawData(pamRawBlock, overlayMarker.getCurrentMark().getLimits(), overlayMarker.getCurrentMark().getMarkChannels());
			}
			else {
				//do not show extra display if there is no sound 
				this.toggle.setDisable(true);
				showDetDisplay=false; 
				Debug.out.println("TDMenuPane: Detection Group NO raw wav data");
			}
		}
		else if (this.groupDetectionDisplay.getDetectionPlotCount()<1 && !groupDetectionDisplay.hasSuperDetectionDisplay()) {
			//Debug.out.println("TDMenuPane: Detection Group Summary is NULL");
			//if there is raw data then we can show that!
			this.toggle.setDisable(true);
			showDetDisplay=false; 
		}
		else {
			//nothing to do here as everything is fine. 
		}

		prepareDisplay(); 
		layoutPane(showDetDisplay); 
	}


	/**
	 * Get the first raw data block in the list. 
	 * @return the first raw data block. Null if there is no raw data 
	 */
	private PamRawDataBlock findRawSourceBlock() {
		PamRawDataBlock rawDataBlock; 
		for (int i=0; i<tdGraphFX.getDataList().size(); i++) {
			rawDataBlock = tdGraphFX.getDataList().get(i).getDataBlock().getFirstRawSourceDataBlock(); 
			if (rawDataBlock!=null) return rawDataBlock; 
		}
		return null; 
	}

	/**
	 * Force the group detection display to draw a data unit. 
	 */
	public void drawDataUnit() {
		this.groupDetectionDisplay.drawDataUnit();
	}





}
