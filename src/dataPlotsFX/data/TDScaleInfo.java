package dataPlotsFX.data;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.debug.Debug;
import dataPlots.data.TDDataInfo;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Information about the scale requirements
 * of a set of plotable data. Contains 
 * an ever increasing set of options !
 * @author Doug Gillespie and Jamie Macaulay
 *
 */
public class TDScaleInfo {

	/**
	 * The data type information. 
	 */
	private DataTypeInfo dataTypeInfo;

	/**
	 * Underlying data class which can be serialized. 
	 */
	private TDScaleInfoData scaleInfoData = new TDScaleInfoData();


	/**
	 * Override other nPlots and display number of panels
	 */
	public static final int BASE_PRIORITY=0; 

	/**
	 * Priority for number of plots is dependent on position of associated 
	 * {@link TDDataInfo} in data info list. 
	 */
	public static final int INLIST_PRIORITY=1; 

	/**
	 * The minimum value of the y axis. (x axis when graph is vertical)
	 */
	transient private DoubleProperty minVal = new SimpleDoubleProperty(0);

	/**
	 * The maximum value of the y axis (x axis when graph is vertical)
	 */
	transient private DoubleProperty maxVal = new SimpleDoubleProperty(1);

	/**
	 * The number of separate plots which can be shown on the TDGraph. 
	 * This is NOT the number of visible plots. 
	 */
	transient private IntegerProperty nPlots = new SimpleIntegerProperty(1);

	/**
	 * The plot priority is used to determine how many plot panes should show in a
	 * graph when there are multiple data blocks displayed. For example, consider a
	 * spectrogram and click data block. Clicks can be displayed per channel on a
	 * frequency time display- there maybe 10 channels of clicks, however the
	 * spectrogram may only be set to show 4 channels. In this case one has to
	 * override the the other- hence if spectrogram is set to BASE_PRIORITY it will
	 * always override clicks for the number of plots. In addition to overriding the
	 * number of plots to show priority also indicates what layer of the draw canvas
	 * data units are plotted on. BASE_PRIORITY plots data on the lowest canvas
	 * layer i.e. the background whilst INLIST_PRIORITY will plot data on the middle
	 * canvas layer.
	 */
	private int plotPriority=INLIST_PRIORITY;

	/**
	 * Indicates that the axis is reversed so if true then the 
	 *  minimum is at top of graph and the maximum is a the bottom 
	 */
	private boolean reverseAxis = false; 


	//	@Deprecated
	//	public TDScaleInfo(double minVal, double maxVal) {
	//		super();
	//		this.minVal.setValue(minVal);
	//		this.maxVal.setValue(maxVal);
	//		setAllVisible(true);
	//		addDivisorListener();
	//	}

	public TDScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits) {
		super();
		this.minVal.setValue(minVal);
		this.maxVal.setValue(maxVal);
		this.dataTypeInfo=new DataTypeInfo(dataType, dataUnits); 
		setAllVisible(true);
		addDivisorListener();
	}

	public TDScaleInfo(double minVal, double maxVal, ParameterType dataType, ParameterUnits dataUnits, int nPlots) {
		this(minVal, maxVal, dataType, dataUnits);
		this.nPlots.setValue(nPlots);
	}

	/**
	 * The divisor listener. 
	 */
	public void addDivisorListener(){
		
		minVal.addListener((obs, oldVal, newVal) -> {
			if (scaleInfoData.autoDivisor) calcDivisor();
			scaleInfoData.minVal = newVal.doubleValue();
		}); 

		maxVal.addListener((obs, oldVal, newVal) -> {
			if (scaleInfoData.autoDivisor) calcDivisor();
			scaleInfoData.maxVal = newVal.doubleValue();
		});
	}

	/**
	 * Set all the scale info visible. 
	 * @param  true to set all visible. 
	 */
	private void setAllVisible(boolean visible){
		scaleInfoData.setAllVisible(visible);
	}

	/**
	 * Automatically calculate the unit divisor. 
	 */
	public void calcDivisor(){
		scaleInfoData.unitDivisor=1; 
		if (maxVal.doubleValue()>=1000) {
			scaleInfoData.unitDivisor=1000;
		}
		else if (maxVal.doubleValue()>=1000000) scaleInfoData.unitDivisor=10000000;
		else if (maxVal.doubleValue()>=1000000000) scaleInfoData.unitDivisor=1000000000;
	}

	/**
	 * Get a string representing the divisor unit. 
	 */
	public String getDivisorString(){
		if (scaleInfoData.unitDivisor==1000) return "k"; //kilo
		else if (scaleInfoData.unitDivisor==1000000) return "M"; //mega
		else if (scaleInfoData.unitDivisor==1000000000) return "G"; //giga
		else if (scaleInfoData.unitDivisor==0.01) return "m"; //millis
		else if (scaleInfoData.unitDivisor==0.00001) return "\u00B5"; //micro
		else return ""; 
	}

	/**
	 * Get the minimum value property for the scale information. 
	 * @return minimum axis value property 
	 */
	public DoubleProperty minValProperty() {
		return  minVal;
	}

	/**
	 * Get the maximum value property for the scale information. 
	 * @return  maximum axis value property 
	 */
	public DoubleProperty maxValProperty() {
		return  maxVal;
	}

	/**
	 * Get the minimum value of the y axis (x axis when vertical). 
	 * @return the minVal
	 */
	public double getMinVal() {
		return minVal.get();
	}

	/**
	 * Set the minimum value of the y axis (x axis when vertical). 
	 * @param minVal the minVal to set
	 */
	public void setMinVal(double minVal) {
		this.minVal.setValue(minVal);
	}

	/**
	 * Get the maximum value of the y axis (x axis when vertical). 
	 * @return the maximum value.
	 */
	public double getMaxVal() {
		return maxVal.get();
	}

	/**
	 * Set the maximum value of the y axis (x axis when vertical). 
	 * @param maxVal the maxVal to set
	 */
	public void setMaxVal(double maxVal) {
		this.maxVal.setValue(maxVal);
	}

	/**
	 * The number of plot panes.
	 * @return the number of separate plot graphs to display.
	 */
	public int getNPlots() {
		return Math.max(1, nPlots.getValue());
	}

	/**
	 * Set the number of plot panes.
	 * @param nPlots the number of plot graphs to show. 
	 */
	public void setnPlots(int nPlots) {
		this.nPlots .setValue(nPlots);
	}

	/**
	 * Get the plot priority.The plot is used to determine how many plot panes 
	 * should show in a graph when there are multiple data blocks displayed.
	 * Note: if a base priority then it is the responsibility of the associated 
	 * TDDataInfo to draw a wrap line. 
	 * @return the plot priority. 
	 */
	public int getPlotPriority() {
		return plotPriority;
	}

	/**
	 * Set the plot priority. 
	 * @param plotPriority
	 */
	public void setPlotPriority(int plotPriority) {
		this.plotPriority = plotPriority;
	}

	/**
	 * Get the channels/sequence numbers associated with each plot pane. 0 indicates pane can show all channels 
	 * (although will not necessarily show all channels depending on TDDataInof settings). 
	 * @return a list of channel/sequence bitmaps indicating which channels are associated with which plot panes. 
	 */
	public int[] getPlotChannels() {
		return scaleInfoData.getPlotChannels();
	}

	/**
	 * Get plot panes which are shown on the graph . 
	 */
	public boolean[] getVisibleChannels() {
		return scaleInfoData.getVisiblePlotChannels();
	}

	/**
	 * Set the channels/sequences associated with each plot pane.
	 * @param plotChannels a list of channel/sequence bitmaps indicating which channels are associated with which plot panes.
	 */
	public void setPlotChannels(int[] plotChannels) {
		scaleInfoData.setPlotChannels(plotChannels);;
	}

	/**
	 * Set a unit divisor for display purposes. e.g 1000 would divide by 1000 and make Hz 
	 * kHz. 
	 * @param unitDivisor - unit divisor.
	 */
	public void setUnitDivisor(double unitDivisor){
		scaleInfoData.unitDivisor=unitDivisor;
	}

	/**
	 * Get a unit divisor for display purposes. e.g 1000 would divide by 1000 and make Hz 
	 * kHz. 
	 * @return unitDivisor - unit divisor.
	 */
	public double getUnitDivisor(){
		return scaleInfoData.unitDivisor;
	}

	/**
	 * The DataTypeInfo. Contains both data type and unit. 
	 * @return the dataTypeInfo for the scale information. 
	 */
	public DataTypeInfo getDataTypeInfo(){
		return this.dataTypeInfo;
	}

	/**
	 * The data type e.g. bearing. 
	 * @return the data type
	 */
	public ParameterType getDataType(){
		return this.dataTypeInfo.dataType;
	}

	/**
	 * The data units. e.g. Hz
	 * @return the data units. 
	 */
	public ParameterUnits getDataUnit(){
		return this.dataTypeInfo.dataUnits;
	}

	/**
	 * Is this scale type currently available ? This is needed for data types which may 
	 * change their localisation content dynamically. For instance, if a datablock has a 
	 * beam former added to it's output, it will then have bearing information which it would
	 * not have if the datablock was not linked up to a localiser. 
	 * @return True if it's currently possible to plot data units on this axis type. 
	 */
	public boolean isAvailable() {
		return true;
	}


	/**
	 * Get the name of the axis. 
	 * @return name of the axis. 
	 */
	public String getAxisName() {
		return TDProjectorFX.getAxisName(dataTypeInfo.dataType);
	}

	public static String getAxisName(DataTypeInfo axisType) {
		return TDProjectorFX.getAxisName(axisType.dataType);
	}

	public static String getUnitName(DataTypeInfo axisType) {
		return TDProjectorFX.getUnitName(axisType.dataUnits);
	}

	/**
	 * Integer property for the number of plots. This is NOT the number of visible plots. 
	 * @return the number of plots property
	 */
	public IntegerProperty nPlotsProperty() {
		return nPlots;
	}

	//	public void setDataTypeInfo(DataTypeInfo dataTypeInfo2) {
	//		this.dataTypeInfo=dataTypeInfo2;
	//	}

	/**
	 * The visible plots. This follows follows @link getPlotChannels. True indicates a plot is shown and false indicates a plot is not visible. 
	 * @return an array indicating which plots are shown and which are not. 
	 */
	public boolean[] getVisiblePlots() {
		return scaleInfoData.getVisiblePlotChannels();
	}

	/**
	 * Get the number of visible plots
	 * @return the number of visible plots. 
	 */
	public int getNVisiblePlots() {
		int n=0; 
		boolean[] visiblePlotChannels = scaleInfoData.getVisiblePlotChannels();
		if (visiblePlotChannels == null) {
			return 0;
		}
		for (int i=0; i<getNPlots(); i++){
			if (visiblePlotChannels[i]) n++;
		}
		return n; 
	}

	/**
	 * Get the scale info data. 
	 * @return the scaleInfoData
	 */
	public TDScaleInfoData getScaleInfoData() {
		//10/12/2019 update the minimum and maximum valuyes as they're bound up in DoubleProperties 
		//with listeners etc. This is likely the reason that these were resetting during PG start
		scaleInfoData.maxVal = this.getMaxVal();
		scaleInfoData.minVal = this.getMinVal();

		return scaleInfoData;
	}

	/**
	 * Set the scale info values based on scale info data. 
	 * @param scaleInfoData the scaleInfoData to set
	 */
	public void setScaleInfoData(TDScaleInfoData scaleInfoData) {
		this.scaleInfoData = scaleInfoData;
		minVal.set(scaleInfoData.minVal);
		maxVal.set(scaleInfoData.maxVal);
	}

	/**
	 * If true then the axis is reversed
	 * @param b - true to reverse the axis
	 */
	public void setReverseAxis(boolean reverseAxis) {
		this.reverseAxis=reverseAxis; 	
	}

	/**
	 * Get the reverse axis. 
	 * @return the reverse axis.
	 */
	public boolean getReverseAxis() {
		return reverseAxis; 
	}




}
