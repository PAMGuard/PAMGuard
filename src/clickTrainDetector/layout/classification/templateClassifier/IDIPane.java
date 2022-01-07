package clickTrainDetector.layout.classification.templateClassifier;

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
	 * @param templateClassifierParams - parameters to set. 
	 */
	public void setParams(TemplateClassifierParams templateClassifierParams) {
		
		medianMinMax.setParams(templateClassifierParams.minMedianIDI, templateClassifierParams.maxMedianIDI, templateClassifierParams.useMedianIDI);
		
		meanMinMax.setParams(templateClassifierParams.minMeanIDI, templateClassifierParams.maxMeanIDI, templateClassifierParams.useMeanIDI);

		stdMinMax.setParams(templateClassifierParams.minStdIDI, templateClassifierParams.maxStdIDI, templateClassifierParams.useStdIDI);

	}
	
	/**
	 * Get IDI parameters.
	 * @param templateClassifierParams - parameter class to add IDI parameters settings to./ 
	 * @return altered parameters class. 
	 */
	public TemplateClassifierParams getParams(TemplateClassifierParams templateClassifierParams) {
		
		@SuppressWarnings("rawtypes")
		MinMaxParams minMaxParams = medianMinMax.getParams(); 
		
		templateClassifierParams.minMedianIDI = (Double) minMaxParams.min;
		templateClassifierParams.maxMedianIDI = (Double) minMaxParams.max;
		templateClassifierParams.useMedianIDI = minMaxParams.enabled;
		
		 minMaxParams = meanMinMax.getParams(); 
		
		templateClassifierParams.minMeanIDI = (Double) minMaxParams.min;
		templateClassifierParams.maxMeanIDI = (Double) minMaxParams.max;
		templateClassifierParams.useMeanIDI = minMaxParams.enabled;
		
		minMaxParams = stdMinMax.getParams(); 
		
		templateClassifierParams.minStdIDI = (Double) minMaxParams.min;
		templateClassifierParams.maxStdIDI = (Double) minMaxParams.max;
		templateClassifierParams.useStdIDI = minMaxParams.enabled;
		
		return templateClassifierParams; 
	}
	

}
