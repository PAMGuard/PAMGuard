package analoginput;

/**
 * Information to be passed from a user of analogue sensor data
 * @author dg50
 *
 */
public class SensorChannelInfo {

	private String name;
	
	private String toolTip;

	/**
	 * @param name
	 */
	public SensorChannelInfo(String name) {
		this.name = name;
	}

	/**
	 * @param name
	 * @param toolTip
	 */
	public SensorChannelInfo(String name, String toolTip) {
		super();
		this.name = name;
		this.toolTip = toolTip;
	}
	
	/**
	 * Make an array of SensorChannelInfo's from an array of names. 
	 * @param names array of names
	 * @return array of SensorChannelInfo
	 */
	public static SensorChannelInfo[] makeQuick(String[] names) {
		SensorChannelInfo[] inf = new SensorChannelInfo[names.length];
		for (int i = 0; i < names.length; i++) {
			inf[i] = new SensorChannelInfo(names[i]);
		}
		return inf;
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
	 * @return the toolTip
	 */
	public String getToolTip() {
		return toolTip;
	}

	/**
	 * @param toolTip the toolTip to set
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

}
