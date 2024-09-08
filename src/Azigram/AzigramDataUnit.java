package Azigram;

import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import fftManager.FFTDataUnit;

/**
 * Just a dirty hack for testing. This AzigramDataUnit is just an FFTDataUnit, 
 * But override the getMagnitude() function to run the demux and Azigram
 * routines.
 * @author brian_mil
 *
 */
public class AzigramDataUnit extends FFTDataUnit {

	ComplexArray P;
	double[] vx, vy, f, directionalData;
	private double[] directionalMagnitude;
	
	public AzigramDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration,
			ComplexArray fftData, int fftSlice) {
		super(timeMilliseconds, channelBitmap, startSample, duration, fftData, fftSlice);
	}

	@Override
	public double[] getMagnitudeData() {
		double[] magSqData = super.getMagnitudeData();
		
		return Arrays.copyOf(magSqData, getP().length());
	
	}
	
	public ComplexArray getP() {
		return P;
	}

	public void setP(ComplexArray p) {
		P = p;
	}

	public double[] getVx() {
		return vx;
	}

	public void setVx(double[] vx) {
		this.vx = vx;
	}

	public double[] getVy() {
		return vy;
	}

	public void setVy(double[] vy) {
		this.vy = vy;
	}

	public double[] getF() {
		return f;
	}

	public void setF(double[] f) {
		this.f = f;
	}

	public double[] getDirectionalAngle() {
		return directionalData;
	}

	public void setDirectionalAngle(double[] mu) {
		directionalData = mu;
	}
	
	@Override
	public double[] getSpectrogramData() {
		return getDirectionalAngle();
	}
	
	/**
	 * Placeholder in case we want an Azigram with transparency linked to 
	 * signal amplitude.
	 * @return
	 */
			
	public double[] getSpectrogramAlpha() {
		return getDirectionalMagnitude();
	}

	public void setDirectionalMagnitude(double[] mag) {
		directionalMagnitude = mag;
	}
	
	public double[] getDirectionalMagnitude() {
		return directionalMagnitude;
	}
}
