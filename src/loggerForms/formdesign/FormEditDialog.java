package loggerForms.formdesign;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import PamView.dialog.PamDialog;
import loggerForms.FormDescription;
import loggerForms.ItemDescription;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.formdesign.FormEditor.EditNotify;
import loggerForms.formdesign.itempanels.CtrlColPanel;

public class FormEditDialog extends PamDialog {
	
	private static FormEditDialog singleInstance;
	private FormEditor formEditor;
	
	/**
	 * @return the formEditor
	 */
	public FormEditor getFormEditor() {
		return formEditor;
	}

	private FormDescription formDescription;
	private EditControlPanel editControlPanel;
	private EditPropertyPanel editPropertyPanel;
//	protected FormList<ControlDescription> formControls;
	private ControlTitle selectedTitle;
	private JButton previewButton;
	private boolean formCancelled;
	
	private FormEditDialog(FormEditor formEditor) {
		super(formEditor.getParentFrame(), "Edit " + formEditor.getFormDescription().getFormName() + " form", false);
		this.formEditor = formEditor;
		this.formDescription = formEditor.getFormDescription();
		JPanel mainPanel = new JPanel(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		mainPanel.add(tabPane);
		editControlPanel = new EditControlPanel(this, formDescription);
		editPropertyPanel = new EditPropertyPanel(this, formDescription);
		tabPane.add(" Control Layout ", editControlPanel.getDialogComponent());
		tabPane.add(" Form Properties ", editPropertyPanel.getDialogComponent());
		
		getButtonPanel().add(previewButton = new JButton("Preview form"));
		previewButton.addActionListener(new PreviewButton());
		this.setResizable(true);
		this.setHelpPoint("visual_methods.loggerFormsHelp.docs.designingForms");
		setDialogComponent(mainPanel);
	}

	public static ArrayList<ItemInformation> showDialog(FormEditor formEditor) {
		if (singleInstance == null || singleInstance.formEditor != formEditor) {
			singleInstance = new FormEditDialog(formEditor);
		}
		/**
		 * Need to check the cloning of this to make sure it really has hard cloned
		 * all the properties of each control, so that we can easily revert. 
		 */
		singleInstance.buildDialog();
		singleInstance.notifyChanges(EditNotify.CONTROLCHANGE);
		singleInstance.setVisible(true);
		if (singleInstance.formCancelled) {
			return null;
		}
		else {
			return singleInstance.formEditor.getAllFormInformations();
		}
	}
	
	/**
	 * Create all the controls we'll need. 
	 */
	private void buildDialog() {
		formCancelled = false;
		ArrayList<ItemInformation> formControls = formDescription.getControlsInformationCopy();
		formEditor.getControlTitles().clear();
		for (ItemInformation ds:formControls) {
			formEditor.getControlTitles().add(new ControlTitle(this, ds));
		}
		editControlPanel.rebuild();
		notifyChanges(EditNotify.CONTROLCHANGE);
		editPropertyPanel.setParams();
		this.pack();
	}

	@Override
	public boolean getParams() {
		for (ControlTitle cTit:formEditor.getControlTitles()) {
			extractValues(cTit);
		}
		
		editPropertyPanel.getParams();
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		formCancelled = true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Get the index of a control title. 
	 * @param controlTitle
	 * @return the index of a control title. 
	 */
	protected int getControlIndex(ControlTitle controlTitle) {
		return formEditor.getControlTitles().indexOf(controlTitle);
	}

	public ControlTitle getSelectedControl() {
		if (formEditor.getControlTitles() == null || formEditor.getControlTitles().size() == 0) {
			return null;
		}
		return selectedTitle;
	}

	public void setSelectedTitle(ControlTitle controlTitle) {
		/*
		 * First get values out of the old control back into the ItemInformation
		 */
		if (this.selectedTitle != null) {
			extractValues(this.selectedTitle);
		}
		
		this.selectedTitle = controlTitle;
		for (ControlTitle aTitle:formEditor.getControlTitles()) {
			aTitle.setValues();
		}
		editControlPanel.titleSelected();
	}

	/**
	 * Pull the valued out of the right hand panel for the
	 * given control. 
	 * @param selectedTitle2
	 */
	private void extractValues(ControlTitle selectedTitle2) {
		ArrayList<CtrlColPanel> propertyPanels = editControlPanel.getItemPropertyPanels();
		for (CtrlColPanel itemPanel:propertyPanels) {
			itemPanel.fetchProperty(itemPanel.getControlTitle().getItemInformation());
		}
	}

	/**
	 * Delete a control
	 * @param controlTitle
	 */
	public void deleteControl(ControlTitle controlTitle) {
		String msg = String.format("Are you sure you want to remove the %s control from the form", 
				controlTitle.getItemInformation().getStringProperty(UDColName.Title.toString()));
		int ans = JOptionPane.showConfirmDialog(getParent(), msg, "Remove item", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return;
		}
		formEditor.getControlTitles().remove(controlTitle);
		editControlPanel.rebuild();
	}

	/**
	 * Insert a new control above this one
	 * @param controlTitle
	 */
	public void insertAbove(ControlTitle controlTitle) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Insert a new control below this one
	 * @param controlTitle
	 */
	public void insertBelow(ControlTitle controlTitle) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Move this control up in the list
	 * @param controlTitle
	 */
	public void moveUp(ControlTitle controlTitle) {
		if (controlTitle == null) {
			return;
		}
		int currInd = formEditor.getControlTitles().indexOf(controlTitle);
		if (currInd <= 0) {
			return;
		}
		formEditor.getControlTitles().remove(currInd);
		formEditor.getControlTitles().add(--currInd, controlTitle);
		
		editControlPanel.rebuild();
		
		setSelectedTitle(controlTitle);
		
	}
	
	/**
	 * Move this control down in the list. 
	 * @param controlTitle
	 */
	public void moveDown(ControlTitle controlTitle) {
		if (controlTitle == null) {
			return;
		}
		int currInd = formEditor.getControlTitles().indexOf(controlTitle);
		if (currInd < 0 || currInd >= formEditor.getControlTitles().size()-1) {
			return;
		}
		formEditor.getControlTitles().remove(currInd);
		formEditor.getControlTitles().add(++currInd, controlTitle);
		
		editControlPanel.rebuild();
		
		setSelectedTitle(controlTitle);
	}

	/**
	 * Add a new control to the end of the controls list. 
	 */
	public void addNewControl() {
		ControlTitle newCtrl = new ControlTitle(this, new ItemInformation(formDescription));
		formEditor.getControlTitles().add(newCtrl);
		editControlPanel.rebuild();

		setSelectedTitle(newCtrl);
	}

	public void notifyChanges(EditNotify notifyType) {
		
		editPropertyPanel.notifyChanges(notifyType);
	}
	private class PreviewButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			previewForm();
		}
	}
	
	/**
	 * Preview the form in a popup window of its' own. 
	 * Will open in a modal dialog so it can't actually one used 
	 * (it will probably write something to the database if asked !);
	 */
	public void previewForm() {
		if (!getParams()) {
			showWarning("Cannot preview the form since it contains errors");
		}
//		System.out.println("Preview form");
		
		formEditor.previewForm();
	}
}
