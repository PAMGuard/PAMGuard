package detectionPlotFX.clickTrainDDPlot;

import java.util.ArrayList;

import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.DataUnit2D;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickDetector.ClickDetection;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.RawFFTPlot;
import detectionPlotFX.plots.FFTSettingsPane;
import detectionPlotFX.projector.DetectionPlotProjector;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

/**
 * Waterfall spectrogram plot for super detections detections which have sub detections
 * that can provide FFT data. 
 * 
 * @author Jamie Macaulay
 * @param <D> - the super detection type
 * @param <E> - the sub detection type
 *
 */
public class WaterfallSpecPlot<D extends DetectionGroupDataUnit> extends RawFFTPlot<D>{

	/**
	 * Reference to the image task. 
	 */
	private Task<Integer> imageTask;
	
	/*
	 * The max number of sub detections to run on current thread. Otherwise image is generated on another thread. 
	 */
	private int MAX_SUB_DET_TASK;

	public WaterfallSpecPlot(DetectionPlotDisplay displayPlot, DetectionPlotProjector projector) {
		super(displayPlot, projector);
//		this.getFFTParams().minColourValue=0; 
		this.getFFTParams().freqAmplitudeLimits[1]=150; 
		this.getFFTParams().freqAmplitudeLimits[0]=0; 
//		this.getFFTParams().upperColourValue=70; 
	}

	@Override
	public synchronized void paintPlot(D ctDataUnit, GraphicsContext graphicsContext, Rectangle rectangle, DetectionPlotProjector projector, int flag) {
		//so we have the data unit. 
		if (ctDataUnit!=lastData){
			reloadImage=true; 
		}
		lastData = ctDataUnit; 
		
		//could be a lot of clicks so should make a load image task. 
		if (reloadImage){
			//Debug.out.println("LOAD WATERFALL IMAGE data: seconds: " + ctDataUnit.getSubDetections().size());
			//reset the spectrogram and clear image
			//starts on a new thread so function returns until complete. 
			startImageLoad(ctDataUnit.getSubDetections()); 
			//exit here. 
		}
		else {
			//repaint the image!!
			//Debug.out.println("PAINT WATERFALL IMAGE the image for: " + ctDataUnit.getUID());
			paintSpecImage(graphicsContext,  rectangle, projector);
			paintDetections(ctDataUnit,  graphicsContext,  rectangle, projector) ;
		}
	}
	
	@Override
	protected synchronized void paintSpecImage(GraphicsContext graphicsContext, Rectangle windowRect, DetectionPlotProjector projector) {
//		Debug.out.println("FFTPlot: PaintSpecImage: " + windowRect.getWidth() + "  " + windowRect.getHeight()+ " spec N: " + getSpectrogram().getTotalPowerSpec());

//		if (imageTask==null || imageTask.isDone()) {
		//calculate the size of the scrollbar
		graphicsContext.clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());

		//plot the spectrogram. 
		getSpectrogram().drawSpectrogram(graphicsContext, windowRect, Orientation.HORIZONTAL,
				timeFreqProjector.getTimeAxis(), 0, false);
//		}
	}
	
	
	@Override
	public void setupAxis(D pamDetection, double sR, DetectionPlotProjector projector) {
		//detection display
		if (pamDetection != null) {
			sR = pamDetection.getParentDataBlock().getSampleRate();
		}

		setupFreqAxis(0, sR/2, projector);

		//must have special case in time for waterfall spectrogram plot. 
		if (pamDetection != null) {
			double duration = 1000*(getFFTParams().fftLength*(pamDetection.getSubDetectionsCount()-1))/pamDetection.getParentDataBlock().getSampleRate();
			//System.out.println("Duration: " + duration); 
			setUpTimeAxis(duration, projector);
		}
	}

	/**
	 * Generate image for the waterfall spectrogram
	 */
	private void generateImage(ArrayList<PamDataUnit<?,?>> detectionList) {
		
		DataUnit2D fftDataUnit; 
		long millis;
		getSpectrogram().resetForLoad();

		for (int i=0; i<detectionList.size(); i++) {
//			Debug.out.println("FFT WATERFALL IMAGE data: seconds: " + detectionList.get(i) + " of : " + detectionList.size());
			
			fftDataUnit = getFFTdata(detectionList.get(i), getFFTParams().fftLength, getFFTParams().plotChannel); 
//			Debug.out.println("millis: " + millis);
			
			millis= (long) (1000*((double) (getFFTParams().fftLength*i)/simpleFFTBlock.getSampleRate())); //need to convert sR to double otherwise screws up number in division
			fftDataUnit.setTimeMilliseconds(millis);
			simpleFFTBlock.addPamData(fftDataUnit);
			getSpectrogram().new2DData(fftDataUnit);
		}
		
		reloadImage=false; 

	}
	
	/**
	 * Create a task to load the waterfall spectrogram image image. 
	 * @param detectionList - the detection list. 
	 */
	private synchronized void startImageLoadTask(ArrayList<PamDataUnit<?,?>> detectionList) {
		imageTask = new Task<Integer>() {
			@Override protected Integer call() throws Exception {
				Thread.sleep(100); //give some time to cancel before a data load. 
				if (isCancelled()) {
					return -1; 
				}
				try{
					generateImage( detectionList);
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
			reloadImage=false; 
		}); 

		th = new Thread(imageTask);
		th.setDaemon(true);
		th.start();
	}



	/**
	 * Starts the load thread. 
	 */
	private synchronized void startImageLoad(ArrayList<PamDataUnit<?,?>> detectionList) {
		
		//clear the simple data block and set correct settings
		this.simpleFFTBlock.setFftHop(getFFTParams().fftLength); //always the same as length for the waterfall spectrogram
		this.simpleFFTBlock.setFftLength(getFFTParams().fftLength);
		this.simpleFFTBlock.setSampleRate(detectionList.get(0).getParentDataBlock().getSampleRate());
		this.simpleFFTBlock.clearAll();//clear all the data units.

		//setup spectrogram
		getSpectrogram().checkConfig(); 
		
		//cancel a running task 
		if (imageTask!=null) imageTask.cancel();

		try {
			if (th!=null) th.join(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (detectionList.size()>MAX_SUB_DET_TASK) {
			//run on another thread
			startImageLoadTask(detectionList);
			return; //thread will call repaint when complete
		}
		else {
			//run in current thread 
			generateImage(detectionList);
			this.detectionPlotDisplay.drawCurrentUnit(); //repaint
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected FFTSettingsPane createSettingsPane() {
		return new WaterFallSpecSettingsPane(null, this);
		
	}
	/**
	 * Get the FFT data for a data unit.  
	 * @param dataUnit - the data unit to extract FFT data from
	 * @param fftLen - the desired fft length. 
	 */
	public DataUnit2D getFFTdata(PamDataUnit dataUnit, int fftLen, int channel) {
		//data units will be clicks 
		if (dataUnit instanceof ClickDetection) {
			ComplexArray fftData  = ((ClickDetection) dataUnit).getComplexSpectrum(channel, fftLen); 

			SimpleFFTDataUnit fftDataUnit = new SimpleFFTDataUnit(dataUnit.getTimeMilliseconds(), 
					PamUtils.makeChannelMap(channel), dataUnit.getStartSample(), 
					fftLen, fftData, 0, dataUnit.getParentDataBlock().getSampleRate()); 

			return fftDataUnit; 
		}
		return null; 
	}

	@Override
	public void paintDetections(D detection, GraphicsContext graphicsContext, Rectangle windowRect,
			DetectionPlotProjector projector) {
	}


}
