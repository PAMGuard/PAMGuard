package loggerForms;

public enum BearingTypes {
	RELATIVE1, RELATIVE2, TRUE, MAGNETIC;
	
	public static BearingTypes getValue(String val) {
		if (val == null) {
			return null;
		}
		try {
			return valueOf(val.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Illegal bearing type argument: " + val);
			return null;
		}
	}
}
