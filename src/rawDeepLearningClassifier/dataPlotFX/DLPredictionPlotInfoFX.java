package rawDeepLearningClassifier.dataPlotFX;


import java.io.Serializable;

import PamUtils.PamArrayUtils;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDManagedSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.generic.GenericLinePlotInfo;
import dataPlotsFX.data.generic.GenericScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.dlClassification.DLClassName;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;

/**
 * Plot the raw probability information. 
 * 
 * Plots multiple probability lines. Note that the  TDDataInfo can handle drawing lines for different channels
 * but we are drawing for different prediction classes here. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLPredictionPlotInfoFX extends GenericLinePlotInfo {

	/**
	 * Scale infos to show what axis clicks can be plotted on. 
	 */
	private GenericScaleInfo probabilityScaleInfo;

	/**
	 * TRhe managed symbol chooser. 
	 */
	private TDSymbolChooserFX managedSymbolChooser;

	/**
	 * The frequency information. 
	 */
	private GenericScaleInfo frequencyInfo; 

	/**
	 * The default colour.
	 */
	//private Color color = Color.DODGERBLUE;

	/**
	 * DL control. 
	 */
	private DLControl dlControl; 

	/**
	 * The DL prediction pane. 
	 */
	private DLPredictionPane predictionSettingsPane; 

	/**
	 * The display parameters.
	 */
	private DLPredDisplayParams dlPredParams = new DLPredDisplayParams();


	public DLPredictionPlotInfoFX(TDDataProviderFX tdDataProvider, DLControl dlContorl, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);

		this.dlControl=dlContorl; 

		probabilityScaleInfo = new GenericScaleInfo(-0.1, 1.1, ParameterType.PROBABILITY, ParameterUnits.PROBABILITY);

		frequencyInfo = new GenericScaleInfo(0, 1, ParameterType.FREQUENCY, ParameterUnits.HZ);
		
		DLClassName[] classNames = getDlControl().getDLModel().getClassNames();

		//make sure this is initialised otherwise the plot won't work when first created. 
		if (dlPredParams.lineInfos==null ) dlPredParams.lineInfos = new LineInfo[classNames.length];
		for (int i=0; i<classNames.length; i++) {
			if (dlPredParams.lineInfos[i]==null) {
				dlPredParams.lineInfos[i] = new LineInfo(true, Color.rgb(0, 0, 255%(i*30 + 50)));
			}
		}

		addScaleInfo(probabilityScaleInfo);
		addScaleInfo(frequencyInfo);
	}


	/**
	 * Base class draws a simple frequency box. Easily overridden to draw something else, e.g. a contour. 
	 * @param plotNumber - the plot number 
	 * @param pamDataUnit - the data unit to plot. 
	 * @param g - the graphics context. 
	 * @param scrollStart - the scroll start. 
	 * @param tdProjector - the TDProjectorFX. 
	 * @param type - the type flag. 
	 * @return
	 */
	private Polygon drawFrequencyData(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {


		Color color = getColor(0).color; 

		DLDataUnit dataUnit = (DLDataUnit) pamDataUnit; 

		g.setLineDashes(null);
		g.setStroke(color);

		//		double[] f = pamDataUnit.getFrequency();
		//		if (f == null) {
		//			return null;
		//		}
		//		if (f.length == 1) {
		//			System.out.println("GenericDataPlotInfo: Single frequency measure in data unit " + pamDataUnit.toString());
		//		}

		if (dataUnit.getPredicitionResult().isBinaryClassification()) {

			//now which prediction is the highest. 
			int index = -1;
			double max = Double.NEGATIVE_INFINITY; 
			for (int i=0; i<dataUnit.getPredicitionResult().getPrediction().length; i++) {
				if (dataUnit.getPredicitionResult().getPrediction()[i]>max) {
					index = i;
				}
			}

			//color for the highest index.
			Color predCol = getColor(index).color; 

			//Prediction has been classified so use a fill. 
			g.setFill(Color.color(predCol.getRed(), predCol.getGreen(), predCol.getBlue(), 
					(Math.min(0.7, dlControl.getDLParams().sampleHop/(double) dlControl.getDLParams().rawSampleSize)))); 
			//			float[] prediciton = dataUnit.getPredicitionResult().getPrediction(); 
			//			float max = -(Float.MAX_VALUE+1);
			//			for (int i=0; i<prediciton.length ; i++) {
			//				if (max<prediciton[i]) {
			//					max = prediciton[i]; 
			//				}
			//			}		
		}
		else {
			//not classified so just has a dashed line. 
			g.setLineDashes(new double[] {9});

		}

		// draw a frequency box. 
		double y0 = tdProjector.getYPix(0);
		double y1 = tdProjector.getYPix(dlControl.getParentDataBlock().getSampleRate()/2);
		double x0 = tdProjector.getTimePix(pamDataUnit.getTimeMilliseconds()-scrollStart);
		double x1 = tdProjector.getTimePix(pamDataUnit.getEndTimeInMilliseconds()-scrollStart);
		double y = Math.min(y0,  y1);
		double h = Math.abs(y1-y0);

		//System.out.println(" Frequency: " + x0 + " " + x1 + " " +  " y: " +  y0 + " " + y1); 
		g.strokeRect(x0, y, x1-x0, h);
		if (dataUnit.getPredicitionResult().isBinaryClassification()) {
			g.fillRect(x0, y, x1-x0, h);
		}

		return null;
	}


	//	@Override
	//	public Double getDataValue(PamDataUnit pamDataUnit) {
	//
	//		DLDataUnit dataUnit = (DLDataUnit) pamDataUnit; 
	//
	//		if (lastUnits==null && dataUnit.getPredicitionResult().getPrediction()!=null) {
	//			lastUnits = new Point2D[dataUnit.getPredicitionResult().getPrediction().length]; 
	//		}
	//
	//		//System.out.println("DLModelPlotInfoFX model prediciton: " + dataUnit.getPredicitionResult().getPrediction()[0] + "  " +  dataUnit.getPredicitionResult().getPrediction()[1]); 
	//		return (double) dataUnit.getPredicitionResult().getPrediction()[0];
	//	}


	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {

		//System.out.println("Get data type: " + getScaleInfo().getDataType()); 
		if (getScaleInfo().getDataType().equals(ParameterType.FREQUENCY)) { // frequency data !
			return drawFrequencyData(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}
		else {
			return drawPredicition(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}

	}


	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		if (managedSymbolChooser == null) {
			managedSymbolChooser = createSymbolChooser();
		}
		return managedSymbolChooser;
	}

	private TDSymbolChooserFX createSymbolChooser() {
		PamSymbolManager symbolManager = getDataBlock().getPamSymbolManager();
		if (symbolManager == null) {
			return null;
		}
		GeneralProjector p = this.getTDGraph().getGraphProjector();
		PamSymbolChooser sc = symbolManager.getSymbolChooser(getTDGraph().getUniqueName(), p);
		return new TDManagedSymbolChooserFX(this, sc, TDSymbolChooserFX.DRAW_SYMBOLS);
	}


	/**
	 * Get the color. 
	 * @param i - the prediction index
	 * @return the color for that prediciton
	 */
	public LineInfo getColor(int i) {
		return this.dlPredParams.lineInfos[i];
	}


	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		//this is not used because we have overriden the super drawing class. 
		return null;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public DLPredictionPane getGraphSettingsPane() {
		if (predictionSettingsPane==null) {
			predictionSettingsPane = new DLPredictionPane(this); 
		}
		return predictionSettingsPane;
	}


	public DLControl getDlControl() {
		return this.dlControl; 

	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getStoredSettings()
	 */
	@Override
	public Serializable getStoredSettings() {
		if (dlPredParams.lineInfos!=null) {
			for (int i=0; i<dlPredParams.lineInfos.length; i++) {
				//set the colour as a double[] because it is not serializable. 
				dlPredParams.lineInfos[i].colorSerializable = new double[] {dlPredParams.lineInfos[i].color.getRed(), 
						dlPredParams.lineInfos[i].color.getGreen(), dlPredParams.lineInfos[i].color.getBlue()}; 
			}
		}
		//System.out.println("First colour is getStore: " + dlPredParams.lineInfos[0].color); 

		return dlPredParams;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {
		if (DLPredDisplayParams.class.isAssignableFrom(storedSettings.getClass())) {
			dlPredParams = (DLPredDisplayParams) storedSettings;
			if (dlPredParams.lineInfos!=null) {
				for (int i=0; i<dlPredParams.lineInfos.length; i++) {
					//set the colour because it is not serializable <- what a pain
					dlPredParams.lineInfos[i].color = Color.color(dlPredParams.lineInfos[i].colorSerializable[0], 
							dlPredParams.lineInfos[i].colorSerializable[1], dlPredParams.lineInfos[i].colorSerializable[2]);
				}
			}
			//System.out.println("First colour is setStore: " + dlPredParams.lineInfos[0].color); 
			updateSettings();
			//Platform.runLater(()->{		
				getGraphSettingsPane().setParams();
			//});
			return true;
		}
		return false;
	}

	/**
	 * Get the DL prediction params.
	 * @return the params
	 */
	public DLPredDisplayParams getDlPredParams() {
		return dlPredParams;
	}



	private void updateSettings() {
		// TODO Auto-generated method stub

	}


	@Override
	public double[][] getDetData(PamDataUnit pamDataUnit) {
		double[] data = PamArrayUtils.float2Double(((DLDataUnit) pamDataUnit).getPredicitionResult().getPrediction());
		
		double[][] dataD = new double[data.length][]; 
		for (int i=0; i<data.length; i++) {
			dataD[i] = new double[] {data[i]};
		}
		
		return dataD;
	}

}
