package dataPlotsFX.scroller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.flipPane.FlipPane;
import pamViewFX.fxNodes.pamScrollers.TimeRangeScrollSpinner;
import pamViewFX.fxNodes.pamScrollers.VisibleRangeObserver;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.AcousticDataGramGraphics;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.AcousticScrollerFX;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.AcousticScrollerGraphics;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.RawScrollBarGraphics;

/**
 * Subclass for the td display. Provides a specific type of navigation dialog for this scroller. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TDAcousticScroller extends AcousticScrollerFX implements PamSettings  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The tdDisplay pane which holds this time scroller.
	 */
	private TDDisplayFX tdDisplay;

	/**
	 * Graphics for acoustic data. 
	 */
	private RawScrollBarGraphics rawScrollBarGraphics;


	/**
	 * The current raw data block for the acoustic scroller. 
	 */
	private PamDataBlock<RawDataUnit>  rawDataBlock;

	/**
	 * The data selector button. 
	 */
	private MenuButton dataSelectButton;

	/**
	 * Need a list of all observers since every time a new data block is added, there
	 * is a call to checkDataBlockGraphics which subscribes AcousticObservers to 
	 * all the source data. This results in many blocks being added many times. 
	 * This list is used to delete all these observers just before they all 
	 * get added again. Very inefficient, but will work well enough. 
	 */
	private ArrayList<AcousticObserver> allObservers = new ArrayList<>();

	/**
	 * Pane ot hold the scroll pane
	 */
	private PamBorderPane mainTDScrollPane;

	/**
	 * The flip pane. 
	 */
	private FlipPane flipPane;

	/**
	 * Settings pane for the scroller. 
	 */
	private TDScrollerSettingsPane settingsPane;
	
	/**
	 * Spinner for changing the window size. i.e. how many seconds the windows shows. 
	 */
	private TimeRangeScrollSpinner spinner;

	/**
	 * 
	 */
	private TDAcousticScrollerParams scrollerColourParams = new TDAcousticScrollerParams();

	/**
	 * Stack pane. 
	 */
	private StackPane settingsHolder; 


	/**
	 * Construct a PAMGUARD scroll bar which contains 
	 * a main scroll bar bit and buttons for moving forward
	 * in large secScollbar name (used in scroll bar management)
	 * @param orientation AbstractPamScroller.VERTICAL or AbstractPamScroller.HORIZONTAL
	 * @param stepSizeMillis step size in milliseconds for scroller. 
	 * @param defaultLoadTime default amount of data to load.
	 * @param hasMenu true if menu options should be shown in navigation area. 
	 * @param owner The pamscroller window owner. Used to keep dialogs inside the TDDisplay window. 
	 */
	public TDAcousticScroller(String name, Orientation orientation,
			int stepSizeMillis, long defaultLoadTime, boolean hasMenu, TDDisplayFX tdDisplay) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);
		this.tdDisplay=tdDisplay;

		//hard wire adding the raw data block. 
		//Find the raw data block. 
		if (!isViewer){
			rawDataBlock= PamController.getInstance().getRawDataBlock(0);
			this.rawScrollBarGraphics=new RawScrollBarGraphics(this, rawDataBlock); 
			this.addAcousticScrollGraphics(rawScrollBarGraphics); 
			addRawAcousticObserver(); //only add this observer if in real time mode! Otherwise handles in data loading thread
		}

		createHolderPane(); 
		
		//button for datagram observers
		createSelectorButton(); 
		
		//add a time spinner. 
		createTimeSpinner(); 

		this.settingsPane.setParams(scrollerColourParams);

		updateDataGramColours(scrollerColourParams);

	}

	boolean spinnerCall = false; 
	
	/**
	 * Create a time spinner. 
	 */
	private void createTimeSpinner() {
		
		if (isViewer) {
			//add to navigation
			super.getNavigationPane().getChildren().add(0, spinner = new TimeRangeScrollSpinner());
		}
		else {
			//set at right of pane
			super.getMainPane().setRight(spinner = new TimeRangeScrollSpinner());
		}
			
		spinner.setEditable(false); //list of times not implemented for editing yet..//TODO
		
		spinner.prefHeightProperty().bind(this.getMainPane().heightProperty());
		setSpinnerOrientation(super.getOrientation());
		
		//set the spinner initially
		spinner.getValueFactory().setValue(this.getVisibleAmount());
		
		//now add listeners - do not use binding as this will be changed and will change values
		
		//add a listener so that the spinner changes of the visible amount chnages
		super.getScrollBarPane().visibleAmountProperty().addListener((obsVal, oldVal, newVal)->{
			spinnerCall = true;
			spinner.getValueFactory().setValue(newVal.longValue());
			spinnerCall = false; 
		});
		
		//add a listener so the visible amount changes of the spinner changes value. 
		spinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
			if (spinnerCall) return ; //prevent overflow. 
			if (newVal<=this.getRangeMillis()) {
//					Debug.out.println("TDAcousticScroller: TimeRangeSpinner: " + newVal);
				Platform.runLater(()->{ //why? But seems necessary
					super.setVisibleMillis(newVal);
				}); 
			}
			else spinner.getValueFactory().decrement(1); //need to use decrement here instead of set time because otherwise arrow buttons
			//don't work. 
		});
	}


	
	private void createHolderPane() {

		//create the flip pane. 
		flipPane=new FlipPane(); 
		flipPane.setFlipDirection(Orientation.VERTICAL);
		flipPane.setFlipTime(250); //default is 700ms- way too high


		flipPane.getFront().getChildren().add(super.getNode()); 

		//create the settings pane
		settingsPane=new TDScrollerSettingsPane(null, this);

		settingsPane.addSettingsListener(()->{
			scrollerColourParams=settingsPane.getParams(scrollerColourParams); 
			this.updateDataGramColours(scrollerColourParams);
			repaint(100);
		});

		//add listener so mouse wheel can change colours. 
		super.getScrollBarPane().setOnScroll((zoom)->{
			//set the new amplitude limits. 
			scrollerColourParams.amplitudeLimits[0]=scrollerColourParams.amplitudeLimits[0]+zoom.getDeltaY()/100.; 
			scrollerColourParams.amplitudeLimits[1]=scrollerColourParams.amplitudeLimits[1]+zoom.getDeltaY()/100.; 
			//			System.out.println("Amplitude Limits: " + scrollerColourParams.amplitudeLimits[0] 
			//					+ "  " +  scrollerColourParams.amplitudeLimits[1] + " zoom: " +zoom.getTotalDeltaX());
			this.updateDataGramColours(scrollerColourParams);
			//no repaint the scroller. 
			repaint(100);
		});


		//do not set mouse transparent - nothing works
		//		flipPane.getFront().setMouseTransparent(true); //must access scroll bar pane underneath
		//		flipPane.getBack().setMouseTransparent(true);
		//		flipPane.setMouseTransparent(true);

		//have the scroller behind the flip pane so colour changes can actually be seen. 
		//Might be a nicer way to do this by having scroller behind flip pane but this is 
		//slighty more flexible for now. 
		flipPane.flipFrontProperty().addListener((obsval, oldval, newval)->{

			flipPane.getFront().getChildren().clear();
			flipPane.getBack().getChildren().clear();

			if (newval){
				super.setScrollRectVisible(true);
				flipPane.getFront().getChildren().add(super.getNode()); 
			}
			else {
				//clear child from front tpo prevent multiple parent exception
				//now add the front and then the settings pane. The settings pane sits on top of the front
				super.setScrollRectVisible(false);
				flipPane.getBack().getChildren().add(super.getNode()); 
				flipPane.getBack().getChildren().add(settingsPane.getContentNode()); 
			}
		});

		mainTDScrollPane=new PamBorderPane(); 
		mainTDScrollPane.setCenter(flipPane);

	}

	/**
	 * Set colours on one datagram. 
	 * @param scrollerColourParams
	 */
	private void updateDataGramColours(TDAcousticScrollerParams scrollerColourParams) {
		//only set the colours for this scroller. 
		if (this.getAcousticScrollGraphics().size()>this.getCurrentGraphicsIndex() && this.getAcousticScrollGraphics().size()!=0) {
			//System.out.println("AMPLITUDELIMITS: " + scrollerColourParams.amplitudeLimits[0] + "   " + scrollerColourParams.amplitudeLimits[1]);
			this.getAcousticScrollGraphics().get(this.getCurrentGraphicsIndex()).setColors(new StandardPlot2DColours(scrollerColourParams.colourMap, 
					scrollerColourParams.amplitudeLimits[0], scrollerColourParams.amplitudeLimits[1]));
		}
		else {
//			System.err.println("TDAcousticScroller: the graphics and index are the same size: " + this.getCurrentGraphicsIndex()); 
		}
	}

	/**
	 * Create select button. 
	 */
	public void createSelectorButton(){

		dataSelectButton = new MenuButton(); 
//		dataSelectButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.MORE_VERT, PamGuiManagerFX.iconSize));
		dataSelectButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-dots-vertical", PamGuiManagerFX.iconSize));
		dataSelectButton.getStyleClass().add("opaque-button-square");		
		dataSelectButton.prefHeightProperty().bind(this.getMainPane().heightProperty());
		dataSelectButton.setPrefWidth(TDDisplayFX.dataAxisSize);


		dataSelectButton.showingProperty().addListener((oldVal, newVal, propVal)->{

			dataSelectButton.getItems().clear();
			CheckMenuItem menutItem;
			for (int i=0; i<this.getAcousticScrollGraphics().size(); i++){
				menutItem=new CheckMenuItem(getAcousticScrollGraphics().get(i).getName());

				if (super.getCurrentGraphicsIndex()==i) menutItem.setSelected(true);

				final int index=i; 
				menutItem.setOnAction((action1)->{
					//					System.out.println(getAcousticScrollGraphics().get(index).getName() + " has been selected");
					tdDisplay.getTDParams().scrollerDataGramIndex=index;

					setScrollerGraphics(index); 

					//now update the params from the datagram! This means different datagrams can have different colours. 
					if (this.getAcousticScrollGraphics().get(index).getColors()!=null) {
						StandardPlot2DColours colors = (StandardPlot2DColours) this.getAcousticScrollGraphics().get(index).getColors(); 
						scrollerColourParams.setParams(colors); 
						settingsPane.setParams(scrollerColourParams);
						repaint(0); 
					}
				});
				dataSelectButton.getItems().add(menutItem); 
			}

			//create a settings menu item
//			MenuItem  settings = new MenuItem ("Settings",PamGlyphDude.createPamGlyph(MaterialIcon.SETTINGS, PamGuiManagerFX.iconSize));
			MenuItem  settings = new MenuItem ("Settings",PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
			settings.setOnAction((action1)->{
				//					System.out.println(getAcousticScrollGraphics().get(index).getName() + " has been selected");
				showSettingPane(); 
			});
			dataSelectButton.getItems().add(settings); 

		});

		//add button to the fliping part- otherwise it gets a bit confusing for the user when in settings as
		//the arrow increment button appears in the exact same place as the flip back button after flipping from settings
		//if (JamieDev.isEnabled() || !super.isViewer){
			super.getMainPane().setLeft(dataSelectButton);
		//}

	}


	void showSettingPane() {
		if (flipPane.isFrontVisible()) {
			settingsPane.setParams(scrollerColourParams); 
			flipPane.flipToBack();
		}
		else {
			//back
			scrollerColourParams=settingsPane.getParams(scrollerColourParams); 

			this.updateDataGramColours(scrollerColourParams);

			flipPane.flipToFront(); 
			super.repaint(0);
		}
	}

	//	/**
	//	 * Set the colours in the datagram
	//	 */
	//	void setScrollerColours(){
	////		System.out.println("Set scroller colours: " + scrollerColourParams.colourMap + 
	////				" scrollerColourParams.amplitudeLimits[0] " + scrollerColourParams.amplitudeLimits[0]
	////						+ " scrollerColourParams.amplitudeLimits[1] " +scrollerColourParams.amplitudeLimits[1]); 
	//		
	//		super.setDataGramColors(new StandardPlot2DColours(scrollerColourParams.colourMap, 
	//				scrollerColourParams.amplitudeLimits[0], scrollerColourParams.amplitudeLimits[1]));
	//	}

	@Override
	public void addDataBlock(PamDataBlock dataBlock) {
		super.addDataBlock(dataBlock);
		//now need to check what datablocks are subscribed
		cancelDataLoadTasks(); 
		//		System.out.println("Check datablock graphics after adding " + dataBlock.getDataName());
		checkDataBlockGraphics();
		//load scroller data
		if (isViewer) loadScrollerData();
	}

	/**
	 * Iterate through all datablocks in the display and check that the graphics list is correct. 
	 */
	private void checkDataBlockGraphics() {
		//easiest would be to clear and add but don't want to have to recalculate graphics data.

		removeAllObservers();

		@SuppressWarnings("rawtypes")
		List<PamDataBlock> list=new ArrayList<PamDataBlock>(); 

		//add graphics which should be there. 
		for (TDGraphFX tdGraph: tdDisplay.getTDGraphs()){
			for (TDDataInfoFX dataInfo : tdGraph.getDataList()){
				//is this datablock included in the list of graphics?
				if (dataInfo != null) {
					list.add(dataInfo.getDataBlock());
				}
			}
		}

		//remove duplicates 
		list = list.parallelStream().distinct().collect(Collectors.toList());

		//now sort out graphics 

		//remove an y graphics whihc should not be there.
		boolean remove;
		ArrayList<AcousticScrollerGraphics> removeGraphics= new ArrayList<AcousticScrollerGraphics>(); 
		for (AcousticScrollerGraphics scrollerGraphics: this.getAcousticScrollGraphics()){
			remove=true; 
			for (PamDataBlock dataBlock: list){
				if (scrollerGraphics==dataBlock){
					remove=false;
					break;
				}
			}
			if (remove && scrollerGraphics!=rawScrollBarGraphics){
				removeGraphics.add(scrollerGraphics);
			}
		}
		this.getAcousticScrollGraphics().removeAll(removeGraphics);

		//add datablocks which should be there
		boolean add;
		for (PamDataBlock dataBlock: list){
			add=true;
			for (AcousticScrollerGraphics scrollerGraphics: this.getAcousticScrollGraphics()){
				if (scrollerGraphics.getDataBlock()==dataBlock){
					add=false;
				}
			}
			if (add){
				//now need to add these graphics. 
				addDataBlockGraphics(dataBlock);
			}
		}
		this.updateDataGramColours(scrollerColourParams);
	}

	/*
	 * Add data block graphics to the scroll bar. 
	 */
	public void addDataBlockGraphics(PamDataBlock dataBlock){
		//		//if FFT data need to add FFT datablock 
//		if (dataBlock instanceof FFTDataBlock){
//			this.addAcousticScrollGraphics(new FFTScrollBarGraphics(this , (FFTDataBlock) dataBlock));
//			/**
//			 * Only need to add an observer in  real time mode. Otherwise observers are handled in AcousticScrollerFX
//			 */
////			if (!isViewer) addAcousticObserver(dataBlock, PamUtils.getLowestChannel(dataBlock.getChannelMap()));
//			if (!isViewer) addAcousticObserver(dataBlock, PamUtils.getLowestChannel(dataBlock.getSequenceMap()));
//		}
		//		if (dataBlock instanceof ClickDataBlock) {
		//			System.out.println("Adding to another click datablock " + dataBlock.hashCode());
		//		}
		//else check for datagram graphics. 
		if (dataBlock.getDatagramProvider()!=null){
			//check that the datagram 
			this.addAcousticScrollGraphics(new AcousticDataGramGraphics(this , dataBlock));
			if (!isViewer) addAcousticObserver(dataBlock, -1);
		}
	}


	@Override
	public void layoutScrollBarPane(Orientation orientation){
		//need to create spinner as CSS has some layout issues
		super.layoutScrollBarPane(orientation);
	}


	/**
	 * Set orientation for the scroller. 
	 * @param orientation- orientation of the time scroller. 
	 */
	public void setOrientation(Orientation orientation){
		super.setOrientation(orientation);
	}

	/**
	 * The range spinner. 
	 * @param timeRangeListener
	 */
	public void addRangeObserver(VisibleRangeObserver timeRangeListener) {
		super.addRangeObserver(timeRangeListener); 
	}

	private void addAcousticObserver(PamDataBlock pamDataBlock, int chan){
		/**
		 * Need to stop multiply subsribing observers !
		 */
		//		System.out.printf("Adding acoustic observer to scroller %d for data %s\n", this.hashCode(), pamDataBlock.getDataName());
		AcousticObserver newObs = new AcousticObserver(this, pamDataBlock, chan);
		pamDataBlock.addObserver(newObs);
		allObservers.add(newObs);
	}

	private AcousticObserver currentAcousticObserver; 
	private void addRawAcousticObserver(){
		/*
		 * This one doesn't get added over and over since it's called from the 
		 * constructor, so don't put the observer in the allObservers list to 
		 * avoid it getting removed. However, do still check against it
		 * getting added multiple times. 
		 */
		if (rawDataBlock!=null){
			if (currentAcousticObserver != null) {
				rawDataBlock.deleteObserver(currentAcousticObserver);
			}
			AcousticObserver newObserver = new AcousticObserver(this, rawDataBlock, 0);
			rawDataBlock.addObserver(newObserver);
		}
		else {
			System.err.println("Acoustic scroller could not find the raw data block");
		}

		//		//TEMP- for testing only. 
		//		if (chan==0) this.getTDGraph().getTDDisplay().getTimeScroller().addNewRawData(rawDataUnit);
		//		//TEMP
	}

	/**
	 * Observes data from the sound acquisition module. 
	 * @author Jamie Macaulay
	 *
	 */
	private class AcousticObserver extends PamObserverAdapter {

		/**
		 * Send only lowest channel data units. 
		 */
		private int chan;
		private PamDataBlock observedData;

		public AcousticObserver(TDAcousticScroller tdAcousticScroller, PamDataBlock observedData, int chan) {
			this.observedData = observedData;
			this.chan=chan; 
		}

		@Override
		public String getObserverName() {
			return "Acoustic Scroller Observer " + chan;
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			TDAcousticScroller.this.setSampleRate(sampleRate);
		}

		@Override
		public void addData(PamObservable o, PamDataUnit rawDataUnit) {
			//			System.out.println("TDAcousticScroller: New data: " + rawDataUnit.getParentDataBlock().getDataName()  + "  ch: "+rawDataUnit.getChannelBitmap()); 
//			if (chan==-1 || PamUtils.hasChannel(rawDataUnit.getChannelBitmap(), chan)){
			if (chan==-1 || PamUtils.hasChannel(rawDataUnit.getSequenceBitmap(), chan)){
				Platform.runLater(()->{
					addNewPamData(rawDataUnit);
				});
			}
		}
	}

	@Override 
	public Pane getNode(){
		//return a holder pane here. 
		return mainTDScrollPane;
	}

	@Override
	public synchronized void loadScrollerData(){
		//only load datagram if scroll data is available. 
		//		if (TDControl.isJamieDev && this.tdDisplay.initializationComplete()){
		//if (JamieDev.isEnabled()){
			super.loadScrollerData(); 
		//}
	}

	/**
	 * Remove all current observers from the observed datablocks, otherwise things
	 * end up observing many many times. 
	 */
	private void removeAllObservers() {
		for (AcousticObserver acousticObserver:allObservers) {
			PamDataBlock observed = acousticObserver.observedData;
			if (observed != null) {
				observed.deleteObserver(acousticObserver);
			}
		}
	}
	
	/**
	 * Set the orientation of the time range spinner 
	 * @param orientation - orientation to set the spinner to. 
	 */
	private void setSpinnerOrientation(Orientation orientation){
		if (orientation==Orientation.HORIZONTAL) {
			spinner.getStyleClass().remove(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			spinner.setPrefWidth(150);
		}
		else{
//			System.out.println("Vertical spinner"); 
			spinner.getStyleClass().remove(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL); 
			spinner.setPrefHeight(100);

		}
	}


	@Override
	public String getUnitName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getUnitType() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		// TODO Auto-generated method stub
		return false;
	}


}
