package dataPlotsFX.rawDataPlotFX;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

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
import dataPlotsFX.layout.TDSettingsPane;
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

	/**
	 * Display settings for the waveform plot.
	 */
	private RawSoundParamsFX rawSoundParams = new RawSoundParamsFX();

	/**
	 * Settings pane which allows a user to change the waveform colours.
	 */
	private RawSoundSettingsPane settingsPane;

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
		g.clearRect(0, 0, tdProjector.getWindowRect().getWidth(), tdProjector.getWindowRect().getHeight());

		/*
		 * Each plot pane shows one channel, so only draw the channel which belongs to
		 * this pane. (Drawing every channel here would superimpose all the waveforms on
		 * every pane.)
		 */
		if (plotNumber < 0 || plotNumber >= amplitudeScaleInfo.getPlotChannels().length) {
			return;
		}
		int chan = PamUtils.getSingleChannel(amplitudeScaleInfo.getPlotChannels()[plotNumber]);
		if (chan < 0 || chan >= rawChannelData.length || rawChannelData[chan] == null) {
			return;
		}

		// check if wrapping
		if (tdProjector.isWrap())
			wrapPix = this.getTDGraph().getTDDisplay().getWrapPix();
		else
			wrapPix = -1;

		// draw raw sound data.
		rawChannelData[chan].drawRawSoundData(g, tdProjector.getWindowRect(), tdProjector.getOrientation(),
				tdProjector.getTimeAxis(), tdProjector.getYAxis(), scrollStart, wrapPix);
	}


	/**
	 * Called whenever new raw sound data is available to be displayed. //
	 * @param rawDataUnit - new RawDataUnit to display.
	 */
	private void newRawData(RawDataUnit rawDataUnit){
		int chan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		if (chan < 0 || chan >= rawChannelData.length) return;
		if (rawChannelData[chan]==null) configureRawDisplay() ;
		if (rawChannelData[chan] != null) {
			rawChannelData[chan].newRawData(rawDataUnit, getBinsPerPixel());
		}
	}

	/**
	 * Check all settings are correct for the raw data display.
	 */
	private void configureRawDisplay() {

		/*
		 * Note that the plot data are indexed by channel number, not by plot number, so
		 * that they can be found from an incoming data unit's channel. The list of plot
		 * channels is indexed by plot number, so the two are only the same when the
		 * channels in use start at zero and are contiguous.
		 */
		boolean[] used = new boolean[rawChannelData.length];
		for (int i = 0; i < amplitudeScaleInfo.getNPlots(); i++) {
			int chanMap = amplitudeScaleInfo.getPlotChannels()[i];
			//for the raw data display 0 indicates that no users are using channel. (On other display sometimes indicates all channels are shown)
			if (chanMap == 0) {
				continue;
			}
			int chan = PamUtils.getSingleChannel(chanMap);
			if (chan < 0 || chan >= rawChannelData.length) {
				continue;
			}
			used[chan] = true;
			if (rawChannelData[chan] == null) {
				rawChannelData[chan] = new RawSoundPlotDataFX(this, chan);
			}
			rawChannelData[chan].checkConfig();
		}
		for (int i = 0; i < rawChannelData.length; i++) {
			if (!used[i]) {
				rawChannelData[i] = null;
			}
		}
		updateLineColours();
		getTDGraph().checkAxis();

	}

	/**
	 * Get the display settings for the waveform plot.
	 *
	 * @return the waveform display parameters.
	 */
	public RawSoundParamsFX getRawSoundParams() {
		return rawSoundParams;
	}

	/**
	 * Set the colour of each waveform plot from the current display settings.
	 */
	public void updateLineColours() {
		for (int i = 0; i < rawChannelData.length; i++) {
			if (rawChannelData[i] != null) {
				rawChannelData[i].setLineColor(rawSoundParams.getLineColour(i));
			}
		}
	}

	@Override
	public TDSettingsPane getGraphSettingsPane() {
		if (settingsPane == null) {
			settingsPane = new RawSoundSettingsPane(this);
		}
		return settingsPane;
	}

	@Override
	public Serializable getStoredSettings() {
		return rawSoundParams;
	}

	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		if (storedSettings instanceof RawSoundParamsFX) {
			rawSoundParams = (RawSoundParamsFX) storedSettings;
			updateLineColours();
			if (settingsPane != null) {
				settingsPane.setParams();
			}
			return true;
		}
		return false;
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

	@Override
	public void notifyChange(int changeType) {
		switch (changeType) {
		case PamController.CHANGED_OFFLINE_DATASTORE:
			//everything held is from the old data store, so it is no longer valid. Clearing
			//the plots also clears what they think they hold, so it is all loaded again.
			clearRawPlotPanes();
			break;
		case PamController.CHANGED_DISPLAY_SETTINGS:
			//the user has changed the colour scheme or the colour blind palette, so the
			//standard PAMGuard colours this plot is drawn in have changed.
			colourSchemeChanged();
			break;
		}
	}

	/**
	 * The standard PAMGuard colours have changed. Re-read the waveform colours, put
	 * the current colours on the settings pane and repaint.
	 */
	private void colourSchemeChanged() {
		//may arrive on the event dispatch thread, and everything below is JavaFX.
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(this::colourSchemeChanged);
			return;
		}
		updateLineColours();
		if (settingsPane != null) {
			settingsPane.setParams();
		}
		getTDGraph().repaint(0);
	}

	/**
	 * Viewer mode functions. 
	 */

	private long diff=100;
	long lastTime=0;
	private Timeline timeline;

	/**
	 * The fraction of the visible time range which is also loaded either side of it.
	 * <p>
	 * Without this the waveform is loaded for exactly what is on screen, so any move
	 * of the display - a click on a scroll arrow, say - reveals data which has never
	 * been loaded and the user watches it appear. Loading half a screen either side
	 * means a small move shows data which is already held, with no disk access at
	 * all. This mirrors the pre-load either side of the visible range which the
	 * tiled spectrogram does (see {@code Scrolling2DPlotInfo}).
	 */
	private static final double LOAD_MARGIN_FRACTION = 0.5;

	/**
	 * Extra time loaded before the start of the range, in millis. Raw data units are
	 * up to a second or so long, so an order starting exactly at the range start can
	 * miss the unit which straddles it.
	 */
	private static final long LOAD_LEAD_MILLIS = 1500;

	/**
	 * Spans of time still to be ordered, most useful first. Ordering them one at a
	 * time (rather than as one big request) keeps the part of the range the user can
	 * actually see loading first.
	 */
	private final ArrayDeque<double[]> loadQueue = new ArrayDeque<>();

	/** Lock guarding {@link #loadQueue}. */
	private final Object loadQueueLock = new Object();

	/**
	 * Incremented every time a fresh load is worked out, so that the completion of an
	 * order from a superseded load does not carry on ordering spans which are no
	 * longer wanted.
	 */
	private volatile int loadGeneration = 0;


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
	 * <p>
	 * Only the parts of the range which are not already held are ordered, and the
	 * range runs half a screen either side of the visible one. So a move of the
	 * display smaller than that margin orders nothing at all, and a larger one orders
	 * only the strip which is genuinely new - the waveform store holds its data
	 * against absolute time, so everything already loaded stays on screen throughout.
	 * <p>
	 * Because the store knows what it holds rather than what was last asked for, an
	 * order which was cut short (by the user scrolling again, say) leaves the part it
	 * never reached un-held and it is asked for again next time, instead of being
	 * left as a gap in the waveform.
	 *
	 * @return - true if the load was started, or nothing needed loading.
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

		long visStart = getTDGraph().getTDDisplay().getTimeScroller().getValueMillis();
		long visible = (long) getTDGraph().getTDDisplay().getVisibleTime();
		long margin = (long) (visible*LOAD_MARGIN_FRACTION);
		double loadStart = visStart - margin - LOAD_LEAD_MILLIS;
		double loadEnd = visStart + visible + margin;

		//What is missing across all the channels on show? They all load from the same
		//orders, but a channel which has just been added to the display holds nothing
		//yet, so take the union rather than asking any one of them.
		List<double[]> required = requiredLoadIntervals(loadStart, loadEnd);
		if (required.isEmpty()) {
			//Everything on screen (and the margin either side) is already held: nothing to
			//load and nothing for the user to watch loading.
			return true;
		}

		//Load what is on screen before the margins either side of it.
		List<double[]> visibleFirst = new ArrayList<double[]>();
		List<double[]> margins = new ArrayList<double[]>();
		for (double[] interval : required) {
			if (interval[1] > visStart && interval[0] < visStart+visible) {
				visibleFirst.add(interval);
			}
			else {
				margins.add(interval);
			}
		}
		visibleFirst.addAll(margins);

		synchronized (loadQueueLock) {
			loadQueue.clear();
			loadQueue.addAll(visibleFirst);
		}

		orderNextInterval(++loadGeneration);

		return true;
	}

	/**
	 * The spans of the given range which are not held by every channel on show.
	 * @param loadStart - start of the range of interest in millis.
	 * @param loadEnd - end of the range of interest in millis.
	 * @return the spans still needing to be loaded, in time order.
	 */
	private List<double[]> requiredLoadIntervals(double loadStart, double loadEnd) {
		List<double[]> all = new ArrayList<double[]>();
		for (int i = 0; i < rawChannelData.length; i++) {
			if (rawChannelData[i] != null) {
				all.addAll(rawChannelData[i].getRequiredLoadIntervals(loadStart, loadEnd));
			}
		}
		//merge the per channel lists into one set of spans covering everything any
		//channel is missing.
		all.sort((a, b) -> Double.compare(a[0], b[0]));
		List<double[]> merged = new ArrayList<double[]>();
		for (double[] interval : all) {
			if (!merged.isEmpty() && interval[0] <= merged.get(merged.size()-1)[1]) {
				double[] last = merged.get(merged.size()-1);
				last[1] = Math.max(last[1], interval[1]);
			}
			else {
				merged.add(new double[] {interval[0], interval[1]});
			}
		}
		return merged;
	}

	/**
	 * Order the next span in the queue, so long as it belongs to the current load.
	 * @param generation - the load this chain of orders belongs to.
	 */
	private void orderNextInterval(int generation) {
		if (generation != loadGeneration) {
			return; //a newer load has taken over.
		}
		double[] next;
		synchronized (loadQueueLock) {
			next = loadQueue.poll();
		}
		if (next == null) {
			Platform.runLater(()->{
				getTDGraph().repaint(0);
			});
			return;
		}

		/*
		 * Cancel the previous order. The waveform store is not cleared - it is held
		 * against absolute time, so anything already loaded is still in the right place
		 * and carries on being displayed while the new data load. Data arriving at a new
		 * resolution replace the old data for the same time as they arrive.
		 */
		rawDataBlock.cancelDataOrder();

		//Need to know that it's THIS class that is ordering.
		//Otherwise the observer can accept FFTData from the FFTDataBlock if another process is ordering.
		super.isOrderring=true;

		/*
		 * allowRepeats = true: what does or does not need loading is decided from what the
		 * store actually holds, so the data block's own 'same request' check must not
		 * suppress a repeat - an order which was cancelled before it delivered everything
		 * has to be repeatable, or the part it missed would stay missing.
		 */
		rawDataBlock.orderOfflineData(this.rawObserver,
				new RawLoadObserver(next[0], next[1], generation, storeResolution()),
				(long) next[0], (long) Math.ceil(next[1]), 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT, true);
	}

	/**
	 * The resolution (samples per stored value) the waveform plots are holding data
	 * at. Used to spot the display being zoomed in between an order being placed and
	 * it completing, in which case what it loaded is already too coarse to count as
	 * held.
	 * @return the store resolution, or -1 if there are no plots.
	 */
	private double storeResolution() {
		for (int i = 0; i < rawChannelData.length; i++) {
			if (rawChannelData[i] != null) {
				return rawChannelData[i].getBinsPerPixel();
			}
		}
		return -1;
	}

	/**
	 * Load observer for one ordered span. On a genuine completion the span is
	 * recorded as held by every channel and the next span is ordered.
	 */
	private class RawLoadObserver implements PamguardMVC.LoadObserver {

		private final double start;
		private final double end;
		private final int generation;
		private final double resolution;

		RawLoadObserver(double start, double end, int generation, double resolution) {
			this.start = start;
			this.end = end;
			this.generation = generation;
			this.resolution = resolution;
		}

		@Override
		public void setLoadStatus(int loadState) {
			isOrderring = false;

			/*
			 * The status is a bitmask which can be OR'd, so it must be tested with '&'. Only
			 * a completed pass may be recorded as held: an interrupted or partial one stopped
			 * short, and marking it would leave a permanent gap in the waveform. A pass which
			 * found no data at all IS complete - the range really does hold nothing (a gap
			 * between files, say) and asking again would just reload it forever.
			 */
			if ((loadState & OfflineDataLoading.REQUEST_INTERRUPTED) != 0
					|| (loadState & OfflineDataLoading.REQUEST_DATA_PARTIAL_LOAD) != 0) {
				return;
			}
			if ((loadState & OfflineDataLoading.REQUEST_DATA_LOADED) == 0
					&& (loadState & OfflineDataLoading.REQUEST_NO_DATA) == 0) {
				return;
			}

			/*
			 * Data units are handed to the plots on the FX thread (see RawDataObserver), so
			 * the last few of this order may not have been added yet. Record the span and
			 * order the next one from the FX thread too, behind them.
			 */
			Platform.runLater(()->{
				/*
				 * Unless the display was zoomed in while this order was in flight, in which case
				 * what it loaded is already too coarse for the display and the store has thrown
				 * its load state away - marking it now would leave the coarse data on screen for
				 * good. The zoom triggers a load of its own, so nothing is lost by not marking.
				 */
				if (storeResolution() == resolution) {
					for (int i = 0; i < rawChannelData.length; i++) {
						if (rawChannelData[i] != null) {
							rawChannelData[i].markRangeLoaded(start, end);
						}
					}
				}
				getTDGraph().repaint(0);
				orderNextInterval(generation);
			});
		}
	}

}
