package simulatedAcquisition;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

import javax.sound.midi.Transmitter;

import pamMaths.PamVector;
import propagation.PropagationModel;
import simulatedAcquisition.movement.MovementModel;
import simulatedAcquisition.movement.MovementModels;
import simulatedAcquisition.sounds.SimSignal;
import simulatedAcquisition.sounds.SimSound;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.PamArray;
import Array.SnapshotGeometry;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;

/**
 * SimObjectDataUnit is one per sound source, i.e. think 
 * of one of these things as an animal. It may be handling 
 * a list of several sounds. 
 * @author doug
 *
 */
public class SimObjectDataUnit extends PamDataUnit {

	private SimObject simObject;

	private LatLong currentPosition = null;

	private PamVector currentHeading;

	private double currentHeight;

	protected long lastUpdateTime;

	private SimProcess simProcess;

	private SimSignal simSignal;

	private Random random = new Random();

	private LinkedList<SimSound> simSounds = new LinkedList<SimSound>();

	private MovementModel movementModel;

	/**
	 * this is OK in samples. Even after a day at 500kHz, have eps of 1e-6 samples. 
	 */
	private double lastGenSample = 0;

	public SimObjectDataUnit(SimProcess simProcess, SimObject simObject, long firstSigTime) {
		super(0);
		this.simProcess = simProcess;
		this.simObject = simObject;
		simObject.simObjectDataUnit = this;

		currentHeading = PamVector.fromHeadAndSlant(simObject.course, simObject.slantAngle);
		currentHeight = simObject.getHeight();
		currentPosition = simObject.startPosition.clone();

	}

	synchronized protected void prepareSimulation() {
		lastUpdateTime = 0;
		lastGenSample = 0;
		clearSignals();
		movementModel = simObject.getSelectedMovementModel();
		movementModel.start(PamCalendar.getTimeInMillis(), this);
		//		System.out.println("Prepare simulation object");
	}
	synchronized protected void clearSignals() {
		simSounds.clear();
	}

	/**
	 * Generate the next sound - this is called whenever there is no sound 
	 * in the current list or when the last sound has been started. 
	 * @param snipStartSample start sample of the snip from which this sound was generated
	 */
	private void genSignal(long snipStartSample) {
		//		System.out.println("Generate sound " + lastGenSample);
		simSignal = getSimSignal();
		if (simSignal == null) {
			System.out.println("Can't generate");
			return;
		}
		float sampleRate = simProcess.getSampleRate();
		if (simObject.randomIntervals == false && lastGenSample > 0) {
			lastGenSample += (long) (sampleRate * simObject.meanInterval);
		}
		else {
			lastGenSample += (long) (random.nextDouble() * 2 * sampleRate * simObject.meanInterval);
		}

		/**
		 * This will have to move from here, so that it can incorporate all the 
		 * delays, etc. 
		 */
		long trueMillis = simProcess.getSimSoundsDataBlock().getParentProcess().absSamplesToMilliseconds((long)lastGenSample);
//		long calMillis = PamCalendar.getTimeInMillis();
//		Debug.out.printf("Generate sound at %s or %s diff %d?\n", PamCalendar.formatTime(trueMillis), PamCalendar.formatTime(calMillis), trueMillis-calMillis);
		SimSound simSound = new SimSound(this, lastGenSample, trueMillis, 
				currentPosition, currentHeight, currentHeading, simSignal);	

		/*
		 * At this point we should work out the time delay to each hydrophone
		 * and store it in the sound
		 */
		AcquisitionProcess daqProcess = simProcess.getDaqControl().getAcquisitionProcess();
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		HydrophoneLocator hLocator = array.getHydrophoneLocator();
		long timeMillis = PamCalendar.getTimeInMillis();
		int n = array.getHydrophoneCount();
		SnapshotGeometry snapshotGeom = ArrayManager.getArrayManager().getSnapshotGeometry(PamUtils.makeChannelMap(n), timeMillis);
		//		AcquisitionParameters daqParam = simProcess.g
		double[][] delays = new double[n][]; // transmission delays
		double[][] gains = new double[n][]; // transmission gains
		PamVector[][] pointingVectors = new PamVector[n][];
		LatLong[] phoneLatLong = new LatLong[n];
		double dist;
		PropagationModel propagationModel = simProcess.getPropagationModel();
		double[] sigAmplitude = new double[n]; // signal amplitude if it were at 1 m.

		for (int i = 0; i < n; i++) {
//			array.geth
			phoneLatLong[i] = snapshotGeom.getAbsPhoneLatLong(i);
//			phoneLatLong[i] = hLocator.getPhoneLatLong(timeMillis, i);
			currentPosition.setHeight(currentHeight); // shouldn't be needed.
			propagationModel.setLocations(phoneLatLong[i], currentPosition, simProcess.getSimulatorSpeedOfSound());
			delays[i] = propagationModel.getDelays();
			gains[i] = propagationModel.getGains();
			pointingVectors[i] = propagationModel.getPointingVectors();
			sigAmplitude[i] = daqProcess.dbMicropascalToSignal(i, simObject.amplitude);
		}
		double bearingTo = phoneLatLong[0].bearingTo(phoneLatLong[1]);
		double distanceTo = phoneLatLong[0].distanceToMetres(phoneLatLong[1]);
		simSound.setHydrophoneDelays(delays);
		simSound.setTransmissionGains(gains);
		simSound.setSoundAmplitude(sigAmplitude);
		simSound.setPointingVectors(pointingVectors);
		simSound.generateWaves(sampleRate);

		// work out the amplitude - from dB !
		double ampDB = simObject.amplitude;

		//Add an echo. Not the most elegent solution to do this but it seemed to be the only one that worked. 
		SimSound simSoundEcho = null; 
		if (simObject.echo && simObject.seperateEcho) {
			long lastGenSampleEcho=(long) (lastGenSample+(simObject.echoDelay/1000.)*sampleRate); 
			simSoundEcho = new SimSound(this, lastGenSampleEcho, (long) (PamCalendar.getTimeInMillis()+simObject.echoDelay), 
					currentPosition, currentHeight, currentHeading, simSignal);	
			simSoundEcho.setHydrophoneDelays(delays);
			simSoundEcho.setTransmissionGains(gains);
			simSoundEcho.setSoundAmplitude(sigAmplitude);
			simSoundEcho.setPointingVectors(pointingVectors);
			simSoundEcho.generateWaves(sampleRate);
		}

		// add this to the data block so that it get's databased. 
		SimSoundDataBlock ssDataBlock = simProcess.getSimSoundsDataBlock();
		if (ssDataBlock != null) {
			SimSoundDataUnit simSoundDataUnit = new SimSoundDataUnit(simSound.getStartTimeMillis(), simSound);
			ssDataBlock.addPamData(simSoundDataUnit);
			if (simSoundEcho!=null) {
				simSoundDataUnit = new SimSoundDataUnit(simSoundEcho.getStartTimeMillis(), simSoundEcho);
				ssDataBlock.addPamData(simSoundDataUnit);
			}
		}
		
		//add sounds(s) to list
		simSounds.add(simSound);
		if (simSoundEcho!=null)	simSounds.add(simSoundEcho);
		
		//		/*
		//		 * At the start before the hydrophone locator is working correctly
		//		 * it can get a very bad position. Check the time delay is less 
		//		 * than a few seconds. 
		//		 */
		//		if (delays[0][0] < 10) {
		//			simSounds.add(simSound);
		////			System.out.println("Sound generated (s)" + delays[0][0]);
		//		}
		//		else {
		////			System.out.println("Bad simulation - sound too far off (s) " + delays[0][0]);
		//			lastGenSample = sampleRate * 2;
		//		}
	}

	/**
	 * Iterate through the current list of sounds and add their signals
	 * at the appropriate time within this data unit. 
	 * @param snip waveform data for a channel of this data unit
	 * @param phone hydrophone number
	 * @param snipStartSample start sample of the current data unit.
	 */
	synchronized protected void takeSignals(double[] snip, int phone, long snipStartSample) {
		/*
		 *  iterate through list of sounds. 
		 *  if there are none, or we've started taking from the last one
		 *  then generate a new one
		 *  If a sound is complete, delete it.  
		 */
		ListIterator<SimSound> li = simSounds.listIterator();
		SimSound simSound = null;
		long snipEndSample = snipStartSample + snip.length; 

		// iterate over list of simulated sounds. 
		while (li.hasNext()) {
			//System.out.println("New simmed sounds to aquisition: ");
			simSound = li.next();
			if (simSound.getLastChannelSample() < snipStartSample) {
				continue;
			}
			if (simSound.getFirstChannelSample() > snipEndSample) {
				continue;
			}
			SimReceivedSound[] channelSounds = simSound.getReceivedSounds(phone);
			if (channelSounds == null) {
				continue;
			}
			for (int iS = 0; iS < channelSounds.length; iS++) {
				SimReceivedSound simRxSound = channelSounds[iS];
				if (simRxSound == null) {
					continue;
				}
				//				long firstSnipSamp = simRxSound.getStartSample() - snipStartSample;
				//				long lastSnipSamp = simRxSound.getEndSample() - snipStartSample;
				int snipOffset = (int) (simRxSound.getStartSample()-snipStartSample); 
				double[] w = simRxSound.getWave();
				int firstSignalSample = (int) Math.max(0, snipStartSample - simRxSound.getStartSample());
				//				int lastSignalSample = (int) Math.min(w.length, snipEndSample - simRxSound.getEndSample());
				int lastSignalSample = (int) Math.min(w.length, snip.length-snipOffset);
				for (int i = firstSignalSample; i < lastSignalSample; i++) {
					snip[i+snipOffset] += w[i];
				}
			}
			//			for (int iD = 0; iD < nDelays; iD++) {
			//				// absolute start sample of the simulated sound after being delayed. 
			//				w = simSound.getSimSignal().getSignal(sampleRate, 0);
			//				startSample = simSound.startSampleAtAnimal + (simSound.getHydrophoneDelay(phone, iD) * sampleRate);
			//				endSample = startSample + w.length;
			//				w = simSound.getSimSignal().getSignal(sampleRate, startSample-Math.floor(startSample));
			////				System.out.println(String.format("Sim sound start %3.3f / %3.3f, Snip start %3.3f",
			////						startSample / sampleRate, simSound.startSample / sampleRate, snipStartSample / sampleRate));
			//				if (endSample < snipStartSample) {
			//					simSound.setCompleteChannel(phone);
			//					continue; // This sound has not reached us yet. 
			//				}
			//				if (startSample >= snipStartSample + snip.length) {
			//					continue; // we've passed the end of this sound.
			//				}
			//				tranmissionGain = simSound.getTranmissionGain(phone, iD);
			//				amplitude = simSound.getSoundAmplitude(phone);
			//				// now find the overlapping region and copy it into the snip.
			//				// the generated sound is probably shorter than the snip
			//				// so loop through the gen sound
			////				w = simSound.waveform;
			//				simSound.started = true;
			//				simOffset = startSample - snipStartSample;
			////				System.out.println(String.format("Sim sound Chan %d start %3.3f, Snip start %3.3f, offset %3.2f samples",
			////						phone, startSample / sampleRate, snipStartSample / sampleRate, simOffset));
			//						
			//				// i is the sample number within the data unit we're generating (the snip)
			//				int i = (int) simOffset;
			//				i = Math.max(i, 0);
			//				while (i < snip.length) {
			////				for (i; i < w.length; i++) {
			//					// calculate the exact position within the simulated sound. 
			//					simPos = i - simOffset;
			//					if (simPos > w.length) {
			//						// only breaks after a complete extra sample since we're interpolating !
			//						break;
			//					}
			////					snip[i] += getInterpolatedAmplitude(w, simPos) *
			////					amplitude * tranmissionGain / 2; 
			//					snip[i] += w[(int) simPos] * amplitude * tranmissionGain / 2 * 100; 
			//					i++;
			//				}
			//			}
		}
		// here, simSound will either be null or the last sound
		// if null or last sound has started, then generate new
		//		if (simSound == null || simSound.started) {
		//			genSignal(snipStartSample);
		//		}
	}

	/**
	 * Get the amplitude of the simulated signal using polynomial interpolation
	 * to estimate the amplitude at the (non-integer) sample
	 * @param signal simulated signal
	 * @param intSample non-integer sample number
	 * @param offset offset from integer value. 
	 * @return estimated amplitude. 
	 */
	double getInterpolatedAmplitude(double[] signal, double simSample) {
		double v1, v2, v3;
		int midSample = (int) (simSample + 0.5);
		double offSet = simSample-midSample;
		v1 = pickSample(signal, midSample-1);
		v2 = pickSample(signal, midSample);
		v3 = pickSample(signal, midSample+1);
		double a = (v3+v1-2*v2)/2.;
		double b = (v3-v1)/2;
		//		return v2;
		return a*offSet*offSet + b*offSet + v2;
	}

	/**
	 * Pick a sample out of a signal, returning 0 if the sample 
	 * number is out with the bounds of the signal array. 
	 * @param signal signal
	 * @param sample sample number
	 * @return sample value or 0
	 */
	private double pickSample(double[] signal, int sample) {
		if (sample >= 0 && sample < signal.length) {
			return signal[sample];
		}
		else {
			return 0;
		}
	}

	synchronized protected void clearOldSounds(long currentSample) {
		ListIterator<SimSound> li = simSounds.listIterator();
		SimSound simSound = null;
		while (li.hasNext()) {
			simSound = li.next();
			if (simSound.getLastChannelSample() < currentSample) {
//				Debug.out.printf("Delete sound at %s %d\n", PamCalendar.formatTime(simSound.getStartTimeMillis()), (long)simSound.getStartSample());
				li.remove();
			}
		}
	}

	synchronized int createNewSounds(long totalSamples) {
		int added = 0;
		//System.out.println("Create New Sounds");
		while (wantNewSound(totalSamples)) {
			long millis = (long) (totalSamples * 1000. / simProcess.getSampleRate()) + 
					PamCalendar.getSessionStartTime();
			// always move just before a sound is generated. 
			boolean contSim = movementModel.takeStep(millis, this);
			if (contSim == false) {
				stopRun();
			}
			else {
				//System.out.println("Create signal " + (simObject.echo && simObject.seperateEcho));
				genSignal(totalSamples);
				added++;					
			}
		}
		return added;
	}
	//		System.out.println(String.format("Added %d new sounds, total now = %d", added, simSounds.size()));

	private void stopRun() {
		PamController.getInstance().pamStop();
	}

	private boolean wantNewSound(long totalSamples) {
		if (simSounds.size() == 0) {
			return true;
		}
		SimSound lastSound = simSounds.getLast();
		if (lastSound.getStartSampleAtAnimal() < totalSamples) {
			return true;
		}
		return false;
	}

	public SimObject getSimObject() {
		return simObject;
	}

	public void setSimObject(SimObject simObject) {
		this.simObject = simObject;
	}

	public SimSignal getSimSignal() {
		if (simSignal == null) {
			if (simObject.signalName == null) {
				simObject.signalName = "Impulse";
			}
			setSimSignal();
		}
		return simSignal;
	}

	public void setSimSignal(SimSignal simSignal) {
		this.simSignal = simSignal;
	}

	public boolean setSimSignal() {
		simSignal = simProcess.simSignals.findSignal(simObject.signalName);
		return (simSignal != null);
	}

	/**
	 * @return the currentPosition
	 */
	public LatLong getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * @param timeMilliseconds 
	 * @param currentPosition the currentPosition to set
	 * @param currentHeading 
	 * @param currentHeight 
	 */
	public void setCurrentPosition(long timeMilliseconds, LatLong currentPosition, double currentHeight, PamVector currentHeading) {
		this.currentPosition = currentPosition;
		this.currentHeading = currentHeading;
		this.currentHeight = currentHeight;
		simProcess.getSimObjectsDataBlock().updatePamData(this, timeMilliseconds);

	}

	/**
	 * Called after the object has been moved with the mouse. 
	 * @param timeInMillis
	 */
	public void resetSounds(long timeInMillis) {
		//		clearSignals();
		//		createNewSounds((long) 0);
	}

	/**
	 * @return the simProcess
	 */
	public SimProcess getSimProcess() {
		return simProcess;
	}

	/**
	 * @return the currentHeading
	 */
	public PamVector getCurrentHeading() {
		return currentHeading;
	}

	/**
	 * @param currentHeading the currentHeading to set
	 */
	public void setCurrentHeading(PamVector currentHeading) {
		this.currentHeading = currentHeading;
	}

	/**
	 * @return the currentHeight
	 */
	public double getCurrentHeight() {
		return currentHeight;
	}

	/**
	 * @param currentHeight the currentHeight to set
	 */
	public void setCurrentHeight(double currentHeight) {
		this.currentHeight = currentHeight;
	}


}
