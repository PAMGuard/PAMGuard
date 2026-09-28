package pamViewFX;

import java.util.ArrayList;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import pamViewFX.PamGuiFX.ToolBarPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamTabFX;
import pamViewFX.fxNodes.PamTabPane;
import pamViewFX.fxNodes.internalNode.PamInternalPane;
import userDisplayFX.UserDisplayNodeFX;

/**
 * Extends the PAMTabFX class so that a new stage with PamGui nodes appears when
 * tab is dragged out of frame, rather than just a new tabPane as the primary
 * pane.
 * 
 * @author Jamie Macaulay
 */
public class PamGuiTabFX extends PamTabFX {
	
	/**
	 * Holds the tab content. 
	 */
	private PamBorderPane contentHolder; 
	
	/**
	 * Sits in the center of the content holder and holds internal panes,
	 */
    private Pane holder;
    
    /**
     * List of internal panes within the tab content pane. 
     */
    private ArrayList<PamGuiInternalPane> internalPanes=new ArrayList<PamGuiInternalPane>();
    
    /**
     * True if panes are editable. 
     */
	boolean editable=false;
	
	/**
	 * Flag for the way in which panes should be autosorted within a tab
	 */
	private int sortType=SORT_HORIZONTAL; 
	
	
	/**
	 * Sort panes vertically 
	 */
	public final static int SORT_HORIZONTAL=0; 
	
	/**
	 * Sort panes vertically 
	 */
	public final static int SORT_VERTICAL=1; 
	
	
	/**
	 * Sort pane so they have equal x and y spacing. 
	 */
	public final static int SORT_TILE=2; 

	
	/**
	 * Allow the panes to be manually resized. 
	 */
	boolean isResizable=true;

	/**
	 * The PAMGuiFX the tab belongs to. 
	 */
	private PamGuiFX pamGui;  
	
	/**
	 * Tab display information. 
	 */
	private TabInfo tabInfo;
	
	/**
	 * Custom right-side content for the shared toolbar when this tab is selected.
	 * If null, the default right-side content (resize toggle) will be shown.
	 */
	private Region customToolbarRight;
	
	/**
	 * Constructor for a new tab
	 * @param tabInfo - info on the tab such as name
	 * @param pamGui - reference to the PamGuiFX the pane belongs to.
	 */
	public PamGuiTabFX(TabInfo tabInfo, PamGuiFX pamGui) {
		this(tabInfo, null, pamGui);
	}

	/**
	 * Constructor for a new tab
	 * @param tabInfo - info on the tab such as name
	 * @param newContent - content to display
	 * @param pamGui - reference to the PamGuiFX the pane belongs to.
	 */
	public PamGuiTabFX(TabInfo tabInfo, UserDisplayNodeFX newContent, PamGuiFX pamGui) {
		super(tabInfo.tabName);
		this.pamGui=pamGui; 
		this.tabInfo=tabInfo; 
		
		
		contentHolder=new PamBorderPane(); 
		
		//create the holder pane for internal display panes
		holder=new Pane();
		contentHolder.setCenter(holder);
		
		//add content
		if (newContent!=null) addInternalPane(newContent);
		
		super.setContent(contentHolder);

		setPanesEditable(editable);

		//allow the user to right-click the tab header to rename it.
		setupRenameContextMenu();
	}

	/**
	 * Add a context menu to the tab header which allows the user to rename the
	 * tab by right-clicking on it.
	 */
	private void setupRenameContextMenu() {
		Label label = getLabel();
		if (label == null) return;

		ContextMenu contextMenu = new ContextMenu();
		MenuItem renameItem = new MenuItem("Rename tab...");
		renameItem.setOnAction(e -> startRename());
		contextMenu.getItems().add(renameItem);

		label.setOnContextMenuRequested(e -> {
			contextMenu.show(label, e.getScreenX(), e.getScreenY());
			e.consume();
		});

		//also allow a double-click to start renaming.
		label.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				startRename();
				e.consume();
			}
		});
	}

	/**
	 * Begin an inline rename of the tab. The tab header label is temporarily
	 * replaced with a text field. The new name is committed when the user
	 * presses Enter or the field loses focus, and cancelled with Escape.
	 */
	public void startRename() {
		Label label = getLabel();
		if (label == null) return;

		TextField textField = new TextField(getName());
		textField.setPrefWidth(Math.max(80, label.getWidth() + 20));

		//guard so the name is only committed once (Enter then focus-loss would otherwise fire twice).
		final boolean[] committed = {false};

		Runnable finish = () -> {
			if (committed[0]) return;
			committed[0] = true;
			String newName = textField.getText() == null ? "" : textField.getText().trim();
			//restore the label as the tab graphic.
			setGraphic(label);
			if (!newName.isEmpty() && !newName.equals(getName())) {
				renameTab(newName);
			}
		};

		textField.setOnAction(e -> finish.run());
		textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
			if (!isFocused) finish.run();
		});
		textField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				//cancel: restore the label without renaming.
				committed[0] = true;
				setGraphic(label);
				e.consume();
			}
		});

		setGraphic(textField);
		textField.selectAll();
		textField.requestFocus();
	}

	/**
	 * Rename the tab. Updates the visible header text, the stored {@link TabInfo}
	 * and the tab name referenced by any user displays held in this tab so that
	 * the display-to-tab association is preserved when settings are saved and
	 * restored.
	 * @param newName - the new tab name.
	 */
	public void renameTab(String newName) {
		if (newName == null || newName.trim().isEmpty()) return;
		newName = newName.trim();

		//update the visible label and drag text.
		setLabelText(newName);

		//update the stored tab info used for persistence.
		tabInfo.tabName = newName;

		//update the tab name referenced by any displays within this tab so the
		//display-to-tab link survives a save/restore (see PamGuiManagerFX.getDisplayTab).
		for (PamGuiInternalPane internalPane : internalPanes) {
			UserDisplayNodeFX node = internalPane.getUserDisplayNode();
			if (node != null && node.getDisplayParams() != null) {
				node.getDisplayParams().tabName = newName;
			}
		}
	}
	
	/**
	 * Set the toolbar pane. 
	 * @deprecated The toolbar is now shared across all tabs via PamGuiFX.getSharedToolbar(). 
	 * This method is kept for backward compatibility but has no effect.
	 * @param toolBarPane - the ToolBarPane (ignored).
	 */
	@Deprecated
	public void setToolbar(ToolBarPane toolBarPane){
		//no-op: toolbar is now shared and set on PamTabPane via setToolbarRegion()
	}
	
	/**
	 * Set the displays within the tab to be manually resized. 
	 * @param resize - true to allow manual resizing
	 */
	public void setResizableDisplays(boolean  resize){
		this.isResizable=resize; 
		ToolBarPane sharedToolbar = pamGui.getSharedToolbar();
		if (sharedToolbar != null) {
			sharedToolbar.showResizableControl(resize);
		}
	}
	
	/**
	 * Set all internal panes to be manually resizable. Decorates each pane with nodes to 
	 * allow the user to drag the pane and set height and width. 
	 * @param editable - true to set edit mode. f
	 */
	public void setPanesEditable(boolean editable){
		this.editable=editable;
		for (int i=0; i<this.internalPanes.size(); i++){
			internalPanes.get(i).showResizeControls(editable);
		}
	}

	
	@Override
	public Pane createNewPane(Tab tab, PamTabPane tabPane, Stage newStage){
		//create a new GUI frame. 
		PamGuiFX pamGUIFX=new PamGuiFX(tabPane, newStage, pamGui.getPamGuiManagerFX()); 
		pamGUIFX.getStylesheets().addAll(pamGui.getPamGuiManagerFX().getPamCSS());
		
		
		//need to add PamGUIFX to list in PamGUIManagerFX. 
		pamGui.getPamGuiManagerFX().getPamGuiFXList().add(pamGUIFX);
		newStage.setOnCloseRequest(e->{
			//if the stage is closed then move the tabs back to the main stage. 
			ArrayList<PamGuiTabFX> tabs = pamGUIFX.getTabs();
			pamGui.getPamGuiManagerFX().getPamGuiFXList().remove(pamGUIFX);
			pamGUIFX.getTabs().clear();
			pamGui.getPamGuiManagerFX().getPamGuiFXList().get(0).addAllTabs(tabs);
		});
	    return pamGUIFX;
	} 

	
	/**
	 * Set the main content node. Sits below the tool bar.
	 */
	public void setMainContent(Region node){
		contentHolder.setCenter(node);
	}
		
	/**
	 * Add an internal pane to the tab. 
	 * @param pane - pane to add. 
	 * @return the internal pane which has been added
	 */
	public PamGuiInternalPane addInternalPane(Region pane){
		if (pane==null) return null;
		pane.setPrefSize(90, 90);
		PamGuiInternalPane newInternalPane=new PamGuiInternalPane(pane, holder);
		holder.getChildren().add(newInternalPane);
		internalPanes.add(newInternalPane);
		autoSortPanes(sortType);
		return newInternalPane;
	}
	

	/**
	 * Add an UserDisplayNodeFX to the graph.  
	 * @param pane - pane to add. 
	 * @return the internal pane which has been added
	 */
	public PamGuiInternalPane addInternalPane(UserDisplayNodeFX userDisplayNodeFX){
//		System.out.println("UserDisplayNodeFX: " + userDisplayNodeFX);
		if (userDisplayNodeFX==null || userDisplayNodeFX.getNode()==null) return null;
		
		for (PamGuiInternalPane internalPane: this.internalPanes) {
			if (userDisplayNodeFX == internalPane.getUserDisplayNode()) {
				System.err.println("UserDisplayNodeFX is laready in pane");
				return null;
			}
		}
		
		
		PamGuiInternalPane newInternalPane=new PamGuiInternalPane(userDisplayNodeFX, holder);
		
		//bind the drag pad to the height of the shared toolbar so that it is always below the toolbar.
		newInternalPane.dragPadNorthProperty().bind(pamGui.getSharedToolbar().heightProperty());
		
		if (!userDisplayNodeFX.isResizeableDisplay()) newInternalPane.showResizeControls(false);
		holder.getChildren().add(newInternalPane);
		internalPanes.add(newInternalPane);
		autoSortPanes(sortType);
		return newInternalPane;
	}
	
	
	/**
	 * Sort panes.
	 */
	public void autoSortPanes(int sortType) {
		double holderWidth=holder.getWidth();
		double holderHeight=holder.getHeight();
		
		//HACK- tab has no height if it's just been made. 
		if (holderWidth<=0 || holderHeight<=0){
			holderWidth=pamGui.getPamGuiManagerFX().getDataModelFX().getWidth();
			holderHeight=pamGui.getPamGuiManagerFX().getDataModelFX().getHeight();
		}
		//HACK end

		// Compute the drag pad insets from the first pane (all panes share the same bindings).
		double padNorth = internalPanes.isEmpty() ? 0 : internalPanes.get(0).getDragPadNorth();
		double padSouth = internalPanes.isEmpty() ? 0 : internalPanes.get(0).getDragPadSouth();
		double padWest  = internalPanes.isEmpty() ? 0 : internalPanes.get(0).getDragPadWest();
		double padEast  = internalPanes.isEmpty() ? 0 : internalPanes.get(0).getDragPadEast();

		// Effective area available for laying out panes, after accounting for drag pads.
		double availWidth  = holderWidth  - padWest - padEast;
		double availHeight = holderHeight - padNorth - padSouth;

		// Origin offset so that panes start inside the padded area.
		double originX = padWest;
		double originY = padNorth;

		//TILE means equally sized windows 
		if (sortType==SORT_TILE){
			for (int i=0; i<internalPanes.size(); i++){
				internalPanes.get(i).setPaneSize(availWidth/internalPanes.size(), availHeight);
				internalPanes.get(i).setPaneLayout(originX + (i*availWidth)/internalPanes.size(), originY);
			}
			return;
		}
		
		//SORT_HORIZONTAL or SORT_VERTICAL has a custom way of setting small and large windows. 
		if (sortType==SORT_HORIZONTAL || sortType==SORT_VERTICAL){
			
			boolean horz = (sortType==SORT_HORIZONTAL);
			
			double r = .6;
			double r1 = 1 - r;

			int smallWindows = 0;
			
			
			//the padding between windows
			double padding = 10; 

			ArrayList<PamGuiInternalPane> dw = internalPanes;

			if (dw.size() == 0) return;

			//calc number of small windows and large windows 
			for (int i = 0; i < dw.size(); i++) {
				if (dw.get(i).getUserDisplayNode().isMinorDisplay()) {
					smallWindows++;
				}
			}
			int largeWindows = dw.size() - smallWindows;

			//now place windows in correct position
			//large windows 
			double x, y, w, h = 0.;
			double pad =0.;
			
			if (largeWindows > 0) {
				x = originX;
				y = originY;
				if (smallWindows == 0) {
					h = (horz) ? availHeight : availHeight/largeWindows;
					w = (horz) ? (availWidth / largeWindows) : availWidth*r;
				}
				else {
					h = (horz) ? availHeight * r :  availHeight/largeWindows;
					w = (horz) ? (availWidth / largeWindows) : availWidth;
				}
				for (int i = 0; i < dw.size(); i++) {
					if (dw.get(i).getUserDisplayNode().isMinorDisplay()) continue;

					dw.get(i).setPaneLayout(x, y);
					
					//set the padding if the pane is not the last pane.
					pad = (i== dw.size()-1) ? 0 : padding;
					dw.get(i).setPaneSize(w - (horz?  pad:0), h -  (horz?  0:pad));
					if (horz) x += w;
					else y += h; 
				}
			}
			
			//small windows
			if (smallWindows > 0) {
				x = (horz) ? originX : originX + availWidth - availWidth * r1;
				y = (horz) ? originY + h : originY;
									
				if (largeWindows > 0) {
					h = (horz) ? availHeight - h : availHeight/smallWindows;
					w = (horz) ? (availWidth / smallWindows) :  availWidth * r1;
				}
				else {
					w = (horz) ? (availWidth / smallWindows) :  availWidth;
					h = (horz) ? availHeight : availHeight/smallWindows;
				}
				
				for (int i = 0; i < dw.size(); i++) {
					if (!dw.get(i).getUserDisplayNode().isMinorDisplay()) continue;
					dw.get(i).setPaneLayout(x, y);
					
					//set the padding if the pane is not the last pane.
					pad = (i== dw.size()-1) ? 0 : padding;
					dw.get(i).setPaneSize(w- (horz?  pad:0), h -  (horz?  0:pad));
					if (horz) x += w;
					else y +=h;
				}
			}
			return;
		}
	}

	/**
	 * Remove an internal pane if it contains the node 
	 * @param removeNode - remove the pane if it contains this node. 
	 */
	public void removeInternalPane(UserDisplayNodeFX removeNode){
		//keep track of removed panes. 
		ArrayList<PamGuiInternalPane> removedInternalPanes=new ArrayList<PamGuiInternalPane>(); 
		//first, find the internal pane.
		for (int i=0; i<internalPanes.size(); i++){
			if (internalPanes.get(i).getUserDisplayNode()==removeNode){
				holder.getChildren().remove(internalPanes.get(i));
				removedInternalPanes.add(internalPanes.get(i));
			}
		}
		//also removed from list
		internalPanes.removeAll(removedInternalPanes);
	}
	
	/**
	 * Get all internal panes within a PamGui the tab's content holder./
	 * @return a list of internal panes. 
	 */
	public ArrayList<PamGuiInternalPane> getInternalPanes(){
		return this.internalPanes;
	}
	
	/**
	 * Get the content holder which holds the main tab content pane. 
	 * @return the content pane. 
	 */
	public PamBorderPane getContentHolder(){
		return contentHolder;
	}
	
	
	/**
	 * Set the content holder which holds the main tab content pane.  
	 * @param contentHolder - the content pane
	 */
	protected void setContentHolder(PamBorderPane contentHolder){
		this.contentHolder=contentHolder;
	}
	
	/**
	 * Get the content tool bar. This returns the single shared toolbar from PamGuiFX
	 * which sits between the tab headers and the tab content area.
	 * @return the shared toolbar for all tabs.
	 */
	public ToolBarPane getContentToolbar(){
		return pamGui.getSharedToolbar();
	}
	
	/**
	 * Set the content tool bar. 
	 * @deprecated The toolbar is now shared across all tabs via PamGuiFX.getSharedToolbar().
	 * This method is kept for backward compatibility but has no effect.
	 * @param contentToolBar - the content toolBar pane (ignored)
	 */
	@Deprecated
	protected void setContentToolbar(ToolBarPane contentToolBar){
		//no-op: toolbar is now shared and set on PamTabPane via setToolbarRegion()
	}
	
	/**
	 * Check whether internal panes are currently editable. 
	 * @return true if internable panes are in editable mode. 
	 */
	public boolean getEditable() {
		return editable;
	}
	
	/**
	 * Set custom right-side content for the shared toolbar when this tab is selected.
	 * This allows individual tabs (e.g. data model) to display tab-specific controls
	 * in the toolbar's right area.
	 * @param rightContent - the custom right content, or null to use the default.
	 */
	public void setCustomToolbarRight(Region rightContent) {
		this.customToolbarRight = rightContent;
	}
	
	/**
	 * Get the custom right-side content for the shared toolbar.
	 * @return the custom right content, or null if the default should be used.
	 */
	public Region getCustomToolbarRight() {
		return customToolbarRight;
	}
	
	/**
	 * Get the PamGuiFX that this tab belongs to.
	 * @return the parent PamGuiFX.
	 */
	public PamGuiFX getPamGuiFX() {
		return pamGui;
	}

	public boolean isStaticDisplay() {
		return this.isResizable;
//		if (contentHolder.getChildren().get(0) instanceof UserDisplayNodeFX){
//			(( UserDisplayNodeFX)this.getContent()).isStaticDisplay();
//			return true;
//		}
		//TODO- internal panes should not contain static displays but may by accident. Need
		//to sort this out. 
//		return false;
	}
	
//	@Override
//	public final void setText(String value) {
//		super.setText(value);
//		this.tabInfo=value; 
//	}

	/**
	 * Convenience class for getting name of tabs. 
	 * @return name of tabs. 
	 */
	public String getName() {
		//note that this should not be getText() because 
		//this is not set in the parent class constructor. 
		return this.tabInfo.tabName;
	}
	
	/**
	 * Extension of PamInternalPane class used specifically to add 
	 * UserDisplayNodeFX from different controlled units. 
	 * @author Jamie Macaulay
	 *
	 */
	class PamGuiInternalPane extends PamInternalPane {
		

		private UserDisplayNodeFX mainPane;

		/**
		 * Get the user display node associated with this pane. Can be null if there is no associated userdisplay node. 
		 * @return the UserDisplayNode 
		 */
		public UserDisplayNodeFX getUserDisplayNode() {
			return mainPane;
		}

		public PamGuiInternalPane(UserDisplayNodeFX mainPane, Region holderPane) {
			super(new PamBorderPane(mainPane.getNode()), holderPane);
			this.mainPane=mainPane; 
		}
		
		public PamGuiInternalPane(Region mainPane, Region holderPane) {
			super(new PamBorderPane(mainPane), holderPane);
			this.mainPane=null; 
		}
		
	}

	/**
	 * Tab information for the tab. Usually used for saving and restoring settings. 
	 * @return information on the tab.
	 */
	public TabInfo getTabInfo() {
		return this.tabInfo;
	}
	
}