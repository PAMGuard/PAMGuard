package rawDeepLearningClassifier.dataPlotFX;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import PamView.GeneralProjector.ParameterType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.data.generic.GenericSettingsPane;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLGroupDetection;


/**
 * Plot results from deep learning group detections. 
 */
public class DLGroupDetectionInfoFX extends GenericDataPlotInfo {
	

	public DLGroupDetectionInfoFX(TDDataProviderFX tdDataProvider, DLControl dlControlm, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);

		try {
			URL iconURL = getClass().getResource(DLDetectionPlotInfoFX.iconResourcePath).toURI().toURL();
			Node showingIcon = DLDetectionPlotInfoFX.makeIcon(iconURL); 
			((GenericSettingsPane) this.getGraphSettingsPane()).setIcon(showingIcon);
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		((GenericSettingsPane) this.getGraphSettingsPane()).setShowingName("Deep Learning Group Detections");
	}
	
	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {

		if (getCurrentScaleInfo().getDataType() == ParameterType.FREQUENCY) { // frequency data !
//			System.out.println("Draw frequency: " + pamDataUnit.getFrequency()[0] + "  " +  pamDataUnit.getFrequency()[1]); 
			return drawFrequencyData(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}
		else if (getCurrentScaleInfo().getDataType() == ParameterType.AMPLITUDE || getCurrentScaleInfo().getDataType() == ParameterType.AMPLITUDE_STEM) {
//			System.out.println("Draw amplitude: "); 
			double[] amplitudes  = getAmplitudes(pamDataUnit);
			return super.drawBoxData(plotNumber, pamDataUnit, amplitudes, g, scrollStart, tdProjector, type); 
		}
		
		return null;
	}
	
	/**
	 * Get the amplitude for a group data unit - this is the highest and lowest amplitude. 
	 * @param pamDataUnit - the data unit. 
	 * @return amplitude limits in dB. 
	 */
	private double[] getAmplitudes(PamDataUnit pamDataUnit) {
		
		double[] amplitudes = new double[2];
		amplitudes[0] = Double.POSITIVE_INFINITY;
		amplitudes[1] = Double.NEGATIVE_INFINITY;

		DLGroupDetection groupDetection = (DLGroupDetection) pamDataUnit; 
		
		double amp;
		for (int i=0; i<groupDetection.getSubDetectionsCount(); i++) {
			amp = groupDetection.getSubDetection(i).getAmplitudeDB();
			
			if (amp>amplitudes[1]) {
				amplitudes[1] = amp;
			}
			
			if (amp<amplitudes[0]) {
				amplitudes[0] = amp;
			}
		}
		
		return amplitudes;
	}

}
