package noiseMonitor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import PamUtils.FrequencyFormat;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.dialog.SourcePanelMonitor;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

public class NoiseDialog extends PamDialog {

	static private NoiseDialog singleInstance;

	private NoiseSettings noiseSettings;

	private SourcePanel sourcePanel;


	private String[] colNames = {"Name", "F1 (Hz)", "Centre (Hz)", "F2 (Hz)"};

	private NoiseTableData noiseTableData;

	private JTable noiseTable;

	private JTextField interval;

	private JTextField nMeasures;

	private JButton removeButton, editButton;
	private JCheckBox thirdsButton;
	private JCheckBox decidecButton;
	private JButton othersButton;
	private JCheckBox useAllButton;

	private ResolutionPanel resolutionPanel = new ResolutionPanel();

	private NoiseControl noiseControl;

	private  NoiseDialog(Window parentFrame) {
		super(parentFrame, "Noise Monitoring", false);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel s = new JPanel(new BorderLayout());
		sourcePanel = new SourcePanel(this, "FFT Data Source", FFTDataUnit.class, true, true);
		sourcePanel.addSourcePanelMonitor(new SourceMonitor());
		s.add(BorderLayout.NORTH, sourcePanel.getPanel());
		JPanel resE = new JPanel(new BorderLayout());
		resE.setBorder(new TitledBorder("FFT Resolution"));
		resE.add(BorderLayout.WEST, resolutionPanel.getPanel());
		s.add(BorderLayout.CENTER, resE);

		mainPanel.add(BorderLayout.NORTH, s);

		JPanel mPanel = new JPanel(new BorderLayout());
		mPanel.setBorder(new TitledBorder("Measurements"));

		noiseTableData = new NoiseTableData();
		noiseTable = new JTable(noiseTableData);
		noiseTable.getSelectionModel().addListSelectionListener(new TableSelection());
		JScrollPane scrollPane = new JScrollPane(noiseTable);
		mPanel.add(BorderLayout.WEST, scrollPane);

		JPanel tPanel = new JPanel(new GridBagLayout());
		JPanel tPanelW = new JPanel(new BorderLayout());
		GridBagConstraints c;
		c = new PamGridBagContraints();
		addComponent(tPanel, new JLabel("Interval between measurements "), c);
		c.gridx++;
		addComponent(tPanel, interval = new JTextField(5), c);
		interval.getDocument().addDocumentListener(new IntervalChanged());
		c.gridx++;
		addComponent(tPanel, new JLabel(" s"), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(tPanel, new JLabel("Number of measures in interval "), c);
		c.gridx++;
		addComponent(tPanel, nMeasures = new JTextField(5), c);
		c.gridx++;
		addComponent(tPanel, useAllButton = new JCheckBox("Use all FFT data"), c);
		useAllButton.addActionListener(new UseAll());
		tPanelW.add(BorderLayout.WEST, tPanel);
		mPanel.add(BorderLayout.NORTH, tPanelW);

		JPanel bPanelN = new JPanel(new BorderLayout());
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new GridLayout(5,1));
		bPanel.add(thirdsButton = new JCheckBox("Add 1/3 Octaves"));
		bPanel.add(decidecButton = new JCheckBox("Add 1/10 Decades"));
		bPanel.add(othersButton = new JButton("Add Other bands"));
		bPanel.add(removeButton = new JButton("Remove"));
		bPanel.add(editButton = new JButton("Edit ..."));
		bPanelN.add(BorderLayout.NORTH, bPanel);
		mPanel.add(BorderLayout.EAST, bPanelN);	
		
		thirdsButton.setToolTipText("Standard 1/3 octave bands referenced at 1kHz");
		decidecButton.setToolTipText("Standard 1/10 decade bands referenced at 1kHz");
//		ButtonGroup bg = new ButtonGroup();
//		bg.add(thirdsButton);
//		bg.add(decidecButton);

		thirdsButton.addActionListener(new ThirdOctaves());
		decidecButton.addActionListener(new DeciDecades());
		othersButton.addActionListener(new AddButton());
		removeButton.addActionListener(new RemoveButton());
		editButton.addActionListener(new EditButton());


		mainPanel.add(BorderLayout.CENTER, mPanel);


		setDialogComponent(mainPanel);
	}

	static public NoiseSettings showDialog(NoiseControl noiseControl, Frame parentFrame, NoiseSettings noiseSettings) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new NoiseDialog(parentFrame);
//		}
		singleInstance.noiseControl = noiseControl;
		singleInstance.noiseSettings = noiseSettings.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.noiseSettings;
	}

	private void setParams() {
		thirdsButton.setSelected(haveThirdOctaves());
		sourcePanel.setSource(noiseSettings.dataSource);	
		sourcePanel.setChannelList(noiseSettings.channelBitmap);
		sourceSelectionChanged();
		interval.setText(String.format("%d", noiseSettings.measurementIntervalSeconds));
		nMeasures.setText(String.format("%d", noiseSettings.nMeasures));
		useAllButton.setSelected(noiseSettings.useAll);
		resolutionPanel.setParams(findSourceData());
		enableControls();
	}


	private void sourceSelectionChanged() {
		FFTDataBlock sourceData = (FFTDataBlock) sourcePanel.getSource();
		resolutionPanel.setParams(sourceData);
		enableControls();
	}

	@Override
	public void cancelButtonPressed() {
		noiseSettings = null;
	}

	@Override
	public boolean getParams() {
		FFTDataBlock sourceData = (FFTDataBlock) sourcePanel.getSource();
		if (sourceData == null) {
			return false;
		}
		noiseSettings.dataSource = sourceData.getDataName();
		noiseSettings.channelBitmap = sourcePanel.getChannelList();
		if (noiseSettings.channelBitmap == 0) {
			return false;
		}
		try {
			noiseSettings.measurementIntervalSeconds = Integer.valueOf(interval.getText());
			noiseSettings.nMeasures = Integer.valueOf(nMeasures.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		noiseSettings.useAll = useAllButton.isSelected();
		return true;
	}

	private void useAll() {
		enableControls();
	}

	private void thirdOctaves() {
		if (thirdsButton.isSelected()) {
			decidecButton.setSelected(false);
			removeDeciDecadeBands();
			addThirdOctaveBands();
		}
		else {
			removeThirdOctaveBands();
		}
		noiseTableData.fireTableDataChanged();
	}
	
	private void deciDecades() {
		if (decidecButton.isSelected()) {
			thirdsButton.setSelected(false);
			removeThirdOctaveBands();
			addDeciDecadeBands();
		}
		else {
			removeDeciDecadeBands();
		}
		noiseTableData.fireTableDataChanged();
	}


	private void removeDeciDecadeBands() {
		ListIterator<NoiseMeasurementBand> li = noiseSettings.getBandIterator();
		NoiseMeasurementBand nmb;
		while (li.hasNext()) {
			nmb = li.next();
			if (nmb.getType() == NoiseMeasurementBand.TYPE_DECIDECADE) {
				li.remove();
			}
		}
		
	}
	
	private void removeThirdOctaveBands() {
		ListIterator<NoiseMeasurementBand> li = noiseSettings.getBandIterator();
		NoiseMeasurementBand nmb;
		while (li.hasNext()) {
			nmb = li.next();
			if (nmb.getType() == NoiseMeasurementBand.TYPE_THIRDOCTAVE) {
				li.remove();
			}
		}
	}

	private void addDeciDecadeBands() {
		if (haveDeciDecades()) {
			removeDeciDecadeBands();
		}

		FFTDataBlock source = findSourceData();
		if (source == null) {
			return;
		}
		double fRes = source.getSampleRate() / source.getFftLength();
		double[][] edges = noiseControl.createDeciDecateBands(fRes*8, source.getSampleRate()/2);
		NoiseMeasurementBand nmb;
		for (int i = 0; i < edges.length; i++) {
			nmb = new NoiseMeasurementBand(NoiseMeasurementBand.TYPE_DECIDECADE, "DeciDecade", 
					edges[i][0], edges[i][1]);
			noiseSettings.addNoiseMeasurementBand(nmb);
		}
	}

	private void addThirdOctaveBands() {
		if (haveThirdOctaves()) {
			removeThirdOctaveBands();
		}
		FFTDataBlock source = findSourceData();
		if (source == null) {
			return;
		}
		double fRes = source.getSampleRate() / source.getFftLength();
//		double[][] edges = noiseControl.createThirdOctaveBands(fRes*8, source.getSampleRate()/2);
		double[][] edges = noiseControl.createANSIThirdOctaveBands(fRes*8, source.getSampleRate()/2);
		NoiseMeasurementBand nmb;
		for (int i = 0; i < edges.length; i++) {
			nmb = new NoiseMeasurementBand(NoiseMeasurementBand.TYPE_THIRDOCTAVE, "ThirdOctave", 
					edges[i][0], edges[i][1]);
			noiseSettings.addNoiseMeasurementBand(nmb);
		}
	}
	
	/**
	 * Do we have ANY bands that are 1/10 decade ? 
	 * @return
	 */
	private boolean haveDeciDecades() {
		int n = noiseSettings.getNumMeasurementBands();
		for (int i = 0; i < n; i++) {
			if (noiseSettings.getMeasurementBand(i).getType() == NoiseMeasurementBand.TYPE_DECIDECADE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Have any bands that are 1/3 octave ? 
	 * @return
	 */
	private boolean haveThirdOctaves() {
		int n = noiseSettings.getNumMeasurementBands();
		for (int i = 0; i < n; i++) {
			if (noiseSettings.getMeasurementBand(i).getType() == NoiseMeasurementBand.TYPE_THIRDOCTAVE) {
				return true;
			}
		}
		return false;
	}

	private void addButton() {

		NoiseMeasurementBand newBand = UserBandDialog.showDialog(getOwner(), null, findSourceData());
		if (newBand == null) {
			return;
		}
		noiseSettings.addNoiseMeasurementBand(newBand);
		noiseTableData.fireTableDataChanged();

	}
	private void removeButton() {
		int selRow = noiseTable.getSelectedRow();
		if (selRow < 0) {
			return;
		}
		noiseSettings.removeMeasurementBand(selRow);
		noiseTableData.fireTableDataChanged();
	}
	private void editButton() {
		int selRow = noiseTable.getSelectedRow();
		if (selRow < 0) {
			return;
		}
		NoiseMeasurementBand newBand = UserBandDialog.showDialog(getOwner(), 
				noiseSettings.getMeasurementBand(selRow), findSourceData());
		if (newBand == null) {
			return;
		}
		noiseSettings.removeMeasurementBand(selRow);
		noiseSettings.addNoiseMeasurementBand(selRow, newBand);
		noiseTableData.fireTableDataChanged();
	}

	private FFTDataBlock findSourceData() {
		PamDataBlock sourceData = sourcePanel.getSource();
		if (sourceData == null) {
			return null;
		}
		return (FFTDataBlock) sourceData;
	}

	private void tableSelectionChanged() {
		enableControls();
	}

	private void enableControls() {
		nMeasures.setEnabled(!useAllButton.isSelected());
		if (useAllButton.isSelected()) {
			autoNMeasures();
		}
		boolean hasSource = (findSourceData() != null);
		int selRow = noiseTable.getSelectedRow();
		othersButton.setEnabled(hasSource);
		editButton.setEnabled(hasSource & selRow >= 0);
		removeButton.setEnabled(selRow >= 0);
	}

	/**
	 * automatically set the number of measures when 
	 * the use all button is checked. 
	 */
	private void autoNMeasures() {
		FFTDataBlock sourceData = (FFTDataBlock) sourcePanel.getSource();
		if (interval == null || nMeasures == null || useAllButton == null) {
			return;
		}
		if (!useAllButton.isSelected()) {
			return;
		}
		if (sourceData == null) {
			return;
		}
		int nSeconds;
		try {
			nSeconds = Integer.valueOf(interval.getText());
		}
		catch (NumberFormatException e) {
			return;
		}
		if (nSeconds <= 0) {
			return;
		}
		
		int autoN = (int) (nSeconds * sourceData.getSampleRate() / sourceData.getFftHop());
		nMeasures.setText(String.format("%d", autoN));
	}

	class IntervalChanged implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent arg0) {
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			autoNMeasures();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			autoNMeasures();
		}

	}
	class UseAll implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			useAll();
		}
	}
	class ThirdOctaves implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			thirdOctaves();
		}
	}
	class DeciDecades implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			deciDecades();
		}
	}
	class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			addButton();
		}
	}
	class RemoveButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			removeButton();
		}
	}
	class EditButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			editButton();
		}
	}

	class SourceMonitor implements SourcePanelMonitor {
		@Override
		public void channelSelectionChanged() {
			sourceSelectionChanged();
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	class NoiseTableData extends AbstractTableModel {

		@Override
		public String getColumnName(int iCol) {
			return colNames[iCol];
		}

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return colNames.length;
		}

		@Override
		public int getRowCount() {
			if (noiseSettings == null) {
				return 0;
			}
			return noiseSettings.getNumMeasurementBands();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (noiseSettings == null) {
				return null;
			}
			NoiseMeasurementBand nmb = noiseSettings.getMeasurementBand(iRow);
			if (nmb == null) {
				return null;
			}
			switch(iCol) {
			case 0:
				return nmb.name;
			case 1:
				return FrequencyFormat.formatFrequency(nmb.f1, true);
			case 2:
				return FrequencyFormat.formatFrequency(Math.sqrt(nmb.f1*nmb.f2), true);
			case 3:
				return FrequencyFormat.formatFrequency(nmb.f2, true);
			}
			return null;
		}

	}

	public class TableSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			tableSelectionChanged();
		}

	}

}
