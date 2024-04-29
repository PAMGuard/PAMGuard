package dataModelFX.connectionNodes;

import java.util.ArrayList;

import pamViewFX.PamControlledGUIFX;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectorNode;
import pamViewFX.fxNodes.hidingPane.HidingPane;
import pamViewFX.fxNodes.utilityPanes.SettingsDialog;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamguardMVC.PamDataBlock;
import dataModelFX.DataModelConnectPane;
import dataModelFX.DataModelStyle;
import dataModelFX.ModuleNodeParams;
import dataModelFX.ConnectionNodeParams;

/**
 * A connection node which represents a PAMGuard PamControlledUnit. 
 * 
 * @author Jamie Macaulay
 */
@SuppressWarnings("unused")
public class ModuleConnectionNode extends StandardConnectionNode implements PAMConnectionNode {
	
	/**
	 * The pamControlledUnit which the ModuleConnectionNode represents. 
	 */
	private PamControlledUnit pamControlledUnit;

	/**
	 * Holds a,list of processes and data blocks along with info on number of data units and processor being used. 
	 */
	private PamBorderPane lowerHidingBox;
	
	/**
	 * The last status update
	 */
	private int lastConnectionStatus = ConnectorNode.NO_CONNECTION; 
	
	/**
	 * Background colour of the module. 
	 */
	private Color moduleBackground=new Color(0.65,0.65,0.65,1);
	
	/**
	 * True of a 'special' module. These are modules which have no connection e.g. array manager, database and binary storage. 
	 * Core modules show semi-transparent lines to all other modules when dragged. For core modules which might be a bit more specific then
	 * the type of module which it connects to can be defined by setting 
	 */
	private boolean core=false;

	/**
	 * If core output then lines connect to socket area. Otherwise if false connect to plug area. 
	 */
	private boolean coreOutput=true; 
	
	/**
	 *The colour of an core lines. 
	 */
	private Color coreLineColour=new Color(0.117,0.5647,1,0.4);

	/**
	 * Reference to the data model pane.
	 */
	private DataModelConnectPane connectionPane;
	
	/**
	 * Keeps a copy of current core lines. 
	 */
	private ArrayList<Line> coreLines=new ArrayList<Line>();


	/**
	 * Shown on core modules. 
	 */
	private Circle coreCircle;
	
	/**
	 * The colour of the connection circle in a core module; 
	 */
	private Color coreCircleColour=Color.LIMEGREEN;

	/**
	 * True if the moduleNode automatically attempts to connect to a parent when added to the ConnectionPane. 
	 */
	private boolean attemptAutoConnect=true;
	
	/**
	 * Enable connection listeners. 
	 */
	private boolean enableConnectListeners=true;

	/**
	 * Node which shows the data model within the controlled unit the ModuleConnectionNode represents.
	 */
	private ModuleProcessDiagram controlDataModelNode;

	/**
	 * Top hiding pane. Holds settings and remove module buttons.
	 */
	private ModuleHidePane topHidingPane;

	/*
	 * The default size of the process and datablock graph
	 */
	private static double widthExpansion=1000;
	
	/**
	 * The current module node params
	 */
	private ModuleNodeParams moduleNodeParams; 

	/**
	 * Create a module node. 
	 * @param connectionPane - the connection pane the module node sits in. 
	 * @param core - true if a special module. Special modules are modules which connect to all or none of the other 
	 * modules. 
	 */
	public ModuleConnectionNode(DataModelConnectPane connectionPane, boolean core) {
		super(connectionPane);		
		
		this.connectionPane=connectionPane;
		this.core=core;
		initModuleNode();
		
		if (core){
			setCore(core);
		}
		
		setAllowBranchSockets(false);
	}

	/**
	 * Create a module node. 
	 * @param connectionPane - the connection pane the module node sits in. 
	 */
	public ModuleConnectionNode(DataModelConnectPane connectionPane) {
		super(connectionPane);
		this.connectionPane=connectionPane;
		initModuleNode();
		this.setAllowBranchSockets(false);
	}
	
	/**
	 * Create the module. 
	 */
	private void initModuleNode(){
				
		//create the top hiding pane
		this.getChildren().add(0,topHidingPane=createTopControls()); 
		
		//create a lower hiding pane. 
		this.getChildren().add(0,createBottomControls()); 
		
		//do not allow branch sockets by default. 
		this.setAllowBranchSockets(false); 
		
		this.getConnectionNodeBody().setPrefSize(connectionPane.getModuleWidth(), connectionPane.getModuleHeight());	
		this.getConnectionNodeBody().setBackground(new Background(new BackgroundFill(moduleBackground,CornerRadii.EMPTY,Insets.EMPTY)));		
		
		//add connection listener 
		this.addConnectionListener((shape, foundShape, type)->{
			connectionListenerTriggered(shape, foundShape, type);
		});

	}
	
	/**
	 * Called whenever a connection node is connected or disconnected.
	 * @param shape - the shape used to connect
	 * @param foundShape - the shape whihc may be able to connect to the shape
	 * @param type - the type of connection. 
	 */
	protected void connectionListenerTriggered(ConnectorNode shape, ConnectorNode foundShape, int type){
		
		//System.out.println(" ModuleConnectionNode. listener triggered " + this.getPamControlledUnit()+ " "+type ); 		
		
		if (lastConnectionStatus==ConnectorNode.NO_CONNECTION && type==ConnectorNode.NO_CONNECTION) return;
		switch(type){
		case ConnectorNode.NO_CONNECTION:
			//System.out.println("ModuleConnectionNode. NO_CONNECTION " +this.getPamControlledUnit()+" "+shape+"  " +foundShape ); 
			if (enableConnectListeners) connectionPane.pamModeltoDataModel();
			break; 
		case ConnectorNode.POSSIBLE_CONNECTION:
			//System.out.println(" ModuleConnectionNode. POSSIBLE_CONNECTION " +shape+"  " +foundShape ); 
			//if (enableConnectListeners) connectionPane.pamModeltoDataModel();
			break; 
		case ConnectorNode.CONNECTED:
			//System.out.println("ModuleConnectionNode. CONNECTED " + this.getPamControlledUnit().getUnitName()+ " "+shape+"  " +foundShape ); 
			if (enableConnectListeners)  connectionPane.pamModeltoDataModel();
			break; 
		}
		lastConnectionStatus=type; 
	}
	

	/**
	 * Create top hiding pane. Contains buttons for settings and deleting module from PAM model
	 * @return top hiding pane. 
	 */
	private ModuleHidePane createTopControls(){
		
		PamButton settingsButton=new PamButton(); 
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, Color.WHITE, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog", Color.WHITE, PamGuiManagerFX.iconSize));
		settingsButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		settingsButton.getStyleClass().add("square-button");
		settingsButton.setOnAction((change)->{
			//createModuleDialog();
			showModuleSettings();
		});

		PamButton removeButton=new PamButton(); 
//		removeButton.setGraphic(PamGlyphDude.createPamGlyph(FontAwesomeIcon.REMOVE, Color.WHITE, PamGuiManagerFX.iconSize));
		removeButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-minus", Color.WHITE, PamGuiManagerFX.iconSize));
		removeButton.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
		removeButton.setOnAction((change)->{
			if (this.pamControlledUnit==null || this.pamControlledUnit.getPamModuleInfo()== null || this.pamControlledUnit.getPamModuleInfo().canRemove()){
				connectionPane.removeModuleNode(this);
			}
		});
		removeButton.getStyleClass().add("square-button");

		PamHBox topBox=new PamHBox(); 
		topBox.getChildren().addAll(settingsButton, removeButton); 
		topBox.prefWidthProperty().bind(this.getConnectionNodeBody().widthProperty());
		HBox.setHgrow(settingsButton, Priority.ALWAYS);
		HBox.setHgrow(removeButton, Priority.ALWAYS);
		//topBox.setPrefHeight(25);
		
		ModuleHidePane hidingPane=new ModuleHidePane(topBox,this.getConnectionNodeBody(), true) ;
		//hidingPane.layoutYProperty().set(-20);
		//make same width as hiding pane; 
		hidingPane.prefWidthProperty().set(getConnectionNodeBody().getPrefWidth());
		
		//FIXME- binding seems to introduce weird effect here so gone for quick fix; 
		//hidingPane.prefWidthProperty().bind(getConnectionRectangle().widthProperty());
		hidingPane.layoutXProperty().bind(this.getConnectionNodeBody().layoutXProperty());
		hidingPane.layoutYProperty().bind(this.getConnectionNodeBody().layoutYProperty().subtract(10));
		
		return hidingPane;
	}

	/**
	 * Bring up settings for a module. This is slightly complicated by the fact that there are potentially two ways
	 * settings can be shown. The first is that a controlled unit may have a dedicated settings pane in which case the 
	 * settings are shown as a dialog. However, a controlled unit may also be essentially a display. In this case 
	 * it is likely that the display itself will show settings, e.g. in a hide pane.
	 * <p>
	 * The function therefore does two things. It tries to find a settings pane and show as dialog. If no settings pane exists 
	 * then all displays in the controlled unit are asked for a settings pane. If a settings pane is returned which is not the 
	 */
	private void showModuleSettings(){
		PamControlledGUIFX pamControlledGUI=(PamControlledGUIFX) this.pamControlledUnit.getGUI(PamGUIManager.FX);
		
		if (pamControlledGUI==null) {
			System.err.println("PamControlledUnit: " +pamControlledUnit.getUnitType() + " does not have an FX GUI?");
			return;
		}

		//does the module have a dedicated settings pane? If so, show that. 
		if (pamControlledGUI.getSettingsPane()!=null){
			createModuleDialog(); 
			return;
		}
		
		// does the module have a display which can show settings? If so switch to tab and show that.  
		for (int i=0; i<pamControlledGUI.getDisplays().size(); i++){
			if (pamControlledGUI.getDisplays().get(i).requestNodeSettingsPane()){
				//requestNodeSettingsPane() will have sorted out the display to show settings so now 
				//just need to to switch to display tab. 
				PamController.getInstance().getGuiManagerFX().switchToTab(pamControlledGUI.getDisplays().get(i));
				return;
			}
		} 
	}
	
	/**
	 * Create the module dialog from pane. 
	 */
	private void createModuleDialog() {
		
		PamControlledGUIFX pamControlledGUI=(PamControlledGUIFX) this.pamControlledUnit.getGUI(PamGUIManager.FX);
		
		if (pamControlledGUI.getSettingsPane()==null) return; 
		//Note- set parameters is called by settings pane. 
		SettingsDialog<?> newDialog=new SettingsDialog<>(pamControlledGUI.getSettingsPane());
		newDialog.setResizable(true);
		newDialog.setOnShown((value)->{
			 pamControlledGUI.getSettingsPane().paneInitialized();
		});
		
		//show the dialog 
		newDialog.showAndWait().ifPresent(response -> {
			if (response!=null) pamControlledGUI.updateParams(); 
		});
		
		//notify stuff that process settings may have changed. 
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
	}

	/**
	 * Create the bottom controls- a list of data blocks and processes associated with the 
	 * @return  bottom hiding pane.
	 */
	private Pane createBottomControls(){
		
		lowerHidingBox=new PamBorderPane(); 		
		this.setStyle("-fx-background-color: -fx-darkbackground");
		//bottomBox.minWidthProperty().bind(getConnectionRectangle().widthProperty());
		
		ModuleHidePane hidingPane=new ControlDataModelHidePane(lowerHidingBox,this.getConnectionNodeBody(), false) ;
		//hidingPane.layoutYProperty().set(-20);
		//make same width as hiding pane; 
		hidingPane.prefWidthProperty().set(getConnectionNodeBody().getPrefWidth());
		
		//FIXME- binding seems to introduce weird effect here so gone for quick fix; 
		//hidingPane.prefWidthProperty().bind(getConnectionRectangle().widthProperty());
		hidingPane.layoutXProperty().bind(this.getConnectionNodeBody().layoutXProperty());
		hidingPane.layoutYProperty().bind(this.getConnectionNodeBody().layoutYProperty().
				add(this.getConnectionNodeBody().heightProperty()).subtract(lowerHidingBox.heightProperty()));
		hidingPane.setWidthExpansion(widthExpansion);
		
		return hidingPane; 
	}
	
	/**
	 * Populate process list. 
	 */
	private void populateProcessList(){
		controlDataModelNode =new ModuleProcessDiagram(pamControlledUnit);
		lowerHidingBox.setCenter(controlDataModelNode); 
		StackPane.setAlignment(lowerHidingBox, Pos.CENTER);
		controlDataModelNode.populateDataModel();
	}
	
	/**
	 * Create a pane which hides or shows on button press. 
	 * Could use HidingPane class here but that class is really designed for nice snug square panes 
	 * (not free floating squares on a large canvas) and is already  very complicated. Better here to 
	 * make a specific class to deal with this specific and unusual case. 
	 * @author Jamie Macaulay
	 *
	 */
	private class ModuleHidePane extends PamBorderPane {
		
		/**
		 * True if the pane is hiding
		 */
		boolean hiding=true;
		
		/**
		 * The pane to hide
		 */
		private Pane hidePane;
		
		/**
		 * The pane to hide behind
		 */
		private Pane holderPane;
		
		/**
		 * True if the pane is on the top of the Module, flase if below. 
		 */
		private boolean top;

		/**
		 *Button which has listeners and graphics to show/hide pane; 
		 */
		private PamButton showButton; 
		
		/**
		 * Animation to show the pane;
		 */
		private Timeline showAnimation;

		/**
		 * Animation to hide the pane. 
		 */
		private Timeline hideAnimation;
		
		/**
		 * Animation to increase width of pane; 
		 */
		private Timeline widthExpandAnimation;
		
		/**
		 * Animation to decrease width of pane; 
		 */
		private Timeline widthContractAnimation;

		/**
		 * Width to expand to upon showing; 
		 */
		private double widthExpSize=-1; 


		ModuleHidePane(Pane hidePane, Pane HolderPane, boolean top){
			this.hidePane =hidePane;
			this.holderPane = HolderPane; 
			this.top= top; 

			this.getStylesheets().addAll(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());

			showButton=new PamButton(); 
			showButton.setMaxWidth(Double.MAX_VALUE);
			showButton.setPrefHeight(10);
			showButton.setMinHeight(10);
			if (top) showButton.getStyleClass().add("module-hide-top");
			else showButton.getStyleClass().add("module-hide-bottom"); 

			showButton.setOnAction((action)->{
				showPane(hiding); 
			});

			if (top) this.setTop(showButton);
			else  this.setBottom(showButton);
			this.setCenter(hidePane);

			//create show and hide animations
			//setupAnimations(); 

			showPane(false);
		}
		
		/**
		 * Get the show button, has listeners built in to show/hide pane and change graphics. 
		 * @return
		 */
		public PamButton getShowButton() {
			return showButton;
		}
		
		/**
		 * Get the pane show animation
		 * @return the timeline for showing the pane. 
		 */
		public Timeline getShowAnimation() {
			return showAnimation;
		}
		
		/**
		 * Set the size to expand the pane width upon showing 
		 * @param width to expand to in pixels. 
		 */
		public void setWidthExpansion(double widthExpSize) {
			this.widthExpSize=widthExpSize;
		}
		

		/**
		 * Show or hide the pane; 
		 * @param show -true to show the pane and false to hide the pane; 
		 */
		public void showPane(boolean show){
			this.layoutXProperty().unbind(); 
			if (show){
				//System.out.println("show the pane "+this.translateYProperty().get()+" "+showAnimation); 
				hidePane.setVisible(true);
				hidePane.layout();

				//need to setup animations in case sizes have changed 
				setupShowAnimations();
				//need to un-bind in case the pane expands.
				showAnimation.play();
			}
			else{
				//need to setup animations incase sizes have changed 
				setupHideAnimations();
				//System.out.println("hide the pane "+ this.translateYProperty().get()+" "+hideAnimation); 
				if (widthContractAnimation!=null) widthContractAnimation.play(); 
				else hideAnimation.play();
			}
			hiding=!show; 
		}
		
		/**
		 * Setup animations to show the hiding pane 
		 */
		private void setupShowAnimations(){
			
			//create animation to move in y direction
			if (hidePane.heightProperty().get()>0) showAnimation=HidingPane.createAnimation(this.translateYProperty(),(top ? -1:1)*hidePane.heightProperty().get(), 100);
			else showAnimation=HidingPane.createAnimation(this.translateYProperty(),(top ? -1:1)*hidePane.getPrefHeight(), 100);
			
			//create animations to expand in x direction
			if (widthExpSize>0){
				
				//double layoutXShift= (widthExpSize-getConnectionNodeShape().getWidth())/2;
					
				widthExpandAnimation=HidingPane.createAnimation(this.prefWidthProperty(), hidePane.getPrefWidth(), 100);

//				final KeyValue kvWidthEx = new KeyValue(this.layoutXProperty(), getConnectionRectangle().layoutXProperty().get()-layoutXShift);
//				final KeyFrame kfWidthEX = new KeyFrame(Duration.millis(100), kvWidthEx);
//				widthExpandAnimation.getKeyFrames().add(kfWidthEX);
				
				//if bottom then make wider with another animation. 
				showAnimation.setOnFinished((event)->{
					widthExpandAnimation.play(); 
				});
				
				widthExpandAnimation.setOnFinished((event)->{
					this.layoutXProperty().bind(getConnectionNodeBody().layoutXProperty());//.subtract(layoutXShift));
					System.out.println("width property: "+hidePane.getWidth() );
				});
			}
			
			else {
				//if bottom then make wider with another animation. 
				showAnimation.setOnFinished((event)->{
					this.layoutXProperty().bind(getConnectionNodeBody().layoutXProperty());
				});
			}

		}
		
		
		/**
		 * Setup animations to hide the hiding pane 
		 */
		private void setupHideAnimations(){

			hideAnimation=HidingPane.createAnimation(this.translateYProperty(), 0, 100);
			
			//create animations to expand in x direction
			if (widthExpSize>0){
				//////////
				/**
				 * Bit of a bodge here. Before a pane is shown it's impossible to calc it's width especially when it
				 * comes to screens which have drastically different DPI and scaling has been applied by the OS and/or Java (as far
				 * as I can manage anyway). So you need to show a pane first, let JavaFX compute the sensible width and then 
				 * set that as the pref. width. THe show animation begins by setting pref width to -1- this allows the pane to layout to
				 * a sensible size. We then take this for the first hide animation and use that as the size of the pane
				 * from then on. Messy but there does not seem to be another way.
				 */
				this.prefWidthProperty().set(hidePane.getWidth());
				hidePane.prefWidthProperty().set(hidePane.getWidth());
				/////////
				
				widthContractAnimation=HidingPane.createAnimation(this.prefWidthProperty(),100, 100);
//				final KeyValue kvWidthCon = new KeyValue(this.layoutXProperty(), getConnectionRectangle().layoutXProperty().get());
//				final KeyFrame kfWidthCon = new KeyFrame(Duration.millis(100), kvWidthCon);
//				widthContractAnimation.getKeyFrames().add(kfWidthCon);
				
				widthContractAnimation.setOnFinished((event)->{
					hideAnimation.play(); 
				});
				
			}
	
			hideAnimation.setOnFinished((event)->{
				this.layoutXProperty().bind(getConnectionNodeBody().layoutXProperty());
				hidePane.setVisible(false);
			});
			
		}
		

		/**
		 * Check whether the settings pane is showing or hiding. 
		 * @return true if hiding
		 */
		public boolean isShowing() {
			return !hiding;
		}
		
	}
	
	/**
	 * Subclass for bottom control data model hide pane with a little extra functionality to update
	 * data model every time hide pane is used. 
	 * @author Jamie Macaulay
	 *
	 */
	private class ControlDataModelHidePane extends ModuleHidePane{
		
		ControlDataModelHidePane(Pane hidePane, Pane HolderPane, boolean top) {
			super(hidePane, HolderPane, top);
		}

		@Override
		public void showPane(boolean show){
			if (show) populateProcessList();
			super.showPane(show);
		}

	}
	

	
	/**
	 * Set the PamControlledUnit associated with the ModuleConnectionNode. 
	 * @param pamControlledUnit - the pamControlledUnit to associate with the ModuleConnectionNode. 
	 */
	public void setPamControlledUnit(PamControlledUnit pamControlledUnit){
		this.pamControlledUnit=pamControlledUnit; 
		
		//add tool tip
		if (pamControlledUnit!=null){
			
			//sometimes seems to cause an issue with dialogs disappearing. 
//			Tooltip tp = new Tooltip(pamControlledUnit.getUnitType());
//			tp.getStyleClass().removeAll(tp.getStyleClass());
//			Tooltip.install(this, tp);
			
			//if no way for input then remove socket.
			if (!hasInput(pamControlledUnit)){
				for (int i=0; i<getConnectionSockets().size(); i++){
					removeConnectionSocket(getConnectionSockets().get(i)); 
				}
			}
			
			if (!hasOutput(pamControlledUnit)){
				for (int i=0; i<getConnectionPlugs().size(); i++){
					removeConnectionPlug(getConnectionPlugs().get(i),false); 
				}
			}
			
			
			Tooltip tooltip = ModuleToolTipFactory.getToolTip(pamControlledUnit);
			
			if (tooltip!=null) {
			Tooltip. install(this, tooltip);
			}
			
		}
		
		if (pamControlledUnit.getPamModuleInfo()!=null) {
			Node icon = ModuleIconFactory.getInstance().
					getModuleNode(pamControlledUnit.getPamModuleInfo().getClassName());
			if (icon!=null){
				StackPane iconPane = new StackPane(icon);
				iconPane.setPrefSize(DataModelStyle.iconSize, DataModelStyle.iconSize);
				iconPane.setAlignment(Pos.CENTER);
				StackPane.setAlignment(iconPane, Pos.CENTER); //make sure the image or node is centered.
				this.getConnectionNodeBody().getChildren().add(iconPane);
			}
		}
		
		
		moduleNodeParams = new ModuleNodeParams(this); 
		
		
		/**
		 * FIXME...OK, so need to setup the controlled unit in order make sure the hide
		 * pane is the correct size- it sets up here first and then that height is used
		 * in the show pane animation. Messy but you can't seem to layout anything via
		 * code so need that height for animations.
		 */
		pamControlledUnit.setupControlledUnit();
		populateProcessList();

	}
	
	/**
	 * Check whether a module has an input. Most do, however some, e.g. Sound
	 * Acquisition, database, do not have inputs.
	 * 
	 * @param pamControlledUnit
	 *            - the pamControlled unit the module node represents.
	 * @return true if the module has a possible input.
	 */
	protected boolean hasInput(PamControlledUnit pamControlledUnit){
		for (int i=0; i<pamControlledUnit.getNumPamProcesses(); i++){
			if (pamControlledUnit.getPamProcess(i).getCompatibleDataUnits()!=null &&
					pamControlledUnit.getPamProcess(i).getCompatibleDataUnits().size()>0) return true; 
		}
		return false;
	}
	
	/**
	 * Check whether a module has an output. 
	 * @param pamControlledUnit - the pamControlled unit the module node represents. 
	 * @return true if the module has a possible output. 
	 */
	protected boolean hasOutput(PamControlledUnit pamControlledUnit){
		for (int i=0; i<pamControlledUnit.getNumPamProcesses(); i++){
			if (pamControlledUnit.getPamProcess(i).getNumOutputDataBlocks()>0) return true;
		}
		return false;
	}
	
	
	/**
	 * Get the PAMControlledUnit associated with the ModuleConnectionNode.
	 * @return the connectionNode. 
	 */
	public PamControlledUnit getPamControlledUnit(){
		return pamControlledUnit; 
	}
	
	/**
	 * Create a circle from which core lines appear.  
	 */
	private void createCoreCircle(){
		coreCircle=new Circle(8); 
		if (coreOutput) coreCircle.layoutXProperty().bind(this.getConnectionNodeBody().layoutXProperty().add(this.getConnectionNodeBody().widthProperty()));
		else coreCircle.layoutXProperty().bind(this.getConnectionNodeBody().layoutXProperty());
		coreCircle.layoutYProperty().bind(this.getConnectionNodeBody().layoutYProperty().add(20));
		coreCircle.setFill(this.getCoreCircleColour());
		coreCircle.setStroke(this.getCoreCircleColour().darker()); 
		coreCircle.toFront();
		this.getChildren().add(coreCircle);
	}
	
	/**
	 * Remove the core circle. 
	 */
	private void removeCoreCircle(){
		this.getChildren().remove(coreCircle);
	}

	/**
	 * Set whether the module is a core modules. Core modules do not allow uses to connect manually to other modules and instead
	 * connect automatically themselves. There are very few core modules e.g. database, binary storage, array manager. 
	 * @param core - true if a core module. 
	 */
	public void setCore(boolean core){
		this.core=core;
		if (core){
//			//special nodes have no plugs or sockets
			this.removeAllConnectionPlugs();
			this.removeAllConnectionSockets();
			
			//add a circle where lines come out of. 
			createCoreCircle();
				       
			this.getConnectionNodeBody().setOnMousePressed((event)->{
				//just in case remove any lines. 
				removeCoreLines();
				this.getConnectionNodeBody().dragStarted(event); 
				createCoreLines();
			});
		
	        this.getConnectionNodeBody().setOnMouseReleased((event)->{
	        	removeCoreLines();
	        });
	     
		}
		else{
			removeCoreCircle();
			//add a plug
			super.addDefaultPlug();
			//add a socket
			if (hasInput(pamControlledUnit)) super.addDefaultSocket();
			//reset mouse behaviour
			this.getConnectionNodeBody().setupMouseBehaviour();
		}
	}
	
	/**
	 * Create core lines.
	 */
	private void createCoreLines(){
		
		ArrayList<ConnectionNode> otherNodes=getCoreConnectionNodes();
		
		if (otherNodes==null || otherNodes.size()==0) return; 
		
		Line line;
		for (int i=0; i<otherNodes.size(); i++){
			
			ModuleConnectionNode otherNode=((ModuleConnectionNode) otherNodes.get(i));
			
			//don't draw a line to this module or other core modules. 
			if (otherNode==this || otherNode.isCore()) continue; 
			
			//all checks done so create the line
			line=new Line(); 
			if (coreOutput) line.startXProperty().bind(this.getConnectionNodeBody().layoutXProperty().add(this.getConnectionNodeBody().widthProperty()));
			else line.startXProperty().bind(this.getConnectionNodeBody().layoutXProperty());
			line.startYProperty().bind(this.getConnectionNodeBody().layoutYProperty().add(20));
			if (coreOutput) line.endXProperty().bind(otherNodes.get(i).getConnectionNodeBody().layoutXProperty());
			else line.endXProperty().bind(otherNodes.get(i).getConnectionNodeBody().layoutXProperty().add(((Pane) otherNodes.get(i).getConnectionNodeBody()).widthProperty()));
			line.endYProperty().bind(otherNodes.get(i).getConnectionNodeBody().layoutYProperty().add(20));
			line.setStroke(coreLineColour);
			line.setStrokeWidth(3);
			line.toBack();
			
			this.coreLines.add(line);
			this.getChildren().add(0,line); //always add lines at front of list so they stay behind other nodes in scene	
		}
	} 
	
	/**
	 * Get all other nodes core lines should connect to. Override this function to connect core lines to specific nodes.  
	 * 
	 * @return list of ConnectionNodes to connect to. 
	 */
	public ArrayList<ConnectionNode> getCoreConnectionNodes() {
		return this.getConnectionPane().getConnectionNodes();
	}

	/**
	 * Remove core lines from node. 
	 */
	private void  removeCoreLines(){
		int size=coreLines.size(); 
		for (int i=0; i<size; i++){
			if (this.getChildren().remove(coreLines.get(0))){
				this.coreLines.remove(0);
			}
		}
		if (coreLines.size()>0) System.err.println("Error in removing core lines. Some lines were not removed"); 
	}
	
	
	/**
	 * Check whether the module is a CoreOutput or CorInput module. If core output
	 * then the module is assumed to connect to all inputs for all other modules. For CoreInput then 
	 * all other modules are connected to input of module. 
	 * @return true if CoreOutput. 
	 */
	public boolean isCoreOutput() {
		return coreOutput;
	}

	/**
	 * Set whether the module is a CoreOutput or CorInput module. If core output
	 * then the module is assumed to connect to all inputs for all other modules. For CoreInput then 
	 * all other modules are connected to input of module. 
	 * @param coreOutput - true if CoreOutput. 
	 */
	public void setCoreOutput(boolean coreOutput) {
		this.coreOutput = coreOutput;
	}

	/**
	 * Get the line colour of core lines. Note this is not the same as ConnectionLines. 
	 * @return the Colour of core lines
	 */
	public Color getCoreLineColour() {
		return coreLineColour;
	}


	/**
	 * Set the line colour of core lines. Note this is not the same as ConnectionLines. 
	 * @param the colour to set core lines to. 
	 */
	public void setCoreLineColour(Color coreLineColour) {
		this.coreLineColour = coreLineColour;
	}
	
	/**
	 * Get the circle colour for a core module. Note this is not the same as ConnectionLines. 
	 * @return the colour of core circle
	 */
	public Color getCoreCircleColour() {
		return coreCircleColour;
	}
	
	/**
	 * Set the circle colour for a core module. Note this is not the same as ConnectionLines. 
	 * @param the Colour of core circle
	 */
	public void setCoreCircleColour(Color circleColour) {
		this.coreCircleColour=circleColour;
	}

	/**
	 * Check whether this is a core module node. Core modules cannot be manullay connected
	 * @return true if a core module
	 */
	public boolean isCore() {
		return core;
	}
	
	/**
	 * Check whether the module attempts to connect automatically to a parent when added to pane. 
	 * @return true if automatically tries to connect to parent when added to a pane
	 */
	public boolean isAutoConnect(){
		return attemptAutoConnect; 
	}
	
	/**
	 * Set whether the module attempts to connect automatically to a parent when added to pane. 
	 * @param autoConnect - true if automatically tries to connect to parent when added to a pane
	 */
	public void setAutoConnect(boolean autoConnect){
		this. attemptAutoConnect=autoConnect; 
	}

	public DataModelConnectPane getDataModelPane() {
		return connectionPane;
	}

	/**
	 * Say a this connection node has a parent with multiple compatible output data blocks. Which to set as it's parent data block? This returns the preferred data
	 * block in this case.  
	 * @param parentControlledUnit - the parent controlled unit. 
	 * @return the preferred output data block from the parent. Can be null. 
	 */
	public PamDataBlock getPrefferedParent(PamControlledUnit parentControlledUnit) {
		//try return the first output data block. 
		if (parentControlledUnit.getPamProcess(0)!=null){
			return parentControlledUnit.getPamProcess(0).getOutputDataBlock(0); 
		}
		return null;
	}

	/**
	 * Check whether connection listeners are enabled
	 * @return true if enabled. 
	 */
	public boolean isEnableConnectListeners() {
		return enableConnectListeners;
	}

	/**
	 * Set whether the connection listeners are enabled. Only set to false if the connections are not 
	 * being made by the user in the GUI
	 * @param enableConnectListeners - true to enable listeners
	 */
	public void setEnableConnectListeners(boolean enableConnectListeners) {
		this.enableConnectListeners = enableConnectListeners;
	}

	/**
	 * Check whether the top settings pane is showing or not. 
	 * @return true if top pane is showing.
	 */
	public boolean isSettingsShowing() {
		return this.topHidingPane.isShowing();
	}
	
	/***Info to save the node***/

	@Override
	public ModuleNodeParams getConnectionNodeParams() {
		updateParams(); 
		return moduleNodeParams; 
	}

	@Override
	public void setConnectionNodeParams(ConnectionNodeParams usedStructInfo) {
		this.moduleNodeParams=(ModuleNodeParams) usedStructInfo; 
	}

	@Override
	public void loadsettings() {
		//do not need to do anything here. The correct connections will be made when pam model-> data model is first called
		
	}

	@Override
	public void updateParams() {
		moduleNodeParams.setLayoutY(this.getConnectionNodeBody().getLayoutY());
		moduleNodeParams.setLayoutX(this.getConnectionNodeBody().getLayoutX());
	}
	
	
	public DataModelConnectPane getDataModelConnectionPane() {
		return this.connectionPane;
	}
	

}
