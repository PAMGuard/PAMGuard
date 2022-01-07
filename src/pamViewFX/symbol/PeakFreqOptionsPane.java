package pamViewFX.symbol;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.PeakFreqModifier;
import PamView.symbol.modifier.PeakFreqSymbolOptions;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.comboBox.ColorComboBox;
import pamViewFX.fxNodes.utilsFX.ColourArray;

/**
 * Option pane for the PeakFreqSymbolChooser. Allows users to change the max and min frequency and 
 * the colour map. 
 * @author Jamie Macaulay 
 *
 */
public class PeakFreqOptionsPane extends StandardSymbolModifierPane  {

	/**
	 * Spinner for min search frequency
	 */
	private PamSpinner<Double> minFreq;

	/**
	 * Spinner for max search frequency. 
	 */
	private PamSpinner<Double> maxFreq;

	/**
	 * Sets the colours to use. 
	 */
	private ColorComboBox colourBox;

	/**
	 * True if everything has initialised; 
	 */
	private boolean initialised =false;

	/**
	 * The frequency pane
	 */
	private Pane freqPane;

	
	public PeakFreqOptionsPane(PeakFreqModifier symbolModifer) {
		super(symbolModifer, Orientation.HORIZONTAL, true, 0);
		this.setBottom(createFreqPane());
		initialised=true; 
	}

	/**
	 * Pane which changes the frequency limits. 
	 * @return pane with controls to change freq limits. 
	 */
	private Pane createFreqPane(){

		PamVBox holder = new PamVBox();
		holder.setSpacing(5);

		PamGridPane freqholder = new PamGridPane(); 
		freqholder.setHgap(5);
		freqholder.setVgap(5);
		freqholder.setAlignment(Pos.CENTER);

		minFreq = new PamSpinner<Double>(0., 10000000., 0., 1000.); 
		minFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		minFreq.setEditable(true);
		minFreq.valueProperty().addListener((obsVal, oldVal, newVal)->{
			//hmmmm...don't like this null settings thing. 
			getParams();
			notifySettingsListeners();
		});
		//minFreq.setPrefWidth(100);

		maxFreq = new PamSpinner<Double>(0., 10000000., Math.max(1000.,getSymbolModifier().getSymbolChooser().getPamDataBlock().getSampleRate()/2.), 1000.); 
		maxFreq.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		maxFreq.setEditable(true);
		//maxFreq.setPrefWidth(100);
		maxFreq.valueProperty().addListener((obsVal, oldVal, newVal)->{
			getParams();
			notifySettingsListeners();
		});

		freqholder.add(new Label("Min. freq (Hz)"), 0,0);
		freqholder.add(new Label("Max. freq (Hz)"), 1,0);
		freqholder.add(minFreq, 0,1);
		freqholder.add(maxFreq, 1,1);

		colourBox = new ColorComboBox(ColorComboBox.COLOUR_ARRAY_BOX); 
		colourBox.setPrefWidth(100); 
		colourBox.setOnAction((action)->{                
			getParams();
			//change the colour of the colour range slider.     
			notifySettingsListeners();
		});



		freqholder.add(new Label("Freq. colour scale"), 0,2);
		freqholder.add(colourBox, 1,2);

		//		Label freqLabel = new Label("Freq. colour options"); 
		//		PamGuiManagerFX.titleFont2style(freqLabel);

		holder.getChildren().addAll(freqholder);

		holder.setPadding(new Insets(10,0,5,0));
		return holder;

	}

	private float getSampleRate() {
		return getSymbolModifier().getSymbolChooser().getPamDataBlock().getSampleRate(); 
	}

	@Override
	public StandardSymbolOptions getParams(){
		StandardSymbolOptions standardSymbolOptions  = super.getParams(); 

		//bit messy but works / 
//		PeakFreqSymbolOptions symbolOptions = (PeakFreqSymbolOptions) standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());

		PeakFreqSymbolOptions symbolOptions =  (PeakFreqSymbolOptions) this.getSymbolModifier().getSymbolModifierParams(); 
		if (initialised) {
			symbolOptions.freqLimts=new double[] {minFreq.getValue(), maxFreq.getValue()};
			symbolOptions.freqColourArray = ColourArray.getColorArrayType(this.colourBox.getSelectionModel().getSelectedItem()); 

		}
		((PeakFreqModifier) this.getSymbolModifier()).checkColourArray(); 
		//System.out.println("StandardSymbolModifierPane : getParams(): new mod: " +mod); 

		return standardSymbolOptions; 

	}

	@Override
	public void setParams() {

		if (initialised) {
			setParams = true; 

			super.setParams();

//			StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) getSymbolModifier().getSymbolChooser().getSymbolOptions();
//			PeakFreqSymbolOptions symbolOptions = (PeakFreqSymbolOptions) standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());
			PeakFreqSymbolOptions symbolOptions =  (PeakFreqSymbolOptions) this.getSymbolModifier().getSymbolModifierParams(); 

			//now set frequency params; 
			checkFreqLimits( symbolOptions ) ;
			minFreq.getValueFactory().setValue(symbolOptions.freqLimts[0]);
			maxFreq.getValueFactory().setValue(symbolOptions.freqLimts[1]);

			colourBox.setValue(symbolOptions.freqColourArray);

			setParams = false; 

		}
	} 

	/**
	 * Check the frequency limits make sense for the given datablock 
	 * @param symbolOptions - the peak frequency options. 
	 */
	private void checkFreqLimits(PeakFreqSymbolOptions symbolOptions ) {

		DoubleSpinnerValueFactory spinnerValFact = (DoubleSpinnerValueFactory) maxFreq.getValueFactory(); 
		spinnerValFact.maxProperty().set(getSampleRate() /2);


		if (symbolOptions.freqLimts==null) {
			symbolOptions.freqLimts= new double[] {0, getSampleRate() /2}; 
		}
		// System.out.println("HERE HERE: " + symbolOptions.freqLimts + " clickSymbolChooser: " + clickSymbolChooser );
		//check nyquist for upper limit
		if (symbolOptions.freqLimts[1]>getSampleRate() /2) {
			symbolOptions.freqLimts[1]=getSampleRate() /2; 
		}
		//check nyquist for lower limit
		if (symbolOptions.freqLimts[0]>getSampleRate() /2) {
			symbolOptions.freqLimts[0]=0; 
		}
	}

	
	/**
	 * Get the pane that holds the frequency controls. 
	 * @return the frequency pane. 
	 */
	public Pane getFreqPane() {
		return freqPane;
	}




}
