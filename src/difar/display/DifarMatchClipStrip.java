package difar.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.ColourArray;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamguardMVC.PamDataUnit;
import clipgenerator.clipDisplay.ClipDisplayParameters;
import difar.DifarControl;
import difar.DifarDataUnit;

/**
 * The clips being matched, shown side by side.
 * <p>
 * The seed clip comes first, then the clips of the match in use, then every
 * other candidate. Each section is named on a divider. Clips wrap onto further
 * rows as the panel narrows, and move between sections when a different match
 * is chosen.
 * <p>
 * Clips are drawn like those in the clip display: time, channel, duration and
 * top frequency around the edge of the spectrogram. A clip in the match in use
 * is framed in its channel colour, matching the map, and the rest are framed in
 * grey.
 */
public class DifarMatchClipStrip extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Height of a clip image in pixels. Widths follow the clip's duration. */
	private static final int CLIP_HEIGHT = 64;

	/** Pixels per second of clip, so clips of different lengths compare. */
	private static final int PIXELS_PER_SECOND = 12;

	private final DifarControl difarControl;

	/** Clip images already drawn, so scrolling and re-selecting are cheap. */
	private final Map<DifarDataUnit, BufferedImage> images = new HashMap<>();

	public DifarMatchClipStrip(DifarControl difarControl) {
		this.difarControl = difarControl;
		setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
	}

	/**
	 * Show the clips for a detection.
	 * @param seed the detection being matched, or null to clear the strip.
	 * @param matched the detections of the match in use. May be empty.
	 * @param candidates every candidate detection, matched or not.
	 */
	public void show(DifarDataUnit seed, List<PamDataUnit> matched, List<PamDataUnit> candidates) {
		removeAll();
		images.keySet().retainAll(keep(seed, candidates));
		if (seed != null) {
			add(new SectionDivider("Seed"));
			add(clipPanel(seed, true));
			add(new SectionDivider("In this match"));
			for (PamDataUnit unit : matched) {
				if (unit != seed) {
					add(clipPanel((DifarDataUnit) unit, true));
				}
			}
			add(new SectionDivider("Not used"));
			for (PamDataUnit unit : candidates) {
				if (unit != seed && !matched.contains(unit)) {
					add(clipPanel((DifarDataUnit) unit, false));
				}
			}
		}
		revalidate();
		repaint();
	}

	/** @return the units whose images are still worth keeping. */
	private List<DifarDataUnit> keep(DifarDataUnit seed, List<PamDataUnit> candidates) {
		List<DifarDataUnit> keep = new ArrayList<>();
		if (seed != null) {
			keep.add(seed);
		}
		for (PamDataUnit unit : candidates) {
			keep.add((DifarDataUnit) unit);
		}
		return keep;
	}

	/**
	 * One clip, drawn like those in the clip display.
	 * @param unit the detection.
	 * @param inMatch true if it is part of the match in use.
	 */
	private JComponent clipPanel(DifarDataUnit unit, boolean inMatch) {
		Color frame = inMatch
				? PamColors.getInstance().getChannelColor(PamUtils.getSingleChannel(unit.getChannelBitmap()))
				: Color.GRAY;
		JPanel outer = new JPanel(new BorderLayout());
		outer.setBackground(frame);
		outer.setBorder(new EmptyBorder(2, 2, 2, 2));
		outer.add(BorderLayout.CENTER, new ClipAxisPanel(unit));
		outer.setToolTipText(String.format("Channel %d, %s, %.2f s",
				PamUtils.getSingleChannel(unit.getChannelBitmap()),
				PamCalendar.formatDateTime(unit.getTimeMilliseconds()),
				unit.getDurationInSeconds()));
		return outer;
	}

	/**
	 * @return the spectrogram of a clip, drawn once and kept.
	 */
	private BufferedImage getImage(DifarDataUnit unit) {
		BufferedImage image = images.get(unit);
		if (image != null) {
			return image;
		}
		ClipDisplayParameters params = difarControl.getClipDisplayParams(unit);
		ColourArray colours = ColourArray.createStandardColourArray(256, params.getColourMap());
		int fftLength = difarControl.getDifarProcess().getDisplayFFTLength(unit);
		int fftHop = difarControl.getDifarProcess().getDisplayFFTHop(unit);
		image = unit.getClipImage(0, fftLength, fftHop, params.amlitudeMinVal,
				params.amlitudeMinVal + params.amplitudeRangeVal, colours.getColours());
		if (image != null) {
			images.put(unit, image);
		}
		return image;
	}

	/**
	 * The area around a clip, carrying its time, channel, duration and top
	 * frequency, in the same places as the clip display puts them.
	 */
	private class ClipAxisPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final DifarDataUnit unit;

		private final int borderSize;

		private final int fontAscent;

		ClipAxisPanel(DifarDataUnit unit) {
			super(new BorderLayout());
			this.unit = unit;
			setBackground(PamColors.getInstance().getBorderColour());
			setForeground(PamColors.getInstance().getForegroudColor(PamColor.AXIS));
			FontMetrics metrics = getFontMetrics(getFont());
			fontAscent = metrics.getAscent();
			borderSize = fontAscent + 1;
			setBorder(new EmptyBorder(borderSize, borderSize, borderSize, 2));
			add(BorderLayout.CENTER, new ClipImagePanel(unit));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			FontMetrics metrics = g.getFontMetrics();
			g.setColor(getForeground());
			g.drawString(PamCalendar.formatTime(unit.getTimeMilliseconds(), true), borderSize, fontAscent);

			String duration = String.format("%3.2fs", unit.getDurationInSeconds());
			Rectangle2D size = metrics.getStringBounds(duration, g);
			g.drawString(duration, (int) (getWidth() - size.getWidth()), getHeight() - metrics.getDescent());

			g.drawString(String.format("Ch%d", PamUtils.getSingleChannel(unit.getChannelBitmap())),
					metrics.charWidth('c') / 2, getHeight() - metrics.getDescent());

			ClipDisplayParameters params = difarControl.getClipDisplayParams(unit);
			double topFrequency = unit.getDisplaySampleRate() / 2. * params.frequencyScale;
			String frequency = FrequencyFormat.formatFrequency(topFrequency, true);
			size = metrics.getStringBounds(frequency, g);
			Graphics2D g2d = (Graphics2D) g;
			int x = fontAscent;
			int y = (int) (size.getWidth() + borderSize);
			g2d.translate(x, y);
			g2d.rotate(-Math.PI / 2.);
			g2d.drawString(frequency, 0, 0);
			g2d.rotate(+Math.PI / 2.);
			g2d.translate(-x, -y);
		}
	}

	/** Draws one clip's spectrogram, scaled to the strip's height. */
	private class ClipImagePanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final DifarDataUnit unit;

		ClipImagePanel(DifarDataUnit unit) {
			this.unit = unit;
			int width = Math.max(32, (int) (unit.getDurationInSeconds() * PIXELS_PER_SECOND));
			setPreferredSize(new Dimension(width, CLIP_HEIGHT));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			BufferedImage image = getImage(unit);
			if (image != null) {
				g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			}
		}
	}

	/**
	 * Names a section of the strip, with a line down one side, and enough space
	 * either side to break the clips into groups.
	 */
	private class SectionDivider extends JPanel {

		private static final long serialVersionUID = 1L;

		private final String title;

		SectionDivider(String title) {
			this.title = title;
			setOpaque(false);
			FontMetrics metrics = getFontMetrics(getFont());
			setPreferredSize(new Dimension(metrics.getHeight() + 16, CLIP_HEIGHT + 2 * metrics.getHeight()));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(PamColors.getInstance().getForegroudColor(PamColor.AXIS));
			g.drawLine(4, 2, 4, getHeight() - 2);
			FontMetrics metrics = g.getFontMetrics();
			Graphics2D g2d = (Graphics2D) g;
			int x = 8 + metrics.getAscent();
			int y = (int) (getHeight() + metrics.getStringBounds(title, g).getWidth()) / 2;
			g2d.translate(x, y);
			g2d.rotate(-Math.PI / 2.);
			g2d.drawString(title, 0, 0);
			g2d.rotate(+Math.PI / 2.);
			g2d.translate(-x, -y);
		}
	}
}
