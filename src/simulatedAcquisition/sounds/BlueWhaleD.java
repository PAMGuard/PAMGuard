package simulatedAcquisition.sounds;

public class BlueWhaleD extends RandomQuadratics{

	public BlueWhaleD() {
		double slope[] = {-10, -10};
		double length[] = {2, 2.5};
		double meanF[] = {48, 60};
		double meanCurve[] = {1, -1};
		setCurveR(meanCurve);
		setLengthR(length);
		setMeanR(meanF);
		setSlopeR(slope);
	}
	
	@Override
	public String getName() {
		return "Blue Whale D-like calls";
	}

}
