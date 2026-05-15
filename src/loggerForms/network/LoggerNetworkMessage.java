package loggerForms.network;

public class LoggerNetworkMessage {
	
	private String topic; 
	
	private byte[] data;
	
	private String error;

	/**
	 * @param topic
	 * @param data
	 * @param error
	 */
	public LoggerNetworkMessage(String topic, byte[] data, String error) {
		super();
		this.topic = topic;
		this.data = data;
		this.error = error;
	}

	@Override
	public String toString() {
		String str = String.format("LoggerNetworkMessage topic: \"%s\"", topic);
		if (data != null) {
			str += "; payload size " + data.length;
		}
		else {
			str += "; no payload";
		}
		if (error != null) {
			str += "; " + error;
		}
		return str;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

}
