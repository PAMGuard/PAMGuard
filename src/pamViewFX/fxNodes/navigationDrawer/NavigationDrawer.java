package pamViewFX.fxNodes.navigationDrawer;

import java.util.ArrayList;

import com.sun.javafx.scene.control.TabObservableList;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Labeled;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;

/**
 * A pane which is somewhat related to a tab pane but where the tabs are on the
 * left or right and horizontal instead of vertical.
 */
public class NavigationDrawer extends PamBorderPane {
	
	private double leftInset=60;
	private double heightSpacer=2; 
	
    private ObservableList<Tab> tabs = new TabObservableList<>(new ArrayList<>());
    
    PamVBox tabHolder;

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
			
		 tabHolder.setPrefWidth(250);
			
		 tabHolder.setStyle("-fx-background-color: -fx-darkbackground");
		 
		 this.setLeft(tabHolder);
		 
//TODO		
	}
	
	
	public void styleButton(Labeled control){
		Insets buttonInsets=new Insets(0,leftInset,0,leftInset);
		control.setAlignment(Pos.CENTER_LEFT);
		control.setStyle("-fx-alignment: center-left;");
		control.setPadding(buttonInsets);
		control.getStyleClass().add("square-button-trans");
		control.setPrefWidth(Double.MAX_VALUE);
		control.setPrefHeight(40);
	}
	
	
	
	
	

}


