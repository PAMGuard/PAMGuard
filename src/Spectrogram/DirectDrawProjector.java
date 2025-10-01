package Spectrogram;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
//import gov.nasa.worldwind.render.ScreenCreditController;

/**
 * Spectrgoram projector for drawing directly onto the spectrogram display. 
 * <p>Unlike the SpectroramProjector, which maps pixels 1:1, this projector needs
 * the scales of the display since it draws directly onto the AWT component. 
 * @author Doug Gillespie
 * @see SpectrogramProjector
 *
 */
public class DirectDrawProjector extends GeneralProjector<Coordinate3d> {

	/**
	 * The start of the display. In viewer mode, this will be quite
	 * straight forward, and be the current time from the scroller. 
	 * In real time, it will be the current time - the display length. 
	 * Setting this is handled in the spectrogram display. 
	 */
	private double xStartMillis;

	private double xPixsPerMillisecond;

	//	private long fftStartBin;
	//	
	//	private double xPixsPerFFTBin;

	private double[] frequencyRange;

	private double yPixsPerHz;

	private SpectrogramDisplay spectrogramDisplay; 

	private int panelId;

	private int displayHeight, displayWidth;

	/**
	 * This is the current cursor position, which will be 0 in 
	 * viewer mode, but will scroll forwards in real time ops. 
	 */
	private double xStartPix = 0;

	private double timeRangeMillis;

	private boolean isViewer;
	
	private boolean isLogScale;

	public DirectDrawProjector(SpectrogramDisplay spectrogramDisplay, int panelId) {

		this.spectrogramDisplay = spectrogramDisplay;

		this.panelId = panelId;

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		setParmeterType(0, GeneralProjector.ParameterType.TIME);

		setParmeterType(1, GeneralProjector.ParameterType.FREQUENCY);
	}
	protected void setScales(int displayWidth, int displayHeight, double startMillis, 
			double timeRangeMillis, double[] frequencyRange, float sampleRate, double imagePos) {
		this.displayHeight = displayHeight;
		this.displayWidth = displayWidth;
		this.frequencyRange = frequencyRange;
		this.timeRangeMillis = timeRangeMillis;
		xStartPix = imagePos;
		xStartMillis = startMillis;
		xPixsPerMillisecond = displayWidth / timeRangeMillis;
		double fRange = frequencyRange[1]-frequencyRange[0];
		if (fRange > 0) {
			yPixsPerHz = displayHeight / fRange;
		}
	}

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getCoord3d(PamUtils.PamCoordinate)
	 */
	@Override
	public Coordinate3d getCoord3d(Coordinate3d dataValues) {
		return getCoord3d(dataValues.getCoordinate(0), 
				dataValues.getCoordinate(1), 
				dataValues.getCoordinate(2));
	}
	
	@Override
	public Coordinate3d getCoord3d(double timeMillis, double freqHz, double d3) {
		double x = xStartPix  + (timeMillis-xStartMillis) * xPixsPerMillisecond;
		// deal with wrap around - don't wrap if it's beyond the display max time. 
		if (x >= displayWidth) {
			
			// if the time is still within the current window, subtract the displayWidth to wrap it around to the start of the panel
			if ((timeMillis-xStartMillis) < timeRangeMillis) {
				x -= displayWidth;
			}
			// otherwise, we're trying to display something in the future (i.e. beyond the currentTimeMilliseconds).  This happens when we've frozen the display
			// but more data units are being detected since PAMGuard is still running in the background.  Ideally we'd want to return null here to indicate
			// that there's something wrong with this data unit and it shouldn't be displayed.  But this method is used everywhere and adding a new return option
			// could easily break something.  So instead we lock the x position to the current cursor position, and just have it draw some meaningless pixels
			// until the user lets go of the mouse and everything returns to normal
			else {
				x = xStartPix;
			}
		}
		if (x < 0) {
			x += displayWidth;
		}
		double y = freq2y(freqHz);
		return new Coordinate3d(x, y);
	}
	
	private double freq2y(double fHz) {
		double y = 0;
		if (isLogScale) {
			y = displayHeight - displayHeight * (Math.log(fHz/frequencyRange[0])/Math.log(frequencyRange[1]/frequencyRange[0]));
		}
		else {
			y = displayHeight - (fHz- frequencyRange[0]) * yPixsPerHz;
		}
		return y;
	}
	
	private double y2freq(double yPix) {
		double hz = 0;
		if (isLogScale) {
			double yRel = (displayHeight - yPix)/displayHeight;
			yRel *= Math.log(frequencyRange[1]/frequencyRange[0]);
			yRel = Math.exp(yRel);
			hz = yRel * frequencyRange[0];
		}
		else {
			hz = (displayHeight-yPix)/yPixsPerHz + frequencyRange[0];
		}
		return hz;		
	}
	

	/* (non-Javadoc)
	 * @see PamView.GeneralProjector#getDataPosition(PamUtils.PamCoordinate)
	 */
	@Override
	public Coordinate3d getDataPosition(PamCoordinate screenPosition) {
		double x = screenPosition.getCoordinate(0);
		double y = screenPosition.getCoordinate(1);
		double timeMillis = (x-xStartPix)/xPixsPerMillisecond + xStartMillis;
		/*
		 *  deal with wrap around - don't wrap if it's beyond the display max time.
		 */
		if (timeMillis > xStartMillis + timeRangeMillis) {
			timeMillis -= timeRangeMillis;
		}
		if (timeMillis < xStartMillis) {
			timeMillis += timeRangeMillis;
		}
		double freqHz = y2freq(y);
		return new Coordinate3d(timeMillis, freqHz);
	}
	/**
	 * @return the spectrogramDisplay
	 */
	public SpectrogramDisplay getSpectrogramDisplay() {
		return spectrogramDisplay;
	}
	/**
	 * @return the displayHeight
	 */
	public int getDisplayHeight() {
		return displayHeight;
	}
	/**
	 * @return the displayWidth
	 */
	public int getDisplayWidth() {
		return displayWidth;
	}
	/**
	 * @return the panelId
	 */
	public int getPanelId() {
		return panelId;
	}
	/**
	 * @return the isLogScale
	 */
	public boolean isLogScale() {
		return isLogScale;
	}
	/**
	 * @param isLogScale the isLogScale to set
	 */
	public void setLogScale(boolean isLogScale) {
		this.isLogScale = isLogScale;
	}

}
