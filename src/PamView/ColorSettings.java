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

		/**
		 * Use the standard Windows look and feel rather than FlatLaf. Windows only, and
		 * off by default - FlatLaf is the standard PAMGuard look. See
		 * {@link PamLookAndFeel#setWindowsLookAndFeel(boolean)}.
		 * <p>
		 * Not present in settings written by older versions, where it deserialises as
		 * false, which is what those versions did on macOS and Linux. Windows users
		 * upgrading will see FlatLaf where they used to see the Windows look and feel,
		 * and can turn this back on from the Display menu.
		 */
		private boolean windowsLookAndFeel = false;

		public ColorSettings() {
			super();
			rebuildSchemes(colourBlindPalet);
			
		}
		
		public void rebuildSchemes(int colourBlindPalet2) {
			colourBlindPalet = colourBlindPalet2;
			if (colourSchemes == null) {
				colourSchemes = new ArrayList<>();
			}
			colourSchemes.clear();
			colourSchemes.add(ColourScheme.createDefaultDayScheme(colourBlindPalet2));
			colourSchemes.add(ColourScheme.createDefaultDarkScheme(colourBlindPalet2));
			colourSchemes.add(ColourScheme.createDefaultNightScheme(colourBlindPalet2));
			colourSchemes.add(ColourScheme.createDefaultPrintScheme(colourBlindPalet2));
		}

		/**
		 * Make sure the list of schemes holds every scheme this version of PAMGuard
		 * knows about. Settings serialised by an older version will be missing any
		 * schemes added since, so rebuild the list if any are absent. The name of the
		 * currently selected scheme is preserved.
		 */
		public void checkSchemes() {
			String[] wanted = {ColourScheme.DAYSCHEME, ColourScheme.DARKSCHEME,
					ColourScheme.NIGHTSCHEME, ColourScheme.PRINTSCHEME};
			if (colourSchemes != null && colourSchemes.size() == wanted.length) {
				boolean allThere = true;
				for (String name : wanted) {
					if (findScheme(name) == null) {
						allThere = false;
						break;
					}
				}
				if (allThere) {
					return;
				}
			}
			rebuildSchemes(colourBlindPalet);
		}

		/**
		 * Find a scheme by name without selecting it.
		 * @param schemeName scheme name
		 * @return the scheme, or null if there isn't one of that name.
		 */
		public ColourScheme findScheme(String schemeName) {
			if (schemeName == null || colourSchemes == null) {
				return null;
			}
			for (ColourScheme cs:colourSchemes) {
				if (cs.getName().equalsIgnoreCase(schemeName)) {
					return cs;
				}
			}
			return null;
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
					newSettings.setWindowsLookAndFeel(windowsLookAndFeel);
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
		
		/**
		 * @return true if the standard Windows look and feel is to be used in place of
		 *         FlatLaf.
		 */
		public boolean isWindowsLookAndFeel() {
			return windowsLookAndFeel;
		}

		/**
		 * @param windowsLookAndFeel true to use the standard Windows look and feel in
		 *                           place of FlatLaf.
		 */
		public void setWindowsLookAndFeel(boolean windowsLookAndFeel) {
			this.windowsLookAndFeel = windowsLookAndFeel;
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
