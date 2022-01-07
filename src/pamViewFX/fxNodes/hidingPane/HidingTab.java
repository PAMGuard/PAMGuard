package pamViewFX.fxNodes.hidingPane;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * A hiding tab is is used in a HidingTabPane  
 * @author Jamie Macaulay
 *
 */
public class HidingTab {

	/**
	 * Holds the tab content. e.g the pane to display in the open tab. 
	 */
	private Region contentPane;

	/**
	 * Label to show when the tab is hiding- should significantly shorter than the label used when 
	 * the tab is open, e.g. a small Icon and no text. 
	 */
	private Label hideTabName;

	/**
	 * The label to show when the tab is open. 
	 */
	private Label tabName;
	
	/**
	 * Check if the tab is showing or hidden. True if the tab is shwoing.  
	 */
	private boolean showing=false;

	/**
	 * The hide hiding tab
	 */
	private Pane hideTab;
	
	/**
	 * The show tab is located at the top of the content pane and allows the 
	 * user to close the tab.
	 */
	private Pane showTab;

	/**
	 * The holder pane for the tab-this shows the hide and show tab panes and is the pane used in the hidingTabPane. 
	 */
	private PamBorderPane tabPane; 
	
	/**
	 * The HidingTabPane this tab belongs to. 
	 */
	private HidingTabPane hidingTabPane; 

	/**
	 * The default width of an open tab. Only used if a content node has no preferred or min size; 
	 */
	private static double defaultTabWidth=200;
	
	/**
	 * The default width of a closed tab. Only used if a hiding node label has no preferred or min size; 
	 */
	private static double defaultTabHideWidth=30;
	
	//Animation
	public Timeline timeLineShow;

	private Timeline timeLineHide;

	private long duration=200; 
	
	
	public HidingTab(Pane contentPane, Label tabName, Label hideTabName) {
		this.contentPane=contentPane; 
		this.tabName=tabName; 
		this.hideTabName=hideTabName; 
		createHideTab();
		createShowTab();
		tabPane=new PamBorderPane(); 
		showTab(false); //initialize the tab- adding the hide tab initially. 
		//HBox.setHgrow(tabPane, Priority.ALWAYS);
		setAnimation();
	}
	

	public HidingTab(Region contentPane, HidingTabPane hidingTabPane, Label tabName, Label hideTabName) {
		this.contentPane=contentPane; 
		this.hidingTabPane=hidingTabPane;
		this.tabName=tabName; 
		this.hideTabName=hideTabName; 
		createHideTab();
		createShowTab();
		tabPane=new PamBorderPane(); 
		//HBox.setHgrow(tabPane, Priority.ALWAYS);
		setAnimation();
		showTab(false); //initialize the tab- adding the hide tab initially. 
	}
	
	
	/**
	 * Create the hide tab. This is the tab-like pane that sits on top of the content pane when the tab is showing. 
	 * Allows users to hide the tab 
	 */
	private void createHideTab(){
		PamBorderPane hideTab=new PamBorderPane();
		PamHBox hbox=new PamHBox();
		hbox.setPadding(new Insets(5, 0, 0, 5));
		hbox.getChildren().add(this.getHideTabName());
		hideTab.setTop(hbox);
		hideTab.setOnMouseEntered(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	hideTab.setId("hide-tab-highlight");
		    }
		});
		hideTab.setOnMouseExited(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	hideTab.setId("hide-tab");
		    }
		});
		hideTab.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	showTab(true);
		    }
		});
		hideTab.setId("hide-tab");
		hideTab.setPrefWidth(30);
		this.hideTab=hideTab;
	}
	
	/**
	 * Creates the tab. This is basically a holder for the slightly larger 
	 * label of the show tab but can be overriden to add more functionality to the tab. 
	 */
	private void createShowTab(){
		PamBorderPane showTab=new PamBorderPane(); 
		showTab.setCenter(this.getTabName()); 
		showTab.setPrefHeight(30);
		showTab.setOnMouseEntered(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	showTab.setId("hide-tab-highlight");
		    }
		});
		showTab.setOnMouseExited(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	showTab.setId("show-tab");
		    }
		});
		showTab.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		    	showTab(false);
		    }
		});
		showTab.setId("show-tab");
		this.showTab=showTab;
	}
	
	/**
	 * Show the tab or hide tab. Note this should only be called if the tab has been added to the tab list. 
	 * @param show- true to show the tab;
	 * @param tab -the tab to show. 
	 */
	public void showTab(boolean show){
		//tell the tab it is now showing or hiding
		setShowing(show);
		//start the animation to show the tab. 
		if  (show) {
			timeLineShow.play();
		}
		else {
			tabPane.setTop(null);
			tabPane.setCenter(null);
			timeLineHide.play();
		}
		

	}
	
	/**
	 * Get the size of the tab. This is it's preferred size as animation deals with prefWidthproperty. 
	 * @return the tab width.
	 */
	public double getTabWidth(){
		//System.out.println("Tab width: "+tabPane.getPrefWidth());
		return tabPane.getPrefWidth();
	}
	
	/**
	 * Get the width of the tab when it is fully showing. - this is not the same as the current width 
	 * @return the width of the tab when it is shwoing,. 
	 */
	private double getShowingTabWidth(){
		double tempWidth;
		tempWidth=getContentPane().getPrefWidth();
		if (tempWidth<=0) tempWidth=getContentPane().getMinWidth();
		if (tempWidth<=0) tempWidth=defaultTabWidth; 
		return tempWidth; 
	};
	
	/**
	 * Get the width of the hiding tab when hiding. Note this is not the current width but the the width the tab should be when it is hidden.
	 * @return the width of the hiding tab
	 */
	private double getHidingTabWidth(){
		double tempWidth;
		tempWidth=getHideTabName().getPrefWidth();
		if (tempWidth<=0) tempWidth=getHideTabName().getMinWidth();
		if (tempWidth<=0) tempWidth=defaultTabHideWidth; 
		return tempWidth; 
	};
	
	

	
	/**
	 * Set up the animation that shows tabs opening and closing. This is similar to the hiding pane animation but 
	 */
	protected void setAnimation(){
		
		// Animation for tab SHOW.
		timeLineShow = HidingPane.createAnimation(getTabPane().prefWidthProperty(),  getShowingTabWidth(),  duration);
		timeLineShow.setOnFinished(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent arg0) {
            	showFinished();
            }
        });

		// Animation for tab HIDE.
		timeLineHide= HidingPane.createAnimation(getTabPane().prefWidthProperty(),  getHidingTabWidth(),  duration);
		timeLineHide.setOnFinished(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent arg0) {
            	hideFinished();
            }
        });
		
		//Resize the hiding tab pane this tab belongs to as the tab resizes. 
		tabPane.prefWidthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	if (hidingTabPane!=null) hidingTabPane.widthChanged(); 
		    }
		});
	}
	
	/**
	 * Called whenever the show animation is finished. Adds the conetn pane. 
	 */
	private void showFinished() {
		tabPane.setTop(getShowTab());
		tabPane.setCenter(getContentPane());
		if (hidingTabPane!=null) hidingTabPane.widthChanged(); 
	}
	
	/**
	 * Called whenever the hide animation has finished; 
	 */
	private void hideFinished() {
		tabPane.setCenter(getHideTab());
		if (hidingTabPane!=null) hidingTabPane.widthChanged(); 
	}
	
	/**
	 * Get mouse clicked property for the Hide Tab. This is used whenever any section of the hide tab is clicked; 
	 * @return
	 */
	public EventHandler<? super MouseEvent> getHideTabClickProperty(){
		return hideTab.getOnMouseClicked();
	}
	
	/**
	 * Get mouse clicked property for the Show Tab. This is used whenever the top tab in the showing tab pane is clicked. 
	 * @return
	 */
	public EventHandler<? super MouseEvent> getShowTabClickProperty(){
		return showTab.getOnMouseClicked();
	}
	
	/**
	 * Get the Node that contains the tab content
	 * @return node that contains the tab content when tab is open. 
	 */
	public Region getContentPane() {
		return contentPane;
	}

	/**
	 * Set the the Node that contains the tab content
	 * @param contentNode- the node to show when the tab is open
	 */
	public void setContentNode(Pane contentNode) {
		this.contentPane = contentNode;
	}

	/**
	 * Get the label that is displayed when the tab is closed. 
	 * @return the label that is displayed when the tab is closed
	 */
	public Label getHideTabName() {
		return hideTabName;
	}

	/**
	 * Set the label that is displayed when the tab is closed. 
	 * @param hideTabName- the label on the tab which shows when the tab is closed. 
	 */
	public void setHideTabName(Label hideTabName) {
		this.hideTabName = hideTabName;
	}

	/**
	 * Get the tab label which shows when the tab is open. 
	 * @return -the label on the tab which shows when the tab is open. 
	 */
	public Label getTabName() {
		return tabName;
	}


	/**
	 * Set the label that is displayed when the tab is open. 
	 * @param hideTabName- the label on the tab which shows when the tab is open. 
	 */
	public void setTabName(Label tabName) {
		this.tabName = tabName;
	}
	
	/**
	 * Check whether the tab is showing
	 * @return true of the tab is currently open and showing. 
	 */
	public boolean isShowing() {
		return showing;
	}

	/**
	 * Set whether the tab should show or not. 
	 * @param showing
	 */
	public void setShowing(boolean showing) {
		this.showing = showing;
	}
	
	/**
	 * Get the  hiding tab node. 
	 * @return the hiding tab. 
	 */
	public Pane getHideTab() {
		return hideTab;
	}

	/**
	 * Get the tab which shows when the tab pane is open- this is located on top of the content pane and allows users to close the tab. 
	 * @return the show tab, that is the tab that sits above the content pane when the tab is showing.   
	 */
	public Pane getShowTab() {
		return showTab;
	}
	
	/**
	 * Get the tab pane . The tab pane contains all tab nodes and changes size on tab open/close.
	 * @return the tab pane.
	 */
	public PamBorderPane getTabPane(){
		return tabPane;
	}

	
	public HidingTabPane getHidingTabPane() {
		return hidingTabPane;
	}

	public void setHidingTabPane(HidingTabPane hidingTabPane) {
		this.hidingTabPane = hidingTabPane;
	}


	


}
