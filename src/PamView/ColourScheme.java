package PamView;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.PamColors.PamColor;

public class ColourScheme implements Serializable, Cloneable {

	private static final long serialVersionUID = 2L;
	
	public static final String DAYSCHEME = "Day";

	public static final String NIGHTSCHEME = "Night";

	public static final String PRINTSCHEME = "Print";
	
	private String name;

	private Hashtable<PamColor, Color> colourTable;

	protected transient Color[] channelColors = { Color.BLUE, Color.RED, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.BLACK, Color.CYAN, Color.PINK };
	
	protected Color[] lineColors = { Color.BLACK, Color.BLUE, Color.RED, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK };
	
//	protected final int NWHALECOLORS = 13;
	
	protected Color[] whaleColors;

	private int colourBlind;

	public ColourScheme(String name, int colourBlind) {
		super();
		this.name = name;
		this.colourBlind = colourBlind;
		colourTable = new Hashtable<>();
		setDefaults(colourBlind);
	}
	
	/**
	 * Set a whale colour
	 * @param key colour key
	 * @param value colour value
	 */
	public void put(PamColor key, Color value) {
		colourTable.put(key, value);
	}
		
	private static Color defaultDayBorder = new JPanel().getBackground();
	/**
	 * Get a particular colour
	 * @param key Colour key
	 * @return a colour or null if not in table. 
	 */
	public Color get(PamColor key) {
		Color colour = colourTable.get(key);
		if (colour == null) {
			colour = defaultDayBorder;
		}
		if (key == PamColor.BORDER && name.equals(DAYSCHEME)) {
			colour = defaultDayBorder;
		}
		
		return colour;
	}

	/**
	 * @return The colour scheme name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get a whale colour, recycling through colours, but missing out 0
	 * if the whale colour gets greater than the number of available colours
	 * @param iCol colour index
	 * @return colour
	 */
	public Color getWhaleColour(int iCol) {
		if (iCol == 0) {
			return whaleColors[0];
		}
		// avoid overflows. 
		int nCol = whaleColors.length - 1;
		iCol = ((iCol-1)%nCol) + 1;
		if (iCol < 0) {
			return whaleColors[0];
		}
		return whaleColors[iCol];
	}
	
	/**
	 * Get a whale colour index. this is the mod of the number of colours, but skipping zero.
	 * @param iCol any number. 
	 * @return colour index (1 - 12)
	 */
	public int getWhaleColourIndex(int iCol) {
		if (colsChecked == false) {
			checkAllColours();
			colsChecked = true;
		}
		if (iCol == 0) return 0;
		int nCol = whaleColors.length - 1;
		return ((iCol-1)%nCol) + 1;
	}
	
	/**
	 * Get the number of whale colours
	 * @return the number of whale colours
	 */
	public int getNumWhaleColours() {
		return whaleColors.length;
	}
	
	private transient boolean colsChecked = false;
	/**
	 * Get a channel colour, cycling through a fixed number of colours if the
	 * colour index is greater than then number of colours.
	 * @param iCol colour index
	 * @return Colour
	 */
	public Color getChannelColour(int iCol) {
		if (colsChecked == false) {
			checkAllColours();
			colsChecked = true;
		}
		if (iCol >= 0) {
			if (iCol > 12) {
				iCol += iCol/12;
			}
			return channelColors[iCol%channelColors.length];
		}
		else
			return Color.black;
	}
	
	private void checkAllColours() {
		/**
		 * If any individual colours are null, then set the whole thing 
		 * to null and let it recreate itself. 
		 */
		if (channelColors != null) {
			for (int i = 0; i < channelColors.length; i++) {
				if (channelColors[i] == null) {
					channelColors = null;
					break;
				}
			}		
		}
		if (channelColors == null) {
			if (channelColors == null) {
				ColourScheme bodge = new ColourScheme("bodge", colourBlind);
				bodge.setDefaults(colourBlind);
				this.channelColors = bodge.channelColors;
			}	
		}
	}

	/**
	 * Get a line colour, cycling through a fixed number of colours if the
	 * colour index is greater than then number of colours.
	 * @param iCol colour index
	 * @return Colour
	 */
	public Color getLineColour(int iCol) {
		return lineColors[iCol%lineColors.length];
	}
	
	/**
	 * @return the channelColors
	 */
	protected Color[] getChannelColors() {
		return channelColors;
	}

	/**
	 * @param channelColors the channelColors to set
	 */
	protected void setChannelColors(Color[] channelColors) {
		this.channelColors = channelColors;
	}

	/**
	 * @return the lineColors
	 */
	protected Color[] getLineColors() {
		return lineColors;
	}

	/**
	 * @param lineColors the lineColors to set
	 */
	protected void setLineColors(Color[] lineColors) {
		this.lineColors = lineColors;
	}

	/**
	 * @return the whaleColors
	 */
	protected Color[] getWhaleColors() {
		return whaleColors;
	}

	/**
	 * @param whaleColors the whaleColors to set
	 */
	protected void setWhaleColors(Color[] whaleColors) {
		this.whaleColors = whaleColors;
	}
	
	public void setWhaleColor(int whaleId, Color color) {
		if (this.whaleColors == null || this.whaleColors.length <= whaleId) {
			return;
		}
		whaleColors[whaleId] = color;
	}

	/**
	 * Set a load of default colours. 
	 */
	public void setDefaults(int colourBlind) {
		put(PamColor.PlOTWINDOW, Color.WHITE);
		put(PamColor.BORDER, new Color(222, 222, 222));
		put(PamColor.PLAIN, Color.BLACK);
		put(PamColor.AXIS, Color.BLACK);
		put(PamColor.GRID, Color.BLACK);
		put(PamColor.MAP, new Color(255, 255, 255));
		put(PamColor.WARNINGBORDER, new Color(128, 0, 0));
		put(PamColor.BACKGROUND_ALPHA, new Color(50,50,50,150));
		put(PamColor.HIGHLIGHT_ALPHA, new Color(0,200,222,180));
		put(PamColor.HIGHLIGHT, new Color(0,80,222,40));
		put(PamColor.GPSTRACK, new Color(255, 255, 255));
		put(PamColor.LATLINE, new Color(218, 142, 180));
		put(PamColor.LONLINE, new Color(150, 150, 200));	
		JButton jButton = new JButton();
		put(PamColor.BUTTONFACE, jButton.getBackground());
//		TitledBorder tb = new TitledBorder(" ");
//		Color c = tb.getTitleColor();
		put(PamColor.TITLEBORDER, Color.WHITE);		
		
		makeWhaleColours(colourBlind);
//		whaleColors[14] = new Color(135,67,10); //??
		
		channelColors = Arrays.copyOfRange(whaleColors, 1, whaleColors.length);
//		for (int i = 3; i < 12; i++) {
//			channelColors[i] = whaleColors[i+1];
//		}
//		channelColors[12] = new Color(192,192,0);
		
	}

	private void makeWhaleColours(int colourBlind) {
		switch (colourBlind) {
		case ColorSettings.ACCESSIBLE_99:
			/*
			 * Taken from https://sashamaps.net/docs/resources/20-colors/
			 */
			whaleColors = new Color[13];
			whaleColors[0] = new Color(0);     // black
			whaleColors[1] = new Color(0x4363d8); // blue
			whaleColors[2] = new Color(0x3cb44b);  // green
			whaleColors[3] = new Color(0xe6194B); // red
			whaleColors[4] = new Color(0xf032e6); // magenta
			whaleColors[5] = new Color(0x42d4f4); // cyan
			whaleColors[6] = new Color(0x9a6324); // brown
			whaleColors[7] = new Color(0xf58231); // orange
			whaleColors[8] = new Color(0xa9a9a9); // grey
			whaleColors[10] = new Color(0xffe119); // yellow
			whaleColors[9] = new Color(0x469990); // teal
			whaleColors[11] = new Color(0x800000); // maroon
			whaleColors[12] = new Color(0xfabed4); //pink
			break;
		case ColorSettings.ACCESSIBLE_999:
			whaleColors = new Color[8];
			whaleColors[0] = new Color(0);     // black
			whaleColors[1] = new Color(0x000075);  // Navy
			whaleColors[2] = new Color(0x800000); // Maroon
			whaleColors[3] = new Color(0xf58231); // orange
			whaleColors[4] = new Color(0xffe119); // yellow
			whaleColors[5] = new Color(0x4363d8); // blue
			whaleColors[6] = new Color(0xa9a9a9); // grey
			whaleColors[7] = new Color(0xdcbeff); // Lavender
			break;
		default: //case ColorSettings.ACCESSIBLE_95:
			whaleColors = new Color[13];
			whaleColors[0] = new Color(0,0,0);     // black
			whaleColors[1] = new Color(0,0,255); // blue
			whaleColors[2] = new Color(0,255,0);  // green
			whaleColors[3] = new Color(255, 0, 0); // red
			whaleColors[4] = new Color(255,0,255); // pink / magenta
			whaleColors[5] = new Color(0,255,255); // cyan
			whaleColors[6] = new Color(255,128,192); // dirty pink
			whaleColors[7] = new Color(255,128,0); // orange
			whaleColors[8] = new Color(192,192,192); // grey
			whaleColors[10] = new Color(255,255,0); // yellow
			whaleColors[9] = new Color(44,167,146); // dk green
			whaleColors[11] = new Color(20,175,235); // dk cyan
			whaleColors[12] = new Color(135,67,165); //purple
			break;
		}
	}

	public static ColourScheme createDefaultDayScheme(int colourBlindPalet2) {
		ColourScheme scheme = new ColourScheme(DAYSCHEME, colourBlindPalet2);
		JPanel p = new JPanel();
		scheme.put(PamColor.BORDER, p.getBackground());
		scheme.put(PamColor.WARNINGBORDER, new Color(255, 0, 0));
		scheme.put(PamColor.PlOTWINDOW, Color.WHITE);
		scheme.put(PamColor.PLAIN, Color.BLACK);
		scheme.put(PamColor.AXIS, Color.BLACK);
		scheme.put(PamColor.GRID, Color.BLACK);
		scheme.put(PamColor.MAP,  new Color(180, 180, 255));
		scheme.put(PamColor.GPSTRACK, Color.GRAY);
		scheme.put(PamColor.LATLINE, new Color(218, 142, 180));
		scheme.put(PamColor.LONLINE, new Color(150, 150, 200));	
		return scheme;
	}
	
	public static ColourScheme createDefaultPrintScheme(int colourBlind) {
		ColourScheme scheme = new ColourScheme(PRINTSCHEME, colourBlind);
		JPanel p = new JPanel();
		scheme.put(PamColor.BORDER, Color.WHITE);
		scheme.put(PamColor.WARNINGBORDER, new Color(255, 0, 0));
		scheme.put(PamColor.PlOTWINDOW, Color.WHITE);
		scheme.put(PamColor.PLAIN, Color.BLACK);
		scheme.put(PamColor.AXIS, Color.BLACK);
		scheme.put(PamColor.GRID, Color.BLACK);
		scheme.put(PamColor.MAP,  Color.WHITE);
		scheme.put(PamColor.GPSTRACK, Color.GRAY);
		scheme.put(PamColor.LATLINE, new Color(210, 210, 210));
		scheme.put(PamColor.LONLINE, new Color(210, 210, 210));		
		return scheme;
	}
	
	public static ColourScheme createDefaultNightScheme(int colourBlind) {
		ColourScheme scheme = new ColourScheme(NIGHTSCHEME, colourBlind);
		scheme.put(PamColor.PlOTWINDOW, new Color(32, 32, 32));
		scheme.put(PamColor.BORDER, Color.BLACK);
		scheme.put(PamColor.PLAIN, Color.WHITE);
		scheme.put(PamColor.AXIS, Color.RED);
		scheme.put(PamColor.GRID, new Color(128, 0, 0));
		scheme.put(PamColor.MAP, new Color(18, 18, 32));
		scheme.put(PamColor.WARNINGBORDER, new Color(100, 0, 100));
		scheme.put(PamColor.GPSTRACK, new Color(255, 255, 255));
		scheme.whaleColors[0] = new Color(192, 0, 0);
		scheme.whaleColors[1] = new Color(255, 255, 255);
		scheme.put(PamColor.TITLEBORDER, Color.RED);		
		scheme.put(PamColor.BUTTONFACE, new Color(28,28,28));
		return scheme;
	}

	/**
	 * @return the colourBlind
	 */
	public int getColourBlind() {
		return colourBlind;
	}
	
}
