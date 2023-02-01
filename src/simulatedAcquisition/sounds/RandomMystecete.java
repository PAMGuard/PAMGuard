package simulatedAcquisition.sounds;

public class RandomMystecete extends RandomQuadratics {

	public RandomMystecete() {
		super();
		double slope[] = {-150, 150};
		double length[] = {.4, 1};
		double meanF[] = {200, 800};
		double meanCurve[] = {-300, 300};
		setCurveR(meanCurve);
		setLengthR(length);
		setMeanR(meanF);
		setSlopeR(slope);
		
	}

	@Override
	public String getName() {
		return "Random baleen whale (humpbackish)";
	}

}
