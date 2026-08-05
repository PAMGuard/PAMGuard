package pamViewFX;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.controlsfx.control.ToggleSwitch;

import Acquisition.layoutFX.PaneFactory;
import Acquisition.layoutFX.PaneFactory.PaneFactoryPane;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamView.PamViewInterface;
import dataModelFX.DataModelPaneFX;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.pamTask.PamLoadingPane;
import pamViewFX.pamTask.PamTaskUpdate;
import userDisplayFX.UserDisplayNodeFX;



/**
 * A pane which holds a set of tabs. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamGuiFX extends StackPane implements PamViewInterface {
	
	/**
	 * This is essentially one PAMGuard stage. PAMGuard may have multiple stages in which case it will have multiple PAMGuiFX instances. 
	 */
	private PamTabPane mainTabPane; 
	
	/**
	 * The preferred width of the side pane. 
	 */
	public static final double SIDE_PANE_PREF_WIDTH = 250;
	
//	/**
//	 * Icon for menu
//	 */
//	public Image menuIconGrey=new Image(getClass().getResourceAsStream("/Resources/MenuButton.png"));
//	
//	public Image showButtonGrey=new Image(getClass().getResourceAsStream("/Resources/SidePanelShow2Grey.png"));
//	
//	public Image hideButtonGrey=new Image(getClass().getResourceAsStream("/Resources/SidePanelHide2Grey.png"));
//
//	public Image recordStart=new Image(getClass().getResourceAsStream("/Resources/recordStart.png"));
//
//	public Image playPause=new Image(getClass().getResourceAsStream("/Resources/playStart.png"));

	/**
	 * A reference to the stage this PAMGUIFX belongs to. 
	 */
	private Stage stage;

	/**
	 * Reference to the PamGuimanagerFX.  
	 */
	private PamGuiManagerFX pamGuiManagerFX;

	/**
	 * Hiding side pane. 
	 */
	private HidingPane hidingSidePane;
	
	/**
	 * The Pane which holds everything that sits in hide pane. 
	 */
	private  PamVBox sidePaneContent;
	

	/**
	 * Pane which holds main PAMGUARD settings
	 */
	private PamVBox settingsPane; 
	
	/**
	 * Hiding pane which holds settings
	 */
	private HidingPane hidingPaneLeft;

	private PamVBox leftPaneWrapper;
	private PamVBox settingsContentPane;
	private PamBorderPane mainLayout;

	/**
	 * Pane which shows load progress. 
	 */
	private PamLoadingPane loadPane;

	/**
	 * Pane which holds load pane. 
	 */
	private HidingPane hidingLoadPane;

	/**
	 * True if the pane is showing. 
	 */
	private boolean showingLoadMode;

	private ObservableList<Node> children;

	/**
	 * The single shared toolbar that sits between tab headers and tab content.
	 * This replaces the per-tab toolbars that were previously duplicated for each tab.
	 */
	private ToolBarPane sharedToolbar;


	/**
	 * Create a new PamGUIFX.
	 * @param mainTabPane - the tabPane. 
	 * @param stage - the stage in which the PamGuiFX will reside. 
	 * @param pamGuiManagerFX - the GUI manager. 
	 */
	public PamGuiFX (PamTabPane mainTabPane, Stage stage, PamGuiManagerFX pamGuiManagerFX){
		
		this.stage=stage; 
		this.pamGuiManagerFX=pamGuiManagerFX; 
        this.mainTabPane = mainTabPane;
		
        Node layout=createMainPane(mainTabPane, stage); 

		//add main pane to PamGui
		this.getChildren().add(layout);

	}
	
	public PamGuiFX (Stage stage, PamGuiManagerFX pamGuiManagerFX){
		
		this.stage=stage; 
		this.pamGuiManagerFX=pamGuiManagerFX; 
		
		//create the main tab pane. 
        this.mainTabPane = new PamTabPane();
       		
        Node layout=createMainPane(mainTabPane, stage); 
	   
	    //add main pane to PamGui
	    this.getChildren().add(layout);

	}
	
	
	/**
	 * Create the pane which sits inside stage and holds tabs, side pane. 
	 * @param mainTabPane - the main tab pane for the scene. 
	 * @param stage - the stage holding this GUI.
	 * @return a pane which sits in the stage. 
	 */
	private Node createMainPane(PamTabPane mainTabPane, Stage stage){

		//create the pane which holds tab pane
		final PamBorderPane layout = new PamBorderPane();
		layout.setCenter(mainTabPane);

		/**create right hiding pane**/
		sidePaneContent=new PamVBox();
		sidePaneContent.setPrefWidth(SIDE_PANE_PREF_WIDTH);
		sidePaneContent.setPadding(new Insets(25,5,5,5)); //give quite  abit of spacing at the top so that there is room for close button
		sidePaneContent.setMinWidth(0);
		hidingSidePane=new HidingPane(Side.RIGHT, sidePaneContent, this, false);
		hidingSidePane.setShowButtonOpacity(1.0);

		hidingSidePane.showHidePane(false);

		//create the button which shows the hiding panel. Although we get this button from the hiding pane, where to place
		//it and what colour it is etc has to be set for whatever pane it is to be located in. 
		PamButton showButtonRight=hidingSidePane.getShowButton();
//		showButtonRight.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, PamGuiManagerFX.iconColor, PamGuiManagerFX.iconSize));
		showButtonRight.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
		//showLeftButton.setText(PamFontAwesome.ICON_CHEVRON_LEFT);
		showButtonRight.getStyleClass().add("close-button-left-trans");
		showButtonRight.setStyle("-fx-background-radius: 0 0 0 0;");

		//alter animations to remove/add showButton to tab pane. 
		//TODO- tried to have animation here to hide buttons but didn't appear to work. 
		//Maybe an issue with running another animation from animation thread?
//		showButton.prefWidthProperty().addListener((listener)->{
//			mainTabPane.layout(); //Don't need this
//		});
				
		hidingSidePane.getTimeLineShow().setOnFinished((value)->{
			showButtonRight.setPrefWidth(1);
			showButtonRight.setVisible(false);
			hidingSidePane.getHideButton().setVisible(true);
		});

		hidingSidePane.getTimeLineHide().setOnFinished((value)->{
			showButtonRight.setPrefWidth(40);
			showButtonRight.setVisible(true);
			hidingSidePane.getHideButton().setVisible(false);
			//sidePaneContent.setVisible(false);
			layout.layout();
		});

		PamButton closeButtonLeft=hidingSidePane.getHideButton();
		closeButtonLeft.getStyleClass().add("close-button-right-trans");
//		closeButtonLeft.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_RIGHT, Color.DARKGRAY.darker(), PamGuiManagerFX.iconSize));
		closeButtonLeft.prefHeightProperty().bind(mainTabPane.getHeaderHeightProperty());
		
		//add hiding pane to main pane. 
		layout.setRight(hidingSidePane);
				
		/**create settings pane. This allows access to primary PAMGUARD settings.**/
		settingsPane=new PamSettingsMenuPane();
		settingsPane.setPrefWidth(250);
		
		leftPaneWrapper = new PamVBox();
		leftPaneWrapper.setPrefWidth(250);
		settingsContentPane = settingsPane;
		leftPaneWrapper.getChildren().add(settingsContentPane);
		
		hidingPaneLeft=new HidingPane(Side.LEFT, leftPaneWrapper, this, false);
		hidingPaneLeft.showHidePane(false);
		
		mainLayout = layout;
		
		hidingPaneLeft.showingProperty().addListener((obs, wasShowing, isShowing) -> {
			if (!isShowing) {
				restoreLeftPane();
			}
		});
		
		
		hidingPaneLeft.getStylesheets().addAll(pamGuiManagerFX.getPamSettingsCSS());

		PamButton showButtonLeft=hidingPaneLeft.getShowButton();
//		showButtonLeft.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.BARS, Color.LIGHTGRAY, PamGuiManagerFX.iconSize));
		showButtonLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2m-menu", PamGuiManagerFX.iconSize));
		showButtonLeft.getStyleClass().add("close-button-right-trans");
		showButtonLeft.setStyle(" -fx-background-radius: 0 0 0 0;");
		
		PamButton closeRightButton=hidingPaneLeft.getHideButton();
		closeRightButton.setPrefWidth(40);
		closeRightButton.getStyleClass().add("close-button-left-trans");
//		closeRightButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CHEVRON_LEFT, Color.WHITE, PamGuiManagerFX.iconSize));
		closeRightButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
		closeRightButton.prefHeightProperty().bind(mainTabPane.getHeaderHeightProperty());
		
		//add hiding pane to main pane. 
		layout.setLeft(hidingPaneLeft);
		
//		//Create a button which sits at end of tab pane
//		settingsButton  = new PamButton();
//		settingsButton.setMinWidth(Control.USE_PREF_SIZE);
//		settingsButton.setGraphic(Glyph.create("FontAwesome|BARS").size(22).color(Color.DARKGREY.darker()));
//		settingsButton.getStyleClass().add("transparent-button");
////		settingsButton.setPadding(new Insets(0,10,0,10));
//		settingsButton.setOnAction(e -> { 
//			//TODO-open settings menu
//		});

		mainTabPane.setTabEndRegion(showButtonRight);
		mainTabPane.setTabStartRegion(showButtonLeft);
//		mainTabPane.getStyleClass().add(Styles.TABS_FLOATING);

		//create the single shared toolbar and set it on the tab pane
		sharedToolbar = new ToolBarPane(PamGuiFX.this);
		mainTabPane.setToolbarRegion(sharedToolbar);
		
		//update the toolbar's resize controls when the selected tab changes
		mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
			updateToolbarForTab((PamGuiTabFX) newTab);
		});

		mainTabPane.getAddTabButton().setOnAction((value)->{
		    addPamTab(new TabInfo("Display " + (this.getNumTabs()+1)), null ,true); 
		    mainTabPane.layout();
		});
		
		//now have a holder - add the loading pane. 
		/**create left hiding pane**/
		loadPane=new PamLoadingPane(this.pamGuiManagerFX);
		hidingLoadPane=new HidingPane(Side.TOP, loadPane, this, false);
		hidingLoadPane.setPrefHeight(110);
		hidingLoadPane.removeHideButton();
		hidingLoadPane.showHidePane(false);
		
		PamBorderPane layoutHolder=new PamBorderPane(layout);
		layoutHolder.setTop(hidingLoadPane);

	    return  layoutHolder; 
	    
	}
	
	/**
	 * Get the load pane for the PamGuiFX- shows loading data and other status updates. 
	 * @return the PamLoadingf Pane
	 */
	public PamLoadingPane getLoadPane() {
		return loadPane;
	}
	
	/**
	 * Update the shared toolbar controls to reflect the currently selected tab.
	 * For example, the resize toggle should reflect the current tab's editable state,
	 * and the resize controls should be shown/hidden based on whether the tab has
	 * resizable displays. If the tab has custom right-side toolbar content 
	 * (e.g. zoom controls for the data model), that will be shown instead.
	 * @param tab - the newly selected tab. Can be null.
	 */
	private void updateToolbarForTab(PamGuiTabFX tab) {
		if (sharedToolbar == null) return;
		if (tab != null) {
			// Swap right-side content if the tab has custom content
			sharedToolbar.swapRightContent(tab.getCustomToolbarRight());
			// Update the resize toggle state
			sharedToolbar.showResizableControl(tab.isStaticDisplay());
		}
	}
	
	/**
	 * Get the single shared toolbar for this PamGuiFX.
	 * @return the shared ToolBarPane.
	 */
	public ToolBarPane getSharedToolbar() {
		return sharedToolbar;
	}

	/**
	 * Show this PamGUIFX. 
	 */
	public void show(){
		stage.show(); 
	}
	
		
	/**
	 * Convenience function to add a closable tab to the display with a new UserDisplayNodeFX.
	 * @param name- tab name.
	 * @param content- content to add to the tab. Can be null; 
	 */
	public PamGuiTabFX addPamTab(TabInfo tabInfo, UserDisplayNodeFX content, boolean detachable ){        
        //create holder pane and add to tab
		PamGuiTabFX newTab = new PamGuiTabFX(tabInfo, this);
		
		//static displays have non closable tabs and non resizable displays
        if (content!=null){
        	newTab.setClosable(!content.isStaticDisplay());
        	newTab.setResizableDisplays(!content.isStaticDisplay());

        	if (content.isStaticDisplay()){
        		//if static, then add to center. 
        		newTab.getContentHolder().setCenter(content.getNode());
        	}
        	else{
        		//add content- will not add and return null if content is null 
        		newTab.addInternalPane(content);
        	}
        }
        
        newTab.setOnClosed((action)->{
        	//when a tab is closer. 
        	for (int i=0; i<newTab.getInternalPanes().size(); i++) {
        		System.out.println("REMOVE TAB: " + newTab.getInternalPanes().size());

	        	newTab.getInternalPanes().get(i).getUserDisplayNode().closeNode();
	        	if (newTab.getInternalPanes().get(i).getUserDisplayNode().getUserDisplayControl()!=null) {
	        		System.out.println("REMOVE CONTROLLED DISPLAY UNIT: " + newTab.getInternalPanes().get(i).getUserDisplayNode().getUserDisplayControl());
	        		//the display is a standalone display and so remove the tab means the controlled unit should be removed from the data model 
	        		PamController.getInstance().removeControlledUnt(newTab.getInternalPanes().get(i).getUserDisplayNode().getUserDisplayControl());
	        		PamGuiManagerFX.getInstance().getDataModelFX().dataModeltoPamModel();
	        	}
        	}
        });
        
        newTab.setDetachable(detachable);
   
        //add tab
        mainTabPane.getTabs().add(newTab);
        
        return newTab;
	}
		
	/**
	 * Add a tab for the data model. 
	 * @param dataModelPaneFX - the data model 
	 */
	public DataModelPaneFX addDataModelTab() {
		
		PamGuiTabFX newTab = new PamGuiTabFX(new TabInfo("Data Model"), this);
		
        newTab.setClosable(false); //can't close a data model
        newTab.setDetachable(false);
        mainTabPane.getTabs().add(newTab);
        DataModelPaneFX dataModelPaneFX=new DataModelPaneFX(); 
        
        //create controls in the tool bar for the data model 
        dataModelPaneFX.createToolbarControls(newTab); 
        
        newTab.setMainContent(dataModelPaneFX);
        return dataModelPaneFX; 
		
	}
	
	/**
	 * Get all tabs for this PamGuiFX
	 * @return list of tabs in the PamGuiFX
	 */
	public ArrayList<PamGuiTabFX> getTabs(){
		ArrayList<PamGuiTabFX> tabs=new ArrayList<PamGuiTabFX>();
		for (int i=0; i<mainTabPane.getTabs().size(); i++){
			tabs.add((PamGuiTabFX) mainTabPane.getTabs().get(i));
		}
		return tabs;
	}
	
	/**
	 * Add a tab to the tab pane.
	 * @param pamGuiTabFX - the tab to add
	 */
	public void addTab(PamGuiTabFX pamGuiTabFX) {
		// add a tab.
		mainTabPane.getTabs().add(pamGuiTabFX);
	}
	
	/**
	 * Add tabs to the tab pane.
	 * @param pamGuiTabFX - the tabs to add
	 */
	public void addAllTabs(List<PamGuiTabFX> pamGuiTabFXs) {
		// add a tab.>
		mainTabPane.getTabs().addAll(pamGuiTabFXs);
	}
	
	/**
	 * Get the number of tabs currently open.
	 * @return the number of tabs.
	 */
	public int getNumTabs(){
		return mainTabPane.getTabs().size();
	}
	
	/**
	 * Get a tab
	 * @param i - tab index
	 * @return the ith PamGuiTab 
	 */
	public PamGuiTabFX getTab(int i){
		return (PamGuiTabFX) mainTabPane.getTabs().get(i);
	}
	
	/**
	 * Select a tab
	 * @param j - index 
	 */
	public void selectTab(int j) {
		this.mainTabPane.getSelectionModel().select(j);
		
	}
	
	private boolean isViewer() {
		return PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}
	

	/**
	 * Create the tool bar pane. The top of each tab content node contains a tool
	 * bar which contains play/pause, process and settings buttons. Some displays
	 * can access this tool bar and add quick access controls.
	 * <p>
	 * e.g. if a click display is added to the tab it may have a list of check boxes
	 * to quickly switch between which types of clicks are shown.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class ToolBarPane extends PamBorderPane {
		
		Node runIcon = PamGlyphDude.createPamIcon("mdi2r-record-circle", Color.RED, 30);
		Node stopIcon = PamGlyphDude.createPamIcon("mdi2p-pause", Color.BLACK, 30);
		
		/**
		 * Record/batch process button. In real time starts/stops data acquisition. In viewer mode
		 * opens the batch run manager to allow batch processing of data e.g. reclassifying clicks. 
		 */
		private PamButton recordButton;
	
		
		/**
		 * Holds all extra controls in the toolbar. 
		 */
		private PamHBox centerHBox;
		
		/**
		 * Height of the toolbar. 
		 */
		private double prefHeight=45;

		/**
		 * Reference to the PamGuiFX that owns this toolbar.
		 */
		private PamGuiFX pamGuiFX;

		/**
		 * HBox whihc by default holds a toggle switch on the right of the toolbar to change window sizes
		 * 
		 */
		private PamHBox rightHBox;

		/**
		 * Switch to toggle window edit. 
		 */
		private ToggleSwitch editWindows;

		/**
		 * Button to select what autosort option for windows. 
		 */
		private MenuButton autoSortChoice;
	
		/**
		 * Create a ToolBarPane for a PamGuiFX. This is a single shared toolbar
		 * that sits between the tab headers and the tab content for all tabs.
		 * @param pamGuiFX - the PamGuiFX this toolbar belongs to.
		 */
		public ToolBarPane(PamGuiFX pamGuiFX){
			super();
			
			this.pamGuiFX=pamGuiFX;
		
			//create record and play buttons. 
			Pane playControls;
			
			if (isViewer()) {
				playControls = createViewerControls();

			}
			else {
				playControls = createRealTimeControls();
			}
			
			

			
			//create window editing button. This holds a toggle to edit windows and options. 
			rightHBox=new PamHBox();
			rightHBox.setAlignment(Pos.CENTER_LEFT);
			rightHBox.setPadding(new Insets(0,10,0,20));
			rightHBox.setSpacing(5);
//			rightHBox.getStyleClass().add("pane-opaque");
			
			editWindows=new ToggleSwitch("Resize"); 
			//HACK,
			editWindows.setPadding(new Insets(8,0,0,0));
			//editWindows.setAlignment(Pos.CENTER);		
			editWindows.selectedProperty().addListener((listen)->{
				PamGuiTabFX selectedTab = getSelectedTab();
				if (selectedTab != null) {
					boolean editable=!selectedTab.getEditable();
					selectedTab.setPanesEditable(editable);
				}
			});
			
			//add a choice box to allow users to automatically resize windows. 
			autoSortChoice=new MenuButton();
			autoSortChoice.getStyleClass().add("transparent-button");
//			autoSortChoice.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.ELLIPSIS_V, 
			autoSortChoice.setGraphic(PamGlyphDude.createPamIcon("mdi2d-dots-vertical", 
					Color.DARKGRAY, PamGuiManagerFX.iconSize));
			
			MenuItem tile=new MenuItem("Tile");
			tile.setOnAction((action)->{
				PamGuiTabFX selectedTab = getSelectedTab();
				if (selectedTab != null) selectedTab.autoSortPanes(PamGuiTabFX.SORT_TILE);
			});
			
			MenuItem vertical=new MenuItem("Horizontal");
			vertical.setOnAction((action)->{
				PamGuiTabFX selectedTab = getSelectedTab();
				if (selectedTab != null) selectedTab.autoSortPanes(PamGuiTabFX.SORT_HORIZONTAL);
			});
			
			MenuItem horizontal=new MenuItem("Vertical");
			horizontal.setOnAction((action)->{
				PamGuiTabFX selectedTab = getSelectedTab();
				if (selectedTab != null) selectedTab.autoSortPanes(PamGuiTabFX.SORT_VERTICAL);
			});
			
			autoSortChoice.getItems().addAll(tile, vertical, horizontal); 
			
			rightHBox.getChildren().addAll(editWindows, autoSortChoice);

			
			//create content hboc for extra controls.
			centerHBox=new PamHBox();
			centerHBox.setSpacing(10);
			//need to set this style to prevent the pane form being transparent. 
			//centerHBox.getStyleClass().add("pane-opaque");
			centerHBox.setPrefHeight(prefHeight);
			
			this.setCenter(centerHBox);
			this.setLeft(playControls);
			this.setRight(rightHBox);
			
			this.setPrefHeight(prefHeight);
			this.getStyleClass().add("pane-opaque");


			//this.setPadding(new Insets(0,0,0,0));
			
			this.toFront();

		}
		
		/**
		 * Get the currently selected PamGuiTabFX from the parent PamGuiFX.
		 * @return the currently selected tab, or null if none.
		 */
		private PamGuiTabFX getSelectedTab() {
			if (pamGuiFX != null && pamGuiFX.mainTabPane != null) {
				javafx.scene.control.Tab selected = pamGuiFX.mainTabPane.getSelectionModel().getSelectedItem();
				if (selected instanceof PamGuiTabFX) {
					return (PamGuiTabFX) selected;
				}
			}
			return null;
		}
		
		
		private Pane createViewerControls() {
			
			//create record and play buttons. 
			PamHBox playControls = new PamHBox();
			
			PamButton reProcess=new PamButton("Reprocess");
			reProcess.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", PamGuiManagerFX.iconSize));
			reProcess.setOnAction((action)->{
				showReprocessPane();
			});
			
			
			PamButton exportButton = new PamButton("Export data");
			exportButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-database-export", PamGuiManagerFX.iconSize));
			exportButton.setOnAction((action)->{
				showExportPane();
			});
			
			PamButton importButton = new PamButton("Import data");
			importButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-database-import", PamGuiManagerFX.iconSize));
			importButton.setOnAction((action)->{
				//import dialog
			});


			
			playControls.getChildren().addAll(reProcess, exportButton, importButton);
			playControls.setSpacing(5);
			playControls.setPadding(new Insets(0,10,0,50));
//			playControls.getStyleClass().add("pane-opaque");
			playControls.setPrefHeight(prefHeight);
			playControls.setAlignment(Pos.CENTER_LEFT);
			
			return playControls;
			
		}
		
		
		/**
		 * Create controls for PAMGuard for real time processing. 
		 * @return
		 */
		private Pane createRealTimeControls() {
		

			//create record and play buttons. 
			PamHBox playControls=new PamHBox();
			recordButton=new PamButton("Process");
//			recordButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.CIRCLE, Color.LIMEGREEN, PamGuiManagerFX.iconSize));
			recordButton.setGraphic(runIcon);
			//recordButton.getStyleClass().add("transparent-button");
			recordButton.setStyle(" -fx-padding: 1 15 1 5");
			
			recordButton.setOnAction((action)->{
				if (PamController.getInstance().getPamStatus()==PamController.PAM_RUNNING){
					PamController.getInstance().pamStop();
					//recordButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-record-circle", Color.RED, PamGuiManagerFX.iconSize));

				}
				else {
					PamController.getInstance().pamStart();
					//recordButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-pause", Color.DARKGRAY, PamGuiManagerFX.iconSize));
				}
			});

//			playButton=new PamButton();
////			playButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.PLAY, Color.BLACK, PamGuiManagerFX.iconSize));
//			playButton.setGraphic(PamGlyphDude.createPamIcon("mdi2p-play", Color.BLACK, PamGuiManagerFX.iconSize));
//			playButton.getStyleClass().add("transparent-button");
////			playButton.setStyle(" -fx-background-radius: 50;");
//			playButton.setOnAction((action)->{
//				//TODO
//				//start pamguard
//				//PamController.getInstance().pamStart();
//			});
			
			playControls.getChildren().addAll(recordButton);
			playControls.setSpacing(10);
			playControls.setPadding(new Insets(0,10,0,20));
//			playControls.getStyleClass().add("pane-opaque");
			playControls.setPrefHeight(prefHeight);
			playControls.setAlignment(Pos.CENTER);
			
			return playControls;
		}
		
		
		/**
		 * Get the content HBox. This holds any extra controls in the top tool bar. 
		 * @return HBox to add extra toolbar content to. 
		 */
		public PamHBox getCenterHBox(){
			return centerHBox;
		}
		
		/**
		 * Add a node to the content area of the toolbar. 
		 * @param node - node to add. 
		 */
		public void addToolbarNode(Node node){
			centerHBox.getChildren().add(node);
		}
		
		/**
		 * Get the button which starts and stops PAMGUARD. 
		 * @return the button. 
		 */
		public Button getRecordButton(){
			return recordButton; 
		}
		
		public void setRecordButtonState(boolean running) {
			if (running) {
				recordButton.setGraphic(stopIcon);
				recordButton.setText("Pause");
			}
			else {
				recordButton.setGraphic(runIcon);
				recordButton.setText("Process");
			}
		}
		
		/**
		 * Get the HBox on the right hand side of the toolbar. 
		 * @return hbox on right hand side of display. 
		 */
		public PamHBox getRightHBox() {
			return rightHBox;
		}


		public void setRightHBox(PamHBox rightHBox) {
			this.rightHBox = rightHBox;
		}

		/**
		 * Swap the right-side content of the toolbar. This allows different tabs to show
		 * different controls on the right side (e.g. the data model shows zoom controls 
		 * while normal tabs show resize controls).
		 * @param customRight - the custom right content, or null to restore the default.
		 */
		public void swapRightContent(Region customRight) {
			if (customRight != null) {
				this.setRight(customRight);
			} else {
				// Restore the default right content (resize toggle + sort options)
				this.setRight(rightHBox);
			}
		}

		/**
		 * Show the controls which allows manula resizing of the pane. 
		 * @param resize. 
		 */
		public void showResizableControl(boolean resize) {
			//always set panes to be in non edit mode. 
			editWindows.setSelected(false);
			editWindows.setVisible(resize);
			editWindows.setDisable(!resize);
		}

	}
	
	/**
	 * Get the record button from the shared toolbar.
	 * @return list of record buttons (single button in a list for backward compatibility). 
	 */
	protected ArrayList<Button> getRecordButtons(){
		ArrayList<Button> buttons=new ArrayList<Button>();
		if (sharedToolbar != null && sharedToolbar.getRecordButton() != null) {
			buttons.add(sharedToolbar.getRecordButton()); 
		}
		return buttons; 
	}

	/**
	 * Show the offline reprocessing pane in the left hiding pane and disable
	 * the rest of the GUI until the user closes or finishes processing.
	 */
	private void showReprocessPane() {
		offlineProcessing.fx.OfflineProcessPaneFX processPaneFX =
				new offlineProcessing.fx.OfflineProcessPaneFX();
		processPaneFX.setOnClose(() -> restoreLeftPane());
		Node content = processPaneFX.createContentPane();
		showLeftPaneContent(content);
	}

	/**
	 * Show the data export pane in the left hiding pane and disable
	 * the rest of the GUI until the user closes or finishes exporting.
	 */
	private void showExportPane() {
		export.layoutFX.ExportPaneFX exportPaneFX =
				new export.layoutFX.ExportPaneFX();
		exportPaneFX.setOnClose(() -> restoreLeftPane());
		Node content = exportPaneFX.createContentPane();
		showLeftPaneContent(content);
	}

	/**
	 * Swap the left hiding pane content with the given node, open the pane,
	 * and disable the main tab area so the user must interact with the left
	 * pane before returning to PAMGuard.
	 *
	 * @param content the content node to display.
	 */
	private void showLeftPaneContent(Node content) {
		leftPaneWrapper.getChildren().clear();
		leftPaneWrapper.getChildren().add(content);
		leftPaneWrapper.setPrefWidth(450);
		hidingPaneLeft.showHidePane(true);
		mainTabPane.setDisable(true);
		if (sharedToolbar != null) {
			sharedToolbar.setDisable(true);
		}
	}

	/**
	 * Restore the left hiding pane to its original settings content and
	 * re-enable the main GUI.
	 */
	private void restoreLeftPane() {
		leftPaneWrapper.getChildren().clear();
		leftPaneWrapper.getChildren().add(settingsContentPane);
		leftPaneWrapper.setPrefWidth(250);
		hidingPaneLeft.showHidePane(false);
		mainTabPane.setDisable(false);
		if (sharedToolbar != null) {
			sharedToolbar.setDisable(false);
		}
	}

	/**
	 * Remove an internal pane if it is contained within any tabs within the PamGuiFX
	 * @param removeNode - remove the pane if it contains this node. 
	 */
	public void removeInternalPane(UserDisplayNodeFX removeNode){
		ArrayList<PamGuiTabFX> tabs= getTabs();
		for (int i=0; i<tabs.size(); i++){
			tabs.get(i).removeInternalPane(removeNode);
		}
	}
	
	/**
	 * Show the PamLoadPnae- this shows the pane that contains progress bars for loading data.
	 * @param show - true to show pane. 
	 */
	public void showLoadingPane(boolean show) {
		if (showingLoadMode!=show){
			showingLoadMode=show;
			hidingLoadPane.showHidePane(show);
		}
	}

	/**
	 * Change the GUI to show load mode. 
	 * @param loadMode - true to show load mode. 
	 */
	public void showLoadMode(boolean loadMode) {
		this.showLoadingPane(loadMode);
	}

	/**
	 * Notify load progress.
	 * @param pamTaskUpdate - information on a thread which s currently doing some work. 
	 */
	public void notifyLoadProgress(PamTaskUpdate pamTaskUpdate) {
		 Platform.runLater (() -> this.loadPane.updateLoadPane(pamTaskUpdate));
	}

	@Override
	public void pamStarted() {
		//System.out.println("PAMGUIFX: Pam started");
		 setRecordbuttonState(true);
	}

	@Override
	public void pamEnded() {
		//System.out.println("PAMGUIFX: Pam ended");
		 setRecordbuttonState(false);
;	
	}
	
	/**
	 * Set the state of the record button in the shared toolbar.
	 * @param running - true if PAMGuard is running
	 */
	public void setRecordbuttonState(boolean running) {
		if (sharedToolbar != null) {
			sharedToolbar.setRecordButtonState(running);
		}
	}
	
	
	/**
	 * Add a pane to the shared toolbar.
	 * @param paneFactory
	 */
	public boolean addStatusBarPane(PaneFactory paneFactory) {
		
		System.out.println("PamGuiFX: addToolBarPane: adding pane factory " + paneFactory.getPaneFactoryName());
		
		if (sharedToolbar == null) return false;
		
		//check that a pane factory of this type does not already exist.
		children = sharedToolbar.getCenterHBox().getChildren();
		
		for (int j=0; j<children.size(); j++) {
			if (children.get(j) instanceof PaneFactoryPane) {
				PaneFactory factory = ((PaneFactoryPane) children.get(j)).getFactoryRef();
				if (factory.equals(paneFactory)) {
					//pane factory of this instance already exists- do not add another. 
					return false;
				}
			}
		}
		
		sharedToolbar.getCenterHBox().getChildren().add(paneFactory.createPane());
		return true;
	}
	

	public void removeStatusBarPane(PaneFactory statusPaneFactory) {
		if (sharedToolbar == null) return;
		children = sharedToolbar.getCenterHBox().getChildren();
		for (int j = children.size() - 1; j >= 0; j--) {
			if (children.get(j) instanceof PaneFactoryPane) {
				PaneFactory factory = ((PaneFactoryPane) children.get(j)).getFactoryRef();
				if (factory.equals(statusPaneFactory)) {
					children.remove(j);
				}
			}
		}
	}




	@Override
	public void modelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addControlledUnit(PamControlledUnit unit) {
		this.pamGuiManagerFX.addControlledUnit(unit);
		
	}

	@Override
	public void removeControlledUnit(PamControlledUnit unit) {
		this.pamGuiManagerFX.removeControlledUnit(unit);
		
	}

	@Override
	public void showControlledUnit(PamControlledUnit unit) {
		this.pamGuiManagerFX.showControlledUnit(unit);
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFrameNumber() {
		return 0;
	}

	@Override
	public JFrame getGuiFrame() {
		// there are no frames in FX mode. 
		return null;
	}

	@Override
	public void enableGUIControl(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Check whether the frame is in mode showing loadpanes. 
	 * @return true if in load mode. 
	 */
	public boolean isShowLoadMode() {
		return this.showingLoadMode;
	}

	/**
	 * Get the PamGuiManagerFX which controls the PamGuiF X
	 * @return the PamGuiManagerFX
	 */
	public PamGuiManagerFX getPamGuiManagerFX() {
		return this.pamGuiManagerFX;
	}

	/**
	 * Get a PamTabFX at a specified index
	 * @param i - the index of the tab
	 * @return the tab at index i. 
	 */
	public PamGuiTabFX getPamTab(int i) {
		return  (PamGuiTabFX) (this.mainTabPane.getTabs().get(i)); 
	}

	/**
	 * Get the side pane. The side pane holds extra quick access
	 * controls for modules. 
	 * @return the side pane.
	 */
	public PamVBox getSidePane() {
		return sidePaneContent;
	}
	
	/**
	 * Show the side pane. The side pane holds extra quick access
	 * controls for modules. 
	 * @param show - true to show the pane. 
	 */
	public void showSidePane(boolean show) {
		this.hidingSidePane.showHidePane(show);
	}
	
	/**
	 * Rename a tab at a selected index. 
	 * @param selectedItem - the new name
	 * @param tabIndex - the tab index
	 */
	public void renameTab(String selectedItem, int tabIndex) {
		//TODO
	}



}
