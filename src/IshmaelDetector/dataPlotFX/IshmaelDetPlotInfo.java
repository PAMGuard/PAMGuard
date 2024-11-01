package IshmaelDetector.dataPlotFX;

import java.io.FileInputStream;

import IshmaelDetector.IshDetControl;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.data.generic.GenericSettingsPane;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import pamViewFX.fxGlyphs.PamSVGIcon;

/**
 * Plots Ishmael detections on the TDdisplayFX
 * 
 * @author Jamie Macaulay
 *
 */
public class IshmaelDetPlotInfo extends GenericDataPlotInfo {

	 /* 
	  * Ish det control
	 */
	private IshDetControl ishControl;
	
	/**
	 * Settings pane for Ishamel detections
	 */
	private GenericSettingsPane genericSymbolpane; 

	public IshmaelDetPlotInfo(TDDataProviderFX tdDataProvider, IshDetControl ishControl, TDGraphFX tdGraph) {
		super(tdDataProvider, tdGraph, ishControl.getIshPeakProcess().getOutputDataBlock());
		this.ishControl=ishControl;
	}
	
	
	public GenericSettingsPane getGraphSettingsPane() {
		if (genericSymbolpane==null) {
			genericSymbolpane = new GenericSettingsPane(this);
			genericSymbolpane.setIcon(makeIcon());
		}
		return genericSymbolpane;
	}
	
	@Override
	public Double getAmplitudeValue(PamDataUnit pamDataUnit) {
		System.out.println("Ishamel max val: " + pamDataUnit.getAmplitudeDB()); 
		return pamDataUnit.getAmplitudeDB();
	}
	
	private Node makeIcon() {
		try {
			Image image = new Image(getClass().getResourceAsStream("/Resources/Ishmael_icon.png"));
			 
			   //Setting the image view 
		      ImageView imageView = new ImageView(image); 
		     
		      //setting the fit height and width of the image view 
		      imageView.setFitHeight(20); 
		      imageView.setFitWidth(20); 
		      
		      //Setting the preserve ratio of the image view 
		      imageView.setPreserveRatio(true); 
		      
		      return imageView;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}


}
