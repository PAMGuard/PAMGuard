package annotation.userforms.species;

import java.io.Serializable;

public class FormsSpeciesSettings implements Cloneable, Serializable{


	public static final long serialVersionUID = 1L;
	
	/**
	 * Control used for species selection - will be a dropdown. 
	 */
	public String selectedControl;

	@Override
	protected FormsSpeciesSettings clone() {
		try {
			return (FormsSpeciesSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
