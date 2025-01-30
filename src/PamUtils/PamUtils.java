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
package PamUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import PamguardMVC.PamConstants;

/**
 * 
 * @author Doug Gillespie
 * 
 */
public class PamUtils {

	/**
	 * 
	 * check that the bits represent a single channel and return the number of
	 * that channel
	 * 
	 * @param channelMap
	 *            bitmap for multiple channels
	 * @return singel channel (or -1 if >1 or zero channels)
	 */
	public static int getSingleChannel(int channelMap) {
		int channels = 0;
		int singleChan = -1;
		for (int i = 0; i < 32; i++) {
			if ((1 << i & channelMap) != 0) {
				singleChan = i;
				channels++;
			}
		}
		
		/*
		 * Looks like this has been returning 1 instead of -1 for many years. 
		 * May need to revert to this behaviour if modules have come to depend
		 * on the incorrect functionality... BSM 2018-07-16
		 */
		if (channels > 1)
			return -1; 
		
		return singleChan;
	}

	/**
	 * Get the number of channels present in this bitmap. 
	 * (calls Integer.bitCount(...))
	 * @param channelBitmap channel map
	 * @return number of bits set.
	 */
	public static int getNumChannels(int channelBitmap) {
		return Integer.bitCount(channelBitmap);
	}
	
	/**
	 * Works out the index of a particular channel in a channel list - often,
	 * if channelBitmap is a set of continuous channels starting with 0, then
	 * the channel pos is the same as the single channel number. However, if there
	 * are gaps in the channelBitmap, then the channel pos will be < than the 
	 * channel Number.
	 * @param singleChannel single channel number (index, not map)
	 * @param channelBitmap
	 * @return the channel position in the channel list or -1 if it isn't available
	 */
	public static int getChannelPos(int singleChannel, int channelBitmap) {
		
		int channelPos = 0;
		
		if ((1<<singleChannel & channelBitmap) == 0) return -1;
		
		for (int i = 0; i < singleChannel; i++) {
			if ((1<<i & channelBitmap) != 0) {
				channelPos++;
			}
		}
		
		return channelPos;
	}
	
	/**
	 * 		
	 * Get's the number of the nth used channel in a bitmap. e.g. if the 
	 * channelBitmap were 0xc (1100), then the 0th channel would be 2 and the 
	 * 1st channel would be 3.
	 * @param singleChannel nth channel in a list 
	 * @param channelBitmap bitmap of all used channels. 
	 * @return true channel number or -1 if the channel is not in the map. 
	 */
	public static int getNthChannel(int singleChannel, int channelBitmap) {
		/*
		 * get's the number of the nth used channel in a bitmap. e.g. if the 
		 * channelBitmap were 0xc (1100), then the 0th channel would be 2 and the 
		 * 1st channel would be 3.
		 */
		int countedChannels = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((channelBitmap & (1<<i)) != 0) {
				if (++countedChannels > singleChannel) return i;
			}
		}
		return -1;
	}
	
	/**
	 * Create a LUT which allows conversion of absolute to relative channel numbers
	 * @param channelBitmap
	 * @return
	 */
	public static int[] getChannelPositionLUT(int channelBitmap) {
//		int[] lut = new int[getHighestChannel(channelBitmap) + 1];
//		int pos = -1;
//		for (int i = 0; i <= getHighestChannel(channelBitmap); i++) {
//			pos = getChannelPos(i, channelBitmap);
//			lut[i] = pos;
//		}
		int[] lut = new int[PamConstants.MAX_CHANNELS];
		int pos = -1;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			pos = getChannelPos(i, channelBitmap);
			lut[i] = pos;
		}
		return lut;
	}
	/**
	 * Get the highest channel number in a channel map. 
	 * @param channelBitmap
	 * @return the last channel in the channel bitmap
	 */
	public static int getHighestChannel(int channelBitmap) {
		int highestChan = -1;
		for (int i = 0; i < 32; i++) {
			if ((1 << i & channelBitmap) != 0) {
				highestChan = i;
			}
		}
		return highestChan;
	}
	
	/**
	 * Get the lowest channel number in a channel map. <p>
	 * Returns -1 if the channel map is empty. 
	 * @param channelBitmap
	 * @return the first channel in the channel bitmap
	 */
	public static int getLowestChannel(int channelBitmap) {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & channelBitmap) != 0) return i;
		}
		return -1;
	}

	/**
	 * Make a simple bitmap for nChannels of data, 
	 * i.e. <p>
	 * if nChannels = 1, map = 1 <p>
	 * if nChannels = 2, map = 3 <p>
	 * if nChannels = 3, map = 7 <p>
	 * if nChannels = 4, map = 15 <p>
	 * etc.
	 * @param nChannels
	 * @return a bitmap representing the set channels 
	 */
	public static int makeChannelMap(int nChannels) {
		int map = 0;
		for (int i = 0; i < nChannels; i++) {
			map += 1 << i;
		}
		return map;
	}
	
	/**
	 * Make a channel map from a list
	 * @param nChannels number of elements in the list
	 * @param channelList list 
	 * @return channel bitmap
	 * @deprecated
	 */
	@Deprecated
	public static int makeChannelMap(int nChannels, int[] channelList) { //Xiao Yan Deng
		/*
		 * Is getting called with channelList = null in which case
		 * just do the old way D.G.
		 */
		if (channelList == null) {
			return makeChannelMap(nChannels);
		}
		int map = 0;
		for (int i = 0; i < nChannels; i++) {
			map += 1 << channelList[i];
		}
//		System.out.println("PamUtils.java->makeChannelMap:: map" + map);
		return map;
	}
	
	/**
	 * Make a channel bitmap from a list. 
	 * @param channelList channel list
	 * @return bitmap of channels. 
	 */
	public static int makeChannelMap(int[] channelList) {
		int map = 0;
		for (int i = 0; i < channelList.length; i++) {
			map += 1 << channelList[i];
		}
		return map;
	}

	public static int SetBit(int bitMap, int bitNumber, int bitValue) {
		return SetBit(bitMap, bitNumber, bitValue == 1);
	}

	public static int SetBit(int bitMap, int bitNumber, boolean bitSet) {
		if (bitSet) {
			return (bitMap |= 1 << bitNumber);
		}
		return (bitMap &= ~(1 << bitNumber));
	}

	/**
	 * Get the next power of 2 size >= len
	 * <br> note that this function will fail if len > 2^30
	 * @param len input length
	 * @return a power of 2 greater than or equal to the input 
	 */
	public static int getMinFftLength(long len) {
		int fftLength = 4;
		while (fftLength < len) {
			fftLength *= 2;
		}
		return fftLength;
	}

	/**
	 * Gets the next integer log of 2 which will
	 * give 2^ans >= n, i.e. the minimum length
	 * required to FFT these data.
	 * @param n length
	 * @return log2 of mnimum data required to fit n. 
	 */
	public static int log2(int n) {

		int log2FFTLen = 2;
		int dum = 4;
		while (dum < n) {
			dum *= 2;
			log2FFTLen++;
		}
		return log2FFTLen;
	}

	
	/** Linear interpolation. Define f() as the linear mapping from the
	 * domain [x0,x1] to the range [y0,y1]. Return y=f(x) according to
	 * this mapping.
	 * <p>author Dave Mellinger
	 */
	public static double linterp(double x0,double x1,double y0,double y1,double x) {
		return (x - x0) / (x1 - x0) * (y1 - y0) + y0;
	}
	

	/**
	 * Create a string of channel numbers in the channel 
	 * map separated by commas. 
	 * @param channelMap channel map
	 * @return string of channel numbers. 
	 */
	static public String getChannelList(int channelMap) {
		String str = null;
		if (channelMap == 0) return "";
		for (int iBit = 0; iBit < PamConstants.MAX_CHANNELS; iBit++) {
			if ((1<<iBit & channelMap) != 0) {
				if (str == null) {
					str = String.format("%d", iBit);
				}
				else {
					str += String.format(", %d", iBit);
				}
			}
		}
		return str;
	}
	
	/**
	 * Turn a bitmap into an array of channel numbers. 
	 * @param channelMap channel map
	 * @return channel list array
	 */
	public static int[] getChannelArray(int channelMap) {
		int nChan = getNumChannels(channelMap);
//		if (nChan <= 0) { // better to return an empty array to avoid null pointer exceptions. 
//			return null;
//		}
		int[] channels = new int[nChan];
		for (int i = 0; i < nChan; i++) {
			channels[i] = getNthChannel(i, channelMap);
		}
		return channels;
	}
	
	/**
	 * Check whether a channel is present in a channel bitmap. 
	 * @param channelBitMap - the channel bitmap to search
	 * @param channel - the channel
	 * @return true if the bitmap contains the channel
	 */
	public static boolean hasChannel(int channelBitMap, int channel) {
		int[] channels=getChannelArray(channelBitMap);
		if (Arrays.binarySearch(channels, 0, channels.length, channel)<0) return false; 
		else return true; 
	}
	
	
	/**
	 * Check whether a channel bitmap contains any other channels from another bitmap. 
	 * @param channelMap1 - the channel bitmap to check
	 * @param channelMap2 - the channel bitmap that contains at least one channel from chanelMpa1 to return true. 
	 * @return true of channelMap1 contains at least one channels from channelMap2. 
	 */
	public static boolean hasChannelMap(int channelMap1, int channelMap2) {
		int[] channels =  getChannelArray(channelMap2); 
		for (int i=0; i<channels.length; i++) {
			if (hasChannel(channelMap1,  channels[i])) return true; 
		}
		return false; 
	}

	

	 /**
	  * Force an angle to sit 0<= angle < 360.
	  * @param angle input angle (degrees) 
	  * @return output angle (degrees)
	  */
	static public double constrainedAngle(double angle) {
		if (Double.isInfinite(angle) || Double.isNaN(angle)) {
			return angle;
		}
		while (angle >= 360) {
			angle -= 360;
		}
		while (angle < 0) {
			angle += 360;
		}
		return angle;
	}
	
	 /**
	  * Force an angle to sit 0<= angle < 2PI.
	  * @param angle input angle (radians) 
	  * @return output angle (radians)
	  */
	static public double constrainedAngleR(double angle) {
		if (Double.isInfinite(angle) || Double.isNaN(angle)) {
			return angle;
		}
		while (angle >= 2*Math.PI) {
			angle -= 2*Math.PI;
		}
		while (angle < 0) {
			angle += 2*Math.PI;
		}
		return angle;
	}
	 /**
	  * Force an angle to sit within some range. 
	  * @param angle input angle (degrees) 
	  * @param maxAngle maximum angle in degrees
	  * @return output angle (degrees)
	  */
	static public double constrainedAngle(double angle, double maxAngle) {
		if (Double.isInfinite(angle) || Double.isNaN(angle)) {
			return angle;
		}
		while (angle >= maxAngle) {
			angle -= 360;
		}
		while (angle < (maxAngle - 360)) {
			angle += 360;
		}
		return angle;
	}
	
	
	 /**
	  * Force an angle to sit within some range. 
	  * @param angle input angle (radians) 
	  * @param maxAngle maximum angle in radians
	  * @return output angle (radians)
	  */
	static public double constrainedAngleR(double angle, double maxAngle) {
		if (Double.isInfinite(angle) || Double.isNaN(angle)) {
			return angle;
		}
		while (angle > maxAngle) {
			angle -= 2*Math.PI;
		}
		while (angle <= (maxAngle - 2*Math.PI)) {
			angle += 2*Math.PI;
		}
		return angle;
	}
	
	
	/**
	 * Find the minimum angle difference between two angles. 
	 * @param angle1 - the firs tangle (0-360 degrees)
	 * @param angle2 - the firs tangle (0-360 degrees)
	 * @return the min angle in DEGREES
	 */
	static public double minAngled(double angle1, double angle2){
		double a = angle1 - angle2;
		a = (a + 180) % 360 - 180;
		return a; 
	}
	
	
	/**
	 * Find the minimum angle difference between two angles. 
	 * @param angle1 - the firs tangle (0-2pi RADIANS)
	 * @param angle2 - the firs tangle (0-2pi RADIANS)
	 * @return the min angle in RADIANS
	 */
	static public double minAngler(double angle1, double angle2){
		double a = angle1 - angle2;
		while (a > Math.PI) {
			a -= (2*Math.PI);
		}
		while (a < -Math.PI) {
			a += (2*Math.PI);
		}
//		a = (a + Math.PI) % 2*Math.PI - Math.PI;
		return a; 
	}
	
	
	 /**
	  * Force a number to sit between two values.
	  * @param number - input numbers
	  * @param maxNumber - the maximum Number 
	  * @return output number.
	  */
	static public double constrainedNumber(double number, double maxNumber) {
		while (number > maxNumber) {
			number -= maxNumber;
		}
		while (number < 0) {
			number += maxNumber;
		}
		return number;
	}
	
	/**
	 * round a number to the closest step size 
	 * @param number number to round
	 * @param step rounding step (e.g. 1, 100, 0.5, etc) 
	 * @return rounded number
	 */
	public static double roundNumber(double number, double step) {
		return Math.round(number / step) * step;
	}
	
//	public static double roundDownDecadal(double number) {
//		if (number <= 0) {
//			return number;
//		}
//		
//	}

	/**
	 * round a number up to the closest step size 
	 * @param number number to round
	 * @param step rounding step (e.g. 1, 100, 0.5, etc) 
	 * @return rounded number
	 */
	public static double roundNumberUp(double number, double step) {
		return Math.ceil(number / step) * step;
	}

	/**
	 * round a number down to the closest step size 
	 * @param number number to round
	 * @param step rounding step (e.g. 1, 100, 0.5, etc) 
	 * @return rounded number
	 */
	public static double roundNumberDown(double number, double step) {
		return Math.floor(number / step) * step;
	}
	
	/**
	 * Round a number up, maintining a set number of decimal places. 
	 * @param number number to round
	 * @param nDP number of decimal places
	 * @return rounded number
	 */
	public static double roundNumberUpP(double number, int nDP) {
		double ln = Math.log10(number);
		double step = Math.floor(ln) - nDP + 1;
		return roundNumberUp(number, Math.pow(10., step));
	}
	
	/**
	 * Return the average ot two double numbers, either or both of which may be null 
	 * @param d1 number 1
	 * @param d2 number 2
	 * @return the average (or just one if the other is null). 
	 */
	public static Double doubleAverage(Double d1, Double d2) {
		if (d1 == null) {
			return d2;
		}
		if (d2 == null) {
			return d1;
		}
		return (d1+d2)/2.;
	}
	
	/**
	 * Get the average of two angles in degrees. Uses trig so that 
	 * the average of 359 and 1 will be 0, no t180. 
	 * @param d1 angle 1
	 * @param d2 angle 2
	 * @return average
	 */
	public static Double angleAverageDegrees(Double d1, Double d2) {
		if (d1 == null) {
			return d2;
		}
		if (d2 == null) {
			return d1;
		}
		double x = (Math.cos(Math.toRadians(d1)) + Math.cos(Math.toRadians(d2)))/2.;
		double y = (Math.sin(Math.toRadians(d1)) + Math.sin(Math.toRadians(d2)))/2.;
		return Math.toDegrees(Math.atan2(y, x));
	}
	/**
	 * Get the weighted average of two angles in degrees. Uses trig so that 
	 * the average of 359 and 1 will be 0, no t180. 
	 * @param d1 angle 1
	 * @param d2 angle 2
	 * @param w1 weight for angle 1
	 * @param w2 weight for angle 2;
	 * @return weighted average
	 */
	public static Double angleAverageDegrees(Double d1, Double d2, double w1, double w2) {
		if (d1 == null) {
			return d2;
		}
		if (d2 == null) {
			return d1;
		}
		double wTot = w1+w2;
		double x = (Math.cos(Math.toRadians(d1))*w1 + Math.cos(Math.toRadians(d2))*w2)/wTot;
		double y = (Math.sin(Math.toRadians(d1))*w1 + Math.sin(Math.toRadians(d2))*w2)/wTot;
		return Math.toDegrees(Math.atan2(y, x));
	}
	
	/**
	 * Format a frequency in Hz as Hz, kHz, MHz, etc.
	 * @param f frequency value in Hz
	 * @return Formatted string
	 */
	public static String formatFrequency(double f) {
		double absF = Math.abs(f);
		if (absF < 1) {
			return String.format("%f Hz", f);
		}
		else if (absF < 10) {
			return String.format("%4.2f Hz", f);
		}
		else if (absF < 100) {
			return String.format("%4.1f Hz", f);
		}
		else if (absF < 1000) {
			return String.format("%4.0f Hz", f);
		}
		else if (absF < 10000) {
			return String.format("%4.2f kHz", f/1000.);
		}
		else if (absF < 100000) {
			return String.format("%4.1f kHz", f/1000.);
		}
		else if (absF < 1000000) {
			return String.format("%4.0f kHz", f/1000.);
		}
		else if (absF < 1e7) {
			return String.format("%4.3f MHz", f/1.e6);
		}
		else {
			return String.format("%4.3f MHz", f/1.e6);
		}
			
	}
	/**
	 * Leave data alone, but create a list of indexes which will
	 * give the ascending order of data.
	 * <br>
	 * Uses a simple bubble sort, so only suitable for short arrays. 
	 * @param data array to sort
	 * @return sort indexes
	 */
	static public int[] getSortedInds(int[] data) {
		int l = data.length;
		int[] inds = new int[l];
		for (int i = 0; i < l; i++) {
			inds[i] = i;
		}
		int dum;
		for (int i = 0; i < l-1; i++) {
			for (int j = 0; j < l-1-i; j++) {
				if (data[inds[j]] > data[inds[j+1]]) {
					dum = inds[j];
					inds[j] = inds[j+1];
					inds[j+1] = dum;
				}
			}
		}
		return inds;
	}
	
	/**
	 * For N synchronised hydrophones there are N*(N-1)/2 time delays.  For N time delays there are 0.5+sqrt(1+8N)/2 hydrophones. 
	 * This is a simple function which calculates the number of hydrophones from the number of time delays. 
	 * @return the number of synchronised recievers whihc would be required to make the number of time delays,. 
	 */
	public static int calcHydrophoneNumber(int numberOfDelays){
		int hydrophoneNumber=(int) (0.5+Math.sqrt(1+8*numberOfDelays)/2);
		return hydrophoneNumber;
	}
	
	/**
	 * For N synchronised hydrophones there are N*(N-1)/2 time delays.  For N time delays there are 0.5+sqrt(1+2N)/2 hydrophones. 
	 * This is a simple function which calculates the number of time delays from the number of hydrophones. 
	 * @return the  number of time delays whihc would be produced by the number of synchronised hydrophones 
	 */
	public static int calcTDNumber(int numberofHydrophones){
		int tdNumber=(int) numberofHydrophones*(numberofHydrophones-1)/2;
		return tdNumber;
	}
	
	/**
	 * 
	 * @param array two dimensional array of doubles
	 * @return two element array with the min and max values of the input array
	 */
	public static double[] getMinAndMax(double[][] array) {
		if (array == null || array.length == 0) {
			return null;
		}
		double[] linemm;
		double[] minmax = {Double.MAX_VALUE, Double.MIN_VALUE};
		for (int i = 0; i < array.length; i++) {
			linemm = getMinAndMax(array[i]);
			minmax[0] = Math.min(minmax[0], linemm[0]);
			minmax[1] = Math.max(minmax[1], linemm[1]);
		}
		return minmax;
	}


	/**
	 * 
	 * @param array one dimensional array of doubles
	 * @return two element array with the min and max values of the input array
	 */
	public static double[] getMinAndMax(double[] array) {
		double[] minmax = {Double.MAX_VALUE, Double.MIN_VALUE};
		for (int i = 0; i < array.length; i++) {
		  minmax[0] = Math.min(minmax[0], array[i]);
		  minmax[1] = Math.max(minmax[1], array[i]);
		}
		return minmax;
	}
	
	/**
	 * Get the absolute maximum value of a double array
	 * @param array array of numbers
	 * @return maximum absoulte value. 
	 */
	public static double getAbsMax(double[] array) {
		if (array == null || array.length < 1) {
			return Double.NaN;
		}
		double max = Math.abs(array[0]);
		for (int i = 1; i < array.length; i++) {
			max = Math.max(max, Math.abs(array[i]));
		}
		return max;
	}
	
	/**
	 * Get the index of the maximum value within an array
	 * @param array - the array in which to find the maximum value;
	 * @return the index of the array's maximum value.
	 */
	public static int getMaxIndex(double[] array) {
		if (array == null || array.length < 1) {
			return -1;
		}
		double max = Math.abs(array[0]);
		int index=-1; 
		for (int i = 1; i < array.length; i++) {
			if (max<array[i]){
				max=array[i];
				index=i; 
			}
		}
		return index;
	}
	
	/**
	 * Copy of the linspace function from MATLAB 
	 * (generates linearly spaced vectors. It is similar to the colon operator ":" 
	 * but gives direct control over the number of points and always includes
	 *  the end points)
	 * @param start - the first value
	 * @param stop - the end value
	 * @param n - the number of evenly spaced points to return
	 * @return a list of evenly spaced points. 
	 */
	public static List<Double> linspace(double start, double stop, int n)
	{
	   List<Double> result = new ArrayList<Double>();

	   double step = (stop-start)/(n-1);

	   for(int i = 0; i <= n-2; i++)
	   {
	       result.add(start + (i * step));
	   }
	   result.add(stop);

	   return result;
	}
	
	/**
	 * Get evenly spaced points of the surface of a sphere. These can be normalised for evenly spaced vectors. 
	 * Uses a spiral algorithm and golden ratio. 
	 * <br>
	 * See http://blog.marmakoide.org/?p=1 (accessed 30/06/2016) for algorithm. 
	 * @param n The number of points to scater on the surface of the sphere. 
	 * @param r the radius of the sphere.  
	 * @param an array of evenly spaced points on the surface of the sphere. 
	 */
	public static double[][] getSpherePoints(int n, double r){

		//golden_angle = pi * (3 - sqrt(5));
		double goldenAngle= Math.PI*( 0.7639);

		double[][] points=new double[n][3]; 

		List<Double> zLin=linspace(1 - 1.0 / n,  1.0 / n - 1,  n);
		double z;
		double theta;
		double radius;
		for (int i=0; i<n ;i++){					
			z=zLin.get(i);
			radius=Math.sqrt(1-z*z); 
			theta= goldenAngle*i;

			points[i][0]=r*radius* Math.cos(theta);
			points[i][1]=r*radius*Math.sin(theta);
			points[i][2]=r*z; 
		}
		
		return points; 
	}

	/**
	 * Clamps a value so that it has to sit between two values
	 * @param min min allowed value
	 * @param value desired value
	 * @param max max allowed value
	 * @return clamped value between min and max. 
	 */
	public static double clamp(double min, double value, double max) {	
		return Math.max(min, Math.min(max, value));
	}
	
	/**
	 * Clamps a value so that it has to sit between two values
	 * @param min min allowed value
	 * @param value desired value
	 * @param max max allowed value
	 * @return clamped value between min and max. 
	 */
	public static int clamp(int min, int value, int max) {	
		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Returns the nearest value, less or more. If exactly in the middle
	 * it will return more
	 * @param less lower value
	 * @param value value to compare
	 * @param more upper value
	 * @return closest of less and more. 
	 */
	public static double nearest(double less, double value, double more) {
		double r1 = value-less;
		double r2 = more-value;
		return r1<r2 ? less:more;
	}
	
	/**
	 * Returns the nearest value, less or more. If exactly in the middle
	 * it will return more
	 * @param less lower value
	 * @param value value to compare
	 * @param more upper value
	 * @return closest of less and more. 
	 */
	public static int nearest(int less, int value, int more) {
		int r1 = value-less;
		int r2 = more-value;
		return r1<r2 ? less:more;
	}
	
	/**
	 * Calculate the distance between two 3D points.
	 * @param x1 - x coordinate of the first point. 
	 * @param y1 - y coordinate of the first point. 
	 * @param z1 - z coordinate of the first point. 
	 * @param x2 - x coordinate of the second point. 
	 * @param y2 - y coordinate of the second point. 
	 * @param z2 - z coordinate of the second point. 
	 * @return the distance between to the two points. 
	 */
	public static double distance(double x1, double y1, double  z1, double x2, double y2, double z2) {
		return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2) + Math.pow(z2-z1, 2));
	}

	/**
	 * Calculate the distance between two 3D points.
	 * @param point1 - the first point
	 * @param point2 - the second point
	 * @return the distance between the two points. 
	 */
	public static double distance(double[] point1, double[] point2) {
		if (point1.length==2) {
			return distance(point1[0], point1[1], 0, point2[0], point2[1], 0); 
		} 
		else {
			return distance(point1[0], point1[1], point1[2], point2[0], point2[1], point2[2]); 
		}
	}
	
	/**
	 * Calculate the area of a polygon 
	 * @param vertices - a list of vertices
	 * @return - the area in vertice units. 
	 */
	public static double area(double[][] vertices){
	    double sum = 0;
	    for (int i = 0; i < vertices.length ; i++)
	    {
	      if (i == 0)
	      {
	        //System.out.println(vertices[i][0] + "x" + (vertices[i + 1][1] + "-" + vertices[vertices.length - 1][1]));
	        sum += vertices[i][0] * (vertices[i + 1][1] - vertices[vertices.length - 1][1]);
	      }
	      else if (i == vertices.length - 1)
	      {
	        //System.out.println(vertices[i][0] + "x" + (vertices[0][1] + "-" + vertices[i - 1][1]));
	        sum += vertices[i][0] * (vertices[0][1] - vertices[i - 1][1]);
	      }
	      else
	      {
	        //System.out.println(vertices[i][0] + "x" + (vertices[i + 1][1] + "-" + vertices[i - 1][1]));
	        sum += vertices[i][0] * (vertices[i + 1][1] - vertices[i - 1][1]);
	      }
	    }

	    double area = 0.5 * Math.abs(sum);
	    return area;
	  }
	
	/**
	 * Returns the centroid of a cluster of points; 
	 * @param cluster - a list of points.
	 * @return the centroid of the cluster of points. 
	 */
	public static double[] centroid(double[][] cluster){
		
		if (cluster.length<1) return null;
		
		int nDim=cluster[0].length; 
		
		double[] centroid=new double[nDim]; 
		
		//add all points together 
		for (int i=0; i<cluster.length; i++){
			for (int j=0; j<nDim; j++){
				centroid[j]+=cluster[i][j];
			}
		}
		
		//now average. 
		for (int j=0; j<nDim; j++){
			centroid[j]=centroid[j]/cluster.length;
		} 
		
		return centroid; 
	}
	
	/**
	 * Test whether line AB intersects line CD.  2D test only.  Using equations
	 * from http://www.jeffreythompson.org/collision-detection/line-line.php and
	 * https://www.geeksforgeeks.org/program-for-point-of-intersection-of-two-lines/, with
	 * some help from https://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
	 * 
	 * @param ax x-value of start of line AB
	 * @param ay y-value of start of line AB
	 * @param bx x-value of end of line AB
	 * @param by y-value of end of line AB
	 * @param cx x-value of start of line CD
	 * @param cy y-value of start of line CD
	 * @param dx x-value of end of line CD
	 * @param dy y-value of end of line CD
	 * 
	 * @return true if lines intersect
	 */
	public static boolean doLinesIntersect(double ax, double ay, double bx, double by, double cx, double cy, double dx, double dy) {
		double det = (dy-cy)*(bx-ax)-(dx-cx)*(by-ay);

		// If determinant = 0, then lines are parallel.  
		if (det==0) return false;

		double uA = ((dx-cx)*(ay-cy)-(dy-cy)*(ax-cx))/det;
		double uB = ((bx-ax)*(ay-cy)-(by-ay)*(ax-cx))/det;

		// if both uA and uB are in the range 0-1, the lines intersect and we should draw
		// the contour
		if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) return true;
		return false;
	}

	/**
	 * Test whether line AB intersects a rectangle defined by 2 opposite 
	 * corners
	 * 
	 * @param ax x-value of start of line AB
	 * @param ay y-value of start of line AB
	 * @param bx x-value of end of line AB
	 * @param by y-value of end of line AB
	 * @param minx x-value of one corner
	 * @param miny y-value of one corner
	 * @param maxx x-value of the opposite corner
	 * @param maxy y-value of the opposite corner
	 * 
	 * @return true if the line crosses any of the edges of the rectangle
	 */
	public static boolean doesLineIntersectRect(double ax, double ay, double bx, double by, double minx, double miny, double maxx, double maxy) {
		boolean top = doLinesIntersect(ax, ay, bx, by, minx, maxy, maxx, maxy);
		boolean bot = doLinesIntersect(ax, ay, bx, by, minx, miny, maxx, miny);
		boolean left = doLinesIntersect(ax, ay, bx, by, minx, miny, minx, maxy);
		boolean right = doLinesIntersect(ax, ay, bx, by, maxx, miny, maxx, maxy);
		if (top || bot || left || right) return true;
		return false;
	}

	/**
	 * IndexM1 and IndexM2 specify which hydrophones to calculate time delays
	 * between. In the case of a paired array this will simply be the hydrophones in
	 * pairs so Index M1 will be 0 and even numbers and IndexM2 will be odd numbers.
	 * 
	 * @return
	 */
	public static ArrayList<Integer> indexM2(int numberOfHydrophones){
		ArrayList<Integer> IndexM1=new ArrayList<Integer>();
		for (int i=0; i<numberOfHydrophones; i++){
			for (int j=0; j<numberOfHydrophones-(i+1);j++){
				int HN=j+i+1;
				IndexM1.add(HN);	
			}
		}
		return IndexM1;
	}

	/**
	 * IndexM1 and IndexM2 specify which hydrophones to calculate time delays
	 * between. In the case of a paired array this will simply be the hydrophones in
	 * pairs so Index M1 will be 0 and even numbers and IndexM2 will be odd numbers.
	 * 
	 * @return
	 */
	public static ArrayList<Integer> indexM1(int numberOfHydrophones){
		ArrayList<Integer> IndexM2=new ArrayList<Integer>();
		for (int i=0; i<numberOfHydrophones; i++){
			for (int j=0; j<numberOfHydrophones-(i+1);j++){
				int HN=i;
				IndexM2.add(HN);	
			}
		}
		return IndexM2;
	}
	
	/**
	 * Check if a string is empty (zero length after trip) OR null. 
	 * @param string
	 * @return true if it's null OR empty
	 */
	public static boolean emptyString(String string) {
		if (string == null) {
			return true;
		}
		string = string.trim();
		if (string.length() == 0) {
			return true;
		}
		// must be at least one no blank character
		return false;
	}
	
	/**
	 * Trim a string, checking it's not null first. 
	 * @param string
	 * @return
	 */
	public static String trimString(String string) {
		if (string == null) {
			return null;
		}
		return string.trim();
	}

	
}