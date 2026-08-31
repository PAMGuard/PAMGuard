package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;

/**
 * Handles the graphics for an acopustic scroller.  
 * @author Jamie Macaulay
 *
 */
public interface AcousticScrollerGraphics {
	
	/**
	 * 	Flag for a sample rate chnage. 
	 */
	public static final int SAMPLE_RATE_CHANGE = 0;

	/**
	 * The data block represented in the graphics.
	 * @return the data block. 
	 */
	public PamDataBlock getDataBlock(); 
	
	/**
	 * Add new data units. These will be used to create graphics
	 * @param rawData . A dataunit. Can be any type of data unit.
	 */
	public void addNewData(PamDataUnit rawData); 
	
	/**
	 * Repaint the canvas with the graphics.
	 */
	public void repaint();
	
	/**
	 * Name of the graphics.
	 * @return the graphics
	 */
	public String getName();

	/**
	 *Clear all data in the store. 
	 */
	public void clearStore();
	
	/**
	 * True if the data block requires offline loading of data.
	 */
	public boolean orderOfflineData();


	
	/**
	 * Set colours for the datagram. 
	 * @param specColors
	 */
	public void setColors(Plot2DColours specColors);
	
	/**
	 * Set colours for the datagram. 
	 * @param specColors
	 */
	public Plot2DColours getColors();

	/**
	 * Notifies of updates form the acoustic scroller.
	 * @param flag - the update flag.
	 */
	public void notifyUpdate(int flag);

	/**
	 * True if the graphics need to (re)load their data. Implementations that build an
	 * expensive preview of a fixed data range (e.g. the FFT spectrogram preview) can
	 * return false when the loaded range has not moved, so that the scroller does not
	 * needlessly rebuild them every time the user nudges the scroll bar. The default
	 * returns true (always reload), preserving the behaviour of cheap, in-memory
	 * graphics such as the detection datagram.
	 * @return true if the graphics should be reloaded.
	 */
	public default boolean needsReload() {
		return true;
	}

	/**
	 * Called when an offline load has finished so the implementation can decide whether
	 * the range is now fully built. Paired with {@link #needsReload()}.
	 * @param naturalCompletion true if the load ran to its natural end (every available
	 *        unit in the range was delivered); false if it was given up while still
	 *        incomplete (e.g. repeatedly interrupted). An implementation should only mark
	 *        itself loaded on a natural completion that actually delivered data. The
	 *        default does nothing.
	 */
	public default void loadCompleted(boolean naturalCompletion) {
	}

	/**
	 * Called immediately before an offline data order is placed, so an implementation
	 * that builds an incremental preview can decide where to start ordering from and
	 * whether to clear its existing store. This allows a load that was interrupted (e.g.
	 * because the user moved the scroll bar) to <b>resume</b> from where it stopped
	 * instead of rebuilding the whole - potentially very large - range from scratch.
	 * <p>
	 * The default clears the store and orders the whole range (the original behaviour).
	 *
	 * @param rangeStart the start of the full range that should be displayed, in millis.
	 * @param rangeEnd   the end of the full range that should be displayed, in millis.
	 * @return the time (millis) from which the offline order should actually start.
	 */
	public default long prepareOfflineLoad(long rangeStart, long rangeEnd) {
		clearStore();
		return rangeStart;
	}

	/**
	 * The parts of the given range which this graphics does not already hold and so
	 * still need to be ordered. Implementations which store their preview against
	 * absolute time (e.g. the tiled FFT spectrogram preview) can return only the gaps,
	 * so that a range which has moved slightly - or been extended by the load margin -
	 * re-uses everything already built and orders only the genuinely new data. This is
	 * what stops the whole preview being rebuilt (and blanked) whenever the loaded data
	 * range moves.
	 * <p>
	 * The default returns <code>null</code>, meaning "I do not track what I hold": the
	 * scroller then falls back to {@link #prepareOfflineLoad(long, long)} and orders
	 * the whole range from the point that returns.
	 *
	 * @param rangeStart the start of the range to be loaded, in millis.
	 * @param rangeEnd   the end of the range to be loaded, in millis.
	 * @return the intervals ({start, end} pairs) still needing to be ordered, an empty
	 *         list if the range is already covered, or null for no incremental loading.
	 */
	public default java.util.List<long[]> getRequiredLoadIntervals(long rangeStart, long rangeEnd) {
		return null;
	}

	/**
	 * Called when an interval has been ordered and loaded to its natural end, so that
	 * an implementation returning intervals from
	 * {@link #getRequiredLoadIntervals(long, long)} can record it as held and not order
	 * it again. Note that this is called even when the interval contained no data at
	 * all, otherwise empty stretches would be re-ordered on every pass. The default
	 * does nothing.
	 *
	 * @param startMillis the start of the loaded interval, in millis.
	 * @param endMillis   the end of the loaded interval, in millis.
	 */
	public default void markRangeLoaded(long startMillis, long endMillis) {
	}

	/**
	 * True if this graphics' data source is currently being loaded by another consumer
	 * (e.g. the main spectrogram display loading the same FFT block). When true the
	 * scroller idle-gates - it waits rather than placing a competing offline order that
	 * would stall behind the other load. The default returns false, so graphics that do
	 * not share a loaded block (and the in-memory detection datagram, which never orders
	 * offline data at all) are unaffected.
	 *
	 * @return true if the scroller should wait before placing an offline order.
	 */
	public default boolean isOfflineLoadBlocked() {
		return false;
	}

	/**
	 * For graphics that order offline data, the maximum span of a single offline order
	 * in milliseconds. Each offline order clears the source data blocks first (see
	 * {@code OfflineDataLoading.clearAllFFTBlocks()}), so ordering a long range in one
	 * go holds <b>every</b> data unit in that range in memory at once - gigabytes of FFT
	 * units for a long, high-sample-rate file. Returning a smaller span makes the
	 * scroller load the range as a sequence of chunks, keeping the resident data-unit
	 * memory to roughly one chunk's worth while this graphics retains its own compact
	 * preview across chunks. The default of 0 loads the whole range in a single order.
	 *
	 * @return the maximum span of a single offline order in millis, or 0 for no chunking.
	 */
	public default long getLoadChunkMillis() {
		return 0;
	}

}
