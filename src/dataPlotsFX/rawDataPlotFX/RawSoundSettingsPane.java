package dataPlotsFX.rawDataPlotFX;

import java.util.ArrayList;
import java.util.List;

import dataPlotsFX.layout.TDSettingsPane;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * Settings pane for the raw sound (waveform) plot. Lets the user pick the colour
 * the waveform is drawn in from the standard PAMGuard colours, or colour each
 * channel with its standard PAMGuard channel colour.
 *
 * @author Jamie Macaulay
 */
public class RawSoundSettingsPane extends PamBorderPane implements TDSettingsPane {

	private static final double PREF_WIDTH = 250;

	/** Size of one colour swatch in pixels. */
	private static final double SWATCH_SIZE = 22;

	/** Gap between a swatch and the ring which shows it is selected. */
	private static final double RING_GAP = 6;

	/** The icon shown on the settings tab. */
	private static final String ICON = "mdi2w-waveform";

	/**
	 * The raw sound data info this pane is controlling.
	 */
	private RawSoundDataInfo rawSoundDataInfo;

	/**
	 * The selection rings, one for each of the standard PAMGuard colours.
	 */
	private List<Rectangle> rings = new ArrayList<>();

	/**
	 * Switch to colour each channel with its standard PAMGuard channel colour.
	 */
	private PamToggleSwitch channelSwitch;

	/**
	 * The pane holding the colour swatches.
	 */
	private TilePane colourPane;

	/**
	 * True while the controls are being set from the parameters, so that the change
	 * listeners do not write the half set controls back into the parameters.
	 */
	private boolean settingParams = false;

	/**
	 * The colour scheme version the swatches were built from, so that they can be
	 * rebuilt when the user changes the colour scheme or the colour blind palette.
	 */
	private int paletteVersion = -1;

	public RawSoundSettingsPane(RawSoundDataInfo rawSoundDataInfo) {
		this.rawSoundDataInfo = rawSoundDataInfo;
		createPane();
		setParams();
		this.setPrefWidth(PREF_WIDTH);
	}

	private void createPane() {
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		Label title = new Label("Waveform colour");
		PamGuiManagerFX.titleFont2style(title);

		colourPane = createColourPane();

		channelSwitch = new PamToggleSwitch("Colour by channel");
		channelSwitch.setTooltip(new Tooltip("Use the standard PAMGuard colour for each channel "
				+ "instead of a single colour for all channels"));
		channelSwitch.selectedProperty().addListener((obsVal, oldVal, newVal) -> {
			newSettings();
		});

		holder.getChildren().addAll(title, colourPane, channelSwitch);

		this.setPadding(new Insets(5, 10, 5, 10));
		this.setCenter(holder);
	}

	/**
	 * Create the pane which holds the colour swatches. The swatches themselves are
	 * filled in by {@link #checkPalette()}.
	 *
	 * @return the colour selection pane.
	 */
	private TilePane createColourPane() {
		TilePane tilePane = new TilePane();
		tilePane.setHgap(3);
		tilePane.setVgap(3);
		tilePane.setPrefColumns(7);
		return tilePane;
	}

	/**
	 * Rebuild the swatches if the colours have changed since they were last built.
	 * <p>
	 * These are the standard PAMGuard colours, so they change whenever the user
	 * picks a different colour scheme or a different colour blind palette - and the
	 * palettes do not all have the same number of colours, so the swatches have to
	 * be rebuilt rather than just recoloured.
	 */
	private void checkPalette() {
		int version = PamColorsFX.getInstance().getColourSchemeVersion();
		if (version == paletteVersion && !rings.isEmpty()) {
			return;
		}
		paletteVersion = version;
		buildSwatches();
	}

	/**
	 * Fill the colour pane with a swatch for each of the standard PAMGuard colours.
	 * <p>
	 * The selected swatch is shown with a ring around it rather than a border on
	 * the swatch itself, so that the selection is still visible on the black and
	 * white ends of the palette.
	 */
	private void buildSwatches() {
		rings.clear();
		colourPane.getChildren().clear();

		int nCols = PamColorsFX.getInstance().getNWhaleColours();
		for (int i = 0; i < nCols; i++) {
			final int index = i;
			Color colour = PamColorsFX.getInstance().getWhaleColor(i);

			Rectangle swatch = new Rectangle(SWATCH_SIZE, SWATCH_SIZE, colour);
			swatch.setArcWidth(4);
			swatch.setArcHeight(4);
			swatch.setStroke(Color.GRAY);
			swatch.setStrokeWidth(1);

			Rectangle ring = new Rectangle(SWATCH_SIZE + RING_GAP, SWATCH_SIZE + RING_GAP, Color.TRANSPARENT);
			ring.setArcWidth(7);
			ring.setArcHeight(7);
			ring.setStrokeWidth(2);
			ring.setStroke(Color.TRANSPARENT);

			StackPane holder = new StackPane(ring, swatch);
			holder.setOnMouseClicked((e) -> {
				rawSoundDataInfo.getRawSoundParams().colourByChannel = false;
				rawSoundDataInfo.getRawSoundParams().lineColourIndex = index;
				setParams();
				newSettings();
			});
			rings.add(ring);
			colourPane.getChildren().add(holder);
		}
	}

	/**
	 * Set the controls from the current parameters and from the current PAMGuard
	 * colours.
	 */
	public void setParams() {
		settingParams = true;

		checkPalette();

		RawSoundParamsFX params = rawSoundDataInfo.getRawSoundParams();
		/*
		 * The colour index wraps if it is bigger than the palette, which it can be
		 * after a change to a palette with fewer colours, so highlight the swatch which
		 * is actually being used rather than the raw index.
		 */
		int selected = -1;
		if (!params.colourByChannel && params.lineColourIndex >= 0) {
			selected = PamColorsFX.getInstance().getWhaleColourIndex(params.lineColourIndex);
		}
		Color selectCol = PamColorsFX.getInstance().getColor(PamColorsFX.PamColor.AXIS);
		for (int i = 0; i < rings.size(); i++) {
			rings.get(i).setStroke(i == selected ? selectCol : Color.TRANSPARENT);
		}
		channelSwitch.setSelected(params.colourByChannel);
		colourPane.setDisable(params.colourByChannel);

		settingParams = false;
	}

	/**
	 * The settings have changed - copy the controls into the parameters and
	 * repaint.
	 */
	private void newSettings() {
		if (settingParams) {
			return;
		}
		RawSoundParamsFX params = rawSoundDataInfo.getRawSoundParams();
		params.colourByChannel = channelSwitch.isSelected();
		setParams();

		rawSoundDataInfo.updateLineColours();
		rawSoundDataInfo.getTDGraph().repaint(0);
	}

	@Override
	public Node getHidingIcon() {
		return PamGlyphDude.createPamIcon(ICON, PamGuiManagerFX.iconSize);
	}

	@Override
	public String getShowingName() {
		return "Waveform";
	}

	@Override
	public Node getShowingIcon() {
		return PamGlyphDude.createPamIcon(ICON, PamGuiManagerFX.iconSize);
	}

	@Override
	public Pane getPane() {
		return this;
	}

}
