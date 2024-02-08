package pamViewFX;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.JFrame;

import org.controlsfx.glyphfont.Glyph;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;
import pamViewFX.fxStyles.PamAtlantaStyle;
import pamViewFX.fxStyles.PamStylesManagerFX;
import pamViewFX.pamTask.PamTaskUpdate;
import userDisplayFX.UserDisplayNodeFX;
import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamView.PamViewInterface;
import dataMap.layoutFX.DataMapPaneFX;
import PamController.PAMControllerGUI;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;
import dataModelFX.DataModelPaneFX;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * 
 * Handles the FX based GUI. The GUI is made up of multiple PAMGuiFX stages. The
 * PAMGuiManagerFX handles the number of these stages and starting/closing the
 * GUI.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamGuiManagerFX implements PAMControllerGUI, PamSettings {

	/**
	 * The main tab pane. This is the tab pane of the first unit and is only 
	 * referred to so that new modules know which tab pane to create a new default tab in. 
	 */
	private PamTabPane mainTabPane; 

	/**
	 * A list of open stages.
	 */
	private ArrayList<PamGuiFX> stages=new ArrayList<PamGuiFX>();

	/**
	 * The data model pane. This sits in an non closable tab. 
	 */
	private DataModelPaneFX dataModelFX;

	/**
	 * The primary stage. 
	 */
	private Stage primaryStage;

	/**
	 * Data map pane
	 */
	private DataMapPaneFX dataMapFX;

	/**
	 * The primary view. 
	 */
	private PamGuiFX primaryView;

	/**
	 * Reference to the PAMContorller
	 */
	private PamController pamController;

	/**
	 * @return the primaryView
	 */
	public PamGuiFX getPrimaryView() {
		return primaryView;
	}

	/**
	 * @param primaryView the primaryView to set
	 */
	public void setPrimaryView(PamGuiFX primaryView) {
		this.primaryView = primaryView;
	}

////	//Font sizes for titles and sub titles. 
//	public static Font titleFontSize=createTitleFont();
////
//	public static Font titleFontSize2=createTitleFont2();

	/**
	 * The default size for icons in PAMGaurd- helps keep everything looking coherent. 
	 */
	public static int iconSize=17;

	/*
	 * The default icon colour. 
	 */
	public static Color iconColor=Color.DARKGRAY;
	
	/**
	 * The general GUI settings 
	 */
	private  PAMGuiFXSettings pamGuiSettings;

	private Scene scene; 

	private static PamGuiManagerFX instance; 

	public PamGuiManagerFX(PamController pamController, Object stage) {

		this.pamController=pamController; 
		pamGuiSettings= new PAMGuiFXSettings(); 
				
		primaryStage= (Stage) stage;
		
		primaryStage.setOnCloseRequest(e->{
			pamStop(e);
		});
		
		start(primaryStage);
		
		instance=this;
				
	}
	
	/**
	 * Get the instance of the PAMGuiManager
	 * @return the instance; 
	 */
	public static PamGuiManagerFX getInstance() {
		return instance; 
	}
	
	
	

	/**
	 * Start the GUI.
	 * @param primaryStage
	 */
	private void start(Stage primaryStage) {
		
		PamStylesManagerFX.getPamStylesManagerFX().setCurStyle(new PamAtlantaStyle());

		//add stage
		stages.add(primaryView = new PamGuiFX(primaryStage, this)); 
		//create new data model. 
		dataModelFX=stages.get(0).addDataModelTab();
		
		scene = new Scene(stages.get(0));
		scene.getStylesheets().addAll(getPamCSS());

		
//		Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
		//		stages.get(0).prefWidthProperty().bind(scene.widthProperty());
		//	    stages.get(0).prefHeightProperty().bind(scene.heightProperty());

		primaryStage.setScene(scene);
		//need to add this for material design icons and fontawesome icons
//		scene.getStylesheets().addAll(GlyphsStyle.DEFAULT.getStylePath());

//		//need to nudge the displays to show controlled units. 
//		notifyModelChanged(PamController.ADD_CONTROLLEDUNIT); 
		

	}

	/**
	 * Create font for main titles 
	 * @return font for title 
	 */
	private static Font createTitleFont(){
		Font font= Font.font(null, FontWeight.BOLD, 27);
		return font;
	}

	/**
	 * Create font for sub titles.
	 * @return font for sub title
	 */
	private static Font createTitleFont2(){
		Font font= Font.font(null, FontWeight.BOLD, 14);
		return font;
	}
	
	/**
	 * Set the label style for titles
	 * @param label
	 */
	public static void titleFont1style(Labeled label) {
		label.setId("label-title1");
	}
	
	/**
	 * Set the label style for sub titles
	 * @param label - the label style. 
	 */
	public static void titleFont2style(Labeled label) {
		label.setId("label-title2");
	}

	/**
	 * Get a list of the current PamGUIFX frames available. 
	 * @return a list of the current showing PamGUIFX frames. 
	 */
	public ArrayList<PamGuiFX> getPamGuiFXList(){
		return stages;
	}

	/**
	 * Add a controlled unit to the scene. The controlled unit may have a graphical component or no graphical component at all. 
	 * @param controlledUnit - 
	 */
	@Override
	public void addControlledUnit(PamControlledUnit controlledUnit){
//		System.out.println("PAMGuiManagerFX. Add Controlled Unit: "+controlledUnit.getClass());

		//now set the content for the tab.
		if (controlledUnit.getGUI(PamGUIManager.FX)!=null){
			//if FX content then is handles as an FX Node within GUI manager. 
			PamControlledGUIFX pamControlledUnitFX=(PamControlledGUIFX) controlledUnit.getGUI(PamGUIManager.FX);
			//figure out which tab the display should be added to. 
			//        	  System.out.println("Controlled Unit: "+controlledUnit);
			//        	  System.out.println("Displays: "+pamControlledUnitFX.getDisplays());
			if (pamControlledUnitFX.getDisplays()!=null){
				for (int i=0; i<pamControlledUnitFX.getDisplays().size(); i++){
					addDisplay(pamControlledUnitFX.getDisplays().get(i));
					//call openNode. 
					pamControlledUnitFX.getDisplays().get(i).openNode();
				}
			}
			
			if (pamControlledUnitFX.getSidePanes()!=null) {
				for (int i=0; i<pamControlledUnitFX.getSidePanes().size(); i++){
					addSidePane(pamControlledUnitFX.getSidePanes().get(i));
				}
			}
		}

		//          //the controlled unit 
		//          if (controlledUnit instanceof PamControlledUnitAWT){
		//        	 //if AWT unit then need to embed swing components in an special FX node. 
		//        	  //TODO
		//          }
		//mainTabPane.getTabs().add(tab);
	}

	
	/**
	 * Add a side pane e.g. when a new module is added to the data model. 
	 * @param pane - the pane to add. 
	 */
	private void addSidePane(Pane pane) {
		stages.get(0).getSidePane().getChildren().add(pane);
		stages.get(0).showSidePane(true);
		
	}
	
	/**
	 * Remove a hiding side pane e.g. when a module is removed from
	 * the data model. 
	 * @param pane - the pane to remove. 
	 */
	private void removeSidePane(Pane pane) {
		stages.get(0).getSidePane().getChildren().remove(pane);
	}

	/**
	 * Add a controlled unit from the scene. The controlled unit may have a graphical component or no graphical component at all. 
	 * @param controlledUnit - the controlled unit to remove (here we're removing all it's displays)
	 */
	@Override
	public void removeControlledUnit(PamControlledUnit controlledUnit){
		//now set the content for the tab.
		if (controlledUnit.getGUI(PamGUIManager.FX)!=null){
			//System.out.println("PAMGuiMangerFX: Remove module (PamControlledUnit) added" +controlledUnit.getUnitName()); 
			//if FX content then is handles as an FX Node within GUI manager. 
			PamControlledGUIFX pamControlledUnitFX=(PamControlledGUIFX) controlledUnit.getGUI(PamGUIManager.FX);
			//figure out which tab the display should be added to. 
			if (pamControlledUnitFX.getDisplays()!=null){
				for (int i=0; i<pamControlledUnitFX.getDisplays().size(); i++){
					//call closeNode. 
					pamControlledUnitFX.getDisplays().get(i).closeNode();
					removeDisplay(pamControlledUnitFX.getDisplays().get(i));
				}
			}
			
			if (pamControlledUnitFX.getSidePanes()!=null) {
				for (int i=0; i<pamControlledUnitFX.getSidePanes().size(); i++){
					removeSidePane(pamControlledUnitFX.getSidePanes().get(i));
				}
			}
		}
	}
	

	/**
	 * Gte all tabs across all open windows. 
	 * @return all tabs. 
	 */
	private ArrayList<PamGuiTabFX> getAllTabs() {
		ArrayList<PamGuiTabFX> alltabs= new ArrayList<PamGuiTabFX> (); 
		for (int i=0; i<this.getPamGuiFXList().size(); i++) {
			for (int j=0; j<this.getPamGuiFXList().get(i).getNumTabs(); j++) {
				alltabs.add(this.getPamGuiFXList().get(i).getTab(j)); 
			}
		}
		return alltabs;
	}

	
	/**
	 * Get the correct tab to add a display.
	 * <p>
	 *  If initialising then searches for the correct tab in user display params.
	 *  <p>
	 *  If initialisation is complete then shows a dialog to allow a user to select the tab. 
	 * @return the tab to add the display to. 
	 */
	private PamGuiTabFX getDisplayTab(UserDisplayNodeFX newDisplay) {
		PamGuiTabFX tab;
		if (PamController.getInstance().isInitializationComplete()){
			//ask the user what tab to add the display to 
			tab=this.showTabSelectDialog();
			return tab; 
		}
		else {
			if (newDisplay.getDisplayParams().tabName!=null) {
				//add the display to the current set of tabs. 
				ArrayList<PamGuiTabFX> allTabs = getAllTabs(); 
				for (int i=0; i<allTabs.size(); i++) {
					if (allTabs.get(i).getText().equals(newDisplay.getDisplayParams().tabName)){
						return allTabs.get(i); 
					}
				}
			}
			
			//if loop has completed then no tab has found. This should not ordinarily happen...
			System.out.println("PamGuiManagerFX: No tab was found for the user display on start up?");
			String tabName; 
			if (newDisplay.getDisplayParams().tabName!=null) tabName=newDisplay.getDisplayParams().tabName; 
			else tabName=newDisplay.getName(); 
				
			tab = stages.get(0).addPamTab(new TabInfo(tabName), newDisplay, true);
			return tab; 
		}
	}
	
	/**
	 * Add a new display to PAMGUARD. 
	 * @param newDisplay - the new Display to add. 
	 */
	private void addDisplay(UserDisplayNodeFX newDisplay){
		PamGuiTabFX tab;
		
		if (!newDisplay.isStaticDisplay()){
			//if a non static display then add to a selected tab. 
			tab=getDisplayTab(newDisplay); 
			tab.addInternalPane(newDisplay);
		}
		else {
			//if a static display create a new tab- name the static display
			tab=stages.get(0).addPamTab(new TabInfo(newDisplay.getName()), newDisplay, true);
		}

		//the tab. 
		if (tab.getInternalPanes().size()>0) {
			PamInternalPane internalFrame =
					tab.getInternalPanes().get(tab.getInternalPanes().size()-1);

//			System.out.println("PAMGUIManagerFX: Adding a new display " 
//					+ newDisplay.getDisplayParams().positionX + "  "
//					+ newDisplay.getDisplayParams().positionY + "  " +
//					+ newDisplay.getDisplayParams().sizeX + "  "
//					+ newDisplay.getDisplayParams().sizeY);

			newDisplay.getDisplayParams().tabName=tab.getName(); 
			newDisplay.setFrameHolder(internalFrame); 

			//Have to platform later this as needs some time to intitialise? 11/08/2018
			Platform.runLater(()->{
				internalFrame.setPaneLayout(newDisplay.getDisplayParams().positionX, newDisplay.getDisplayParams().positionY); 
				if (newDisplay.getDisplayParams().sizeX>0) {
					internalFrame.setPaneSize(newDisplay.getDisplayParams().sizeX, newDisplay.getDisplayParams().sizeY); //this meant display was too small? 
				}
			});
		}
		else {
			
		}
	}


	private void removeDisplay(UserDisplayNodeFX newDisplay){
		//remove from any PamGuiFX
		for (int i=0; i<stages.size() ; i++){
			stages.get(i).removeInternalPane(newDisplay);
		}
	}


	/**
	 * Get all tabs from all stages. 
	 * @param excludestatic displays
	 * @return all tabs from all stages. 
	 */
	private ArrayList<PamGuiTabFX> getTabs(boolean excludeStatic){
		//first create list of tabs. 
		ArrayList<PamGuiTabFX> allTabs=new ArrayList<PamGuiTabFX>();

		ArrayList<PamGuiTabFX> tabs;
		for (int i=0; i<stages.size(); i++){
			tabs=stages.get(i).getTabs();
			for (int j=0; j<tabs.size(); j++){
				if (!tabs.get(j).isStaticDisplay() || excludeStatic){
					allTabs.add(tabs.get(j));
				}
				//System.out.println(" Tab name: "+tabs.get(j).getLabel().getText());
			}
		}

		return allTabs; 
	}

	/**
	 * Show a dialog to allow users to select which tab display should be added to. 
	 * @return the tab number to add display to. 
	 */
	private PamGuiTabFX showTabSelectDialog(){

		//first create list of tabs. 
		ArrayList<PamGuiTabFX> allTabs=getTabs(true);

		int tabIndex=0; 

		//no tab available, so add a new tab.
		if (allTabs.size()<=1){
			return stages.get(0).addPamTab(new TabInfo("Display 1"), null, true);
		}
		else{

			TabSelectionPane tabSelectionPane=new TabSelectionPane(stages.get(0));

			PamSettingsDialogFX<?> settingsDialog=new PamSettingsDialogFX(tabSelectionPane); 
			settingsDialog.getDialogPane().getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS());
			settingsDialog.initStyle(StageStyle.UNDECORATED);

			//			ChoiceDialog<String> dialog = new ChoiceDialog<>(tabStrings.get(1), tabStrings);
			//			dialog.setTitle("Select Tab");
			//			dialog.setContentText("Select tab to add display to");

			//Get response
			Optional<PamGuiTabFX> result = (Optional<PamGuiTabFX>) settingsDialog.showAndWait();
			allTabs=getTabs(true);

			//can create 
			if (result.isPresent()){
				tabIndex=allTabs.indexOf(result.get());
			}
			else return null;
		}

		//now create
		return allTabs.get(tabIndex); 
	}


	/**
	 * Get CSS for PAMGUARD setting 'look and feel' for sliding dialogs
	 * @return the CSS for settings feels. 
	 */
	public ArrayList<String> getPamSettingsCSS() {//return new PrimerDark().getUserAgentStylesheet();
		//		return getClass().getResource("/Resources/css/pamSettingsCSS.css").toExternalForm();
		return PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS();
	}

	/**
	 * Get CSS for PAMGUARD GUI standard 'look and feel'
	 * @return the standard CSS fro PAMGUARD. 
	 */
	public ArrayList<String> getPamCSS() {
		//return new PrimerLight().getUserAgentStylesheet();
		//		return getClass().getResource("/Resources/css/pamCSS.css").toExternalForm();
		return PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS();
	}

	/**
	 * Get CSS for PAMGUARD GUI standard 'look and feel' for regular dialogs
	 * @return the standard CSS fro PAMGUARD. 
	 */
	public ArrayList<String> getPamDialogCSS() {//return new PrimerDark().getUserAgentStylesheet();

		return PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getDialogCSS();
	}


	/**
	 * Set the GUI to show PAMGUARD has started or stopped. 
	 */
	public void setPamRunning(boolean running){
		Glyph graphic;
		for (int i=0; i<this.stages.size(); i++){
			ArrayList<Button> recordButtons=stages.get(i).getRecordButtons();
			for (int j=0; j<recordButtons.size(); j++){
				if (running){
					graphic=Glyph.create("FontAwesome|SQUARE").size(22).color(Color.BLACK);
				}
				else {
					graphic=Glyph.create("FontAwesome|CIRCLE").size(22).color(Color.LIMEGREEN);
				}
				//now set all run buttons to correct format. 
				recordButtons.get(j).setGraphic(graphic);

			}
		}
	}

	public void pamStop(WindowEvent e) {
		if (this.prepareToClose(true)) { 
			Platform.exit();
			System.exit(0);
		}
		else e.consume();//dont close. 

	}

	@Override
	public void notifyModelChanged(int changeType) {
		// pass to data model
		if (dataModelFX!=null) {
			dataModelFX.notifyModelChanged(changeType);
		}
	}

	public void addView(PamViewInterface newView) {
		// TODO Auto-generated method stub
	}


	public void showControlledUnit(PamControlledUnit unit) {
		// TODO Auto-generated method stub
	}

	public void destroyModel() {
		// TODO Auto-generated method stub

	}


	public void enableGUIControl(boolean enable) {
		// TODO Auto-generated method stub

	}


	public Stage getMainScene() {
		return primaryStage;
	}


	public void sortFrameTitles() {
		// TODO Auto-generated method stub

	}


	public void getAllFrameParameters() {
		// TODO Auto-generated method stub

	}


	/**
	 * Get the number of stages 
	 * @return the number of stages (windows) currently open. 
	 */
	public int getNumFrames() {
		return stages.size();
	}

	/**
	 * Get a stage.
	 * @param i - frame number
	 * @return the stage
	 */
	public PamGuiFX getFrame(int i) {
		return stages.get(i);
	}

	/**
	 * Create a dialog to name a new module. 
	 * @param moduleInfo
	 * @return the new name of the module. 
	 */
	public String getModuleName(PamModuleInfo moduleInfo) {

		Dialog<String> dialog = new Dialog<String>();
		dialog.initStyle(StageStyle.UNDECORATED);
		dialog.setHeaderText(null);
		dialog.setTitle("Name controlled Unit");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		TextField nameField = new TextField();
		nameField.setPromptText("Module Name");
		nameField.setPrefColumnCount(20);
		//now need to set name which hasn't been set before- add numbers to end of default name;
		String name=moduleInfo.getDefaultName();
		boolean hasName=PamController.getInstance().isControlledUnit(name); 
		int n=2;
		while (hasName){
			name=(moduleInfo.getDefaultName()+" "+n);
			hasName=PamController.getInstance().isControlledUnit(name); 
			n++;
		}
		nameField.setText(name);

		PamHBox namePane=new PamHBox();
		//namePane.setPadding(new Insets(5)); 
		namePane.setSpacing(5); 
		namePane.setAlignment(Pos.CENTER_RIGHT);
		namePane.getChildren().addAll(new Label("Module name"), nameField);

		PamVBox mainPane=new PamVBox();
		mainPane.setSpacing(5);
		mainPane.setPadding(new Insets(5,5,5,5));
		Label title=new Label("Name New Module");
		titleFont2style(title); 
//		title.setFont(titleFontSize2);
		mainPane.getChildren().addAll(title, namePane);

		dialog.getDialogPane().setContent(mainPane);
		dialog.getDialogPane().getStylesheets().addAll(this.getPamDialogCSS());

		//add listener to prevent close request if the dialog
		final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		//System.out.println("btOK "+btOk);
		if (btOk!=null) btOk.addEventFilter(ActionEvent.ACTION, event -> {
			boolean checkName=PamController.getInstance().isControlledUnit(nameField.getText()); 
			if (checkName){
				PamDialogFX.showWarning(null, "Invalid Module Name", "Another module has this name. Modules must have different names. Please choose another name");
				event.consume();
			}
		});

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK) {
				return nameField.getText();
			}
			return null;
		});

		// open dialog. 
		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()){
			System.out.println("Your name: " + result.get());
			return result.get();
		}
		else return null;

	}

	/**
	 * Switch to the tab which contains a certain display within it's content area. 
	 * @param userDisplayNodeFX - the display to show. 
	 */
	public void switchToTab(UserDisplayNodeFX userDisplayNodeFX) {
		//System.out.println("Switch to tab: "+userDisplayNodeFX.getName());
		/**
		 * Need to iterate through each open window, then each tab, then all internal panes within each tab. Phew. 
		 */
		PamInternalPane internalPane;
		for (int i=0; i<this.getNumFrames(); i++){
			for (int j=0; j<this.getFrame(i).getNumTabs(); j++){
				//if a static display will be center of tab
				if (this.getFrame(i).getTab(j).getContentHolder().getCenter()==userDisplayNodeFX.getNode()){
					this.getFrame(i).selectTab(j);
					return;
				}
				//otherwise look through internal frames.
				for (int k=0; k<this.getFrame(i).getTab(j).getInternalPanes().size(); k++){
					internalPane=this.getFrame(i).getTab(j).getInternalPanes().get(k);
					//check if internal pane is the node. 
					if (internalPane.getInternalRegion()==userDisplayNodeFX.getNode()){
						this.getFrame(i).selectTab(j);
						return;
					}
					//also, might be embedded as a child of the internal pane. 
					if (internalPane.getInternalRegion().getChildrenUnmodifiable().indexOf(userDisplayNodeFX.getNode())>=0){
						this.getFrame(i).selectTab(j);
						return; 
					}
				}
			}
		}
	}


	/***
	 * Get the data model pane. 
	 * @return the data model pane. 
	 */
	public DataModelPaneFX getDataModelFX() {
		return dataModelFX;
	}

	/**
	 * Show GUI in load mode. SHows load bars etc. 
	 * @param loadMode- shoe the GUI in loadMode;
	 */
	public void showLoadMode(boolean loadMode) {
		for (int i=0; i<this.stages.size(); i++){
			stages.get(i).showLoadMode(loadMode); 
		}

	}

	/**
	 * Notify the GUI that progress has been made in an external task. 
	 * @param pamTaskUpdate - class which holds information on the Task and progress. 
	 */
	public void notifyLoadProgress(PamTaskUpdate pamTaskUpdate) {
		for (int i=0; i<this.stages.size(); i++){
			stages.get(i).notifyLoadProgress(pamTaskUpdate); 
		}	
	}

	@Override
	public PamSettings getInitialSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		
		
		
		
//		System.out.println("Initialising FX Toolbox");
//		//start the application		
//		new Thread(() -> {
//			Application.launch(PamguardFXApplication.class); 
//		}).start();
//		// wait for toolkit to start:
//		try {
//			PamguardFXApplication.awaitFXToolkit();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//		System.out.println("FX Toolbox has been initialised: "+ PamguardFXApplication.getPrimaryStage());

//		//set the main stage.
//		this.primaryStage=PamguardFXApplication.getPrimaryStage();
	}

	@Override
	public boolean hasCallBack() {
		// the FX application calls the rest of the GUI. 
		return true;
	}

	@Override
	public PamViewInterface initPrimaryView(PamController pamController, PamModel pamModel) {
		//start everythinG

		//now restore the settings for the display - this has to happen here as too early everywhere else. 
		PamSettingManager.getInstance().registerSettings(this);
		primaryStage.setFullScreen(pamGuiSettings.fullscreen);
		primaryStage.setWidth(pamGuiSettings.width);
		primaryStage.setHeight(pamGuiSettings.height);

		primaryStage.show();
		return primaryView;
	}

	public Stage setPrimaryStage(Stage primaryStage2) {
		return this.primaryStage=primaryStage2;
	}

	@Override
	public String getModuleName(Object parentFrame, PamModuleInfo moduleInfo) {
		return 	getModuleName(moduleInfo); 
	}

	/**
	 * Show the alert dialog. 
	 * @param strheader
	 * @param confirm
	 * @return
	 */
	public static ButtonType showAlertDialog(String strheader, String confirm) {
		//create alert dialog. 
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText(strheader);
		alert.setContentText(confirm);

		Optional<ButtonType> result = alert.showAndWait();
		return result.get();
	}

	class BombThread implements Runnable {

		volatile boolean waitingStop = true;
		@Override
		public void run() {
			pamController.pamStop();
			waitingStop = false;
		}

	}

	/**
	 * 
	 * This get's called whenever the main window closes - 
	 * ideally, I'd like to stop this happening when the system
	 * is running, but since that's not possible, just make sure
	 * everything has stopped.
	 * 
	 * @param weShouldSave true if the settings should get saved to the psf file, false if otherwise
	 * @return true if ok to close, false otherwise. 
	 */
	public boolean prepareToClose(boolean weShouldSave) {
		//		System.out.println("Preparing to close PAMGUARD");

		if (pamController.canClose() == false) {
			return false;
		}

		int pamStatus = pamController.getPamStatus();
		if (pamStatus != PamController.PAM_IDLE) {
			boolean ans = PamDialogFX.showMessageDialog(this.getMainScene().getOwner(),  
					"PAMGUARD stop", "Are you sure you want to stop and exit. " +
							"PAMGUARD is busy",
							ButtonType.YES,
							ButtonType.CANCEL, AlertType.CONFIRMATION);
			if (!ans) {
				return false;
			}
		}

		/*
		 * Sometimes have trouble stopping in which case we're 
		 * going to have to bomb. If it can't stop in 5s, then die ! 
		 */
		BombThread bombThread = new BombThread();
		Thread t = new Thread(bombThread);
		t.start();
		for (int i = 6; i >= 0 && bombThread.waitingStop; i--) {
			for (int j = 0; j<10 && bombThread.waitingStop; j++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			if (!bombThread.waitingStop) break;
			System.out.println(String.format("Stopping PAMGuard. Will force exit in %d seconds", i));
		}

		//save the viewer data in viewer mode. 
		if (pamController.getRunMode() == PamController.RUN_PAMVIEW) {
			pamController.saveViewerData();
		}

//		// deal with anything that needs sorting out in the realm of UID's.
		// move this to pamController.pamClose()
//		pamController.getUidManager().runShutDownOps();

		// if the user doesn't want to save the config file, make sure they know
		// that they'll lose any changes to the settings
		if (!weShouldSave) {
			boolean ans = PamDialogFX.showMessageDialog(this.getMainScene().getOwner(),  
					"<html><body><p style='width: 300px;'>Are you sure you want to exit without saving your current configuration?  "
					+ "Any changes that have been made to the current configuration will be lost</p></body></html>",
					"Exit without Save",
					ButtonType.YES,
					ButtonType.CANCEL, AlertType.CONFIRMATION);
			if (!ans) {	// Hitting Cancel returns a No value
				return false;
			}
		}

		// finally save all settings just before PAMGUARD closes. 
		if (weShouldSave) {
			PamSettingManager.getInstance().saveFinalSettings();
		}

		pamController.pamClose();

		return true;
	}


	public boolean getShowLoadMode() {
		// TODO Auto-generated method stub
		return this.getFrame(0).isShowLoadMode(); 
	}

	@Override
	public void pamStarted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamEnded() {
		// TODO Auto-generated method stub
		//pamstop. 
	}

	@Override
	public void modelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFrameNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JFrame getGuiFrame() {
		// TODO Auto-generated method stub
		return null;
	}

	/***Settings Manager***/
	
	@Override
	public String getUnitName() {
		return "GUI_Manager_FX"; //<-only ever one GUIManagerFX
	}

	@Override
	public String getUnitType() {
		return "GUIManagerFX";
	}

	@Override
	public Serializable getSettingsReference() {
		Serializable set = prepareSerialisedSettings();
		return set;
	}

	@Override
	public long getSettingsVersion() {
		return PAMGuiFXSettings.serialVersionUID;
	}
	
	private Serializable prepareSerialisedSettings(){
		ArrayList<TabInfo> tabInfos = new ArrayList<TabInfo>();
		for (int i=0; i<this.getPamGuiFXList().size(); i++) {
			for (int j=0; j<this.getPamGuiFXList().get(i).getNumTabs(); j++) {
				tabInfos.add(this.getPamGuiFXList().get(i).getTab(i).getTabInfo()); 
			}
		}
		pamGuiSettings.tabInfos=tabInfos; 
		pamGuiSettings.width = primaryStage.getWidth();
		pamGuiSettings.height = primaryStage.getHeight();
		pamGuiSettings.fullscreen = primaryStage.isFullScreen();

		return pamGuiSettings;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings settings) {
//		System.out.println("Settings: "+settings);
		if (settings == null) {
			return false;
		}
		PAMGuiFXSettings pamGuiSettings = ((PAMGuiFXSettings) settings.getSettings()); 
		this.pamGuiSettings=pamGuiSettings.clone(); 
		this.setParams(pamGuiSettings); 
//		tdParams.scrollableTimeRange=300000L;
//		System.out.println("Settings: "+settings.graphParameters.size());
		return true;
	}

	/**
	 * Set gui params. This should only be set during start up. 
	 * @param pamGuiSettings - the GUI parameters. 
	 */
	private void setParams(PAMGuiFXSettings pamGuiSettings2) {
		//set all the correct tabs. Do not want to replace tabs that already exist here so

		//TODO
		
	}

	public Window getPrimaryStage() {
		return this.primaryStage;
	}



}
