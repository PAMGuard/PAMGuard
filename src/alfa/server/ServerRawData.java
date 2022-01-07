package alfa.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import PamguardMVC.debug.Debug;

/**
 * Class to handle the raw data string from the server, which contains EVERYTHING as json. 
 * Example string is {"imei": "300234068339920", "device_type": "ROCKBLOCK", "serial": "16945", "momsn": "27", "transmit_time": "19-06-21 19:28:40", "iridium_latitude": "57.9165", 
 * "iridium_longitude": "-148.9899", "iridium_cep": "4.0", "iridium_session_status": "0", "data": 
 * "2450475354412c312c3139303632312c3138323833332c35372e383838332c2d3134392e323034342c36302c3130302c35372e383936362c2d3134392e303136342c312c31302c482c342c302c302c302c312c482c342c302c302c302c302c482c342c302c302c302c302c482c342c302c302c302c30"}
 * @author dg50
 *
 */
public class ServerRawData {

	private Integer momsn;

	private ServerRawData(Integer momsn) {
		this.momsn = momsn;
	}
	
	public static ServerRawData unpackRawJson(String jString) {

		JsonFactory jf = new JsonFactory();
		Integer momsn;
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jString.getBytes()));
			JsonNode jNode = jTree.findValue("momsn");
			momsn = jNode.asInt();
		} catch (IOException e) {
			Debug.out.printf("Interval data logging unable to interpret histogram string: %s", jString);
			return null;
		}
		
		return new ServerRawData(momsn);
	}

	/**
	 * @return the momsn
	 */
	public Integer getMOMSN() {
		return momsn;
	}

}
