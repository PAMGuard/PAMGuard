package PamguardMVC.superdet.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import PamView.tables.SortableTableValue;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.superdet.SuperDetection;

/**
 * A generic table of detection groups (click trains, CPOD trains, detection
 * groups, click events, ...) for use in dialogs and side panels. Shows one row
 * per group with standard timing and count columns and offers a right click
 * menu with "go to" functionality plus any extra commands supplied by the
 * owner.
 * <p>
 * This is the shared table component used by the click detector's event list
 * dialog for its per-data-block tabs and is intended for reuse by any module
 * which wants to list detection groups.
 *
 * @author Jamie Macaulay
 */
public class DetectionGroupTablePanel {

	/**
	 * Commands supplied by the owner of the table. All have default (do nothing /
	 * not available) implementations.
	 */
	public interface GroupTableCommands {
		/**
		 * Scroll displays to the given group.
		 * @param group group to go to.
		 * @param secsBefore number of seconds before the group start to scroll to.
		 */
		public void gotoGroup(SuperDetection group, int secsBefore);

		/**
		 * @return true if convertGroup should be offered in the popup menu.
		 */
		public default boolean canConvertGroup() {
			return false;
		}

		/**
		 * Copy and convert the given group into a manually annotated event.
		 * @param group group to convert.
		 */
		public default void convertGroup(SuperDetection group) {
		}

		/**
		 * @return true if localiseGroup should be offered in the popup menu.
		 */
		public default boolean canLocaliseGroup() {
			return false;
		}

		/**
		 * Open target motion localisation for the given group.
		 * @param group group to localise.
		 */
		public default void localiseGroup(SuperDetection group) {
		}
	}

	private PamDataBlock dataBlock;

	private GroupTableCommands commands;

	private PamPanel mainPanel;

	private JTable table;

	private GroupTableModel tableModel;

	private String[] colNames = {"Id", "UID", "Start Time", "End Time", "N Detections", "Info"};

	/**
	 * Sorter attached to the table so that columns can be sorted by clicking on
	 * their headers.
	 */
	private TableRowSorter<GroupTableModel> rowSorter;

	private ArrayList<SuperDetection> visibleData = new ArrayList<>();

	public DetectionGroupTablePanel(PamDataBlock dataBlock, GroupTableCommands commands) {
		this.dataBlock = dataBlock;
		if (commands == null && this instanceof GroupTableCommands) {
			// subclasses can implement GroupTableCommands themselves.
			commands = (GroupTableCommands) this;
		}
		this.commands = commands;

		tableModel = new GroupTableModel();
		table = new JTable(tableModel);
		rowSorter = new TableRowSorter<>(tableModel);
		for (int i = 0; i < colNames.length; i++) {
			rowSorter.setSortable(i, isColumnSortable(i));
		}
		table.setRowSorter(rowSorter);
		table.getTableHeader().setToolTipText("Click a column header to sort the table");
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new TableMouse());

		mainPanel = new PamPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(700, 300));
		mainPanel.add(BorderLayout.CENTER, scrollPane);

		dataBlock.addObserver(new DataObserver());

		updateTableData();
	}

	/**
	 * @return the panel containing the table.
	 */
	public PamPanel getPanel() {
		return mainPanel;
	}

	/**
	 * @return the data block displayed in the table.
	 */
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	/**
	 * Re-read the group list from the data block and repaint the table.
	 */
	public void updateTableData() {
		synchronized (dataBlock.getSynchLock()) {
			visibleData = new ArrayList<>();
			java.util.ListIterator<PamDataUnit> it = dataBlock.getListIterator(0);
			while (it.hasNext()) {
				PamDataUnit du = it.next();
				if (du instanceof SuperDetection) {
					visibleData.add((SuperDetection) du);
				}
			}
		}
		tableModel.fireTableDataChanged();
	}

	/**
	 * Get the currently selected group.
	 * @return selected group or null.
	 */
	public SuperDetection getSelectedGroup() {
		int row = table.getSelectedRow();
		if (row < 0) {
			return null;
		}
		return getGroup(table.convertRowIndexToModel(row));
	}

	/**
	 * Get the group in a given row of the table model. Note that this is the row
	 * in the model, not the row on the screen, which may be different if the
	 * table has been sorted.
	 * @param modelRow row index in the table model.
	 * @return group or null if the row index is out of range.
	 */
	public SuperDetection getGroup(int modelRow) {
		if (modelRow < 0 || modelRow >= visibleData.size()) {
			return null;
		}
		return visibleData.get(modelRow);
	}

	/**
	 * Is a column worth sorting on ? Columns of free text such as the Info
	 * column are not.
	 * @param column column index.
	 * @return true if the column can be sorted.
	 */
	public boolean isColumnSortable(int column) {
		return column != 5;
	}

	/**
	 * Get a short information string for a group for the Info column. Subclasses
	 * or owners can override for specific group types.
	 * @param group the group.
	 * @return info string, can be empty, not null.
	 */
	public String getGroupInfo(SuperDetection group) {
		return "";
	}

	/**
	 * Get the singular lower case type name for the groups in this table, taken
	 * from the data block name, e.g. "click train". Used in popup menu wording so
	 * that click trains are called click trains rather than events.
	 * @return type name for use in menus.
	 */
	public String getGroupTypeName() {
		String name = dataBlock.getDataName();
		if (name == null) {
			return "detection group";
		}
		if (name.endsWith("s")) {
			name = name.substring(0, name.length()-1);
		}
		return name.toLowerCase();
	}

	private class TableMouse extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				SuperDetection group = getSelectedGroup();
				if (group != null) {
					commands.gotoGroup(group, 0);
				}
			}
		}
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

	private void showPopupMenu(MouseEvent me) {
		int row = table.rowAtPoint(me.getPoint());
		if (row >= 0) {
			table.setRowSelectionInterval(row, row);
		}
		SuperDetection group = getSelectedGroup();
		if (group == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		long id = group.getDatabaseIndex() > 0 ? group.getDatabaseIndex() : group.getUID();
		String typeName = getGroupTypeName();
		int[] beforeTimes = {0, 10, 60};
		for (int i = 0; i < beforeTimes.length; i++) {
			String title;
			if (beforeTimes[i] == 0) {
				title = String.format("Goto %s %d ...", typeName, id);
			}
			else {
				title = String.format("Goto %ds before %s %d ...", beforeTimes[i], typeName, id);
			}
			menuItem = new JMenuItem(title);
			final int beforeTime = beforeTimes[i];
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					commands.gotoGroup(group, beforeTime);
				}
			});
			menu.add(menuItem);
		}
		if (commands.canLocaliseGroup()) {
			menuItem = new JMenuItem(String.format("Localise %s %d ...", typeName, id));
			menuItem.setToolTipText("Open the target motion localiser for this group");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					commands.localiseGroup(group);
				}
			});
			menu.add(menuItem);
		}
		if (commands.canConvertGroup()) {
			menu.addSeparator();
			menuItem = new JMenuItem("Convert to event ...");
			menuItem.setToolTipText("Copy the detections of this group into a new manually annotated event. "
					+ "The original group is not changed. Only detections currently loaded are copied.");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					commands.convertGroup(group);
				}
			});
			menu.add(menuItem);
		}
		menu.show(me.getComponent(), me.getX(), me.getY());
	}

	private class GroupTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return visibleData.size();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return Integer.class;
			case 1:
				return Long.class;
			case 2:
			case 3:
				return SortableTableValue.class;
			case 4:
				return Integer.class;
			}
			return super.getColumnClass(column);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= visibleData.size()) {
				return null;
			}
			SuperDetection group = visibleData.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return group.getDatabaseIndex();
			case 1:
				return group.getUID();
			case 2:
				return new SortableTableValue(PamCalendar.formatDBDateTime(group.getTimeMilliseconds()),
						group.getTimeMilliseconds());
			case 3:
				return new SortableTableValue(PamCalendar.formatDBDateTime(group.getEndTimeInMilliseconds()),
						group.getEndTimeInMilliseconds());
			case 4:
				return group.getSubDetectionsCount();
			case 5:
				return getGroupInfo(group);
			}
			return null;
		}
	}

	private class DataObserver extends PamObserverAdapter {
		@Override
		public String getObserverName() {
			return "Detection group table";
		}
		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			updateTableData();
		}
		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			updateTableData();
		}
	}

}
