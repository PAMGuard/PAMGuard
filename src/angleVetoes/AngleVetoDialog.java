package angleVetoes;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog to get data for a single angle veto. 
 * 
 * @author Douglas Gillespie
 * @see AngleVetoesDialog
 * @see AngleVetoes
 *
 */
public class AngleVetoDialog extends PamDialog {

	private AngleVeto angleVeto;
	private static AngleVetoDialog singleInstance;
	
	private JTextField startAngle, endAngle;
	
	private AngleVetoDialog(Frame parentFrame) {
		super(parentFrame, "Angle Veto Parameters", false);
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		addComponent(p, new JLabel("Start angle"), c);
		c.gridx++;
		addComponent(p, startAngle = new JTextField(4), c);
		c.gridx++;
		addComponent(p, new JLabel("\u00B0"), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(p, new JLabel("End angle"), c);
		c.gridx++;
		addComponent(p, endAngle = new JTextField(4), c);
		c.gridx++;
		addComponent(p, new JLabel("\u00B0"), c);
		
		setDialogComponent(p);
	}

	public static AngleVeto showDialog(Frame parentFrame, AngleVeto angleVeto) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new AngleVetoDialog(parentFrame);
		}
		
		singleInstance.angleVeto = angleVeto;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.angleVeto;
	}
	
	@Override
	public void cancelButtonPressed() {
		angleVeto = null;
	}
	
	private void setParams() {
		if (angleVeto == null) {
			angleVeto = new AngleVeto();
		}
		else {
			angleVeto = angleVeto.clone();
		}
		startAngle.setText(String.format("%.1f", angleVeto.startAngle));
		endAngle.setText(String.format("%.1f", angleVeto.endAngle));
	}

	@Override
	public boolean getParams() {
		try {
			angleVeto.startAngle = Double.valueOf(startAngle.getText());
			angleVeto.endAngle = Double.valueOf(endAngle.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
