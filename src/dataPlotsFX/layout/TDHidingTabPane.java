package dataPlotsFX.layout;

import pamViewFX.fxNodes.hidingPane.HidingTab;
import pamViewFX.fxNodes.hidingPane.HidingTabPane;
import javafx.scene.control.Label;

public class TDHidingTabPane extends HidingTabPane {

	/**
	 * Reference to the tdGraph. 
	 */
	private TDGraphFX tdGraphFX;

	public TDHidingTabPane(TDGraphFX tdGraphFX) {
		super();
		this.tdGraphFX=tdGraphFX;
	}
	
	/**
	 * Add a settings pane to the hiding tab. 
	 * @param settingsPane- the settings pane to add. 
	 */
	public void addSettingsPane(TDSettingsPane settingsPane){
		//create showing tab
		Label showingTab;
		if (settingsPane.getShowingIcon()!=null) showingTab = new Label(settingsPane.getShowingName(),settingsPane.getShowingIcon());
		else showingTab = new Label(settingsPane.getShowingName());
		
		//create hiding tab
		Label hidingLabel;
		if (settingsPane.getHidingIcon()!=null) hidingLabel = new Label("",settingsPane.getHidingIcon());
		else hidingLabel = new Label("||");
	
		HidingTab hideTab = new HidingTab(settingsPane.getPane() ,this, showingTab, hidingLabel);		
		
		this.getTabs().add(hideTab); 
		//make sure heights are sorted out 
		workOutHeight();
	}
	
	/**
	 * Calculate the height of the tabs. This is the tab content
	 * with the largest tab. 
	 */
	private void workOutHeight() {

		double prefHeight=-1; 
		for (int i=0; i<this.getTabs().size(); i++) {
			if (this.getTabs().get(i).getContentPane().getPrefHeight()>prefHeight) {
				prefHeight=this.getTabs().get(i).getContentPane().getPrefHeight();
			}
			if (this.getTabs().get(i).getContentPane().getMinHeight()>prefHeight) {
				prefHeight=this.getTabs().get(i).getContentPane().getMinHeight();
			}
		}
		
		this.setMinHeight(prefHeight);
	}
	
	/**
	 * Get the TDGraphFX associated with the hiding pane
	 * @return - the TDGraphFX associated with the hiding pane.
	 */
	public TDGraphFX getTDGraph() {
		return tdGraphFX;
	}


}
