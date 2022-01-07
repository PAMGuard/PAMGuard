package pamViewFX.fxNodes.hidingPane;

import java.util.ArrayList;
import java.util.Collection;

import pamViewFX.fxNodes.PamHBox;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;

/**
 * The hiding tab pane has tabs which collapse in size rather than the tradition tab panes in which switching a tab simply 
 * removes the current tab from the pane. The advantage of this system is that you can show two tabs side by side. 
 * <p>
 * This disadvantage is that the tab pane changes size and this must be handled by the application. Generally this pane should be inside a holder pane 
 * which is resized every time the width of the hiding tab pane is resized. This can be done by settings  a holder pane. e.g. 
 * <p>
 * 	settingsTabs=new HidingTabPane();
 * <p>
 *  HidingPane settingsPane=addHidingPane( settingsTabs, tdMainDisplay.settingsIconGrey, 0, Side.RIGHT, Pos.TOP_RIGHT);
 *  <p>
 *	settingsTabs.setHolderPane(settingsPane);
 * <p><p>
 * Note that the holder pane should not have any other components other than the tab pane as the tab pane changes the holder panes width to its width. Hence if there are other
 * nodes the tab pane can become distorted. 
 *
 * @author Jamie Macaulay
 */
public class HidingTabPane extends PamHBox {
	
	/**
	 * Array of tabs.
	 */
	private TabList tabs;
	
	/**
	 * The tab pane can either be on the left or right of a pane. If to the right the panes stack and open towards the left and vice versa. 
	 */
	//private Side side= Side.RIGHT;

	/**
	 * The holder pane for the tabbed pane. As the size of the tabbed pane changes in size the content 
	 * pane might need resized. If not null then the height/width of the content pane is set to the 
	 * size of the tabbed pane. 
	 */
	private Pane holderPane;
	
	/**
	 * Extra (left) start spacing
	 */
	private DoubleProperty startSpacing=new SimpleDoubleProperty();


	/**
	 * Extra end (right) spacing for the tab pane if needed
	 */
	private DoubleProperty endSpacing=new SimpleDoubleProperty();


	/**
	 * Creates a tab
	 */
	public HidingTabPane(){
//		this.setSpacing(20);
		tabs=new TabList();
		this.setMaxWidth(Double.MAX_VALUE);

		//TEST-create a series of tabs//
//		SpectrogramControlPane specControl=new SpectrogramControlPane(null,null);
//		specControl.setPrefWidth(400);
//		HidingTab hideTab1=new HidingTab(specControl, this, new Label("My Tab"), new Label("0"));
//		HidingTab hideTab2=new HidingTab(new SpectrogramControlPane(null,null), this, new Label("My Tab1"), new Label("1hlo"));
//		HidingTab hideTab3=new HidingTab(new SpectrogramControlPane(null,null), this, new Label("My Tab2"), new Label("2"));
//		HidingTab hideTab4=new HidingTab(new SpectrogramControlPanel(null,null), this, new Label("My Tab4"), new Label("3"));
//		HidingTab hideTab5=new HidingTab(new SpectrogramControlPanel(null,null), this, new Label("My Tab5"), new Label("4"));
//		tabs.add(hideTab1);
//		tabs.add(hideTab2);
//		tabs.add(hideTab3);
//		tabs.add(hideTab4);
//		tabs.add(hideTab5);
//		PamButton button1 = new PamButton("Hello1"); 
//		button1.setOnAction(new EventHandler<ActionEvent>() {
//		    @Override public void handle(ActionEvent e) {
//		       THIS.getChildren().add(new SpectrogramControlPanel(null,null));
//		       System.out.println("TabHidingPane width:"+THIS.getWidth());
//		       THIS.setWidth(THIS.getChildren().size()*250.);
//		       if (holderPane!=null ) holderPane.setPrefSize(THIS.getWidth(),THIS.getHeight());
//		    }
//		});
//		this.setPrefWidth(1000);
//		HBox.setHgrow(hideTabS, Priority.ALWAYS);
//		HBox.setHgrow(showTabS, Priority.ALWAYS);
		//TEST//
		
		widthChanged();
	}
	

	/**
	 * Called whenever the width of pane may have changed e.g. when a tab is showing
	 */
	protected void widthChanged(){
		//resize the tab pane.
		double width=calcTabPaneWidth();
		resizeTabPane(width, -1);
	}
	
	/**
	 * Called whenever a tab is added to the tab list. 
	 * @param tab the hiding tab which is being added
	 */
	private void addTab(HidingTab tab){
		getChildren().add(tabs.indexOf(tab),tab.getTabPane());
		tab.setHidingTabPane(this); //this is important so the tab can call to resize the pane. 
		widthChanged();
	}
	
	/**
	 * Called whenever a tab is removed from the tab list. 
	 * @param tab the tab to remove. 
	 */
	private void removeTab(HidingTab tab){
		getChildren().remove(tabs.indexOf(tab));
		widthChanged();
	}
	
	/**
	 * Remove all tabs from the hiding tab list; 
	 */
	private void removeAllTabs(){
		for (int i=0; i<tabs.size(); i++){
			this.removeTab(tabs.get(0));
		} 
	}
	
	/**
	 * Calculate the size of the hBoxPane based on the number of tabs and whether they are open /closed. 
	 * Calculate size based on tab size.
	 * @return double -expected size and height. 
	 */
	private double calcTabPaneWidth(){
		double width=0; 
		for (int i=0; i<tabs.size(); i++){
			width+=tabs.get(i).getTabWidth(); 
		} 
		//System.out.println("Tab total width: "+width+" nTabs: "+tabs.size());

		return width; 
	};

	/**
	 * Resize the pane, passing a note to the content pane, if not null, that the tab pane has resized. 
	 */
	private void resizeTabPane(double width, double height){
	     if (width>0) this.setPrefWidth(width);
	     if (height>0) this.setPrefHeight(height);
	     if (holderPane!=null ){
	    	 holderPane.setPrefSize(width+endSpacing.getValue()+startSpacing.getValue(),height);
	    	 this.setPadding(new Insets(0,endSpacing.getValue(),0,startSpacing.getValue()));
	     }
	}
	
	/**
	 * Array which holds the tabs. 
	 * @author Jamie Macaulay
	 *
	 */
	public class TabList extends ArrayList<HidingTab>{

		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(HidingTab tab){
			super.add(tab);
			addTab(tab);
			return true;
		}
		
		@Override
		public HidingTab remove(int index){
			HidingTab tab=super.remove(index);
			removeTab(tab);
			return tab; 
		}
		
		
		@Override
		public boolean removeAll(Collection<?> c){
			removeAllTabs();
			return super.removeAll(c);
		}
		
	}

	/**
	 * Get the holder pane- the pane in which the tab pane sits.  
	 * @return the holder pane. 
	 */
	public Pane getHolderPane() {
		return holderPane;
	}

	/**
	 * Set the holder pane- the holder pane can be null. 
	 * @param holderPane - the holder pane. 
	 */
	public void setHolderPane(Pane holderPane) {
		this.holderPane = holderPane;
	}
	
	/**
	 * Get the start spacing property. This is extra spacing to the lef tof the tab pane. 
	 * @return start spacing property. 
	 */
	public DoubleProperty startSpacingProperty() {
		return startSpacing;
	}
	
	/**
	 * Set extra left spacing for the tab pane. 
	 * @param spacing in pixels
	 */
	public void setStartSpacing(double spacing) {
		this.startSpacing.setValue(spacing);
	}
	
	/**
	 * Get the extra spacing property at the end of the tab pane. Use this if the holder pane has extra components
	 * e.g. say that there a 30 pixel side pane in the holding pane. To keep that pane out of the process of the hiding pane
	 * resizing then the spacing value should be set 30.0;
	 * @return end spacing property. 
	 */
	public DoubleProperty endSpacingProperty() {
		return endSpacing;
	}

	/**
	 * Set extra right spacing for the tab pane. 
	 * @param spacing in pixels
	 */
	public void setEndSpacing(double spacing) {
		this.endSpacing.setValue(spacing);
	}
	
	/**
	 * Get all the tabs for this hiding tab pane. 
	 * @return tabs in the pane. 
	 */
	public TabList getTabs(){
		return tabs; 
	}
	


}
