package IMU;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;

public class IMUSettingsDialog extends PamDialog {

	private PamPanel panel;
	private ButtonGroup methodGroup;
	
	private IMUParams imuParams; 
	static private IMUSettingsDialog singleInstance;


	public IMUSettingsDialog(Window parentFrame, IMUControl imuControl) {
		super(parentFrame, "IMU Settings", false);
		
		panel = new PamPanel();
		panel.setBorder(new TitledBorder("IMU Angle logging"));
		panel.setLayout(new GridBagLayout());
		
		PamPanel settingsPanel;
		HidingPanel hPanel;
		JRadioButton button;
		methodGroup=new ButtonGroup();
		for (int i=0; i<imuControl.getMethods().size(); i++){
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 3;
			PamDialog.addComponent(panel, button = new JRadioButton(imuControl.getMethods().get(i).getName()), c);
			if (i==imuControl.getParams().currentIMUType) button.setSelected(true);
			methodGroup.add(button);
			c.gridy++;
			c.gridwidth = 3;
			//create a hiding panel
			settingsPanel=new PamPanel(new BorderLayout());
			hPanel = new HidingPanel(settingsPanel, imuControl.getMethods().get(i).getSettingsPanel(), HidingPanel.HORIZONTAL, false);
			settingsPanel.add(BorderLayout.CENTER, hPanel);
			hPanel.setTitle(imuControl.getMethods().get(i).getName() + "Settings...");
			//add an action listener to resize the dialog depending on the hiding panel. 
			hPanel.getHideButton().addActionListener(new ResizeDialog());
			PamDialog.addComponent(panel, settingsPanel, c);
		}
		
		setDialogComponent(panel);
		setResizable(true);

	}
	
	public static IMUParams showDialog(Frame parentFrame, IMUControl imuControl) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new IMUSettingsDialog(parentFrame, imuControl);
		}
		if (singleInstance.imuParams == null) {
			singleInstance.imuParams = new IMUParams();
		}
		else {
			singleInstance.imuParams = imuControl.getParams().clone();
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.imuParams;
	}
	
	private class ResizeDialog implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			singleInstance.invalidate();
			singleInstance.pack();
			singleInstance.validate();
			singleInstance.repaint();
		}
	}

	private void setParams() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}



}
