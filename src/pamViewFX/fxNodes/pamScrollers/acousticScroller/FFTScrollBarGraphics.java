package pamViewFX.fxNodes.pamScrollers.acousticScroller;
import java.util.List;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX2;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class FFTScrollBarGraphics implements AcousticScrollerGraphics {
	
	/**
	 * The spectrogram plot. 
	 */
	public SpecDatagramPlot spectrogramPlot;
	
	/**
	 * Reference to the FFT data block. 
	 */
	private FFTDataBlock fftDataBlock; 
	
	/**
	 * The spectrogram channel to plot. 
	 */
	private int channel=0;

	/**
	 * The acoustic scroller. 
	 */
	private AcousticScrollerFX acousticScroller;

	private Canvas canvas;
	
//	public GeneralSpectrogramColours spectrogramColours;

//	private PamAxisFX freqAxis; 
	
	/**
	 * 
	 */
	AcousticScrollerProjector projector;

	/**
	 * Colours for this datagram 
	 */
	private StandardPlot2DColours datagramColours;

	private PamAxisFX freqAxis; 
	
	
		
	public FFTScrollBarGraphics(AcousticScrollerFX acousticScroller, FFTDataBlock fftDataBlock){
		this.acousticScroller=acousticScroller; 
		this.fftDataBlock=fftDataBlock; 
//		spectrogramColours=new GeneralSpectrogramColours();
				
		projector=new AcousticScrollerProjector(); 
		
		this.datagramColours= new StandardPlot2DColours(); 
		
		spectrogramPlot=new SpecDatagramPlot(projector, fftDataBlock, datagramColours, 0, this.acousticScroller.isViewer); 
//		channel=PamUtils.getLowestChannel(fftDataBlock.getChannelMap());
		channel=PamUtils.getLowestChannel(fftDataBlock.getSequenceMap());

		//create axis and bind frequencies. 
		createFreqAxis();
				 
	}
	
	public class AcousticScrollerProjector extends TDProjectorFX {

		public AcousticScrollerProjector() {
			super();
			Rectangle windowRect=new Rectangle(); 
			windowRect.widthProperty().bind(acousticScroller.getScrollBarPane().getDrawCanvas().widthProperty());
			windowRect.heightProperty().bind(acousticScroller.getScrollBarPane().getDrawCanvas().heightProperty());
			this.setWindowRect(windowRect); 
		}	

		@Override
		public PamAxisFX getYAxis(){
			return freqAxis; 
		}

		@Override
		public PamAxisFX getTimeAxis(){
			return acousticScroller.getTimeAxis(); 
		}
		
		@Override
		public double getVisibleTime(){
			return acousticScroller.getRangeMillis();
		}
		
		public double getGraphTimePixels(){
			return acousticScroller.getScrollBarPane().getWidth();
		}

	}
	
	private void createFreqAxis(){
		freqAxis = new PamAxisFX(0, 1, 0, 1, 0, 10, PamAxisFX.ABOVE_LEFT, "Graph Units", PamAxisFX.LABEL_NEAR_CENTRE, "%4d");
		freqAxis.y1Property().setValue(0);
		freqAxis.y2Property().bind(acousticScroller.getScrollBarPane().heightProperty().divide(2));
		freqAxis.x1Property().bind(acousticScroller.getScrollBarPane().widthProperty());
		freqAxis.x2Property().bind(acousticScroller.getScrollBarPane().widthProperty());
	}
	
	/**
	 * Update the frequency axis based on fft datablock sample rate. 
	 */
	private void updateFreqLimits(){
		freqAxis.minValProperty().setValue(0);
		freqAxis.maxValProperty().setValue(fftDataBlock.getSampleRate()/2);
//		DoubleProperty[] axisVals={freqAxis.minValProperty() , freqAxis.maxValProperty()};
//		spectrogramPlot.setFreqLimits(axisVals);
	}


	@Override
	public PamDataBlock getDataBlock() {
		return fftDataBlock;
	}

	PamDataUnit lastData;

	private Rectangle windowRect;

	/**
	 * Sample rate the frequency axis was last set up for, so that the axis is
	 * (re)built when the data actually change rather than on every unit.
	 */
	private volatile float freqAxisSampleRate = -1;

	@Override
	public void addNewData(PamDataUnit rawData) {
		try{
			if (rawData.getParentDataBlock()==fftDataBlock
//					&& PamUtils.hasChannel(rawData.getChannelBitmap(), channel)
					&& PamUtils.hasChannel(rawData.getSequenceBitmap(), channel)
					&& lastData!=rawData){
				if (freqAxisSampleRate != fftDataBlock.getSampleRate()) {
					updateFreqLimits();
					freqAxisSampleRate = fftDataBlock.getSampleRate();
				}
				spectrogramPlot.new2DData((FFTDataUnit) rawData);
				lastData=rawData;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void repaint() {
		
		//get the canvas. 
		canvas=acousticScroller.getScrollBarPane().getDrawCanvas();

		//calculate the size of the scrollbar
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(),canvas.getHeight());
		
		windowRect=new Rectangle(0,0, 	canvas.getWidth(), 		canvas.getHeight());

//		System.out.println("Projector: top " + projector.getYPix(24000) +" bottom "+ projector.getYPix(0)+ "  height: " + projector.getHeight() 
//			+ " lims "+	freqAxis.minValProperty().getValue() + "  " + freqAxis.maxValProperty().getValue()); 
		
		//plot the spectrogram.
		spectrogramPlot.drawSpectrogram(canvas.getGraphicsContext2D(), windowRect, acousticScroller.getOrientation(),
				acousticScroller.getTimeAxis(), acousticScroller.getMinimumMillis(), false);
	}

	@Override
	public String getName() {
		return "Spectrogram";
	}

	@Override
	public void clearStore() {
		/*
		 * Nothing to do. The preview is held as tiles keyed on absolute time and the tiled
		 * store retires or discards them itself when the geometry changes (see
		 * Scrolling2DPlotDataFX2.checkConfig), so there is never a reason to blank it from
		 * here - that would throw away exactly the data a moved range wants to re-use.
		 */
	}

	@Override
	public List<long[]> getRequiredLoadIntervals(long rangeStart, long rangeEnd) {
		//Bring the tiling up to date with the current range and resolution first, or the
		//load state below is read from a stale (or not yet configured) tiling.
		spectrogramPlot.checkConfig();
		return spectrogramPlot.getRequiredLoadIntervals(rangeStart, rangeEnd);
	}

	@Override
	public void markRangeLoaded(long startMillis, long endMillis) {
		/*
		 * Record the interval as held - including where it held no data at all - so that it
		 * is not ordered again. Load state is tracked per tile, so only tiles which lie
		 * WHOLLY inside the loaded interval may be marked: marking a tile the order only
		 * partly covered would leave a permanent gap in the preview. Orders are whole
		 * numbers of tiles (see getLoadChunkMillis) so normally that is the whole interval;
		 * the trimming only bites if the tiling changed while the order was in flight.
		 */
		long tile = spectrogramPlot.getTileMillis();
		if (tile <= 0) {
			return;
		}
		long from = Math.floorDiv(startMillis + tile - 1, tile) * tile;
		long to = Math.floorDiv(endMillis, tile) * tile;
		if (to > from) {
			spectrogramPlot.markRangeLoaded(from, to);
		}
	}

	@Override
	public boolean needsReload() {
		/*
		 * Load only if some part of the range to load is not already held. A scroll
		 * position change leaves the preview entirely correct, and a change of loaded data
		 * range usually leaves most of it correct too - only the genuinely new part needs
		 * ordering, and if the range moved by less than the load margin, nothing does.
		 */
		return !getRequiredLoadIntervals(acousticScroller.getLoadStartMillis(),
				acousticScroller.getLoadEndMillis()).isEmpty();
	}

//	/**
//	 * Maximum time to wait for the FFT block to become idle before giving up on this
//	 * pass (the scroller will retry on the next change), in milliseconds.
//	 */
//	private static final long MAX_IDLE_WAIT_MILLIS = 30_000;
//
//	@Override
//	public boolean orderOfflineData() {
//		/*
//		 * Idle-gating. The scroll-bar preview must only load FFT data when nothing else
//		 * (e.g. the spectrogram display) is loading from this FFT block, so that it
//		 * never competes with - or interrupts - the display. This method runs on the
//		 * scroller's background load thread (AcousticScrollerFX.LoadTask), so we can
//		 * simply wait here until the block is idle and then let the normal full-range
//		 * load proceed. If the display starts loading again the scroller cancels this
//		 * task (and its data order), and the preview load is retried on the next change.
//		 *
//		 * The preview is rendered at scroll-bar resolution, so the spectrogram renderer
//		 * time-compresses the loaded FFTs down to the preview width (a few hundred
//		 * columns) regardless of how many FFTs are loaded.
//		 */
//		long waited = 0;
//		while (waited < MAX_IDLE_WAIT_MILLIS) {
//			if (Thread.currentThread().isInterrupted()) {
//				return false;
//			}
//			boolean busy = fftDataBlock.getOfflineDataLoading() != null
//					&& fftDataBlock.getOfflineDataLoading().getOrderStatus();
//			if (!busy) {
//				break;
//			}
//			try {
//				Thread.sleep(200);
//			}
//			catch (InterruptedException e) {
//				return false;
//			}
//			waited += 200;
//		}
//		return true;
//	}

	@Override
	public void notifyUpdate(int flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColors(Plot2DColours specColors) {
		spectrogramPlot.setSpecColors(specColors);
		spectrogramPlot.reBuildImage();
		spectrogramPlot.rebuildFinished();
	}
	
	@Override
	public Plot2DColours getColors() {
		return spectrogramPlot.getSpecColors(); 
	}
	
	/**
	 * Spectrogram preview for the scroll bar. Uses the tiled renderer
	 * ({@link Scrolling2DPlotDataFX2}) so that the whole displayed range is retained
	 * as re-colourable tiles - this keeps the preview correct when amplitude limits
	 * change (the single-image {@code Scrolling2DPlotDataFX} only kept a small
	 * scrolling buffer, so its earliest data was lost on a re-colour).
	 */
	private class SpecDatagramPlot extends Scrolling2DPlotDataFX2 {

		public SpecDatagramPlot(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
			super(specPlotInfo, iChannel);
		}

		public SpecDatagramPlot(AcousticScrollerProjector projector, FFTDataBlock fftDataBlock,
				StandardPlot2DColours dataGramColors, int i, boolean isViewer) {
			super(projector, fftDataBlock, dataGramColors, i, isViewer);
		}

		/**
		 * The scroll bar shows the whole loaded data range, so a quarter of it is a huge
		 * span of data - far more than can be held as raw FFT units while it loads. Cap
		 * the tiles at one order's worth so that each order completes whole tiles (which
		 * is what lets the preview record what it holds and re-use it when the loaded
		 * range moves). Tiles only decide how the preview is chopped up, not the
		 * resolution it is drawn at, so more of them costs nothing visually.
		 */
		@Override
		protected double targetTileMillis(double visibleMillis) {
			return Math.min(super.targetTileMillis(visibleMillis), chunkBudgetMillis());
		}

		public void rebuildFinished(){
			acousticScroller.repaint(0);
		}

	}

	@Override
	public boolean orderOfflineData() {
		return true;
	}

	/**
	 * Approximate budget for the FFT units held resident while loading a single chunk,
	 * in bytes. The whole displayed range is built into a compact tiled preview, so the
	 * raw FFT units only ever need to live for the chunk currently being processed.
	 */
	private static final long CHUNK_MEMORY_BUDGET = 30L * 1024 * 1024;

	@Override
	public long getLoadChunkMillis() {
		/*
		 * A whole number of tiles (at least one) no larger than the memory budget, so that
		 * every order covers whole tiles and can be marked loaded when it completes, while
		 * the resident FFT-unit memory stays bounded. The tiles themselves are capped at
		 * the budget (see SpecDatagramPlot.targetTileMillis) so 'at least one tile' is not
		 * a way round the budget - beyond the point where a tile hits its minimum image
		 * width, which is a small enough span not to matter.
		 */
		long budget = chunkBudgetMillis();
		long tile = spectrogramPlot.getTileMillis();
		if (tile <= 0) {
			return budget;
		}
		long nTiles = Math.max(1, budget / tile);
		return nTiles * tile;
	}

	/**
	 * The span of data which can be held in memory as raw FFT units while one order is
	 * loaded, in millis. Estimated from the FFT data rate of the block: a unit holds
	 * roughly fftLength complex doubles, and there are sampleRate/hop units per second.
	 * @return the memory budget expressed as a span of time in millis.
	 */
	private long chunkBudgetMillis() {
		float sr = fftDataBlock.getSampleRate();
		int hop = fftDataBlock.getHopSamples();
		int fftLen = fftDataBlock.getFftLength();
		if (sr <= 0 || hop <= 0 || fftLen <= 0) {
			return 30000; //sensible fallback before the block is configured.
		}
		double unitsPerSec = sr / hop;
		long bytesPerUnit = (long) fftLen * 8L; //complex spectrum stored as doubles.
		double bytesPerSec = Math.max(unitsPerSec * bytesPerUnit, 1);
		long millis = (long) (CHUNK_MEMORY_BUDGET / bytesPerSec * 1000.0);
		//clamp so chunks stay neither tiny (excessive order overhead) nor huge (memory).
		return Math.max(2000L, Math.min(millis, 120000L));
	}

	@Override
	public boolean isOfflineLoadBlocked() {
		//The preview shares the FFT block with the main spectrogram display. If the display
		//(or anything else) is already loading this block, wait for it rather than placing a
		//competing order that would just stall behind it.
		return fftDataBlock.getOfflineDataLoading() != null
				&& fftDataBlock.getOfflineDataLoading().getOrderStatus();
	}

//	/**
//	 * Loaded data range the preview has actually finished building for (set on load
//	 * completion, not when a load starts), so that an interrupted load is resumed.
//	 */
//	private volatile long loadedMinMillis = Long.MIN_VALUE;
//	private volatile long loadedMaxMillis = Long.MIN_VALUE;

//	@Override
//	public boolean needsReload() {
//		/*
//		 * The preview is a fixed image of the whole loaded data range, so it only needs
//		 * rebuilding when that range moves to a new section of the dataset - NOT every
//		 * time the scroller asks to load (e.g. when the user pauses/resumes dragging the
//		 * scroll bar within the same loaded range, via AcousticScrollerFX.pauseDataload).
//		 * Rebuilding the whole preview is expensive, so skip it once the current range
//		 * has been fully loaded.
//		 */
//		return acousticScroller.getMinimumMillis() != loadedMinMillis
//				|| acousticScroller.getMaximumMillis() != loadedMaxMillis;
//	}
//
//	@Override
//	public void loadCompleted() {
//		// Record the range that has now finished loading. Because a cancelled load goes
//		// through cancelled()/failed() (not succeeded()), this is only reached on a
//		// genuinely complete load - so an interrupted load leaves the range unmarked and
//		// is reloaded (resumed) next time.
//		loadedMinMillis = acousticScroller.getMinimumMillis();
//		loadedMaxMillis = acousticScroller.getMaximumMillis();
//	}

}
