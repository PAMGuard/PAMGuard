package dataPlotsFX.spectrogramPlotFX;


import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import PamUtils.FrequencyFormat;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.scrollingPlot2D.Plot2DControPane;


/**
 * Pane which quickly allows users to change spectrogram settings
 * @author Jamie Macaulay
 * 
 */
public class TDSpectrogramControlPane extends Plot2DControPane implements TDSettingsPane {

	/**
	 * Reference to the FFTPlotInfo for this pane
	 */
	private FFTPlotInfo fftPlotInfo;

	/**
	 * Reference to the TDGraph for this pane. 
	 */
	private TDGraphFX tdGraph; 
//
//	/**
//	 * Frequencies can be shown in kHz to stop very large numbers on axis. True if frequencies are to be represented in kHz rather than Hz. 
//	 */
//	double frequencyScale = 1;

//	/**
//	 * Value at which the slider switches from Hz to kHz
//	 */
//	private static int convertTokHz=2000;
	


	private Canvas icon; 

	public TDSpectrogramControlPane(TDGraphFX tdGraph, FFTPlotInfo fftPlotInfo){
		super(fftPlotInfo, tdGraph);
		this.tdGraph=tdGraph;
		this.fftPlotInfo=fftPlotInfo;

		createIcon();

		SpectrogramParamsFX spectrogramParams = (SpectrogramParamsFX) fftPlotInfo.getPlot2DParameters();

		setFrequencyProperties(spectrogramParams.getFrequencyLimits());

		setIconColour(fftPlotInfo.getPlot2DParameters().getColourMap());

	}

	/**
	 * Create icon for the pane 
	 */
	private void createIcon() {
		icon=new Canvas(20,20);
	}

	/**
	 * Set listeners, min max frequency, frequency low and high values. 
	 * @param frequencyLimits. Double Properties for frequency min and max. frequencyLimits[0]=min frequency (Hz). frequencyLimits[1]=max frequency (hZ)
	 */
	public void setFrequencyProperties(DoubleProperty[] frequencyLimits){
		//set the limits of the frequency slider. 
		setFrequencyRange(frequencyLimits);

		//set the correct binding so frequency changes with slider
		setFrequencyBinding(frequencyLimits);

		//add listeners to repaint when frequency changes. 
		addFrequencyListeners(frequencyLimits);
	} 


	/**
	 * Add listeners to min/max double property for frequency limits. Will change
	 * frequency limits whenever frequency scale bar is changed. Will also repaint
	 * tdGraph
	 * 
	 * @param frequencyLimits- the frequency limits to add listeners for spectrogram
	 *                         repaint.
	 */
	private void addFrequencyListeners(DoubleProperty[] frequencyLimits){
		//add listeners to frequency limits;
		frequencyLimits[0].addListener((obserVal, oldVal, newVal)->{
			fftPlotInfo.calcFrequencyRangeDisplay();
			tdGraph.repaint(0);
		});

		frequencyLimits[1].addListener((obserVal, oldVal, newVal)->{
			fftPlotInfo.calcFrequencyRangeDisplay();
			tdGraph.repaint(0);
		});
	}

	/**
	 * Set the colour of the icon which sits on hiding tab pane. Recolours so similar to spectrogram colour array
	 */
	private void setIconColour(){
		setIconColour( getColourArrayType());
	}

	/**
	 * Set the colour of the icon which sits on hiding tab pane. Recolours so similar to spectrogram colour array
	 */
	private void setIconColour(ColourArrayType colourArrayType){
		icon.getGraphicsContext2D().setFill(ColourArray.getLinerGradient(Orientation.VERTICAL, icon.getHeight(), colourArrayType));
		icon.getGraphicsContext2D().fillRect(0, 0, 20, 20);
	}

	//	/**
	//	 * Check whether the slider is displaying in units of Hz or kHz. 
	//	 * @return true if displaying in units of kHz. 
	//	 */
	//	public boolean isKhz(){
	//		return kHz; 
	//	}

//	/**
//	 * Frequency scale = 1 for Hz and 1000 for kHz. 
//	 * @return the frequencyScale
//	 */
//	public double getFrequencyScale() {
//		return frequencyScale;
//	}

	/**
	 * Set the frequency range of the slider. 
	 * @param frequencyLimits- the frequency limits to set frequency range for.
	 */
	public void setFrequencyRange(DoubleProperty[] frequencyLimits){
		double min=0; //the min frequency value will always be zero
		double max=Math.max(1,fftPlotInfo.getDataBlock2D().getSampleRate()/2.);


		double minVal=frequencyLimits[0].get();
		double maxVal=frequencyLimits[1].get();
		//the frequency limits may not have been intialised. 
		if (minVal==maxVal){
			//need to set the frequency limits manually here or will stay between 0 and 1
			minVal=min; 
			maxVal=max; 
		}


		if (minVal>=max) minVal=0;
		if (maxVal>max) maxVal=max; 

		//check to see if using kHz; 
		
		
		FrequencyFormat format = FrequencyFormat.getFrequencyFormat(max);
		getFreqLabel().setText("Frequency (" + format.getUnitText() + ")"); 
		
//		///scale the frequency by the correct value.
//		max=max/frequencyScale;
//		minVal=minVal/frequencyScale;
//		maxVal=maxVal/frequencyScale;

		getFrequencySlider() .setMin(min);
		getFrequencySlider() .setMax(max);  //Nyquist
		//assume that the params have been sorted so theat the frequency values are not greater than Nyquist
		getFrequencySlider() .setLowValue(minVal);
		getFrequencySlider() .setHighValue(maxVal);
		//set tick mark spacing
		getFrequencySlider().majorTickUnitProperty().setValue((max-min)/4);
		getFrequencySlider().majorTickUnitProperty().setValue((max-min)/(4*4));
		
		getFrequencySlider().setLabelFormatter(new FrequencyStringConverter(format));

	}




	/**
	 * Need to set the binding depending on whether kHz are being used or not. 
	 * frequencyLimits- the frequency limits to set bin to range slider. 
	 */
	private void setFrequencyBinding(DoubleProperty[] frequencyLimits){
		//		frequencyLimits[0].unbind();
		//		frequencyLimits[1].unbind();
		//			frequencyLimits[0].bind(getFrequencySlider() .lowValueProperty().multiply(frequencyScale));
		//			frequencyLimits[1].bind(getFrequencySlider() .highValueProperty().multiply(frequencyScale));

		getFrequencySlider() .lowValueProperty().addListener((obsVal, oldVal, newVal)->{
			frequencyLimits[0].setValue(newVal.doubleValue());
		});

		getFrequencySlider() .highValueProperty().addListener((obsVal, oldVal, newVal)->{
			frequencyLimits[1].setValue(newVal.doubleValue());
		});

	}

	/**
	 * Set the position of the min thumb of the frequency slider. Note that since the frequency sliders binds to min/max val in spectrogram params
	 * this will also set the minimum value in associated SpectrogramParamsFX class. 
	 * @param frequency- frequency in Hz to set min value to. 
	 */
	public void setMinFrequency(double frequency){
		if (frequency<0) return;
		double sliderVal=frequency;
		getFrequencySlider() .lowValueProperty().setValue(sliderVal);
	}

	/**
	 * Set the position of the max thumb of the frequency slider. Note that since the frequency sliders binds to min/max val in spectrogram params
	 * this will also set the maximum value in associated SpectrogramParamsFX class. 
	 * @param frequency- frequency in Hz to set maximum value to. 
	 */
	public void setMaxFrequency(double frequency){
		if (frequency<0) return;
		double sliderVal=frequency;
		getFrequencySlider().highValueProperty().setValue(sliderVal);
	}

	/**
	 * Get the maximum allowable value from the frequency slider. 
	 * @return maximum allowed value of the range. 
	 */
	public double getMaxFrequencyRange() {
		return getFrequencySlider().getMax();
	}

	/**
	 * Get the TDGraph this spectrogram control pane is associated wit. 
	 * @return the tdGraph which shows the spectrogram control pane. 
	 */
	public TDGraphFX getTDGraph() {
		return tdGraph;
	}

	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return fftPlotInfo.getShortName();
	}

	@Override
	public Node getShowingIcon() {
		return null;
	}

	@Override
	public Pane getPane() {
		return this;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Plot2DControPane#colourBoxChange()
	 */
	@Override
	public void colourBoxChange() {
		super.colourBoxChange();
		//set icon to correct colour. 
		setIconColour();
	}


}
