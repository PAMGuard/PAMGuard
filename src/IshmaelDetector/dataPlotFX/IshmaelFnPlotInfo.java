package IshmaelDetector.dataPlotFX;

import IshmaelDetector.IshDetControl;
import IshmaelDetector.IshDetFnDataUnit;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericLinePlotInfo;
import dataPlotsFX.data.generic.GenericScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import javafx.scene.paint.Color;
import rawDeepLearningClassifier.dataPlotFX.LineInfo;

/**
 * Plots the raw Ishmael detection output. 
 * 
 * @author Jamie Macaulay
 *
 */
public class IshmaelFnPlotInfo extends GenericLinePlotInfo {

	/**
	 * The frequency information. 
	 */
	private GenericScaleInfo ishmaelPeakInfo; 

	/**
	 * The default colour.
	 */
	//private Color color = Color.DODGERBLUE;

	/**
	 * Reference to the Ishmael detector control. 
	 */
	private IshDetControl ishDetControl; 

	
	private LineInfo defaultLineInfo = new LineInfo(true, Color.BLUE); 

	private LineInfo threshLineInfo = new LineInfo(true, Color.RED); 

	public IshmaelFnPlotInfo(TDDataProviderFX tdDataProvider, IshDetControl ishDetControl, TDGraphFX tdGraph) {
		super(tdDataProvider, tdGraph, ishDetControl.getIshDetFnProcess().getOutputDataBlock());

		this.ishDetControl=ishDetControl; 

		ishmaelPeakInfo = new GenericScaleInfo(0, 100, ParameterType.ISHDET, ParameterUnits.NONE);

		addScaleInfo(ishmaelPeakInfo);
	}


	@Override
	public double[][] getDetData(PamDataUnit pamDataUnit) {
		//System.out.println("Ch: " + pamDataUnit.getChannelBitmap() + " len: "+  ((IshDetFnDataUnit) pamDataUnit) .getDetData().length);
		double[][] data =  ((IshDetFnDataUnit) pamDataUnit) .getDetData();
		
		double[][] plotData = new double[2][];
		
		if (data.length==3) {
			//here plot the threshold and smoothed data. 
			plotData[0] = data[2]; //the results
			double[] thresh = new double[data[1].length]; 
			for (int i=0; i<thresh.length; i++) {
				thresh[i] = data[1][i]+ishDetControl.getIshDetectionParams().thresh;
			}
			plotData[1]=thresh;
		}
		else {
			
			//static threshold  - add the threshold to the data
			double[] thresh = new double[data[0].length]; 
			for (int i=0; i<thresh.length; i++) {
				thresh[i] = ishDetControl.getIshDetectionParams().thresh;
			}
			plotData[0]= data[0];
			plotData[1]= thresh;

		}

		return plotData;
	}


	@Override
	public LineInfo getColor(int i) {
		if (i==0) {
			return defaultLineInfo;
		}
		else {
			return threshLineInfo; 
		}
	}

}
