package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import Layout.PamAxis;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataOffline.OfflineDataLoading;
import dataPlotsFX.rawDataPlotFX.RawSoundDataInfo;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.PamScrollObserver;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.pamScrollers.AbstractPamScrollerFX;
import pamViewFX.fxNodes.pamScrollers.VisibleRangeObserver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


/**
 * The acoustic scroll pane shows a summary of all the acoustic data available in a particular scroll period. 
 * @author Jamie Macaulay
 *
 */
public class AcousticScrollerFX extends AbstractPamScrollerFX {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The pane which shows the acoustic data and allows users to drag the scroll bar. 
	 */
	private ScrollBarPane scrollBarPane;

	/**
	 * Range listener 
	 */
	private PamBorderPane mainPane; 

	/**
	 * List of range observers
	 */
	public ArrayList<VisibleRangeObserver> rangeObserver = new ArrayList<VisibleRangeObserver>();

	/*
	 * The navigation pane. 
	 */
	private Pane navigationPane; 

	/**
	 * True if in viewer mode. 
	 */
	public boolean isViewer=false; 
	
	/**
	 * True if in network receiver mode. 
	 */
	public boolean isNetRx=false; 

	/**
	 * The acoustic plot.
	 */
	private RawSoundDataInfo rawSoundDataInfo;

	/**
	 * The default height of the scroll bar.
	 */
	private static final double scrollBarHeight=40.;

	/**
	 * The fraction of the displayed data range which is also loaded either side of it.
	 * <p>
	 * The scroller draws a preview of the currently loaded data range, so on the face
	 * of it there is no point loading anything outside it. But when that range moves -
	 * the user pages forwards or backwards, or jumps to a detection - a preview which
	 * holds exactly the old range has nothing for the new one and has to be rebuilt
	 * from disk, which the user sees as the scroll bar blanking and slowly refilling.
	 * Loading a margin either side (and only ordering what is actually missing, see
	 * {@link AcousticScrollerGraphics#getRequiredLoadIntervals(long, long)}) means a
	 * move smaller than the margin needs no loading at all, and a larger one only loads
	 * the part that is genuinely new. Same idea as the pre-load either side of the
	 * visible range in the tiled spectrogram (see {@code Scrolling2DPlotDataFX2}).
	 */
	private static final double LOAD_MARGIN_FRACTION = 0.25;


	/**
	 * The time axis is used for pixel calculations for sound data but is not actually displayed. 
	 */
	private PamAxisFX timeAxis;

	/**
	 * The orientation of the scroll bar. 
	 */
	Orientation orientation= Orientation.HORIZONTAL;

	/**
	 * A list of graphics which can be displayed in the acoustic scoll bar. 
	 */
	ArrayList<AcousticScrollerGraphics> acousticScrollerGraphics = new ArrayList<AcousticScrollerGraphics>(); 

	/**
	 * Colours for the colour bar. 
	 */
	private StandardPlot2DColours dataGramColors= new StandardPlot2DColours(); 

	/**
	 * The current graphics selected for the scroll bar. 
	 */
	int currentGraphicsIndex=0;

	/**
	 * A list of the current running threads in the que. 
	 */
	private ArrayList<LoadTask>  loadTasks;

	/**
	 * Executes threads in sequence. 
	 */
	private ExecutorService executorService;

	/**
	 * The left arrow (or top if vertical).
	 */
	private PamButton arrowBottomLeft;

	/**
	 * The right arrow (or top if vertical).
	 */
	private PamButton arrowTopRight;

	/**
	 * The amount to move the scroller in % of entire visible range. + is right and - is left. 
	 */
	private double scrollArrowIncrement=0.05;  



	/**
	 * Constructor for the acoustic scroller. 
	 * @param name - the name of the scroller. 
	 * @param orientation - the orientation of the scroller. 
	 * @param stepSizeMillis - the step size 
	 * @param defaultLoadTime - default load time
	 * @param hasMenu - has a menu. 
	 */
	public AcousticScrollerFX(String name, Orientation orientation,
			int stepSizeMillis, long defaultLoadTime, boolean hasMenu) {
		super(name, orientation, stepSizeMillis, defaultLoadTime, hasMenu);

		isViewer=PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		isNetRx = PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER;

		//System.out.println("Acoustic ScrollerFX :  Data length:  " + this.getScrollerData().getLength());

		scrollBarPane=new ScrollBarPane();

		//09/04/2017 - added this here to ensure correct range is set in scroll bar. 
		rangesChanged(super.getRangeMillis());

		timeAxis = new PamAxisFX(0, 1, 0, 1, 0, 0, PamAxis.ABOVE_LEFT, null, PamAxis.LABEL_NEAR_CENTRE, null);
		//timeAxis.minValProperty().bind(scrollBarPane.minValueProperty.divide(1000.));
		timeAxis.minValProperty().setValue(0);
		//timeAxis.maxValProperty().setValue(30);
		timeAxis.maxValProperty().bind(scrollBarPane.getMaxValueProperty().subtract(scrollBarPane.getMinValueProperty()).divide(1000.));
		timeAxis.x1Property().setValue(0);
		timeAxis.x2Property().bind(scrollBarPane.widthProperty());
		timeAxis.y1Property().bind(scrollBarPane.heightProperty());
		timeAxis.y2Property().bind(scrollBarPane.heightProperty());


		scrollBarPane.visibleAmountProperty().addListener((obsVal, oldVal, newVal)->{
			rangeChanged( -1 , getVisibleMillis());
		});

		//add listener to current value amount property.
		scrollBarPane.currentValueProperty().addListener((obsVal, oldVal, newVal)->{
			scrollMoved(); //inform that scroll has been moved. 
		});

		scrollBarPane.isChangingProperty().addListener((obsVal, oldVal, newVal)->{
			if (oldVal.booleanValue()!=newVal.booleanValue()) {
				pauseDataload(newVal);
			}
		});
		
		scrollBarPane.getScrollBox().setPrefWidth(100);
		scrollBarPane.showVisibleRangeButton(false); 


		if (isViewer){
			//add observer to load data in viewer mode. 
			loadTasks=new ArrayList<LoadTask>(); 
			super.addObserver(new ViewerLoadObserver());
		}

		//buttons for moving the scroll bar. 
		arrowBottomLeft=new PamButton(); 
		arrowBottomLeft.getStyleClass().add("icon-button");
		arrowBottomLeft.getStyleClass().add("opaque-button-square"); 
		arrowBottomLeft.setOnAction((action)->{
			scrollBarPane.moveScrollRectangle(-this.scrollArrowIncrement);
		});

		arrowTopRight=new PamButton(); 
		arrowTopRight.getStyleClass().add("icon-button");
		arrowTopRight.getStyleClass().add("opaque-button-square"); 
		arrowTopRight.setOnAction((action)->{
			scrollBarPane.moveScrollRectangle(this.scrollArrowIncrement);
		});

		mainPane=new PamBorderPane();	

		layoutScrollBarPane(orientation);

	}



	private class ViewerLoadObserver implements PamScrollObserver {

		@Override
		public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
			//nothing to do here. 
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			//here the range has chnaged i.e. new data has been loaded. Need to redraw the datagrams.
			//			System.out.println("AcousticScrollerFX: Begin loading data");
			cancelDataLoadTasks();
			if (isViewer) loadScrollerData();
		}
	}


	/**
	 * Cancel all data loading task that might be in a que. Don;t want these loading when we are
	 * in a new data range! 
	 */
	protected void cancelDataLoadTasks() {
		for (int i =0; i<acousticScrollerGraphics.size(); i++){
			try {
				acousticScrollerGraphics.get(i).getDataBlock().cancelDataOrder(true);
			} catch (NullPointerException e) {
				System.out.println("AcousticScrolllerFX.cancelDataLoadTasks: " + e.getMessage());
			}
		}
	}

	/**
	 * Begins redrawing data hello in the scroll bar. 
	 */
	public void loadScrollerData(){
		loadScrollerData(true); 
	}


	/**
	 * Begins redrawing data hello in the scroll bar. 
	 * @param - newTasks - true to create new tasks. False to use old tasks (that might have kept a record of previous loading)
	 */
	public synchronized void loadScrollerData(boolean newTasks){
		if (loadTasks==null) return;
		/***
		 * Why do we use threads here?
		 * Basically we do this because in most data blocks we process a lot of  data, which has already been loaded, from the data block
		 * This may only take a few seconds but the threading stops the display stuttering for a few seconds. 
		 * Nothing worse than a stutterring display for the suer experience.  
		 */

		//stop all tasks
		for (int i=0; i<loadTasks.size(); i++){
			loadTasks.get(i).cancel(false);
		}

		ArrayList<LoadTask>  oldloadTasks = new ArrayList<LoadTask> (loadTasks); 
		
		loadTasks.clear();

		//23/01/2017 need to use an executor service here or we end up with issues  
		if (executorService!=null)	executorService.shutdown();

		//create an executor service and 
		executorService = Executors.newFixedThreadPool(1);
		for (int i=0; i<acousticScrollerGraphics.size(); i++){

			if (newTasks) {
				loadTasks.add(new LoadTask(acousticScrollerGraphics.get(i)));
			}
			else {
				loadTasks.add(new LoadTask(acousticScrollerGraphics.get(i), oldloadTasks.get(i).getCurrentCount()));
			}

			//this executes the threads SEQUENTIALLY
			executorService.submit(loadTasks.get(i));
		}

	}

	/**
	 * Task for loading datagram data. 
	 * @author Jamie Macaulay
	 *
	 */
	protected class LoadTask extends  Task<Double> {

		private AcousticScrollerGraphics acousticScrollerGraphics;

		/**
		 * Indicates that offline data is still beinhg loaded.
		 */
		private volatile boolean dataLoading = false;

		/**
		 * Sticky interrupted flag for the current pass. The offline loader can fire several
		 * callbacks for one order (e.g. an INTERRUPTED followed by a spurious NO_DATA when a
		 * cancelled order is re-queued under PRIORITY_CANCEL_RESTART). If ANY callback during
		 * the pass was REQUEST_INTERRUPTED the pass was cut short and must be resumed - so we
		 * latch it here rather than trusting only the last status.
		 */
		private volatile boolean passInterrupted = false;

		/**
		 * True once an offline load has run to its natural end (all available data
		 * delivered). Distinct from being cancelled or given up while still incomplete -
		 * only a natural completion should let the graphics mark its range fully loaded.
		 */
		private volatile boolean naturalCompletion = false;

		/**
		 * Wall-clock time (millis) of the last sign of life from the current order - either
		 * when it was placed or when it last delivered a data unit. Used to detect a stalled
		 * order (e.g. queued behind the display's load on the same block and never serviced)
		 * so the wait loop can give up instead of spinning forever.
		 */
		private volatile long lastLoadActivity = 0;

		/**
		 * If an order neither delivers data nor finishes within this many millis it is
		 * considered stalled and abandoned (the preview retries on the next change).
		 */
		private static final long STALL_TIMEOUT_MILLIS = 15000;

		private int currentCount =0;

		public LoadTask(AcousticScrollerGraphics acousticScrollerGraphics) {
			this.acousticScrollerGraphics=acousticScrollerGraphics; 
		}
		

		public LoadTask(AcousticScrollerGraphics acousticScrollerGraphics, int currentCount) {
			this.acousticScrollerGraphics=acousticScrollerGraphics; 
			this.currentCount=currentCount; 
		}


		@Override 
		protected Double call() throws Exception {

			try {
			//allow for multiple calls here. Thread goes to sleep and if interrupted just cancels.
			//Thread.sleep(100);

			if (AcousticScrollerFX.this.getRangeMillis()==0) {
				return 0.;
			}

			if (this.isCancelled()){
				return 0.;
			}

			//skip graphics that are already up to date (e.g. the spectrogram preview when
			//the loaded range has not moved) so they are not needlessly rebuilt as the
			//user drags the scroll bar.
			if (!acousticScrollerGraphics.needsReload()) {
				Platform.runLater(()->{
					repaint(0);
				});
				return 0.;
			}

			//System.out.println("AcousticScrollerFX: Starting to load: " + acousticScrollerGraphics.getName());
			if (acousticScrollerGraphics.orderOfflineData()){
				//load data from files - PAMGuard handles threading here.
				redrawOrderredDataGram(acousticScrollerGraphics);
			}
			else {
				redrawDataGram(acousticScrollerGraphics);
			}
			return 1.;
			}
			catch (Exception e) {
				e.printStackTrace();
				return -1.;
			}
		}

		@Override
		public boolean cancel(boolean cancel) {
			//make sure to stop the data load gracefully!
			acousticScrollerGraphics.getDataBlock().cancelDataOrder();
			return super.cancel(cancel);
		}

		@Override protected void succeeded() {
			super.succeeded();
			//let the graphics record that its load completed (so a fixed preview is not
			//rebuilt again until the loaded range actually changes). Pass the final load
			//state so the graphics can tell a natural completion from an interruption - an
			//interrupted or empty/NO_DATA pass must not be recorded as complete, or the
			//preview would either never build or get stuck half-loaded.
			acousticScrollerGraphics.loadCompleted(naturalCompletion);
			Platform.runLater(()->{
				repaint(0);
			});
			//			updateMessage("AcousticScrollerFX: Done loading: " +acousticScrollerGraphics.getName());
		}

		@Override protected void cancelled() {
			super.cancelled();
			Platform.runLater(()->{
				repaint(0);
			});
			//			updateMessage("Cancelled!");
		}

		@Override protected void failed() {
			super.failed();
			Platform.runLater(()->{
				repaint(0);
			});
			//			updateMessage("Failed!");
		}

		/**
		 * Create the datagram for a data block. 
		 * @param task
		 * @param acousticScrollerGraphics
		 */
		private synchronized void redrawDataGram(AcousticScrollerGraphics acousticScrollerGraphics){

			//System.out.println("Redraw datagram: " + currentCount); 
			if (acousticScrollerGraphics.getDataBlock().getUnitsCount()<=currentCount) return; 

			if (currentCount==0) acousticScrollerGraphics.clearStore(); 
		
			@SuppressWarnings("unchecked")
			ListIterator<PamDataUnit> it = acousticScrollerGraphics.getDataBlock().getListIterator(currentCount);	
			
			
			
			//			int count =0; 
			//removing the sync lock here was reallt helpful in preventing lock ups - especially because the datagram does not
			//load wghen the scroller moves. 
			//synchronized (acousticScrollerGraphics.getDataBlock().getSynchLock()) {
			while (it.hasNext()) {

				//					if (count%500==0){
				//						AcousticDataGramGraphics acousticDataGramGraphics=(AcousticDataGramGraphics) acousticScrollerGraphics; 
				//						System.out.println("Hello datagram load: " + count+ " "+ acousticScrollerGraphics.getDataBlock().getUnitsCount() +
				//								" "+acousticDataGramGraphics.getDataGramStore().currentIndex);
				//					}
				//					count++;
				currentCount++; 

				try {
					acousticScrollerGraphics.addNewData(it.next());
				}
				catch (Exception e) {
//					System.err.println("Error in AcousticScrollerFX: " + e.getMessage());
				}

				if (this.isCancelled()){
					return;
				}

				Platform.runLater(()->{
					repaint(30);
				});
			}
			//}
		}


		/**
		 * Create the datagram for a datablock which requires ordering of offline data.
		 * <p>
		 * Only the parts of the range the graphics does not already hold are ordered, and
		 * the range runs a margin either side of the displayed one (see
		 * {@link AcousticScrollerFX#getLoadStartMillis()}). So when the loaded data range
		 * moves, whatever the preview already built is kept and drawn - it is stored
		 * against absolute time - and only genuinely new data are loaded; a move smaller
		 * than the margin loads nothing at all.
		 * @param acousticScrollerGraphics - the graphics to load data for.
		 */
		private void redrawOrderredDataGram(AcousticScrollerGraphics acousticScrollerGraphics){

			long loadStart = getLoadStartMillis();
			long loadEnd = getLoadEndMillis();
			long chunkMillis = acousticScrollerGraphics.getLoadChunkMillis();

			/*
			 * Ask the graphics what it is still missing. Graphics which do not track what
			 * they hold return null, in which case fall back to the original behaviour:
			 * prepareOfflineLoad arms a deferred clear for a fresh range and returns where
			 * to (re)start from - the range start for a fresh range, or the furthest
			 * already-built point when continuing a previously interrupted load.
			 */
			List<long[]> required = acousticScrollerGraphics.getRequiredLoadIntervals(loadStart, loadEnd);
			if (required == null) {
				long cursor = acousticScrollerGraphics.prepareOfflineLoad(loadStart, loadEnd);
				required = new ArrayList<long[]>();
				required.add(new long[] {cursor, loadEnd});
			}

			/*
			 * Load as a sequence of chunks rather than one big order. Each offline order
			 * clears the source data blocks first (see OfflineDataLoading.clearAllFFTBlocks),
			 * so a single whole-range order would hold every FFT unit in the range in memory
			 * at once - gigabytes for a long, high-sample-rate file. Chunking keeps the
			 * resident FFT memory to roughly one chunk's worth (see
			 * AcousticScrollerGraphics.getLoadChunkMillis), while the graphics retains its
			 * own compact preview across chunks. A chunkMillis of 0 keeps the original
			 * single-order behaviour.
			 */
			ArrayList<long[]> chunks = chunkIntervals(required, chunkMillis);

			naturalCompletion = false;
			int noProgress = 0;
			int iChunk = 0;

			while (iChunk < chunks.size() && !isCancelled()) {

				long[] chunk = chunks.get(iChunk);

				//Idle-gate: if another consumer (e.g. the main spectrogram display) is loading
				//this block, wait for it to finish rather than placing a competing order that
				//would just stall behind it. Polling here IS the retry-when-free mechanism.
				while (acousticScrollerGraphics.isOfflineLoadBlocked() && !isCancelled()) {
					try { Thread.sleep(500); } catch (InterruptedException e) { }
				}
				if (isCancelled()) {
					break;
				}

				OfflineDataLoadInfo offlineDataInfo = new OfflineDataLoadInfo(new LoadOfflineDataObserver(acousticScrollerGraphics), new DataFinished(),
						chunk[0], chunk[1], 0, OfflineDataLoading.OFFLINE_DATA_WAIT, false);
				offlineDataInfo.setPriority(OfflineDataLoadInfo.PRIORITY_CANCEL_RESTART);

				//NOTE: set the flag BEFORE placing the order, otherwise the DataFinished observer
				//(which fires on the load thread) can set dataLoading=false before we set it true here.
				dataLoading=true;
				passInterrupted=false;
				lastLoadActivity=System.currentTimeMillis();

				//loads on it's own thread.
				acousticScrollerGraphics.getDataBlock().orderOfflineData(offlineDataInfo);

				//wait for this chunk to load.
				boolean stalled = false;
				while(dataLoading && !isCancelled()){
					try {
						Thread.sleep(500);
						if (System.currentTimeMillis() - lastLoadActivity > STALL_TIMEOUT_MILLIS) {
							//The order is neither delivering data nor finishing - almost always
							//because it is queued behind the display's load on the same block.
							//Abandon it rather than spin forever; the preview retries on the next change.
							stalled = true;
							break;
						}
					} catch (InterruptedException e) {
					}
				}

				if (isCancelled()) {
					break;
				}

				if (stalled) {
					//cancel the abandoned order so it does not linger on the block, then give up.
					acousticScrollerGraphics.getDataBlock().cancelDataOrder();
					break;
				}

				//Use the sticky flag: if ANY callback during this chunk was INTERRUPTED the
				//chunk was cut short, even if a later spurious NO_DATA callback followed.
				if (passInterrupted) {
					//Interrupted by the competing display load (not by the user). Retry the same
					//chunk - whatever it did manage to build is kept (the store is keyed on
					//absolute time), so a retry is not wasted work. Guard against spinning when
					//the order is repeatedly bumped before delivering anything; the load is
					//retried on the next scroll change anyway.
					if (++noProgress > 10) {
						break;
					}
					try { Thread.sleep(300); } catch (InterruptedException e) { }
					continue;
				}

				//Chunk loaded to its natural end (including NO_DATA): record it as held so it
				//is not ordered again, and move on to the next.
				acousticScrollerGraphics.markRangeLoaded(chunk[0], chunk[1]);
				iChunk++;
				noProgress = 0;
			}

			//Only a run that got through every chunk without being cancelled is a genuine
			//natural completion (so the graphics may mark its range fully loaded).
			if (!isCancelled() && iChunk >= chunks.size()) {
				naturalCompletion = true;
			}
		}

		/**
		 * Split the intervals which need loading into orders no longer than chunkMillis,
		 * putting the chunks which overlap the displayed data range first so that what
		 * the user can actually see is built before the off-screen margins.
		 * @param intervals - the intervals needing to be loaded.
		 * @param chunkMillis - the maximum span of one order, or 0 for no chunking.
		 * @return the chunks to order, in the order they should be ordered.
		 */
		private ArrayList<long[]> chunkIntervals(List<long[]> intervals, long chunkMillis) {

			long dispStart = getMinimumMillis();
			long dispEnd = getMaximumMillis();

			ArrayList<long[]> displayed = new ArrayList<long[]>();
			ArrayList<long[]> margins = new ArrayList<long[]>();

			for (long[] interval : intervals) {
				long len = interval[1]-interval[0];
				if (len <= 0) {
					continue;
				}
				long span = (chunkMillis <= 0) ? len : chunkMillis;
				for (long start = interval[0]; start < interval[1]; start += span) {
					long end = Math.min(start+span, interval[1]);
					if (end > dispStart && start < dispEnd) {
						displayed.add(new long[] {start, end});
					}
					else {
						margins.add(new long[] {start, end});
					}
				}
			}

			displayed.addAll(margins);
			return displayed;
		}


		private class DataFinished  implements LoadObserver {
			@Override
			public void setLoadStatus(int loadState) {
				if ((loadState & OfflineDataLoading.REQUEST_INTERRUPTED) != 0) {
					passInterrupted=true;
				}
				dataLoading=false;
			}
		}
		

		/**
		 * Observes incoming data and updates the scroll bar graphics. . 
		 * 
		 * Note: that this data is on the AWT thread and must be switched to the FX thread before any processing takes place.  
		 * @author Jamie Macaulay
		 *
		 */
		private class LoadOfflineDataObserver extends PamObserverAdapter {

			/**
			 * Acoustic scroller graphics for this loaded. 
			 */
			private AcousticScrollerGraphics acousticScrollerGraphics;

			LoadOfflineDataObserver(AcousticScrollerGraphics acousticScrollerGraphics){
				this.acousticScrollerGraphics=acousticScrollerGraphics;
			}

			@Override
			public void addData(PamObservable o, PamDataUnit dataUnit) {
				//mark progress so the wait loop's stall detector knows the order is alive.
				lastLoadActivity = System.currentTimeMillis();
				acousticScrollerGraphics.addNewData(dataUnit);
				Platform.runLater(()->{
					repaint(100);
				});
			}

			@Override
			public String getObserverName() {
				return "AcousticScrollerFX DataLoading " + acousticScrollerGraphics.getDataBlock().getDataName(); 
			}
		}
		
		public int getCurrentCount() {
			return currentCount;
		}


		public void setCurrentCount(int currentCount) {
			this.currentCount = currentCount;
		}




	};


	/**
	 * The start of the range the scroller graphics should load, in millis. This is the
	 * start of the currently loaded (displayed) data range less a margin, clamped to
	 * the start of the available data - see {@link #LOAD_MARGIN_FRACTION}.
	 * @return the start of the range to load in millis.
	 */
	public long getLoadStartMillis() {
		long start = getMinimumMillis() - (long) (getRangeMillis()*LOAD_MARGIN_FRACTION);
		//checkMinimumTime pulls the time up to the start of the available data. It returns
		//0 if there is no data map at all, in which case leave the unclamped value alone.
		long clamped = AbstractScrollManager.getScrollManager().checkMinimumTime(start);
		if (clamped > 0) {
			start = Math.min(clamped, getMinimumMillis());
		}
		return start;
	}

	/**
	 * The end of the range the scroller graphics should load, in millis. This is the end
	 * of the currently loaded (displayed) data range plus a margin, clamped to the end
	 * of the available data - see {@link #LOAD_MARGIN_FRACTION}.
	 * @return the end of the range to load in millis.
	 */
	public long getLoadEndMillis() {
		long end = getMaximumMillis() + (long) (getRangeMillis()*LOAD_MARGIN_FRACTION);
		//checkMaximumTime pulls the time back to the end of the available data. It returns
		//0 if there is no data map at all, in which case leave the unclamped value alone.
		long clamped = AbstractScrollManager.getScrollManager().checkMaximumTime(end);
		if (clamped > 0) {
			end = Math.max(clamped, getMaximumMillis());
		}
		return end;
	}

	/**
	 * Pause the datagram loading.
	 * <p>
	 * This is used to pause the data gram loading for example if a data block is
	 * being accessed somewhere else. If pauseDataload(true) is called afterwards
	 * then the datagrams will start loading from their previous position. 
	 * @param - true to pause
	 */
	public void pauseDataload(boolean pause) {
		if (pause) {
			//stop all tasks
			if (loadTasks!=null) {
				for (int i=0; i<loadTasks.size(); i++){
					loadTasks.get(i).cancel(false);
				}
			}

			if (executorService!=null) executorService.shutdownNow();
		}
		else {
			loadScrollerData(false);
		}
	}

	/**
	 * Add graphics to the scroll bar.  This handles drawing data units on the scroll bar's background canvas. 
	 * @param rawScrollBarGraphics - the scroll bar graphics class to add. 
	 */
	public void addAcousticScrollGraphics(AcousticScrollerGraphics rawScrollBarGraphics2) {
		this.acousticScrollerGraphics.add(rawScrollBarGraphics2); 
	}

	/**
	 * Remove graphics from the s roll bar. This handles drawing data units on the scroll bar's background canvas. 
	 * @param rawScrollBarGraphics - the scroll bar graphics class to remove.
	 */
	public void removeAcousticScrollGraphics(AcousticScrollerGraphics rawScrollBarGraphics2) {
		this.acousticScrollerGraphics.add(rawScrollBarGraphics2); 
	}

	/**
	 * Set current index of graphics.
	 * @param the current index of currentIndex 
	 */
	public void setCurrentGraphicsIndex(int currentIndex) {
		if (currentIndex>=acousticScrollerGraphics.size()) {
			System.err.println("AcousticScrollerFX: The set graphics index is greater than the array size");
			currentGraphicsIndex=0; 
		}
		else this.currentGraphicsIndex = currentIndex;
	}

	/**
	 * The current index of graphics.
	 * @return the current index of the graphics showing in the scroll bar. 
	 */
	public int getCurrentGraphicsIndex() {
		return this.currentGraphicsIndex;
	}

	/**
	 * Get all subscribed graphics. 
	 * @return subscribed graphics. 
	 */
	public ArrayList<AcousticScrollerGraphics> getAcousticScrollGraphics() {
		return acousticScrollerGraphics;
	}


	/**
	 * Add new sound data to the scroller. This paints a summary of sound data onto the scroller. 
	 * @param rawDataUnit
	 */
	RawDataUnit lastUnit = null; 
	int count=0; 

	/**
	 * Add a new data unit to the scroll bar so it can be drawn on the background canvas. 
	 * @param rawDataUnit - the raw data unit. 
	 */
	public void addNewPamData(PamDataUnit rawDataUnit){
		//rawSoundPlotDataFX.setSampleRate(rawDataUnit.getParentDataBlock().getSampleRate());
		for (int i=0; i<this.acousticScrollerGraphics.size(); i++){
			acousticScrollerGraphics.get(i).addNewData(rawDataUnit);
			if (i==this.currentGraphicsIndex) repaint(100);
		}

		//		rawSoundPlotDataFX.newRawData(rawDataUnit, getBinsPerPixel(rawDataUnit.getParentDataBlock().getSampleRate()));
		//		repaint();
		//count++; 
	}


	/**
	 * Set the index of the scroller graphics. These graphics are shown in the scroller canvas. 
	 * @param i - the index of the acousticScrollerGraphics array. 
	 */
	public void setScrollerGraphics(int i) {
		this.currentGraphicsIndex=i; 
		repaint(0); 
	}


	/**
	 * Repaint the scroll bar graphics. 
	 */
	private long lastTime=0; 
	/**
	 * Timer that repaints after time diff has been reached 
	 */
	private Timeline timeline;

	/**
	 * Repaint the canvas. 
	 * @param diff
	 */
	public synchronized void repaint(long diff){
		//		System.out.println("AcousticScrollerFX: Reapint acoustic scroller");
		long currentTime=System.currentTimeMillis();
		if (currentTime-lastTime<diff){

			//start a timer. If a repaitn hasn;t be called becuase diff is tto short this will ensure that 
			//the last reapint which is less than diff is called. This means a final repaint is always called 
			if (timeline!=null) timeline.stop();
			timeline = new Timeline(new KeyFrame(
					Duration.millis(diff),
					ae -> repaint(0)));
			timeline.play();
			//			System.out.println("didn't want to repaint righ tnow");
			return; 
		}

		lastTime=currentTime;
		if (currentGraphicsIndex>=0 && acousticScrollerGraphics.size()>0){
			this.scrollBarPane.getDrawCanvas().getGraphicsContext2D().clearRect(0, 0, 
					scrollBarPane.getDrawCanvas().getWidth(), scrollBarPane.getDrawCanvas().getHeight());
			this.acousticScrollerGraphics.get(this.currentGraphicsIndex).repaint(); 
			//			System.out.println("scroll graphico repaint!");
		}
		else {
			//			System.out.println("didn't like index to repaint!");
		}

	}

	/**
	 * Set the sample rate on the display. 
	 */
	public void setSampleRate(float samplerate){
		//clear all graphics stores as sample rate has chnaged. 
		for (int i=0; i<this.acousticScrollerGraphics.size(); i++){
			acousticScrollerGraphics.get(i).notifyUpdate(AcousticScrollerGraphics.SAMPLE_RATE_CHANGE);
		}
	}

	/**
	 * Get the number of bins per pixel. This is calculated from the width of the tdGraph in pixels, the time the display represents and
	 * the sample rate of raw sound data. 
	 * @return the number of bins per pixel. 
	 */
	public double getBinsPerPixel(float sampleratre){
		return ((timeAxis.getMaxVal()-timeAxis.getMinVal())*sampleratre)/scrollBarPane.widthProperty().get(); 
	}



	//	Canvas canvas;
	//	Rectangle windowRect;
	//	
	//	private void repaint(){
	//		
	//		//get the canvas. 
	//		canvas=scrollBarPane.getDrawCanvas();
	//		
	//		
	//		windowRect=new Rectangle(0,0, 	canvas.getWidth(), 		canvas.getHeight());
	//
	////		canvas.getGraphicsContext2D().fillRect(1000*(Math.random()), 50*(Math.random()), 10*(Math.random()-0.5), 10*(Math.random()));
	////		canvas.getGraphicsContext2D().setFill(Color.BLUEVIOLET);
	//		
	//		//clear the rectangle 
	//		scrollBarPane.getDrawCanvas().getGraphicsContext2D().clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());
	//		
	////		System.out.println("AcousticScrollerFX: x1: "+timeAxis.x1Property().get() +" x2: "+  timeAxis.x2Property().get() + 
	////				" min val: "+timeAxis.minValProperty().get()+ " max val: "+timeAxis.maxValProperty().get()+ "binsperpixel: "+rawSoundPlotDataFX.getBinsPerPixel()
	////			+ " samplerate: "+rawSoundPlotDataFX.getSampleRate());
	////		System.out.println("AcousticScroller millis: "+ getValueMillis()+ "   "+PamCalendar.formatTime(getValueMillis())+ " "+this.getVisibleAmount()/1000.);
	//		
	////		System.out.println("AcousticScroller: timeAxis: "+timeAxis.getPosition(0) +" "+ timeAxis.getPosition(30)); 
	//		
	//		rawSoundPlotDataFX.drawRawSoundData(scrollBarPane.getDrawCanvas().getGraphicsContext2D(),  windowRect, orientation ,  timeAxis,  amplitudeAxis,
	//				this.getMinimumMillis(), -1);
	//	}

	/**
	 * Layout the control pane containing scroll bar and buttons to navigate in time. 
	 * @param orientation- orientation of the time scroller. 
	 */

	public void layoutScrollBarPane(Orientation orientation){

		mainPane.getChildren().clear();

		if (orientation==Orientation.VERTICAL){
			scrollBarPane.setRotate(90);

			PamVBox holder=new PamVBox(); 
			holder.getChildren().addAll(arrowTopRight, scrollBarPane, arrowBottomLeft); 
//			arrowBottomLeft.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_DOWN, PamGuiManagerFX.iconSize));
//			arrowTopRight.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_UP, PamGuiManagerFX.iconSize));
			arrowBottomLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-down", PamGuiManagerFX.iconSize));
			arrowTopRight.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-up", PamGuiManagerFX.iconSize));

			VBox.setVgrow(scrollBarPane, Priority.ALWAYS);
			mainPane.setRight(holder);
			mainPane.setPrefWidth(scrollBarHeight);

		}
		else {
			PamHBox holder=new PamHBox(); 
			holder.getChildren().addAll(arrowBottomLeft, scrollBarPane, arrowTopRight); 
//			arrowBottomLeft.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_LEFT, PamGuiManagerFX.iconSize));
//			arrowTopRight.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.KEYBOARD_ARROW_RIGHT, PamGuiManagerFX.iconSize));
			arrowBottomLeft.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-left", PamGuiManagerFX.iconSize));
			arrowTopRight.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chevron-right", PamGuiManagerFX.iconSize));
			arrowBottomLeft.prefHeightProperty().bind(scrollBarPane.heightProperty());
			arrowTopRight.prefHeightProperty().bind(scrollBarPane.heightProperty());

			HBox.setHgrow(scrollBarPane, Priority.ALWAYS);
			mainPane.setCenter(holder);
			mainPane.setPrefHeight(scrollBarHeight);
		}

		if (isViewer){
			//create default control pane
			navigationPane=createNavigationPane(isViewer, orientation);
			mainPane.setRight(navigationPane);
		}

		
		//important that the height and width listeners are the canvas and not the parent pane - otherwise
		//the height is not exactly correct sometimes. 
		scrollBarPane.getDrawCanvas().heightProperty().addListener((old_Val, new_val, obs_val)->{
			repaint(0); 
		});

		scrollBarPane.getDrawCanvas().widthProperty().addListener((old_Val, new_val, obs_val)->{
			repaint(0); 
		});

		scrollBarPane.layoutRectangle();
	}

	public void setOrientation(Orientation orientation) {
		if (this.orientation!=orientation) this.layoutScrollBarPane(orientation);
		this.orientation=orientation; 
	}

	@Override 
	public Pane getNode(){
		return mainPane;
	}

	/**
	 * The main pane holding the scroller nodes. 
	 * @return the main pane holding scroller nodes 
	 */
	public PamBorderPane getMainPane(){
		return mainPane;
	}

	@Override
	public void setUnitIncrement(long unitIncrement) {
		//TODO
		//System.out.println("AcousticScrollBar: setUnitIncrement()");
	}

	/**
	 * Get ther visible 
	 * @return The visible amount of the display in milliseconds. 
	 */
	public long getVisibleMillis() {
		return (long) scrollBarPane.getVisibleAmount();
	}

	@Override
	public void rangesChanged(long setValue) {
		//System.out.println("AcousticScrollerFX: Ranges Changed: " + setValue);
		if (scrollBarPane!=null){
			scrollBarPane.setMinVal(0.);
			//scrollBarPane.setMaxVal(scrollerData.getMaximumMillis()-scrollerData.getMinimumMillis());
			scrollBarPane.setMaxVal(setValue);
		}
	}

	@Override
	public void setRangeMillis(long minimumMillis, long maximumMillis, boolean notify) {
		//17/0/2017  added this to get scroll bar values to update properly 
		//System.out.println("AcousticScrollerFX: Ranges Changed: " + (maximumMillis-minimumMillis) +  " " + notify);
		super.setRangeMillis(minimumMillis,  maximumMillis,  notify);
		rangesChanged(maximumMillis - minimumMillis);
	}


	@Override
	public void setBlockIncrement(long blockIncrement) {
		//TODO
		//System.out.println("AcousticScrollBar: setBlockIncrement()");
		//scrollBar.setBlockIncrement((int) (blockIncrement/scrollerData.stepSizeMillis));
	}


	public long getBlockIncrement() {
		// TODO Auto-generated method stub
		return 0;
	} 

	@Override
	public void setVisibleMillis(long visibleAmount) {
		//System.out.println("AcousticScrollBar: setVisibleAmount(): " + visibleAmount);
		scrollBarPane.setVisibleAmount(visibleAmount);
	}

	@Override
	public long getValueMillis() {
		if (scrollerData==null) return 0; 
		if (scrollBarPane==null) return scrollerData.getMinimumMillis();
		//		System.out.println("AcousticScrollBar: getValueMillis(): " +" min: "+scrollerData.getMinimumMillis()+ " max: "+ scrollerData.getMaximumMillis()+
		//				" millis "+(scrollerData.getMinimumMillis() + scrollBarPane.getCurrentValue()));
		return (long) (scrollerData.getMinimumMillis() + scrollBarPane.getCurrentValue());
	}

	/**
	 * Get the value in millis. 
	 * @return the value in millis as a double. 
	 */
	public double getValueMillisD() {
		if (scrollerData==null) return 0; 
		if (scrollBarPane==null) return scrollerData.getMinimumMillis();
		return (scrollerData.getMinimumMillis() + scrollBarPane.getCurrentValue());
	}

	@Override
	public void valueSetMillis(long valueMillis) {
		// make sure we're on FX thread, since this may have arrived from Swing. 
		if (Platform.isFxApplicationThread() == false) {
			// not fx thread, so call this function on fx thread
			long valval = valueMillis;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					valueSetMillis(valval);
				}
			});
		}
		else {
			// should only be able to get here if we're ON the fx application thread. 
			valueMillis = Math.max(scrollerData.getMinimumMillis(), Math.min(scrollerData.getMaximumMillis(), valueMillis));
			long val = valueMillis - scrollerData.getMinimumMillis();
			if (val >= scrollBarPane.getMinVal() && val <= scrollBarPane.getMaxVal()) {
				scrollBarPane.setCurrentValue(val);
				scrollBarPane.layoutRectangle();
				//					repaint(0);
			}
		}
	}

	@Override
	public void anotherScrollerMovedInner(long newValue) {
		// TODO Auto-generated method stub
	}

	/**
	 * Add a range observer. 
	 * @param timeRangeListener - a time range observer
	 */
	public void addRangeObserver(VisibleRangeObserver timeRangeListener) {
		rangeObserver.add(timeRangeListener);
	} 

	/**
	 * Remove a range observer. 
	 * @param timeRangeListener - a time range observer.
	 */
	public void removeRangeObserver(VisibleRangeObserver timeRangeListener) {
		rangeObserver.remove(timeRangeListener);
	} 

	/**
	 * Called whenever the scroll bar value changes. 
	 */
	private void scrollMoved() {
		//System.out.println("Scroll Bar moved: ");
		pauseDataload(true);
		AbstractScrollManager.getScrollManager().moveInnerScroller(this, getValueMillis());
		notifyValueChange();

		/*
		 * An interactive drag sets isChanging=true while moving and false on release;
		 * the isChangingProperty listener resumes the (paused) data load on that
		 * release, which is what restarts the spectrogram preview after a drag.
		 * Programmatic moves - the arrow buttons and clicking in the scroll trough -
		 * change the scroll value WITHOUT that isChanging true->false transition, so
		 * the load paused just above would never be resumed and the preview would not
		 * reprocess. Resume it here for those moves. (needsReload() still guards against
		 * a needless rebuild when the loaded range has not actually changed.)
		 */
		if (!isScrollerChanging()) {
			pauseDataload(false);
		}
	}


	/**
	 * Called whenever ranges change. 
	 * @param oldVal - the old value of visible range in millis.
	 * @param newVal - the new value of visible range in millis.
	 */
	private void rangeChanged(long oldVal, long newVal){
		for (int i=0; i<rangeObserver.size(); i++){
			rangeObserver.get(i).visibleRangeChanged(oldVal, newVal);
		}
	}

	
	/**
	 * 
	 * @return
	 */
	public ScrollBarPane getScrollBarPane() {
		return scrollBarPane;
	}

	public PamAxisFX getTimeAxis() {
		return this.timeAxis;
	}





	/**
	 * Set the acoustic scroller colours. Also sets colours for all available datagrams.
	 * @param dataGramColors - the colours to set; 
	 */
	public void setDataGramColors(StandardPlot2DColours dataGramColors) {
		for (int i=0; i<this.acousticScrollerGraphics.size(); i++){
			acousticScrollerGraphics.get(i).setColors(this.dataGramColors);
		}
	}

	/**
	 * @return the isChanging property. 
	 */
	public BooleanProperty scrollerChangingProperty() {
		return scrollBarPane.isChangingProperty();
	}

	/**
	 * The property value for the scroll bar moving. True if the scroll bar is moving. 
	 * @return the scroll bar dragging property.
	 */
	public BooleanProperty scrollMovingProperty(){
		return this.scrollBarPane.scrollMovingProperty();
	}

	/**
	 * @return true of the scroller is changing position or visible range
	 */
	public boolean isScrollerChanging() {
		return scrollBarPane.isChangingProperty().get();
	}

	/**
	 * Set the scroll bar rectangle visible or invisble. 
	 * @param b - true to set visible. 
	 */
	public void setScrollRectVisible(boolean b) {
		scrollBarPane.getScrollRectangle().setVisible(b);
	}

	/**
	 * Get the navigation pane. This is used in viewer mode to navigate through the dataset. 
	 * @return the navigation pane. 
	 */
	public Pane getNavigationPane() {
		return navigationPane;
	}


	@Override
	public long getVisibleAmount() {
		return getVisibleMillis();
	}

}
