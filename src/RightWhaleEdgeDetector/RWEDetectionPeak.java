package RightWhaleEdgeDetector;

/**
 * Temporary holder of data about spectral peaks. 
 * @author Doug Gillespie
 *
 */
public class RWEDetectionPeak {

	public RWEDetectionPeak(int bin1) {
		this.bin1 = this.bin2 = bin1;
	}

	int bin1, bin2;
	int peakBin;
	double maxAmp;
	double signal, noise;
	RWESound rweSound; // link back to a sound as regions grow. 
}
