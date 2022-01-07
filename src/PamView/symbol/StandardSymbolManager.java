package PamView.symbol;

import java.util.ArrayList;

import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.symbol.modifier.HydrophoneSymbolModifier;
import PamView.symbol.modifier.SuperDetSymbolModifier;
import PamguardMVC.PamDataBlock;

/*
 * Standard version of the SymbolManager which should work or be usable for 
 * most data types with only a small amount of overriding for special cases. 
 */
public class StandardSymbolManager extends PamSymbolManager<StandardSymbolChooser> {
	
	// nearly all this is now obsolete. Need to pluck up courage and delete a lot
	// of it. 
	/**
	 * The symbol has option to colour by channels 
	 */
	public static final int HAS_CHANNEL_OPTIONS = 1 << 1; 
	
	/**
	 * The symbol has channels 
	 */
	public static final int HAS_SPECIAL_COLOUR = 1 << 2; 
		
	/**
	 * Has a line to colour. e.g. whistles or bearing
	 */
	public static final int HAS_LINE = 1 << 3; 
	
	/**
	 * Has a line length to change e.g bearings. 
	 */
	public static final int HAS_LINE_LENGTH = 1 << 4;
	
	/**
	 * Save typing by adding has line and has line length together. 
	 */
	public static final int HAS_LINE_AND_LENGTH = HAS_LINE | HAS_LINE_LENGTH;

	/**
	 * Has a symbol e.g. clicks and whistles. 
	 */
	public static final int HAS_SYMBOL = 1 << 5; 

	/**
	 * Number of standard colour choices
	 */
	public static final int NUM_STANDARD_CHOICES = 5;
	
	

	private SymbolData defaultSymbol;
	
	/**
	 * Name for special colours, e.g. "Colour by click Type" (clicks), "Colour Randomly" (whistles)
	 */
	private String specialColourName = null;
	
	/**
	 * A bitmap of symbol choices. 
	 */
	private int symbolOptionMap = 0; 
	
	/**
	 * Symbol change listeners. Called when symbols  change settings 
	 */
	private ArrayList<SymbolChangeListener> symbolChangeListeners = new  ArrayList<SymbolChangeListener>(); 
	
	/**
	 * Constructor for a standard symbol manager, requiring an associated datablock 
	 * and a standard symbol. Enabling colouring by channel is set to true and
	 * the special colour names is set to null. 
	 * @param pamDataBlock Data Block
	 * @param defaultSymbol Default Symbol 
	 */
	public StandardSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol) {
		super(pamDataBlock);
		this.defaultSymbol = defaultSymbol;
	}
	
	/**
	 * Constructor for a standard symbol manager, requiring an associated datablock 
	 * and a standard symbol. Enabling colouring by channel is set to true and
	 * the special colour names is set to null. 
	 * @param pamDataBlock Data Block
	 * @param defaultSymbol Default Symbol 
	 * @param hasChannelOption
	 */
	public StandardSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, boolean hasChannelOption) {
		super(pamDataBlock);
		addSymbolOption(HAS_SYMBOL);
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		this.defaultSymbol = defaultSymbol;
	}
	/**
	 * Constructor for a standard symbol manager, requiring an associated datablock 
	 * and a standard symbol. 
	 * @param pamDataBlock Data Block
	 * @param defaultSymbol Default Symbol 
	 * @param hasChannelOption colour by channel option is available
	 * @param specialColourName colour by special name is available. 
	 */
	public StandardSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, boolean hasChannelOption, String specialColourName) {
		super(pamDataBlock);
		addSymbolOption(HAS_SYMBOL);
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		addSymbolOption(HAS_SPECIAL_COLOUR);
		this.defaultSymbol = defaultSymbol;
		this.specialColourName = specialColourName;
	}
	/**
	 * Constructor for a standard symbol manager, requiring an associated datablock 
	 * and a standard symbol. Enabling colouring by channel is set to true and
	 * the special colour names is set to null. 
	 * @param pamDataBlock Data Block
	 * @param defaultSymbol Default Symbol 
	 * @param specialColourName
	 */
	public StandardSymbolManager(PamDataBlock pamDataBlock, SymbolData defaultSymbol, String specialColourName) {
		super(pamDataBlock);
		addSymbolOption(HAS_SYMBOL);
		addSymbolOption(HAS_SPECIAL_COLOUR);
		this.defaultSymbol = defaultSymbol; 
		this.specialColourName = specialColourName;
	}

	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		StandardSymbolChooser symbolChooser = new StandardSymbolChooser(this, getPamDataBlock(), displayName, defaultSymbol, projector);
		return symbolChooser;
	}

	/**
	 * @return the specialColourName
	 */
	public String getSpecialColourName() {
		return specialColourName;
	}

	/**
	 * @param specialColourName the specialColourName to set
	 */
	public void setSpecialColourName(String specialColourName) {
		this.specialColourName = specialColourName;
	}

	public  String colourChoiceName(int iChoice) {
		String prefix = "Colour ";
		switch (iChoice) {
		case StandardSymbolOptions.COLOUR_BY_SUPERDET:
			return prefix + "by super detection";
		case StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL:
			return prefix + "by super detection then " + specialColourName;
		case StandardSymbolOptions.COLOUR_HYDROPHONE:
			return prefix + "by hydrophone";
		case StandardSymbolOptions.COLOUR_SPECIAL:
			return prefix + specialColourName;
		case StandardSymbolOptions.COLOUR_FIXED:
			return "Fixed colour";
		case StandardSymbolOptions.COLOUR_ANNOTATION:
			return prefix + "by Annotation";
		}
		return null;
	}

	/**
	 * @return the defaultSymbol
	 */
	public SymbolData getDefaultSymbol() {
		return defaultSymbol;
	}

	/**
	 * @param defaultSymbol the defaultSymbol to set
	 */
	public void setDefaultSymbol(SymbolData defaultSymbol) {
		this.defaultSymbol = defaultSymbol;
	}
	
	/**
	 * Add an option to the symbol manager
	 * @param flag - integer flag. 
	 * @return true if the manager has the option enabled. 
	 */

	public int addSymbolOption(int symbolChoice) {
		return symbolOptionMap = symbolOptionMap | (symbolChoice);
	}
	
	/**
	 * Check whether the symbol manager has a symbol option.
	 * @param flag - integer flag. 
	 * @return true if the manager has the option enabled. 
	 */

	public int removeSymbolOption(int symbolChoice) {
		return symbolOptionMap = symbolOptionMap & ~(symbolChoice);
	}

	/**
	 * 
	 * @param symbolChoice
	 */
	public boolean hasSymbolOption(int symbolChoice) {
		
		if ((symbolOptionMap & (symbolChoice)) != 0) return true; 
		
		else return false; 
	}
	
	/**
	 * Add a symbol chnage listener
	 *	@param symbolChangeListener the 
	 */
	public void addSymbolChnageListener(SymbolChangeListener symbolChangeListener){
		this.symbolChangeListeners.add(symbolChangeListener);
	}
	
	/**
	 * Remove a symbol change listener
	 *	@param symbolChangeListener the 
	 */
	public void removeSymbolChnageListener(SymbolChangeListener symbolChangeListener){
		this.symbolChangeListeners.remove(symbolChangeListener);
	}

	
	/**
	 * Notify all listeners that a symbolChooser has changed.
	 * @param chooser  the chooser which has changed.
	 */
	public void notifySymbolListeners(StandardSymbolChooser chooser) {
		for (int i=0; i<symbolChangeListeners.size(); i++ ){
			symbolChangeListeners.get(i).symbolChanged(chooser);
		}
	}

	/**
	 * The number of available colour choices. 
	 * @return the number of colour choices. 
	 */
	public int getNColourChoices() {
		return 6;
	}


}
