package detectiongrouplocaliser.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import detectiongrouplocaliser.DetectionGroupControl;
import detectiongrouplocaliser.DetectionGroupDataBlock;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import detectiongrouplocaliser.DetectionGroupObserver;
import detectiongrouplocaliser.DetectionGroupProcess;
import detectiongrouplocaliser.GroupAnnotationHandler;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.PamScroller;
import userDisplay.UserDisplayComponentAdapter;
import warnings.PamWarning;

/**
 * Panel to show a table of data on all DetectionGroup data units. 
 * @author dg50
 *
 */
public class DetectionGroupTable extends UserDisplayComponentAdapter implements DetectionGroupObserver {

	private JPanel mainPanel;

	private DetectionGroupProcess detectionGroupProcess;

	private DetectionGroupDataBlock detectionGroupDataBlock;

	private DetectionGroupControl detectionGroupControl;

	private JTable table;

	private TableModel tableModel;

	private JScrollPane scrollPane;

	private JPanel controlPanel;

	private JRadioButton showAll, showCurrent;

	private JButton checkIntegrity;

	private boolean isViewer;

	private DisplayOptionsHandler displayOptionsHandler;

	private SwingTableColumnWidths widthManager;

	public DetectionGroupTable(DetectionGroupProcess detectionGroupProcess) {
		super();
		this.detectionGroupProcess = detectionGroupProcess;
		this.detectionGroupDataBlock = detectionGroupProcess.getDetectionGroupDataBlock();
		this.detectionGroupControl = detectionGroupProcess.getDetectionGroupControl();
		isViewer = detectionGroupControl.isViewer();
		mainPanel = new JPanel(new BorderLayout());
		tableModel = new TableModel();
		table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		detectionGroupControl.addGroupObserver(this);
		table.addMouseListener(new TableMouseHandler());

		if (isViewer) {
			displayOptionsHandler = detectionGroupControl.getDisplayOptionsHandler();
			controlPanel = new JPanel(new BorderLayout());
			mainPanel.add(BorderLayout.SOUTH, controlPanel);
			controlPanel.setBorder(new TitledBorder("Data Control"));
			JPanel lControlPanel = new JPanel(new GridBagLayout());
			controlPanel.add(BorderLayout.WEST, lControlPanel);
			GridBagConstraints c = new PamGridBagContraints();
			lControlPanel.add(showAll = displayOptionsHandler.createButton(DisplayOptionsHandler.SHOW_ALL, "Show all groups in database", false), c);
			c.gridx++;
			lControlPanel.add(showCurrent = displayOptionsHandler.createButton(DisplayOptionsHandler.SHOW_CURRENT, "Show only data currently loaded", false), c);
			c.gridx++;
			checkIntegrity = new JButton("Check Data Integrity");
			checkIntegrity.setToolTipText("Check and fix start and end times of events against the database data");
			lControlPanel.add(checkIntegrity, c);
			checkIntegrity.setVisible(detectionGroupControl.isViewer());
			showAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dataChanged();
				}
			});
			showCurrent.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dataChanged();
				}
			});
			ButtonGroup bg = new ButtonGroup();
			bg.add(showAll);
			bg.add(showCurrent);
			checkIntegrity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					detectionGroupProcess.checkDataIntegrity();
				}
			});
		}


		//		sortColumnWidths();
	}


	@Override
	public Component getComponent() {
		return mainPanel;
	}

	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponentAdapter#openComponent()
	 */
	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		super.openComponent();
	}


	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponentAdapter#closeComponent()
	 */
	@Override
	public void closeComponent() {
		detectionGroupControl.removeGroupObserveR(this);
		super.closeComponent();
	}

	private class TableMouseHandler extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				editSelectedEvent(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

	}

	@Override
	public void dataChanged() {
		tableModel.fireTableDataChanged();
	}

	public void editSelectedEvent(MouseEvent e) {
		// TODO Auto-generated method stub

	}


	public void editSelectedEvent() {
		DetectionGroupDataUnit dgdu = getSelectedDataRow();
		if (dgdu == null) return;
		editGroup(dgdu);
	}


	public void showPopupMenu(MouseEvent e) {
		DetectionGroupDataUnit dgdu = getSelectedDataRow();
		if (dgdu == null) return;
		PamSymbol menuIcon = new PamSymbol(detectionGroupControl.getSymbolforMenuItems(dgdu));
		JPopupMenu pMenu = new JPopupMenu("Detection Group UID " + dgdu.getUID());
		JMenuItem menuItem;
		menuItem = new JMenuItem("Delete Group UID " + dgdu.getUID(), menuIcon);
		pMenu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteGroup(dgdu, true);
			}
		});
		GroupAnnotationHandler annotationHandler = detectionGroupProcess.getAnnotationHandler();
		menuItem = annotationHandler.createAnnotationEditMenu(dgdu);
		menuItem.setIcon(menuIcon);
		pMenu.add(menuItem);

		int[] beforeTimesSecs = {0, 10, 60};
		pMenu.addSeparator();
		for (int i = 0; i < beforeTimesSecs.length; i++) {
			int before = beforeTimesSecs[i];
			String title;
			if (before == 0) {
				title = "Scroll to Group UID " + dgdu.getUID() + " at " + PamCalendar.formatDBDateTime(dgdu.getTimeMilliseconds());
			}
			else {
				title = String.format("Scroll to %ds before Group UID %d", before, dgdu.getUID());
			}
			menuItem = new JMenuItem(title);
			//		menuItem.setIcon(menuIcon);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					scrollToEvent(dgdu.getTimeMilliseconds()-before*1000);
				}
			});
			pMenu.add(menuItem);
		}

		pMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	protected void scrollToEvent(long timeMilliseconds) {
		// start a it earlier. 
		timeMilliseconds -= 5000;
		//		now WTF - how do I tell every scroller to go to this point in time ? 
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		scrollManager.startDataAt(detectionGroupDataBlock, timeMilliseconds);

	}


	protected void editGroup(DetectionGroupDataUnit dgdu) {
		detectionGroupProcess.editDetectionGroup(dgdu);
		dataChanged();
	}


	protected void deleteGroup(DetectionGroupDataUnit dgdu, boolean askFirst) {
		if (askFirst) {
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), "Delete Group Detection", 
					"Are you sure you want to permanently delete this group detection ?", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return;
			}
		}
		detectionGroupProcess.deleteDetectionGroup(dgdu);
		dataChanged();
	}


	/**
	 * 
	 * @return which data row is selected, or null. 
	 */
	private DetectionGroupDataUnit getSelectedDataRow() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return null;
		}
		return (DetectionGroupDataUnit) detectionGroupDataBlock.getDataUnit(row, PamDataBlock.REFERENCE_ABSOLUTE);
	}

	@Override
	public void modelChanged() {
		widthManager.getColumnWidths();
		tableModel.fireTableStructureChanged();
		widthManager.setColumnWidths();
	}

	private void sortColumnWidths() {
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableCol = table.getColumnModel().getColumn(i);
			tableCol.setPreferredWidth(tableModel.getRelativeWidth(i)*50);
		}

	}

	private int getViewOption() {
		if (!isViewer) {
			return DisplayOptionsHandler.SHOW_ALL;
		}
		else if (showCurrent != null) {
			if (showCurrent.isSelected()) {
				return displayOptionsHandler.SHOW_CURRENT;
			}
		}
		return DisplayOptionsHandler.SHOW_ALL;
	}

	private class TableModel extends AbstractTableModel {

		private GroupAnnotationHandler annotationHandler;
		private List<DataAnnotationType<?>> usedAnnotations;
		private ArrayList<String> columnNames = new ArrayList<>();
		private int nBaseColumns = 4;
		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE,48,12,true,Color.BLACK,Color.BLUE);
		private int firstRowToShow;
		private int numRowsToShow;

		public TableModel() {
			super();
			fireTableStructureChanged();
		}

		@Override
		public int getColumnCount() {
			if (columnNames == null) {
				return 0;
			}
			return columnNames.size();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return columnNames.get(column);
		}

		@Override
		public int getRowCount() {
			//			System.out.println("getRowCount()");
			if (getViewOption() ==  DisplayOptionsHandler.SHOW_ALL) {
				firstRowToShow = 0;
				return numRowsToShow = detectionGroupDataBlock.getUnitsCount();
			}
			else {
				// work through datablock and work out the first and n indexes to show. 
				firstRowToShow = -1;
				int lastRowToShow = -1;
				int i = 0;
				synchronized (detectionGroupDataBlock.getSynchLock()) {
					ListIterator it = detectionGroupDataBlock.getListIterator(0);
					while (it.hasNext()) {
						DetectionGroupDataUnit nextData = (DetectionGroupDataUnit) it.next();
						if (nextData.getSubDetectionsCount() > 0) {
							if (firstRowToShow < 0) {
								firstRowToShow = i;
							}
							lastRowToShow = i+1;
						}
						i++;
					}
				}
				return lastRowToShow - firstRowToShow;
			}
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			try {
				DetectionGroupDataUnit dgdu = (DetectionGroupDataUnit) detectionGroupDataBlock.getDataUnit(iRow+firstRowToShow, PamDataBlock.REFERENCE_ABSOLUTE);
				if (dgdu == null) {
					return null;
				}
				switch (iCol) {
				case 0:
					Color col = PamColors.getInstance().getWhaleColor((int) dgdu.getUID());
					symbol.setFillColor(col);
					symbol.setLineColor(col);
					return symbol;
				case 1:
					if (dgdu.getUpdateCount() > 0) {
						return "*"+dgdu.getUpdateCount();
					}
					else {
						return null;
					}
				case 2:
					//					if (iRow == 0) {
					//						System.out.println("getValueAt(0,0)");
					//					}
					return dgdu.getUID();
				case 3:
					return PamCalendar.formatDateTime(dgdu.getTimeMilliseconds());
				case 4:
					return PamCalendar.formatDateTime(dgdu.getEndTimeInMilliseconds());
				case 5:
					return String.format("%d (%d)", dgdu.getSubDetectionsCount(), dgdu.getSubDetectionsCount());
				default:
					int iAnnot = iCol - nBaseColumns;
					if (usedAnnotations.size() <= iAnnot) {
						return null;
					}
					DataAnnotationType ann = usedAnnotations.get(iAnnot);
					DataAnnotation annotation = dgdu.findDataAnnotation(ann.getAnnotationClass());
					if (annotation != null) {
						return annotation.toString();
					}

				}

			}
			catch (Exception e) {
				return null;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return ImageIcon.class;
			}
			return super.getColumnClass(col);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#fireTableStructureChanged()
		 */
		@Override
		public void fireTableStructureChanged() {
			columnNames.clear();
			columnNames.add("");
			columnNames.add("*");
			columnNames.add("UID");
			columnNames.add("Start Time");
			columnNames.add("End Time");
			columnNames.add("N Items");
			nBaseColumns = columnNames.size();
			// count columns in each annotation ...
			annotationHandler = detectionGroupProcess.getAnnotationHandler();
			usedAnnotations = annotationHandler.getUsedAnnotationTypes();
			for (DataAnnotationType annotation:usedAnnotations) {
				columnNames.add(annotation.getAnnotationName());
			}
			super.fireTableStructureChanged();
		}


		/**
		 * Attempt to set column widths. 
		 * @param colIndex
		 * @return relative column widths.
		 */
		public int getRelativeWidth(int colIndex) {
			switch(colIndex) {
			case 0:
				return 1;
			case 1:
				return 1;
			case 2: 
				return 5;
			case 3:
				return 5;
			default:
				return 10;
			}
		}
	}

	/* (non-Javadoc)
	 * @see userDisplay.UserDisplayComponentAdapter#setUniqueName(java.lang.String)
	 */
	@Override
	public void setUniqueName(String uniqueName) {
		widthManager = new SwingTableColumnWidths(uniqueName, table);
		super.setUniqueName(uniqueName);
	}



}
