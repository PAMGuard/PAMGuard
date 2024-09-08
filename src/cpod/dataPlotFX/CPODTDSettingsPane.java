package cpod.dataPlotFX;

import PamView.symbol.PamSymbolManager;
import dataPlotsFX.layout.TDSettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

/**
 * The settings pane for CPOD detections on the TDGraphFX. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODTDSettingsPane implements TDSettingsPane {
	
	/**
	 * Reference to the CPOD data info
	 */
	private CPODPlotInfoFX cpodPlotInfoFX;
	
	/**
	 * The main settings pane. 
	 */
	private Pane mainPane;

	/**
	 * The symbol options pane for the CPOD. 
	 */
	private StandardSymbolOptionsPane symbolOptionsPane;

	/**
	 * The data selection pane. 
	 */
	private DynamicSettingsPane<Boolean> dataSelectPane; 

	public CPODTDSettingsPane(CPODPlotInfoFX cpodPlotInfoFX) {
		this.cpodPlotInfoFX=cpodPlotInfoFX; 
		mainPane = createMainPane(); 
		mainPane.setPrefWidth(400);
	}

	/**
	 * Create the main pane. 
	 * @return the main pane. 
	 */
	private Pane createMainPane() {
	
		PamBorderPane mainPane = new PamBorderPane(); 
		
		TabPane tabPane = new TabPane(); 
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		tabPane.getTabs().add(new Tab("Symbol" , createSymbolOptionsPane().getContentNode())); 
		
		
		//the data selct pane. 
		
		dataSelectPane = createDataSelectPane();
		dataSelectPane.addSettingsListener(()->{
			//dynamic settings pane so have to repaint whenever a control is selected. 
			//getParams();
			cpodPlotInfoFX.getTDGraph().repaint(50);
		});
		dataSelectPane.setParams(true);
		
		Pane dataSelectSettignsPane = (Pane) dataSelectPane.getContentNode(); 
		dataSelectSettignsPane.setPadding(new Insets(5,5,5,5));
		
		PamVBox dataselectHolder = new PamVBox(); 
		dataselectHolder.setSpacing(5); 
		
		Label dataSelectLabel = new Label("Filter detection"); 
		PamGuiManagerFX.titleFont2style(dataSelectLabel);
		
		dataselectHolder.getChildren().addAll(dataSelectLabel, dataSelectSettignsPane); 

		tabPane.getTabs().add(new Tab("Data", dataselectHolder)); 

		mainPane.setCenter(tabPane);
		
		return mainPane;
	}
	
	
	/**
	 * The create data selector pane from the click data block.  
	 * @return the data select pane. 
	 */
	private DynamicSettingsPane<Boolean> createDataSelectPane(){		
		return cpodPlotInfoFX.getCPODDataSelector().getDialogPaneFX();
	}

	/**
	 * Create the symbol options pane. 
	 * @return the symbol options pane. 
	 */
	private StandardSymbolOptionsPane createSymbolOptionsPane(){

		PamSymbolManager<?> pamSymbolManager=  cpodPlotInfoFX.getDataBlock().getPamSymbolManager();

		//StandardSymbolManager standardSymbolManager = (StandardSymbolManager) pamSymbolManager; 
		//System.out.println("HAS LINE SYMBOL: " +  standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH) 
		//+ "  " + standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE) ); 
		symbolOptionsPane= (StandardSymbolOptionsPane)  pamSymbolManager.getFXOptionsPane(cpodPlotInfoFX.getTDGraph().getUniqueName(), 
				cpodPlotInfoFX.getTDGraph().getGraphProjector()); 

		//		//remove the line box. 
		//		symbolOptionsPane.getVBoxHolder().getChildren().remove(symbolOptionsPane.getLinBox()); 
		//		
		//create a new settings listener
		symbolOptionsPane.addSettingsListener(()->{
			//cpodPlotInfoFX.getClickSymbolChooser().notifySettingsChange(); 
			newSettings();
		});
		
		return symbolOptionsPane; 
	}
	

	/**
	 * Called whenever there are new settings.  
	 */
	private void newSettings() {
		cpodPlotInfoFX.getTDGraph().repaint(50); 
	}

	@Override
	public Node getHidingIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShowingName() {
		return "CPOD detections";
	}

	@Override
	public Node getShowingIcon() {
		return null;
	}

	@Override
	public Pane getPane() {
		// TODO Auto-generated method stub
		return mainPane;
	}

}
