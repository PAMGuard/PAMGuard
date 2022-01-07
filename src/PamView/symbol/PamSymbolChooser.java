package PamView.symbol;

import java.util.ArrayList;
import java.util.ListIterator;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.symbol.FXSymbolOptionsPane;

/**
 * Manage symbol choices for a single display and datablock. 
 * @author dg50
 *
 */
abstract public class PamSymbolChooser {
	
	private PamDataBlock pamDataBlock;
	private String displayName;
	private PamSymbolManager pamSymbolManager;
	private GeneralProjector projector;

	private ArrayList<SymbolModifier> symbolModifiers = new ArrayList<>();
	
	public PamSymbolChooser(PamSymbolManager pamSymbolManager, PamDataBlock pamDataBlock, String displayName, GeneralProjector projector) {
		super();
		this.pamSymbolManager = pamSymbolManager;
		this.pamDataBlock = pamDataBlock;
		this.displayName = displayName;
		this.projector = projector;
	}

	/**
	 * Get the symbol choice for a data unit. e.g. clicks 
	 * @param projector
	 * @param dataUnit
	 * @return
	 */
	public abstract SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit);


	private SymbolData lastSymbolDataFX;
	private PamSymbolFX lastSymbolFX;
	
	public PamSymbolFX getPamSymbolFX(GeneralProjector projector, PamDataUnit dataUnit) {
		SymbolData symbolData = getSymbolChoice(projector, dataUnit);
		if (symbolData == lastSymbolDataFX) {
			return lastSymbolFX;
		}
		else {
			lastSymbolFX = new PamSymbolFX(lastSymbolDataFX = symbolData);
			return lastSymbolFX;
		}
	}
	
	private SymbolData lastSymbolData;
	private PamSymbol lastSymbol;
	public PamSymbol getPamSymbol(GeneralProjector projector, PamDataUnit dataUnit) {
		SymbolData symbolData = getSymbolChoice(projector, dataUnit);
		if (symbolData == lastSymbolData) {
			return lastSymbol;
		}
		else {
			lastSymbol = new PamSymbol(lastSymbolData = symbolData);
			return lastSymbol;
		}
	}

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Swing version of option pane. Panel which contains options to change symbol types and colours depending on detection type. 
	 * @param projector - the geenral projector
	 * @return the JavaFX options pane for symbols. 
	 */
	public SwingSymbolOptionsPanel getSwingOptionsPanel(GeneralProjector projector) {
		return null;
	}
	
	/**
	 * The JavaFX version of the option panel. Pane which contains options to change symbol types and colours depending on detection type. 
	 * @param projector - the geenral projector
	 * @return the JavaFX options pane for symbols. 
	 */
	public FXSymbolOptionsPane getFXOptionPane(GeneralProjector projector) {
		return null;
	}

	/**
	 * The generla projector associated with the symbol chooser. 
	 * @return the projector
	 */
	public GeneralProjector getProjector() {
		return projector;
	}
	
	/**
	 * Set the projector. Important for getting the right display options 
	 * such as line length on the map
	 * @param projector projector to set. 
	 */
	public void setProjector(GeneralProjector projector) {
		this.projector = projector;
	}

	/**
	 * Check to see if a symbol modifier of a given class already exists. 
	 * May have to change this so that it's by name since only one of each class is
	 * allowed in current system. 
	 * @param modifierClass
	 * @return
	 */
	public SymbolModifier hasSymbolModifier(SymbolModifier symbolModifier) {
		Class modifierClass = symbolModifier.getClass();
		for (SymbolModifier aMod : symbolModifiers) {
			if (aMod.getClass() == modifierClass && aMod.getName().equals(symbolModifier.getName())) {
				return aMod;
			}
		}
		return null;
	}
	
	/**
	 * Get the index of the symbol modifier of a given class. 
	 * @param modifierClass modifier class
	 * @return index, or -1 if it doesn't exist. 
	 */
	private int findSymbolModifier(Class modifierClass) {
		for (int i = 0; i < symbolModifiers.size(); i++) {
			if (symbolModifiers.get(i).getClass() == modifierClass) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Add a symbol modifier. Don't add if one of same class already exists. Generally
	 * this function is called from the symbol manager which will add the 
	 * appropriate additional modifiers. 
	 * @param symbolModifier
	 */
	public void addSymbolModifier(SymbolModifier symbolModifier) {
		if (hasSymbolModifier(symbolModifier) != null) {
			return;
		}
		symbolModifiers.add(symbolModifier);
	}
	
	/**
	 * Add a symbol modifier at a specific position in the list. Can be used to 
	 * get a module specific modifier higher up the list, e.g. click type modifier 
	 * coming before click event modifier. 
	 * @param symbolModifier symbol modifier
	 * @param position position, 0 = first in list 
	 */
	public void addSymbolModifier(SymbolModifier symbolModifier, int position) {
		if (hasSymbolModifier(symbolModifier) != null) {
			return;
		}
		int pos = Math.min(symbolModifiers.size(), position);
		
		symbolModifiers.add(pos, symbolModifier);
	}
	
	/**
	 * Add a symbol modifier directly after a different symbol modifier of a given class. 
	 * Can use this function to put a symbol modifier as a specific position in the list. 
	 * If a modifier of the other class can't be found, it will be put at the end of the list. 
	 * @param symbolModifier Symbol modifier
	 * @param insertAfter Class of the symbol modifier that should be immediately before this one. 
	 */
	public void addSymbolModifier(SymbolModifier symbolModifier, Class insertAfter) {
		int modPos = findSymbolModifier(insertAfter);
		if (modPos >= 0) {
			// put at position
			addSymbolModifier(symbolModifier, modPos); 
		}
		else {
			// put at end
			addSymbolModifier(symbolModifier);
		}
	}
	
	/**
	 * Get the full list of symbol modifiers. don't add to them here though, 
	 * this is just for use in the dialog and other internal functions. 
	 * @return full list of symbol modifiers. 
	 */
	public ArrayList<SymbolModifier> getSymbolModifiers() {
		return symbolModifiers;
	}
	
	/**
	 * Remove a symbol modifier based on it's class. 
	 * @param modifierClass
	 * @return true if it existed. 
	 */
	public boolean removeSymbolModifier(Class modifierClass) {
		ListIterator<SymbolModifier> iter = symbolModifiers.listIterator();
		while (iter.hasNext()) {
			SymbolModifier symbolModifier = iter.next();
			if (symbolModifier.getClass() == modifierClass) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	abstract public void setSymbolOptions(PamSymbolOptions symbolOptions);
	
	abstract public PamSymbolOptions getSymbolOptions();

}
