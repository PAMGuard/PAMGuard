
package networkTransfer.send;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import networkTransfer.NetworkClient;
import networkTransfer.NetworkParamsPanel;
import networkTransfer.mqttClient.PamMqttClient;

public class NetworkSendDialog extends PamDialog {

	private static NetworkSendDialog singleInstance;

	private NetworkSendParams networkSendParams;
		
	private NetworkSender networkSender;

	private DataPanel dataPanel;

	private QueuePanel queuePanel;
		
	private JTabbedPane tabbedPane;

	private FormatPanel formatPanel;
		
	private NetworkParamsPanel netParamsPanel;
	
	private NetworkSendDialog(Window parentFrame, NetworkSender networkSender) {
		super(parentFrame, "Network Sending", false);
		this.networkSender = networkSender;

		tabbedPane = new JTabbedPane();
		
		netParamsPanel = new NetworkParamsPanel(this,this.networkSendParams,true);
		
		tabbedPane.add("Connection", netParamsPanel.getNetParamsPanel());

		formatPanel = new FormatPanel();
		tabbedPane.add("Format", formatPanel);
		
		queuePanel = new QueuePanel();
		tabbedPane.add("Queue", queuePanel);

		dataPanel = new DataPanel();
		tabbedPane.add("Data Sources", dataPanel);

		setDialogComponent(tabbedPane);

		setResizable(true);
	}

	public static NetworkSendParams showDialog(Window frame, NetworkSender networkSender, NetworkSendParams networkSendParams) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new NetworkSendDialog(frame, networkSender);
		}
		singleInstance.networkSendParams = networkSendParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.networkSendParams;
	}

	private void setParams() {

		formatPanel.setParams();
		queuePanel.setParams();
//		dataPanel.setParams(networkSendParams.getSendingFormat());
		netParamsPanel.setParams(networkSendParams);
		tabbedPane.invalidate();
		
	}

	@Override
	public void cancelButtonPressed() {
		networkSendParams = null;
	}

	@Override
	public boolean getParams() {
		boolean worked = (dataPanel.getParams() && queuePanel.getParams() && formatPanel.getParams() && netParamsPanel.getParams());
		return worked;
	}

	
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	public void testTCPStandard() {
		
	}
	
	public void testMqtt() {
		
	}

	
	private class DataPanel extends JPanel {

		private JCheckBox[] binaryBoxes;
		private JCheckBox[] jsonBoxes;
		private ArrayList<PamDataBlock> possibles;
		private JPanel streamPanel;
		private JTextField stationId1, stationId2;

		public DataPanel() {
			super();
//			setBorder(new TitledBorder("Data"));
//			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setLayout(new BorderLayout());
			
			JPanel idPanelO = new JPanel(new BorderLayout());
			JPanel idPanel = new JPanel();
			add(BorderLayout.NORTH, idPanelO);
			idPanelO.setBorder(new TitledBorder("Station id"));
			idPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(idPanel, new JLabel("Station Id 1"), c);
			c.gridx++;
			addComponent(idPanel, stationId1 = new JTextField(4), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(idPanel, new JLabel("Station Id 2"), c);
			c.gridx++;
			idPanelO.add(BorderLayout.WEST, idPanel);
			addComponent(idPanel, stationId2 = new JTextField(4), c);
			
			
			
			streamPanel = new JPanel();
			streamPanel.setBorder(new TitledBorder("Output Streams"));
			streamPanel.setLayout(new BoxLayout(streamPanel, BoxLayout.Y_AXIS));
			add(BorderLayout.CENTER, streamPanel);
		}

		public void setParams(int outputFormat) {
			stationId1.setText(String.format("%d", networkSendParams.stationId1));
			stationId2.setText(String.format("%d", networkSendParams.stationId2));
			
			streamPanel.removeAll();
			streamPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = 1;
			JLabel ll;
			streamPanel.add(ll = new JLabel("Binary", JLabel.CENTER), c);
			c.gridx++;
			streamPanel.add(new JLabel("Json", JLabel.CENTER), c);
			
			possibles = networkSender.listPossibleDataSources(NetworkSendParams.NETWORKSEND_BOTH);
			if (possibles == null) {
				return;
			}
			binaryBoxes = new JCheckBox[possibles.size()];
			jsonBoxes = new JCheckBox[possibles.size()];
			int i = 0;
			for (PamDataBlock aBlock:possibles) {
//				checkBoxes[i] = new JCheckBox(aBlock.getDataName());
//				streamPanel.add(checkBoxes[i]);
//				if (networkSendParams.findDataBlock(aBlock) != null) {
//					checkBoxes[i].setSelected(true);
//				}
				boolean isBinary = networkSendParams.getSendSelection(aBlock, NetworkSendParams.NETWORKSEND_BYTEARRAY);
				boolean isJson = networkSendParams.getSendSelection(aBlock, NetworkSendParams.NETWORKSEND_JSON);
				binaryBoxes[i] = new JCheckBox();
				jsonBoxes[i] = new JCheckBox();
				binaryBoxes[i].setSelected(isBinary);
				jsonBoxes[i].setSelected(isBinary);
				if (aBlock.getBinaryDataSource() != null) {
					binaryBoxes[i].setToolTipText(String.format("Output %s data in binary format", aBlock.getDataName()));
				}
				else {
					binaryBoxes[i].setEnabled(false);
					binaryBoxes[i].setToolTipText(String.format("Binary output not available for %d", aBlock.getDataName()));
				}
				if (aBlock.getJSONDataSource() != null) {
					jsonBoxes[i].setToolTipText(String.format("Output %s data in Json format", aBlock.getDataName()));
				}
				else {
					jsonBoxes[i].setEnabled(false);
					jsonBoxes[i].setToolTipText(String.format("Json output not available for %d", aBlock.getDataName()));
				}
				c.gridx = 0;
				c.gridy++;
				streamPanel.add(ll = new JLabel(aBlock.getDataName() + " ", JLabel.RIGHT), c);
				c.gridx++;
				streamPanel.add(binaryBoxes[i], c);
				c.gridx++;
				streamPanel.add(jsonBoxes[i], c);
				i++;
			}
		}

		public boolean getParams() {
			try {
				networkSendParams.stationId1 = Integer.valueOf(stationId1.getText());
				networkSendParams.stationId2 = Integer.valueOf(stationId2.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid satation id");
			}
			
			networkSendParams.clearDataBlocks();
			if (binaryBoxes == null || jsonBoxes == null) {
				return true;
			}
			for (int i = 0; i < binaryBoxes.length; i++) {
				int fmt = 0;
				if (binaryBoxes[i].isSelected()) {
					fmt |= NetworkSendParams.NETWORKSEND_BYTEARRAY;
				}
				if (jsonBoxes[i].isSelected()) {
					fmt |= NetworkSendParams.NETWORKSEND_JSON;
				}
				networkSendParams.setSendFormat(possibles.get(i), fmt);
			}
			return true;
		}

	}

	private class QueuePanel extends JPanel {

		JTextField queueSize, queueLength;
		public QueuePanel() {
			setBorder(new TitledBorder("Max Queue Size"));
			setLayout(new BorderLayout());
			JPanel inny = new JPanel();
			add(BorderLayout.NORTH, inny);
			inny.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(inny, new JLabel("Max Queue Size ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(inny, queueSize = new JTextField(5), c);
			c.gridx++;
			addComponent(inny, new JLabel(" kilobytes"), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(inny, new JLabel("Max Queue Length ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(inny, queueLength = new JTextField(5), c);
			c.gridx++;
			addComponent(inny, new JLabel(" objects"), c);
			c.gridx=0;
			c.gridy++;
			c.gridwidth = 3;
			String jsonWarn = 
					"<html><br>Setting Max Queue Size = 0 means unlimited queue size.<br>" +
							  "This should be used with caution, but may be necessary <br>" +
							  "if the output data is very large (such as when using the <br>" +
							  "json format)</html>";
			addComponent(inny, new JLabel(jsonWarn),c);
			
			
//			add a note to say that json text may need a very large queue size
//			maybe set maxqueuesize=0 for unlimited?
			
		}

		public void setParams() {
			queueLength.setText(String.format("%d", networkSendParams.maxQueuedObjects));
			queueSize.setText(String.format("%d", networkSendParams.maxQueueSize));
		}

		public boolean getParams() {
			try {
				networkSendParams.maxQueuedObjects = Integer.valueOf(queueLength.getText());
				networkSendParams.maxQueueSize = Integer.valueOf(queueSize.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid queue size or length parameter");
			}
			return true;
		}

	}
		
	
	private class FormatPanel extends JPanel {
		JCheckBox byteArray, jsonString;
		ButtonGroup buttonGroup;
		
		public FormatPanel() {
			setBorder(new TitledBorder("Output Format"));
			setLayout(new BorderLayout());
			JPanel inny = new JPanel();
			add(BorderLayout.NORTH, inny);
			inny.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			ButtonListener buttonListen = new ButtonListener();
			addComponent(inny, byteArray = new JCheckBox("Byte Array"), c);
			byteArray.addActionListener(buttonListen);
			c.gridy++;
			addComponent(inny, new JLabel("Used when communicating with remote PAMGuard installations", JLabel.LEFT), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(inny, new JLabel(" "), c);
			c.gridy++;
			addComponent(inny, jsonString = new JCheckBox("JSON-formatted String"), c);
			jsonString.addActionListener(buttonListen);
			c.gridy++;
			addComponent(inny, new JLabel("Used for a human-readable output", JLabel.LEFT), c);
			
			// add the buttons to the button group
			//buttonGroup = new ButtonGroup();
			//buttonGroup.add(byteArray);
			//buttonGroup.add(jsonString);
		}
			

		public void setParams() {
//			int sendFmt = networkSendParams.getSendingFormat();
//			
//			byteArray.setSelected((sendFmt & NetworkSendParams.NETWORKSEND_BYTEARRAY) != 0);
//
//			jsonString.setSelected((sendFmt & NetworkSendParams.NETWORKSEND_JSON) != 0);	
			
		}

		public boolean getParams() {
			int format = 0;
			if (byteArray.isSelected()) {
				format |= NetworkSendParams.NETWORKSEND_BYTEARRAY;
			}
			if (jsonString.isSelected()) {
				format |= NetworkSendParams.NETWORKSEND_JSON;
			}
//			networkSendParams.setSendingFormat(format);
	
			if (format == 0) {
				return showWarning("Must select one or both of data format options :)");
			}
			return true;
		}
		
		private class ButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox theButton = (JCheckBox) e.getSource();
				boolean jsonCheck = jsonString.isSelected();
				boolean byteArrayCheck = byteArray.isSelected();

				if(jsonCheck && !byteArrayCheck) {
					dataPanel.setParams(NetworkSendParams.NETWORKSEND_JSON);
				}else if(!jsonCheck && byteArrayCheck) {
					dataPanel.setParams(NetworkSendParams.NETWORKSEND_BYTEARRAY);
				}else if(jsonCheck && byteArrayCheck) {
					dataPanel.setParams(NetworkSendParams.NETWORKSEND_BOTH);
				}else {
					dataPanel.setParams(-1);

				}
				revalidate();
				repaint();
			}
			
			
		}
	}
}
