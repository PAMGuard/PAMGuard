package dataPlotsFX.scrollingPlot2D;

import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.spectrogramPlotFX.SpectrogramControlPane;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import pamViewFX.fxNodes.utilsFX.ColourArray;

public class Plot2DControPane extends SpectrogramControlPane {

	private Scrolling2DPlotInfo plotInfo2D;
	private TDGraphFX tdGraph;

	/**
	 * Keep a track of the last amplitude ranges so don't recolour twice. 
	 */
	private double[] lastAmpLimits={0 , 0};
	
	public Plot2DControPane(Scrolling2DPlotInfo plotInfo2D, TDGraphFX tdGraph) {
		this.plotInfo2D = plotInfo2D;
		this.tdGraph = tdGraph;
		setBindsAndListeners();
		this.setPadding(new Insets(5,5,5,5));
		this.setPrefWidth(180);
	}

	public Plot2DControPane(Scrolling2DPlotInfo plotInfo2D, TDGraphFX tdGraph, Orientation orientation, 
			boolean showComboColour, boolean showFreqSlider,
			boolean showColSlider, boolean labels) {
		super(orientation, showComboColour, showFreqSlider, showColSlider, labels);
		this.plotInfo2D = plotInfo2D;
		this.tdGraph = tdGraph;
		setBindsAndListeners();
		this.setPadding(new Insets(5,5,5,5));
		this.setPrefWidth(180);
	}

	public Plot2DControPane(Scrolling2DPlotInfo plotInfo2D, TDGraphFX tdGraph, Orientation orientation, 
			boolean showComboColour, boolean showFreqSlider,
			boolean showColSlider) {
		super(orientation, showComboColour, showFreqSlider, showColSlider);
		this.plotInfo2D = plotInfo2D;
		this.tdGraph = tdGraph;
		setBindsAndListeners();
		this.setPadding(new Insets(5,5,5,5));
		this.setPrefWidth(180);
	}
	
	public void setBindsAndListeners() {

		PlotParams2D spectrogramParams = plotInfo2D.getPlot2DParameters();
		setAmplitudeProperties(spectrogramParams.getAmplitudeLimits(), spectrogramParams.getMaxAmplitudeLimits());

		//set up listeners to repait graph and change colours when slider moves. 
		addAmplitudeListeners(spectrogramParams.getAmplitudeLimits());


		getColorBox().valueProperty().addListener((ov,  t,  t1) -> {                
			//change the colour in spectrogram params.   
			plotInfo2D.getSpectrogramColours().setColourMap(ColourArray.getColourArrayType(getColorBox().getValue()));
			//chnage the colour of the wrap line whilst we're at it. 
			plotInfo2D.getPlot2DParameters().setWrapLineColor(ColourArray.getColourArrayType(getColorBox().getValue()));

			//24/05/2021 - something here was causing issues with recolouring of colour plotsc - don't know what but
			//dows not look thiw line is actually needed anyway. 
			//plotInfo2D.configureDisplay(); 
			
			colourBoxChange();
			
			//re-colour the plots. 
			plotInfo2D.reColourPlots(false);
		});
	}
	
	public void colourBoxChange() {
		
	}

	/**
	 * Add listeners to min/max double property for frequency limits. Will change frequency limits whenever frequency scale bar is changed. WIll also repaint tdGraph
	 * @param amplitudeLimits- the amplitude limits to change if colour bar slider changes. 
	 */
	private void addAmplitudeListeners(DoubleProperty[] amplitudeLimits){

		/**
		 * Add listeners to amplitude limits. Here we use the changing property of the range slider thumnbs instead of the ampoliotude limits.
		 * This is because the recolour can take a whiole and having threads starting and stoipping still slows down moving the thumbs making for 
		 * a clunky user expoerience. Therefore recolour only takes places ones thumbs have stopped moving. 
		 */
		getColourSlider().lowValueChangingProperty().addListener((obserVal, oldVal, newVal)->{
			//only recolour the spectrogram if real time is paused or in viewer mode and once thumb has 
			//stopped dragging. 
			if ((plotInfo2D.isPaused() || plotInfo2D.isViewer())
					&& !newVal && !equalsLastLimits(amplitudeLimits, lastAmpLimits)){
				//				System.out.println("Low recolouring!" + newVal);
				lastAmpLimits[0]=amplitudeLimits[0].get();
				lastAmpLimits[1]=amplitudeLimits[1].get();
				plotInfo2D.reColourPlots(true);
			}

		});

		getColourSlider().highValueChangingProperty().addListener((obserVal, oldVal, newVal)->{
			if ((plotInfo2D.isPaused() || plotInfo2D.isViewer())
					&& !newVal && !equalsLastLimits(amplitudeLimits, lastAmpLimits)){
				//				System.out.println("High recolouring!" + newVal);
				lastAmpLimits[0]=amplitudeLimits[0].get();
				lastAmpLimits[1]=amplitudeLimits[1].get();
				plotInfo2D.reColourPlots(true);
			}
		});

	}	
	
	/**
	 * Checks whether current amplitude limits are the same as last limits
	 * @param amplitudeLimits - current limits
	 * @param lastLimits - last limits
	 * @return true if the limits are the same. 
	 */
	private boolean equalsLastLimits(DoubleProperty[] amplitudeLimits, double[] lastLimits){
		if (lastLimits[0]==amplitudeLimits[0].get() 
				&& lastLimits[1]==amplitudeLimits[1].get()) return true;
		else return false; 
	}

}
