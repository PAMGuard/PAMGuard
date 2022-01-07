package bearinglocaliser.algorithms;


public class StaticAlgorithmProperties {
	
	private String name, shortName;

	public StaticAlgorithmProperties(String name) {
		super();
		this.setName(name);
	}

	public StaticAlgorithmProperties(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
