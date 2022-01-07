package clickDetector.ClickClassifiers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;

public class ClickClassifyDialog extends PamDialog {
	
	private JPanel mainPanel;
	
	private JPanel classifierSpecificContainer;

	private JComboBox typeList;
	
	private JCheckBox classifyOnline;
	private JCheckBox discardUnclassified;

	
	private ClickControl clickControl;
	
	private static ClickClassifyDialog singleInstance;
	
	private ClickParameters clickParameters;
	
	private ClickIdentifier currentIdentifier;
	
	private ClassifyDialogPanel classifierDialogPanel;
	
	private Frame parentFrame;
	
	private ClickClassifyDialog(ClickControl clickControl, Frame parentFrame) {
		super(parentFrame, "Click Classification", false);
		this.clickControl = clickControl;
		this.parentFrame = parentFrame;
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel topBit = new JPanel();
		topBit.setLayout(new GridLayout(3, 1));
		topBit.setBorder(new TitledBorder("Click Classifier Selection"));
		topBit.add(typeList = new JComboBox());
		topBit.add(classifyOnline = new JCheckBox("Run classification online"));
		topBit.add(discardUnclassified = new JCheckBox("Discard unclassified clicks"));
		mainPanel.add(BorderLayout.NORTH, topBit);
		
		classifierSpecificContainer = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, classifierSpecificContainer);
		
		
		for (int i = 0; i < clickControl.getClassifierManager().getNumClassifiers(); i++) {
			typeList.addItem(clickControl.getClassifierManager().getClassifierName(i));
		}
		typeList.addActionListener(new TypeAction());
		
		setDialogComponent(mainPanel);
		setResizable(true);

		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickClassification");
		
	}

	public static ClickParameters showDialog(ClickControl clickControl, Frame parentWindow,
			ClickParameters clickParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentWindow || 
				singleInstance.clickControl != clickControl) {
		singleInstance = new ClickClassifyDialog(clickControl, parentWindow);
		}
		singleInstance.clickParameters = clickParameters.clone();
		singleInstance.currentIdentifier = clickControl.getClickIdentifier();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.clickParameters;
	}
	
	private void setParams() {
		int ind = clickParameters.clickClassifierType;
		ind = Math.max(0,  Math.min(typeList.getItemCount()-1, ind));
		typeList.setSelectedIndex(ind);
		classifyOnline.setSelected(clickParameters.classifyOnline);
		discardUnclassified.setSelected(clickParameters.discardUnclassifiedClicks);
		setTypeSpecificPanel();
	}
	
	private ClassifyDialogPanel getClassifierDialogPanel() {
		if (currentIdentifier == null) {
			return null;
		}
		return currentIdentifier.getDialogPanel(parentFrame);
	}
	
	private void setTypeSpecificPanel() {
		classifierDialogPanel = getClassifierDialogPanel();
		classifierSpecificContainer.removeAll();
		if (classifierDialogPanel == null) {
			pack();
			return;
		}
		Component sc = classifierDialogPanel.getComponent();
		classifierDialogPanel.setParams();
		if (sc != null) {
			classifierSpecificContainer.add(BorderLayout.CENTER, sc);
		}
		classifierDialogPanel.setParams();
		classifierDialogPanel.setActive(true);
		pack();
	}
	
	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}

	@Override
	public boolean getParams() {
		clickParameters.clickClassifierType = typeList.getSelectedIndex();
		clickParameters.classifyOnline = classifyOnline.isSelected();
		clickParameters.discardUnclassifiedClicks = discardUnclassified.isSelected();
		ClassifyDialogPanel cdp;
		if (currentIdentifier != null && (cdp=currentIdentifier.getDialogPanel(parentFrame)) != null) {
			return cdp.getParams();
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private void changeClassifierType() {
		int type = typeList.getSelectedIndex();
		currentIdentifier = clickControl.getClassifierManager().getClassifier(type);
		setTypeSpecificPanel();
	}

	class TypeAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			changeClassifierType();			
		}		
	}
}
