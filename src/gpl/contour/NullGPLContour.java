package gpl.contour;

/**
 * slightly fudged contour which doesnt' have any points for situations where 
 * no contour was found within a GPL detection. 
 * @author dg50
 *
 */
public class NullGPLContour extends GPLContour {

	public NullGPLContour(double totalNoise, double totalSignal, int minTBin, int maxTBin, int minFBin, int maxFBin) {
		setTotalExcess(totalNoise);
		setTotalEnergy(totalSignal);
		setMinTBin(minTBin);
		setMaxTBin(maxTBin);
		setMinFBin(minFBin);
		setMaxFBin(maxFBin);
	}

}
