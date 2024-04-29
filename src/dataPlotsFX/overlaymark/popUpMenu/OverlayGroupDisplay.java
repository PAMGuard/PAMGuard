package dataPlotsFX.overlaymark.popUpMenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.docx4j.org.apache.poi.poifs.storage.RawDataBlock;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetection;
import clickDetector.ClickDetection;
import detectionPlotFX.DetectionGroupDisplay;
import detectionPlotFX.GroupDisplayListener;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.data.DDPlotRegister;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.projector.DetectionPlotProjector;
import detectionPlotFX.whistleDDPlot.WhistleDDInfo;
import detectionPlotFX.whistleDDPlot.WhistleDDPlotProvider;
import detectionPlotFX.whistleDDPlot.WhistleFFTPlot;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamBorderPane;
import whistlesAndMoans.WhistleMoanControl;

/**
 * 
 * A detection display which can display groups data units and 
 * their super detections in a tab pane if applicable. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class OverlayGroupDisplay extends PamBorderPane {

	/**
	 * Display which shows super detections. 
	 */
	private DetectionGroupDisplay superDetectionsDisplay; 

	/**
	 * The main detection pane.
	 */
	private DetectionGroupDisplay detectionsPane;

	/**
	 * The current detection group. 
	 */
	private List<PamDataUnit> detectionGroup;

	/**
	 * Indicates whether the super detection tab is present. 
	 */
	private boolean hasSuper = false;

	private DisplayTab detectionsTab;

	private DisplayTab suprDetTab;

	/**
	 * Constructor for the group detection display.
	 */
	public OverlayGroupDisplay() {
		//create two displays, one for super detections and the other for the
		//sub detections. 
		detectionsPane= new DetectionGroupDisplay(); 
		//detectionsPane.setPadding(new Insets(5,5,5,5));
		superDetectionsDisplay = new DetectionGroupDisplay();
	}

	/**
	 * Get the current number of detection plots that are available to dsiplay the currently set 
	 * data units. 
	 * @return the number of deteciton plots. Will be zero if the data cannot be displayed. 
	 */
	public int getDetectionPlotCount() {
		if (detectionsPane.getDetectionDisplay().getCurrentDataInfo()==null) return 0; 
		return detectionsPane.getDetectionDisplay().getCurrentDataInfo().getDetectionPlotCount(); 
	}

	/**
	 * Layout the pane. 
	 */
	public void layoutPane() {
		if (this.detectionGroup==null) {
			this.setCenter(null); 
			return; 
		}

		
		if (hasSuperDetectionDisplay()) {
			hasSuper=true;
			//need to redo this to prevent duplicate children issues...
			TabPane tabPane = new TabPane(); 
			//tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);

			/**
			 * A tab pane in JavaFX is a control. Therefore, by default, it consumes any
			 * event. Usually this is not a problem however the OverlayGroupDisplay is used
			 * in a pop up menu that can be dragged. The tab pane prevents the dragging by
			 * consuming the mouse event. The solution is a bit of a HACK but effectively
			 * grabs the mouse event in the tab pane and manually fires it to underlying
			 * node so it is effectively no longer consumed.
			 */
			tabPane.addEventHandler(EventType.ROOT, event -> this.fireEvent(event));
			//		    tabPane.setPickOnBounds(false);

			tabPane.getTabs().add(detectionsTab = new DisplayTab("Data Units", detectionsPane));
			detectionsTab.setClosable(false);
			tabPane.getTabs().add(suprDetTab = new DisplayTab("Super Detection", superDetectionsDisplay));
			suprDetTab.setClosable(false);

			tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab)->{
				DetectionGroupDisplay display = ((DisplayTab) newTab).getDetGroupDisplay(); 
				display.triggerListeners(null, display.getCurrentUnit()); 
			}); 

			this.setCenter(tabPane);
			
			//might have detections which cannot be individually shown but summaries are shown in super display. 
			if (getDetectionPlotCount()<1) {
				detectionsTab.setDisable(true);
				tabPane.getSelectionModel().select(1);
			} 
			else {
				detectionsTab.setDisable(false);
			}
		}
		else  {
			hasSuper=false; 
			this.setCenter(detectionsPane);
		}
		

	}

	/**
	 * Tab which holds a reference to a det group display
	 * @author Jamie Macaulay
	 *
	 */
	class DisplayTab extends Tab {

		private DetectionGroupDisplay detGroupDisplay; 

		DisplayTab(String tabName, DetectionGroupDisplay detGroupDisplay){
			super(tabName, detGroupDisplay);
			this.detGroupDisplay=detGroupDisplay; 
		}

		public DetectionGroupDisplay getDetGroupDisplay() {
			return detGroupDisplay;
		}

		public void setDetGroupDisplay(DetectionGroupDisplay detGroupDisplay) {
			this.detGroupDisplay = detGroupDisplay;
		}
	}


	/**
	 * Check whether the list of data units has super detections, and, if so, whether those super detections
	 * can be displayed on a graph. 
	 * @return true if super detection graph exists. 
	 */
	public boolean hasSuperDetectionDisplay() {

		@SuppressWarnings("rawtypes")
		HashSet<SuperDetection> dataUnitSet = getUniqueSuperDetections(); 

		if (dataUnitSet==null) return false; 

		//check to see if any of the super detections can be plotted
		for (PamDataUnit aDataUnit : dataUnitSet) {
			//			Debug.out.println("Number of super detections: " + dataUnitSet +  "  " +DDPlotRegister.getInstance().findDataProvider(aDataUnit.getParentDataBlock()));
			if (DDPlotRegister.getInstance().findDataProvider(aDataUnit.getParentDataBlock())!=null) {
				return true; //there is defo some super detection stuff to plot. 
			}
		}

		return false;
	}


	/**
	 * Get the unique super detections for the data units in detecitonSummary. 
	 * @return the unique super detections for all data units.
	 */
	private HashSet<SuperDetection> getUniqueSuperDetections(){
		@SuppressWarnings("rawtypes")
		HashSet<SuperDetection> dataUnitSet = new HashSet<SuperDetection>(); 

		if (detectionGroup==null) return null; 
		//are there any super detections. 
		for (int i=0; i<detectionGroup.size(); i++) {
			for (int j=0; j<detectionGroup.get(i).getSuperDetectionsCount(); j++) {
				dataUnitSet.add(detectionGroup.get(i).getSuperDetection(j)); 
			} 
		}

		return dataUnitSet; 
	}

	/**
	 * Clear are units and painting from the display. 
	 */
	public void clearDisplay() {
		this.detectionsPane.clearDisplay(); 
		this.superDetectionsDisplay.clearDisplay();
	}


	/**
	 * Force a draw of the currently selected data unit. 
	 */
	public void drawDataUnit() {
		//		if (hasSuper) {
		//			((DisplayTab) tabPane.getSelectionModel().getSelectedItem()).getDetGroupDisplay().drawCurrentUnit();
		//		}
		//		else {
		//			this.detectionsPane.drawCurrentUnit();
		//		}

		this.superDetectionsDisplay.drawCurrentUnit();
		this.detectionsPane.drawCurrentUnit();
	}



	/**
	 * Set a list of detections. These will automatically be checked for super detections. 
	 * @param dataList - a list of detections. 
	 */
	public void setDetectionGroup(List<PamDataUnit> dataList) {
		detectionsPane.getDetectionDisplay().getDataTypePane().setVisible(true);
		this.detectionGroup = dataList; 

		HashSet<SuperDetection> superDets = getUniqueSuperDetections(); 

		//		Debug.out.println("Data list size: " + dataList);		
		//		Debug.out.println("Super list size: " + superDets);

		this.detectionsPane.setDetectionGroup(dataList);
	
		
		if (superDets!=null) {
			this.superDetectionsDisplay.setDetectionGroup(new ArrayList<PamDataUnit>(superDets));
		}
		this.prepareDisplay(); 
	}

	/**
	 * Prepare the display
	 */
	private void prepareDisplay() {
		this.detectionsPane.prepareDisplay();
		this.superDetectionsDisplay.prepareDisplay();
		layoutPane();
	}

	/**
	 * Add listeners to both the detections and super detections display, 
	 * @param groupDisplayListener - the listener to add 
	 */
	public void addDisplayListener(GroupDisplayListener groupDisplayListener) {
		detectionsPane.addDisplayListener(groupDisplayListener);
		superDetectionsDisplay.addDisplayListener(groupDisplayListener);
	}

	/**
	 * Set the detection display to show a spectrogram of raw data. Not a good idea to try a very large chunk of data. 
	 * @param minX - the start time to show.
	 * @param maxX - the end time to show. 
	 */
	public void showRawData(PamRawDataBlock pamRawBlock, double[] limits, int channelMap) {
		TempRawDataUnit rawDataUnit = new TempRawDataUnit(limits[0], limits[1], PamUtils.PamUtils.getLowestChannel(channelMap), pamRawBlock); 

//		if () {
			//if the  graph is frequency axis. 
			rawDataUnit.setFrequency(new double[] {limits[2], limits[3]});
//		}

		//currently only show a spectrogram so just disable the combob box thing. 
		detectionsPane.getDetectionDisplay().getDataTypePane().setVisible(false);
			
			
		//set the data info for the pane
		detectionsPane.getDetectionDisplay().setDataInfo(new RawSpecDDInfo(detectionsPane.getDetectionDisplay(), pamRawBlock.getSampleRate()));

		//show the raw data unit on the display
		detectionsPane.getDetectionDisplay().setDataUnit(rawDataUnit);
	}


	/**
	 * How to plot the dummy data and what graphs are available. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class RawSpecDDInfo extends DDDataInfo<ClickDetection>  {

		public RawSpecDDInfo(DetectionPlotDisplay dDPlot, float sR) {
			super(dDPlot, sR);

			//create an FFT data block
			this.addDetectionPlot(new RawWaveFFTPlot(dDPlot, dDPlot.getDetectionPlotProjector()));
			//TODO could add waveform here.

			super.setCurrentDetectionPlot(0);
		}
	}

	/**
	 * A raw data plot that shows only raw data and no overlaid detections.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class RawWaveFFTPlot extends RawFFTPlot<TempRawDataUnit> {

		public RawWaveFFTPlot(DetectionPlotDisplay displayPlot, DetectionPlotProjector projector) {
			super(displayPlot, projector);
			//set padding to default of zero for this plot. 
			this.getFFTParams().detPadding=0; 
		}

		@Override
		public void paintDetections(TempRawDataUnit detection, GraphicsContext graphicsContext, Rectangle windowRect,
				DetectionPlotProjector projector) {
			// TODO Auto-generated method stub	
		}

	}

	/**
	 * A temporary data unit which holds the overlay parameters. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class TempRawDataUnit extends PamDataUnit {

		private PamRawDataBlock pamRawBlock;

		public TempRawDataUnit(double minMillis, double maxMillis, int channelMap, PamRawDataBlock rawDataBlock) {
			super((long) minMillis);
			this.getBasicData().setMillisecondDuration(maxMillis-minMillis);
			this.pamRawBlock=rawDataBlock; 
		}

		@Override
		public PamDataBlock getParentDataBlock() {
			//ovwerride and return the raw datablock. This 
			return pamRawBlock;
		}

	}




}
