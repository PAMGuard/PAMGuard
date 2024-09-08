package dataModelFX;

import java.util.ArrayList;

import PamController.PamController;
import PamModel.PamModuleInfo;
import dataModelFX.ConnectionNodeParams.PAMConnectionNodeType;
import dataModelFX.connectionNodes.ModuleIconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamScrollPane;
import pamViewFX.fxNodes.PamTilePane;
import pamViewFX.fxNodes.PamTitledPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.connectionPane.structures.ConnectionGroupBody;
import pamViewFX.fxNodes.connectionPane.structures.ExtensionSocketStructure;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * A pane which shows a list of all the available modules. These can be dragged
 * into the data model pane to create a module and add to the PAMGuarf data model. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DataModelModulePane  extends PamBorderPane {
		
	/**
	 * Reference to the data model pane. 
	 */
	@SuppressWarnings("unused")
	private DataModelPaneFX dataModelPaneFX;
	
	/**
	 * List of possible modules (not the modules actually added to the model).
	 */
	private ArrayList<ModuleRectangle> moduleRectangles;
	

	/**
	 * The main scroll panes holder
	 */
	private PamScrollPane moduleSelectPane; 
	
	//data for dragging modules between panes. 
	public static final String MODULE_DRAG_KEY="module";
	
	public  ObjectProperty<ModuleRectangle> draggingModule=new SimpleObjectProperty<>();
	
	public  ObjectProperty<StructureRectangle> draggingStructure=new SimpleObjectProperty<>();


	public DataModelModulePane(DataModelPaneFX dataModelPaneFX) {
		this.dataModelPaneFX=dataModelPaneFX; 
		this.setCenter(createPane()); 
	}
	
	private PamScrollPane createPane(){
		moduleSelectPane=new PamScrollPane(); 
				
		moduleSelectPane.getStylesheets().addAll(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getSlidingDialogCSS());
	
		moduleSelectPane.setPrefWidth(250);
		moduleSelectPane.getStyleClass().add("scroll-pane-dark");		
	
//		//////TEST///////
//		MenuButton m = new MenuButton("Add Modules ");
//		m.showingProperty().addListener(new ChangeListener<Boolean>() {
//	        @Override
//	        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//	            if(newValue) {
//	            	m.getItems().removeAll(m.getItems()); 
//	                m.getItems().add(PamModuleInfo.getModulesMenu(PamController.getInstance().getGuiManagerFX().getMainScene()));
//	            }
//	        }
//	    });
//		//moduleSelectPane.add(m, 0, 0);
//		////////////////
	
		moduleSelectPane.setFitToWidth(true);
		
		return moduleSelectPane; 
	}
	
	/**
	 * Update the list of modules. 
	 * Task: 	1) Makes modules which can no longer be added to the pane a different colours
	 * 			2) Disables the module so it cannot be dragged, 
	 */
	public void updateModuleListPane(){
		for (int i=0; i<moduleRectangles.size(); i++){
			if (moduleRectangles.get(i).getPamModuleInfo().getMaxNumber()>0){
				int nModules=PamController.getInstance().findControlledUnits(moduleRectangles.get(i).getPamModuleInfo().getModuleClass()).size();
				if (nModules>=moduleRectangles.get(i).getPamModuleInfo().getMaxNumber()){
					//show disabled colour
					moduleRectangles.get(i).setOpacity(ModuleNodeParams.DISABLED_OPACITY);
					moduleRectangles.get(i).setDisable(true);
					continue; 
				}
			}
			moduleRectangles.get(i).setOpacity(1);
			moduleRectangles.get(i).setDisable(false);
		}
	}
	
	/**
	 * Create a a set of module rectangles to populate the module list pane. 
	 */
	protected void populateModuleListPane(){
				
		moduleRectangles=new ArrayList<ModuleRectangle>(); 
		
		//get the list of module
		ArrayList<PamModuleInfo> moduleList = PamModuleInfo.getModuleList();
		//list of module group names
		ArrayList<String> moduleGroups=new ArrayList<String>();
		
		//holder pane
		PamVBox moduleSelectPane=new PamVBox(); 		

		//get all menu groups
		String moduleGroup; 
		for (int i=0; i<moduleList.size(); i++){
			moduleGroup=moduleList.get(i).getModulesMenuGroup().getMenuName();
			if (!moduleGroups.contains(moduleGroup) && DataModelPaneFX.isFXModule(moduleList.get(i))) moduleGroups.add(moduleGroup);
			moduleList.get(i).getModulesMenuGroup().getMenuName();
		}
		
		//create a set of titled panes
		PamTitledPane[] titledPanes=new PamTitledPane[moduleGroups.size()];
		//create tile pane to hold modules
		PamTilePane[] moduleTilePanes=new PamTilePane[moduleGroups.size()];
		for (int i=0; i<moduleGroups.size(); i++){
			
			moduleTilePanes[i]=new PamTilePane(15,15);
			moduleTilePanes[i].setPadding(new Insets(15));
			moduleTilePanes[i].setPrefColumns(3); 
			titledPanes[i]=new PamTitledPane(moduleGroups.get(i), moduleTilePanes[i]);
			
			//add modules to tile pane, only if in the correct group. 
			for (int j=0; j<moduleList.size(); j++){
				moduleList.get(i).getModulesMenuGroup().getMenuName();
				if (DataModelPaneFX.isFXModule(moduleList.get(j)) && 
						moduleList.get(j).getModulesMenuGroup().getMenuName()==moduleGroups.get(i)) {
					//add to list
					moduleRectangles.add(createModuleRectangle(moduleList.get(j)));
					moduleTilePanes[i].getChildren().add(moduleRectangles.get(moduleRectangles.size()-1));
				}
			}
			moduleSelectPane.getChildren().add(titledPanes[i]);
		}
		
		moduleSelectPane.getChildren().add(createStucturePane()); 
		
		//update colours
		updateModuleListPane(); 
		
		//
	
		this.moduleSelectPane.setContent(moduleSelectPane); 
	}
	
	
	/**
	 * Create a structure pane. This contains non module structures which can be used in the data model. 
	 * Structures can be nodes which aggregate plugs, create groups of modules etc. 
	 * @return the structure pane. 
	 */
	private PamTitledPane createStucturePane() {
		
		PamTilePane  structurePane=new PamTilePane(15,15);
		structurePane.setPadding(new Insets(15));

		
		//now add all the structures we want.
		StructureRectangle rect;
		rect = createStructureRectangle(ConnectionGroupBody.getGroupStructureIcon(ModuleNodeParams.DEFAULT_WIDTH), PAMConnectionNodeType.PAMGroupStructure); 
		structurePane.getChildren().add(rect);
		rect = createStructureRectangle(ExtensionSocketStructure.getStructureIcon(ModuleNodeParams.DEFAULT_WIDTH), PAMConnectionNodeType.PAMExtensionStructure); 
		structurePane.getChildren().add(rect);

		PamTitledPane strcutureTitledPane = new PamTitledPane("Data Model Structures", structurePane);
		
		return strcutureTitledPane; 
	}
	
	
	/**
	 * Create an icon which represents a connection structure., 
	 * @param icon - the icon. 
	 * @return the connection structure. 
	 */
	 private StructureRectangle createStructureRectangle(Node icon, PAMConnectionNodeType type){
		 
		 //create the icon
		 StructureRectangle rect  = new StructureRectangle(icon, type); 
		 
		 rect.setOnDragDetected(new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent event){
					//only drag if can create 
					// if (pamModuleInfo.canCreate()) <- does not work for some reason; module

					Dragboard dragboard = rect.startDragAndDrop(TransferMode.MOVE);
					dragboard.setDragView(new ImageView(rect.snapshot(new SnapshotParameters(), null)).getImage());
					ClipboardContent clipboardContent = new ClipboardContent();
					clipboardContent.putString(MODULE_DRAG_KEY);
					dragboard.setContent(clipboardContent);
					draggingStructure.set(rect);
					event.consume();
				}
			});
		 
		 return rect; 

	 }
	
	/**
	 * Create a rectangle which represents the module.
	 * @param pamModuleInfo - module info
	 * @return the newly created rectangle. 
	 */
	protected ModuleRectangle createModuleRectangle(PamModuleInfo pamModuleInfo){

		Node icon=ModuleIconFactory.getInstance().getModuleNode(pamModuleInfo.getClassName());
		
		ModuleRectangle rect;
		if (icon==null)  rect = new ModuleRectangle(pamModuleInfo, ModuleNodeParams.DEFAULT_WIDTH,ModuleNodeParams.DEFAULT_HEIGHT);
		else rect = new ModuleRectangle(pamModuleInfo, icon, ModuleNodeParams.DEFAULT_WIDTH,ModuleNodeParams.DEFAULT_HEIGHT);
		
		rect.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				//only drag if can create 
				// if (pamModuleInfo.canCreate()) <- does not work for some reason; module

				Dragboard dragboard = rect.startDragAndDrop(TransferMode.MOVE);
				dragboard.setDragView(new ImageView(rect.snapshot(new SnapshotParameters(), null)).getImage());
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.putString(MODULE_DRAG_KEY);
				dragboard.setContent(clipboardContent);
				draggingModule.set(rect);
				event.consume();
			}
		});

		Tooltip tp = new Tooltip(pamModuleInfo.getDefaultName());
		tp.getStyleClass().removeAll(tp.getStyleClass());
		Tooltip.install(rect, tp);

		return rect;
	}
	
	
	/**
	 * A pane containing the icon for a structure. 
	 * @author Jamie Macaulay
	 *
	 */
	public class StructureRectangle extends StackPane {
		
		private PAMConnectionNodeType type;

		public StructureRectangle(Node rectangle, PAMConnectionNodeType type ) {
			this.getChildren().add(rectangle); 
			this.type=type; 
		}

		public PAMConnectionNodeType getStructureType() {
			return type;
		}
		
	}

	
	/**
	 * Rectangle which holds reference to which type of module it's associated with. 
	 * @author Jamie Macaulay
	 *
	 */
	class ModuleRectangle extends StackPane {
		
		private static final double corner=15; 
		
		/**
		 * PamModuleInfo associated with the module rectangle. 
		 */
		private PamModuleInfo pamModuleInfo;

		public PamModuleInfo getPamModuleInfo() {
			return pamModuleInfo;
		}

		public void setPamModuleInfo(PamModuleInfo pamModuleInfo) {
			this.pamModuleInfo = pamModuleInfo;
		}

		ModuleRectangle(PamModuleInfo pamModuleInfo, double width, double height){
			super();
			this.pamModuleInfo=pamModuleInfo;
			this.setWidth(width);
			this.setHeight(height);
			getChildren().add(createRect(width, height)); 
		}

		public ModuleRectangle(PamModuleInfo pamModuleInfo, Node imageIcon, double width, double height) {
			super();
			this.pamModuleInfo=pamModuleInfo;
			//imageView.setOpacity(0.9);
			getChildren().addAll(createRect(width, height), imageIcon); 
		}
		
		private Rectangle createRect(double width, double height){
			Rectangle rect=new Rectangle(width,height);
			rect.setArcWidth(corner);
			rect.setArcHeight(corner);
			rect.setFill(new Color(ModuleNodeParams.DEFAULT_COLOUR[0],
					ModuleNodeParams.DEFAULT_COLOUR[1],ModuleNodeParams.DEFAULT_COLOUR[2],1));
			return rect;
		}
		
	}

	/**
	 * Get the module drag key.  Used for drag and drop listeners between module and connection panes.  
	 * @return the module drag key string. 
	 */
	public String getModuleDragKey() {
		return DataModelModulePane.MODULE_DRAG_KEY;
	}
	
	/**
	 * Get the dragging module properties.  Used for drag and drop listeners between module and connection panes. 
	 * @return the dragging module properties
	 */
	public ObjectProperty<ModuleRectangle> getDraggingModule() {
		return draggingModule;
	}
	
	/**
	 * Get the dragging structure properties. Used when dragging structures into the 
	 * @return the dragging structure. 
	 */
	public ObjectProperty<StructureRectangle> getDraggingStructure() {
		return draggingStructure;
	}
	

}
