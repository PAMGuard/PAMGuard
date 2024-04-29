package pamViewFX;

import java.util.ArrayList;

import javafx.scene.control.Tab;
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
	 * Pane which sits just below the tab which can contain quick access controls. 
	 */
	private ToolBarPane toolbar;
	
	/**
	 * Holds the tab content and the content tool bar. 
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
		
		//needs to be in this order so toolbar pane sits on top of center pane. 
		holder=new Pane();
		contentHolder.setCenter(holder);
		
		//add content
		if (newContent!=null) addInternalPane(newContent);
		
		super.setContent(contentHolder);
		
		setPanesEditable(editable);
	}
	
	/**
	 * Set the toolbar pane. This is a narrow bar which sits just under the tab contians both generic and 
	 * possibly tab specific controls. 
	 * @param toolBarPane - the ToolBarPane to add. 
	 */
	public void setToolbar(ToolBarPane toolBarPane){
		this.toolbar=toolBarPane;
		contentHolder.setTop(toolBarPane);
	}
	
	/**
	 * Set the displays within the tab to be manually resized. 
	 * @param toolBarPane - true to allow manual resizing
	 */
	public void setResizableDisplays(boolean  resize){
		this.isResizable=resize; 
		toolbar.showResizableControl(resize);
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
		System.out.println("UserDisplayNodeFX: " + userDisplayNodeFX);
		if (userDisplayNodeFX==null || userDisplayNodeFX.getNode()==null) return null;
		
		for (PamGuiInternalPane internalPane: this.internalPanes) {
			if (userDisplayNodeFX == internalPane.getUserDisplayNode()) {
				System.err.println("UserDisplayNodeFX is laready in pane");
				return null;
			}
		}
		
		
		PamGuiInternalPane newInternalPane=new PamGuiInternalPane(userDisplayNodeFX, holder);
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
		
		//TILE means equally sized windows 
		if (sortType==SORT_TILE){
			for (int i=0; i<internalPanes.size(); i++){
				internalPanes.get(i).setPaneSize(holderWidth/internalPanes.size(), holderHeight);
				internalPanes.get(i).setPaneLayout((i*holderWidth)/internalPanes.size(), 0);
			}
			return;
		}
		
		//SORT_HORIZONTAL or SORT_VERTICAL has a custom way of setting small and large windows. 
		if (sortType==SORT_HORIZONTAL || sortType==SORT_VERTICAL){
			
			boolean horz = (sortType==SORT_HORIZONTAL);
			
			double r = .6;
			double r1 = 1 - r;

			int smallWindows = 0;

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
			double x, y, w, h = 0;
			if (largeWindows > 0) {
				x = 0;
				y = 0;
				if (smallWindows == 0) {
					h = (horz) ? holderHeight : holderHeight/largeWindows;
					w = (horz) ? (holderWidth / largeWindows) : holderWidth*r;
				}
				else {
					h = (horz) ? holderHeight * r :  holderHeight/largeWindows;
					w = (horz) ? (holderWidth / largeWindows) : holderWidth ;
				}
				for (int i = 0; i < dw.size(); i++) {
					if (dw.get(i).getUserDisplayNode().isMinorDisplay()== true) continue;

					dw.get(i).setPaneLayout(x, y);
					dw.get(i).setPaneSize(w, h);
					if (horz) x += w;
					else y += h; 
				}
			}
			
			//small windows
			if (smallWindows > 0) {
				x = (horz) ? 0 : holderWidth - holderWidth* r1;
				y = (horz) ? h : 0;
									
				if (largeWindows > 0) {
					h = (horz) ? holderHeight - h : holderHeight/smallWindows;
					w = (horz) ? (holderWidth / smallWindows) :  holderWidth* r1;
				}
				else {
					w = (horz) ? (holderWidth / smallWindows) :  holderWidth;
					h = (horz) ? holderHeight : holderHeight/smallWindows;;
				}
				
				for (int i = 0; i < dw.size(); i++) {
					if (dw.get(i).getUserDisplayNode().isMinorDisplay() == false) continue;
					dw.get(i).setPaneLayout(x, y);
					dw.get(i).setPaneSize(w, h);
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
	 * Get the content holder which holds the toolbar pane and the main tab content pane. 
	 * @return the content pane. 
	 */
	public PamBorderPane getContentHolder(){
		return contentHolder;
	}
	
	
	/**
	 * Set the content holder which holds the toolbar pane and the main tab content pane.  
	 * @param contentToolBar - the content pane
	 */
	protected void setContentHolder(PamBorderPane contentHolder){
		this.contentHolder=contentHolder;
	}
	
	/**
	 * Get the content tool bar. This sits just below the tab
	 * @return the content tool bar for this tab. 
	 */
	public ToolBarPane getContentToolbar(){
		return toolbar;
	}
	
	/**
	 * Set the content tool bar. 
	 * @param contentToolBar - the content toolBar pane
	 */
	protected void setContentToolbar(ToolBarPane contentToolBar){
		this.toolbar=contentToolBar;
	}
	
	/**
	 * Check whether internal panes are currently editable. 
	 * @return true if internable panes are in editable mode. 
	 */
	public boolean getEditable() {
		return editable;
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