package spectrogramNoiseReduction.medianFilter;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import spectrogramNoiseReduction.layoutFX.SpecNoiseNodeFX;

public class MedianFilterNodeFX implements SpecNoiseNodeFX {
private SpectrogramMedianFilter spectrogramMedianFilter;
	
	private PamHBox medianFilterPane;
	
	private TextField filterLength;
	
	
	public MedianFilterNodeFX(
			SpectrogramMedianFilter spectrogramMedianFilter) {
		super();
		this.spectrogramMedianFilter = spectrogramMedianFilter;
		
		medianFilterPane = new PamHBox();
		medianFilterPane.setSpacing(5);

		medianFilterPane.getChildren().add(new Label("Filter length (should be odd) "));
		medianFilterPane.getChildren().add(filterLength = new TextField());
		filterLength.setPrefColumnCount(6);
	}
	
	

	@Override
	public void setSelected(boolean selected) {
		filterLength.setDisable(!selected);
		
	}



	@Override
	public boolean getParams() {
		try {
			int newVal = 
				Integer.valueOf(filterLength.getText());
			if (newVal < 3 || newVal%2 == 0) {
				PamDialogFX.showMessageDialog("Filter Length Error", 
						"Filter length must be odd and >= 3");
				return false;
			}
			spectrogramMedianFilter.medianFilterParams.filterLength = newVal;
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void setParams() {

		filterLength.setText(String.format("%d", spectrogramMedianFilter.medianFilterParams.filterLength));
		
	}


	@Override
	public Node getNode() {
		return medianFilterPane;
	}
}

