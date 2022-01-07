package generalDatabase.external;

import generalDatabase.DBControl;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.SQLTypes;
import generalDatabase.external.crossreference.CrossReferenceStatus;
import generalDatabase.external.crossreference.CrossReferenceStatusMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.CancelObserver;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class CopyDialog extends PamDialog implements CancelObserver {

	private static CopyDialog copyDialog;
	private CopyTypes copyDirection;
	private DBControl externalDatabase;
	private CopyManager copyManager;
	private DBControl sourceDatabase, destDatabase;
	private List<TableInformation> tableInfo;

//	private JCheckBox[] selectTables;
//	private InfoButton[] tableStatus;
	private JLabel[] tableSRCRecords;
	private JLabel[] tableDSTRecords;
	private JLabel[] tableNEWRecords;
	private JLabel infoLabel, currentTable;
	private JProgressBar tableProgress, totalProgress;
	private JComboBox<ImportOption>[] importOptions;

	private Object infoSynch = new Object();
	private boolean initialChecksComplete = false;
	
	JButton startButton;
	public boolean carryOn;
	
	private CopyStatus generalStatus = CopyStatus.NOTSTARTED;
//	private JButton allButton, fixButton;
	
	private JCheckBox copySettings;
	private int nTables;

	private CopyDialog(Window parentFrame, CopyManager copyManager, DBControl externalDatabase, CopyTypes copyDirection, String title) {
		super(parentFrame, title, false);
		this.copyManager = copyManager;
		this.externalDatabase = externalDatabase;
		this.copyDirection = copyDirection;
		tableInfo = new ArrayList<>();
		this.setCancelObserver(this);

		if (copyDirection == CopyTypes.EXPORT) {
			sourceDatabase = copyManager.getPamguardDatabase();
			destDatabase = externalDatabase;
		}
		else {
			sourceDatabase = externalDatabase;
			destDatabase = copyManager.getPamguardDatabase();
		}

//		System.out.println("Move data from " + sourceDatabase.getDatabaseName() + " to " + destDatabase.getDatabaseName());
		tableInfo = copyManager.getTableInfo(sourceDatabase, destDatabase);
		nTables = tableInfo.size();

		JPanel mainPanel = new JPanel(new BorderLayout());
		startButton = new JButton(copyDirection.getName());

		JPanel tablePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		if (tableInfo == null || tableInfo.size() == 0) {
			tablePanel.add(new JLabel("Database contains no tables to " + copyDirection.getName()));
		}
		else {
//			selectTables = new JCheckBox[tableInfo.size()];
//			tableStatus = new InfoButton[tableInfo.size()];
			tableSRCRecords = new JLabel[tableInfo.size()];
			tableDSTRecords = new JLabel[tableInfo.size()];
			tableNEWRecords = new JLabel[tableInfo.size()];
			importOptions = new JComboBox[tableInfo.size()];
//			allButton = new JButton("Select All");
//			allButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					allButtonClicked();
//				}
//			});
			c.fill = GridBagConstraints.NONE;
//			tablePanel.add(allButton, c);
			c.fill = GridBagConstraints.HORIZONTAL;
			tablePanel.add(new JLabel("  Table", JLabel.RIGHT), c);
			c.gridx++;
			JLabel lab;
			tablePanel.add(lab = new JLabel("  Source", JLabel.RIGHT), c);
			lab.setToolTipText("Number of records in source database " );
			c.gridx++;
			tablePanel.add(lab = new JLabel(" Dest'", JLabel.RIGHT), c);
			lab.setToolTipText("Number of records in destination database ");
			c.gridx++;
			tablePanel.add(lab = new JLabel(" New", JLabel.RIGHT), c);
			lab.setToolTipText("Number of new records ");
			c.gridx++;
			tablePanel.add(lab = new JLabel(" Action ", JLabel.CENTER), c);
			c.gridx++;
//			tablePanel.add(fixButton = new JButton("Create"), c);
//			fixButton.setToolTipText("Create missing database tables");
//			fixButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					fixButton();
//				}
//			});
			
			c.gridy++;
			int i = 0;
			for (TableInformation tableDef:tableInfo) {
				c.gridx = 0;
//				selectTables[i] = new JCheckBox(tableDef.getTableName());
//				selectTables[i].setSelected(false);
//				selectTables[i].addActionListener(new SelectTable(i));
//				tableStatus[i] = new InfoButton(i, "Unknown");
				
				tableSRCRecords[i] = new JLabel("   ?   ", JLabel.RIGHT);
				tableSRCRecords[i].setToolTipText("Number of records in source table " );
				tableDSTRecords[i] = new JLabel("   ?   ", JLabel.RIGHT);
				tableDSTRecords[i].setToolTipText("Number of records in destination table ");
				tableNEWRecords[i] = new JLabel("   ?   ", JLabel.RIGHT);
				tableNEWRecords[i].setToolTipText("Number of new records " );
				
				
				tablePanel.add(new JLabel(tableDef.getTableName(), JLabel.RIGHT), c);
				c.gridx++;
				
				tablePanel.add(tableSRCRecords[i], c);
				c.gridx++;
				tablePanel.add(tableDSTRecords[i], c);
				c.gridx++;
				tablePanel.add(tableNEWRecords[i], c);
				c.gridx++;
				
//				tablePanel.add(tableStatus[i], c);
//				c.gridx++;
				
				importOptions[i] = new JComboBox<ImportOption>();
				importOptions[i].addItem(ImportOption.DONOTHING);
				tablePanel.add(importOptions[i], c);
				importOptions[i].addActionListener(new ImportOptionListener(i));

				c.gridy++;
				i++;
			}
		}
		JScrollPane tableScroll = new JScrollPane(tablePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tableScroll.setBorder(new TitledBorder("Tables"));
		Dimension pSize = tablePanel.getPreferredSize();
		if (pSize.height > 500) {
			pSize.height = 500;
			pSize.width += 60;
			tableScroll.setPreferredSize(pSize);
		}
		tableScroll.setMaximumSize(new Dimension(0, 500));
		mainPanel.add(tableScroll, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		mainPanel.add(controlPanel, BorderLayout.SOUTH);

		JPanel settingsPanel = new JPanel(new BorderLayout());
		settingsPanel.setBorder(new TitledBorder("PAMGuard Configuration"));
		settingsPanel.add(BorderLayout.CENTER, copySettings = new JCheckBox("Load imported PAMGuard configuration"));
		if (copyDirection == CopyTypes.IMPORT) {
			copySettings.setSelected(false);
			controlPanel.add(settingsPanel);
		}
		
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new TitledBorder("Status Information"));
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.add(currentTable = new JLabel("   "));
		infoPanel.add(infoLabel = new JLabel("   "));
		infoPanel.add(tableProgress = new JProgressBar());
		infoPanel.add(totalProgress = new JProgressBar());
		controlPanel.add(infoPanel);
		
		getButtonPanel().add(startButton, 0);
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startButton();
			}
		});
		getOkButton().setVisible(false);

		setResizable(true);
		setDialogComponent(mainPanel);
		
		setHelpPoint("utilities.generalDatabaseHelp.docs.import_export");

	}

	/*
	 * Only fix missing tables, don't delete existing data - this must be
	 * done manually. 
	 */
	protected void fixButton() {
		copyManager.createTables(destDatabase, tableInfo, new FirstListInfoMonitor());
	}

	/**
	 * Start button has been pressed. Get on with copying the data. 
	 */
	protected void startButton() {
		// final check of which tables to copy
		for (int i = 0; i < nTables; i++) {
			tableInfo.get(i).setCopyChoice((ImportOption) importOptions[i].getSelectedItem());
		}
		carryOn = true;
		generalStatus = CopyStatus.COPYING;
		copyManager.copyDataW(sourceDatabase, destDatabase, tableInfo, new CopyingInfoMonitor());
		enableEverything();
	}
	
	// called when the cancel button is pressed. 
	protected void stopButton() {
		carryOn = false;
	}

//	protected void allButtonClicked() {
//		for (int i = 0; i < nTables; i++) {
//			selectTables[i].setSelected(true);
//		}
//		enableStart();
//	}

	public static void showDialog(Window parentFrame, CopyManager copyManager, DBControl externalDatabase, CopyTypes copyDirection) {
		String title = null;
		switch (copyDirection) {
		case EXPORT:
			title = "Export to " + externalDatabase.getDatabaseName();
			break;
		case IMPORT:
			title = "Import from " + externalDatabase.getDatabaseName();
			break;
		}
//		if (copyDialog == null || copyDialog.getOwner() != parentFrame) {
			copyDialog = new CopyDialog(parentFrame, copyManager, externalDatabase, copyDirection, title);
//		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				copyDialog.getTableInformation();
			}

		});

		copyDialog.setVisible(true);
	}

	private class InfoButton extends JButton implements ActionListener
	{

		private Color[] warningColours = null;
		private int tableIndex;

//		public InfoLabel(int tableIndex, String text, int horizontalAlignment) {
//			super(text, horizontalAlignment);
//			this.tableIndex = tableIndex;
//		}

		public InfoButton(int tableIndex, String text) {
			super(text);
			this.tableIndex = tableIndex;
			setEnabled(false);
			addActionListener(this);
		}

		private void makeWarningColours() {
			if (warningColours == null) {
				warningColours = new Color[3];
				warningColours[0] = getBackground();
				warningColours[1] = Color.CYAN;
				warningColours[2] = Color.ORANGE;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			return super.getToolTipText();
		}

		public void setWarningLevel(int warnLevel) {
			makeWarningColours();
			warnLevel = Math.min(warnLevel, 2);
			setBackground(warningColours[warnLevel]);
			if (warnLevel > 0) {
//				selectTables[tableIndex].setSelected(false);
			}
			switch (warnLevel) {
			case 0:
				setText("Table OK");
				break;
			case 1:
				setText("Warnings");
				break;
			case 2:
				setText("Errors");
				break;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// called to try to fix the problem ....
			JPopupMenu actionMenu = new JPopupMenu();
			JMenuItem menuItem;
			TableInformation tableInf = tableInfo.get(tableIndex);
			if (tableInfo == null) {
				System.out.printf("Null table info at table index %d\n", tableIndex);
				return;
			}
			if (tableInf.getDestTableExists() == false) {
				menuItem = new JMenuItem("Create Table");
				menuItem.setToolTipText("Create table in output database");
				menuItem.addActionListener(new DropAndReplace(tableIndex));
				actionMenu.add(menuItem);
			}
			else {
				menuItem = new JMenuItem("Drop and replace");
				menuItem.setToolTipText("Delete and replace the table and it's contents");
				menuItem.addActionListener(new DropAndReplace(tableIndex));
				actionMenu.add(menuItem);
			}
			
			actionMenu.show(this, getWidth()/2, getHeight()/2);
		}

	}
	
//	private class SelectTable implements ActionListener {
//
//		private int tableIndex;
//		
//		/**
//		 * @param tableIndex
//		 */
//		public SelectTable(int tableIndex) {
//			super();
//			this.tableIndex = tableIndex;
//		}
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			selectTable(tableInfo.get(tableIndex));
//		}
//		
//	}
	
	private class DropAndReplace implements ActionListener {

		private int tableIndex;

		public DropAndReplace(int tableIndex) {
			this.tableIndex = tableIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dropAndReplace(tableIndex);
		}
		
	}
	
	/**
	 * Gather information about the individual tables, e.g. how much 
	 * data in source and output tables. 
	 */
	private void getTableInformation() {
		totalProgress.setMaximum(tableInfo.size());
		tableProgress.setIndeterminate(true);
		copyManager.getTableInformationW(tableInfo, sourceDatabase, destDatabase, new FirstListInfoMonitor());
	}

//	/**
//	 * Table check box actioned ...
//	 * @param tableInformation
//	 */
//	public void selectTable(TableInformation tableInformation) {
//		boolean sel = selectTables[tableInformation.getTableIndex()].isSelected();
//		if (sel && tableInformation.getWarningLevel() > 0) {
//			// need to rerun table checks for this table which may not even exist. 
//			copyManager.autoFixTable(destDatabase, tableInformation, new OneTableInfoMonitor());
//		}
//		enableStart();
//	}

	/*
	 * Drop and replace a table. 
	 */
	public void dropAndReplace(int tableIndex) {
		copyManager.dropAndReplace(sourceDatabase, destDatabase, 
				tableInfo.get(tableIndex), new OneTableInfoMonitor());
	}

	private class FirstListInfoMonitor implements TableInformationMonitor {		
		@Override
		public void setTableInformation(TableInformation tableInformation) {
			if (tableInformation == null) {
				// only ever gets a null once at the end of the initial checking. 
				tableProgress.setIndeterminate(false);
				totalProgress.setValue(0);
				currentTable.setText("Table analysis complete");
				initialChecksComplete = true;
				generalStatus = CopyStatus.NOTSTARTED;
				return;
			}
			totalProgress.setValue(tableInformation.getTableIndex()+1);			
			sayTableInfo(tableInformation);
			enableEverything();
			
			enableStart();
			
			pack();
		}

		@Override
		public boolean carryOn() {
			return carryOn;
		}
	}
	private class OneTableInfoMonitor implements TableInformationMonitor {		
		@Override
		public void setTableInformation(TableInformation tableInformation) {
			if (tableInformation == null) {
				// only ever gets a null once at the end of the initial checking. 
				tableProgress.setIndeterminate(false);
				totalProgress.setValue(0);
				currentTable.setText("Table analysis complete");
				initialChecksComplete = true;
				generalStatus = CopyStatus.NOTSTARTED;
				return;
			}
//			totalProgress.setValue(tableInformation.getTableIndex()+1);			
			sayTableInfo(tableInformation);
			enableEverything();
			
			enableStart();
		}

		@Override
		public boolean carryOn() {
			return carryOn;
		}
	}
	private class CopyingInfoMonitor implements TableInformationMonitor {		
		@Override
		public void setTableInformation(TableInformation tableInformation) {
			if (tableInformation == null) {
				// only ever gets a null once at the end of the initial checking. 
				tableProgress.setIndeterminate(false);
				totalProgress.setValue(0);
				currentTable.setText("Data copy complete");
				carryOn = false;
				generalStatus = CopyStatus.DONE;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						copyDone();
					}
				});
				return;
			}
			if (tableInformation.getSourceRecords() > 0) {
				int percComp = 100*tableInformation.getRowsCopied() / tableInformation.getSourceRecords();
				tableProgress.setValue(percComp);
			}
			else {
				tableProgress.setValue(100);
			}
			totalProgress.setValue(tableInformation.getTableIndex()+1);			
			sayTableInfo(tableInformation);
			
			enableStart();
		}

		@Override
		public boolean carryOn() {
			return carryOn;
		}
	}
	private class CrossReferenceMonitor implements CrossReferenceStatusMonitor {

		@Override
		public void update(CrossReferenceStatus crossReferenceStatus) {
			if (crossReferenceStatus == null) {
				return;
			}
			infoLabel.setText("Updating cross references for " + copyDirection.getName() + "ed data");
			currentTable.setText(crossReferenceStatus.getMessage());
			totalProgress.setMaximum(crossReferenceStatus.getnTables());
			totalProgress.setValue(crossReferenceStatus.getiTable());
			tableProgress.setMaximum(crossReferenceStatus.getnRecs());
			tableProgress.setValue(crossReferenceStatus.getiRec());
		}
		
	}

	private class ImportOptionListener implements ActionListener {

		private int tableIndex;
		
		public ImportOptionListener(int tableIndex) {
			super();
			this.tableIndex = tableIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			enableEverything();
		}
		
	}
	private void enableEverything() {
		enableStart();
		enableCancel();
		enableTables();
//		enableFix();
	}
	
//	private void enableFix() {
//		int nMissing = 0;
//		if (tableInfo == null) {
//			return;
//		}
//		for (TableInformation ti:tableInfo) {
//			if (ti.getDestTableExists() == false) {
//				nMissing++;
//			}
//		}
//		fixButton.setEnabled(nMissing > 0);
//	}

	/**
	 * Called when the main copy tasks have completed. 
	 */
	public void copyDone() {
		if (copySettings.isSelected()) {
			copyManager.loadImportedSettings(sourceDatabase);
		}
		copyManager.checkCrossReferencing(destDatabase, new CrossReferenceMonitor());
		PamController.getInstance().notifyModelChanged(PamController.INITIALIZATION_COMPLETE);
		enableEverything();
	}


	/**
	 * Work out if it's safe to enable the start button. 
	 */
	private void enableStart() {
		boolean ok = true;
		int nSel = 0;
		for (int i = 0; i < nTables; i++) {
			if (importOptions[i].getSelectedItem() == ImportOption.DONOTHING) {
				continue;
			}
			nSel++;
			TableInformation tableInf = tableInfo.get(i);
//			if (tableInf.getWarningLevel()> 0) {
//				ok = false;
//			}
		}
		if (nSel == 0) ok = false;
		if (generalStatus != CopyStatus.NOTSTARTED) {
			ok = false;
		}
		startButton.setEnabled(ok);
//		allButton.setEnabled(generalStatus != CopyStatus.COPYING);
		if (generalStatus == CopyStatus.DONE) {
			getCancelButton().setText("Close");
		}
	}
	
	private void enableCancel() {
//		switch (generalStatus) {
//		
//		}
		getCancelButton().setEnabled(true);
	}
	
	private void enableTables() {
		for (int i = 0; i < nTables; i++) {
			TableInformation ti = tableInfo.get(i);
			boolean enable = true;
			enable = (generalStatus != CopyStatus.COPYING);
			importOptions[i].setEnabled(enable);
			
//			tableStatus[i].setEnabled(enable & ti.getWarningLevel() > 0);
			
		}
	}
	
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	public void sayTableInfo(TableInformation tableInformation) {
		if (tableInformation == null) {
			return;
		}
		switch (tableInformation.getCopyStatus()) {
		case COPYING:
			String txt = String.format("Table %s %d copies / sec", tableInformation.getTableName(),
					(int) (tableInformation.getCopyRate()+.5));
			currentTable.setText(txt);
			infoLabel.setText(tableInformation.getCurrentAction());
			break;
		default:
			currentTable.setText(tableInformation.getSourceTableDef().getTableName());
			infoLabel.setText(tableInformation.getCurrentAction());
		}

		int warnLevel = tableInformation.getWarningLevel();
		int tableIndex = tableInformation.getTableIndex();
//		tableStatus[tableIndex].setWarningLevel(warnLevel);
//		if (warnLevel == 0) {
//			String str = String.format("Destination table OK. %d records to %s", tableInformation.getSourceRecords(),
//					copyDirection.getName());
//			tableStatus[tableIndex].setToolTipText(str);
//		}
//		else {
//			tableStatus[tableIndex].setToolTipText(tableInformation.getWarnings());
//		}
		
		Integer nRec = tableInformation.getSourceRecords();
		if (nRec == null) {
			tableSRCRecords[tableIndex].setText("  ?  ");
		}
		else {
			tableSRCRecords[tableIndex].setText(nRec.toString());
		}

		int nNew = tableInformation.getNewRecords();
		Integer nDst = tableInformation.getDestRecords();
		if (nDst == null) {
			tableDSTRecords[tableIndex].setText("  ?  ");
		}
		else {
			tableDSTRecords[tableIndex].setText(nDst.toString());
			if (tableInformation.getDestTableDef().utcMax != null) {
				String tip = String.format("New records coming after the last current record at %s",
						PamCalendar.formatDBDateTime(tableInformation.getDestTableDef().utcMax, true));
				tableNEWRecords[tableIndex].setToolTipText(tip);
			}
			tableNEWRecords[tableIndex].setText(Integer.valueOf(nNew).toString());
//			importOptions[tableIndex].addItem(ImportOption.MERGERECORDS);
		}

		if (nNew > 0) {
			if (tableInformation.getDestTableExists() == false) {
				addImportOption(tableIndex, ImportOption.NEWTABLE);
			}
			addImportOption(tableIndex, ImportOption.DROPANDCOPY);
			if (nDst != null) {
				addImportOption(tableIndex, ImportOption.MERGERECORDS);
			}
		}

	}
	
	private void addImportOption(int tableIndex, ImportOption option) {
		// need to check it doesn't exist and also consider selecting it.
		JComboBox<ImportOption> jcb = importOptions[tableIndex];
		for (int i = 0; i < jcb.getItemCount(); i++) {
			if (jcb.getItemAt(i) == option) {
				return;
			}
		}
		jcb.addItem(option);
	}
	
	

	@Override
	public boolean cancelPressed() {
		boolean wasOn = carryOn;
		if (wasOn) {
			int ans = JOptionPane.showConfirmDialog(this, "Do you really want to stop copying data ?", 
					"Database " + copyDirection.getName(), JOptionPane.YES_NO_OPTION);
			if (ans == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		carryOn = false;
		return !wasOn;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelButtonPressed() {		
	}


}
