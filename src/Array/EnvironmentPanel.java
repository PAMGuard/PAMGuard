package Array;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class EnvironmentPanel {

	private ArrayDialog arrayDialog;
	
	private JPanel environmentPanel;
	
	private JTextField speedOfSound, sosError;// m/s
	
	private double newSpeed;
	
	private double newError;
	

	EnvironmentPanel(ArrayDialog arrayDialog) {
		this.arrayDialog = arrayDialog;
		environmentPanel = makePanel();
	}
	
	JPanel makePanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Environment"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		PamDialog.addComponent(panel,new JLabel("Speed of sound "),c);
		c.gridx++;
		PamDialog.addComponent(panel,speedOfSound = new JTextField(5),c);
		c.gridx++;
		PamDialog.addComponent(panel,new JLabel(" +/- "),c);
		c.gridx++;
		PamDialog.addComponent(panel,sosError = new JTextField(3),c);
		c.gridx++;
		PamDialog.addComponent(panel,new JLabel(" m/s"),c);
		
		JPanel outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(BorderLayout.CENTER, panel);
		
		return outerPanel;
	}

	public boolean getParams() {
		try {
			newSpeed = Double.valueOf(speedOfSound.getText());
			newError = Double.valueOf(sosError.getText());
		}
		catch (Exception Ex) {
			return false;
		}
		return true;
	}
	/**
	 * @return Returns the newSpeed.
	 */
	public double getNewSpeed() {
		return newSpeed;
	}
	
	public double getNewError() {
		return newError;
	}

	/**
	 * @param newSpeed The newSpeed to set.
	 */
	public void setNewSpeed(double newSpeed) {
		speedOfSound.setText(PamDialog.formatDouble(newSpeed));
		this.newSpeed = newSpeed;
	}
	
	public void setNewError(double newError) {
		sosError.setText(PamDialog.formatDouble(newError));
		this.newError = newError;
	}
	/**
	 * @return Returns the environmentPanel.
	 */
	public JPanel getEnvironmentPanel() {
		return environmentPanel;
	}
	
}
