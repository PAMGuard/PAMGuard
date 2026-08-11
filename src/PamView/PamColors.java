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
import pamViewFX.fxStyles.PamStylesManagerFX;


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

	/**
	 * Name of the colour scheme last applied by {@link #setColors()}, so that
	 * {@link #colourSchemeVersion} only counts real changes.
	 */
	private String lastAppliedScheme;

	/**
	 * Incremented every time the colour scheme actually changes.
	 * <p>
	 * Components which are not in a window at the time of a change - the module
	 * specific part of the top tool bar is added and removed as the user moves
	 * between tabs, see {@link TopToolBar#setActiveControlledUnit} - miss both the
	 * look and feel update and the recolouring, and come back still wearing the old
	 * scheme. Comparing this against the version a component was last styled at
	 * tells you whether it needs bringing up to date.
	 */
	private int colourSchemeVersion;

	
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
		setColors();
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
			setColourScheme(schemeName);
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
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			/*
			 * The dark look and feel is installed as soon as the colour scheme is restored
			 * from settings, which is before the main window exists. Any window built
			 * before that point would have been given the old look and feel, so rebuild
			 * them all once now that the GUI is up.
			 */
			SwingUtilities.invokeLater(new SetColoursLater(true));
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.CHANGED_DISPLAY_SETTINGS:
			SwingUtilities.invokeLater(new SetColoursLater(false));
		}
	}

	class SetColoursLater implements Runnable {

		/**
		 * Rebuild the UI of every open window before re-colouring, whether or not the
		 * look and feel has just changed.
		 */
		private final boolean refreshLookAndFeel;

		SetColoursLater(boolean refreshLookAndFeel) {
			this.refreshLookAndFeel = refreshLookAndFeel;
		}

		@Override
		public void run() {
			if (refreshLookAndFeel && PamLookAndFeel.isDarkLookAndFeel()) {
				PamLookAndFeel.refreshWindows();
			}
			setColors();
		}

	}

	/**
	 * Apply the current colour scheme everywhere: install the Swing look and feel
	 * which goes with it, re-colour every {@link ColorManaged} Swing component, and
	 * restyle the registered JavaFX displays.
	 * <p>
	 * Must run on the event dispatch thread (changing the look and feel from another
	 * thread can deadlock Swing), so it bounces itself onto the EDT if called from
	 * elsewhere - notably from the JavaFX application thread, which is where the
	 * dark mode toggles in the JavaFX GUI live.
	 */
	public void setColors() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(this::setColors);
			return;
		}
		String schemeName = colourScheme == null ? null : colourScheme.getName();
		if (schemeName != null && !schemeName.equals(lastAppliedScheme)) {
			lastAppliedScheme = schemeName;
			colourSchemeVersion++;
		}
		/*
		 * Text fields, check boxes, combo box popups, scroll bars, tab headers and
		 * internal frame title bars are all painted by the look and feel, not by us, so
		 * a dark scheme needs a dark look and feel.
		 */
		if (PamLookAndFeel.setLookAndFeel(colourScheme)) {
			/*
			 * Rebuilding the UI of every open window is deliberately left to a later pass of
			 * the event queue.
			 *
			 * Changing the look and feel restyles HTML text components (the help viewer and
			 * anything else showing HTML), and the Swing text package delivers the resulting
			 * document change events asynchronously, through
			 * DefaultStyledDocument.ChangeUpdateRunnable. Rebuilding the component trees in
			 * this same pass would leave those queued events walking view hierarchies that
			 * have since been thrown away, which lands in a JDK re-entrancy bug in
			 * View.forwardUpdate and throws an ArrayIndexOutOfBoundsException out of
			 * javax.swing.text.CompositeView.getView. Harmless - the views are rebuilt
			 * anyway - but it fills the log with stack traces. Letting the queue drain first
			 * keeps the two apart.
			 */
			SwingUtilities.invokeLater(() -> {
				PamLookAndFeel.refreshWindows();
				recolourComponents();
			});
		}
		else {
			recolourComponents();
		}
	}

	/**
	 * Apply the current colour scheme to the Swing components which implement
	 * {@link ColorManaged}, and to the registered JavaFX displays.
	 */
	private void recolourComponents() {
		if (PamGUIManager.isSwing()) notifyAllComponents();

		// and the JavaFX panes embedded in the Swing GUI (time display, data map...).
		setFXColours();
	}

	/**
	 * Restyle the JavaFX displays for the current colour scheme. Wrapped in a try /
	 * catch so that a system without a working JavaFX toolkit can't stop the Swing
	 * side of the colour change.
	 */
	private void setFXColours() {
		try {
			PamStylesManagerFX.getPamStylesManagerFX().updateStyles();
		}
		catch (Throwable e) {
			System.out.println("Unable to update JavaFX styles: " + e.getMessage());
		}
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
		 * which are used in colour managed and non colour managed parts of PAMGuard. 
		 */
		if (ColorManaged.class.isAssignableFrom(c.getClass())) {
			PamColor colourId = ((ColorManaged) c).getColorId();
			if (colourId != null) {
				try {
					setColor(c, colourId);
				}
				catch (Exception e) {
					/*
					 * Components are free to override setBackground and do what they like in
					 * there, so one badly behaved component mustn't be allowed to abort the walk
					 * and leave the rest of the display in the old colour scheme.
					 */
					System.out.printf("Error setting colour of %s: %s\n", c.getClass().getName(), e.getMessage());
					e.printStackTrace();
				}
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
		/*
		 * Settings written by an older version won't contain any scheme added since,
		 * e.g. the Dark scheme, so make sure the list is up to date before selecting.
		 */
		colorSettings.checkSchemes();
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

	/**
	 * Get a counter which increments every time the colour scheme changes.
	 * <p>
	 * Anything which can be detached from the window while a scheme change happens
	 * should remember the version it was last styled at and, when it comes back,
	 * refresh itself if the version has moved on - see
	 * {@link TopToolBar#setActiveControlledUnit}.
	 *
	 * @return the current colour scheme version.
	 */
	public int getColourSchemeVersion() {
		return colourSchemeVersion;
	}

	/**
	 * Set the colour scheme by name and refresh all colours.
	 * @param schemeName - the scheme name, e.g. ColourScheme.DAYSCHEME or ColourScheme.NIGHTSCHEME
	 */
	public void setColourScheme(String schemeName) {
		colourScheme = colorSettings.selectScheme(schemeName);
		setColors();
		/*
		 * Everything below touches Swing, and this is also called from the JavaFX
		 * application thread by the dark mode toggles in the JavaFX GUI, so make sure it
		 * happens on the event dispatch thread. It is queued after the work setColors
		 * has just queued there.
		 */
		SwingUtilities.invokeLater(this::colourSchemeChanged);
	}

	/**
	 * Tidy up after a colour scheme change: tick the right item in the colour scheme
	 * menu, and tell the displays their settings have changed so that anything which
	 * draws itself rather than being painted by Swing or CSS - the plots, maps and
	 * the JavaFX canvases - redraws with the new colours. Without that a display
	 * which isn't scrolling (e.g. anything in viewer mode) keeps the old colours
	 * until the next time something happens to make it repaint.
	 */
	private void colourSchemeChanged() {
		if (colourMenuItems != null) {
			for (int i = 0; i < colourMenuItems.length; i++) {
				colourMenuItems[i].setSelected(colourMenuItems[i].getText().equalsIgnoreCase(colourScheme.getName()));
			}
		}
		PamController pamController = PamController.getInstance();
		if (pamController != null && pamController.isInitializationComplete()) {
			pamController.notifyModelChanged(PamControllerInterface.CHANGED_DISPLAY_SETTINGS);
		}
	}

}
