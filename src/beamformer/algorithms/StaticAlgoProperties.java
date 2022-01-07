package beamformer.algorithms;

/**
 * Fixed properties of a beam forming algorithm. 
 * @author dg50
 *
 */
public class StaticAlgoProperties {

	private String name;
		
	private boolean canBeamogram;

	private String shortName;

	public StaticAlgoProperties(String name, String shortName, boolean canBeamogram) {
		super();
		this.name = name;
		this.shortName = shortName;
		this.canBeamogram = canBeamogram;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the canBeamogram
	 */
	public boolean isCanBeamogram() {
		return canBeamogram;
	}

	/**
	 * @param canBeamogram the canBeamogram to set
	 */
	public void setCanBeamogram(boolean canBeamogram) {
		this.canBeamogram = canBeamogram;
	}

	/**
	 * Get a shorter name more suitable for writing to the database and to binary files. 
	 * @return
	 */
	public String getShortName() {
		return shortName;
	}
	
}
