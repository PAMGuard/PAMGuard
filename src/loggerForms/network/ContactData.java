package loggerForms.network;

/*
 * information sent from stations connected to the Logger network
 */
public class ContactData {

	private long lastUpdateTime;
	
	private String battery;


	/**
	 * @param lastUpdateTime
	 * @param battery
	 */
	public ContactData(long lastUpdateTime, String battery) {
		super();
		this.lastUpdateTime = lastUpdateTime;
		this.battery = battery;
	}

	/**
	 * @return the battery
	 */
	public String getBattery() {
		return battery;
	}

	/**
	 * @param battery the battery to set
	 */
	public void setBattery(String battery) {
		this.battery = battery;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * @param lastUpdateTime the lastUpdateTime to set
	 */
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	
}
