package loggerForms.network;

import javax.swing.JMenuItem;

public class LoggerDummyNetwork extends LoggerNetworkManager {

	public LoggerDummyNetwork() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean sendData(String station, String topic, byte[] payload) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void subsribeTopic(String topic, LoggerNetworkReceiver messageUser) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean unsubscribeTopic(String topic, LoggerNetworkReceiver messageUser) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean setupListener() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean closeListener() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public JMenuItem getConfigMenu() {
		// TODO Auto-generated method stub
		return null;
	}

}
