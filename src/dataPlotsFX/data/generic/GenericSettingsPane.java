package dataPlotsFX.data.generic;


import PamView.symbol.PamSymbolManager;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDSettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.symbol.FXSymbolOptionsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;



/**
 * Settings pane which holds the symbol options from the data block of the plot info. 
 * @author Jamie Macaulay
 *
 *
 * @author Jamie Macaulay 
 *
 */
public class GenericSettingsPane extends PamBorderPane implements TDSettingsPane {

	/**
	 *
	 */
	private static final double PREF_WIDTH = 300;

	/*
	 * The raw clip info. 
	 */
	private TDDataInfoFX tdDataInfoFX;

	/**
	 * The icon for the pane. 
	 */
	private Node icon = new Canvas(20,20); 


	private PamVBox holder;

	private boolean disableGetParams;

	private String showingName = "Clip Settings";

	private FXSymbolOptionsPane<?> symbolOptionsPane;

	/**
	 * The clip plot pane. 
	 */
	public GenericSettingsPane(TDDataInfoFX tdDataInfoFX){
		this.tdDataInfoFX = tdDataInfoFX; 
		createPane();
		this.setPrefWidth(PREF_WIDTH);
		setParams(); 

	}

	public void createPane() {
		holder = new PamVBox(); 
		holder.setSpacing(5);
		holder.getChildren().addAll( createSymbolOptionsPane().getContentNode());
		this.setPadding(new Insets(5,10,5,10)); 

		this.setCenter(holder);
	}

	
	private void newSettings() {
		newSettings(0);
	}

	/**
	 * There are new settings. Repaints the graph. 
	 * @param milliswait
	 */
	private void newSettings(long milliswait) {
		getParams();

		this.tdDataInfoFX.getTDGraph().repaint(milliswait);
	}


	/**
	 * Set the paramters. 
	 */
	public void setParams() {
		disableGetParams = true; 
		 
		disableGetParams = false; 
	}


	/**
	 * Get parameters. 
	 */
	private void getParams() {

		if (!disableGetParams) {

		}

	}
	
	/**
	 * Create the symbol options pane. 
	 * @return the symbol options pane. 
	 */
	private StandardSymbolOptionsPane createSymbolOptionsPane(){

		PamSymbolManager<?> pamSymbolManager=  tdDataInfoFX.getDataBlock().getPamSymbolManager();

		symbolOptionsPane= pamSymbolManager.getFXOptionsPane(tdDataInfoFX.getTDGraph().getUniqueName(), 
				tdDataInfoFX.getTDGraph().getGraphProjector()); 

		//create a new settings listener
		symbolOptionsPane.addSettingsListener(()->{
			newSettings();
		});
		
		//		symbolOptionsPane.getLinBox().prefWidthProperty().bind(this.widthProperty());
		//symbolOptionsPane.getLinBox().add(symbolOptionsPane.getLineColorPicker(), 1, 1);

		return (StandardSymbolOptionsPane) symbolOptionsPane;
	}


	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return showingName;
	}
	
	/**
	 * Set the name on the pane
	 * @param showingName - the name that will show in the pane. 
	 */
	public void setShowingName(String showingName) {
		this.showingName= showingName; 
	}

	@Override
	public Node getShowingIcon() {
		return null;
	}
	
	public void setIcon(Node showingIcon) {
		this.icon= showingIcon; 
	}

	@Override
	public Pane getPane() {
		return this;
	}

}

