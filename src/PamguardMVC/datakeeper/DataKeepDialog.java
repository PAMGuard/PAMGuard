package PamguardMVC.datakeeper;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.ChannelListScroller;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObserver;

public class DataKeepDialog extends PamDialog {

	private static DataKeepDialog singleInstance;
	private boolean okPressed;
	
	private JPanel listPanel;
	private JTextField[] listEdits;
	private ArrayList<PamDataBlock> dataBlockList;
	
	private DataKeepDialog(Window parentFrame) {
		super(parentFrame, "Minimum Data Storage Times", false);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Data Storage Times"));
		listPanel = new JPanel();
		listPanel.setLayout(new GridBagLayout());
		JPanel lnPanel = new JPanel(new BorderLayout());
		lnPanel.add(BorderLayout.NORTH, listPanel);
		mainPanel.add(BorderLayout.CENTER, new ChannelListScroller(lnPanel));				
		
		setDialogComponent(mainPanel);
		
	}

	public static boolean showDialog(Window parentWindow) {
		if (singleInstance == null || singleInstance.getOwner() != parentWindow) {
			singleInstance = new DataKeepDialog(parentWindow);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.okPressed;
	}
	
	private void setParams() {
		dataBlockList = PamController.getInstance().getDataBlocks();
		DataKeeper dataKeeper = DataKeeper.getInstance();
		listPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		if (dataBlockList == null) {
			listPanel.add(new JLabel("No Data Available", JLabel.CENTER));
			return;
		}
		listEdits = new JTextField[dataBlockList.size()];
		listPanel.add(new JLabel("Data Name", JLabel.CENTER));
		c.gridx++;
		listPanel.add(new JLabel("Keep Time", JLabel.CENTER));
		for (int i = 0; i < dataBlockList.size(); i++) {
			c.gridx = 0;
			c.gridy++;
			PamDataBlock dataBlock = dataBlockList.get(i);
			JLabel lab;
			listPanel.add(lab = new JLabel(dataBlock.getDataName() + " ", JLabel.RIGHT), c);
			lab.setToolTipText(dataBlock.getLongDataName());
			c.gridx++;
			listPanel.add(listEdits[i] = new JTextField(6), c);
			c.gridx++;
			listPanel.add(new JLabel(" seconds", JLabel.LEFT), c);
			int t = dataKeeper.getKeepTimeSeconds(dataBlock);
			if (t == 0) {
				t = Math.max(t, dataBlock.getNaturalLifetime());
			}
			listEdits[i].setText(String.format("%d", t));
			PamObserver longestObs = dataBlock.getLongestObserver();
			if (longestObs != null) {
				double to = (double) longestObs.getRequiredDataHistory(dataBlock, null) / 1000.;
				String s = String.format("Longest observer is \"%s\" requiring %3.1f seconds of data", 
						longestObs.getObserverName(),to);
				listEdits[i].setToolTipText(s);
			}
		}
		pack();
		invalidate();
		pack();
	}

	@Override
	public boolean getParams() {
		okPressed = true;
		DataKeeper dataKeeper = DataKeeper.getInstance();
		if (dataBlockList == null) return false;
		if (listEdits == null) return false;
		if (dataBlockList.size() != listEdits.length) {
			return showWarning("Terrible, terrible mismatch between data and lists in dialog");
		}
		for (int i = 0; i < dataBlockList.size(); i++) {
			PamDataBlock dataBlock = dataBlockList.get(i);
			int t = 0;
			try {
				t = Integer.valueOf(listEdits[i].getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Number Format Error", "Invalid number for datablock " + dataBlock.getDataName());
			}
			dataKeeper.setKeepTimeSeconds(dataBlock, t);
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		okPressed = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
