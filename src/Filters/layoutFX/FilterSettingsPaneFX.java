package Filters.layoutFX;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.FilterPaneFX;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import Filters.FilterControl;
import Filters.FilterParameters_2;
import Filters.FilterParams;
import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamguardMVC.PamRawDataBlock;

/**
 * Settings pane for filter module. Basically just a source pane and a filter pane
 * @author Jamie Macaulay
 *
 */
public class FilterSettingsPaneFX extends SettingsPane<FilterParameters_2>{
	
	/**
	 * Reference to the source selection pane. 
	 */
	private SourcePaneFX sourcePane;
	
	/**
	 * Reference to the filter control
	 */
	private FilterControl filterControl;
	
	/**
	 * Reference to generic filter pane. 
	 */
	private FilterPaneFX filterPaneFX;

	/**
	 * Cloned copy of current filter params;
	 */
	private FilterParameters_2 filterParams_2;
	
	/**
	 * The main content pane. 
	 */
	private PamBorderPane mainPane; 

	public FilterSettingsPaneFX(FilterControl filterControl){
		super(null); 
		this.filterControl=filterControl; 
		mainPane=new PamBorderPane(); 
		filterPaneFX=new FilterPaneFX();
		filterPaneFX.getControlPane().getChildren().add(0, 	createFilterPane());
		((Region) filterPaneFX.getContentNode()).setPadding(new Insets(5,0,0,0));
		mainPane.setCenter(filterPaneFX.getContentNode());
	}
	
	/**
	 * Create the main filter pane. 
	 * @return the filter pane. 
	 */
	private Pane createFilterPane(){
		
		PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);
	
		sourcePane = new SourcePaneFX( "Raw data source for Filter", RawDataUnit.class, true, false);
		vBox.getChildren().add(sourcePane);
		
		//add generic filter settings pane
		return vBox; 
	}

	@Override
	public FilterParameters_2 getParams(FilterParameters_2 filterParams2) {
		
		FilterParams filterParams=filterPaneFX.getParams(filterParams_2.filterParams);
		
		if (filterParams==null) return null; 
		else filterParams_2.filterParams=filterParams;
		
		filterParams_2.rawDataSource = sourcePane.getSourceName();
//		System.out.println("Get params Filter source pane channel map: "+sourcePane.getChannelList());
		filterParams_2.channelBitmap = sourcePane.getChannelList();
		
		return filterParams_2;
	}

	@Override
	public void setParams(FilterParameters_2 filterParams_2) {
		this.filterParams_2=filterParams_2.clone();
		
//		System.out.println("Set source pane channel map: "+this.filterParams_2.channelBitmap);

//		System.out.println("rawDataSource filter: "+input.rawDataSource);
		PamRawDataBlock rawDataBlock = PamController.getInstance().getRawDataBlock(this.filterParams_2.rawDataSource);
		
		//System.out.println("Filtered datablock: "+rawDataBlock);
		sourcePane.clearExcludeList();
		sourcePane.excludeDataBlock(filterControl. getFilterProcess().getOutputDataBlock(0), true);
		
		sourcePane.setSource(rawDataBlock);
		System.out.println("Set source pane channel map: "+this.filterParams_2.channelBitmap);
		sourcePane.setChannelList(this.filterParams_2.channelBitmap);
		
		filterPaneFX.setParams(this.filterParams_2.filterParams);
	}

	@Override
	public String getName() {
		return "Filter Parameters";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		filterPaneFX.paneInitialized();
	}



}
