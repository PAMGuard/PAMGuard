package detectionPlotFX.plots;

import java.util.Arrays;

import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickWaveform;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamMaths.WignerTransform;
import pamMaths.WignerTransform.WignerUpdate;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Plots a Wigner transform of a detection.
 *  
 * @author Jamie Macaulay
 *
 * @param <D> the specific pamDetection type. 
 */
@SuppressWarnings("unused")
public abstract class WignerPlot<D extends PamDataUnit> implements DetectionPlot<D>  {

	/**
	 * Reference to the control pane.
	 */
	private WignerSettingsPane<D> wignerControlPane;


	/**
	 * The plot parameters
	 */
	private WignerPlotParams wignerPlotOptions=new WignerPlotParams();


	/**
	 * The  number of colours. 
	 */
	private static final int NCOLOURS = 256;

	/**
	 * The channel which is currently plotted.
	 */
	private int plottedChan;

	/**
	 * Reference to the detection display
	 */
	private DetectionPlotDisplay detectionPlotDisplay;


	private double wignerMin;

	/**
	 * Wigner max
	 */
	private double wignerMax;


	/**
	 * The last plotted detection. 
	 */
	protected D currentDetection;

	/**
	 * The wigner load task. 
	 */
	protected WignerTask wignerLoadTask; 

	/**
	 * The number of bins before the wigner plot is put on a different thread. 
	 */
	private int threadLimit=64;

	/**
	 * Needs raw data to be loaded again 
	 */
	private boolean needsRawData = true;

	/**
	 * Needs the image to be calculated again. 
	 */
	private boolean needsRecalc = true;

	/**
	 * The wigner image 
	 */
	private Image wignerImage;



	/**
	 * Stores current waveform information. 
	 */
	private double[] wignerWaveform;

	/**
	 * The current wigner data. This can be 
	 */
	private WignerData wignerData; 


	/**
	 * Constructor for the waveform plot. 
	 */
	public WignerPlot(DetectionPlotDisplay detectionPlotDisplay){
		wignerData= new WignerData(); 
		this.detectionPlotDisplay=detectionPlotDisplay; 
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Wigner Plot";
	}


	boolean setupPlot = false; 
	@Override
	public void setupPlot() {
		//need to get rid of the right axis. 
		detectionPlotDisplay.setAxisVisible(true, false, true, true);
		setupPlot = true; 
	
		
	}

	/**
	 * Called to set up the axis correctly before the plot is created
	 * @param waveform - the waveforms of every channel. Each point represents one sample. 
	 * @param sR - the sample rate. 
	 * @param pamAxis - all the axis of the graph. this is in order TOP, RIGHT, BOTTOM, LEFT. 
	 */
	@Override
	public void setupAxis(D pamDetection, double sR, DetectionPlotProjector projector){
		//		System.out.println("WIGNER PLOT: SETTING UP AXIS: ");
		//all axis are used in the waveform plot except the right axis. 

		//		double[][] waveform=getWaveform(pamDetection); 
		//
		//		if (waveform==null || waveform.length<1) return; 

		projector.setEnableScrollBar(true);

		detectionPlotDisplay.getAxisPane(Side.LEFT).setnPlots(1); //TODO- make two panels?

		setFrequencyAxis(pamDetection, projector);

	}

	private void setFrequencyAxis(D pamDetection, DetectionPlotProjector plotProjector) {
		//System.out.println("Wigner plot samplerate: "+pamDetection.getParentDataBlock().getSampleRate());
		double sampleRate = detectionPlotDisplay.getCurrentDataInfo().getHardSampleRate();
		double maxVal = sampleRate /2;
		if (maxVal > 2000) {
			plotProjector.setAxisMinMax(0,maxVal / 1000, Side.LEFT, "Frequency (kHz)");
		}
		else {
			plotProjector.setAxisMinMax(0,maxVal, Side.LEFT, "Frequency (Hz)");
		}

		//freqAxis.setInterval(freqAxis.getMaxVal() / 4);
	}


	/**
	 * Setup the time axis.  
	 * @param pamDetection - the pamDetection
	 * @param southAxis - the south axis. 

	 */
	private void setTimeAxis(D pamDetection, DetectionPlotProjector plotProjector, double bin1, double bin2) {
		//double sampleRate = pamDetection.getParentDataBlock().getSampleRate();
		double sampleRate= this.detectionPlotDisplay.getCurrentDataInfo().getHardSampleRate();


		//		double msStart =  1000.*bin1/sampleRate;
		//		double msLen = 1000.*bin2/sampleRate;

		double msStart=0; 
		double msLen = 1000.*(bin2-bin1)/sampleRate;


		plotProjector.setAxisLabel("Time (samples)", Side.TOP);

		//set the limits of the axis. 
		plotProjector.setMinScrollLimit(msStart);
		plotProjector.setMaxScrollLimit(msLen);
		plotProjector.setScrollAxis(Side.BOTTOM);
		
//		this.detectionPlotDisplay.setupScrollBar();
		
		//becuase bin and bin2 are only calculated after the image and that image can take a long time on a thread. 
		if (setupPlot) {
			setupPlot=false;
			this.detectionPlotDisplay.setupScrollBar();
		}

//		System.out.println("Wigner Time Axis: min " + (plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000.)*sampleRate + 
//				" max: " + 	(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000)*sampleRate);

		plotProjector.setAxisMinMax((plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000.)*sampleRate, 
				(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000)*sampleRate, Side.TOP);

		//		if (msLen < 2) {
		////			msLen *= 1000;
		////			msStart *= 1000;
		//			plotProjector.setAxisLabel("Time (micro-s)", Side.BOTTOM);
		//		}
		//		else {
		plotProjector.setAxisLabel("Time (ms)", Side.BOTTOM);
		//		}
	}

	/**
	 * Get the waveforms from a PamDetection
	 * @param pamDetection the detection
	 * @param chan - the single channel to plot (not the channel bitmap)
	 * @return the waveform for each channel 
	 */
	public abstract double[] getWaveform(D pamDetection, int chan); 


	/**
	 * Paint the waveform on the background of the scroll pane. 
	 * @param pamDetection - the pamDetection.
	 * @param gc - the graphics context.
	 * @param rectangle - the rectangle.
	 * @param projector - the projector
	 */
	private void paintScrollDataGram(D pamDetection, GraphicsContext gc, Rectangle clipRect, DetectionPlotProjector projector) {


		double[][] waveformWigner= conditionWaveform(getWaveform(pamDetection, this.plottedChan));
		double[][] waveformTemp = new double[][]{waveformWigner[0]}; 

		setTimeAxis(pamDetection,  projector, waveformWigner[1][0], waveformWigner[1][1]);

		//System.out.println("WignerWaveform: paintScrollDataGram: "  + " " + WaveformPlot.getYScale(waveformTemp) + "  " + PamArrayUtils.minmax(waveformTemp)[1] +  "  " + waveformTemp[0].length); 
		
		WaveformPlot.paintWaveform(waveformTemp, currentDetection.getSequenceBitmap(),  gc,  clipRect,  0, waveformTemp[0].length,
				WaveformPlot.getYScale(waveformTemp), null, true,  false);	

	}

	@Override
	public synchronized void paintPlot(D pamDetection, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
		//		System.out.println("Paint Wigner Plot: " + needsRecalc); 

		if (currentDetection!=pamDetection){
			needsRawData=true; 
			needsRecalc=true;
			wignerWaveform=null;
		}
		currentDetection = pamDetection; 


		if (currentDetection==null || currentDetection.getSequenceBitmapObject()!=null) return; //can;t do anything with a null detection or a detection with sequences and not channels

		if (wignerControlPane!=null) {
			//need to also update number of channels
			wignerControlPane.setChannelList(pamDetection.getChannelBitmap());
			//System.out.println("Wigner channel to set: "+wignerPlotOptions.chan);
			wignerControlPane.setParams(wignerPlotOptions);
		}

		plottedChan=wignerPlotOptions.chan;

		//load the raw data. Might be stored in the data unit or might need to be loaded. 
		if (needsRawData) {
			//			System.out.println("GET WAVEFORM");
			wignerWaveform = getWaveform(pamDetection, this.plottedChan);
			needsRawData=false; 
			if (wignerWaveform==null) {
				//threaded loading of waveform: Now up to sub class to set needsRawData to false; 
				return; 
			}
		}

		//calculate the wigner image. For large chunks of raw data this can take a very long time. 
		if (needsRecalc){
			//System.out.println("CALC WIGNER DATA");
			if (wignerWaveform==null) {
				//might be loading the data on another thread in which case repaint paintPlot will be called again from another thread. 
				return; 
			}
			
			this.wignerImage=null; 

			//now calculate the wigner image.
			//			System.out.println("projector: "+projector); 
			double[][] wignerSurf=calcWignerData(pamDetection, wignerWaveform,  projector); 
			if (wignerSurf!=null) {
				wignerData.setWignerData(wignerSurf);
			}
			else {
				//threaded loading of wignerData
				return; 
			}
		}

		if (flag == DetectionPlot.SCROLLPANE_DRAW) {
			paintScrollDataGram(pamDetection,  gc,  rectangle,  projector);
		}
		else {
			if (this.wignerImage==null && (wignerLoadTask==null || wignerLoadTask.isDone())) {
				//create the image 
				//System.out.println("CREATE WIGNER IMAGE!: " + this.wignerPlotOptions.manualLength + "  " +  projector.getAxis(Side.BOTTOM).getMaxVal()); 
				createImage(wignerData.getWignerData());
			}

//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			drawPlot(gc,  rectangle, pamDetection, projector);
		}
	}



	/**
	 * Draw the image on a graphics context. 
	 * @param gc - graphics context 
	 */
	protected void drawPlot(GraphicsContext gc, Rectangle rectangle, D pamDetection, DetectionPlotProjector projector){

		if (wignerImage == null) {
			return;
		}

		double xStart = wignerImage.getWidth()*(projector.getAxis(Side.BOTTOM).getMinVal()-projector.getMinScrollLimit())
				/(projector.getMaxScrollLimit()-projector.getMinScrollLimit()); 

		double xEnd = wignerImage.getWidth()*(projector.getAxis(Side.BOTTOM).getMaxVal()-projector.getMinScrollLimit())
				/(projector.getMaxScrollLimit()-projector.getMinScrollLimit()); 


		//System.out.println("WignerPlot size: "+ wignerImage.getWidth()+ "  "+ wignerImage.getHeight());
		//calculate the start and end of the wigner image. 
		double imHeight = wignerImage.getHeight();
		gc.drawImage(wignerImage, xStart, 0, 
				(xEnd - xStart), wignerImage.getHeight(), 0, 0, rectangle.getWidth(), rectangle.getHeight());

		int ch = PamUtils.getNthChannel(plottedChan, pamDetection.getChannelBitmap());
		//gc.fillText(String.format("Ch %d", ch), 2, 15); //TEMP SIZE
	}


	/**
	 * 	Generate the image from the Wigner plot data. Note that this algorithms cannot 
	 * be used to plot large Wigner data sets. 
	 * @param complete set of Wigner data to plot.
	 */
	protected synchronized void createImage(double[][] wignerData) {

		if (wignerData==null) return;

		wignerImage = new WritableImage(wignerData.length, wignerData[0].length);

		wignerMin = WignerTransform.getMinValue(wignerData);
		wignerMax = WignerTransform.getMaxValue(wignerData);

//				System.out.println("Create image: WignerDat" + wignerData.length +  " " + wignerData[0].length+ 
//						"Image Size: " + wignerImage.getWidth() + "x" +wignerImage.getHeight() + " Max: " + wignerMax 
//						+" Min: " +wignerMin);   

		PixelWriter raster = ((WritableImage) wignerImage).getPixelWriter();

		ColourArray colourArray = ColourArray.createStandardColourArray(256, this.wignerPlotOptions.colorArray);
		Color[] colours=colourArray.getColours();

		int val;
		double colVal=0;


		int xCounter=0; 

		for (int i = 0; i < wignerData.length; i++) {
			colVal=0; 
			xCounter=0; 
			for (int j = 0; j < wignerData[i].length; j++) {
				colVal = ((wignerData[i][j]-wignerMin) / (wignerMax-wignerMin));
//				System.out.println("Draw : " + i + " " + j )
//				if (colVal<=0) {
//					System.out.println("Draw wig: " + wignerData[i][j]); 
//				}
				
				val =  getColourIndex(colVal);
				raster.setColor(i, (int) (wignerImage.getHeight()-1-xCounter), colours[val]);
				xCounter++; 
				colVal=0; 
			}
		}
		
		
//		System.out.println("Draw : " + xCounter); 

		
		if (wignerImage.getWidth()>1000) {
			wignerImage = PamUtilsFX.scale(wignerImage, 1000, 1000, true); 
		}
	}

	/**
	 * Calculate Wigner data for a data unit. 
	 * @param pamDetection - the data unit
	 * @param chan - the channel of the data unit to plot. 
	 * @param pamAxis - the axis to plot on. 
	 * @return wignerData - the wigner surface. This will be nbins by nbins in size. 
	 */
	protected synchronized double[][] calcWignerData(D pamDetection, double[] waveform, DetectionPlotProjector projector) {

		//condition the waveform 
		double[][] conditionedWav =  conditionWaveform(waveform); 

		double[] clikcWave1=conditionedWav[0]; 
		double bin1= conditionedWav[1][0]; 
		double bin2= conditionedWav[1][1]; 

		// need to set time axis here as we have an unknown set of bins before processing data unit. 
		setTimeAxis(pamDetection,  projector,  bin1,  bin2);

		//now calculate the wigner plot. 
		if (clikcWave1.length>this.threadLimit){

			if (wignerLoadTask!=null){
				wignerLoadTask.cancel(); 
			}

			needsRecalc=false; 

			wignerLoadTask = new WignerTask(clikcWave1); 
			Thread th = new Thread(wignerLoadTask);
			th.setDaemon(true);
			th.start();

			return null;
		}
		else{
			double[][] wignerData = WignerTransform.wignerTransform(clikcWave1);
			return wignerData;
		}
		

	}

	/**
	 * Perform pre-conditioning of waveform. Returns both the waveform and bin limits if the waveform has been 
	 * shortened. 
	 * @return conditioned waveform in first row and bin limits in second row. 
	 */
	public double[][] conditionWaveform(double[] clickWave) {
		if (clickWave==null) return null;

		int bin1 = 0, bin2 = clickWave.length, peakBin = 0;
		//double sampleRate = pamDetection.getParentDataBlock().getSampleRate();
		double peakVal, newVal;
		if (wignerPlotOptions.limitLength && bin2 > wignerPlotOptions.manualLength) {
			peakVal = clickWave[0];
			for (int i = 0; i < clickWave.length; i++) {
				if ((newVal=clickWave[i]) > peakVal) {
					peakBin = i;
					peakVal = newVal;
				}
			}
			bin1 = peakBin - wignerPlotOptions.manualLength/2;
			bin1 = Math.min(Math.max(0, bin1), bin2-wignerPlotOptions.manualLength-1);
			
			bin2 = bin1 + wignerPlotOptions.manualLength;
			clickWave = Arrays.copyOfRange(clickWave, bin1, bin2);
		}
		if (clickWave.length < 2) {
			//			System.out.println("Very short wave");
			return null;
		}

		double[][] conditionedWavData= new double[3][]; 
		conditionedWavData[0]=clickWave;
		conditionedWavData[1]=new double[] {bin1, bin2};

		return conditionedWavData; 

	}



	/**
	 * Get the index of a colour value. 
	 * @param value of the wigner plot between 0 and 1;
	 * @return colour index (0 - 255)
	 */
	private int getColourIndex(double val) {
		//System.out.println("Get Colour Index: "+dBLevel);
		// fftMag is << 1
		double  p;
		p = 256	* (val - wignerPlotOptions.minColourVal)
				/ (wignerPlotOptions.maxColourVal- wignerPlotOptions.minColourVal);
		return (int) Math.max(Math.min(p, NCOLOURS-1), 0);
	}

	@Override
	public Pane getSettingsPane() {
		if (wignerControlPane==null) {
			wignerControlPane= new WignerSettingsPane<D>(this);
			wignerControlPane.setParams(wignerPlotOptions);
		}
		return wignerControlPane.getContentNode();
	}

	/**
	 * Get the parameters for the wigner plot. 
	 * @return
	 */
	public WignerPlotParams getWignerParameters() {
		return this.wignerPlotOptions;
	}

	/**
	 * Repaint  the current data unit. 
	 */
	public void reDrawLastUnit() {
		detectionPlotDisplay.drawCurrentUnit();
		detectionPlotDisplay.setupScrollBar(); 
	}

	public void setWignerParameters(WignerPlotParams params) {
		this.wignerPlotOptions=params;
	}


	/**
	 * Runs a Wigner plot in separate thread. 
	 * @author Jamie Macaulay
	 *
	 */
	public class WignerTask extends Task<Integer> {

		/**
		 * Click waveform for this task. 
		 */
		private double[] clickWave;

		private WignerPlotUpdate wignerUpdate; 

		WignerTask(double[] clickWave){
			this.clickWave=clickWave; 
			wignerUpdate=new WignerPlotUpdate(); 
		}

		@Override
		protected Integer call() throws Exception {

			wignerUpdate.reset(); 
			wignerData.resetData(clickWave.length);

			//			System.out.println("START THE THREAD!!!!!!");
			try {
				WignerTransform.wignerTransform(clickWave, wignerUpdate, true);
			}
			catch(Exception e) {
				System.out.println("I'm a Wigner Exception");
				e.printStackTrace();
			}

			//			if (wignerData!=null){
			//				wignerMin = WignerTransform.getMinValue(wignerData);
			//				wignerMax = WignerTransform.getMaxValue(wignerData);
			//				Platform.runLater(()->{
			//					createImage();
			//					needsRecalc=false; 
			//					reDrawLastUnit();
			//				});
			//			}	

			return null;
		}


		@Override protected void cancelled() {
			super.cancelled();
			wignerUpdate.setCancelled(true);
			updateMessage("Wigner Cancelled");
		}


		@Override protected void succeeded() {
			super.succeeded();
			//			System.out.println("Redrawing the last image!");
			reDrawLastUnit();
			updateMessage("Wigner Done");
		}

	}

	/**
	 * The Wigner update object. Interface between Wigner calculation and updates. 
	 * @author Jamie Macaulay
	 *
	 */
	protected class WignerPlotUpdate implements WignerUpdate {

		volatile boolean cancel = false; 

		long count =0; 

		@Override
		public void wignerUpdated(ComplexArray tfr, int line) {
			if (count%100==0)
				//			System.out.println("Wigner Plot update: " + count);
				count++; 
			//the wigner data class handles image compression etc. 
			wignerData.addDataLine(tfr, line);
		}

		public void reset() {
			count=0; 

		}

		@Override
		public boolean isCancelled() {
			return cancel;
		}

		public void setCancelled(boolean cancel ) {
			this.cancel=cancel; 
		}

	}


	/**
	 * Check whether the wigner plot needs recalculated with a new settings. 
	 * @param wignerParameters
	 */
	public void checWignerRecalc(WignerPlotParams wignerParameters) {
		if (wignerParameters.chan != this.getWignerParameters().chan) {
			//System.out.println("NEEDS A RECALC: " + this.getWignerParameters().chan);
			this.needsRawData=true; 
			this.needsRecalc=true;
		}
		if (wignerParameters.manualLength != this.getWignerParameters().manualLength) {
			this.needsRecalc=true; 
		}
		this.wignerImage=null; //always needs recalculated when new settings. 
	}

	/**
	 * Indicates whether the wigner image needs recalculated. 
	 * @return
	 */
	public boolean isNeedsRecalc() {
		return needsRecalc;
	}

	/**
	 * Set the needs recalc flag. This will force a recalculation of wigner data. 
	 * @param needsRecalc
	 */
	public void setNeedsRecalc(boolean needsRecalc) {
		this.needsRecalc = needsRecalc;
	}

	/*
	 * 
	 * Check whether the wigner plot needs recalculated. 
	 */
	public boolean getNeedsRecalc() {
		// TODO Auto-generated method stub
		return this.needsRecalc;
	}

	/**
	 * Get the settings pane. This pane changes wigner plot specific settings. 
	 * @return the Wigner settings pane
	 */
	public WignerSettingsPane<D> getWignerSettingsPane() {
		return wignerControlPane; 
	}


	/**
	 * Get the current waveform used to calculate the wigner plot. 
	 * @return - the current wigner waveform 
	 */
	public double[] getWignerWaveform() {
		return wignerWaveform;
	}

	/**
	 * Set th current wigner waveform. 
	 * @param wignerWaveform - the current wigner waveform. 
	 */
	public void setWignerWaveform(double[] wignerWaveform) {
		this.wignerWaveform = wignerWaveform;
	}

	public DetectionPlotDisplay getDetectionPlotDisplay() {
		return this.detectionPlotDisplay;
	}


}
