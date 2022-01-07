package IshmaelDetector.layoutFX;

import IshmaelDetector.IshDetParams;
import IshmaelDetector.SgramCorrControl;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;

public class SpecCorrelationPane extends SettingsPane<IshDetParams> {
	
	private Pane mainPane; 

	public SpecCorrelationPane(SgramCorrControl specIshDetControl) {
		super(specIshDetControl);
		mainPane =  createCorrealtionPane() ; 
	}
	
	/**
	 * Create the spectrogram corealtion pane. 
	 * @return the correlation pane. 
	 */
	private Pane createCorrealtionPane() {
		
		PamVBox mainPane = new PamVBox(); 
		
		PamStackPane stackPane = new PamStackPane(); 
		
		//ishSpecDetectionPlot = IshSpecDetectionPlot(); 

		
		mainPane.getChildren().add(new Label("Spec Correaltion Here: ")); 
		
		return mainPane;  
	}

	@Override
	public IshDetParams getParams(IshDetParams currParams) {
		// TODO Auto-generated method stub
		return currParams;
	}

	@Override
	public void setParams(IshDetParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Ish Spec. Correlation Pane";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Simple sub class of the detection plot display which holds data on the match template to
	 * use. 
	 * @author Jamie Macaulay
	 *
	 */
	@SuppressWarnings("unused")
	private class IshSpecDetectionPlot extends DetectionPlotDisplay {
		

	
	}
	
	/**
	 * Detection Display dataInfo for clicks. This deals with drawing click waveforms, frequency, 
	 * wigner plots etc. 
	 * @author Jamie Macaulay
	 *
	 */
	public class TemplateDDDataInfo extends DDDataInfo<RawDataUnit> {

		public TemplateDDDataInfo(DDDataProvider dDDataProvider, DetectionPlotDisplay dDPlot,
				float sampleRate) {
			super(dDPlot, sampleRate);

			//this.addDetectionPlot(new FFTPlot(dDPlot));

			super.setCurrentDetectionPlot(0);
		}
	}

}
