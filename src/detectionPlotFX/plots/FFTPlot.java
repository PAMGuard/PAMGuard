package detectionPlotFX.plots;

import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import PamController.PamController;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import dataPlotsFX.data.DataTypeInfo;
import dataPlotsFX.data.FFTPlotManager;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import detectionPlotFX.layout.DetectionPlot;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot.FreqTimeProjector;
import detectionPlotFX.projector.DetectionPlotProjector;
import javafx.geometry.Side;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/**
 * Plots the spectrogram of a detection which already has a calculated spectrgram, e.g. a a RawDataHolder. 
 * <p>
 * Note that this is not particularly suitable for data that needs to load raw data from sound files. Use RawFFTPlot 
 * instead for this functionality. 
 * 
 * @author Jamie Macaulay
 *
 * @param <D> - the detection type. 
 */
public abstract class FFTPlot<D extends PamDataUnit> implements DetectionPlot<D> {

	/**
	 * Plot kHz instead of 
	 */
	private static final double USE_KHZ_FREQ = 2000;


	/**
	 * Reference to the detection plot display. 
	 */
	protected DetectionPlotDisplay detectionPlotDisplay;


	/**
	 * The last data unit which has been plotted. 
	 */
	protected D lastData;

	/**
	 * The time frequency projector 
	 */
	protected FreqTimeProjector timeFreqProjector;


	/**
	 * The spectrogram colours
	 */
	protected StandardPlot2DColours spectrogramColours= new StandardPlot2DColours();

	/**
	 * The FFT parameters. 
	 */
	protected FFTPlotParams fftParams = createPlotParams();


	private static DataTypeInfo dataTypeInfo = new DataTypeInfo(ParameterType.FREQUENCY, ParameterUnits.HZ);

	/**
	 * The settings pane for the FFT. 
	 */
	private FFTSettingsPane<?> setttingsPane;

	/**
	 * The current writable image. 
	 */
	private FFTWriteableImage writableImage;


	/**
	 * True if plotting as kHz instead of Hz - usually for higher frequency data greater than USE_KHZ_FREQ;
	 */
	private boolean useKHz = false;

	public FFTPlot(DetectionPlotDisplay displayPlot, DetectionPlotProjector projector) {
		this.detectionPlotDisplay=displayPlot; 
	
		//set the FFT params. 
		//setFFTParams(fftParams);

	}

	@Override
	public String getName() {
		return "FFT Plot";
	}

	@Override
	public void setupPlot() {
		detectionPlotDisplay.setAxisVisible(false, false, true, true);
		detectionPlotDisplay.getAxisPane(Side.LEFT).setnPlots(1); //TODO- make two panels?
		//		timeFreqProjector.setTimeAxis(detectionPlotDisplay.getAxis(Side.BOTTOM));
	}

	@Override
	public void setupAxis(D pamDetection, double sR, DetectionPlotProjector projector) {
		//	//detection display
		//	if (pamDetection != null) {
		//		sR = pamDetection.getParentDataBlock().getSampleRate();
		//	}
		projector.setEnableScrollBar(true);

		setupFreqAxis(0, sR/2, projector);
	
		setUpTimeAxis(pamDetection.getDurationInMilliseconds(), projector);
		
	}


	/**
	 * Setup the time axis.  
	 * @param pamDetection - the pamDetection
	 * @param southAxis - the south axis. 
	 * @param bin1
	 * @param bin2
	 */
	private void setUpTimeAxis(D pamDetection, DetectionPlotProjector projector) {
		//double sampleRate = pamDetection.getParentDataBlock().getSampleRate();
		//		double sampleRate= this.detectionPlotDisplay.getCurrentDataInfo().getHardSampleRate();
		projector.setAxisMinMax(0, (pamDetection.getDurationInMilliseconds()+2*fftParams.detPadding)/1000. , Side.BOTTOM, "Time (seconds)");
	}

	/**
	 * Setup the time axis within the given duration. 
	 * @param millisDuration
	 * @param southAxis
	 */
	public void setUpTimeAxis(double millisDuration, DetectionPlotProjector plotProjector) {
		//plotProjector.setAxisMinMax(0, millisDuration, Side.BOTTOM, "Time (ms)");
		
		//System.out.println("Setupnthe time axis the FFT plot"); 

		
		//double sampleRate = pamDetection.getParentDataBlock().getSampleRate();
		double sampleRate= this.detectionPlotDisplay.getCurrentDataInfo().getHardSampleRate();

		//plotProjector.setAxisLabel("Time (samples)", Side.TOP);

		plotProjector.setMinScrollLimit(0);
		plotProjector.setMaxScrollLimit(millisDuration);
		plotProjector.setScrollAxis(Side.BOTTOM);

//		System.out.println("Wigner Time Axis: min " + (plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000.)*sampleRate + 
//				" max: " + 	(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000)*sampleRate);


		plotProjector.setAxisMinMax((plotProjector.getAxis(Side.BOTTOM).getMinVal()/1000*sampleRate), 
				(plotProjector.getAxis(Side.BOTTOM).getMaxVal()/1000*sampleRate), Side.TOP);
		
		
		//System.out.println("Paint the FFT plot: " +  projector.getAxis(Side.BOTTOM).getMaxVal()); 



		//		if (msLen < 2) {
		////			msLen *= 1000;
		////			msStart *= 1000;
		//			plotProjector.setAxisLabel("Time (micro-s)", Side.BOTTOM);
		//		}
		//		else {
		plotProjector.setAxisLabel("Time (ms)", Side.BOTTOM);
	}


	/**
	 * Set the frequency axis 
	 * @param pamDetection - the detection to set axis for. 
	 * @param freqAxis - the frequency axis. 
	 */
	private void setupFreqAxis(D pamDetection, DetectionPlotProjector plotProjector) {
		setupFreqAxis(0., pamDetection.getParentDataBlock().getSampleRate()/2., plotProjector);
	}

	/**
	 * Set up the frequency axis 
	 * @param minFreq
	 * @param maxFreq
	 * @param freqAxis
	 */
	public void setupFreqAxis(double minFreq, double maxFreq, DetectionPlotProjector projector) {
		if (maxFreq>USE_KHZ_FREQ) {
			//use kHz
			//projector.getAxis(Side.LEFT).setLabelScale(0.001);
			this.useKHz = true;
			projector.setAxisMinMax(minFreq, maxFreq/1000., Side.LEFT, "Frequency (kHz)");
		}
		else {
			projector.setAxisMinMax(minFreq, maxFreq, Side.LEFT, "Frequency (Hz)");
		}		
	}

	@Override
	public synchronized void paintPlot(D pamDetection, GraphicsContext gc, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
			
		//System.out.println("Paint the FFT plot: " +  projector.getAxis(Side.BOTTOM).getMaxVal()); 
		
		double clipLevel = ArrayManager.getArrayManager().getCurrentArray().getClipLevel(PamUtils.getChannelArray(pamDetection.getChannelBitmap())[0]); 

		int y =  this.fftParams.fftLength/2; 
		int x =  (int) Math.ceil(pamDetection.getSampleDuration()/(double) this.fftParams.fftHop); 
		
		if (checkSettingsChange(writableImage, fftParams, pamDetection)) {
			//System.out.println("Redraw Image: X " +x + " " + y); 
			writableImage = new FFTWriteableImage(x,y, fftParams, pamDetection); 
			//draw the spectrogram onto the writable image. 
			drawClipSpectrogram(pamDetection,  this.fftParams.fftLength, this.fftParams.fftHop, this.fftParams.windowFunction, clipLevel,  writableImage); 
		}
		
		double xStart = writableImage.getWidth()*(projector.getAxis(Side.BOTTOM).getMinVal()-projector.getMinScrollLimit())
				/(projector.getMaxScrollLimit()-projector.getMinScrollLimit()); 

		double xEnd = writableImage.getWidth()*(projector.getAxis(Side.BOTTOM).getMaxVal()-projector.getMinScrollLimit())
				/(projector.getMaxScrollLimit()-projector.getMinScrollLimit()); 


		gc.setImageSmoothing(false);
//		System.out.println("FFTPlot size: "+ writableImage.getWidth()+ "  "+ writableImage.getHeight());
//		//calculate the start and end of the wigner image. 
		gc.drawImage(writableImage, xStart, 0, 
				(xEnd - xStart), writableImage.getHeight(), 0, 0, rectangle.getWidth(), rectangle.getHeight());
		
//		//draw the full image. 
//		gc.drawImage(writableImage, 0, 0, 
//				writableImage.getWidth(), writableImage.getHeight(), 0, 0, rectangle.getWidth(), rectangle.getHeight());
	}

	
	/**
	 * Check whether the FFT image needs redrawn. 
	 * @param writableImage2 - the current image. 
	 * @param fftParams2 - the current params. 
	 * @param pamDetection - the current detection. 
	 * @return true if the image needs to be redrawn. 
	 */
	private boolean checkSettingsChange(FFTWriteableImage writableImage2, FFTPlotParams fftParams2, D pamDetection) {
		if (writableImage2==null 
				|| writableImage2.fftParams.fftHop!=fftParams2.fftHop 
				|| writableImage2.fftParams.fftLength!=fftParams2.fftLength
				|| writableImage2.pamDetection!=pamDetection 
				|| writableImage2.fftParams.freqAmplitudeRange[0] != fftParams2.freqAmplitudeRange[0]
				|| writableImage2.fftParams.freqAmplitudeRange[1] != fftParams2.freqAmplitudeRange[1] 
				|| writableImage2.fftParams.windowFunction != fftParams2.windowFunction
				|| writableImage2.fftParams.colorArray != fftParams2.colorArray
				) return true; 
			return false; 
	}

	/**
	 * Paint the detection over the FFT. 
	 * @param graphicsContext - the graphics handle
	 * @param windowRect - the window rectangle in pixels
	 * @param pamAxis - the pamAxis whihc are being plotte on. 
	 */
	public abstract void paintDetections(D detection, GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector); 


	/**
	 * Get the spectrogram image from the data unit. 
	 * @param pamDetection - the spectrogram image. 
	 * @return the spectrogram image. 
	 */
	public abstract double[][] getSpectrogram(D pamDetection, int fftSize, int fftHop, int windowType);
	
	/**
	 * Draw a spectrogram of a detection onto a writeable image. 
	 * @param g - the graphics handle
	 * @param spectrum - the click spectrum in FFT bins. 
	 * @param tC -the time in pixels 
	 * @param the clip level - add the clip level. Note that we add the clip level because it is one number that
	 * takes the voltage range into account
	 * @param tdProjector - the projector 
	 */
	protected void drawClipSpectrogram(D pamDetection,  int fftLength, int fftHop, int windowType, double clipLevel, WritableImage writableImage){
		//do not draw
		
		double millisPerPixel = pamDetection.getDurationInMilliseconds()/writableImage.getWidth(); 
		
		
		double[][]  spectrogram = getSpectrogram(pamDetection, fftLength, fftHop, windowType); 
		float sR = pamDetection.getParentDataBlock().getSampleRate();

		if (spectrogram==null) {
			System.err.println("FFTPlot.drawClipSpectrogram: spectrogram is null"); 
			return; 
		}
		int tc = -1;

		double timeMillisFFT; 
		int newtc; 
		//if zero just draw one line to be efficient
		//		System.out.println("SpectrogramLength: "  + spectrogram.length); 
		
		//maybe compress image? 

		//the number of pixel slices that each FFT line trake 
		int nslices = (int) Math.ceil((1000*fftLength/sR)/millisPerPixel); 

		for (int i=0; i<spectrogram.length; i++) {
			
			timeMillisFFT = (i*fftHop*1000.)/sR; 
			
			newtc = (int) (timeMillisFFT/millisPerPixel);

			//newtc = (int) writableImage.getScrollingPLot2DSegmenter().getImageXPixels(timeMillisFFT, writableImage); 
			//			if (tc!=newtc) {
			//plot the spectrogram line
			
			//System.out.println("Plot spectrogram: timeMillisFFT " +  i +  "  "  + timeMillisFFT + " tc: " + newtc + " fftLength: " + fftLength + " fftHop: " + fftHop ); 
			
			tc=newtc; 

			//double[] minmax = PamUtils.getMinAndMax(spectrogram); 
			//System.out.println("Plot spectrogram: tc " + tc + " minmax: " +  20*Math.log10(minmax[0])+clipLevel + " " + 20*Math.log10(minmax[1])+ " Clip level: " + clipLevel); 

			
			//how many lines in the image does the FFT take up?

			for (int j=0; j<writableImage.getHeight(); j++) {

				//what is the spectrum value for the height?
				int spec = (int) ((j/(double) writableImage.getHeight())*spectrogram[i].length); 

				if (spec>spectrogram[i].length-1) spec = spectrogram[i].length-1; 

				//find the correct colour for the spectrogram value and draw
				for (int k=0; k<nslices; k++) {
					if ((tc+k)>=writableImage.getWidth()) continue; 
					
					//image was upsidedown in y
					writableImage.getPixelWriter().setColor(tc+k,  (int) (writableImage.getHeight()-j-1), getSpectrumColour(spectrogram[i][spec], clipLevel, sR, fftLength));
				}
			}
			//			}

		}
	}
	
	/**
	 * Get the colour for the spectrum. 
	 * @param spectrumValue - the spectrum value
	 * @param the clip level - add the clip level. Note that we add the clip level because it is one number that
	 * takes the voltage range into account. 
	 * @return the new color. 
	 */
	private Color getSpectrumColour(double spectrumValue, double clipLevel, float sampleRate, int fftLength) {
		//TODO - need to add microphone and hydrophone senstivities here. 
		//System.out.println("Clip level: " + clipLevel); 
		if (spectrogramColours.getClass()==null) {
			//update();
		}
		double dBcol = FFTPlotManager.fftAmplitude2dB(spectrumValue,  sampleRate, clipLevel, fftLength, true); 
		return spectrogramColours.getColourArray()[StandardPlot2DColours.getColourIndex(dBcol, getFFTParams().freqAmplitudeRange[0], 
				getFFTParams().freqAmplitudeRange[1], spectrogramColours.getColourArray().length)]; 
	}


	/**
	 * Create the FFT settings pane. 
	 * @return the FFT settings pane. 
	 */

	protected FFTSettingsPane<?> createSettingsPane(){
		return new FFTSettingsPane<D>(null, this);
	}

	/**
	 * Enable the time buffer spinner.
	 * @param enable - true to enable
	 * */
	public void enableTimeSpinner(boolean enable) {
		if (setttingsPane==null) {
			getSettingsPane();
		}
		setttingsPane.enableTimeBufferSpinner(enable);
	}
	
	
	/**
	 * Called whenever the settings are changed. 
	 * @param - the new settings. 
	 */
	public void settingsChanged(FFTPlotParams fftPlotParams) {
		//check to see if any flags need set for repainting 
		//checkSettingsChange(fftPlotParams); 

		//				//now set the new settings. 
		setFFTParams(fftPlotParams);
		
		this.reDrawLastUnit(); //this checks settings and will redraw if necessary
		
		//detectionPlotDisplay.setupScrollBar(); 
	}


	@Override
	public Pane getSettingsPane() {
		if (setttingsPane==null){
			setttingsPane= createSettingsPane();
			setttingsPane.setParams(this.fftParams) ;

			//add settings listener to dynamic settings pane. 
			setttingsPane.addSettingsListener(()->{
				//this needs to be a new instance of the the FFTPlotParams or some settings don't 
				//register a chnage
				settingsChanged(setttingsPane.getParams(createPlotParams() )); 
			});

			//					/////////*****Test Pane*******///////////			
			//					PamButton testButton = new PamButton("TEST"); 
			//					testButton.setOnAction((action)->{
			////						this.detectionPlotDisplay.drawCurrentUnit();
			//						setttingsPane.setParams(this.fftParams) ;
			//					});
			//					
			//					setttingsPane.setCenter(testButton);
			//		
			//					
			//					PamButton testButton1 = new PamButton("TEST2"); 
			//					testButton1.setOnAction((action)->{
			//						detectionPlotDisplay.getPlotPane().getPlotCanvas().getGraphicsContext2D().drawImage(spectrogram.getWritableImage(), 0, 0, 200, 200);
			//					});
			//					
			//					
			//					setttingsPane.setTop(testButton1);
			//					/////////*****Test Pane*******///////////			
		}
		return (Pane) setttingsPane.getContentNode();
	}
	
	/**
	 * Create plot paramters for the FFT plot params
	 * @return
	 */
	public FFTPlotParams createPlotParams() {
		return new FFTPlotParams();
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
			try {
				//TODO- need to pick correct data block 
				daqProcess = (AcquisitionProcess) PamController.getInstance().getRawDataBlock(0).getSourceProcess();
				daqProcess.prepareFastAmplitudeCalculation(iChannel);
			}
			catch (ClassCastException e) {
				e.printStackTrace();
				return magSqData;
			}

			magSqData =  fftData.magsq();

			//			System.out.println("Magnitude squared length is: " + magSqData.length + " "+ magSqData[0]);

			for (int i = 0; i < magSqData.length; i++) {
				magSqData[i] = daqProcess.fftAmplitude2dB(magSqData[i], iChannel, 
						sR, magSqData.length*2, true, false);
			}

			//			System.out.println("Magnitude squared length is: " + magSqData.length + " "+ magSqData[0]);

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

	/**
	 * Set the FFT params.
	 * @param params - the params 
	 */
	public void setFFTParams(FFTPlotParams fftPlotParams) {

		this.spectrogramColours.getAmplitudeLimits()[0].setValue(getFFTParams().freqAmplitudeRange[0]);
		this.spectrogramColours.getAmplitudeLimits()[1].setValue(getFFTParams().freqAmplitudeRange[1]);
		this.spectrogramColours.setColourMap(fftPlotParams.colorArray);


		this.fftParams=fftPlotParams;

	}
	
	/**
	 * Get the FFT plot params. 
	 * @return the fft plot params. 
	 */
	public FFTPlotParams getFFTParams() {
		return this.fftParams;
	}
	
	
	private class FFTWriteableImage extends WritableImage {


		/**
		 * The FFT params
		 */
		private FFTPlotParams fftParams;
		
		/**
		 * The PAM detection used for plotting 
		 */
		private D pamDetection;


		public FFTWriteableImage(int x, int y, FFTPlotParams fftParams, D pamDetection) {
			super(x,y); 
			this.fftParams = fftParams.clone(); 
			this.pamDetection=pamDetection; 
		}
		
	}
	
	public boolean isUseKHz() {
		return useKHz;
	}



}