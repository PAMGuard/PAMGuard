package pamViewFX.fxNodes.navigationDrawer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Labeled;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamVBox;

/**
 * A pane which is somewhat related to a tab pane but where the tabs are on the
 * left or right and horizontal instead of vertical.
 */
public class NavigationDrawer extends PamBorderPane {
	
	private double leftInset=20;
		
    private ObservableList<Tab> tabs = FXCollections.observableArrayList();
    
    //the currently selected tab is null. 
    private Tab currentlySelectTab = null;
    
    PamVBox tabHolder;

	private NavSlectionModel selectionModel;
    
    /**
     * <p>The tabs to display in this TabPane. Changing this ObservableList will
     * immediately result in the NavigationDrawer updating to display the new contents
     * of this ObservableList.</p>
     *
     * <p>If the tabs ObservableList changes, the selected tab will remain the previously
     * selected tab, if it remains within this ObservableList. If the previously
     * selected tab is no longer in the tabs ObservableList, the selected tab will
     * become the first tab in the ObservableList.</p>
     * @return the list of tabs
     */
    public final ObservableList<Tab> getTabs() {
        return tabs;
    }
	
 
	public NavigationDrawer() {
		
		 tabHolder = new PamVBox(); 
		 tabHolder.setSpacing(0);
			
		 tabHolder.setPrefWidth(200);
		 
		 BorderPane.setMargin(tabHolder, new Insets(0,0,0,0));
			
//		 tabHolder.setStyle("-fx-background-color: -fx-darkbackground");
//		 tabHolder.setStyle("-fx-background-color: red;  -fx-padding: 0px 0px 0px 0px;");

		 this.setLeft(tabHolder);
		 
		 this.tabs.addListener((Change<? extends Tab> c) -> {
			 layoutNavigationDrawer(); 
		 });
		 
//		 this.setStyle("-fx-background-color: orange");
		 
		 //this is required for some styles ot make sure there are no insets. 
		 this.setStyle("-fx-padding: 0px 0px 0px 0px");

		 
		 selectionModel = new NavSlectionModel(); 
		 
		
	}
	
	/**
	 * Layout the navigation drawer. 
	 */
	private void layoutNavigationDrawer() {
		
//		System.out.println("LAYOUT NAVIGATION DRAWER:"); 
		
		tabHolder.getChildren().clear();
		for (int i = 0; i<tabs.size(); i++) {
			final int ii =i;
			PamButton button = new PamButton(); 
			button.setGraphic(tabs.get(i).getGraphic());
			button.setText(tabs.get(i).getText());
			styleButton(button);
			button.setOnAction((action)->{
				setNavigationTab(ii);
			});
			tabHolder.getChildren().add(button);
		}
		
		//make sure previous tab is selected. 
		if (currentlySelectTab!=null) {
			int ind = tabs.indexOf(currentlySelectTab);
			if (ind>=0) {
				setNavigationTab(ind);
			}
			else if (tabs.size()>0) {
				setNavigationTab(0);
			}
			else {
				this.setCenter(null);
			}
		}
		else if (tabs.size()>0) {
			setNavigationTab(0);
		}
		else {
			this.setCenter(null);
		}
	}
	
	public void setNavigationTab(int ii){
		//set all tabs to default style. 
		for (int i=0; i<tabHolder.getChildren().size(); i++){
			defaultControlStyle((Labeled) tabHolder.getChildren().get(i));
		}
		this.setCenter(tabs.get(ii).getContent());
		currentlySelectTab=tabs.get(ii); 
		selectedControlStyle((Labeled) tabHolder.getChildren().get(ii));
	}



	/**
	 * Style the button
	 * @param control
	 */
	public void styleButton(Labeled control){
		Insets buttonInsets=new Insets(0,leftInset,0,0);
		control.setAlignment(Pos.CENTER_LEFT);
		defaultControlStyle( control);
		control.setPadding(buttonInsets);
		control.setPrefWidth(Double.MAX_VALUE);
		control.setPrefHeight(40);
	}
	
	private void defaultControlStyle(Labeled control) {
		control.setStyle("-fx-alignment: center-left; -fx-background-color: transparent;   -fx-border-color: transparent;   -fx-background-radius: 0 0 0 0;  -fx-border-radius: 0 0 0 0;");
	}
	
	private void selectedControlStyle(Labeled control) {
		control.setStyle("-fx-alignment: center-left; -fx-background-color: -color-accent-6;   -fx-border-color: -color-accent-6;   -fx-background-radius: 0 0 0 0;  -fx-border-radius: 0 0 0 0;");
	}



	public NavSlectionModel getSelectionModel() {
		return selectionModel;
	}
	
	
	public class NavSlectionModel extends SingleSelectionModel<Tab>{

		@Override
		protected Tab getModelItem(int index) {
			return tabs.get(index);
		}

		@Override
		protected int getItemCount() {
			return tabs.size();
		}
		
	}
	
	
	
	

}


