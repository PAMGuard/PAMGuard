package propagation;

import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import fftManager.FastFFT;

public class Absorption {
	
	private double tempC = 10;
	
	private FastFFT fastFFT = new FastFFT();

	public Absorption() {
		// TODO Auto-generated constructor stub
	}

	public double[] fftAbsorption(double[] wave, double sampleRate, double depth, double range) {
		int nFFT = FastFFT.nextBinaryExp(wave.length);
		double[] fVals = new double[nFFT/2];
		for (int i = 0; i < fVals.length; i++) {
			fVals[i] = (i+0.5) * sampleRate/nFFT;
			
		}
		double[] atten = calcAborption(fVals, getTempC(), depth);
		for (int i = 0; i < atten.length; i++) {
			atten[i] = Math.pow(10, -atten[i]*range/20);			
		}
		ComplexArray cWave = fastFFT.rfft(wave, nFFT);
		/*
		 * Now need to double it's length and attenuate 
		 */
		double[] c = Arrays.copyOf(cWave.getData(), nFFT*2);
		for (int ir = 0, ii = 1, jr = nFFT*2-2, ji = nFFT*2-1, ia = 0; 
				ia < nFFT/2; ir+=2, ii+=2, jr-=2, ji-=2, ia++) {
			c[ir] *= atten[ia];
			c[ii] *= atten[ia];
			c[jr] = c[ir];
			c[ji] = -c[ii];
		}
		cWave.setData(c); 
		// now transform back again
		fastFFT.ifft(cWave, nFFT, true);
		double[] newWave = cWave.getReal();
		return Arrays.copyOf(newWave, wave.length);
	}
	
	/**
	 * Absorption coefficient in seawater in dB/m <br>
     * After Kinsler and Frey pp. 159-160 <br>
     * Copied from Mark Johnson Matlab code
	 * @param f is frequency in Hz
	 * @param T is temperature, degrees C
	 * @param d is depth, m
	 * @return absorption in dB/m
	 */
	public double calcAborption(double f, double T, double d) {
		/*
Ta = T+273 ;
Pa = 1+d/10 ;
f1 = 1.32e3*Ta.*exp(-1700./Ta) ;
f2 = 1.55e7*Ta.*exp(-3052./Ta) ;
A = 8.95e-8*(1+2.3e-2*T-5.1e-4*T.^2) ;
B = 4.88e-7*(1+1.3e-2*T).*(1-0.9e-3*Pa) ;
C = 4.76e-13*(1-4.0e-2*T+5.9e-4*T.^2).*(1-3.8e-4*Pa) ;
a = A.*f1.*f.^2./(f1.^2+f.^2)+B.*f2.*f.^2./(f2.^2+f.^2)+C.*f.^2 
		 */
		double Ta = T+273. ;
		double Pa = 1+d/10 ;
		double f1 = 1.32e3*Ta*Math.exp(-1700./Ta) ;
		double f2 = 1.55e7*Ta*Math.exp(-3052./Ta) ;
		double Tsq = Math.pow(T, 2);
		double A = 8.95e-8*(1+2.3e-2*T-5.1e-4*Tsq) ;
		double B = 4.88e-7*(1+1.3e-2*T)*(1-0.9e-3*Pa) ;
		double C = 4.76e-13*(1-4.0e-2*T+5.9e-4*Tsq)*(1-3.8e-4*Pa) ;
		double fsq = Math.pow(f, 2);
		double f1sq = Math.pow(f1, 2);
		double f2sq = Math.pow(f2, 2);
		double a = A*f1*fsq/(f1sq+fsq)+B*f2*fsq/(f2sq+fsq)+C*fsq ;
		return a;
	}	
	
	/**
	 * Absorption coefficient in seawater in dB/m <br>
     * After Kinsler and Frey pp. 159-160 <br>
     * Copied from Mark Johnson Matlab code
	 * @param f is array of frequency values in Hz
	 * @param T is temperature, degrees C
	 * @param d is depth, m
	 * @return absorption in dB/m for each frequency
	 */
	public double[] calcAborption(double[] f, double T, double d) {
		double[] a = new double[f.length];
		for (int i = 0; i < a.length; i++) {
			a[i] = calcAborption(f[i], T, d);
		}
		return a;
	}

	/**
	 * @return the temperature in degrees C
	 */
	public double getTempC() {
		return tempC;
	}

	/**
	 * @param tempC the temperature in degrees C
	 */
	public void setTempC(double tempC) {
		this.tempC = tempC;
	}
	
	
	/*
	 * function    a = absorption(f,T,d)

%    a = absorption(f,T,d)
%     Absorption coefficient in seawater in dB/m
%     f is frequency in Hz
%     T is temperature, degrees C
%     d is depth, m
%     After Kinsler and Frey pp. 159-160
%
%     Input arguments can be scalars, or a mixture
%     of vectors and scalars as long as each argument
%     is either a vector of length nx1 or a scalar
%     for a fixed n.
%
%     mark johnson, WHOI
%     july 2009

if nargin==0,
   help absorption
   return
end

Ta = T+273 ;
Pa = 1+d/10 ;
f1 = 1.32e3*Ta.*exp(-1700./Ta) ;
f2 = 1.55e7*Ta.*exp(-3052./Ta) ;
A = 8.95e-8*(1+2.3e-2*T-5.1e-4*T.^2) ;
B = 4.88e-7*(1+1.3e-2*T).*(1-0.9e-3*Pa) ;
C = 4.76e-13*(1-4.0e-2*T+5.9e-4*T.^2).*(1-3.8e-4*Pa) ;

a = A.*f1.*f.^2./(f1.^2+f.^2)+B.*f2.*f.^2./(f2.^2+f.^2)+C.*f.^2 ;

	 */

}
