package GPS;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ShipDimensionsFields extends JPanel implements ActionListener{
	
	JTextField[] shipDims;
	
	String[] dimNames;

	
	public ShipDimensionsFields(String[] dimNames) {
		super();
		this.dimNames = dimNames;
		shipDims = new JTextField[dimNames.length];
		GridBagLayout l;
		setLayout(l = new GridBagLayout());
		GridBagConstraints gbs = new PamGridBagContraints();
		this.setBorder(new EmptyBorder(5,0,0,0));
		gbs.gridx = gbs.gridy = 0;
		for (int i = 0; i < dimNames.length; i++) {
			gbs.gridy = i;
			gbs.gridx = 0;
			PamDialog.addComponent(this, new JLabel(" " + dimNames[i] + " ", JLabel.RIGHT), gbs);
			gbs.gridx = 1;
			PamDialog.addComponent(this, shipDims[i] = new JTextField(4), gbs);
			gbs.gridx = 2;
			PamDialog.addComponent(this, new JLabel(" m"), gbs);
			shipDims[i].addActionListener(this);
		}
	}
	
	public boolean setDimensions(double[] dimensions) {
		for (int i = 0; i < Math.min(dimensions.length, shipDims.length); i++) {
			shipDims[i].setText(String.format("%.1f", dimensions[i]));
		}
		return (dimensions.length == shipDims.length);
	}
	
	public double[] getDimensions() {
		double[] dimensions = new double[shipDims.length];
		for (int i = 0; i < shipDims.length; i++) {
			try {
				dimensions[i] = Double.valueOf(shipDims[i].getText());
			}
			catch (NumberFormatException ex) {
				ex.printStackTrace();
				return null;
			}
		}
		return dimensions;
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
//		dimensionsChanged();
	}
	
	
}
