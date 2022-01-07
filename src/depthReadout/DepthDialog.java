package depthReadout;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class DepthDialog extends PamDialog {


	private static DepthDialog singleInstance;
	
	private DepthSystem depthSystem;
	
	private DepthControl depthControl;
	
	private DepthParameters depthParameters;
	
	DevicePanel devicePanel;
	
	private DepthDialog(DepthControl depthControl, Frame parentFrame) {
		super(parentFrame, "Depth Readout", false);
		
		this.depthControl = depthControl;
		this.depthSystem = depthSystem;
		this.depthParameters = depthParameters.clone();
		// construct the dialog panel. 
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		addComponent(p, devicePanel = new DevicePanel(), c);
		
		configureDialog();

		setHelpPoint("utilities.depthreadout.docs.depth_overview");
		
		setDialogComponent(p);

		
	}
	
	private void configureDialog() {
		
	}
	
	private void newDevice() {
		devicePanel.getParams();
//		depthSystem = depthControl.
	}
	
	private class HitEnter implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			System.out.println("Hit enter");
		}
		
	}
	
	class DevicePanel extends JPanel {
		
		JComboBox deviceTypes;
		
		JTextField nDevices;
		
		JComboBox rangeList;

		public DevicePanel() {
			super();
			setBorder(new TitledBorder("Select type and number of readout devices"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = c.gridy = 0;
			addComponent(this, new JLabel("Device Type"), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, deviceTypes = new JComboBox() , c);
			c.gridx+=2;
			c.gridwidth = 1;
			addComponent(this, new JLabel(" Range"), c);
			c.gridx++;
			addComponent(this, rangeList = new JComboBox() , c);
			
			c.gridx = 0;
			c.gridy ++;
			c.gridwidth = 1;
			addComponent(this, new JLabel("Number "), c);
			c.gridx++;
			addComponent(this, nDevices = new JTextField(3) , c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(this, new JLabel(" (hit enter to update)"), c);
			nDevices.addActionListener(new HitEnter());
			
			
		}
		
		public void setParams() {
			deviceTypes.removeAllItems();
			for (int i = 0; i < depthControl.getNumDepthSystems(); i++) {
				deviceTypes.addItem(depthControl.getDepthSystemName(i));
			}
			deviceTypes.setSelectedIndex(depthParameters.systemNumber);
			nDevices.setText(String.format("%d", depthParameters.nSensors));
		}
		
		public boolean getParams() {
			depthParameters.systemNumber = deviceTypes.getSelectedIndex();
			try {
				depthParameters.nSensors = Integer.valueOf(nDevices.getText());
			}
			catch (NumberFormatException e){
				return false;
			}
			return true;
		}
		
		public void changeDevice() {
			
		}
		
		public void changeNSensors() {
			
		}
	}

	public static DepthParameters showDialog(DepthControl depthControl, Frame parentFrame, DepthSystem depthSystem) {
//		singleInstance = new DepthDialog(depthControl, parentFrame, depthSystem);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.depthParameters;
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	private void setParams() {
		
	}
	
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	

}
