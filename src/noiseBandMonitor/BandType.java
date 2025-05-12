package noiseBandMonitor;

public enum BandType {
	OCTAVE, THIRDOCTAVE, DECIDECADE, DECADE, TENTHOCTAVE, TWELTHOCTAVE;

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		switch(this) {
		case OCTAVE:
			return "Octave";
		case THIRDOCTAVE:
			return "Third Octave";
		case DECIDECADE:
			return "Deci Decade";
		case DECADE:
			return "Decade Bands";
		case TENTHOCTAVE:
			return "Tenth Octave";
		case TWELTHOCTAVE:
			return "Twelth Octave";
		}
		return null;
	}
	
	public String description() {
		switch(this) {
		case OCTAVE:
			return "ANSI S1.11 Octave Bands";
		case THIRDOCTAVE:
			return "ANSI S1.11 Third Octave Bands";
		case DECIDECADE:
			return "Deci Decade: Very similar to Third Octave Bands, but aligned on decadal frequencies";
		case DECADE:
			return "Decadal (x10) frequency bands";
		case TENTHOCTAVE:
			return "Tenth Octave Bands";
		case TWELTHOCTAVE:
			return "Twelth Octave Bands";
		}
		return null;
	}
	
	/**
	 * Get the number of frequency bands per octave. 
	 * @return
	 */
//	public double bandsPerOctave() {
//		switch (this) {
//		case OCTAVE:
//			return 2;
//		case THIRDOCTAVE:
//			return 3;
//		case DECIDECADE:
//			return .1/Math.log10(2);
//		case TENTHOCTAVE:
//			return 10;
//		case TWELTHOCTAVE:
//			return 12;
//		default:
//			break;
//		}
//		return 0;
//	}
	
//	/**
//	 * Is it a standard band - this means 1/3 octave or octave. Tenth and other
//	 * band types need a different calculation in the BandData class. 
//	 * @return
//	 */
//	public boolean standardBand() {
//		switch (this) {
//		case OCTAVE:
//		case THIRDOCTAVE:
//		case DECIDECADE:
//			return true;
//		case TENTHOCTAVE:
//		case TWELTHOCTAVE:
//			return false;
//		default:
//			break;
//		}
//		return false;
//	}
	
	/**
	 * Ratio between centres of adjacent bands. 
	 * @return
	 */
	public double getBandRatio() {
		switch(this) {
		case OCTAVE:
			return 2;
		case THIRDOCTAVE:
			return Math.pow(2, 1./3.);
		case DECIDECADE:
			return Math.pow(10, 0.1);
		case DECADE:
			return 10.;
		case TENTHOCTAVE:
			return Math.pow(2, .1);
		case TWELTHOCTAVE:
			return Math.pow(2, 1./12.);
		}
		return 0;
//		return Math.pow(2., 1./(double) bandsPerOctave());
	}
	
//	/**
//	 * These are referring to some ANSI standard 1/3 octave bands, 
//	 * so only apply to octave and 1/3 octave bands. The step for 1/3 is 
//	 * 1 and for octave is 3, i.e. taking every third centre frequency. 
//	 * @return
//	 */
//	public int getBandStep() {
//		switch(this) {
//		case OCTAVE:
//			return 3;
//		case THIRDOCTAVE:
//			return 1;
//		case DECIDECADE:
//			return .1/Math.log10(2);
//		case TENTHOCTAVE:
//			return -3;
//		case TWELTHOCTAVE:
//			return -3;
//		}
//		return 0;		
//	}
}
