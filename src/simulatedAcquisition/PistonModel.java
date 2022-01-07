package simulatedAcquisition;

/**
 * Calculations associated with a piston model. 
 * @author Doug Gillespie
 *
 */
public class PistonModel {
	
	/**
	 * Calculate the impulse response for a sound emitted from a baffled
	 * piston of radius a at angle theta. <br>
	 * This function returns IR's of the same length regardless of angle
	 * so that timing offset is the same whatever the length of the function. 
	 * @param a radius of piston in metres
	 * @param theta angle in radians. 
	 * @param sr sample rate
	 * @param c speed of sound
	 * @param binOFfset offset from integer sample of the first bin.  
	 * @return impulse response. 
	 */
	static public double[] getImpulseResponse(double a, double theta, double sr, double c, double binOffset) {
		double R = a * 1000;
		double r = R*Math.sin(theta);
		double z = R*Math.cos(theta);
		int nBins = (int) Math.ceil(2*a*sr/c);
		if (nBins%2 == 0) {
			// ensure there is a central bin. 
			nBins++;
		}
		int midBin = nBins/2;
		double[] g = new double[nBins];
		if (r <= a) {
			g[midBin] = 1;
			return g;
		}
		// for now just make a symetric function. 
		double totVals = 0.;
		double t0 = R/c;
		double t1 =  t0 - Math.sqrt(z*z+(a-r)*(a-r))/c;
		double t2 =  Math.sqrt(z*z+(a+r)*(a+r))/c - t0;
		int n1 = (int) Math.floor(t1*sr);
		int n2 = (int) Math.floor(t2*sr);
		if (-n1 == n2) {
			g[midBin] = 1;
			return g;
		}
		for (int i = -n1; i <= n2; i++) {
			double t = t0+(i+binOffset)/sr;
			double tmp = (c*c*t*t - z*z + r*r -a*a)/(2*r*Math.sqrt(c*c*t*t-z*z));
			// with bin offset, there is tiny chance it will go beyond edge of 
			// function, so only include plausible values. 
			if (Math.abs(tmp) <= 1.0) {
				g[midBin+i] = Math.acos(tmp);
				totVals += g[midBin+i];
			}
		}

		for (int i = -n1; i <= n2; i++) {
			g[midBin+i] /= totVals;
		}
		
		return g;
		
	}
	
	/**
	 * Convolve the impulse response with the wave. <br>
	 * Note that the IR may have a lot of zeros, so be efficient !
	 * @param wave input waveform
	 * @param ir impulse response
	 * @return array of length (length(wave) + length(ir) -1)
	 */
	static public double[] conv(double[] wave, double[] ir) {
		int nW = wave.length;
		int nR = ir.length;
		double[] newWave = new double[nW+nR-1];
		for (int iR = 0; iR < nR; iR++) {
			if (ir[iR] == 0) {
				continue;
			}
			for (int iW = 0; iW < nW; iW++) {
				newWave[iR+iW] += wave[iW]*ir[iR];
			}
		}
		return newWave;
	}
}
