package pamViewFX.fxNodes.utilityPanes;

import PamUtils.LatLong;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;

/**
 * A pane which shows a latitude or longitude. Allows users to enter data as decimal or minutes/seconds. 
 * (copied to FX from Doug Gillespie's LatLongDialogStrip)
 * @author Jamie Macaulay 
 *
 */
public class LatLongStrip extends PamBorderPane {

	Label formattedText;
	TextField degrees, minutes, seconds, decminutes, decimal;
	Label dl, ml, sl, dml, dec;
	ComboBox<String> nsew;
	boolean isLatitude;
	//	boolean decimalMinutes = true;

	/**
	 * HBox to hold decimal minutes, degrees, seconds controls. 
	 */
	private PamHBox degHBox;

	/**
	 * HBox to hold decimal controls
	 */
	private PamHBox decHBox;

	/**
	 * The format type e.g. LatLong.FORMAT_DECIMALMINUTES.
	 */
	private int formatType = LatLong.FORMAT_DECIMALMINUTES;
	
	private Label titleLabel;


	/**
	 * Construct a strip of controls to include in a larger dialog. 
	 * <p>With this version of the constructor there is an option
	 * to display a titled border with the 
	 * word Latitude or Longitude
	 * @param latitude true if it's latitude, false for longitude. 
	 * @param showBorder true to show the titled border. 
	 */
	public LatLongStrip(boolean latitude, boolean showBorder) {
		isLatitude = latitude;
		createDialogStrip(showBorder);
	}

	/**
	 * Construct a strip of controls to include in a larger dialog. 
	 * <p>By default the strip will have a titled border with the 
	 * word Latitude or Longitude
	 * @param latitude true if it's latitude, false for longitude. 
	 */
	public LatLongStrip(boolean latitude) {
		isLatitude = latitude;
		createDialogStrip(true);
	}

	private void createDialogStrip(boolean showBorder) {

		String title;
		if (isLatitude) title = "Latitude";
		else title = "Longitude";

		//		title.setFont(PamGuiManagerFX.titleFontSize2);

		degrees = new TextField();
		degrees.setEditable(true);
		degrees.setPrefColumnCount(4);
		minutes = new TextField();
		minutes.setPrefColumnCount(3);
		minutes.setEditable(true);

		seconds = new TextField();
		seconds.setPrefColumnCount(6);
		seconds.setEditable(true);
		decminutes = new TextField();
		decminutes.setPrefColumnCount(6);
		decminutes.setEditable(true);

		decimal=new TextField(); 
		decimal.setPrefColumnCount(9);
		decimal.setEditable(true);

		nsew = new ComboBox<String>();
		nsew.setOnAction((action)->{
			double v = getValue();
			// and say the formated version
			sayFormattedValue(v);
		});

		dl = new Label("deg.");
		ml = new Label("min.");
		sl = new Label("sec.");
		dml = new Label("dec min.");
		dec = new Label("decimal deg."); 

		formattedText = new Label("Position");
		if (isLatitude) {
			nsew.getItems().add("N");
			nsew.getItems().add("S");
		}
		else{
			nsew.getItems().add("E");
			nsew.getItems().add("W");
		}

		degrees.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		minutes.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		seconds.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		decminutes.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		decimal.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		nsew.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});


		degHBox = new PamHBox();
		degHBox.setSpacing(5);
		degHBox.setAlignment(Pos.CENTER_LEFT);

		PamHBox holder= new PamHBox(); 
		holder.setAlignment(Pos.CENTER_LEFT);
		holder.setSpacing(5);
		holder.getChildren().addAll(titleLabel = new Label(title), degHBox, nsew); 

		this.setCenter(holder);
		this.setBottom(formattedText);

		showControls(formatType, true);
	}

	private void newTypedValues(KeyEvent e) {
		double v = getValue();
		
		
//		// now need to put that into the fields that
//		// are not currently shown so that they are
//		// ready if needed. 
//
//		if (e != null) {
//			setValue(v, true);
//		}

		// and say the formated version
		sayFormattedValue(v);
	}
	
	
	/**
	 * Change the current controls for to show to show the current format of Latitude or Longitude.
	 * @param formatStyle - the style of Latitude or longitude e.g. LatLong.FORMAT_DECIMALMINUTES;
	 */
	public void showControls(int formatStyle) {
		
		showControls(formatStyle, false);
	}

	/**
	 * Change the current controls for to show to show the current format of Latitude or Longitude.
	 * @param force - force a reset of controls even if the format style is the same as the current style. 
	 * @param formatStyle - the style of Latitude or longitude e.g. LatLong.FORMAT_DECIMALMINUTES;
	 */
	private void showControls(int formatStyle, boolean force) {
		
		if (formatType==formatStyle && !force) {
			return;
		}
		
		//important this comes before setting format style. 
		double currentValue = getValue(); 

		this.formatType = formatStyle; 

		degHBox.getChildren().clear(); 
				
		System.out.println("FORMATSTYLE: " + formatStyle + " val " + currentValue); 
		
		switch (formatType) {
		case LatLong.FORMAT_DECIMALMINUTES:
			degHBox.getChildren().add(dl); 
			degHBox.getChildren().add(degrees); 
			degHBox.getChildren().add(dml); 
			degHBox.getChildren().add(decminutes); 
			break;
		case LatLong.FORMAT_MINUTESSECONDS:
			degHBox.getChildren().add(dl); 
			degHBox.getChildren().add(degrees); 
			degHBox.getChildren().add(ml); 
			degHBox.getChildren().add(minutes); 
			degHBox.getChildren().add(sl); 
			degHBox.getChildren().add(seconds); 
			break;
		case LatLong.FORMAT_DECIMAL:
			degHBox.getChildren().add(dec); 
			degHBox.getChildren().add(decimal); 
			break;

		}
		
		setValue(currentValue);
		
		sayFormattedValue(getValue());
	}

	/**
	 * Set data in the lat long dialog strip
	 * @param value Lat or Long in decimal degrees.
	 */
	public void setValue(double value) {
		setValue(value, false);
	}

	public void setValue(double value, boolean hiddenOnly) {
		
//		System.out.println("Set value: " + value);
		if (value >= 0) {
			nsew.getSelectionModel().select(0);
		}
		else {
			nsew.getSelectionModel().select(1);
		}

		double deg = LatLong.getSignedDegrees(value);
		//		System.out.println("Deg: " + LatLong.getSignedDegrees(value) + " value: " +value);


		//		if (degrees.isVisible() == false || !hiddenOnly) degrees.setText(String.format("%d", (int)Math.abs(deg)));
		//		if (minutes.isVisible() == false || !hiddenOnly) minutes.setText(String.format("%d", LatLong.getIntegerMinutes(value)));
		//		if (decminutes.isVisible() == false || !hiddenOnly) decminutes.setText(String.format("%3.5f", LatLong.getDecimalMinutes(value)));
		//		if (seconds.isVisible() == false || !hiddenOnly) seconds.setText(String.format("%3.5f", LatLong.getSeconds(value)));
		//		if (nsew.isVisible() == false || !hiddenOnly) nsew.getSelectionModel().select(deg >= 0 ? 0 : 1);
		//		if (decimal.isVisible() == false || !hiddenOnly) decimal.setText(String.format("%.8f", value));

		switch (formatType) {
		case LatLong.FORMAT_DECIMALMINUTES:

			degrees.setText(String.format("%d", (int)Math.abs(deg)));
			decminutes.setText(String.format("%3.5f", LatLong.getDecimalMinutes(value)));

			break;
		case LatLong.FORMAT_MINUTESSECONDS:

			degrees.setText(String.format("%d", (int)Math.abs(deg)));
			minutes.setText(String.format("%d", LatLong.getIntegerMinutes(value)));
			seconds.setText(String.format("%3.5f", LatLong.getSeconds(value)));

			break;
		case LatLong.FORMAT_DECIMAL:
			
			decimal.setText(String.format("%.8f", value));
			
			break;

		}

		sayFormattedValue(value);
	}

	/**
	 * Get the value for the latitude or longitude in decimal
	 * @return the value - the value in decimal
	 */
	public double getValue() {
		
		double deg = 0;
		double min = 0;
		double sec = 0;
		double sin = 1.;
		
		if (nsew.getSelectionModel().getSelectedIndex() == 1) sin = -1.;


		if (formatType == LatLong.FORMAT_DECIMAL){
			try {
				deg = Double.valueOf(decimal.getText());
				return deg; 
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
		}

		try {
			deg = Integer.valueOf(degrees.getText());
		}
		catch (NumberFormatException Ex) {
			return Double.NaN;
		}


		if (formatType == LatLong.FORMAT_DECIMALMINUTES){
			try {
				min = Double.valueOf(decminutes.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
		}
		else {
			try {
				min = Integer.valueOf(minutes.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}
			try {
				sec = Double.valueOf(seconds.getText());
			}
			catch (NumberFormatException ex) {
				return Double.NaN;
			}

		}
		deg += min/60 + sec/3600;
		deg *= sin;
		return deg;
	}

	/**
	 * Clear the latitude/longitude data from the pane. 
	 */
	public void clearData() {
		degrees.setText("");
		minutes.setText("");
		seconds.setText("");
		decminutes.setText("");
		decimal.setText("");
	}


	public void sayFormattedValue(double value) {
		if (isLatitude) {
			formattedText.setText(LatLong.formatLatitude(value));
		}
		else {
			formattedText.setText(LatLong.formatLongitude(value));
		}
	}
	//	public boolean isDecimalMinutes() {
	//		return decimalMinutes;
	//	}
	//	public void setDecimalMinutes(boolean decimalMinutes) {
	//		this.decimalMinutes = decimalMinutes;
	//		showControls();
	//	}

	/**
	 * Set the pane to be enabled or disabled. 
	 * @param enabled -true to enable the pane. 
	 */
	public void setEnabled(boolean enabled) {
		super.setDisable(!enabled);
		degrees.setDisable(!enabled);
		minutes.setDisable(!enabled);
		seconds.setDisable(!enabled);
		decminutes.setDisable(!enabled);
		nsew.setDisable(!enabled);
		decimal.setDisable(!enabled);
	}

	/**
	 * Get the title label. 
	 * @return the title label. 
	 */
	public Label getTitleLabel() {
		return titleLabel;
	}


}
