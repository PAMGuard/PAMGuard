package dataPlotsFX.scrollingPlot2D;

import java.io.Serializable;

import PamController.PamController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.DataBlock2D;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDDisplayFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.spectrogramPlotFX.Spectrogram2DPlotData;
import dataPlotsFX.spectrogramPlotFX.SpectrogramParamsFX;

/**
 * Wrapper for the FFT data block allowing it to be plot as a spectrogram on a TDGraphFX. 
 * @author Doug Gillespie and Jamie Macaulay
 *
 * Was FFTPlotInfo
 */
@SuppressWarnings("rawtypes") 
abstract public class Scrolling2DPlotInfo extends TDDataInfoFX implements Plot2DColours {

	/**
	 * Parameters for the spectrogram display
	 */
	private PlotParams2D plotParams2D;

	/**
	 * Reference to the FT data block for the spectrogram
	 */
	private DataBlock2D dataBlock2D;

	/**
	 * Observer to monitor incoming FFT data
	 */
	private Data2DObserver fftObserver;

	/**
	 * List of possible channels to display
	 */
	private Scrolling2DPlotDataFX[] scrolling2DPlotData = new Scrolling2DPlotDataFX[PamConstants.MAX_CHANNELS];

	/**
	 * Scale information for frequency axis. 
	 * 
	 */
	private TDScaleInfo yScaleInfo;



	private StandardPlot2DColours spectrogramColours;

	/*
	 * The current frequency limits in real units (Hz, angles, etc.) Minimum and maximum values. 
	 * This is the MASTER of this value and ideally everything else should get and set from it. 
	 */
//	private DoubleProperty[] yScaleRange = new DoubleProperty[2];

	/**
	 * True if FFT data for these spectrograms is current being orderred. 
	 */
	private volatile boolean isOrderring=false; 



	public Scrolling2DPlotInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, DataBlock2D pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
//		this.fftControl = fftControl;
		this.dataBlock2D = pamDataBlock;
		plotParams2D = createPlotParams();
		createColours();

		yScaleInfo = createTDScaleInfo(this, 0, 1);
		//set the plot priority so the the spectrogram always determines how many plot panes are displayed when the frequency axis is shown. 
		yScaleInfo.setPlotPriority(TDScaleInfo.BASE_PRIORITY);

		super.getScaleInfos().add(yScaleInfo);

		fftObserver = new Data2DObserver();
		if (!isViewer) pamDataBlock.addObserver(fftObserver);

		bindPlotParams();

		spectrogramColours=new StandardPlot2DColours(plotParams2D); 

//		yScaleRange[0]=new SimpleDoubleProperty(0);
//		yScaleRange[1]=new SimpleDoubleProperty(100);
//		tdGraph.getCurrentScaleInfo().

		configureDisplay();

		//		Button button= new Button(); 
		//		button.setOnAction((action)->{
		//			orderOfflineData();
		//		});
		//		this.getTDGraph().setRight(button);


	}
	
	/**
	 * Create plot params. These will generally be extensions o f
	 * PlotParams2D to include additional information 
	 * required by specific plot types. 
	 * @return params controlling the plot. 
	 */
	public abstract PlotParams2D createPlotParams();

	/**
	 * Create a bespoke scale info object reflecting the right type of axis information
	 * @param minVal
	 * @param maxVal
	 * @return Scale informatoin. 
	 */
	public abstract TDScaleInfo createTDScaleInfo(Scrolling2DPlotInfo scrolingPlotinfo, double minVal, double maxVal);
	
	/**
	 * Set up binding between display and control panels. Amplitude params  / scales
	 * are handled in the base class, but here will need to do the frequency scale for 
	 * spectrograms and whatever required for other display types.  
	 */
	public abstract void bindPlotParams();
	

	@Override
	public void removeData() {
		super.removeData();
		getDataBlock().deleteObserver(fftObserver);
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TDScaleInfo getScaleInfo(
			boolean autoScale) {
		//ensure plot panes are set 
		setNPlotPanes(yScaleInfo,this.getDataBlock(), false); 
		return yScaleInfo;
	}

	@Override
	public TDScaleInfo getScaleInfo() {
		return getScaleInfo(false);
	}


	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public Serializable getStoredSettings() {
		//set amplitude limits
		DoubleProperty[] al = plotParams2D.getAmplitudeLimits();
		plotParams2D.amplitudeLimitsSerial[0]=al[0].getValue();
		plotParams2D.amplitudeLimitsSerial[1]=al[1].getValue();

		//set frequency limits
		return plotParams2D;
	}

	/**
	 * Set settings which have been read back from storage (the psf file).
	 * Assume these are of the right type and cast into whatever is needed !
	 * @param storedSettings
	 * @return true if all OK.
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {

		if (PlotParams2D.class.isAssignableFrom(storedSettings.getClass()) == false) {
			return false;
		}
		//SpectrogramParamsFX
		plotParams2D=(PlotParams2D) storedSettings;
		//create limit properties
		plotParams2D.createAmplitudeProperty();

		//bind frequency in params to td scale info i.e. the y axis. 
		bindPlotParams();

		spectrogramColours=new StandardPlot2DColours(plotParams2D); 
		

		return true;
	}

	@Override
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector ) {
		
		/**
		 * This makes the 2D plot look less blurry. Better for scientific data such as spectrograms. 
		 */
		g.setImageSmoothing(false);
		
		//System.out.println("chan: "+chan+ " "+specParams.channelList[plotNumber]+" "+specChannelData[chan]); 
		int chan=PamUtils.getSingleChannel(yScaleInfo.getPlotChannels()[plotNumber]);
		//clear the display
		//g.clearRect(0,0, tdProjector.getWindowRect().getWidth(), tdProjector.getWindowRect().getHeight());
		g.setFill(this.spectrogramColours.getColourArray()[0]);
		g.fillRect(0,0, tdProjector.getWindowRect().getWidth(), tdProjector.getWindowRect().getHeight());
		
		/***
		 * BUG 12/02/2017: THis is causing a NG canvas error on the JavaFX thread. 
		 */
		try{
			if (chan >= 0 && scrolling2DPlotData[chan] != null) {
				scrolling2DPlotData[chan].drawSpectrogram(g, tdProjector.getWindowRect(), 
						tdProjector.getOrientation(), tdProjector.getTimeAxis(), scrollStart, tdProjector.isWrap());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * The number of panels which need to be rebuilt 
	 */
	private int nRebuilt=0;

	/**
	 * @return the nRebuilt
	 */
	public int getnRebuiltPanels() {
		return nRebuilt;
	}

	/**
	 * @param nRebuilt the nRebuilt to set
	 */
	public void setnRebuiltPanels(int nRebuilt) {
		this.nRebuilt = nRebuilt;
	}

	/**
	 * Re-colour the whole saved spectrogram image. 
	 * @param thread. True to perform re-colour calculation on different thread. 
	 */
	public void reColourPlots(boolean thread){
		//System.out.println("RecolourPlots!!!!");
		nRebuilt=0; //keep a track of number of panels which have to be rebuilt. 
		for (int i=0; i<scrolling2DPlotData.length; i++){
			if (scrolling2DPlotData[i]!=null && thread){
				scrolling2DPlotData[i].reBuildImage();
			}
			else if (scrolling2DPlotData[i]!=null) scrolling2DPlotData[i].reBuildImage(null);
		}
	}

	/**
	 * Configure all the parameters for the spectrogram
	 * @return true if configuration has succeeded. 
	 */
	public synchronized boolean configureDisplay() {

		/**
		 * 23/02/2017
		 * Putting this function inside a runLater on theFX thread was causing all sortsz of very styrange problems. 
		 */
		try{
			/**
			 * SpecChannelData is ordered by channel but the fftScaleInfo is ordered by panel. A panel can show any channel.
			 *  
			 */
			int chan;
			outerLoop:
				for (int i = 0; i < scrolling2DPlotData.length; i++) {
					for (int j = 0; j < yScaleInfo.getPlotChannels().length; j++) {
						chan = PamUtils.getSingleChannel(yScaleInfo.getPlotChannels()[j]); 
						if (chan==i){
							if (scrolling2DPlotData[i] ==null) scrolling2DPlotData[i] = makeScrolling2DPlotData(chan);
							scrolling2DPlotData[i].checkConfig();
							continue outerLoop;
						}
					}
					//System.out.println("Set the channel to null: " + i); 
					scrolling2DPlotData[i] = null;
				}


			Platform.runLater(()->{
				// needed because this is currently on AWT thread and needs moving to FX thread. 
				finalConfigurationTasks();
			});

		}
		catch(Exception e){
			e.printStackTrace(); 
		}

		return true;
	}

	/**
	 * final configuration tasks called from Platform->runLater()
	 */
	public void finalConfigurationTasks() {
		
		//important to set the axis properly on the graph. 
		getTDGraph().checkAxis();

		createColours();
		
	}

	/**
	 * Create the colour arrays to use in the plots. 
	 */
	protected void createColours() {
		//		spectrogramColours=new GeneralSpectrogramColours(specParams); 
		//colourArray = ColourArray.createHotArray(256);
		if (spectrogramColours==null || plotParams2D==null || plotParams2D.getColourMap() == spectrogramColours.getColourMap()) {
			return;
		}
		spectrogramColours.setColourMap(plotParams2D.getColourMap());
	}


	/**
	 * Get the colour triplet for a particular db value. 
	 * @param dBLevel
	 * @return colour triplet. 
	 */
	public Color getColours(double dBLevel) {
		return spectrogramColours.getColours(dBLevel);
	}



	/**
	 * Observes incoming FFT data and updates the spectrogram. 
	 * 
	 * Note: that this data is on the another thread and must be switched to the FX thread before any processing takes place.  
	 * @author Jamie Macaulay
	 *
	 */
	private class Data2DObserver extends PamObserverAdapter {

		private float fftSampleRate;

		public static final int mb = 1024*1024;

		long count=0; 

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			//only add fft data units if it's this class that has orderrred the data.
			if (isOrderring || !isViewer){

				//				count++; 
				//				if (count%10000==0){
				//					Runtime runtime = Runtime.getRuntime();
				//					System.out.println("Used Memory:" 
				//							+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
				//					System.out.println("FFTPlotInfo: update(PamObservable o, PamDataUnit dataUnit) " + count 
				//							+ " " +fftDataBlock.getUnitsCount()+ " chan: "+PamUtils.getSingleChannel(dataUnit.getChannelBitmap())
				//							+ " "+dataUnit.getFrequency());
				//					count=0;
				//				}
//				if (count%1000==0) {
//					System.out.println("FFT time: " + PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds())+ "  " + o + " ");
//				}
				count++;

				new2DData((DataUnit2D) dataUnit); //do not put in FX thread!
				

				
				
				Platform.runLater(()->{
					//only repaint the base canvas. Otherwise overlaid detections will repaint and this can take a 
					//a very long time. 
					if (isViewer)
						getTDGraph().repaint(TDDisplayFX.STANDARD_REFRESH_MILLIS, TDPlotPane.BASE_CANVAS);
					else {
						//01/11/2019
						// change back to repaint everything - if we only repaint the base canvas, the spectrogram will scroll continuously but
						// overlay data will only get updated periodically and get out of sync with the spectrogram image
						getTDGraph().repaint(TDDisplayFX.STANDARD_REFRESH_MILLIS);	
					}
				});
			}
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			//System.out.println(" FFTPlotInfo: setSampleRate(float sampleRate, boolean notify)");
			this.fftSampleRate = sampleRate;
			// call function in outerclass which can be overridden...
			Platform.runLater(()->{
				Scrolling2DPlotInfo.this.setSampleRate(sampleRate, notify);
			});
		}


		@Override
		public String getObserverName() {
			return getDataName() + "Spectrogram";
		}


	}


	/**
	 * Set display related parameters for the spectrogram. 
	 * @return spectrogram parameters (different from FFT parameters).
	 */
	public PlotParams2D getPlot2DParameters() {
		return plotParams2D;
	}

	/**
	 * Get display related parameters for the spectrogram. 
	 * @param spectrogram parameters (different from FFT parameters).
	 */
	public void setPlot2DParameters(SpectrogramParamsFX spectrogramParameters) {
		this.plotParams2D = spectrogramParameters;
		configureDisplay();
	}

	public void setSampleRate(float sampleRate, boolean notify) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Set the y range of the displayed bins. 
	 * Mostly used by the spectrogram to zoom in 
	 * in frequency 
	 * @param lowVal lowest value to display
	 * @param highVal highest value to display
	 */
	public void setDisplayedDataRange(double lowVal, double highVal) {
//		yScaleRange[0].setValue(lowVal);
//		yScaleRange[1].setValue(highVal);
		
//		for (int i = 0; i < 2; i++) {
//			displayBinRange[i].setValue( dataBlock2D.getDataWidth() - displayBinRange[i].get());
//			displayBinRange[i].setValue(Math.min(Math.max(displayBinRange[i].get(), 0),
//					dataBlock2D.getDataWidth() - 1));
//		}
	}


	/**
	 * Called when new  data arrive. 
	 * @param dataUnit2D
	 */
	public void new2DData(DataUnit2D dataUnit2D) {
		
		//TODO - added this here because sometimes a data order returns units that are from the 
		//the wrong time. Not sure why this is but likely to do with queue issues perhaps. This 
		//temporary fix will help but will not work when we move the spectrogram to loading sections
		//of data outwith the visible display
		if (dataUnit2D.getTimeMilliseconds() < (getVisibleStart() -getSmooshMillis()) ||
				dataUnit2D.getTimeMilliseconds() > getVisibleEnd()	) {
			//System.out.println("data unit not in range: " + PamCalendar.formatDateTime2(dataUnit2D.getTimeMilliseconds())); 

			return;
		}
		


		int chan = PamUtils.getSingleChannel(dataUnit2D.getSequenceBitmap());

		if (scrolling2DPlotData[chan]==null){ 
			configureDisplay();
		}
		
		
		if (scrolling2DPlotData[chan] != null) {
//			if (scrolling2DPlotData[chan].getLastPowerSpecTime()>dataUnit2D.getEndTimeInMilliseconds()) {
//				System.out.println("Last data unit was after the current one: " + PamCalendar.formatDateTime2(dataUnit2D.getTimeMilliseconds())); 
//				scrolling2DPlotData[chan].resetForLoad();
//				return;
//			}
			scrolling2DPlotData[chan].new2DData(dataUnit2D);
		}
		

	}

	////Viewer Mode Functions/////

	private long diff=100; 
	long lastTime=0;
	private Timeline timeline;

	/**
	 * Order offline data for the spectrogram. This will only call if the scroll bar is moving. Or the last call 
	 * was less than time "diff" milliseconds before. The call is put into a timer thread which will execute the final 
	 * call once scrolling has stopped, ensuring the spectrogram is in sync time wise with the rest of the scrolling data. 
	 */
	private void orderOfflineData(){

		long currentTime=System.currentTimeMillis();
		
//		if (currentTime-lastTime<diff || isScrollChanging() ){

			//start a timer. If a rebuild hasn't be called because diff is TOO short this will ensure that 
			//the last rebuild which is less than diff is called. This means a final repaint is always called 
			if (timeline!=null){
				timeline.stop();
			}
			timeline = new Timeline(new KeyFrame(
					Duration.millis(diff),
					ae -> {
						try {
//							System.out.println("isScrollChanging(): " + isScrollChanging());
							//06/04/2017 Had to add the following if statement to prevent a FX canvas error. Not entirely sure why
							if (!isScrollChanging()){
								lastTime=currentTime;
								orderOfflineData(null);
							}
							else orderOfflineData();
								
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}));
			timeline.play();
			return;
//		}

	}

	//refers 	
	private volatile long lastReqStart;

	private volatile long lastReqEnd;

	/**
	 * Order offline viewer data for viewer mode. 
	 * @return - true if all data load successfully. 
	 * @throws InterruptedException  - throws the interrupted expression. We want the thread to be interrupted and cancelled here. Allows fast scrolling
	 */
	private synchronized boolean orderOfflineData(Task<Boolean> task) throws InterruptedException {
		
		
		//do not try and order an data before everything has set up. 
		if (!PamController.getInstance().isInitializationComplete()) return false;

		if (dataBlock2D == null) {
			return false;
		}
		if (scrolling2DPlotData == null) {
			return false;
		}

		//allow for fast scrolling. Thread goes to sleep and if interrupted just cancels. 
		if (task!=null) Thread.sleep(100);		
		if (task!=null && task.isCancelled()){
			return false; 
		}
		/**
		 * First cancel the last order and reset pointers in the output images. 
		 */
		dataBlock2D.cancelDataOrder(true);
		//don't know why, but some weird order quue stuff going on sometimes and this seems to 
		//help prevent extra data, outwith the order still being ordered...
		Thread.sleep(100);		

		for (int i = 0; i < scrolling2DPlotData.length; i++) {
			if (scrolling2DPlotData[i] != null) {
				if (task!=null && task.isCancelled()) return false; 
				//System.out.printf("In orderofflinedata: ");
				scrolling2DPlotData[i].resetForLoad();
			}
		}

		if (task!=null && task.isCancelled()) return false; 

		long dataStart = getVisibleStart()-getSmooshMillis();
		long dataEnd = getVisibleEnd();
		
		//System.out.println("Order offline data from: " + PamCalendar.formatDateTime2(dataStart) + "to : " + PamCalendar.formatDateTime2(dataEnd) + "  " + getSmooshMillis());


		if (lastReqStart == dataStart && lastReqEnd == dataEnd) {
			return true;
		}
		lastReqStart = dataStart;
		lastReqEnd = dataEnd;

		//need to know that it's THIS class that is ordering. Otherwise the observer can accept FFTData from the FFTDataBlock if another process is orderring
		isOrderring=true;

		//order offline data

		dataBlock2D.orderOfflineData(this.fftObserver, new DataLoadObserver(), dataStart, dataEnd, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);

		return true; 
	}
	
	
	/**
	 * Get the start time of the visible portion of the display.
	 * @return the start time of the display in millis.
	 */
	protected long getVisibleStart() {
		return getTDGraph().getTDDisplay().getTimeScroller().getValueMillis();
	}
	
	/**
	 * Get the end time of the visible portion of the display.
	 * @return the end time of the display in millis.
	 */
	protected long getVisibleEnd() {
		return getVisibleStart() + (long) (getTDGraph().getTDDisplay().getVisibleTime());
	}
	
	/**
	 * Get the extra millis for loading data. 
	 * @return the end time of the display in millis.
	 */
	protected long getSmooshMillis() {
		// assume we will never have an FFT length above 5 seconds in time. That should
		// be fine for blue whales with a sample rate of 500Hz == FFT length of 2500
		// samples which would look terrible.
		return Math.min((long) (getTDGraph().getTDDisplay().getVisibleTime()*0.2), 5000);
	}

	/**
	 * (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#timeScrollValueChanged(long)
	 */
	@Override
	public void timeScrollValueChanged(double valueMillis) {
		/*
		 *  Called in viewer mode - need to request FFT data in order to 
		 *  rebuild the  spectrogram image
		 */
		if (isViewer()) {
			orderOfflineData();
		}
	}

	/**
	 * (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#timeScrollRangeChanged(long, long)
	 */
	@Override
	public void timeScrollRangeChanged(double minimumMillis, double maximumMillis) {
		super.timeScrollRangeChanged(minimumMillis, maximumMillis);
		//		System.out.println(String.format("Spec time range change from %s to %s", PamCalendar.formatDateTime(minimumMillis),
		//				PamCalendar.formatTime(maximumMillis)));
		if (isViewer()) {
			orderOfflineData();
		}
	}

	@Override
	public void timeRangeSpinnerChange(double oldValue, double newValue) {	
		if (isViewer()) {
			orderOfflineData();
		}
	}

	/**
	 * Recolours the spectrogram image. On a different thread as it takes a bit of computational time.
	 * @author Jamie Macaulay
	 *
	 */
	private class LoadData2DTask extends Task<Boolean> {

		@Override
		protected Boolean call() throws Exception {
			if (this.isCancelled()) return  false; 
			return orderOfflineData(this);
		}	


		@Override protected void succeeded() {
			super.succeeded();
			Platform.runLater(()->{
				getTDGraph().repaint(0);
			});
			updateMessage("LoadData2DTask: Done!");
		}

		@Override protected void cancelled() {
			super.cancelled();
			Platform.runLater(()->{
				getTDGraph().repaint(0);
			});
			updateMessage("LoadData2DTask: Cancelled!");
		}

		@Override protected void failed() {
			super.failed();
			Platform.runLater(()->{
				getTDGraph().repaint(0);
			});
			updateMessage("LoadData2DTask: Failed!");
		}

	}


	/////////////////////////////////

	/**
	 * Get the FFT data block for this FFTPlotInfo
	 * @return an FFT Data Block
	 */
	public DataBlock2D getDataBlock2D() {
		return dataBlock2D;
	}

	/**
	 * Special case here with spectrogram. Want light icons for overlaid buttons for most spectrograms. 
	 */
	@Override
	public int getDisplayColType(){
		switch (plotParams2D.getColourMap()){
		case RED:
			return TDGraphFX.DARK_TD_DISPLAY; 
		case BLUE:
			return TDGraphFX.DARK_TD_DISPLAY; 
		case GREEN:
			return TDGraphFX.DARK_TD_DISPLAY; 
		case HOT:
			return TDGraphFX.DARK_TD_DISPLAY; 
		case GREY:
			return TDGraphFX.DARK_TD_DISPLAY; 
		case REVERSEGREY:
			return TDGraphFX.DARK_TD_DISPLAY;  
		default:
			return TDGraphFX.DARK_TD_DISPLAY; 
		}
	}

	/**
	 * Get the number of panels whihc are active i.e. are showing data.
	 * @return the number of active channels 
	 */
	public int getNActivePanels(){
		int nChan=0; 
		for (int i=0; i<scrolling2DPlotData.length; i++){
			if (scrolling2DPlotData[i]!=null) nChan++;
		}
		return nChan; 
	}


	/**
	 * Put this into a function so that if a display needs to extend the 
	 * Spectrogram2DPlotdata class in any way, it can easily do so. 
	 * @param iChannel channel number
	 * @return plot information. 
	 */
	public Scrolling2DPlotDataFX makeScrolling2DPlotData(int iChannel) {
		return new Spectrogram2DPlotData(this, iChannel);
	}

	/**
	 * Get the colour of the wrap line. It should contrast with the spectrogram. 
	 * @return the colour of the wrap line. 
	 */
	@Override
	public Color getWrapColor() {
		return this.plotParams2D.getWrapColour();
	}

	public StandardPlot2DColours getSpectrogramColours() {
		return this.spectrogramColours;
	}

	/**
	 * @param spectrogramColours the spectrogramColours to set
	 */
	public void setSpectrogramColours(StandardPlot2DColours spectrogramColours) {
		this.spectrogramColours = spectrogramColours;
	}

	/**
	 * Convenience function to get at the axis min value
	 * @return Data axis minimum value
	 */
	public double getDataAxisMinVal() {
		return getTDGraph().getDataAxisMinVal();
	}

	/**
	 * Convenience function to get at the axis max value
	 * @return Data axis maximum value
	 */
	public double getDataAxisMaxVal() {
		return getTDGraph().getDataAxisMaxVal();
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object, long)
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg, long nonminalTime) {
		/*
		 * no need to keep any data at all for this since it's created it's own more efficient
		 * data store.  
		 */
		return 0;
	}
	
	/**
	 * Notify of changes from PamController. 
	 * @param changeType - the chnage type. 
	 */
	@Override
	public void notifyChange(int changeType){
		switch (changeType) {
		case PamController.OFFLINE_DATA_LOADED :
			//this.orderOfflineData();
			break;
		}
	}
	

}
