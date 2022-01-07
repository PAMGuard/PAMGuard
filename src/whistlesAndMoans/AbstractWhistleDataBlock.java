package whistlesAndMoans;

import PamguardMVC.AcousticDataBlock;
import PamguardMVC.PamProcess;

public abstract class AbstractWhistleDataBlock<T extends AbstractWhistleDataUnit> extends AcousticDataBlock<T> {

	public AbstractWhistleDataBlock(Class unitClass, String dataName,
			PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
	}

	private int fftLength;
	
	private int fftHop;
	
	public int getFftLength() {
		return fftLength;
	}

	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	public int getFftHop() {
		return fftHop;
	}

	public void setFftHop(int fftHop) {
		this.fftHop = fftHop;
	}
	
	public double binsToHz(double bin) {
		return bin * getSampleRate() / fftLength;
	}
	
	public double binsToSeconds(double bin) {
		return bin * fftHop / getSampleRate();
	}


}
