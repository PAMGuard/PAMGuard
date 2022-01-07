package binaryFileStorage;

/**
 * Where to store data for detectors which have a specific noise measured output 
 * i.e. the datablock has a BackgroundManager which will be producing regular noise
 * measures which will want storing somewhere. <br>
 * Can either store in the pamguard data files, which will make those files incompatible
 * with previous data, or in pgnf files where the noise will set by iteself. 
 * @author Doug Gillespie
 *
 */
public enum NoiseStoreType {

	PGDF, PGNF;

	@Override
	public String toString() {
		switch (this) {
		case PGDF:
			return "PAMGuard binary data files";
		case PGNF:
			return "PAMGuard noise files";
		default:
			break;
		
		}
		return super.toString();
	}
	
	public String getToolTip() {
		switch (this) {
		case PGDF:
			return "Background measures are stored in pgdf files along with detection data";
		case PGNF:
			return "Background measures are stored in separate noise files";
		default:
			return null;
		
		}
	}
	
}
