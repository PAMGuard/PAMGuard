package dataPlotsFX.layout;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.ToggleSwitch;

import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import dataPlotsFX.TDControl;
import dataPlotsFX.TDParametersFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.sound.SoundOutputManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Top pane. This controls rotation of the display, adding and removing graphs and other settings. 
 * @author Jamie Macaulay
 *
 */
public class TDControlPaneFX extends Pane  {

	/**
	 * Reference to the TDDisplay class. 
	 */
	private TDDisplayFX tdDisplay;

	/**
	 * Reference to TDControl.
	 */
	private TDControl tdControl;

	/**
	 * Contains the graph settings buttons for all added graphs to the display. 
	 */
	private PamHBox graphControlPane;

	/**
	 * The control pane- contains add graph, remove graph, rotate and wrap buttons
	 */
	private PamHBox controlPane;

	/**
	 * Button to rotate the graph. 
	 */
	private PamButton rotate;

	/**
	 * Check box to wrap the display 
	 */
	private ToggleButton wrapDisplay;

	private PamGridPane zoomPane;

	private PamButton settings;

	private ToggleButton[] markerTypeButtons;

	/**
	 * Button to zoom into graph
	 */
	private PamButton zoomInto;
	
	/**
	 * Button to zoom out of graph
	 */
	private PamButton zoomOut;

	/**
	 * Sleected when the display is paused
	 */
	private ToggleSwitch pause;

	private Pane pausePane;

	private static int defaultVSpace=6; 

	private static int defaultHSpace=6; 


	public TDControlPaneFX(TDControl tdControl2, TDDisplayFX tdMainDsiplay) {
		this.tdControl=tdControl2;
		this.tdDisplay=tdMainDsiplay;

		//CSS styling- pane has the standard PAMGUARD settings pane styling. 		  
		this.getStylesheets().add(tdMainDsiplay.getCSSSettingsResource());
		this.getStyleClass().add("pane");

		PamBorderPane mainPane=new PamBorderPane();


		controlPane = new PamHBox(); 
		controlPane.setSpacing(5);
		//graph add pane is first
		controlPane.getChildren().addAll(createGraphAddPane(), new Separator(Orientation.VERTICAL)); 
		
		//playback controls
		if (tdControl2.isViewer()){
			//sound playback currently only works in viewer mode. 
			controlPane.getChildren().addAll(createPlaybackPane(), new Separator(Orientation.VERTICAL));
		}
		
		//controls for manipulating the graph. 
		controlPane.getChildren().addAll(createControlsPane(), new Separator(Orientation.VERTICAL));	
		
		controlPane.setSpacing(5);

		graphControlPane=new PamHBox(); 
		graphControlPane.setPadding(new Insets(15, 12, 15, 12));
		graphControlPane.setSpacing(20);

//		//create graph buttons;
//		createGraphButtons();

		//add all to the main pane. 
		mainPane.setLeft(controlPane);
		mainPane.setCenter(graphControlPane);
		if (!tdDisplay.isViewer()){
			//pane which pauses the real time display 
			pausePane=createPausePane(); 
			mainPane.setRight(pausePane);
		}
		
		//make sure the pane stretches to fill the control pane  
		mainPane.prefWidthProperty().bind(this.widthProperty());

		this.getChildren().add(mainPane);
	}


	private Pane createPausePane() {
	
		
		pause = new ToggleSwitch(); 
//		pause.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.PAUSE, Color.WHITE, PamGuiManagerFX.iconSize));
		pause.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pause", Color.WHITE, PamGuiManagerFX.iconSize));
//		pause.selectedProperty().addListener((obsVal, oldVal, newVal)->{
//			this.setPaused(newVal); 
//		});
//		//HACK. For some reason toggle switchsdon;t do alignemnt <- not needed in Java 12+
//		Insets insets= new Insets(TDDisplayFX.controlPaneHeight/2-10,0,0,0); 
//		pause.setPadding(insets);
		
		PamHBox pauseHolder = new PamHBox(); 
		pauseHolder.setSpacing(5);
		pauseHolder.setAlignment(Pos.CENTER_RIGHT);
		pauseHolder.getChildren().add(new Separator(Orientation.VERTICAL)); 
		pauseHolder.getChildren().addAll(new Label("Pause"), pause);
		
		return pauseHolder;
	}


	/**
	 * Create pane which adds and remove graphs 
	 * @return pane containing adda dn remove graph controls. 
	 */
	private Node createGraphAddPane(){

		//create button to add a graph
		Button addGraph=new PamButton("Plot");
//		addGraph.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, Color.WHITE, PamGuiManagerFX.iconSize));
		addGraph.setGraphic(PamGlyphDude.createPamIcon("mdi2p-plus", Color.WHITE, PamGuiManagerFX.iconSize));
		addGraph.setOnAction(new AddButton());

		//create a menu button to remove graphs
		SplitMenuButton removeGraph=new SplitMenuButton();
		removeGraph.setText("Plot");
//		removeGraph.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.REMOVE, Color.WHITE, PamGuiManagerFX.iconSize));
		removeGraph.setGraphic(PamGlyphDude.createPamIcon("mdi2m-minus", Color.WHITE, PamGuiManagerFX.iconSize));
		removeGraph.setOnAction(new RemoveLastGraph());
		removeGraph.showingProperty().addListener(new RemoveGraph(removeGraph) );

		//create pane to hold control buttons. 
		PamGridPane controlPane = new PamGridPane();
		controlPane.setPadding(new Insets(5, 5, 5, 5));
		controlPane.setVgap(defaultVSpace);
		controlPane.setHgap(defaultHSpace);

		Label controlLabel = new Label("Add Displays");
		GridPane.setColumnSpan(controlLabel, GridPane.REMAINING);
		GridPane.setHalignment(controlLabel, HPos.CENTER);
		
		//graph controls
		controlPane.add(controlLabel, 0, 0, 2, 1);
		controlPane.add(addGraph, 0, 1);
		controlPane.add(removeGraph, 1, 1);

		return controlPane; 

	}

	/**
	 * Create zoom and pan controls. 
	 * 
	 * @return pane contains pane and zoom controls. 
	 */
	private Node createControlsPane(){

		//create pane to hold control buttons. 
		PamGridPane controlPane = new PamGridPane();
		controlPane.setPadding(new Insets(5, 5, 5, 5));
		controlPane.setVgap(defaultVSpace);
		controlPane.setHgap(defaultHSpace);

		//-----------pan and zoom controls 
		final ToggleGroup group = new ToggleGroup();


		//need to create a temporary graph to get overlay markers....bit of a hack. 
		TDGraphFX tempGraph= new TDGraphFX(tdControl, tdDisplay, -1); 

		//Marker Buttons
		markerTypeButtons=new ToggleButton[tdDisplay.getOverlayMarkerAdpaters(tempGraph).size()]; 
		for (int i=0; i<tdDisplay.getOverlayMarkerAdpaters(tempGraph).size(); i++){

			markerTypeButtons[i]=new ToggleButton();
			markerTypeButtons[i].setGraphic(tdDisplay.getOverlayMarkerAdpaters(tempGraph).get(i).getIcon()); 
			final int index=i; 
			markerTypeButtons[i].setOnAction((action)->{
				setGraphMarkerType(index);
			});
			markerTypeButtons[i].setToggleGroup(group);
			markerTypeButtons[i].setTooltip(tdDisplay.getOverlayMarkerAdpaters(tempGraph).get(i).getToolTip());
		}
	
		
		//zoom in and zoom out buttons
		zoomInto=new PamButton();
//		zoomInto.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.ARROW_COMPRESS_ALL, Color.WHITE, PamGuiManagerFX.iconSize));
		zoomInto.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-collapse-all", Color.WHITE, PamGuiManagerFX.iconSize));
		zoomInto.setOnAction((action)->{
			tdDisplay.zoomDisplay(true);
		}); 

		zoomOut=new PamButton();
//		zoomOut.setGraphic(PamGlyphDude.createPamGlyph(MaterialDesignIcon.ARROW_EXPAND_ALL, Color.WHITE, PamGuiManagerFX.iconSize));
		zoomOut.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-expand-all", Color.WHITE, PamGuiManagerFX.iconSize));
		zoomOut.setOnAction((action)->{
			tdDisplay.zoomDisplay(false);
		}); 
		//create button to rotate the display
		rotate=new PamButton();
//		rotate.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ROTATE_90_DEGREES_CCW, Color.WHITE, PamGuiManagerFX.iconSize));
		rotate.setGraphic(PamGlyphDude.createPamIcon("mdi2f-format-rotate-90", Color.WHITE, PamGuiManagerFX.iconSize));
		rotate.setOnAction(new RotateButton());
		
//		if (JamieDev.isEnabled()){
//			controlPane.add(rotate, 0, 0);	
//		}

		//create button to open the advanced settings stage
		wrapDisplay=new ToggleButton();
//		wrapDisplay.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.EXCHANGE, Color.WHITE, PamGuiManagerFX.iconSize));
		wrapDisplay.setGraphic(PamGlyphDude.createPamIcon("mdi2a-arrow-left-right-bold-outline", Color.WHITE, PamGuiManagerFX.iconSize));
		wrapDisplay.setOnAction((action)->{
			Platform.runLater(()->{
				tdDisplay.setWrapRequest(wrapDisplay.isSelected());
			});
		});
		wrapDisplay.setSelected(tdControl.getTdParameters().wrap);
		wrapDisplay.setTooltip(new Tooltip("Wrap the display if selected. Otherwise the display will scroll"));


		int n=0; 
		if (!this.tdControl.isViewer()) {
			controlPane.add(wrapDisplay, 0, 1);
			n=n+1;
		}

		for (int i=0; i<tdDisplay.getOverlayMarkerAdpaters(tempGraph).size(); i++){
			controlPane.add(markerTypeButtons[i], n, 1);
			n++; 
		}
		
		Label controlLabel = new Label("Pan and Zoom");
		controlLabel.setTextAlignment(TextAlignment.CENTER);
		controlLabel.setAlignment(Pos.CENTER);
		GridPane.setColumnSpan(controlLabel, n+1);
		GridPane.setHalignment(controlLabel, HPos.CENTER);
		GridPane.setHgrow(controlLabel, Priority.ALWAYS);

		//graph controls
		controlPane.add(controlLabel, 0, 0, n, 1);
		controlPane.add(zoomInto, n, 1);
		controlPane.add(zoomOut, n+1, 1);
	

		return controlPane; 

	}

	/**
	 * Set the marker type for all TDGrpahs 
	 * @param index - the marker type. 
	 */
	private void setGraphMarkerType(int index){
		tdDisplay.getTDParams().overlayMarkerType=index; 
		//set all the marker managers to the correct type. 
		for (TDGraphFX tdGraph: this.tdDisplay.getTDGraphs() ){
			tdGraph.getOverlayMarkerManager().setCurrentMarkIndex(index); 
		}
	}



	/**
	 * Create the playback controls. 
	 * @return the playback controls pane. 
	 */
	private Node createPlaybackPane(){

		
		SoundOutputManager so = tdDisplay.getSoundOutputManager();

		//create button to open settings

		//create pane to hold control buttons. 
		PamGridPane controlPane = new PamGridPane();
		controlPane.setPadding(new Insets(5, 5, 5, 5));
		controlPane.setVgap(defaultVSpace);
		controlPane.setHgap(defaultHSpace);
		

		Label controlLabel = new Label("Playback");
		GridPane.setColumnSpan(controlLabel, 2);
		GridPane.setHalignment(controlLabel, HPos.CENTER);
		//graph controls
		controlPane.add(controlLabel, 0, 0, 2, 1);
		//graph controls
		controlPane.add(so.getPlayButton(), 0, 1);
		controlPane.add(so.getPauseButton(), 1, 1);

		return controlPane; 

	}

	protected void playButtonPressed() {
		tdDisplay.getSoundOutputManager().playButtonPressed();
	}

	protected void pausePlayButtonPressed() {
		tdDisplay.getSoundOutputManager().playPause();
	}

	//	/**
	//	 * 
	//	 * @return
	//	 */
	//	private Pane createZoomPane(){
	//		//create button to add a graph
	//		Button addGraph=new Button();
	//		addGraph.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.PLUS, Color.WHITE, PamGuiManagerFX.iconSize));
	//		addGraph.setOnAction(new AddButton());
	//		
	//		
	//		zoomPane = new PamGridPane();
	//		controlPane.setPadding(new Insets(5, 5, 5, 5));
	//		controlPane.setVgap(4);
	//		controlPane.setHgap(6);
	//		
	//		
	//		controlPane.add(addGraph, 0, 0);
	//		controlPane.add(removeGraph, 0, 1);
	//		controlPane.add(rotate, 1, 0);
	//		controlPane.add(wrapDisplay, 1, 1);
	//	}





	/**
	 * Set params for the button control pane.
	 */
	public void setParams(TDParametersFX tdParams){
		//clearAllGraphButtons();
//		//refreshGraphButtonPane();
		this.wrapDisplay.setSelected(tdParams.wrap);
		if (tdParams.overlayMarkerType>=markerTypeButtons.length) tdParams.overlayMarkerType=0; 
		this.markerTypeButtons[tdParams.overlayMarkerType].setSelected(true);
		this.setRotateButtonOrientation();
	}




	
	/**
	 * Add menu items to a menu to add TDDataInfos
	 * @param addGraph - the menu to add menu items to. 
	 * @param tdGraphFX - the TDGraphFX. 
	 */
	public static void createAddMenuItems(List<MenuItem> menuItems, TDGraphFX tdGraphFX) {
		
		final ArrayList<TDDataProviderFX> dataPlotsInfo=TDDataProviderRegisterFX.getInstance().getDataInfos();
		
		// create the menu to add data blocks to the graph : 
		MenuItem dataPlotItem;

		for (int i=0; i<dataPlotsInfo.size(); i++){
			//create dataPlot item
			final int iDataPlotInfo=i; 
			dataPlotItem=new MenuItem(dataPlotsInfo.get(i).getName());
			dataPlotItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					tdGraphFX.addDataItem(dataPlotsInfo.get(iDataPlotInfo).createDataInfo(tdGraphFX));
				}
			});
			//add to menu. 
			menuItems.add(dataPlotItem);
		}
	}
	
	/**
	 * Add menu items to a menu to add TDDataInfos
	 * @param addGraph - the menu to add menu items to. 
	 * @param tdGraphFX - the TDGraphFX. 
	 */
	public static void createRemoveMenuItems(List<MenuItem> menuItems, TDGraphFX tdGraphFX) {
		MenuItem dataPlotItem;
		for (int i=0; i<tdGraphFX.getDataList().size(); i++){
			final int iRemove=i; 
			dataPlotItem=new MenuItem(tdGraphFX.getDataList().get(i).getDataName());
			dataPlotItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override public void handle(ActionEvent e) {
					tdGraphFX.removeDataItem(iRemove); 
				}
			});
			menuItems.add(dataPlotItem);
		}
	}

	/**
	 * Called to add a new graph
	 * @author Jamie Macaulay
	 */
	private class AddButton implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			tdDisplay.addTDGraph();
			//createGraphButtons();
			//addGraphButton(tdDisplay.getTDGraphs().get(tdDisplay.getTDGraphs().size()-1));
		}
	}

	/**
	 * Called to add a new graph
	 * @author Jamie Macaulay
	 */
	private class RotateButton implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			//rotate the graph. 
			tdDisplay.rotate();
			//rotate the button.
			setRotateButtonOrientation();
		}
	}

	/**
	 * Set the correct orientation for the rotate button. 
	 */
	private void setRotateButtonOrientation(){
		if (tdDisplay.getTDParams().orientation==Orientation.VERTICAL) rotate.rotateProperty().setValue(180);
		else rotate.rotateProperty().setValue(0);
	}

	/**
	 * Called to remove the last graph
	 * @author Jamie Macaulay
	 *
	 */
	private class RemoveLastGraph implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent arg0) {
			if (tdDisplay.getTDGraphs().size()-1<0) return;
			//remove the graph from the main display
			//remove the settings button from that graph, 
			int remove=tdDisplay.getTDGraphs().size()-1;
			tdDisplay.removeTDGraph(remove);
			//removeGraphButton(remove); //not needed any more. 
		}

	}

	/**
	 * Called whenever the menu portion is called. Create the menu allowing users
	 * to remove specific graphs from the display.
	 * @author Jamie Macaulay
	 */
	private class RemoveGraph implements ChangeListener<Boolean>{

		private SplitMenuButton splitButton;

		public RemoveGraph(SplitMenuButton splitButton){
			this.splitButton=splitButton;
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> arg0,
				Boolean arg1, Boolean arg2) {
			int nGraphs=tdDisplay.getTDGraphs().size();
			splitButton.getItems().removeAll(splitButton.getItems());
			MenuItem item;
			for (int i=0; i<nGraphs; i++){
				final int  remove=i;
				splitButton.getItems().add(item=new MenuItem(("Graph "+i)));
				item.setOnAction(new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent e) {
						//remove the graph from the main display
						tdDisplay.removeTDGraph(remove);
						//remove the settings for that graph. 
						//removeGraphButton(remove); //not needed any more. 
					}
				});
			}
		}
	}

	/**
	 * Get the button which rotates the plot.
	 * @return the button which roates the plot; 
	 */
	public Button getRotateButton(){
		return rotate; 
	}

	/**
	 * Get the tdControl class. 
	 * @return reference to tdControl. 
	 */
	public TDControl getTDControl() {
		return tdControl;
	}

	/**
	 * Check whether the pause button has been selected.
	 * @return true if selected
	 */
	public boolean isPaused() {
		return this.pause.isSelected();
	}
	

//	/**
//	 * Clears any current buttons and creates a new set of graph settings buttons. Used primarily during initialization of display.
//	 */
//	private void createGraphButtons(){
////		System.out.println("CREATE Graph Button:" ); 
//		clearAllGraphButtons();
//		for (int i=0; i<tdDisplay.getTDGraphs().size(); i++){
////			System.out.println("Add graph Button!!" + i); 
//			addGraphButton(tdDisplay.getTDGraphs().get(i));
//		}
//	}
//	
//	/**
//	 * Add a button to the hBox pane to access settings for an individual tdGraph.
//	 * @param tdGraph- tdGraph to assign to button. 
//	 */
//	private void addGraphButton(TDGraphFX tdGraph){
//		TDGraphMenuButton graphButton=new TDGraphMenuButton(tdGraph); 
//		
//		graphButton.setText(("Graph "+(tdDisplay.getTDGraphs().indexOf(tdGraph))));
//		graphButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, Color.WHITE, PamGuiManagerFX.iconSize));
//
//		graphButton.setPrefHeight(50);
//		//add to the pane
//		graphControlPane.getChildren().add(graphButton);
//	}
//
//
//	/**
//	 * Remove one of the buttons from the hBox pane.
//	 * @param iGraph- graph button to remove. 
//	 */
//	private void removeGraphButton(int iGraph){
//		if (graphControlPane.getChildren().size() > iGraph) {
//			graphControlPane.getChildren().remove(iGraph); 
//		}
//		//now we have to rename all the buttons so they correspond to the correct graph. 
//		int n=0; 
//		for (int i=0; i<graphControlPane.getChildren().size(); i++){
//			if (graphControlPane.getChildren().get(i) instanceof TDGraphMenuButton) {
//				((TDGraphMenuButton) graphControlPane.getChildren().get(i)).setText("Graph: "+n);
//				n++;
//			}
//		}
//	}
//
//	/**
//	 * Remove all the graph buttons. 
//	 */
//	private void clearAllGraphButtons(){
////		System.out.println("CLEAR all graph buttons:" ); 
//		graphControlPane.getChildren().removeAll(graphControlPane.getChildren());
//	}
//	
//	
//
//	/**
//	 * Simple class to allow a button to hold a reference to the tdGraph it changes the settings for, 
//	 * @author Jamie Macaulay
//	 *
//	 */
//	class TDGraphMenuButton extends SplitMenuButton{
//
//		private TDGraphFX tdGraph;
//		
//		private PopOver popOver; 
//
//		TDGraphMenuButton(TDGraphFX tdGraph){
//			this.tdGraph=tdGraph; 
//			
//			this.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, Color.WHITE, PamGuiManagerFX.iconSize));
//			
//			popOver=new PopOver(); 
//			popOver.setFadeInDuration(new Duration(100));
//			popOver.setFadeOutDuration(new Duration(100));
//			popOver.setContentNode(tdGraph.getTDGraphSettingsPane().getContentNode()); 
//			popOver.setArrowLocation(ArrowLocation.TOP_LEFT);
//			popOver.setCornerRadius(5);
//			
//			//show the graph settings pane
//			this.setOnAction((action)->{
//				
//				tdGraph.getTDGraphSettingsPane().setParams(tdGraph.getGraphParameters());
//				
//				Point2D loc = this.localToScreen(new Point2D(this.getLayoutX(), this.getLayoutY())); 
//				
//				//I have no idea why this works but it works- FIXME
//				//In FX mode need to make the scene the owner- in Swing there is no scene. 
//				if (PamGUIManager.isFX()) popOver.show(tdGraph.getScene().getWindow(), loc.getX()-this.getLayoutX()+this.getWidth()/2, loc.getY()+this.getHeight()/2);
//				else popOver.show(tdGraph.getTDDisplay(), loc.getX()-this.getLayoutX()+this.getWidth()/2, loc.getY()+this.getHeight()/2);
//								
//				((Parent) popOver.getSkin().getNode()).getStylesheets()
//			    .add(tdDisplay.getCSSSettingsResource());
//			});
//			
//			// show the graph add and remove menu
//			this.showingProperty().addListener(new ShowTDGraphMenu(this));
//		}
//
//		public TDGraphFX getTDgraph(){
//			return tdGraph;
//		}
//	}
	
	
//	/**
//	 * Called whenever the menu part of the split pane button is clicked on a TDGraph settings button. 
//	 * Brings up a list of quick access options, including which data blocks to add/remove from the graph. 
//	 * @author Jamie Macaulay 
//	 */
//	private class ShowTDGraphMenu implements ChangeListener<Boolean>{
//
//		private TDGraphMenuButton splitMenuButton;
//
//		public ShowTDGraphMenu(TDGraphMenuButton splitMenuButton){
//			this.splitMenuButton=splitMenuButton;
//		}
//
//		@Override
//		public void changed(ObservableValue<? extends Boolean> arg0,
//				Boolean arg1, Boolean arg2) {
//			//remove all menu items
//			splitMenuButton.getItems().removeAll(splitMenuButton.getItems());
//			createSettingsMenu(splitMenuButton);
//		}
//
//	}
//
//
//	/**
//	 * Create a menu to allow data blocks to quickly added and removed from a tdGraph. 
//	 * @param tdGraphMenuButton - the graph button which shows the settings menu. 
//	 */
//	private void createSettingsMenu(TDGraphMenuButton tdGraphMenuButton){
//		//the complete list of data providers. 
//		final TDGraphFX tdGraph=tdGraphMenuButton.getTDgraph(); 
//
//		// create the menu to add data blocks to the graph : 
//		Menu addGraph=new Menu("Add"); 
//		addGraph.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.ADD, Color.WHITE, PamGuiManagerFX.iconSize));
//		createAddMenuItems( addGraph.getItems(), tdGraph); 
//
//		//create the list to allow to remove data blocks from the graph. 
//		Menu removeGraph=new Menu("Remove"); 
//		removeGraph.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.REMOVE, Color.WHITE, PamGuiManagerFX.iconSize));
//		createRemoveMenuItems(removeGraph.getItems(), tdGraph); 
//
//		//add both menus to the split buttons main menu. 
//		tdGraphMenuButton.getItems().add(addGraph);
//		tdGraphMenuButton.getItems().add(removeGraph);
//	}
	
//	/**
//	 * Update the pane showing all graph settings buttons
//	 */
//	public void refreshGraphButtonPane(){
//		clearAllGraphButtons();
//		createGraphButtons();
//	}


	//	/**
	//	 * Called whenever a graph settings button is pressed 
	//	 * @author Jamie Macaulay
	//	 */
	//	private class GraphButton implements EventHandler<ActionEvent>{
	//		
	//		private TDGraphFX tdGraphFX;
	//
	//		public TDGraphFX getTdGraphFX() {
	//			return tdGraphFX;
	//		}
	//
	//		public GraphButton(TDGraphFX tdGraphFX){
	//			this.tdGraphFX=tdGraphFX;
	//		}
	//
	//		@Override
	//		public void handle(ActionEvent arg0) {
	//			
	//		}
	//	}

}
