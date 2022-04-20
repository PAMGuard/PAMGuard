package detectionPlotFX.plots;

import Layout.PamAxis;
import PamguardMVC.PamDataUnit;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Plot for the spectrum of a waveform. 
 * @author Jamie Macaulay
 *
 * @param <D> - the input data unit type
 */
@SuppressWarnings("rawtypes")
public abstract class  SpectrumPlot <D extends PamDataUnit> implements DetectionPlot<D>{

	/**
	 * The default line width. 
	 */
	private static double defaultLineWidth = 1; 

	/**
	 * Parameters for the click spectrum. 
	 */
	private SpectrumPlotParams spectrumPlotParams=new SpectrumPlotParams();

	/**
	 * Reference to the detection display
	 */
	private DetectionPlotDisplay detectionPlotDisplay; 

	/**
	 * Used to prevent threading issues. 
	 */
	private Object storedSpectrumLock = new Object();

	/**
	 * Stored spectrum so don;t have to keep recalculating. 
	 */
	private double[][] storedSpectrum;

	/**
	 * Stored cepstrum so don;t have to keep recalculating. 
	 */
	private double[][] storedCepstrum;

	//private long lastUpdateTime=-1;

	/**
	 * Stored log spectrum so don;t have to keep recalculating. 
	 */
	private double[][] logSpectrum;

	/**
	 * Fill the spectrum with a transparent polygon
	 */
	private boolean fillSpectrum = true;

	private double maxVal;

	/**
	 * The spectrum settings pane. 
	 */
	private SpectrumSettingsPane spectrumSettingsPane;


	private double sR;


	//	//TODO
	//	//average spectrum of an event (not implmented yet)//
	//	
	//	private PamDetection lastEvent=null;
	//
	//	private double[][] eventCepstrum;
	//	
	//	private double[][] eventSpectrum;
	//
	//	private double[][] eventSpectrumTemplates;
	//
	//	private double[][] eventSpectrumTemplatesLog;
	//	
	//	private double[][] logEventSpectrum;
	//
	//	private double[][] logTemplateSpectrum;

	//end of average spectrum of event//

	public SpectrumPlot(DetectionPlotDisplay detectionPlotDisplay){
		this.detectionPlotDisplay=detectionPlotDisplay; 
	}

	@Override
	public String getName() {
		return "Spectrum";
	}

	/**
	 * Get the power spectrum for the detection
	 * @param data - the pamDetection 
	 * @return the power spectrum for each channel.
	 */
	public abstract double[][] getPowerSpectrum(D data, int minSample, int maxSample);

	/**
	 * Get the cepstrum for the detection
	 * @param data - the pamDetection 
	 * @return the cepstrum for each channel.
	 */
	public abstract double[][] getCepstrum(D data, int minSample, int maxSample);


	@Override
	public void setupPlot() {
		detectionPlotDisplay.setAxisVisible(false, false, true, true);
		detectionPlotDisplay.getAxisPane(Side.LEFT).setnPlots(1);

	}

	@Override
	public void setupAxis(D data, double sR, DetectionPlotProjector plotProjector) {

		this.sR=sR;  

		int[] minmax = getAxisMinMaxSamples(plotProjector);
		
		if (minmax[1]>=data.getSampleDuration()) minmax[1]=(int) (data.getSampleDuration()-1);
		if (minmax[0]<0 || minmax[0]>=minmax[1]) minmax[0]=0;
		
		//System.out.println("Min max: " + minmax[0] + "  " + minmax[1]);

		plotProjector.setEnableScrollBar(false);
		plotProjector.setScrollAxis(Side.TOP); //invisible. 

		storedSpectrum=this.getPowerSpectrum(data, minmax[0], minmax[1]);
		storedCepstrum=this.getCepstrum(data, minmax[0], minmax[1]);

		sortWestAxis(data, plotProjector);
		sortSouthAxis(data,plotProjector); 
	}

	/**
	 * Get the minimum and maximum samples currently shown in the plot projector
	 * @param plotProjector - the pot projector. 
	 * @return the minimum and maximum samples
	 */
	private int[] getAxisMinMaxSamples(DetectionPlotProjector plotProjector) {
		int[] minmax = new int[2]; 
		
//		System.out.println("Plot projector: " + plotProjector.getAxis(Side.TOP).getMinVal() + "  " +
//				plotProjector.getAxis(Side.TOP).getMaxVal());

		minmax[0] = (int) ((plotProjector.getAxis(Side.TOP).getMinVal()/1000.)*sR); //this is in milliseconds
		minmax[1] = (int) ((plotProjector.getAxis(Side.TOP).getMaxVal()/1000.)*sR); 

		return minmax; 
	}

	@Override
	public void paintPlot(D data, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
		//		this.lastDataUnit=data;
		//System.out.println("Paint plot projector: "+ projector);		
		paintPanel(data, gc, rectangle, projector);
	}



	private void sortWestAxis(D data, DetectionPlotProjector projector) {
		if (spectrumPlotParams.logScale) {
			projector.setAxisMinMax(-spectrumPlotParams.logRange, -0, Side.LEFT,"Amplitude (dB)");
			//			amplitudeAxis.setRange(-spectrumPlotParams.logRange, -0);
			//			amplitudeAxis.setLabel("Amplitude (dB)");
			if (spectrumPlotParams.logRange%10 == 0) {
				projector.setAxisInterval(10, Side.LEFT);
			}
			else {
				projector.setAxisInterval(PamAxisFX.INTERVAL_AUTO, Side.LEFT);
			}
		}
		else {
			if (spectrumPlotParams.plotCepstrum) {
				projector.setAxisMinMax(-1, 1, Side.LEFT);
			}
			else {
				projector.setAxisMinMax(0, 1, Side.LEFT);
			}
			projector.setAxisLabel("Amplitude (Linear)",Side.LEFT);
			projector.setAxisInterval(PamAxis.INTERVAL_AUTO,Side.LEFT);
		}
		//		spectrumAxisPanel.SetBorderMins(10, 20, 10, 20);
		//		spectrumAxisPanel.repaint();

	}

	private void sortSouthAxis(D data, DetectionPlotProjector projector) {
		if (data==null) return; 
		if (spectrumPlotParams.plotCepstrum) {
			int specLen = 1024;
			if (storedCepstrum == null || storedCepstrum.length == 0) {
				specLen = 1024;
			}
			else {
				specLen = storedCepstrum[0].length;
			}
			double maxT = specLen / getSampleRate(data) * 1000;
			projector.setAxisMinMax(0, maxT, Side.BOTTOM, "Interval ms");
		}
		else {
			projector.setAxisMinMax(0, getSampleRate(data) / 2 / 1000, Side.BOTTOM, "Frequency kHz");
		}
	}

	private void paintPanel(D data, GraphicsContext gc, Rectangle clipRect, DetectionPlotProjector projector) {
		int[] minmax = getAxisMinMaxSamples(projector);

		storedSpectrum=this.getPowerSpectrum(data, minmax[0], minmax[1]);
		storedCepstrum=this.getCepstrum(data, minmax[0], minmax[1]);

		if (spectrumPlotParams.logScale) {
			paintLogSpectrum(gc, clipRect,projector);
		}
		else {
			paintLinSpectrum(gc, clipRect,projector);
		}

	}

	private double maxLogVal;
	private boolean lastCepChoice;


	/**
	 * 
	 * Paint the line spectrum. 
	 * 
	 * @param g - the graphics context. 
	 * @param clipRect - the clip rectangle. 
	 */
	private void paintLinSpectrum(GraphicsContext g, Rectangle clipRect, DetectionPlotProjector projector) {

		double[][] clickLineData;
		//double[][] eventLineData;
		synchronized (storedSpectrumLock) {
			if (storedSpectrum == null || storedSpectrum.length == 0) return;

			if (spectrumPlotParams.plotCepstrum) {
				clickLineData = storedCepstrum;
				//eventLineData = eventCepstrum;
			}
			else {
				clickLineData = storedSpectrum;
				//eventLineData = eventSpectrum;
			}

			// work out the scales, mins and max's, etc.
			maxVal = 0;

			for (int iChan = 0; iChan < clickLineData.length; iChan++) {
				for (int i = 0; i < clickLineData[iChan].length; i++){
					maxVal = Math.max(maxVal, clickLineData[iChan][i]);
					//					if (eventLineData!=null && isViewer==true &&clickSpectrumParams.showEventInfo==true ){
					//						if( i<eventLineData[iChan].length){
					//							maxVal = Math.max(maxVal, eventLineData[iChan][i]);
					//						}
					//					}
				}
			}

			//drawSpectrum( g, clipRect, clickLineData, eventLineData);
			drawSpectrum(g, clipRect, clickLineData, projector); 
		}
	}
	/**
	 *Draw the shapes for the line spectrum. Eventually this function should be integrated with drawLogSpectrum().
	 * @param g
	 * @param clipRect
	 * @param eventLineData 
	 * @param clickLineData 
	 */
	private void drawSpectrum(GraphicsContext g2, Rectangle clipRect, double[][] clickLineData, DetectionPlotProjector projector){

		double xScale, yScale;
		double x0, y0, x1, y1;
		Rectangle r = clipRect;

		double scale = 1./(maxVal*1.1);

		xScale = r.getWidth() /  (clickLineData[0].length - 1);
		yScale = r.getHeight() / (maxVal * 1.1);

		double[] scaledDataX;
		double[] scaledDataY;

		for (int iChan = 0; iChan < clickLineData.length; iChan++) {
			g2.setLineWidth(defaultLineWidth);
			g2.setStroke(PamColorsFX.getInstance().getChannelColor(iChan));
			x0 = 0;
			y0 = r.getHeight() - (yScale * clickLineData[iChan][0]);

			//add two points extra points, the first and last for the polygon drawing if needed
			scaledDataX = new double[clickLineData[iChan].length+2];
			scaledDataY = new double[clickLineData[iChan].length+2];
			scaledDataY[0]= r.getHeight() ;
			for (int i = 1; i < clickLineData[iChan].length+1; i++) {
				x1 = (i * xScale);
				//				y1 = r.height - (int) (yScale * clickLineData[iChan][i]);
				//System.out.println("Hello Projector: " +projector);
				y1 =  projector.getCoord3d(0,clickLineData[iChan][i-1]*scale,0).y;
				//				g2.strokeLine(x0, y0, x1, y1);

				scaledDataX[i]=x1;
				scaledDataY[i]=y1;
				x0 = x1;
				y0 = y1;
			}
			g2.strokePolyline(scaledDataX, scaledDataY, scaledDataY.length-1);
			if (fillSpectrum) {
				//				System.out.println("Last point: " + x0 + "  y0 " + r.getHeight()); 
				scaledDataX[scaledDataX.length-1]=x0; // the last x position
				scaledDataY[scaledDataY.length-1]= r.getHeight() ; //return the line to zero for polygon drawing
				//				PamUtils.PamArrayUtils.printArray(scaledDataY);
				g2.setFill(PamUtilsFX.addColorTransparancy(PamColorsFX.getInstance().getChannelColor(iChan), 0.3));
				g2.fillPolygon(scaledDataX, scaledDataY, scaledDataY.length);
			}
		}

	}
	/**
	 * Paint the log spectrum
	 * @param g - the graphics handle. 
	 * @param clipRect - the clip rectangle. 
	 * @param projector - the projector which handles conversion from pixels to data. 
	 */
	private void paintLogSpectrum(GraphicsContext g, Rectangle clipRect, DetectionPlotProjector projector) {

		double[][] clickLineData;
		//		double[][] eventLineData;
		synchronized (storedSpectrumLock) {
			if (storedSpectrum == null || storedSpectrum.length == 0 || storedSpectrum[0].length == 0) return;

			if (spectrumPlotParams.plotCepstrum) {
				clickLineData = storedCepstrum;
				//eventLineData = eventCepstrum;
			}
			else {
				clickLineData = storedSpectrum;
				//eventLineData = eventSpectrum;
			}

			if (logSpectrum == null || spectrumPlotParams.plotCepstrum != lastCepChoice) {
				double temp;
				logSpectrum = new double[clickLineData.length][clickLineData[0].length];
				//				if (eventSpectrum!=null && isViewer==true ){
				//					logEventSpectrum = new double[clickLineData.length][clickLineData[0].length];
				//				}
				//				if (eventSpectrumTemplatesLog!=null && isViewer==true ){
				//					logTemplateSpectrum = new double[clickLineData.length][];
				//				}
				maxLogVal = 10*Math.log10(clickLineData[0][0]);

				for (int iChan = 0; iChan < clickLineData.length; iChan++) {
					for (int i = 0; i < clickLineData[iChan].length; i++){
						if (clickLineData[iChan][i] > 0){
							logSpectrum[iChan][i] = temp = 10 * Math.log10(clickLineData[iChan][i]);
							maxLogVal = Math.max(maxLogVal, temp);
							//							if (eventLineData!=null && isViewer==true &&clickSpectrumParams.showEventInfo==true){
							//								if( i<eventLineData[iChan].length){
							//									logEventSpectrum[iChan][i] = temp = 10 * Math.log10(eventLineData[iChan][i]);
							//									maxLogVal = Math.max(maxLogVal,  temp);
							//								}
							//							}
						}
					}
				}

				//				if (clickSpectrumParams.plotCepstrum == false && eventSpectrumTemplatesLog!=null && isViewer==true ){
				//	
				//					for(int i=0; i<eventSpectrumTemplatesLog.length;i++){
				//						double[] logTemplate=new double[eventSpectrumTemplatesLog[i].length];
				//						for (int j = 0; j < eventSpectrumTemplatesLog[i].length; j++){
				//							logTemplate[j] = temp = eventSpectrumTemplatesLog[i][j];
				//						}
				//						logTemplateSpectrum[i]=logTemplate;
				//					}
				//				}
			}
			lastCepChoice = spectrumPlotParams.plotCepstrum;
			//drawLogSpectrum(g,clipRect,clickLineData, eventLineData);
			drawLogSpectrum(g,clipRect,clickLineData); 
		}
	}


	/**
	 * Draw the shapes for the log spectrum. This function should eventually be integrated with the drawSpectrum() function.
	 * @param g
	 * @param clipRect
	 * @param eventLineData 
	 * @param clickLineData 
	 */
	public void drawLogSpectrum(GraphicsContext g2, Rectangle clipRect, double[][] clickLineData){

		Rectangle r = clipRect;
		double xScale, yScale, scaleLim;
		int x0, y0, x1, y1;
		Color channelColour;

		//		if (eventLineData!=null && isViewer==true && clickSpectrumParams.showEventInfo==true){
		//	
		//			xScale = (double) r.getWidth() / (double) (eventLineData[0].length - 1);
		//			scaleLim = Math.abs(clickSpectrumParams.logRange);
		//			yScale = r.getHeight() / Math.abs(scaleLim);
		//	
		//			g2.setStroke(dashed);
		//	
		//			for (int iChan = 0; iChan < logEventSpectrum.length; iChan++) {
		//	
		//				GeneralPath polygon=drawPolygon(logEventSpectrum[iChan],maxLogVal,xScale,yScale,true,getBounds());
		//				channelColour=PamColors.getInstance().getChannelColor(iChan);
		//				g2.setPaint(new Color(channelColour.getRed(),channelColour.getGreen(),channelColour.getBlue(),35));
		//				g2.fill(polygon);
		//				g2.setPaint(PamColors.getInstance().getChannelColor(iChan));
		//				g2.draw(polygon);
		//			}
		//		}
		//	
		//		if (clickTemplateParams.clickTemplateArray.size()>0 && eventLineData !=null){
		//			g2.setStroke(dashedtmplate);
		//			for (int i = 0; i < logTemplateSpectrum.length; i++) {
		//				if (logTemplateSpectrum[i]!=null && clickTemplateParams.clickTempVisible.get(i)==true){
		//					xScale = (double) r.getWidth() / (double) (eventLineData[i].length - 1);
		//					scaleLim = Math.abs(clickSpectrumParams.logRange);
		//					yScale = r.getHeight() / Math.abs(scaleLim);
		//					GeneralPath polygon2 = drawPolygon(eventLineData[i],0,xScale,yScale,true,r);
		//					g2.setPaint(clickTemplateParams.clickTemplateArray.get(i).getColour());
		//					g2.draw(polygon2);
		//				}
		//			}
		//		}


		xScale = (double) r.getWidth() / (double) (clickLineData[0].length - 1);
		scaleLim = Math.abs(spectrumPlotParams.logRange);
		yScale = r.getHeight() / Math.abs(scaleLim);

		for (int iChan = 0; iChan < logSpectrum.length; iChan++) {
			g2.setStroke(PamColorsFX.getInstance().getChannelColor(iChan));
			x0 = 0;
			y0 = (int) (yScale * (maxLogVal-logSpectrum[iChan][0]));
			for (int i = 1; i < logSpectrum[iChan].length; i++) {
				x1 = (int) (i * xScale);
				y1 = (int) (yScale * (maxLogVal-logSpectrum[iChan][i]));
				g2.strokeLine(x0, y0, x1, y1);
				x0 = x1;
				y0 = y1;
			}
		}

	}

	@Override
	public Pane getSettingsPane() {
		// filter settings and waveform colour.
		if (spectrumSettingsPane==null) {
			spectrumSettingsPane = new SpectrumSettingsPane(this);
			spectrumSettingsPane.setParams(spectrumPlotParams);
		}
		return (Pane) spectrumSettingsPane.getContentNode();
	}

	/**
	 * Get the sample rate in samples per second of the last data unit. 
	 * @param the detection to find sample rate for. 
	 * @return the sample rate in samples per second. 
	 */
	public double getSampleRate(D currentDetection) {
		if (currentDetection==null) return 0;
		return currentDetection.getParentDataBlock().getSampleRate();
	}

	/**
	 * Get spectrum params.  
	 * @return the spectrum params. 
	 */
	public SpectrumPlotParams getSpectrumParams() {
		return spectrumPlotParams;
	}

	/**
	 * Re paint the last data unit. 
	 */
	public void reDrawLastUnit() {
		detectionPlotDisplay.drawCurrentUnit();
	}

	/**
	 * Check whether the spectrum is filled with a transparent polygon/
	 * @return true if spectum is filled. 
	 */
	public boolean isFillSpectrum() {
		return fillSpectrum;
	}

	/**
	 * Set whether the spectrum is filled with a transparent polygon
	 * @param fillSpectrum - true to fill spectrum
	 */
	public void setFillSpectrum(boolean fillSpectrum) {
		this.fillSpectrum = fillSpectrum;
	}




}
