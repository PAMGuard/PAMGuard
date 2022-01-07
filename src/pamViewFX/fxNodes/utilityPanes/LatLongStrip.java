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
 * (copied ot FX from Doug Gillespie's LatLongDialogStrip)
 * @author Jamie Macaulay 
 *
 */
public class LatLongStrip extends PamBorderPane {
	
	Label formattedText;
	TextField degrees, minutes, seconds, decminutes;
	Label dl, ml, sl, dml;
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
		
		String borderTitle;
		if (isLatitude) borderTitle = "Latitude";
		else borderTitle = "Longitude";
		
		Label title= new Label(borderTitle);
		PamGuiManagerFX.titleFont2style(title);
//		title.setFont(PamGuiManagerFX.titleFontSize2);

		degrees = new TextField();
		degrees.setPrefColumnCount(4);
		minutes = new TextField();
		minutes.setPrefColumnCount(3);

		seconds = new TextField();
		seconds.setPrefColumnCount(6);
		decminutes = new TextField();
		decminutes.setPrefColumnCount(6);
				
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
		
		nsew.setOnKeyPressed((key)->{
			newTypedValues(key); 
		});

		
		degHBox = new PamHBox();
		degHBox.setSpacing(5);
		degHBox.setAlignment(Pos.CENTER_LEFT);
			
		this.setRight(nsew);
		this.setCenter(degHBox);
		this.setBottom(formattedText);

		showControls( LatLong.FORMAT_DECIMALMINUTES);
	}

	private void newTypedValues(KeyEvent e) {
		double v = getValue();
		// now need to put that into the fields that
		// are not currently shown so that they are
		// ready if needed. 
		
		if (e != null) {
			sayValue(v, true);
		}
		
		// and say the formated version
		sayFormattedValue(v);
	}
	
	public void showControls(int formatStyle) {
		boolean decimal = (formatStyle == LatLong.FORMAT_DECIMALMINUTES);
		degHBox.getChildren().clear(); 
		if (decimal) {
			degHBox.getChildren().add(dl); 
			degHBox.getChildren().add(degrees); 
			degHBox.getChildren().add(dml); 
			degHBox.getChildren().add(decminutes); 
		}
		else {
			degHBox.getChildren().add(dl); 
			degHBox.getChildren().add(degrees); 
			degHBox.getChildren().add(ml); 
			degHBox.getChildren().add(minutes); 
			degHBox.getChildren().add(sl); 
			degHBox.getChildren().add(seconds); 
		}
		minutes.setVisible(decimal == false);
		ml.setVisible(decimal == false);
		seconds.setVisible(decimal == false);
		sl.setVisible(decimal == false);
		decminutes.setVisible(decimal);
		dml.setVisible(decimal);
		sayFormattedValue(getValue());
	}
	
	/**
	 * Set data in the lat long dialog strip
	 * @param value Lat or Long in decimal degrees.
	 */
	public void sayValue(double value) {
		sayValue(value, false);
	}
	
	public void sayValue(double value, boolean hiddenOnly) {
		if (value >= 0) {
			nsew.getSelectionModel().select(0);
		}
		else {
			nsew.getSelectionModel().select(1);
		}
		
		double deg = LatLong.getSignedDegrees(value);
//		System.out.println("Deg: " + LatLong.getSignedDegrees(value) + " value: " +value);
		
		if (degrees.isVisible() == false || !hiddenOnly) degrees.setText(String.format("%d", (int)Math.abs(deg)));
		if (minutes.isVisible() == false || !hiddenOnly) minutes.setText(String.format("%d", LatLong.getIntegerMinutes(value)));
		if (decminutes.isVisible() == false || !hiddenOnly) decminutes.setText(String.format("%3.5f", LatLong.getDecimalMinutes(value)));
		if (seconds.isVisible() == false || !hiddenOnly) seconds.setText(String.format("%3.5f", LatLong.getSeconds(value)));
		if (nsew.isVisible() == false || !hiddenOnly) nsew.getSelectionModel().select(deg >= 0 ? 0 : 1);
		sayFormattedValue(value);
	}
	
	/**
	 * Get the value for the latitude and longitude 
	 * @return the value. 
	 */
	public double getValue() {
		double deg = 0;
		double min = 0;
		double sec = 0;
		double sin = 1.;
		if (nsew.getSelectionModel().getSelectedIndex()== 1) sin = -1.;
		try {
		  deg = Integer.valueOf(degrees.getText());
		}
		catch (NumberFormatException Ex) {
			return Double.NaN;
		}
		if (LatLong.getFormatStyle() == LatLong.FORMAT_DECIMALMINUTES){
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
	}
}
