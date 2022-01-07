package clickDetector.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import PamView.dialog.PamDialog;
import clickDetector.ClickParameters;

/**
 * Dialog for controlling which clicks and click trains get displayed on the map.
 * @author Doug
 *
 */
@Deprecated // Can now be deleted. Replaced by new ClickTrainDataSelector class. 
public class ClickMapDialog extends PamDialog {

	static private ClickMapDialog singleInstance;
	
	private ClickParameters clickParameters;
	
	private JRadioButton showAll, showSome, showNone;
	private JTextField minTime, minTheta, lineLength;
	private JCheckBox individualBearings;

	private ClickMapDialog(Frame parentFrame) {

		super(parentFrame, "Click Lines on Map", true);
		
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		p.setLayout(new BorderLayout());
		
		JTextArea tx = new JTextArea("Click trains that change bearing and\n" + 
				                     "can give a range are always plotted.\n\n"+
				                     "Other click trains, that only have bearing\n" +
				                     "information can be plotted according to\n" + 
				                     "the following options...", 3, 3);
		tx.setFont(new Font("Arial", Font.PLAIN, 12));
		tx.setBackground(new JLabel().getBackground()); // steal default colour from a JLabel
		p.add(BorderLayout.NORTH, tx);
		
		p.add(BorderLayout.CENTER, new OptionsPanel());
		
		setDialogComponent(p);

		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_MapOptions");

	}

	
	private static ClickParameters showDialog(Frame parentFrame, ClickParameters clickParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickMapDialog(parentFrame);
		}
		singleInstance.clickParameters = clickParameters.clone();
		singleInstance.setParams(clickParameters);
		singleInstance.setVisible(true);
		return singleInstance.clickParameters;
	}

	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}
	
	void setParams(ClickParameters clickParameters){
		
		showAll.setSelected(clickParameters.showShortTrains == ClickParameters.LINES_SHOW_ALL);
		showSome.setSelected(clickParameters.showShortTrains == ClickParameters.LINES_SHOW_SOME);
		showNone.setSelected(clickParameters.showShortTrains == ClickParameters.LINES_SHOW_NONE);
		lineLength.setText(String.format("%1.0f", clickParameters.defaultRange));
		minTime.setText(String.format("%1.0f", clickParameters.minTimeSeparation));
		minTheta.setText(String.format("%1.0f", clickParameters.minBearingSeparation));
		individualBearings.setSelected(clickParameters.plotIndividualBearings);
		
		enableControls();
		
	}
	
	@Override
	public void restoreDefaultSettings() {

		setParams(new ClickParameters());
		
	}
	
	@Override
	public boolean getParams() {
		
		clickParameters.showShortTrains = getSelection();
		try {
			clickParameters.defaultRange = Double.valueOf(lineLength.getText());
			clickParameters.minTimeSeparation = Double.valueOf(minTime.getText());
			clickParameters.minBearingSeparation = Double.valueOf(minTheta.getText());
		}
		catch (Exception Ex) {
			return false;
		}
		clickParameters.plotIndividualBearings = individualBearings.isSelected();
		return true;
		
	}
	
	class OptionsPanel extends JPanel implements ActionListener
	{
		
		OptionsPanel() {
			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constraints = new GridBagConstraints();
			setLayout(layout);
			
			constraints.gridy = 0;
			constraints.gridx = 0;
			constraints.gridwidth = 3;
			constraints.anchor = GridBagConstraints.WEST;
			addComponent(this, showNone = new JRadioButton("Show only trains with fitted localisation"), constraints);
			constraints.gridy = 1;
			addComponent(this, showAll = new JRadioButton("Show all"), constraints);
			constraints.gridy = 2;
			addComponent(this, showSome = new JRadioButton("Show selection ..."), constraints);
			ButtonGroup g = new ButtonGroup();
			g.add(showNone);
			g.add(showAll);
			g.add(showSome);
			showNone.addActionListener(this);
			showAll.addActionListener(this);
			showSome.addActionListener(this);
			
			JLabel l;
			constraints.gridy = 4;
			constraints.gridx = 0;
			constraints.gridwidth = 1;
			addComponent(this, l = new JLabel("min time interval "), constraints);
			l.setHorizontalAlignment(SwingConstants.RIGHT);
			constraints.gridx = 1;
			addComponent(this, minTime = new JTextField(5), constraints);
			constraints.gridx = 2;
			addComponent(this, l = new JLabel(" s "), constraints);
			l.setHorizontalAlignment(SwingConstants.LEFT);

			constraints.gridy = 5;
			constraints.gridx = 0;
			constraints.gridwidth = 1;
			addComponent(this, new JLabel("min angle change "), constraints);
			constraints.gridx = 1;
			addComponent(this, minTheta = new JTextField(5), constraints);
			constraints.gridx = 2;
			addComponent(this, new JLabel(" degrees "), constraints);

			constraints.gridy = 6;
			constraints.gridx = 0;
			constraints.gridwidth = 1;
			addComponent(this, new JLabel("line length "), constraints);
			constraints.gridx = 1;
			addComponent(this, lineLength = new JTextField(5), constraints);
			constraints.gridx = 2;
			addComponent(this, new JLabel(" m "), constraints);
			
			constraints.gridx = 0;
			constraints.gridwidth = 3;
			constraints.gridy++;
			individualBearings = new JCheckBox("Plot individual click bearings");
			addComponent(this,  individualBearings, constraints);
						
		}
		
		void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
			((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
			panel.add(p);
		}
		
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	
	int getSelection() {
		if (showAll.isSelected()) return ClickParameters.LINES_SHOW_ALL;
		if (showSome.isSelected()){
//			showNone.setVisible(false);
//			invalidate();
			return ClickParameters.LINES_SHOW_SOME;
		}
		if (showNone.isSelected()) return ClickParameters.LINES_SHOW_NONE;
		return 0;
	}
	
	void enableControls() {
		int sel = getSelection();
		lineLength.setEnabled(sel != ClickParameters.LINES_SHOW_NONE);
		minTheta.setEnabled(sel == ClickParameters.LINES_SHOW_SOME);
		minTime.setEnabled(sel == ClickParameters.LINES_SHOW_SOME);
		
	}
	
}
