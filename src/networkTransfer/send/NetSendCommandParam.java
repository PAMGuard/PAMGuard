package networkTransfer.send;

import java.util.HashMap;
import java.util.Map;

public enum NetSendCommandParam {
	

	 ADDRESS("-netSend.address"),
	 PORT("-netSend.port"),
	 ID1("-netSend.id1"),
	 ID2("-netSend.id2"),
	 USER("-netSend.user"),
	 PASSWORD("-netSend.password"),
	 USESSL("-netSend.ssl"),
	 USEMQTT("-netSend.mqtt"),
	 TRUSTPATH("-netSend.trustPath"),
	 TRUSTPASS("-netSend.trustPass"),
	 KEYPATH("-netSend.keyPath"),
	 KEYPASS("-netSend.keyPass"),
	 SENDJSON("-netSend.json"),
	 PERSISTANCE_DIRECTORY("-netSend.percistanceDir");
	
	 private static final Map<String, NetSendCommandParam> PARAM_BY_ARG = new HashMap<>();
	
	 public final String arg;

	 static {
		 for (NetSendCommandParam e: values()) {
			 PARAM_BY_ARG.put(e.arg, e);
		 }
	 }
	 
	 private NetSendCommandParam(String arg) {
		 this.arg = arg;
	 }

	 public static NetSendCommandParam arg(String label) {
		 return PARAM_BY_ARG.get(label);
	 }
	 
	 public static boolean isArgRegistered(String arg) {
		 if(PARAM_BY_ARG.containsKey(arg)) {
			 return true;
		 }
		 return false;
	 }
	 
}
