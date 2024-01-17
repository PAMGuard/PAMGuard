package tethys.deployment;

/**
 * Class to handle instrument information
 * @author dg50
 *
 */
public class PInstrument {

	public String instrumentType;
	
	public String instrumentId;

	public PInstrument(String instrumentType, String instrumentId) {
		super();
		this.instrumentType = instrumentType;
		this.instrumentId = instrumentId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PInstrument == false) {
			return false;
		}
		PInstrument other = (PInstrument) obj;
		boolean eq = true;
		if (this.instrumentType != null) {
			eq &= this.instrumentType.equals(other.instrumentType);
		}
		if (this.instrumentId != null) {
			eq &= this.instrumentId.equals(other.instrumentId);
		}
		if (other.instrumentType != null) {
			eq &= other.instrumentType.equals(this.instrumentType);
		}
		if (other.instrumentId != null) {
			eq &= other.instrumentId.equals(this.instrumentId);
		}
		
		return eq;
		
	}

	@Override
	public String toString() {
		return String.format("%s : %s", instrumentType == null ?  "Undefined" : instrumentType, instrumentId);
	}
	
}
