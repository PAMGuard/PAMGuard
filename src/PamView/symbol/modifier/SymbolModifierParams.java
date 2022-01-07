package PamView.symbol.modifier;

import java.io.Serializable;

public class SymbolModifierParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Map of what's to be modified. 
	 */
	public int modBitMap = 0;
	
	@Override
	protected SymbolModifierParams clone()  {
		try {
			return (SymbolModifierParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}


}
