package rawDeepLearningClassifier.dataPlotFX;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.rawClipDataPlot.RawClipDataInfo;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import pamViewFX.fxGlyphs.PamSVGIcon;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLDetection;

/**
 * Allows the deep learning results to be plotted on the TD display. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class DLDetectionPlotInfoFX extends RawClipDataInfo {


	public DLDetectionPlotInfoFX(TDDataProviderFX tdDataProvider, DLControl dlControlm, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		Node showingIcon = makeIcon(); 
		this.getClipSettingsPane().setIcon(showingIcon);
		this.getClipSettingsPane().setShowingName("Deep Learning Detections");

	}

	@Override
	public double[][] getSpectrogram(PamDataUnit pamDataUnit, int iChan) {
		//Note: generally this should not be used. The function should be Overridden and the spectrogram
		//saved within data units. This is simply a backup function and is overly processor intensive as
		//the spectrogram will be calculated multiple times. 
		//any data unit used with this should be a raw data holder. 
		DLDetection rawDataProvider = (DLDetection) pamDataUnit; 

		double[][] spec =  rawDataProvider.getSpectrogram( this.getRawClipParams().fftLength, this.getRawClipParams().fftHop).getSpectrogram(iChan); 
		//System.out.println("Getting a spectrogram: " + spec.length + " x " + spec[0].length + " " + spec[0][5]);
		return spec; 
	}

	
	private Node makeIcon() {
		String resourcePath = "/Resources/modules/noun_Deep Learning_2486374.svg"; 
		try {
			PamSVGIcon iconMaker= new PamSVGIcon(); 
			//PamSVGIcon svgsprite = iconMaker.create(new File(getClass().getResource(resourcePath).toURI()), Color.WHITE);
			PamSVGIcon svgsprite = iconMaker.create(getClass().getResource(resourcePath).toURI().toURL(), Color.WHITE);
			 
//			svgsprite.getSpriteNode().setStyle("-fx-text-color: white");				
//			svgsprite.getSpriteNode().setStyle("-fx-fill: white");
			svgsprite.setFitHeight(20);
			svgsprite.setFitWidth(20);
			return svgsprite.getSpriteNode(); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
}

