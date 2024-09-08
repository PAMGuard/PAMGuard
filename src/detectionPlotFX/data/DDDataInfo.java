package detectionPlotFX.data;

import java.util.ArrayList;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import detectionPlotFX.DDScaleInfo;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.pamAxis.PamAxisPane2;

/**
 * Information on the data a detection can display. This essentially is a wrapper for a data block which allows
 * the individual pamDataUnits of the data block to be displayed in the detection display. 
 * @param T the type of data unit
 * @author Jamie Macaulay
 *
 */
public abstract class DDDataInfo<T extends PamDataUnit> {


	/**
	 * Reference to the detection display plot
	 */
	private DetectionPlotDisplay dDPlot;

	/**
	 * Pane whihc holds plot specifc settings.
	 */
	private PamBorderPane settingsPane= new PamBorderPane();

	/**
	 * The data block which the DDDataInfo is associated with 
	 */
	private PamDataBlock pamDataBlock;

	/**
	 * The scale information. Holds the min and max values for the x and y axis. 
	 */
	private DDScaleInfo scaleInfo = new DDScaleInfo(0, 1, -1, 1);

	/**
	 * True if in viewer mode. 
	 */
	private boolean isViewer=false;

	/**
	 * List of detection plots which can display the data unit. 
	 */
	private ArrayList<DetectionPlot> detectionPlots; 

	/*
	 * The index of the current detection plot
	 */
	private int currentDetectionPlotIndex=-1;

	/**
	 * The sample rate 
	 */
	private float hardSampleRate=-1;


	//	/**
	//	 * Create the DDDataInfo
	//	 * @param dDDataProvider - the data provider 
	//	 * @param dDPlot - the detection plot
	//	 * @param pamDataBlock - the parent datablock
	//	 */
	//	public DDDataInfo(DetectionPlotDisplay dDPlot, PamDataBlock pamDataBlock) {
	//		super();
	//		this.dDPlot=dDPlot; 
	//		this.pamDataBlock = pamDataBlock;
	//		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	//		detectionPlots=new ArrayList<DetectionPlot>(); 
	//	}


	/**
	 * Create the DDDataInfo
	 * @param dDDataProvider - the dataplot provider
	 * @param dDPlot - the dBPLot
	 * @param sampleRate - the samplerate. 
	 */
	public DDDataInfo(DetectionPlotDisplay dDPlot, float sampleRate) {
		super();
		this.dDPlot=dDPlot; 
		this.hardSampleRate = sampleRate;
		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		detectionPlots=new ArrayList<DetectionPlot>(); 
	}

	/**
	 * Create the DDdata info wothout reference to a provider
	 * @param dDPlot - - the data plot
	 * @param pamDataBlock - the parent datablopck
	 */
	public DDDataInfo(DetectionPlotDisplay dDPlot, PamDataBlock pamDataBlock) {
		super();
		this.dDPlot=dDPlot; 
		this.pamDataBlock = pamDataBlock;
		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		detectionPlots=new ArrayList<DetectionPlot>(); 
	}

	//	/**
	//	 * Create the DDdata info wothout reference to a provider
	//	 * @param dDPlot - - the data plot
	//	 * @param pamDataBlock - the parent datablopck
	//	 */
	//	public DDDataInfo(DetectionPlotDisplay dDPlot, float sR) {
	//		super();
	//		this.dDPlot=dDPlot; 
	//		this.hardSampleRate=sR; 
	//		this.isViewer = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	//		detectionPlots=new ArrayList<DetectionPlot>(); 
	//	}

	/**
	 * Get the data block for this information
	 * @return the datablock. This can be null. 
	 */
	public PamDataBlock getDataBlock() {
		return pamDataBlock;
	}


	/**
	 * Get the sample rate. This is acquired form the datablock if one exists. Otherwise a hard 
	 * wired sample rate is used. 
	 * @return the sample rate. 
	 */
	public float getHardSampleRate() {
		if (pamDataBlock!=null) return pamDataBlock.getSampleRate();
		else return hardSampleRate; 
	}



	/**
	 * Set the sample rate. This is only used if the parent data block is null. 
	 * @return the sample rate. 
	 */
	public void setHardSampleRate(float hardSampleRate) {
		this.hardSampleRate = hardSampleRate; 
	}

	/**
	 * Add a type of data unit to the list. 
	 * @param unitType String name of the data unit. 
	 */
	public void addDetectionPlot(DetectionPlot<?> detectionPlot) {
		detectionPlots.add(detectionPlot);
	}

	/**
	 * Get a detection plot
	 * @param the index of the detection plot to get.
	 */
	public DetectionPlot<?> getDetectionPlot(int i) {
		return detectionPlots.get(i);
	}

	/**
	 * The number of possible detection plots for the data unit. 
	 */
	public int getDetectionPlotCount(){
		return detectionPlots.size(); 
	}

	/**
	 * Get the scale information. This holds the min and max values for the x and y axis along with other
	 * axis related parameters. 
	 */
	public DDScaleInfo getDDScaleInfo(){
		return this.scaleInfo;
	}

	/**
	 * Get the detection plot display
	 * @return the detection plot display. 
	 */
	public DetectionPlotDisplay getDetectionPlotDisplay() {
		return dDPlot;
	}

	/*
	 * Sets the current detection plot to be shown.
	 * @param index of the plot in the plot list.   
	 */
	public void setCurrentDetectionPlot(int index) {
		this.currentDetectionPlotIndex=index;
		if (index>=0 && index<detectionPlots.size()){
			detectionPlots.get(index).setupPlot();

			if (detectionPlots.get(index).getSettingsPane()!=null){
				settingsPane.setCenter(detectionPlots.get(index).getSettingsPane());
				settingsPane.setMinHeight(detectionPlots.get(index).getSettingsPane().getMinHeight());
			}
			else {
				settingsPane.setCenter(null); 
			}

			//need to force a layout 
			for (PamAxisPane2 axis : dDPlot.getAllAxisPanes()){
				axis.layout();
			}
			dDPlot.layout();
		}


		else settingsPane.setCenter(null);
	}

	/**
	 * Set the current plot to be shown.
	 * @param plot the plot to show. 
	 */
	public void setCurrentDetectionPlot(DetectionPlot plot) {
		int index=detectionPlots.indexOf(plot);
		setCurrentDetectionPlot(index); 
	}

	/**
	 * Set the current plot to be shown based on the detections plot name
	 * @param the name of the plot to show
	 * @param true if the plot was set - otherwise false (i.e. if the name was wrong)
	 */
	public boolean setCurrentDetectionPlot(String plotName) {
		for (int i=0; i<detectionPlots.size(); i++) {
			if (detectionPlots.get(i).getName().equals(plotName)) {
				setCurrentDetectionPlot(i); 
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the currently selected detection plot for the data unit
	 * @return the current detection plot or null if no detection plot is selected. 
	 */
	public DetectionPlot getCurrentDetectionPlot() {
		if (currentDetectionPlotIndex<0 || currentDetectionPlotIndex>=detectionPlots.size()) return null; 
		return detectionPlots.get(currentDetectionPlotIndex);
	}


	/**
	 * Get the settings pane for this DDDataInfo. This holds plot specific settings. 
	 * @return
	 */
	public PamBorderPane getSettingsPane(){
		return settingsPane;
	}


	/**
	 * Paint data into the graphics window. 
	 * @param g the graphics handle for drawing on. 
	 * @param windowRect Window rectangle to draw in
	 * @param pamAxis scroll start time in milliseconds
	 * @param graphAxis all the axis of the graph. Some may be invisible. Order is TOP, RIGHT, BOTTOM (x axis), LEFT (y axis)
	 * @param newDataUnit - the data unit to plot. 
	 * @param a flag with extra information - e.g. if this is a scroll bar paint or not. 
	 */
	public void drawData(GraphicsContext g, Rectangle windowRect, DetectionPlotProjector projector, T pamDataUnit, int flag){

		if (getCurrentDetectionPlot()==null) return; 

		g.clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());

		getCurrentDetectionPlot().setupAxis((PamDataUnit) pamDataUnit, this.getHardSampleRate(), projector);

		getCurrentDetectionPlot().paintPlot((PamDataUnit) pamDataUnit, g, windowRect, projector, flag);
	
	}


	/**
	 * Paint data into the graphics window. 
	 * @param g the graphics handle for drawing on. 
	 * @param windowRect Window rectangle to draw in
	 * @param pamAxis scroll start time in milliseconds
	 * @param graphAxis all the axis of the graph. Some may be invisible. Order is TOP, RIGHT, BOTTOM (x axis), LEFT (y axis)
	 * @param newDataUnit - the data unit to plot. 
	 */
	public void drawData(GraphicsContext g, Rectangle windowRect, DetectionPlotProjector projector, T pamDataUnit){

		drawData( g,  windowRect,  projector, pamDataUnit,  DetectionPlot.STANDARD_DRAW);
	}



	public void setupAxis( DetectionPlotProjector projector, T pamDataUnit){
		getCurrentDetectionPlot().setupAxis((PamDataUnit) pamDataUnit, this.getHardSampleRate(), projector);
	}

	//	/**
	//	 * Gets a value for a specific data unit which should be in the
	//	 * same units as the scale information. This will then be 
	//	 * converted into a plot position by the TDGraph. 
	//	 * @param pamDataUnit
	//	 * @return data value or null if this data point should not be plotted. 
	//	 */
	//	abstract public Double[] getYValue(PamDataUnit pamDataUnit);
	//	
	//	/**
	//	 * Gets a value for a specific data unit which should be in the
	//	 * same units as the scale information. This will then be 
	//	 * converted into a plot position by the TDGraph. 
	//	 * @param pamDataUnit
	//	 * @return data value or null if this data point should not be plotted. 
	//	 */
	//	abstract public Double[] getXValue(PamDataUnit pamDataUnit);


}
