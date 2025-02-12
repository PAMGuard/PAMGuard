package generalDatabase.lookupTables;



import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import PamView.dialog.PamDialog;
import PamView.dialog.SettingsButton;
import PamView.panel.PamPanel;

/**
 * Lookup component to go in dialogs, Logger forms, etc. 
 * <p> Has the look and feel of Logger drop down boxes but
 * lots of extra features, such as being editable and updatable on the 
 * fly, etc. 
 * @author Doug Gillespie
 *
 */
public class LookupComponent {

	private String lookupTopic;
	
	private JPanel mainPanel;
	
	private JPanel westPanel;
	
	private JTextField codeField;
	
	private JComboBox comboBox;
	
	private JLabel westLabel;
	
	private JLabel northLabel;
	
	private LookupList lookupList;
	
	private ArrayList<LookupChangeListener> changeListeners = new ArrayList<>();
	
	private String nullSelectionText = "=== no selection ===";
	
	/**
	 * @return the lookupList
	 */
	public LookupList getLookupList() {
		return lookupList;
	}


	/**
	 * @param lookupList the lookupList to set
	 */
	public void setLookupList(LookupList lookupList) {
		this.lookupList = lookupList;
	}

	private Vector<LookupItem> selectedList;
	
	private boolean allowNullSelection = true;
	
	private boolean allowEdits = true;

	/**
	 * Make a lookup list with an option to auto-update automatically whenever there is a change to this list
	 * within PAMGuard, e.g. if the same topic is used in multiple controls. 
	 * @param lookupTopic lookup topic
	 * @param lookupList lookup list
	 * @param autoUpdate  flag to auto update in the event of changes. 
	 */
	public LookupComponent(String lookupTopic, LookupList lookupList, boolean autoUpdate) {
		this.lookupList = lookupList;
		
		this.lookupTopic = lookupTopic;
		
		mainPanel = new PamPanel();
		mainPanel.setLayout(new BorderLayout());
		
		PamPanel eventPanel = new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints(); 
		c.gridy=0;
		c.gridx=0;
		c.insets = new Insets(0,0,0,5); 


		westPanel = new JPanel(new BorderLayout());
		westPanel.add(BorderLayout.CENTER, codeField = new JTextField(5));
		c.gridx++;

		PamDialog.addComponent(eventPanel, westPanel, c);
		c.gridx++;
		
		PamDialog.addComponent(eventPanel, comboBox = new JComboBox(), c);
		c.gridx++;
		
		SettingsButton editButton = new SettingsButton(); 
		editButton.addActionListener(new EditList());
		PamDialog.addComponent(eventPanel, editButton, c);
		
		

		mainPanel.add(BorderLayout.CENTER, eventPanel);

		
		comboBox.addActionListener(new ListActionListener());
		codeField.addFocusListener(new CodeFieldListener());
		comboBox.addMouseListener(new CodeMouseListener());
		codeField.addMouseListener(new CodeMouseListener());
		codeField.addKeyListener(new F4KeyListener());
		comboBox.addKeyListener(new F4KeyListener());
		
		fillList();
	}
	
	/**
	 * Make a lookup component which won't auto-update. 
	 * @param lookupTopic lookup topic
	 * @param lookupList lookup list
	 */
	public LookupComponent(String lookupTopic, LookupList lookupList) {
		this(lookupTopic, lookupList, false);
	}
	
	/**
	 * Set whether or not to show the code panel to the left of the main drop down. 
	 * @param showCode show the code panel
	 */
	public void setShowCodePanel(boolean showCode) {
		westPanel.setVisible(showCode);
	}
	
	/**
	 * 
	 * @return true if the code panel is shown. 
	 */
	public boolean isShowCodePanel() {
		return westPanel.isVisible();
	}
	
	/**
	 * @return The currently selected lookup item. 
	 */
	public LookupItem getSelectedItem() {
		int selInd = comboBox.getSelectedIndex();
		if (allowNullSelection) {
			selInd--;
		}
		if (selInd < 0 || selectedList == null) {
			return null;
		}
		if (selInd >= selectedList.size()) {
			codeField.setText("Error!");
			return null;
		}
		return selectedList.get(selInd);
	}
	
	private class CodeMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
			}
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (me.isPopupTrigger()) {
				showPopupMenu(me);
			}
		}
		
	}
	
	class F4KeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent k) {
//			System.out.println("Key pressed" + k.getKeyCode());
			if (k.getKeyCode() == 115) {
//				f4Pressed();
				comboBox.showPopup();
				comboBox.requestFocus();
			}
		}
	}
	
	
	public void showPopupMenu(MouseEvent me) {
		if (allowEdits == false) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItemEdit = new JMenuItem("Edit list ...");
		menuItemEdit.addActionListener(new EditList());
		menu.add(menuItemEdit);
		JMenuItem menuItemUpdate = new JMenuItem("Update list ...");
		menuItemUpdate.addActionListener(new UpdateList());
		menu.add(menuItemUpdate);
		menu.show(me.getComponent(), me.getX(), me.getY());
	}
	
	private class EditList implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			LookupList newList = 
				LookUpTables.getLookUpTables().editLookupTopic(null, lookupTopic);
			if (newList != null) {
				lookupList = newList;
				fillList();
			}
		}
	}
	
	private class UpdateList implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			lookupList = null;//LookUpTables.getLookUpTables().getLookupList(lookupTopic);
			
			fillList();
			
		}
	}

	private class CodeFieldListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent arg0) {
			
		}

		@Override
		public void focusLost(FocusEvent arg0) {
			if (setComboFromCode() == false) {
				
			}
		}
		
	}
	
	public boolean setComboFromCode() {
		String code = codeField.getText();
		int codeIndex = findCodeIndex(code);
		if (allowNullSelection) {
			codeIndex++;
		}
		comboBox.setSelectedIndex(codeIndex);
		return true;
	}

	/**
	 * find the index of a particular code in the selected list
	 * @param code text from the code field
	 * @return index in the selected list or -1 if code not found
	 */
	private int findCodeIndex(String code) {
		if (selectedList == null || code == null) {
			return -1;
		}
		for (int i = 0; i < selectedList.size(); i++) {
		  if (code.equalsIgnoreCase(selectedList.get(i).getCode())) {
			  return i;
		  }
		}
		return -1;
	}

	private class ListActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			newListSelection(arg0);
		}
	}
	
	private void newListSelection(ActionEvent arg0) {
		LookupItem item = getSelectedItem();
		if (item == null) {
			codeField.setText("");
		}
		else {
			codeField.setText(item.getCode());
		}
		tellChangeListeners(item);
	}

	public void fillList() {
		comboBox.removeAllItems();
		if (allowNullSelection) {
			comboBox.addItem(nullSelectionText);
		}
		if (lookupList == null) {
			lookupList = LookUpTables.getLookUpTables().getLookupList(lookupTopic);
		}
		if (lookupList == null) {
			return;
		}
		selectedList = lookupList.getSelectedList();
		if (selectedList == null) {
			return;
		}
		for (int i = 0; i < selectedList.size(); i++) {
			comboBox.addItem(String.format("%s (%s)",selectedList.get(i).getText(),selectedList.get(i).getCode()));
		}		
	}

	public JComponent getComponent() {
		return mainPanel;
	}
	
	public void setWestTitle(String westTitle) {
		if (westLabel == null) {
			westLabel = new JLabel(westTitle, SwingConstants.RIGHT);
			westPanel.add(BorderLayout.WEST, westLabel);
		}
		else {
			westLabel.setText(westTitle);
		}
	}
	public void setNorthTitle(String northTitle) {
		if (northLabel == null) {
			northLabel = new JLabel(northTitle, SwingConstants.LEFT);
			mainPanel.add(BorderLayout.NORTH, northLabel);
		}
		else {
			westLabel.setText(northTitle);
		}
	}

	public boolean isAllowNullSelection() {
		return allowNullSelection;
	}

	public void setAllowNullSelection(boolean allowNullSelection) {
		this.allowNullSelection = allowNullSelection;
		fillList();
	}

	public boolean isAllowEdits() {
		return allowEdits;
	}

	public void setAllowEdits(boolean allowEdits) {
		this.allowEdits = allowEdits;
	}

	/**
	 * Set the code manually, then let the comboBox part
	 * update itself
	 * @param codeText
	 */
	public void setSelectedCode(String codeText) {
		codeField.setText(codeText);
		setComboFromCode();
	}

	/**
	 * @return the selectedList
	 */
	public Vector<LookupItem> getSelectedList() {
		return selectedList;
	}
	
	
	public void setToolTipText(String hint){
		setToolTipText(hint,hint);
	}
	
	public void setToolTipText(String codeHint, String comboHint){
		codeField.setToolTipText(codeHint);
		comboBox.setToolTipText(comboHint);
	}


	public void setTopic(String topicName) {
		this.lookupTopic = topicName;
		lookupList = null;
		fillList();
	}
	
	/**
	 * Add a change listener to receive notifications whenever the lookup selection changes. 
	 * @param changeListener change listener
	 */
	public void addChangeListener(LookupChangeListener changeListener) {
		changeListeners.add(changeListener);
	}

	/**
	 * Remove a change listener that received notifications whenever the lookup selection changes. 
	 * @param changeListener change listener
	 */
	public void removeChangeListener(LookupChangeListener changeListener) {
		changeListeners.remove(changeListener);
	}
	
	/**
	 * Notify change listeners that a new LUT item has been selected. 
	 * @param selectedItem
	 */
	private void tellChangeListeners(LookupItem selectedItem) {
		for (LookupChangeListener lcl:changeListeners) {
			lcl.lookupChange(selectedItem);
		}
	}


	/**
	 * @return the nullSelectionText
	 */
	public String getNullSelectionText() {
		return nullSelectionText;
	}


	/**
	 * @param nullSelectionText the nullSelectionText to set
	 */
	public void setNullSelectionText(String nullSelectionText) {
		this.nullSelectionText = nullSelectionText;
	}
	
}
