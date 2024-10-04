package Filters;

import java.util.Arrays;

import fftManager.Complex;

public abstract class IIRFilterMethod extends FilterMethod {


	protected Complex[] poles;

	protected Complex[] zeros;
	
	public IIRFilterMethod(double sampleRate, FilterParams filterParams) {
		super(sampleRate, filterParams);
	}

	public Complex[] getPoles(FilterParams filterParams) {
		if (!this.filterParams.equals(filterParams)) {
			this.filterParams = filterParams.clone();
			calculateOmegaValues();
			calculateFilter();
		}
		return poles;
	}

	public Complex[] getZeros(FilterParams filterParams) {
		if (!this.filterParams.equals(filterParams)) {
			this.filterParams = filterParams.clone();
			calculateOmegaValues();
			calculateFilter();
		}
		return zeros;
	}
	
	public PoleZeroPair[] getPoleZeros() {
		if (!this.filterParams.equals(filterParams)) {
			this.filterParams = filterParams.clone();
			calculateOmegaValues();
			calculateFilter();
		}
		if (poles == null || zeros == null) {
			return null;
		}
		// see how many are non null
		int maxInd = 0;
		for (int i = 0; i < poles.length; i++) {
			if (poles[i] == null || zeros[i] == null) {
				break;
			}
			maxInd = i;
		}
		/*
		 * Need a fudge to find the odd one, which is it's own complex conjugate. 
		 * Of course with rounding errors, this may not have a perfectly zero 
		 * imag value, but it should always be in the middle of the list.
		 */
		int oddInd = getOddPZIndex(filterParams);
		
		PoleZeroPair[] pairs = new PoleZeroPair[maxInd+1];
		for (int i = 0; i <= maxInd; i++) {
			pairs[i] = new PoleZeroPair(poles[i], zeros[i],i==oddInd);
		}
		Arrays.sort(pairs);
		return pairs;
	}
	
	int getOddPZIndex(FilterParams filterParams) {
		if (filterParams.filterOrder % 2 == 0) return -1;
		if (filterParams.filterBand == FilterBand.BANDPASS) return -1;
		if (filterParams.filterBand == FilterBand.BANDSTOP) return -1;
		return filterParams.filterOrder/2;
	}

	public int poleZeroCount() {
		if (filterParams.filterBand == FilterBand.BANDPASS)
			return filterParams.filterOrder * 2;
		else if (filterParams.filterBand == FilterBand.BANDSTOP)
			return filterParams.filterOrder * 2;
		else
			return filterParams.filterOrder;
	}

	protected double omega1, omega2, omega3, zeroValue;

	public void calculateOmegaValues() {

		switch (filterParams.filterBand) {
		case LOWPASS:
			omega1 = filterParams.lowPassFreq / getSampleRate() * 2. * Math.PI;
			// / sampleRate;
			zeroValue = -1.0;
			break;
		case HIGHPASS:
			omega1 = filterParams.highPassFreq / getSampleRate() * 2. * Math.PI;
			// / sampleRate;
			omega1 = Math.PI - omega1;
			zeroValue = +1.0;
			break;
		case BANDPASS:
			omega2 = filterParams.highPassFreq / getSampleRate() * 2. * Math.PI;
			// / sampleRate;
			omega3 = filterParams.lowPassFreq / getSampleRate()* 2. * Math.PI;
			// / sampleRate;
			omega1 = omega3 - omega2;
			zeroValue = 0.0;
			break;
		case BANDSTOP:
			omega2 = filterParams.highPassFreq / getSampleRate() * 2. * Math.PI;
			// / sampleRate;
			omega3 = filterParams.lowPassFreq / getSampleRate()* 2. * Math.PI;
			// / sampleRate;
			omega1 = (omega3 - omega2);
			zeroValue = 1.0;
			break;
		}
	}

	double mPiTerm(int m) {

		double dn = filterParams.filterOrder;
		double dm = m;
		if (filterParams.filterOrder % 2 == 0) // even filter order
			return (2.0 * dm + 1.0) * Math.PI / (2.0 * dn);
		else // odd filter order
			return dm * Math.PI / dn;
	}

	int doBandpassTransformation(Complex[] poles, Complex[] zeros, int nPoints) {
		// move the data into a couple of temporary arrays...
		Complex[] oldPoles = new Complex[poles.length];
		Complex[] oldZeros = new Complex[zeros.length];
		for (int i = 0; i < nPoints; i++) {
			oldPoles[i] = new Complex(poles[i]);
			oldZeros[i] = new Complex(zeros[i]);
		}
		if (poles.length != nPoints*2) {
			poles = Arrays.copyOf(poles, nPoints*2);
		}
		if (zeros.length != nPoints*2) {
			zeros = Arrays.copyOf(zeros, nPoints*2);
		}

		double A = Math.cos((omega3 + omega2) / 2.0)
				/ Math.cos((omega3 - omega2) / 2.0);
		int m, m1, m2;
		Complex FirstBit = new Complex();
		Complex SecondBit = new Complex();
		//		Complex temp;
		for (m = 0; m < nPoints; m++) {
			m1 = 2 * m;
			m2 = m1 + 1;
			FirstBit = new Complex(0.5 * A, 0).times(oldPoles[m]
					.plus(new Complex(1., 0)));
			//			temp = oldPoles[m].plus(new Complex(1, 0)).times(A);
			//			temp = temp.times(temp);
			//			temp = temp.minus(oldPoles[m]);
			//			temp = temp.times(0.25);
			// Copy of line at top of Lynn & Fuerst p 185 !
			SecondBit = (oldPoles[m].plus(1.).pow(2.).times(
					0.25 * Math.pow(A, 2.)).minus(oldPoles[m])).pow(0.5);

			poles[m1] = FirstBit.plus(SecondBit);
			zeros[m1] = new Complex(1, 0);
			poles[m2] = FirstBit.minus(SecondBit);
			zeros[m2] = new Complex(-1, 0);
		}

		return nPoints * 2;
	}
	int doBandStopTransformation(Complex[] poles, Complex[] zeros, int nPoints) {
		// move the data into a couple of temporary arrays...
		Complex[] oldPoles = new Complex[poles.length];
		Complex[] oldZeros = new Complex[zeros.length];
		for (int i = 0; i < nPoints; i++) {
			oldPoles[i] = new Complex(poles[i]);
			oldZeros[i] = new Complex(zeros[i]);
		}

		if (poles.length != nPoints*2) {
			poles = Arrays.copyOf(poles, nPoints*2);
		}
		if (zeros.length != nPoints*2) {
			zeros = Arrays.copyOf(zeros, nPoints*2);
		}
		/*
		 * Bandpass alpha term is
		 * A = Math.cos((omega3 + omega2) / 2.0)
		 *	/ Math.cos((omega3 - omega2) / 2.0);
		 */
		double A = Math.cos((omega3 + omega2) / 2.0)
				/ Math.cos((omega3 - omega2) / 2.0);
		int m, m1, m2;
		Complex FirstBit = new Complex();
		Complex SecondBit = new Complex();
		Complex firstBitZ, secondBitZ;
		Complex temp;
		for (m = 0; m < nPoints; m++) {
			m1 = 2 * m;
			m2 = m1 + 1;
			FirstBit = getFirstBit(oldPoles[m],A);
			temp = oldPoles[m].plus(new Complex(1, 0)).times(A);
			temp = temp.times(temp);
			temp = temp.minus(oldPoles[m]);
			temp = temp.times(0.25);
			// Copy of line at top of Lynn & Fuerst p 185 !
			SecondBit = getSecondBit(oldPoles[m], A);

			firstBitZ = getFirstBit(oldZeros[m], A);
			secondBitZ = getSecondBit(oldZeros[m], A);

			poles[m1] = FirstBit.plus(SecondBit);
			zeros[m1] = firstBitZ.plus(secondBitZ);
			poles[m2] = FirstBit.minus(SecondBit);
			zeros[m2] = firstBitZ.minus(secondBitZ);
		}

		return nPoints * 2;
	}
	private Complex getFirstBit(Complex z, double A) {
		return new Complex(0.5 * A, 0).times(z.plus(1));
	}
	private Complex getSecondBit(Complex z, double A) {
		return (z.plus(1.).pow(2.).times(
				0.25 * Math.pow(A, 2.)).minus(z)).pow(0.5);
	}

	/**
	 * Get coefficients from poles and zeros for fast filter computations. 
	 * @return
	 */
	@Override
	public double[] getFastFilterCoefficients() {
		Complex[] poles = getPoles(filterParams);
		Complex[] zeros = getZeros(filterParams);
		PoleZeroPair[] pzPairs = getPoleZeros();
		int oddBand = -1;
		/**
		 * These values when sorted perfectly match what you get from filter design in 
		 * Matlab, so seem to have got things right up to here, 
		 * Be wary of working out the number of pairs though. Have to work out
		 * based on filter order, then double for band and notch types. 
		 */
		int nPolePairs = filterParams.filterOrder/2;
		int nOddOnes = (filterParams.filterOrder)%2;
		if (filterParams.filterBand == FilterBand.BANDPASS || filterParams.filterBand == FilterBand.BANDSTOP) {
			nPolePairs *=2;
			nOddOnes *= 2;
		}
		nPolePairs = pzPairs.length/2;
		nOddOnes = pzPairs.length%2;
				
		if (filterParams.filterBand == FilterBand.BANDPASS) {
//			nPolePairs = poles.length/2;
//			nOddOnes = 0;
			if (filterParams.filterOrder%2 == 1) {
				oddBand =  filterParams.filterOrder-1;
			}
		}
		
		int nCoefficients = (nPolePairs+nOddOnes) * 4;
		double[] doubleCoefficients = new double[nCoefficients];
		double filterGain = getFilterGainConstant();

		double a1, a2, b1, b2;
		int j = 0;
//		int k = nPolePairs;
		Complex pole1, zero1, pole2, zero2;
		// poles are conjugate pairs, zeros are the same. 
		int i = 0, k = 0;
//		for (k=1; i < pzPairs.length-1; i+=2, k+=2) {
		for (i = 0, k = pzPairs.length-1; i < nPolePairs; i++, k--) {
			pole1 = pzPairs[i].getPole();
			zero1 = pzPairs[i].getZero();
			pole2 = pole1.conj();//[k].getPole();
			zero2 = zero1; //pzPairs[k].getZero();
			a1 = -(zero1.real+zero2.real); //xp1
			a2 = zero1.times(zero2).mag();  //xp2 real not 1 for band stop filter. 
			if (filterParams.filterBand == FilterBand.BANDPASS) {
				a2*= zero1.real*zero2.real; // flip sign of a2 if zeros on opposite ends. 
			}
			b1 = (pole1.real+pole2.real);  //yp1
			b2 = -pole1.times(pole2).mag(); //yp2
//			a1 = -2.*zeros[i/2].real; //xp1
//			a2 = zeros[i/2].magsq();  //xp2 
//			b1 = 2.*(poles[i/2].real);  //yp1
//			b2 = -poles[i/2].magsq(); //yp2
//			if (i == oddBand) {
//				/**
//				 * In an odd ordered band pass filter, the final 'pair' is not quite the conjugate
//				 * pair that the other have since the zero has opposite sign (for all other pairs, they
//				 * have the same sign. The poles are still a conjugate pair. 
//				 * for  a'normal' pair, the numerator of the biquad pair would be (1+q/z)(1+q/z) = 
//				 * (1+2q/z+q^2/z^2). For the odd bandpass we now have (1+q/z)(1-q/z) =
//				 * (1-q^2/z^2), which results in the A1 term being zero and the A2 term becoming 
//				 * it's negative (I never bother with A0 since it's always 1). 
//				 */
//				a1 = 0;
//				a2 = -a2;
//			}
			/*
			yp1 = P1.real * 2.;
			yp2 = -P1.magsq();
			xp1 = -Z1.real * 2.;
			xp2 = Z1.magsq();
			 */
			doubleCoefficients[j+0] = a1;
			doubleCoefficients[j+1] = a2;
			doubleCoefficients[j+2] = b1;
			doubleCoefficients[j+3] = b2;
			j+=4;
		}
		for (int odd = 0; odd < nOddOnes; odd++, i++) {
//		if (nOddOnes != 0) {
			pole1 = pzPairs[i].getPole();
			zero1 = pzPairs[i].getZero();
			if (pole1 == null || zero1 == null) {
				continue;
			}
			a1 = -zero1.real;
			b1 = pole1.real;
			doubleCoefficients[j+0] = a1;
			doubleCoefficients[j+1] = 0;
			doubleCoefficients[j+2] = b1;
			doubleCoefficients[j+3] = 0;
			j+=4;
		}
		//		for (i = 0; i < doubleCoefficients.length; i++) {
		//			doubleCoefficients[i]/=filterGain;
		//		}
		return doubleCoefficients;
	}
	/*
	 * Gain and phase
	 */
	@Override
	public double getFilterGain(double omega) {
		Complex ComplexFreq = new Complex(Math.cos(omega), Math.sin(omega));
		Complex ComplexLine;
		double FilterGain = 1.0;
		int j;
		if (poles == null || poles.length < poleZeroCount()) {
			return 1;
		}
		for (j = 0; j <poleZeroCount(); j++) {
			if (poles[j] == null) {
				return 1;
			}
			ComplexLine = poles[j].minus(ComplexFreq);
			FilterGain /= Math.sqrt(ComplexLine.norm());
			ComplexLine = zeros[j].minus(ComplexFreq);
			FilterGain *= Math.sqrt(ComplexLine.norm());
		}
		return FilterGain;
	}

	@Override
	public double getFilterPhase(double omega) {
		Complex ComplexFreq = new Complex(Math.cos(omega), Math.sin(omega));
		Complex ComplexLine;
		double FilterPhase = 0.0;
		int j;
		for (j = 0; j < poleZeroCount(); j++) {
			if (poles[j] == null) {
				return 0;
			}
			ComplexLine = poles[j].minus(ComplexFreq);
			FilterPhase -= ComplexLine.ang();
			ComplexLine = zeros[j].minus(ComplexFreq);
			FilterPhase += ComplexLine.ang();
		}
		return FilterPhase;
	}

	@Override
	public double getFilterGainConstant() {
		double Omega2, Omega3;
		double FilterGain;
		switch (filterParams.filterBand) {
		case LOWPASS:
			FilterGain = getFilterGain(0.0);
			break;
		case HIGHPASS:
			FilterGain = getFilterGain(Math.PI);
			break;
		case BANDPASS:
			Omega2 = filterParams.highPassFreq / getSampleRate() * 2.0 * Math.PI;
			// / (double) sampleRate;
			Omega3 = filterParams.lowPassFreq / getSampleRate() * 2.0 * Math.PI;
			// / (double) sampleRate;
			FilterGain = getFilterGain(Math.sqrt(Omega2 * Omega3));
			break;
		case BANDSTOP:
			Omega2 = filterParams.highPassFreq / getSampleRate() * 2.0 * Math.PI;
			// / (double) sampleRate;
			Omega3 = filterParams.lowPassFreq / getSampleRate() * 2.0 * Math.PI;
			// / (double) sampleRate;
			FilterGain = getFilterGain(Math.sqrt(0));
			break;
		default:
			return 0.0;
		}
		// if its a Chebyshev filter, its much harder !
		// better to be a bit big, so that signal doesn't saturate....
		if (filterParams.filterType == FilterType.CHEBYCHEV) {
			if (filterParams.filterBand == FilterBand.BANDSTOP)
				FilterGain *= Math
				.pow(10.0, filterParams.passBandRipple / 20.0);
			if (filterParams.filterOrder % 2 == 0)
				FilterGain *= Math
				.pow(10.0, filterParams.passBandRipple / 20.0);
		}
		return FilterGain;
	}
	/*
	 * end of gain and phase
	 */

	/* (non-Javadoc)
	 * @see Filters.FilterMethod#createFilter()
	 */
	@Override
	public Filter createFilter(int channel) {
//				return new IirfFilter(channel, getSampleRate(), getFilterParams());
		/*
		 * Fast IIR filter gives a speed increase of about *2.4 on my machine
		 * compared to the older IirfFilter method 
		 */
//		return new IirfFilter(channel, getSampleRate(), getFilterParams());
		return new FastIIRFilter(channel, getSampleRate(), getFilterParams());
	}
}
