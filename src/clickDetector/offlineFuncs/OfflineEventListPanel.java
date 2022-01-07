package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupChangeListener;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

/**
 * List of offline events which can be included in various dialogs 
 * associated with offline event creation and management. 
 * @author Doug Gillespie
 *
 */
public class OfflineEventListPanel {

	private PamPanel mainPanel;

	private PamPanel selectionPanel;

	private ClickControl clickControl;

	private OfflineEventDataBlock offlineEventDataBlock;

	private String[] colNames = {"", "Id", "Start Time", "End Time", "Type", "N Clicks",
			"Min", "Best", "Max", "Comment"};

	private JTable eventTable;

	private EventTableModel eventTableModel;

	private JRadioButton showAll, showCurrent, showFuture;

	/**
	 * Set whether table is enabled or disabled. 
	 */
	private boolean enabled=true; 

	/**
	 * show all events
	 */
	public static final int SHOW_ALL = 1;
	/**
	 * Show only those events which overlap the current loaded click data. 
	 */
	public static final int SHOW_SELECTION = 2;
	
	/**
	 * Show the current time period and all later events
	 * (good for scrolling data, so current event is shown + all future ones)
	 */
	public static final int SHOW_FUTURE = 3;

	private LookupList lutList;
	private LookupComponent lutComponent;

	private ArrayList<OfflineEventDataUnit> visibleData = new ArrayList<>();

	public OfflineEventListPanel(ClickControl clickControl) {

		this.clickControl = clickControl;
		makeSelectionPanel();
		makeMainPanel();
	}

	private void makeSelectionPanel() {
		selectionPanel = new PamPanel();
		selectionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		selectionPanel.add(showAll = new JRadioButton("Show all  "), c);
		c.gridy++;
		selectionPanel.add(showCurrent = new JRadioButton("Show current time period   "), c);
		c.gridy++;
		selectionPanel.add(showFuture = new JRadioButton("Show from current time   "), c);
		
		showAll.addActionListener(new ShowSelectionChanged());
		showCurrent.addActionListener(new ShowSelectionChanged());
		showFuture.addActionListener(new ShowSelectionChanged());
		ButtonGroup bg = new ButtonGroup();
		bg.add(showAll);
		bg.add(showCurrent);	
		bg.add(showFuture);
		showAll.setToolTipText("Show events for the entire period of the full data set");
		showCurrent.setToolTipText("Show events overlapping in time with currently loaded click data");
		showFuture.setToolTipText("Show events from the start of the currently loaded click data to the end of the data set");

		lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
		lutComponent = new LookupComponent(ClicksOffline.ClickTypeLookupName, lutList, true);
		lutComponent.setAllowEdits(false);
		lutComponent.setAllowNullSelection(true);
		lutComponent.setShowCodePanel(false);
		lutComponent.setNullSelectionText("Show all event types");
		LookUpTables.getLookUpTables().addUpdatableComponent(lutComponent);
		c.gridx = 1;
		c.gridy = 0;
		selectionPanel.add(new JLabel("Event type selection ..."), c);
		c.gridy++;
		selectionPanel.add(lutComponent.getComponent(), c);
		lutComponent.addChangeListener(new LookupChangeListener() {
			@Override
			public void lookupChange(LookupItem selectedItem) {
				tableDataChanged();
			}
		});
	}

	public int getShowSelection() {
		if (showAll.isSelected()) {
			return SHOW_ALL;
		}
		else if (showCurrent.isSelected()) {
			return SHOW_SELECTION;
		}
		else if (showFuture.isSelected()) {
			return SHOW_FUTURE;
		}
		return SHOW_SELECTION;
	}

	public void setShowSelection(int showSelection) {
		showAll.setSelected(showSelection == SHOW_ALL);
		showCurrent.setSelected(showSelection == SHOW_SELECTION);
		showFuture.setSelected(showSelection == SHOW_FUTURE);
	}

	/**
	 * Called when the show selection has changed. Wit more complex data selection
	 * this now just copies all selected data into an array list. 
	 */
	public void selectData() {
		int sel = getShowSelection();
		LookupItem spp = lutComponent.getSelectedItem();
		OfflineEventDataUnit currentEvent = getSelectedEvent();
		visibleData.clear();
		long dataStart = 0;
		long dataEnd = Long.MAX_VALUE;
		if (sel == SHOW_SELECTION) {
			ClickDataBlock cdb = clickControl.getClickDataBlock();
			dataStart = cdb.getCurrentViewDataStart();
			dataEnd = cdb.getCurrentViewDataEnd();
		}
		else if (sel == SHOW_FUTURE) {
			ClickDataBlock cdb = clickControl.getClickDataBlock();
			dataStart = cdb.getCurrentViewDataStart();
		}
		synchronized (offlineEventDataBlock.getSynchLock()) {
			int nEvents = offlineEventDataBlock.getUnitsCount();
			OfflineEventDataUnit anEvent;
			ListIterator<OfflineEventDataUnit> eventIter = offlineEventDataBlock.getListIterator(0);
			while (eventIter.hasNext()) {
				anEvent = eventIter.next();
				if (anEvent.getTimeMilliseconds() > dataEnd || anEvent.getEndTimeInMilliseconds() < dataStart) {
					continue;
				}
				if (spp != null && spp.getCode().equalsIgnoreCase(anEvent.getEventType()) == false) {
					continue;
				}
				visibleData.add(anEvent);
			}
		}
		//		for (int i = 0; i < nEvents; i++) {
		//			anEvent = offlineEventDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
		//			if (anEvent.getTimeMilliseconds() > dataEnd) {
		//				break;
		//			}
		//			if (first < 0) { // search for the first event
		//				if (anEvent.getTimeMilliseconds() < dataStart) {
		//					continue;
		//				}
		//				first = i;
		//				n++;
		//			}
		//			else {
		//				n++;
		//			}
		//		}
		//		}
	}

	private void makeMainPanel() {	
		mainPanel = new PamPanel(new BorderLayout());
		offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		eventTableModel = new EventTableModel();
		eventTable = new JTable(eventTableModel);
		eventTable.setRowSelectionAllowed(true);
		JScrollPane scrollPane = new JScrollPane(eventTable);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(700, 300));
		mainPanel.add(BorderLayout.CENTER, scrollPane);

		TableColumn tableCol;
		String toolTip;
		EventCellRenderer renderer;
		for (int i = 0; i < eventTableModel.getColumnCount(); i++) {
			tableCol = eventTable.getColumnModel().getColumn(i);
			tableCol.setPreferredWidth(eventTableModel.getRelativeWidth(i)*50);
			toolTip = eventTableModel.getToolTipText(i);
			if (toolTip != null) {
				renderer =
						new EventCellRenderer();
				renderer.setToolTipText(toolTip);
				tableCol.setCellRenderer(renderer);
			}
		}

		eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		tableData.addTableModelListener(moduleTable);
		setShowSelection(SHOW_SELECTION);
	}


	/**
	 * Call when an event has been added or removed in order to update the table. 
	 */
	public void tableDataChanged() {
		selectData();
		eventTableModel.fireTableDataChanged();
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return mainPanel;
	}

	public JTable getTable(){
		return eventTable;
	}

	/**
	 * Get a smaller panel of buttons allowing user to 
	 * select a sub set of data. 
	 * @return small panel. 
	 */
	public JPanel getSelectionPanel() {
		return selectionPanel;
	}

	public void addMouseListener(MouseListener mouseListener) {
		eventTable.addMouseListener(mouseListener);
	}

	public void addListSelectionListener(ListSelectionListener listSelectionListener) {
		eventTable.getSelectionModel().addListSelectionListener(listSelectionListener);
	}

	public void setSelectedEvent(OfflineEventDataUnit selectedEvent) {
		int iRow = findEventRow(selectedEvent);
		if (iRow < 0) {
			eventTable.clearSelection();
		}
		else {
			// see http://www.esus.com/docs/GetQuestionPage.jsp?uid=1295
			eventTable.setRowSelectionInterval(iRow, iRow);
			eventTable.scrollRectToVisible(
					new Rectangle(0, eventTable.getRowHeight()*(iRow),
							10, eventTable.getRowHeight()));
		}
	}

	public void setSelectedEvents(ArrayList<OfflineEventDataUnit> selectedEvent) {
		//		eventTable.getSelectionModel().setValueIsAdjusting(true);
		if (selectedEvent.size()==0) return; 
		int iRow=findEventRow(selectedEvent.get(0));
		if (iRow < 0) {
			eventTable.clearSelection();
		}
		else{
			for (int i=0; i<selectedEvent.size(); i++){
				iRow = findEventRow(selectedEvent.get(i));
				if (iRow<0) continue;
				// see http://www.esus.com/docs/GetQuestionPage.jsp?uid=1295
				eventTable.getSelectionModel().addSelectionInterval(iRow, iRow);
				eventTable.scrollRectToVisible(
						new Rectangle(0, eventTable.getRowHeight()*(iRow),
								10, eventTable.getRowHeight()));
			}
		}
		//		eventTable.getSelectionModel().setValueIsAdjusting(false);

	}

	/**
	 * Finds the row for a specific event. 
	 * @param event event to find
	 * @return row number, or -1 if event not found
	 */
	private int findEventRow(OfflineEventDataUnit event) {
		if (event == null) {
			return -1;
		}
		int nR = eventTableModel.getRowCount();
		for (int i = 0; i < nR; i++) {
			if (getSelectedEvent(i) == event) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @return the event for the currently selected row, or null if nothing selected
	 */
	public OfflineEventDataUnit getSelectedEvent() {
		int rowIndex = eventTable.getSelectedRow();
		return getSelectedEvent(rowIndex);
	}

	/**
	 * Find the event corresponding to the given row index
	 * @param rowIndex row index
	 * @return Event or null if rowIndex out of range. 
	 */
	public OfflineEventDataUnit getSelectedEvent(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= visibleData.size()) {
			return null;
		}
		else {
			return visibleData.get(rowIndex);
		}
	}

	private class ShowSelectionChanged implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			tableDataChanged();
		}
	}

	/**
	 * Class which makes it obvious when the table component is disabled. 
	 */
	class EventCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		public final Color background=new Color(235,235,235);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			if (isSelected) return super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
			final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (enabled) c.setBackground(row % 2 == 0 ? background : Color.WHITE);
			else c.setBackground(Color.LIGHT_GRAY);
			return c;
		}

	}

	class EventTableModel extends AbstractTableModel {

		//		int firstItem = 0;
		//		int nItems = 0;

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE,48,12,true,Color.BLACK,Color.BLUE);

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return ImageIcon.class;
			}
			return super.getColumnClass(col);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int iCol) {
			return colNames[iCol];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return super.isCellEditable(arg0, arg1);
		}

		@Override
		public int getRowCount() {
			return visibleData.size();
		}

		public int getRelativeWidth(int colIndex) {

			switch(colIndex) {
			case 0:
				return 1;
			case 1:
				return 1;
			case 2: 
				return 5;
			case 3:
				return 2;
			case 4:
				return 1;
			case 5:
				return 2;
			case 6:
				return 1;
			case 7:
				return 1;
			case 8:
				return 1;
			case 9:
				return 8;
			default:
				return 1;
			}
		}
		public String getToolTipText(int col) {
			switch(col) {
			case 0:
				return null;
			case 1:
				return "Event Number (Id in viewer database table)";
			case 2:
				return "Event start date and time";
			case 3:
				return "Event end time";
			case 4:
				return "Event type / species";
			case 5:
				return "Total number of clicks in event (number currently loaded in memory)";
			case 6:
				return "Minimum number of animals in event";
			case 7:
				return "Best estimate of number of animals in event";
			case 8:
				return "Maximum number of animals in event";
			case 9:
				return "Comment";
			default:
				return null;

			}
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			OfflineEventDataUnit edu = getSelectedEvent(rowIndex);
			if (edu == null) {
				return null;
			}
			String str;
			/*
			 * private String[] colNames = {"Start Time", "End Time", "Type", "N Clicks", "Comment"};
			 */
			switch(colIndex) {
			case 0:
				Color col = PamColors.getInstance().getWhaleColor(edu.getColourIndex());
				symbol.setFillColor(col);
				symbol.setLineColor(col);
				return symbol;
			case 1:
				return edu.getDatabaseIndex();
			case 2: // start time
				str  = PamCalendar.formatDBDateTime(edu.getTimeMilliseconds());
				if (edu.isSuspectEventTimes()) {
					str = "?"+ str + "?";
				}
				return str;
			case 3:
				str  = PamCalendar.formatTime(edu.getEventEndTime());
				if (edu.isSuspectEventTimes()) {
					str = "?"+ str + "?";
				}
				return str;
			case 4:
				return edu.getEventType();
			case 5:
				return String.format("%d (%d)", edu.getNClicks(), edu.getLoadedSubDetectionsCount());
			case 6:
				return edu.getMinNumber();
			case 7:
				return edu.getBestNumber();
			case 8:
				return edu.getMaxNumber();
			case 9:
				return edu.getComment();
			}
			return null;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
