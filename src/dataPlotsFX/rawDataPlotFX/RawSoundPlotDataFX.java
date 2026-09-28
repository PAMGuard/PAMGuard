package dataPlotsFX.rawDataPlotFX;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import PamDetection.RawDataUnit;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Draws one channel of raw sound data onto the plot pane of a tdGraphFX.
 * <p>
 * Raw sound data is not kept in memory - only a second or so is available from
 * the data block - so a display which shows more than that has to keep its own
 * summary of the waveform. This class keeps that summary as a set of
 * <b>tiles</b>, each covering a fixed span of <i>absolute</i> time and holding
 * the min/max of the waveform over a fixed number of samples (a
 * <i>cell</i>). This is the same idea as the tiled spectrogram store, but a
 * waveform min/max envelope is much cheaper than an image, so the tiles are
 * simple float arrays rather than {@code WritableImage}s.
 * <p>
 * The important consequence of storing tiles against absolute time rather than
 * against screen pixels is that <b>drawing is a pure time-to-pixel mapping</b>.
 * Nothing has to be re-calculated when the visible time range changes, so the
 * waveform stretches and squashes live as the user drags the time range spinner
 * rather than only refreshing once they stop.
 * <p>
 * The resolution of the store (samples per cell) is set from the number of
 * samples per screen pixel, rounded <i>down</i> to a power of two so that there
 * is always at least one cell per pixel and so that small changes in the visible
 * range do not change the resolution at all. When the range is expanded enough
 * to coarsen the resolution the existing tiles are min/max down-sampled in place
 * - which is exact - rather than interpolated or thrown away, so no detail is
 * invented and nothing is lost beyond the reduction in resolution itself.
 * Tiles which are finer or coarser than the current resolution still draw
 * correctly, since every tile knows the geometry it was built with.
 * <p>
 * Drawing has two modes:
 * <ul>
 * <li>more than one cell per pixel - the min/max of all the cells in a pixel is
 * drawn as a single vertical line, i.e. a proper waveform envelope.
 * <li>more than one pixel per cell - the cell values are joined up with lines
 * (when the store holds individual samples), or drawn as a min/max band (when
 * the tile is coarser than the current display, which happens for a moment
 * after zooming in).
 * </ul>
 *
 * @author Jamie Macaulay
 */
public class RawSoundPlotDataFX {

	/** Number of cells in a newly created tile. */
	private static final int TILE_CELLS = 1024;

	/**
	 * Maximum number of cells to hold in memory across all tiles. At 9 bytes per
	 * cell (two floats and a flag) this is about 18MB per channel.
	 */
	private static final int MAX_STORE_CELLS = 2000000;

	/** Minimum number of tiles to keep, whatever the memory budget says. */
	private static final int MIN_TILES = 8;

	/**
	 * The number of cells to aim for in each screen pixel.
	 * <p>
	 * One cell per pixel is not enough. A cell holds the min/max of a fixed number
	 * of samples, so a pixel made from a single cell shows the range of that cell's
	 * samples rather than the range of the samples actually under the pixel. With
	 * the resolution rounded to powers of two there are then between one and two
	 * cells in a pixel, and the resulting envelope alternates between pixels which
	 * happen to catch a peak and pixels which do not - which on, say, a 1 s window
	 * of 96kHz data makes a sine wave look like a ragged comb rather than a
	 * waveform. Over-sampling the store means every pixel is made from enough cells
	 * for its envelope to be right to a small fraction of a pixel.
	 */
	private static final int CELLS_PER_PIXEL = 8;

	/**
	 * How far a data unit's start time may be from the end of the previous one and
	 * still be treated as continuing it.
	 * <p>
	 * Data unit times are whole milliseconds, so at a high sample rate the start
	 * time of each unit is rounded by up to half a millisecond even when the data
	 * are perfectly continuous. Half a millisecond is nothing when the display is
	 * showing seconds, but is most of the screen when it is showing a couple of
	 * milliseconds, so a continuing unit is placed at the exact end of the previous
	 * one rather than at its own rounded time. The comparison is always made
	 * against the unit's own time stamp, so this can absorb the rounding but can
	 * never accumulate into a drift of more than the tolerance.
	 */
	private static final double CONTIGUOUS_TOLERANCE_MILLIS = 1.5;

	/** The channel displayed in this plot pane. */
	private int iChannel;

	/** The current sample rate. */
	private float sampleRate = 1000;

	/** The colour of the sound lines. */
	private Color lineColor = Color.BLUE;

	/** Width of the sound lines in pixels. */
	private double lineWidth = 1;

	/** Lock guarding all access to the tile store and the write cursor. */
	private final Object tileLock = new Object();

	/**
	 * The waveform tiles, keyed by their exact start time in milliseconds. Tiles
	 * of different resolutions never overlap in time (a new tile removes any
	 * overlapping tile of a different resolution) so the map is a simple ordered
	 * list of the data held.
	 */
	private final TreeMap<Double, WaveTile> tiles = new TreeMap<>();

	/** Number of raw samples summarised into one cell. Always a power of two. */
	private int samplesPerCell = 1;

	/** Total number of cells currently held, used for the memory budget. */
	private long totalCells = 0;

	/** Longest time span of any tile currently held, in millis. */
	private double maxTileSpan = 0;

	/** Increments with every tile created, so that newer tiles draw on top. */
	private long tileGeneration = 0;

	/** Time of the most recent data added, used to pick eviction victims. */
	private double activityMillis = 0;

	/**
	 * The exact time the next sample is expected at, i.e. the end of the last data
	 * unit added. NaN if no data have been added.
	 */
	private double nextExpectedMillis = Double.NaN;

	/**
	 * The spans of absolute time which have actually been loaded, as start -> end,
	 * always kept merged and non-overlapping.
	 * <p>
	 * This is what the store <i>holds</i>, as opposed to what it has data for: a
	 * stretch of silence, or a gap between files, is held but has no data in it, and
	 * must not be requested over and over again. Equally, an order which was cut
	 * short leaves the part it never reached un-held, so that it is asked for again
	 * rather than left as a permanent gap in the waveform. It is the caller's job to
	 * report loaded spans - see {@link #markRangeLoaded(double, double)}.
	 */
	private final TreeMap<Double, Double> loadedIntervals = new TreeMap<>();

	/*
	 * Write cursor. Data arrive in order, so rather than looking a tile up for
	 * every sample the position of the next sample to be written is held here and
	 * only re-derived when there is a gap in the data or the resolution changes.
	 */
	private WaveTile cursorTile;
	private int cursorCell;
	private int cursorCount;
	private float cellMin;
	private float cellMax;
	private boolean cellHasVal;

	public RawSoundPlotDataFX(RawSoundDataInfo rawSoundDataInfo, int channel) {
		this.sampleRate = rawSoundDataInfo.getSampleRate();
		this.iChannel = channel;
	}

	public RawSoundPlotDataFX() {
		// sample rate must be set...
	}

	/**
	 * A single tile of waveform summary data covering a fixed span of absolute
	 * time.
	 * <p>
	 * A tile keeps the geometry it was built with rather than reading the current
	 * values from the enclosing class, so a tile built at one resolution can still
	 * be drawn correctly (just at a different scale) after the display resolution
	 * has changed.
	 */
	private static class WaveTile {

		/** Start of the tile in millis. Held as a double so tiles abut exactly. */
		final double startMillis;

		/** Number of raw samples summarised into one cell of this tile. */
		int samplesPerCell;

		/** Time span of one cell of this tile, in millis. */
		double cellMillis;

		/** Number of cells in this tile. */
		int nCells;

		/** Minimum sample value in each cell. */
		float[] minVals;

		/** Maximum sample value in each cell. */
		float[] maxVals;

		/** Which cells have had data written to them. */
		boolean[] written;

		/** Number of cells which hold data. */
		int writtenCells = 0;

		/** Creation order, so that newer tiles are drawn over older ones. */
		long generation;

		WaveTile(double startMillis, int samplesPerCell, double cellMillis, int nCells) {
			this.startMillis = startMillis;
			this.samplesPerCell = samplesPerCell;
			this.cellMillis = cellMillis;
			this.nCells = nCells;
			this.minVals = new float[nCells];
			this.maxVals = new float[nCells];
			this.written = new boolean[nCells];
		}

		double spanMillis() {
			return nCells * cellMillis;
		}

		double endMillis() {
			return startMillis + spanMillis();
		}

		boolean hasData() {
			return writtenCells > 0;
		}
	}

	/**
	 * Set the sample rate.
	 *
	 * @param sampleRate sample rate in samples per second.
	 */
	public void setSampleRate(float sampleRate) {
		if (sampleRate == this.sampleRate) {
			return;
		}
		synchronized (tileLock) {
			this.sampleRate = sampleRate;
			// the existing tiles were built against the old sample rate.
			clearStore();
		}
	}

	/**
	 * Get the sample rate
	 *
	 * @return the sample rate in bins per second
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * Check whether the time scale is correct.
	 */
	public void checkConfig() {

	}

	/**
	 * Get the channel this plot is showing.
	 *
	 * @return the channel number.
	 */
	public int getChannel() {
		return iChannel;
	}

	/**
	 * Get the colour the waveform is drawn in.
	 *
	 * @return the line colour.
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Set the colour the waveform is drawn in.
	 *
	 * @param lineColor - the line colour.
	 */
	public void setLineColor(Color lineColor) {
		if (lineColor != null) {
			this.lineColor = lineColor;
		}
	}

	/**
	 * Get the width of the waveform line in pixels.
	 *
	 * @return the line width.
	 */
	public double getLineWidth() {
		return lineWidth;
	}

	/**
	 * Set the width of the waveform line in pixels.
	 *
	 * @param lineWidth - the line width.
	 */
	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	/* ===================== incoming data ===================== */

	/**
	 * Called whenever new raw sound data is to be added to the display.
	 *
	 * @param rawDataUnit  - raw data unit.
	 * @param binsPerPixel - the number of samples per screen pixel for the current
	 *                     time range, or -1 if this is not known (in which case the
	 *                     current resolution is kept).
	 */
	public void newRawData(RawDataUnit rawDataUnit, double binsPerPixel) {
		setBinsPerPixel(binsPerPixel);
		addNewRawData(rawDataUnit);
	}

	/**
	 * Convert a new RawDataUnit into min/max cells and add it to the tile store.
	 *
	 * @param rawDataUnit - the new raw data unit.
	 */
	public void addNewRawData(RawDataUnit rawDataUnit) {
		if (rawDataUnit == null || sampleRate <= 0) {
			return;
		}
		double[] rawData = rawDataUnit.getRawData();
		if (rawData == null || rawData.length == 0) {
			return;
		}

		double millisPerSample = 1000. / sampleRate;

		synchronized (tileLock) {
			double startMillis = rawDataUnit.getTimeMilliseconds();
			if (!Double.isNaN(nextExpectedMillis)
					&& Math.abs(startMillis - nextExpectedMillis) <= CONTIGUOUS_TOLERANCE_MILLIS) {
				startMillis = nextExpectedMillis;
			}
			nextExpectedMillis = startMillis + rawData.length * millisPerSample;

			int i = 0;
			while (i < rawData.length) {
				double sampleTime = startMillis + i * millisPerSample;
				if (!positionCursor(sampleTime)) {
					return;
				}
				/*
				 * Work out how many samples can go into the current tile without any further
				 * time arithmetic, then run a tight loop over them. Only when a tile fills up
				 * (or the data run out) do we go back round and re-derive the position.
				 */
				long samplesLeft = (long) (cursorTile.nCells - cursorCell) * samplesPerCell - cursorCount;
				int end = (int) Math.min(rawData.length, i + Math.max(samplesLeft, 1));
				for (; i < end; i++) {
					float val = (float) rawData[i];
					if (!cellHasVal) {
						cellMin = cellMax = val;
						cellHasVal = true;
					}
					else {
						if (val < cellMin) {
							cellMin = val;
						}
						if (val > cellMax) {
							cellMax = val;
						}
					}
					if (++cursorCount >= samplesPerCell) {
						writeCell();
						cursorCell++;
						cursorCount = 0;
						cellHasVal = false;
					}
				}
				if (cursorCell >= cursorTile.nCells) {
					cursorTile = null;
				}
			}

			// show the part filled cell straight away, but carry on accumulating into it.
			writeCell();

			activityMillis = nextExpectedMillis;
			evictIfNeeded();
		}
	}

	/**
	 * Set the store resolution from the number of samples per screen pixel. The
	 * resolution is rounded down to a power of two so that there are always at
	 * least {@link #CELLS_PER_PIXEL} cells in a pixel, and so that the resolution
	 * only changes when the visible range changes by a factor of two or more.
	 *
	 * @param binsPerPixel - samples per screen pixel, or -1 if not known.
	 */
	private void setBinsPerPixel(double binsPerPixel) {
		if (binsPerPixel <= 0 || Double.isNaN(binsPerPixel) || Double.isInfinite(binsPerPixel)) {
			return; // unknown - keep whatever resolution we have.
		}
		double samplesPerCellTarget = binsPerPixel / CELLS_PER_PIXEL;
		int newSamplesPerCell = 1;
		while (newSamplesPerCell * 2. <= samplesPerCellTarget && newSamplesPerCell < (1 << 24)) {
			newSamplesPerCell *= 2;
		}
		if (newSamplesPerCell == samplesPerCell) {
			return;
		}
		synchronized (tileLock) {
			writeCell();
			cursorTile = null;
			cellHasVal = false;
			cursorCount = 0;
			if (newSamplesPerCell > samplesPerCell) {
				// The display has been zoomed out. Min/max down-sampling by a power of two is
				// exact, so the existing data is coarsened rather than thrown away.
				downSampleTiles(newSamplesPerCell);
			}
			else {
				/*
				 * The display has been zoomed in. What is held is still drawn (as a coarse
				 * min/max band) but it no longer has the detail the display is asking for, so
				 * the store no longer counts as holding it and the data are re-loaded at the
				 * finer resolution.
				 */
				loadedIntervals.clear();
			}
			samplesPerCell = newSamplesPerCell;
		}
	}

	/**
	 * Position the write cursor ready for a sample at the given time. If the cursor
	 * is already in the right place (the usual case for continuous data) nothing is
	 * done.
	 *
	 * @param sampleTime - the time of the next sample in millis.
	 * @return true if the cursor is usable.
	 */
	private boolean positionCursor(double sampleTime) {
		if (cursorTile != null && cursorTile.samplesPerCell == samplesPerCell) {
			double cursorTime = cursorTile.startMillis
					+ (cursorCell + cursorCount / (double) samplesPerCell) * cursorTile.cellMillis;
			if (Math.abs(sampleTime - cursorTime) < 0.5 * cursorTile.cellMillis) {
				return true;
			}
		}

		// there is a gap in the data (or the resolution has changed) so finish off
		// whatever cell we were in the middle of and start again.
		writeCell();
		cellHasVal = false;
		cursorCount = 0;

		double cellMillis = samplesPerCell * 1000. / sampleRate;
		if (cellMillis <= 0 || Double.isNaN(cellMillis) || Double.isInfinite(cellMillis)) {
			cursorTile = null;
			return false;
		}
		double tileSpan = TILE_CELLS * cellMillis;
		double tileStart = Math.floor(sampleTime / tileSpan) * tileSpan;

		WaveTile tile = tiles.get(tileStart);
		if (tile == null || tile.samplesPerCell != samplesPerCell) {
			tile = createTile(tileStart, cellMillis);
		}

		int cell = (int) Math.floor((sampleTime - tile.startMillis) / tile.cellMillis);
		cell = Math.max(0, Math.min(cell, tile.nCells - 1));
		int offset = (int) Math.round((sampleTime - (tile.startMillis + cell * tile.cellMillis)) * sampleRate / 1000.);

		cursorTile = tile;
		cursorCell = cell;
		cursorCount = Math.max(0, Math.min(offset, samplesPerCell - 1));
		return true;
	}

	/**
	 * Create a new tile.
	 * <p>
	 * Any data of a <i>different</i> resolution covering the same time is cleared,
	 * since the new tile is about to hold the same waveform at the resolution the
	 * display now wants. Only the overlapping cells are cleared, not the whole of
	 * the old tile - a coarse tile can span far more time than a fine one, so
	 * throwing the whole thing away when zooming in would take a large chunk of the
	 * history with it.
	 *
	 * @param tileStart  - start of the tile in millis.
	 * @param cellMillis - time span of one cell.
	 * @return the new tile.
	 */
	private WaveTile createTile(double tileStart, double cellMillis) {
		WaveTile tile = new WaveTile(tileStart, samplesPerCell, cellMillis, TILE_CELLS);
		tile.generation = ++tileGeneration;

		clearOverlap(tileStart, tile.endMillis(), tile.samplesPerCell);

		tiles.put(tileStart, tile);
		totalCells += tile.nCells;
		maxTileSpan = Math.max(maxTileSpan, tile.spanMillis());
		return tile;
	}

	/**
	 * Clear all cells between two times which are held at a resolution other than
	 * the given one. Tiles left holding nothing are removed.
	 *
	 * @param start          - start of the time range in millis.
	 * @param end            - end of the time range in millis.
	 * @param samplesPerCell - the resolution to keep.
	 */
	private void clearOverlap(double start, double end, int samplesPerCell) {
		List<Double> empty = null;
		for (WaveTile other : tiles.values()) {
			if (other.samplesPerCell == samplesPerCell) {
				continue;
			}
			if (other.endMillis() <= start || other.startMillis >= end) {
				continue;
			}
			int c0 = Math.max(0, (int) Math.floor((start - other.startMillis) / other.cellMillis));
			int c1 = Math.min(other.nCells, (int) Math.ceil((end - other.startMillis) / other.cellMillis));
			for (int c = c0; c < c1; c++) {
				if (other.written[c]) {
					other.written[c] = false;
					other.writtenCells--;
				}
			}
			if (!other.hasData()) {
				if (empty == null) {
					empty = new ArrayList<>();
				}
				empty.add(other.startMillis);
			}
		}
		if (empty != null) {
			for (Double key : empty) {
				removeTile(key);
			}
		}
	}

	/**
	 * Write the accumulated min/max into the current cell. The accumulator is left
	 * alone so that a part filled cell can be shown and then written again once it
	 * is complete.
	 */
	private void writeCell() {
		if (cursorTile == null || !cellHasVal) {
			return;
		}
		if (cursorCell < 0 || cursorCell >= cursorTile.nCells) {
			return;
		}
		cursorTile.minVals[cursorCell] = cellMin;
		cursorTile.maxVals[cursorCell] = cellMax;
		if (!cursorTile.written[cursorCell]) {
			cursorTile.written[cursorCell] = true;
			cursorTile.writtenCells++;
		}
	}

	/**
	 * Min/max down-sample every tile which is finer than the new resolution. Both
	 * the old and the new resolution are powers of two, so the reduction factor is
	 * exact and the result is identical to what would have been stored had the data
	 * arrived at the new resolution in the first place.
	 *
	 * @param newSamplesPerCell - the new number of samples per cell.
	 */
	private void downSampleTiles(int newSamplesPerCell) {
		for (WaveTile tile : tiles.values()) {
			if (tile.samplesPerCell >= newSamplesPerCell) {
				continue;
			}
			int factor = newSamplesPerCell / tile.samplesPerCell;
			if (factor < 2) {
				continue;
			}
			int newN = (tile.nCells + factor - 1) / factor;
			float[] mn = new float[newN];
			float[] mx = new float[newN];
			boolean[] wr = new boolean[newN];
			int written = 0;
			for (int c = 0; c < tile.nCells; c++) {
				if (!tile.written[c]) {
					continue;
				}
				int nc = c / factor;
				if (!wr[nc]) {
					mn[nc] = tile.minVals[c];
					mx[nc] = tile.maxVals[c];
					wr[nc] = true;
					written++;
				}
				else {
					mn[nc] = Math.min(mn[nc], tile.minVals[c]);
					mx[nc] = Math.max(mx[nc], tile.maxVals[c]);
				}
			}
			totalCells += newN - tile.nCells;
			tile.minVals = mn;
			tile.maxVals = mx;
			tile.written = wr;
			tile.writtenCells = written;
			tile.nCells = newN;
			tile.cellMillis *= factor;
			tile.samplesPerCell = newSamplesPerCell;
			maxTileSpan = Math.max(maxTileSpan, tile.spanMillis());
		}
	}

	/**
	 * Drop the tiles furthest from the most recent activity until the store is
	 * within its memory budget.
	 */
	private void evictIfNeeded() {
		while (totalCells > MAX_STORE_CELLS && tiles.size() > MIN_TILES) {
			double lo = tiles.firstKey();
			double hi = tiles.lastKey();
			double victim = (Math.abs(lo - activityMillis) >= Math.abs(hi - activityMillis)) ? lo : hi;
			WaveTile tile = tiles.get(victim);
			if (tile == cursorTile) {
				victim = (victim == lo) ? hi : lo;
				if (tiles.get(victim) == cursorTile) {
					return;
				}
			}
			removeTile(victim);
		}
	}

	/**
	 * Remove a tile from the store, keeping the cell count and write cursor
	 * straight.
	 *
	 * @param key - the map key (start time) of the tile to remove.
	 */
	private void removeTile(Double key) {
		WaveTile tile = tiles.remove(key);
		if (tile == null) {
			return;
		}
		totalCells -= tile.nCells;
		//the data for this span have gone, so the store no longer holds it - forget it
		//was loaded or it would never be asked for again if the user scrolls back to it.
		unmarkLoaded(tile.startMillis, tile.endMillis());
		if (tile == cursorTile) {
			cursorTile = null;
			cellHasVal = false;
			cursorCount = 0;
		}
	}

	/**
	 * A new binsPerPixel value is being used, e.g. because the visible time range
	 * has changed. Nothing has to be re-drawn or re-calculated for the display to
	 * be correct - this simply coarsens the store when the display is zoomed out so
	 * that memory and drawing time stay bounded.
	 *
	 * @param binsPerPixel - the new number of samples per screen pixel.
	 */
	public void recalcSoundData(double binsPerPixel) {
		setBinsPerPixel(binsPerPixel);
	}

	/* ===================== load state ===================== */

	/**
	 * Record a span of time as loaded, so that it is not requested again. Called
	 * once an order has completed - including where it turned out to hold no data at
	 * all, since an empty stretch is just as loaded as a full one.
	 *
	 * @param startMillis - start of the loaded span in millis.
	 * @param endMillis   - end of the loaded span in millis.
	 */
	public void markRangeLoaded(double startMillis, double endMillis) {
		if (endMillis <= startMillis) {
			return;
		}
		synchronized (tileLock) {
			double start = startMillis;
			double end = endMillis;
			//swallow the interval before this one if it runs into it.
			Map.Entry<Double, Double> before = loadedIntervals.floorEntry(start);
			if (before != null && before.getValue() >= start) {
				start = before.getKey();
				end = Math.max(end, before.getValue());
			}
			//and every interval which starts within the (now extended) one.
			Iterator<Map.Entry<Double, Double>> it = loadedIntervals.tailMap(start, true).entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Double, Double> entry = it.next();
				if (entry.getKey() > end) {
					break;
				}
				end = Math.max(end, entry.getValue());
				it.remove();
			}
			loadedIntervals.put(start, end);
		}
	}

	/**
	 * The parts of the given time range which are not held and so still need to be
	 * loaded. An empty list means the range can be drawn as it stands, with no disk
	 * access at all - which is the point of loading a margin either side of the
	 * visible range, since a small scroll then needs no loading.
	 *
	 * @param startMillis - start of the range of interest in millis.
	 * @param endMillis   - end of the range of interest in millis.
	 * @return the {start, end} spans still needing to be loaded, in time order.
	 */
	public List<double[]> getRequiredLoadIntervals(double startMillis, double endMillis) {
		List<double[]> missing = new ArrayList<>();
		if (endMillis <= startMillis) {
			return missing;
		}
		synchronized (tileLock) {
			double cursor = startMillis;
			for (Map.Entry<Double, Double> entry : loadedIntervals.entrySet()) {
				if (entry.getValue() <= cursor) {
					continue;
				}
				if (entry.getKey() >= endMillis) {
					break;
				}
				if (entry.getKey() > cursor) {
					missing.add(new double[] { cursor, entry.getKey() });
				}
				cursor = entry.getValue();
				if (cursor >= endMillis) {
					break;
				}
			}
			if (cursor < endMillis) {
				missing.add(new double[] { cursor, endMillis });
			}
		}
		return missing;
	}

	/**
	 * Drop a span of time from the loaded set, so that it is loaded again if it is
	 * needed. Used when the data for that span are thrown away. Must be called with
	 * {@link #tileLock} held.
	 *
	 * @param startMillis - start of the span to forget in millis.
	 * @param endMillis   - end of the span to forget in millis.
	 */
	private void unmarkLoaded(double startMillis, double endMillis) {
		if (endMillis <= startMillis || loadedIntervals.isEmpty()) {
			return;
		}
		Double from = loadedIntervals.floorKey(startMillis);
		if (from == null) {
			from = loadedIntervals.firstKey();
		}
		List<double[]> keep = null;
		Iterator<Map.Entry<Double, Double>> it = loadedIntervals.tailMap(from, true).entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Double, Double> entry = it.next();
			if (entry.getKey() >= endMillis) {
				break;
			}
			if (entry.getValue() <= startMillis) {
				continue;
			}
			//overlaps the span being forgotten - remove it and put back whatever of it
			//lies outside the span.
			it.remove();
			if (keep == null) {
				keep = new ArrayList<>();
			}
			if (entry.getKey() < startMillis) {
				keep.add(new double[] { entry.getKey(), startMillis });
			}
			if (entry.getValue() > endMillis) {
				keep.add(new double[] { endMillis, entry.getValue() });
			}
		}
		if (keep != null) {
			for (double[] interval : keep) {
				loadedIntervals.put(interval[0], interval[1]);
			}
		}
	}

	/* ===================== drawing ===================== */

	/**
	 * Draw the stored waveform onto the screen.
	 *
	 * @param g2d         - the graphics context to draw onto.
	 * @param windowRect  - rectangle describing the window.
	 * @param orientation - the orientation of the display VERTICAL or HORIZONTAL.
	 * @param timeAxis    - the time axis.
	 * @param dataAxis    - the amplitude axis.
	 * @param scrollStart - the start of the display in milliseconds datenum. This is
	 *                    the left most side of the display i.e. where the oldest
	 *                    visible data is displayed.
	 * @param wrapPix     - the wrap position on the time display, -1 if not
	 *                    wrapping.
	 */
	public void drawRawSoundData(GraphicsContext g2d, Rectangle windowRect, Orientation orientation, PamAxisFX timeAxis,
			PamAxisFX dataAxis, double scrollStart, double wrapPix) {

		if (timeAxis == null || dataAxis == null) {
			return;
		}

		double timePixels = (orientation == Orientation.HORIZONTAL) ? windowRect.getWidth() : windowRect.getHeight();
		double visibleMillis = (timeAxis.getMaxVal() - timeAxis.getMinVal()) * 1000.;
		if (timePixels <= 0 || visibleMillis <= 0) {
			return;
		}
		double pixPerMillis = timePixels / visibleMillis;
		boolean wrap = wrapPix >= 0;

		synchronized (tileLock) {
			double drawStart = scrollStart;
			double drawEnd = scrollStart + visibleMillis;

			/*
			 * Tiles built at different resolutions can be shown at the same time (for a
			 * moment after the display is zoomed in, when the old data cannot be refined).
			 * Draw them oldest first so that the newest data ends up on top.
			 */
			List<WaveTile> visible = new ArrayList<>();
			if (!tiles.isEmpty()) {
				for (WaveTile tile : tiles.subMap(drawStart - maxTileSpan, true, drawEnd, false).values()) {
					if (!tile.hasData() || tile.endMillis() <= drawStart || tile.startMillis >= drawEnd) {
						continue;
					}
					visible.add(tile);
				}
			}
			if (!visible.isEmpty()) {
				visible.sort(Comparator.comparingLong((WaveTile tile) -> tile.generation));

				g2d.setStroke(lineColor);
				g2d.setFill(lineColor);
				g2d.setLineWidth(lineWidth);

				for (WaveTile tile : visible) {
					drawTile(g2d, tile, orientation, dataAxis, scrollStart, drawEnd, pixPerMillis, timePixels, wrapPix);
				}
			}
		}

		if (wrap) {
			g2d.setStroke(Color.RED);
			g2d.setLineWidth(1);
			if (orientation == Orientation.HORIZONTAL) {
				g2d.strokeLine(wrapPix, 0, wrapPix, windowRect.getHeight());
			}
			else {
				g2d.strokeLine(0, wrapPix, windowRect.getWidth(), wrapPix);
			}
		}
	}

	/**
	 * Draw a single tile.
	 */
	private void drawTile(GraphicsContext g2d, WaveTile tile, Orientation orientation, PamAxisFX dataAxis,
			double scrollStart, double drawEnd, double pixPerMillis, double timePixels, double wrapPix) {

		boolean wrap = wrapPix >= 0;
		double pixPerCell = tile.cellMillis * pixPerMillis;

		int c0 = 0;
		int c1 = tile.nCells;
		if (!wrap) {
			c0 = Math.max(0, (int) Math.floor((scrollStart - tile.startMillis) / tile.cellMillis) - 1);
			c1 = Math.min(tile.nCells, (int) Math.ceil((drawEnd - tile.startMillis) / tile.cellMillis) + 1);
		}

		if (pixPerCell >= 1) {
			drawTileCells(g2d, tile, orientation, dataAxis, scrollStart, pixPerMillis, timePixels, wrapPix, c0, c1,
					pixPerCell);
		}
		else {
			drawTileEnvelope(g2d, tile, orientation, dataAxis, scrollStart, pixPerMillis, timePixels, wrapPix, c0, c1);
		}
	}

	/**
	 * Draw a tile which is spread over more pixels than it has cells. If the tile
	 * holds individual samples the samples are joined up with lines; if it is a
	 * coarser tile being shown zoomed in then each cell is drawn as a min/max band
	 * so that the display shows the range of the data rather than inventing a
	 * waveform which was never measured.
	 */
	private void drawTileCells(GraphicsContext g2d, WaveTile tile, Orientation orientation, PamAxisFX dataAxis,
			double scrollStart, double pixPerMillis, double timePixels, double wrapPix, int c0, int c1,
			double pixPerCell) {

		boolean samples = tile.samplesPerCell == 1;
		double prevPix = Double.NaN;
		double prevVal = 0;

		if (samples && c0 == 0) {
			/*
			 * The first cell of the tile has to be joined to the last cell of the tile
			 * before it, or there is a gap in the waveform at every tile boundary.
			 */
			WaveTile prev = precedingTile(tile);
			if (prev != null) {
				for (int c = prev.nCells - 1; c >= 0; c--) {
					if (prev.written[c]) {
						prevPix = timeToPixel(prev.startMillis + (c + 0.5) * prev.cellMillis, scrollStart, pixPerMillis,
								timePixels, wrapPix);
						prevVal = dataAxis.getPosition(prev.maxVals[c]);
						break;
					}
				}
			}
		}

		for (int c = c0; c < c1; c++) {
			if (!tile.written[c]) {
				prevPix = Double.NaN;
				continue;
			}
			double cellStart = tile.startMillis + c * tile.cellMillis;
			double pix = timeToPixel(cellStart + 0.5 * tile.cellMillis, scrollStart, pixPerMillis, timePixels, wrapPix);

			if (samples) {
				double val = dataAxis.getPosition(tile.maxVals[c]);
				if (!Double.isNaN(prevPix) && pix >= prevPix) {
					strokeLine(g2d, orientation, prevPix, prevVal, pix, val);
				}
				else {
					// first point, or the display has wrapped round - just mark the point so
					// that a single sample is still visible.
					fillSpan(g2d, orientation, pix, val, val);
				}
				prevPix = pix;
				prevVal = val;
			}
			else {
				double y1 = dataAxis.getPosition(tile.minVals[c]);
				double y2 = dataAxis.getPosition(tile.maxVals[c]);
				fillBand(g2d, orientation, pix - pixPerCell / 2., pixPerCell, y1, y2);
			}
		}
	}

	/**
	 * Draw a tile which has more than one cell per pixel. The min/max of all the
	 * cells falling in a pixel is drawn as a single vertical line, which is the
	 * standard way of showing a waveform envelope and, unlike drawing every cell,
	 * cannot produce aliasing artefacts.
	 * <p>
	 * The last cell of the previous pixel is included in each pixel's range. Without
	 * that, a display with only a few samples per pixel draws a row of disconnected
	 * dashes rather than a waveform, since each pixel only covers a small part of a
	 * cycle and its dash need not touch its neighbour's. The waveform really does
	 * pass through that range in that pixel's time, so nothing is over-stated.
	 */
	private void drawTileEnvelope(GraphicsContext g2d, WaveTile tile, Orientation orientation, PamAxisFX dataAxis,
			double scrollStart, double pixPerMillis, double timePixels, double wrapPix, int c0, int c1) {

		int currentPix = Integer.MIN_VALUE;
		float pixMin = 0;
		float pixMax = 0;
		boolean any = false;

		// the previous cell, which may be the last cell of the previous tile.
		float lastMin = 0;
		float lastMax = 0;
		boolean haveLast = false;
		if (c0 == 0) {
			WaveTile prev = precedingTile(tile);
			if (prev != null) {
				for (int c = prev.nCells - 1; c >= 0; c--) {
					if (prev.written[c]) {
						lastMin = prev.minVals[c];
						lastMax = prev.maxVals[c];
						haveLast = true;
						break;
					}
				}
			}
		}

		for (int c = c0; c < c1; c++) {
			if (!tile.written[c]) {
				haveLast = false;
				continue;
			}
			double cellCentre = tile.startMillis + (c + 0.5) * tile.cellMillis;
			int pix = (int) Math.floor(timeToPixel(cellCentre, scrollStart, pixPerMillis, timePixels, wrapPix));
			if (pix != currentPix) {
				if (any) {
					fillSpan(g2d, orientation, currentPix, dataAxis.getPosition(pixMin), dataAxis.getPosition(pixMax));
				}
				currentPix = pix;
				pixMin = haveLast ? Math.min(lastMin, tile.minVals[c]) : tile.minVals[c];
				pixMax = haveLast ? Math.max(lastMax, tile.maxVals[c]) : tile.maxVals[c];
				any = true;
			}
			else {
				pixMin = Math.min(pixMin, tile.minVals[c]);
				pixMax = Math.max(pixMax, tile.maxVals[c]);
			}
			lastMin = tile.minVals[c];
			lastMax = tile.maxVals[c];
			haveLast = true;
		}
		if (any) {
			fillSpan(g2d, orientation, currentPix, dataAxis.getPosition(pixMin), dataAxis.getPosition(pixMax));
		}
	}

	/**
	 * Find the tile which runs up to the start of the given tile at the same
	 * resolution, i.e. the tile holding the data immediately before it.
	 *
	 * @param tile - the tile to look back from.
	 * @return the preceding tile, or null if the data are not continuous.
	 */
	private WaveTile precedingTile(WaveTile tile) {
		Map.Entry<Double, WaveTile> entry = tiles.lowerEntry(tile.startMillis);
		while (entry != null) {
			WaveTile other = entry.getValue();
			if (other.endMillis() < tile.startMillis - tile.cellMillis) {
				return null; // everything from here back is too old to join up.
			}
			if (other.samplesPerCell == tile.samplesPerCell
					&& Math.abs(other.endMillis() - tile.startMillis) < other.cellMillis) {
				return other;
			}
			entry = tiles.lowerEntry(other.startMillis);
		}
		return null;
	}

	/**
	 * Convert a time to a position along the time axis in pixels.
	 */
	private double timeToPixel(double timeMillis, double scrollStart, double pixPerMillis, double timePixels,
			double wrapPix) {
		double pix = (timeMillis - scrollStart) * pixPerMillis;
		if (wrapPix >= 0) {
			pix += wrapPix;
			pix = pix % timePixels;
			if (pix < 0) {
				pix += timePixels;
			}
		}
		return pix;
	}

	/**
	 * Draw a one pixel wide vertical span of the waveform envelope.
	 *
	 * @param timePix - position along the time axis.
	 * @param d1      - one end of the span on the data axis.
	 * @param d2      - the other end of the span on the data axis.
	 */
	private void fillSpan(GraphicsContext g2d, Orientation orientation, double timePix, double d1, double d2) {
		fillBand(g2d, orientation, timePix, Math.max(lineWidth, 1), d1, d2);
	}

	/**
	 * Draw a band of the waveform, i.e. a rectangle spanning a range of time and a
	 * range of amplitude. A minimum height of one pixel is used so that a flat
	 * (silent) waveform still shows as a line.
	 */
	private void fillBand(GraphicsContext g2d, Orientation orientation, double timePix, double timeWidth, double d1,
			double d2) {
		double dLow = Math.min(d1, d2);
		double dHigh = Math.max(d1, d2);
		double height = dHigh - dLow;
		if (height < 1) {
			dLow = (dLow + dHigh - 1) / 2.;
			height = 1;
		}
		double width = Math.max(timeWidth, 1);
		if (orientation == Orientation.HORIZONTAL) {
			g2d.fillRect(timePix, dLow, width, height);
		}
		else {
			g2d.fillRect(dLow, timePix, height, width);
		}
	}

	/**
	 * Stroke a line between two points given in (time axis, data axis) coordinates.
	 */
	private void strokeLine(GraphicsContext g2d, Orientation orientation, double t1, double d1, double t2, double d2) {
		if (orientation == Orientation.HORIZONTAL) {
			g2d.strokeLine(t1, d1, t2, d2);
		}
		else {
			g2d.strokeLine(d1, t1, d2, t2);
		}
	}

	/* ===================== clearing ===================== */

	/**
	 * Reset the raw sound data for loading.
	 */
	public void resetForLoad() {
		clearRawData();
	}

	/**
	 * Clear all stored data.
	 */
	public void clearRawData() {
		synchronized (tileLock) {
			clearStore();
		}
	}

	/**
	 * Clear the store. Must be called with {@link #tileLock} held.
	 */
	private void clearStore() {
		tiles.clear();
		loadedIntervals.clear();
		totalCells = 0;
		maxTileSpan = 0;
		cursorTile = null;
		cursorCount = 0;
		cellHasVal = false;
		nextExpectedMillis = Double.NaN;
	}

	/**
	 * Get the number of samples summarised into each stored value.
	 *
	 * @return the number of samples per stored bin.
	 */
	public double getBinsPerPixel() {
		return samplesPerCell;
	}

}
