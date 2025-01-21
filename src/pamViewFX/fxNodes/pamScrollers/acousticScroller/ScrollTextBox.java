package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;

public class ScrollTextBox extends PamHBox {
	
	/**
	 * The text field 
	 */
	private TextField textBox;
	
	/**
	 * Button whihc can be used to select different ranges.
	 */
	private MenuButton rangeButton;
	
	/**
	 * List of default range values that appear in the menu. 
	 */
	private Number[] defaultRangeValues;
	
	/**
	 * The converter for converting strings to numbers and vice versa. 
	 */
	private DurationStringConverter stringConverter = new DurationStringConverter();

	private boolean showMillis; 
	
	/**
	 * 
	 */
	private  double[] defaultDurations = new double[] {60000., 5*60000., 3600*1000., 86400*1000., 7*86400*1000., 30*86400*1000.};
	
	public ScrollTextBox() {
		textBox = new TextField();
		this.getChildren().add(textBox); 
		
		rangeButton =new MenuButton();
//		rangeButton.setGraphic( PamGlyphDude.createPamIcon("mdi2m-menu-down", PamGuiManagerFX.iconSize));
		
		rangeButton.setStyle("-fx-border-radius: 0 5 5 0; -fx-background-radius: 0 5 5 0;");
			
		setRangeButtonVisible(false);
		setDefaultDurations(defaultDurations);
	}

	/**
	 * Set the default durations. These are shown in the range button. 
	 * @param durations - the durations to set.
	 */
	public void setDefaultDurations(double[] durations) {
		rangeButton.getItems().clear();
		for (int i=0; i<durations.length; i++) {
			rangeButton.getItems().add(new MenuItem(stringConverter.toString(durations[i]))); 
		}
	}
	
	public void setText(String format) {
		this.textBox.setText(format);
		
	}

	public TextField getTextBox() {
		return this.textBox;
	}

	public String getText() {
		return 	this.textBox.getText();
	}

	public void setPrefColumnCount(int i) {
		this.textBox.setPrefColumnCount(i);
		
	}

	/**
	 * Set whether the a button to select a custom set of ranges is availble. Usually these will be times. 
	 * @param b - true to show the button. 
	 */
	public void setRangeButtonVisible(boolean b) {
		if (b && !this.getChildren().contains(rangeButton)) {
			textBox.setStyle("-fx-border-radius: 5 0 0 5; -fx-background-radius: 5 0 0 5;");
			this.getChildren().add(rangeButton);
		}
		else {
			textBox.setStyle("-fx-border-radius: 5 5 5 5; -fx-background-radius: 5 5 5 5;");
			this.getChildren().remove(rangeButton);
		}

	}

	/**
	 * Get the duration set in the text box in millis. 
	 * @param showMillis - true of the text box only shows millis.
	 * @return
	 */
	public Number getTextBoxDuration() {
		return stringConverter.fromString(getText());
	}

	/**
	 * Set the duration in the text box as a formatted time string.  
	 * @param visAmount the duration to set in millis. 
	 */
	public void setTextBoxMillis(double visAmount) {
		String text = stringConverter.toString(visAmount);
		setText(text);
	}
	
	
	/**
	 * Check whether the scroll bar's default <b>display</b> units are millis 
	 * (note that stored units for calculations always remain milliseconds)
	 * @return true if the display units are millis
	 */
	public boolean isShowMillis() {
		return showMillis;
	}

	/**
	 * Set whether the scroll bar's default <b>display</b> units to milliseconds
	 * (note that stored units for calculations always remain milliseconds)
	 * @param true if the display units are millis
	 */
	public void setShowMillis(boolean showMillis) {
		this.showMillis = showMillis;
		stringConverter.setShowMillis(showMillis);
	}
	

}
