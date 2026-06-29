package simulatedAcquisition.sounds;

public class BottlenoseLike extends RandomPolynomials{
	
	public BottlenoseLike(float sampleRate) {
		super(false,sampleRate);
	}

	@Override
	public String getName() {
		return "Random bottlenose-like without Harmonics";
	}

}
