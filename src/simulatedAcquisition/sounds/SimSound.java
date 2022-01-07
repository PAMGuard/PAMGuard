package simulatedAcquisition.sounds;

import pamMaths.PamVector;
import simulatedAcquisition.PistonModel;
import simulatedAcquisition.SimObject;
import simulatedAcquisition.SimObjectDataUnit;
import simulatedAcquisition.SimReceivedSound;
import PamUtils.LatLong;

/**
 * Class to hold information on a single sound. 
 * These will be held in a list for each simulated object
 * @author Doug Gillespie
 *
 */
public class SimSound {
	
	/**
	 * first sample - at the animal. It will get to the hydrophone later. 
	 */
	double startSampleAtAnimal;
	
	long startTimeMillis;
	
//	double endSample;
	
	boolean started;
		
	/**
	 * Master list of sounds received at each hydrophone
	 */
	private SimReceivedSound[][] receivedSounds;
	
	LatLong latLong;
	
	double height;
	
	double[][] hydrophoneDelays;
	
	double[][] transmissionGains;
	
	private long firstChannelSample;
	private long lastChannelSample;
	
	/**
	 * Signal level of the sound 1m from the hydrophone
	 */
	double[] soundAmplitude;

	private PamVector[][] pointingVectors;

	private PamVector heading;

	private SimSignal simSignal;

	private SimObjectDataUnit simObjectDataUnit;

	public SimSound(SimObjectDataUnit simObjectDataUnit, double startSample, long startTimeMillis, LatLong latLong, double height, PamVector heading, SimSignal simSignal) {
		this.simObjectDataUnit = simObjectDataUnit;
		this.startSampleAtAnimal = startSample + Math.random();
		this.simSignal = simSignal;
		this.startTimeMillis = startTimeMillis;
		this.latLong = latLong.clone();
		this.height = height;
		this.heading = heading;
//		this.endSample = startSample;//+ waveform.length;
		started = false;
	}
	
	public double[][] getHydrophoneDelays() {
		return hydrophoneDelays;
	}

	public void setHydrophoneDelays(double[][] delays) {
		this.hydrophoneDelays = delays;
	}
	
	public double getHydrophoneDelay(int iPhone, int iDelay) {
		return hydrophoneDelays[iPhone][iDelay];
	}
	
	public int getNumDelays() {
		return hydrophoneDelays[0].length;
	}
	public double[][] getTransmissionGains() {
		return transmissionGains;
	}

	public void setTransmissionGains(double[][] gains) {
		this.transmissionGains = gains;
	}
	
	public double getTranmissionGain(int iPhone, int iGain) {
		return transmissionGains[iPhone][iGain];
	}

	public double[] getSoundAmplitude() {
		return soundAmplitude;
	}
	
	public double getSoundAmplitude(int iChan) {
		return soundAmplitude[iChan];
	}

	public void setSoundAmplitude(double[] soundAmplitude) {
		this.soundAmplitude = soundAmplitude;
	}
	

	/**
	 * Set the start sample at the animal. 
	 * @param startSample - the start sample to set
	 */
	public void setStartSample(long startSample) {
		this.startSampleAtAnimal=startSample;
	}

	/**
	 * @return the startSample
	 */
	public double getStartSample() {
		return startSampleAtAnimal;
	}

	/**
	 * @return the startTimeMillis
	 */
	public long getStartTimeMillis() {
		return startTimeMillis;
	}

	/**
	 * @return the started
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * @return the latLong
	 */
	public LatLong getLatLong() {
		return latLong;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the heading
	 */
	public PamVector getHeading() {
		return heading;
	}

	/**
	 * @return the simSignal
	 */
	public SimSignal getSimSignal() {
		return simSignal;
	}

	public void setPointingVectors(PamVector[][] pointingVectors) {
		this.pointingVectors = pointingVectors;
	}

	/**
	 * This is where a whole load of different waveforms are generated for all the different channels.  
	 * @param sampleRate samplerate for generation. 
	 */
	public void generateWaves(float sampleRate) {
		/**
		 * First work out how many waves there are going to be
		 */
		int nChan = hydrophoneDelays.length;
		if (nChan == 0) {
			return;
		}
		int nDelays = hydrophoneDelays[0].length;
		if (nDelays == 0) {
			return;
		}
		receivedSounds = new SimReceivedSound[nChan][nDelays];
		firstChannelSample = Long.MAX_VALUE;
		lastChannelSample = 0;
		
		simSignal.prepareSignal();
		for (int iChan = 0; iChan < nChan; iChan++) {
			for (int iDelay = 0; iDelay < nDelays; iDelay++) {
				receivedSounds[iChan][iDelay] = createReceivedSound(iChan, sampleRate, hydrophoneDelays[iChan][iDelay], 
						soundAmplitude[iChan] * transmissionGains[iChan][iDelay], pointingVectors[iChan][iDelay]);
				if (receivedSounds[iChan][iDelay] != null) {
					firstChannelSample = Math.min(firstChannelSample, receivedSounds[iChan][iDelay].getStartSample());
					lastChannelSample = Math.max(lastChannelSample, receivedSounds[iChan][iDelay].getEndSample());
				}
			}
		}
		
	}



	/**
	 * Create the received sound for the animal, including piston model of 
	 * beam if required.  
	 * @param sampleRate system sample rate
	 * @param delaySeconds delay from the animal to the hydrophone from prop model
	 * @param amplitude amplitude from prop model
	 * @param pointVector direction to hydrophone from animal. 
	 * @return simulated recieved sound. 
	 */
	private SimReceivedSound createReceivedSound(int channel, float sampleRate, double delaySeconds, double amplitude, PamVector pointVector) {
		SimReceivedSound simRxSound;
		double dStartSample = startSampleAtAnimal + delaySeconds * sampleRate;
		// we've effectively missed the preceding sample, so the first 
		// sample in the data will be the next one. 
		long iStartSample = (long) Math.ceil(dStartSample);
		double sampleOffset = (double) iStartSample - dStartSample;
		double[] w = simSignal.getSignal(channel, sampleRate, sampleOffset);
		if (simObjectDataUnit.getSimObject().pistonBeam) {
			// piston model beam convolution. 
			// first work out the angle between the head of the animal and the direction to the hydrophone
			SimObject simObject = simObjectDataUnit.getSimObject();
			double ang = pointVector.angle(heading);
			double c = simObjectDataUnit.getSimProcess().getSimulatorSpeedOfSound();
			double[] impulseResponse = PistonModel.getImpulseResponse(simObject.pistonRadius, ang, sampleRate, 1500, sampleOffset);
			w = PistonModel.conv(w, impulseResponse);
			/*
			 * If it's coming out the back end of the animal, 
			 * add an additional attenuation term
			 */
			if (ang > Math.PI/2) {
				double arseAtten = 50; // 50 dB
				arseAtten *= Math.cos(ang);
				arseAtten = Math.pow(10., arseAtten/20);
				for (int i = 0; i < w.length; i++) {
					w[i] *= arseAtten;
				}
			}
			
		}
		for (int i = 0; i < w.length; i++) {
			w[i] *= amplitude;	
		}
		simRxSound = new SimReceivedSound(iStartSample, w);
		
		return simRxSound;
	}

	/**
	 * @return the firstChannelSample
	 */
	public long getFirstChannelSample() {
		return firstChannelSample;
	}

	/**
	 * @return the lastChannelSample
	 */
	public long getLastChannelSample() {
		return lastChannelSample;
	}

	/**
	 * @param iChan channel number
	 * @return the receivedSounds forgiven channel
	 */
	public SimReceivedSound[] getReceivedSounds(int iChan) {
		if (receivedSounds == null) {
			return null;
		}
		return receivedSounds[iChan];
	}

	/**
	 * @return the receivedSounds for all channels
	 */
	public SimReceivedSound[][] getReceivedSounds() {
		return receivedSounds;
	}

	/**
	 * @return the startSampleAtAnimal
	 */
	public double getStartSampleAtAnimal() {
		return startSampleAtAnimal;
	}

	/**
	 * @return the pointingVectors
	 */
	public PamVector[][] getPointingVectors() {
		return pointingVectors;
	}

	/**
	 * @return the simObjectDataUnit
	 */
	public SimObjectDataUnit getSimObjectDataUnit() {
		return simObjectDataUnit;
	}
	
	
	
}
