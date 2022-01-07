package Spectrogram;

import java.awt.event.MouseEvent;

import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import PamController.PamControlledUnit;
import PamguardMVC.PamProcess;
import dataPlotsFX.layout.TDGraphFX;

/**
 * Standard process for any PamControlled unit observing marks made manually
 * on spectrogram displays. 
 * <p>
 * These processes receive a notification message from spectrgram displays when
 * the mouse is pressed on the display and again when it is released. The 
 * notification contains the time and frequency bounds of the mark, which can
 * then be used to perform some kind of data processing, e.g. calculate the
 * bearing to the sound within the mark, make a waveform clip, etc. 
 * <p>
 * It is important that the necessary data is available in memory for the 
 * SpectrogramMarkProcess. This is most likely to be either the spectrgram
 * data itself, or the waveform data used to create the spectrogram FFT
 * blocks. To ensure that these data are available, any SpectrogramDisplay
 * which has one or more active SpectrogramMarkObservers will subscribe to the FFT
 * and Waveform data blocks and ensure that at least one complete screen full of 
 * data is always stored. When the mouse is pressed, the SpectrogramDisplay ceases
 * scrolling, but Pamguard data processing continues. During the time that the mouse
 * is pressed, the SpectrogramDisplay will extend the time data are kept for as 
 * necessary to ensure that data from the mouse press time are indeed available. 
 * <p>
 * This abstract subclass of PamProcess provides some simple utilities to get at the
 * waveform data and tha FFT data.  
 * 
 * @author Doug Gillespie
 * @see Spectrogram.SpectrogramDisplay
 * @see Spectrogram.SpectrogramMarkObserver
 * @see PamguardMVC.PamDataBlock
 *
 */
abstract public class SpectrogramMarkProcess extends PamProcess implements SpectrogramMarkObserver {
	
	public SpectrogramMarkProcess(PamControlledUnit pamControlledUnit) {
		super(pamControlledUnit, null);
		SpectrogramMarkObservers.addSpectrogramMarkObserver(this);
	}
	
	/**
	 * Override this to process data from the marked spectrogram. 
	 * <p>
	 * The spectrogram will have already ensured that Raw waveform data
	 * and FFT data that made the SpectrogramDisplay are still in 
	 * memory and the data blocks can be accessed using <p>
	 * PamRawDataBlock rawDataBlock = (PamRawDataBlock) display.getSourceRawDataBlock();
	 * <p>and<p>
	 * PamDataBlock fftDataBlock = display.getSourceFFTDataBlock();
	 * <p>
	 * To obtain raw data, first convert the startMilliseconds and duration
	 * to sample numbers using <p>
	 *	long startSample = absMillisecondsToSamples(startMilliseconds);
	 *	<p> and <p>
	 *	int numSamples = (int) relMillisecondsToSamples(duration);
	 *	<p> then make a bitmap of the channels you wnat - <p>
	 *	int channelMap; <p>
	 *	channelMap = PamUtils.SetBit(0, channel, 1); // just the channel that had the mark <p>
	 *	channelMap = rawDataBlock.getChannelMap(); // all channels in the raw data block  <p>
	 *	<p> Then get teh samples from the raw data block ... <p>
	 *	double[][] rawData = rawDataBlock.getSamples(startSample, numSamples, channelMap);
	 *<p>
	 *To get the FFT data, use 
		*PamDataUnit fftDataUnit; <p>
	*	int fftDataUnitIndex = fftDataBlock.getIndexOfFirstUnitAfter(startMilliseconds); <p>
	*	if (fftDataUnitIndex >= 0) while (fftDataUnitIndex < fftDataBlock.getUnitsCount()) { <p>
	*		fftDataUnit = fftDataBlock.getDataUnit(fftDataUnitIndex, PamDataBlock.REFERENCE_CURRENT); <p>
	*		if (fftDataUnit.timeMilliseconds + fftDataUnit.duration > startMilliseconds + duration) { <p>
	*			break; <p>
	*		} <p>
	*		// process that unit in any way you want, then get the next unit <p>
	*		fftDataUnitIndex ++; <p>
	*	} <p>
	*Remember that the data units will contain one channel of fft data each and multiple channels 
	*may be interleaved.
	*
	 */
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEvent, int downUp, 
			int channel, long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay) {

//		Sample code for getting raw data.
//		PamRawDataBlock rawDataBlock = (PamRawDataBlock) display.getSourceRawDataBlock();
		
		FFTDataBlock fftDataBlock = null;
		if (display!=null) {
			fftDataBlock = display.getSourceFFTDataBlock();
		} else if (tdDisplay!=null) {
			fftDataBlock = tdDisplay.getFFTDataBlock();
		}
		if (fftDataBlock==null) return false;
		/* 
		 * Seem to be some issues with getting the correct sample rate, so do it again here
		 * 
		 */
		setSampleRate(fftDataBlock.getSampleRate(), false);
//		long startSample = absMillisecondsToSamples(startMilliseconds);
//		int numSamples = (int) relMillisecondsToSamples(duration);
//		int channelMap;
//		channelMap = PamUtils.SetBit(0, channel, 1); // just the channel that had the mark
//		channelMap = rawDataBlock.getChannelMap(); // all channels in the raw data block 
//		double[][] rawData = rawDataBlock.getSamples(startSample, numSamples, channelMap);
		FFTDataUnit fftDataUnit;
//		int fftDataUnitIndex = fftDataBlock.getIndexOfFirstUnitAfter(startMilliseconds);
//		if (fftDataUnitIndex >= 0) while (fftDataUnitIndex < fftDataBlock.getUnitsCount()) {
//			fftDataUnit = fftDataBlock.getDataUnit(fftDataUnitIndex, PamDataBlock.REFERENCE_CURRENT);
//			if (fftDataUnit.getTimeMilliseconds() + fftDataUnit.getDuration() > startMilliseconds + duration) {
//				break;
//			}
//			// process that unit in any way you want, then get the next unit
//			fftDataUnitIndex ++;
//		}
		return false;
	}
	
	public String getMarkObserverName() {
		return getProcessName();
	}
	
//	public void spectrogramNotification(SpectrogramDisplay display, 
//	int downUp, int channel, long startMilliseconds, 
//	long duration, double f1, double f2) {
//	if (downUp == SpectrogramMarkObserver.MOUSE_DOWN) {
//	System.out.println(String.format("Mouse down channel %d - start %s; Frequency %4.1f ",
//	channel, PamCalendar.formatTime(startMilliseconds),  f1));
//	}
//	else {
//	System.out.println(String.format("Mouse up   channel %d - start %s; duration %4.2fs; Frequency %4.1f to %4.1f ",
//	channel, PamCalendar.formatTime(startMilliseconds), duration/1000., f1, f2));
//	
//	}
//	
//	}
	
	
	@Override
	public void pamStart() {
		
	}
	
	@Override
	public void pamStop() {
		
	}
	
}
