package noiseBandMonitor;

public enum BandType {
	OCTAVE, THIRDOCTAVE;

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		switch(this) {
		case OCTAVE:
			return "Octave";
		case THIRDOCTAVE:
			return "ThirdOctave";
//		case TWELTHOCTAVE:
//			return "TwelthOctave";
		}
		return null;
	}
	
	public double getBandRatio() {
		switch(this) {
		case OCTAVE:
			return Math.pow(2,1./2.);
		case THIRDOCTAVE:
			return Math.pow(2,1./6.);
//		case TWELTHOCTAVE:
//			return Math.pow(2,1./12.);
		}
		return 0;
	}
	
	public int getBandStep() {
		switch(this) {
		case OCTAVE:
			return 3;
		case THIRDOCTAVE:
			return 1;
//		case TWELTHOCTAVE:
//			return 1;
		}
		return 0;		
	}
}
