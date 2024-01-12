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

package fftManager;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Class definition for a Complex number type.
 * 
 * @author Paul Redmond / Doug Gillespie
 * 
 */
public class Complex implements Cloneable, Serializable, Comparable<Complex>, ManagedParameters {
	
	static public final long serialVersionUID = 1;
	
	public double real = 0.0;

	public double imag = 0.0;

	public Complex() {
		conCalls++;
	}
	
	private static long conCalls = 0;


	/**
	 * Constructor 
	 * @param real real part
	 * @param imag imaginary part
	 */
	public Complex(double real, double imag) {
		this.real = real;
		this.imag = imag;
		conCalls++;
	}

	/**
	 * Constructor
	 * @param a Complex Number (to clone)
	 */
	public Complex(Complex a) {
		this.real = a.real;
		this.imag = a.imag;
		conCalls++;
	}

	@Override
	public Complex clone()  {

		try {
			return (Complex) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the angle of a complex number
	 * @return angle in radians
	 */
	public double ang() {
		if (real == 0. && imag == 0.)
			return 0.;
		return Math.atan2(imag, real);
	}

	/**
	 * Gets the squared magnitude of a complex number
	 * @return squared magnitude
	 */
	public double magsq() {
		return real * real + imag * imag;
	}

	/**
	 * Gets the squared magnitude of a complex number
	 * @return squared magnitude
	 */
	@Deprecated
	public double norm() {
		return real * real + imag * imag;
	}

	/**
	 * Gets the magnitude of a complex number
	 * @return magnitude
	 */
	public double mag() {
		return Math.sqrt(magsq());
	}

	/**
	 * Gets the square root of a Complex number
	 * @return square root of the Complex number
	 */
	public Complex sqrt() {
		double newmag = Math.sqrt(mag());
		double newang = ang() / 2.0;
		return new Complex(newmag * Math.cos(newang), newmag * Math.sin(newang));
	}

	/**
	 * Raises a complex number to a scalar power. 
	 * @param f power to raise number to
	 * @return new Complex number
	 */
	public Complex pow(double f) {
		double newmag = Math.pow(mag(), f);
		double newang = ang() * f;
		return new Complex(newmag * Math.cos(newang), newmag * Math.sin(newang));
	}

    /**
     * A new Complex object whose value is the complex exponential of this
     * @return
     */
    public Complex exp() {
        return new Complex(Math.exp(real) * Math.cos(imag), 
        		Math.exp(real) * Math.sin(imag));
    }
	
	/**
	 * Adds a complex number
	 * @param b Complex number to add
	 * @return new Complex number
	 */
	public Complex plus(Complex b) {
		return new Complex(real + b.real, imag + b.imag);
	}

	/**
	 * Assign new real and imaginary values to an existing Complex number
	 * @param b Complex number to take values from
	 */
	public void assign(Complex b) {
		real = b.real;
		imag = b.imag;
	}

	/**
	 * Assign new real and imaginary values to an existing Complex number
	 * @param real new real part
	 * @param imag new imaginary part
	 */
	public void assign(double real, double imag) {
		this.real = real;
		this.imag = imag;
	}

	/**
	 * Add a real number to a complex number
	 * @param b real number
	 * @return new Complex number
	 */
	public Complex plus(double b) {
		return new Complex(real + b, imag);
	}
	/**
	 * Subtract a real number from a complex number
	 * @param b real number
	 * @return new Complex number
	 */
	public Complex minus(Complex b) {
		return new Complex(real - b.real, imag - b.imag);
	}

	/**
	 * Subtract a real number from a complex number
	 * @param b real number
	 * @return new Complex number
	 */
	public Complex minus(double b) {
		return new Complex(real - b, imag);
	}

	/**
	 * Multiply a complex number by a real
	 * @param b real number
	 * @return new Complex number
	 */
	public Complex times(double b) {
		return new Complex(real * b, imag * b);
	}

	/**
	 * Multiply a complex number by another complex number
	 * @param b complex number
	 * @return new complex number
	 */
	public Complex times(Complex b) {
		return new Complex(real * b.real - imag * b.imag, real * b.imag + imag
				* b.real);
	}
	 
	/**
	 * Multiply a complex numbers real and imaginary parts by a real number
	 * @param b real number
	 */
	public void internalTimes(double b) {
		real *= b;
		imag *= b;
	}

	/**
	 * Multiply a complex numbers real and imaginary parts by a complex number
	 * @param b complex number
	 */
	public void internalTimes(Complex b) {
		double newReal = real * b.real - imag * b.imag;
		double newImag = real * b.imag + imag * b.real;
		real = newReal;
		imag = newImag;
	}

	public Complex conj() {
		return new Complex(real, -imag);
	}

	/**
	 * Converts a number to it's own complex conjugate
	 * @param x
	 */
	public static void conj(Complex x) {
		x.imag = -x.imag;
	}
	
	/**
	 * Returns true if either the real or imaginary part 
	 * is a Not-a-Number (NaN) value, false otherwise
	 * @return true if either part of the number is NaN
	 */
	public boolean isNaN() {
		return Double.isNaN(imag) || Double.isNaN(real);
	}

	/**
	 * Returns true if either the real or imaginary part 
	 * is infinite, false otherwise
	 * @return true if either part of the number is infinite
	 */
	public boolean isInfinite() {
		return Double.isInfinite(imag) || Double.isInfinite(real);
	}
	
	/**
	 * Create a complex array from a double array
	 * @param doubleArray doubel array
	 * @return complex array
	 */
	public static Complex[] createComplexArray(double[] doubleArray) {
		Complex[] c = allocateComplexArray(doubleArray.length);
		for (int i = 0; i < doubleArray.length; i++) {
			c[i].real = doubleArray[i];
		}
		return c;
	}
	
	/**
	 * Allocate a new complex array in which each
	 * element has been created and it's contents set to
	 * 0 +i0;
	 * @param n length of array
	 * @return Complex array of length n
	 */
	public static Complex[] allocateComplexArray(int n) {
		Complex[] newArray = new Complex[n];
		for (int i = 0; i < n; i++) {
			newArray[i] = new Complex();
		}
		return newArray;
	}
	
	public static Complex[][] allocateComplexArray(int n, int m) {
		Complex[][] newArray = new Complex[n][m];
		newArray = new Complex[n][];
		for (int i = 0; i < n; i++) {
			newArray[i] = allocateComplexArray(m);
		}
		return newArray;
	}
	
	/**
	 * Sets all the elements of a complex array to zero
	 * @param array Complex Array
	 */
	public static void zeroComplexArray(Complex[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i].assign(0., 0.);
		}
	}
	
	@Override
	public String toString() {
		return toString(4);
//		return toString("%2.2f");
	}
	
	/**
	 * 
	 * @param format such as %2.2f
	 * @return
	 */
	public String toString(String format) {
		return String.format("["+format+", i"+format+"]",real,imag);
	}
	
	
	public String toString(int decimalPlaces) {
		return String.format("[%."+decimalPlaces+"f, %."+decimalPlaces+"fi]",real,imag);
	}

	public static long getConstructorCalls() {
		return conCalls;
	}

	@Override
	public int compareTo(Complex o) {
		/**
		 * Implementation of Comparable that works simply on the magnitude. 
		 */
		return Double.compare(this.magsq(), o.magsq());
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
