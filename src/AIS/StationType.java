package AIS;

public enum StationType {
	UNKNOWN, A, B, BASESTATION, NAVAID;


	@Override
	public String toString() {
		switch(this) {
		case UNKNOWN:
			return "Unknown";
		case A:
			return "Class A";
		case B:
			return "Class B";
		case BASESTATION:
			return "Base Station";
		case NAVAID:
			return "Aid to Navigation";
		}
		return null;
	}
}
