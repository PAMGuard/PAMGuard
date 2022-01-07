package PamController;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamModel.PamModuleInfo;
import PamView.dialog.PamDialog;
import PamguardMVC.PamConstants;

public class NewModuleDialog extends PamDialog {

	String newName;
	
	JLabel label;
	
	JTextField nameField;
	
	String currentName;
	
	PamModuleInfo moduleInfo;
	
	
	private NewModuleDialog(Frame parentFrame) {
		super(parentFrame, "Module Name", true);

		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("New Module Name"));
		
		
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.insets = new Insets(5, 2, 5, 2);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		addComponent(p, label = new JLabel("  Some name or other  "), c);
		c.gridy++;
		addComponent(p, nameField = new JTextField(20), c);
		
		setDialogComponent(p);
		
	}
	
	public static String showDialog(Frame frame, PamModuleInfo moduleInfo, String currentName) {
		
		NewModuleDialog d = new NewModuleDialog(frame);
		
		d.moduleInfo = moduleInfo;
		d.currentName = currentName;
		d.setParams();
		d.setVisible(true);
		
		return d.newName;
	}

	@Override
	public void cancelButtonPressed() {
		newName = null;
	}
	
	public void setParams() {
		label.setText(String.format(" Module name for new %s ", moduleInfo.getDescription()));
		if (currentName != null) {
			nameField.setText(currentName);
		}
		else {
			nameField.setText(moduleInfo.getNewDefaultName());
		}
	}
	

	@Override
	public boolean getParams() {
		newName = nameField.getText();
		if (newName == null) return false;
		newName = newName.trim();
		if (newName.length() == 0) return false;
		if (newName.length() > PamConstants.MAX_ITEM_NAME_LENGTH) {
			JOptionPane.showMessageDialog(this, "Maximum module name length is " + 
					PamConstants.MAX_ITEM_NAME_LENGTH + " characters", 
					"New " + moduleInfo.getDescription(), JOptionPane.ERROR_MESSAGE);
			return false; // repeat name			
		}
		if (newName.equalsIgnoreCase(currentName)) {
			// that's OK - no need to do other checks.
			
		}
		else if (PamController.getInstance().findControlledUnit(moduleInfo.getModuleClass(), newName) != null) {
			JOptionPane.showMessageDialog(this, "Module name \"" + newName + "\" already exists", 
					"New " + moduleInfo.getDescription(), JOptionPane.ERROR_MESSAGE);
			return false; // repeat name
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		setParams();

	}

}
