package pamViewFX.fxNodes;

import java.io.Serializable;
import java.util.ArrayList;

public class ColorSettingsFX implements Serializable, Cloneable{

	static public final long serialVersionUID = 1;

	private ArrayList<ColorSchemeFX> colourSchemes = new ArrayList<>();

	private String currentScheme;


	public ColorSettingsFX() {
		super();
		colourSchemes.add(ColorSchemeFX.createDefaultDayScheme());
		colourSchemes.add(ColorSchemeFX.createDefaultNightScheme());
		colourSchemes.add(ColorSchemeFX.createDefaultPrintScheme());
	}

	/**
	 * Get, but don't select a colour scheme. 
	 * @param schemeIndex
	 * @return Colour Scheme. 
	 */
	public ColorSchemeFX getScheme(int schemeIndex) {
		return colourSchemes.get(schemeIndex);
	}

	/**
	 * Get the number of colour schemes. 
	 * @return the number of colour schemes. 
	 */
	public int getNumSchemes() {
		return colourSchemes.size();
	}

	public ColorSchemeFX selectScheme(int schemeIndex) {
		if (schemeIndex >= colourSchemes.size()) {
			currentScheme =  colourSchemes.get(0).getName();
			return colourSchemes.get(0);
		}
		else {
			currentScheme =  colourSchemes.get(schemeIndex).getName();
			return colourSchemes.get(schemeIndex);
		}
	}

	public ColorSchemeFX selectScheme(String schemeName) {
		if (schemeName == null) {
			return selectScheme(0);
		}
		for (ColorSchemeFX cs:colourSchemes) {
			if (cs.getName().equalsIgnoreCase(schemeName)) {
				currentScheme = cs.getName();
				return cs;
			}
		}
		return selectScheme(0);
	}

	@Override
	public ColorSettingsFX clone() {
		try {
			ColorSettingsFX newSettings = (ColorSettingsFX) super.clone();
			if (colourSchemes == null || colourSchemes.size() == 0) {
				newSettings = new ColorSettingsFX();
			}
			return newSettings;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return new ColorSettingsFX();
	}

	public String getCurrentScheme() {
		return currentScheme;
	}

}

