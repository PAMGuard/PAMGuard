package PamController.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

//import org.hsqldb.TableDerived;

import PamController.PamControlledUnitSettings;
import PamView.dialog.PamDialog;

public class SettingsImportDialog extends PamDialog {

	private ArrayList<SettingsImportGroup> groupedSettings;

	private JTable moduleTable;

	private JLabel warningLabel;

	private ModuleModel tableModel;

	private String[] colNames = {"Module Type", "Module Name", "Import Options"};

	private JComboBox<ImportChoice> choiceBoxes;

	private boolean returnVal = true;

	public SettingsImportDialog(Window parentFrame,	String psfName, ArrayList<SettingsImportGroup> groupedSettings) {
		super(parentFrame, "Import from " + psfName, false);
		this.groupedSettings = groupedSettings;
		tableModel = new ModuleModel();
		moduleTable = new JTable(tableModel);
		moduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		choiceBoxes = new JComboBox<ImportChoice>();

		TableColumn choiceColumn = moduleTable.getColumnModel().getColumn(2);
		choiceColumn.setCellEditor(new DefaultCellEditor(choiceBoxes));
		choiceBoxes.addComponentListener(new ChoiceBoxListener());
		choiceBoxes.addActionListener(new CheckChoiceListener());

		moduleTable.getSelectionModel().addListSelectionListener(new TableRowSelector());

		int rh = moduleTable.getRowHeight();
		int oh = choiceBoxes.getPreferredSize().height;
		moduleTable.setRowHeight(Math.max(rh+4,oh));
		moduleTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		moduleTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		moduleTable.getColumnModel().getColumn(2).setPreferredWidth(200);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Module import selection"));
		JScrollPane scrollPanel = new JScrollPane(moduleTable);
		mainPanel.add(BorderLayout.CENTER, scrollPanel);
		mainPanel.setPreferredSize(new Dimension(600,  250));
		JPanel topBorder = new JPanel(new BorderLayout());
		warningLabel = new JLabel();
		topBorder.add(BorderLayout.CENTER, warningLabel);
		mainPanel.add(BorderLayout.NORTH, topBorder);

		String warn = null;
		if (groupedSettings == null || groupedSettings.size() == 0) {
			warningLabel.setText("Hover mouse to see important warning message ...");
			warn = "<html>The format of older psf files is not supported by the new import functions.<p>"
					+ "If you cannot see any modules listed below you should open and then close the <br>"
					+ "configuration you want to import from with the latest version of PAMGuard and<br>"
					+ "try again.</html>";
			warningLabel.setText(warn);
			topBorder.setBorder(new EmptyBorder(5, 5, 5, 5));
			topBorder.setBackground(Color.red);
			getOkButton().setEnabled(false);
		}
		else {
			warn = "<html>Select the modules you wish to import and whether you want to add them<p>"
					+ "as new modules or replace existing modules of the same type</html>";
			topBorder.setBorder(new EmptyBorder(2,2,2,2));
			warningLabel.setText(warn);
		}
		moduleTable.setToolTipText(warn);
//		warningLabel.setToolTipText(warn);

		setHelpPoint("overview.PamMasterHelp.docs.ImportingModules");
		setDialogComponent(mainPanel);
		setResizable(true);
	}

	public static boolean showDialog(Window parentFrame, String psfName, ArrayList<SettingsImportGroup> groupedSettings) {
		SettingsImportDialog importDialog = new SettingsImportDialog(parentFrame, psfName, groupedSettings);
		importDialog.returnVal = true;
		importDialog.setVisible(true);
		return importDialog.returnVal;
	}

	private class CheckChoiceListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int iRow = moduleTable.getSelectedRow();
			if (iRow < 0) return;
			groupedSettings.get(iRow).setImportChoice((ImportChoice) choiceBoxes.getSelectedItem());
		}

	}
	private class TableRowSelector implements ListSelectionListener {

		private int previousRow;

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// first save the previous value ...
			if (previousRow >= 0) {
				groupedSettings.get(previousRow).setImportChoice((ImportChoice) choiceBoxes.getSelectedItem());
			}

			int iRow = moduleTable.getSelectedRow();
			if (iRow < 0) return;
			SettingsImportGroup aSet = groupedSettings.get(iRow);
			choiceBoxes.removeAllItems();
			for (int j = 0; j < aSet.getImportChoices().size(); j++) {
				choiceBoxes.addItem(aSet.getImportChoices().get(j));
			}
			choiceBoxes.setSelectedItem(aSet.getImportChoice());
			previousRow = iRow;
		}

	}
	private class ChoiceBoxListener extends ComponentAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
		 */
		@Override
		public void componentShown(ComponentEvent e) {
			int iRow = moduleTable.getSelectedRow();
			if (iRow < 0) return;
			SettingsImportGroup aSet = groupedSettings.get(iRow);
			choiceBoxes.removeAllItems();
			for (int j = 0; j < aSet.getImportChoices().size(); j++) {
				choiceBoxes.addItem(aSet.getImportChoices().get(j));
			}
			choiceBoxes.setSelectedItem(aSet.getImportChoice());

			//			super.componentShown(e);
		}

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		returnVal = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	private class ModuleModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return groupedSettings.size();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SettingsImportGroup set = groupedSettings.get(rowIndex);
			PamControlledUnitSettings mainSet = set.getMainSettings();
			switch (columnIndex) {
			case 0:
				return mainSet.getUnitType();
			case 1:
				return mainSet.getUnitName();
			case 2:
				//				return choiceBoxes[rowIndex].getSelectedItem().toString();
				return set.getImportChoice().toString();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 2) {
				return true;
			}
			return super.isCellEditable(rowIndex, columnIndex);
		}

		//		/* (non-Javadoc)
		//		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		//		 */
		//		@Override
		//		public Class<?> getColumnClass(int columnIndex) {
		//			if (columnIndex == 2) {
		//				return JComboBox.class;
		//			}
		//			else {
		//				return super.getColumnClass(columnIndex);
		//			}
		//		}

	}
}
