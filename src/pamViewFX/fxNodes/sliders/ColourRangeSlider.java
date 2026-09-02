package pamViewFX.fxNodes.sliders;

import java.util.Locale;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 *
 * The colour range slider shows a linear colour gradient between two thumbs
 * with the rest of the slider coloured by the minimum and maximum of the colour
 * gradient. The slider is generally used to allow users to change settings for
 * anything requiring a colour scale, e.g. a spectrogram.
 * 
 * @author Jamie Macaulay
 *
 */
public class ColourRangeSlider extends PamRangeSlider {

	/**
	 * The colour of the whole track i.e. the low limit colour. 
	 */
	private Color trackCol;

	/**
	 * The colour of the top bar. i.e. the high limit colour
	 */
	private Color topBarCol;

	/** 
	 * Additional node for top bar/ 
	 */
	private Pane topBar;

	/**
	 * The default track width
	 */
	private double trackWidth=15;

	/**
	 * The colour map array time. 
	 */
	private ColourArrayType colourMap = ColourArrayType.RED;

	/**
	 * Context menu allowing the user to type new absolute (min/max) limits.
	 */
	private ContextMenu limitsContextMenu;

	/**
	 * Text field for the minimum (low) absolute limit.
	 */
	private TextField minField;

	/**
	 * Text field for the maximum (high) absolute limit.
	 */
	private TextField maxField;

	/**
	 * Optional callback run after the user applies new absolute limits, e.g. so an
	 * owner can persist the new limits and recolour.
	 */
	private Runnable onLimitsChanged;

	/**
	 * Create the colour range slider.
	 */
	public ColourRangeSlider() {
		super();
		initLimitsContextMenu();
	}


	public ColourRangeSlider(Orientation orientation) {
		super();
		this.orientationProperty().set(orientation);
		initLimitsContextMenu();
	}

	/**
	 * Create the right-click context menu which lets the user change the absolute
	 * (min/max) limits of the slider by typing in new values.
	 */
	private void initLimitsContextMenu() {
		minField = new TextField();
		maxField = new TextField();
		minField.setPrefColumnCount(6);
		maxField.setPrefColumnCount(6);

		Button applyButton = new Button("Apply");
		applyButton.setOnAction(e -> applyLimitsAndHide());
		//allow Enter in either field to apply.
		minField.setOnAction(e -> applyLimitsAndHide());
		maxField.setOnAction(e -> applyLimitsAndHide());

		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setPadding(new Insets(5));
		grid.add(new Label("Limits"), 0, 0, 2, 1);
		grid.add(new Label("Min"), 0, 1);
		grid.add(minField, 1, 1);
		grid.add(new Label("Max"), 0, 2);
		grid.add(maxField, 1, 2);
		grid.add(applyButton, 1, 3);

		CustomMenuItem menuItem = new CustomMenuItem(grid);
		//keep the menu open while the user types / clicks in the fields.
		menuItem.setHideOnClick(false);

		limitsContextMenu = new ContextMenu(menuItem);

		setOnContextMenuRequested(e -> {
			//prefill with the current limits each time the menu is shown.
			minField.setText(formatLimit(getMin()));
			maxField.setText(formatLimit(getMax()));
			limitsContextMenu.show(this, e.getScreenX(), e.getScreenY());
			e.consume();
		});
	}

	/**
	 * Format a limit value for display, trimming a trailing ".0" for whole numbers.
	 */
	private String formatLimit(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value)) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	/**
	 * Parse and apply the limits from the text fields, then hide the menu.
	 */
	private void applyLimitsAndHide() {
		if (applyLimits()) {
			limitsContextMenu.hide();
		}
	}

	/**
	 * Parse the min/max text fields and apply them as the slider's absolute limits.
	 * The thumb (low/high) values are clamped to stay within the new limits. Invalid
	 * input (non-numeric, or max not greater than min) is ignored.
	 *
	 * @return true if valid limits were applied.
	 */
	private boolean applyLimits() {
		double newMin;
		double newMax;
		try {
			newMin = Double.parseDouble(minField.getText().trim());
			newMax = Double.parseDouble(maxField.getText().trim());
		}
		catch (NumberFormatException ex) {
			return false;
		}
		if (newMax <= newMin) {
			return false;
		}

		//expand the max first so we never transiently have min > max, then shrink.
		setMax(Math.max(getMax(), newMax));
		setMin(newMin);
		setMax(newMax);

		//keep the thumbs within the new limits.
		if (getLowValue() < newMin) {
			setLowValue(newMin);
		}
		if (getHighValue() > newMax) {
			setHighValue(newMax);
		}

		if (onLimitsChanged != null) {
			onLimitsChanged.run();
		}
		return true;
	}

	/**
	 * Set a callback which is run after the user applies new absolute limits via the
	 * right-click menu. Owners can use this to persist the new limits and recolour.
	 *
	 * @param onLimitsChanged the callback, or null to clear.
	 */
	public void setOnLimitsChanged(Runnable onLimitsChanged) {
		this.onLimitsChanged = onLimitsChanged;
	}

	/**
	 * Create the top bar. 
	 */
	public void initTopBar(){
		topBar=new StackPane();
		//TODO-need to sort for horizontal 
		if (getOrientation()==Orientation.VERTICAL){
			topBar.layoutXProperty().bind(getTrack().layoutXProperty());
		}
		else {
			topBar.layoutYProperty().bind(getTrack().layoutYProperty());
		}
		//		topBar.setStyle("-fx-background-color: red;");
		getChildren().add(topBar);
	}

	/**
	 * Extract the tracks from CSS. 
	 */
	@Override
	protected void initColorTracks() {
		super.initColorTracks();

		if (getTrack() ==null) return; 


		initTopBar(); 
		getHighThumb().toFront(); //need to bring to front.
		getLowThumb().toFront(); //need to bring to front. 

		if (getOrientation()==Orientation.VERTICAL){
			getRangeBar().setPrefWidth(trackWidth);
			getTrack().setPrefWidth(trackWidth);
			getTopBar().setPrefWidth(trackWidth);
		}
		else{
			getRangeBar().setPrefHeight(trackWidth);
			getTrack().setPrefHeight(trackWidth);
			getTopBar().setPrefHeight(trackWidth);
		}

		setColourArrayType(colourMap); 
	}


	@Override
	public void layoutChildren() {
		super.layoutChildren();
		double rangeStart;
		if (this.getTrack()==null) return; 
		//now resize the top bar. 
		if (getOrientation()==Orientation.VERTICAL){
			rangeStart=this.getHighThumb().getLayoutY()+getHighThumb().getHeight(); 
			topBar.layoutYProperty().setValue(0);
			topBar.resize(trackWidth, rangeStart+1);
		}
		else {
			rangeStart=this.getLowThumb().getLayoutX()+getLowThumb().getWidth(); 
			topBar.layoutXProperty().setValue(0);
			topBar.resize(rangeStart+1, trackWidth);
		}

	}


	protected Region getTopBar() {
		return topBar;
	}

	@Override
	public void setTrackColor(Color trackColour) {
		//FIXME- this is getting called somewhere and messing up the colours. 
	}


	/**
	 * Set the colour array type. 
	 * @param colourArrayType - the ColourArrayType to set. 
	 */
	public void setColourArrayType(ColourArrayType colourMap) {
		//set the colour gradient
		this.colourMap=colourMap; 
		Color[] colorList=ColourArray.getColorList(colourMap);

		if (getOrientation()==Orientation.VERTICAL) {
			trackCol=colorList[0]; 
			topBarCol=colorList[colorList.length-1]; 
		}
		else {
			topBarCol=colorList[0]; 
			trackCol=colorList[colorList.length-1]; 
		}

		if (getTrack()!=null) {

			//set the solid colours for the track and top bar.
			//		getTrack().setBackground(new Background(new BackgroundFill(trackCol, CornerRadii.EMPTY, Insets.EMPTY)));
			//28/03/2017 - had to change to css as adding to a scroll pane seemed to override background.
			getTrack().setStyle("-fx-background-color: " + PamUtilsFX.color2Hex(trackCol));

			getTopBar().setStyle("-fx-background-color: " + PamUtilsFX.color2Hex(topBarCol));

			//set the colour gradient
			getRangeBar().setStyle("-fx-background-color: " + gradientCSS(colorList) + ";");
		}
	}

	/**
	 * Build the CSS for a linear gradient running the length of the slider through
	 * the given colours.
	 * <p>
	 * The colours have to be set as inline styles rather than with
	 * {@link Region#setBackground(Background)}. Switching colour scheme swaps the
	 * style sheets on the live scene, which makes JavaFX re-apply CSS to every node
	 * in it, and a background set programmatically does not survive that: the range
	 * bar goes back to the flat <code>-fx-focus-color</code> of ControlsFX's own
	 * rangeslider.css and the gradient becomes a block of colour. An inline style
	 * beats any style sheet, so it survives. This is the same reason the track is
	 * set with a style rather than a background.
	 *
	 * @param colorList the colours of the gradient, in order.
	 * @return a CSS colour value for the gradient.
	 */
	private String gradientCSS(Color[] colorList) {
		if (colorList.length == 1) {
			return colourString(colorList[0]);
		}
		StringBuilder sb = new StringBuilder();
		//vertical sliders run bottom (minimum) to top, horizontal ones left to right.
		sb.append(getOrientation()==Orientation.VERTICAL ? "linear-gradient(from 0% 100% to 0% 0%"
				: "linear-gradient(from 0% 0% to 100% 0%");
		for (int j=0; j<colorList.length; j++){
			sb.append(", ").append(colourString(colorList[j]));
			sb.append(String.format(Locale.US, " %.2f%%", 100.*j/(colorList.length-1)));
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * The web colour string for a colour, without the trailing semi colon
	 * {@link PamUtilsFX#color2Hex(Color)} adds, so that it can be used part way
	 * through a CSS value such as a gradient.
	 *
	 * @param colour the colour
	 * @return the colour as #RRGGBB
	 */
	private static String colourString(Color colour) {
		return String.format("#%02X%02X%02X", (int) (colour.getRed()*255),
				(int) (colour.getGreen()*255), (int) (colour.getBlue()*255));
	}







}
