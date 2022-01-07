package tipOfTheDay;

/**
 * Stores information for a single tip of the day
 * @author Doug Gillespie
 * @see TipOfTheDayManager
 */
public class PamTip {

	private String topic;
	
	private String tip;
	
	private String module;
	
	private String helpURL;

	public PamTip(String topic, String tip, String module, String helpURL) {
		super();
		this.topic = topic;
		this.tip = tip;
		this.module = module;
		this.helpURL = helpURL;
	}
	
	public PamTip(String topic, String tip) {
		super();
		this.topic = topic;
		this.tip = tip;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getHelpURL() {
		return helpURL;
	}

	public void setHelpURL(String helpURL) {
		this.helpURL = helpURL;
	}
	
	
}
