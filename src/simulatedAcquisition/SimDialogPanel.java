package simulatedAcquisition;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import propagation.PropagationModel;

public class SimDialogPanel {
	
	private static final long serialVersionUID = 1L;
	
	private SimParameters simParameters;

	private JTextField  noise;
	
	private SimTableData simTableData;
	
	private JTable simTable;
	
	private JButton addButton, removeButton, editButton, copyButton;
	
	private JComboBox propModels;
	
	private Window parentFrame;
	
	JPanel dialogPanel;

	private SimProcess simProcess;
	
	public SimDialogPanel(Window parentFrame, SimProcess simProcess) {
		this.parentFrame = parentFrame;
		this.simProcess = simProcess;

		dialogPanel = new JPanel(new BorderLayout());
		// sample rate box at top
		JPanel t = new JPanel();
		t.setLayout(new GridBagLayout());
		t.setBorder(new TitledBorder("Environment"));
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(t, new JLabel("Background Noise  "), c);
		c.gridx++;
		PamDialog.addComponent(t, noise = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(t, new JLabel("<html> dB re.1&mu;Pa/&radic;Hz</html>"), c);
		c.gridx = 0;
		c.gridy++;
		PamDialog.addComponent(t, new JLabel("Propagation Model  "), c);
		c.gridx++;
		c.gridwidth = 2;
		PamDialog.addComponent(t, propModels = new JComboBox(), c);
		for (int i = 0; i < simProcess.propagationModels.size(); i++) {
			propModels.addItem(simProcess.propagationModels.get(i));
		}
		
		dialogPanel.add(BorderLayout.NORTH, t);
		
		// objects table.
		t = new JPanel(new BorderLayout());
		t.setBorder(new TitledBorder("Simulated Objects"));
		simTableData = new SimTableData();
		simTable = new JTable(simTableData);
		JScrollPane sp = new JScrollPane(simTable);
		sp.setPreferredSize(new Dimension(500,100));
		t.add(BorderLayout.CENTER, sp);
		JPanel b = new JPanel();
		b.add(addButton = new JButton("Add ..."));
		b.add(copyButton = new JButton("Copy ..."));
		b.add(removeButton = new JButton("Remove"));
		b.add(editButton = new JButton("Edit ..."));
		addButton.addActionListener(new AddButton());
		copyButton.addActionListener(new CopyButton());
		removeButton.addActionListener(new RemoveButton());
		editButton.addActionListener(new EditButton());
		JPanel bOut = new JPanel(new BorderLayout());
		bOut.add(BorderLayout.EAST, b);
		t.add(BorderLayout.SOUTH, bOut);

		simTable.getSelectionModel().addListSelectionListener(new TableSelection());
		
		dialogPanel.add(BorderLayout.CENTER, t);
		
		simTable.addMouseListener(new SimTableMouse());
		
//		setDialogComponent(dialogPanel);
		
	}
	
//	static public SimParameters showDialog(Frame parentFrame, SimParameters simParameters) {
//		if (singleInstance == null || parentFrame != singleInstance.getOwner() ) {
//			singleInstance = new SimDialog(parentFrame);
//		}
//		singleInstance.simParameters = simParameters.clone();
//		singleInstance.setParams();
//		singleInstance.setVisible(true);
//		return singleInstance.simParameters;
//	}
	
	public void setParams(SimParameters simParameters) {
		this.simParameters = simParameters;
//		sampleRate.setText(String.format("%3.0f", simParameters.sampleRate));
		noise.setText(String.format("%3.0f", simParameters.backgroundNoise));
		propModels.setSelectedItem(simProcess.getPropagationModel());
		enableControls();
		simTableData.fireTableDataChanged();
	}
	
	public boolean getParams() {
		try {
//			simParameters.sampleRate = Float.valueOf(sampleRate.getText());
			simParameters.backgroundNoise = Double.valueOf(noise.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		PropagationModel pm = (PropagationModel) propModels.getSelectedItem();
		if (pm == null) {
			return false;
		}
		simProcess.setPropagationModel(pm);
		simParameters.propagationModel = pm.getName();
		
		return true;
	}
	
	private class SimTableMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				editAction();
			}
		}
		
	}

	private class TableSelection implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			tableSelectionChange();
		}
	}
	
	private class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addAction();
		}
	}
	private class CopyButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			copyAction();
		}
	}
	private class RemoveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			removeAction();
		}
	}
	private class EditButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			editAction();
		}
	}
	private void addAction() {
		
		SimObject simObject = new SimObject();
		/*
		 * Try to set on the ship or the master reference point (for static arrays)
		 */
		LatLong latLong = MasterReferencePoint.getLatLong();
		if (latLong != null) {
			simObject.startPosition.setLatitude(latLong.getLatitude());
			simObject.startPosition.setLongitude(latLong.getLongitude());
		}
		
		simObject = SimObjectDialog.showDialog(parentFrame, simProcess, simObject);
		if (simObject != null) {
			simParameters.addSimObject(simObject);
			simTableData.fireTableDataChanged();
		}
	}

	private void copyAction() { 
		int r = simTable.getSelectedRow();
		if (r < 0) {
			return;
		}		
		SimObject simObject = simParameters.getObject(r).clone();
		SimObject newObject = SimObjectDialog.showDialog(parentFrame, simProcess, simObject);
		if (newObject != null) {
			simParameters.addSimObject(simObject);
			simTableData.fireTableDataChanged();
		}
	}
		
	private void removeAction() {
		int r = simTable.getSelectedRow();
		if (r < 0) {
			return;
		}
		simParameters.removeObject(r);
		simTableData.fireTableDataChanged();
//		SpeciesItem sp = listeningParameters.speciesList.get(r);
//		String str = String.format("Are you sure you want to remove %s from the list ?" , sp);
//		int ans = JOptionPane.showConfirmDialog(getOwner(), str, "Remove Item", JOptionPane.OK_CANCEL_OPTION);
//		if (ans == JOptionPane.CANCEL_OPTION) {
//			return;
//		}
//		listeningParameters.speciesList.remove(r);
//		tableData.fireTableDataChanged();
	}

	private void editAction() {
		int r = simTable.getSelectedRow();
		if (r < 0) {
			return;
		}
		SimObject simObject = simParameters.getObject(r);
		SimObject newObject = SimObjectDialog.showDialog(parentFrame, simProcess, simObject);
		if (newObject != null) {
			simParameters.replaceSimObject(simObject, newObject);
			simTableData.fireTableDataChanged();
		}
	}
	
	private void tableSelectionChange() {
		enableControls();
	}
	
	private void enableControls() {
		int r = simTable.getSelectedRow();
		copyButton.setEnabled(r>=0);
		editButton.setEnabled(r>=0);
		removeButton.setEnabled(r>=0);
	}

	private class SimTableData extends AbstractTableModel {

		String[] cols = {"Name", "Lat", "Long", "COG", "SPD", "Depth", "Sound"};
		@Override
		public int getColumnCount() {
			return cols.length;
		}

		@Override
		public String getColumnName(int col) {
			return cols[col];
		}

		@Override
		public int getRowCount() {
			if (simParameters == null) {
				return 0;
			}
			return simParameters.getNumObjects();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (simParameters == null) {
				return null;
			}
			SimObject s = simParameters.getObject(row);
			switch(col) {
			case 0:
				return s.name;
			case 1:
				return s.startPosition.formatLatitude();
			case 2:
				return s.startPosition.formatLongitude();
			case 3:
				return s.course;
			case 4:
				return s.speed;
			case 5:
				return -s.getHeight();
			case 6:
				return simProcess.simSignals.findSignal(s.signalName);
			}
			return null;
		}
		
	}
}
