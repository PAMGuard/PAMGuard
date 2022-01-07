package simulatedAcquisition.sounds;

public class RightWhales extends RandomQuadratics {

	public RightWhales() {
		super();
		double slope[] = {50, 150};
		double length[] = {.4, 1};
		double meanF[] = {90, 130};
		double meanCurve[] = {30, 300};
		setCurveR(meanCurve);
		setLengthR(length);
		setMeanR(meanF);
		setSlopeR(slope);
		
	}

	@Override
	public String getName() {
		return "Right Whale like calls";
	}

}
