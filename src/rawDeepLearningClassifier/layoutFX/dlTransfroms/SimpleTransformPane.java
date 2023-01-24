package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.*;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TitledPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;

/**
 * Pane for a simple transform. This is a DLTransfrom which has a list of Numbers as parameters. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SimpleTransformPane extends DLTransformPane {

	/**
	 * The transform associated with the settings pane. 
	 */
	private SimpleTransform simpleTransfrom; 

	/**
	 * The default spinner width. 
	 */
	protected static int PREF_SPINNER_WIDITH = 70; 

	int nParamCol=10; 

	/**
	 * A list of the spinners. 
	 */
	public ArrayList<Spinner<Number>> spinners; 
	

	/**
	 * 
	 * @param simpleTransfrom
	 * @param paramNames
	 */
	public SimpleTransformPane(SimpleTransform simpleTransfrom, String...paramNames) {
		this(simpleTransfrom, paramNames, null, 10); 
	}

	/**
	 * Create a settings pane for a SimpleTransform
	 * @param simpleTransfrom
	 * @param paramNames
	 * @param unitNames
	 */
	public SimpleTransformPane(SimpleTransform simpleTransfrom, String[] paramNames, String[] unitNames) {
		this(simpleTransfrom, paramNames, unitNames, 10); 

	}

	/**
	 * Create a settings pane for a SimpleTransform
	 * @param simpleTransfrom
	 * @param paramNames
	 * @param unitNames
	 */
	public SimpleTransformPane(SimpleTransform simpleTransfrom, String[] paramNames, String[] unitNames, int nColumns) {
		this.simpleTransfrom = simpleTransfrom; 
		this.nParamCol=nColumns; 
		this.setCenter(createPane(simpleTransfrom, paramNames,unitNames,nColumns) ); 
	}
	
	
	
	protected Node createPane(SimpleTransform simpleTransfrom, String[] paramNames, String[] unitNames, int nColumns) {


		PamGridPane gridPane = new PamGridPane(); 
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		gridPane.setPadding(new Insets(2,2,2,2));


		PamSpinner<Number> spinner; 
		spinners = new ArrayList<Spinner<Number>>(); 
		//			hBox.getChildren().add(new Label(simpleTransfrom.getDLTransformType().toString())); 

		int row = 0; 
		int column = 0; 
//		if  (simpleTransfrom.getParams()!=null) {
			for (int i=0; i<paramNames.length; i++) {
				if (i%nParamCol == 0 && i!=0) {
					row++; 
					column=0; 
				}

				spinner =  createSpinner(i);  
				//spinner.setPrefWidth(prefSpinnerWidth);
				spinner.setEditable(true);
				spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
				spinners.add(spinner); 
				spinner.valueProperty().addListener((obsVal, oldVal, newVal)->{
					this.notifySettingsListeners();
				});

				gridPane.add(new Label(paramNames[i]), column , row); 
				gridPane.add(spinner, column+1, row);
				if (unitNames!=null && unitNames[i]!=null) {
					gridPane.add(new Label(unitNames[i]), column+2 , row); 
					column=column+3;
				}
				else {
					column=column+2;
				}

				//System.out.println("New line: " + i + "  " + nParamCol + "  " +  i%nParamCol); 
			}
//		}
		
		TitledPane titledPane = new TitledPane(simpleTransfrom.getDLTransformType().toString(), gridPane); 

		//			PamBorderPane borderPane = new PamBorderPane(); 
		//			borderPane.setTop(new Label(simpleTransfrom.getDLTransformType().toString()));
		//			borderPane.setCenter(hBox);

		titledPane.setExpanded(false);


		return titledPane;  

	}
	
	/**
	 * Create a PAM spinner. Can be overriden to change default spinner types etc. 
	 * @param - the spinner index i.e. i = 1 is the second spinner to be created for the second parameter. 
	 * @return a new spinner
	 */
	protected PamSpinner<Number> createSpinner(int i) {
		PamSpinner spnner =  new PamSpinner<Number>(0.0, Double.MAX_VALUE, 2, 0.01);
		spnner.setPrefWidth(PREF_SPINNER_WIDITH);
		return spnner;
	}

	/**
	 * Set the spinner minimum, maximum and step size for a spinner control in the pane. An integer value factory will be set if the 
	 * inputs are Integer objects. Otherwise a double value factory is used.
	 * @param spinner - the spinner control index 
	 * @param minVal - the minimum value to set. 
	 * @param maxVal - the maximum value to set. 
	 * @param stepSize - the step size to set. 
	 */
	public void setSpinnerMinMaxValues(int spinner, Number minVal, Number maxVal, Number stepSize) {
		SpinnerValueFactory  spinnerValueFactory; 
		if (minVal instanceof Integer) {
			spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory( minVal.intValue(), maxVal.intValue(), 0, stepSize.intValue()); 
		}
		else if (minVal instanceof Float) {
			spinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minVal.floatValue(), maxVal.floatValue(), 0.0, stepSize.floatValue()); 
		}
		else { 
			spinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minVal.doubleValue(), maxVal.doubleValue(), 0.0, stepSize.doubleValue()); 
		}
		this.spinners.get(spinner).setValueFactory(spinnerValueFactory);	
	}
	
	/**
	 * Set the value for a spinner control in the pane.
	 * @param spinner - the spinner control index 
	 * @param stepSize - the value to set. 
	 */
	public void setSpinnerMinMaxValues(int spinner, Number newValue) {
		this.spinners.get(spinner).getValueFactory().setValue(newValue);
	}


	/**
	 * Get all spinner used for parameter settings. 
	 * @return the spinner sued for parameter settings. 
	 */
	public ArrayList<Spinner<Number>> getSpinners() {
		return spinners;
	}

	@Override
	public SimpleTransform getParams(DLTransform currParams) {

		SimpleTransform simpleTransform = (SimpleTransform) currParams;

		//Set the new numbers
		Number[] params = new Number[spinners.size()]; 
		for (int i=0; i<spinners.size(); i++) {
			params[i] = spinners.get(i).getValue(); 
		}

		simpleTransform.setParams(params);

		return simpleTransform;
	}

	/**
	 * Set the parameters
	 */
	@Override
	public void setParams(DLTransform input) {

		SimpleTransform simpleTransform = (SimpleTransform) input;
		
		if (simpleTransform.getParams()==null) {
			System.err.println("SimpleTransformPane: params for " + simpleTransform.getDLTransformType() + " are null - maybe backwards compatibility issue ");
			simpleTransform.setParams(((SimpleTransform)DLTransformsFactory.makeDLTransform(simpleTransform.getDLTransformType(), 1000)).getParams());
		}
		
		//System.out.println("Transform type: " + simpleTransform.getDLTransformType() + " " + simpleTransform.getParams().length + "  " + spinners.size());
		
		for (int i=0; i<spinners.size(); i++) {
			
			//spinners.get(i).getValueFactory().setValue(simpleTransform.getParams()[i] ); 
			
			//System.out.println("Set params: " + input.getDLTransformType() + " param val: " + simpleTransform.getParams()[i] + "  " + (simpleTransform.getParams()[i] instanceof Float)); 
			if (simpleTransform.getParams()[i] instanceof Float || simpleTransform.getParams()[i] instanceof Double) {
				//System.out.println("Double: simpleTransform.getParams()[i]: " + simpleTransform.getParams()[i] + "  " +spinners.get(i).getValueFactory());
				spinners.get(i).getValueFactory().setValue(simpleTransform.getParams()[i].doubleValue());

			}
			else {
				//System.out.println("Double: simpleTransform.getParams()[i]: " + simpleTransform.getParams()[i] + "  " +spinners.get(i).getValueFactory());
				spinners.get(i).getValueFactory().setValue(simpleTransform.getParams()[i].intValue());
			}
		}

	}

	@Override
	public DLTransform getDLTransform() {
		return this.getParams(simpleTransfrom) ;
	}

	

}
