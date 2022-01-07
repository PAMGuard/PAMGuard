package loggerForms.formdesign;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.formdesign.FormEditor.EditNotify;
import loggerForms.formdesign.propertypanels.PropertyPanel;

public class EditPropertyPanel implements PamDialogPanel {

	private JPanel propertyPanel = new JPanel();
	private FormEditDialog formEditDialog;
	private FormDescription formDescription;
	private ArrayList<PropertyPanel> propertyPanels = new ArrayList<>();
	
	public EditPropertyPanel(FormEditDialog formEditDialog,
			FormDescription formDescription) {
		this.formEditDialog = formEditDialog;
		this.formDescription = formDescription;
		
		JPanel propPanel  = new JPanel();
		propPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.insets = new Insets(0, 2, 0, 2);
		
		FormEditor formEditor = formEditDialog.getFormEditor();
		JLabel label;
		for (PropertyTypes propType:PropertyTypes.values()) {
			PropertyPanel panel = formEditor.makePropertyPanel(propType);
			if (panel == null) {
				continue;
			}
			c.gridx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			propPanel.add(label = new JLabel("  " + propType.toString(), JLabel.RIGHT), c);
			label.setToolTipText(propType.getDescription());
			c.gridx++;
			c.fill = GridBagConstraints.NONE;
			propertyPanels.add(panel);
			propPanel.add(panel.getPanel(), c);
			c.gridy++;
		}
		JPanel pl = new JPanel(new BorderLayout());
		pl.add(BorderLayout.WEST, propPanel);
		JScrollPane scrollPane = new JScrollPane(pl);
		scrollPane.setPreferredSize(new Dimension(0, 450));
		
		propertyPanel.setLayout(new BorderLayout());
		propertyPanel.add(BorderLayout.CENTER, scrollPane);
	}


	@Override
	public JComponent getDialogComponent() {
		return propertyPanel;
	}

	@Override
	public void setParams() {
		FormEditor formEditor = formEditDialog.getFormEditor();
		for (PropertyPanel pp:propertyPanels) {
			pp.pushProperty(formEditor.getFormProperty(pp.getPropertyType()));
			pp.propertyEnable(pp.getUseProperty().isSelected());
		}
		
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		FormEditor formEditor = formEditDialog.getFormEditor();
		for (PropertyPanel pp:propertyPanels) {
			/**
			 * First see if the property is selected - all have a checkbox. 
			 */
			boolean isUsed = pp.getUseProperty().isSelected();
			if (isUsed == false) {
				formEditor.setFormProperty(pp.getPropertyType(), null);
			}
			else {
				/**
				 * Should probably put something in here to ensure that the necessary properties
				 * in the property panel are not empty or have reasonable values in them. 
				 */
				ItemInformation newInfo = pp.fetchProperty(formEditor.getFormProperty(pp.getPropertyType()));
				formEditor.setFormProperty(pp.getPropertyType(), newInfo);
			}
		}
		
		return ok;
	}


	/**
	 * Called when there are changes in the controls. <p>
	 * Will get called quite often, but can rebuild lists, etc. 
	 * @param notifyType
	 */
	public void notifyChanges(EditNotify notifyType) {
		for (PropertyPanel panel:propertyPanels) {
			panel.notifyChanges(notifyType);
		}
	}
}
