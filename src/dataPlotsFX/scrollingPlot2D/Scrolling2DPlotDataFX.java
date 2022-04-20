package dataPlotsFX.scrollingPlot2D;

import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import PamguardMVC.DataBlock2D;
import PamguardMVC.DataUnit2D;
import dataPlotsFX.projector.TimeProjectorFX;

/**
 * Contains the spectrogram data for a single channel. 
 * Will only exist and be populated if that channel is 
 * included in the spectrogram display. 
 * @author Doug Gillespie and Jamie Macaulay
 * 
 * Was SpectrogramPlotDataFX
 *
 */
public class Scrolling2DPlotDataFX {

	/**
	 * Channel the spectrogram shows data from. 
	 */
	private int iChannel;

	/**
	 * Reference to the FFT data block
	 */
	private DataBlock2D dataBlock2D;

	/**
	 * Reference to the TDMainDisplay- contains info on time axis etc. 
	 */
//	private TDDisplayFX tdMainDisplay;

	/**
	 * The current data width and fftHop values.
	 * width is fftLength/2 with the new system.	 *  
	 */
	private int dataWidth, hopSamples;

	/**
	 * Integer power spectrum levels in dB, scaled to give decent resolution. 
	 */
	private short[][] intPowerSpectrum;

	/**
	 * Array for accumulating data which spec image is pre scaled / compressed in time. 
	 */
	private double[] scaledLineSpectrum;

	/**
	 * Index of latest bin written to intPowerSpectrum. 
	 */
	private int lastPowerSpecBin;

	/**
	 * Millisecond time of last bin written to intPowerSpectrum.
	 */
	private long lastPowerSpecTime;

	/**
	 * Decibel scale. Allows up to 32676/25 = 1300 dB with .04dB resolution.
	 */
	private double decibelIntScale = 25;

	/**
	 * Number of time bins in the big history buffer of powerspec data. 
	 */
	private int historyBins;

	/**
	 * time scale of spectrogram in seconds per pixel. 
	 */
	private double timeScale;

	/**
	 * time range of plot in seconds. 
	 */
	private double timeRange;

	/**
	 * Time compression of spectrogram image. 
	 */
	private int timeCompression = 1;

	/**
	 * The size of the time axis in pixels
	 */
	private double timePixels;

	/**
	 * A running total of the number of pixels drawn on the spectrogram
	 */
	private long totalXDraw=0;

	/**
	 * A running total of fft data units added to the intPowerSpectrum[] buffer. 
	 */
	private long totalPowSpec=0;

	/**
	 * Spectrogram image. This is held in memory and used for drawing onto the TDGraphFX plot panes.  
	 */
	private WritableImage writableImage;

	/**
	 * This is the position of the last write on the spectrum image in memory. The spectrum is drawn from the writable 
	 * image start untill then and then from the start again. 
	 * imageXPos keeps a track of where the last pixels were written.  
	 */
	private double imageXPos;

	/**
	 * Indicates whether the image is being rebuilt. If this is the case we don't want spectrogram updating when incomming fft data is available., 
	 */
	private boolean reBuildImage=false;

	/**
	 * The number of fft bins to have been added to the 
	 */
	private int rebuildAdd=0; 

	/**
	 * Basically this is the amount that the spectogram image can stretched or shrunk before it is rebuilt. Note: Cannot be <= 1 or else end 
	 * up in infinite while loops
	 */
	//FIXME- if this is anything other than 2 spectrogram crashes.
	private static int compressionLevel=2; 

	/**
	 * Pixel writer- writes to writable image. 
	 */
	private PixelWriter writableRaster;

	/**
	 * The last FFT data unit to have been written. 
	 */
	private DataUnit2D lastFFT;

	/*
	 * Max storage in megabytes. will allow about 3 minutes of normal data. 
	 */
	private static final long MAXSTORAGESIZE =  50*1024 *1024; 
	

	/*
	 * Max size of the image. This is used because some graphics cards can result i
	 */
	private static final long MAXIMAGESIZE =  3092; 
	
	/**
	 * An fft data unit used to fill spectrogram with blank data. 
	 */
	private DataUnit2D blankDataUnit2D;

	/**
	 * True if in viewer mode. 
	 */
	private boolean isViewer=false ;
	
	/*
	 * The current frequency limits. Note: easy to bind these to another DoubleProperty 
	 * Not always frequency, so don't call it that. 
	 */
//	private DoubleProperty[] verticalBinRange = new DoubleProperty[2];

	/**
	 * Spectrogram colours.
	 */
	private Plot2DColours specColors;

	/**
	 * The projector. 
	 */
	private TimeProjectorFX tdProjector;
	
	/**
	 * True to flip the spectrogram. 
	 */
	private boolean reverse=false; 


	private Scrolling2DPlotInfo specPlotInfo; 


	public Scrolling2DPlotDataFX(Scrolling2DPlotInfo specPlotInfo, int iChannel) {
		super();
		this.iChannel = iChannel;
		dataBlock2D = specPlotInfo.getDataBlock2D();
		this.tdProjector = specPlotInfo.getTDGraph().getGraphProjector();
		this.specColors=specPlotInfo; 
		this.specPlotInfo = specPlotInfo;
		
		this.blankDataUnit2D=new DummyDataUnit2D(0, 1<<iChannel, 0, 0); 
		blankDataUnit2D.sortOutputMaps(dataBlock2D.getChannelMap(), dataBlock2D.getSequenceMapObject(), 1<<iChannel);
		
		//bind frequency too FFTData. 
		isViewer=specPlotInfo.isViewer();
//		verticalBinRange[0]=new SimpleDoubleProperty(0); 
//		verticalBinRange[1]=new SimpleDoubleProperty(100); 
//		
//		//bind frequency ranges. 
//		verticalBinRange[0].bind(specPlotInfo.di[0]);
//		verticalBinRange[1].bind(specPlotInfo.displayBinRange[1]);
	}
	
	/**
	 * Called when making a datagram. 
	 * @param tdProjector
	 * @param fftDataBlock
	 * @param spectrogramColours
	 * @param iChannel
	 * @param isViewer
	 */
	public Scrolling2DPlotDataFX(TimeProjectorFX tdProjector, DataBlock2D fftDataBlock, Plot2DColours spectrogramColours, int iChannel, boolean isViewer) {
		super();
		this.iChannel = iChannel;
		this.dataBlock2D = fftDataBlock;
		this.specColors=spectrogramColours; 
		this.tdProjector = tdProjector;

		this.blankDataUnit2D=new DummyDataUnit2D(0, 1<<iChannel, 0, 0); 
		
		this.isViewer=isViewer; 
		reverse=true; 
		//bind frequency to FFTData. 
//		verticalBinRange[0]=new SimpleDoubleProperty(0); 
//		verticalBinRange[1]=new SimpleDoubleProperty(100); 
	}


//	/**
//	 * The frequency range, min and max for the spectrogram. 
//	 * @return the frequency range. 
//	 */
//	public DoubleProperty[] getFreqBinRange() {
//		return verticalBinRange;
//	}

	/**
	 * New fft data arrived for this channel. 
	 * @param fftDataUnit fft data unit. 
	 */
	public void new2DData(DataUnit2D fftDataUnit) {
				
		checkConfig();
		
		//if the image is currently being rebuilt keep a track of the number of bins that have been added but dump data.  
		if (reBuildImage){
			lastPowerSpecTime = fftDataUnit.getTimeMilliseconds();
			rebuildAdd++; 
			return; 
		}
	
		//check for gaps in data.
		if (!isViewer) checkTimeGap(fftDataUnit);
		
		//		if (true) return;
		fillPowerSpecLine(fftDataUnit);
		drawImageLine(lastPowerSpecBin);
		
		//lastFFT=fftDataUnit;
	}
	

	double timediff;

	/**
	 * Check that the new fft is concurrent to the last fft. If not fill the spectrogram with blank sapce. 
	 */
	public void checkTimeGap(DataUnit2D dataUnit2D){
		
		if (lastFFT==null) return; 
		
		//calculate time difference. 
		timediff=dataUnit2D.getTimeMilliseconds() - (lastFFT.getTimeMilliseconds()+1000.*(hopSamples)/dataBlock2D.getSampleRate());
		
//		timediff=fftDataUnit.getStartSample() - (lastFFT.getStartSample() + fftHop);
		
		if (timediff>(intPowerSpectrum.length*dataBlock2D.getHopSamples() / dataBlock2D.getSampleRate()*1000)){
			System.out.println("Time since last FFT is too big. Rebuilding store:");
			//clean slate
			rebuildStore();
			createWritableImage();
			return; 
		}
		
		if (timediff>10){
			//System.out.println("Time difference is too large. Filling spectrogram with blank data");
			long timeMillis=lastFFT.getTimeMilliseconds();
			while (timeMillis<dataUnit2D.getTimeMilliseconds()){
				//System.out.println("Fill! "+timeMillis); 
				timeMillis=(long) (timeMillis+1000.*(hopSamples)/dataBlock2D.getSampleRate()); 
				blankDataUnit2D.setTimeMilliseconds(timeMillis);
				fillPowerSpecLine(blankDataUnit2D);
				drawImageLine(lastPowerSpecBin);

			}
		}
		
	}
	


	/**
	 * Fill a line in the integer array that stores historical data. 
	 * @param fftDataUnit
	 */
	private void fillPowerSpecLine(DataUnit2D fftDataUnit) {
		if (!reBuildImage) totalPowSpec++;
		if (++lastPowerSpecBin >= historyBins) {
			lastPowerSpecBin = 0;
		}
		lastPowerSpecTime = fftDataUnit.getTimeMilliseconds();
//		ComplexArray fftData = fftDataUnit.getFftData();
		
//		AcquisitionProcess daqProcess = null;
//		// get the acquisition process. 
//		try {
//			daqProcess = (AcquisitionProcess) (fftDataBlock.getSourceProcess());
//			daqProcess.prepareFastAmplitudeCalculation(iChannel);
//		}
//		catch (ClassCastException e) {
//			return;
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
		
		if (lastPowerSpecBin<0) return;
		if (lastPowerSpecBin >= intPowerSpectrum.length) return;
		double[] magData = fftDataUnit.getMagnitudeData();
		if (magData == null) { // needed for the blank data unit. 
			//System.out.println("Fill the bin with zeros! " + lastPowerSpecBin);

			for (int i = 0; i < dataWidth; i++) {
				intPowerSpectrum[lastPowerSpecBin][i] = 0;
			}			
		}
		else {
			int n = Math.min(dataWidth, magData.length);
//			System.out.printf("Fill bin %d of %d bin width %d data width %d\n", lastPowerSpecBin, intPowerSpectrum.length, magData.length,  intPowerSpectrum[0].length);
			int indexn; 
			for (int i = 0; i < n; i++) {
				if (reverse) indexn=n-i-1;
				else indexn=i; 
				intPowerSpectrum[lastPowerSpecBin][i] = (short) (magData[indexn]*25.);
			}
		}
//		if (FFTDataUnit.class.isAssignableFrom(fftDataUnit.getClass())) {
//			
//		for (int i = 0; i < dataWidth; i++) {
//			ComplexArray fftData = ((FFTDataUnit) fftDataUnit).getFftData();
//			if (fftData!=null) intPowerSpectrum[lastPowerSpecBin][i] = (short) (daqProcess.fftAmplitude2dB(fftData.magsq(i), iChannel, 
//					fftDataBlock.getSampleRate(), dataWidth*2, true, true)*25);
//			else intPowerSpectrum[lastPowerSpecBin][i] = 0; 
//			//			intPowerSpectrum[lastPowerSpecBin][i] = (short) (daqProcess.fftAmplitude2dB(fftData[i].magsq(), iChannel, 
//			//					fftDataBlock.getSampleRate(), fftLength, true, true)*25);
//		}
//		}
	}

	/**
	 * Check the configuration of the channel data - array sizes are correct, etc.
	 */
	public void checkConfig() {
		/*
		 *  fundamental changes are things like FFT length which will require
		 *  total recreation of the stored array.  
		 */
		int change = changeLevel();
		//System.out.println("CHANGE LEVEL: " + change + " DataWidth: " +dataWidth);
		if (change > 0) {
			//if (change > 1) {
				rebuildStore();
				createWritableImage();
			//}
		}
	}

	private void rebuildStore() {
		
		this.dataWidth = dataBlock2D.getDataWidth(iChannel);
		this.hopSamples = dataBlock2D.getHopSamples();
		this.timeScale = dataBlock2D.getHopSamples() / dataBlock2D.getSampleRate();
		//get the size of the store. 
		this.historyBins = getPreferredHistoryBins();
		
//		Debug.out.println(String.format("Rebuild FX spectrogram store with %d data pixels", this.historyBins));
//		Debug.out.println(String.format("The store size is %d by %d with a hop of %d", this.historyBins, dataWidth, dataBlock2D.getHopSamples()));

		intPowerSpectrum = new short[this.historyBins][dataWidth];
		scaledLineSpectrum = new double[dataWidth];
		lastPowerSpecBin = -1;
		totalPowSpec=0;
	}

	/**
	 * Create the buffered image. This may be scaled in t by powers of 2 to pre-compress the
	 * image for longer spectrograms.
	 */
	private void createWritableImage() {		
				
		int oldCompression = timeCompression;
		
		if (tdProjector.getTimeAxis()==null) return;
		
		this.timeRange = tdProjector.getVisibleTime()/1000.0;
		double screenPixels =  tdProjector.getGraphTimePixels();
		
		if (screenPixels == 0) return;
		
		int fftPixels = (int) Math.round(timeRange / timeScale);
		timeCompression = 1;
		
//		System.out.println(" timeRange: " +  timeRange + " timeScale: " + timeScale); 
		
		while (fftPixels >= screenPixels * timeCompression * compressionLevel) {
			timeCompression *= compressionLevel;
		}
		this.timePixels = fftPixels / timeCompression;
		
		// see if the image is long enough anyway. 
		if (writableImage == null || oldCompression != timeCompression || writableImage.getWidth() < timePixels
				|| writableImage.getHeight()!=dataWidth) {
			timePixels *= compressionLevel; // allocate twice as much as is needed. 
			if (timePixels<=0) timePixels=1; //20/02/2017 stops an exception if time pixels are 0
			writableImage = new WritableImage((int) timePixels, dataWidth);
			writableRaster = writableImage.getPixelWriter();
			imageXPos = -1;
			totalXDraw=0;
//			System.out.println("NEW Writable image is " + writableImage.getWidth() + " by: " +writableImage.getHeight() + "DataWidth: " + dataWidth);
//			System.out.println("New image: timePixels: "+timePixels+" fftLength/2: "+dataWidth/2+" time Compression: "+timeCompression+" fftPixels: "+
//					fftPixels+ " tdMainDisplay Pixels: "+tdProjector.getGraphTimePixels());
		}
		else {
			this.timePixels = writableImage.getWidth();
		}
	}

	/**
	 * Work out if anything much has changed:
	 * @return 2 if we need to totally start again (e.g. new fft length)
	 * 1, things have changed a bit, but probably no need to reallocate
	 * 0 no change at all. 
	 */
	private int changeLevel() {

		if (intPowerSpectrum == null || intPowerSpectrum.length == 0) {
			return 2;
		}
		// check the FFT length
		if (this.dataWidth != dataBlock2D.getDataWidth(iChannel)) {
//			System.out.println("FFTLength does not equal FFT data block length: Change level 2");
			return 2;
		}

		// check the time scaling
		double tScale = dataBlock2D.getHopSamples() / dataBlock2D.getSampleRate();
		if (this.timeScale != tScale) {
//			System.out.println("Time scale does not equal theoretical time scale: Change level 2");
			return 2;
		}

		int prefHist = getPreferredHistoryBins();
		if (prefHist > historyBins * 2) {
//			System.out.println("Preffered histroy bins greater than 2xhistory bins: Change level 2");
			return 2;
		}

		// TODO - this all needs streamlining quite considerably ! Both fix for time pixels which always seems to be 2 more than preferredPixels;
		double preferredPixels = tdProjector.getGraphTimePixels();
		if (this.timePixels < 0.5*preferredPixels|| timePixels > preferredPixels * 4) {
//			System.out.println("TimePixels less than preferredPixels or greater than 4xprefferred pixels: Change level 2: "+" preferredPixels: "+preferredPixels+ "timePixels: "+timePixels);
			return 2;
		}

		int oldCompression = timeCompression;
		double screenPixels = tdProjector.getGraphTimePixels();
		if (screenPixels == 0 || timeScale == 0) return 0;
		int fftPixels = (int) Math.round(timeRange / timeScale);
		int tc = 1;
		while (fftPixels >= screenPixels * tc * compressionLevel) {
			tc *= compressionLevel;
		}
		if (tc != oldCompression) {
//			System.out.println("tc does not equal old compression: Change level 2 tc: " +tc+ " tc: "+oldCompression +" "+timeRange);
			return 2;
		}

		if (prefHist != historyBins) {
			//this prints a lot so commented out
			//System.out.println("Prefferred history doesn't equal history bins: Change level 1: prefHist: "+prefHist+" historyBins: "+historyBins);
			return 1;
		}
		// check the timeRange
		if (tdProjector.getVisibleTime()/1000.0 != this.timeRange) {
//			System.out.println("Time range does not match main display range: Change level 1");
			return 1;
		}

		return 0;
	}

	/**
	 * Get the preferred number of history bins for the data. This will generally be 
	 * somewhere between a couple of times the length of the display window and 
	 * the maximum memory amount. 
	 * @return preferred store size. 
	 */
	private int getPreferredHistoryBins() {
		double displayLength = tdProjector.getVisibleTime()/1000.0;
		double tScale = dataBlock2D.getHopSamples() / dataBlock2D.getSampleRate();
		
//		Debug.out.println("DisplayLength: "+ displayLength+ "s: tScale: " + tScale +
//				" dataBlock2D.getSampleRate(): " + dataBlock2D.getSampleRate() + " hop: " + dataBlock2D.getHopSamples());

		int prefHist = Math.max(1, (int) (displayLength / tScale * 10.));
		int maxHist = (int) (MAXSTORAGESIZE / Math.max(dataBlock2D.getDataWidth(iChannel),1) / 2); // (now need to divide by 2 for width rather than fft length, then double for int16 on half ffft lenght)
//		Debug.out.println("Power array storage size: "+Math.min(prefHist,  maxHist) + " prefHist: " + prefHist + " maxHist: " + maxHist);
		return Math.min(prefHist,  maxHist);
	}


	int scaledImageIndex = 0;
	
	/**
	 * Draw a line into the image at the current position for the stored power spec
	 * and for the image. 
	 */
	private void drawImageLine(int lastPowerSpecBin) {
		if (writableImage == null) {
			return;
		}
		for (int i = 0; i < dataWidth; i++) {
			scaledLineSpectrum[i] += intPowerSpectrum[lastPowerSpecBin][i]/decibelIntScale;
		}
		//02/05/2018this was  writableImage.getWidth() instead of  writableImage.getHeight()???- major undiscoverred bug which
		//screwed things up for a long long time. 
		int wid = Math.min((int) writableImage.getHeight(), dataWidth);
		//System.out.println("Scrolling2DPlotDataFX: drawImageLineL: Data width: " + wid +  " image size: " + writableImage.getWidth() + " by: " + writableImage.getHeight() + " datawidth: " + dataWidth);
		if (++scaledImageIndex >= timeCompression) {
			if (++imageXPos >= writableImage.getWidth()) {
				//here we start drawing from the start of the writable image again
//				System.out.println("Move to start of writable image: scaledImageIndex"+scaledImageIndex+" timeCompression: "+timeCompression); 
				imageXPos = 0;
			}
			for (int i = 0; i < wid; i++) {
				writableRaster.setColor((int) imageXPos, wid-1-i, 
						specColors.getColours(scaledLineSpectrum[i]/timeCompression));
				scaledLineSpectrum[i] = 0;
			}
			if (!reBuildImage) totalXDraw++;
			scaledImageIndex = 0;
		}
	}
	
	/****Some useful variables used all the time**/
	
	//time axis
	private double tScale; // the time scale in pixels per millisecond. 
//	private long wrapLengthMillis;	//the length of the left wrap section in millis
	private double wrapScreenPix;	//the number of screen pixels the wrap takes up
	private double wrapImagePixdw;	//the length of the wrap section in spectrogram image pixels (not time display pixels)
	private double imagePixdw; // the width of the screen in image pixels. 
	private double imagestart;	//the start of time image 
	private double endScreenPix; 	//the end of the screen in pixels. This s not just timePixels
	private double nImagePixs;//the number of pixels on image in memory, from zero to current location of pointer. 
	//the number of screen pixels corresponding to the current position on the writable image to zero. 
	private double nScreenPix; 	//the number of screen pixels corresponding to the current position on the writable image to zero. 
	private double scrollEndTime; //the current time. 
	//location to start drawing image on screen from. 
	private double screenStartPix;  
	//freq axis
	private double freqPixels;
	private double imageFP1, imageFP2;
	private double freqWidth;

	
	public double drawSpectrogramWrap(GraphicsContext g2d, double timePixels, double freqPixels, PamAxisFX timeAxis,
			double scrollStart, double scrollEndTime, double imageFP1, double imageFP2, double[] freqBinRange, double freqWidth){
		

		/**
		 * When we have wrapped display we need to take a chunk of the image and place in one
		 * wrap section (i.e the new data from zero) and then place the remainig part of the image in the wrap 
		 * section with old data. 
		 * 
		 * First try to get a section 
		 */
		 double tScale = timePixels / (timeAxis.getMaxVal()-timeAxis.getMinVal()) / 1000.;

		//the number of screen pixels the wrap takes up
		 wrapScreenPix=tdProjector.getTimePix(0)+Math.max(timeAxis.getPosition(-tdProjector.getVisibleTime()/1000.), (lastPowerSpecTime - scrollEndTime) * tScale);;

		//the length of the wrap section in spectrogram image pixels (not time display pixels)
		 wrapImagePixdw=wrapScreenPix/(timeScale * 1000. * tScale * timeCompression); 

		 imagePixdw= timePixels/(timeScale * 1000. * tScale * timeCompression);

		//the start of time image 
		 imagestart=imageXPos+1-wrapImagePixdw;

		//location to start drawing image on screne from. 
		 screenStartPix=0; 

		if (imagestart<-1){
			//gotta shift the start of the screen a bit if there is not enough image 
			screenStartPix=(Math.abs(imagestart))*timeScale * 1000. * tScale * timeCompression; 
		}

		//			System.out.println("imageXPos: "+imageXPos+"wrapLengthMillis: "+wrapLengthMillis+ " wrapScreenPix "+wrapScreenPix+
		//					" wrapScreenPixdw: "+wrapScreenPix+ " wrapImagePixdw: "+wrapImagePixdw+ " imagestart: "+
		//					imagestart + "nScreenPix: "+nScreenPix);

		/**
		 * Lift as much as possible into first section
		 */
		g2d.drawImage(writableImage, Math.max(0,imagestart), freqBinRange[1], wrapImagePixdw, freqWidth,
				screenStartPix, imageFP1, wrapScreenPix, imageFP2);

		if (imagestart<-1){
			/**
			 * If this occurs, the image is negative, i.e. we have to go back and fill a bit in from the end of the image in the wrap section. 
			 */				
			g2d.drawImage(writableImage, writableImage.getWidth()+imagestart, freqBinRange[1], Math.abs(imagestart), freqWidth,
					0, imageFP1, screenStartPix, imageFP2);

		}

		//System.out.println(String.format(" totalXDraw %d wrapImagePixdw %.2f", totalXDraw,wrapImagePixdw ));
		if (totalXDraw>imagePixdw){

			imagestart=imageXPos+1-imagePixdw;

			screenStartPix= wrapScreenPix;
			imagePixdw=imagePixdw-wrapImagePixdw;

			if (imagestart<-1){
				/**
				 * Cannot fill the entire area in one go without going into negative parts of the image. 
				 */
				screenStartPix=wrapScreenPix+(Math.abs(imagestart))*timeScale * 1000. * tScale * timeCompression; 
				imagePixdw=imagePixdw+imagestart; 
			}

			//System.out.println(String.format(" imagestart %.3f imagePixdw %.3f  screenstart %.3f", imagestart,  imagePixdw,(timePixels-(endScreenPix-timePixels)+wrapScreenPix)));

			/**
			 * Now draw the data after the wrap line. i.e. the older part of the image
			 */
			g2d.drawImage(writableImage,  Math.max(0,imagestart), freqBinRange[1], imagePixdw, freqWidth,
					screenStartPix, imageFP1, timePixels-screenStartPix, imageFP2);

			if (imagestart<-1){

				/**
				 * Fill the remaining section with the negative parts of the image 
				 */
				g2d.drawImage(writableImage, writableImage.getWidth()+imagestart, freqBinRange[1], Math.abs(imagestart), freqWidth,
						wrapScreenPix, imageFP1, screenStartPix-wrapScreenPix, imageFP2);
			}
		
		}
		
		drawWrapLine( g2d,  wrapScreenPix,  freqPixels);
		
		return wrapScreenPix; 
		
	}
	
	public synchronized void drawSpectrogramScroll(GraphicsContext g2d,  double timePixels, double freqPixels, PamAxisFX timeAxis,
			double scrollStart, double scrollEndTime, double imageFP1, double imageFP2, double[] freqBinRange, double freqWidth){
		

		//the time scale in pixels per millisecond
		tScale = timePixels / (timeAxis.getMaxVal()-timeAxis.getMinVal()) / 1000.;
		//the end of the screen in pixels. This s not just timePixels
		endScreenPix = timePixels+Math.max(timeAxis.getPosition(-tdProjector.getVisibleTime()/1000.), (lastPowerSpecTime - scrollEndTime) * tScale); //FIXME- add this section on appears to cause issues in swing but not JavaFX?...
		//the number of pixels on image in memory, from zero to current location of pointer. 
		nImagePixs = imageXPos+1;
		//the number of screen pixels corresponding to the current position on the writable image to zero. 
		nScreenPix = nImagePixs * timeScale * 1000. * tScale * timeCompression;
		
//		System.out.println("Writable Image: "+ writableImage.getWidth() + " "+ writableImage.getHeight() +  " freqBinRange[1] "+ freqBinRange[1] + " imageXPos+1 "+ imageXPos+1 + 
//				" freqWidth: "+ freqWidth + " screenStartPix "+ (screenStartPix) + " imageFP1: " +imageFP1 +
//				" nScreenPix: " + nScreenPix +  " imageFP2: " +imageFP2);
		
		/**
		 * Here the writable image is displayed on the right hand of the screen up to
		 * it's latest draw point. The image has been stored in memory and drawn up some
		 * pixel defined by imageXPos, say pixel 100. As an example, if pixel 100 the
		 * image would be displayed up to pixel 100 on the right hand side of the
		 * screen.
		 */
		g2d.drawImage(writableImage, 0.0, freqBinRange[1],  imageXPos+1, freqWidth,
				endScreenPix-nScreenPix, imageFP1, nScreenPix, imageFP2);
				
		/**
		 * Now the rest of the buffered image must be painted onto the display. This is
		 * the older data. Following from the example above if the last pixel to be
		 * painted from the writable image in in memory was pixel 100, then from pixel
		 * 101 to the end of the image is older data. This may be blank but if the
		 * spectrogram has been running for a while then is likely to contain older
		 * data. pixel 101 will be the oldest data, followed by pixel 102 as second
		 * oldest and so on. Here the older data in the image is painted on the left
		 * side of the screen. All calculations correct the end of the older data should
		 * be the start of the new data.
		 */
		nScreenPix = endScreenPix-nScreenPix;
		if (nScreenPix < 0) {
			//reset transform in case of other drawings on base. 
			return;
		}

		nImagePixs = nScreenPix /(timeScale * 1000. * tScale * timeCompression);
		double imageStartPix =  writableImage.getWidth()-nImagePixs;//Math.max(imageXPos+1, writableImage.getWidth()-nImagePixs);
		double rectStart=0;

		//System.out.println("nScreenPix: "+nScreenPix+ " imageStartPix: "+imageStartPix+ " nImagePixs "+nImagePixs+"writableImage.getWidth() "+writableImage.getWidth());

		/**
		 * In case of scroll bar being used to move spectrogram must check three cases.
		 * 1) Scrolled right past the spectrogram image 2) Have reached the end of the
		 * image but still part of the image is on screen 3) The spectrogram wasn't
		 * going long enough to fill the image in memory but have scrolled past the
		 * written part of the image and end of image. (this result in strange sections
		 * of the spectrogram showing where it shouldn't). None of these checks are
		 * required when PAMGUARD is running, only when paused/in viewer mode.
		 */
		if ((imageStartPix<0 && Math.abs(imageStartPix)>nImagePixs)){
			/**
			 * In this case scrolled totally past the saved spectrogram image. No need to draw anything
			 */
//			System.out.println("Scrolled right past spectrogram with no image on screen: Clear image: ");
			double clearPixels=Math.max(timePixels, freqPixels); //to be safe just use max (to avoid if vertical.horizontal chaeck)
			g2d.clearRect(0, 0, clearPixels, clearPixels);
			return;
		}
		else if (totalXDraw<writableImage.getWidth() && imageStartPix<totalXDraw ){
			/**
			 * In this case scrolled past end of image but part of image is still on screen
			 * and on top of this the spectrogram image hasn't been filled entirely yet.
			 * Need to be careful in this situation bits of the image being drawn where
			 * there should be a blank area.
			 */
//			System.out.println("Spectrogram image not filled and scrolled past end: "+imageStartPix+ " "+(writableImage.getWidth()+1-imageStartPix));
			rectStart=(totalXDraw-imageStartPix)*(timeScale * 1000. * tScale * timeCompression); ;
			imageStartPix=totalXDraw+1;
			//09/01/2017 added this because the spectrogram was stretching weird after being rebuilt. Seems to work. 
			nScreenPix=nScreenPix-rectStart;
		}
		else if (imageStartPix<0){
			/**
			 * In this case scrolled past end of image but part of image is still on screen. 
			 * want to start drawing on screen in location which is not zero (mod of imageStartPix)
			 */
			rectStart=Math.abs(imageStartPix)*(timeScale * 1000. * tScale * timeCompression); 
//			System.out.println("Scrolled past end of spectrogram: "+imageStartPix+ " "+(writableImage.getWidth()+1-imageStartPix)+" rectStart: "+rectStart);
			imageStartPix=0;
			// need to have this to reduce scale of rectangle so it shows properly if the
			// spectrogram does not fill screen.
			nScreenPix=nScreenPix-rectStart;
		}

//				System.out.println("nScreenPix left: "+nScreenPix+" rect start: " + rectStart +  " writbale img width: "
//						+ writableImage.getWidth()+" imageXPos"+imageXPos+ " tScale: "+tScale);
		g2d.drawImage(writableImage, imageStartPix, freqBinRange[1],writableImage.getWidth()+1-imageStartPix, freqWidth,
				rectStart, imageFP1, nScreenPix+1, imageFP2);	
	}



	/**
	 * Quick reference to transform for horizontal spectrogram //default. 
	 */
	private static Affine horzAffine=new Affine();
	public void drawSpectrogram(GraphicsContext g2d, Rectangle windowRect, Orientation orientation, PamAxisFX timeAxis,
			double scrollStart, boolean wrap) {

		if (writableImage == null || totalXDraw==0) {
			return;
		}
		
		
		g2d.setImageSmoothing(false);
		
		//System.out.println("Paint start at " + PamCalendar.formatTime(scrollStart)+"  "+scrollStart);
		// will need to work out from the time how on earth we're going to draw this thing in two parts
		/*
		 * We have the scroll start, scale and width of the window we're drawing into
		 * We have the length, scale and current position of the image we're drawing into it. 
		 * 
		 *   First is to draw the right hand side of the spectrogram which goes from the 
		 *   current position in the buffered image to the beginning of the buffered image. Then 
		 *   if the left pixel is not < 0 on the screen, draw the right side of the BI into the 
		 *   left of the screen. 
		 *   
		 *   ??? Don't Do the whole thing in two parts. One copy data into the screen image which is buffered image
		 *   exactly the right size to fit in the display screen. 
		 *   
		 *   tScale is in pixels per millisecond. 
		 *   timeScale is in seconds per fft slice. 
		 */

		if (orientation == Orientation.VERTICAL) {
			timePixels = windowRect.getHeight();
			freqPixels = windowRect.getWidth();

			imageFP2 = -freqPixels; //make negative to flip the image
			imageFP1 = 0;

			//create transform; 
			Affine at = new Affine(); 
			//rotate by 90 degrees. JavaFX seems to use degrees instead of radians. Not right.
			Rotate rot=Transform.rotate(-90,  windowRect.getWidth()/2., windowRect.getHeight()/2.);  
			at.append(rot);

			//translate to compensate for ratio change and, since the image is flipped, need to move it back. 
			Translate translate=Transform.translate((windowRect.getWidth()-windowRect.getHeight())/2., (windowRect.getWidth()+windowRect.getHeight())/2.); 
			at.append(translate);

			//transform graphics context.
			g2d.setTransform(at);
			g2d.fillRect(0, 0, freqPixels, timePixels);

			//	        System.out.println("Spec Affine Transform: "+at.toString()); 
		}
		else {
			//return any transform back to normal. 
			g2d.setTransform(horzAffine);

			timePixels = windowRect.getWidth();
			freqPixels = windowRect.getHeight();
			imageFP2 = freqPixels;
			imageFP1 = 0;
		}

		//System.out.println("timeAxis.getMaxVal(): "+timeAxis.getMaxVal()+" timeAxis.getMinVal() "+timeAxis.getMinVal()+"Max-min: "+(timeAxis.getMaxVal()-timeAxis.getMinVal()));
		scrollEndTime = scrollStart + ((timeAxis.getMaxVal()-timeAxis.getMinVal())*1000.);
		//the time scale in pixels per millisecond
		tScale = timePixels / (timeAxis.getMaxVal()-timeAxis.getMinVal()) / 1000.;
		//the number of pixels on image in memory, from zero to current location of pointer. 
		nImagePixs = imageXPos+1;

		//y axis
		
		//convert to double array...,akes life easier. 
//		double[] freqBinRange= {this.verticalBinRange[0].get() , this.verticalBinRange[1].get()};
		double[] freqBinRange = {0, dataWidth}; // default values fo rbeamogram. 
		if (specPlotInfo != null) {
			double min = specPlotInfo.getDataAxisMinVal();
			double max = specPlotInfo.getDataAxisMaxVal();
			freqBinRange[0] = valueToBin(min);
			freqBinRange[1] = valueToBin(max);
		}
//		double[] freqBinRange = {valueToBin(specPlotInfo.yScaleRange[0].doubleValue()), 
//				valueToBin(specPlotInfo.yScaleRange[1].doubleValue())};
		
		for (int i = 0; i < 2; i++) {
			freqBinRange[i] = Math.max(0, Math.min(freqBinRange[i], dataWidth));
		}
//		freqBinRange[1] = 180/3;
		

		freqWidth= Math.min(freqBinRange[0]-freqBinRange[1]+1, dataWidth);
		
		//System.out.println("Scrolling2Dplot2: Freq width: "  + freqBinRange[0] + " " + freqBinRange[1] + " freqwidth: " + freqWidth);
		
		if (wrap){
			 drawSpectrogramWrap( g2d,  timePixels, freqPixels ,  timeAxis,
					 scrollStart,  scrollEndTime, imageFP1,  imageFP2, freqBinRange,  freqWidth);
		}
		else{			
			drawSpectrogramScroll( g2d, timePixels, freqPixels,  timeAxis,
				 scrollStart,  scrollEndTime, imageFP1,  imageFP2, freqBinRange,  freqWidth);
		}

		//reset transform in case of other drawings on base canvas. 
		if (orientation == Orientation.VERTICAL) g2d.setTransform(horzAffine);

	}
	
	/**
	 * Draw the wrap line. 
	 * @param g2d
	 * @param wrapScreenPix
	 * @param freqPixels
	 */
	 public void drawWrapLine(GraphicsContext g2d, double wrapScreenPix, double freqPixels){
		 g2d.setStroke(this.specColors.getWrapColor());
		 g2d.setLineWidth(1);
		 g2d.strokeLine(wrapScreenPix, 0 , wrapScreenPix, freqPixels);
	 }


	/**
	 * Call to re-colour image if colour map or amplitude scale changes. 
	 */
	ReBuildImageTask task; 
	Thread thread;
	long currentTime=0;
	public void reBuildImage(){
		//just a quick check to make sure not calling at almost exactly the same time.
		if ((System.currentTimeMillis()-currentTime)<100) return;
		currentTime=System.currentTimeMillis();
		if (thread!=null){ 
			task.cancel();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				rebuildStore();
				createWritableImage();
			}
		}
		
		//create a new task 
		task=new ReBuildImageTask();
		
		task.setOnSucceeded((value)->{
			//need to make sure the spectrogram repaints properly at tend of task or can be left with white chunks. 
			 rebuildFinished();
		});
		
		thread=new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Call to re build the image, for example if colour map or amplitude scale changes. 
	 * Note: Do not run a seperate thread if the image is running in real time mode. 
	 * Can be used on thread if image is paused or in viewer mode. During rebuilding in real time mode some data may be lost
	 * @param if on a separate thread input which task this function is a part of. Means the rebuild can be cancelled. Can null
	 * @return true if image was completely rebuilt. 
	 */
	protected boolean reBuildImage(Task<Boolean> task){

//		System.out.println("Rebuild the Image!!!");
		if (writableImage==null) return false; 
		
		reBuildImage=true;
		
		//indicates whether all pixels have been recoloured 
		boolean success=true; 

		//must keep a record of old positions just in case thread is interrupted 
		long oldtotalXDraw=totalXDraw; 
		int oldlastPowerSpecBin=lastPowerSpecBin;
		int oldscaledImageIndex=scaledImageIndex;		
		
		
//		System.out.println("lastPowerSpecBin0: " + lastPowerSpecBin + " imageXPos "+ imageXPos+ 
//				" totalXDraw "+totalXDraw + " totalPowSpec " + totalPowSpec+  " intPowerSpectrum.length "+intPowerSpectrum.length);

		//Only want to set imageXPos and store pos back to zero if the entire image in memory has not been written yet.
		//if (totalXDraw<=imageXPos+1) {
			//return image to zeros. 
			imageXPos=0; 
		//}
		
		if (totalPowSpec<=lastPowerSpecBin+1){
			lastPowerSpecBin =  0; 
			scaledImageIndex = 0;
		}

		//don't want to draw empty space if array has not filled so check total array fills. 
		int toDraw=(int) Math.min(totalPowSpec, intPowerSpectrum.length);
		
		
		for (int i=0; i<toDraw-1; i++){
			//stop task if necessary; 
			if (task!=null && task.isCancelled()){
				success=false;
				//note this should only ever occur when the spectrogram is paused or in viewer mode. 
				totalXDraw=oldtotalXDraw;
				//imageXPos=oldimageXPos;
				lastPowerSpecBin=oldlastPowerSpecBin;
				scaledImageIndex=oldscaledImageIndex;
				break; 
			}
			//note: important to have historyBins-1 or pixels move on every repaint.
			if (++lastPowerSpecBin >= historyBins-1) {
				lastPowerSpecBin = 0;
			}
			drawImageLine(lastPowerSpecBin); 
			
		}
		
		
		//Need to remove all data from writable image first as we are using the power spectra in memory to rewrite the image. There
		//may be data on the image whihc is no longer in memory. 
		if (task!=null && task.isCancelled()){
			for (double x=imageXPos+1; x<writableImage.getWidth(); x++){
				for (int y=0; y<writableImage.getHeight(); y++){
					writableImage.getPixelWriter().setColor((int) x, y, Color.TRANSPARENT);
				}
			}
		}
		//System.out.println(" imageXPos: " +imageXPos + " lastPowerSpecBin: "+lastPowerSpecBin)
		
		
		//13/12/2016 - bug found whihc caused wierd drawing. Need to return values back to original state. 
		totalXDraw=(long) imageXPos;
//		imageXPos=oldimageXPos;
		lastPowerSpecBin=oldlastPowerSpecBin;
		scaledImageIndex=oldscaledImageIndex;
		
		//System.out.println("After: totalXDraw "+totalXDraw+ " imageXPos"+imageXPos+" lastPowerSpecBin "
		//+lastPowerSpecBin+" scaledImageIndex: "+scaledImageIndex + " totalPowSpec "+totalPowSpec+ " iChannel: "+iChannel);

		reBuildImage=false;

		//now if in real time mode data may have been recieved during rebuilding of image. 
		//in this case we need to draw in the new data and hope that no more arrives. 
		while (rebuildAdd>0){
			
			//note: important to have historyBins-1 or pixels move on every repaint.
			if (++lastPowerSpecBin >= historyBins-1) {
				lastPowerSpecBin = 0;
			}
			++totalPowSpec; //must update this here as we're filling in new data essentailly. 
			drawImageLine(lastPowerSpecBin); 
			rebuildAdd--;
		}

		//System.out.println("rebuildAdd: "+count+" "+rebuildAdd);
		//System.out.println("After Rebuild: totalXDraw "+totalXDraw+ " imageXPos"+imageXPos+" lastPowerSpecBin "
		//+lastPowerSpecBin+" scaledImageIndex: "+scaledImageIndex  + " totalPowSpec "+totalPowSpec+ " iChannel: "+iChannel);

		//if on another thread call a repaint on javafx thread. 
		if (success && task!=null){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					//tdMainDisplay.repaintAll();
					 rebuildFinished();
				}
			});
		}
		else  rebuildFinished(); 

		return success; 
	}


	/**
	 * Recolours the spectrogram image. On a different thread as it takes a bit of computational time.
	 * @author Jamie Macaulay
	 *
	 */
	private class ReBuildImageTask extends Task<Boolean> {

		@Override
		protected Boolean call() throws Exception {
			if (this.isCancelled()) return false; 
			return reBuildImage(this);
		}	
		
		 @Override protected void succeeded() {
             super.succeeded();
         }

	}

	/**
	 * Reset anything necessary  before new data are loaded offline in 
	 * viewer mode. 
	 */
	public void resetForLoad(){
		//Debug.out.println("Reset for spec load: "); 
		resetForLoad(false); 
	} 

	/**
	 * Reset anything necessary  before new data are loaded offline in 
	 * viewer mode. 
	 * @ newWritableImage - if the FFT or hop size has changed this should be true. 
	 */
	public void resetForLoad(boolean newWritableImage) {
		//TODO- need to implement this function properly in viewer
		imageXPos = 0;
		this.totalXDraw=0;
		this.totalPowSpec=0;
		rebuildStore();
		if (writableImage != null && !newWritableImage){
			clearWritableImage(writableImage);
		}
		if (newWritableImage) {
			createWritableImage();
		}
	}

	/**
	 * Clear the writable image.
	 */
	private void clearWritableImage(WritableImage writableImage){
		for (int i=0; i<writableImage.getWidth(); i++){
			for (int j=0; j<writableImage.getHeight(); j++){
				writableImage.getPixelWriter().setColor(i,j,Color.TRANSPARENT); 
			}
		}
	}

	/**
	 * Get the current FFT hop size in bins (samples) for this spectrogram plot. 
	 * @return FFT hop size in bins (samples)
	 */
	public int getFftHop() {
		return hopSamples;
	}
	
	
	/*
	 *Called whenever rebuild is finished.  
	 */
	public void rebuildFinished(){
		
	}
	
	/**
	 * Convert a data value (e.g. an angle or a frequency) into a bin number
	 * @param value data value
	 * @return bin number
	 */
	double valueToBin(double value) {
		double val = (double) dataWidth * (value-dataBlock2D.getMinDataValue()) / (dataBlock2D.getMaxDataValue()-dataBlock2D.getMinDataValue());
		return dataWidth-val;
	}
	
	/**
	 * Convert a bin number back into a real data value (e.g. an angle or a frequency)
	 * @param bin bin number
	 * @return data value
	 */
	double binToValue(double bin) {
		bin = dataWidth - bin;
		return bin / (double) dataWidth * (dataBlock2D.getMaxDataValue()-dataBlock2D.getMinDataValue()) + dataBlock2D.getMinDataValue();
	}
	
	/**
	 * Checks the frequency range range to be displayed. 
	 * 
	 */
	@Deprecated
	public void setFreqLimits(DoubleProperty[] frequencyLimits){
//		float sampleRate = this.dataBlock2D.getSampleRate();
//		verticalBinRange  [0].setValue( Math
//				.floor(frequencyLimits[0].get()
//						* this.dataBlock2D.getDataWidth(iChannel) / dataBlock2D.getMaxDataValue()));
//		verticalBinRange[1].setValue (Math
//				.ceil(frequencyLimits[1].get()
//						* this.dataBlock2D.getDataWidth(iChannel) / dataBlock2D.getMaxDataValue()));
//			
//		for (int i = 0; i < 2; i++) {
//			verticalBinRange[i] .setValue( this.dataBlock2D.getDataWidth(iChannel) - verticalBinRange[i].get());
//			verticalBinRange[i] .setValue(Math.min(Math.max(verticalBinRange[i].get(), 0),
//					this.dataBlock2D.getDataWidth(iChannel) - 1));
//		}
//		//System.out.println(String.format("Freq bins range: "+freqBinRange[0]+" "+ freqBinRange[1]));
	}
	

	/**
	 * Set the colours for the spectrogram
	 * @param specColors - spectrogram colours. 
	 */
	public void setSpecColors(Plot2DColours specColors) {
		this.specColors = specColors;
	}
	
	/**
	 * Get the colours for the spectrogram
	 * @param specColors - spectrogram colours. 
	 */
	public Plot2DColours getSpecColors( ) {
		return specColors;
	}

	
	/**
	 * Get the image in memory of the spectrogram
	 * @return the current image whihc has spectrogram drawn on it. 
	 */
	public Image getWritableImage() {
		return this.writableImage;
	}

	/**
	 * Get the total number of 2D data units added to the plot
	 * @return the total number of 2D plot lines. 
	 */
	public long getTotalPowerSpec() {
		// TODO Auto-generated method stub
		return this.totalPowSpec;
	}



}
