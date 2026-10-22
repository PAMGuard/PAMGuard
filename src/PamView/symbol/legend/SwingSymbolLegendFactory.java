package PamView.symbol.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;

/**
 * Creates Swing components for symbol legends. A legend component is a vertical
 * list of panels, one for each {@link SymbolLegend}. Each panel has a title with
 * an icon showing whether the fill colour, line colour and / or shape are set,
 * followed by the legend entries:
 * <ul>
 * <li>{@link LegendSymbolData} - a symbol and a name</li>
 * <li>{@link ColourBarSymbolData} - a symbol and a labelled colour bar</li>
 * <li>{@link ColourSetSymbolData} - a symbol and a swatch of the colours in the set</li>
 * </ul>
 * Colours are taken from the current colour scheme when the component is
 * created, so the component should be recreated if the scheme changes.
 *
 * @author Jamie Macaulay
 * @see pamViewFX.symbol.FXSymbolLegendFactory
 */
public class SwingSymbolLegendFactory {

	/**
	 * Size of legend symbols in pixels.
	 */
	public static final int SYMBOL_SIZE = 12;

	/**
	 * Size of the colour bar in pixels.
	 */
	private static final int COLOUR_BAR_WIDTH = 100, COLOUR_BAR_HEIGHT = 10;

	/**
	 * Size of colour set swatches in pixels.
	 */
	private static final int SWATCH_SIZE = 8;

	private SwingSymbolLegendFactory() {
	}

	/**
	 * Create a component containing all legends in a vertical list.
	 * @param legends - the legends, e.g. from
	 * {@link PamView.symbol.PamSymbolManager#getSymbolLegends(String, PamView.GeneralProjector)}
	 * @return legend component.
	 */
	public static JComponent createLegendComponent(List<SymbolLegend> legends) {
		JPanel panel = new RoundedPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(PamColors.getInstance().getColor(PamColor.PlOTWINDOW));
		panel.setForeground(legendForeground());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		if (legends != null) {
			for (int i = 0; i < legends.size(); i++) {
				JComponent legendPanel = createLegendPanel(legends.get(i));
				legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				if (i > 0) {
					legendPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
				}
				panel.add(legendPanel);
			}
		}
		return panel;
	}

	/**
	 * Create a panel for a single legend.
	 * @param legend - the legend.
	 * @return legend panel.
	 */
	public static JComponent createLegendPanel(SymbolLegend legend) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(1, 1, 1, 3);

		String tip = "Sets " + SymbolModType.getName(legend.getModBits()).toLowerCase();
		JLabel title = createLabel(legend.getTitle());
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		title.setToolTipText(tip);
		c.gridwidth = 2;
		panel.add(title, c);
		c.gridwidth = 1;

		// mod bits icon in its own column which takes up any spare width, so the
		// icons for all legends line up on the right.
		JLabel modBitsLabel = new JLabel(new ModBitsIcon(legend.getModBits()));
		modBitsLabel.setToolTipText(tip);
		c.gridx = 2;
		c.weightx = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(1, 6, 1, 1);
		panel.add(modBitsLabel, c);
		c.weightx = 0;

		for (LegendSymbolData symbolData : legend.getSymbols()) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.CENTER;
			c.insets = new Insets(1, 4, 1, 3);
			panel.add(new JLabel(new SymbolIcon(symbolData, SYMBOL_SIZE)), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(1, 1, 1, 3);
			panel.add(createEntryComponent(symbolData), c);
		}
		return panel;
	}

	/**
	 * Create the component which goes to the right of the symbol for a legend
	 * entry.
	 * @param symbolData - the legend entry.
	 * @return component.
	 */
	private static JComponent createEntryComponent(LegendSymbolData symbolData) {
		if (symbolData instanceof ColourBarSymbolData) {
			return createColourBar((ColourBarSymbolData) symbolData);
		}
		JLabel label = createLabel(symbolData.getName());
		if (symbolData instanceof ColourSetSymbolData) {
			label.setIcon(new ColourSetIcon(((ColourSetSymbolData) symbolData).getColours()));
			label.setIconTextGap(4);
		}
		return label;
	}

	/**
	 * Create a colour bar with the entry name above and the values at either end
	 * below.
	 * @param colourBarData - colour bar legend entry.
	 * @return colour bar component.
	 */
	private static JComponent createColourBar(ColourBarSymbolData colourBarData) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		String name = colourBarData.getName();
		if (colourBarData.getUnits() != null) {
			name += " (" + colourBarData.getUnits() + ")";
		}
		panel.add(createLabel(name), c);
		c.gridy++;
		panel.add(new ColourBar(colourBarData), c);
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 1;
		JLabel minLabel = createSmallLabel(colourBarData.formatValue(colourBarData.getMinValue()));
		panel.add(minLabel, c);
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		JLabel maxLabel = createSmallLabel(colourBarData.formatValue(colourBarData.getMaxValue()));
		maxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(maxLabel, c);
		return panel;
	}

	private static JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(legendForeground());
		return label;
	}

	private static JLabel createSmallLabel(String text) {
		JLabel label = createLabel(text);
		label.setFont(label.getFont().deriveFont(label.getFont().getSize2D() * 0.85f));
		return label;
	}

	/**
	 * @return the text / axis colour for the current colour scheme.
	 */
	private static Color legendForeground() {
		return PamColors.getInstance().getColor(PamColor.AXIS);
	}

	/**
	 * Panel with a rounded border, filled with a slightly translucent
	 * background colour and outlined in the foreground colour.
	 */
	private static class RoundedPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private static final int ARC = 12;

		/**
		 * Opacity of the background so data under the legend still shows through.
		 */
		private static final int ALPHA = 240;

		public RoundedPanel() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color bg = getBackground();
			g2d.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), ALPHA));
			g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2d.setColor(getForeground());
			g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
			g2d.dispose();
		}
	}

	/**
	 * Icon which draws a legend symbol at a fixed size, adapted to the colour scheme.
	 */
	private static class SymbolIcon implements Icon {

		private PamSymbol pamSymbol;

		private int size;

		public SymbolIcon(SymbolData symbolData, int size) {
			this.pamSymbol = new PamSymbol(PamSymbolChooser.adaptToColourScheme(symbolData));
			this.size = size;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			pamSymbol.draw(g2d, new Point(x + size/2, y + size/2), size-2, size-2);
			g2d.dispose();
		}

		@Override
		public int getIconWidth() {
			return size;
		}

		@Override
		public int getIconHeight() {
			return size;
		}
	}

	/**
	 * Icon which shows which symbol properties a legend sets. A circle which is
	 * filled if the fill colour is set and has a solid outline if the line colour
	 * is set (faint if not), followed by a triangle if the shape is set.
	 */
	private static class ModBitsIcon implements Icon {

		private static final int SIZE = 12;

		private static final float STROKE = 1.6f;

		private int modBits;

		public ModBitsIcon(int modBits) {
			this.modBits = modBits;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color fg = legendForeground();
			Ellipse2D circle = new Ellipse2D.Double(x + 1.5, y + 1.5, SIZE - 3, SIZE - 3);
			if ((modBits & SymbolModType.FILLCOLOUR) != 0) {
				g2d.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 140));
				g2d.fill(circle);
			}
			if ((modBits & SymbolModType.LINECOLOUR) != 0) {
				g2d.setColor(fg);
				g2d.setStroke(new BasicStroke(STROKE));
			}
			else {
				g2d.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 90));
				g2d.setStroke(new BasicStroke(STROKE));
			}
			g2d.draw(circle);
			if ((modBits & SymbolModType.SHAPE) != 0) {
				double x0 = x + SIZE + 2;
				Path2D triangle = new Path2D.Double();
				triangle.moveTo(x0 + (SIZE - 2) / 2., y + 1.5);
				triangle.lineTo(x0 + SIZE - 2, y + SIZE - 1.5);
				triangle.lineTo(x0, y + SIZE - 1.5);
				triangle.closePath();
				g2d.setColor(fg);
				g2d.setStroke(new BasicStroke(STROKE));
				g2d.draw(triangle);
			}
			g2d.dispose();
		}

		@Override
		public int getIconWidth() {
			return (modBits & SymbolModType.SHAPE) != 0 ? 2 * SIZE + 2 : SIZE;
		}

		@Override
		public int getIconHeight() {
			return SIZE;
		}
	}

	/**
	 * Icon which draws a row of swatches for a colour set.
	 */
	private static class ColourSetIcon implements Icon {

		private Color[] colours;

		public ColourSetIcon(Color[] colours) {
			this.colours = colours == null ? new Color[0] : colours;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			for (int i = 0; i < colours.length; i++) {
				g.setColor(PamColors.getInstance().adaptSymbolColour(colours[i]));
				g.fillRect(x + i * SWATCH_SIZE, y, SWATCH_SIZE, SWATCH_SIZE);
			}
			g.setColor(legendForeground());
			g.drawRect(x, y, getIconWidth() - 1, SWATCH_SIZE - 1);
		}

		@Override
		public int getIconWidth() {
			return Math.max(1, colours.length * SWATCH_SIZE);
		}

		@Override
		public int getIconHeight() {
			return SWATCH_SIZE;
		}
	}

	/**
	 * Horizontal colour bar.
	 */
	private static class ColourBar extends JComponent {

		private static final long serialVersionUID = 1L;

		private ColourBarSymbolData colourBarData;

		public ColourBar(ColourBarSymbolData colourBarData) {
			this.colourBarData = colourBarData;
			Dimension d = new Dimension(COLOUR_BAR_WIDTH, COLOUR_BAR_HEIGHT);
			setPreferredSize(d);
			setMinimumSize(d);
		}

		@Override
		protected void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight();
			for (int i = 0; i < w; i++) {
				g.setColor(colourBarData.getColour(w > 1 ? i / (double) (w - 1) : 0));
				g.drawLine(i, 0, i, h);
			}
			g.setColor(legendForeground());
			g.drawRect(0, 0, w - 1, h - 1);
		}
	}

}
