package PamUtils.complex;

import java.io.Serializable;
import java.util.Arrays;

import fftManager.Complex;

/**
 * Class for handling arrays of Complex data. 
 * <p>
 * This class should be used wherever possible in preference to 
 * arrays of Complex objects since this class realised purely 
 * on an array of primitives, assumed to be in real, imaginary
 * order which should give a speed improvement over previous methods. 
 * <p>
 * functions exist within this to convert to an array of Complex
 * objects to support current PAMGUARD functionality but it is hoped
 * that modules will gradually be rewritten so they don't need this. 
 * @author Doug Gillespie 
 *
 */
public class ComplexArray implements Cloneable, Serializable {


	private static final long serialVersionUID = 1L;

	/**
	 * Main data array of interleaved real / complex data. 
	 */
	private double[] data;

	/**
	 * Construct a complex array. Length of allocated
	 * data array will be 2*n
	 * @param n Number of real / imaginary pairs. 
	 */
	public ComplexArray(int n) {
		data = new double[n*2];
	}

	/**
	 * Construct a complex array from an existing array of 
	 * double data. Data must be interleaved real / imaginary pairs
	 * so the length of data MUST be even. 
	 * @param complexData interleaved real and imaginary data. 
	 */
	public ComplexArray(double[] complexData) {
		this.data = complexData;
	}
	
	/**
	 * Construct a complex array from two existing arrays of 
	 * double data, one with real, the other with imaginary 
	 * parts. Make sure imagData is same length as realData or else... 
	 * @param complexData interleaved real and imaginary data. 
	 */
	public ComplexArray(double[] realData, double[] imagData) {
		this.data = new double[realData.length*2];
		for (int i = 0, j = 0; i < realData.length; i++, j+=2) {
			this.data[j] = realData[i];
			this.data[j+1] = imagData[i];
		}

	}
	
	/**
	 * Make a copy of the complex array with a new length. 
	 * Wraps around the Arrays.copyof function
	 * @param newLength new number of complex objects. 
	 * @return Complex array with the new length. 
	 */
	public ComplexArray copyOf(int newLength) {
		return new ComplexArray(Arrays.copyOf(data, newLength*2));
	}

	/**
	 * Set a single complex number in the array
	 * @param i index of the complex number
	 * @param re real part
	 * @param im imaginary part
	 */
	public void set(int i, double re, double im) {
		i<<=1;
		data[i++] = re;
		data[i] = im;
	}
	
	public void set(int i, Complex complex) {
		i<<=1;
		data[i++] = complex.real;
		data[i] = complex.imag;
	}
	
	public Complex get(int i) {
		return new Complex(data[i*2], data[i*2+1]);
	}

	/**
	 * The length of the complex array, i.e. the number of 
	 * complex numbers. This is half the length of the internal data array. 
	 * @return The number of complex numbers in the array
	 */
	public int length() {
		if (data == null){
			return 0;
		}
		return data.length/2;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ComplexArray clone() {
		ComplexArray newOne;
		try {
			newOne = (ComplexArray) super.clone();
			newOne.data = data.clone();
			return newOne;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the angle of all complex numbers in the array. 
	 * @return angles in radians
	 */
	public double[] ang() {
		if (data == null) {
			return null;
		}
		double[] angles = new double[data.length/2];
		for (int re = 0, im = 1, o = 0; re < data.length; re+=2, im+=2, o++) {
			if (data[re] == 0 && data[im] == 0) {
				angles[o] = 0;
			}
			else {
				angles[0] = Math.atan2(data[im], data[re]);
			}
		}
		return angles;
	}

	/**
	 * Gets the angle of a single complex number in the array
	 * @param i index in the array
	 * @return angle in radians. 
	 */
	public double ang(int i) {
		double re = data[i*2];
		double im = data[i*2+1];
		if (re == 0 && im == 0) {
			return 0;
		}
		return Math.atan2(im, re);
	}

	/**
	 * Gets the squared magnitude of a complex array
	 * @return squared magnitudes
	 */
	public double[] magsq() {
		if (data == null) {
			return null;
		}
		double[] out = new double[data.length/2];
		for (int re = 0, im = 1, o = 0; re < data.length; re+=2, im+=2, o++) {
			out[o] = data[re]*data[re] + data[im]*data[im];
		}
		return out;
	}

	/**
	 * Gets the squared magnitude of a complex array between binLo and binHi-1
	 * @return array of length hiBin-loBin of squared magnitudes
	 * @param loBin bin to start at
	 * @param hiBin bin to stop before
	 */
	public double[] magsq(int loBin, int hiBin) {
		if (data == null) {
			return null;
		}
		int n = hiBin-loBin;
		double[] out = new double[n];
		for (int ir = loBin*2, im = loBin*2+1, o = 0; o < n; ir+=2, im+=2, o++) {
			out[o] = data[ir]*data[ir] + data[im]*data[im];
		}
		return out;
	}
	
	/**
	 * Gets the squared magnitude of a complex number
	 * @param i index of the complex number
	 * @return squared magnitude
	 */
	public double magsq(int i) {
		i<<=1;
		double re = data[i++];
		double im = data[i];
		return re * re + im * im;
	}


	/**
	 * Gets the magnitude of all complex numbers in the array. 
	 * @return Array of magnitudes
	 */
	public double[] mag() {
		double[] out = magsq();
		for (int i = 0; i < out.length; i++) {
			out[i] = Math.sqrt(out[i]);
		}
		return out;
	}
	/**
	 * Gets the magnitude of a single complex number
	 * @param i index of the complex number
	 * @return magnitude magnitude
	 */
	public double mag(int i) {
		return Math.sqrt(magsq(i));
	}

	/**
	 * Calculates the square root of all complex numbers in the array
	 * @return square roots of complex numbers. 
	 */
	public ComplexArray sqrt() {
		if (data == null) {
			return null;
		}
		ComplexArray s = new ComplexArray(length());
		for (int o = 0; o < data.length/2; o++) {
			double newmag = Math.sqrt(mag(o));
			double newang = ang(o) / 2.0;
			s.set(o, newmag * Math.cos(newang), newmag * Math.sin(newang));
		}
		return s;
	}
	/**
	 * Gets the square root of a Complex number
	 * @param i index of the complex number
	 * @return square root of the Complex number
	 */
	public Complex sqrt(int i) {
		double newmag = Math.sqrt(mag(i));
		double newang = ang(i) / 2.0;
		return new Complex(newmag * Math.cos(newang), newmag * Math.sin(newang));
	}

	/**
	 * Raises the complex array to the power f
	 * @param f power factor
	 * @return array of complex numbers raised to the power f. 
	 */
	public ComplexArray pow(double f) {
		if (data == null) {
			return null;
		}
		ComplexArray s = new ComplexArray(length());
		for (int o = 0; o < data.length/2; o++) {
			double newmag = Math.pow(mag(o), f);
			double newang = ang(o) * f;
			s.set(o, newmag * Math.cos(newang), newmag * Math.sin(newang));
		}
		return s;
	}

	/**
	 * Raises a complex number to a scalar power.
	 * @param i index of the complex number 
	 * @param f power to raise number to
	 * @return new Complex number
	 */
	public Complex pow(int i, double f) {
		double newmag = Math.pow(mag(i), f);
		double newang = ang(i) * f;
		return new Complex(newmag * Math.cos(newang), newmag * Math.sin(newang));
	}

	/**
	 * Add a complex array to the current array
	 * @param c complex array to add
	 * @return sum of this and c.
	 */
	public ComplexArray plus(ComplexArray c) {
		if (data == null) {
			return null;
		}
		ComplexArray s = clone();
		for (int i = 0; i < c.data.length; i++) {
			s.data[i] += c.data[i];
		}
		return s;
	}

	/**
	 * Subtract a complex array from this array. 
	 * @param c complex array to subtract. 
	 * @return this minus c.
	 */
	public ComplexArray minus(ComplexArray c) {
		if (data == null) {
			return null;
		}
		ComplexArray s = clone();
		for (int i = 0; i < c.data.length; i++) {
			s.data[i] -= c.data[i];
		}
		return s;
	}

	/**
	 * Multiply a complex array by a scaler factor
	 * @param f multiplication factor. 
	 * @return new complex array
	 */
	public ComplexArray times(double f) {
		if (data == null) {
			return null;
		}
		double[] tData = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			tData[i] = data[i]*f;
		}
		return new ComplexArray(tData);
	}
	
	public Complex times(int i, double f) {
		return new Complex(getReal(i)*f, getImag(i)*f);
	}

	/**
	 * Calculate the complex conjugate of the complex array
	 * @return complex conjugate of the complex array. 
	 */
	public ComplexArray conj() {
		if (data == null) {
			return null;
		}
		double[] tData = data.clone();
		for (int i = 1; i < tData.length; i+=2) {
			tData[i] = -tData[i];
		}
		return new ComplexArray(tData);
	}

	/**
	 * Multiply this array by the complex conjugate of Array s;
	 * @param s Array to multiply by
	 * @return new array = this*conj(other)
	 */
	public ComplexArray conjTimes(ComplexArray s) {
		if (data == null) {
			return null;
		}
		double[] sData = s.data;
		double[] tData = new double[data.length];
		for (int re = 0, im = 1; re < data.length; re+=2, im+=2) {
			tData[re] = data[re]*sData[re] + data[im]*sData[im];
			tData[im] = -data[re]*sData[im] + data[im]*sData[re];
		}
		return new ComplexArray(tData);
	}
	
	/**
	 * Multiply this array by the complex conjugate of Array s
	 * but only using data within the complex bin range >= binRange[0] to < binRange[1]
	 * @param s Array to multiply be
	 * @param binRange range of frequency bins to include. 
	 * @return new array = this*conj(other)
	 */
	public ComplexArray conjTimes(ComplexArray s, int[] binRange) {
		if (data == null) {
			return null;
		}
		double[] sData = s.data;
		double[] tData = new double[data.length];
		for (int re = binRange[0]*2, im = binRange[0]*2+1; re < binRange[1]*2; re+=2, im+=2) {
			tData[re] = data[re]*sData[re] + data[im]*sData[im];
			tData[im] = -data[re]*sData[im] + data[im]*sData[re];
		}
		return new ComplexArray(tData);		
	}
	
	/**
	 * Dot product (aka Inner Product) of this array and another complex array 's'.
	 * The order of the equation is this&#8901s.</p>
	 * <p>For example, if this ComplexArray u = [u0 u1 u2] and the passed array is
	 * v = [v0 v1 v2], the number returned would be:
	 * <ul>
	 * <li>Complex newVal = u0<u>v0</u> + u1<u>v1</u> + u2<u>v2</u></li>
	 * </ul>
	 * <p>Where the underlined variables indicate the complex conjugate.  This method
	 * is similar to the conjTimes method, but adds Complex Numbers together
	 * and returns the resultant Complex number</p>
	 * @param s the ComplexArray to perform the dot product with
	 * @return Complex number = this&#8901s 
	 */
	public Complex dotProduct(ComplexArray s) {
		if (data == null) {
			return null;
		}
		double[] sData = s.data;
		double realVal = 0;
		double imVal = 0;
		for (int re = 0, im = 1; re < data.length; re+=2, im+=2) {
			realVal += data[re]*sData[re] + data[im]*sData[im];
			imVal += -data[re]*sData[im] + data[im]*sData[re];
		}
		return new Complex(realVal,imVal);
	}
	
	/**
	 * <p>Calculate the Cross-Spectral Density Matrix (CSDM) from this complex array.
	 * This array is assumed to be a column vector with number of rows = length().
	 * The returned object will be a ComplexArray column vector with length() number
	 * of rows, and each row will be a ComplexArray object containing a row
	 * vector with length() number of columns.</p>
	 * <p>For example, if this ComplexArray u = [u0 u1 u2], then CSDM(u) = ComplexArray[] of
	 * length 3, where:
	 * <ul>
	 * <li>ComplexArray[0] = [u0<u>u0</u> u0<u>u1</u> u0<u>u2</u>]</li>
	 * <li>ComplexArray[1] = [u1<u>u0</u> u1<u>u1</u> u1<u>u2</u>]</li>
	 * <li>ComplexArray[2] = [u2<u>u0</u> u2<u>u1</u> u2<u>u2</u>]</li>
	 * </ul>
	 * <p>Where the underlined variables indicate the complex conjugate</p>
	 * @return
	 */
	public ComplexArray[] calcCSDM() {
		ComplexArray[] csdm = new ComplexArray[this.length()];
		for (int row=0; row<this.length(); row++) {
			ComplexArray prelim = new ComplexArray(this.length());
			for (int col=0; col<this.length(); col++) {
//				prelim.set(col, this.get(row).times(this.get(col).conj())); to save time and not continuously create new Complex objects, work on the data vector directly
				prelim.data[col*2] = data[row*2]*data[col*2] + data[row*2+1]*data[col*2+1];	// real value
				prelim.data[col*2+1] = -data[row*2]*data[col*2+1] + data[row*2+1]*data[col*2];	// imaginary value
			}
			csdm[row]=prelim;
		}
		return csdm;
	}

	/**
	 * 
	 * @return the entire real part of the array
	 */
	public double[] getReal() {
		double[] r = new double[data.length/2];
		for (int i = 0, j = 0; j < data.length; i++, j+=2) {
			r[i] = data[j];
		}
		return r;
	}
	
	/**
	 * Get a real element from the array
	 * @param i index of the complex number
	 * @return single real element
	 */
	public double getReal(int i) {
		return data[i<<1];
	}

	/**
	 * Set a single real element in the array
	 * @param i index of the complex number
	 * @param re real value to set. 
	 */
	public void setReal(int i, double re) {
		data[i<<1] = re;
	}

	/**
	 * Get a single imaginary element from the array
	 * @param i index of the complex number
	 * @return single imaginary number
	 */
	public double getImag(int i) {
		i<<=1;
		return data[i+1];
	}
	
	/**
	 * 
	 * @return the entire imag part of the array
	 */
	public double[] getImag() {
		double[] r = new double[data.length/2];
		for (int i = 0, j = 1; j < data.length; i++, j+=2) {
			r[i] = data[j];
		}
		return r;
	}
	
	/**
	 * Set a single imaginary element in the array
	 * @param i index of the complex number
	 * @param re imaginary value to set. 
	 */
	public void setImag(int i, double im) {
		data[i*2+1] = im;
	}

	/**
	 * Get the data array of interleaved real / complex elements
	 * @return data array
	 */
	public double[] getData() {
		return data;
	}

	/**
	 * Set the data array
	 * @param data array of interleaved real / imaginary pairs. 
	 */
	public void setData(double[] data) {
		this.data = data;
	}

	/**
	 * Is either the real or imaginary part of a specifiec element NaN
	 * @param i index of the complex number
	 * @return true if either the real or imaginary part is NaN
	 */
	public boolean isNaN(int i) {
		return (Double.isNaN(data[i<<1]) || Double.isNaN(data[(i<<1)+1]));
	}

	/**
	 * Multiply the array internally by a scalar number
	 * @param d scalar multiplier
	 */
	public void internalTimes(double d) {
		for (int i = 0; i < data.length; i++) {
			data[i] *= d;
		}
	}
	
    /**
     *  return a new ComplexArray whose value is (this * b)
     * @param b
     * @return
     */
    public ComplexArray times(Complex b) {
    	double[] newData = data.clone();
    	for(int i = 0; i<newData.length; i+=2) {
    		Complex a = new Complex(newData[i],newData[i+1]);
    		a = a.times(b);
    		newData[i] = a.real;
    		newData[i+1] = a.imag;
    	}
    	return new ComplexArray(newData);
    }
	
	/**
	 * Multiple a single element of the array by a scalar
	 * @param i index of the complex number
	 * @param d scalar factor
	 */
	public void internalTimes(int i, double d) {
		i<<=1;
		data[i++] *= d;
		data[i] *= d;
	}

	/**
	 * Set the entire data array to zero. 
	 */
	public void setZero() {
		for (int i = 0; i < data.length; i++) {
			data[i] = 0;
		}
	}
	
	/**
	 * FFT's of real data often only contain the first half of the data since the second
	 * half is just a flipped complex conjugate of the first half. This function will 
	 * fill back in the second half of the data.  
	 * @return double length array with the second half being the complex cunjugate of the first. 
	 */
	public ComplexArray fillConjugateHalf() {
		double[] fData = Arrays.copyOf(data, data.length*2);
		for (int re1 = 0, im1 = re1+1, re2 = fData.length-2, im2 = re2+1; re1 < data.length; re1+=2, im1+=2, re2-=2, im2-=2) {
			fData[re2] = data[re1];
			fData[im2] = -data[im1];
		}
		return new ComplexArray(fData);
	}

	/**
	 * Create a complex array from a real array. 
	 * @param realArray array of real data. 
	 * @return a complex array - twice the length of realArray with imag parts = 0;
	 */
	public static ComplexArray realToComplex(double[] realArray) {
		if (realArray == null) {
			return null;
		}
		int n = realArray.length;
		double[] cData = new double[n*2];
		for (int i = 0, j = 0; i < n; i++, j+=2) {
			cData[j] = realArray[i];
		}
		return new ComplexArray(cData);
	}

	/**
	 * @return sum of squares of all data in the array. 
	 */
	public double sumSquared() {
		double s = 0;
		for (int ir = 0, im = 1; ir < data.length; ir+=2, im+=2) {
			double rV = data[ir];
			double iV = data[im];
			s += rV*rV+iV*iV;
		}
		return s;
	}
	
	/**
	 * Calculate sum of squares between two specified bins. Sum is from loBin to hiBin-1 inclusive.<br>
	 * No checking of limits so with throw IndexOutOfBoundsException if bins are <0 or > length. 
	 * @param loBin first bin (>=0)
	 * @param hiBin last bin (or rather, the first bin that isn't included; <= Length)
	 * @return sum of squares between those bins. 
	 */
	public double sumSquared(int loBin, int hiBin) {
		double s = 0;
		hiBin *= 2;
		loBin *= 2;
		for (int ir = loBin, im = loBin+1; ir < hiBin; ir+=2, im+=2) {
			double rV = data[ir];
			double iV = data[im];
			s += rV*rV+iV*iV;
		}
		return s;
	}

	/**
	 * 
	 * @param complexData
	 * @return sum of squares of the complex array
	 */
	public static double sumSquared(double[] complexData) {
		double s = 0;
		for (int ir = 0, im = 1; ir < complexData.length; ir+=2, im+=2) {
			double rV = complexData[ir];
			double iV = complexData[im];
			s += rV*rV+iV*iV;
		}
		return s;
	}

}
