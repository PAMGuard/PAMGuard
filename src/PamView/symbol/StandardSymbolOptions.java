package PamView.symbol;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;

import PamUtils.PamArrayUtils;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;

public class StandardSymbolOptions extends PamSymbolOptions implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/*
	 * all now deprecated
	 * 
	 */
	public static final int COLOUR_BY_SUPERDET = 0;
	public static final int COLOUR_SPECIAL = 1;
	public static final int COLOUR_SUPERDET_THEN_SPECIAL = 2;
	public static final int COLOUR_HYDROPHONE = 3;
	public static final int COLOUR_FIXED = 4;
	public static final int COLOUR_ANNOTATION = 5;
	
	
	
	@Deprecated
	public int colourChoice = COLOUR_FIXED;
	
	/**
	 * Use annotation to colour / reshape the symbol. 
	 */
	@Deprecated
	private boolean useAnnotation;
	
	/**
	 * Name of annotation when colouring by annotation. 
	 */
	@Deprecated
	private String annotationChoice; 
	
	/**
	 * The colour of lines. 
	 */
	@Deprecated
	private LineData lineData = new LineData(Color.BLACK);
	
	@Deprecated
	private AnnotationSymbolOptions annotationSymbolOptions;
	
	/*
	 * The fixed symbol
	 */
	public SymbolData symbolData;

	
	public double mapLineLength = 1000; // default map line length in metres. 
	
	public boolean hideLinesWithLatLong = false;

	/**
	 * The order of the symbol modifiers to use. 
	 */
	private int[] modifierOrder;
	
	
	/**
	 * A list of which symbol modifiers is enabled. This somewhat replicated the functionality
	 * in the symbol chooser params if all modifiers are deselected but it allows 
	 * for a better user experience by allowing a symbol modifier to be disabled whilst 
	 * retaining it's settings. This means users can easily switch between modifiers. 
	 */
	private boolean[] isEnabled; 
	
	/**
	 * A map of the symbol options associated with each symbol modifier. 
	 */
	private HashMap<String, SymbolModifierParams> modifierParams;
	
	public StandardSymbolOptions(SymbolData defaultSymbol) {
		this.symbolData = defaultSymbol; 
	}

	@Override
	protected StandardSymbolOptions clone() {
		try {
			StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) super.clone();
			standardSymbolOptions.symbolData.clone();
			return standardSymbolOptions;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Deprecated
	public AnnotationSymbolOptions getAnnotationSymbolOptions() {
		if (annotationSymbolOptions == null) {
			annotationSymbolOptions = new AnnotationSymbolOptions();
		}
		return annotationSymbolOptions;
	}

	@Deprecated
	public void setAnnotationSymbolOptions(AnnotationSymbolOptions annotationSymbolOptions) {
		this.annotationSymbolOptions = annotationSymbolOptions;
	}
	
	/**
	 *
	 * 
	 * Check whether the symbol modifiers are enabled or disabled. 
	 * 
	 * @return a list of booleans indicating whether the symbol modifier is enabled. 
	 */
	public boolean[] isEnabled(StandardSymbolChooser symbolChooser) {
		if (isEnabled == null || isEnabled.length != symbolChooser.getSymbolModifiers().size()) {
			isEnabled = new boolean[symbolChooser.getSymbolModifiers().size()];
			for (int i = 0; i < getModifierOrder(symbolChooser).length; i++) {
				isEnabled[i] = true;
			}
		}
	return isEnabled;
	}
	
	
	/**
	 * Set whether a symbol modifier is enabled. 
	 * @param enabled - true to enable. 
	 * @param i - the index of the symbol modifier (not modified). 
	 */
	public void setEnabled(boolean enabled, int i) {
		isEnabled[i] = enabled;
	}
	

	/**
	 * @return the modifierOrder
	 */
	public int[] getModifierOrder(StandardSymbolChooser symbolChooser) {
		if (modifierOrder == null || modifierOrder.length != symbolChooser.getSymbolModifiers().size() || PamArrayUtils.contains(modifierOrder, -1)) {
			modifierOrder = new int[symbolChooser.getSymbolModifiers().size()];
			for (int i = 0; i < modifierOrder.length; i++) {
				modifierOrder[i] = i;
			}
		}
		return modifierOrder;
	}

	/**
	 * @param modifierOrder the modifierOrder to set
	 */
	public void setModifierOrder(int[] modifierOrder) {
		this.modifierOrder = modifierOrder;
	}
	
	/**
	 * Get parameters for a named symbol modifier
	 * <p>
	 * This should <b>only</b> be called during serialization - otherwise the symbol
	 * modifiers hold a reference to their params
	 * 
	 * @param modifierName
	 * @param params
	 */
	public void setModifierParams(String modifierName, SymbolModifierParams params) {
		if (modifierParams == null) {
			modifierParams = new HashMap<>();
		}
		//System.out.println("StandardSymbolOptions: setModifierParams: " + modifierName + "  " + params); 
		//modifierParams.remove(modifierName); //just in case - do not want duplicates
		modifierParams.put(modifierName, params);
	}
	
	/**
	 * Set parameters for a named symbol modifier.
	 * <p>
	 * This should <b>only</b> be called during de-serialization - otherwise the symbol
	 * modifiers hold a reference to their parameters. 
	 * 
	 * @param modifierName
	 * @return
	 */
	public SymbolModifierParams getModifierParams(SymbolModifier modifierName) {
		if (modifierParams == null) {
			modifierParams = new HashMap<String, SymbolModifierParams>();
		}
		SymbolModifierParams p = modifierParams.get(modifierName.getName());
//		System.out.println("StandardSymbolOptions: getModifierParams: " + modifierName + " p: " + p + "  " + this);

		if (p == null) {
//			System.out.println("StandardSymbolOptions: getModifierParams: " + modifierName);
			p = modifierName.getSymbolModifierParams(); 
			modifierParams.put(modifierName.getName(), p);
		}
		return p;
		
	}
	
	
}
