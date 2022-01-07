package networkTransfer.emulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import networkTransfer.send.NetworkObjectPacker;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

public class EmulateDialog extends PamDialog {

	private NetworkEmulator networkEmulator;
	private StatusPanel statusPanel;
	private static EmulateDialog singleInstance;
	private EmulatorParams emulatorParams;
	private JSpinner nBuoys;
	private SpinnerNumberModel buoyNumberModel;
	private JLabel latLong;
	private JTextField circleRadius;
	private ArrayList<PamDataBlock> usedDataBlocks;
	private ArrayList<JCheckBox> dataCheckBoxes;
	private JButton startButton;
	private JButton stopButton;
	private Timer tableTimer;
	private NetworkObjectPacker networkObjectPacker = new NetworkObjectPacker();
	private JTextField statusInterval;
	
	
	public EmulateDialog(NetworkEmulator networkEmulator, Window parentFrame) {
		super(parentFrame, networkEmulator.getNetworkSender().getUnitName() + " Buoy Emulator", false);
		this.networkEmulator = networkEmulator;
		this.emulatorParams = networkEmulator.emulatorParams;
		
		JPanel mainPanel, leftCtrlPanel, rightCtrlPanel, northPanel;
		mainPanel = new JPanel(new BorderLayout());
		northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
		leftCtrlPanel = new JPanel();
		northPanel.add(leftCtrlPanel);
		leftCtrlPanel.setBorder(new TitledBorder("Buoys"));
		leftCtrlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(leftCtrlPanel, new JLabel("Number of buoys ", JLabel.RIGHT), c);
		c.gridx++;
		buoyNumberModel = new SpinnerNumberModel(emulatorParams.nBuoys, 1, PamConstants.MAX_CHANNELS, 1);
		addComponent(leftCtrlPanel, nBuoys = new JSpinner(buoyNumberModel), c);
		nBuoys.addChangeListener(new NBuoyListener());
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		JButton latLongButton = new JButton(" Centre ");
		addComponent(leftCtrlPanel, latLongButton, c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(leftCtrlPanel, latLong = new JLabel("                           "), c);
		c.gridx = 0;
		c.gridy ++;
		c.gridwidth = 1;
		addComponent(leftCtrlPanel, new JLabel("Circle Radius ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(leftCtrlPanel, circleRadius = new JTextField(5), c);
		c.gridx++;
		addComponent(leftCtrlPanel, new JLabel(" m ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy ++;
		c.gridwidth = 1;
		addComponent(leftCtrlPanel, new JLabel("Status / GPS interval ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(leftCtrlPanel, statusInterval = new JTextField(5), c);
		c.gridx++;
		addComponent(leftCtrlPanel, new JLabel(" s ", JLabel.LEFT), c);
		
		rightCtrlPanel = new JPanel();
		northPanel.add(rightCtrlPanel);
		rightCtrlPanel.setLayout(new GridBagLayout());
		rightCtrlPanel.setBorder(new TitledBorder("Data Streams"));
		c = new PamGridBagContraints();
		// only do data streams which have binary storage. 
		ArrayList<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
		usedDataBlocks = new ArrayList<PamDataBlock>();
		dataCheckBoxes = new ArrayList<JCheckBox>();
		for (PamDataBlock aBlock:dataBlocks) {
			if (aBlock.getBinaryDataSource() != null) {
				usedDataBlocks.add(aBlock);
				JCheckBox cb = new JCheckBox(aBlock.getDataName());
				dataCheckBoxes.add(cb);
				cb.addActionListener(new DataBoxCheck());
				addComponent(rightCtrlPanel, cb, c);
				c.gridy++;
			}
		}
		
		statusPanel = new StatusPanel();
		
		mainPanel.add(BorderLayout.NORTH, northPanel);
//		northPanel.setBorder(new TitledBorder("Control"));
		mainPanel.add(BorderLayout.CENTER, statusPanel);
		setDialogComponent(mainPanel);
		setModal(true);
		setResizable(true);
		
		getOkButton().setVisible(false);
		getCancelButton().setText("Close");
		getButtonPanel().add(startButton = new JButton("Start"), 0);
		getButtonPanel().add(stopButton = new JButton("Stop"), 1);
		startButton.addActionListener(new StartButton());
		stopButton.addActionListener(new StopButton());
		tableTimer = new Timer(1000, new TableTimer());
		enableControls();
	}
	
	private void enableControls() {
		boolean s = networkEmulator.isStarted();
		nBuoys.setEnabled(s == false);
		circleRadius.setEnabled(s == false);
		statusInterval.setEnabled(s == false);
		for (JCheckBox c:dataCheckBoxes) {
			c.setEnabled(s == false);
		}
		stopButton.setEnabled(s);
		startButton.setEnabled(s == false && countEnabledBlocks() > 0);
		getCancelButton().setEnabled(s == false);
	}
	
	private int countEnabledBlocks() {
		int n = 0;
		if (dataCheckBoxes == null || dataCheckBoxes.size() == 0) {
			return 0;
		}
		for (int i = 0; i < dataCheckBoxes.size(); i++) {
			if (dataCheckBoxes.get(i).isSelected()) {
				n++;
			}
		}
		
		return n;
	}
	
	private class DataBoxCheck implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}
	
	private class NBuoyListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			getParams();
			networkEmulator.prepareEmulator();
			statusPanel.updateTable();
		}
		
	}
	
	private class StartButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			start();
		}
	}
	
	private class StopButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			stop();
		}
	}

	
	private class TableTimer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			statusPanel.updateTable();
		}
	}
	
	public static void showDialog(NetworkEmulator networkEmulator, Window parentFrame){
		if (singleInstance == null || singleInstance.networkEmulator != networkEmulator || singleInstance.getOwner() != parentFrame) {
			singleInstance = new EmulateDialog(networkEmulator, parentFrame);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
	}
	
	public void start() {
		if (getParams() == false) {
			showWarning("Unable to start data buoy emulation");
			return;
		}
		networkEmulator.start();
		tableTimer.start();
		enableControls();
	}

	public void stop() {
		networkEmulator.stop();
		tableTimer.stop();
		enableControls();
		statusPanel.updateTable();
	}

	void setParams() {
		buoyNumberModel.setValue(emulatorParams.nBuoys);
		latLong.setText(emulatorParams.gpsCentre.toString());
		circleRadius.setText(new Double(emulatorParams.circleRadius).toString());
		statusInterval.setText(new Integer(emulatorParams.statusIntervalSeconds).toString());
		if (emulatorParams.usedBlocks == null) {
			emulatorParams.usedBlocks = new boolean[dataCheckBoxes.size()];
		}
		else {		
			emulatorParams.usedBlocks = Arrays.copyOf(emulatorParams.usedBlocks, dataCheckBoxes.size());
		}
		for (int i = 0; i < dataCheckBoxes.size(); i++) {
			dataCheckBoxes.get(i).setSelected(emulatorParams.usedBlocks[i]);
		}
		enableControls();
	}

	@Override
	public  void cancelButtonPressed() {
		getParams();
	}

	@Override
	public boolean getParams() {
		emulatorParams.nBuoys = (Integer) buoyNumberModel.getValue();
		try {
			emulatorParams.circleRadius = Double.valueOf(circleRadius.getText());
			emulatorParams.statusIntervalSeconds = Integer.valueOf(statusInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Number format error in configuration");
		}
		for (int i = 0; i < dataCheckBoxes.size(); i++) {
			emulatorParams.usedBlocks[i] = dataCheckBoxes.get(i).isSelected();
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class StatusPanel extends JPanel {

		private StatusTableModel statusTableModel;

		public StatusPanel() {
			super();
			setBorder(new TitledBorder("Status"));
			setLayout(new BorderLayout());
			JPanel northPanel = new JPanel();
			add(BorderLayout.NORTH, northPanel);
			
			statusTableModel = new StatusTableModel();
			JTable statusTable = new JTable(statusTableModel);
			JScrollPane scrollPane = new JScrollPane(statusTable);
			add(BorderLayout.CENTER, scrollPane);
			
			setPreferredSize(new Dimension(270, 200));
		}
		
		private void updateTable() {
			statusTableModel.fireTableDataChanged();
		}
		
		
	}
	
	private class StatusTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		String[] colNames = {"BuoyId", "Position", "Data Time", "Total Units", "Bytes", "Status"};
		
		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			return emulatorParams.nBuoys;
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			EmBuoyStatus ebs = networkEmulator.getStreamStatus(iRow);
			if (ebs == null) {
				return null;
			}
			switch (iCol) {
			case 0:
				return ebs.getBuoyId();
			case 1:
				return ebs.getLatLong().toString();
			case 2:
				if (ebs.currentDataTime == 0) return "-";
				return PamCalendar.formatDateTime(ebs.currentDataTime);
			case 3:
				return ebs.unitsSent;
			case 4:
				return ebs.totalBytes;
			case 5:
				if (ebs.socketStatus == false) {
					return "Closed";
				}
				return String.format("Ports %d/%d", ebs.localPort, ebs.remotePort);
			}
			return null;
		}

		@Override
		public String getColumnName(int iCol) {
			return colNames[iCol];
		}
		
		
		
	}
}
