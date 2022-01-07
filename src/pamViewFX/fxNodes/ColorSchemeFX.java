package pamViewFX.fxNodes;

import java.io.Serializable;
import java.util.Hashtable;

import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamColorsFX.PamColor;

public class ColorSchemeFX implements Serializable, Cloneable {

	private static final long serialVersionUID = 2L;

	private String name;

	private Hashtable<PamColor, Color> colourTable;

	protected Color[] channelColors = { Color.BLUE, Color.RED, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.BLACK, Color.CYAN, Color.PINK };
	
	protected Color[] lineColors = { Color.BLACK, Color.BLUE, Color.RED, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK };
	
	protected final int NWHALECOLORS = 13;
	
	protected Color[] whaleColors = new Color[13];

	public ColorSchemeFX(String name) {
		super();
		this.name = name;
		colourTable = new Hashtable<>();
		setDefaults();
	}
	
	/**
	 * Set a whale colour
	 * @param key colour key
	 * @param value colour value
	 */
	public void put(PamColor key, Color value) {
		colourTable.put(key, value);
	}
		
	/**
	 * Get a particular colour
	 * @param key Colour key
	 * @return a colour or null if not in table. 
	 */
	public Color get(PamColor key) {
		return colourTable.get(key);
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
		return whaleColors[iCol];
	}
	
	/**
	 * Get the number of whale colours
	 * @return the number of whale colours
	 */
	public int getNumWhaleColours() {
		return whaleColors.length;
	}
	
	/**
	 * Get a channel colour, cycling through a fixed number of colours if the
	 * colour index is greater than then number of colours.
	 * @param iCol colour index
	 * @return Colour
	 */
	public Color getChannelColour(int iCol) {
		if (iCol >= 0)
		return channelColors[iCol%channelColors.length];
		else
			return Color.BLACK;
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

	/**
	 * Set a load of default colours. 
	 */
	public void setDefaults() {
		put(PamColor.PlOTWINDOW, Color.WHITE);
		put(PamColor.BORDER, Color.rgb(222, 222, 222));
		put(PamColor.PLAIN, Color.BLACK);
		put(PamColor.AXIS, Color.BLACK);
		put(PamColor.GRID, Color.BLACK);
		put(PamColor.MAP, Color.rgb(255, 255, 255));
		put(PamColor.WARNINGBORDER, Color.rgb(128, 0, 0));
		put(PamColor.BACKGROUND_ALPHA, Color.rgb(50,50,50,0.8));
		put(PamColor.HIGHLIGHT_ALPHA, Color.rgb(0,200,222,0.7));
		put(PamColor.HIGHLIGHT, Color.rgb(0,80,222,0.15));
		put(PamColor.GPSTRACK, Color.rgb(255, 255, 255));
		put(PamColor.LATLINE, Color.rgb(218, 142, 180));
		put(PamColor.LONLINE, Color.rgb(150, 150, 200));	
		put(PamColor.BUTTONFACE, Color.DARKGREY);
//		TitledBorder tb = new TitledBorder(" ");
//		Color c = tb.getTitleColor();
		put(PamColor.TITLEBORDER, Color.WHITE);		
		
		whaleColors[0] = Color.rgb(0,0,0);
		whaleColors[1] = Color.rgb(255, 0, 0);
		whaleColors[2] = Color.rgb(0,255,0);
		whaleColors[3] = Color.rgb(0,0,255);
		whaleColors[4] = Color.rgb(255,0,255);
		whaleColors[5] = Color.rgb(0,255,255);
		whaleColors[6] = Color.rgb(255,128,0);
		whaleColors[7] = Color.rgb(255,128,192);
		whaleColors[8] = Color.rgb(192,192,192);
		whaleColors[9] = Color.rgb(255,255,0);
		whaleColors[10] = Color.rgb(44,167,146);
		whaleColors[11] = Color.rgb(20,175,235);
		whaleColors[12] = Color.rgb(135,67,165);
	}


	public static ColorSchemeFX createDefaultDayScheme() {
		ColorSchemeFX scheme = new ColorSchemeFX("Day");
		scheme.put(PamColor.BORDER, Color.GREY);
		scheme.put(PamColor.WARNINGBORDER, Color.rgb(255, 0, 0));
		scheme.put(PamColor.PlOTWINDOW, Color.WHITE);
		scheme.put(PamColor.PLAIN, Color.BLACK);
		scheme.put(PamColor.AXIS, Color.BLACK);
		scheme.put(PamColor.GRID, Color.BLACK);
		scheme.put(PamColor.MAP,  Color.rgb(180, 180, 255));
		scheme.put(PamColor.GPSTRACK, Color.GRAY);
		scheme.put(PamColor.LATLINE, Color.rgb(218, 142, 180));
		scheme.put(PamColor.LONLINE, Color.rgb(150, 150, 200));	
		return scheme;
	}
	
	public static ColorSchemeFX createDefaultPrintScheme() {
		ColorSchemeFX scheme = new ColorSchemeFX("Print");
		scheme.put(PamColor.BORDER, Color.GREY);
		scheme.put(PamColor.WARNINGBORDER, Color.rgb(255, 0, 0));
		scheme.put(PamColor.PlOTWINDOW, Color.WHITE);
		scheme.put(PamColor.PLAIN, Color.BLACK);
		scheme.put(PamColor.AXIS, Color.BLACK);
		scheme.put(PamColor.GRID, Color.BLACK);
		scheme.put(PamColor.MAP,  Color.WHITE);
		scheme.put(PamColor.GPSTRACK, Color.GRAY);
		scheme.put(PamColor.LATLINE, Color.rgb(210, 210, 210));
		scheme.put(PamColor.LONLINE, Color.rgb(210, 210, 210));		
		return scheme;
	}
	
	public static ColorSchemeFX createDefaultNightScheme() {
		ColorSchemeFX scheme = new ColorSchemeFX("Night");
		scheme.put(PamColor.PlOTWINDOW, Color.rgb(32, 32, 32));
		scheme.put(PamColor.BORDER, Color.BLACK);
		scheme.put(PamColor.PLAIN, Color.WHITE);
		scheme.put(PamColor.AXIS, Color.RED);
		scheme.put(PamColor.GRID, Color.rgb(128, 0, 0));
		scheme.put(PamColor.MAP, Color.rgb(18, 18, 32));
		scheme.put(PamColor.WARNINGBORDER, Color.rgb(100, 0, 100));
		scheme.put(PamColor.GPSTRACK, Color.rgb(255, 255, 255));
		scheme.whaleColors[0] = Color.rgb(192, 0, 0);
		scheme.whaleColors[1] = Color.rgb(255, 255, 255);
		scheme.put(PamColor.TITLEBORDER, Color.RED);		
		scheme.put(PamColor.BUTTONFACE, Color.rgb(28,28,28));
		return scheme;
	}

	public int getWhaleColourIndex(int iCol) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
