package AIS;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class AISDisplayDialog extends PamDialog implements ActionListener {

	private static AISDisplayDialog aisDisplayDialog;
	private AISParameters aisParameters;

	private JCheckBox showTail;
	private JTextField tailLength;
	private JCheckBox predictionArrow;
	private JTextField predictionTime;
	
	private AISDisplayDialog(Frame parentFrame) {
		super(parentFrame, "AIS Display", true);
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Options"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 3;
		addComponent(p, showTail = new JCheckBox("Show vessel track"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(p, new JLabel("Tail Length "), c);
		c.gridx++;
		addComponent(p, tailLength = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" minutes"), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		addComponent(p, predictionArrow = new JCheckBox("Show prediction arrow"), c);
		c.gridwidth = 1;
		c.gridy++;
		addComponent(p, new JLabel("Arrow Length "), c);
		c.gridx++;
		addComponent(p, predictionTime = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" seconds"), c);
		c.gridy++;
		c.gridx = 0;
		
		showTail.addActionListener(this);
		predictionArrow.addActionListener(this);
		
		
		
		setDialogComponent(p);
	}
	
	static public AISParameters showDialog(Frame parentFrame, AISParameters aisParameters) {
		if (aisDisplayDialog == null || aisDisplayDialog.getParent() != parentFrame) {
			aisDisplayDialog = new AISDisplayDialog(parentFrame);
		}
		aisDisplayDialog.aisParameters = aisParameters.clone();
		aisDisplayDialog.setParams();
		aisDisplayDialog.setVisible(true);
		return aisDisplayDialog.aisParameters;
	}

	@Override
	public void cancelButtonPressed() {
		aisParameters = null;
	}

	private void setParams() {
		showTail.setSelected(aisParameters.showTail);
		tailLength.setText(String.format("%d", aisParameters.tailLength));
		predictionArrow.setSelected(aisParameters.showPredictionArrow);
		predictionTime.setText(String.format("%d", aisParameters.predictionLength));
		enableControls();
	}
	
	@Override
	public boolean getParams() {
		aisParameters.showTail = showTail.isSelected();
		aisParameters.showPredictionArrow = predictionArrow.isSelected();
		try {
			aisParameters.tailLength = Integer.valueOf(tailLength.getText());
			aisParameters.predictionLength = Integer.valueOf(predictionTime.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		enableControls();		
	}

	private void enableControls() {
		tailLength.setEnabled(showTail.isSelected());
		predictionTime.setEnabled(predictionArrow.isSelected());
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
