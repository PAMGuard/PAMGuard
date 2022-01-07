package Filters;

import fftManager.Complex;

public class PoleZeroPair implements Comparable<PoleZeroPair> {

	private Complex pole, zero;
	private boolean oddOne = false; 

	public PoleZeroPair(Complex pole, Complex zero, boolean oddOne) {
		super();
		this.pole = pole;
		this.zero = zero;
		this.oddOne = oddOne;
	}

	/**
	 * @return the pole
	 */
	public Complex getPole() {
		return pole;
	}

	/**
	 * @param pole the pole to set
	 */
	public void setPole(Complex pole) {
		this.pole = pole;
	}

	/**
	 * @return the zero
	 */
	public Complex getZero() {
		return zero;
	}

	/**
	 * @param zero the zero to set
	 */
	public void setZero(Complex zero) {
		this.zero = zero;
	}

	@Override
	public int compareTo(PoleZeroPair o) {
		if (o == null) {
			return 1;
		}
		if (o.oddOne) {
			return -1;
		}
		if (this.oddOne) {
			return 1;
		}
		/*
		 * The zeros are nearly always the same at 1 or -1 depending on filter. What's of most
		 * interest is to have the imaginary part of the pole in reverse order. 
		 */
		double d = pole.imag-o.pole.imag;
		if (d != 0) {
			return (int) Math.signum(-d);
		}
		int ans = pole.compareTo(o.pole);
		if (ans == 0) {
			ans = zero.compareTo(o.zero);
		}
		return ans;
	}

	@Override
	public String toString() {
		return String.format("(P%s,Z%s)", pole, zero);		
	}

	/**
	 * OddOne is set to true if this was an odd ordered high or low pass
	 * filter, in which case it won't have conjugate pair. 
	 * @return the oddOne
	 */
	public boolean isOddOne() {
		return oddOne;
	}

	/**
	 * @param oddOne the oddOne to set
	 */
	public void setOddOne(boolean oddOne) {
		this.oddOne = oddOne;
	}

}
