package pamViewFX.fxNodes;


/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


import java.io.Serializable;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamColors;


/**
 * @author Doug Gillespie
 * 
 * Some standard colours to use for various bits of a view.
 * <p>
 * Ultimately, it should be possible to set these dynamically during operation or
 * have night / day settings, etc.
 * <p>
 * Any bit of the display can register with a single instance of PamColors and
 * it will then receive notifications whenever any of the colours change.
 * 
 */
public class PamColorsFX implements PamSettings {

	public static enum PamColor {
		PlOTWINDOW, BORDER, PLAIN, AXIS, GRID, MAP, WARNINGBORDER, BACKGROUND_ALPHA, HIGHLIGHT_ALPHA, HIGHLIGHT, 
		GPSTRACK, LATLINE, LONLINE, TITLEBORDER, BUTTONFACE
	};
	

	static private PamColorsFX singleInstance;
	
	/**
	 * Kept only so that the (unused, never registered) PamSettings implementation
	 * below still has something to hand back. The colours actually returned by this
	 * class all come from {@link PamColors}, which is where the user selects the
	 * colour scheme and where it is saved with the settings.
	 */
	private ColorSchemeFX colourScheme = null;//ColourScheme.createDefaultDayScheme();


	private ColorSettingsFX colorSettings = new ColorSettingsFX();

	
	private PamColorsFX() {

		colourScheme = colorSettings.getScheme(0);
		
		//PamSettingManager.getInstance().registerSettings(this, PamSettingManager.LIST_SYSTEMGLOBAL);
	}


	
	static public PamColorsFX getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamColorsFX();
		}
		return singleInstance;
	}

	public void notifyModelChanged(int changeType) {
		if (PamController.getInstance().isInitializationComplete() == false) {
			return;
		}
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.INITIALIZATION_COMPLETE:
		case PamControllerInterface.CHANGED_DISPLAY_SETTINGS:
			SwingUtilities.invokeLater(new SetColoursLater());
		}
	}
	
	class SetColoursLater implements Runnable {

		@Override
		public void run() {
			setColors();
		}
		
	}

	public void setColors() {
		notifyAllComponents();

//		javax.swing.UIManager.put("ScrollBar.background", new javax.swing.plaf.ColorUIResource(255,0,0));
//		javax.swing.UIManager.put("ScrollBar.highlight", new javax.swing.plaf.ColorUIResource(0,255,0));
//		javax.swing.UIManager.put("Button.foreground", new javax.swing.plaf.ColorUIResource(0,0,255));
	}
	
	private void notifyAllComponents() {
//		PamController pc = PamController.getInstance();
//		if (pc == null) {
//			return;
//		}
//		int nG = pc.getGuiManagerFX().getNumFrames();
//		for (int i = 0; i < nG; i++) {
//			//FIXME - causes issues on startup
//			//notifyContianer(pc.getGuiManagerFX().getFrame(i));
//		}
	}

	/**
	 * Translate one of the JavaFX colour keys to the equivalent Swing one. The two
	 * enums are kept in step by name; the Swing one has a couple of extra values
	 * which have no JavaFX equivalent.
	 *
	 * @param col JavaFX colour key
	 * @return the matching Swing colour key, or PLAIN if there isn't one.
	 */
	private static PamView.PamColors.PamColor toSwingColour(PamColor col) {
		try {
			return PamView.PamColors.PamColor.valueOf(col.name());
		}
		catch (IllegalArgumentException e) {
			return PamView.PamColors.PamColor.PLAIN;
		}
	}

	/**
	 * Get a colour for the currently selected colour scheme.
	 * <p>
	 * This delegates to {@link PamColors} rather than keeping a second, parallel set
	 * of colour schemes: {@link PamColors} is the one the user actually selects
	 * schemes in (Display / Colour Scheme, and the dark mode toggles), and it is the
	 * one which is saved with the settings, so anything drawn from a separate table
	 * here would simply ignore the user's choice.
	 *
	 * @param col the colour key
	 * @return the colour to use.
	 */
	public Color getColor(PamColor col) {
		java.awt.Color awtColour = PamColors.getInstance().getColor(toSwingColour(col));
		if (awtColour == null) {
			return Color.DARKGRAY;
		}
		return awtColorToFx(awtColour);
	}

	public Color getForegroudColor(PamColor col) {
		return getColor(PamColor.AXIS);
	}

	public Color getWhaleColor(int col) {
		return awtColorToFx(PamColors.getInstance().getWhaleColor(col));
	}

	public Color getChannelColor(int iChan) {
		return awtColorToFx(PamColors.getInstance().getChannelColor(iChan));
	}

	static private Font boldFont;
	public Font getBoldFont() {
		if (boldFont == null) {
			boldFont = new Font("system", 12);
		}
		return boldFont;
	}
	
	public Serializable getSettingsReference() {
		return colorSettings;
	}

	public long getSettingsVersion() {
		return ColorSettingsFX.serialVersionUID;
	}

	public String getUnitName() {
		return "Pam Color Manager";
	}

	public String getUnitType() {
		return "Pam Color Manager";
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		ColorSettingsFX newSettings = (ColorSettingsFX) pamControlledUnitSettings.getSettings();
		this.colorSettings = newSettings.clone();
		colourScheme = colorSettings.selectScheme(colorSettings.getCurrentScheme());
		setColors();	
		return true;
	}

	public ColorSettingsFX getColorSettings() {
		return colorSettings;
	}
	
	/**
	 * Interpret a colour string of the type used in Logger forms.
	 * <p>These can take two basic formats, first a colour name (e.g. blue)
	 * or a RGB code in the format RGB(RRR,GGG,BBB) where RRR, GGG and BBB
	 * are integer colour codes for red, green and blue each of which must lie
	 * between 0 and 255.
	 * @param colString Colour string
	 * @return colour or null if the colour cannot be interpreted. 
	 */
	public static Color interpretColourString(String colString) {
		if (colString == null) {
			return null;
		}
		colString = colString.toUpperCase();
		if (colString.equals("RED")) {
			return Color.RED;
		}
		else if (colString.equals("BLACK")) {
			return Color.BLACK;
		}
		else if (colString.equals("BLUE")) {
			return Color.BLUE;
		}
		else if (colString.equals("CYAN")) {
			return Color.CYAN;
		}
		else if (colString.equals("DARK_GRAY")) {
			return Color.DARKGRAY;
		}
		else if (colString.equals("GRAY")) {
			return Color.GRAY;
		}
		else if (colString.equals("GREEN")) {
			return Color.GREEN;
		}
		else if (colString.equals("LIGHT_GRAY")) {
			return Color.LIGHTGRAY;
		}
		else if (colString.equals("MAGENTA")) {
			return Color.MAGENTA;
		}
		else if (colString.equals("ORANGE")) {
			return Color.ORANGE;
		}
		else if (colString.equals("PINK")) {
			return Color.PINK;
		}
		else if (colString.equals("WHITE")) {
			return Color.WHITE;
		}
		else if (colString.equals("YELLOW")) {
			return Color.YELLOW;
		}
		
		Color aCol = null;
		try {
			aCol = Color.valueOf(colString);
		}
		catch (Exception e) {
			aCol = null;
		}
		if (aCol == null) {
			aCol = decodeColour(colString);
		}
		return aCol;
	}
	
	/**
	 * Assumes a formatted string of the form (rrr,ggg,bbb)
	 * @param colString
	 * @return a color or null if string not interpreted. 
	 */
	private static Color decodeColour(String colString) {
		String nums = colString.replaceAll( "[^\\d]", " " );
		Scanner scanner = new Scanner(nums);		
		int[] cols = new int[3];
		try {
			for (int i = 0; i < 3; i++) {
				cols[i] = scanner.nextInt();
			}
		}
		catch (Exception ex) {
			return null;
		}
		return Color.rgb(cols[0], cols[1], cols[2]);
	}
	
	/**
	 * Get a colour string in the format (RRR,GGG,BBB)
	 * @param col Colour
	 * @return null if col is null or formatted string. 
	 */
	public static String getLoggerColourString(Color col) {
		if (col == null) {
			return null;
		}
		return String.format("(%d,%d,%d)", col.getRed(), col.getGreen(), col.getBlue());
	}
	
	/**
	 * Get the standard border colour
	 * @return border colour.
	 */
	public Color getBorderColour() {
		return getColor(PamColor.BORDER);
	}
	public Color getGPSColor() {
		return getColor(PamColor.GPSTRACK);
	}

	public int getNWhaleColours() {
		return PamColors.getInstance().getNWhaleColours();
	}

	public int getWhaleColourIndex(int iCol) {
		return PamColors.getInstance().getWhaleColourIndex(iCol);
	}

	/**
	 * Get a counter which increments every time the colour scheme or the colour
	 * blind palette changes. Anything which copies colours out of here (rather than
	 * asking for them as it draws) can compare this against the version it last
	 * read to find out whether it is out of date. See
	 * {@link PamColors#getColourSchemeVersion()}.
	 *
	 * @return the current colour scheme version.
	 */
	public int getColourSchemeVersion() {
		return PamColors.getInstance().getColourSchemeVersion();
	}

	/**
	 * @return the colourScheme
	 */
	public ColorSchemeFX getColourScheme() {
		return colourScheme;
	}

	public static Color awtColorToFx(java.awt.Color color) {
		Color col = Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()/255.);
		return col;
	}
	
}
