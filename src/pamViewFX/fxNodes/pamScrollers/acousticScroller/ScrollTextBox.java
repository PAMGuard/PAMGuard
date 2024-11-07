package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import javafx.scene.control.TextField;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;

public class ScrollTextBox extends PamHBox {
	
	private TextField textBox;
	
	public ScrollTextBox() {
		textBox = new TextField();
		this.getChildren().add(textBox); 
		this.getChildren().add(new PamButton("xx")); 

	}
	
	/**
	 * 
	 * @param textBoxText
	 * @return the visible range to set  in millis 
	 */
	public static double getTextBoxValue(String textBoxText, boolean showMillis) {

		double millis=-1; 

		/**
		 * Three possible inputs. 1) number in seconds e.g. number with letter e.g.10s for 10 seconds or 10m for 10 millis) time e.g. 00:00:01
		 */
		String formatted = null; 
		try {
			if (textBoxText.contains("ms")){
				//find number 
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= Double.valueOf(formatted); 

			}
			else if (textBoxText.contains("s")){
				//find number 
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1000.); 

			}
			else if(textBoxText.contains("m")){
				formatted = textBoxText.replaceAll("[^.?0-9]+", " ");
				millis= (Double.valueOf(formatted)*1.); 

			}
			else if(textBoxText.contains(":")){
				String[] vals=textBoxText.split(":");

				int days=0; 
				int minutes=0; 
				int seconds=0; 
				if (vals.length==2){
					minutes=Integer.valueOf(vals[0]); 
					seconds=Integer.valueOf(vals[1]); 
				}
				else if (vals.length==3){
					days=Integer.valueOf(vals[0]); 
					minutes=Integer.valueOf(vals[1]); 
					seconds=Integer.valueOf(vals[2]); 
				}

				double totalSeconds=days*60*60 + minutes*60 + seconds; 
				millis=(totalSeconds*1000); 

			}
			else {
				// the value in seconds. 
				millis=(showMillis ? Double.valueOf(textBoxText) :  Double.valueOf(textBoxText)*1000); 
				return millis; 
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1.; 
		}

		return millis;
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
	

}
