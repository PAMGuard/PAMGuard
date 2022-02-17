package clickTrainDetector.layout.classification.idiClassifier;

import clickTrainDetector.classification.idiClassifier.IDIClassifierParams;
import clickTrainDetector.classification.templateClassifier.TemplateClassifierParams;

import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.MinMaxPane;
import pamViewFX.fxNodes.utilityPanes.MinMaxPane.MinMaxParams;

/**
 * 
 * Pan which allows users to select IDI measurement limits for click trains. 
 * 
 * @author Jamie Macaulay
 *
 */
public class IDIPane extends PamBorderPane {
	
	/**
	 * The median IDI min max pane
	 */
	private MinMaxPane<Double> medianMinMax;  
	
	/**
	 * The mean IDI min max pane
	 */
	private MinMaxPane<Double> meanMinMax;  

	/**
	 * The standard deviation min max pane. 
	 */
	private MinMaxPane<Double> stdMinMax;  

	
	public IDIPane(){
		this.setCenter(createIDIPane());
	}

	/**
	 * Create the IDI pane.
	 * @return the IDI pane
	 */
	private Pane createIDIPane() {
		
		medianMinMax = new MinMaxPane<Double>("Median IDI", "s", 0., 100000., 0.2); 
			
		meanMinMax = new MinMaxPane<Double>("Mean IDI", "s", 0., 100000., 0.2); 

		stdMinMax = new MinMaxPane<Double>("Std IDI", "s", 0., 100000., 0.2); 
		
		medianMinMax.setConverter(PamSpinner.createStringConverter(5));
		meanMinMax.setConverter(PamSpinner.createStringConverter(5));
		stdMinMax.setConverter(PamSpinner.createStringConverter(5));

		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(5);
		vBox.getChildren().addAll(medianMinMax, meanMinMax, stdMinMax); 
		
  		return vBox;
	}
	
	/**
	 * Set IDI parameters.
	 * @param idiClassifierParams - parameters to set. 
	 */
	public void setParams(IDIClassifierParams idiClassifierParams) {
		
		medianMinMax.setParams(idiClassifierParams.minMedianIDI, idiClassifierParams.maxMedianIDI, idiClassifierParams.useMedianIDI);
		
		meanMinMax.setParams(idiClassifierParams.minMeanIDI, idiClassifierParams.maxMeanIDI, idiClassifierParams.useMeanIDI);

		stdMinMax.setParams(idiClassifierParams.minStdIDI, idiClassifierParams.maxStdIDI, idiClassifierParams.useStdIDI);

	}
	
	/**
	 * Get IDI parameters.
	 * @param idiClassifierParams - parameter class to add IDI parameters settings to./ 
	 * @return altered parameters class. 
	 */
	public IDIClassifierParams getParams(IDIClassifierParams idiClassifierParams) {
		
		@SuppressWarnings("rawtypes")
		MinMaxParams minMaxParams = medianMinMax.getParams(); 
		
		idiClassifierParams.minMedianIDI = (Double) minMaxParams.min;
		idiClassifierParams.maxMedianIDI = (Double) minMaxParams.max;
		idiClassifierParams.useMedianIDI = minMaxParams.enabled;
		
		 minMaxParams = meanMinMax.getParams(); 
		
		idiClassifierParams.minMeanIDI = (Double) minMaxParams.min;
		idiClassifierParams.maxMeanIDI = (Double) minMaxParams.max;
		idiClassifierParams.useMeanIDI = minMaxParams.enabled;
		
		minMaxParams = stdMinMax.getParams(); 
		
		idiClassifierParams.minStdIDI = (Double) minMaxParams.min;
		idiClassifierParams.maxStdIDI = (Double) minMaxParams.max;
		idiClassifierParams.useStdIDI = minMaxParams.enabled;
		
		return idiClassifierParams; 
	}
	

}
