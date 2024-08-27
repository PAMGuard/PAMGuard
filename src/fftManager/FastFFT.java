package fftManager;

import java.util.Arrays;

import PamUtils.complex.ComplexArray;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

/**
 * FFT Wrapper which uses the edu.emory.mathcs.jtransforms.fft
 * transforms library for the actual FFT calculations. 
 * <br>These are simple wrappers to use the transforms library with 
 * standard PAMGUARD transform classes. 
 * 
 * @author Doug Gillespie
 *
 */
public class FastFFT  {
	
	private DoubleFFT_1D doubleFFT_1D;
	
	private DoubleFFT_2D doubleFFT_2D;
	
	private int transformSize = 0;
	
	public FastFFT() {
		
	}

	/**
	 * Dummy data for input to the doubleFFT_1D function
	 */
	private double[] dummyX;
	
	/**
	 * Fast FFT function for real data. 
	 * @param x real data array
	 * @param y preallocated Complex array for output data (can be null)
	 * @param m log2 of the FFT length (sorry !)
	 * @return Complex FFT data. 
	 */
	public synchronized Complex[] rfft(double[] x, Complex[] y, int m) {
		int n = 1<<m;
		/*
		 * Copy the double array since it's going to be transformed 
		 * and we won't want to mess with x.
		 */
		dummyX = Arrays.copyOf(x, n);
	
		/*
		 * Check the transform has been created with the right fft length
		 */
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		
		/*
		 * Run the FFT
		 */
		doubleFFT_1D.realForward(dummyX);
		
		/*
		 * Now copy the interleaved data out of the double array back into the 
		 * Complex y.
		 */
		return packDoubleToComplex(dummyX, y);
	}
	
	/**
	 * FFT of real data. Will return only the first half of
	 * the FFT, since second is simply complex conjugate of the first. 
	 * @param x waveform (will be padded or truncated to length n)
	 * @param n length of FFT
	 * @return Complex spectrum
	 */
	public synchronized ComplexArray rfft(double[] x, int n) {
		/*
		 * Copy the double array since it's going to be transformed 
		 * and we won't want to mess with x.
		 */
		double[] y = Arrays.copyOf(x, n);

		/*
		 * Check the transform has been created with the right fft length
		 */
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		
		/*
		 * Run the FFT
		 */
		doubleFFT_1D.realForward(y);
		
		/*
		 * Now return the data as a complex array object. 
		 */
		return new ComplexArray(y);
	}
	
	
	public synchronized ComplexArray rfftFull(double[] x, int n) {
		/*
		 * Copy the double array since it's going to be transformed 
		 * and we won't want to mess with x.
		 */
		double[] y = new double[2*n];
		 System.arraycopy(x, 0, y, 0, x.length);

		/*
		 * Check the transform has been created with the right fft length
		 */
		if (doubleFFT_1D == null || transformSize != n) {
			transformSize = n;
			doubleFFT_1D = new DoubleFFT_1D(n);
		}
		
		/*
		 * Run the FFT
		 */
		doubleFFT_1D.realForwardFull(y);
		
		/*
		 * Now return the data as a complex array object. 
		 */
		return new ComplexArray(y);
	}


	/**
	 * In place fft of complex data. 
	 * @param x complex array
	 */
	public synchronized void fft(Complex[] x) {
		double[] d = packComplexToDouble(x, dummyX);
		int n = x.length;
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		doubleFFT_1D.complexForward(d);
		packDoubleToComplex(d, x);
	}
	
	/**
	 * In place complex fft of complex data. 
	 * @param x Complex data array. 
	 */
	public synchronized void fft(ComplexArray x) {
		int n = x.length();
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		doubleFFT_1D.complexForward(x.getData());
	}
	
	/**
	 * In lace FFT of a 2D complex array. 
	 * Will use the multithreading abilities of the 
	 * JTransofrms library. 
	 * @param x
	 */
	public synchronized void fft(Complex[][] x) {
		int rows = x.length;
		int cols = x[0].length;
		doubleFFT_2D = new DoubleFFT_2D(rows, cols);
		double[][] d = packComplexToDouble(x);
		doubleFFT_2D.complexForward(d);
		x = packDoubleToComplex(d, x);
	}

	/**
	 * Inverse FFT for Complex data. 
	 * <br> I FFT is performed 'in place' so data are overwritten
	 * @param x Complex Data
	 * @param m log2 of the FFT length (sorry !)
	 */
	public synchronized void ifft(Complex[] x, int m) {
		int n = 1<<m;
		Complex[] inData = x; 
		/*
		 * Pack the complex data into a double array
		 */
		double[] d = packComplexToDouble(x, dummyX);
		/*
		 * Check the transform has been created with the right fft length
		 */
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		doubleFFT_1D.complexInverse(d, false);
		/*
		 * Unpack the double data back into a complex array. 
		 */
		packDoubleToComplex(d, x);
		if (x != inData) {
			System.out.println("Repacked complex data into new array - ERROR !!!!");
		}

	}
	
	/**
	 * Inverse FFT for Complex data. 
	 * <br> I FFT is performed 'in place' so data are overwritten
	 * @param x ComplexArray - the input data. 
	 * @param n the FFT length
	 */
	public synchronized void ifft(ComplexArray x, int n) {
		 ifft( x,  n, false);  
	}
	
	/**
	 * Inverse FFT for Complex data. 
	 * <br> I FFT is performed 'in place' so data are overwritten
	 * @param x ComplexArray - the input data
	 * @param scale - true for scaling to be performed
	 * @param n the FFT length
	 */
	public synchronized void ifft(ComplexArray x, int n, boolean scale ) {
		double[] data = x.getData();
		if (data.length != 2*n) {
			data = Arrays.copyOf(data, 2*n);
		}
		/*
		 * Check the transform has been created with the right fft length
		 */
		if (doubleFFT_1D == null || transformSize != n) {
			doubleFFT_1D = new DoubleFFT_1D(transformSize = n);
		}
		
		doubleFFT_1D.complexInverse(data, scale);
		
		x.setData(data);
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
	public synchronized static int nextBinaryExp(int sourceNumber) {
		int power = 0;

		for (int i = 0; i < 31; i++) {
			power = 1 << i;
			if (power >= sourceNumber)
				break;
		}
		return power;
	}
	
	/**
	 * Finds the next highest binary exponential of the input integer. If the
	 * input is itself a binary exponential, then the result is itself. E.g.
	 * given 7 returns 8, 8 returns 8, 9 returns 16. Notes has limit of 2^100.
	 * Matlab calls this function nextpow2; it's also akin to frexp in C.
	 * 
	 * @param startPower power of 2 to start at (e,g. 1 will return a minimum of 2)
	 * @param sourceNumber
	 * @return The next highest 2^ of the input, unless input is itself a binary
	 *         exponential.
	 */
	public synchronized static int nextBinaryExp(int startPower, int sourceNumber) {
		int power = startPower;

		for (int i = startPower; i < 31; i++) {
			power = 1 << i;
			if (power >= sourceNumber)
				break;
		}
		return power;
	}
	
	/**
	 * 
	 * @param FFTlength
	 * @return log2 of FFTlength, -1 if not natural power of 2
	 */
	public synchronized static int log2(int num) {
		// return -1 if it's not a natural power of 2
		for (int i = 0; i < 32; i++) {
			if (1<<i == num) return i;
		}
		return -1;
	}
	
	double[][] packComplexToDouble(Complex[][] c) {
		int nR = c.length;
		int nC = c[0].length;
		double[][] d = new double[nR][nC*2];
		for (int i = 0; i < nR; i++) {
			d[i] = packComplexToDouble(c[i], d[i]);
		}
		return d;
	}
	
	Complex[][] packDoubleToComplex(double[][] d, Complex[][] c) {
		int nR = d.length;
		int nC = d[0].length/2;
		if (c == null || c.length != d.length) {
			c = new Complex[nR][];
		}
		for (int i = 0; i < nR; i++) {
			c[i] = packDoubleToComplex(d[i], c[i]);
		}
		return c;
	}
	
	/**
	 * Packs a complex array into a double array of twice the length
	 * <br>
	 * Will allocate double array if necessary
	 * @param c Complex array
	 * @param d double array
	 * @return double array
	 */
	private double[] packComplexToDouble(Complex[] c, double[] d) {
		int n = c.length;
		if (d == null || d.length != 2*n) {
			d = new double[2*n];
		}
		int iD = 0;
		for (int i = 0; i < n; i++) {
			d[iD++] = c[i].real;
			d[iD++] = c[i].imag;
		}
		return d;
	}
	
	private Complex[] packDoubleToComplex(double[] d, Complex[] c) {
		int n = d.length;
		// check that n is even. 
		int m = n/2;
		m *= 2;
		if (m != n) {
			System.out.println(String.format("Complex packing error - odd number of datas = %d", n));
		}
		if (c == null || c.length != n/2) {
//			if (c == null)
//			System.out.println("null Complex array in packDoubleToComplex");
//			else {
//				System.out.printf("packDoubleToComplex resize array from %d to %d\n", c.length, n);
//			}
			c = Complex.allocateComplexArray(n/2);
		}
		int iC = 0;
		int i = 0;
		try {
			for (i = 0; i < n; i+=2) {
				//			System.out.println("n, i, ic " + n + " " + i + " " + iC);
				c[iC].real = d[i];
				c[iC++].imag = d[i+1];
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return c;
	}


}
