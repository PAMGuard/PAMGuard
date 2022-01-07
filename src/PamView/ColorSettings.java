package PamView;

import java.io.Serializable;
import java.util.ArrayList;

public class ColorSettings implements Serializable, Cloneable{

		static public final long serialVersionUID = 1;
		
		private ArrayList<ColourScheme> colourSchemes = new ArrayList<>();
		
		private String currentScheme;
		
		public static final int ACCESSIBLE_95 = 0;
		public static final int ACCESSIBLE_99 = 1;
		public static final int ACCESSIBLE_999 = 2;
		
		private int colourBlindPalet = ACCESSIBLE_95; 

		public ColorSettings() {
			super();
			rebuildSchemes(colourBlindPalet);
			
		}
		
		public void rebuildSchemes(int colourBlindPalet2) {
			colourBlindPalet = colourBlindPalet2;
			colourSchemes.clear();
			colourSchemes.add(ColourScheme.createDefaultDayScheme(colourBlindPalet2));
			colourSchemes.add(ColourScheme.createDefaultNightScheme(colourBlindPalet2));
			colourSchemes.add(ColourScheme.createDefaultPrintScheme(colourBlindPalet2));
		}

		/**
		 * Get, but don't select a colour scheme. 
		 * @param schemeIndex
		 * @return Colour Scheme. 
		 */
		public ColourScheme getScheme(int schemeIndex) {
			return colourSchemes.get(schemeIndex);
		}
		
		/**
		 * Get the number of colour schemes. 
		 * @return the number of colour schemes. 
		 */
		public int getNumSchemes() {
			return colourSchemes.size();
		}
		
		public ColourScheme selectScheme(int schemeIndex) {
			if (schemeIndex >= colourSchemes.size()) {
				currentScheme =  colourSchemes.get(0).getName();
				return colourSchemes.get(0);
			}
			else {
				currentScheme =  colourSchemes.get(schemeIndex).getName();
				return colourSchemes.get(schemeIndex);
			}
		}
		
		public ColourScheme selectScheme(String schemeName) {
			if (schemeName == null) {
				return selectScheme(0);
			}
			for (ColourScheme cs:colourSchemes) {
				if (cs.getName().equalsIgnoreCase(schemeName)) {
					currentScheme = cs.getName();
					return cs;
				}
			}
			return selectScheme(0);
		}

		@Override
		protected ColorSettings clone() {
			try {
				ColorSettings newSettings = (ColorSettings) super.clone();
				if (colourSchemes == null || colourSchemes.size() == 0) {
					newSettings = new ColorSettings();
				}
				return newSettings;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return new ColorSettings();
		}

		public String getCurrentScheme() {
			return currentScheme;
		}

		/**
		 * @return the colourBlindPalet
		 */
		public int getColourBlindPalet() {
			return colourBlindPalet;
		}

		/**
		 * @param colourBlindPalet the colourBlindPalet to set
		 */
		public void setColourBlindPalet(int colourBlindPalet) {
			this.colourBlindPalet = colourBlindPalet;
		}
		
		public String getColourBlindName() {
			return getColourBlindName(colourBlindPalet);
		}
		
		public static String getColourBlindName(int scheme) {
			switch (scheme) {
			case ACCESSIBLE_95:
				return "PAMGuard default: 95% accessibility";
			case ACCESSIBLE_99:
				return "Colour blind: 99% accessibility";
			case ACCESSIBLE_999:
				return "Colour blind: 99.9% accessibility";
			}
			return null;
		}
		
}
