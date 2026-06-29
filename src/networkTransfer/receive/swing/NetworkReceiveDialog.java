package networkTransfer.receive.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import networkTransfer.NetworkParams;
import networkTransfer.NetworkParamsPanel;
import networkTransfer.receive.NetworkReceiveParams;
import networkTransfer.receive.NetworkReceiver;

public class NetworkReceiveDialog extends PamDialog {
	
	private static NetworkReceiveDialog singleInstance;
	
	private NetworkReceiver networkReceiver;
	
	private NetworkReceiveParams networkReceiveParams;
	
	private JTextField receivePort;
	
	private JRadioButton channelsRenumber, channelsMaintain;
	
	private JTextField compassOffset;
	
	private JRadioButton connectionStandard, connectionMqtt; 
	
	private StandardNetRxPanel portPanel;
	
	private NetworkParamsPanel networkParamsPanel;
	
	private JPanel mainPanel;
	
	private MethodSwitch methods;
	

	private NetworkReceiveDialog(Window parentFrame, NetworkReceiver networkReceiver) {
		super(parentFrame, networkReceiver.getUnitName(), true);
		this.networkReceiver = networkReceiver;
		this.networkReceiveParams = networkReceiver.getNetworkReceiveParams().clone();
		
		JTabbedPane tabbedPanel = new JTabbedPane();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel connectionModePanel = new JPanel();
		connectionModePanel.setBorder(new TitledBorder("Connection Type"));
		connectionModePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		ButtonGroup bg = new ButtonGroup();
		bg.add(connectionStandard = new JRadioButton("Standard TCP Host"));
		bg.add(connectionMqtt = new JRadioButton("Mqtt Subscriber"));
		connectionStandard.setToolTipText("Old simple TCP host");
		connectionMqtt.setToolTipText("Setup net receiver as mqtt subscriber");
		/*channelsRenumber.setToolTipText("Channels will re renumbered 0, 1, 2, etc. a unique number being used for each sending station." +
				"\nThis is generally the best option when multiple stations are sending to this receiving station.");
		channelsMaintain.setToolTipText("Channels will not be renumbered. i.e. they will keep the numbers assigned by the sending station." +
				"\nThis is generally the best option when a single remote station is sending to this receiving station.");*/
		addComponent(connectionModePanel, connectionStandard, c);
		c.gridy++;
		addComponent(connectionModePanel, connectionMqtt, c);
		mainPanel.add(connectionModePanel);
		
		methods = new MethodSwitch();
		connectionStandard.addActionListener(methods);
		connectionMqtt.addActionListener(methods);
		
		portPanel = new StandardNetRxPanel();
		mainPanel.add(portPanel.panel);
		
		networkParamsPanel = new NetworkParamsPanel(this,this.networkReceiveParams,false);
		mainPanel.add(networkParamsPanel.getNetParamsPanel());
		
		JPanel channelPanel = new JPanel();
		channelPanel.setBorder(new TitledBorder("Channel Numbering"));
		channelPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		bg = new ButtonGroup();
		bg.add(channelsRenumber = new JRadioButton("Renumber channels"));
		bg.add(channelsMaintain = new JRadioButton("Maintain channel numbers"));
		channelsRenumber.setToolTipText("Channels will re renumbered 0, 1, 2, etc. a unique number being used for each sending station." +
				"\nThis is generally the best option when multiple stations are sending to this receiving station.");
		channelsMaintain.setToolTipText("Channels will not be renumbered. i.e. they will keep the numbers assigned by the sending station." +
				"\nThis is generally the best option when a single remote station is sending to this receiving station.");
		addComponent(channelPanel, channelsMaintain, c);
		c.gridy++;
		addComponent(channelPanel, channelsRenumber, c);
		mainPanel.add(channelPanel);
		
		JPanel compassPanel = new JPanel();
		compassPanel.setLayout(new GridBagLayout());
//		compassPanel.set
		c = new PamGridBagContraints();
		compassPanel.add(new JLabel("Offset ", JLabel.RIGHT), c);
		c.gridx++;
		compassPanel.add(compassOffset = new JTextField(5), c);
		c.gridx++;
		compassPanel.add(new JLabel(" " + LatLong.deg, JLabel.LEFT), c);
		
		
		tabbedPanel.add("Network", mainPanel);
		tabbedPanel.add("Compass", compassPanel);
		setDialogComponent(tabbedPanel);
		
	}
	
	private class MethodSwitch implements ActionListener{
		
		public void runSwap() {
			if(connectionStandard.isSelected()) {
				portPanel.panel.setVisible(true);
				networkParamsPanel.panel.setVisible(false);
			}else {
				portPanel.panel.setVisible(false);
				networkParamsPanel.panel.setVisible(true);
			}
			mainPanel.repaint();
			mainPanel.revalidate();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runSwap();
			
		}
		
	}

	public static NetworkReceiveParams showDialog(Window parentFrame, NetworkReceiver networkReceiver) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.networkReceiver != networkReceiver) {
			singleInstance = new NetworkReceiveDialog(parentFrame, networkReceiver);
		}
		singleInstance.networkReceiveParams = networkReceiver.getNetworkReceiveParams().clone();
		//singleInstance.networkReceiveParams.stationName = "base";
		singleInstance.setParams();
		singleInstance.setVisible(true);
		singleInstance.runSwap();
		return (NetworkReceiveParams) singleInstance.networkReceiveParams;
	}
	
	private void setParams() {
		receivePort.setText(String.format("%d", networkReceiveParams.receivePort));
		channelsRenumber.setSelected(networkReceiveParams.channelNumberOption == NetworkReceiveParams.CHANNELS_RENUMBER);
		channelsMaintain.setSelected(networkReceiveParams.channelNumberOption == NetworkReceiveParams.CHANNELS_MAINTAIN);
		compassOffset.setText(String.format("%3.1f",networkReceiveParams.compassCorrection));
		networkParamsPanel.setParams(networkReceiveParams);
		if(networkReceiveParams.connectionType==NetworkReceiveParams.CONNECTIONTYPE_STANDARD_TCP) {
			connectionStandard.setSelected(true);
			connectionMqtt.setSelected(false);
		}else {
			connectionStandard.setSelected(false);
			connectionMqtt.setSelected(true);
		}
		//
	}
	
	public void runSwap() {
		methods.runSwap();
	}

	@Override
	public void cancelButtonPressed() {
		networkReceiveParams = null;
	}

	@Override
	public boolean getParams() {
		
		
		if(connectionStandard.isSelected()) {
			networkReceiveParams.connectionType = NetworkReceiveParams.CONNECTIONTYPE_STANDARD_TCP;
			try {
				networkReceiveParams.receivePort = Integer.valueOf(receivePort.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid port number");
			}
		} else {
			networkReceiveParams.connectionType = NetworkReceiveParams.CONNECTIONTYPE_MQTT;
			if(!networkParamsPanel.getParams()) {
				return false;
			}
		}
		
		if (channelsRenumber.isSelected()) {
			networkReceiveParams.channelNumberOption = NetworkReceiveParams.CHANNELS_RENUMBER;
		}
		else {
			networkReceiveParams.channelNumberOption = NetworkReceiveParams.CHANNELS_MAINTAIN;
		}
		
		try {
			networkReceiveParams.compassCorrection = Double.valueOf(compassOffset.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Compass Offset value");
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		networkReceiveParams = new NetworkReceiveParams();
		setParams();
	}
	
	private class StandardNetRxPanel extends PamPanel{
		
		public JPanel panel;
		
		public StandardNetRxPanel (){
			JPanel portPanel = new JPanel();
			portPanel.setBorder(new TitledBorder("Connection"));
			portPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(portPanel, new JLabel("Receive Port ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(portPanel, receivePort = new JTextField(5), c);
			panel = portPanel;
		}
		
		
	}
	
}
