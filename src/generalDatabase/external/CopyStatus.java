package generalDatabase.external;

public enum CopyStatus {
	NOTSTARTED, COPYING, DONE, FAILED, SKIPPED;

	@Override
	public String toString() {
		switch (this) {
		case NOTSTARTED:
			return "Not Started";
		case COPYING:
			return "NCopying";
		case DONE:
			return "Copy Complete";
		case FAILED:
			return "Copy Failed";
		case SKIPPED:
			return "Skipped";
		}
		return null;
	}
}
