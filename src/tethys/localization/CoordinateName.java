package tethys.localization;

/**
 * Enumeration of the Name field in nilus.Localize.Effort.CoordinateReferenceSystem<br>
 * Within Tethys, these are only ever used in their String form. 
 * @author dg50
 *
 */
public enum CoordinateName {
	WGS84, UTM, Cartesian, Polar, Spherical, Cylindrical, Range, PerpindicularRange;
}
