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
/**
 * @author David McLaren, Paul Redmond
 *  
 * Preferences - Java - Code Style - Code Templates
 */

package fftManager;

/**
 * The fft encapsulates methods to perform fast fourier transform on input of
 * kernel and signal data.
 * 
 * @author David McLaren, Paul Redmond
 * 
 */

@Deprecated
public class FFT {

	int ifftInitialArrayLength;

	/**
	 * This is a test method for crossCorrelation. It applies the
	 * crossCorellation method to predefined test signal and kernel data and
	 * compares to a predefined result. If the actual result does not match the
	 * predefined result an error could have been introduced in the
	 * crossCorrelation method or any called method e.g. recursiveFFT and
	 * recursiveIFFT.
	 * 
	 * @return Indicates whether the crossCorellation result matches the
	 *         prefined result, true = match, false = mismatch.
	 * 
	 */
	public boolean testCrossCorrelation() {
		double[] testSignal = { 0.0, 1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0, -1.0,
				-2.0, -3.0, -4.0, -3.0, -2.0, -1.0, 0.0 };
		double[] testKernel = { 1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0 };

		double[] correctResults = { 0.9090909090909091, 0.9999999999999999,
				0.8863636363636364, 0.6136363636363634, 0.22727272727272718,
				-0.22727272727272735, -0.6136363636363638, -0.8863636363636366,
				-1.0, -0.909090909090909, -0.7045454545454546,
				-0.4318181818181816, -0.13636363636363627, 0.13636363636363638,
				0.431818181818182, 0.7045454545454547 };

		int signalStart = 0;
		int signalEnd = testSignal.length;
		int kernelStart = 0;
		int kernelEnd = testKernel.length;
		

		double[] xCorrelation = crossCorrelation(testSignal, signalStart,
				signalEnd, testKernel, kernelStart, kernelEnd);

		boolean resultMatch = true;
		// Display
		for (int i = 0; i < xCorrelation.length; i++) {
			if (correctResults[i] != xCorrelation[i]) {
				resultMatch = false;
			}

			// System.out.println(xCorrelation[i]);
		}

		if (!resultMatch)
			System.out
					.println("testCrossCorellation result did NOT match expected results, are you sure cross correlation still working");
		else
			System.out
					.println("testCrossCorellation result matches expected result.");

		return resultMatch;
	}

	/**
	 * Setup up the internal reference to the input double[] to be used to
	 * create the kernel. Since it is a 'reference' care should be taken by the
	 * caller when modifying the input array between setKernel and and FFT
	 * operation.
	 * 
	 * @param signalSource
	 *            The double array used create the kernel.
	 * @param startIndex
	 *            The start position with the source array.
	 * @param endIndex
	 *            The end position with the source array.
	 */
	/*
	 * private void setSignal(double[] signalSource, int startIndex, int
	 * endIndex){ this.signalInputStart = startIndex; this.signalInputEnd =
	 * endIndex; this.signalInput = signalSource; //
	 * doubleToPaddedComplex(testSignal, signalStart, signalEnd,
	 * highestBinaryExp); };
	 * 
	 */

	/**
	 * Setup up the internal reference to the double array to be used to create
	 * the kernel. Since it is a 'reference' care should be taken by the caller
	 * when modifying the input array between setKernel and and FFT operation.
	 * 
	 * @param kernelSource
	 *            The double array used create the kernel
	 * @param startIndex
	 *            The start position with the source array.
	 * @param endIndex
	 *            The end position with the source array.
	 */
	/*
	 * private void setKernel(double[] kernelSource, int startIndex, int
	 * endIndex){ this.kernelInputStart = startIndex; this.kernelInputEnd =
	 * endIndex; this.kernelInput = kernelSource; };
	 * 
	 * 
	 */
	/**
	 * Simple constructer, creates an fft object which provides the methods:
	 * crossCorrelation, recursiveIFFT, and recursiveFFT.
	 * 
	 */
	public FFT() {
		super();
	}

	/**
	 * Sets a variable used by the fft algorithm to hold intial input array
	 * length.
	 * 
	 * @param initialArrayLength
	 *            .Holds the value fft input array at the start.
	 */
	private void setIFFTInitialArrayLength(int initialArrayLength) {
		ifftInitialArrayLength = initialArrayLength;
	}

	/**
	 * Finds the next highest binary exponential of the input integer. If the
	 * input is itself a binary exponential, then the result is itself. E.g.
	 * given 7 returns 8, 8 returns 8, 9 returns 16. Notes has limit of 2^100.
	 * Matlab calls this function nextpow2; it's also akin to frexp in C.
	 * 
	 * @param sourceNumber
	 * @return The next highest 2^ of the input, unless input is itself a binary
	 *         exponential.
	 */
	public static int nextBinaryExp(int sourceNumber) {
		int power = 0;

		for (int i = 0; i < 100; i++) {
			power = 1 << i;
			//power = (int) (java.lang.Math.pow(2, i));
			if (power >= sourceNumber)
				break;
		}
		// //System.out.println("Nearest power: " + power);

		return power;
	}
	
	public static int log2(int num) {
		// return -1 if it's not a natural power of 2
		for (int i = 0; i < 32; i++) {
			if (1<<i == num) return i;
		}
		return -1;
	}

	/**
	 * Performs a recursive radix 2 FFT. TODO add exception throw for errors,
	 * e.g. input length not recursively divisable by 2.
	 * 
	 * Better to use FastFFT.rfft which is several times faster. 
	 * 
	 * @param complexIn
	 *            The data array of type Complex.
	 * @return The resulting array after FFT is applied.
	 */
	@Deprecated
	public Complex[] recursiveFFT(Complex complexIn[]) {

		int n = complexIn.length;
		if (n == 1) {

			return complexIn;
		}

		double wnReal = Math.cos(-2.0 * Math.PI / (n));
		double wnImag = Math.sin(-2.0 * Math.PI / (n));
		double wReal = 1.0;
		double wImag = 0.0;
		double tempwReal;
		double tempwImag;
		double[] yReal = new double[complexIn.length];
		double[] yImag = new double[complexIn.length];

		Complex[] aOdds = new Complex[complexIn.length / 2];
		Complex[] aEvens = new Complex[complexIn.length / 2];
		Complex[] yOdds = new Complex[complexIn.length / 2];
		Complex[] yEvens = new Complex[complexIn.length / 2];
		Complex[] yOut = new Complex[complexIn.length];

		int j = 0;

		for (int i = 0; i < n; i = i + 2) {
			aOdds[j] = new Complex();
			aEvens[j] = new Complex();
			aOdds[j].real = complexIn[i].real;
			aEvens[j].real = complexIn[i + 1].real;
			aOdds[j].imag = complexIn[i].imag;
			aEvens[j].imag = complexIn[i + 1].imag;
			j++;
		}

		yOdds = recursiveFFT(aOdds);
		yEvens = recursiveFFT(aEvens);

		for (int k = 0; k < n / 2; k++) {

			yReal[k] = yOdds[k].real
					+ (wReal * yEvens[k].real - wImag * yEvens[k].imag);
			yImag[k] = yOdds[k].imag
					+ (wReal * yEvens[k].imag + wImag * yEvens[k].real);
			yReal[k + n / 2] = yOdds[k].real
					- (wReal * yEvens[k].real - wImag * yEvens[k].imag);
			yImag[k + n / 2] = yOdds[k].imag
					- (wReal * yEvens[k].imag + wImag * yEvens[k].real);

			tempwReal = wReal * wnReal - wImag * wnImag;
			tempwImag = wImag * wnReal + wReal * wnImag;
			wReal = tempwReal;
			wImag = tempwImag;

		}

		for (int k = 0; k < yReal.length; k++) {
			yOut[k] = new Complex();
			yOut[k].real = yReal[k];
			yOut[k].imag = yImag[k];
		}

		return yOut;
	}

	/**
	 * Performs a recursive radix 2 Inverse FFT. TODO add exception throw for
	 * errors, e.g. input length not recursively divisable by 2.
	 * <br>
	 * Better to use FastFFT.ifft which is several times faster !
	 * @param complexIn
	 *            The data array of type Complex.
	 * @return The resulting array after FFT is applied.
	 * 
	 */
	@Deprecated
	public Complex[] recursiveIFFT(Complex complexIn[]) {

		int n = complexIn.length;
		if (n == 1) {
			return complexIn;
		}

		double wnReal = Math.cos(2.0 * Math.PI / (n));
		double wnImag = Math.sin(2.0 * Math.PI / (n));
		double wReal = 1.0;
		double wImag = 0.0;
		double tempwReal;
		double tempwImag;
		double[] yReal = new double[complexIn.length];
		double[] yImag = new double[complexIn.length];
		Complex[] aOdds = new Complex[complexIn.length / 2];
		Complex[] aEvens = new Complex[complexIn.length / 2];
		Complex[] yOdds = new Complex[complexIn.length / 2];
		Complex[] yEvens = new Complex[complexIn.length / 2];
		Complex[] yOut = new Complex[complexIn.length];

		int j = 0;

		for (int i = 0; i < n; i = i + 2) {
			aOdds[j] = new Complex();
			aEvens[j] = new Complex();
			aOdds[j].real = complexIn[i].real;
			aEvens[j].real = complexIn[i + 1].real;
			aOdds[j].imag = complexIn[i].imag;
			aEvens[j].imag = complexIn[i + 1].imag;
			j++;
		}

		yOdds = recursiveIFFT(aOdds);
		yEvens = recursiveIFFT(aEvens);

		for (int k = 0; k < n / 2; k++) {
			yReal[k] = yOdds[k].real
					+ (wReal * yEvens[k].real - wImag * yEvens[k].imag);
			yImag[k] = yOdds[k].imag
					+ (wReal * yEvens[k].imag + wImag * yEvens[k].real);
			yReal[k + n / 2] = yOdds[k].real
					- (wReal * yEvens[k].real - wImag * yEvens[k].imag);
			yImag[k + n / 2] = yOdds[k].imag
					- (wReal * yEvens[k].imag + wImag * yEvens[k].real);
			tempwReal = wReal * wnReal - wImag * wnImag;
			tempwImag = wImag * wnReal + wReal * wnImag;
			wReal = tempwReal;
			wImag = tempwImag;
		}

		if (yReal.length == ifftInitialArrayLength) {
			for (int k = 0; k < yReal.length; k++) {
				yOut[k] = new Complex();
				yOut[k].real = yReal[k] / ifftInitialArrayLength;
				yOut[k].imag = yImag[k] / ifftInitialArrayLength;
			}
		} else {
			for (int k = 0; k < yReal.length; k++) {
				yOut[k] = new Complex();
				yOut[k].real = yReal[k];
				yOut[k].imag = yImag[k];
			}
		}

		return yOut;
	}

	/**
	 * Takes an array of doubles and uses it to create an array of type Complex.
	 * The start and end position within in the input array handle the scenario
	 * where the target data is a block within unwanted array elements. The
	 * resulting Complex array can be padded to a desired length with appended
	 * Zero'd Complex type.
	 * 
	 * @param doubleArray
	 *            An array of double to be used form the 'real' part in an array
	 *            of type Complex.
	 * @param startPos
	 *            The index of first double to be used in forming the complex
	 *            array.
	 * @param endPos
	 *            The index of the last double to be used in forming the complex
	 *            array.
	 * @param padToTotalLength
	 *            pads the returned complex[] to have this number of elements.
	 *            This is done by appending (Real: 0.0, Imag: 0.0) Complex
	 *            objects to the end.
	 * @return complex[]
	 */
	private Complex[] doubleToPaddedComplex(double doubleArray[], int startPos,
			int endPos, int padToTotalLength) {

		Complex[] complex = new Complex[padToTotalLength];

		// TODO exception if end !> start
		int countElements = endPos - startPos;
		// TODO exception if padToTotalLength ! >= countElements

		for (int i = 0; i < countElements; i++) {
			complex[i] = new Complex();
			complex[i].real = doubleArray[startPos + i];
			complex[i].imag = 0;
		}

		// pad signal
		int padStart = countElements;
		// //System.out.println("padStart: " + padStart);
		for (int i = padStart; i < padToTotalLength; i++) {
			complex[i] = new Complex();
			complex[i].real = 0;
			complex[i].imag = 0;
		}

		// Display
		/*
		 * for (int i = 0; i < complex.length; i++) {
		 * //System.out.println("complex" + i + " : " + complex[i].real); }
		 */

		return complex;
	} // End: method doubleToPaddedComplex

	/**
	 * Performs cross correlation in the frequency domain using FFT and IFFT.
	 * 
	 * @param signal
	 *            An array of type double containing the signal.
	 * @param signalStart
	 *            The index of where the signal starts within the array.
	 * @param signalEnd
	 *            The index of where the signal ends within the array.
	 * @param kernel
	 *            An array of type double containing the kernel.
	 * @param kernelStart
	 *            The index of where the kernel starts within the array.
	 * @param kernelEnd
	 *            The index of where the kernel ends within the array.
	 * @return The results of cross-correlation of kernel against signal.
	 */
	public double[] crossCorrelation(double[] signal, int signalStart,
			int signalEnd, double[] kernel, int kernelStart, int kernelEnd) {

		// signal should be bigger than kernel.
		// TODO: check sizes - raise exceptions, performance ?
		int signalSize = signalEnd - signalStart;
		int kernelSize = kernelEnd - kernelStart;
		int highestBinaryExp = Math.max(nextBinaryExp(signalSize),
				nextBinaryExp(kernelSize));

		//
		Complex[] signalComplex = doubleToPaddedComplex(signal, signalStart,
				signalEnd, highestBinaryExp);
		Complex[] kernelComplex = doubleToPaddedComplex(kernel, kernelStart,
				kernelEnd, highestBinaryExp);

		// Get the match filtered data
		setIFFTInitialArrayLength(highestBinaryExp);
		double[] xCorrelation = crossCorrelation(signalComplex, kernelComplex);

		// Create the result array same size as signal data.
		// There maybe a faster way to copy/truncate arrays, e.g.
		// maybe using the native System.arraycopy; is it better though?
		if (xCorrelation.length == signalSize)	//if it's already right size...
			return xCorrelation;				//...don't bother copying
		double[] xCorrelationTrimmed = new double[signalSize];
		for (int i = 0; i < signalSize; i++) {
			xCorrelationTrimmed[i] = xCorrelation[i];
		}

		return xCorrelationTrimmed;
	}

	/**
	 * Performs cross correlation in the frequency domain using FFT and IFFT.
	 * 
	 * @param signalComplex
	 *            An array of type Complex containing the signal.
	 * @param kernelComplex
	 *            An array of type Complex containing the kernel.
	 * @return The results of cross-correlation of kenerl against signal.
	 */
	private double[] crossCorrelation(Complex[] signalComplex,
			Complex[] kernelComplex) {
		// System.out.println("=====================================");

		int arraySize = signalComplex.length;

		Complex[] fftProduct = new Complex[arraySize];
		Complex[] ifftAnswer = new Complex[arraySize];
		double[] xcorrAnswer = new double[arraySize];
		double kernelSumSquares = 0;

		Complex[] signalFftAnswer = recursiveFFT(signalComplex);
		Complex[] kernelFftAnswer = recursiveFFT(kernelComplex);

		//
		for (int i = 0; i < kernelFftAnswer.length; i++) {

			fftProduct[i] = new Complex();

			kernelFftAnswer[i].imag = -kernelFftAnswer[i].imag;

			// calculate real part
			fftProduct[i].real = signalFftAnswer[i].real
					* kernelFftAnswer[i].real - signalFftAnswer[i].imag
					* kernelFftAnswer[i].imag;

			// calculate imaginary part
			fftProduct[i].imag = signalFftAnswer[i].imag
					* kernelFftAnswer[i].real + signalFftAnswer[i].real
					* kernelFftAnswer[i].imag;
		}

		ifftAnswer = recursiveIFFT(fftProduct);

		for (int i = 0; i < kernelComplex.length; i++) {
			kernelSumSquares = kernelSumSquares
					+ ((kernelComplex[i].real) * (kernelComplex[i].real));
		}

		for (int i = 0; i < xcorrAnswer.length; i++) {
			xcorrAnswer[i] = ifftAnswer[i].real / kernelSumSquares;
		}

		return xcorrAnswer;
	}// End method xcorrelation method

}
