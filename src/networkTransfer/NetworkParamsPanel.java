package networkTransfer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import networkTransfer.mqttClient.PamMqttClient;
import networkTransfer.send.ClientConnectFailedException;
import networkTransfer.send.NetworkSendDialog;
import networkTransfer.send.NetworkSendParams;
import networkTransfer.send.TCPSendClient;

public class NetworkParamsPanel extends PamPanel{
	
	private NetworkParams networkParams;
	
	private JTextField ipAddress, portNumber;
	
	private JTextField userName;

	private JPasswordField password;

	private JCheckBox rememberPassword;
	
	private JCheckBox useSSL;
	
	private JCheckBox useMqtt;
	
	private boolean showMqttSelect;
	
	private JButton testConnection;
	
	private TlsConfigurePanel tlsConfigurePanel;
	
	private PamDialog parentDialog;
	
	private JPanel primaryPanel;
	
	private TlsCheckListener tlsCheckListener;
	
	public JPanel panel;
	
	public JTextField baseTopic, stationId, persistenceDir;
	
	public NetworkParamsPanel(PamDialog parentDialog, NetworkParams networkParams, boolean showMqttSelect) {
		this.networkParams = networkParams;
		this.showMqttSelect = showMqttSelect;
		this.parentDialog = parentDialog;

	}
	
	public JPanel getNetParamsPanel() {
		primaryPanel = new JPanel(new BorderLayout());
						
		JPanel ipPanel = new JPanel();
		ipPanel.setBorder(new TitledBorder("Host details"));
		ipPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(ipPanel, new JLabel("Host address ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, ipAddress = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("Port number ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, portNumber = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("User name ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, userName = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("password ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, password = new JPasswordField(20), c);
		c.gridx = 1;
		c.gridy++;
		addComponent(ipPanel, rememberPassword = new JCheckBox("Remember Password"), c);
		c.gridx = 1;
		c.gridy++;
		addComponent(ipPanel,useSSL = new JCheckBox("Use TLS Protocol"),c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("Base Topic ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, baseTopic = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("Station ID ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, stationId = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(ipPanel, new JLabel("Persistance Directory ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(ipPanel, persistenceDir = new JTextField(60), c);
		c.gridx = 0;
		c.gridy++;
		
		if(this.showMqttSelect) {
			addComponent(ipPanel,useMqtt = new JCheckBox("Use Mqtt Transmission"),c);
			c.gridx = 1;
			c.gridy++;
			c.gridwidth = 1;
		}
		
		addComponent(ipPanel, testConnection = new JButton("Test connection"), c);
		
		tlsConfigurePanel = new TlsConfigurePanel(this.networkParams);
		
		primaryPanel.add(BorderLayout.NORTH,ipPanel);
		primaryPanel.add(BorderLayout.SOUTH,tlsConfigurePanel);
		
		tlsCheckListener = new TlsCheckListener();
		
		useSSL.addActionListener(tlsCheckListener);
		
		testConnection.addActionListener(new TestConnection());
		
		this.panel = primaryPanel;
		
		return primaryPanel;
	}
	
	public void setParams(NetworkParams networkParams) {
		this.networkParams = networkParams;
		ipAddress.setText(networkParams.ipAddress);
		portNumber.setText(String.format("%d",networkParams.portNumber));
		userName.setText(networkParams.userId);
		password.setText(networkParams.password);
		rememberPassword.setSelected(networkParams.savePassword);
		baseTopic.setText(networkParams.baseTopic);
		stationId.setText(networkParams.stationId);
		useSSL.setSelected(networkParams.useSSL);
		persistenceDir.setText(networkParams.persistenceDirectory);
		if(this.useMqtt!=null) {
			useMqtt.setSelected(networkParams.mqtt);
		}
		tlsConfigurePanel.setParams(networkParams);
		tlsCheckListener.setTlsConfigVisability();

	}
	
	public boolean getParams() {
		networkParams.ipAddress = ipAddress.getText();
		networkParams.userId = userName.getText();
		networkParams.password = new String(password.getPassword());
		networkParams.useSSL = useSSL.isSelected();
		networkParams.persistenceDirectory = persistenceDir.getText();
		if(this.useMqtt!=null){
			networkParams.mqtt = useMqtt.isSelected();
		}
		networkParams.baseTopic = baseTopic.getText();
		networkParams.stationId = stationId.getText();
		tlsConfigurePanel.getParams();
		try {
			networkParams.portNumber = Integer.valueOf(portNumber.getText());
		}
		catch (NumberFormatException e) {
			return parentDialog.showWarning("Invalid port address: " + portNumber.getText());
		}
		return true;
	}
	
	private class TestConnection implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			testConnection();
		}
	}
	
	private class TlsCheckListener implements ActionListener{
		
		public void setTlsConfigVisability() {
			
			if(useSSL.isSelected()) {
				tlsConfigurePanel.toggleAll(true);
			}else {
				tlsConfigurePanel.toggleAll(false);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setTlsConfigVisability();
		}
		
	}
	
	public void testConnection() {
		parentDialog.getParams();
		NetworkClient client;
		boolean testFailed = false;
		try {
			if(this.useMqtt==null || this.useMqtt.isSelected()) {
				PamMqttClient.test(this.networkParams);
			}else {
				client = new TCPSendClient(this.networkParams);
			}
		}catch(ClientConnectFailedException e) {
			testFailed = true;
			parentDialog.showWarning(e.getMessage());
			e.printStackTrace();
		}
		
		if(!testFailed) {
			parentDialog.showWarning("Test Success!");
		}
	}
	
	private class TlsConfigurePanel extends PamPanel{
		
		JCheckBox useSystemTrustStore;
		JTextField trustStorePath;
		JPasswordField trustStorePassword;
		JTextField keyStorePath;
		JPasswordField keyStorePassword;
		NetworkParams netParams;
		
		private TlsConfigurePanel(NetworkParams netParams){
			this.netParams = netParams;
			this.setBorder(new TitledBorder("SSL Config"));
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			NetworkSendDialog.addComponent(this, new JLabel("Use System Certificates ", SwingConstants.RIGHT), c);
			c.gridx++;
			NetworkSendDialog.addComponent(this, useSystemTrustStore = new JCheckBox(), c);
			c.gridx = 0;
			c.gridy++;
			NetworkSendDialog.addComponent(this, new JLabel("Trust store path", SwingConstants.RIGHT), c);
			c.gridx++;
			NetworkSendDialog.addComponent(this, trustStorePath = new JTextField(20), c);
			c.gridx = 0;
			c.gridy++;
			NetworkSendDialog.addComponent(this, new JLabel("password ", SwingConstants.RIGHT), c);
			c.gridx++;
			NetworkSendDialog.addComponent(this, trustStorePassword = new JPasswordField(30), c);
			c.gridx = 0;
			c.gridy++;
			NetworkSendDialog.addComponent(this, new JLabel("Key store path", SwingConstants.RIGHT), c);
			c.gridx++;
			NetworkSendDialog.addComponent(this, keyStorePath = new JTextField(20), c);
			c.gridx = 0;
			c.gridy++;
			NetworkSendDialog.addComponent(this, new JLabel("password ", SwingConstants.RIGHT), c);
			c.gridx++;
			NetworkSendDialog.addComponent(this, keyStorePassword = new JPasswordField(30), c);
			useSystemTrustStore.addActionListener(new UseSysStoreListen());
			
		}

		public void setParams(NetworkParams netParams) {
			this.netParams = netParams;
			useSystemTrustStore.setSelected(netParams.useSystemTrustStore);
			if(netParams.trustStorePath!=null) {
				trustStorePath.setText(String.format("%s", netParams.trustStorePath));
			}
			if(netParams.trustStorePassword!=null) {
				trustStorePassword.setText(String.format("%s",netParams.trustStorePassword));
			}
			if(netParams.keyStorePath!=null) {
				keyStorePath.setText(String.format("%s", netParams.keyStorePath));
			}
			if(netParams.keyStorePassword!=null) {
				keyStorePassword.setText(String.format("%s",netParams.keyStorePassword));
			}
		}
		
		public void toggleAll(boolean on) {
			useSystemTrustStore.setEnabled(on);
			trustStorePath.setEnabled(on);
			trustStorePassword.setEnabled(on);
			keyStorePath.setEnabled(on);
			keyStorePassword.setEnabled(on);
		}

		public boolean getParams() {
			netParams.useSystemTrustStore = useSystemTrustStore.isSelected();
			
			if(!useSSL.isSelected()) {
				return true;
			}
			
			if(!netParams.useSystemTrustStore){
				boolean properStrings = !trustStorePath.getText().isEmpty() && !trustStorePath.getText().isBlank() && trustStorePassword.getPassword()!=null && trustStorePassword.getPassword().length!=0;
				if(!properStrings) {
					return parentDialog.showWarning("If not using system certificates, you must provide a valid trust store path and password");
				}
				File supposedTrustStore = new File(trustStorePath.getText());
				boolean trustStorePathExists = supposedTrustStore.exists();
				if(!trustStorePathExists) {
					return parentDialog.showWarning("If not using system certificates, you must provide a valid trust store");
				}
				netParams.trustStorePath = trustStorePath.getText();
				netParams.trustStorePassword = new String(trustStorePassword.getPassword());
				
			}
			netParams.keyStorePath = keyStorePath.getText();
			netParams.keyStorePassword = new String(keyStorePassword.getPassword());
			if(keyStorePath.getText().isBlank() || keyStorePath.getText().isEmpty()) {
				netParams.keyStorePath = null;
				netParams.keyStorePassword = null;
			}
			return true;
		}
		
		
		private class UseSysStoreListen implements ActionListener{
			
			public UseSysStoreListen() {
				trustStorePath.setEnabled(!useSystemTrustStore.isSelected());
				trustStorePassword.setEnabled(!useSystemTrustStore.isSelected());
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				trustStorePath.setEnabled(!useSystemTrustStore.isSelected());
				trustStorePassword.setEnabled(!useSystemTrustStore.isSelected());
			}
			
		}
	}
	
}
