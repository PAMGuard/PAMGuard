package dataPlotsFX.scrollingPlot2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import PamguardMVC.DataBlock2D;
import PamguardMVC.DataUnit2D;
import dataPlotsFX.projector.TimeProjectorFX;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Tiled re-implementation of {@link Scrolling2DPlotDataFX} for a single channel.
 * <p>
 * Instead of a single scrolling/wrapping {@link WritableImage}, the spectrogram
 * is stored as a set of fixed-time-span <b>tiles</b>, each a small
 * {@link WritableImage} keyed by absolute time in a {@link TreeMap}. At draw time
 * the visible tiles are stitched together. Moving the display only requires
 * ordering/drawing the tiles that are not already rendered, rather than rebuilding
 * the whole image. In viewer mode the owning {@link Scrolling2DPlotInfo} pre-loads
 * tiles up to half the visible range either side so that small scrolls show no
 * loading.
 * <p>
 * A change of visible time range does not necessarily throw the images away. Each
 * tile carries the geometry it was built with, so it can be drawn at any scale.
 * Moderate changes of range are absorbed without touching the tiles at all (see
 * {@link #TILE_SPAN_TOLERANCE}); a change big enough to alter the rendered
 * resolution retires the tiles to a draw-only store where they carry on being
 * displayed, scaled, until new tiles cover the same time (see
 * {@link #MAX_RETAIN_SCALE}). Only a very large change discards everything. This
 * matters most in real time, where nothing re-loads old data, so a discarded image
 * leaves the display blank until new data arrive.
 * <p>
 * This class is a drop-in replacement for {@link Scrolling2DPlotDataFX}: it extends
 * it (so it fits the existing {@code Scrolling2DPlotDataFX[]} arrays and
 * {@code makeScrolling2DPlotData} factory) and overrides the full public surface
 * used by callers. The single-image base class is left untouched so the change can
 * be reverted with a one-line swap.
 *
 * @author Jamie Macaulay
 */
public class Scrolling2DPlotDataFX2 extends Scrolling2DPlotDataFX {

	/** Tile has been created but contains no rendered data yet. */
	private static final int STATE_EMPTY = 0;
	/** Tile is being filled with data (an order is in progress or live data arriving). */
	private static final int STATE_LOADING = 1;
	/** Tile's whole time span has been ordered/loaded and need not be re-ordered. */
	private static final int STATE_LOADED = 2;

	/**
	 * Max total storage for tiles in bytes (image + power store). Mirrors the
	 * budget used by the single-image base class.
	 */
	private static final long MAXSTORAGESIZE = 50 * 1024 * 1024;

	/** Max total storage for retained (stale-geometry) tiles, in bytes. */
	private static final long MAXLEGACYSIZE = MAXSTORAGESIZE / 2;

	/**
	 * How far the ideal tile span may drift from the current tile span before the
	 * tiling is rebuilt. Tile span only affects how the data is chopped up, not the
	 * rendered resolution (that is set by the time compression), so as long as the
	 * tiles are within this factor of the ideal size for the visible range they are
	 * kept exactly as they are and simply drawn at the new scale.
	 */
	private static final double TILE_SPAN_TOLERANCE = 4.;

	/**
	 * Maximum change in rendered resolution (millis per image pixel) over which
	 * existing tile images are retained and re-drawn at the new scale. Beyond this
	 * the images are too coarse (or too over-sampled) to be worth keeping and are
	 * discarded, as they were before retention was added.
	 */
	private static final double MAX_RETAIN_SCALE = 4.;

	/** Max width of a single tile image in pixels (some graphics cards dislike huge images). */
	private static final int MAXIMAGESIZE = 3092;

	/** Decibel integer scale, matching {@link Scrolling2DPlotDataFX#decibelIntScale}. */
	private static final double DECIBEL_INT_SCALE = 25;

	/** Quick reference transform for horizontal spectrogram (default orientation). */
	private static final Affine horzAffine = new Affine();

	/** Channel this plot shows data from. */
	private int iChannel;

	/** The FFT/2D data block. */
	private DataBlock2D dataBlock2D;

	/** The projector providing the time axis / visible range. */
	private TimeProjectorFX tdProjector;

	/**
	 * Owning plot info (also the colour source - implements {@link Plot2DColours}).
	 * May be <code>null</code> when this renderer is driven directly from a projector
	 * and colour source (e.g. the acoustic scroll-bar spectrogram preview) rather than
	 * a {@link Scrolling2DPlotInfo} on a {@code TDGraphFX}.
	 */
	private Scrolling2DPlotInfo specPlotInfo;

	/** Colour source for the spectrogram. */
	private Plot2DColours specColors;

	/** True to flip the data vertically (used by some derived displays, false for spectrograms). */
	private boolean reverse = false;

	/** Lock guarding all access to the tile store. */
	private final Object tileLock = new Object();

	/** The tiles, keyed by tile index ({@code floor(absTimeMillis / tileMillis)}). */
	private final TreeMap<Long, SpecTile> tiles = new TreeMap<>();

	/**
	 * Tiles built with an older geometry (tile span and/or time compression), kept
	 * for drawing only. When the visible time range changes the tiling has to be
	 * rebuilt, but the images themselves are still perfectly good data - they are
	 * simply drawn at whatever scale the new time axis demands until fresh tiles
	 * cover the same time (which in real time means as new data arrive, and in
	 * viewer mode as the re-order completes). They take no part in load-state
	 * tracking. Oldest generation first, so newer images draw on top.
	 */
	private final List<SpecTile> legacyTiles = new ArrayList<>();

	/** Number of vertical bins (= fftLength/2 for a spectrogram). */
	private int dataWidth;

	/** FFT hop in samples. */
	private int hopSamples;

	/** Seconds per FFT slice. */
	private double timeScale;

	/** Number of FFT slices averaged into one image column (time compression from zoom). */
	private int timeCompression = 1;

	/** Number of FFT slices spanned by one tile. */
	private int tileSlices;

	/** Width of a tile image in pixels ({@code tileSlices / timeCompression}). */
	private int tileImgWidth;

	/** Time span of one tile, in milliseconds. */
	private long tileMillis;

	/** Max number of tiles to keep in memory. */
	private int maxTiles = 64;

	/** Visible time range (millis) the tile memory budget was last worked out for. */
	private double lastVisibleMillis = -1;

	/** Tile holding the in-progress (un-finalised) accumulation column. */
	private SpecTile pendingTile;

	/** Centre tile index of recent activity, used to pick eviction victims. */
	private long activityCentreIndex;

	/** Running total of FFT data units added (mirrors base semantics). */
	private long totalPowSpec = 0;

	public Scrolling2DPlotDataFX2(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
		super(specPlotInfo, iChannel);
		this.iChannel = iChannel;
		this.specPlotInfo = specPlotInfo;
		this.dataBlock2D = specPlotInfo.getDataBlock2D();
		this.tdProjector = specPlotInfo.getTDGraph().getGraphProjector();
		this.specColors = specPlotInfo;
	}

	/**
	 * Construct a tiled renderer driven directly from a projector and colour source,
	 * with no owning {@link Scrolling2DPlotInfo}. Used where the spectrogram is drawn
	 * outside a {@code TDGraphFX} - e.g. the acoustic scroll-bar spectrogram preview.
	 * The whole frequency range is shown (there is no frequency-zoom axis to consult),
	 * so {@link #getDataAxisMinVal()}/Max are not used in this mode.
	 *
	 * @param tdProjector the projector providing the time axis / visible range / width.
	 * @param dataBlock   the FFT/2D data block.
	 * @param colours     the colour source for the spectrogram.
	 * @param iChannel    the channel (sequence) to plot.
	 * @param isViewer    true if running in viewer mode.
	 */
	public Scrolling2DPlotDataFX2(TimeProjectorFX tdProjector, DataBlock2D dataBlock, Plot2DColours colours,
			int iChannel, boolean isViewer) {
		super(tdProjector, dataBlock, colours, iChannel, isViewer);
		this.iChannel = iChannel;
		this.specPlotInfo = null;
		this.dataBlock2D = dataBlock;
		this.tdProjector = tdProjector;
		this.specColors = colours;
		// flip the data vertically, matching the legacy datagram constructor of the
		// single-image renderer that this preview previously used.
		this.reverse = true;
	}

	/**
	 * A single spectrogram tile covering a fixed span of absolute time.
	 * <p>
	 * A tile keeps a copy of the geometry it was built with (span, image width, bin
	 * count, compression) rather than reading the enclosing class's current values.
	 * That way a tile built for one visible time range can still be drawn correctly
	 * (just at a different scale) after the display resolution has changed.
	 */
	private class SpecTile {
		final long tileIndex;
		final long startMillis;
		final long endMillis;
		/** Time span of this tile, in millis (the {@code tileMillis} it was built with). */
		final long spanMillis;
		/** Width of this tile's image, in pixels. */
		final int imgWidth;
		/** Number of frequency bins (image height) of this tile. */
		final int nBins;
		/** Number of FFT slices this tile spans. */
		final int slices;
		/** Number of FFT slices averaged into one column of this tile. */
		final int compression;
		final WritableImage image;
		final PixelWriter writer;
		/** Averaged scaled-dB values per [column][bin], retained for re-colouring. */
		final short[][] colData;
		/** Which columns have had data written. */
		final boolean[] colWritten;
		/** True once at least one column has data. */
		boolean hasData = false;
		int state = STATE_EMPTY;

		/** Accumulator for the column currently being built. */
		final double[] accum;
		int accumCol = -1;
		int accumCount = 0;

		SpecTile(long tileIndex) {
			this.tileIndex = tileIndex;
			this.spanMillis = tileMillis;
			this.imgWidth = Math.max(1, tileImgWidth);
			this.nBins = Math.max(1, dataWidth);
			this.slices = Math.max(1, tileSlices);
			this.compression = Math.max(1, timeCompression);
			this.startMillis = tileIndex * spanMillis;
			this.endMillis = startMillis + spanMillis;
			this.image = new WritableImage(imgWidth, nBins);
			this.writer = image.getPixelWriter();
			this.colData = new short[imgWidth][nBins];
			this.colWritten = new boolean[imgWidth];
			this.accum = new double[nBins];
		}

		/** Approximate memory used by this tile (image + power store), in bytes. */
		long byteSize() {
			return (long) imgWidth * nBins * 6L;
		}

		/** The rendered resolution of this tile - millis of data per image pixel. */
		double millisPerPixel() {
			return spanMillis / (double) imgWidth;
		}
	}

	@Override
	public boolean isIncrementalStore() {
		return true;
	}

	@Override
	public long getTileMillis() {
		return tileMillis;
	}

	/**
	 * The span of time one tile should aim to cover, in millis. A quarter of the visible
	 * range keeps the number of tiles small while still letting a scroll re-use most of
	 * them. The actual span is rounded to the time compression and to sensible image
	 * widths by {@link #checkConfig()}, so this is only a target.
	 * <p>
	 * Overridden where the tiles must not be larger than a single offline order - the
	 * scroll-bar preview marks tiles loaded a whole order at a time, so a tile spanning
	 * more than one order could never be completed.
	 *
	 * @param visibleMillis the visible time range in millis.
	 * @return the target span of one tile in millis.
	 */
	protected double targetTileMillis(double visibleMillis) {
		return visibleMillis / 4.0;
	}

	/* ===================== configuration ===================== */

	@Override
	public void checkConfig() {
		if (dataBlock2D == null) {
			return;
		}
		int newDataWidth = dataBlock2D.getDataWidth(iChannel);
		int newHop = dataBlock2D.getHopSamples();
		float sr = dataBlock2D.getSampleRate();
		if (newDataWidth <= 0 || newHop <= 0 || sr <= 0) {
			return;
		}
		double newTimeScale = newHop / sr;

		if (tdProjector.getTimeAxis() == null) {
			return;
		}
		double visibleMillis = tdProjector.getVisibleTime();
		double screenPixels = tdProjector.getGraphTimePixels();
		if (visibleMillis <= 0 || screenPixels <= 0) {
			return;
		}

		// time compression - same derivation as the base class createWritableImage()
		int newComp = 1;
		int fftPixels = (int) Math.round((visibleMillis / 1000.) / newTimeScale);
		while (fftPixels >= screenPixels * newComp * 2) {
			newComp *= 2;
		}

		// tile span: aim for the target span (a quarter of the visible range unless a
		// subclass says otherwise), aligned to compression.
		double targetMillis = Math.max(targetTileMillis(visibleMillis), 1);
		int slices = (int) Math.max(1, Math.round((targetMillis / 1000.) / newTimeScale));
		slices = Math.max(newComp, ((slices + newComp - 1) / newComp) * newComp);
		int imgW = slices / newComp;
		if (imgW > MAXIMAGESIZE) {
			imgW = MAXIMAGESIZE;
			slices = imgW * newComp;
		}
		if (imgW < 64) {
			imgW = 64;
			slices = imgW * newComp;
		}
		long newTileMillis = Math.max(1, Math.round(slices * newTimeScale * 1000.));

		// A change of data width or FFT rate invalidates the images themselves; a change
		// of time compression changes the resolution they are rendered at; a change of
		// tile span only changes where the tile boundaries fall.
		boolean dataChanged = newDataWidth != dataWidth || newTimeScale != timeScale;
		boolean compChanged = newComp != timeCompression;
		boolean spanChanged = newTileMillis != tileMillis;
		boolean configured = tileMillis > 0 && dataWidth > 0;

		if (!dataChanged && !compChanged && !spanChanged) {
			checkMaxTiles(visibleMillis);
			return;
		}

		if (!dataChanged && !compChanged && configured) {
			// Only the ideal tile span has moved (i.e. the visible range changed but not
			// enough to alter the time compression). The existing tiles hold data at
			// exactly the right resolution and simply need drawing across a different
			// number of pixels, so leave everything alone as long as the tiles are still
			// a sensible size for the new range. This is what stops the whole image being
			// thrown away every time the user nudges the visible time range.
			double spanRatio = (double) newTileMillis / (double) tileMillis;
			if (spanRatio <= TILE_SPAN_TOLERANCE && spanRatio >= 1. / TILE_SPAN_TOLERANCE) {
				checkMaxTiles(visibleMillis);
				return;
			}
		}

		synchronized (tileLock) {
			// checkConfig() is called from both the data thread and the FX thread, so
			// re-check inside the lock: another thread may have applied the same change
			// already and we must not throw its new tiles away.
			if (newDataWidth == dataWidth && newTimeScale == timeScale && newComp == timeCompression
					&& newTileMillis == tileMillis) {
				computeMaxTiles(visibleMillis);
				lastVisibleMillis = visibleMillis;
				return;
			}

			// Can the existing images be kept (drawn at the new scale) while the new
			// tiling fills in? Only if they are still valid data at a comparable
			// resolution - a huge change of visible range makes them useless, in which
			// case everything is discarded as it always was.
			double scaleRatio = (timeCompression > 0) ? (double) newComp / (double) timeCompression : Double.MAX_VALUE;
			boolean retain = !dataChanged && configured && scaleRatio <= MAX_RETAIN_SCALE
					&& scaleRatio >= 1. / MAX_RETAIN_SCALE;
			if (retain) {
				retireTiles();
			}
			else {
				legacyTiles.clear();
			}

			this.dataWidth = newDataWidth;
			this.hopSamples = newHop;
			this.timeScale = newTimeScale;
			this.timeCompression = newComp;
			this.tileSlices = slices;
			this.tileImgWidth = imgW;
			this.tileMillis = newTileMillis;
			clearTiles();
			computeMaxTiles(visibleMillis);
			lastVisibleMillis = visibleMillis;
			pruneLegacy();
		}
	}

	/**
	 * Move the current tiles into the legacy (draw only) store so that they carry on
	 * being shown, scaled to the new time axis, until new tiles cover the same time.
	 * Must be called with {@link #tileLock} held and before the tiles are cleared.
	 */
	private void retireTiles() {
		flushAccum(pendingTile, true);
		for (SpecTile tile : tiles.values()) {
			if (tile.hasData) {
				legacyTiles.add(tile);
			}
		}
	}

	/**
	 * Drop legacy tiles which are no longer worth keeping - either rendered at a very
	 * different resolution to the current tiling, or over the legacy memory budget
	 * (oldest generation dropped first). Must be called with {@link #tileLock} held.
	 */
	private void pruneLegacy() {
		if (legacyTiles.isEmpty()) {
			return;
		}
		double mPerPix = (tileImgWidth > 0) ? tileMillis / (double) tileImgWidth : 0;
		if (mPerPix > 0) {
			for (int i = legacyTiles.size() - 1; i >= 0; i--) {
				double ratio = legacyTiles.get(i).millisPerPixel() / mPerPix;
				if (ratio > MAX_RETAIN_SCALE || ratio < 1. / MAX_RETAIN_SCALE) {
					legacyTiles.remove(i);
				}
			}
		}
		long bytes = 0;
		for (SpecTile tile : legacyTiles) {
			bytes += tile.byteSize();
		}
		while (bytes > MAXLEGACYSIZE && !legacyTiles.isEmpty()) {
			bytes -= legacyTiles.remove(0).byteSize();
		}
	}

	/**
	 * Drop legacy tiles which are well outside the window being drawn. Called from
	 * the draw, which is the only place the current scroll position is known. Must be
	 * called with {@link #tileLock} held.
	 */
	private void pruneLegacyByTime(double scrollStart, double visibleMillis) {
		if (legacyTiles.isEmpty() || visibleMillis <= 0) {
			return;
		}
		double keepStart = scrollStart - visibleMillis;
		double keepEnd = scrollStart + 2 * visibleMillis;
		for (int i = legacyTiles.size() - 1; i >= 0; i--) {
			SpecTile tile = legacyTiles.get(i);
			if (tile.endMillis < keepStart || tile.startMillis > keepEnd) {
				legacyTiles.remove(i);
			}
		}
	}

	/**
	 * Update the tile memory budget if the visible range has changed, without
	 * touching the tiles themselves.
	 */
	private void checkMaxTiles(double visibleMillis) {
		if (visibleMillis == lastVisibleMillis) {
			return;
		}
		synchronized (tileLock) {
			computeMaxTiles(visibleMillis);
			lastVisibleMillis = visibleMillis;
		}
	}

	private void computeMaxTiles(double visibleMillis) {
		long tileBytes = (long) tileImgWidth * Math.max(dataWidth, 1) * 6L; // image (4) + power store (2)
		int memMax = (int) (MAXSTORAGESIZE / Math.max(tileBytes, 1));
		int tilesPerVisible = (int) Math.ceil(visibleMillis / Math.max(tileMillis, 1));
		int minKeep = tilesPerVisible * 3 + 6;
		maxTiles = Math.max(minKeep, Math.min(memMax, 1024));
	}

	private void clearTiles() {
		tiles.clear();
		pendingTile = null;
		totalPowSpec = 0;
	}

	/* ===================== incoming data ===================== */

	@Override
	public void new2DData(DataUnit2D fftDataUnit) {
		checkConfig();
		if (tileMillis <= 0 || dataWidth <= 0) {
			return;
		}
		synchronized (tileLock) {
			long t = fftDataUnit.getTimeMilliseconds();
			setLastPowerSpecTime(t);
			totalPowSpec++;

			long index = Math.floorDiv(t, tileMillis);
			SpecTile tile = getOrCreateTile(index, STATE_LOADING);
			writeFFTToTile(tile, t, fftDataUnit);
			activityCentreIndex = index;
			evictIfNeeded();
		}
	}

	/**
	 * Get a tile, creating it if necessary. {@code minState} is the state to apply
	 * (never downgrades an already-loaded tile).
	 */
	private SpecTile getOrCreateTile(long index, int minState) {
		SpecTile tile = tiles.get(index);
		if (tile == null) {
			tile = new SpecTile(index);
			tiles.put(index, tile);
		}
		if (minState == STATE_LOADED) {
			tile.state = STATE_LOADED;
		}
		else if (tile.state == STATE_EMPTY) {
			tile.state = minState;
		}
		return tile;
	}

	/**
	 * Route a single FFT into the correct column of a tile, accumulating for time
	 * compression.
	 */
	private void writeFFTToTile(SpecTile tile, long t, DataUnit2D fftDataUnit) {
		int slice = (int) Math.round((t - tile.startMillis) / (timeScale * 1000.));
		if (slice < 0) {
			slice = 0;
		}
		if (slice >= tile.slices) {
			slice = tile.slices - 1;
		}
		int col = slice / tile.compression;
		if (col < 0 || col >= tile.imgWidth) {
			return;
		}

		// flush the previous (different) column/tile before starting a new one.
		if (pendingTile != tile || (pendingTile != null && pendingTile.accumCol != col)) {
			flushAccum(pendingTile, true);
			tile.accumCol = col;
			tile.accumCount = 0;
			pendingTile = tile;
		}

		double[] magData = fftDataUnit.getMagnitudeData();
		if (magData != null) {
			int n = Math.min(tile.nBins, magData.length);
			for (int i = 0; i < n; i++) {
				int idx = reverse ? n - 1 - i : i;
				tile.accum[i] += magData[idx];
			}
		}
		// null magData (e.g. blank unit) contributes zeros but still advances the count.
		tile.accumCount++;
	}

	/**
	 * Write the accumulated column into the tile image / power store.
	 *
	 * @param tile     the tile to flush (may be null).
	 * @param finalise true to clear the accumulator afterwards (column complete);
	 *                 false for a preview flush that leaves accumulation running.
	 */
	private void flushAccum(SpecTile tile, boolean finalise) {
		if (tile == null || tile.accumCount == 0 || tile.accumCol < 0 || tile.accumCol >= tile.imgWidth) {
			return;
		}
		int col = tile.accumCol;
		for (int i = 0; i < tile.nBins; i++) {
			double avg = tile.accum[i] / tile.accumCount;
			tile.writer.setColor(col, tile.nBins - 1 - i, specColors.getColours(avg));
			tile.colData[col][i] = (short) (avg * DECIBEL_INT_SCALE);
		}
		tile.colWritten[col] = true;
		tile.hasData = true;
		if (finalise) {
			for (int i = 0; i < tile.nBins; i++) {
				tile.accum[i] = 0;
			}
			tile.accumCount = 0;
		}
	}

	/**
	 * Evict tiles furthest from recent activity until within the memory cap.
	 */
	private void evictIfNeeded() {
		while (tiles.size() > maxTiles) {
			long lo = tiles.firstKey();
			long hi = tiles.lastKey();
			long victim = (Math.abs(lo - activityCentreIndex) >= Math.abs(hi - activityCentreIndex)) ? lo : hi;
			if (pendingTile != null && pendingTile.tileIndex == victim) {
				// don't evict the tile we're still writing into; try the other end.
				victim = (victim == lo) ? hi : lo;
				if (pendingTile.tileIndex == victim) {
					break;
				}
			}
			tiles.remove(victim);
		}
	}

	/* ===================== ordering integration ===================== */

	@Override
	public List<long[]> getRequiredLoadIntervals(long preloadStart, long preloadEnd) {
		synchronized (tileLock) {
			List<long[]> intervals = new ArrayList<>();
			if (tileMillis <= 0) {
				// not configured yet - order the whole window.
				intervals.add(new long[] { preloadStart, preloadEnd });
				return intervals;
			}
			// Return each contiguous run of not-loaded tiles as a separate interval
			// (rather than one bounding box) so the owner can order them as small,
			// separate chunks and keep resident FFT-unit memory low.
			long i0 = Math.floorDiv(preloadStart, tileMillis);
			long i1 = Math.floorDiv(preloadEnd - 1, tileMillis);
			long runStart = Long.MIN_VALUE;
			long prev = Long.MIN_VALUE;
			for (long i = i0; i <= i1; i++) {
				SpecTile t = tiles.get(i);
				boolean missing = (t == null || t.state != STATE_LOADED);
				if (missing) {
					if (runStart == Long.MIN_VALUE) {
						runStart = i;
					}
					prev = i;
				}
				else if (runStart != Long.MIN_VALUE) {
					intervals.add(new long[] { runStart * tileMillis, (prev + 1) * tileMillis });
					runStart = Long.MIN_VALUE;
				}
			}
			if (runStart != Long.MIN_VALUE) {
				intervals.add(new long[] { runStart * tileMillis, (prev + 1) * tileMillis });
			}
			return intervals;
		}
	}

	@Override
	public void markRangeLoaded(long startMillis, long endMillis) {
		synchronized (tileLock) {
			if (tileMillis <= 0) {
				return;
			}
			// finalise any in-progress column so the latest data is visible.
			flushAccum(pendingTile, true);
			long i0 = Math.floorDiv(startMillis, tileMillis);
			long i1 = Math.floorDiv(endMillis - 1, tileMillis);
			for (long i = i0; i <= i1; i++) {
				getOrCreateTile(i, STATE_LOADED);
			}
			activityCentreIndex = (i0 + i1) / 2;
			evictIfNeeded();
		}
	}

	@Override
	public void resetForLoad() {
		resetForLoad(false);
	}

	@Override
	public void resetForLoad(boolean newWritableImage) {
		synchronized (tileLock) {
			clearTiles();
			// a reset means start again from scratch, so the retained images go too.
			legacyTiles.clear();
			setLastPowerSpecTime(0);
		}
		if (newWritableImage) {
			checkConfig();
		}
	}

	/* ===================== drawing ===================== */

	@Override
	public synchronized void drawSpectrogram(GraphicsContext g2d, Rectangle windowRect, Orientation orientation,
			PamAxisFX timeAxis, double scrollStart, boolean wrap) {

		synchronized (tileLock) {
			if (dataWidth <= 0 || tileMillis <= 0 || (tiles.isEmpty() && legacyTiles.isEmpty())) {
				return;
			}

			g2d.setImageSmoothing(false);

			// preview-flush the in-progress column so the newest data shows.
			flushAccum(pendingTile, false);

			double timePixels;
			double freqPixels;
			double imageFP1;
			double imageFP2;

			if (orientation == Orientation.VERTICAL) {
				timePixels = windowRect.getHeight();
				freqPixels = windowRect.getWidth();
				imageFP2 = -freqPixels; // negative to flip the image
				imageFP1 = 0;

				Affine at = new Affine();
				Rotate rot = Transform.rotate(-90, windowRect.getWidth() / 2., windowRect.getHeight() / 2.);
				at.append(rot);
				Translate translate = Transform.translate((windowRect.getWidth() - windowRect.getHeight()) / 2.,
						(windowRect.getWidth() + windowRect.getHeight()) / 2.);
				at.append(translate);
				g2d.setTransform(at);
			}
			else {
				g2d.setTransform(horzAffine);
				timePixels = windowRect.getWidth();
				freqPixels = windowRect.getHeight();
				imageFP2 = freqPixels;
				imageFP1 = 0;
			}

			double visibleMillis = (timeAxis.getMaxVal() - timeAxis.getMinVal()) * 1000.;
			if (visibleMillis <= 0) {
				if (orientation == Orientation.VERTICAL) {
					g2d.setTransform(horzAffine);
				}
				return;
			}
			double tScalePixPerMs = timePixels / visibleMillis;

			// frequency (vertical) bin range - identical derivation to the base class.
			// With no owning plot info (e.g. the scroll-bar preview) there is no
			// frequency-zoom axis, so show the whole range {0, dataWidth}.
			double[] freqBinRange = { 0, dataWidth };
			if (specPlotInfo != null) {
				double min = specPlotInfo.getDataAxisMinVal();
				double max = specPlotInfo.getDataAxisMaxVal();
				freqBinRange[0] = valueToBin(min);
				freqBinRange[1] = valueToBin(max);
			}
			for (int i = 0; i < 2; i++) {
				freqBinRange[i] = Math.max(0, Math.min(freqBinRange[i], dataWidth));
			}
			double freqWidth = Math.min(freqBinRange[0] - freqBinRange[1] + 1, dataWidth);

			double wrapPix = (wrap && specPlotInfo != null) ? specPlotInfo.getTDGraph().getWrapPix() : 0;

			// forget any retained images which have scrolled well out of the way.
			pruneLegacyByTime(scrollStart, visibleMillis);

			// Two passes: the retained (old geometry) images first, then the current
			// tiles over the top of them. A current tile is transparent wherever it has
			// no data yet, so the retained image shows through until it is replaced.
			for (int pass = 0; pass < 2; pass++) {
				Collection<SpecTile> passTiles = (pass == 0) ? legacyTiles : tiles.values();
				for (SpecTile tile : passTiles) {
					if (!tile.hasData) {
						continue;
					}
					if (!wrap) {
						// Snap both tile edges to whole pixels using the same time->pixel
						// mapping. Because one tile's end time equals the next tile's start
						// time, their shared edge rounds to the same integer pixel, so the
						// tiles abut exactly with no sub-pixel gap (which would otherwise show
						// as a dark line of background between the images).
						double leftEdge = Math.round((tile.startMillis - scrollStart) * tScalePixPerMs);
						double rightEdge = Math.round((tile.endMillis - scrollStart) * tScalePixPerMs);
						if (rightEdge < 0 || leftEdge > timePixels) {
							continue;
						}
						drawTilePiece(g2d, tile, 0, tile.imgWidth, leftEdge, rightEdge - leftEdge, freqBinRange[1],
								freqWidth, imageFP1, imageFP2);
					}
					else {
						// in wrap mode only the most recent visible window of data is valid.
						if (tile.endMillis <= scrollStart || tile.startMillis >= scrollStart + visibleMillis) {
							continue;
						}
						double leftEdge = Math.round(wrapPix + (tile.startMillis - scrollStart) * tScalePixPerMs);
						double rightEdge = Math.round(wrapPix + (tile.endMillis - scrollStart) * tScalePixPerMs);
						double w = rightEdge - leftEdge;
						if (w <= 0) {
							continue;
						}
						double s0 = leftEdge % timePixels;
						if (s0 < 0) {
							s0 += timePixels;
						}
						if (s0 + w <= timePixels) {
							drawTilePiece(g2d, tile, 0, tile.imgWidth, s0, w, freqBinRange[1], freqWidth, imageFP1,
									imageFP2);
						}
						else {
							double firstW = timePixels - s0;
							double frac = firstW / w;
							double srcSplit = frac * tile.imgWidth;
							drawTilePiece(g2d, tile, 0, srcSplit, s0, firstW, freqBinRange[1], freqWidth, imageFP1,
									imageFP2);
							drawTilePiece(g2d, tile, srcSplit, tile.imgWidth - srcSplit, 0, w - firstW,
									freqBinRange[1], freqWidth, imageFP1, imageFP2);
						}
					}
				}
			}

			if (wrap) {
				g2d.setStroke(specColors.getWrapColor());
				g2d.setLineWidth(1);
				g2d.strokeLine(wrapPix, 0, wrapPix, freqPixels);
			}

			if (orientation == Orientation.VERTICAL) {
				g2d.setTransform(horzAffine);
			}
		}
	}

	/**
	 * Draw a (sub-)rectangle of a tile image to the screen. {@code srcX}/{@code srcW}
	 * are in tile-image pixels (time axis); the vertical range uses the same bin
	 * coordinates as the single-image base class. The vertical coordinates are given
	 * in current-{@code dataWidth} bins and scaled to the tile's own bin count, so
	 * that a retained tile built with a different data width still draws correctly.
	 */
	private void drawTilePiece(GraphicsContext g2d, SpecTile tile, double srcX, double srcW, double destX,
			double destW, double srcY, double srcH, double destY, double destH) {
		if (srcW <= 0 || destW <= 0) {
			return;
		}
		if (tile.nBins != dataWidth && dataWidth > 0) {
			double binScale = tile.nBins / (double) dataWidth;
			srcY *= binScale;
			srcH *= binScale;
		}
		g2d.drawImage(tile.image, srcX, srcY, srcW, srcH, destX, destY, destW, destH);
	}

	/* ===================== re-colour ===================== */

	@Override
	public void reBuildImage() {
		Thread thread = new Thread(() -> {
			recolourTiles(null);
			rebuildFinished();
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	protected boolean reBuildImage(Task<Boolean> task) {
		boolean ok = recolourTiles(task);
		rebuildFinished();
		return ok;
	}

	/**
	 * Re-render every tile image from its retained power store, e.g. after a colour
	 * map or amplitude scale change.
	 */
	private boolean recolourTiles(Task<Boolean> task) {
		synchronized (tileLock) {
			if (dataWidth <= 0) {
				return false;
			}
			// retained tiles are recoloured too, else they would be left showing the old
			// colour map / amplitude scale until they are replaced.
			for (int pass = 0; pass < 2; pass++) {
				Collection<SpecTile> passTiles = (pass == 0) ? legacyTiles : tiles.values();
				for (SpecTile tile : passTiles) {
					if (task != null && task.isCancelled()) {
						return false;
					}
					for (int col = 0; col < tile.imgWidth; col++) {
						if (!tile.colWritten[col]) {
							continue;
						}
						for (int i = 0; i < tile.nBins; i++) {
							double avg = tile.colData[col][i] / DECIBEL_INT_SCALE;
							tile.writer.setColor(col, tile.nBins - 1 - i, specColors.getColours(avg));
						}
					}
				}
			}
		}
		return true;
	}

	/* ===================== misc / overrides ===================== */

	@Override
	public double valueToBin(double value) {
		double val = (double) dataWidth * (value - dataBlock2D.getMinDataValue())
				/ (dataBlock2D.getMaxDataValue() - dataBlock2D.getMinDataValue());
		return dataWidth - val;
	}

	@Override
	public double binToValue(double bin) {
		bin = dataWidth - bin;
		return bin / (double) dataWidth * (dataBlock2D.getMaxDataValue() - dataBlock2D.getMinDataValue())
				+ dataBlock2D.getMinDataValue();
	}

	@Override
	public int getFftHop() {
		return hopSamples;
	}

	@Override
	public long getTotalPowerSpec() {
		return totalPowSpec;
	}

	@Override
	public void setSpecColors(Plot2DColours specColors) {
		this.specColors = specColors;
	}

	@Override
	public Plot2DColours getSpecColors() {
		return specColors;
	}

	@Override
	public Image getWritableImage() {
		synchronized (tileLock) {
			if (tiles.isEmpty()) {
				return null;
			}
			return tiles.lastEntry().getValue().image;
		}
	}
}
