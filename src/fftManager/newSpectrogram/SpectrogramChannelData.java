package fftManager.newSpectrogram;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import pamScrollSystem.PamScroller;
import Layout.PamAxis;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import Spectrogram.SpectrogramParameters;
import Acquisition.AcquisitionProcess;
import dataPlots.TDControl;
import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * contains the spectrgram data for a single channel. 
 * Will only exist and be populated if that channel is 
 * included in the spectrogram display. 
 * @author Doug Gillespie
 *
 */
public class SpectrogramChannelData {
	
	/**
	 * The channel/sequence number being displayed in this plot
	 */
	private int iChannel;
	
	private SpectrogramPlotInfo specPlotInfo;

	private FFTDataBlock fftDataBlock;
	
	private TDControl tdControl;
	
	private int fftLength, fftHop;
	
	/**
	 * Integer power spectrum levels in dB, scaled
	 * to give decent resolution. 
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
	
	private double decibelIntScale = 25; // allows up to 32676/25 = 1300 dB with .04dB resolution
		
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
	private int timePixels;
	private BufferedImage bufferedImage;
	
	private int imageXPos;
	private WritableRaster writableRaster;
	
	private static final long MAXSTORAGESIZE = 20 * 1024 * 1024; // max storage in megabytes. will allow about 3 mins of normal data. 
	
	public SpectrogramChannelData(SpectrogramPlotInfo specPlotInfo, int iChannel) {
		super();
		this.specPlotInfo = specPlotInfo;
		this.iChannel = iChannel;
		fftDataBlock = specPlotInfo.getFftDataBlock();
		this.tdControl = specPlotInfo.getTdControl();
	}
	
	/**
	 * New fft data arrived for this channel. 
	 * @param fftDataUnit fft data unit. 
	 */
	public void newFFTData(FFTDataUnit fftDataUnit) {
		checkConfig();
//		if (true) return;
		fillPowerSpecLine(fftDataUnit);
		drawImageLine();
	}
	
	int scaledImageIndex = 0;
	/**
	 * Draw a line into the image at the current position for the stored power spec
	 * and for the image. 
	 */
	private void drawImageLine() {
		if (bufferedImage == null) {
			return;
		}
		for (int i = 0; i < fftLength/2; i++) {
			scaledLineSpectrum[i] += intPowerSpectrum[lastPowerSpecBin][i]/decibelIntScale;
		}
		if (++scaledImageIndex >= timeCompression) {
			if (++imageXPos >= timePixels) {
				imageXPos = 0;
			}
			for (int i = 0; i < fftLength/2; i++) {
				writableRaster.setPixel(imageXPos, fftLength/2-1-i, 
						specPlotInfo.getColours(scaledLineSpectrum[i]/timeCompression));
				scaledLineSpectrum[i] = 0;
			}
			scaledImageIndex = 0;
		}
	}

	/**
	 * Fill a line in the integer array that stores historical data. 
	 * @param fftDataUnit
	 */
	private void fillPowerSpecLine(FFTDataUnit fftDataUnit) {
		if (++lastPowerSpecBin >= historyBins) {
			lastPowerSpecBin = 0;
		}
		lastPowerSpecTime = fftDataUnit.getTimeMilliseconds();
		ComplexArray fftData = fftDataUnit.getFftData();
		AcquisitionProcess daqProcess = null;
		
		// if the fftDataBlock is using channel numbers, then we are fine to get the amplitude information.  But if it
		// is using sequence numbers, take the lowest channel in the channel map and use that one to get amplitude info
		int chanToUse = fftDataBlock.getARealChannel(iChannel);
		
		// get the acquisition process. 
		try {
			daqProcess = (AcquisitionProcess) (fftDataBlock.getSourceProcess());
			daqProcess.prepareFastAmplitudeCalculation(chanToUse);
		}
		catch (ClassCastException e) {
			return;
		}

		for (int i = 0; i < fftLength/2; i++) {
			intPowerSpectrum[lastPowerSpecBin][i] = (short) (daqProcess.fftAmplitude2dB(fftData.magsq(i), chanToUse, 
					fftDataBlock.getSampleRate(), fftLength, true, true)*25);
		}
	}

	/**
	 * check the configuration of the channel data - array sizes are correct, etc.
	 */
	public void checkConfig() {
		/*
		 *  fundamental changes are things like FFT length which will require
		 *  total recreation of the stored array.  
		 */
		int change = changeLevel();

		if (change > 0) {
			this.timeRange = tdControl.getTimeRangeSpinner().getSpinnerValue();
			if (change > 1) {
				rebuildStore();
				createBufferedImage();
			}
		}
		
	}
	
	private void rebuildStore() {
		this.fftLength = fftDataBlock.getFftLength();
		this.fftHop = fftDataBlock.getFftHop();
		this.timeScale = fftDataBlock.getFftHop() / fftDataBlock.getSampleRate();
		this.historyBins = getPreferredHistoryBins();
		System.out.println(String.format("Rebuild Swing spectrogram store with %d data pixels", this.historyBins));
		intPowerSpectrum = new short[this.historyBins][fftLength/2];
		scaledLineSpectrum = new double[fftLength/2];
		lastPowerSpecBin = -1;
	}
	
	/**
	 * Create the buffered image. This may be scaled in t by powers of 2 to pre-compress the
	 * image for longer spectrograms.
	 */
	private void createBufferedImage() {
		int oldCompression = timeCompression;
		int screenPixels = tdControl.getGraphTimePixels();
		if (screenPixels == 0) return;
		int fftPixels = (int) Math.round(timeRange / timeScale);
		timeCompression = 1;
		while (fftPixels >= screenPixels * timeCompression * 2) {
			timeCompression *= 2;
		}
		this.timePixels = fftPixels / timeCompression;
		// see if the image is long enough anyway. 
		if (bufferedImage == null || oldCompression != timeCompression || bufferedImage.getWidth() < timePixels) {
			timePixels *= 2; // allocate twice as much as is needed. 
			bufferedImage = new BufferedImage(timePixels, fftLength/2, BufferedImage.TYPE_INT_RGB);
			writableRaster = bufferedImage.getRaster();
			imageXPos = -1;
			System.out.println(String.format("New image %dx%d, compressx%d for screen width %d/%d", 
					timePixels, fftLength/2, timeCompression, fftPixels,tdControl.getGraphTimePixels()));
		}
		else {
			this.timePixels = bufferedImage.getWidth();
			
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
		if (this.fftLength != fftDataBlock.getFftLength()) {
			return 2;
		}
		
		// check the time scaling
		double tScale = fftDataBlock.getFftHop() / fftDataBlock.getSampleRate();
		if (this.timeScale != tScale) {
			return 2;
		}

		int prefHist = getPreferredHistoryBins();
		if (prefHist > historyBins * 2) {
			return 2;
		}
		
		// this all needs streamlining quite considerably !
		int preferredPixels = tdControl.getGraphTimePixels();
		if (this.timePixels < preferredPixels || timePixels > preferredPixels * 4) {
			return 2;
		}

		int oldCompression = timeCompression;
		int screenPixels = tdControl.getGraphTimePixels();
		if (screenPixels == 0 || timeScale == 0) return 0;
		int fftPixels = (int) Math.round(timeRange / timeScale);
		int tc = 1;
		while (fftPixels >= screenPixels * tc * 2) {
			tc *= 2;
		}
		if (tc != oldCompression) {
			return 2;
		}

		if (prefHist != historyBins) {
			return 1;
		}
		// check the timeRange
		if (tdControl.getTimeRangeSpinner().getSpinnerValue() != this.timeRange) {
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
		double displayLength = tdControl.getTimeRangeSpinner().getSpinnerValue();
		double tScale = fftDataBlock.getFftHop() / fftDataBlock.getSampleRate();
		int prefHist = (int) (displayLength / tScale * 10);
		int maxHist = (int) (MAXSTORAGESIZE / fftDataBlock.getFftLength()); // (no need to divide by 2, then double for int16 on half ffft lenght)
		return Math.min(prefHist,  maxHist);
	}

	public void drawSpectrogram(Graphics g, Rectangle windowRect, int orientation, PamAxis timeAxis,
			long scrollStart) {
		if (bufferedImage == null) {
			return;
		}
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
		 *   ??? Don't Do the whole thing in two parts. One copy data into the screen image whic is buffered image
		 *   exactly the right size to fit in the display screen. 
		 *   
		 *   tScale is in pixels per millisecond. 
		 *   timeScale is in seconds per fft slice. 
		 */
		Graphics2D g2d = (Graphics2D) g.create();
		int timePixels, freqPixels;
		int imageFP1, imageFP2;
		if (orientation == PamScroller.VERTICAL) {
			timePixels = windowRect.height;
			freqPixels = windowRect.width;
			AffineTransform at = AffineTransform.getScaleInstance(-1, 1);;
	        at.setToRotation(-Math.PI/2., windowRect.width, windowRect.height);
	        at.translate(windowRect.width, -windowRect.width+windowRect.height);
	        
	        g2d.setTransform(at);

	        imageFP1 = freqPixels;
	        imageFP2 = 0;
		}
		else {
			timePixels = windowRect.width;
			freqPixels = windowRect.height;
	        imageFP2 = freqPixels;
	        imageFP1 = 0;
		}
		
		long scrollEndTime = scrollStart + (long) ((timeAxis.getMaxVal()-timeAxis.getMinVal())*1000.);
		double tScale = timePixels / (timeAxis.getMaxVal()-timeAxis.getMinVal()) / 1000;
		int endScreenPix = timePixels + Math.max(-10000, (int) ((lastPowerSpecTime - scrollEndTime) * tScale));
		int nImagePixs = imageXPos+1;
		int nScreenPix = (int) (nImagePixs * timeScale * 1000 * tScale * timeCompression);
		int[] freqBinRange = specPlotInfo.freqBinRange;
//		if (orientation == PamScroller.VERTICAL) {
//			int dum = freqBinRange[0];
//			freqBinRange[0] = freqBinRange[1];
//			freqBinRange[1] = dum;
//		}
		
		g2d.drawImage(bufferedImage, endScreenPix-nScreenPix, imageFP1, endScreenPix, imageFP2, 0, freqBinRange[1],  
				imageXPos+1, freqBinRange[0]+1, null);
		// now work out how much more of the image needs to be drawn to fill the rest of the window. 
		nScreenPix = endScreenPix-nScreenPix;
		if (nScreenPix < 0) {
			g2d.dispose();
			return;
		}
		nImagePixs = (int) (nScreenPix /(timeScale * 1000 * tScale * timeCompression));
		int imageStartPix = Math.max(imageXPos+1, bufferedImage.getWidth()-nImagePixs);
		g2d.drawImage(bufferedImage, 0, imageFP1, nScreenPix+1, imageFP2, imageStartPix, freqBinRange[1], 
				bufferedImage.getWidth()+1, freqBinRange[0]+1, null);
       
	}

	/**
	 * Rest anything necessary  before new data are loaded offline in 
	 * viewer mode. 
	 */
	public void resetForLoad() {
		imageXPos = 0;
		checkConfig();
		if (bufferedImage != null) {
			bufferedImage.getGraphics().clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		}
	}
	
}
