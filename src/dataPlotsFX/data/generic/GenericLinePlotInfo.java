package dataPlotsFX.data.generic;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.HoverData;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamConstants;
import dataPlotsFX.TDManagedSymbolChooserFX;
import dataPlotsFX.TDSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import rawDeepLearningClassifier.dataPlotFX.LineInfo;


/**
 * A Data Info which plots 1D line data i.e. usually used to plot continuous 1D
 * data e.g. deep learning predictions, Ishmael data, click trigger data.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class GenericLinePlotInfo extends TDDataInfoFX {

	/**
	 * TRhe managed symbol chooser. 
	 */
	private TDSymbolChooserFX managedSymbolChooser;
	
	/**
	 * The radius if drawing a point instead of a line i.e. if there is only one data  point. 
	 */
	public static final double OVAL_RADIUS = 10; 

	/**
	 * The last units
	 */
	private TimePoint2D[][] lastUnits = new TimePoint2D[PamConstants.MAX_CHANNELS][];


	public GenericLinePlotInfo(TDDataProviderFX tdDataProvider, TDGraphFX tdGraph, PamDataBlock pamDataBlock) {
		super(tdDataProvider, tdGraph, pamDataBlock);
	}


	/* (non-Javadoc)
	 * @see dataPlotsFX.data.TDDataInfoFX#drawDataUnit(int, PamguardMVC.PamDataUnit, javafx.scene.canvas.GraphicsContext, long, dataPlotsFX.projector.TDProjectorFX, int)
	 */
	@Override
	public Polygon drawDataUnit(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		return drawPredicition(plotNumber, pamDataUnit, g, scrollStart, tdProjector, type);
	}


	/**
	 * Get the line data. Each double[] is a separate line with N evenly spaced data points. 
	 * @param pamDataUnit - the pam data unit containing the data. 
	 * @return the line data. 
	 */
	public abstract double[][] getDetData(PamDataUnit pamDataUnit); 


	/**
	 * Draw the prediction as a line.
	 * 
	 * @param plotNumber - the plot number. 
	 * @param pamDataUnit - the PAM data unit. 
	 * @param g - the graphics context. 
	 * @param scrollStart - the scroll start. 
	 * @param tdProjector - the TDProjectorFX. 
	 * @param type - the type flag. 
	 * @return the polygon of the shape. 
	 */
	public Polygon drawPredicition(int plotNumber, PamDataUnit pamDataUnit, GraphicsContext g, double scrollStart,
			TDProjectorFX tdProjector, int type) {
		
		//System.out.println("A Plot line: "  + PamCalendar.formatDateTime(pamDataUnit.getTimeMilliseconds()));


		double mintC= -1000.;
		double maxtC = tdProjector.getWidth()+1000.;


		if  (this.getTDGraph().isWrap()) {
			mintC=0;
			maxtC=tdProjector.getWidth(); 
		}


		int chan = PamUtils.PamUtils.getLowestChannel(pamDataUnit.getChannelBitmap()); 

		g.setLineDashes(null);

		double[][] detData = getDetData(pamDataUnit); 

		//		System.out.println("Draw prediction: " + pamDataUnit + "  " + detData);

		if ((lastUnits[chan]==null || lastUnits[chan].length<1) && detData!=null) {
			//System.out.println("lastUnits:  " + lastUnits);
			//create the array of last units. 
			lastUnits[chan] = new TimePoint2D[detData.length]; 
		}
	
		//use the center of the window for plotting
		double timeMillis = pamDataUnit.getTimeMilliseconds()+pamDataUnit.getDurationInMilliseconds()/2; 
		double tC=tdProjector.getTimePix(timeMillis-scrollStart);


		//		//draws lines so tc should be some slop in pixels. 
		//		if (tC < mintC || tC>maxtC) {
		////			System.out.println("Line is outside display " + tC);
		//			return null;
		//		}
		//		


		double dataPixel; 
		Coordinate3d c; 
		Color color;
		double dataBin; //the time between the data points in millis

		//System.out.println("A Plot line 2: "  + PamCalendar.formatDateTime(pamDataUnit.getTimeMilliseconds()) + " detData " + detData.length + " " +  detData[0].length);

		//iterate through each line from det data
		for (int i=0; i<detData.length; i++) {

			if (getColor(i).enabled) {
				color = getColor(i).color;

				g.setStroke(color);
				g.setFill(color); 

				//iterate through each data point in the line
				for (int j=0; j<detData[i].length; j++) {
					
					if (lastUnits[chan][i]!=null && lastUnits[chan][i].getTimeMillis()>pamDataUnit.getTimeMilliseconds() ) {
						//unlikely to ever get here but is a safety stop if the line graph has messed up, 
						lastUnits[chan][i] = null;
					}

					dataBin = pamDataUnit.getDurationInMilliseconds()/detData[i].length; 

					//plot in the middle of the data bin. 
					timeMillis = (pamDataUnit.getTimeMilliseconds()+dataBin/2); 

					//brighten the colour up. 
					//color = Color.color(color.getRed()*0.8, color.getGreen()*0.8, color.getBlue()*0.8); 
					//					System.out.println("TDDataInfoFX: tc: "+tC+ " dataUnitTime: "+PamCalendar.formatTime((long) timeMillis)+" scrollStart: "
					//					+PamCalendar.formatTime((long) scrollStart)+" (timeMillis-scrollStart)/1000. "+((timeMillis-scrollStart)/1000.));

					c = tdProjector.getCoord3d(timeMillis, detData[i][j], 0);

					dataPixel = tdProjector.getYPix(detData[i][j]);

					if (lastUnits[chan][i]==null) {
						lastUnits[chan][i] = new TimePoint2D(tC, dataPixel, pamDataUnit.getTimeMilliseconds() ); 
						
						
						g.fillOval(tC-OVAL_RADIUS/2, dataPixel-OVAL_RADIUS/2, OVAL_RADIUS,OVAL_RADIUS);

					}
					else {
						if (tC>lastUnits[chan][i].getX() && (!this.getTDGraph().isWrap() ||
								tC<maxtC && tC>=mintC && lastUnits[chan][i].getX()<maxtC && lastUnits[chan][i].getX()>mintC)) {
							//in wrap mode we can get some weird effects with tC co-ordintates. Still have not quite cracked this...


							//							if (Math.abs(tC - lastUnits[chan][i].getX())>100) {
							//								System.out.println("tC: " + tC + " lastUnits[i].getX(): " + lastUnits[chan][i].getX() 
							//										+ "  " + tdProjector. getTimeAxis().getPosition((timeMillis-scrollStart)/1000.) + "  " + tdProjector.getWidth());
							//							}
							g.strokeLine(tC, dataPixel, lastUnits[chan][i].getX(), lastUnits[chan][i].getY());		
						}
						lastUnits[chan][i] = new TimePoint2D(tC, dataPixel, pamDataUnit.getTimeMilliseconds()); 
					}
					tdProjector.addHoverData(new HoverData(c , pamDataUnit, 0, plotNumber));
				}
				//getSymbolChooser().getPamSymbol(pamDataUnit,type).draw(g, new Point2D(tC, dataPixel));
			}
		}
		return null; 
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
	public abstract LineInfo getColor(int i);


	@Override
	public Double getDataValue(PamDataUnit pamDataUnit) {
		//this is not used because we have overridden the super drawing class. 
		return null;
	}


	public class TimePoint2D extends Point2D {


		private long timeMillis;

		public TimePoint2D(double x, double y, long timeMillis) {
			super(x, y);
			this.timeMillis=timeMillis;
		}

		public long getTimeMillis() {
			return timeMillis;
		}

		public void setTimeMillis(long timeMillis) {
			this.timeMillis = timeMillis;
		}

	}
	/**
	 * Notifications from the PamController are passed to this function.
	 * 
	 * @param changeType - notification flag.
	 */
	public void notifyChange(int changeType) {
		//	System.out.println("Prediction NOTIFYMODELCHANGED: " + changeType); 
		switch (changeType) {
		case PamController.CHANGED_PROCESS_SETTINGS:
			lastUnits  = new TimePoint2D[PamConstants.MAX_CHANNELS][];
			break;
		case PamController.RUN_NORMAL:
			lastUnits  = new TimePoint2D[PamConstants.MAX_CHANNELS][];
			break;
		case PamController.PAM_STOPPING:
			lastUnits = new TimePoint2D[PamConstants.MAX_CHANNELS][];
			break;
		}
	}

}
