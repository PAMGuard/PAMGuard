package cpod.dataPlotFX;

import java.awt.geom.Path2D;

import PamController.PamControlledUnit;
import PamUtils.PamUtils;
import PamView.HoverData;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import cpod.CPODClick;
import cpod.CPODClickDataBlock;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDScaleInfo;
import dataPlotsFX.data.generic.GenericDataPlotInfo;
import dataPlotsFX.data.generic.GenericScaleInfo;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.layout.TDSettingsPane;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * CPOD plot info. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CPODPlotInfoFX extends GenericDataPlotInfo {

	/**
	 * The CPOD settings pane. 
	 */
	CPODTDSettingsPane cpodSettingsPane;


	/**
	 * The stem scale info. 
	 */
	private TDScaleInfo stemScaleInfo;


	private GenericScaleInfo nyclesInfo;


	private GenericScaleInfo amplitudeLinInfo;


	private GenericScaleInfo bandWidthInfo;


	private DataSelector dataSelector; 


	public CPODPlotInfoFX(PamControlledUnit cpodControl, CPODPlotProviderFX cpodPlotProviderFX, TDGraphFX tdGraph,
			CPODClickDataBlock cpodDataBlock) {
		super(cpodPlotProviderFX, tdGraph, cpodDataBlock);

		this.removeScaleInfo(this.getBearingScaleInfo());
		this.removeScaleInfo(this.getScaleInfo());

		stemScaleInfo = new TDScaleInfo(90, 170, ParameterType.AMPLITUDE_STEM, ParameterUnits.DB);

		nyclesInfo = new GenericScaleInfo(0, 40, ParameterType.NCYCLES, ParameterUnits.N);

		amplitudeLinInfo = new GenericScaleInfo(-1, 1, ParameterType.AMPLITUDE_LIN, ParameterUnits.N);

		bandWidthInfo = new GenericScaleInfo(0, 100, ParameterType.BANDWIDTH, ParameterUnits.HZ);


		this.addScaleInfo(stemScaleInfo);
		this.addScaleInfo(nyclesInfo);
		this.addScaleInfo(amplitudeLinInfo);
		this.addScaleInfo(bandWidthInfo);

		//create the data selector
		dataSelector=this.getDataBlock().getDataSelectCreator().getDataSelector(this.getTDGraph().getUniqueName() +"_CPOD", false, null);


		// TODO Auto-generated constructor stub
		updateAvailability();
	}

	/* (non-Javadoc)
	 * @see dataPlots.data.TDDataInfo#getHidingDialogComponent()
	 */
	@Override
	public TDSettingsPane getGraphSettingsPane() {
		if ( cpodSettingsPane== null) {
			cpodSettingsPane = new CPODTDSettingsPane(this); 
		}
		return cpodSettingsPane; 
	}


	//	/**
	//	 * 
	//	 * @param pamDataUnit data unit
	//	 * @return text to display in tooltip if mouse hovered over symbol
	//	 */
	//	public String getToolTipText(PamDataUnit pamDataUnit) {
	//		return pamDataUnit.getSummaryString();
	//	}


	/**
	 *Get the data selector for clicks. This handles plotting clicks of different species
	 * @return the data selector. 
	 */
	public DataSelector getCPODDataSelector() {
		return this.dataSelector;
	}

	public Double getDataValue(PamDataUnit pamDataUnit) {

		//otherwise go to defaults. 
		return super.getDataValue(pamDataUnit);
	}

	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g,  double scrollStart, TDProjectorFX tdProjector, int type) {
		//		if (getScaleInfoIndex()==getScaleInfos().indexOf(frequencyInfo))  drawClickFFT( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type);

		//System.out.println("ClickPlotInfo: Draw data unit: " + pamDataUnit.getUID()); 
		//draw special data units. 
		Path2D path2D = null; 

		ParameterType paramType = getCurrentScaleInfo().getDataType(); 

		//		if (getScaleInfoIndex()==getScaleInfos().indexOf(stemScaleInfo)) 
		if (getCurrentScaleInfo().getDataType() == ParameterType.AMPLITUDE_STEM){
			//draw on a stem plot.
			path2D = drawStemClick( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type, paramType);
		}

		else if (getCurrentScaleInfo().getDataType() == ParameterType.NCYCLES){
			//draw CPOD cycles. 
			path2D = drawStemClick( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type, paramType);

		}

		else if (getCurrentScaleInfo().getDataType() == ParameterType.AMPLITUDE_LIN){
			//draw cpod SPL. 
			path2D = drawStemClick( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type, paramType);
		}

		else if (getCurrentScaleInfo().getDataType() == ParameterType.BANDWIDTH){
			//draw cpod SPL. 
			path2D = drawStemClick( plotNumber,  pamDataUnit,g ,  scrollStart,  tdProjector,  type, paramType);
		}

		else return super.drawDataUnit(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);

		//add to hover list if special data units. 
		if (path2D!=null && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL && type!=TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ){
			tdProjector.addHoverData(new HoverData(path2D, pamDataUnit, 0, plotNumber));
			return null; 
		}

		return null; 
	}

	/**
	 * Specific ways to plot CPOD data as NYCLES, AMPLITUDE_LIN and AMPLITUDE_STEM. 
	 * @param plotNumber - the plot on which the FFT is drawing. 
	 * @param pamDataUnit - the click data unit to draw 
	 * @param g - the graphics handle. 
	 * @param windowRect - the window in which to draw in.  
	 * @param orientation - the orientation of the display
	 * @param scrollStart - the scroll start 
	 * @param tdProjector - the projector which dictates the the position of the unit on the screen. 
	 * @param type - type flag for the data unit. e.g. whether selected or not. 
	 * @param paramType - the paramter type that is being draw. 
	 * @return the Path2D of the draw data. 
	 */
	private  Path2D drawStemClick(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type, ParameterType paramType) {
		long timeMillis=pamDataUnit.getTimeMilliseconds();

		//System.out.println("plotNumber; "+ plotNumber+ " chan: "+PamUtils.getChannelArray(pamDataUnit.getChannelBitmap())[0]);
		//check if we can plot click on this plot pane. 
		if (!shouldDraw(plotNumber, pamDataUnit)) {
			return null; 
		}

		//get position on time axis
		double tC = tdProjector.getTimePix(timeMillis-scrollStart);

		//System.out.println("TDDataInfoFX: tc: "+tC+"  timeMillis"+timeMillis+" scrollStart: "+scrollStart+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));
		if (tC < 0 || tC>tdProjector.getWidth()) {
			return null;
		}

		Color ffColor = this.getSymbolChooser().getPamSymbol(pamDataUnit, type).getFillColor();
		//int col=colorToInt(Color.valueOf(clickDisplayParams.fftColor));

		if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED ) g.setLineWidth(2);
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL ) g.setLineWidth(6);
		else g.setLineWidth(2);

		g.setStroke(ffColor);

		int[] chanClick=PamUtils.getChannelArray(pamDataUnit.getChannelBitmap());
		int[] chanPlot=PamUtils.getChannelArray(getTDGraph().getCurrentScaleInfo().getPlotChannels()[plotNumber]); 

		Path2D path2D = new Path2D.Double(0,1); 

		double x1 = 0, y1 = 0, x2 = 0, y2 = 0; 
		for (int i=0; i<chanClick.length; i++){

			switch (paramType) {
			case  AMPLITUDE_STEM:
				//						System.out.println("Draw amplitude: min y : " +  tdProjector.getYPix(1) + " max y: "
				//					+tdProjector.getYPix(pamDataUnit.getAmplitudeDB()) + " tC: " + tC +  " true amplitude: " + pamDataUnit.getAmplitudeDB());
				x1=tC; 
				y1=tdProjector.getYPix(1);
				x2=tC; 
				y2= tdProjector.getYPix(pamDataUnit.getAmplitudeDB()); 


				break;

			case AMPLITUDE_LIN:
				double spl = ((CPODClick) pamDataUnit).getSpl()/255.0; 
				x1=tC; 
				y1=tdProjector.getYPix(-spl);
				x2=tC; 
				y2= tdProjector.getYPix(spl); 
				break;
			case NCYCLES:
			case BANDWIDTH:
				double nycl = ((CPODClick) pamDataUnit).getnCyc(); 

				x1=tC;
				y1=tdProjector.getYPix(nycl);

				//x needs to be at least one pixel greater than X1. 
				x2 = Math.max(tC+1., tdProjector.getTimePix((timeMillis+pamDataUnit.getDurationInMilliseconds())-scrollStart));

				y2= tdProjector.getYPix(nycl); 

				break; 

			}

			if (tdProjector.getOrientation()==Orientation.VERTICAL){
				x1=y1; 
				x2=y2; 
				y1=tC;
				y2=tC; 
			}

			g.strokeLine(x1,y1,x2,y2);
			path2D.moveTo(x1,y1);
			path2D.lineTo(x2,y2);

			return path2D;
		}
		return null;
	}

	@Override
	public boolean shouldDraw(int plotNumber, PamDataUnit dataUnit) {
		double score = dataSelector.scoreData(dataUnit); 
		//System.out.println("CPOD score: " +  score);
		if (score==0) return false;

		return true; 
	}

}
