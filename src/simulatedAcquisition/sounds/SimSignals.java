package simulatedAcquisition.sounds;

import java.util.ArrayList;

import simulatedAcquisition.SimProcess;
import simulatedAcquisition.sounds.ClickSound.WINDOWTYPE;

/**
 * Class to list and manage different types of signals. 
 * @author Doug Gillespie
 * @see SimSignal
 *
 */
public class SimSignals  {

	private ArrayList<SimSignal> simSignalList = new ArrayList<SimSignal>();
	
	private SimProcess simProcess;

	public SimSignals(SimProcess simProcess) {
		super();
		this.simProcess = simProcess;
		simSignalList.add(new ImpulseSignal());
		simSignalList.add(new ClickSound("Porpoise", 130000, 130000, 0.07e-3, WINDOWTYPE.HANN));
		simSignalList.add(new ClickSound("Beaked Whale", 30000, 60000, 0.3e-3, WINDOWTYPE.HANN));
		simSignalList.add(new ClickSound("Click", 5000, 5000, 0.5e-3, WINDOWTYPE.DECAY));
		simSignalList.add(new ClickSound("Click", 3000, 3000, 2e-3, WINDOWTYPE.DECAY));
		simSignalList.add(new ClickSound("Chirp", 3000, 6000, 0.1, WINDOWTYPE.TAPER10));
		simSignalList.add(new ClickSound("Chirp", 3000, 8000, 0.5, WINDOWTYPE.TAPER10));
		simSignalList.add(new ClickSound("Dolphin Click", 60000, 60000, 20.e-6, WINDOWTYPE.DECAY));
		simSignalList.add(new ClickSound("Fin 20Hz", 20, 20, 0.5, WINDOWTYPE.HANN));
		simSignalList.add(new ClickSound("Fin 40Hz", 40, 40, 0.5, WINDOWTYPE.HANN));
//		simSignalList.add(new LinearChirp(simProcess.getSampleRate(), 3000, 6000, 0.1));
//		simSignalList.add(new LinearChirp(simProcess.getSampleRate(), 3000, 8000, .5));
		simSignalList.add(new RandomWhistles());
		simSignalList.add(new BranchedChirp(5000, 12000, 8000, 3000, .6));
		simSignalList.add(new RightWhales());
		simSignalList.add(new RandomMystecete());
		simSignalList.add(new BlueWhaleD());
		simSignalList.add(new WhiteNoise());
		simSignalList.add(new PinkNoise());
//		simSignalList.add(new BranchedChirp(simProcess.getSampleRate(), 3000, 8000, 12000, .3));
	}
	
	public int getNumSignals() {
		return simSignalList.size();
	}
	
	public SimSignal getSignal(int i) {
		return simSignalList.get(i);
	}
	
	public SimSignal findSignal(String name) {
		for (int i = 0; i < simSignalList.size(); i++) {
			if (simSignalList.get(i).getName().equals(name)) {
				return simSignalList.get(i);
			}
		}
		return simSignalList.get(0);
	}
	
	public SimSignal findSignal(Class signalClass) {
		for (int i = 0; i < simSignalList.size(); i++) {
			if (simSignalList.get(i).getClass() == signalClass) {
				return simSignalList.get(i);
			}
		}
		return null;
	}
	
	
}
