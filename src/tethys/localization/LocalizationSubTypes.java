package tethys.localization;
/**
 * Enumeration of the ReferenceFrame field in nilus.Localize.Effort.CoordinateReferenceSystem.Subtype <br>
 * Within Tethys, these are only ever used in their String form. 
 * @author dg50
 *
 */
public enum LocalizationSubTypes {
	Geographic, Derived, Engineering;
	/*
	 * Geographic is WGS84
	 * Derived is something like UTM, but is basically geographic
	 * Engineering is relative to platform or fixed point. 
	 */
}
