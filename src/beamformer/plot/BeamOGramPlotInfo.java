package beamformer.plot;

import java.io.Serializable;

import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.PamDataUnit;
import beamformer.BeamFormerBaseControl;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import dataPlotsFX.scrollingPlot2D.Scrolling2DScaleInfo;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import dataPlotsFX.spectrogramPlotFX.SpectrogramParamsFX;
import javafx.scene.canvas.GraphicsContext;
import pamViewFX.fxNodes.utilsFX.ColourArray;

public class BeamOGramPlotInfo extends Scrolling2DPlotInfo {

	private TDScaleInfo bearingScaleInfo;
	
	private TDSymbolChooserFX symbolChooserFX = new BOSymbolChooser();
	
	private BeamOGramControlPane beamOGramControlPane;

	private BeamOGramPlotParams boParams;
	
	private BeamFormerBaseControl beamFormerControl;

	public BeamOGramPlotInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, BeamFormerBaseControl beamFormerControl, DataBlock2D pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		this.beamFormerControl = beamFormerControl;
		beamOGramControlPane = new BeamOGramControlPane(tdGraph, this);
		setDisplayedDataRange(180, 0);
	}

	@Override
	public TDScaleInfo createTDScaleInfo(Scrolling2DPlotInfo scrolingPlotinfo, double minVal, double maxVal) {
//		return new Scrolling2DScaleInfo(scrolingPlotinfo, minVal, maxVal, ParameterType.BEARING, ParameterUnits.DEGREES);
		bearingScaleInfo = new TDScaleInfo(180, 0, ParameterType.BEARING, ParameterUnits.DEGREES);
		bearingScaleInfo.setReverseAxis(true);
		return bearingScaleInfo;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#makeScrolling2DPlotData(int)
	 */
	@Override
	public Scrolling2DPlotDataFX makeScrolling2DPlotData(int iChannel) {
		return new BeamOGramPlotData(this, iChannel);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getGraphSettingsPane()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return beamOGramControlPane;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#drawData(int, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX)
	 */
	@Override
	public void drawData(int plotNumber, GraphicsContext g, double scrollStart, TDProjectorFX tdProjector) {
		/**
		 * Need to correctly set the scales here for the plot based on the 
		 * BeamOGrm step size. Ths may e different for different beamograms !
		 */

		int chan=PamUtils.getSingleChannel(bearingScaleInfo.getPlotChannels()[plotNumber]);
//		if (chan >= 0 && scrolling2DPlotData[chan] != null) {
//			
//		}
		
		super.drawData(plotNumber, g, scrollStart, tdProjector);
		super.drawAllDataUnits(plotNumber, g, scrollStart, tdProjector);
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#getDataValue(PamguardMVC.PamDataUnit)
	 */
	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		AbstractLocalisation loc = pamDataUnit.getLocalisation();
		if (loc == null) return null;
		double[] angles = loc.getAngles();
		if (angles != null && angles.length > 0) {
			return angles[0];
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#getSymbolChooser()
	 */
	@Override
	public TDSymbolChooserFX getSymbolChooser() {
		return symbolChooserFX;
	}

	@Override
	public PlotParams2D createPlotParams() {
		boParams =  new BeamOGramPlotParams();
		return boParams;
	}

	@Override
	public void bindPlotParams() {
		// TODO Auto-generated method stub
//		System.out.println("Bind BeamoGram plot params");
		if (bearingScaleInfo == null) return;
		/*
		 * //don;t use binding here to allow params to be changed for zooming etc. 
		freqScaleInfo.minValProperty().addListener((obsVal, oldVal, newVal)->{
			stopLoop=true; 
			if (spectrogramControlPane!=null) spectrogramControlPane.setMinFrequency(newVal.doubleValue());
			stopLoop=false; 
		}); 
		
		freqScaleInfo.maxValProperty().addListener((obsVal, oldVal, newVal)->{
			stopLoop=true;
			if (spectrogramControlPane!=null)  spectrogramControlPane.setMaxFrequency(newVal.doubleValue());
			stopLoop=false; 
		}); 

		 */
		bearingScaleInfo.minValProperty().addListener((obsVal, oldVal, newVal)->{
//			System.out.println("Bind BeamoGram plot params minValProperty = " + newVal.doubleValue());
		});
		bearingScaleInfo.maxValProperty().addListener((obsVal, oldVal, newVal)->{
//			System.out.println("Bind BeamoGram plot params maxValProperty = " + newVal.doubleValue());
//			beamFormerControl.
		});
		
	}
	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#setStoredSettings(java.io.Serializable)
	 */
	@Override
	public boolean setStoredSettings(Serializable storedSettings) {

		if (BeamOGramPlotParams.class.isAssignableFrom(storedSettings.getClass()) == false) {
			return false;
		}
		boolean ans = super.setStoredSettings(storedSettings);

		boParams = (BeamOGramPlotParams) storedSettings;

		//setup bindings and listeners. 
//		beamOGramControlPane.setFrequencyProperties(specParams.getFrequencyLimits());
		beamOGramControlPane.setAmplitudeProperties(boParams.getAmplitudeLimits(), boParams.getMaxAmplitudeLimits());

		setSpectrogramColours(new StandardPlot2DColours(boParams)); 

		//set correct colours
		beamOGramControlPane.getColorBox().setValue(ColourArray.getName(boParams.getColourMap()));

		double[] anglims = boParams.getAngleLimits();
//		for (int i = 0; i < 2; i++) {
//			yScaleRange[i].set(anglims[i]);
//		}
		
		return ans;
	}

	/* (non-Javadoc)
	 * @see dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo#getStoredSettings()
	 */
	@Override
	public Serializable getStoredSettings() {
		Serializable ans = super.getStoredSettings();
		double[] anglims = boParams.getAngleLimits();
//		for (int i = 0; i < 2; i++) {
//			anglims[i] = yScaleRange[i].doubleValue();
//		}
		
		return boParams;
	}

}
