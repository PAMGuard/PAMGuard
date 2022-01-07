package generalDatabase.external;

public enum ImportOption {
	
	DONOTHING, MERGERECORDS, DROPANDCOPY, NEWTABLE;

	@Override
	public String toString() {
		switch (this) {
		case MERGERECORDS:
			return "Copy new records";
		case DONOTHING:
			return "Do nothing";
		case DROPANDCOPY:
			return "Overwrite everything";
		case NEWTABLE:
			return "Copy as new table";
		default:
			return null;		
		}
	}
	
	
}
