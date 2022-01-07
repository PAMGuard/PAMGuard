package PamUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

/**
 * Static classes to perform interpolation tasks
 * <p>
 * Original functions from: <p>
 * Linear interpolation: http://www.java2s.com/Code/Java/Collections-Data-Structure/LinearInterpolation.htm
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class PamInterp {


	/**
	 * Linear interpolation
	 * @param x - the original x value array
	 * @param y - the original y value array
	 * @param xi - new x values to find y for. 
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final double[] interpLinear(double[] x, double[] y, double[] xi) throws IllegalArgumentException {

		if (x.length != y.length) {
			throw new IllegalArgumentException("X and Y must be the same length");
		}
		if (x.length == 1) {
			throw new IllegalArgumentException("X must contain more than one value");
		}
		double[] dx = new double[x.length - 1];
		double[] dy = new double[x.length - 1];
		double[] slope = new double[x.length - 1];
		double[] intercept = new double[x.length - 1];

		// Calculate the line equation (i.e. slope and intercept) between each point
		for (int i = 0; i < x.length - 1; i++) {
			dx[i] = x[i + 1] - x[i];
			if (dx[i] == 0) {
				throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
			}
			if (dx[i] < 0) {
				throw new IllegalArgumentException("X must be sorted");
			}
			dy[i] = y[i + 1] - y[i];
			slope[i] = dy[i] / dx[i];
			intercept[i] = y[i] - x[i] * slope[i];
		}

		// Perform the interpolation here
		double[] yi = new double[xi.length];
		for (int i = 0; i < xi.length-1; i++) {
			if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
				yi[i] = Double.NaN;
			}
			else {
				int loc = Arrays.binarySearch(x, xi[i]);
				if (loc < -1) {
					loc = -loc - 2;
					yi[i] = slope[loc] * xi[i] + intercept[loc];
				}
				else {
					yi[i] = y[loc];
				}
			}
		}


		return yi;
	}

	/**
	 * Linear interpolation
	 * @param x - the original x value array
	 * @param y - the original y value array
	 * @param xi - new x values to find y for. 
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final BigDecimal[] interpLinear(BigDecimal[] x, BigDecimal[] y, BigDecimal[] xi) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("X and Y must be the same length");
		}
		if (x.length == 1) {
			throw new IllegalArgumentException("X must contain more than one value");
		}
		BigDecimal[] dx = new BigDecimal[x.length - 1];
		BigDecimal[] dy = new BigDecimal[x.length - 1];
		BigDecimal[] slope = new BigDecimal[x.length - 1];
		BigDecimal[] intercept = new BigDecimal[x.length - 1];

		// Calculate the line equation (i.e. slope and intercept) between each point
		BigInteger zero = new BigInteger("0");
		BigDecimal minusOne = new BigDecimal(-1);

		for (int i = 0; i < x.length - 1; i++) {
			//dx[i] = x[i + 1] - x[i];
			dx[i] = x[i + 1].subtract(x[i]);
			if (dx[i].equals(new BigDecimal(zero, dx[i].scale()))) {
				throw new IllegalArgumentException("X must be montotonic. A duplicate " + "x-value was found");
			}
			if (dx[i].signum() < 0) {
				throw new IllegalArgumentException("X must be sorted");
			}
			//dy[i] = y[i + 1] - y[i];
			dy[i] = y[i + 1].subtract(y[i]);
			//slope[i] = dy[i] / dx[i];
			slope[i] = dy[i].divide(dx[i]);
			//intercept[i] = y[i] - x[i] * slope[i];
			intercept[i] = x[i].multiply(slope[i]).subtract(y[i]).multiply(minusOne);
			//intercept[i] = y[i].subtract(x[i]).multiply(slope[i]);
		}

		// Perform the interpolation here
		BigDecimal[] yi = new BigDecimal[xi.length];
		for (int i = 0; i < xi.length; i++) {
			//if ((xi[i] > x[x.length - 1]) || (xi[i] < x[0])) {
			if (xi[i].compareTo(x[x.length - 1]) > 0 || xi[i].compareTo(x[0]) < 0) {
				yi[i] = null; // same as NaN
			}
			else {
				int loc = Arrays.binarySearch(x, xi[i]);
				if (loc < -1) {
					loc = -loc - 2;
					//yi[i] = slope[loc] * xi[i] + intercept[loc];
					yi[i] = slope[loc].multiply(xi[i]).add(intercept[loc]);
				}
				else {
					
					yi[i] = y[loc];
				}
			}
		}

		return yi;
	}

	/**
	 * Linear interpolation
	 * @param x - the original x value array
	 * @param y - the original y value array
	 * @param xi - new x values to find y for. 
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static final double[] interpLinear(long[] x, double[] y, long[] xi) throws IllegalArgumentException {

		double[] xd = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			xd[i] = (double) x[i];
		}

		double[] xid = new double[xi.length];
		for (int i = 0; i < xi.length; i++) {
			xid[i] = (double) xi[i];
		}

		return interpLinear(xd, y, xid);
	}

	/**
	 * Interpolate a waveform in the frequency domain in order to preserve frequency
	 * content and prevent introduction of high frequency artifacts.
	 * 
	 * @param waveform
	 *            - the waveform to interpolate.
	 * @param ratio
	 *            - the new waveform sample rate ration. For example to up sample
	 *            from 192kHz to 288kHz then ratio=288/192. 
	 * @return the interpolated waveform.
	 */
	public static final double[] interpWaveform(double[] waveform, double ratio){
		FastFFT fastFFt = new FastFFT(); 
		
		//calc fft of the waveform. 
		ComplexArray fftWav= fastFFt.rfft(waveform, waveform.length); 
		
		//first create a complex array which is the correct size. 
		int interpL=(int) Math.floor(waveform.length*ratio); 
		ComplexArray fftWavInterp = new ComplexArray(interpL); 
	
		//insert new results into new array. 
		for (int i=0; i<fftWav.length(); i++) {
			fftWavInterp.setReal(i, ratio*fftWav.getReal(i));
			fftWavInterp.setImag(i, ratio*fftWav.getImag(i));
		}
		
		fastFFt.ifft(fftWavInterp, interpL, false);
		
		double[] outWaveform = new double[fftWavInterp.length()]; 
		for (int i=0; i<fftWavInterp.length(); i++) {
			outWaveform[i] = fftWavInterp.getReal(i); 
		}

		return outWaveform; 
		
//		function [o]=interpolate2(in,N)
//				L=size(in,1);
//				IN=fft(in);
//				IN(round(L/2)+1:floor(N*L),:)=0;
//				if N<1,IN(round(N*L/2)+1:L,:)=0;end;
//				IN=N*IN(1:floor(N*L),:);
//				IN(1,:)=IN(1,:)/2;
//				o=2*real(ifft(IN));

	
	}
}

