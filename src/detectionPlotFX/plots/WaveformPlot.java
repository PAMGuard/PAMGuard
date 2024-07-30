package detectionPlotFX.plots;

import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import detectionPlotFX.DDScaleInfo;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.PamColorsFX.PamColor;

/**
 * Functions to plot a waveform on a plot panel. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public abstract class WaveformPlot<D extends PamDataUnit> implements DetectionPlot<D> {

	/*
	 * The scale info for the axis. 
	 */
	private DDScaleInfo waveformScaleInfo=new DDScaleInfo(0,1,-1,1);

	/**
	 * Reference to the detection display. 
	 */
	private DetectionPlotDisplay detectionPlotDisplay;

	public DetectionPlotDisplay getDetectionPlotDisplay() {
		return detectionPlotDisplay;
	}


	/**
	 * The last detection to be plotted. Null for no plotting
	 */
	private D currentDetection=null;

	/**
	 * Object to lock function and make thread safe. 
	 */
	private Object storedWaveformLock = new Object();

	/**
	 * Parameters for the waveform plot;
	 */
	WaveformPlotParams waveformPlotParams= new WaveformPlotParams(); 

	/**
	 * The maximum amplitude of the current waevform. 
	 */
	private double maxAmplitude;

	private int log2Amplitude;

	private double[][] currentWaveform;

	private WaveformSettingsPane waveformSettingsPane;

	/**
	 * 	True to show channels on the plot. 
	 */
	private boolean showChannels = true;

	/**
	 * Constructor for the waveform plot. 
	 */
	public WaveformPlot(DetectionPlotDisplay detectionPlotDisplay){
		this.detectionPlotDisplay=detectionPlotDisplay; 
	}


	@Override
	public void setupPlot() {
		//need to get rid of the right axis. 
		detectionPlotDisplay.setAxisVisible(true, false, true, true);
		
	}


	/**
	 * Called to set up the axis correctly before the plot is created
	 * @param waveform - the waveforms of every channel. Each point represents one sample. 
	 * @param sR - the sample rate. 
	 * @param pamAxis - all the axis of the graph. this is in order TOP, RIGHT, BOTTOM, LEFT. 
	 */
	@Override
	public void setupAxis(D pamDetection, double sR, DetectionPlotProjector plotProjector){
		System.out.println("WaveformPlot.setupAxis plotting the waveform: " + getWaveform(pamDetection)[0].length);
		//all axis are used in the waveform plot except the right axis. 
		double[][] waveform=getWaveform(pamDetection); 

		if (waveform==null || waveform.length<=0 || waveform[0]==null){
			System.err.println("Could not set up the waveform axis" + waveform); 
			return; 
		}

		double binLength=-1;
		if (waveformPlotParams.waveFixedXScale) binLength=waveformPlotParams.maxLength;
		else binLength=waveform[0].length;

		//System.out.println("Waveform Length: " + binLength); 
//		plotProjector.setEnableScrollBar(true);


		//set the scroller minimum and maximum 
		plotProjector.setMinScrollLimit(0);
		plotProjector.setMaxScrollLimit((binLength*1000.)/sR);
		plotProjector.setScrollAxis(Side.BOTTOM);
		
		
		//set the min and max value from the bottom axis on the top axis. This essentially means that the scroller can
		//set the top axis. 
		plotProjector.setAxisMinMax((plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000.)*sR, 
				(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000)*sR, Side.TOP);
		
		System.out.println("WaveformPlot.setupAxis: min " + (plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000.)*sR + 
				" max: " + 	(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000)*sR);
		
		
		//		plotProjector.setAxisMinMax(0, (binLength*1000.)/sR, Side.BOTTOM);
		plotProjector.setAxisMinMax(-1, 1, Side.LEFT); //TODO

		plotProjector.setAxisLabel("Time (samples) ", Side.TOP); 
		plotProjector.setAxisLabel("Time (ms) ", Side.BOTTOM); 
		
		
		plotProjector.setAxisLabel("Amplitude (-1 to 1) ", Side.LEFT); 

		plotProjector.setNumberPlots(waveform.length);

	}

	/**
	 * Paint the waveform on the canvas.
	 * @param waveform - the waveforms of every channel. Each point represents one sample. 
	 * @param graphicsContext - the graphics handle to draw on. 
	 * @param - the area, on the plot, to draw on. 
	 * @param pamAxis - the PamAxis for the plot, x and y axis (time and some measure of amplitude. )
	 */
	@Override
	public void paintPlot(D pamDetection, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector plotProjector, int flag){
		//if (currentDetection == pamDetection) return; 		
		currentDetection = pamDetection; 
		
		if (flag == DetectionPlot.SCROLLPANE_DRAW) {
			//drawing a datagram in the scroll pane. 
			//System.out.println("WaveformPlot: Print scroll datagram: " +  pamDetection+ "  " + rectangle); 
			paintScrollDataGram(pamDetection,  gc,  rectangle, plotProjector); 
		}
		else {
			forcePaintPlot(pamDetection,  gc,  rectangle, plotProjector);
		}
	}	
	
	/**
	 * Paint the waveform on the background of the scroll pane. 
	 * @param pamDetection - the pamDetection.
	 * @param gc - the graphics context.
	 * @param rectangle - the rectangle.
	 * @param projector - the projector
	 */
	private void paintScrollDataGram(D pamDetection, GraphicsContext gc, Rectangle clipRect, DetectionPlotProjector projector) {
		currentWaveform=getWaveform(pamDetection);
		
		if (currentWaveform==null) return;
		
		paintWaveform(currentWaveform, currentDetection.getSequenceBitmap(),  gc,  clipRect,  0, currentWaveform[0].length,
				log2Amplitude, null, true,  false);	

	}

	/**
	 * Force the plot to repaint.
	 * @param pamDetection - the pamDetection.
	 * @param gc - the graphics context.
	 * @param rectangle - the rectangle.
	 * @param projector - the projector
	 */
	private void forcePaintPlot(D pamDetection, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector){
		System.out.println("WaveformPlot.forcePaintPlot:");
		currentWaveform=getWaveform(pamDetection);
	
		if (currentWaveform==null) return;

		waveformPlotParams.invert=false; 
		setYScale();
		paintWaveform(currentWaveform, gc,  rectangle, projector, null);
		if (waveformPlotParams.waveShowEnvelope) {
			gc.setStroke(Color.GRAY);
			paintWaveform(getEnvelope(pamDetection), gc,  rectangle, projector, Color.LIGHTGREY);
			waveformPlotParams.invert=true;
			paintWaveform(getEnvelope(pamDetection), gc,  rectangle, projector, Color.LIGHTGREY);
			waveformPlotParams.invert=false; 
		}	
	}


	private void setYScale() {
		/* work out the y scale */
		maxAmplitude = 0;
		if (currentDetection == null) return;

		//double[] minMax=PamUtils.getMinAndMax(getWaveform(currentDetection));
		this.log2Amplitude = getYScale(getWaveform(currentDetection)); 
	}
	
	/**
	 * Get the yscale for plotting the waveform. 
	 * @param waveform
	 * @return
	 */
	public static int getYScale(double[][] waveform) {
		
		if ( waveform==null || waveform.length==0 || waveform[0]==null) return 0;
		
		double[] minMax=PamUtils.getMinAndMax(waveform);

		double maxAmplitude=Math.max(Math.abs(minMax[0]), Math.abs(minMax[1])); 
	
		//System.out.println("YScale: " + (Math.ceil(Math.log(maxAmplitude) / Math.log(2)))); 

		return (int) Math.ceil(Math.log(maxAmplitude) / Math.log(2));
	}


	/**
	 * Get the waveforms from a PamDetection
	 * @param pamDetection the detection
	 * @return the waveform for each channel 
	 */
	public abstract double[][] getWaveform(D pamDetection); 


	/**
	 * Get the hilbert transform for each click from a PamDetection
	 * @param pamDetection the detection
	 * @return the waveform for each channel 
	 */
	public abstract double[][] getEnvelope(D pamDetection); 

	
	/**
	 * Paint the waveform. 
	 * @param waveform - the waveform
	 * @param g - the graphics context. 
	 * @param clipRect
	 * @param projector
	 * @param color
	 */
	public void paintWaveform(double[][] waveform, GraphicsContext g, Rectangle clipRect, DetectionPlotProjector projector,  Color color) {
		synchronized (storedWaveformLock) {
			if (currentDetection == null || waveform==null || waveform.length==0 || waveform[0]==null)
				return;
						
			paintWaveform(waveform, currentDetection.getSequenceBitmap(),  g,  clipRect,  (int) projector.getAxis(Side.TOP).getMinVal(), (int) projector.getAxis(Side.TOP).getMaxVal(),
					log2Amplitude, color, !waveformPlotParams.showSperateWaveform || waveform.length==1,  waveformPlotParams.invert);
		}
	}

	
	/**
	 * Paint the waveform onto a graphics handle. The plot projector should be in samples.
	 * @param currentDetection - the current data unit. 
	 * @param waveform - the waveform to paint.
	 * @param g - the graphics handle 
	 * @param clipRect - the clip r 
	 * @param minbin - the minimum waveform bin to paint
	 * @param maxbin - the maximum waveform bin to paint
	 * @param color - the colour to pain as. If null then painted in standard PAM colours. 
	 * @param invert - true to invert the waveform. i.e. paint upside down
	 */
	public static void paintWaveform(double[][] waveform, int channelBitMap, GraphicsContext g, Rectangle clipRect,
			int minbin, int maxbin, double yScaleInfo, Color color, boolean singlePlot, boolean invert) {
		System.out.println("Paint the waveform: " + waveform[0].length);
		g.setLineWidth(1);

//		boolean singlePlot=!waveformPlotParams.showSperateWaveform; 

		String txt;
		double yGap = clipRect.getHeight(); 
		if (!singlePlot) yGap /= waveform.length;
		// draw some axis
		@SuppressWarnings("unused")
		double x0, y0, x1, y1, x2, y2;
		// double yMax;
		x1 = 0;
		x2 = clipRect.getWidth();
		if (!singlePlot) {
			for (int i = 1; i < waveform.length; i++) {
				y1 = yGap * i;
				g.strokeLine(x1, y1, x2, y1);
			}
		}

		double yScale = yGap / Math.pow(2, yScaleInfo) / 2;
//		double xScale = (double) clipRect.getWidth() /(projector.getAxis(Side.TOP).getMaxVal() - projector.getAxis(Side.TOP).getMinVal());
		double xScale = (double) clipRect.getWidth() /(maxbin - minbin);

		Color strokeCol;
		x0 = clipRect.getWidth()*(0-minbin)/(maxbin-minbin); 
		//System.out.println(" x0: " +  x0 + " xScale: " + xScale); 

		//			for (int i = 0; i < PamUtils.getNumChannels(currentDetection.getChannelBitmap()); i++) {
		for (int i = 0; i < Math.min(PamUtils.getNumChannels(channelBitMap), waveform.length); i++) {

			if (color==null) {
				strokeCol=PamColorsFX.getInstance().getChannelColor(i); 
			}
			else strokeCol=color; 

			g.setStroke(strokeCol);

			y0 = yGap / 2;
			if (!singlePlot) {
				y0 += yGap * i;
			}

			if (!invert) drawWave(g, waveform[i], y0, yScale, x0,  xScale, invert);
			else drawWave(g, waveform[i], y0, yScale, x0, xScale, invert);
			//				int channel = PamUtils.getNthChannel(i, currentDetection.getChannelBitmap());
			
			
			int channel = PamUtils.getNthChannel(i, channelBitMap);
			g.setStroke(PamColorsFX.getInstance().getColor(PamColor.GRID));
			txt = String.format("ch %d", channel);

			//stroke the text
			if (!singlePlot) {
				g.fillText(txt, 2, y0 + yGap/2 - 2, 100);
				//					g.strokeText(txt, 2, y0 + yGap/2 - 2, 100);
			}
			//				if ((storedClick.triggerList & (1<<channel)) > 0) {
			//					txt += " (T)";
			//				}
			//g.strokeText(txt, 2, y0 + yGap/2 - 2, 10);
			//	txt = String.format("Click %d", currentDetection.get);
			//	g.strokeText(txt, 2, g.getFontMetrics().getHeight() + 2);
		}
	}

	/**
	 * Draw a waveform.. 
	 * @param g - the graphics handle.
	 * @param wave - the waveform
	 * @param y0
	 * @param yScale
	 * @param xScale
	 * @param invert - true to invert the waveform. 
	 */
	private static void drawWave(GraphicsContext g, double[] wave, double y0, double yScale, double x0, double xScale, boolean invert) {
		double x1, x2, y1, y2;
		int scale = invert ? -1 : 1; 
		if (wave == null || wave.length <= 0) {
			return;
		}
		y1 = y0 - (wave[0] * yScale);
		x1 = x0;
		int len = wave.length;
		for (int j = 1; j < len; j++) {
			y2 = y0 - (scale*wave[j] * yScale);
			x2 = x0 + (j * xScale);
			g.strokeLine(x1, y1, x2, y2);
			y1 = y2;
			x1 = x2;
		}
	}

	/**
	 * Get the scale info
	 * @return the scale info. 
	 */
	public DDScaleInfo getScaleInfo(){
		return waveformScaleInfo;
	}


	@Override
	public Pane getSettingsPane() {
		// filter settings and waveform colour.
		if (waveformSettingsPane==null) {
			waveformSettingsPane = new WaveformSettingsPane(this);
			waveformSettingsPane.setParams(waveformPlotParams);
		}
		return (Pane) waveformSettingsPane.getContentNode();
	}

	/**
	 * Get the waveform settings pane. 
	 * @return - the waveform settings pane. 
	 */
	public WaveformSettingsPane getWaveformSettingsPane() {
		return waveformSettingsPane; 
	}

	/**
	 * Get the waveform plot params. 
	 * @return the waveform plot. 
	 */
	public WaveformPlotParams getWaveformPlotParams() {
		return waveformPlotParams;
	}

	/**
	 * Repaint  the current data unit. 
	 */
	public void reDrawLastUnit() {
		detectionPlotDisplay.drawCurrentUnit();
	}


	/**
	 * Get the sample rate in samples per second of the last data unit. 
	 * @return the sample rate in samples per second. 
	 */
	public double getSampleRate() {
		if (currentDetection==null) return 0;
		return currentDetection.getParentDataBlock().getSampleRate();
	}


	/**
	 * Set the plot to show channels.
	 * @param show - true to show channels
	 */
	public void setShowChannels(boolean show) {
		this.showChannels=show; 
	}




}
