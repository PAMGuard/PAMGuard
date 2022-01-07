package detectionPlotFX.clickTrainDDPlot;

import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.plots.FFTPlotParams;
import detectionPlotFX.plots.FFTSettingsPane;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamVBox;

/**
 * Settings pane for a waterfall spectrogram. 
 * @author Jamie Macaulay 
 *
 */
public class WaterFallSpecSettingsPane extends FFTSettingsPane {

	private CheckBox normCheckBox;

	public WaterFallSpecSettingsPane(Object owner, RawFFTPlot fftPlot) {
		super(owner, fftPlot);
	}
	
	@Override
	public String getName() {
		return "Waterfall Spectorgram";
	}
	
	@Override
	public FFTPlotParams getParams(FFTPlotParams input) {
		FFTPlotParams fftPlotParams = super.getParams(input);
		//the hop is always the same as the
		fftPlotParams.normalise = normCheckBox.isSelected();
		
		return fftPlotParams; 
	}
		
	@Override
	public void setParams(FFTPlotParams input) {
		super.setParams(input);
		//the hop is always the same as the 
		normCheckBox.setSelected(input.normalise);
	}
	
	/**
	 * Create the waterfall spectrogram  pane.
	 */
	@Override
	protected Pane createFFTSettingsPane() {
		//create all the controls - bit of a hack. 
		super.createFFTSettingsPane(); 
		
		normCheckBox = new CheckBox("Normalise");
		normCheckBox.selectedProperty().addListener((obsval, oldVal, newVal)->{
			newSettings(); 
		});
		
		PamVBox vBox = new PamVBox(); 
		vBox.setSpacing(7);
		vBox.setPadding(new Insets(5,5,5,5));
		
		vBox.getChildren().addAll(new Label("FFT Length"), 
				super.getFftLengthSpinner(), normCheckBox);
		
		return vBox; 
	}

		


}
