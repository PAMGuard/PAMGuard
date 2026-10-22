package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import java.util.ArrayList;
import java.util.List;

import PamView.ColourScheme;
import PamView.PamColors;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 * Draws the stretches of a data stream for which there is <em>any</em> data at all
 * as plain blocks of colour on the acoustic scroll bar.
 * <p>
 * Building the scroll bar's spectrogram preview means reading and FFT-ing every
 * sound file in the loaded range, which for a folder of sud files takes a while.
 * Until that finishes the scroll bar is blank, and with duty cycled (or otherwise
 * non continuous) recordings that is exactly when the user most needs to know where
 * the recordings are - there is no way to navigate to a recording you cannot see.
 * <p>
 * The blocks come instead from the source's offline data map, which is built when
 * the viewer opens the dataset and simply lists the start and end time of every
 * file, so drawing them costs nothing and needs no data loading. The preview then
 * draws over the top of them as it loads, block by block.
 *
 * @author Jamie Macaulay
 */
public class DataAvailabilityBlocks {

	/**
	 * Maximum number of steps up the data block chain when looking for a data map, so
	 * that a (pathological) cyclic chain cannot hang the draw.
	 */
	private static final int MAX_PARENT_STEPS = 20;

	/**
	 * Smallest contrast ratio (the usual 1:1 to 21:1 measure) between the blocks and
	 * the scroll bar background at which the blocks are still clearly visible. Below
	 * this the colour map's base colour is swapped for a neutral grey. The case that
	 * matters is a colour map which starts at black - hot, grey, and most of the
	 * others - shown in a dark colour scheme: black on the dark scheme's #2b2b2b is a
	 * ratio of only 1.5, which is far too faint to navigate by.
	 */
	private static final double MIN_CONTRAST_RATIO = 2.0;

	/** Relative luminance above which a background counts as a light one. */
	private static final double LIGHT_BACKGROUND_LUMINANCE = 0.18;

	/** Block colour used when the colour map's own base colour would be invisible. */
	private static final Color LIGHT_MODE_FALLBACK = Color.gray(0.65);

	/** Block colour used when the colour map's own base colour would be invisible. */
	private static final Color DARK_MODE_FALLBACK = Color.gray(0.45);

	/** The block whose source data availability is drawn. */
	private PamDataBlock dataBlock;

	/** The scroller the blocks are drawn on. */
	private AcousticScrollerFX acousticScroller;

	/** Data map the cached intervals came from, so a change of map invalidates them. */
	private OfflineDataMap<?> cachedMap;

	/** Number of map points the cached intervals were built from. */
	private int cachedNumPoints = -1;

	/** Time range the cached intervals were built for. */
	private long cachedStart, cachedEnd;

	/** Millisecond tolerance the cached intervals were merged with. */
	private long cachedMergeMillis = -1;

	/** The merged available intervals, as {start, end} pairs. */
	private List<long[]> cachedIntervals;

	/**
	 * @param acousticScroller - the scroller the blocks are drawn on.
	 * @param dataBlock - the block being displayed. The data map actually used is the
	 * first one found on it or on one of its parents (see {@link #findDataMap()}).
	 */
	public DataAvailabilityBlocks(AcousticScrollerFX acousticScroller, PamDataBlock dataBlock) {
		this.acousticScroller = acousticScroller;
		this.dataBlock = dataBlock;
	}

	/**
	 * Draw the blocks for the currently displayed time range. Does nothing if no data
	 * map can be found, which is the normal state in real time operation.
	 *
	 * @param g2d - the scroll bar canvas graphics context.
	 * @param widthPixels - the canvas width in pixels.
	 * @param heightPixels - the canvas height in pixels.
	 * @param colours - the colour map the blocks should match, or null to use a
	 * neutral grey.
	 */
	public void draw(GraphicsContext g2d, double widthPixels, double heightPixels, Plot2DColours colours) {
		if (widthPixels <= 0 || heightPixels <= 0) {
			return;
		}
		boolean vertical = acousticScroller.getOrientation() == Orientation.VERTICAL;
		double timePixels = vertical ? heightPixels : widthPixels;
		double visibleMillis = (acousticScroller.getTimeAxis().getMaxVal()
				- acousticScroller.getTimeAxis().getMinVal()) * 1000.;
		if (visibleMillis <= 0) {
			return;
		}
		long scrollStart = acousticScroller.getMinimumMillis();
		long scrollEnd = scrollStart + (long) visibleMillis;
		//merge anything closer together than a pixel - blocks a pixel apart are drawn as
		//one anyway, and merging first keeps the number of draw calls down on a dataset
		//of many thousands of short files.
		List<long[]> intervals = getAvailableIntervals(scrollStart, scrollEnd,
				Math.max(1, (long) (visibleMillis / timePixels)));
		if (intervals == null || intervals.isEmpty()) {
			return;
		}

		drawBlocks(g2d, intervals, scrollStart, visibleMillis, widthPixels, heightPixels, vertical,
				getBlockColour(colours));
	}

	/**
	 * Fill the given time intervals as blocks spanning the whole width (or, on a
	 * vertical scroller, height) of the scroll bar. Uses the same time to pixel mapping
	 * as {@code Scrolling2DPlotDataFX2.drawSpectrogram}, so the blocks line up exactly
	 * with the preview which draws over them.
	 *
	 * @param g2d - the scroll bar canvas graphics context.
	 * @param intervals - the intervals to fill, as {start, end} pairs in millis.
	 * @param scrollStart - the time at the start of the scroll bar, in millis.
	 * @param visibleMillis - the time span of the whole scroll bar, in millis.
	 * @param widthPixels - the canvas width in pixels.
	 * @param heightPixels - the canvas height in pixels.
	 * @param vertical - true if the scroller runs down the screen rather than across.
	 * @param colour - the colour to fill with.
	 */
	static void drawBlocks(GraphicsContext g2d, List<long[]> intervals, long scrollStart, double visibleMillis,
			double widthPixels, double heightPixels, boolean vertical, Color colour) {
		double timePixels, blockTop, blockDepth;
		g2d.save();
		if (vertical) {
			timePixels = heightPixels;
			/*
			 * Exactly the transform the spectrogram sets up for a vertical scroller, so that
			 * the blocks land where the preview drawn over them lands - including the
			 * direction time runs in, which the rotation reverses. Working in the rotated
			 * frame rather than just swapping x and y is what keeps the two in step.
			 */
			Affine at = new Affine();
			at.append(Transform.rotate(-90, widthPixels / 2., heightPixels / 2.));
			at.append(Transform.translate((widthPixels - heightPixels) / 2., (widthPixels + heightPixels) / 2.));
			g2d.setTransform(at);
			//the spectrogram images are drawn with a negative height (which flips them), so
			//in this frame the scroll bar occupies -width to 0.
			blockTop = -widthPixels;
			blockDepth = widthPixels;
		}
		else {
			timePixels = widthPixels;
			blockTop = 0;
			blockDepth = heightPixels;
		}
		double scale = timePixels / visibleMillis;
		g2d.setFill(colour);
		for (long[] interval : intervals) {
			double p1 = (interval[0] - scrollStart) * scale;
			double p2 = (interval[1] - scrollStart) * scale;
			if (p2 < 0 || p1 > timePixels) {
				continue;
			}
			//a file which is short compared with the time per pixel must still show as
			//something, or a heavily duty cycled dataset draws nothing at all. Clamp after
			//taking the length so that a block running off the end keeps its minimum width.
			double len = Math.max(1, Math.min(p2, timePixels) - Math.max(p1, 0));
			p1 = Math.max(0, Math.min(p1, timePixels - len));
			g2d.fillRect(p1, blockTop, len, blockDepth);
		}
		g2d.restore();
	}

	/**
	 * The stretches of time within the given range for which data exist, merged
	 * together where they are closer than <code>mergeMillis</code> apart. The result is
	 * cached, since the data map only changes when a new dataset is opened.
	 *
	 * @param rangeStart - start of the range of interest in millis.
	 * @param rangeEnd - end of the range of interest in millis.
	 * @param mergeMillis - gaps shorter than this are absorbed into the blocks.
	 * @return the available intervals as {start, end} pairs, or null if unknown.
	 */
	public synchronized List<long[]> getAvailableIntervals(long rangeStart, long rangeEnd, long mergeMillis) {
		OfflineDataMap<?> dataMap = findDataMap();
		if (dataMap == null) {
			cachedIntervals = null;
			cachedMap = null;
			return null;
		}
		int nPoints = dataMap.getNumMapPoints();
		if (cachedIntervals != null && dataMap == cachedMap && nPoints == cachedNumPoints
				&& rangeStart == cachedStart && rangeEnd == cachedEnd && mergeMillis == cachedMergeMillis) {
			return cachedIntervals;
		}

		List<long[]> intervals;
		synchronized (dataMap) {
			intervals = mergeIntervals(dataMap.getMapPoints(rangeStart, rangeEnd), mergeMillis);
		}

		cachedMap = dataMap;
		cachedNumPoints = nPoints;
		cachedStart = rangeStart;
		cachedEnd = rangeEnd;
		cachedMergeMillis = mergeMillis;
		cachedIntervals = intervals;
		return intervals;
	}

	/**
	 * Turn a list of data map points into a list of time intervals, running together
	 * any which are closer than <code>mergeMillis</code> apart. Merging matters as much
	 * for tidiness as for speed: sound files are typically a few minutes long, so a day
	 * of continuous recording is hundreds of map points which must be drawn as one
	 * unbroken block rather than hundreds of abutting ones.
	 *
	 * @param points - the map points, in time order. May be null.
	 * @param mergeMillis - gaps shorter than this are absorbed into the blocks.
	 * @return the merged intervals as {start, end} pairs, never null.
	 */
	static List<long[]> mergeIntervals(List<? extends OfflineDataMapPoint> points, long mergeMillis) {
		List<long[]> intervals = new ArrayList<long[]>();
		if (points == null) {
			return intervals;
		}
		long[] current = null;
		for (OfflineDataMapPoint point : points) {
			long start = point.getStartTime();
			//a map point with no known end (or a nonsense one) still marks a moment which
			//holds data, so give it zero length rather than dropping it.
			long end = Math.max(point.getEndTime(), start);
			if (current != null && start <= current[1] + mergeMillis) {
				current[1] = Math.max(current[1], end);
			}
			else {
				current = new long[] { start, end };
				intervals.add(current);
			}
		}
		return intervals;
	}

	/**
	 * Find the data map which says where there are data. FFT data are not themselves
	 * stored, so the map comes from further up the chain - normally the sound file map
	 * on the acquisition's raw data block, which lists every file and its times. Walk
	 * up rather than jumping straight to the raw source, since a chain containing e.g.
	 * a decimator has raw data blocks in it which have no map of their own.
	 *
	 * @return a data map with at least one point in it, or null if there is none.
	 */
	private OfflineDataMap<?> findDataMap() {
		PamDataBlock block = dataBlock;
		for (int i = 0; i < MAX_PARENT_STEPS && block != null; i++) {
			OfflineDataMap<?> map = block.getPrimaryDataMap();
			if (map != null && map.getNumMapPoints() > 0) {
				return map;
			}
			PamProcess parentProcess = block.getParentProcess();
			block = parentProcess == null ? null : parentProcess.getParentDataBlock();
		}
		return null;
	}

	/**
	 * The colour to draw the blocks in. This is the base (lowest level) colour of the
	 * spectrogram colour map, so that the blocks look like empty spectrogram and the
	 * preview drawn over them does not appear to change colour as it loads. Where that
	 * colour would be invisible - a colour map starting at black on a dark background,
	 * or at white on a light one - a neutral grey is used instead.
	 *
	 * @param colours - the colour map, which may be null.
	 * @return the block colour.
	 */
	private Color getBlockColour(Plot2DColours colours) {
		return chooseBlockColour(getColourMapBase(colours), getBackgroundColour());
	}

	/**
	 * Pick the block colour given the colour map's base colour and the colour behind
	 * the scroll bar.
	 *
	 * @param base - the colour map's lowest colour, or null if not known.
	 * @param background - the colour behind the scroll bar canvas.
	 * @return the colour to draw the blocks in.
	 */
	static Color chooseBlockColour(Color base, Color background) {
		if (base != null && contrastRatio(base, background) >= MIN_CONTRAST_RATIO) {
			return base;
		}
		return luminance(background) > LIGHT_BACKGROUND_LUMINANCE ? LIGHT_MODE_FALLBACK : DARK_MODE_FALLBACK;
	}

	/**
	 * The lowest colour of the current colour map, i.e. the colour the spectrogram
	 * itself uses where there is no signal.
	 *
	 * @param colours - the colour map, which may be null.
	 * @return the base colour, or null if it cannot be determined.
	 */
	private Color getColourMapBase(Plot2DColours colours) {
		if (colours == null) {
			return null;
		}
		try {
			//anything below the bottom of the amplitude scale clamps to the first colour.
			return colours.getColours(-Double.MAX_VALUE);
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * The colour actually behind the scroll bar canvas. Read from the pane's resolved
	 * background rather than assumed from the colour scheme, so that it stays right
	 * whatever a theme's css says the plot background should be.
	 *
	 * @return the background colour.
	 */
	private Color getBackgroundColour() {
		Region pane = acousticScroller.getScrollBarPane();
		if (pane != null) {
			Background background = pane.getBackground();
			if (background != null && !background.getFills().isEmpty()) {
				//the last fill is the one drawn on top.
				BackgroundFill fill = background.getFills().get(background.getFills().size() - 1);
				Paint paint = fill.getFill();
				if (paint instanceof Color) {
					return (Color) paint;
				}
			}
		}
		//before the pane is styled (or if a theme uses a gradient) fall back on the scheme.
		ColourScheme scheme = PamColors.getInstance().getColourScheme();
		return scheme != null && scheme.isDark() ? Color.BLACK : Color.WHITE;
	}

	/**
	 * The contrast ratio between two colours, 1 (identical) to 21 (black on white).
	 * Used rather than a plain difference in brightness because the scroll bar
	 * backgrounds are near black in the dark schemes, where a small difference in
	 * brightness is a large - and a large one a small - difference in what can
	 * actually be seen.
	 *
	 * @param a - one colour.
	 * @param b - the other colour.
	 * @return the contrast ratio.
	 */
	private static double contrastRatio(Color a, Color b) {
		double la = luminance(a);
		double lb = luminance(b);
		return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
	}

	/**
	 * Relative luminance of a colour, 0 (black) to 1 (white).
	 * @param colour - the colour.
	 * @return the relative luminance.
	 */
	private static double luminance(Color colour) {
		return 0.2126 * linear(colour.getRed()) + 0.7152 * linear(colour.getGreen())
				+ 0.0722 * linear(colour.getBlue());
	}

	/**
	 * Remove the sRGB gamma from one colour channel.
	 * @param value - the channel value, 0 to 1.
	 * @return the linear channel value.
	 */
	private static double linear(double value) {
		return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
	}

}
