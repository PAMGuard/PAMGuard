package detectionPlotFX.plots;

import java.util.Arrays;

import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamArrayUtils;
import PamUtils.PamCoordinate;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataBlock2D;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.superdet.SuperDetection;
import Spectrogram.WindowFunction;
import dataPlotsFX.data.DataTypeInfo;
import dataPlotsFX.projector.TimeProjectorFX;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotDataFX;
import dataPlotsFX.scrollingPlot2D.Scrolling2DPlotInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.projector.DetectionPlotProjector;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

/**
 * Plots FFT data as  background if raw data is available. Data units can be drawn over the spectrogram image. 
 * <p>
 * Specifically, this handles the calualtion of spectrograms from raw data and can handle large chunks of raw data. 
 * Data are ordered on a thred, an image is then created and loaded on another thread. 
 * <p>
 * For detections whihc may already have a small saved spectrogram, e.g. anything that implements RawDataHolder, then use FFTPlot instead. 
 * 
 * @author Jamie Macaulay
 *
 * @param <D> - the data unit.
 */
public abstract class RawFFTPlot<D extends PamDataUnit> extends FFTPlot<D> {

	RawDataOrder rawDataOrder; 


	/**
	 * The last data unit whihc has been plotted. 
	 */
	protected D lastData;

	/**
	 * True if the raw data needs to be loaded again
	 */
	protected boolean reloadRaw=true;

	/**
	 * True if the image needs recalculated
	 */
	protected boolean reloadImage=true;


	/**
	 * Pane which shows a spectrogram 
	 */
	private Scrolling2DPlotDataFX spectrogram; 

	/**
	 * The FFT data block. 
	 */
	protected SimpleFFTDataBlock simpleFFTBlock;


	/**
	 * The current window function
	 */
	private double[] windowFunction; 

	/**
	 * Performs FFT calculations
	 */
	private FastFFT fastFFT = new FastFFT();

	private static DataTypeInfo dataTypeInfo = new DataTypeInfo(ParameterType.FREQUENCY, ParameterUnits.HZ);

	/**
	 * The task which creates the image; 
	 */
	private Task<Integer> imageTask;

	
	protected Thread th;



	public RawFFTPlot(DetectionPlotDisplay displayPlot, DetectionPlotProjector projector) {
		super(displayPlot, projector); 
		rawDataOrder= new FFTDataOrder(); 
		//time frequency
		spectrogram =  new FFTSpecPlot(timeFreqProjector=new FreqTimeProjector(projector), 
				simpleFFTBlock = new SimpleFFTDataBlock("Dummy Detection Plot", null, 0, fftParams.fftHop, fftParams.fftLength) 
				, spectrogramColours, 0 ,true); 

		//set the FFT params. 
		setFFTParams(fftParams);
//		//check to see what needs and set appropriate flag for next repaint
		checkSettingsChange( fftParams);
	}

	@Override
	public String getName() {
		return "FFT Plot";
	}


	@Override
	public synchronized void paintPlot(D dataUnit, GraphicsContext graphicsContext, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
		
		//paint the detections first
		paintDetections(dataUnit,  graphicsContext,  rectangle, projector);
		
		//so we have the data unit or if reload raw then also set reload image. 
		if (dataUnit!=lastData || reloadRaw){
			reloadRaw=true; 
			reloadImage=true; 
		}
		lastData=dataUnit;
		//three threaded sequences. 1) Load the data
		//2) generate the image and 3), on the FX thread, paint the image, 
		if (reloadRaw && detectionPlotDisplay.isViewer()){
			//System.out.println("Load RAW data for data unit: " +dataUnit.getUID());
			//reset the spectrogram and clear image
			reloadRaw=false; // really only want this called into once unless explicitly invoked. 
			//Otherwise variables get cleared etc and it;s a mess
			spectrogram.checkConfig(); 
			
			 loadRawData(dataUnit,  fftParams.detPadding,  fftParams.plotChannel); 
			//rawDataOrder.startRawDataLoad(dataUnit,  fftParams.detPadding,  fftParams.plotChannel);
			//on a different thread which will call repaint again			
		}
		else if (reloadImage && detectionPlotDisplay.isViewer()){
//			System.out.println("Load IMAGE data: seconds: " + this.rawDataOrder.getRawDataObserver().getRawData().length
//					+ " for data unit: " + +dataUnit.getUID());
			spectrogram.checkConfig(); 
			startImageLoad(); 
		}
		else {
			//repaint the image!!
//			System.out.println("PAINT the image for: " +dataUnit.getUID());
			if (detectionPlotDisplay.isViewer()) paintSpecImage(graphicsContext,  rectangle, projector);
			paintDetections(dataUnit,  graphicsContext,  rectangle, projector) ;
		}
	}
	
	/**
	 * Load the raw data. This can be overridden if necessary. 
	 * @param dataUnit - the data unit to load
	 * @param padding - the padding. 
	 * @param plotChannel - the plot channel. 
	 */
	public void loadRawData(D dataUnit, double padding, int plotChannel) {
		rawDataOrder.startRawDataLoad(dataUnit,  fftParams.detPadding,  fftParams.plotChannel);
	}
	
	
	@Override
	public void setupAxis(D pamDetection, double sR, DetectionPlotProjector projector) {
		//	//detection display
		//	if (pamDetection != null) {
		//		sR = pamDetection.getParentDataBlock().getSampleRate();
		//	}
		projector.setEnableScrollBar(false);

		setupFreqAxis(0, sR/2, projector);

		double duration = fftParams.detPadding*2;
		if (pamDetection != null) {
			duration += pamDetection.getDurationInMilliseconds();
		}
		setUpTimeAxis(duration, projector);
	}

	
	@Override
	public void setUpTimeAxis(double millisDuration, DetectionPlotProjector plotProjector) {
		//keep it simple for this
		plotProjector.setAxisMinMax(0, millisDuration/1000., Side.BOTTOM, "Time (s)");
	}

	/**
	 * Repaint the spectrogram
	 * @param dataStart
	 */
	public void repaintSpectrogram(long dataStart) {
		GraphicsContext graphicsContext;
		Canvas plotCanvas = detectionPlotDisplay.getPlotPane().getPlotCanvas();
		//		detectionPlotDisplay.getPlotPane().setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
		graphicsContext = plotCanvas.getGraphicsContext2D();
		Rectangle r = new Rectangle(plotCanvas.getWidth(), plotCanvas.getHeight());
		//		getRawDataObserver()
		//		paintSpecImage(graphicsContext, r, pamAxis);
		graphicsContext.clearRect(0, 0, r.getWidth(), r.getHeight());
		//		graphicsContext.strokeLine(10, 10, r.getWidth()-10, r.getHeight()-10);
		//		graphicsContext.strokeLine(10, r.getHeight()-10, r.getWidth()-10, 10);
		//plot the spectrogram. 
		spectrogram.drawSpectrogram(graphicsContext, r, Orientation.HORIZONTAL,
				this.timeFreqProjector.getTimeAxis(), dataStart, false);
	}

	/**
	 * Paint the image. 
	 * @param graphicsContext - the graphicsContext
	 * @param rectangle - the rectangle
	 * @param pamAxis - the PamAxis[]
	 */
	protected synchronized void paintSpecImage(GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector) {
		//Debug.out.println("FFTPlot: PaintSpecImage: " + windowRect.getWidth() + "  " + windowRect.getHeight()+ " spec N: " + spectrogram.getTotalPowerSpec());

		//calculate the size of the scrollbar
		graphicsContext.clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());

		//plot the spectrogram. 
		spectrogram.drawSpectrogram(graphicsContext, windowRect, Orientation.HORIZONTAL,
				this.timeFreqProjector.getTimeAxis(), rawDataOrder.getRawDataObserver().getLoadStart(), false);
	}

	/**
	 * Paint the detection over the FFT. 
	 * @param graphicsContext - the graphics handle
	 * @param windowRect - the window rectangle in pixels
	 * @param pamAxis - the pamAxis whihc are being plotte on. 
	 */
	public abstract void paintDetections(D detection, GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector); 

	/**
	 * Starts the load thread. 
	 */
	private synchronized void startImageLoad() {
		this.simpleFFTBlock.clearAll();//clear all the data units.
		
		double[] rawData=rawDataOrder.getRawDataObserver().getRawData();

//				System.out.println("Raw data sample");
//				for (int i=0; i<100; i++){
//					System.out.print(rawData[i]+" ");
//				}
		int channel = rawDataOrder.getRawDataObserver().getChannel(); 
		float sR = rawDataOrder.getRawDataObserver().getSampleRate(); 
		long dataStart = rawDataOrder.getRawDataObserver().getLoadStart();

		//		loadSpectrogramImage(rawData,  sR,  channel,  dataStart, null);

		if (imageTask!=null) imageTask.cancel();
		try {
			if (th!=null) th.join(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		imageTask = new Task<Integer>() {
			@Override protected Integer call() throws Exception {
				Thread.sleep(100); //give some time to cancel before a data load. 
				if (isCancelled()) {
					return -1; 
				}
				try{
					loadDataUnitImage(rawData,  sR,  channel,  dataStart, this);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return 0;
			}
		};

		imageTask.setOnSucceeded((workerState)->{
			this.reloadImage=false; 
			this.detectionPlotDisplay.drawCurrentUnit(); //repaint
		}); 

		th = new Thread(imageTask);
		th.setDaemon(true);
		th.start();

	}



	/**
	 * Create the image of the spectrogram from FFT data units. 
	 * @param rawData- the raw data to calculate spectrogram from. 
	 * @param sR - the sample rate in samples per second. 
	 * @param channel - the channel. 
	 * @param dataStart - the start time of the data in millis. 
	 * @param task - reference to that task loading the image. 
	 */
	protected synchronized void loadDataUnitImage(double[] rawData, float sR, int channel, long dataStart, Task task ){
		
		System.out.println("RawFFTPlot: Raw data to process: " + rawData.length + " bins:  FFTPlot: hop: " + fftParams.fftLength + " length: " + fftParams.fftHop
				+ " sampleRate: " + sR);

		this.simpleFFTBlock.setFftHop(fftParams.fftHop);
		this.simpleFFTBlock.setFftLength(fftParams.fftLength);
		this.simpleFFTBlock.setSampleRate(sR);
		windowFunction = WindowFunction.getWindowFunc(fftParams.windowFunction, fftParams.fftLength);

		//reset the spectrogram and clear image
		spectrogram.resetForLoad();

		if (rawData==null) return;

		//now iterate through samples and create fft data units
		double[] rawChunk;
		double[] conditionedChunk= new double[fftParams.fftLength];
		ComplexArray fftOutData;
		RawFFTPlot<D>.SimpleFFTDataUnit fftDataUnit; 

		for (int i=0; i<rawData.length;  i=i+fftParams.fftHop){

			if (task!=null && task.isCancelled()) {
				spectrogram.resetForLoad();
				return; 
			}

			rawChunk= Arrays.copyOfRange(rawData, i, i+fftParams.fftLength);

			//now need to apply the FFT transfrom 
			for (int w = 0; w < fftParams.fftLength; w++) {
				if (w<fftParams.fftLength) {
					conditionedChunk[w] = (rawChunk[w] * windowFunction[w]);
				}
				else conditionedChunk[w]= w; 
			}

			//			System.out.println("Chunk: ");
			//			for (int k=0; k<100; k++){
			//				System.out.print(conditionedChunk[k] + " ");
			//			}
			fftOutData =  fastFFT.rfft(conditionedChunk, fftParams.fftLength);

			// set the correct reference in the data block
			long millis= (long) (dataStart+ 1000*i/(double) sR); //need to convert sR to double otherwise screws up number in division
			fftDataUnit = new SimpleFFTDataUnit(millis, PamUtils.makeChannelMap(new int[]{channel}), 
					i, fftParams.fftLength, fftOutData, i, sR); 
			simpleFFTBlock.addPamData(fftDataUnit);
			spectrogram.new2DData(fftDataUnit);
		}
//		System.out.println("Finished load of image: "); 
	}
	

	public class SimpleFFTDataUnit extends DataUnit2D<PamDataUnit,SuperDetection> {

		ComplexArray fftData;

		private float sR;


		public SimpleFFTDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration, ComplexArray fftData, int fftSlice, float sR) {
			super(timeMilliseconds, channelBitmap, startSample, duration);
			this.fftData=fftData; 
			this.sR=sR; 
		}

		@Override
		public double[] getMagnitudeData() {
			/**
			 * Return the values in decibels (spectrum level I think). 
			 */
			if (fftData == null) {
				return null;
			}

			double[] magSqData =  fftData.magsq();
			AcquisitionProcess daqProcess = null;

			//			int iChannel = PamUtils.getSingleChannel(getChannelBitmap());
			int iChannel = this.getParentDataBlock().getARealChannel(PamUtils.getSingleChannel(getChannelBitmap()));

			// get the acquisition process. 
//			try {
//				//TODO- need to pick correct data block 
			if (PamController.getInstance().getRawDataBlock(0)!=null) {
				daqProcess = (AcquisitionProcess) PamController.getInstance().getRawDataBlock(0).getSourceProcess();
				daqProcess.prepareFastAmplitudeCalculation(iChannel);
				//			}
				//			catch (ClassCastException e) {
				//				e.printStackTrace();
				//				return magSqData;
				//			}

				magSqData =  fftData.magsq();

				//			System.out.println("Magnitude squared length is: " + magSqData.length + " "+ magSqData[0]);

				for (int i = 0; i < magSqData.length; i++) {
					magSqData[i] = daqProcess.fftAmplitude2dB(magSqData[i], iChannel, 
							sR, magSqData.length*2, true, false);
				}

				//			System.out.println("Magnitude squared length is: " + magSqData.length + " "+ magSqData[0]);

			} else {
				for (int i = 0; i < magSqData.length; i++) {
					magSqData[i] = 20*Math.log10(magSqData[i]) + 175; //Guess
				}
			}
			
			//normalise data (used in subclasses for weird spectrograms)
			//Debug.out.println("Max amplitude squared is: " + PamArrayUtils.max(magSqData));
			if (getFFTParams().normalise) {
				magSqData= PamArrayUtils.divide(magSqData, PamArrayUtils.max(magSqData)/50); //add a scale factor to keep within usual FFT scales. Bit of a HACK
			}
			//System.out.println("Max amplitude squared is: " + PamArrayUtils.max(magSqData));

			return magSqData;
		}
	}


	/**
	 * Repaint  the current data unit. 
	 */
	public void reDrawLastUnit() {
		detectionPlotDisplay.drawCurrentUnit();
	}

	boolean rebuildImage=false;; 
	
	@Override
	public void settingsChanged(FFTPlotParams fftPlotParams){
		
		//check the settings to setup what needs loaded/redrawn again etc. 
		checkSettingsChange( fftPlotParams); 
		 
		setFFTParams(fftPlotParams);
			
		this.reDrawLastUnit(); //this checks settings and will redraw if necessary
	}

	/**
	 * Check new settings against old settings to see what might need recalculated.
	 * Because the spectrogram is quite processor intensive we want to recalculate as 
	 * little as possible. 
	 * @param fftPlotParams - the new settings which will be set. 
	 */
	
	public void checkSettingsChange(FFTPlotParams fftPlotParams) {


//		System.out.println("Check the settings: new: " + fftPlotParams.colorArray.name()
//		+ " old: " + this.fftParams.colorArray.name());
		
		//there are 3 options
		//rebuild the image - assign new colour values to the FFT
		//reloadImage - calculate new set of FFT;'s from raw data 
		//reoloadRaw - reload raw data. This will also set reloadImage to true. 
		
		

		if (fftPlotParams.freqAmplitudeRange[0]!=spectrogramColours.getAmplitudeLimits()[0].doubleValue() || 
				fftPlotParams.freqAmplitudeRange[1]!=spectrogramColours.getAmplitudeLimits()[1].doubleValue() || 
				fftPlotParams.colorArray!=this.fftParams.colorArray
				) {
			this.rebuildImage=true; //need to rebuild the image
		}
		
		//check fft length and hop. 
		if (fftPlotParams.fftLength!=this.fftParams.fftLength ||
				fftPlotParams.fftHop!=this.fftParams.fftHop ||
				fftPlotParams.windowFunction!=this.fftParams.windowFunction ||
				fftPlotParams.normalise!=this.fftParams.normalise) {
			
//			Debug.out.println("CheckSettings:  Image needs relaoded: FFTLength: " 
//					+ (fftPlotParams.fftLength!=this.fftParams.fftLength) + "  FFTHop: "
//					+ (fftPlotParams.fftHop!=this.fftParams.fftHop)  + "  WindowFunction: "
//					+ (fftPlotParams.windowFunction!=this.fftParams.windowFunction) + "  Normalise: "
//					+ (fftPlotParams.normalise!=this.fftParams.normalise) + "  "); 
			
			this.reloadImage=true; 
		}

		//check if raw data needs reloaded. 
		if (fftPlotParams.plotChannel!=this.fftParams.plotChannel ||
				fftPlotParams.detPadding!=this.fftParams.detPadding) {
			//need to load raw data
			this.reloadRaw=true;
		}
		
		//FIXME
		//there is some sort of threading error going on were the image is not built properly
		//maybe a lock issue. For now this is a fix which works  
		if (spectrogram.getTotalPowerSpec()<=1) {
//			Debug.out.println("CheckSettings:  Image needs relaoded power spec"); 
			this.reloadImage=true; 
			this.rebuildImage=false; 
		}
	
//		Debug.out.println("Check image: reloadRaw: "+ reloadRaw + 
//				" reloadImage: " + reloadImage + " rebuildImage: " +rebuildImage
//				+ " number of data units the spectrum thinks it has: " + spectrogram.getTotalPowerSpec()); 
	}

	/**
	 * Set the FFT params.
	 * @param params - the params 
	 */
	public void setFFTParams(FFTPlotParams fftPlotParams) {

		this.spectrogramColours.getAmplitudeLimits()[0].setValue(fftPlotParams.freqAmplitudeRange[0]);
		this.spectrogramColours.getAmplitudeLimits()[1].setValue(fftPlotParams.freqAmplitudeRange[1]);
		this.spectrogramColours.setColourMap(fftPlotParams.colorArray);
		
		simpleFFTBlock.setFftLength(fftPlotParams.fftLength); 
		simpleFFTBlock.setFftHop(fftPlotParams.fftHop); 

		if (rebuildImage) {
			this.spectrogram.reBuildImage();
			rebuildImage=false; 
		}
		
		this.fftParams=fftPlotParams;
	
		if (this.reloadImage || this.reloadRaw) {
			detectionPlotDisplay.drawCurrentUnit();
		}
		// TODO Auto-generated method stub	

	}


	class FFTSpecPlot extends Scrolling2DPlotDataFX{

		public FFTSpecPlot(TimeProjectorFX tdProjector, DataBlock2D fftDataBlock, Plot2DColours spectrogramColours,
				int iChannel, boolean isViewer) {
			super(tdProjector, fftDataBlock, spectrogramColours, iChannel, isViewer);
			// TODO Auto-generated constructor stub
		}


		public FFTSpecPlot(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
			super(specPlotInfo, iChannel);
		}


		@Override
		public void rebuildFinished(){
			reDrawLastUnit();
		}
	}


	/**
	 * Data block for holding FFT Values
	 * @author Jamie Macaulay
	 *
	 */
	public class SimpleFFTDataBlock  extends DataBlock2D<DataUnit2D> {

		private float sampleRate=48000; 

		private int fftLength=1024; 

		private int fftHop=512; 

		public SimpleFFTDataBlock(String dataName, PamProcess parentProcess, int channelMap, int fftHop, int fftLength) {
			super(FFTDataUnit.class, dataName, parentProcess, fftLength);
			this.fftHop=fftHop;
			this.fftLength=fftLength;
		}

		public float getSampleRate(){
			return sampleRate; 
		}

		@Override
		public int getHopSamples() {
			return fftHop;
		}


		@Override
		public int getDataWidth(int sequenceNumber) {
			return fftLength/2;
		}

		@Override
		public double getMinDataValue() {
			return 0;
		}


		@Override
		public double getMaxDataValue() {
			return sampleRate/2;
		}

		/**
		 * @param sampleRate the sampleRate to set
		 */
		public void setSampleRate(float sampleRate) {
			this.sampleRate = sampleRate;
		}


		/**
		 * @param fftLength the fftLength to set
		 */
		public int getFftLength() {
			return fftLength; 
		}


		/**
		 * @param fftLength the fftLength to set
		 */
		public void setFftLength(int fftLength) {
			this.fftLength = fftLength;
		}

		/**
		 * Get Fft HOP
		 * @return
		 */
		public int getFFTHop() {
			return fftHop;
		}

		/**
		 * @param fftHop the fftHop to set
		 */
		public void setFftHop(int fftHop) {
			this.fftHop = fftHop;
		}

		@Override
		public DataTypeInfo getScaleInfo() {
			return dataTypeInfo;
		}

	}


	/**
	 * 
	 * Simple wrapper class for to make the DetectionPlotProjector into a TimeProjectorFX. This is 
	 * because the spectrogram needs a time projector. 
	 * 
	 * @author Jamie Macaulay	
	 *
	 */
	public class FreqTimeProjector extends TimeProjectorFX {

		/**
		 * Reference to the detection plot projector. 
		 */
		DetectionPlotProjector projector; 

		/**
		 * Constructor for the freq time projector. 
		 */
		public FreqTimeProjector(DetectionPlotProjector projector){
			super();
			this.projector=projector; 
		}


		@Override
		public PamAxisFX getTimeAxis(){
			return projector.getAxis(Side.BOTTOM); 
		}


		@Override
		public Coordinate3d getCoord3d(double d1, double d2, double d3) {
			return projector.getCoord3d(d1,d2,d3); 
		}


		@Override
		public Coordinate3d getCoord3d(PamCoordinate dataObject) {
			return projector.getCoord3d((Coordinate3d) dataObject);
		}

		@Override
		public PamCoordinate getDataPosition(PamCoordinate screenPosition) {
			return projector.getDataPosition(screenPosition);
		}

	}


	/**
	 * A simple FFT data block 
	 * @return the SimpleFFTDataBlock 
	 */
	public SimpleFFTDataBlock getFFTDataBlock() {
		return simpleFFTBlock;
	}

	/**
	 * Get the projector
	 * @return the time projector 
	 */
	public TimeProjectorFX getProjector() {
		return this.timeFreqProjector;
	}

	/**
	 * Get scroll start
	 * @return the scroll start
	 */
	public long getScrollStart() {
		return rawDataOrder.getRawDataObserver().getLoadStart();
	}

	/**
	 * @return the spectrogram
	 */
	public Scrolling2DPlotDataFX getSpectrogram() {
		return spectrogram;
	}

	/**
	 * FFT Order for raw data. 
	 */
	private class  FFTDataOrder extends RawDataOrder {

		@Override
		public void dataLoadingFinished(double[] rawData) {
			reloadRaw=false;
//			System.out.println("The last raw data unit has finished loading");
			Platform.runLater(()->{;
				detectionPlotDisplay.drawCurrentUnit();
			});
		}


	}
	
	
	public RawDataOrder getRawDataOrder() {
		return rawDataOrder;
	}

	public void setRawDataOrder(RawDataOrder rawDataOrder) {
		this.rawDataOrder = rawDataOrder;
	}

	
	@Override
	public double[][] getSpectrogram(D pamDetection, int fftLen, int fftHop, int windowType){
		//this function does nothing because the FFT paint has been overriden
		return null; 
	}



}
