package noiseBandMonitor;

public enum BandType {
	
	OCTAVE, THIRDOCTAVE, DECIDECADE, DECADE, TENTHOCTAVE, TWELTHOCTAVE;

	/* (non-Javadoc)
	 * Band name to use in drop down lists. 
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
			return "Decade";
		case TENTHOCTAVE:
			return "Tenth Octave";
		case TWELTHOCTAVE:
			return "Twelth Octave";
		}
		return null;
	}
	
	/**
	 * For Decade bands, decimate by a factor 10 for each band. For all others
	 * decimate by a factor 2. 
	 * @return
	 */
	public int getDecimateFactor() {
		switch (this) {
		case DECIDECADE:
		case OCTAVE:
		case TENTHOCTAVE:
		case THIRDOCTAVE:
		case TWELTHOCTAVE:
			return 2;
		case DECADE:
			return 10;
		default:
			break;
		}
		return 2;
	}
	
	/**
	 * Longer description to use in tool tips. 
	 * @return
	 */
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
			return "Tenth Octave Bands (warning - slow to process)";
		case TWELTHOCTAVE:
			return "Twelth Octave Bands (warning - slow to process)";
		}
		return null;
	}
	
	
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
	}
	
}
