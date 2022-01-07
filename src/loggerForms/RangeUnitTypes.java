package loggerForms;

public enum RangeUnitTypes {
	nmi, km, m, pix;
	
	public static RangeUnitTypes getValue(String val) {
		if (val == null) {
			return null;
		}
		try {
			return valueOf(val.toLowerCase());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Illegal range unit type argument: " + val);
			return null;
		}
	}
}
