package pamViewFX.symbol;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.ColourBarSymbolData;
import PamView.symbol.legend.ColourSetSymbolData;
import PamView.symbol.legend.LegendSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Creates JavaFX panes for symbol legends which look the same as the Swing
 * legends from {@link PamView.symbol.legend.SwingSymbolLegendFactory}. A legend
 * pane is a rounded, slightly translucent box holding a vertical list of
 * sections, one for each {@link SymbolLegend}. Each section has a title with an
 * icon on the right showing whether the fill colour, line colour and / or shape
 * are set, followed by the legend entries:
 * <ul>
 * <li>{@link LegendSymbolData} - a symbol and a name</li>
 * <li>{@link ColourBarSymbolData} - a symbol and a labelled colour bar</li>
 * <li>{@link ColourSetSymbolData} - a symbol and a swatch of the colours in the set</li>
 * </ul>
 * Colours are set explicitly rather than from style sheets so the legend is
 * readable on whatever the plot background is. The pane should be recreated if
 * the colours or the legends change.
 *
 * @author Jamie Macaulay
 * @see PamView.symbol.legend.SwingSymbolLegendFactory
 */
public class FXSymbolLegendFactory {

	/**
	 * Size of legend symbols in pixels.
	 */
	public static final double SYMBOL_SIZE = 12;

	/**
	 * Size of the colour bar in pixels.
	 */
	private static final double COLOUR_BAR_WIDTH = 100, COLOUR_BAR_HEIGHT = 10;

	/**
	 * Size of colour set swatches in pixels.
	 */
	private static final double SWATCH_SIZE = 8;

	/**
	 * Corner radius of the legend box.
	 */
	private static final double CORNER_RADIUS = 6;

	/**
	 * Opacity of the legend background so data under the legend still shows through.
	 */
	private static final double BACKGROUND_OPACITY = 200. / 255.;

	/**
	 * Line width of the fill / line / shape icons.
	 */
	private static final double ICON_STROKE = 1.6;

	private FXSymbolLegendFactory() {
	}

	/**
	 * Create a legend pane using the plot window and axis colours of the current
	 * colour scheme, i.e. the same colours as the Swing legend.
	 * @param legends - the legends, e.g. from
	 * {@link PamView.symbol.PamSymbolManager#getSymbolLegends(String, PamView.GeneralProjector)}
	 * @return legend pane.
	 */
	public static Pane createLegendPane(List<SymbolLegend> legends) {
		PamColors pamColors = PamColors.getInstance();
		return createLegendPane(legends, PamUtilsFX.awtToFXColor(pamColors.getColor(PamColor.PlOTWINDOW)),
				PamUtilsFX.awtToFXColor(pamColors.getColor(PamColor.AXIS)));
	}

	/**
	 * Create a legend pane.
	 * @param legends - the legends.
	 * @param background - background colour. This is made slightly translucent.
	 * @param foreground - colour for text and outlines.
	 * @return legend pane.
	 */
	public static Pane createLegendPane(List<SymbolLegend> legends, Color background, Color foreground) {
		LinkedHashMap<String, List<SymbolLegend>> groups = new LinkedHashMap<>();
		groups.put(null, legends);
		return createGroupedLegendPane(groups, background, foreground);
	}

	/**
	 * Create a legend pane holding the legends for several data sources, e.g.
	 * different data blocks on the same display. If there is more than one group,
	 * each group starts with a header showing its name.
	 * @param groups - map of group names to legends, in display order.
	 * @param background - background colour. This is made slightly translucent.
	 * @param foreground - colour for text and outlines.
	 * @return legend pane.
	 */
	public static Pane createGroupedLegendPane(Map<String, List<SymbolLegend>> groups, Color background, Color foreground) {
		VBox vBox = new VBox(4);
		vBox.setPadding(new Insets(4, 6, 4, 6));
		vBox.setFillWidth(true);
		vBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		CornerRadii radii = new CornerRadii(CORNER_RADIUS);
		Color fill = new Color(background.getRed(), background.getGreen(), background.getBlue(), BACKGROUND_OPACITY);
		vBox.setBackground(new Background(new BackgroundFill(fill, radii, Insets.EMPTY)));
		vBox.setBorder(new Border(new BorderStroke(foreground, BorderStrokeStyle.SOLID, radii, BorderWidths.DEFAULT)));

		boolean showHeaders = groups.size() > 1;
		boolean first = true;
		for (Map.Entry<String, List<SymbolLegend>> group : groups.entrySet()) {
			if (showHeaders && group.getKey() != null) {
				Label header = createLabel(group.getKey(), foreground);
				header.setFont(Font.font(null, FontWeight.BOLD, Font.getDefault().getSize() * 1.1));
				header.setUnderline(true);
				if (!first) {
					VBox.setMargin(header, new Insets(4, 0, 0, 0));
				}
				vBox.getChildren().add(header);
			}
			first = false;
			if (group.getValue() == null) {
				continue;
			}
			for (SymbolLegend legend : group.getValue()) {
				vBox.getChildren().add(createLegendSection(legend, foreground));
			}
		}
		return vBox;
	}

	/**
	 * Create the section for a single legend: the title and fill / line / shape
	 * icon followed by the entries.
	 * @param legend - the legend.
	 * @param foreground - colour for text and outlines.
	 * @return legend section.
	 */
	public static Pane createLegendSection(SymbolLegend legend, Color foreground) {
		GridPane gridPane = new GridPane();
		gridPane.setHgap(4);
		gridPane.setVgap(2);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		ColumnConstraints iconColumn = new ColumnConstraints();
		iconColumn.setHgrow(Priority.ALWAYS);
		iconColumn.setHalignment(HPos.RIGHT);
		gridPane.getColumnConstraints().addAll(new ColumnConstraints(), new ColumnConstraints(), iconColumn);

		Tooltip tip = new Tooltip("Sets " + SymbolModType.getName(legend.getModBits()).toLowerCase());
		Label title = createLabel(legend.getTitle(), foreground);
		title.setFont(Font.font(null, FontWeight.BOLD, Font.getDefault().getSize()));
		title.setTooltip(tip);
		gridPane.add(title, 0, 0, 2, 1);

		// the icon goes in its own column which takes up any spare width, so the icons
		// for all legends line up on the right.
		Label modBitsLabel = new Label();
		modBitsLabel.setGraphic(createModBitsIcon(legend.getModBits(), foreground));
		modBitsLabel.setTooltip(tip);
		GridPane.setMargin(modBitsLabel, new Insets(0, 0, 0, 6));
		gridPane.add(modBitsLabel, 2, 0);

		int row = 1;
		for (LegendSymbolData symbolData : legend.getSymbols()) {
			Node symbolNode = createSymbolNode(symbolData, SYMBOL_SIZE);
			GridPane.setMargin(symbolNode, new Insets(0, 0, 0, 3));
			GridPane.setHalignment(symbolNode, HPos.CENTER);
			gridPane.add(symbolNode, 0, row);
			gridPane.add(createEntryNode(symbolData, foreground), 1, row);
			row++;
		}
		return gridPane;
	}

	/**
	 * Create the node which goes to the right of the symbol for a legend entry.
	 * @param symbolData - the legend entry.
	 * @param foreground - colour for text and outlines.
	 * @return node.
	 */
	private static Node createEntryNode(LegendSymbolData symbolData, Color foreground) {
		if (symbolData instanceof ColourBarSymbolData) {
			return createColourBar((ColourBarSymbolData) symbolData, foreground);
		}
		Label label = createLabel(symbolData.getName(), foreground);
		if (symbolData instanceof ColourSetSymbolData) {
			label.setGraphic(createColourSetNode(((ColourSetSymbolData) symbolData).getColours(), foreground));
			label.setGraphicTextGap(4);
		}
		return label;
	}

	/**
	 * Create a colour bar with the entry name above and the values at either end
	 * below.
	 * @param colourBarData - colour bar legend entry.
	 * @param foreground - colour for text and outlines.
	 * @return colour bar node.
	 */
	private static Node createColourBar(ColourBarSymbolData colourBarData, Color foreground) {
		String name = colourBarData.getName();
		if (colourBarData.getUnits() != null) {
			name += " (" + colourBarData.getUnits() + ")";
		}
		Rectangle bar = new Rectangle(COLOUR_BAR_WIDTH, COLOUR_BAR_HEIGHT);
		bar.setFill(ColourArray.getLinerGradient(Orientation.HORIZONTAL, COLOUR_BAR_WIDTH, colourBarData.getColourArrayType()));
		bar.setStroke(foreground);
		bar.setStrokeWidth(1);

		double smallFont = Font.getDefault().getSize() * 0.85;
		Label minLabel = createLabel(colourBarData.formatValue(colourBarData.getMinValue()), foreground);
		minLabel.setFont(Font.font(smallFont));
		Label maxLabel = createLabel(colourBarData.formatValue(colourBarData.getMaxValue()), foreground);
		maxLabel.setFont(Font.font(smallFont));
		BorderPane labels = new BorderPane();
		labels.setLeft(minLabel);
		labels.setRight(maxLabel);
		labels.setPrefWidth(COLOUR_BAR_WIDTH);
		labels.setMaxWidth(COLOUR_BAR_WIDTH);

		VBox vBox = new VBox(1, createLabel(name, foreground), bar, labels);
		vBox.setAlignment(Pos.CENTER_LEFT);
		return vBox;
	}

	/**
	 * Create a row of swatches for a colour set.
	 * @param colours - the colours in the set.
	 * @param foreground - colour for the outline.
	 * @return swatch node.
	 */
	private static Node createColourSetNode(java.awt.Color[] colours, Color foreground) {
		HBox hBox = new HBox();
		hBox.setBorder(new Border(new BorderStroke(foreground, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		hBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		if (colours == null) {
			return hBox;
		}
		PamColors pamColors = PamColors.getInstance();
		for (java.awt.Color colour : colours) {
			Rectangle swatch = new Rectangle(SWATCH_SIZE, SWATCH_SIZE);
			swatch.setFill(PamUtilsFX.awtToFXColor(pamColors.adaptSymbolColour(colour)));
			hBox.getChildren().add(swatch);
		}
		return hBox;
	}

	/**
	 * Create a node which draws a legend symbol at a fixed size, adapted to the
	 * colour scheme.
	 * @param symbolData - the symbol.
	 * @param size - size in pixels.
	 * @return symbol node.
	 */
	public static Node createSymbolNode(SymbolData symbolData, double size) {
		Canvas canvas = new Canvas(size, size);
		PamSymbolFX symbol = new PamSymbolFX(PamSymbolChooser.adaptToColourScheme(symbolData));
		symbol.draw(canvas.getGraphicsContext2D(), new Point2D(size/2, size/2), size-2, size-2);
		return canvas;
	}

	/**
	 * Create an icon which shows which symbol properties a legend sets. A circle
	 * which is filled if the fill colour is set and has a solid outline if the
	 * line colour is set (faint if not), followed by a triangle if the shape is
	 * set.
	 * @param modBits - bitmap of {@link SymbolModType} flags.
	 * @param foreground - colour to draw the icon in.
	 * @return icon node.
	 */
	private static Node createModBitsIcon(int modBits, Color foreground) {
		double size = 12;
		boolean shape = (modBits & SymbolModType.SHAPE) != 0;
		Canvas canvas = new Canvas(shape ? 2 * size + 2 : size, size);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(ICON_STROKE);
		if ((modBits & SymbolModType.FILLCOLOUR) != 0) {
			gc.setFill(foreground.deriveColor(0, 1, 1, 140. / 255.));
			gc.fillOval(1.5, 1.5, size - 3, size - 3);
		}
		if ((modBits & SymbolModType.LINECOLOUR) != 0) {
			gc.setStroke(foreground);
		}
		else {
			gc.setStroke(foreground.deriveColor(0, 1, 1, 90. / 255.));
		}
		gc.strokeOval(1.5, 1.5, size - 3, size - 3);
		if (shape) {
			double x0 = size + 2;
			gc.setStroke(foreground);
			gc.strokePolygon(new double[] {x0 + (size - 2) / 2, x0 + size - 2, x0},
					new double[] {1.5, size - 1.5, size - 1.5}, 3);
		}
		return canvas;
	}

	private static Label createLabel(String text, Color foreground) {
		Label label = new Label(text);
		label.setTextFill(foreground);
		return label;
	}

}
