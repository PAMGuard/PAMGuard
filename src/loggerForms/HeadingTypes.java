package loggerForms;

public enum HeadingTypes {

	RELATIVE1, RELATIVE2, TRUE, MAGNETIC;
	
	public static HeadingTypes getValue(String val) {
		if (val == null) {
			return null;
		}
		try {
			return valueOf(val.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Illegal heading type argument: " + val);
			return null;
		}
	}
}
