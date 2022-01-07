package noiseMonitor.alarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import noiseMonitor.NoiseDataBlock;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class NoiseAlarmDialog extends PamDialog {

	private static NoiseAlarmDialog singleInstance;
	private NoiseAlarmParameters noiseAlarmParameters;
	private NoiseDataBlock noiseDataBlock;
	
	private JCheckBox[] bandCheckBoxes;
	private JPanel bandPanel;
	private JRadioButton[] statsButtons = new JRadioButton[NoiseDataBlock.NNOISETYPES];
	
	private NoiseAlarmDialog(Window parentFrame, NoiseDataBlock noiseDataBlock) {
		super(parentFrame, noiseDataBlock.getDataName() + " Alarm", false);
		this.noiseDataBlock = noiseDataBlock;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel measurePanel = new JPanel();
		measurePanel.setBorder(new TitledBorder("Measure selection"));
		measurePanel.setLayout(new BoxLayout(measurePanel, BoxLayout.Y_AXIS));
		int usedStats = noiseDataBlock.getStatisticTypes();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
			statsButtons[i] = new JRadioButton(NoiseDataBlock.displayNames[i] + " Level");
			bg.add(statsButtons[i]);
			if ((1<<i & usedStats) != 0) {
				measurePanel.add(statsButtons[i]);
//				statsButtons[i].addActionListener(selectionChanged);
			}
			else {
				statsButtons[i].setEnabled(false);
			}
		}
		
		bandPanel = new JPanel();
		bandPanel.setLayout(new GridBagLayout());
		mainPanel.add(BorderLayout.NORTH, measurePanel);
		JScrollPane scrollPane = new JScrollPane(bandPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel bandOuter = new JPanel(new BorderLayout());
		bandOuter.setBorder(new TitledBorder("Bands"));
		bandOuter.setPreferredSize(new Dimension(200, 300));
		bandOuter.add(BorderLayout.CENTER, scrollPane);
		bandOuter.add(BorderLayout.NORTH, new JLabel("Multiple band energies will be added"));
		mainPanel.add(BorderLayout.CENTER, bandOuter);
		setDialogComponent(mainPanel);
	}

	public static NoiseAlarmParameters showDialog(Window parentFrame, NoiseAlarmParameters noiseAlarmParameters, NoiseDataBlock noiseDataBlock) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame || singleInstance.noiseDataBlock != noiseDataBlock) {
			singleInstance = new NoiseAlarmDialog(parentFrame, noiseDataBlock);
		}
		singleInstance.noiseAlarmParameters = noiseAlarmParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.noiseAlarmParameters;
	}
	
	private void setParams() {
		for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
			statsButtons[i].setSelected(i == noiseAlarmParameters.usedMeasure);
		}
		
		bandPanel.removeAll();
		int nBands = noiseDataBlock.getNumMeasurementBands();
		GridBagConstraints c = new PamGridBagContraints();
		if (nBands == 0) {
			addComponent(bandPanel, new JLabel("No defined bands", JLabel.CENTER), c);
			return;
		}
//		String[] bandNames = noiseDataBlock.getBandLongNames();
		bandCheckBoxes = new JCheckBox[nBands];
		for (int i = 0; i < nBands; i++) {
			bandCheckBoxes[i] = new JCheckBox(noiseDataBlock.getBandLongName(i));
			c.gridx = 0;
			c.gridy++;
			addComponent(bandPanel, bandCheckBoxes[i], c);
		}
		boolean[] selBands = noiseAlarmParameters.selectedBands;
		if (selBands == null) {
			selBands = new boolean[nBands];
		}
		int n = Math.min(selBands.length, bandCheckBoxes.length);
		for (int i = 0; i < n; i++) {
			bandCheckBoxes[i].setSelected(selBands[i]);
		}
		pack();
	}
	
	@Override
	public boolean getParams() {
		for (int i = 0; i < NoiseDataBlock.NNOISETYPES; i++) {
			if (statsButtons[i].isSelected()) {
				noiseAlarmParameters.usedMeasure = i;
			}
		}
		
		
		int nBands = noiseDataBlock.getNumMeasurementBands();
		int nSel = 0;
		if (nBands == 0) {
			return showWarning("No bands to select");
		}
		boolean[] selBands = new boolean[nBands];
		for (int i = 0; i < nBands; i++) {
			selBands[i] = bandCheckBoxes[i].isSelected();
			if (selBands[i]) {
				nSel++;
			}
		}
		noiseAlarmParameters.selectedBands = selBands;
		
		return nSel > 0;
	}

	@Override
	public void cancelButtonPressed() {
		noiseAlarmParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
