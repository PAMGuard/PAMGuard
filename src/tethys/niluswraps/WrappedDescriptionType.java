package tethys.niluswraps;

import java.io.Serializable;

import nilus.DescriptionType;
import nilus.Helper;

/**
 * Because we want to save DescriptionType objects in serialised
 * psfx files and because Nilus description types are not serialised
 * these have to be wrapped in a total bodge way with reasonably convenient 
 * constructors and getters for converting back and forth from the nilus object.
 * this is now slightly more rationalised in NilusSettingsWrapper.  
 * @author dg50
 *
 */
public class WrappedDescriptionType extends NilusSettingsWrapper<DescriptionType> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor from a set of strings
	 * @param objectives
	 * @param _abstract
	 * @param method
	 */
	public WrappedDescriptionType(String objectives, String _abstract, String method) {
		super();
		DescriptionType description = getDescription();
		description.setObjectives(objectives);
		description.setAbstract(_abstract);
		description.setMethod(method);
	}
	
	public DescriptionType getDescription() {
		DescriptionType description = getNilusObject(DescriptionType.class);
		if (description == null) {
			description = new DescriptionType();
			try {
				Helper.createRequiredElements(description);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			setNilusObject(description);
		}
		return description;
	}
	
	public void setDescription(DescriptionType description) {
		setNilusObject(description);
	}
	
	/**
	 * Construct from a nilus object
	 * @param descriptionType
	 */
	public WrappedDescriptionType(DescriptionType descriptionType) {
		this.setNilusObject(descriptionType);
	}

	public WrappedDescriptionType() {
	}

	public String getObjectives() {
		return getDescription().getObjectives();
	}

	/**
	 * @return the _abstract
	 */
	public String getAbstract() {
		return getDescription().getAbstract();
	}

	/**
	 * @param _abstract the _abstract to set
	 */
	public void setAbstract(String _abstract) {
		getDescription().setAbstract(_abstract);
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return getDescription().getMethod();
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		DescriptionType description = getDescription();
		if (description == null) {
			return;
		}
		getDescription().setMethod(method);
	}

	/**
	 * @param objectives the objectives to set
	 */
	public void setObjectives(String objectives) {
		getDescription().setObjectives(objectives);;
	}

}
