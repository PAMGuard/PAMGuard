package rawDeepLearningClassifier.layoutFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamSpinner;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.FFTTransformPane;

public class FFTSpinnerBug  extends Application{

	protected static final String INITAL_VALUE = "0";

	public static void main(String[] args) {
	    Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		//System.out.println("Create step list: " + createStepList().size()); 
		PamSpinner<Number> spinner = new PamSpinner<Number>(FFTTransformPane.createStepList());
		spinner.getValueFactory().setConverter(new NumberConverter()); 
		spinner.setPrefWidth(80);
		
		PamBorderPane pane = new PamBorderPane(); 
		
		pane.setCenter(spinner); 
		
		pane.setPadding(new Insets(20,20,20,20));
		
	    Scene scene = new Scene(pane, 350, 300);
	    
	    System.out.println("Size before: " + ((ListSpinnerValueFactory) spinner.getValueFactory()).getItems().size());
	    spinner.getValueFactory().setValue(1019);
	    System.out.println("Size after: " + ((ListSpinnerValueFactory) spinner.getValueFactory()).getItems().size());
	    
	    
	    for (int i=0; i<((ListSpinnerValueFactory) spinner.getValueFactory()).getItems().size(); i++) {
		    System.out.println("List value: " + i + ": " +  ((ListSpinnerValueFactory) spinner.getValueFactory()).getItems().get(i));

	    }

	    stage.setTitle("Hello Spinner");
	    stage.setScene(scene);
	    stage.show();
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
