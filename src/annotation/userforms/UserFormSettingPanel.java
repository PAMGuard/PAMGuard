package annotation.userforms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamController.PamController;
import annotation.AnnotationSettingsPanel;
import annotation.handler.AnnotationOptions;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import loggerForms.FormDescription;
import loggerForms.FormsControl;

public class UserFormSettingPanel implements AnnotationSettingsPanel {
	
	private JPanel mainPanel;
	private UserFormAnnotationType userFormAnnotationType;
	
	private JLabel warningLabel;
	private JComboBox<String> userFormList;
	private JButton editButton, createButton;

	public UserFormSettingPanel(UserFormAnnotationType userFormAnnotationType) {
		this.userFormAnnotationType = userFormAnnotationType;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, warningLabel = new JLabel(" "));
		JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mainPanel.add(BorderLayout.CENTER, formPanel);
		formPanel.add(new JLabel("User form: "));
		formPanel.add(userFormList = new JComboBox<String>());
		formPanel.add(editButton = new JButton("Edit form"));
		formPanel.add(createButton = new JButton("Create new"));
		userFormList.addActionListener(new FormSelect());
		editButton.addActionListener(new EditForm());
		createButton.addActionListener(new CreateForm());
	}

	@Override
	public JComponent getSwingPanel() {
		return mainPanel;
	}

	@Override
	public void setSettings(AnnotationOptions annotationOptions) {
		UserFormAnnotationOptions formOptions = (UserFormAnnotationOptions) annotationOptions;
		boolean dbOk = (getDatabaseConnection() != null);
		if (dbOk == false) {
			warningLabel.setText("A database is required before user form annotations can be configured");
			enableControls();
			return;
		}
		else {
			warningLabel.setText("Select / Edit or Create a Logger User Form");
		}
		FormsControl formsControl = UserFormAnnotationType.getFormsControl();
		formsControl.readUDFTables();
		userFormList.removeAllItems();
		int nForms = formsControl.getNumFormDescriptions();
		for (int i = 0; i < nForms; i++) {
			FormDescription fd = formsControl.getFormDescription(i);
			userFormList.addItem(fd.getUdfName());
		}
		userFormList.setSelectedItem(formOptions.getUdfFormName());
		
		enableControls();
	}

	@Override
	public AnnotationOptions getSettings() {
		UserFormAnnotationOptions settings = userFormAnnotationType.getUserFormAnnotationOptions();
		settings.setUdfFormName((String) userFormList.getSelectedItem());
		return settings;
	}
	
	private PamConnection getDatabaseConnection() {
		return DBControlUnit.findConnection();
	}
	
	private void enableControls() {
		boolean dbOk = (getDatabaseConnection() != null);
		userFormList.setEnabled(dbOk);
		createButton.setEnabled(dbOk);
		editButton.setEnabled(dbOk && userFormList.getSelectedIndex() >= 0);
	}

	private class EditForm implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			editForm();
		}
	}
	private class FormSelect implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	private class CreateForm implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			createForm();
		}
	}
	public void editForm() {
		FormsControl formsControl = UserFormAnnotationType.getFormsControl();
		String formName = (String) userFormList.getSelectedItem();
		FormDescription fd = formsControl.findFormDescription(formName);
		if (fd != null) {
			fd.editForm(PamController.getMainFrame());
		}
	}

	/**
	 * Create a new form. 
	 */
	public void createForm() {
		FormsControl formsControl = UserFormAnnotationType.getFormsControl();
		String newFormName = formsControl.newLoggerform(null);
		if (newFormName == null) {
			return;
		}
		// immediately make it hidden so it doesn't show up in logger forms
		// set this as the selected form. 
		formsControl.readUDFTables();
		FormDescription fd = formsControl.findFormDescription(newFormName);
		if (fd != null) {
//			fd.getHIDDEN();
			//need some way to do this !
		}
		userFormAnnotationType.getUserFormAnnotationOptions().setUdfFormName(newFormName);
		
		
		setSettings(userFormAnnotationType.getUserFormAnnotationOptions());
		
	}
}
