package annotationMark.fx;

import PamDetection.AbstractLocalisation;
import PamView.GeneralProjector.ParameterType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import annotationMark.spectrogram.AnnotationHandleState;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDGraphFX.TDPlotPane;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import pamViewFX.fxNodes.PamSymbolFX;

/**
 * Plot info for annotation marks on an FX time display. In addition to the
 * standard frequency box drawn by {@link GenericDataPlotInfo}, this draws
 * interactive resize handles (small squares at the four corners and the middle
 * of each edge), a move symbol in the centre of a box when the mouse hovers over
 * it, and live measurement text inside a box while it is being moved or resized.
 * The handle under the mouse is highlighted on hover to show it can be grabbed.
 *
 * @author Jamie Macaulay
 */
public class MarkDataPlotInfo extends GenericDataPlotInfo {

	/** Size, in pixels, of the little square resize handles. */
	private static final double HANDLE_SIZE = 8;

	/** Pixel tolerance for grabbing an edge / handle. */
	private static final double HANDLE_TOL = 8;

	/** The data unit the mouse is currently hovering over (may be null). */
	private volatile PamDataUnit hoveredUnit;

	/**
	 * Which of the 8 handles (0-7, see the handle array in {@link #drawHandles}) the
	 * mouse is over, or -1 for the body (move) / none.
	 */
	private volatile int hoveredHandle = -1;

	/** True once the hover mouse handlers have been registered on the graph. */
	private boolean hoverRegistered = false;

	/** Duration, in milliseconds, over which the handles fade out once the mouse leaves. */
	private static final double FADE_MILLIS = 2000;

	/** The box whose handles are currently fading out (may be null). */
	private volatile PamDataUnit fadingUnit;

	/** {@link System#nanoTime()} at which the current fade started. */
	private long fadeStartNanos;

	/** Animation timer that drives repaints while the handles fade out. */
	private AnimationTimer fadeTimer;

	private final Font measurementFont = Font.font("SansSerif", 11);

	public MarkDataPlotInfo(MarkPlotProviderFX tdDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
	}

	public Double getBearingValue(PamDataUnit pamDataUnit) {
		AbstractLocalisation locData = pamDataUnit.getLocalisation();
		if (locData == null) {
			return null;
		}
		double[] angles = locData.getAngles();
		if (angles != null) {
			return Math.toDegrees(angles[0]);
		}

		return null;
	}

	/**
	 * In addition to the normal highlight drawing, draw the interactive handles for
	 * the hovered and/or currently-edited annotation box. These are drawn on the
	 * highlight canvas (which repaints cheaply) so hover and drag feedback is
	 * smooth. Only boxes that belong on this plot panel (channel) are drawn.
	 */
	@Override
	public void drawHighLightData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		super.drawHighLightData(plotNumber, g, scrollStart, tdProjector);

		ensureHoverRegistered();

		if (getCurrentScaleInfo() == null || getCurrentScaleInfo().getDataType() != ParameterType.FREQUENCY) {
			return;
		}

		PamDataUnit hover = hoveredUnit;
		if (hover != null && hover.getParentDataBlock() == getDataBlock() && shouldDraw(plotNumber, hover)) {
			drawHandles(hover, g, scrollStart, tdProjector, true, 1.0);
		}
		// A box the mouse has just left fades its handles out over a few seconds.
		PamDataUnit fading = fadingUnit;
		if (fading != null && fading != hover && fading.getParentDataBlock() == getDataBlock()
				&& shouldDraw(plotNumber, fading)) {
			double opacity = fadeOpacity();
			if (opacity > 0) {
				drawHandles(fading, g, scrollStart, tdProjector, true, opacity);
			}
		}
		// The box being dragged may not be the hovered one (fast drags): make sure its
		// handles and live measurement text are always shown.
		PamDataBlock<?> dataBlock = getDataBlock();
		if (dataBlock != null) {
			synchronized (dataBlock.getSynchLock()) {
				for (int i = 0; i < dataBlock.getUnitsCount(); i++) {
					PamDataUnit unit = dataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
					if (unit != hover && AnnotationHandleState.isEditing(unit) && shouldDraw(plotNumber, unit)) {
						drawHandles(unit, g, scrollStart, tdProjector, false, 1.0);
					}
				}
			}
		}
	}

	/**
	 * @return the current fade opacity (1 at the start of the fade, 0 once
	 *         {@link #FADE_MILLIS} has elapsed).
	 */
	private double fadeOpacity() {
		double elapsed = (System.nanoTime() - fadeStartNanos) / 1e6;
		return Math.max(0, 1 - elapsed / FADE_MILLIS);
	}

	/**
	 * Draw the resize handles (and optionally the move symbol / measurement text)
	 * for a single annotation box.
	 *
	 * @param unit        the annotation data unit.
	 * @param g           the graphics context (highlight canvas).
	 * @param scrollStart the display scroll start in millis.
	 * @param tdProjector the graph projector.
	 * @param showMove    true to draw the move symbol in the centre (mouse is over
	 *                    this box).
	 * @param opacity     overall opacity (0-1) - used to fade the handles out once
	 *                    the mouse leaves the box.
	 */
	private void drawHandles(PamDataUnit unit, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector,
			boolean showMove, double opacity) {
		double[] f = unit.getFrequency();
		if (f == null || f.length < 2) {
			return;
		}
		double[] box = boxPixels(unit, f, scrollStart, tdProjector);
		double xL = box[0], xR = box[1], yT = box[2], yB = box[3];
		if (xR - xL < 2 || yB - yT < 2) {
			return; // too small to bother.
		}

		PamSymbolFX symbol = getSymbolChooser() == null ? null
				: getSymbolChooser().getPamSymbol(unit, TDSymbolChooserFX.NORMAL_SYMBOL);
		Color lineCol = fade(symbol == null ? Color.CYAN : symbol.getLineColor(), opacity);
		Color edgeCol = fade(Color.WHITE, opacity);

		double cx = (xL + xR) / 2;
		double cy = (yT + yB) / 2;
		double[][] handles = { { xL, yT }, { cx, yT }, { xR, yT }, { xR, cy }, { xR, yB }, { cx, yB }, { xL, yB },
				{ xL, cy } };

		int active = (unit == hoveredUnit) ? hoveredHandle : -1;
		g.setLineWidth(1);
		for (int i = 0; i < handles.length; i++) {
			double[] h = handles[i];
			if (i == active) {
				// highlight the handle the mouse is over: bigger and brighter so it is
				// obvious it can be grabbed for a drag.
				double sz = HANDLE_SIZE * 1.7;
				g.setFill(fade(lineCol.brighter().brighter(), opacity));
				g.fillRect(h[0] - sz / 2, h[1] - sz / 2, sz, sz);
				g.setStroke(edgeCol);
				g.setLineWidth(2);
				g.strokeRect(h[0] - sz / 2, h[1] - sz / 2, sz, sz);
				g.setLineWidth(1);
			}
			else {
				g.setFill(lineCol);
				g.fillRect(h[0] - HANDLE_SIZE / 2, h[1] - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
				g.setStroke(edgeCol);
				g.strokeRect(h[0] - HANDLE_SIZE / 2, h[1] - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
			}
		}

		if (showMove) {
			drawMoveSymbol(g, cx, cy, lineCol);
		}

		if (AnnotationHandleState.isEditing(unit)) {
			// contrast the text against the colour the box is actually drawn in while it is
			// highlighted / being dragged.
			Color highlightFill = symbol == null ? lineCol
					: getSymbolChooser().getPamSymbol(unit, TDSymbolChooserFX.HIGHLIGHT_SYMBOL).getFillColor();
			drawMeasurementText(g, xL, yT, xR, yB, highlightFill);
		}
	}

	/**
	 * Compute the pixel bounds of an annotation box as {xLeft, xRight, yTop,
	 * yBottom}.
	 */
	private double[] boxPixels(PamDataUnit unit, double[] f, double scrollStart, TDProjectorFX tdProjector) {
		double y0 = tdProjector.getYPix(f[0]);
		double y1 = tdProjector.getYPix(f[1]);
		double x0 = tdProjector.getTimePix(unit.getTimeMilliseconds() - scrollStart);
		double x1 = tdProjector.getTimePix(unit.getEndTimeInMilliseconds() - scrollStart);
		return new double[] { Math.min(x0, x1), Math.max(x0, x1), Math.min(y0, y1), Math.max(y0, y1) };
	}

	/**
	 * Draw a four-way move symbol (a cross with arrow heads) centred at (cx, cy).
	 */
	private void drawMoveSymbol(GraphicsContext g, double cx, double cy, Color col) {
		double a = 9; // arm length
		double head = 3; // arrow head size
		g.setStroke(col);
		g.setLineWidth(2);
		g.strokeLine(cx - a, cy, cx + a, cy);
		g.strokeLine(cx, cy - a, cx, cy + a);
		// arrow heads
		g.strokeLine(cx - a, cy, cx - a + head, cy - head);
		g.strokeLine(cx - a, cy, cx - a + head, cy + head);
		g.strokeLine(cx + a, cy, cx + a - head, cy - head);
		g.strokeLine(cx + a, cy, cx + a - head, cy + head);
		g.strokeLine(cx, cy - a, cx - head, cy - a + head);
		g.strokeLine(cx, cy - a, cx + head, cy - a + head);
		g.strokeLine(cx, cy + a, cx - head, cy + a - head);
		g.strokeLine(cx, cy + a, cx + head, cy + a - head);
	}

	/**
	 * Draw the live measurement text inside a box while it is being edited. The text
	 * colour is derived from the (highlighted) box colour but made brighter or
	 * darker so it stays legible against it.
	 */
	private void drawMeasurementText(GraphicsContext g, double xL, double yT, double xR, double yB, Color boxCol) {
		String[] lines = AnnotationHandleState.getEditingText();
		if (lines == null || lines.length == 0) {
			return;
		}
		g.setFill(contrastingText(boxCol));
		g.setFont(measurementFont);
		g.setTextAlign(TextAlignment.LEFT);
		double lineHeight = 13;
		double y = yT + lineHeight + 2;
		double x = xL + 4;
		for (String line : lines) {
			if (y > yB - 2) {
				break; // don't spill out of the box.
			}
			g.fillText(line, x, y);
			y += lineHeight;
		}
	}

	/**
	 * Make a legible text colour from a (possibly translucent) box colour: take the
	 * colour's hue at full opacity, then brighten it if it is dark or darken it if
	 * it is bright.
	 */
	private Color contrastingText(Color boxCol) {
		Color opaque = new Color(boxCol.getRed(), boxCol.getGreen(), boxCol.getBlue(), 1.0);
		double lum = 0.2126 * opaque.getRed() + 0.7152 * opaque.getGreen() + 0.0722 * opaque.getBlue();
		if (lum > 0.5) {
			return opaque.darker().darker();
		}
		return opaque.brighter().brighter();
	}

	/**
	 * Apply an overall opacity to a colour (used to fade the handles out).
	 */
	private Color fade(Color col, double opacity) {
		if (opacity >= 1) {
			return col;
		}
		return col.deriveColor(0, 1, 1, Math.max(0, opacity));
	}

	/**
	 * Start fading the handles of a box out over {@link #FADE_MILLIS}, driving
	 * repaints with an animation timer.
	 */
	private void startFade(PamDataUnit unit) {
		fadingUnit = unit;
		fadeStartNanos = System.nanoTime();
		ensureFadeTimer();
		fadeTimer.start();
	}

	/**
	 * Cancel any in-progress fade (e.g. the mouse re-entered the box).
	 */
	private void cancelFade() {
		fadingUnit = null;
		if (fadeTimer != null) {
			fadeTimer.stop();
		}
	}

	private void ensureFadeTimer() {
		if (fadeTimer != null) {
			return;
		}
		fadeTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				TDGraphFX graph = getTDGraph();
				if (fadingUnit == null || graph == null) {
					stop();
					return;
				}
				if (fadeOpacity() <= 0) {
					fadingUnit = null;
					stop();
				}
				graph.repaintMarks();
			}
		};
	}

	/**
	 * Register a hover mouse handler on each plot pane (once), so we know which
	 * annotation box the mouse is over and can draw the move symbol / handles and
	 * highlight the handle under the mouse.
	 */
	private void ensureHoverRegistered() {
		if (hoverRegistered || getTDGraph() == null) {
			return;
		}
		java.util.List<TDPlotPane> panes = getTDGraph().getPlotPanes();
		if (panes == null || panes.isEmpty()) {
			return; // panes not laid out yet - try again on the next repaint.
		}
		for (int i = 0; i < panes.size(); i++) {
			panes.get(i).addMouseHandler(new HoverHandler(i));
		}
		hoverRegistered = true;
	}

	/**
	 * Mouse handler which tracks which annotation box - and which of its handles -
	 * the mouse is over on a single plot panel, so they can be highlighted.
	 */
	private class HoverHandler extends ExtMouseAdapter {

		private final int plotNumber;

		HoverHandler(int plotNumber) {
			this.plotNumber = plotNumber;
		}

		@Override
		public boolean mouseMoved(MouseEvent e) {
			updateHover(e.getX(), e.getY());
			return false;
		}

		@Override
		public boolean mouseExited(MouseEvent e) {
			if (hoveredUnit != null) {
				startFade(hoveredUnit); // fade the handles out rather than snapping off.
				hoveredUnit = null;
				hoveredHandle = -1;
				getTDGraph().repaintMarks();
			}
			return false;
		}

		private void updateHover(double px, double py) {
			TDGraphFX graph = getTDGraph();
			if (graph == null || getCurrentScaleInfo() == null
					|| getCurrentScaleInfo().getDataType() != ParameterType.FREQUENCY) {
				return;
			}
			TDProjectorFX projector = graph.getGraphProjector();
			double scrollStart = graph.getScrollStart();
			PamDataUnit found = findUnitAt(px, py, scrollStart, projector);
			// track which box / handle the mouse is over so the handle can be highlighted.
			int handle = (found == null) ? -1 : handleAt(found, px, py, scrollStart, projector);
			if (found != null && found == fadingUnit) {
				cancelFade(); // re-entered a box mid-fade - snap it back to full strength.
			}
			if (found != hoveredUnit || handle != hoveredHandle) {
				PamDataUnit prev = hoveredUnit;
				hoveredUnit = found;
				hoveredHandle = handle;
				if (prev != null && prev != found) {
					startFade(prev); // mouse left this box (to empty space or another box) - fade it out.
				}
				graph.repaintMarks();
			}
		}

		/**
		 * Find the top-most box on this panel whose bounds (grown to include the edge
		 * handles) contain the point.
		 */
		private PamDataUnit findUnitAt(double px, double py, double scrollStart, TDProjectorFX projector) {
			PamDataBlock<?> dataBlock = getDataBlock();
			if (dataBlock == null) {
				return null;
			}
			PamDataUnit found = null;
			synchronized (dataBlock.getSynchLock()) {
				for (int i = 0; i < dataBlock.getUnitsCount(); i++) {
					PamDataUnit unit = dataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
					double[] f = unit.getFrequency();
					if (f == null || f.length < 2 || !shouldDraw(plotNumber, unit)) {
						continue;
					}
					double[] box = boxPixels(unit, f, scrollStart, projector);
					if (px >= box[0] - HANDLE_TOL && px <= box[1] + HANDLE_TOL && py >= box[2] - HANDLE_TOL
							&& py <= box[3] + HANDLE_TOL) {
						found = unit; // keep scanning: later units are drawn on top.
					}
				}
			}
			return found;
		}
	}

	/**
	 * Work out which of the 8 handles (0-7, matching the handle array in
	 * {@link #drawHandles}) the mouse is over, or -1 for the box body (move).
	 */
	private int handleAt(PamDataUnit unit, double px, double py, double scrollStart, TDProjectorFX projector) {
		double[] f = unit.getFrequency();
		double[] box = boxPixels(unit, f, scrollStart, projector);
		boolean left = Math.abs(px - box[0]) <= HANDLE_TOL;
		boolean right = Math.abs(px - box[1]) <= HANDLE_TOL;
		boolean top = Math.abs(py - box[2]) <= HANDLE_TOL;
		boolean bottom = Math.abs(py - box[3]) <= HANDLE_TOL;
		if (top && left) return 0;
		if (top && right) return 2;
		if (bottom && left) return 6;
		if (bottom && right) return 4;
		if (top) return 1;
		if (right) return 3;
		if (bottom) return 5;
		if (left) return 7;
		return -1; // inside the body - move.
	}
}
