package tethys.localization;

/**
 * Enumeration of the Name field in nilus.Localize.Effort.CoordinateReferenceSystem<br>
 * Within Tethys, these are only ever used in their String form. 
 * @author dg50
 *
 */
public enum CoordinateName {
	WGS84, UTM, Cartesian, Polar, Spherical, Cylindrical, Range, PerpendicularRange;
	/*
	 * I beleive there is only one LocalizationSubType for each of these. 
	 * Cartesian onwards is always engineering subtype
	 * WGS84 must be geographic
	 * UTM must be derived. 
	 */
	
	/*
	 * Localizationtype is more confusing
	 * e.g. for WGS84, LocalizationType might be point or track. 
	 * UTM can be point or track. 
	 * The most of the others  
	 *
	 */
	
	public LocalizationSubTypes getSubType() {
		switch (this) {
		case Cartesian:
		case Cylindrical:
		case PerpendicularRange:
		case Polar:
		case Range:
		case Spherical:
			return LocalizationSubTypes.Engineering;
		case UTM:
			return LocalizationSubTypes.Derived;
		case WGS84:
			return LocalizationSubTypes.Geographic;
		default:
			break;		
		}
		return null;
	}
}
