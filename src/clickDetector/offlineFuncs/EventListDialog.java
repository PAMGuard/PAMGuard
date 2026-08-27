package clickDetector.offlineFuncs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import PamController.PamController;
import PamView.dialog.PamDialog;
import clickDetector.ClickControl;
import cpod.CPODClickTrainDataUnit;
import clickTrainDetector.CTDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.superdet.DetectionGroup;
import PamguardMVC.superdet.SuperDetection;
import PamguardMVC.superdet.swing.DetectionGroupTablePanel;

/**
 * Dialog listing the click detector's events plus, in additional tabs, any
 * other types of detection group in the configuration (click trains from the
 * click train detector module, the in-built click train detector, CPOD click
 * trains, detection groups, ...). All tabs offer "go to event"; the click
 * events tab retains the full editing functionality (edit / delete) and other
 * group types can be copied and converted into manually annotated events.
 */
public class EventListDialog extends PamDialog {
	
	private OfflineEventListPanel offlineEventListPanel;
	
	private static EventListDialog singleInstance;
	
	private ClickControl clickControl;
		
	private Window parentFrame;
	
	private OfflineEventDataBlock offlineEventDataBlock;

	private JPanel mainPanel;

	private JTabbedPane tabbedPane;

	/**
	 * Data blocks currently shown in the extra (non click event) tabs.
	 */
	private ArrayList<PamDataBlock> groupTabBlocks = new ArrayList<>();

	private ArrayList<DetectionGroupTablePanel> groupTablePanels = new ArrayList<>();
	
	protected EventListDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " Event List", false);
		this.parentFrame = parentFrame;
		this.clickControl = clickControl;
		offlineEventDataBlock = clickControl.getClickDetector().getOfflineEventDataBlock();
		offlineEventDataBlock.addObserver(new EventObserver());
		
		mainPanel = new JPanel(new BorderLayout());
		tabbedPane = new JTabbedPane();

		JPanel eventListPanel = new JPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		eventListPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.setBorder(new TitledBorder("Selection"));
		southPanel.add(BorderLayout.WEST, offlineEventListPanel.getSelectionPanel());
		eventListPanel.add(BorderLayout.SOUTH, southPanel);
		tabbedPane.addTab("Click events", eventListPanel);

		mainPanel.add(BorderLayout.CENTER, tabbedPane);
				
		offlineEventListPanel.addMouseListener(new TableMouse());
		offlineEventListPanel.addListSelectionListener(new ListSelection());

		createGroupTabs();
		
		setResizable(true);
		
		setDialogComponent(mainPanel);
		setModal(false);
	}
	
	public JPanel getMainPanel(){
		return mainPanel;
		
	}

	/**
	 * Find all the data blocks of detection groups (other than this click
	 * detector's own events) and make a tab for each.
	 */
	private void createGroupTabs() {
		ArrayList<PamDataBlock> blocks = PamController.getInstance().getDataBlocks(DetectionGroup.class, true);
		ArrayList<PamDataBlock> wantedBlocks = new ArrayList<>();
		ClicksOffline clicksOffline = clickControl.getClicksOffline();
		if (blocks != null && clicksOffline != null) {
			for (PamDataBlock block : blocks) {
				if (clicksOffline.canShowInEventList(block)) {
					wantedBlocks.add(block);
				}
			}
		}
		if (wantedBlocks.equals(groupTabBlocks)) {
			return; // no change.
		}
		// remove old tabs (all but the first).
		while (tabbedPane.getTabCount() > 1) {
			tabbedPane.removeTabAt(1);
		}
		groupTabBlocks = wantedBlocks;
		groupTablePanels.clear();
		for (PamDataBlock block : wantedBlocks) {
			DetectionGroupTablePanel tablePanel = new GroupTablePanel(block);
			groupTablePanels.add(tablePanel);
			tabbedPane.addTab(block.getDataName(), tablePanel.getPanel());
			tabbedPane.setToolTipTextAt(tabbedPane.getTabCount()-1, block.getLongDataName());
		}
	}

	/**
	 * Table panel for the extra tabs with goto and convert-to-event commands.
	 */
	private class GroupTablePanel extends DetectionGroupTablePanel implements DetectionGroupTablePanel.GroupTableCommands {

		public GroupTablePanel(PamDataBlock dataBlock) {
			super(dataBlock, null);
		}

		@Override
		public void gotoGroup(SuperDetection group, int secsBefore) {
			clickControl.gotoEvent(group, secsBefore);
		}

		@Override
		public boolean canConvertGroup() {
			return true;
		}

		@Override
		public void convertGroup(SuperDetection group) {
			ArrayList<SuperDetection> groups = new ArrayList<>();
			groups.add(group);
			clickControl.getClicksOffline().convertGroupsToEvent(null, groups);
		}

		@Override
		public String getGroupInfo(SuperDetection group) {
			if (group instanceof CPODClickTrainDataUnit) {
				return ((CPODClickTrainDataUnit) group).getStringInfo();
			}
			if (group instanceof CTDataUnit) {
				CTDataUnit ct = (CTDataUnit) group;
				String info = "";
				if (ct.getCTChi2() != null) {
					info = String.format("X² %3.1f", ct.getCTChi2());
				}
				int clsfd = ct.getClassificationIndex();
				if (clsfd >= 0 && clsfd < ct.getCtClassifications().size()) {
					info += " " + ct.getCtClassifications().get(clsfd).getSummaryString();
				}
				return info;
			}
			return "";
		}
	}
	
	

	public static void showDialog(Window parentFrame, ClickControl clickControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || 
				singleInstance.clickControl != clickControl) {
			singleInstance = new EventListDialog(parentFrame, clickControl);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
	}
	
	private void setParams() {
		createGroupTabs();
		offlineEventListPanel.tableDataChanged();
		for (DetectionGroupTablePanel tablePanel : groupTablePanels) {
			tablePanel.updateTableData();
		}
		offlineEventListPanel.setSelectedEvent(clickControl.getLatestOfflineEvent());
		enableControls();
	}

	private void enableControls() {
		
	}
	
	@Override
	public void cancelButtonPressed() {

	}

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

	}
	
	private class ListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			enableControls();
		}
	}
	
	private class TableMouse extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				mouseDoubleClick();
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

	public void mouseDoubleClick() {
		OfflineEventDataUnit event = offlineEventListPanel.getSelectedEvent();
		if (event != null) {
			editEvent(event);
		}
	}
	
	private void editEvent(OfflineEventDataUnit event) {
		if (event == null) {
			return;
		}
		OfflineEventDataUnit event2 = OfflineEventDialog.showDialog(getOwner(), clickControl, event);
		if (event2 != null) {
			// do we need to do anything else ?
			// should probably send round an update so the map, etc know. 
			offlineEventListPanel.tableDataChanged();
			offlineEventDataBlock.updatePamData(event, System.currentTimeMillis());
		}
	}

	public void showPopupMenu(MouseEvent me) {
		OfflineEventDataUnit event = offlineEventListPanel.getSelectedEvent();
		if (event == null) {
			return;
		}
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		int evNo = event.getDatabaseIndex();
		int[] beforeTimes = {0, 10, 60};
		for (int i = 0; i < beforeTimes.length; i++) {
			String title;
			if (beforeTimes[i] == 0) {
				title = String.format("Goto event %d ...", evNo);
			}
			else {
				title = String.format("Goto %ds before event %d ...", beforeTimes[i], evNo);
			}
			menuItem = new JMenuItem(title);
			menuItem.addActionListener(new GotoEvent(event, beforeTimes[i]));
			menu.add(menuItem);
		}
		menuItem = new JMenuItem(String.format("Edit event %d ...", evNo));
		menuItem.addActionListener(new EditEvent(event));
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem(String.format("Delete event %d ...", evNo));
		menuItem.addActionListener(new DeleteEvent(event));
		menu.add(menuItem);
		
		menu.show(me.getComponent(), me.getX(), me.getY());
	}
	
	private class EditEvent implements ActionListener {
		
		private OfflineEventDataUnit event;

		public EditEvent(OfflineEventDataUnit event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			editEvent(event);
		}
	}
	private class DeleteEvent implements ActionListener {
		
		private OfflineEventDataUnit event;

		public DeleteEvent(OfflineEventDataUnit event) {
			this.event = event;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			deleteEvent(event);
		}
	}
	
	private class GotoEvent implements ActionListener {
		
		private OfflineEventDataUnit event;
		private int beforeTime;

		public GotoEvent(OfflineEventDataUnit event, int beforeTime) {
			this.event = event;
			this.beforeTime = beforeTime;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			clickControl.gotoEvent(event, beforeTime);
		}
		
	}

	public void deleteEvent(OfflineEventDataUnit event) {
		String msg = String.format("Are you sure you want to delete event %d with %d clicks ?",
				event.getDatabaseIndex(), event.getNClicks());
		int ans = JOptionPane.showConfirmDialog(mainPanel, msg, "Warning", JOptionPane.YES_NO_OPTION);
		if (ans == JOptionPane.YES_OPTION) {
			clickControl.deleteEvent(event);
			offlineEventListPanel.tableDataChanged();
		}
	}
	
	private class EventObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Event list dialog";
		}

		@Override
		public void addData(PamObservable o, PamDataUnit arg) {
			offlineEventListPanel.tableDataChanged();
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			offlineEventListPanel.tableDataChanged();			
		}
		
	}

	public static void notifyModelChanged(int changeType) {
		if (singleInstance != null) {
			singleInstance.modelChanged(changeType);
		}
	}

	private void modelChanged(int changeType) {
		if (changeType == PamController.OFFLINE_DATA_LOADED) {
			offlineEventListPanel.tableDataChanged();
			for (DetectionGroupTablePanel tablePanel : groupTablePanels) {
				tablePanel.updateTableData();
			}
		}
		
	}
	
}
