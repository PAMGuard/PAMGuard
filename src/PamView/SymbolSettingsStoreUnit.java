package PamView;

import java.io.Serializable;

class SymbolSettingsStoreUnit implements Serializable {
	
	static public final long serialVersionUID = 1;
	
	private String description;

	private PamSymbol pamSymbol;

	public SymbolSettingsStoreUnit(String description, PamSymbol pamSymbol) {
		super();
		this.description = description;
		this.pamSymbol = pamSymbol;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PamSymbol getPamSymbol() {
		return pamSymbol;
	}

	public void setPamSymbol(PamSymbol pamSymbol) {
		this.pamSymbol = pamSymbol;
	}
	
}
