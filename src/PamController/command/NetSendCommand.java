package PamController.command;

import PamController.PamControlledUnit;
import PamController.PamController;
import networkTransfer.send.NetworkSender;

public class NetSendCommand extends ExtCommand {

	public NetSendCommand(String name) {
		super(name, true);
	}
	
	public NetworkSender getNetworkSender() {
		PamControlledUnit netSend = PamController.getInstance().findControlledUnit(NetworkSender.class, null);
		if (netSend instanceof NetworkSender) {
			return (NetworkSender) netSend;
		}
		return null;
	}

	@Override
	public final String execute(String command) {
		NetworkSender netSend = getNetworkSender();
		if (netSend == null) {
			return "No Network Sender";
		}
		else {
			return execute(netSend, command);
		}
	}

	protected String execute(NetworkSender netSend, String command) {
		return netSend.executeExternalCommand(command);
	}

	@Override
	public String getHint() {
		return String.format("Send a \"%s\" information request to the Network Send module", getName());
	}

}
