package videoRangePanel.layoutAWT;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import videoRangePanel.VRHeightData;

public class HeightDialog extends PamDialog {

	private static HeightDialog singleInstance; 
	private VRHeightData vrHeightData;
	
	JTextField name, height;
	
	private HeightDialog(Frame parentFrame) {
		super(parentFrame, "Camera height", false);
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Height Data"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		addComponent(p, new JLabel("Name "), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(p, name = new JTextField(20), c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Height "), c);
		c.gridx++;
		addComponent(p, height = new JTextField(6), c);
		c.gridx++;
		addComponent(p, new JLabel(" m "), c);
		
		setDialogComponent(p);
	}

	public static VRHeightData showDialog(Frame frame, VRHeightData vrHeightData) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new HeightDialog(frame);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.vrHeightData;
	}
	
	private void setParams() {
		name.setText("");
		if (vrHeightData == null) {
			height.setText("");
			setTitle("New Height Data");
			return;
		}
		else {
			setTitle("Edit Height Data");
		}
		if (vrHeightData.name != null) {
			name.setText(vrHeightData.name);
		}
		height.setText(String.format("%.2f", vrHeightData.height));
	}
	
	@Override
	public void cancelButtonPressed() {
		vrHeightData = null;
	}

	@Override
	public boolean getParams() {
		if (vrHeightData == null) {
			vrHeightData = new VRHeightData();
		}
		vrHeightData.name = new String(name.getText());
		try {
			vrHeightData.height = Double.valueOf(height.getText());
		}
		catch (NumberFormatException e){
			return false;
		}
		return (vrHeightData.name.length() > 0);
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
