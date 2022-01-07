package patchPanel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

public class PatchPanelDialog extends PamDialog {

	private PatchPanelParameters patchPanelParameters;
	
	private PatchPanelControl patchPanelControl;
	
	private static PatchPanelDialog singleInstance;
	
	private SourcePanel sourcePanel;
	
	private JCheckBox[][] patchBoxes = new JCheckBox[PamConstants.MAX_CHANNELS][PamConstants.MAX_CHANNELS];
	
	private JCheckBox immediate;
	
	private JLabel[] leftLabels = new JLabel[PamConstants.MAX_CHANNELS];
	private JLabel[] rightLabels = new JLabel[PamConstants.MAX_CHANNELS];
	
	private boolean constructionComplete = false;
	
	public PatchPanelDialog(Frame parentFrame) {
		super(parentFrame, "Patch Panel", true);
		// TODO Auto-generated constructor stub
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		sourcePanel = new SourcePanel(this, "Data Source", RawDataUnit.class, false, true);
		outerPanel.add(BorderLayout.NORTH, sourcePanel.getPanel());		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Channel Connections"));
		mainPanel.add(BorderLayout.WEST, new JLabel("Inputs  "));
		mainPanel.add(BorderLayout.NORTH, new JLabel("        Outputs"));
		
		JPanel boxPanel = new JPanel();
//		GridLayout gridLayout = new GridLayout(PamConstants.MAX_CHANNELS+2, PamConstants.MAX_CHANNELS+2);
//		gridLayout.setHgap(0);
//		gridLayout.setVgap(0);
		boxPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		// top row of labels
		for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
			c.gridx ++;
			addComponent(boxPanel, new JLabel(String.format("%d", out)), c);
			
		}
		PatchBoxListener patchBoxListener = new PatchBoxListener();
//		boxPanel.add(new JLabel(" "));
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			c.gridy++;
			c.gridx = 0;
//			boxPanel.add(leftLabels[in] = new JLabel(String.format("%d", in)));
			addComponent(boxPanel, leftLabels[in] = new JLabel(String.format("%d", in)), c);
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				c.gridx++;
//				boxPanel.add(patchBoxes[in][out] = new JCheckBox(""));
				addComponent(boxPanel, patchBoxes[in][out] = new JCheckBox(""), c);
				patchBoxes[in][out].addActionListener(patchBoxListener);
			}
			c.gridx++;
//			boxPanel.add(rightLabels[in] = new JLabel(String.format("%d", in)));
			addComponent(boxPanel, rightLabels[in] = new JLabel(String.format("%d", in)), c);
		}
		// bottom row of labels
//		boxPanel.add(new JLabel(" "));
		c.gridy++;
		c.gridx = 0;
		for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
			c.gridx++;
//			boxPanel.add(new JLabel(String.format("%d", out)));
			addComponent(boxPanel, new JLabel(String.format("%d", out)), c);
		}
		
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 30;
		c.anchor = GridBagConstraints.WEST;
		addComponent(boxPanel, immediate = new JCheckBox("Apply immediately"), c);
		mainPanel.add(BorderLayout.CENTER, boxPanel);
		
		outerPanel.add(BorderLayout.CENTER, mainPanel);
		setDialogComponent(outerPanel);
	}

	public static PatchPanelParameters showDialog(Frame parentFrame, 
			PatchPanelParameters patchPanelParameters, PatchPanelControl patchPanelControl) {
		
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new PatchPanelDialog(parentFrame);
		}
		singleInstance.patchPanelParameters = patchPanelParameters.clone();
		singleInstance.patchPanelControl = patchPanelControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.patchPanelParameters;
	}
	
	private void showrows() {
		int availableChannels = 3;
		PamDataBlock sourceData = sourcePanel.getSource();
		if (sourceData != null) {
			availableChannels = sourceData.getChannelMap();
		}
		boolean e;
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			e = ((1<<in & availableChannels) != 0);
			leftLabels[in].setVisible(e);
			rightLabels[in].setVisible(e);
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				patchBoxes[in][out].setVisible(e);
			}
		}
		invalidate();
		pack();
	}
	
	@Override
	public void cancelButtonPressed() {

		patchPanelParameters = null;
		
	}

	private void setParams() {

		constructionComplete = false;
		
		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(patchPanelControl.patchPanelProcess.getOutputDataBlock(0), true);
		PamRawDataBlock rawDataBlock = PamController.getInstance().getRawDataBlock(patchPanelParameters.dataSource);
		sourcePanel.setSource(rawDataBlock);
		
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				patchBoxes[in][out].setSelected(patchPanelParameters.patches[in][out] > 0);
			}
		}
		
		immediate.setSelected(patchPanelParameters.immediate);
		
		showrows();
		
		constructionComplete = true;
	}
	class PatchBoxListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			buttonsChanged();
			
		}
		
	}
	
	private void buttonsChanged() {
		
		if (immediate.isSelected() == false) return;
		
		if (getParams() == false) return;
		
		patchPanelControl.newSettings(patchPanelParameters);
		
	}
	
	@Override
	public boolean getParams() {
		
		PamDataBlock inputData = sourcePanel.getSource();
		ArrayList<PamDataBlock> rawDatas = PamController.getInstance().getRawDataBlocks();
		patchPanelParameters.dataSource = rawDatas.indexOf(inputData);
		
		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				if (patchBoxes[in][out].isSelected() && patchBoxes[in][out].isVisible()) {
					patchPanelParameters.patches[in][out] = 1;
				}
				else {
					patchPanelParameters.patches[in][out] = 0;
				}
			}
		}
		
		patchPanelParameters.immediate = immediate.isSelected();
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		for (int in = 0; in < PamConstants.MAX_CHANNELS; in++) {
			for (int out = 0; out < PamConstants.MAX_CHANNELS; out++) {
				if (in == out) {
					patchPanelParameters.patches[in][out] = 1;
				}
				else {
					patchPanelParameters.patches[in][out] = 0;
				}
			}
		}
		setParams();	
		buttonsChanged();
	}

}
