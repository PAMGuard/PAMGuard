package generalDatabase.external;

public enum CopyTypes {
	
	EXPORT, IMPORT;
	
	public String getName() {
		switch (this) {
		case EXPORT:
			return "Export";
		case IMPORT:
			return "Import";
		}
		return null;
	}
}
