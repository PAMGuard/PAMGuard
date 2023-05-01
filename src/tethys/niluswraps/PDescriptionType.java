package tethys.niluswraps;

import java.io.Serializable;

import nilus.DescriptionType;

/**
 * Because we want to save DescriptionType objects in serialised
 * psfx files and because Nilus description types are not serialised
 * these have to be wrapped in a total bodge way with reasonably convenient 
 * constructors and getters for converting back and forth from the nilus object. 
 * @author dg50
 *
 */
public class PDescriptionType implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String objectives;
	
	protected String _abstract;
	
	protected String method;
	
	/**
	 * Constructor from a set of strings
	 * @param objectives
	 * @param _abstract
	 * @param method
	 */
	public PDescriptionType(String objectives, String _abstract, String method) {
		super();
		this.objectives = objectives;
		this._abstract = _abstract;
		this.method = method;
	}
	
	/**
	 * Construct from a nilus object
	 * @param descriptionType
	 */
	public PDescriptionType(DescriptionType descriptionType) {
		this.objectives = descriptionType.getObjectives();
		this._abstract = descriptionType.getAbstract();
		this.method = descriptionType.getMethod();
	}

	public PDescriptionType() {
	}

	public String getObjectives() {
		return objectives;
	}

	/**
	 * @return the _abstract
	 */
	public String getAbstract() {
		return _abstract;
	}

	/**
	 * @param _abstract the _abstract to set
	 */
	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @param objectives the objectives to set
	 */
	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}

	/**
	 * convert into a nilus object for output. 
	 * @return
	 */
	public DescriptionType getDescription() {
		DescriptionType descriptionType = new DescriptionType();
		descriptionType.setAbstract(_abstract);
		descriptionType.setObjectives(objectives);
		descriptionType.setMethod(method);
		return descriptionType;
	}
}
