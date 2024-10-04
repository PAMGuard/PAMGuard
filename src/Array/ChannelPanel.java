package Array;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import Acquisition.AcquisitionControl;
import PamController.PamController;

public class ChannelPanel implements ActionListener, ListSelectionListener {

	private JComboBox daqControllerList;
	
	private JPanel channelPanel;
	
	private JTable channelTable;
	
	private JButton editButton;
	
	private AcquisitionControl acquisitionControl;
	
	private ChannelTableData channelTableData;
	
	private ArrayList<AcquisitionControl> daqControllers;
	
	private String[] channelColumns = {"Data Channel", "Hydrophone", "Gain", "Range", "Bandwidth"}; 
	
	private ArrayDialog arrayDialog;
	
	ChannelPanel(ArrayDialog arrayDialog) {
		
		this.arrayDialog  = arrayDialog;
		
		channelPanel = makePanel();
		
		setParams();
	}
	
	public JPanel getChannelPanel() {
		return channelPanel;
	}

	void newArraySelection() {
		channelTableData.fireTableDataChanged();	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == daqControllerList) {
			selectDaqController();
		}
		if (e.getSource() == editButton) {
			editChannelList();
		}
	}
	
	public void setParams() {
		daqControllers = AcquisitionControl.getControllers();
		daqControllerList.removeAllItems();
		for (int i = 0; i < daqControllers.size(); i++) {
			daqControllerList.addItem(daqControllers.get(i).toString());
		}
		if (daqControllerList.getItemCount() > 0) {
			daqControllerList.setSelectedIndex(0);
			selectDaqController();
		}
		setRecieverLabels();
		enableButtons();		
	}
	
	private void editChannelList() {
		int selRow = channelTable.getSelectedRow();
		if (selRow < 0) return;
		PamArray currentArray = arrayDialog.getHydrophoneDialogPanel().getDialogSelectedArray();
		Integer[] options = new Integer[currentArray.getHydrophoneArray().size()];
		for (int i = 0; i < options.length; i++) {
			options[i] = i;
		}
		int[] hydrophoneList = acquisitionControl.getHydrophoneList();
		if (hydrophoneList == null) {
			hydrophoneList = new int[acquisitionControl.acquisitionParameters.nChannels];
			for (int i = 0; i < hydrophoneList.length; i++) {
				hydrophoneList[i] = i;
			}
		}
		if (hydrophoneList.length < acquisitionControl.acquisitionParameters.nChannels) {
			int[] newList = new int[acquisitionControl.acquisitionParameters.nChannels];
			for (int i = 0; i < hydrophoneList.length; i++) {
				newList[i] = hydrophoneList[i];
			}
			hydrophoneList = newList;
		}
		/*
		 * showInputDialog(Component parentComponent, Object message, String title, int messageType, 
		 * Icon icon, Object[] selectionValues, Object initialSelectionValue) 
		 */
		Integer newChannel = (Integer) JOptionPane.showInputDialog(arrayDialog,
				"Select " + PamController.getInstance().getGlobalMediumManager().getRecieverString(false) + " for ADC channel " + selRow,
				new String("ADC Channel " + selRow), JOptionPane.OK_CANCEL_OPTION, null, options, new Integer(hydrophoneList[selRow]));
		
		
		if (newChannel != null) {
			hydrophoneList[selRow] = newChannel;
			acquisitionControl.setHydrophoneList(hydrophoneList);
			channelTableData.fireTableDataChanged();
			arrayDialog.newChannelSelection();
		}
	}
	
	void selectDaqController() {
		int index = daqControllerList.getSelectedIndex();
		if (index >= 0 && index < daqControllers.size()) {
			acquisitionControl = daqControllers.get(index);
		}
		else {
			acquisitionControl = null;
		}
		arrayDialog.newChannelSelection();
	}

	private JPanel makePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Channel Configuration"));
		panel.setLayout(new BorderLayout());
		
		channelTableData = new ChannelTableData();
		channelTable = new JTable(channelTableData);
		channelTable.setBorder(new EmptyBorder(10,10,10,10));
		channelTable.getSelectionModel().addListSelectionListener(this);
		channelTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		channelTable.getColumnModel().getColumn(4).setPreferredWidth(150);
		JScrollPane scrollPane = new JScrollPane(channelTable);
		scrollPane.setPreferredSize(new Dimension(290, 110));
		panel.add(BorderLayout.CENTER, scrollPane);
		
		panel.add(BorderLayout.NORTH, daqControllerList = new JComboBox());
		daqControllerList.addActionListener(this);
		
		JPanel s = new JPanel();
		s.setLayout(new FlowLayout(FlowLayout.LEFT));
		s.add(editButton = new JButton("Change hydrophone ..."));
		panel.add(BorderLayout.SOUTH, s);
		editButton.addActionListener(this);
		
		
		return panel;
	}
	void enableButtons() {
		// add is always selected. edit and delete are only enabled if a row is selected
		int selRow = channelTable.getSelectedRow();
		editButton.setEnabled(selRow >= 0);
	}

	class ChannelTableData extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return channelColumns.length;
		}

		@Override
		public int getRowCount() {
			if (acquisitionControl == null) return 0;
			return (acquisitionControl.acquisitionParameters.nChannels);
		}

		@Override
		public String getColumnName(int column) {
			return channelColumns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (acquisitionControl == null) return null;
			// see if the controller has a hydrophone list
			int[] hydrophoneList = acquisitionControl.getHydrophoneList();
			int channelList[] = acquisitionControl.acquisitionParameters.getHardwareChannelList();
//			AcquisitionParameters acquisitionParameters = acquisitionControl.acquisitionParameters;
			
			switch (columnIndex) {
			case 0:
				if (channelList != null && channelList.length > rowIndex) {
					return String.format("SW Ch %d / HW Ch %d", rowIndex, channelList[rowIndex]);
				}
				else {
					return String.format("Ch %d", rowIndex);
				}
			case 1:
				if (hydrophoneList != null && rowIndex < hydrophoneList.length) {
					if (hydrophoneList[rowIndex] < 
							arrayDialog.getHydrophoneDialogPanel().getDialogSelectedArray().getHydrophoneCount()) {
						return hydrophoneList[rowIndex];
					}
					else {
						return null;
					}
				}
				else {
					return rowIndex;
				}
			case 2:
				if (acquisitionControl.acquisitionParameters.preamplifier != null){
					return acquisitionControl.acquisitionParameters.preamplifier.getGain() + " dB";
				}
				return 0 + " dB";
			case 3:
				return String.format("%.1f Vp-p", acquisitionControl.acquisitionParameters.voltsPeak2Peak);
			case 4:
				if (acquisitionControl.acquisitionParameters.preamplifier != null){
					double[] bw = acquisitionControl.acquisitionParameters.preamplifier.getBandwidth();
					return String.format("%.1f-%.1f kHz", bw[0]/1000., bw[1]/1000.);
				}
				break;
			}
				
			return null;
		}
		
	}
	
	/**
	 * Set the correct receiver labels
	 */
	private void setRecieverLabels() {
		
		editButton.setText("Change " + PamController.getInstance().getGlobalMediumManager().getRecieverString(false)  + " ...");
		if (channelColumns!=null) {
			channelColumns[1] =  PamController.getInstance().getGlobalMediumManager() .getRecieverString(); 
		}
		
		if (channelTableData!=null) {
			channelTableData.fireTableStructureChanged();
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		enableButtons();		
	}

	public AcquisitionControl getAcquisitionControl() {
		return acquisitionControl;
	}
}
