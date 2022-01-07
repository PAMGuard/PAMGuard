package clickDetector.dataSelector;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import clickDetector.ClickParameters;

public class ClickTrainSelectPanel implements PamDialogPanel {

	private ClickTrainDataSelector clickTrainDataSelector;

	private JPanel mainPanel;

	private JCheckBox manualEvents, automaticEvents;
	private JRadioButton showAll, showSome, showNone;
	private JTextField minTime, minTheta;
	private JTextField lineLength;
//	private JCheckBox individualBearings;

	public ClickTrainSelectPanel(ClickTrainDataSelector clickTrainDataSelector) {
		this.clickTrainDataSelector = clickTrainDataSelector;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel typePanel = new JPanel();
		typePanel.setBorder(new TitledBorder("Event Types"));
		typePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		typePanel.add(manualEvents = new JCheckBox("Show events marked manually"), c);
		c.gridy++;
		typePanel.add(automaticEvents = new JCheckBox("Show events marked automatically"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		typePanel.add(new JLabel("Default line length ", JLabel.RIGHT), c);
		c.gridx = 1;
		typePanel.add(lineLength = new JTextField(5), c);
		c.gridx = 2;
		typePanel.add(new JLabel(" m "), c);
		mainPanel.add(typePanel);

		JPanel selPanel = new JPanel(new GridBagLayout());
		selPanel.setBorder(new TitledBorder("Detailed Selection"));
		c = new PamGridBagContraints();
		c.gridwidth = 3;
		JTextArea tx = new JTextArea("Click trains that change bearing and " + 
				"can give a range \nare always plotted.\n"+
				"Other click trains, that only have bearing " +
				"information\ncan be plotted according to " + 
				"the following options...", 3, 3);
		tx.setFont(new Font("Arial", Font.PLAIN, 12));
		tx.setBackground(new JLabel().getBackground()); // steal default colour from a JLabel
		selPanel.add(tx,c);
		c.gridy++;
		selPanel.add(showNone = new JRadioButton("Show only trains with fitted localisation"), c);
		c.gridy++;
		selPanel.add(showAll = new JRadioButton("Show all"), c);
		c.gridy++;
		selPanel.add(showSome = new JRadioButton("Show selection ..."), c);
		ButtonGroup g = new ButtonGroup();
		g.add(showNone);
		g.add(showAll);
		g.add(showSome);

		SelectionListener s = new SelectionListener();
		showNone.addActionListener(s);
		showAll.addActionListener(s);
		showSome.addActionListener(s);

		c.gridy++;
		c.gridwidth = 1;
		selPanel.add(new JLabel("Min time interval between click trains ", JLabel.RIGHT), c);
		c.gridx = 1;
		selPanel.add( minTime = new JTextField(5), c);
		c.gridx = 2;
		selPanel.add(new JLabel(" s "), c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		selPanel.add(new JLabel("Min angle change between click trains ", JLabel.RIGHT), c);
		c.gridx = 1;
		selPanel.add(minTheta = new JTextField(5), c);
		c.gridx = 2;
		selPanel.add(new JLabel("\u00B0"), c);

		
//		c.gridx = 0;
//		c.gridwidth = 3;
//		c.gridy++;
//		individualBearings = new JCheckBox("Plot individual click bearings");
//		addComponent(this,  individualBearings, constraints);

		mainPanel.add(selPanel);

	}
	
	private class SelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

	@Override
	public JComponent getDialogComponent() {
		// TODO Auto-generated method stub
		return mainPanel;
	}

	public void enableControls() {
		minTime.setEnabled(showSome.isSelected());
		minTheta.setEnabled(showSome.isSelected());
	}

	@Override
	public void setParams() {
		
		ClickTrainSelectParameters params = clickTrainDataSelector.getCtSelectParams();
		manualEvents.setSelected(params.showManualTrains);
		automaticEvents.setSelected(params.showAutoTrains);
		showAll.setSelected(params.showShortTrains == ClickParameters.LINES_SHOW_ALL);
		showNone.setSelected(params.showShortTrains == ClickParameters.LINES_SHOW_NONE);
		showSome.setSelected(params.showShortTrains == ClickParameters.LINES_SHOW_SOME);
		minTheta.setText(String.format(String.format("%3.1f", params.minBearingSeparation)));
		minTime.setText(String.format(String.format("%3.1f", params.minTimeSeparation)));
		
		lineLength.setText(String.format("%3.1f", params.defaultRange));
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		ClickTrainSelectParameters params = clickTrainDataSelector.getCtSelectParams().clone();
		params.showManualTrains = manualEvents.isSelected();
		params.showAutoTrains = automaticEvents.isSelected();
		if (showAll.isSelected()) {
			params.showShortTrains = ClickParameters.LINES_SHOW_ALL;
		}
		else if (showSome.isSelected()) {
			params.showShortTrains = ClickParameters.LINES_SHOW_SOME;
		}
		else {
			params.showShortTrains = ClickParameters.LINES_SHOW_NONE;
		}
		try {
			params.minBearingSeparation = Double.valueOf(minTheta.getText());
			params.minTimeSeparation = Double.valueOf(minTime.getText());
			params.defaultRange = Double.valueOf(lineLength.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "Selection Error", "Invalid parameter in dialog");
		}
		
		clickTrainDataSelector.setCtSelectParams(params);
		return true;
	}

}
