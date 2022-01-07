package simulatedAcquisition.sounds;


public class ImpulseSignal extends SimSignal {

	private double[] sig;
	
	public ImpulseSignal() {
		super();
		sig = new double[5];
		for (int i = 0; i < sig.length; i++){
			sig[i] = 1;	
		}
	}

	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		return sig;
	}
	
	@Override
	public String getName() {
		return "Impulse";
	}
	

}
