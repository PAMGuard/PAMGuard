package soundPlayback;

import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.FrequencyFormat;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import soundPlayback.preprocess.EnvelopeTracer;
import soundPlayback.preprocess.PlaybackDecimator;
import soundPlayback.preprocess.PlaybackFilter;
import soundPlayback.preprocess.PlaybackGain;
import soundPlayback.preprocess.PlaybackPreprocess;
import warnings.QuickWarning;

/**
 * Pam Process for sound playback (controls data from multiple channels
 * and then passes it on to the PlaybackSystem.
 * <p>
 * For file analysis, playback is through a chosen sound card. For 
 * playback of real time data, the playbackSystem is ideally handled by the real time
 * acquisition system - so samples stay synchronised.
 * @author Doug Gillespie
 * @see PlaybackSystem
 *
 */
public class PlaybackProcess extends PamInstantProcess {

	private PlaybackControl playbackControl;
	
	private RawDataUnit rawDataUnits[];
	
	private int[] channelPos;
	
	int haveChannels;
	
	int runningChannels;
	
	boolean running = false;
	
	private PBSampleRateData sampleRateData;

	private PlaybackDecimator playbackDecimator;
	
	private PlaybackGain playbackGain;
	
	private PlaybackFilter playbackFilter;
		
	private QuickWarning playWarning;
	
	private ArrayList<PlaybackPreprocess> preProcesses = new ArrayList<>();
	
	public PlaybackProcess(PlaybackControl playbackControl) {
		super(playbackControl);
		this.playbackControl = playbackControl;
		playWarning = new QuickWarning(playbackControl.getUnitName());
		
		preProcesses.add(playbackFilter = new PlaybackFilter(playbackControl));
		preProcesses.add(new EnvelopeTracer(playbackControl));
		preProcesses.add(playbackDecimator = new PlaybackDecimator(playbackControl));
		preProcesses.add(playbackGain = new PlaybackGain(playbackControl));
	}

	@Override
	public void prepareProcess() {
//		super.prepareProcess();
//		System.out.println("Playback prepare process");
		prepareProcess(getSourceSampleRate());
	}
	
	public boolean prepareProcess(double sourceSampleRate) {
		if (playbackControl.playbackSystem == null) {
			return false;
		}
		sampleRateData = sortOutputSampleRate(sourceSampleRate);
		
		if (runningChannels == 0) {
			clearWarning();
			return true;
		}
//		System.out.println("Playback speed: " + sampleRateData);
		
		boolean prepOK = playbackControl.playbackSystem.prepareSystem(playbackControl, 
				runningChannels, (float) sampleRateData.getOutputSampleRate());
		if (!prepOK) {
			showStartWarning(runningChannels, sampleRateData.getOutputSampleRate());
			return false;
		}
		else {
			clearWarning();
		}
		
		preparePreProcesses(sourceSampleRate);
		return true;
	}

	private void clearWarning() {
		playWarning.clearWarning();
	}

	private void showStartWarning(int runningChannels, double outputSampleRate) {
		String warn = String.format("Unable to start sound output device %s\n" +
				"playing %d channels at %s", playbackControl.playbackSystem.getName(), 
				runningChannels, FrequencyFormat.formatFrequency(outputSampleRate, true));
		playWarning.setWarning(warn, 2);
	}

	/**
	 * Sort out the sample rates and any decimation factors<br>
	 * Will also set a decimator factor which will be >= 1.<br>
	 * 1 means no decimator needed, >1 needs decimation. 
	 * @param dataSampleRate This gets passed around as a parameter since it may vary a bit in viewer mode. 
	 * @return the actual output sample rate. 
	 */
	private PBSampleRateData sortOutputSampleRate(double sourceSampleRate) {
		if (playbackControl.playbackSystem == null) {
			return null;
		}
		double decimateFactor;
		boolean rt = playbackControl.isRealTimePlayback(); // must happen at same speed as input (decimate if different rate)
		boolean mustFollowFS = playbackControl.playbackSystem.mustSameIOSampleRate();
		PlaybackParameters params = playbackControl.playbackParameters;
		double outputSampleRate = params.getPlaybackRate();
		if (rt) { // real time processing. 
			if (mustFollowFS) {
				decimateFactor = 1.0;
				return new PBSampleRateData(sourceSampleRate, sourceSampleRate);
			}
			else {
//				decimateFactor = sourceSampleRate / outputSampleRate;
//				if (decimateFactor < 1) { // input rate is < 48kHz, so just output at that rate. 
//					decimateFactor = 1;
//					return new PBSampleRateData(sourceSampleRate, sourceSampleRate);
//				}
//				else {
					return new PBSampleRateData(sourceSampleRate, outputSampleRate);
//				}
			}
		}
		else { // file processing, or running offline. 
//			if (mustFollowFS) {
//				decimateFactor = 1;
//				return new PBSampleRateData(sourceSampleRate, sourceSampleRate);
//			}
//			else {
//				decimateFactor = sourceSampleRate * params.getPlaybackSpeed(sourceSampleRate) / PlaybackControl.DEFAULT_SAMPLE_RATE;
//				if (decimateFactor < 1.) {
//					decimateFactor = 1;
//					return new PBSampleRateData(sourceSampleRate, sourceSampleRate, params.getPlaybackSpeed(sourceSampleRate));
//				}
//				else {
					return new PBSampleRateData(sourceSampleRate, params.getPlaybackRate(), params.getPlaybackSpeed());
//				}
//			}
		}
		/*
		 * Now set up the decimator if we're using one. 
		 */
	}
	
	/**
	 * Called from speed slider on sidebar. 
	 */
	public synchronized void resetDecimator() {
		if (sampleRateData == null) {
			return;
		}
		sampleRateData = this.sortOutputSampleRate(sampleRateData.getInputSampleRate());
		playbackDecimator.reset(sampleRateData.getInputSampleRate(), playbackControl.getPlaybackParameters().channelBitmap);
	}

	/**
	 * Prepare processes to apply to the data prior to the data being output. 
	 * @param sourceSampleRate 
	 */
	private void preparePreProcesses(double sourceSampleRate) {
		for (PlaybackPreprocess pp : preProcesses) {
			pp.reset(sourceSampleRate, playbackControl.getPlaybackParameters().channelBitmap);
		}
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
	}

	@Override
	public void pamStart() {
		if (playbackControl.playbackSystem == null) return;
		running = true;
	}
	
	@Override
	public void pamStop() {
		if (playbackControl.playbackSystem == null) return;
		playbackControl.playbackSystem.unPrepareSystem();
		running = false;
	}

	@Override
	synchronized public void newData(PamObservable o, PamDataUnit arg) {
		if (playbackControl.playbackSystem == null) return;
		int channel = PamUtils.getSingleChannel(arg.getChannelBitmap());
		int pos = channelPos[channel];
//		System.out.printf("Channel %d, pos %d\n", channel, pos);
		if (pos < 0) {
//			playWarning.setWarning("pos<0", 2);
			return; // it's a channel we don't want
		}
		/*
		 * Need to ensure that channels get grouped in the right order. 
		 * Ordering may get confused if playback was turned off and on 
		 * during a run. 
		 * It should always be true, that if haveChannels is zero, then
		 * pos should also be zero. 
		 */
		if (pos == 0) {
			haveChannels = 0;
//			return;
		}
		rawDataUnits[pos] = (RawDataUnit) arg;
		haveChannels |= arg.getChannelBitmap();
		if (haveChannels == playbackControl.playbackParameters.channelBitmap) {
			// do something with the data
//			double gain = Math.pow(10., playbackControl.playbackParameters.playbackGain/20.);
			RawDataUnit[] playUnits = preprocessData(rawDataUnits);
			if (playUnits != null) {
				boolean playOK = playbackControl.playbackSystem.playData(playUnits, 1);
				if (!playOK) {
					playWarning.setWarning("PlaybackProcess: playData return error", 2);
					noteNewSettings(); // forces full reset of playback
				}
				else {
					clearWarning();
				}
//				System.out.println("pLAY");
				playbackControl.setPlaybackProgress(playUnits[pos].getTimeMilliseconds());
			}
			haveChannels = 0;
			rawDataUnits = new RawDataUnit[rawDataUnits.length];
//			for (int i = 0; i < rawDataUnits.length; i++) {
//				rawDataUnits[i] = null;
//			}
		}	
//		else {
//			String msg = String.format("Have Channels %d want %d",
//					haveChannels, playbackControl.playbackParameters.channelBitmap);
//			playWarning.setWarning(msg, 2);
////			System.out.println(msg);
//		}
	}

	private RawDataUnit[] preprocessData(RawDataUnit[] inputDataUnits) {
		int isNull = 0;
		RawDataUnit[] outUnits = new RawDataUnit[inputDataUnits.length];
		synchronized(this) {
			for (int i = 0; i < outUnits.length; i++) {
				outUnits[i] = preprocessData(inputDataUnits[i]);
				if (outUnits[i] == null) {
					isNull++;
				}
			}
		}
		if (isNull > 0) {
			return null;
		}
		else {
			return outUnits;
		}
	}

	private RawDataUnit preprocessData(RawDataUnit rawDataUnit) {
		int procsDone = 0;
		RawDataUnit outUnit = rawDataUnit;
		for (PlaybackPreprocess pp : preProcesses) {
			if (pp.isActive()) {
				outUnit = pp.processDataUnit(outUnit, procsDone++ == 0);
			}
		}
//		if (playbackControl.getPlaybackParameters().getHpFilter() > 0) {
//			outUnit = playbackFilter.processDataUnit(outUnit, procsDone == 0);
//		}
//		if (playbackDecimator != null) {
//			outUnit = playbackDecimator.processDataUnit(outUnit, procsDone == 0);
//			procsDone++;
//		}
//		outUnit = playbackGain.processDataUnit(outUnit, procsDone == 0);
		return outUnit;
	}

	@Override
	synchronized public void noteNewSettings() {
		if (!PamController.getInstance().isInitializationComplete()) {
			return;
		}
		
		PamDataBlock sourceData = PamController.getInstance().getRawDataBlock(
				playbackControl.playbackParameters.dataSource);
		
		setParentDataBlock(sourceData);

		runningChannels = PamUtils.getNumChannels(playbackControl.playbackParameters.channelBitmap);
		rawDataUnits = new RawDataUnit[runningChannels];
		channelPos = PamUtils.getChannelPositionLUT(playbackControl.playbackParameters.channelBitmap);
		haveChannels = 0;
		
		// if it's running, restart it to get new sample rate, device, etc.

		if (playbackControl.playbackSystem != null && running) {
			playbackControl.playbackSystem.unPrepareSystem();
			playbackControl.playbackSystem.prepareSystem(playbackControl, runningChannels, playbackControl.choseSampleRate());
		}

		prepareProcess();
//		sampleRateData = sortOutputSampleRate();
//		preparePreProcesses();
//		sortOutDecimator(playbackControl.playbackParameters.channelBitmap);
		
		super.noteNewSettings();
	}

	/**
	 * Note that this is the sample rate of the input data, not 
	 * the playback output data. Probably best not to call it !
	 */
	@Override
	public float getSampleRate() {
		return super.getSampleRate();
	}
	
	/**
	 * Get the source data sample rate. 
	 * @return
	 */
	public double getSourceSampleRate() {
		return playbackControl.getSourceSampleRate();
	}

	/**
	 * Setup a high pass filter on the data. 
	 * This will run before the decimator to keep it all simple ! 
	 */
	public synchronized void setUpFilter() {
		playbackFilter.reset(getSampleRate(), playbackControl.getPlaybackParameters().channelBitmap);
	}

	public void setGain(int gaindB) {
		playbackControl.playbackParameters.playbackGain = gaindB;
		playbackGain.setGaindB(gaindB);
	}

	/**
	 * @return the sampleRateData
	 */
	public PBSampleRateData getSampleRateData() {
		return sampleRateData;
	}

	/**
	 * @return the preProcesses
	 */
	public ArrayList<PlaybackPreprocess> getPreProcesses() {
		return preProcesses;
	}

	@Override
	public ArrayList getCompatibleDataUnits(){
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class));
	}

}
