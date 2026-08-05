package networkTransfer;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import PamController.PamController;
import PamController.PamGUIManager;
import networkTransfer.mqttClient.PamMqttClient;
import networkTransfer.send.ClientConnectFailedException;
import networkTransfer.send.NetTransmitException;
import networkTransfer.send.NetworkQueuedObject;
import networkTransfer.send.NetworkSendParams;
import networkTransfer.send.NetworkSender;
import pamguard.Pamguard;
import warnings.PamWarning;
import warnings.WarningSystem;

public abstract class NetworkClient {
	
	protected NetworkParams networkParams;
	
	PamWarning sendWarning;
	
	public boolean requireReconnect;
	
	protected boolean initializing;
	
	public NetworkClient(NetworkParams netParams) {
		this.networkParams = netParams;
		sendWarning = new PamWarning("Network Send Error","Warn!",0);
		initializing = false;
	}
	
	public abstract void configureClient(NetworkParams networkParams);

	public abstract boolean connect() throws ClientConnectFailedException;
	
	public abstract void disconnect();
	
	public abstract boolean isConnected();
	
	public abstract void sendNetworkQueuedObject(NetworkQueuedObject qo) throws NetTransmitException;
	
	public abstract void additionalClose();

	public abstract void notifyModelChanged(int changeType);
	
	public abstract int getQueueLength();
	
	public abstract int getQueueSize();

	public abstract boolean testClient() throws ClientConnectFailedException;
	
	public String getStatus() {
		
		if(initializing) {
			return "Initializing";
		}
		
		if(this.isConnected()) {
			return "Connected";
		}
		if(requireReconnect) {
			return "Connection Error";
		}
		return "Disconnected";
	}
	
	public void close() {
		disconnect();
		additionalClose();
	}
	
	public boolean isInitializing() {
		return this.initializing;
	}
	
	public SocketFactory getSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
		
		
		if(this.networkParams.useSystemTrustStore) {
			if(PamController.getInstance().getRunMode()==PamGUIManager.NOGUI) {
				System.setProperty("javax.net.ssl.trustStore","/etc/ssl/certs/aps_store.jks");
			    System.setProperty("javax.net.ssl.trustStorePassword", "APSKEYSTORE001");
			}
			return SSLSocketFactory.getDefault();
		}
		
		SSLContext context = SSLContext.getInstance("TLSv1.3");
		
		KeyManager[] keys = null;
		
		if(this.networkParams.keyStorePath!=null) {
			FileInputStream keyFile = new FileInputStream(this.networkParams.keyStorePath);
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyFile, this.networkParams.keyStorePassword.toCharArray());
			keyFile.close();
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, this.networkParams.keyStorePassword.toCharArray());
			keys = kmf.getKeyManagers();
		}
		

		KeyStore trustStore =  KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream trustFile = new FileInputStream(this.networkParams.trustStorePath);
		trustStore.load(trustFile,this.networkParams.trustStorePassword.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(trustStore);
		
		context.init(keys, tmf.getTrustManagers(), new SecureRandom());
		SSLContext.setDefault(context);
		
		return context.getSocketFactory();
	}
	
	public void setWarning(String message) {
		setWarning(message,2);
	}
	
	boolean initialWarningSet = false;
	
	public synchronized void setWarning(String message, int level) {
		/*if(message==null) {
			WarningSystem.getWarningSystem().removeWarning(sendWarning);
			initialWarningSet = false;
		}else {
			sendWarning.setWarningMessage(message);
			sendWarning.setWarnignLevel(level);
			if(!initialWarningSet) {
				initialWarningSet = true;
				WarningSystem.getWarningSystem().addWarning(sendWarning);
			}else {
				WarningSystem.getWarningSystem().updateWarning(sendWarning);
			}
		}*/
	}
	
	public synchronized void removeWarning() {
		/*if(!WarningSystem.getWarningSystem().removeWarning(sendWarning)) {
			WarningSystem.getWarningSystem().forceRemoveWarning(sendWarning);
		}
		initialWarningSet = false;
		*/
	}

	public void updateParams(NetworkSendParams networkSendParams2) {
		this.networkParams = networkSendParams2;
	}
	
}
