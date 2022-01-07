package loggerForms.formdesign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;

/**
 * Title panel for the control editor. This is currently 
 * just a dropdown box of controltypes. 
 * @author Doug
 *
 */
public class ControlTitle {

	private ControlEditor controlEditor;
	
	private JComboBox<ControlTypes> controlType;
	
	private JTextField controlTitle;

	private FormEditDialog formEditDialog;

	private ItemInformation itemInformation;
	
	/**
	 * @return the controlDescription
	 */
	public ItemInformation getItemInformation() {
		return itemInformation;
	}


	private JPanel titleComponent;
		
	private Color baseColour;
	
	public ControlTitle(FormEditDialog formEditDialog, ItemInformation itemInformation) {
		this.formEditDialog = formEditDialog;
		this.itemInformation = itemInformation;
		titleComponent = new JPanel();
		baseColour = titleComponent.getBackground();
		titleComponent.setBorder(new EmptyBorder(2,2,2,2));
		titleComponent.setLayout(new BorderLayout());
		controlType  = new JComboBox<>();
		for (ControlTypes ct:ControlTypes.values()) {
			controlType.addItem(ct);
		}
		controlType.setSelectedItem(itemInformation.getControlType());
		controlType.addActionListener(new ControlTypeListener());
		titleComponent.add(BorderLayout.WEST, controlType);
		
		this.controlTitle = new JTextField(12);
		titleComponent.add(BorderLayout.CENTER, controlTitle);
		
		TitleMouse titleMouse = new TitleMouse();
		controlType.addMouseListener(titleMouse);
		controlTitle.addMouseListener(titleMouse);		
		titleComponent.addMouseListener(titleMouse);
	}

	private class TitleMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent ev) {
			formEditDialog.setSelectedTitle(ControlTitle.this);
			if (ev.isPopupTrigger()) {
				showPopupMenu(ev);
			}
		}

		@Override
		public void mouseReleased(MouseEvent ev) {
			if (ev.isPopupTrigger()) {
				showPopupMenu(ev);
			}
		}
		
		
	}

	public void showPopupMenu(MouseEvent ev) {
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new 	JMenuItem("Move up");
		menuItem.addActionListener(new MoveUp());
		menuItem.setEnabled(formEditDialog.getControlIndex(this) > 0);
		popMenu.add(menuItem);
		menuItem = new 	JMenuItem("Move Down");
		menuItem.addActionListener(new MoveDown());
		menuItem.setEnabled(formEditDialog.getControlIndex(this) < formEditDialog.getFormEditor().getControlTitles().size()-1);
		popMenu.add(menuItem);
		menuItem = new 	JMenuItem("Insert Below");
		menuItem.addActionListener(new InsertBelow());
		popMenu.add(menuItem);
		menuItem = new 	JMenuItem("Insert Above");
		menuItem.addActionListener(new InsertAbove());
		popMenu.add(menuItem);
		menuItem = new 	JMenuItem("Delete Item");
		menuItem.addActionListener(new Delete());
		popMenu.add(menuItem);
		popMenu.show(ev.getComponent(), ev.getX(), ev.getY());
	}
	
	private class MoveUp implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.moveUp(ControlTitle.this);
		}
	}
	
	private class MoveDown implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.moveDown(ControlTitle.this);
		}
	}
	
	private class InsertBelow implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.insertBelow(ControlTitle.this);
		}
	}
	
	private class InsertAbove implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.insertAbove(ControlTitle.this);
		}
	}
	
	private class Delete implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formEditDialog.deleteControl(ControlTitle.this);
		}
	}
	
	private class ControlTypeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ControlTypes ctrlType = (ControlTypes) controlType.getSelectedItem();
			if (ctrlType != null) {
				itemInformation.setProperty(UDColName.Type.toString(), ctrlType.toString());
				formEditDialog.setSelectedTitle(ControlTitle.this);
			}
		}
	}
	
	public Component getComponent() {
		return titleComponent;
	}
	

	public void setValues() {
		String tit = itemInformation.getStringProperty(UDColName.Title.toString());
		if (tit == null) {
			controlTitle.setText("");
		}
		else {
			controlTitle.setText(tit);
		}
		boolean sel = formEditDialog.getSelectedControl() == this;
		controlType.setEnabled(sel);
		controlTitle.setEnabled(false);
		titleComponent.setBackground(sel ? Color.orange : baseColour);
	}


	/**
	 * Get the type of control that is currently selected
	 * in the drop down list. 
	 * @return
	 */
	public ControlTypes getType() {
		return (ControlTypes) controlType.getSelectedItem();
	}


	public void setTitle(String title) {
		controlTitle.setText(title);
	}
}
