package dataPlotsFX.rawDataPlotFX;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;
import Acquisition.AcquisitionControl;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;


public class RawSoundDataInfo extends TDDataInfoFX {

	/**
	 * Reference to the raw data block. 
	 */
	private PamRawDataBlock rawDataBlock;

	/**
	 * Reference to sound acquisition control
	 */
	private AcquisitionControl aquisitionControl;

	/**
	 * List of possible channels to display. rawChannelData[0] is for showing channel 0,  rawChannelData[1] for showing channel 1 etc. 
	 */
	private RawSoundPlotDataFX[] rawChannelData = new RawSoundPlotDataFX[PamConstants.MAX_CHANNELS];

	/**
	 * Scale information for showing raw sound data. Note, always shows only <b>one</b> channel on one panel.
	 */
	private TDScaleInfo amplitudeScaleInfo;

	/**
	 * Observer which waits for incoming sound data and then passes to relevant plot pane. 
	 */
	private RawDataObserver rawObserver;

	public RawSoundDataInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, AcquisitionControl control,
			PamRawDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.aquisitionControl = control;
		this.rawDataBlock=pamDataBlock;

		//add observer to update display. 
		rawObserver = new RawDataObserver(); 
		//only add observer if not in viewer mdoe. 
		if (!isViewer) pamDataBlock.addObserver(rawObserver);

		//add types of data that can be displayed by this data unit
		//create data axis scale information for each type. Assume a 16 bit sound card 
		amplitudeScaleInfo = new TDScaleInfo(-1, 1, ParameterType.AMPLITUDE, ParameterUnits.RAW);
		this.getScaleInfos().add(amplitudeScaleInfo);


		setNPlotPanes(amplitudeScaleInfo, pamDataBlock, false);

		//set the plot priority so the the spectrogram always determines how many 
		//plot panes are displayed when the frequency axis is shown. 
		amplitudeScaleInfo.setPlotPriority(TDScaleInfo.BASE_PRIORITY);

		configureRawDisplay(); 

	}

	double wrapPix;

	@Override
	public synchronized void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		// clear screen
//		System.out.println("Draw data:"); 
		g.clearRect(0, 0, tdProjector.getWindowRect().getWidth(), tdProjector.getWindowRect().getHeight());
		
		// call all panels to repaint.
		for (int i = 0; i < amplitudeScaleInfo.getNPlots(); i++) {
			int chan = PamUtils.getSingleChannel(amplitudeScaleInfo.getPlotChannels()[i]);
			if (chan == -1)
				continue;

			// check if wrapping
			if (tdProjector.isWrap())
				wrapPix = this.getTDGraph().getTDDisplay().getWrapPix();
			else
				wrapPix = -1;

			// draw raw sound data.
			rawChannelData[chan].drawRawSoundData(g, tdProjector.getWindowRect(), tdProjector.getOrientation(),
					tdProjector.getTimeAxis(), tdProjector.getYAxis(), scrollStart, wrapPix);
		}
	}


	/**
	 * Called whenever new raw sound data is available to be displayed. //
	 * @param rawDataUnit - new RawDataUnit to display.
	 */
	private void newRawData(RawDataUnit rawDataUnit){
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		if (rawChannelData[chan]==null) configureRawDisplay() ;
		if (rawChannelData[chan] != null) {
			rawChannelData[chan].newRawData(rawDataUnit, getBinsPerPixel());
		}
	}

	/**
	 * Check all settings are correct for the raw data display. 
	 */
	private void configureRawDisplay() {

		for (int i = 0; i < rawChannelData.length; i++) {
			int chan = amplitudeScaleInfo.getPlotChannels()[i]; 
			//			System.out.println("nUser: "+nUsers); 
			//for the raw data display 0 indicates that no users are using channel. (On other display sometimes indicates all channels are shown)
			if (chan == 0) {
				rawChannelData[i] = null;
				continue;
			}
			if (rawChannelData[i] == null) {
				rawChannelData[i] = new RawSoundPlotDataFX(this, PamUtils.getSingleChannel(chan));
			}
			rawChannelData[i].checkConfig();
		}
		getTDGraph().checkAxis();

	}

	/**
	 * Set sample rate for all displays. 
	 */
	private void setDisplaySample(){
		if (rawChannelData==null) return;
		for (int i=0; i<rawChannelData.length; i++){
			if ( rawChannelData[i]!=null) rawChannelData[i].setSampleRate(this.getSampleRate());
		}
	}

	/**
	 * Waits for incoming sound data and updates the spectrogram. 
	 * 
	 * Note: that this data is on the AWT thread and must be switched to the FX thread before any processing takes place.  
	 * @author Jamie Macaulay
	 *
	 */
	private class RawDataObserver extends PamObserverAdapter {

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return 0;
		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
//			System.out.println("RawDataInfo: update(PamObservable o, PamDataUnit dataUnit)");
			Platform.runLater(()->{
				newRawData((RawDataUnit) dataUnit);
			});
		}

		@Override
		public void removeObservable(PamObservable o) {
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			setDisplaySample();
		}

		@Override
		public void noteNewSettings() {
		}

		@Override
		public String getObserverName() {
			return getDataName() + "Raw Data Observer";
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			// TODO Auto-generated method stub
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

	}

	@Override
	public TDScaleInfo getScaleInfo(
			boolean autoScale) {
		//		amplitudeScaleInfo.setnPlots(PamUtils.getNumChannels(getDataBlock().getChannelMap()));
		amplitudeScaleInfo.setnPlots(PamUtils.getNumChannels(getDataBlock().getSequenceMap()));
		return amplitudeScaleInfo;
	}

	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		//not used in drawing raw data units. 
		return null;
	}

	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		//not used in drawing raw data units. 
		return null;
	}

	/**
	 * Get the sample rate of the raw sound data. 
	 * @return sample rate of the raw sound data in S/s. 
	 */
	protected float getSampleRate(){
		return rawDataBlock.getSampleRate();
	}

	/**
	 * Get the number of bins per pixel. This is calculated from the width of the tdGraph in pixels, the time the display represents and
	 * the sample rate of raw sound data. 
	 * @return the number of bins per pixel. 
	 */
	protected double getBinsPerPixel() {
		return (getTDGraph().getTDDisplay().getTimeAxis().getMaxVal()
				- getTDGraph().getTDDisplay().getTimeAxis().getMinVal()) * getSampleRate()
				/ getTDGraph().getTDDisplay().getTimeAxis().getTotalPixels();
	}

	/**
	 * (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#timeScrollRangeChanged(long, long)
	 */
	@Override
	public void timeScrollRangeChanged(double minimumMillis, double maximumMillis) {
		super.timeScrollRangeChanged(minimumMillis, maximumMillis);
		//		System.out.println(String.format("RawSoundDataInfo: Spec time range change from %s to %s", PamCalendar.formatDateTime(minimumMillis),
		//				PamCalendar.formatTime(maximumMillis)));
		if (isViewer()) {
			orderOfflineData();
		}
	}

	@Override
	public void timeRangeSpinnerChange(double oldValue, double newValue) {	
		//System.out.println("RawDataInfo: spinner changed: old value "+oldValue+" new value "+newValue); 
		if (isViewer()) {
			orderOfflineData();
		}
		else {
			recalcSoundData();
		}
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
	 * Recalculate the sound array. 
	 */
	private void recalcSoundData(){
		for (int i=0; i<rawChannelData.length; i++){
			if (rawChannelData[i]!=null) rawChannelData[i].recalcSoundData(getBinsPerPixel());
		}
	}


	/**
	 * Clear the raw sound data plot panes. 
	 */
	private void clearRawPlotPanes(){
		for (int i=0; i<rawChannelData.length; i++){
			if (rawChannelData[i]!=null) rawChannelData[i].clearRawData(); 
		}
	}

	/**
	 * Viewer mode functions. 
	 */

	private long diff=100; 
	long lastTime=0;
	private Timeline timeline;
	private long lastReqStart;
	private long lastReqEnd;
	
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
						if (!isScrollChanging()){
							lastTime=currentTime;
							orderRawData();
						}
						else orderOfflineData();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}));
		timeline.play();
		return;
		//		
	}


	/**
	 * Order offline viewer data for viewer mode. 
	 * @return - true if all data load successfully. 
	 */
	private synchronized boolean orderRawData() {
		
		//do not try and order an data before everything has set up. 
		if (!PamController.getInstance().isInitializationComplete()) return false;

		if (rawDataBlock == null) {
			return false;
		}
		if (rawChannelData == null) {
			return false;
		}
		
		/**
		 * First cancel the last order and reset pointers in the output images. 
		 */
		rawDataBlock.cancelDataOrder();

		//clear the plot panes. 
		clearRawPlotPanes(); 

		long dataStart =getTDGraph().getTDDisplay().getTimeScroller().getValueMillis()-1500;
		long dataEnd = dataStart + (long) (getTDGraph().getTDDisplay().getVisibleTime())+1500;

		if (lastReqStart == dataStart && lastReqEnd == dataEnd) {
			return true;
		}
		
		lastReqStart = dataStart;
		lastReqEnd = dataEnd;

		//Need to know that it's THIS class that is ordering. 
		//Otherwise the observer can accept FFTData from the FFTDataBlock if another process is ordering. 
		super.isOrderring=true;

		rawDataBlock.orderOfflineData(this.rawObserver, new DataLoadObserver(), 
				dataStart, dataEnd, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);

		return true; 
	}

}
