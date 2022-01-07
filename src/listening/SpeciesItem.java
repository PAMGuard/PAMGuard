package listening;

import java.io.Serializable;

import PamView.PamSymbol;

/**
 * Data for a species to include in things heard data
 * @author Doug Gillespie
 *
 */
public class SpeciesItem implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private PamSymbol symbol;

	public SpeciesItem(String name) {
		super();
		this.name = name;
	}

	@Override
	protected SpeciesItem clone() {
		try {
			return (SpeciesItem) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PamSymbol getSymbol() {
		return symbol;
	}

	public void setSymbol(PamSymbol symbol) {
		this.symbol = symbol;
	}
	
}
