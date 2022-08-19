package pamViewFX.fxNodes.utilityPanes;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;


/**
 * A simple control which has a high and low pass editiable frequency spinner. 
 * @author Jamie Macaulay
 *
 */
public class FreqBandPane extends PamGridPane {
	

	/**
	 * High pass spinner.
	 */
	private PamSpinner<Double> highPassFreq;

	/**
	 * Low pass frequency spinner.
	 */
	private Spinner<Double> lowPassFreq;

	/**
	 * Layout of the pane- either horizontal or vertical
	 */
	private Orientation orientation=Orientation.VERTICAL;

	/**
	 * Label for high pass or alternatively could call this low cut
	 */
	private Label highPassLabel;
	
	/**
	 * The current samplerate. 
	 */
	private double sampleRate=500000;


	/**
	 * Low pass label. Could also be called high cut. 
	 */
	private Label lowPassLabel;

	private Label highPassHzLabel;

	/**
	 * Create a FreqBandPane. This holds a high and low frequency spinner.
	 */
	public FreqBandPane(){
		createPane();
	}

	public FreqBandPane(Orientation orientation){
		this.orientation=orientation;
		createPane();
	}

	private void createPane(){
		
		this.setHgap(5);
		this.setVgap(5);
	
		highPassFreq=new PamSpinner<Double>(10.,500000.,1000.,2000.);
		highPassFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		highPassFreq.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after>=lowPassFreq.getValue()) lowPassFreq.getValueFactory().setValue(Math.min(sampleRate/2.,highPassFreq.getValue()+100)); 
			if (after>sampleRate/2.) highPassFreq.getValueFactory().setValue(sampleRate/2.); 
		});
		highPassFreq.setEditable(true);
		//highPassFreq.setPrefWidth(140);
		
		//highCut.setPrefColumnCount(6);
		if (orientation==Orientation.VERTICAL){
			this.add(highPassLabel=new Label("High Pass"), 0, 0);
			this.add(highPassFreq, 1, 0);
			this.add(highPassHzLabel=new Label("Hz"), 2, 0);
		}
		else{
			this.add(highPassLabel=new Label("High Pass"), 0, 0);
			this.add(highPassFreq, 1, 0);
			this.add(highPassHzLabel=new Label("Hz"), 2, 0);
		}

		lowPassFreq=new PamSpinner<Double>(1.,500000.,2000.,2000.);
		lowPassFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		lowPassFreq.getValueFactory().valueProperty().addListener((obs, before, after)->{
			if (after<=highPassFreq.getValue()) highPassFreq.getValueFactory().setValue(Math.min(0,lowPassFreq.getValue()-100));
			if (after>sampleRate/2.) lowPassFreq.getValueFactory().setValue(sampleRate/2.); 
			
		});
		lowPassFreq.setEditable(true);
		//lowPassFreq.setPrefWidth(140);

		
		if (orientation==Orientation.VERTICAL){
			this.add(lowPassLabel=new Label("Low Pass"), 0, 1);
			this.add(lowPassFreq, 1, 1);
			this.add(new Label("Hz"), 2, 1);
		}
		else{
			this.add(lowPassLabel=new Label("Low Pass"), 3, 0);
			this.add(lowPassFreq, 4, 0);
			this.add(new Label("Hz"), 5, 0);
		}
	}
	
	/**
	 * Get the high pass frequency spinner control. 
	 * @return the high pass frequency control. 
	 */
	public PamSpinner<Double> getHighPassFreq() {
		return highPassFreq;
	}

	/**
	 * Get the low pass frequency spinner control
	 * @return the low pass frequency spinner control. 
	 */
	public Spinner<Double> getLowPassFreq() {
		return lowPassFreq;
	}

	/**
	 * Get the label for the high pass frequency spinner. Use this to change the lable text. 
	 * @return the label for the high pass spinner
	 */
	public Label getHighPassLabel() {
		return highPassLabel;
	}

	
	/**
	 * Get the current sample rate. 
	 * @return the sample rate in S/s (Hz)
	 */
	public double getSampleRate() {
		return sampleRate;
	}

	/**
	 * Set the sample rate. This sets the upper bounds of the frequency spinners. 
	 * @param sampleRate the sample rate in S/s (Hz)
	 */
	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public Label getLowPassLabel() {
		return lowPassLabel;
	}
	
	/**
	 * Sets the pane to a band type text. This is "bandName" **TextBox** to **TextBox** Hz
	 * Generally this should only be used if the pane has a horizontal layout 
	 * @param the name of the band- is insfront of the the text boxes. 
	 */
	public void setBandText(String bandname){
		highPassLabel.setText(bandname);
		lowPassLabel.setText("to");
		highPassHzLabel.setText("");

	}
	
	/**
	 * Disable or enable the pane. 
	 */
	public void setDisableFreqPane(boolean disable){
		highPassFreq.setDisable(disable);
		lowPassFreq.setDisable(disable);
	}
	
	
	/**
	 * Set to filter text. i.e. high pass and low pass text labels. 
	 */
	public void setFilterText(){
		//Tdo
	}



}
