package loggerForms.dataselect;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import PamView.dialog.PamGridBagContraints;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import loggerForms.controlDescriptions.CdLookup;

public class LookupDataDialogPanel extends ControlDataDialogPanel {
	
	private JPanel mainPanel;
	private CdLookup cdLookup;
	private LookupList lutList;
//	private JButton lutButton;
	private JCheckBox includeUnassigned;
	private JCheckBox[] checkBoxes;
	private LookupDataSelector lookupDataSelector;
//	private JPopupMenu popupMenu;	

	public LookupDataDialogPanel(LookupDataSelector lookupDataSelector) {
		super(lookupDataSelector);
		this.lookupDataSelector = lookupDataSelector;
		cdLookup = (CdLookup) lookupDataSelector.getControlDescription();
		lutList = cdLookup.getLookupList();
		Vector<LookupItem> items = lutList.getLutList();
		int n = items.size();
		
		mainPanel = new JPanel(new GridBagLayout());
		JPanel boxPanel = mainPanel;
		if (n > 3) {
			boxPanel = new JPanel(new GridBagLayout());
			mainPanel.setLayout(new BorderLayout());
			JScrollPane scrollPane = new JScrollPane(boxPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
						@Override
						public Dimension getPreferredSize() {
							Dimension dim = super.getPreferredSize();
							dim.height = Math.min(dim.height, 160);
							return dim;
						}				
			};
			mainPanel.add(scrollPane, BorderLayout.CENTER);
		}
		
		GridBagConstraints c = new PamGridBagContraints();
		c.ipady = 0;
		c.insets = new Insets(0,0,0,0);
		includeUnassigned = new JCheckBox("Include unassigned data");
		boxPanel.add(includeUnassigned,c);
		c.gridy++;
		checkBoxes = new JCheckBox[n];
		for (int i = 0; i < n; i++) {
			checkBoxes[i] = new JCheckBox(items.get(i).toString());
			boxPanel.add(checkBoxes[i], c);
			c.gridy++;
			
		}		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public boolean getParams() {
		LookupDataSelParams params = lookupDataSelector.getParams();
		params.setUseUnassigned(includeUnassigned.isSelected());
		Vector<LookupItem> items = lutList.getLutList();
		if (items == null || checkBoxes == null) {
			return true;
		}
		int n = Math.min(items.size(), checkBoxes.length);
		for (int i = 0; i < n; i++) {
			params.setItemSelection(items.get(i).getCode(), checkBoxes[i].isSelected());
		}
		return true;
	}

	@Override
	public void setParams() {
		LookupDataSelParams params = lookupDataSelector.getParams();
		includeUnassigned.setSelected(params.isUseUnassigned());
		Vector<LookupItem> items = lutList.getLutList();
		if (items == null || checkBoxes == null) {
			return;
		}
		int n = Math.min(items.size(), checkBoxes.length);
		for (int i = 0; i < n; i++) {
			boolean sel = params.getItemSelection(items.get(i).getCode());
			checkBoxes[i].setSelected(sel);
		}
		return;
	}


}
