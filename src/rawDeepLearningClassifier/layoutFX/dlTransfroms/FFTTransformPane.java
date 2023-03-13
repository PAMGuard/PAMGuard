package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import org.jamdev.jdl4pam.transforms.SimpleTransform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import pamViewFX.fxNodes.PamSpinner;
import javafx.util.StringConverter;

/**
 * Transform pane for Fourier transform
 * @author Jamie Macaulay 
 *
 */
public class FFTTransformPane extends SimpleTransformPane {

	public FFTTransformPane(SimpleTransform simpleTransform, String[] paramNames, String[] unitNames) {
		super(simpleTransform, paramNames, unitNames);
	}

	@Override
	protected PamSpinner<Number> createSpinner(int i) {
		if (i==0) {
			//System.out.println("Create step list: " + createStepList().size()); 
			PamSpinner<Number> spinner = new PamSpinner<Number>(createStepList());
			spinner.getValueFactory().setConverter(new NumberConverter()); 
			spinner.setPrefWidth(PREF_SPINNER_WIDITH);
			return spinner; 
		}
		else return super.createSpinner(i); 
	}
	
	
	/**
	 * Create a step list of FFTlength sizes for a spinner
	 * @return the step list. 
	 */
	public static ObservableList<Number> createStepList() {
		ObservableList<Number> stepSizeListLength=FXCollections.observableArrayList();
		for (int i=2; i<15; i++){
			stepSizeListLength.add(Integer.valueOf((int) Math.pow(2,i)));
			
		}
		return stepSizeListLength;
	}
	
	
	
	class NumberConverter extends StringConverter<Number> {
		@Override
		public String toString(Number object) {
			// TODO Auto-generated method stub
			return object.toString();
		}

		@Override
		public Number fromString(String string) {
			return Integer.valueOf(string);
		}
		
	}
	


	
}
