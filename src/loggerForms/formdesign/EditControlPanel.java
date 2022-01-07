package loggerForms.formdesign;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.controlpropsets.BasePropertySet;
import loggerForms.formdesign.itempanels.CtrlColPanel;

/**
 * Left hand side of the dialog which displays all the components which can be 
 * added / removed. 
 * @author Doug
 *
 */
public class EditControlPanel implements PamDialogPanel {

	private JPanel controlPanel = new JPanel();
	private JPanel leftPanel  = new JPanel();
	private JPanel mainPanel = new JPanel();
	private JPanel controlEditPanel = new JPanel();
	private JLabel controlTypeLabel = new JLabel(" ", JLabel.CENTER);
	private JLabel controlEditTitle = new JLabel(" ", JLabel.CENTER);
	private FormEditDialog formEditDialog;
	private FormDescription formDescription;
	//	private ControlTableModel tableDataModel;
	private JButton addButton, upButton, downButton;
	private JButton deleteButton;
	private ArrayList<CtrlColPanel> itemPropertyPanels;

	/**
	 * @return the itemPropertyPanels
	 */
	public ArrayList<CtrlColPanel> getItemPropertyPanels() {
		return itemPropertyPanels;
	}

	public EditControlPanel(FormEditDialog formEditDialog,
			FormDescription formDescription) {
		this.formEditDialog = formEditDialog;
		this.formDescription = formDescription; 
		itemPropertyPanels = new ArrayList<>();
		
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		JPanel ctrlNorthPanel = new JPanel(new BorderLayout());
		ctrlNorthPanel.add(BorderLayout.NORTH, controlPanel);
		JScrollPane scrollPane = new JScrollPane(ctrlNorthPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		leftPanel.setLayout(new BorderLayout());
		scrollPane.setPreferredSize(new Dimension(0, 450));
		leftPanel.add(BorderLayout.CENTER, scrollPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton = new JButton("Add"));
		buttonPanel.add(upButton = new JButton("Up"));
		buttonPanel.add(downButton = new JButton("Down"));
		buttonPanel.add(deleteButton = new JButton("Delete"));
		JPanel buttonOuterPanel = new JPanel(new BorderLayout());
		buttonOuterPanel.add(BorderLayout.EAST, buttonPanel);

		addButton.addActionListener(new AddButton());
		upButton.addActionListener(new UpButton());
		downButton.addActionListener(new DownButton());
		deleteButton.addActionListener(new DeleteButton());
		addButton.setToolTipText("Add a new control item at the end of the list");
		upButton.setToolTipText("Move selected item up");
		downButton.setToolTipText("Move selected item down");
		deleteButton.setToolTipText("Delete the selected item");
		leftPanel.add(BorderLayout.SOUTH, buttonOuterPanel);

		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(BorderLayout.WEST, leftPanel);

		JPanel controlOuter = new JPanel(new BorderLayout());
		JPanel northInfoPanel = new JPanel();
		northInfoPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		northInfoPanel.add(controlTypeLabel, c);
		c.gridy++;
		northInfoPanel.add(controlEditTitle, c);
		controlOuter.add(BorderLayout.NORTH, northInfoPanel);
		JPanel p2 = new JPanel(new BorderLayout());
		controlOuter.setBorder(new EmptyBorder(new Insets(2,2,2,2)));
		p2.add(BorderLayout.NORTH, controlEditPanel);
//		p2.setBorder(new TitledBorder("P2"));
//		JScrollPane editScroll = new JScrollPane(p2, 
//				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlOuter.add(BorderLayout.CENTER, p2);

		mainPanel.add(BorderLayout.EAST, controlOuter);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Recreate all the controls in the dialog. 
	 */
	public void rebuild() {
		controlPanel.removeAll();
		ArrayList<ControlTitle> controls = formEditDialog.getFormEditor().getControlTitles();
		for (ControlTitle aTitle:controls) {
			controlPanel.add(aTitle.getComponent());
			aTitle.setValues();
		}
		if (controls.size() > 0) {
			formEditDialog.setSelectedTitle(controls.get(0));
		}
		titleSelected();
		controlPanel.revalidate();
	}

	private class UpButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.moveUp(formEditDialog.getSelectedControl());
		}
	}

	private class DownButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.moveDown(formEditDialog.getSelectedControl());
		}
	}
	private class AddButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.addNewControl();
		}
	}
	private class DeleteButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.deleteControl(formEditDialog.getSelectedControl());
		}
	}
	public void titleSelected() {
		ControlTitle selTitle = formEditDialog.getSelectedControl();
		int selIndex = formEditDialog.getControlIndex(selTitle);
		upButton.setEnabled(selTitle != null && selIndex > 0);
		downButton.setEnabled(selTitle != null && selIndex < formEditDialog.getFormEditor().getControlTitles().size()-1);
		deleteButton.setEnabled(selTitle != null);

		if (selTitle == null) {
			controlEditPanel.removeAll();
		}
		else {
			BasePropertySet propertySet = ControlEditor.createControlPropertySet(formDescription, selTitle);
			fillControlEditPanel(propertySet, selTitle);
		}
	}

	private void fillControlEditPanel(BasePropertySet propertySet, ControlTitle selTitle) {
		controlEditPanel.removeAll();
		itemPropertyPanels.clear();
		ControlTypes type = selTitle.getType();
		if (type != null) {
			controlTypeLabel.setText("  " + type.toString() + " - " + type.getDescription() + "  ");
		}
		else {
			controlTypeLabel.setText("  Select a control type from the drop down list  ");
		}
		if (propertySet != null) {
			controlEditTitle.setText(propertySet.getPanelTitle());
			controlEditPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.fill = GridBagConstraints.NONE;
			for (UDColName propertyName:UDColName.values()) {
				CtrlColPanel propertyPanel = propertySet.getItemPropertyPanel(selTitle, propertyName);
				if (propertyPanel != null) {
					itemPropertyPanels.add(propertyPanel);
					c.gridx = 0;
					c.fill = GridBagConstraints.HORIZONTAL;
					controlEditPanel.add( new JLabel(" " + propertyPanel.getTitle() + " ", JLabel.RIGHT), c);
					c.gridx++;
					c.fill = GridBagConstraints.NONE;
					controlEditPanel.add(propertyPanel.getPanel(), c);
					c.gridy++;
					propertyPanel.pushProperty(selTitle.getItemInformation());
				}
			}
		}
		else {
			controlEditTitle.setText("This control has no configuarble properties");
		}
		controlEditPanel.invalidate();
		controlEditPanel.repaint();
		formEditDialog.pack();
	}

	//	private class ControlTableModel extends AbstractTableModel {
	//
	//		@Override
	//		public int getColumnCount() {
	//			return 1;
	//		}
	//
	//		@Override
	//		public int getRowCount() {
	//			return formEditDialog.controlTitles.size();
	//		}
	//
	//		@Override
	//		public Object getValueAt(int arg0, int arg1) {
	//			return formEditDialog.controlTitles.get(arg1);
	//		}
	//		
	//	}
}
