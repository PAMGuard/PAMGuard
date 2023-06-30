package pamViewFX.fxNodes;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;

/**
 * Tab pane which allows for detachable tabs.
 * @author Jamie Macaulay
 *
 */
public class PamTabPane extends TabPane {
	
	private PamTabPaneSkin tabPaneSkin;
	
	/**
	 * Region which sits at the start of the tab pane in tab header area (to left if horizontal or top if vertical).
	 */
	private Region tabStartRegion;

	/**
	 * Region which sits at the end of the tab pane in tab header area (to right if horizontal or bottom if vertical).
	 */
	private Region tabEndRegion;
	
	/**
	 * True if a button exists in tab header to add new tabs.
	 */
	private boolean addTabButton=false; 
	

	public PamTabPane() {
		super();
		addTabButton=true;
		tabPaneSkin = new PamTabPaneSkin(this);
	}

	public PamTabPane(Tab... arg0) {
		super(arg0);
		tabPaneSkin = new PamTabPaneSkin(this);
	}
	

	@Override
	protected Skin<?> createDefaultSkin() {
		if (tabPaneSkin==null ) tabPaneSkin = new PamTabPaneSkin(this);
	    return tabPaneSkin;
	}   
	
	
	/**
	 * Get the start region for this PamTabPanwe. The start region sits on the left side/top of the tab pane.
	 * @return the tab start region. null if there is no region. 
	 */
	public Region getTabStartRegion() {
		return tabStartRegion;
	}

	/**
	 * Set the start region for this PamTabPanwe. The start region sits on the left side/top of the tab pane.
	 * @param the tab start region. Set to null to remove current region. 
	 */
	public void setTabStartRegion(Region tabStartRegion) {
		if (tabPaneSkin!=null) tabPaneSkin.removeTabStartRegion(this.tabStartRegion);
		this.tabStartRegion = tabStartRegion;
	}


	/**
	 * Get the end region for this PamTabPanwe. The end region sits on the right side/bottom of the tab pane.
	 * @return the tab end region. null if there is no region. 
	 */
	public Region getTabEndRegion() {
		return tabEndRegion;
	}

	/**
	 * Set the end region for this PamTabPanwe. The end region sits on the right side/bottom of the tab pane.
	 * @param the tab end region. Set to null to remove current region.  
	 */
	public void setTabEndRegion(Region tabEndRegion) {
		if (tabPaneSkin!=null) tabPaneSkin.removeTabEndRegion(this.tabEndRegion);
		this.tabEndRegion = tabEndRegion;
	}
	
	/**
	 * Check if the button to add additional tabs is showing. This sits after the last added tab. 
	 * @return true if the button to add tabs is showing. 
	 */
	public boolean isAddTabButton() {
		return addTabButton;
	}

	/**
	 * Set whether a button shows to add tabs to the TabPane
	 * @param addTabButton - true to show a button next to the last tab which allows new tabs to be added. 
	 */
	public void setAddTabButton(boolean addTabButton) {
		this.addTabButton = addTabButton;
		tabPaneSkin = new PamTabPaneSkin(this);
	}
	
	/**
	 * Get the height in pixels of the header. 
	 * @return  the height of the header in pixels. 
	 */
	public double getHeaderHeight(){
		return tabPaneSkin.getHeaderHeight();
	}

	/**
	 * Get the height property for the tab pane header (area where all the tabs are).
	 * @return the height property for the header area. 
	 */
	public ReadOnlyDoubleProperty getHeaderHeightProperty() {
		return tabPaneSkin.getHeaderHeightProperty();
	}
	
	/**
	 * Get the add tab button.
	 * @return the button which adds the tabs to tab pane. 
	 */
	public PamButton getAddTabButton(){
		return tabPaneSkin.getAddTabButton();
	}
	
	/**
	 * Reset the size of the tab pane to fit the smallest of it's tabs and have space 
	 * for the tab bar. Shouldn't be necessary, but for some reason, tabs don't resize
	 * after their initial creation even if the size of their content changes. 
	 * @return new height of the tab pane. 
	 */
	public double repackTabs() {
		ObservableList<Tab> tabs = getTabs();
		double minHeight = 0;
		for (int i = 0; i < tabs.size(); i++) {
			Tab tab = tabs.get(i);
			Node tabCont = tab.getContent();
			minHeight = Math.max(minHeight, tabCont.prefHeight(0));
		}
		minHeight += getTabMaxHeight()+8;
//		System.out.println("Tab bit extra height = " + getTabMaxHeight());
		setMinHeight(minHeight);
		setMaxHeight(minHeight);
		setTabMaxHeight(minHeight);
		setTabMinHeight(minHeight);
		return minHeight;
	}
	
	
}
