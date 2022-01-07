package spectrogramNoiseReduction.threshold;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import spectrogramNoiseReduction.layoutFX.SpecNoiseNodeFX;

public class ThresholdNodeFX implements SpecNoiseNodeFX {

	private SpectrogramThreshold spectrogramThreshold;
	
	private PamVBox thresholdPane;
	
	private PamSpinner<Double> thresholdDB;
	
	private ComboBox<String> outputType;
	
	public ThresholdNodeFX(SpectrogramThreshold spectrogramThreshold) {
		super();
		this.spectrogramThreshold = spectrogramThreshold;

		thresholdPane = new PamVBox();

		thresholdPane.getChildren().add(new Label("Threshold (dB) "));
		
		
		thresholdDB=new PamSpinner<Double>(1.0,100.0,8.0, 1.0);
		thresholdDB.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		thresholdDB.setEditable(true);
		thresholdPane.getChildren().add(thresholdDB);
		
		
//		thresholdDB.setPrefColumnCount(4);

		thresholdPane.getChildren().add(new Label("Below threshold -> 0. Set above threshold data to ..."));

		thresholdPane.getChildren().add(outputType = new ComboBox<String>());
		outputType.getItems().add("Binary output (0's and 1's)");
		outputType.getItems().add("Use the output of the preceeding step");
		outputType.getItems().add("Use the input from the raw FFT data");

		thresholdPane.getChildren().add( new Label("(Some downstream processes may want phase information)"));
	}

	@Override
	public boolean getParams() {
		try {
			double newVal = thresholdDB.getValue();
			if (newVal <= 0) {
				PamDialogFX.showMessageDialog("Threshold Error", 
						"Threshold must be greater than 0");
				return false;
			}
			spectrogramThreshold.thresholdParams.thresholdDB = newVal;
		}
		catch (Exception e) {
			return false;
		}
		spectrogramThreshold.thresholdParams.finalOutput = outputType.getSelectionModel().getSelectedIndex();
		return true;
	}


	@Override
	public void setParams() {

		thresholdDB.getValueFactory().setValue(spectrogramThreshold.thresholdParams.thresholdDB);
		
		outputType.getSelectionModel().select(spectrogramThreshold.thresholdParams.finalOutput);

	}

	@Override
	public void setSelected(boolean selected) {
		thresholdDB.setDisable(!selected);
		outputType.setDisable(!selected);
	}

	@Override
	public Node getNode() {
		return thresholdPane;
	}

}

