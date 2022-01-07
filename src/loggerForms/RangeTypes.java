package loggerForms;

public enum RangeTypes {

	FIXED, VARIABLE;
	
	public static RangeTypes getValue(String val) {
		if (val == null) {
			return null;
		}
		try {
			return valueOf(val.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Illegal range type argument: " + val);
			return null;
		}
	}
}
