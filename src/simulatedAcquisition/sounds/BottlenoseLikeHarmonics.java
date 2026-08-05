package simulatedAcquisition.sounds;

public class BottlenoseLikeHarmonics extends RandomPolynomials{
	
	public BottlenoseLikeHarmonics(float fs) {
		super(true,fs);
	}

	@Override
	public String getName() {
		return "Random bottlenose-like with Harmonics";
	}

}
