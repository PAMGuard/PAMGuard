package nidaqdev.networkdaq;

public enum ChassisConfig {
	
	NI9063(9063, "CRio 9063 with 9223 card"),
	NI90639222(90639222, "CRio 9063 with 9222 card"),
	NI9067(9067, "CRio 9067 with 9222 cards"),
	NI9068(9068, "CRio 9068 with 9222 cards");

	private int chassisId;
	private String desc;

	ChassisConfig(int Id, String descr) {
		this.chassisId = Id;
		this.desc = descr;
	}

	/**
	 * @return the chassisId
	 */
	public int getChassisId() {
		return chassisId;
	}

	/**
	 * @return the description of the chassis
	 */
	public String getDescription() {
		return desc;
	}

	@Override
	public String toString() {
		return desc;
	}
	
}
