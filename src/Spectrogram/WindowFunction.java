/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Spectrogram;

/**
 * Window function for use with FFT. 
 * 
 * @author Douglas Gillespie
 *
 */
public class WindowFunction {
	
	static public final int RECTANGULAR = 0;
	static public final int HAMMING = 1;
	static public final int HANNING = 2;
	static public final int BARTLETT = 3;
	static public final int BLACKMAN = 4;
	static public final int BLACKMANHARRIS = 5;
	
	static public final int NWINDOWS = 6;
	
	private static String[] names;
	public static String[] getNames() {
		if (names == null) {
			names = new String[NWINDOWS];
			names[0] = new String("Rectangular");
			names[1] = new String("Hamming");
			names[2] = new String("Hann"); // fixed 7/4/13 since it's Hann, not Hanning and has just been pointed out. 
			names[3] = new String("Bartlett (Triangular)");
			names[4] = new String("Blackman");
			names[5] = new String("Blackman-Harris");
		}
		return names;
	}
	
	public static double[] getWindowFunc(int windowType, int length) {
		switch(windowType) {
		case RECTANGULAR:
			return rectangular(length);
		case HAMMING:
			return hamming(length);
		case HANNING:
			return hann(length);
		case BARTLETT:
			return bartlett(length);
		case BLACKMAN:
			return blackman(length);
		case BLACKMANHARRIS:
			return blackmanharris(length);
		default:
			return rectangular(length);				
		}
	}
	
	public static double[] hamming(int length) {
		double[] window = new double[length];

		for (int i = 0; i < length; i++) {
			window[i] = (0.54 - 0.46 * Math.cos(2.0 * Math.PI
					* ((double) (i) / (double) (length - 1))));
		}
		return (window);
	}
	
	public static double[] hann(int length) {
		double[] window = new double[length];

		for (int i = 0; i < length; i++) {
			window[i] = (0.5 - 0.5 * Math.cos(2.0 * Math.PI
					* ((double) (i) / (double) (length - 1))));
		}
		return (window);
	}

	public static double[] rectangular(int length) {
		double[] window = new double[length];

		for (int i = 0; i < length; i++) {
			window[i] = 1;
		}
		return (window);
	}
	
	// Bartlett (triangular)
	public static double[] bartlett(int length) {
		double[] window = new double[length];

		double a = 2.0/(length - 1);
		int i;
		for (i = 0; i < (length-1)/2; i++) {
			window[i] = i * a;
		}
		for (i = (length-1)/2; i < length; i++) {
			window[i] = 2 - a * i;
		}
		return (window);
	}
	
	// Blackman
	public static double[] blackman(int length) {
		double[] window = new double[length];

		double arg = 2. * Math.PI / (length-1);
		for (int i = 0; i < length; i++) {
			window[i] = 0.42 - 0.5 * Math.cos(arg * i) + 0.08 * Math.cos(2 * arg * i);
		}
		return (window);
	}
	// Blackman-Harris
	public static double[] blackmanharris(int length) {
		double[] window = new double[length];

		double arg = 2. * Math.PI / (length-1);
		for (int i = 0; i < length; i++) {
			window[i] = 0.35875 - 0.48829 * Math.cos(arg * i) + 0.14128 * Math.cos(2 * arg * i)
			- 0.01168 * Math.cos(3 * arg * i);
		}
		return (window);
	}
	
	/**
	 * also known as tapered-cosine, for a=0 becomes rectangular and a=1 becomes hann
	 * @param length
	 * @return
	 */
	public static double[] tukey(int length,double a) {
		double[] window = new double[length];
		for (int i = 0; i < length; i++) {
			
			if (i<=a*(length-1)/2){
				window[length-i]=window[i]=0.5*(   1+    Math.cos(Math.PI*   (    (     (2*i)/(a*(length-1))   )-1    )   )   );
			}else{
				window[i]=1;
			}
			
		}
		
		return window;
	}

	/**
	 * Calculate the gain of the window function
	 * @param windowFunction window function array
	 * @return window rms gain as a factor (NOT dB); 
	 */
	public static double getWindowGain(double[] windowFunction) {
		int n = windowFunction.length;
		double tot = 0;
		for (int i = 0; i < n; i++) {
			tot += windowFunction[i]*windowFunction[i];
		}
		return Math.sqrt(tot/n);
	}
	
	/*
	 * C versions
// Hamming
void ham(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;
  double ham,factor;

	if (n > l) n = l;

  factor = 8.0*atan(1.0)/(n-1);
  for (i = 0 ; i < n ; i++){
	 ham = 0.54 - 0.46*cos(factor*i);

    if (xl) *(xl++) = (float) (input->dat[0]) * ham;
    if (xr) *(xr++) = (float) (input->dat[1]) * ham;
	 input++;
	}
	for ( ; i < l; i++){
     if (xl) *(xl++) = 0;
     if (xr) *(xr++) = 0;
	}
}

// Hanning
void han(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;
  double factor,han;

  if (n > l) n = l;

  factor = 8.0*atan(1.0)/(n-1);
  for (i = 0 ; i < n ; i++){
	 han = 0.5 - 0.5*cos(factor*i);

    if (xl) *(xl++) = (float) (input->dat[0]) * han;
    if (xr) *(xr++) = (float) (input->dat[1]) * han;
    input++;
	}
	for ( ; i < l; i++){
     if (xl) *(xl++) = 0;
     if (xr) *(xr++) = 0;
	}
}

// triangular
void triang(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;
  double tri,a;
  a = 2.0/(n-1);

  if (n > l) n = l;

  for (i = 0 ; i <= (n-1)/2 ; i++) {
	 tri = i*a;

    if (xl) *(xl++) = (float) (input->dat[0]) * tri;
    if (xr) *(xr++) = (float) (input->dat[1]) * tri;
    input++;
  }
  for ( ; i < n ; i++) {
	 tri = 2.0 - i*a;

    if(xl) *(xl++) = (float) (input->dat[0]) * tri;
    if(xr) *(xr++) = (float) (input->dat[1]) * tri;
	 input++;
  }
  for ( ; i < l; i++){
    if (xl) *(xl++) = 0;
    if (xr) *(xr++) = 0;
  }
}


// Blackman black - Blackman window


void black(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;
  double black,factor;

  if (n > l) n = l;

  factor = 8.0*atan(1.0)/(n-1);
  for (i=0; i<n; ++i){
	 black = 0.42 - 0.5*cos(factor*i) + 0.08*cos(2*factor*i);

    if(xl) *(xl++) = (float) (input->dat[0]) * black;
    if(xr) *(xr++) = (float) (input->dat[1]) * black;
    input++;
	}
	for ( ; i < l; i++){
     if (xl) *(xl++) = 0;
     if (xr) *(xr++) = 0;
	}
}
// Blackman Harris
void harris(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;
  double harris,factor,arg;

  if (n > l) n = l;

  factor = 8.0*atan(1.0)/n;
  for (i=0; i<n; ++i){
    arg = factor * i;
    harris = 0.35875 - 0.48829*cos(arg) + 0.14128*cos(2*arg)
               - 0.01168*cos(3*arg);

    if(xl) *(xl++) = (float) (input->dat[0]) * harris;
    if(xr) *(xr++) = (float) (input->dat[1]) * harris;
    input++;
  }
  for ( ; i < l; i++){
    if (xl) *(xl++) = 0;
    if (xr) *(xr++) = 0;
  }
}
// rectangle

 //  no windowing


void rectngl(LRData *input, float *xl, float *xr, int n, int l)
{
  int    i;

  if (n > l) n = l;

  for (i=0; i<n; ++i){
    if (xl)	*(xl++) = (float) (input->dat[0]);
    if (xr) *(xr++) = (float) (input->dat[1]);
	 input++;
  }
  for ( ; i < l; i++){
    if (xl) *(xl++) = 0;
    if (xr) *(xr++) = 0;
  }
}
	 */
}

