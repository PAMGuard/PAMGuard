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
package PamView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamGUIManager;
import PamController.PamSettingManager;
import PamController.PamSettings;


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
public class PamColors implements PamSettings {

	public static enum PamColor {
		PlOTWINDOW, BORDER, PLAIN, AXIS, GRID, MAP, WARNINGBORDER, BACKGROUND_ALPHA, HIGHLIGHT_ALPHA, HIGHLIGHT, 
		GPSTRACK, LATLINE, LONLINE, TITLEBORDER, BUTTONFACE, EDITCTRL
	}
	

	static private PamColors singleInstance;
	
	private ColourScheme colourScheme = null;//ColourScheme.createDefaultDayScheme();
	
	private JCheckBoxMenuItem[] colourMenuItems;

//	private MenuItemEnabler nightMenuEnabler = new MenuItemEnabler(); 
//	private MenuItemEnabler dayMenuEnabler = new MenuItemEnabler(); 
//	private MenuItemEnabler printMenuEnabler = new MenuItemEnabler(); 
	
	private ColorSettings colorSettings = new ColorSettings();

	
	private PamColors() {
			
		colourScheme = colorSettings.getScheme(0);
		
		PamSettingManager.getInstance().registerSettings(this, PamSettingManager.LIST_SYSTEMGLOBAL);
	}

	public JMenuItem getMenu(JFrame parentFrame) {

		JMenu colorMenu = new JMenu("Color Scheme");
		JMenuItem menuItem;
		
		ColourScheme currentScheme = getColourScheme();
		String currSchemeName = "";
		if (currentScheme != null) {
			currSchemeName = currentScheme.getName();
		}

		int n = colorSettings.getNumSchemes();
		colourMenuItems = new JCheckBoxMenuItem[n];
		for (int i = 0; i < n; i++) {
			String name = colorSettings.getScheme(i).getName();
			colourMenuItems[i] = new JCheckBoxMenuItem(name);
			colourMenuItems[i].addActionListener(new SelectScheme(name));
			colorMenu.add(colourMenuItems[i]);
			colourMenuItems[i].setSelected(name.equalsIgnoreCase(currSchemeName));
		}
		JMenu blindMenu = new JMenu("Colour Pallets");
		colorMenu.add(blindMenu);
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			int colourBlind = i;
			JCheckBoxMenuItem blindItem = new JCheckBoxMenuItem(ColorSettings.getColourBlindName(i));
			if (currentScheme != null) {
				blindItem.setSelected(i == colorSettings.getColourBlindPalet());
			}
			bg.add(blindItem);
			blindItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setBlindPalet(colourBlind);
				}
			});
			blindMenu.add(blindItem);
		}
		
		return colorMenu;
	}
	
	protected void setBlindPalet(int selected) {
		colorSettings.setColourBlindPalet(selected);
		colorSettings.rebuildSchemes(selected);

		colourScheme = colorSettings.selectScheme(colorSettings.getCurrentScheme());
	}

//	/**
//	 * Called to fix some configs that seem to have messed up / lost come colours, e.g. on map
//	 */
//	protected void resetDefaultColours() {
//		colorSettings = new ColorSettings();
//		colourScheme = colorSettings.getScheme(0);
//	}

	private class SelectScheme implements ActionListener {

		private String schemeName;
		
		public SelectScheme(String schemeName) {
			super();
			this.schemeName = schemeName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			colourScheme = colorSettings.selectScheme(schemeName);
			setColors();
			if (colourMenuItems != null) {
				for (int i = 0; i < colourMenuItems.length; i++) {
					colourMenuItems[i].setSelected(colourMenuItems[i].getText().equals(schemeName));
				}
			}
		}
		
	}
	
	private class EditWhaleColours implements ActionListener {

		private JFrame parentFrame;

		public EditWhaleColours(JFrame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Color[] wCols = colourScheme.getWhaleColors();
//			Color[] newCols = ColourListDialog.show(parentFrame, "Whale Colours", wCols);
//			if (newCols) {
//				colourScheme.setWhaleColors(newCols);
//				setColors();
//			}
		}
	}
	private class EditChannelColours implements ActionListener {

		private JFrame parentFrame;

		public EditChannelColours(JFrame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Color[] wCols = colourScheme.getChannelColors();
//			Color[] newCols = ColourListDialog.show(parentFrame, "Whale Colours", wCols);
//			if (newCols) {
//				colourScheme.setChannelColors(newCols);
//				setColors();
//			}
		}
	}
	
	static public PamColors getInstance() {
		if (singleInstance == null) {
			singleInstance = new PamColors();
		}
		return singleInstance;
	}

	public void notifyModelChanged(int changeType) {
		if (!PamController.getInstance().isInitializationComplete()) {
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
		if (PamGUIManager.isSwing()) notifyAllComponents();

//		javax.swing.UIManager.put("ScrollBar.background", new javax.swing.plaf.ColorUIResource(255,0,0));
//		javax.swing.UIManager.put("ScrollBar.highlight", new javax.swing.plaf.ColorUIResource(0,255,0));
//		javax.swing.UIManager.put("Button.foreground", new javax.swing.plaf.ColorUIResource(0,0,255));
	}
	
	private void notifyAllComponents() {
		PamController pc = PamController.getInstance();
		if (pc == null) {
			return;
		}
		int nG = pc.getGuiFrameManager().getNumFrames();
		for (int i = 0; i < nG; i++) {
			notifyContianer(pc.getGuiFrameManager().getFrame(i));
		}
	}
	
	/** 
	 * Tells a container to set it's colour and the colour of 
	 * all it's components if they implement the ColorManaged
	 * interface. 
	 * <p>
	 * Generally this should be called initially for each frame 
	 * to start the iteration through all the swing components. 
	 * @param container container / or frame to start searching from 
	 */
	public void notifyContianer(Container container) {
		if (container == null) {
			return;
		}
		setColorManagedColor(container);
		int nC = container.getComponentCount();
		Component c;
		for (int i = 0; i < nC; i++) {
			c = container.getComponent(i);
			if (Container.class.isAssignableFrom(c.getClass())) {
				notifyContianer((Container) c);
			}
			else {
				setColorManagedColor(c);
			}
		}
	}
	
	private void setColorManagedColor(Component c) {
		/**
		 * Change to allow a null colour id, so that we can make components
		 * which are used in colour managed and non colour managed parts of PAMguard. 
		 */
		if (ColorManaged.class.isAssignableFrom(c.getClass())) {
			PamColor colourId = ((ColorManaged) c).getColorId();
			if (colourId != null) {
				setColor(c, ((ColorManaged) c).getColorId());
			}
		}
		// try to colour in frmae borderw with a better night time colour. 
//		if (JFrame.class.isAssignableFrom(c.getClass())) {
//			// try to set the frame colour. 
//			JFrame frame = (JFrame) c;
//			ColorModel colorModel = frame.getColorModel();
//			colorModel.
//		}
	}



	/**
	 * Color a component immediately. 
	 * @param component
	 * @param col
	 */
	public void setColor(Component component, PamColor col) {
		if (col == null) {
			return;
		}
		component.setBackground(getColor(col));
		component.setForeground(getForegroudColor(col));
		component.repaint();
	}

	public Color getColor(PamColor col) {
//		switch (col) {
//		case BORDER:
//			return colorSettings.pamBorder;
//		case PlOTWINDOW:
//			return colorSettings.pamPlotWindow;
//		case PLAIN:
//			return colorSettings.plain;
//		case AXIS:
//			return colorSettings.axis;
//		case GRID:
//			return colorSettings.grid;
//		case MAP:
//			return colorSettings.mapColor;
//		case WARNINGBORDER:
//			return colorSettings.pamWarningBorder;
//		case BACKGROUND_ALPHA:
//			return colorSettings.pamBackgroundAlpha;
//		case HIGHLIGHT_ALPHA:
//			return colorSettings.pamHighlightAlpha;
//		case HIGHLIGHT:
//			return colorSettings.pamHighlight;
//		case GPSTRACK:
//			return colorSettings.gpsColor;
//		case LATLINE:
//			return colorSettings.latLineColor;
//		case LONLINE:
//			return colorSettings.lonLineColor;
//		default:
//			return colorSettings.plain;
//		}
		Color colour = colourScheme.get(col);
		if (colour == null) {
			colour = colourScheme.get(PamColor.PLAIN);
		}
		if (colour == null) {
			colour = Color.DARK_GRAY;
		}
		return colour;
	}
	
	public Color getForegroudColor(PamColor col) {
		return getColor(PamColor.AXIS);
//		switch (col) {
//		case BORDER:
//			return colorSettings.axis;
//		case PlOTWINDOW:
//			return colorSettings.axis;
//		case PLAIN:
//			return colorSettings.axis;
//		case AXIS:
//			return colorSettings.axis;
//		case GRID:
//			return colorSettings.axis;
//		default:
//			return colorSettings.axis;
//		}
	}
	
	public Color getWhaleColor(int col) {
		return colourScheme.getWhaleColour(col);
	}

	public Color getChannelColor(int iChan) {
		Color col = colourScheme.getChannelColour(iChan);
		if (col == null) {
			col = colourScheme.getChannelColour(iChan);
		}
		if (col == null) {
			col = Color.BLACK;
		}
		return col;
	}
	
	static private Font boldFont;
	public Font getBoldFont() {
		if (boldFont == null) {
			double scaling = PamSettingManager.getInstance().getCurrentDisplayScaling();
			int fontSize = (int) (12 * scaling);
			boldFont = new Font("system", Font.BOLD, fontSize);
		}
		return boldFont;
	}
	
	@Override
	public Serializable getSettingsReference() {
		return colorSettings;
	}

	@Override
	public long getSettingsVersion() {
		return ColorSettings.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "Pam Color Manager";
	}

	@Override
	public String getUnitType() {
		return "Pam Color Manager";
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		ColorSettings newSettings = (ColorSettings) pamControlledUnitSettings.getSettings();
		this.colorSettings = newSettings.clone();
		colourScheme = colorSettings.selectScheme(colorSettings.getCurrentScheme());

		colourScheme.setWhaleColor(7,  new Color(255,128,192)); // dirty pink
		colourScheme.setWhaleColor(6,  new Color(255,128,0)); // orange

		colourScheme.setWhaleColor(10, new Color(255,255,0)); // yellow
		colourScheme.setWhaleColor(9, new Color(44,167,146)); // dk green
		
		setColors();	
		
		return true;
	}

	public ColorSettings getColorSettings() {
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
			return Color.DARK_GRAY;
		}
		else if (colString.equals("GRAY")) {
			return Color.GRAY;
		}
		else if (colString.equals("GREEN")) {
			return Color.GREEN;
		}
		else if (colString.equals("LIGHT_GRAY")) {
			return Color.LIGHT_GRAY;
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
			aCol = Color.decode(colString);
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
		return new Color(cols[0], cols[1], cols[2]);
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
		return colourScheme.get(PamColor.BORDER);
	}
	public Color getGPSColor() {
		return colourScheme.get(PamColor.GPSTRACK);
	}

	public int getNWhaleColours() {
		return colourScheme.getNumWhaleColours();
	}
	
	public int getWhaleColourIndex(int iCol) {
		return colourScheme.getWhaleColourIndex(iCol);
	}

	/**
	 * @return the colourScheme
	 */
	public ColourScheme getColourScheme() {
		return colourScheme;
	}

}
