package PamView.symbol;

import java.io.Serializable;
import java.util.Hashtable;

public class ManagedSymbolData implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Use generic symbol choice - the same for all displays. 
	 */
	public boolean useGeneric = false;
	
	/**
	 * A list of options used by the different displays of different names. 
	 */
	private Hashtable<String, PamSymbolOptions> symbolOptions;

	@Override
	public ManagedSymbolData clone() {
		try {
			return (ManagedSymbolData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Hashtable<String, PamSymbolOptions> getSymbolOptions() {
		if (symbolOptions == null) {
			symbolOptions = new Hashtable<>();
		}
		return symbolOptions;
	}

	
}
