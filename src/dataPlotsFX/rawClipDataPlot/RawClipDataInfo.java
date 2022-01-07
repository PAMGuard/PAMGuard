package dataPlotsFX.rawClipDataPlot;


import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamView.HoverData;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clipgenerator.ClipSpectrogram;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.clickPlotFX.ClickSymbolChooserFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.data.generic.GenericScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;


/**
 * Raw clip data info. 
 * 
 * @author Jamie Macaulay
 *
 */
public class RawClipDataInfo extends GenericDataPlotInfo {

	/**
	 * Parameters for plotting the raw clips. 
	 */
	private RawClipParams rawClipParams = new RawClipParams(); 

	/**
	 * The raw clip FFT plot manager. 
	 */
	private RawClipFFTPlotManager rawClipFFTPlotManager; 

	/**
	 * The raw clip settings pane. 
	 */
	private RawClipSettingsPane clipSettingsPane;

	/**
	 * Handles the plotting of raw waveforms. 
	 */
	private RawClipWavePlotManager rawWavePlotManager;

	/**
	 * The raw scale info for plotting raw waveforms. 
	 */
	private GenericScaleInfo rawWaveInfo;

	private RawClipSymbolChooser rawSymbolChoosr; 


	public RawClipDataInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
		rawClipFFTPlotManager = new RawClipFFTPlotManager(this); 
		rawWavePlotManager = new RawClipWavePlotManager(this); 
		clipSettingsPane = new RawClipSettingsPane(this); 
		clipSettingsPane.setParams();
		

		//create the symbol chooser. 
		rawSymbolChoosr = new RawClipSymbolChooser(this, pamDataBlock.getPamSymbolManager().getSymbolChooser(tdGraph.getUniqueName(), tdGraph.getGraphProjector()), TDSymbolChooserFX.DRAW_SYMBOLS); 
		
		rawWaveInfo = new GenericScaleInfo(-1, 1, ParameterType.AMPLITUDE, ParameterUnits.RAW); 
		Arrays.fill(rawWaveInfo.getPlotChannels(),1); //TODO-manage plot pane channels somehow. 

		addScaleInfo(rawWaveInfo);
	}

	/**
	 * Get the raw clip parameters. 
	 * @return the raw clip parameters. 
	 */
	public RawClipParams getRawClipParams() {
		return rawClipParams;
	}


	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		
		Path2D path2D = null; 
		if (getScaleInfoIndex() == getScaleInfos().indexOf(frequencyInfo)) { // frequency data !
			//draw the FFT data unit. 
			//System.out.println("Draw frequency data: " ); 
			if (this.getRawClipParams().showSpectrogram) {
				path2D =  rawClipFFTPlotManager.drawClipFFT( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);
			}
			else {
				super.drawFrequencyData(plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type); 
			}
		}
		else if (getScaleInfoIndex()==getScaleInfos().indexOf(rawWaveInfo)) {
			//draw the FFT data unit. 
			//System.out.println("Draw wave data: " ); 
			path2D= rawWavePlotManager.drawRawData( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);
		}
		else {
			//System.out.println("Draw standard data: " ); 
			return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
		}

		//add to hover list if special data units. 
		if (path2D!=null && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ){
			tdProjector.addHoverData(new HoverData(path2D, pamDataUnit, 0, plotNumber));
			return null; 
		}
		
		return null; 
	}

	/**
	 * Get the spectrogram for a raw clip. 
	 * @param pamDataUnit - the pam data unit. Should implement RawDataHolder. 
	 * @param chanClick - the channel click
	 * @return the double 
	 */
	public double[][] getSpectrogram(PamDataUnit pamDataUnit, int chanClick) {
		//Note: generally this should not be used. The function should be Overridden and the spectrogram
		//saved within data units. This is simply a backup function and is overly processor intensive as
		//the spectrogram will be calculated multiple times. 

		//any data unit used with this should be a raw data holder. 
		RawDataHolder rawDataProvider = (RawDataHolder) pamDataUnit; 

		//create the clip spectrogram 
		ClipSpectrogram clickSpec = new ClipSpectrogram(pamDataUnit); 

		//calculate the clip spectrogram 
		clickSpec.calcSpectrogram(rawDataProvider.getWaveData(), rawClipParams.fftLength, rawClipParams.fftHop, rawClipParams.windowType);

		return clickSpec.getSpectrogram(chanClick);
	}

	/**
	 * The raw FFT plot manager. Handles plotting clip spectrograms
	 * @return 
	 */
	public RawClipFFTPlotManager getFFTplotManager() {
		return rawClipFFTPlotManager;
	}
	
	/**
	 * The raw FFT plot manager. Handles plotting clip spectrograms
	 * @return 
	 */
	public RawClipWavePlotManager getRawWavePlotManager() {
		return rawWavePlotManager;
	}

	@Override
	public void notifyChange(int changeType){
		switch (changeType) {
		case PamController.CHANGED_PROCESS_SETTINGS:
			this.getClipSettingsPane().setParams(); 
			this.getFFTplotManager().update();
			break;
		case PamController.GLOBAL_MEDIUM_UPDATE:
			double[] amplitudeLims = PamController.getInstance().getGlobalMediumManager().getDefaultAmplitudeScales();

			getAmplitudeScaleInfo().setMinVal(amplitudeLims[0]);
			getAmplitudeScaleInfo().setMaxVal(amplitudeLims[1]);

			getRawClipParams() .freqAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 

			this.getClipSettingsPane().setParams(); //force the change in the click display params

			this.getTDGraph().repaint(0);
			break;
		case PamController.OFFLINE_DATA_LOADED:
			//this is critical to stop the buffer containing data over long time periods which can make
			//the segmenter unstable (need to work on that. )
			this.rawClipFFTPlotManager.clear();
			this.rawWavePlotManager.clear(); 
			break; 

		}
	}

	public RawClipSettingsPane getClipSettingsPane(){
		return clipSettingsPane;
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		return clipSettingsPane;
	}

	@Override
	public void lastUnitDrawn(GraphicsContext g, double scrollStart, TDProjectorFX tdProjector, int plotnumber) {
		//the FFT manager needs to know when to draw the writable images stored in memory
		this.getFFTplotManager().lastUnitDrawn(g, scrollStart, tdProjector, plotnumber); 
		this.getRawWavePlotManager().lastUnitDrawn(g, scrollStart, tdProjector, plotnumber);
	}

	/**
	 * Get the scale info for plotting raw waveforms. 
	 * @return the scale info for raw waveforms. 
	 */
	public TDScaleInfo getRawScaleInfo() {
		return this.rawWaveInfo;
	}
	

	/**
	 * 
	 * (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#getScaleInfos()
	 */
	@Override
	public ArrayList<TDScaleInfo> getScaleInfos() {		

		setNPlotPanes(this.rawWaveInfo,this.getDataBlock(), false); 
		//setNPlotPanes(this.frequencyInfo,this.getDataBlock(), false); 
		return super.getScaleInfos();
	}
	
	@Override
	public TDScaleInfo getScaleInfo() {
		//need to set correct number of plots 
		setNPlotPanes(this.rawWaveInfo,this.getDataBlock(), false); 
		//setNPlotPanes(this.frequencyInfo,this.getDataBlock(), false); 

		//System.out.println("Raw scale info chan: " + this.rawWaveInfo.getPlotChannels()[0] + "  " + this.getDataBlock().getChannelMap() + "  " + this.getDataBlock().getDataName()); 
		return super.getScaleInfo();
	}
	
	/**
	 * Get click symbol chooser. 
	 * @return the click symbol chooser. 
	 */
	public RawClipSymbolChooser getSymbolChooser() {
		return this.rawSymbolChoosr;
	}

	public void settingsUpdate() {
		// TODO Auto-generated method stub
		
	}


}
