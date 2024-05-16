package pamViewFX.fxNodes;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

//import com.sun.javafx.scene.control.skin.TabPaneSkin;

/**
 * The PamTabPane skin adds a few extra features to the standard TabPane:
 * <p>
 * 1) The ability to have a button which adds new tabs.
 * <p>
 * 2) Regions (e.g. buttons, labels etc.) Can be added directly into the tab pane. The regions sit at the start (left/top) and end (right/bottom) of the header pane, which sits at the top of the tab pane. 
 * @author Jamie Macaulay
 */
public class PamTabPaneSkin extends TabPaneSkin {

	/**
	 * The pane which holds the entire tab area. 
	 */
	private StackPane headerArea; 

	/**
	 * Reference to the PamTabPane for this skin. 
	 */
	private PamTabPane pamTabPane;

	/**
	 * Area which contains the tabs. 
	 */
	private StackPane tabContentArea;

	/**Controls**/
	/**
	 * Button which allows a new tab to be added. 
	 */
	private PamButton addTabButton;

	/**
	 * How many pixels the tab button 'floats' in the header area. 
	 */
	public double addButtonInsets=3; 


	public PamTabPaneSkin(PamTabPane tabPane) {
		super(tabPane);
		this.pamTabPane=tabPane; 

		//get areas which hold tabs 
		headerArea = (StackPane) tabPane.lookup(".tab-header-area");
		//contentArea = (StackPane) tabPane.lookup(".tab-container");
		tabContentArea = (StackPane) tabPane.lookup(".headers-region");
		//this is a bit hacky. Because tabs are animated there are issues with the layout function calling
		//after the animation has finished. Therefore the add tab button tends to not layout properly- similar 
		//to not calling validate in swing- this solves issue. 
		tabContentArea.widthProperty().addListener( (changed) -> {
			layoutAddButton();
		});

		//create the add tab button
		createAddButton();

		//add extra regions to header area
		headerArea.getChildren().add(addTabButton); 

	}

	/*
	 * Remove the tab start region. This sits in the tab header area, either at the left if the tab pane is horizontal or top if the tab pane is vertical. 
	 */
	public void removeTabStartRegion(Region tabRegion){
		headerArea.getChildren().remove(tabRegion);
	}

	/*
	 * Remove the tab end region. This sits in the tab header area, either at the right if the tab pane is horizontal or bottom if the tab pane is vertical. 
	 */
	public void removeTabEndRegion(Region tabRegion){
		headerArea.getChildren().remove(tabRegion);
	}

	/*
	 * Add the tab start region. This sits in the tab header area, either at the left if the tab pane is horizontal or top if the tab pane is vertical. 
	 */
	public void addTabStartRegion(Region tabRegion){
		headerArea.getChildren().add(tabRegion);
	}

	/*
	 * Add the tab end region. This sits in the tab header area, either at the right if the tab pane is horizontal or bottom if the tab pane is vertical. 
	 */
	public void addTabEndRegion(Region tabRegion){
		headerArea.getChildren().add(tabRegion);
	}

	/**
	 * Create a button which sits in tab pane and allows user to add a new tab. 
	 * @return button which creates new tab. 
	 */
	private void createAddButton(){
		addTabButton=new PamButton();
		addTabButton.setOnAction(e -> { 
			pamTabPane.getTabs().add(new PamTabFX("New Tab")); 
			headerArea.layout();
		});
		addTabButton.getStyleClass().add("tab-button");
	}

	/**
	 * Set the add button in the correct position
	 */
	private void layoutAddButton(){

		//need to careful here. The width and height of the tab pane don'tr swap on rotation i.e. width of pane remains the axis on which tabs are added. 
		//Weird but guess the java people decided that would make programming easier. 
		double insetx=0.; 
		if (pamTabPane.getSide()==Side.TOP || pamTabPane.getSide()==Side.BOTTOM){
			addTabButton.layoutXProperty().setValue(tabContentArea.getWidth()+insetx+headerArea.getPadding().getLeft()+addButtonInsets);
			addTabButton.layoutYProperty().setValue(headerArea.getHeight()-tabContentArea.getHeight()+addButtonInsets);
			addTabButton.resize(tabContentArea.getHeight()-4, tabContentArea.getHeight()-addButtonInsets*2);	
		}
		else{
			//yep, this makes no sense but works when tabs are vertical. 
			addTabButton.layoutXProperty().setValue(headerArea.getWidth()-tabContentArea.getWidth()-tabContentArea.getHeight()-headerArea.getPadding().getTop());
			addTabButton.layoutYProperty().setValue(headerArea.getHeight()-tabContentArea.getHeight());
			addTabButton.resize(tabContentArea.getHeight(),tabContentArea.getHeight());	
		}

		//			System.out.println("tab Content area width: " +tabContentArea.getWidth()+" tab Content area height: "+tabContentArea.getHeight()+ " addTabButton.layoutYProperty()" +addTabButton.layoutYProperty().getValue());

	}

	@Override 
	protected void layoutChildren(final double x, final double y,
			final double w, final double h) {
		super.layoutChildren(x, y, w, h);

		//check to make sure region have been added to stack pane. 
		if (pamTabPane.getTabStartRegion()!=null && !headerArea.getChildren().contains(pamTabPane.getTabStartRegion())){
			addTabStartRegion(pamTabPane.getTabStartRegion()); 
		}
		if (pamTabPane.getTabEndRegion()!=null && !headerArea.getChildren().contains(pamTabPane.getTabEndRegion())){
			addTabEndRegion(pamTabPane.getTabEndRegion()); 
		}

		//layout out tab start and end regions
		//move tabs to left and right
		Insets in = headerArea.getPadding();
		double startHeaderSize;
		double endHeadersize;
		if (pamTabPane.getSide()==Side.TOP || pamTabPane.getSide()==Side.BOTTOM){
			startHeaderSize  = (pamTabPane.getTabStartRegion()==null)  ? in.getLeft() : getRegionWidth(pamTabPane.getTabStartRegion()); 
			endHeadersize = (pamTabPane.getTabEndRegion()==null) ? in.getRight():  getRegionWidth(pamTabPane.getTabEndRegion()); 
			//set padding of tab header to make space for any added buttons
			headerArea.setPadding(new Insets(
					in.getTop(),
					endHeadersize,
					in.getBottom(),
					startHeaderSize));
		}
		else {
			startHeaderSize  = (pamTabPane.getTabStartRegion()==null)  ? in.getTop() :  pamTabPane.getTabStartRegion().getHeight(); 
			endHeadersize = (pamTabPane.getTabEndRegion()==null) ? in.getBottom():  pamTabPane.getTabEndRegion().getHeight(); 
			//set padding of tab header to make space for any added buttons
			headerArea.setPadding(new Insets(
					startHeaderSize,
					in.getRight(),
					endHeadersize,
					in.getLeft()));
		}

		//layout additional header regions for start
		if (pamTabPane.getTabStartRegion()!=null){
			pamTabPane.getTabStartRegion().layoutYProperty().setValue(0);
			if (pamTabPane.getSide()==Side.TOP || pamTabPane.getSide()==Side.BOTTOM){
				double regionWidth=getRegionWidth(pamTabPane.getTabStartRegion());
				pamTabPane.getTabStartRegion().resize(regionWidth, headerArea.getHeight());
			}
			else {
				//again, realise this is all a bit messed up but width/height are mixed upo weirdly whenever tab pane is vertically orientated. 
				double regionHeight=getRegionHeight(pamTabPane.getTabStartRegion());
				pamTabPane.getTabStartRegion().layoutYProperty().setValue(0);
				pamTabPane.getTabStartRegion().layoutXProperty().setValue(headerArea.getWidth()-regionHeight);
				pamTabPane.getTabStartRegion().resize(headerArea.getHeight(),regionHeight);
			}
		}

		//layout additional header regions for end
		if (pamTabPane.getTabEndRegion()!=null){
			if (pamTabPane.getSide()==Side.TOP || pamTabPane.getSide()==Side.BOTTOM){
				double regionWidth=getRegionWidth(pamTabPane.getTabEndRegion());
				pamTabPane.getTabEndRegion().layoutXProperty().setValue(headerArea.getWidth()-regionWidth);
				pamTabPane.getTabEndRegion().resize(regionWidth, headerArea.getHeight());
			}
			else{
				double regionHeight=getRegionHeight(pamTabPane.getTabStartRegion());
				pamTabPane.getTabEndRegion().layoutYProperty().setValue(0);
				pamTabPane.getTabEndRegion().layoutXProperty().setValue(0);
				pamTabPane.getTabEndRegion().resize(headerArea.getHeight(),regionHeight);
			}
		}

		//layout button which adds a new tab			
		if (pamTabPane.isAddTabButton()) layoutAddButton();

		//System.out.println("Hello "+hello.getWidth()+" "+headerArea.getWidth()+ "  "+tabContentArea.getWidth());	 

	}

	private double getRegionWidth(Region region){
		double regionWidth=50; 
		if (region.getMinWidth()>0) regionWidth=region.getMinWidth();
		if (region.getPrefWidth()>0) regionWidth=region.getPrefWidth();
		return regionWidth; 
	}

	private double getRegionHeight(Region region){
		double regionHeight=50; 
		if (region.getMinHeight()>0) regionHeight=region.getMinHeight();
		if (region.getPrefHeight()>0) regionHeight=region.getPrefHeight();
		return regionHeight; 
	}

	/**
	 * Get the button which allows users to add tab to tab pane
	 * @return the button which sits in tab pane and allows users to add a new tab. 
	 */
	public PamButton getAddTabButton() {
		return addTabButton;
	}

	/**
	 * Get the height of the header.
	 * @return the header height 
	 */
	public double getHeaderHeight() {
		return headerArea.getHeight();
	}

	/**
	 * Get the height property of the header. 
	 * @return the height property of the header. 
	 */
	public ReadOnlyDoubleProperty getHeaderHeightProperty(){
		return headerArea.heightProperty();
	}

	public void setAddTabButton(boolean addTabButton2) {
		if (addTabButton2){
			if (!headerArea.getChildren().contains(addTabButton)) {
				headerArea.getChildren().remove(addTabButton); 
				layoutAddButton();
			}
		}
		else {
			headerArea.getChildren().remove(addTabButton); 
		}


	}


}
